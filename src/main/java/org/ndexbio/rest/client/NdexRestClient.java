/**
 * Copyright (c) 2013, 2015, The Regents of the University of California, The Cytoscape Consortium
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package org.ndexbio.rest.client;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.zip.GZIPInputStream;

import org.apache.commons.codec.binary.Base64;
import org.ndexbio.model.errorcodes.NDExError;
import org.ndexbio.model.exceptions.DuplicateObjectException;
import org.ndexbio.model.exceptions.ForbiddenOperationException;
import org.ndexbio.model.exceptions.NdexException;
import org.ndexbio.model.exceptions.ObjectNotFoundException;
import org.ndexbio.model.exceptions.UnauthorizedOperationException;
import org.ndexbio.model.object.NdexObject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

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
		
		if ((_username == null) || (_username.trim()).isEmpty() ) {
			// if User Name is null or empty, then treat this as anonymous
			// request (i.e., do not add "Authorization" header)
			return;
		}
		
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
	
	private void addAuthentication(HttpURLConnection con, String userName, String password) {
		String authString = null;
		
		if ((userName == null) || (userName.trim()).isEmpty() ) {
			// if User Name is null or empty, then treat this as anonymous
			// request (i.e., do not add "Authorization" header)
			return;
		}
		
		switch ( this.authnType ) {
		case BASIC:
			String credentials = userName + ":" + password;
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
	private String getAuthenticationString() {
		String authString = null;
		switch ( this.authnType ) {
		case BASIC:
			String credentials = _username + ":" + _password;
			authString = "Basic " + new String(new Base64().encode(credentials.getBytes()));
			break;
		case OAUTH:
			System.out.println("OAuth authentication is not implmented yet.");
			//break;
		case SAML:
			authString = "SAML " + new String(new Base64().encode(this.SAMLResponse.getBytes()));
			break;
		default:
			break;
		}
		return authString;	
	}

	public void setCredentials(String username, String password) {
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
	public Object getNdexObject(
			final String route, 
			final String userName,
			final String password,
			final String query,
			final Class<? extends Object> mappedClass)
			throws JsonProcessingException, IOException, NdexException {
		InputStream input = null;
		HttpURLConnection con = null;
		try {

			ObjectMapper mapper = new ObjectMapper();

			con = getReturningConnection(route, query, userName, password);
			try {

				if ((con.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED   ) ||
					(con.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND      ) ||
					(con.getResponseCode() == HttpURLConnection.HTTP_CONFLICT       ) ||
					(con.getResponseCode() == HttpURLConnection.HTTP_FORBIDDEN      ) ||	
					(con.getResponseCode() == HttpURLConnection.HTTP_INTERNAL_ERROR )) {				

					input = con.getErrorStream();
					 
					if (null != input) {
		                // server sent an Ndex-specific exception (i.e., exception defined in 
						// org.ndexbio.rest.exceptions.mappers package of ndexbio-rest project).
						// Re-construct and re-throw this exception here on the client side.
						processNdexSpecificException(input, con.getResponseCode(), mapper);
					}
						
					throw new IOException("failed to connect to ndex");
				}				
				
				input = con.getInputStream();

				if (null != input){
					if ("gzip".equalsIgnoreCase(con.getContentEncoding()))  {
						input = new GZIPInputStream(input);
					}
					return mapper.readValue(input, mappedClass);
				}
				throw new NdexException("failed to connect to ndex server.");
			} catch (IOException e) {	
				String s = e.getMessage();
			    if ( s.startsWith("Server returned HTTP response code: 401")) {
				    throw new NdexException ("User '" + getUsername() + "' unauthorized to access " + 
			             getBaseroute() + route + 
			             "\nServer returned : " + e);
			    }
			    throw e;
			}	
		} finally {
			if (null != input) input.close();
			if ( con != null ) con.disconnect();
		}
	}
	public Object getNdexObject(
			final String route, 
			final String query,
			final Class<? extends Object> mappedClass)
			throws JsonProcessingException, IOException, NdexException {
		InputStream input = null;
		HttpURLConnection con = null;
		try {

			ObjectMapper mapper = new ObjectMapper();

			con = getReturningConnection(route, query);
			try {

				if ((con.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED   ) ||
					(con.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND      ) ||
					(con.getResponseCode() == HttpURLConnection.HTTP_CONFLICT       ) ||
					(con.getResponseCode() == HttpURLConnection.HTTP_FORBIDDEN      ) ||	
					(con.getResponseCode() == HttpURLConnection.HTTP_INTERNAL_ERROR )) {				

					input = con.getErrorStream();
					 
					if (null != input) {
		                // server sent an Ndex-specific exception (i.e., exception defined in 
						// org.ndexbio.rest.exceptions.mappers package of ndexbio-rest project).
						// Re-construct and re-throw this exception here on the client side.
						processNdexSpecificException(input, con.getResponseCode(), mapper);
					}
						
					throw new IOException("failed to connect to ndex");
				}				
				
				input = con.getInputStream();

				if (null != input){
					if ("gzip".equalsIgnoreCase(con.getContentEncoding()))  {
						input = new GZIPInputStream(input);
					}
					return mapper.readValue(input, mappedClass);
				}
				throw new NdexException("failed to connect to ndex server.");
			} catch (IOException e) {	
				String s = e.getMessage();
			    if ( s.startsWith("Server returned HTTP response code: 401")) {
				    throw new NdexException ("User '" + getUsername() + "' unauthorized to access " + 
			             getBaseroute() + route + 
			             "\nServer returned : " + e);
			    }
			    throw e;
			}	
		} finally {
			if (null != input) input.close();
			if ( con != null ) con.disconnect();
		}
	}
	
	public List<? extends Object> getNdexObjectList(
			final String route, 
			final String query,
			final Class<? extends Object> mappedClass)
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
		
		return getReturningConnection(route, query, this.getUsername(), this.getPassword());
	}

	public HttpURLConnection getReturningConnection(final String route,
			final String query, String userName, String password) throws IOException {
		URL request = new URL(_baseroute + route + query);

		//System.out.println("GET (returning connection) URL = " + request);

		HttpURLConnection con = (HttpURLConnection) request.openConnection();
		addAuthentication(con, userName, password);
		return con;
	}	
	
	
	private HttpURLConnection postReturningConnection(final String route, final String postData) throws IOException {
		URL request = new URL(_baseroute + route);

		HttpURLConnection con = (HttpURLConnection) request.openConnection();
		addAuthentication(con);
		con.setRequestMethod("POST");
		con.setDoOutput(true);
		con.connect();

		DataOutputStream output = new DataOutputStream(con.getOutputStream());
		output.writeBytes(postData);
		output.flush();
		output.close();
		
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
	
	public Object putNdexObject(
			final String route, 
			final JsonNode putData,
			final Class<? extends Object>  mappedClass)
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
		//System.out.println("PUT (returning connection) URL = " + request);
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

	/*
	 * Re-construct and re-throw exception received from the server.  
	 */
	private void processNdexSpecificException(
		InputStream input, int httpServerResponseCode, ObjectMapper mapper) 
		throws JsonProcessingException, IOException, NdexException {

		NDExError ndexError = mapper.readValue(input, NDExError.class);
		
		switch (httpServerResponseCode) {
            case (HttpURLConnection.HTTP_UNAUTHORIZED):
    	        // httpServerResponseCode is HTTP Status-Code 401: Unauthorized.
    	        throw new UnauthorizedOperationException(ndexError);
        
	        case (HttpURLConnection.HTTP_NOT_FOUND):
	    	    // httpServerResponseCode is HTTP Status-Code 404: Not Found.
	    	    throw new ObjectNotFoundException(ndexError);
	    
		    case (HttpURLConnection.HTTP_CONFLICT):
		    	// httpServerResponseCode is HTTP Status-Code 409: Conflict.
		    	throw new DuplicateObjectException(ndexError);
		    
		    case (HttpURLConnection.HTTP_FORBIDDEN):
		    	// httpServerResponseCode is HTTP Status-Code 403: Forbidden.
		    	throw new ForbiddenOperationException(ndexError);
		    
		    default:
		    	// default case is: HTTP Status-Code 500: Internal Server Error.
		    	throw new NdexException(ndexError);
		}
	}

	public Object postNdexObject(
			final String route, 
			final JsonNode postData,
			final Class<? extends Object>  mappedClass)
			throws JsonProcessingException, IOException, NdexException {
		InputStream input = null;
		HttpURLConnection con = null;

		try {

			ObjectMapper mapper = new ObjectMapper();

			con = postReturningConnection(route, postData);
			//System.out.println("Response code=" + con.getResponseCode() + "  response message=" + con.getResponseMessage());
			
			if (null == con) {
				return null;
			}
			
			if ((con.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED   ) ||
				(con.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND      ) ||
				(con.getResponseCode() == HttpURLConnection.HTTP_CONFLICT       ) ||
				(con.getResponseCode() == HttpURLConnection.HTTP_FORBIDDEN      ) ||				
				(con.getResponseCode() == HttpURLConnection.HTTP_INTERNAL_ERROR )) {				

			    input = con.getErrorStream();
			 
				if (null != input) {
                    // server sent an Ndex-specific exception (i.e., exception defined in 
					// org.ndexbio.rest.exceptions.mappers package of ndexbio-rest project).
					// Re-construct and re-throw this exception here on the client side.
					processNdexSpecificException(input, con.getResponseCode(), mapper);
				}
				
				throw new IOException("failed to connect to ndex");
			}
	
			input = con.getInputStream();
			if (null != input) {
				return mapper.readValue(input, mappedClass);
			}
			throw new IOException("failed to connect to ndex");

		} finally {
			if (null != input) input.close();
			if ( con != null ) con.disconnect();
		}
	}
	
	
	public int postString(
			final String route, 
			final String postData,
			final Class<? extends Object>  mappedClass)
			throws JsonProcessingException, IOException, NdexException {
		InputStream input = null;
		HttpURLConnection con = null;

		try {

			con = postReturningConnection(route, postData);
			
			if ((con.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED   ) ||
				(con.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND      ) ||
				(con.getResponseCode() == HttpURLConnection.HTTP_CONFLICT       ) ||
				(con.getResponseCode() == HttpURLConnection.HTTP_FORBIDDEN      ) ||	
				(con.getResponseCode() == HttpURLConnection.HTTP_INTERNAL_ERROR )) {				

			    input = con.getErrorStream();
			 
				if (null != input) {
					ObjectMapper mapper = new ObjectMapper();
                    // server sent an Ndex-specific exception (i.e., exception defined in 
					// org.ndexbio.rest.exceptions.mappers package of ndexbio-rest project).
					// Re-construct and re-throw this exception here on the client side.
					processNdexSpecificException(input, con.getResponseCode(), mapper);
				}
				
				throw new IOException("failed to connect to ndex");
			}
	
            return con.getResponseCode();  

		} finally {
			if (null != input) input.close();
			if ( con != null ) con.disconnect();
		}
	}
	
	
	public List <? extends Object> postNdexObjectList(
			final String route, 
			final JsonNode postData,
			final Class<? extends Object>  mappedClass)
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
		//System.out.println("POST (returning connection) URL = " + request);

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

	public void postNetworkAsMultipartObject(String route, String fileToUpload) throws JsonProcessingException, IOException {

		Preconditions.checkState(!Strings.isNullOrEmpty(fileToUpload), "No file name specified.");
		
		File uploadFile = new File(fileToUpload);
		
		String charset  = "UTF-8";
		
		Path p = Paths.get(fileToUpload);
		String fileNameForPostRequest = p.getFileName().toString(); // get the filename only; remove the path
		
		MultipartUtility multipart = new MultipartUtility(_baseroute + route, charset, getAuthenticationString());
		
        multipart.addFormJson("filename", fileNameForPostRequest);
	    multipart.addFilePart("fileUpload", uploadFile);

	    List<String> response = multipart.finish();
	    
	    if (null == response) 
	    	return;
	             
	    for (String line : response) {
	        System.out.println(line);
	    }
	}
	
	
	
	private HttpURLConnection postReturningConnectionString(String route,
			String postDataString) throws JsonProcessingException, IOException {

		URL request = new URL(route);
		//System.out.println("POST (returning connection) URL = " + request);

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
	public void delete(final String route) throws IOException {
        InputStream input = null;
        HttpURLConnection con = null;

        URL request = new URL(_baseroute + route);
        try {

	        con = (HttpURLConnection) request.openConnection();
	        addAuthentication(con);
	        con.setDoOutput(true);
	        con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
	        con.setRequestMethod("DELETE");

	        input = con.getInputStream();
        }
        finally {
	        if (null != input) input.close();
	        if (con != null) con.disconnect();
        }
    }	
	/*
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
	}*/
	

	
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