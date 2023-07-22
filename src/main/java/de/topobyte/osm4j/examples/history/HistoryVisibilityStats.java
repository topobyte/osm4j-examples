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

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.HttpStatus;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmMetadata;
import de.topobyte.osm4j.pbf.seq.PbfIterator;

public class HistoryVisibilityStats
{

	public static void main(String[] args) throws IOException
	{
		// Define a URL to retrieve some data
		String url = "https://osm-internal.download.geofabrik.de/europe/germany/bremen-internal.osh.pbf";

		CloseableHttpClient client = GeofabrikUtils.createClientWithCookie();

		HttpClientContext context = HttpClientContext.create();
		HttpGet request = new HttpGet(url);

		client.execute(request, context, response -> {
			if (response.getCode() == HttpStatus.SC_OK) {
				InputStream input = response.getEntity().getContent();
				process(input);
			} else {
				System.out.println(
						"Accessing the history file was not successful");
			}
			return null;
		});
	}

	private static void process(InputStream input)
	{
		// Create an iterator for PBF data
		OsmIterator iterator = new PbfIterator(input, true);

		Multiset<EntityType> countVisible = HashMultiset.create();
		Multiset<EntityType> countInvisible = HashMultiset.create();

		// Iterate all elements
		for (EntityContainer container : iterator) {

			OsmMetadata metadata = container.getEntity().getMetadata();

			if (metadata.isVisible()) {
				countVisible.add(container.getType());
			} else {
				countInvisible.add(container.getType());
			}
		}

		// Print info about visibility statistics
		System.out.println(String.format("nodes: %d visible, %d invisible",
				countVisible.count(EntityType.Node),
				countInvisible.count(EntityType.Node)));
		System.out.println(String.format("ways: %d visible, %d invisible",
				countVisible.count(EntityType.Way),
				countInvisible.count(EntityType.Way)));
		System.out.println(String.format("relations: %d visible, %d invisible",
				countVisible.count(EntityType.Relation),
				countInvisible.count(EntityType.Relation)));
	}

}
