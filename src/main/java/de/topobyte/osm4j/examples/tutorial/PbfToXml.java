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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.access.OsmOutputStream;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.pbf.access.PbfIterator;
import de.topobyte.osm4j.xml.output.OsmXmlOutputStream;

public class PbfToXml
{

	public static void main(String[] args) throws MalformedURLException,
			IOException, ParserConfigurationException, SAXException
	{
		// Open a file as input
		InputStream input = new FileInputStream("/tmp/bbox.pbf");

		// Create an iterator for PBF data
		OsmIterator iterator = new PbfIterator(input, true);

		// Create an output stream
		OsmOutputStream osmOutput = new OsmXmlOutputStream(System.out, true);

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
	}

}