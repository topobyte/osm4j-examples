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

package de.topobyte.osm4j.examples.tutorial.geometry;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.wololo.geojson.Feature;
import org.wololo.geojson.FeatureCollection;
import org.wololo.jts2geojson.GeoJSONWriter;

import de.topobyte.adt.geo.BBox;
import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.dataset.InMemoryMapDataSet;
import de.topobyte.osm4j.core.dataset.MapDataSetLoader;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.util.OsmModelUtil;
import de.topobyte.osm4j.core.resolve.EntityNotFoundException;
import de.topobyte.osm4j.geometry.GeometryBuilder;
import de.topobyte.osm4j.geometry.RegionBuilder;
import de.topobyte.osm4j.geometry.RegionBuilderResult;
import de.topobyte.osm4j.tbo.access.TboIterator;
import de.topobyte.osm4j.xml.dynsax.OsmXmlIterator;

public class RestaurantDensityBerlin
{

	public static void main(String[] args) throws IOException
	{
		GeometryBuilder geometryBuilder = new GeometryBuilder();
		RegionBuilder regionBuilder = new RegionBuilder();

		// This is where we get the boroughs from
		String urlBoroughs = "http://osmtestdata.topobyte.de/Berlin.admin10.all.tbo";

		// This is where we will get the restaurants from
		String queryTemplate = "http://overpass-api.de/api/interpreter?data="
				+ "node[\"amenity\"=\"restaurant\"](%f,%f,%f,%f);out;";

		// Read boroughs
		InputStream input = new URL(urlBoroughs).openStream();
		OsmIterator iterator = new TboIterator(input, true, false);
		InMemoryMapDataSet boroughsData = MapDataSetLoader.read(iterator, false,
				false, true);
		input.close();

		// Build borough polygons and map their names
		List<MultiPolygon> boroughs = new ArrayList<>();
		Map<MultiPolygon, String> names = new HashMap<>();

		for (OsmRelation relation : boroughsData.getRelations()
				.valueCollection()) {
			Map<String, String> tags = OsmModelUtil.getTagsAsMap(relation);
			String name = tags.get("name");
			if (name == null) {
				continue;
			}
			try {
				RegionBuilderResult region = regionBuilder.build(relation,
						boroughsData);
				MultiPolygon polygon = region.getMultiPolygon();
				if (polygon.isEmpty()) {
					continue;
				}
				boroughs.add(polygon);
				names.put(polygon, name);
			} catch (EntityNotFoundException e) {
				System.out.println("Unable to build polygon: " + tags);
			}
		}

		// Calculate the bounding box for the data query
		Envelope env = boroughs.iterator().next().getEnvelopeInternal();
		for (MultiPolygon borough : boroughs) {
			env.expandToInclude(borough.getEnvelopeInternal());
		}
		BBox bbox = new BBox(env);

		// Define a query to retrieve some data
		String query = String.format(queryTemplate, bbox.getLat2(),
				bbox.getLon1(), bbox.getLat1(), bbox.getLon2());

		// Setup a map for counting values for each borough
		Map<MultiPolygon, Integer> counters = new HashMap<>();
		for (MultiPolygon borough : boroughs) {
			counters.put(borough, 0);
		}

		// Analyze nodes
		input = new URL(query).openStream();
		iterator = new OsmXmlIterator(input, true);
		for (EntityContainer entity : iterator) {
			if (entity.getType() != EntityType.Node) {
				break;
			}
			OsmNode node = (OsmNode) entity.getEntity();
			Map<String, String> tags = OsmModelUtil.getTagsAsMap(node);

			// This is not exactly necessary since the input will contain
			// restaurant nodes only anyway. Doing it anyway, your data soure
			// may be a different one.
			String amenity = tags.get("amenity");
			if (amenity == null || !amenity.equals("restaurant")) {
				continue;
			}

			// Determine which borough a restaurant is in, and increment the
			// counter
			Point point = geometryBuilder.build(node);
			for (MultiPolygon borough : boroughs) {
				if (borough.contains(point)) {
					counters.put(borough, counters.get(borough) + 1);
				}
			}
		}

		// Build density information
		Map<MultiPolygon, Double> densities = new HashMap<>();
		Map<MultiPolygon, Double> values = new HashMap<>();

		// Calculate density by dividing the counter by the borough's area
		for (MultiPolygon borough : boroughs) {
			int count = counters.get(borough);
			double density = count / borough.getArea();
			densities.put(borough, density);
		}

		// Determine a relative density scale from 0..1 depending on the minimum
		// and maximum density of all boroughs.
		// Then scale the relative density logarithmically to make it more
		// expressive.
		double min = Collections.min(densities.values());
		double max = Collections.max(densities.values());
		for (MultiPolygon borough : boroughs) {
			double density = densities.get(borough);
			double relative = (density - min) / (max - min);

			int exponent = 2;
			int upscale = (int) Math.pow(10, exponent) - 1;
			double value = Math.log10((1 + relative * upscale)) / exponent;
			values.put(borough, value);
		}

		// Build feature collection for output, assign colors ranging from
		// orange to blue
		Feature[] features = new Feature[boroughs.size()];

		for (int i = 0; i < boroughs.size(); i++) {
			MultiPolygon borough = boroughs.get(i);
			double value = values.get(borough);

			Map<String, Object> properties = new HashMap<>();
			properties.put("name", names.get(borough));
			properties.put("restaurants", counters.get(borough));
			properties.put("fill", color(value));
			properties.put("fill-opacity", "0.5");
			properties.put("stroke", "#555555");

			GeoJSONWriter writer = new GeoJSONWriter();
			org.wololo.geojson.Geometry g = writer.write(borough);
			features[i] = new Feature(g, properties);
		}

		FeatureCollection featureCollection = new FeatureCollection(features);

		String json = featureCollection.toString();

		System.out.println(GeoJsonHelper.prettyPrintFeatureCollection(json));
	}

	// Interpolate a color between color0 and color1. Parameter value is in
	// [0..1]. value = 0 maps to color0 and value = 1 maps to color1.
	// Interpolate linearly in between.
	private static String color(double value)
	{
		int color0 = 0xffa200;
		int color1 = 0x3829ff;

		int r0 = (color0 >> 16) & 0xFF;
		int g0 = (color0 >> 8) & 0xFF;
		int b0 = (color0) & 0xFF;

		int r1 = (color1 >> 16) & 0xFF;
		int g1 = (color1 >> 8) & 0xFF;
		int b1 = (color1) & 0xFF;

		int r = (int) Math.round((1 - value) * r0 + value * r1);
		int g = (int) Math.round((1 - value) * g0 + value * g1);
		int b = (int) Math.round((1 - value) * b0 + value * b1);

		return String.format("#%02x%02x%02x", r, g, b);
	}

}
