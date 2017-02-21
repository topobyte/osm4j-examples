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
import java.util.Map;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;

import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.util.OsmModelUtil;
import de.topobyte.osm4j.pbf.seq.PbfIterator;

public class HistoryOfRestaurants
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
		DateTimeFormatter pattern = DateTimeFormat
				.forPattern("YYYY-MM-dd HH:mm");

		// Here we store information about the previous node so that we can
		// compute the difference between the current node and the previous one
		long previousId = 0;
		OsmNode previousNode = null;
		Map<String, String> previousTags = null;
		boolean wasRestaurantBefore = false;

		// Iterate all elements
		for (EntityContainer container : iterator) {

			// Check if the element is a node
			if (container.getType() == EntityType.Node) {

				// Cast the entity to OsmNode
				OsmNode node = (OsmNode) container.getEntity();
				long timestamp = node.getMetadata().getTimestamp();

				// This is an update, i.e. a later instance of the same node, if
				// the previous node's id and the current node's id are the same
				boolean isUpdate = previousId == node.getId();
				previousId = node.getId();

				// Here we store the values from variables named previous* to
				// last* so that we can overwrite the previous* variables and
				// still access their values using their last* counterparts.
				OsmNode lastNode = previousNode;
				previousNode = node;

				Map<String, String> currentTags = OsmModelUtil
						.getTagsAsMap(node);
				Map<String, String> lastTags = previousTags;
				previousTags = currentTags;

				// Determine if the current node is a restaurant, i.e. has tag
				// 'amenity=restaurant'
				boolean isRestaurant = isRestaurant(currentTags);

				// If this is not an update, we reset the wasRestaurant flag
				// which might still be true from a different restaurant
				// encountered before.
				if (!isUpdate) {
					wasRestaurantBefore = false;
				}

				// If the current node is a restaurant, we set the flag so that
				// in the next round we will now that an update to this node is
				// on a restaurant node.
				if (isRestaurant) {
					wasRestaurantBefore = true;
				}

				// If this is not a restaurant and also has not been one in the
				// past, skip
				if (!isRestaurant && !wasRestaurantBefore) {
					continue;
				}

				// Print an empty line between different nodes
				if (lastNode != null && !isUpdate) {
					System.out.println();
				}

				// Print node info if the restaurant in encountered the first
				// time, print changes on updates
				if (!isUpdate) {
					System.out.println(String.format(
							"id: %d, lat: %.6f, lon: %.6f, timestamp: %s",
							node.getId(), node.getLatitude(),
							node.getLongitude(), pattern.print(timestamp)));
					System.out.println(String.format("tags: %s",
							OsmModelUtil.getTagsAsList(node)));
				} else {
					System.out.println(String.format("upate on: %s",
							pattern.print(timestamp)));
					printChangesInPosition(node, lastNode);
					printChangesInTags(currentTags, lastTags);
				}
			}
		}
	}

	private static boolean isRestaurant(Map<String, String> currentTags)
	{
		String amenity = currentTags.get("amenity");
		if (amenity == null) {
			return false;
		}
		return amenity.equals("restaurant");
	}

	private static void printChangesInPosition(OsmNode node, OsmNode lastNode)
	{
		if (node.getLatitude() != lastNode.getLatitude()
				|| node.getLongitude() != lastNode.getLongitude()) {
			System.out.println(String.format("  position: lat: %.6f, lon: %.6f",
					node.getLatitude(), node.getLongitude()));
		}
	}

	private static void printChangesInTags(Map<String, String> current,
			Map<String, String> old)
	{
		SetView<String> onlyCurrent = Sets.difference(current.keySet(),
				old.keySet());
		SetView<String> onlyOld = Sets.difference(old.keySet(),
				current.keySet());
		SetView<String> both = Sets.intersection(current.keySet(),
				old.keySet());

		for (String key : onlyOld) {
			System.out.println(
					String.format("  rem tag '%s:%s'", key, old.get(key)));
		}

		for (String key : onlyCurrent) {
			System.out.println(
					String.format("  add tag '%s:%s'", key, current.get(key)));
		}

		for (String key : both) {
			if (!current.get(key).equals(old.get(key))) {
				System.out.println(
						String.format("  update tag '%s': '%s' -> '%s'", key,
								old.get(key), current.get(key)));
			}
		}
	}

}
