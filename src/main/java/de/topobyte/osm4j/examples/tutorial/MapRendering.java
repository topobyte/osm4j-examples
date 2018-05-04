// Copyright 2015 Sebastian Kuerten
//
// This file is part of osm4j.
//
// osm4j is free software: you can redistribute it and/or modify
// it under the terms of the GNU Lesser General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// osm4j is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License
// along with osm4j. If not, see <http://www.gnu.org/licenses/>.

package de.topobyte.osm4j.examples.tutorial;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPolygon;

import de.topobyte.adt.geo.BBox;
import de.topobyte.jgs.transform.CoordinateTransformer;
import de.topobyte.jts2awt.Jts2Awt;
import de.topobyte.mercator.image.MercatorImage;
import de.topobyte.osm4j.core.access.OsmInputException;
import de.topobyte.osm4j.core.access.OsmReader;
import de.topobyte.osm4j.core.dataset.InMemoryMapDataSet;
import de.topobyte.osm4j.core.dataset.MapDataSetLoader;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.model.util.OsmModelUtil;
import de.topobyte.osm4j.core.resolve.EntityFinder;
import de.topobyte.osm4j.core.resolve.EntityFinders;
import de.topobyte.osm4j.core.resolve.EntityNotFoundException;
import de.topobyte.osm4j.core.resolve.EntityNotFoundStrategy;
import de.topobyte.osm4j.geometry.RegionBuilder;
import de.topobyte.osm4j.geometry.RegionBuilderResult;
import de.topobyte.osm4j.geometry.WayBuilder;
import de.topobyte.osm4j.geometry.WayBuilderResult;
import de.topobyte.osm4j.xml.dynsax.OsmXmlReader;

public class MapRendering extends JPanel
{

	public static void main(String[] args) throws IOException, OsmInputException
	{
		// This is the region we would like to render
		BBox bbox = new BBox(13.45546, 52.51229, 13.46642, 52.50761);
		int width = 800;
		int height = 600;

		// Define a query to retrieve some data
		String queryTemplate = "http://overpass-api.de/api/interpreter?data=(node(%f,%f,%f,%f);<;>;);out;";
		String query = String.format(queryTemplate, bbox.getLat2(),
				bbox.getLon1(), bbox.getLat1(), bbox.getLon2());

		// Open a stream
		InputStream input = new URL(query).openStream();

		// Create a reader and read all data into a data set
		OsmReader reader = new OsmXmlReader(input, false);
		InMemoryMapDataSet data = MapDataSetLoader.read(reader, true, true,
				true);

		// The MercatorImage class knows how to transform input coordinates to
		// the selected region selected via bounding box
		MercatorImage mapImage = new MercatorImage(bbox, width, height);

		// Instantiate our class
		MapRendering panel = new MapRendering(bbox, mapImage, data);
		panel.setPreferredSize(new Dimension(width, height));

		// Setup a frame to show our panel
		JFrame frame = new JFrame("Map rendering");
		frame.setContentPane(panel);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}

	// Some fields that define the map colors and street line widths
	private Color cBackground = new Color(0xEEEEEE);
	private Color cBBox = Color.BLUE;;
	private Color cStreetForeground = Color.WHITE;
	private Color cStreetBackground = new Color(0xDDDDDD);
	private Color cStreetText = Color.BLACK;
	private Color cBuildings = new Color(0xFFC2C2);

	private int widthStreetBackground = 14;
	private int widthStreetForeground = 10;

	// This is a set of values for the 'highway' key of ways that we will render
	// as streets
	private Set<String> validHighways = new HashSet<>(
			Arrays.asList(new String[] { "primary", "secondary", "tertiary",
					"residential", "living_street" }));

	// This will be used to map geometry coordinates to screen coordinates
	private MercatorImage mercatorImage;

	// We need to keep the reference to the bounding box, so that we can create
	// a new MercatorImage if the size of our panel changes
	private BBox bbox;

	// The data set will be used as entity provider when building geometries
	private InMemoryMapDataSet data;

	// We build the geometries to be rendered during construction and store them
	// in these fields so that we don't have to recompute everything when
	// rendering.
	private List<Geometry> buildings = new ArrayList<>();
	private List<LineString> streets = new ArrayList<>();
	private Map<LineString, String> names = new HashMap<>();

	public MapRendering(BBox bbox, MercatorImage mercatorImage,
			InMemoryMapDataSet data)
	{
		this.bbox = bbox;
		this.mercatorImage = mercatorImage;
		this.data = data;

		// When the panel's size changes, define a new MercatorImage and trigger
		// a repaint on our panel
		addComponentListener(new ComponentAdapter() {

			@Override
			public void componentResized(ComponentEvent e)
			{
				refreshMercatorImage();
				repaint();
			}

		});

		buildRenderingData();
	}

	private void buildRenderingData()
	{
		// We create building geometries from relations and ways. Ways that are
		// part of multipolygon buildings may be tagged as buildings themselves,
		// however rendering them independently will fill the polygon holes they
		// are cutting out of the relations. Hence we store the ways found in
		// building relations to skip them later on when working on the ways.
		Set<OsmWay> buildingRelationWays = new HashSet<>();
		// We use this to find all way members of relations.
		EntityFinder wayFinder = EntityFinders.create(data,
				EntityNotFoundStrategy.IGNORE);

		// Collect buildings from relation areas...
		for (OsmRelation relation : data.getRelations().valueCollection()) {
			Map<String, String> tags = OsmModelUtil.getTagsAsMap(relation);
			if (tags.containsKey("building")) {
				MultiPolygon area = getPolygon(relation);
				if (area != null) {
					buildings.add(area);
				}
				try {
					wayFinder.findMemberWays(relation, buildingRelationWays);
				} catch (EntityNotFoundException e) {
					// cannot happen (IGNORE strategy)
				}
			}
		}
		// ... and also from way areas
		for (OsmWay way : data.getWays().valueCollection()) {
			if (buildingRelationWays.contains(way)) {
				continue;
			}
			Map<String, String> tags = OsmModelUtil.getTagsAsMap(way);
			if (tags.containsKey("building")) {
				MultiPolygon area = getPolygon(way);
				if (area != null) {
					buildings.add(area);
				}
			}
		}

		// Collect streets
		for (OsmWay way : data.getWays().valueCollection()) {
			Map<String, String> tags = OsmModelUtil.getTagsAsMap(way);

			String highway = tags.get("highway");
			if (highway == null) {
				continue;
			}

			Collection<LineString> paths = getLine(way);

			if (!validHighways.contains(highway)) {
				continue;
			}

			// Okay, this is a valid street
			for (LineString path : paths) {
				streets.add(path);
			}

			// If it has a name, store it for labeling
			String name = tags.get("name");
			if (name == null) {
				continue;
			}
			for (LineString path : paths) {
				names.put(path, name);
			}
		}
	}

	public void refreshMercatorImage()
	{
		mercatorImage = new MercatorImage(bbox, getWidth(), getHeight());
	}

	@Override
	protected void paintComponent(Graphics graphics)
	{
		super.paintComponent(graphics);
		Graphics2D g = (Graphics2D) graphics;
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		// Fill the background
		g.setColor(cBackground);
		g.fillRect(0, 0, getWidth(), getHeight());

		// First render buildings
		g.setColor(cBuildings);
		for (Geometry building : buildings) {
			Shape polygon = Jts2Awt.toShape(building, mercatorImage);
			g.fill(polygon);
		}

		// First pass of street rendering: outlines
		g.setColor(cStreetBackground);
		g.setStroke(new BasicStroke(widthStreetBackground,
				BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

		for (LineString street : streets) {
			Path2D path = Jts2Awt.getPath(street, mercatorImage);
			g.draw(path);
		}

		// Second pass of street rendering: foreground
		g.setColor(cStreetForeground);
		g.setStroke(new BasicStroke(widthStreetForeground,
				BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

		for (LineString street : streets) {
			Path2D path = Jts2Awt.getPath(street, mercatorImage);
			g.draw(path);
		}

		// Now add labels where possible
		g.setFont(g.getFont().deriveFont(12f));
		g.setColor(cStreetText);

		for (LineString street : streets) {
			String name = names.get(street);
			if (name == null) {
				continue;
			}
			paintStreetLabel(g, street, name, mercatorImage);
		}

		// Also draw a rectangle around the query bounding box
		Geometry queryBox = new GeometryFactory().toGeometry(bbox.toEnvelope());
		Shape shape = Jts2Awt.toShape(queryBox, mercatorImage);
		g.setColor(cBBox);
		g.setStroke(new BasicStroke(2));
		g.draw(shape);
	}

	private void paintStreetLabel(Graphics2D g, LineString street, String name,
			CoordinateTransformer t)
	{
		// We will need this to measure the length of street names
		FontMetrics metrics = g.getFontMetrics();

		// For each segment
		for (int i = 1; i < street.getNumPoints(); i++) {

			// Segment is from c to d (WGS84 coordinates)
			Coordinate c = street.getCoordinateN(i - 1);
			Coordinate d = street.getCoordinateN(i);

			// Map coordinates to screen coordinates
			double cx = t.getX(c.x);
			double cy = t.getY(c.y);
			double dx = t.getX(d.x);
			double dy = t.getY(d.y);

			// Determine the length of the segment on the screen
			double len = Math.sqrt((dx - cx) * (dx - cx) + (dy - cy)
					* (dy - cy));

			// And also the length of the rendered street name
			int textLength = metrics.stringWidth(name);

			// Render only if there is enough space
			if (len <= textLength) {
				continue;
			}

			// We're going to modify the Graphics2D's transformation object to
			// render the text rotated and positioned, so we need to backup the
			// current transform object
			AffineTransform backup = g.getTransform();

			// We center the text on the segment so we calculate the offset
			// depending on the actual length of the text
			double offset = (len - textLength) / 2;

			// Define how to render text using transformations
			g.translate(cx, cy);
			g.rotate(Math.atan2(dy - cy, dx - cx));
			g.translate(offset, 4);

			// Draw!
			g.drawString(name, 0, 0);

			// Undo our transformation
			g.setTransform(backup);
		}
	}

	private WayBuilder wayBuilder = new WayBuilder();
	private RegionBuilder regionBuilder = new RegionBuilder();

	private Collection<LineString> getLine(OsmWay way)
	{
		List<LineString> results = new ArrayList<>();
		try {
			WayBuilderResult lines = wayBuilder.build(way, data);
			results.addAll(lines.getLineStrings());
			if (lines.getLinearRing() != null) {
				results.add(lines.getLinearRing());
			}
		} catch (EntityNotFoundException e) {
			// ignore
		}
		return results;
	}

	private MultiPolygon getPolygon(OsmWay way)
	{
		try {
			RegionBuilderResult region = regionBuilder.build(way, data);
			return region.getMultiPolygon();
		} catch (EntityNotFoundException e) {
			return null;
		}
	}

	private MultiPolygon getPolygon(OsmRelation relation)
	{
		try {
			RegionBuilderResult region = regionBuilder.build(relation, data);
			return region.getMultiPolygon();
		} catch (EntityNotFoundException e) {
			return null;
		}
	}

}
