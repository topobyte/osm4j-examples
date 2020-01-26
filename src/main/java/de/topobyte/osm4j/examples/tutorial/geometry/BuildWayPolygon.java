// Copyright 2018 Sebastian Kuerten
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
import java.util.HashMap;
import java.util.Map;

import org.locationtech.jts.geom.Geometry;
import org.wololo.geojson.Feature;
import org.wololo.jts2geojson.GeoJSONWriter;

import com.slimjars.dist.gnu.trove.map.TLongObjectMap;

import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.dataset.InMemoryMapDataSet;
import de.topobyte.osm4j.core.dataset.MapDataSetLoader;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.model.util.OsmModelUtil;
import de.topobyte.osm4j.core.resolve.EntityNotFoundException;
import de.topobyte.osm4j.geometry.RegionBuilder;
import de.topobyte.osm4j.geometry.RegionBuilderResult;
import de.topobyte.osm4j.xml.dynsax.OsmXmlIterator;

public class BuildWayPolygon
{

	public static void main(String[] args)
			throws IOException, EntityNotFoundException
	{
		String query = "http://overpass-api.de/api/interpreter?data=(way(477546559);>;);out;";

		// Open a stream
		InputStream input = new URL(query).openStream();

		OsmIterator iterator = new OsmXmlIterator(input, false);
		InMemoryMapDataSet data = MapDataSetLoader.read(iterator, false, false,
				true);

		TLongObjectMap<OsmWay> ways = data.getWays();

		if (ways.isEmpty()) {
			System.out.println("No way found");
			return;
		}

		OsmWay way = ways.valueCollection().iterator().next();

		RegionBuilder rb = new RegionBuilder();
		RegionBuilderResult result = rb.build(way, data);
		Geometry polygon = result.getMultiPolygon();

		Map<String, String> tags = OsmModelUtil.getTagsAsMap(way);
		Map<String, Object> properties = new HashMap<>();
		for (String key : tags.keySet()) {
			properties.put(key, tags.get(key));
		}

		GeoJSONWriter writer = new GeoJSONWriter();
		org.wololo.geojson.Geometry g = writer.write(polygon);
		Feature feature = new Feature(g, properties);

		String json = feature.toString();

		System.out.println(GeoJsonHelper.prettyPrintFeature(json));
	}

}
