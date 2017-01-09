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

import de.topobyte.osm4j.core.access.DefaultOsmHandler;
import de.topobyte.osm4j.core.access.OsmInputException;
import de.topobyte.osm4j.core.access.OsmReader;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.xml.dynsax.OsmXmlReader;

public class CountingElements3
{

	public static void main(String[] args) throws MalformedURLException,
			IOException, ParserConfigurationException, SAXException,
			OsmInputException
	{
		// Define a query to retrieve some data
		String query = "http://www.overpass-api.de/api/xapi?*[bbox="
				+ "13.465661,52.504055,13.469817,52.506204]";

		// Open a stream
		InputStream input = new URL(query).openStream();

		// Create a reader for XML data
		OsmReader reader = new OsmXmlReader(input, false);

		// Create our counter and set it as a handler for the reader
		Counter counter = new Counter();
		reader.setHandler(counter);

		// Let the reader parse the data
		reader.read();

		// Print the results
		System.out.println("nodes: " + counter.numNodes);
		System.out.println("ways: " + counter.numWays);
		System.out.println("relations: " + counter.numRelations);
	}

	private static class Counter extends DefaultOsmHandler
	{

		int numNodes = 0;
		int numWays = 0;
		int numRelations = 0;

		@Override
		public void handle(OsmNode node)
		{
			numNodes++;
		}

		@Override
		public void handle(OsmWay way)
		{
			numWays++;
		}

		@Override
		public void handle(OsmRelation relation)
		{
			numRelations++;
		}

	}

}
