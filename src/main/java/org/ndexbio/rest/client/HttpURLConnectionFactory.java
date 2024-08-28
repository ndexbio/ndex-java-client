package org.ndexbio.rest.client;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Creates new {@link java.net.HttpURLConnection objects.
 * This is done to make testing easier
 * 
 * @author churas
 */
public class HttpURLConnectionFactory {
	
	/**
	 * Invokes {@code new} on {@link java.net.URL#URL(java.lang.String) }
	 * passing {@code spec} as only parameter and then returns
	 * {@link java.net.URL#openConnection() } 
	 * 
	 * @param spec value to pass to {@link java.net.URL#URL(java.lang.String) }
	 * @return new {@link java.net.HttpURLConnection}
	 * @throws MalformedURLException if {@code spec} is invalid URL
	 * @throws IOException if there is an error opening the connection
	 */
	public HttpURLConnection getConnection(String spec) throws MalformedURLException, IOException {
		URL request = new URL(spec);
		return (HttpURLConnection)request.openConnection();
	}
}
