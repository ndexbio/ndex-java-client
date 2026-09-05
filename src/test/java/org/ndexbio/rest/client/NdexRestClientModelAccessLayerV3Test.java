package org.ndexbio.rest.client;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.mock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.Test;
import org.ndexbio.model.object.FileSearchResult;
import org.ndexbio.model.object.FileVisibilityType;
import org.ndexbio.model.object.MoveNetworksRequest;
import org.ndexbio.model.object.NdexFolder;
import org.ndexbio.model.object.SimpleFileQuery;
import org.ndexbio.model.object.network.VisibilityType;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Covers the NDEx v3 folder, file-search and CX2 network methods, asserting the routes and
 * query strings they build and the objects they map back.
 */
public class NdexRestClientModelAccessLayerV3Test {

	private static final UUID NETWORK_ID = UUID.fromString("e2ae10b4-dfba-4a15-bdb1-91c2257e12ac");
	private static final UUID FOLDER_ID = UUID.fromString("11111111-2222-3333-4444-555555555555");

	// ---------- buildQuery ----------

	@Test
	public void buildQueryOmitsNullValues() {
		assertEquals("", NdexRestClientModelAccessLayer.buildQuery("visibility", null, "folderId", null));
		assertEquals("?visibility=PUBLIC",
				NdexRestClientModelAccessLayer.buildQuery("visibility", VisibilityType.PUBLIC, "folderId", null));
		assertEquals("?folderId=" + FOLDER_ID,
				NdexRestClientModelAccessLayer.buildQuery("visibility", null, "folderId", FOLDER_ID));
		assertEquals("?visibility=PRIVATE&folderId=" + FOLDER_ID,
				NdexRestClientModelAccessLayer.buildQuery("visibility", VisibilityType.PRIVATE, "folderId", FOLDER_ID));
	}

	@Test(expected = IllegalArgumentException.class)
	public void buildQueryRejectsOddArgumentCount() {
		NdexRestClientModelAccessLayer.buildQuery("visibility");
	}

	// ---------- getMyFolders ----------

	@Test
	public void getMyFoldersUsesV3RouteWithLimit() throws Exception {
		NdexRestClient client = mock(NdexRestClient.class);
		NdexFolder folder = new NdexFolder();
		folder.setName("My Project");
		List<NdexFolder> folders = Collections.singletonList(folder);
		expect(client.getNdexObjectList("v3/files/folders?limit=50", "", NdexFolder.class)).andReturn(folders);
		replay(client);

		List<NdexFolder> result = new NdexRestClientModelAccessLayer(client).getMyFolders(50);

		assertSame(folders, result);
		assertEquals("My Project", result.get(0).getName());
		verify(client);
	}

	// ---------- searchFiles ----------

	@Test
	public void searchFilesOmitsVisibilityWhenNull() throws Exception {
		NdexRestClient client = mock(NdexRestClient.class);
		FileSearchResult expected = new FileSearchResult(0L, 0L, Collections.emptyList());
		expect(client.postNdexObject(eq("v3/search/files?start=0&size=100"), anyObject(JsonNode.class),
				eq(FileSearchResult.class))).andReturn(expected);
		replay(client);

		FileSearchResult result = new NdexRestClientModelAccessLayer(client).searchFiles(null, null, 0, 100);

		assertSame(expected, result);
		verify(client);
	}

	@Test
	public void searchFilesAppendsVisibilityAndSerializesQuery() throws Exception {
		NdexRestClient client = mock(NdexRestClient.class);
		Capture<JsonNode> body = EasyMock.newCapture();
		FileSearchResult expected = new FileSearchResult(3L, 10L, Collections.emptyList());
		expect(client.postNdexObject(eq("v3/search/files?start=10&size=25&visibility=PRIVATE"),
				EasyMock.capture(body), eq(FileSearchResult.class))).andReturn(expected);
		replay(client);

		SimpleFileQuery query = new SimpleFileQuery();
		query.setSearchString("cancer signaling");
		query.setAccountName("alice");
		FileSearchResult result = new NdexRestClientModelAccessLayer(client)
				.searchFiles(query, FileVisibilityType.PRIVATE, 10, 25);

		assertEquals(3L, result.getNumFound());
		assertEquals("cancer signaling", body.getValue().get("searchString").asText());
		assertEquals("alice", body.getValue().get("accountName").asText());
		verify(client);
	}

	@Test
	public void searchFilesAcceptsPublicVisibility() throws Exception {
		NdexRestClient client = mock(NdexRestClient.class);
		expect(client.postNdexObject(eq("v3/search/files?start=0&size=5&visibility=PUBLIC"),
				anyObject(JsonNode.class), eq(FileSearchResult.class)))
				.andReturn(new FileSearchResult(0L, 0L, Collections.emptyList()));
		replay(client);

		new NdexRestClientModelAccessLayer(client).searchFiles(new SimpleFileQuery(), FileVisibilityType.PUBLIC, 0, 5);

		verify(client);
	}

	// ---------- moveNetworks ----------

	@Test
	public void moveNetworksPostsBatchRoute() throws Exception {
		NdexRestClient client = mock(NdexRestClient.class);
		Capture<JsonNode> body = EasyMock.newCapture();
		client.postNdexObjectNoContent(eq("v3/batch/networks/move"), EasyMock.capture(body));
		expectLastCall();
		replay(client);

		MoveNetworksRequest request = new MoveNetworksRequest();
		request.setTargetFolder(FOLDER_ID);
		request.setNetworks(Arrays.asList(NETWORK_ID));
		new NdexRestClientModelAccessLayer(client).moveNetworks(request);

		assertEquals(FOLDER_ID.toString(), body.getValue().get("targetFolder").asText());
		assertEquals(NETWORK_ID.toString(), body.getValue().get("networks").get(0).asText());
		verify(client);
	}

	// ---------- createCX2Network ----------

	@Test
	public void createCX2NetworkAppliesVisibilityAndFolder() throws Exception {
		assertEquals("v3/networks?visibility=PUBLIC&folderId=" + FOLDER_ID,
				captureCreateRoute(VisibilityType.PUBLIC, FOLDER_ID));
	}

	@Test
	public void createCX2NetworkAppliesVisibilityOnly() throws Exception {
		assertEquals("v3/networks?visibility=PRIVATE", captureCreateRoute(VisibilityType.PRIVATE, null));
	}

	@Test
	public void createCX2NetworkAppliesFolderOnly() throws Exception {
		assertEquals("v3/networks?folderId=" + FOLDER_ID, captureCreateRoute(null, FOLDER_ID));
	}

	@Test
	public void createCX2NetworkWithNeitherSendsNoQuery() throws Exception {
		assertEquals("v3/networks", captureCreateRoute(null, null));
	}

	@Test
	public void createCX2NetworkSingleArgOverloadIsUnqueried() throws Exception {
		NdexRestClient client = mock(NdexRestClient.class);
		Capture<String> route = EasyMock.newCapture();
		expect(client.createReturningConnection(EasyMock.capture(route), anyObject(InputStream.class), eq("POST"),
				anyObject())).andReturn(createdConnection(NETWORK_ID));
		replay(client);

		UUID id = new NdexRestClientModelAccessLayer(client).createCX2Network(cx2Stream());

		assertEquals("v3/networks", route.getValue());
		assertEquals(NETWORK_ID, id);
		verify(client);
	}

	private String captureCreateRoute(VisibilityType visibility, UUID folderId) throws Exception {
		NdexRestClient client = mock(NdexRestClient.class);
		Capture<String> route = EasyMock.newCapture();
		expect(client.createReturningConnection(EasyMock.capture(route), anyObject(InputStream.class), eq("POST"),
				anyObject())).andReturn(createdConnection(NETWORK_ID));
		replay(client);

		UUID id = new NdexRestClientModelAccessLayer(client).createCX2Network(cx2Stream(), visibility, folderId);

		assertEquals(NETWORK_ID, id);
		verify(client);
		return route.getValue();
	}

	// ---------- updateCX2Network ----------

	@Test
	public void updateCX2NetworkAppendsVisibility() throws Exception {
		assertEquals("v3/networks/" + NETWORK_ID + "?visibility=PUBLIC",
				captureUpdateRoute(VisibilityType.PUBLIC));
	}

	@Test
	public void updateCX2NetworkOmitsVisibilityWhenNull() throws Exception {
		assertEquals("v3/networks/" + NETWORK_ID, captureUpdateRoute(null));
	}

	@Test
	public void updateCX2NetworkSingleArgOverloadIsUnqueried() throws Exception {
		NdexRestClient client = mock(NdexRestClient.class);
		Capture<String> route = EasyMock.newCapture();
		expect(client.createReturningConnection(EasyMock.capture(route), anyObject(InputStream.class), eq("PUT"),
				anyObject())).andReturn(noContentConnection());
		replay(client);

		new NdexRestClientModelAccessLayer(client).updateCX2Network(NETWORK_ID, cx2Stream());

		assertEquals("v3/networks/" + NETWORK_ID, route.getValue());
		verify(client);
	}

	@Test
	public void updateCXNetworkStillUsesV2Route() throws Exception {
		NdexRestClient client = mock(NdexRestClient.class);
		Capture<String> route = EasyMock.newCapture();
		expect(client.createReturningConnection(EasyMock.capture(route), anyObject(InputStream.class), eq("PUT"),
				anyObject())).andReturn(noContentConnection());
		replay(client);

		new NdexRestClientModelAccessLayer(client).updateCXNetwork(NETWORK_ID, cx2Stream());

		assertEquals("v2/network/" + NETWORK_ID, route.getValue());
		verify(client);
	}

	private String captureUpdateRoute(VisibilityType visibility) throws Exception {
		NdexRestClient client = mock(NdexRestClient.class);
		Capture<String> route = EasyMock.newCapture();
		expect(client.createReturningConnection(EasyMock.capture(route), anyObject(InputStream.class), eq("PUT"),
				anyObject())).andReturn(noContentConnection());
		replay(client);

		new NdexRestClientModelAccessLayer(client).updateCX2Network(NETWORK_ID, cx2Stream(), visibility);

		verify(client);
		return route.getValue();
	}

	@Test
	public void updateSucceedsWhenV3AnswersTwoHundredWithABody() throws Exception {
		// The bug this guards: only 204 was treated as success, so a successful v3 update took the error
		// path and failed parsing its own success body as an NDExError.
		NdexRestClient client = mock(NdexRestClient.class);
		expect(client.createReturningConnection(anyObject(String.class), anyObject(InputStream.class), eq("PUT"),
				anyObject())).andReturn(okWithBodyConnection());
		replay(client);

		new NdexRestClientModelAccessLayer(client).updateCX2Network(NETWORK_ID, cx2Stream(),
				VisibilityType.PUBLIC);

		verify(client);
	}

	@Test
	public void updateStillSucceedsWhenV2AnswersNoContent() throws Exception {
		NdexRestClient client = mock(NdexRestClient.class);
		expect(client.createReturningConnection(anyObject(String.class), anyObject(InputStream.class), eq("PUT"),
				anyObject())).andReturn(noContentConnection());
		replay(client);

		new NdexRestClientModelAccessLayer(client).updateCXNetwork(NETWORK_ID, cx2Stream());

		verify(client);
	}

	@Test
	public void updateReportsAServerErrorFromTheErrorStream() throws Exception {
		// getInputStream() throws for a 4xx, so the error body has to come off getErrorStream()
		NdexRestClient client = mock(NdexRestClient.class);
		expect(client.createReturningConnection(anyObject(String.class), anyObject(InputStream.class), eq("PUT"),
				anyObject())).andReturn(notFoundConnection());
		client.processNdexSpecificException(anyObject(InputStream.class), eq(HttpURLConnection.HTTP_NOT_FOUND),
				anyObject(com.fasterxml.jackson.databind.ObjectMapper.class));
		expectLastCall().andThrow(new org.ndexbio.model.exceptions.ObjectNotFoundException("network", NETWORK_ID.toString()));
		replay(client);

		try {
			new NdexRestClientModelAccessLayer(client).updateCX2Network(NETWORK_ID, cx2Stream(), null);
			fail("expected the server error to surface");
		} catch (org.ndexbio.model.exceptions.NdexException expected) {
			// the point is that it is an NdexException, not a Jackson parse failure
		}
		verify(client);
	}

	// ---------- helpers ----------

	private static InputStream cx2Stream() {
		return new ByteArrayInputStream("[{\"CXVersion\":\"2.0\"}]".getBytes(StandardCharsets.UTF_8));
	}

	private static HttpURLConnection createdConnection(UUID newId) throws Exception {
		HttpURLConnection con = mock(HttpURLConnection.class);
		expect(con.getResponseCode()).andReturn(HttpURLConnection.HTTP_CREATED).anyTimes();
		expect(con.getInputStream()).andReturn(new ByteArrayInputStream(
				("{\"uuid\":\"" + newId + "\"}").getBytes(StandardCharsets.UTF_8))).anyTimes();
		replay(con);
		return con;
	}

	/** What v3 actually answers to PUT /v3/networks/{id}: 200 with an NdexObjectUpdateStatus body. */
	private static HttpURLConnection okWithBodyConnection() throws Exception {
		HttpURLConnection con = mock(HttpURLConnection.class);
		expect(con.getResponseCode()).andReturn(HttpURLConnection.HTTP_OK).anyTimes();
		expect(con.getInputStream()).andReturn(new ByteArrayInputStream(
				("{\"uuid\":\"" + NETWORK_ID + "\",\"modificationTime\":\"2026-09-04T00:00:00Z\"}")
						.getBytes(StandardCharsets.UTF_8))).anyTimes();
		expect(con.getErrorStream()).andReturn(null).anyTimes();
		replay(con);
		return con;
	}

	/** A failing update: an NDExError body on the error stream. */
	private static HttpURLConnection notFoundConnection() throws Exception {
		HttpURLConnection con = mock(HttpURLConnection.class);
		expect(con.getResponseCode()).andReturn(HttpURLConnection.HTTP_NOT_FOUND).anyTimes();
		expect(con.getErrorStream()).andReturn(new ByteArrayInputStream(
				"{\"errorCode\":\"NDEx_Object_Not_Found\",\"message\":\"no such network\"}"
						.getBytes(StandardCharsets.UTF_8))).anyTimes();
		replay(con);
		return con;
	}

	private static HttpURLConnection noContentConnection() throws Exception {
		HttpURLConnection con = mock(HttpURLConnection.class);
		expect(con.getResponseCode()).andReturn(HttpURLConnection.HTTP_NO_CONTENT).anyTimes();
		replay(con);
		return con;
	}
}
