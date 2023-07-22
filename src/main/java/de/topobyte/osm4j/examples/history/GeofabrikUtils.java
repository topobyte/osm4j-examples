// Copyright 2023 Sebastian Kuerten
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
import java.util.Properties;

import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.cookie.CookieStore;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.cookie.BasicClientCookie;

public class GeofabrikUtils
{

	public static CloseableHttpClient createClientWithCookie()
			throws IOException
	{
		String cookieData = null;

		try (InputStream input = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("secure.properties")) {
			Properties properties = new Properties();
			properties.load(input);
			cookieData = properties.getProperty("geofabrik.cookie");
		}

		if (cookieData == null) {
			System.out.println(
					"To access internal OSM files from Geofabrik, you need to add a cookie definition");
			System.out.println("to `src/main/resources/secure.properties`:");
			System.out.println();
			System.out
					.println("geofabrik.cookie=login|2018-06-15|ABC...0123==");
			System.out.println();
			System.out.println(
					"To determine what to add to that file, navigate to");
			System.out.println("https://osm-internal.download.geofabrik.de/");
			System.out.println(
					"then login to that page. Then when loading that page, inspect");
			System.out.println(
					"the headers the browser sends to the website in the developer");
			System.out.println(
					"tools by inspecting the request headers in the network tab.");
			System.out.println();
		}

		CookieStore cookieStore = new BasicCookieStore();
		BasicClientCookie cookie = new BasicClientCookie("gf_download_oauth",
				cookieData);
		cookie.setDomain("osm-internal.download.geofabrik.de");
		cookieStore.addCookie(cookie);

		CloseableHttpClient client = HttpClientBuilder.create()
				.setDefaultCookieStore(cookieStore).build();
		return client;
	}

}
