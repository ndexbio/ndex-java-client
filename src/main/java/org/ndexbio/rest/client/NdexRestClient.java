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
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Simple REST Client for NDEX web service.
 * 
 * 
 */
public class NdexRestClient {

	Logger logger = LoggerFactory.getLogger(NdexRestClient.class.getName());
    

	private static String clientVersion = "NDEx-Java/" + NdexRestClient.getVersion();
	
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
	
	private String idToken;
	
	private String rawUserAgent;
	
	/**
	 * Factory that creates {@java java.net.URL} objects
	 */
	private static HttpURLConnectionFactory _connectionFactory = new HttpURLConnectionFactory();
	
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
		
		if ( hostName.toLowerCase().startsWith("http://") || 
				hostName.toLowerCase().startsWith("https://")) {
			if ( hostName.toLowerCase().endsWith("/v2") ||
					hostName.toLowerCase().endsWith("/v3")) {
				_baseroute= hostName.substring(0, hostName.length()-2);
			} else
				_baseroute = hostName;
			
		} else
			_baseroute = "https://"+ hostName + "/";
		authnType = AuthenticationType.BASIC;
		this.authenticationURL = null;
		
		_username = null;
		_password = null;
		_userUid = null;
		idToken = null;

		rawUserAgent = "Java/"+System.getProperty("java.version") + " " + clientVersion;
		userAgent = rawUserAgent;
		
	}
	
	/**
	 * Sets alternate {@link org.ndexbio.rest.client.HttpURLConnectionFactory}
	 * to make testing easier
	 * @param connectionFactory
	 */
	protected static void setConnectionFactory(HttpURLConnectionFactory connectionFactory){
		_connectionFactory = connectionFactory;
	}
	
	protected static HttpURLConnectionFactory getConnectionFactory(){
		return _connectionFactory;
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
		con.setRequestProperty("User-Agent", userAgent);
		if ( this.authnType == AuthenticationType.BASIC && (_username == null || _username.isEmpty())) {
	
			// if User Name is null or empty, then treat this as anonymous
			// request (i.e., do not add "Authorization" header)
			return;
		}

		String authString = getAuthenticationString();

		con.setRequestProperty("Authorization", authString);
	}
	
	/**
	 * Gets authentication string that can be put in a header.
	 * Currently supports BASIC Auth and OAUTH
	 * 
	 * @return Either {@code Basic XXXXX or Bearer XXXXX} unless authentication
	 *         type is not supported in which case {@code null} is returned
	 */
	public String getAuthenticationString() {
		String authString = null;
		switch ( this.authnType ) {
		case BASIC:
			String credentials = _username + ":" + _password;
			authString = "Basic " + new String(new Base64().encode(credentials.getBytes()));
			break;
		case OAUTH:
			return "Bearer " + idToken;
	/*	case SAML:
			authString = "SAML " + new String(new Base64().encode(this.SAMLResponse.getBytes()));
			break; */
		default:
			break;
		}
		return authString;	
	}

	/**
	 * Use username and password to sign in to NDEx server from this client object. 
	 * @param username  
	 * @param password
	 * @throws JsonProcessingException
	 * @throws IOException
	 * @throws NdexException
	 */
	public void signIn(String username, String password) throws JsonProcessingException, IOException, NdexException  {
		this._username = username.trim();
		this._password = password.trim();
		
		if ( _username !=null && username.length()>0) {
			User currentUser;
			this.authnType = AuthenticationType.BASIC;		
				currentUser = getNdexObject(NdexApiVersion.v2 + "/user?valid=true", "", User.class);
				_userUid = currentUser.getExternalId();
		}
		
	}
	
	/**
	 * This seems to have been superseded by {@link #signIn(java.lang.String, java.lang.String) }
	 * @deprecated Use {@link #signIn(java.lang.String, java.lang.String) }
	 * @param username
	 * @param password
	 * @return
	 * @throws JsonProcessingException
	 * @throws IOException
	 * @throws NdexException
	 * @deprecated
	 */
	@Deprecated
	public User authenticateUser(String username, String password) throws JsonProcessingException, IOException, NdexException  {
		this._username = username.trim();
		this._password = password.trim();
		
		if ( username.length()>0) {
			User currentUser;
			this.authnType = AuthenticationType.BASIC;		
			currentUser = getNdexObject(NdexApiVersion.v2 + "/user?valid=true", "", User.class);
			this._userUid = currentUser.getExternalId();
			return currentUser;
		}
		
		return null;
	}
	
	
	/**
	 * Use a IDToken to sign in to NDEx server from this client. User need to make sure the IDToken is not expired. When this IDToken 
	 * is close to its expiration time. User can use a new IDToken to sign in again.
	 * @param IDToken
	 * @throws JsonProcessingException
	 * @throws IOException
	 * @throws NdexException
	 */
	public void signIn(String IDToken) throws JsonProcessingException, IOException, NdexException {
		this.authnType = AuthenticationType.OAUTH;
		this.idToken = IDToken;
		
		User currentUser;
		currentUser = getNdexObject(NdexApiVersion.v2 + "/user?valid=true", "", User.class);
		_userUid = currentUser.getExternalId();
	}

	public void signOut() {
		this._password = null;
		this._username  =null;
		this._userUid = null;
		this.idToken = null;
		this.authnType = AuthenticationType.BASIC;
	}

	
	public User getUserByUserName(String username) throws JsonProcessingException, IOException, NdexException {
		User u = getNdexObject(NdexApiVersion.v2 + "/user?username=" + username, "", User.class);
		return u;
	}
	
	public User getUserByEmail(String email) throws JsonProcessingException, IOException, NdexException {
		User u = getNdexObject(NdexApiVersion.v2 + "/user?email=" + email, "", User.class);
		return u;
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
						input.close();
					}

					throw new IOException("failed to connect to ndex server at " + route);
				}
			}

			return getInputStreamFromConnection(con);

			/*if (null != input) {
				if ("gzip".equalsIgnoreCase(con.getContentEncoding())) {
					input = new GZIPInputStream(input);
				}
				return input;
			}
			throw new NdexException("failed to connect to ndex server."); */
		} catch (IOException e) {
			String s = e.getMessage();
			if (s.startsWith("Server returned HTTP response code: 401")) {
				throw new NdexException("User '" + getUsername() + "' unauthorized to access " + getBaseroute() + route
						+ "\nServer returned : " + e);
			}
			throw e;
		}

	}
	
	private static InputStream getInputStreamFromConnection(HttpURLConnection conn) throws IOException, NdexException {
		InputStream input = conn.getInputStream();

		if (null != input) {
			if ("gzip".equalsIgnoreCase(conn.getContentEncoding())) {
				input = new GZIPInputStream(input);
			}
			return input;
		}
		throw new NdexException("Failed to connect to NDEx server. Can't get input stream from " + conn.getURL().getPath());
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
				
				try (InputStream input = getInputStreamFromConnection(con)) {
					return mapper.readValue(input, mappedClass);
				}
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
			throws JsonProcessingException, IOException, NdexException {
		HttpURLConnection con = null;
		try {
			
			ObjectMapper mapper = new ObjectMapper();
			JavaType type = mapper.getTypeFactory().
					  constructCollectionType(List.class, mappedClass);

			con = getReturningConnection(route, query);
			try (InputStream input= getInputStreamFromConnection( con) ){
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
			throws JsonProcessingException, IOException, NdexException {
		HttpURLConnection con = null;
		try {
			
			ObjectMapper mapper = new ObjectMapper();
			JavaType type = mapper.getTypeFactory().constructMapType(HashMap.class, keyClass, valueClass);

			con = getReturningConnection(route, query);
			try (InputStream input = getInputStreamFromConnection(con)) {
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
		HttpURLConnection con = _connectionFactory.getConnection(_baseroute + route + query);
		
		setAuthorizationAndUserAgent(con);
		con.setRequestProperty("Accept-Encoding", "gzip");

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
	protected HttpURLConnection createReturningConnection(final String route, InputStream in, String method) throws IOException {
		return createReturningConnection(route, in, method, null);
	}
	
	/**
	 * Method need to be in uppercase.
	 * @param route
	 * @param in
	 * @param method
	 * @return
	 * @throws IOException
	 */
	protected HttpURLConnection createReturningConnection(final String route, InputStream in, String method,
			final Map<String, String> requestProperties) throws IOException {
		HttpURLConnection con = _connectionFactory.getConnection(_baseroute + route);

		setAuthorizationAndUserAgent(con);
		if (requestProperties != null){
			for (String key : requestProperties.keySet()){
				con.setRequestProperty(key, requestProperties.get(key));
			}
		}
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

		HttpURLConnection con = _connectionFactory.getConnection(_baseroute + route);
		ObjectMapper objectMapper = new ObjectMapper();
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
	
			try (InputStream input = getInputStreamFromConnection( con) ) {
				T val = mapper.readValue(input, mappedClass);
				return val;
			}

		} finally {
			if ( con != null ) con.disconnect();
		}
	}

	
	protected <T> List<T> postNdexList(
			final String route, 
			final ArrayNode postData,
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
	
			try (InputStream input = getInputStreamFromConnection( con) ) {
				
				JavaType type = mapper.getTypeFactory().
						  constructCollectionType(List.class, mappedClass);

				List<T> val = mapper.readValue(input,  type);
				return val;
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

			try (InputStream input = getInputStreamFromConnection(con)) {
					JavaType type = mapper.getTypeFactory().constructParametricType(SolrSearchResult.class,
							mappedClass);

					SolrSearchResult<T> val = mapper.readValue(input, type);

					return val;
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

		BufferedInputStream input = new BufferedInputStream( getInputStreamFromConnection(con));

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
					processNdexSpecificException(input, con.getResponseCode(),  new ObjectMapper());
				}
				
				throw new IOException("failed to connect to ndex");
				}
			}
	
			return getInputStreamFromConnection(con);
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
		throw new NdexException("HTTP connection error. return code: " + returnCode);
	}
	

	private HttpURLConnection postReturningConnection(String route,
			JsonNode postData) throws JsonProcessingException, IOException {

		HttpURLConnection con = _connectionFactory.getConnection(_baseroute + route);

		String postDataString = postData == null? "": postData.toString();
		
		con.setDoOutput(true);
		con.setDoInput(true);
        con.setRequestProperty("Content-Type", "application/json; charset=utf-8");
		con.setRequestProperty("Accept-Encoding", "gzip");
		con.setInstanceFollowRedirects(false);
		con.setRequestMethod("POST");
        
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

		HttpURLConnection con = _connectionFactory.getConnection(_baseroute + route);

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
	public void processNdexSpecificException(
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
			
		this.userAgent = rawUserAgent +
				additionalUserAgent !=null?
				(" " + additionalUserAgent) : ""; 
		
	}
	
	/**
     * Reads /ndexjavaclient.properties for version information from 
	 * ndex.version property. 
	 * 
     * @return version if found otherwise {@code NA}
     */
    public static String getVersion(){

		try (InputStream is = NdexRestClient.class.getResourceAsStream("/ndexjavaclient.properties")){
			Properties props = new Properties();
			if (is != null){
				props.load(is);
			}
			return props.getProperty("ndex.version", "NA");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "NA";
	}
}