// Copyright 2017 Sebastian Kuerten
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

package de.topobyte.osm4j.examples.history;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmMetadata;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmTag;
import de.topobyte.osm4j.core.model.util.OsmModelUtil;
import de.topobyte.osm4j.pbf.seq.PbfIterator;

public class HistoryBasics
{

	public static void main(String[] args) throws IOException
	{
		// Define a URL to retrieve some data
		String url = "http://download.geofabrik.de/europe/germany/bremen.osh.pbf";

		// Open a stream
		InputStream input = new URL(url).openStream();

		// Create an iterator for PBF data
		OsmIterator iterator = new PbfIterator(input, true);

		// We will use this formatter to print timestamps
		DateTimeFormatter formatter = DateTimeFormat
				.forPattern("YYYY-MM-dd HH:mm");

		// Iterate all elements
		for (EntityContainer container : iterator) {

			// Check if the element is a node
			if (container.getType() == EntityType.Node) {

				// Cast the entity to OsmNode
				OsmNode node = (OsmNode) container.getEntity();
				OsmMetadata metadata = node.getMetadata();

				// Get some metadata info
				boolean visible = metadata.isVisible();
				long timestamp = metadata.getTimestamp();

				// Also get the tags as a list
				List<? extends OsmTag> tags = OsmModelUtil.getTagsAsList(node);

				// Print information
				System.out.println(String.format(
						"id: %d, lat: %.6f, lon: %.6f, visible: %b, timestamp: %s, tags: %s",
						node.getId(), node.getLatitude(), node.getLongitude(),
						visible, formatter.print(timestamp), tags));
			}
		}
	}

}
