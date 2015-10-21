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

package de.topobyte.osm4j.examples;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.util.OsmModelUtil;
import de.topobyte.osm4j.xml.dynsax.OsmXmlIterator;

public class ListRestaurantsFromOverpassInBoundingBox
{

	public static void main(String[] args) throws ParserConfigurationException,
			SAXException, IOException
	{
		// Define a query to retrieve some data
		String query = "http://www.overpass-api.de/api/xapi?map?bbox="
				+ "13.465661,52.504055,13.469817,52.506204";

		// Open a stream
		InputStream input = new URL(query).openStream();

		// Create a reader for XML data
		OsmIterator iterator = new OsmXmlIterator(input, false);

		// Iterate contained entities
		for (EntityContainer container : iterator) {

			// Only use nodes
			if (container.getType() == EntityType.Node) {

				// Get the node from the container
				OsmNode node = (OsmNode) container.getEntity();

				// Convert the node's tags to a map
				Map<String, String> tags = OsmModelUtil.getTagsAsMap(node);

				// Get the value for the 'amenity' key
				String amenity = tags.get("amenity");

				// Check if this is a restaurant
				boolean isRestaurant = amenity != null
						&& amenity.equals("restaurant");

				// If yes, print name and coordinate
				if (isRestaurant) {
					System.out.println(String.format("%s: %f, %f",
							tags.get("name"),
							node.getLatitude(),
							node.getLongitude()
							));
				}
			}
		}
	}

}