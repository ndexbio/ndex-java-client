/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.ndexbio.rest.client;

import static org.easymock.EasyMock.*;
import static org.junit.jupiter.api.Assertions.*;

import java.net.HttpURLConnection;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ndexbio.model.object.User;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayInputStream;
import java.util.Arrays;
import org.junit.jupiter.api.AfterEach;

public class NdexRestClientTest {

    private String _username = "testUser";
    private String _password = "testPassword";
    private String _hostName = "http://localhost/";
	private UUID _userUUID = UUID.randomUUID();
	private String _authorizationString = "Basic dGVzdFVzZXI6dGVzdFBhc3N3b3Jk";
	private String _userAgent = "Java/" + System.getProperty("java.version") + " NDEx-Java/" + NdexRestClient.getVersion();
    private HttpURLConnection mockConnection;
	private HttpURLConnectionFactory mockConnectionFactory;
	private HttpURLConnectionFactory originalConnectionFactory;

	
    @BeforeEach
    public void setUp() throws Exception {
		originalConnectionFactory = NdexRestClient.getConnectionFactory();
		mockConnectionFactory = createMock(HttpURLConnectionFactory.class);
		NdexRestClient.setConnectionFactory(mockConnectionFactory);
        mockConnection = createMock(HttpURLConnection.class);
		
		expect(mockConnectionFactory.getConnection(_hostName
				+ NdexApiVersion.v2 + "/user?valid=true")).andReturn(mockConnection).anyTimes();
		mockConnection.setRequestProperty("Authorization", _authorizationString);
		expectLastCall().anyTimes();
		mockConnection.setRequestProperty("Accept-Encoding", "gzip");
		expectLastCall().anyTimes();
		mockConnection.setRequestProperty(eq("User-Agent"), endsWith(_userAgent));
        expectLastCall().anyTimes();
		
		ObjectMapper mapper = new ObjectMapper();
        User mockUser = new User();
        mockUser.setExternalId(_userUUID);
        expect(mockConnection.getResponseCode()).andReturn(HttpURLConnection.HTTP_OK).anyTimes();
        expect(mockConnection.getInputStream()).andReturn(new ByteArrayInputStream(mapper.writer().writeValueAsBytes(mockUser))).once();
        expect(mockConnection.getContentEncoding()).andReturn("").once();
    }
	
	@AfterEach
	public void tearDown() throws Exception {
		NdexRestClient.setConnectionFactory(originalConnectionFactory);
	}

	@Test
	public void testConstructorWithOnlyHostNameParameterNoHttpPrefix(){
		NdexRestClient client = new NdexRestClient("foo");
		assertNull(client.getAdditionalUserAgent());
		assertNull(client.getPassword());
        assertNull(client.getUsername());
        assertNull(client.getUserUid());
		assertEquals("https://foo/", client.getBaseroute());
	}
	
	@Test
	public void testConstructorWithOnlyHostNameParameterHttpPrefix(){
		NdexRestClient client = new NdexRestClient("http://foo");
		assertNull(client.getAdditionalUserAgent());
		assertNull(client.getPassword());
        assertNull(client.getUsername());
        assertNull(client.getUserUid());
		assertEquals("http://foo", client.getBaseroute());
	}
	
	@Test
	public void testConstructorWithOnlyHostNameParameterHttpsPrefix(){
		NdexRestClient client = new NdexRestClient("https://foo");
		assertNull(client.getAdditionalUserAgent());
		assertNull(client.getPassword());
        assertNull(client.getUsername());
        assertNull(client.getUserUid());
		assertEquals("https://foo", client.getBaseroute());
	}
	
	@Test
	public void testConstructorWithOnlyHostNameParameterHttpPrefixAndVSuffix(){
		
		for (String hostname : Arrays.asList("http://foo/v2", "http://foo/V2",
				"http://foo/V3", "http://foo/v3")){
			NdexRestClient client = new NdexRestClient(hostname);
			assertNull(client.getAdditionalUserAgent());
			assertNull(client.getPassword());
			assertNull(client.getUsername());
			assertNull(client.getUserUid());
			assertEquals("http://foo/", client.getBaseroute());
		}
	}
	
	@Test
	public void testConstructorWithOnlyHostNameParameterHttpsPrefixAndVSuffix(){
		
		for (String hostname : Arrays.asList("https://foo/v2", "https://foo/V2",
				"https://foo/V3", "https://foo/v3")){
			NdexRestClient client = new NdexRestClient(hostname);
			assertNull(client.getAdditionalUserAgent());
			assertNull(client.getPassword());
			assertNull(client.getUsername());
			assertNull(client.getUserUid());
			assertEquals("https://foo/", client.getBaseroute());
		}
	}
	
    @Test
    public void testSignInWithValidCredentials() throws Exception {
		
        replay(mockConnection, mockConnectionFactory);
        NdexRestClient client = new NdexRestClient(_username, _password, _hostName);
		assertEquals(this._password, client.getPassword());
        assertEquals(this._username, client.getUsername());
        assertEquals(this._userUUID, client.getUserUid());
		assertEquals(_userUUID, client.getUserUid());
		assertEquals("http://localhost/", client.getBaseroute());
        verify(mockConnection, mockConnectionFactory);
    }
	
	@Test
    public void testSignInWithValidOAuth() throws Exception {
		// have to do it all again for mocks due to change with oauth here
		mockConnectionFactory = createMock(HttpURLConnectionFactory.class);
		NdexRestClient.setConnectionFactory(mockConnectionFactory);
        mockConnection = createMock(HttpURLConnection.class);
		
		expect(mockConnectionFactory.getConnection(_hostName
				+ NdexApiVersion.v2 + "/user?valid=true")).andReturn(mockConnection).anyTimes();
		mockConnection.setRequestProperty("Authorization", "Bearer 12345");
		expectLastCall().anyTimes();
		mockConnection.setRequestProperty("Accept-Encoding", "gzip");
		expectLastCall().anyTimes();
		mockConnection.setRequestProperty(eq("User-Agent"), endsWith(_userAgent));
        expectLastCall().anyTimes();
		
		ObjectMapper mapper = new ObjectMapper();
        User mockUser = new User();
        mockUser.setExternalId(_userUUID);
        expect(mockConnection.getResponseCode()).andReturn(HttpURLConnection.HTTP_OK).anyTimes();
        expect(mockConnection.getInputStream()).andReturn(new ByteArrayInputStream(mapper.writer().writeValueAsBytes(mockUser))).once();
        expect(mockConnection.getContentEncoding()).andReturn("").once();
		
        replay(mockConnection, mockConnectionFactory);
        NdexRestClient client = new NdexRestClient(_hostName);
		client.signIn("12345");
		assertEquals(null, client.getPassword());
        assertEquals(null, client.getUsername());
		assertEquals(_userUUID, client.getUserUid());
		assertEquals("http://localhost/", client.getBaseroute());
        verify(mockConnection, mockConnectionFactory);
    }
    
	@Test
	public void testGetUserByUserName() throws Exception {
		HttpURLConnection userMockConnection = createMock(HttpURLConnection.class);
		
		expect(mockConnectionFactory.getConnection(_hostName
				+ NdexApiVersion.v2 + "/user?username=someuser")).andReturn(userMockConnection).anyTimes();
		userMockConnection.setRequestProperty("Authorization", _authorizationString);
		expectLastCall().anyTimes();
		userMockConnection.setRequestProperty("Accept-Encoding", "gzip");
		expectLastCall().anyTimes();
		userMockConnection.setRequestProperty(eq("User-Agent"), endsWith(_userAgent));
        expectLastCall().anyTimes();
		
		ObjectMapper mapper = new ObjectMapper();
        User mockUser = new User();
        mockUser.setExternalId(_userUUID);
        expect(userMockConnection.getResponseCode()).andReturn(HttpURLConnection.HTTP_OK).anyTimes();
        expect(userMockConnection.getInputStream()).andReturn(new ByteArrayInputStream(mapper.writer().writeValueAsBytes(mockUser))).once();
        expect(userMockConnection.getContentEncoding()).andReturn("").once();
		replay(mockConnection, userMockConnection, mockConnectionFactory);
		NdexRestClient client = new NdexRestClient(_username, _password, _hostName);
        User resUser = client.getUserByUserName("someuser");
		assertEquals(_userUUID, resUser.getExternalId());
	}
	
	@Test
	public void testGetUserByEmail() throws Exception {
		HttpURLConnection userMockConnection = createMock(HttpURLConnection.class);
		
		expect(mockConnectionFactory.getConnection(_hostName
				+ NdexApiVersion.v2 + "/user?email=someuser@email.com")).andReturn(userMockConnection).anyTimes();
		userMockConnection.setRequestProperty("Authorization", _authorizationString);
		expectLastCall().anyTimes();
		userMockConnection.setRequestProperty("Accept-Encoding", "gzip");
		expectLastCall().anyTimes();
		userMockConnection.setRequestProperty(eq("User-Agent"), endsWith(_userAgent));
        expectLastCall().anyTimes();
		
		ObjectMapper mapper = new ObjectMapper();
        User mockUser = new User();
        mockUser.setExternalId(_userUUID);
        expect(userMockConnection.getResponseCode()).andReturn(HttpURLConnection.HTTP_OK).anyTimes();
        expect(userMockConnection.getInputStream()).andReturn(new ByteArrayInputStream(mapper.writer().writeValueAsBytes(mockUser))).once();
        expect(userMockConnection.getContentEncoding()).andReturn("").once();
		replay(mockConnection, userMockConnection, mockConnectionFactory);
		NdexRestClient client = new NdexRestClient(_username, _password, _hostName);
        User resUser = client.getUserByEmail("someuser@email.com");
		assertEquals(_userUUID, resUser.getExternalId());
	}
	
    @Test
    public void testSignOut() throws Exception {
		replay(mockConnection, mockConnectionFactory);
        NdexRestClient client = new NdexRestClient(_username, _password, _hostName);
        client.signOut();
        assertNull(client.getPassword());
        assertNull(client.getUsername());
        assertNull(client.getUserUid());
    }
}