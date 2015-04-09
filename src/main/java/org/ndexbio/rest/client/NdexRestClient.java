package org.ndexbio.rest.client;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.UUID;

import org.apache.commons.codec.binary.Base64;
import org.ndexbio.model.exceptions.NdexException;
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
	
	String authenticationURL = null;
	String SAMLResponse = null;
	
	AuthenticationType authnType;

	public NdexRestClient(String username, String password, String route) {
		super();
		System.out.println("Starting init of NDExRestClient ");
		_baseroute = route;
		_username = username;
		_password = password;
		authnType = AuthenticationType.BASIC;
		this.authenticationURL = null;
		System.out.println("init of NDExRestClient with " + _baseroute + "  "
				+ _username + " " + password);
	}

	@Deprecated
	// Default to localhost, standard location for testing
	public NdexRestClient(String username, String password) {
		this(username,password, "http://localhost:8080/ndexbio-rest");
	}

	public NdexRestClient(String username, String password, String route, String authnURL,
			AuthenticationType authenType) throws Exception {
		this(username,password,route);
		
		this.authenticationURL = authnURL;
		this.authnType = authenType;
		if ( authnType == AuthenticationType.SAML)
			getSAMLResponse();
	}

	private void getSAMLResponse() throws Exception {
		String requestStr = NdexRestClientUtilities.encodeMessage(createSAMLRequest());
		String encodedUserName = NdexRestClientUtilities.encodeMessage(this._username);
		String encodedPassword = NdexRestClientUtilities.encodeMessage(this._password);
		
		HttpURLConnection con = postReturningConnectionString(this.authenticationURL,
				"SAMLRequest=" + requestStr + "&username=" + encodedUserName + "&password="
				+ encodedPassword);
		
		InputStream input = con.getInputStream();
		
		java.util.Scanner s = new java.util.Scanner(input).useDelimiter("\\A");
		this.SAMLResponse = s.hasNext() ? s.next() : "";
		
		s.close();
		input.close();
		// check errors in response here
		
		if ( SAMLResponse == null || SAMLResponse.length() <2 ) {
			throw new Exception("Failed to authenticate. No response received from IDP.");
		}
		
	}
	
	
	
	private static String createSAMLRequest() {
   	    String authnRequest = new String(NdexRestClientUtilities.SAMLRequestTemplate);
   	    authnRequest = authnRequest.replace("<AUTHN_ID>", NdexRestClientUtilities.createID());
   	    authnRequest = authnRequest.replace("<ISSUE_INSTANT>", NdexRestClientUtilities.getDateAndTime());
	    return authnRequest;
	}
	
/*	private void addBasicAuth(HttpURLConnection con) {
		String credentials = _username + ":" + _password;
		String basicAuth = "Basic "
				+ new String(new Base64().encode(credentials.getBytes()));
		con.setRequestProperty("Authorization", basicAuth);
	} */

	
	private void addAuthentication(HttpURLConnection con) {
		String authString = null;
		switch ( this.authnType ) {
		case BASIC:
			String credentials = _username + ":" + _password;
			authString = "Basic " + new String(new Base64().encode(credentials.getBytes()));
			break;
		case OAUTH:
			System.out.println("OAuth authentication is not implmented yet.");
			return;
			//break;
		case SAML:
			authString = "SAML " + new String(new Base64().encode(this.SAMLResponse.getBytes()));
			break;
		default:
			
			//break;
		}
		con.setRequestProperty("Authorization", authString);
		
			
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
			}
			throw new IOException("failed to connect to ndex");

		} finally {
			if ( input != null) input.close();
			if ( con != null) con.disconnect();
		}
	}

	public NdexObject getNdexObject(
			final String route, 
			final String query,
			final Class<? extends NdexObject> mappedClass)
			throws JsonProcessingException, IOException, NdexException {
		InputStream input = null;
		HttpURLConnection con = null;
		try {

			ObjectMapper mapper = new ObjectMapper();

			con = getReturningConnection(route, query);
			try {
				input = con.getInputStream();
				if (null != input){
					return mapper.readValue(input, mappedClass);
				}
				throw new NdexException("failed to connect to ndex server.");
			} catch (IOException e) {	
				String s = e.getMessage();
			   if ( s.startsWith("Server returned HTTP response code: 401"))
				  throw new NdexException ("Unautherized access to network.");
			   
			   throw e;
			}	
		} finally {
			if (null != input) input.close();
			if ( con != null ) con.disconnect();
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
			}
			throw new IOException("failed to connect to ndex");

		} finally {
			if (null != input) input.close();
			if ( con != null) con.disconnect();
		}
	}

	public HttpURLConnection getReturningConnection(final String route,
			final String query) throws IOException {
		URL request = new URL(_baseroute + route + query);

		System.out.println("GET (returning connection) URL = " + request);

		HttpURLConnection con = (HttpURLConnection) request.openConnection();
		addAuthentication(con);
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
			if ( con != null ) con.disconnect();
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
			}
			throw new IOException("failed to connect to ndex");

		} finally {
			if (null != input) input.close();
			if ( con != null) con.disconnect();
		}
	}

	public HttpURLConnection putReturningConnection(final String route,
			final Object putData) throws JsonProcessingException, IOException {

		
		
		URL request = new URL(_baseroute + route);
		System.out.println("PUT (returning connection) URL = " + request);
		ObjectMapper objectMapper = new ObjectMapper();
		HttpURLConnection con = (HttpURLConnection) request.openConnection();
		addAuthentication(con);

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
			}
			throw new IOException("failed to connect to ndex");

		} finally {
			if (null != input) input.close();
			if ( con != null) con.disconnect();
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
			}
			throw new IOException("failed to connect to ndex");

		} finally {
			if (null != input) input.close();
			if ( con != null ) con.disconnect();
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
			}
			throw new IOException("failed to connect to ndex");

		} finally {
			if (null != input) input.close();
			if ( con != null ) con.disconnect();
		}
	}

	public HttpURLConnection postReturningConnection(String route,
			JsonNode postData) throws JsonProcessingException, IOException {

		URL request = new URL(_baseroute + route);
		System.out.println("POST (returning connection) URL = " + request);

		HttpURLConnection con = (HttpURLConnection) request.openConnection();

		String postDataString = postData.toString();
		
//		System.out.println(postDataString);

		con.setDoOutput(true);
		con.setDoInput(true);
		con.setInstanceFollowRedirects(false);
		con.setRequestMethod("POST");
		// con.setRequestProperty("Content-Type",
		// "application/x-www-form-urlencoded");
		con.setRequestProperty("Content-Type", "application/json; charset=utf-8");
		
//		con.setRequestProperty("charset", "utf-8");
//		con.setRequestProperty("Content-Length",
//				"" + Integer.toString(postDataString.getBytes().length));
		con.setUseCaches(false);
		addAuthentication(con);

		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(wr, "UTF-8"));
		writer.write(postDataString);
		writer.flush();
		writer.close();
		wr.close();
		
		return con;
	}


	
	
	
	private HttpURLConnection postReturningConnectionString(String route,
			String postDataString) throws JsonProcessingException, IOException {

		URL request = new URL(route);
		System.out.println("POST (returning connection) URL = " + request);

		HttpURLConnection con = (HttpURLConnection) request.openConnection();
		
		con.setDoOutput(true);
		con.setDoInput(true);
		con.setInstanceFollowRedirects(false);
		con.setRequestMethod("POST");
		 con.setRequestProperty("Content-Type",
		 "application/x-www-form-urlencoded");
		
		con.setUseCaches(false);
//		addBasicAuth(con);

		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(wr, "UTF-8"));
		writer.write(postDataString);
		writer.flush();
		writer.close();
		wr.close();
		
		return con;
	}
	
	
	
	/*
	 * DELETE
	 * this method is deprecated;  delete() should be used instead.
	 */
	public JsonNode delete(final String route) throws JsonProcessingException,
			IOException {
		InputStream input = null;
		HttpURLConnection con = null;
		ObjectMapper mapper = new ObjectMapper();

		URL request = new URL(_baseroute + route);
		try {

			con = (HttpURLConnection) request.openConnection();
			addAuthentication(con);
			con.setDoOutput(true);
			con.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");
			con.setRequestMethod("DELETE");

			input = con.getInputStream();
			JsonNode result = null;
			if (null != input) {
				result = mapper.readTree(input);
				return result;
			}
			throw new IOException("failed to connect to ndex");
	
		}

		finally {
			if (null != input) input.close();
			if ( con != null) con.disconnect();
		}
	}
	
	
	/*
	 * DELETE.
	 * Delete the currently authenticated in user (self).
	 */
	public void delete() throws JsonProcessingException,IOException {
       InputStream input = null;
       HttpURLConnection con = null;

       URL request = new URL(_baseroute + "/user");
       try {
	       con = (HttpURLConnection) request.openConnection();
	       addAuthentication(con);
	       con.setDoOutput(true);
	       con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
	       con.setRequestMethod("DELETE");

	       input = con.getInputStream();
       }
       finally {
	       if (null != input) { 
	    	   input.close();
	       }
	       if (null != con) {
	    	   con.disconnect();
	       }
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