package org.ndexbio.rest.client;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.UUID;

import org.apache.commons.codec.binary.Base64;
import org.ndexbio.model.object.NdexObject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
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
	UUID _userUid = null;

	String _baseroute = null;

	public NdexRestClient(String username, String password, String route) {
		super();
		System.out.println("Starting init of NDExRestClient ");
		_baseroute = route;
		_username = username;
		_password = password;
		System.out.println("init of NDExRestClient with " + _baseroute + "  "
				+ _username + " " + password);
	}

	public NdexRestClient(String username, String password) {
		super();
		System.out.println("Starting init of NDExRestClient ");

		// Default to localhost, standard location for testing
		_baseroute = "http://localhost:8080/ndexbio-rest";
		_username = username;
		_password = password;
		System.out.println("init of NDExRestClient with " + _baseroute + "  "
				+ _username + " " + password);

	}

	private void addBasicAuth(HttpURLConnection con) {
		String credentials = _username + ":" + _password;
		String basicAuth = "Basic "
				+ new String(new Base64().encode(credentials.getBytes()));
		con.setRequestProperty("Authorization", basicAuth);
	}

	public void setCredential(String username, String password) {
		this._username = username;
		this._password = password;
	}

	/*
	 * GET
	 */

	public JsonNode get(final String route, final String query)
			throws JsonProcessingException, IOException {
		InputStream input = null;
		HttpURLConnection con = null;
		try {

			ObjectMapper mapper = new ObjectMapper();

			con = getReturningConnection(route, query);
			input = con.getInputStream();
			JsonNode result = null;
			if (null != input) {
				result = mapper.readTree(input);
				return result;
			} else {
				throw new IOException("failed to connect to ndex");
			}

		} finally {
			input.close();
			con.disconnect();
		}
	}

	public NdexObject getNdexObject(
			final String route, 
			final String query,
			final Class<? extends NdexObject> mappedClass)
			throws JsonProcessingException, IOException {
		InputStream input = null;
		HttpURLConnection con = null;
		try {

			ObjectMapper mapper = new ObjectMapper();

			con = getReturningConnection(route, query);
			input = con.getInputStream();
			if (null != input){
				return mapper.readValue(input, mappedClass);
			} else {
				throw new IOException("failed to connect to ndex");
			}

		} finally {
			if (null != input) input.close();
			con.disconnect();
		}
	}
	
	public List<? extends NdexObject> getNdexObjectList(
			final String route, 
			final String query,
			final Class<? extends NdexObject> mappedClass)
			throws JsonProcessingException, IOException {
		InputStream input = null;
		HttpURLConnection con = null;
		try {
			
			ObjectMapper mapper = new ObjectMapper();
			JavaType type = mapper.getTypeFactory().
					  constructCollectionType(List.class, mappedClass);

			con = getReturningConnection(route, query);
			input = con.getInputStream();
			if (null != input){
				return mapper.readValue(input, type);
			} else {
				throw new IOException("failed to connect to ndex");
			}

		} finally {
			if (null != input) input.close();
			con.disconnect();
		}
	}

	public HttpURLConnection getReturningConnection(final String route,
			final String query) throws IOException {
		URL request = new URL(_baseroute + route + query);

		System.out.println("GET (returning connection) URL = " + request);

		HttpURLConnection con = (HttpURLConnection) request.openConnection();
		addBasicAuth(con);
		return con;
	}

	/*
	 * PUT
	 */

	public JsonNode put(
			final String route, 
			final JsonNode putData)
			throws JsonProcessingException, IOException {
		InputStream input = null;
		HttpURLConnection con = null;
		try {

			ObjectMapper mapper = new ObjectMapper();

			con = putReturningConnection(route, putData);
			input = con.getInputStream();
			// TODO 401 error handling
			return mapper.readTree(input);

		} finally {
			if (null != input) input.close();
			con.disconnect();
		}
	}
	
	public NdexObject putNdexObject(
			final String route, 
			final JsonNode putData,
			final Class<? extends NdexObject>  mappedClass)
			throws JsonProcessingException, IOException {
		InputStream input = null;
		HttpURLConnection con = null;
		try {

			ObjectMapper mapper = new ObjectMapper();

			con = putReturningConnection(route, putData);
			input = con.getInputStream();
			if (null != input){
				return mapper.readValue(input, mappedClass);
			} else {
				throw new IOException("failed to connect to ndex");
			}

		} finally {
			if (null != input) input.close();
			con.disconnect();
		}
	}

	public HttpURLConnection putReturningConnection(final String route,
			final Object putData) throws JsonProcessingException, IOException {

		
		
		URL request = new URL(_baseroute + route);
		System.out.println("PUT (returning connection) URL = " + request);
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

		return con;
	}

	/*
	 * POST
	 */

	public JsonNode post(
			final String route, 
			final JsonNode postData)
			throws JsonProcessingException, IOException {
		
		InputStream input = null;
		HttpURLConnection con = null;
		try {

			ObjectMapper mapper = new ObjectMapper();

			con = postReturningConnection(route, postData);
			input = con.getInputStream();
			JsonNode result = null;
			if (null != input) {
				result = mapper.readTree(input);
				return result;
			} else {
				throw new IOException("failed to connect to ndex");
			}

		} finally {
			if (null != input) input.close();
			con.disconnect();
		}
	}
	
	public NdexObject postNdexObject(
			final String route, 
			final JsonNode postData,
			final Class<? extends NdexObject>  mappedClass)
			throws JsonProcessingException, IOException {
		InputStream input = null;
		HttpURLConnection con = null;
		try {

			ObjectMapper mapper = new ObjectMapper();

			con = postReturningConnection(route, postData);
			input = con.getInputStream();
			if (null != input){
				return mapper.readValue(input, mappedClass);
			} else {
				throw new IOException("failed to connect to ndex");
			}

		} finally {
			if (null != input) input.close();
			con.disconnect();
		}
	}
	
	public List <? extends NdexObject> postNdexObjectList(
			final String route, 
			final JsonNode postData,
			final Class<? extends NdexObject>  mappedClass)
			throws JsonProcessingException, IOException {
		InputStream input = null;
		HttpURLConnection con = null;
		try {

			ObjectMapper mapper = new ObjectMapper();
			JavaType type = mapper.getTypeFactory().
					  constructCollectionType(List.class, mappedClass);

			con = postReturningConnection(route, postData);
			input = con.getInputStream();
			if (null != input){
				return mapper.readValue(input, type);
			} else {
				throw new IOException("failed to connect to ndex");
			}

		} finally {
			if (null != input) input.close();
			con.disconnect();
		}
	}

	public HttpURLConnection postReturningConnection(String route,
			JsonNode postData) throws JsonProcessingException, IOException {

		URL request = new URL(_baseroute + route);
		System.out.println("POST (returning connection) URL = " + request);

		HttpURLConnection con = (HttpURLConnection) request.openConnection();

		String postDataString = postData.toString();

		con.setDoOutput(true);
		con.setDoInput(true);
		con.setInstanceFollowRedirects(false);
		con.setRequestMethod("POST");
		// con.setRequestProperty("Content-Type",
		// "application/x-www-form-urlencoded");
		con.setRequestProperty("Content-Type", "application/json");
		con.setRequestProperty("charset", "utf-8");
		con.setRequestProperty("Content-Length",
				"" + Integer.toString(postDataString.getBytes().length));
		con.setUseCaches(false);
		addBasicAuth(con);

		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes(postDataString);
		wr.flush();
		wr.close();

		return con;
	}


	/*
	 * DELETE
	 */

	public JsonNode delete(final String route) throws JsonProcessingException,
			IOException {
		InputStream input = null;
		HttpURLConnection con = null;
		ObjectMapper mapper = new ObjectMapper();

		URL request = new URL(_baseroute + route);
		try {

			con = (HttpURLConnection) request.openConnection();
			addBasicAuth(con);
			con.setDoOutput(true);
			con.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");
			con.setRequestMethod("DELETE");

			input = con.getInputStream();
			JsonNode result = null;
			if (null != input) {
				result = mapper.readTree(input);
				return result;
			} else {
				throw new IOException("failed to connect to ndex");
			}
	
		}

		finally {
			if (null != input) input.close();
			con.disconnect();
		}
	}

	/*
	 * Getters and Setters
	 */
	public String getUsername() {
		return _username;
	}

	public void setUsername(String _username) {
		this._username = _username;
	}

	public String getPassword() {
		return _password;
	}

	public void setPassword(String _password) {
		this._password = _password;
	}

	public String getBaseroute() {
		return _baseroute;
	}

	public void setBaseroute(String _baseroute) {
		this._baseroute = _baseroute;
	}

	public UUID getUserUid() {
		return _userUid;
	}

	public void setUserUid(UUID _userUid) {
		this._userUid = _userUid;
	}

}