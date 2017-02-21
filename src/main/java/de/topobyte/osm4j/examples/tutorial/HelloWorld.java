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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmTag;
import de.topobyte.osm4j.xml.dynsax.OsmXmlIterator;

public class HelloWorld
{

	public static void main(String[] args) throws IOException
	{
		// Define a query to retrieve some data
		String query = "http://osmtestdata.topobyte.de/big-ben.osm";

		// Open a stream
		InputStream input = new URL(query).openStream();

		// Create an iterator for XML data
		OsmIterator iterator = new OsmXmlIterator(input, false);

		// Iterate all elements
		for (EntityContainer container : iterator) {

			// Check if the element is a node
			if (container.getType() == EntityType.Node) {

				// Cast the entity to OsmNode
				OsmNode node = (OsmNode) container.getEntity();

				// Print basic information
				System.out.println("id: " + node.getId());
				System.out.println("latitude: " + node.getLatitude());
				System.out.println("longitude: " + node.getLongitude());

				// Also print all tags
				System.out.println("tags:");
				for (int i = 0; i < node.getNumberOfTags(); i++) {
					OsmTag tag = node.getTag(i);
					System.out.println(tag.getKey() + " = " + tag.getValue());
				}
			}
		}
	}

}
