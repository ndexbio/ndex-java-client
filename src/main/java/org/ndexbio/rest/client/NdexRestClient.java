/**
 * Copyright (c) 2013, 2016, The Regents of the University of California, The Cytoscape Consortium
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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.GZIPInputStream;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.ndexbio.model.errorcodes.ErrorCode;
import org.ndexbio.model.errorcodes.NDExError;
import org.ndexbio.model.exceptions.DuplicateObjectException;
import org.ndexbio.model.exceptions.ForbiddenOperationException;
import org.ndexbio.model.exceptions.InvalidNetworkException;
import org.ndexbio.model.exceptions.NdexException;
import org.ndexbio.model.exceptions.NetworkConcurrentModificationException;
import org.ndexbio.model.exceptions.ObjectNotFoundException;
import org.ndexbio.model.exceptions.UnauthorizedOperationException;
import org.ndexbio.model.object.SolrSearchResult;
import org.ndexbio.model.object.User;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Simple REST Client for NDEX web service.
 * 
 * 
 */
public class NdexRestClient {

	private static String clientVersion = "NDEx-Java/2.2.2";
	
	
	// for authorization
	String _username = null;
	String _password = null;
	UUID _userUid = null;

	String _baseroute = null;
	
	String authenticationURL = null;
//	String SAMLResponse = null;
	
	AuthenticationType authnType;
	
	private String userAgent;
	
	private String additionalUserAgent;
	
/*	private String getVersionString () {
		final Properties properties = new Properties();
		try {
			properties.load(this.getClass().getResourceAsStream("/project.properties"));
		} catch (IOException e) {
			return "NDEx-Java-x.x.x";
		}
		return (properties.getProperty("version"));
	} */
	
	/**
	 * 
	 * @param username	
	 * @param password
	 * @param hostName If the hostName is just a host name or IP address. The constructed client object will
	 * 	      point to the v2 end point of that server. If the hostName is a string starting with 'http://', the constructed 
	 * 		  client object will point to the REST API end point specified by hostName.
	 * @throws NdexException 
	 * @throws IOException 
	 * @throws JsonProcessingException 
	 */
	public NdexRestClient(String username, String password, String hostName) throws JsonProcessingException, IOException, NdexException {
		this(username, password,hostName,null);

		if ( username!=null && password!=null)
			signIn(username,password);
	}

	
	public NdexRestClient(String username, String password, String hostName, String userAgent) throws JsonProcessingException, IOException, NdexException {
		this(hostName);

		if ( userAgent !=null)
			setAdditionalUserAgent(userAgent);

		if ( username!=null && password!=null)
			signIn(username,password);
		
	}

	/**
	 * Create a client as an anonymous user.
	 * @param hostName

	 */
	public NdexRestClient(String hostName)  {
		
		if ( hostName.toLowerCase().startsWith("http://"))
			_baseroute = hostName;
		else
			_baseroute = "http://"+ hostName + "/v2";
		authnType = AuthenticationType.BASIC;
		this.authenticationURL = null;
		
		_username = null;
		_password = null;
		_userUid = null;
	//	 if (clientVersion == null) {
	//		 clientVersion = getVersionString();
	//	 }
		 userAgent = clientVersion;
		
	}

/*	
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
*/	
	private void setAuthorizationAndUserAgent(HttpURLConnection con) {
		if ((_username == null) || _username.isEmpty()) {
			// if User Name is null or empty, then treat this as anonymous
			// request (i.e., do not add "Authorization" header)
			return;
		}

		String authString = getAuthenticationString();

		con.setRequestProperty("Authorization", authString);
		con.setRequestProperty("User-Agent", userAgent);
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
			break;
	/*	case SAML:
			authString = "SAML " + new String(new Base64().encode(this.SAMLResponse.getBytes()));
			break; */
		default:
			break;
		}
		return authString;	
	}

	public void signIn(String username, String password) throws JsonProcessingException, IOException, NdexException  {
		this._username = username.trim();
		this._password = password.trim();
		
		if ( _username !=null && username.length()>0) {
			User currentUser;
				currentUser = getNdexObject("/user?valid=true", "", User.class);
				_userUid = currentUser.getExternalId();
		
		}
		
	}

	public void signOut() {
		this._password = null;
		this._username  =null;
		this._userUid = null;
	}

	/*
	 * GET
	 */
	
	/**
	 * Users are responsible to close the stream at the end.
	 * @param route
	 * @param userName
	 * @param password
	 * @param query
	 * @return
	 * @throws JsonProcessingException
	 * @throws IOException
	 * @throws NdexException
	 */
	protected InputStream getStream(final String route, final String query)
			throws JsonProcessingException, IOException, NdexException {
		HttpURLConnection con = null;

		con = getReturningConnection(route, query); //, _username, _password);
		try {

			if ((con.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED)
					|| (con.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND)
					|| (con.getResponseCode() == HttpURLConnection.HTTP_CONFLICT)
					|| (con.getResponseCode() == HttpURLConnection.HTTP_FORBIDDEN)
					|| (con.getResponseCode() == HttpURLConnection.HTTP_INTERNAL_ERROR)) {

				try (InputStream input = con.getErrorStream()) {

					if (null != input) {
						// server sent an Ndex-specific exception (i.e., exception defined in
						// org.ndexbio.rest.exceptions.mappers package of ndexbio-rest project).
						// Re-construct and re-throw this exception here on the client side.
						processNdexSpecificException(input, con.getResponseCode(), new ObjectMapper());
					}

					input.close();
					throw new IOException("failed to connect to ndex server at " + route);
				}
			}

			InputStream input = con.getInputStream();

			if (null != input) {
				if ("gzip".equalsIgnoreCase(con.getContentEncoding())) {
					input = new GZIPInputStream(input);
				}
				return input;
			}
			throw new NdexException("failed to connect to ndex server.");
		} catch (IOException e) {
			String s = e.getMessage();
			if (s.startsWith("Server returned HTTP response code: 401")) {
				throw new NdexException("User '" + getUsername() + "' unauthorized to access " + getBaseroute() + route
						+ "\nServer returned : " + e);
			}
			throw e;
		}

	}
	
	protected <T> T  getNdexObject(
			final String route, 
			final String query,
			final Class<T> mappedClass)
			throws JsonProcessingException, IOException, NdexException {
		HttpURLConnection con = null;

			ObjectMapper mapper = new ObjectMapper();

			con = getReturningConnection(route, query); //, _username, _password);
			try {

				if ((con.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED   ) ||
					(con.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND      ) ||
					(con.getResponseCode() == HttpURLConnection.HTTP_CONFLICT       ) ||
					(con.getResponseCode() == HttpURLConnection.HTTP_FORBIDDEN      ) ||	
					(con.getResponseCode() == HttpURLConnection.HTTP_INTERNAL_ERROR )) {				

					try (InputStream input = con.getErrorStream()) {
					 
						if (null != input) {
							// server sent an Ndex-specific exception (i.e., exception defined in 
							// org.ndexbio.rest.exceptions.mappers package of ndexbio-rest project).
							// Re-construct and re-throw this exception here on the client side.
							processNdexSpecificException(input, con.getResponseCode(), mapper);
						}
					}	
					throw new IOException("failed to connect to ndex");
				}				
				
				try (InputStream input = con.getInputStream()) {

					if (null != input){
						if ("gzip".equalsIgnoreCase(con.getContentEncoding()))  {
							InputStream input2 = new GZIPInputStream(input);
							return mapper.readValue(input2, mappedClass);
						}
						return mapper.readValue(input, mappedClass);
					}
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
		
	}

	protected <T> List<T> getNdexObjectList(
			final String route, 
			final String query,
			final Class<T> mappedClass)
			throws JsonProcessingException, IOException {
		HttpURLConnection con = null;
		try {
			
			ObjectMapper mapper = new ObjectMapper();
			JavaType type = mapper.getTypeFactory().
					  constructCollectionType(List.class, mappedClass);

			con = getReturningConnection(route, query);
			try (InputStream input= con.getInputStream()) {
				if (null != input){
					return mapper.readValue(input, type);
				}
			}
			throw new IOException("failed to connect to ndex");

		} finally {
			if ( con != null) con.disconnect();
		}
	}

	protected <T,V> Map<T, V> getHashMap(
			final String route, 
			final String query,
			final Class<T> keyClass,
			final Class<V> valueClass)
			throws JsonProcessingException, IOException {
		HttpURLConnection con = null;
		try {
			
			ObjectMapper mapper = new ObjectMapper();
			JavaType type = mapper.getTypeFactory().constructMapType(HashMap.class, keyClass, valueClass);

			con = getReturningConnection(route, query);
			try (InputStream input = con.getInputStream()) {
				if (null != input){
					return mapper.readValue(input, type);
				}
			}
			throw new IOException("failed to connect to ndex");

		} finally {
			if ( con != null) con.disconnect();
		}
	}

	private HttpURLConnection getReturningConnection(final String route,
			final String query) throws IOException {
		URL request = new URL(_baseroute + route + query);

		HttpURLConnection con = (HttpURLConnection) request.openConnection();
		setAuthorizationAndUserAgent(con);
		return con;
	}	

	/**
	 * Method need to be in uppercase.
	 * @param route
	 * @param in
	 * @param method
	 * @return
	 * @throws IOException
	 */
	private HttpURLConnection createReturningConnection(final String route, InputStream in, String method) throws IOException {
		URL request = new URL(_baseroute + route);

		HttpURLConnection con = (HttpURLConnection) request.openConnection();
		setAuthorizationAndUserAgent(con);
		con.setRequestMethod(method);
		con.setDoOutput(true);
		con.connect();

		IOUtils.copy(in,con.getOutputStream());
		con.getOutputStream().flush();
		con.getOutputStream().close();
		in.close();
		return con;
	}

	/*
	 * PUT
	 */
	
	protected void putNdexObject(
			final String route, 
			final JsonNode putData)
			throws IllegalStateException, Exception {
		HttpURLConnection con = null;
		try {

			con = putReturningConnection(route, putData);
			if ( con.getResponseCode() != HttpURLConnection.HTTP_NO_CONTENT && 
					 con.getResponseCode() != HttpURLConnection.HTTP_OK) {
				processNdexSpecificException(con.getInputStream(),con.getResponseCode(), new ObjectMapper());
			}

		} finally {
			if ( con != null) con.disconnect();
		}
	}
	
	private HttpURLConnection putReturningConnection(final String route,
			final Object putData) throws JsonProcessingException, IOException {

		URL request = new URL(_baseroute + route);
		ObjectMapper objectMapper = new ObjectMapper();
		HttpURLConnection con = (HttpURLConnection) request.openConnection();
		setAuthorizationAndUserAgent(con);

		con.setDoOutput(true);
		con.setRequestMethod("PUT");
		con.setRequestProperty("Content-Type", "application/json");
		con.setRequestProperty("Accept", "application/json");
		try (OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream())) {
			String putDataString = putData == null? "" : objectMapper.writeValueAsString(putData);
			out.write(putDataString);
			out.flush();
		}

		return con;
	}



	/*
	 * POST
	 */


	protected <T> T postNdexObject(
			final String route, 
			final JsonNode postData,
			final Class<T>  mappedClass)
			throws JsonProcessingException, IOException, NdexException {
		HttpURLConnection con = null;

		try {

			ObjectMapper mapper = new ObjectMapper();

			con = postReturningConnection(route, postData);
			
			if (null == con) {
				return null;
			}
			
			if ((con.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED   ) ||
				(con.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND      ) ||
				(con.getResponseCode() == HttpURLConnection.HTTP_CONFLICT       ) ||
				(con.getResponseCode() == HttpURLConnection.HTTP_FORBIDDEN      ) ||				
				(con.getResponseCode() == HttpURLConnection.HTTP_INTERNAL_ERROR )) {				

			    try (InputStream input = con.getErrorStream()) {
			 
				if (null != input) {
                    // server sent an Ndex-specific exception (i.e., exception defined in 
					// org.ndexbio.rest.exceptions.mappers package of ndexbio-rest project).
					// Re-construct and re-throw this exception here on the client side.
					processNdexSpecificException(input, con.getResponseCode(), mapper);
				}
				
				throw new IOException("failed to connect to ndex");
			    }
			}
	
			try (InputStream input = con.getInputStream() ) {
			if (null != input) {
				
				T val = mapper.readValue(input, mappedClass);
				
				return val;
			}
			throw new IOException("failed to connect to ndex");
			}

		} finally {
			if ( con != null ) con.disconnect();
		}
	}

	protected <T> SolrSearchResult<T> postSearchQuery(final String route, final JsonNode postData,
			final Class<T> mappedClass) throws JsonProcessingException, IOException, NdexException {
		HttpURLConnection con = null;

		try {

			ObjectMapper mapper = new ObjectMapper();

			con = postReturningConnection(route, postData);

			if (null == con) {
				return null;
			}

			if ((con.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED)
					|| (con.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND)
					|| (con.getResponseCode() == HttpURLConnection.HTTP_CONFLICT)
					|| (con.getResponseCode() == HttpURLConnection.HTTP_FORBIDDEN)
					|| (con.getResponseCode() == HttpURLConnection.HTTP_INTERNAL_ERROR)) {

				try (InputStream input = con.getErrorStream()) {

					if (null != input) {
						// server sent an Ndex-specific exception (i.e., exception defined in
						// org.ndexbio.rest.exceptions.mappers package of ndexbio-rest project).
						// Re-construct and re-throw this exception here on the client side.
						processNdexSpecificException(input, con.getResponseCode(), mapper);
					}

					throw new IOException("failed to connect to ndex");
				}
			}

			try (InputStream input = con.getInputStream()) {
				if (null != input) {
					JavaType type = mapper.getTypeFactory().constructParametricType(SolrSearchResult.class,
							mappedClass);

					SolrSearchResult<T> val = mapper.readValue(input, type);

					return val;

				}
				throw new IOException("failed to connect to ndex");
			}

		} finally {
			if (con != null)
				con.disconnect();
		}
	}

	protected UUID createNdexObjectByPost(final String route, final JsonNode postData)
			throws JsonProcessingException, IOException, NdexException {
		HttpURLConnection con = null;

		con = postReturningConnection(route, postData);

		if (null == con) {
			return null;
		}

		if ((con.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED)
				|| (con.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND)
				|| (con.getResponseCode() == HttpURLConnection.HTTP_CONFLICT)
				|| (con.getResponseCode() == HttpURLConnection.HTTP_FORBIDDEN)
				|| (con.getResponseCode() == HttpURLConnection.HTTP_INTERNAL_ERROR)) {

			try (InputStream input = con.getErrorStream()) {

				if (null != input) {
					// server sent an Ndex-specific exception (i.e., exception defined in
					// org.ndexbio.rest.exceptions.mappers package of ndexbio-rest project).
					// Re-construct and re-throw this exception here on the client side.
					processNdexSpecificException(input, con.getResponseCode(), new ObjectMapper());
				}

				throw new IOException("failed to connect to ndex");
			}
		}

		BufferedInputStream input = new BufferedInputStream(con.getInputStream());

		BufferedReader br = new BufferedReader(new InputStreamReader(input));
		StringBuffer sb = new StringBuffer();
		String inputLine = "";
		while ((inputLine = br.readLine()) != null) {
			sb.append(inputLine);
		}

		String newURL = sb.toString().trim();
		String uuidStr = newURL.substring(newURL.lastIndexOf("/") + 1);
		return UUID.fromString(uuidStr);
	}
	

	protected InputStream postNdexObject(
			final String route, 
			final JsonNode postData)
			throws JsonProcessingException, IOException, NdexException {
			
		    HttpURLConnection con = null;

			ObjectMapper mapper = new ObjectMapper();

			con = postReturningConnection(route, postData);
			
			if (null == con) {
				return null;
			}
			
			if ((con.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED   ) ||
				(con.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND      ) ||
				(con.getResponseCode() == HttpURLConnection.HTTP_CONFLICT       ) ||
				(con.getResponseCode() == HttpURLConnection.HTTP_FORBIDDEN      ) ||				
				(con.getResponseCode() == HttpURLConnection.HTTP_INTERNAL_ERROR )) {				

				try (InputStream input = con.getErrorStream() ) {
			 
				if (null != input) {
                    // server sent an Ndex-specific exception (i.e., exception defined in 
					// org.ndexbio.rest.exceptions.mappers package of ndexbio-rest project).
					// Re-construct and re-throw this exception here on the client side.
					processNdexSpecificException(input, con.getResponseCode(), mapper);
				}
				
				throw new IOException("failed to connect to ndex");
				}
			}
	
			InputStream input = con.getInputStream();
			if (null != input) {
				return input;
			}
			throw new IOException("failed to connect to ndex");

		
	}
	

	/**
	 * This function is for posting a large object to server. Expect server to return a URL.
	 * @param route
	 * @param in
	 * @return
	 * @throws IOException
	 * @throws NdexException 
	 */
	protected void putStream(final String route, InputStream in) throws IOException, NdexException {
		HttpURLConnection con = createReturningConnection(route, in,"PUT");
			
		if (null == con) {
			throw new NdexException ("Failed to create http connection.");
		}
		
		int returnCode =  con.getResponseCode();
		if (returnCode == HttpURLConnection.HTTP_NO_CONTENT)
			return ;

		if ((returnCode == HttpURLConnection.HTTP_UNAUTHORIZED   ) ||
			(returnCode == HttpURLConnection.HTTP_NOT_FOUND      ) ||
			(returnCode == HttpURLConnection.HTTP_CONFLICT       ) ||
			(returnCode == HttpURLConnection.HTTP_FORBIDDEN      ) ||				
			(returnCode == HttpURLConnection.HTTP_INTERNAL_ERROR )) {				

		    try (InputStream input = con.getErrorStream() ) {
		 
		    		if (null != input) {
		    			// server sent an Ndex-specific exception (i.e., exception defined in 
		    			// org.ndexbio.rest.exceptions.mappers package of ndexbio-rest project).
		    			// Re-construct and re-throw this exception here on the client side.
		    			processNdexSpecificException(input, con.getResponseCode(), new ObjectMapper());
		    		}
			
		    		throw new IOException("failed to connect to ndex");
		    }
		}
	}
	

	private HttpURLConnection postReturningConnection(String route,
			JsonNode postData) throws JsonProcessingException, IOException {

		URL request = new URL(_baseroute + route);

		HttpURLConnection con = (HttpURLConnection) request.openConnection();

		String postDataString = postData == null? "": postData.toString();
		
		con.setDoOutput(true);
		con.setDoInput(true);
		con.setInstanceFollowRedirects(false);
		con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        
		con.setUseCaches(false);
		setAuthorizationAndUserAgent(con);

		try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
			try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(wr, "UTF-8"))) {
				writer.write(postDataString);
				writer.flush();
			}
			return con;
		}
	}


	/*
	 * DELETE
	 */
	
	protected void delete(final String route) throws IOException, NdexException {
		HttpURLConnection con = null;

		URL request = new URL(_baseroute + route);

		con = (HttpURLConnection) request.openConnection();
		setAuthorizationAndUserAgent(con);
		con.setDoOutput(true);
		con.setRequestProperty("Content-Type", "application/json");
		con.setRequestMethod("DELETE");

		if (con.getResponseCode() != HttpURLConnection.HTTP_NO_CONTENT) {
			processNdexSpecificException(con.getInputStream(), con.getResponseCode(), new ObjectMapper());
		}

		if (con != null)
			con.disconnect();

	}

	/*
	 * Getters and Setters
	 */
	public String getUsername() {
		return _username;
	}

	public String getPassword() {
		return _password;
	}

	public String getBaseroute() {
		return _baseroute;
	}

	/*
	public void setBaseroute(String _baseroute) {
		this._baseroute = _baseroute;
	} */

	public UUID getUserUid() {
		return _userUid;
	}

	/*
	 * Re-construct and re-throw exception received from the server.  
	 */
	private static void processNdexSpecificException(
		InputStream input, int httpServerResponseCode, ObjectMapper mapper) 
		throws JsonProcessingException, IOException, NdexException {

		NDExError ndexError = null;
		
		try {
			ndexError = mapper.readValue(input, NDExError.class);
		} catch (Exception e) {
			throw e;
		}
		
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
		    	if (ndexError.getErrorCode() == ErrorCode.NDEx_Concurrent_Modification_Exception)
		    		throw new NetworkConcurrentModificationException(ndexError);
		    	if ( ndexError.getErrorCode() == ErrorCode.NDEx_Modify_Invalid_Network_Exception)
		    		throw new InvalidNetworkException(ndexError);

		    	// default case is: HTTP Status-Code 500: Internal Server Error.
		    	throw new NdexException(ndexError);
		}
	}

	public String getAdditionalUserAgent() {
		return additionalUserAgent;
	}
	
	public String getUserAgent() {
		return userAgent;
	}

	public void setAdditionalUserAgent(String additionalUserAgent) {
		this.additionalUserAgent = additionalUserAgent;
		this.userAgent = clientVersion + " " + additionalUserAgent; 
	}

	
	
	

	
	
	
}