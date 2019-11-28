// Copyright 2019 Sebastian Kuerten
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

package de.topobyte.osm4j.examples.misc;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmEntity;
import de.topobyte.osm4j.core.model.util.OsmModelUtil;
import de.topobyte.osm4j.pbf.seq.PbfIterator;

public class ListRestaurantsInHavana
{

	public static void main(String[] args) throws IOException
	{
		Path path = Paths.get("/tmp/havana-latest.pbf");
		InputStream input = Files.newInputStream(path);
		PbfIterator iterator = new PbfIterator(input, false);

		int n = 0;
		int w = 0;
		int r = 0;
		int noName = 0;

		for (EntityContainer object : iterator) {
			OsmEntity entity = object.getEntity();
			Map<String, String> tags = OsmModelUtil.getTagsAsMap(entity);
			String amenity = tags.get("amenity");
			if ("restaurant".equals(amenity)) {
				String name = tags.get("name");
				if (name == null) {
					noName++;
				} else {
					System.out.println(name);
				}
				if (object.getType() == EntityType.Node) {
					n++;
				} else if (object.getType() == EntityType.Way) {
					w++;
				} else if (object.getType() == EntityType.Relation) {
					r++;
				}
			}
		}

		System.out.println("-Summary-");
		System.out.println(
				String.format("nodes: %d, ways: %d, relations: %d", n, w, r));
		System.out.println(String.format("no name: %d", noName));
	}

}
