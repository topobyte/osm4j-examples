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
import java.util.Map;

import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.util.OsmModelUtil;
import de.topobyte.osm4j.xml.dynsax.OsmXmlIterator;

public class CountingBusinesses
{

	public static void main(String[] args) throws IOException
	{
		// Define a query to retrieve some data
		String query = "http://www.overpass-api.de/api/xapi?*[bbox="
				+ "13.465661,52.504055,13.469817,52.506204]";

		// Open a stream
		InputStream input = new URL(query).openStream();

		// Create an iterator for XML data
		OsmIterator iterator = new OsmXmlIterator(input, false);

		// Initialize the counters
		int numRestaurants = 0;
		int numCafes = 0;
		int numPubs = 0;

		// Iterate objects, ignore non-node objects
		for (EntityContainer container : iterator) {
			if (container.getType() == EntityType.Node) {
				OsmNode node = (OsmNode) container.getEntity();

				// Get the node's tags as a map
				Map<String, String> tags = OsmModelUtil.getTagsAsMap(node);

				// Get the value for the 'amenity' key
				String amenity = tags.get("amenity");

				// Ignore nodes without such a tag
				if (amenity == null) {
					continue;
				}

				// Increment counters depending on the type
				if (amenity.equals("restaurant")) {
					numRestaurants++;
				} else if (amenity.equals("cafe")) {
					numCafes++;
				} else if (amenity.equals("pub")) {
					numPubs++;
				}
			}
		}

		// Print the results
		System.out.println("restaurants: " + numRestaurants);
		System.out.println("cafes: " + numCafes);
		System.out.println("pubs: " + numPubs);
	}

}
