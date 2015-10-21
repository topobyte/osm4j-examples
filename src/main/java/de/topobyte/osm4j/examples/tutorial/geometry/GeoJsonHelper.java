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
