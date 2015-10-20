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
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.xml.dynsax.OsmXmlIterator;

public class CountingElements2
{

	public static void main(String[] args) throws MalformedURLException,
			IOException, ParserConfigurationException, SAXException
	{
		// Define a query to retrieve some data
		String query = "http://www.overpass-api.de/api/xapi?map?bbox="
				+ "13.465661,52.504055,13.469817,52.506204";

		// Open a stream
		InputStream input = new URL(query).openStream();

		// Create an iterator for XML data
		OsmIterator iterator = new OsmXmlIterator(input, false);

		// Initialize some counters
		int numNodes = 0;
		int numWays = 0;
		int numRelations = 0;

		// Iterate elements and increment our counters
		// depending on the type of element
		while (iterator.hasNext()) {
			EntityContainer container = iterator.next();
			if (container.getType() == EntityType.Node) {
				numNodes++;
			} else if (container.getType() == EntityType.Way) {
				numWays++;
			} else if (container.getType() == EntityType.Relation) {
				numRelations++;
			}
		}

		// Print the results
		System.out.println("nodes: " + numNodes);
		System.out.println("ways: " + numWays);
		System.out.println("relations: " + numRelations);
	}

}
