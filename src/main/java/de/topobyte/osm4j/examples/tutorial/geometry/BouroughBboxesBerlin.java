// Copyright 2016 Sebastian Kuerten
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.MultiPolygon;

import de.topobyte.adt.geo.BBox;
import de.topobyte.osm4j.core.access.OsmInputException;
import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.dataset.InMemoryMapDataSet;
import de.topobyte.osm4j.core.dataset.MapDataSetLoader;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.util.OsmModelUtil;
import de.topobyte.osm4j.core.resolve.EntityNotFoundException;
import de.topobyte.osm4j.geometry.RegionBuilder;
import de.topobyte.osm4j.geometry.RegionBuilderResult;
import de.topobyte.osm4j.tbo.access.TboIterator;

public class BouroughBboxesBerlin
{

	public static void main(String[] args) throws OsmInputException, IOException
	{
		RegionBuilder regionBuilder = new RegionBuilder();

		// This is where we get the boroughs from
		String urlBoroughs = "http://osmtestdata.topobyte.de/Berlin.admin10.all.tbo";

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

		// Calculate the bounding box and print info
		for (MultiPolygon borough : boroughs) {
			String name = names.get(borough);
			Envelope envelope = borough.getEnvelopeInternal();
			BBox bbox = new BBox(envelope);
			System.out.println(String.format("%s: %s", name, bbox.toString()));
		}
	}

}
