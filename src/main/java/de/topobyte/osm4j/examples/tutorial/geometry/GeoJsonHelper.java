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

package de.topobyte.osm4j.examples.tutorial.geometry;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class GeoJsonHelper
{

	public static String prettyPrintFeature(String featureJson)
			throws JsonProcessingException, IOException
	{
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);

		JsonNode tree = mapper.readTree(featureJson);

		JsonNode type = tree.get("type");
		JsonNode geometry = tree.get("geometry");
		JsonNode properties = tree.get("properties");

		ObjectNode root = mapper.createObjectNode();
		if (type != null) {
			root.set("type", type);
		}
		if (properties != null) {
			root.set("properties", properties);
		}
		if (geometry != null) {
			root.set("geometry", geometry);
		}

		return mapper.writeValueAsString(root);
	}

	public static String prettyPrintFeatureCollection(String featureJson)
			throws JsonProcessingException, IOException
	{
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);

		JsonNode tree = mapper.readTree(featureJson);

		JsonNode type = tree.get("type");
		JsonNode features = tree.get("features");

		ObjectNode root = mapper.createObjectNode();
		if (type != null) {
			root.set("type", type);
		}
		if (features != null) {
			root.set("features", features);
		}

		return mapper.writeValueAsString(root);
	}

}
