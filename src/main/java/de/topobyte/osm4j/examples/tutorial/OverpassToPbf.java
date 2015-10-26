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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.access.OsmOutputStream;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.pbf.seq.PbfWriter;
import de.topobyte.osm4j.xml.dynsax.OsmXmlIterator;

public class OverpassToPbf
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
		OsmIterator iterator = new OsmXmlIterator(input, true);

		// Create an output stream
		OutputStream output = new FileOutputStream("/tmp/bbox.pbf");
		OsmOutputStream osmOutput = new PbfWriter(output, true);

		// Iterate objects and copy them to the output
		for (EntityContainer container : iterator) {
			switch (container.getType()) {
			default:
			case Node:
				osmOutput.write((OsmNode) container.getEntity());
				break;
			case Way:
				osmOutput.write((OsmWay) container.getEntity());
				break;
			case Relation:
				osmOutput.write((OsmRelation) container.getEntity());
				break;
			}
		}

		// Close output
		osmOutput.complete();
		output.close();
	}

}
