package org.ndexbio.rest;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;

import org.apache.commons.codec.binary.Base64;
import org.ndexbio.model.object.Network;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
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

		HttpURLConnection con = getReturningConnection(route, query);

		InputStream is = con.getInputStream();
		// TODO 401 error handling
		JsonNode result = mapper.readTree(is);

		is.close();
		return result;
	}
	
	public HttpURLConnection getReturningConnection(final String route, final String query) 
			throws IOException{
		URL request = new URL(_baseroute + route + query);
		
		System.out.println("GET (returning connection) URL = " + request);

		HttpURLConnection con = (HttpURLConnection) request.openConnection();
		addBasicAuth(con);
		return con;
	}
	
	public JsonNode put(final String route, final JsonNode putData)
			throws JsonProcessingException, IOException {

		ObjectMapper mapper = new ObjectMapper();

		URL request = new URL(_baseroute + route);

		HttpURLConnection con = (HttpURLConnection) request.openConnection();
		addBasicAuth(con);
		
		String putDataString = putData.toString();
		
		con.setDoOutput(true);
		con.setRequestMethod("PUT");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Accept", "application/json");
		OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());
		out.write(putDataString);
		out.flush();
		out.close();

		InputStream is = con.getInputStream();
		// TODO 401 error handling
		JsonNode result = mapper.readTree(is);
		

		is.close();
		return result;
	}
	
	public HttpURLConnection putReturningConnection(final String route, final Object putData)
			throws JsonProcessingException, IOException {

		URL request = new URL(_baseroute + route);
		ObjectMapper objectMapper = new ObjectMapper();
		HttpURLConnection con = (HttpURLConnection) request.openConnection();
		addBasicAuth(con);
		
		con.setDoOutput(true);
		con.setRequestMethod("PUT");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Accept", "application/json");
        OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());
        String putDataString = objectMapper.writeValueAsString(putData);
		out.write(putDataString);
		out.flush();
		out.close();
		//objectMapper.writeValue(con.getOutputStream(), putData);
		

		return con;
	}
	
	public JsonNode post(final String route, final JsonNode postData)
			throws JsonProcessingException, IOException {

		ObjectMapper mapper = new ObjectMapper();

		HttpURLConnection con = postReturningConnection(route, postData);
		InputStream inputStream = con.getInputStream();
		// TODO 401 error handling
		JsonNode result = mapper.readTree(inputStream);

		inputStream.close();
		con.disconnect();
		return result;
	}
	
	public HttpURLConnection postReturningConnection(String route, JsonNode postData)
			throws JsonProcessingException, IOException {
		
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
				
		return con;
	}
	
	public Collection<Network> post(String route, JsonNode searchParameters,
			TypeReference<Collection<Network>> typeReference) {
		// TODO Auto-generated method stub
		return null;
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