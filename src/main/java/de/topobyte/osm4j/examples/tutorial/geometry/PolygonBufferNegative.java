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

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPolygon;
import org.wololo.geojson.Feature;
import org.wololo.jts2geojson.GeoJSONWriter;

import com.slimjars.dist.gnu.trove.map.TLongObjectMap;

import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.dataset.InMemoryMapDataSet;
import de.topobyte.osm4j.core.dataset.MapDataSetLoader;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.resolve.EntityNotFoundException;
import de.topobyte.osm4j.geometry.RegionBuilder;
import de.topobyte.osm4j.geometry.RegionBuilderResult;
import de.topobyte.osm4j.xml.dynsax.OsmXmlIterator;

public class PolygonBufferNegative
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

		// Build the polygon from the relation
		RegionBuilder regionBuilder = new RegionBuilder();
		RegionBuilderResult region = regionBuilder.build(relation, data);
		MultiPolygon polygon = region.getMultiPolygon();

		// Create a buffer
		Geometry buffer = polygon.buffer(-0.005);

		// Combine the polygon and its buffer into a GeometryCollection
		GeometryCollection both = new GeometryFactory()
				.createGeometryCollection(new Geometry[] { polygon, buffer });

		// GeoJSON output
		Map<String, Object> properties = new HashMap<>();
		GeoJSONWriter writer = new GeoJSONWriter();
		org.wololo.geojson.Geometry g = writer.write(both);
		Feature feature = new Feature(g, properties);

		String json = feature.toString();
		System.out.println(GeoJsonHelper.prettyPrintFeature(json));
	}

}
