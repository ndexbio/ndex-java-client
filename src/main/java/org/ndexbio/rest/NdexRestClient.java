package org.ndexbio.rest;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.codec.binary.Base64;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Simple REST Client for NDEX web service.
 * 
 * TODO: support user authentication!
 * 
 */
public class NdexRestClient {

	// for authorization
	String _username = null;
	String _password = null;

	String _baseroute = null;

	public NdexRestClient(String username, String password, String route) {
		_baseroute = route;
		_username = username;
		_password = password;

	}

	public NdexRestClient(String username, String password) {
		// Default to localhost, standard location for testing
		_baseroute = "http://localhost:8080/ndexbio-rest";
		_username = username;
		_password = password;

	}
	
	private void addBasicAuth(HttpURLConnection con) {
		// String credentials = "dexterpratt:insecure";
		String credentials = _username + ":" + _password;
		String basicAuth = "Basic "
				+ new String(new Base64().encode(credentials.getBytes()));
		con.setRequestProperty("Authorization", basicAuth);
	}

	public void setCredential(String username, String password) {
		this._username = username;
		this._password = password;
	}

	/**
	 * 
	 * perform a GET to the NDEx Server
	 * 
	 * 
	 * @param query
	 * @return JsonNode
	 * @throws IOException
	 * @throws JsonProcessingException
	 */
	public JsonNode get(final String route, final String query)
			throws JsonProcessingException, IOException {

		ObjectMapper mapper = new ObjectMapper();

		URL request = new URL(_baseroute + route + query);
		
		System.out.println("URL = " + request);

		HttpURLConnection con = (HttpURLConnection) request.openConnection();
		addBasicAuth(con);

		InputStream is = con.getInputStream();
		// TODO 401 error handling
		JsonNode result = mapper.readTree(is);

		is.close();
		return result;
	}
	
	public JsonNode put(final String route, final String resourceContent)
			throws JsonProcessingException, IOException {

		ObjectMapper mapper = new ObjectMapper();

		URL request = new URL(_baseroute + route);

		HttpURLConnection con = (HttpURLConnection) request.openConnection();
		addBasicAuth(con);
		
		con.setDoOutput(true);
		con.setRequestMethod("PUT");
		OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());
		out.write(resourceContent);
		out.close();

		InputStream is = con.getInputStream();
		// TODO 401 error handling
		JsonNode result = mapper.readTree(is);
		

		is.close();
		return result;
	}
	
	public JsonNode post(final String route, final JsonNode postData)
			throws JsonProcessingException, IOException {

		ObjectMapper mapper = new ObjectMapper();

		URL request = new URL(_baseroute + route);

		HttpURLConnection con = (HttpURLConnection) request.openConnection();
		
		
		String postDataString = postData.toString();
		
		      
		con.setDoOutput(true);
		con.setDoInput(true);
		con.setInstanceFollowRedirects(false); 
		con.setRequestMethod("POST"); 
		//con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); 
		con.setRequestProperty("Content-Type", "application/json"); 
		con.setRequestProperty("charset", "utf-8");
		con.setRequestProperty("Content-Length", "" + Integer.toString(postDataString.getBytes().length));
		con.setUseCaches (false);
		addBasicAuth(con);

		DataOutputStream wr = new DataOutputStream(con.getOutputStream ());
		wr.writeBytes(postDataString);
		wr.flush();
		wr.close();
		

		InputStream is = con.getInputStream();
		
		// TODO 401 error handling
		JsonNode result = mapper.readTree(is);

		is.close();
		con.disconnect();
		return result;
	}
	
	public JsonNode delete(final String route)
			throws JsonProcessingException, IOException {

		ObjectMapper mapper = new ObjectMapper();

		URL request = new URL(_baseroute + route);

		HttpURLConnection con = (HttpURLConnection) request.openConnection();
		addBasicAuth(con);
		con.setDoOutput(true);
		con.setRequestProperty(
		    "Content-Type", "application/x-www-form-urlencoded" );
		con.setRequestMethod("DELETE");

		InputStream is = con.getInputStream();
		// TODO 401 error handling
		JsonNode result = mapper.readTree(is);

		is.close();
		return result;
	}
	




}