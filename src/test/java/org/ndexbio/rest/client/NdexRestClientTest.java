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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.ndexbio.cxio.aspects.datamodels.EdgesElement;
import org.ndexbio.cxio.aspects.datamodels.NetworkAttributesElement;
import org.ndexbio.cxio.metadata.MetaDataCollection;
import org.ndexbio.model.cx.CitationElement;
import org.ndexbio.model.cx.NiceCXNetwork;
import org.ndexbio.model.exceptions.NdexException;
import org.ndexbio.model.exceptions.ObjectNotFoundException;
import org.ndexbio.model.exceptions.UnauthorizedOperationException;
import org.ndexbio.model.object.Group;
import org.ndexbio.model.object.NdexPropertyValuePair;
import org.ndexbio.model.object.NdexStatus;
import org.ndexbio.model.object.Permissions;
import org.ndexbio.model.object.ProvenanceEntity;
import org.ndexbio.model.object.SimplePropertyValuePair;
import org.ndexbio.model.object.SimpleQuery;
import org.ndexbio.model.object.SolrSearchResult;
import org.ndexbio.model.object.Status;
import org.ndexbio.model.object.Task;
import org.ndexbio.model.object.User;
import org.ndexbio.model.object.network.NetworkSummary;
import org.ndexbio.model.object.network.VisibilityType;

import com.fasterxml.jackson.core.JsonProcessingException;

public class NdexRestClientTest {

	private NdexRestClient client;
	private NdexRestClientModelAccessLayer ndex;
	private static String _username = "cj1";
	private static String _password = "aaaaaaaaa";
	private String _route = "dev.ndexbio.org";

	@Rule
	public ExpectedException thrown1 = ExpectedException.none();

	@Before
	public void setUp() throws Exception {

		client = new NdexRestClient(_username, _password, _route);
		ndex = new NdexRestClientModelAccessLayer(client);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetStatus() throws IOException, NdexException {
		NdexStatus s = ndex.getServerStatus(true);
		assertNotNull(s.getProperties().get("Build"));
		assertEquals(((String) s.getProperties().get("ServerVersion")).substring(0, 1), "2");
		s = ndex.getServerStatus(false);
		assertNull((Object) s.getProperties().get("ImporterExporters"));
	}

	@Test
	public void testAnynomousClient() throws JsonProcessingException, IOException, NdexException {
		NdexRestClient c1 = new NdexRestClient(_route);
		NdexRestClientModelAccessLayer ndex2 = new NdexRestClientModelAccessLayer(c1);
		SimpleQuery query = new SimpleQuery();
		query.setSearchString("userName:\"cj1\"");
		SolrSearchResult<User> r = ndex2.findUsers(query, 0, 100);
		assertEquals(r.getNumFound(), 1);
		// List<User> us = r.getResultList();
		assertEquals(r.getResultList().get(0).getUserName(), "cj1");
		
		//test getting public network as anonymous user
		NiceCXNetwork c =  ndex.getNetwork(UUID.fromString("c81ea28a-bdc4-11e7-9235-06832d634f41"));
		assertEquals(c.getEdges().size(),3);
		assertEquals(c.getNodes().size(),3);
		
		//get a network from accessKey http://dev.ndexbio.org/#/network/86fbe77b-a799-11e7-b522-06832d634f41?accesskey=d62c3bdab55c1956f8cda2a4a1072043cba64a795f89d280dd615cc2d8c9f5b2
		// this is a private network in this account 
		UUID privateNetUUID = UUID.fromString("86fbe77b-a799-11e7-b522-06832d634f41");
		String accessKey = "d62c3bdab55c1956f8cda2a4a1072043cba64a795f89d280dd615cc2d8c9f5b2";
		try (InputStream in = ndex2.getNetworkAsCXStream(privateNetUUID, accessKey)) {
			NiceCXNetwork cx = NdexRestClientUtilities.getCXNetworkFromStream(in);
			for (NetworkAttributesElement e : cx.getNetworkAttributes()) {
				if (e.getName().equals("name")) {
					assertEquals(e.getValue(), "Aurora A signaling - sharable link test network - don't remove");
					break;
				}
			}

			assertEquals(cx.getEdges().size(), 47);
			assertEquals(cx.getNodes().size(), 32);
		}
		
		// get a network summary by uuid and accessKey 
		NetworkSummary summary = ndex2.getNetworkSummaryById(privateNetUUID, accessKey);
		assertEquals(summary.getName(), "Aurora A signaling - sharable link test network - don't remove");
		assertEquals(summary.getNodeCount(), 32);
	}

	@Test
	public void testGetGroupOperations() throws IllegalStateException, Exception {
		Group newGroup = new Group();
		String testGroupName = "CJ's Test Group Created From Java Client";
		String testGroupDesc = "Don't Modifiy this group";
		newGroup.setGroupName(testGroupName);
		newGroup.setDescription(testGroupDesc);

		UUID groupId = ndex.createGroup(newGroup);

		Group g = ndex.getGroup(groupId);
		assertEquals(g.getGroupName(), testGroupName);
		assertEquals(g.getExternalId(), groupId);
		assertEquals(g.getDescription(), testGroupDesc);

		String testString = "SpecialStringOnlyForABCDEFTesting";
		g.setDescription(testString);
		ndex.updateGroup(g);
		g = ndex.getGroup(groupId);
		assertEquals(g.getDescription(), testString);
		SimpleQuery query = new SimpleQuery();
		query.setSearchString(testString);
		Thread.sleep(3000);
		SolrSearchResult<Group> r = ndex.findGroups(query, 0, 10);
		assertEquals(r.getNumFound(), 1);
		assertEquals(r.getResultList().get(0).getDescription(), testString);
		ndex.deleteGroup(groupId);
		thrown1.expect(ObjectNotFoundException.class);
		ndex.getGroup(groupId);
	}

	@Test
	public void testUserFunctions() throws JsonProcessingException, IOException, NdexException {
		List<NetworkSummary> myNetworks = ndex.getMyNetworks();
		assertTrue(myNetworks.size() > 10);

	}


	@Test
	public void testNetworkSummaris() throws JsonProcessingException, IOException, NdexException {
		
		List<UUID> uuids = new ArrayList<>();
		uuids.add(UUID.fromString("3dbd6883-14a2-11e7-b0de-06832d634f41"));
		uuids.add(UUID.fromString("c81ea28a-bdc4-11e7-9235-06832d634f41"));
		
		List<NetworkSummary> myNetworks = ndex.getNetworkSummariesByIds(uuids);
		assertTrue(myNetworks.size() == 2);
		assertTrue(myNetworks.get(1).getName().equals("Network for unit test - dont remove -public"));

	}
	
	
	@Test
	public void testTaskOperators() throws IOException, NdexException {
		UUID taskId = UUID.fromString("ff254008-adfa-11e7-9b0a-06832d634f41");
		Task t = ndex.getTask(taskId);
		assertEquals(t.getExternalId(), taskId);
		List<Task> tlist = ndex.getUserTasks(null, 0, 0);
		assertTrue(tlist.size() > 0);
		int l1 = tlist.size();
		List<Task> tlist2 = ndex.getUserTasks(Status.COMPLETED, 0, 0);
		List<Task> tlist3 = ndex.getUserTasks(Status.FAILED, 0, 0);
		List<Task> tlist4 = ndex.getUserTasks(Status.PROCESSING, 0, 0);
		List<Task> tlist5 = ndex.getUserTasks(Status.QUEUED, 0, 0);
		assertEquals(l1, tlist2.size() + tlist3.size() + tlist4.size() + tlist5.size());

	}

	@Test
	public void testNetworkOperations() throws IllegalStateException, Exception {

		UUID networkId;
		NiceCXNetwork cx;

		// create a network in NDEx
		try (InputStream is = this.getClass().getResourceAsStream("/test_network.cx")) {

			networkId = ndex.createCXNetwork(is);
		}

		try (InputStream is =
				// new FileInputStream("src/test/resources/test_network.cx")
				this.getClass().getResourceAsStream("/test_network.cx")) {

			cx = NdexRestClientUtilities.getCXNetworkFromStream(is);
		}

		//download the network and create a niceCX model from the stream.
		NiceCXNetwork cx2 = ndex.getNetwork(networkId);

		assertEquals(cx2.getNodes().size(), cx.getNodes().size());
		assertEquals(cx2.getMetadata().size(), cx.getMetadata().size());
		
		Map<Long, EdgesElement> cx2Edges = cx2.getEdges();
		Map<Long, EdgesElement> cxEdges = cx.getEdges();
		assertEquals(cx2Edges.size(), cxEdges.size());
		
		EdgesElement e1 = cx2Edges.get(72L);
		EdgesElement e2 = cxEdges.get(72L);
		
		assertEquals(e1.getSource(), e2.getSource());
		assertEquals(e2.getTarget(), e2.getTarget());

		//wait till the network is completely processed in the server.
		NetworkSummary s = ndex.getNetworkSummaryById(networkId);
		int count = 0;
		while (!s.isCompleted()) {
			if (count > 10)
				fail("Network takes too long to process.");
			Thread.sleep(2000);
			System.out.println("Getting networkSummary from Ndex server.");
			s = ndex.getNetworkSummaryById(networkId);
		}

		assertEquals(s.getName(), "cj test Network for unit test - dont remove");
		assertEquals(s.getNodeCount(), cx.getNodes().size());
		assertEquals(s.getEdgeCount(), cx.getEdges().size());
		assertTrue(!s.getHasSample());
		assertTrue(s.getHasLayout());

		MetaDataCollection md = ndex.getNetworkMetadata(networkId);
		assertEquals(md.size(), cx.getMetadata().size());
		
		//clone this network
		UUID clonedNetworkId = ndex.cloneNetwork(networkId);
		NiceCXNetwork clonedNiceCX = ndex.getNetwork(clonedNetworkId);
		assertEquals(clonedNiceCX.getNodes().size(), cx2.getNodes().size());
		assertEquals(clonedNiceCX.getEdges().size(), cx2.getEdges().size());
		assertEquals(clonedNiceCX.getNetworkAttributes().size(), cx2.getNetworkAttributes().size());
		assertEquals(clonedNiceCX.getNodeAttributes().size(), cx2.getNodeAttributes().size());
		
		//delete the clone
		ndex.deleteNetwork(clonedNetworkId);
		try {
			ndex.getNetworkSummaryById(clonedNetworkId);
			Assert.fail("Cloned network was not deleted successfully.");
		} catch (ObjectNotFoundException e) {
			System.out.println("Cloned network deleted.");
		}
		//Get the first network in my account.
		List<NetworkSummary> myNetworks = ndex.getMyNetworks(0, 1);

		boolean found = false;
		for ( NetworkSummary net : myNetworks) {
			if ( net.getExternalId().equals(networkId)) {
				found = true;
				break;
			}
		}
		
		assertTrue ( found);

		cx2 = ndex.getNetwork(networkId);

		assertEquals(cx2.getNodes().size(), cx.getNodes().size());
		assertEquals(cx2.getMetadata().size(), cx.getMetadata().size());
		assertEquals(cx2.getEdges().size(), cx.getEdges().size());
		assertEquals(cx2.getEdges().get(72L).getSource(), cx.getEdges().get(72L).getSource());
		assertEquals(cx2.getEdges().get(72L).getTarget(), cx.getEdges().get(72L).getTarget());
				
		//update network
		
		NiceCXNetwork cx_bel;

		try (InputStream is =
				this.getClass().getResourceAsStream("/BEL_Framework_Small_Corpus_Document.cx")) {

			cx_bel = NdexRestClientUtilities.getCXNetworkFromStream(is);
		}
		
		try (InputStream iss = this.getClass().getResourceAsStream("/BEL_Framework_Small_Corpus_Document.cx")) {
			ndex.updateCXNetwork(networkId, iss);
		}
		
		s = ndex.getNetworkSummaryById(networkId);
		count = 0;
		while (!s.isCompleted()) {
			if (count > 10)
				fail("Network takes too long to process.");
			Thread.sleep(2000);
			System.out.println("Getting networkSummary from Ndex server.");
			s = ndex.getNetworkSummaryById(networkId);
		}
		
		assertEquals(s.getName(), "BEL Framework Small Corpus Document");
		assertEquals(s.getNodeCount(), 1598);
		assertEquals(s.getEdgeCount(), 2174);
		assertEquals(s.getProperties().size() + 3, cx_bel.getNetworkAttributes().size());
		assertTrue(s.getHasSample());
		assertTrue(!s.getHasLayout());
		
		List<CitationElement> e = ndex.getNetworkAspect(networkId, "citations", -1, CitationElement.class);

		assertEquals ( e.size() , cx_bel.getCitations().size());
	
		CitationElement et = e.get(10);
		
		assertTrue(cx_bel.getCitations().containsKey(et.getId()));
		
		// set sample network
		try (InputStream is = this.getClass().getResourceAsStream("/test_network.cx")) {
			ndex.setSampleNetwork(networkId, is);
		}
		
		// get sample network
		NiceCXNetwork sampleCX = ndex.getSampleNetwork(networkId);
		assertEquals ( sampleCX.getEdges().size(), cx.getEdges().size());
		assertEquals ( sampleCX.getNodes().size(), cx.getNodes().size());
		assertEquals ( sampleCX.getNetworkAttributes().size(), cx.getNetworkAttributes().size());
		assertEquals ( sampleCX.getEdgeAttributes().size(), cx.getEdgeAttributes().size());
		assertEquals ( sampleCX.getNodeAttributes().size(), cx.getNodeAttributes().size());
		
		//get network user permission on network 
		Map<String, Permissions> p = ndex.getUserNetworkPermission(client.getUserUid(), networkId, false);
		assertTrue (p!=null);
		assertEquals(p.get(networkId.toString()), Permissions.ADMIN);
		
		//set network properites
		List<NdexPropertyValuePair> props = s.getProperties();
		props.add(new NdexPropertyValuePair ("MyKey", "value1"));
		ndex.setNetworkProperties(networkId, props);
		NetworkSummary s2 = ndex.getNetworkSummaryById(networkId);
		assertEquals(s2.getProperties().size(), props.size());
		assertTrue(s.getHasSample());
		
		count = 0;
		while (!s2.isCompleted()) {
			if (count > 20)
				fail("Network takes too long to process.");
			Thread.sleep(2000);
			System.out.println("Getting networkSummary from Ndex server.");
			s2 = ndex.getNetworkSummaryById(networkId);
		}
		
		NiceCXNetwork n2 = ndex.getNetwork(networkId);
		assertEquals( n2.getNetworkAttributes().size(), props.size() + 3);
		
		//change it to public 
		Map<String,Object> sysprop = new HashMap<> ();
		sysprop.put("visibility", "PUBLIC");
		sysprop.put("readOnly", Boolean.TRUE);
		ndex.setNetworkSystemProperty(networkId, sysprop);
		
		NdexRestClient c1 = new NdexRestClient(_route);
		NdexRestClientModelAccessLayer ndex2 = new NdexRestClientModelAccessLayer(c1);
		NetworkSummary s3 = ndex2.getNetworkSummaryById(networkId);
		assertEquals(s3.getVisibility(), VisibilityType.PUBLIC);
		assertTrue(s3.getIsReadOnly());
		
		//change it back to private and not readonly
		sysprop.put("visibility", "PRIVATE");
		sysprop.put("readOnly", Boolean.FALSE);
		ndex.setNetworkSystemProperty(networkId, sysprop);
		
		try {
			ndex2.getNetworkSummaryById(networkId);
			Assert.fail("Changed network back to private, but it still visible to anonymous users.");
		} catch (UnauthorizedOperationException e3) {
			System.out.println("Network becomes private again and no longer visible to anonymous users.");
		}
		

		//delete network 
		ndex.deleteNetwork(networkId);
         
		System.out.println("All network operations have been executed.");
		// final check
		thrown1.expect(ObjectNotFoundException.class);
		ndex.getNetworkSummaryById(networkId);
		
		// don't put any code after this line in this function. It won't be executed because of the exception thrown above. 
	}
	
/*	@Test
	// this test is should be commented out most of the time. Need to put in a valid ID token 
	public void testIDTokenAuthentication() throws JsonProcessingException, IOException, NdexException {
	    // paste a valid id token here
		String idtoken = "";

		NdexRestClient client2 = new NdexRestClient( _route);
		client2.signIn(idtoken);
		NdexRestClientModelAccessLayer ndex3 = new NdexRestClientModelAccessLayer(client2);
		NetworkSummary s = ndex3.getNetworkSummaryById(UUID.fromString("4a555eec-2717-11e8-b6cb-525400c25d22"));
		assertEquals(s.getEdgeCount(), 17);
	//	assertEquals()
		

		
	} */


	
/*	private static String printInputStream(InputStream is) {

		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();

		String line;
		try {

			br = new BufferedReader(new InputStreamReader(is));
			while ((line = br.readLine()) != null) {
				System.out.println(line);
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return sb.toString();

	} */


	@Test
	public void testAuthentication() throws Exception {

		/*
		 * NetworkSummary networksummary =
		 * mal.getNetworkSummaryById("d8c5b86a-1997-11e4-8f64-90b11c72aefa");
		 * 
		 * System.out.println(networksummary);
		 * 
		 * //Network n0 =
		 * mal.getNeighborhood("d750c790-199e-11e4-86bd-90b11c72aefa","YGR218W", 1);
		 * PropertyGraphNetwork n0 =
		 * mal.getNeighborhoodAsPropertyGraph("d750c790-199e-11e4-86bd-90b11c72aefa",
		 * "YGR218W", 1); System.out.println(n0);
		 * 
		 * Network network = mal.getEdges("f6ecda26-18fc-11e4-8590-90b11c72aefa", 0,
		 * 12); ObjectMapper mapper = new ObjectMapper();
		 * System.out.println(mapper.writeValueAsString(network));
		 * 
		 * PropertyGraphNetwork pn =
		 * mal.getPropertyGraphNetwork("d9ed6aa1-1364-11e4-8b0d-90b11c72aefa", 0,12);
		 * for ( PropertyGraphNode n : pn.getNodes().values()) { System.out.println
		 * ("node id: "+ n.getId()); for (NdexProperty p : n.getProperties()) {
		 * System.out.println("\t" + p.getPredicateString() + ": " + p.getValue()); }
		 * 
		 * }
		 * 
		 * for (PropertyGraphEdge e : pn.getEdges()) { System.out.println("Edge:" +
		 * e.getSubjectId() + "->" + e.getPredicate() + "->" + e.getObjectId()); }
		 * System.out.println(pn);
		 * 
		 * int i = 0; for ( NdexProperty p : pn.getProperties()) { if (
		 * p.getPredicateString().equals(PropertyGraphNetwork.uuid) ) { break; } i++; }
		 * pn.getProperties().remove(i);
		 * 
		 * NdexProperty pname = new NdexProperty();
		 * pname.setPredicateString(PropertyGraphNetwork.name);
		 * pname.setValue("my test network1"); pn.getProperties().add(pname);
		 * 
		 * NetworkSummary summary = mal.insertPropertyGraphNetwork(pn);
		 * 
		 * System.out.println(summary);
		 */
		// boolean b = ndex.checkCredential();

		// Assert.assertTrue(b);

		/*
		 * Network n = ndex.getNetwork("f717cacf-7fbf-11e4-a6f2-90b11c72aefa");
		 * 
		 * if (n != null) System.out.println("foo"); // example of search.
		 * List<NetworkSummary> s = ndex.findNetworks("*", true, "Support", 0,3);
		 * System.out.println(s.get(0).getName());
		 * 
		 * // example of get server status. NdexStatus status = ndex.getServerStatus();
		 * System.out.println(status.getNetworkCount());
		 */
	}

	/*
	 * @Test public void testApi() throws Exception { JsonNode response =
	 * client.get("/networks/api", ""); Iterator<JsonNode> elements =
	 * response.elements(); while (elements.hasNext()){ JsonNode resource =
	 * elements.next(); System.out.println(resource.get("requestType") + "  " +
	 * resource.get("path")); System.out.println("   implemented by: " +
	 * resource.get("methodName") + "(" + resource.get("parameterTypes") + ")");
	 * String consumes = resource.get("consumes").textValue(); if (null !=
	 * consumes){ System.out.println("   consumes: " + consumes); }
	 * //System.out.println(response.toString());
	 * 
	 * }
	 * 
	 * 
	 * }
	 */

	/*
	 * @Test public void testStatus() throws Exception { JsonNode status =
	 * client.get("/networks/status", "");
	 * System.out.println(status.get("networkCount") + " networks");
	 * System.out.println(status.get("userCount") + " users");
	 * System.out.println(status.get("groupCount") + " groups"); }
	 */
	/*
	 * @Test public void testFindNetworksByName() throws Exception { List<Network>
	 * networks = mal.findNetworksByText("BEL", "contains", 10, 0);
	 * System.out.println("\n______\nTesting Finding Networks by text contains BEL:"
	 * ); for(Network network : networks){ System.out.println(network.getName() +
	 * "  (edge count = " + network.getEdgeCount() + ")"); } }
	 * 
	 * @Test public void testFindNetworksByProperty() throws Exception {
	 * List<Network> networks = mal.findNetworksByProperty("Format", "BEL_DOCUMENT",
	 * "=", 10); System.out.
	 * println("\n______\nTesting Finding Networks by Property Format = BEL_DOCUMENT:"
	 * ); for(Network network : networks){ System.out.println(network.getName() +
	 * "  (edge count = " + network.getEdgeCount() + ")"); } }
	 * 
	 * @Test public void testFindTermsInNetworkByNamespace() throws Exception {
	 * List<Network> networks = mal.findNetworksByProperty("Format", "BEL_DOCUMENT",
	 * "=", 10); for(Network network : networks){ System.out.println("\n______\n" +
	 * network.getName() + "  id = " + network.getId() + "\nTerms:");
	 * 
	 * List<BaseTerm> baseTerms = mal.findBaseTermsInNetworkByNamespace("HGNC",
	 * network.getId()); for (BaseTerm baseTerm : baseTerms){ System.out.println(" "
	 * + baseTerm.getName() + "\t  id = " + baseTerm.getId()); }
	 * 
	 * }
	 * 
	 * }
	 */

}
