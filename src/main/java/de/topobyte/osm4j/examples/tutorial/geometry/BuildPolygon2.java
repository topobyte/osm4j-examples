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
import java.util.HashMap;
import java.util.Map;

import org.wololo.geojson.Feature;
import org.wololo.jts2geojson.GeoJSONWriter;

import com.vividsolutions.jts.geom.Geometry;

import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.dataset.InMemoryMapDataSet;
import de.topobyte.osm4j.core.dataset.MapDataSetLoader;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.util.OsmModelUtil;
import de.topobyte.osm4j.core.resolve.EntityNotFoundException;
import de.topobyte.osm4j.geometry.GeometryBuilder;
import de.topobyte.osm4j.xml.dynsax.OsmXmlIterator;
import gnu.trove.map.TLongObjectMap;

public class BuildPolygon2
{

	public static void main(String[] args)
			throws IOException, EntityNotFoundException
	{
		// String query =
		// "http://overpass-api.de/api/interpreter?data=(rel(16566);>;);out;";
		String query = "http://osmtestdata.topobyte.de/relation-16566.osm";

		// Open a stream
		InputStream input = new URL(query).openStream();

		OsmIterator iterator = new OsmXmlIterator(input, false);
		InMemoryMapDataSet data = MapDataSetLoader.read(iterator, false, false,
				true);

		TLongObjectMap<OsmRelation> relations = data.getRelations();

		if (relations.isEmpty()) {
			System.out.println("No relation found");
			return;
		}

		OsmRelation relation = relations.valueCollection().iterator().next();

		Geometry polygon = new GeometryBuilder().build(relation, data);

		Map<String, String> tags = OsmModelUtil.getTagsAsMap(relation);
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
