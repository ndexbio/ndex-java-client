package org.ndexbio.rest;

import java.util.Iterator;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.ndexbio.model.object.network.BaseTerm;
import org.ndexbio.model.object.network.Network;

import com.fasterxml.jackson.databind.JsonNode;


public class NdexRestClientTest {
	
	private NdexRestClient client;
	private NdexRestClientModelAccessLayer mal;
		
	@Before
	public void setUp() throws Exception {
		client = new NdexRestClient("Support", "probably-insecure"); //("dexterpratt", "insecure");
		mal = new NdexRestClientModelAccessLayer(client);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testAuthentication() throws Exception {
		
		boolean b = mal.checkCredential();
		
		Assert.assertTrue(b);
	}

/*	
	@Test
	public void testApi() throws Exception {
		JsonNode response = client.get("/networks/api", "");
		Iterator<JsonNode> elements = response.elements();
		while (elements.hasNext()){
			JsonNode resource = elements.next();
			System.out.println(resource.get("requestType") + "  " + resource.get("path"));
			System.out.println("   implemented by: " + resource.get("methodName") + "(" + resource.get("parameterTypes") + ")");
            String consumes = resource.get("consumes").textValue();
            if (null != consumes){
            	System.out.println("   consumes: " + consumes);
            }
			//System.out.println(response.toString());
			
		}
		
		
	}
	*/

	/*
	@Test
	public void testStatus() throws Exception {
		JsonNode status = client.get("/networks/status", "");
		System.out.println(status.get("networkCount") + " networks");
		System.out.println(status.get("userCount") + " users");
		System.out.println(status.get("groupCount") + " groups");
	}
	*/
/*	
	@Test
	public void testFindNetworksByName() throws Exception {
		List<Network> networks = mal.findNetworksByText("BEL", "contains", 10, 0);
		System.out.println("\n______\nTesting Finding Networks by text contains BEL:");
		for(Network network : networks){
			System.out.println(network.getName() + "  (edge count = " + network.getEdgeCount() + ")");	
		}
	}
	
	@Test
	public void testFindNetworksByProperty() throws Exception {
		List<Network> networks = mal.findNetworksByProperty("Format", "BEL_DOCUMENT", "=", 10);
		System.out.println("\n______\nTesting Finding Networks by Property Format = BEL_DOCUMENT:");
		for(Network network : networks){
			System.out.println(network.getName() + "  (edge count = " + network.getEdgeCount() + ")");	
		}
	}

	@Test
	public void testFindTermsInNetworkByNamespace() throws Exception {
		List<Network> networks = mal.findNetworksByProperty("Format", "BEL_DOCUMENT", "=", 10);
		for(Network network : networks){
			System.out.println("\n______\n" + network.getName() + "  id = " + network.getId() + "\nTerms:");
			
			List<BaseTerm> baseTerms = mal.findBaseTermsInNetworkByNamespace("HGNC", network.getId());
			for (BaseTerm baseTerm : baseTerms){
				System.out.println(" " + baseTerm.getName() + "\t  id = " + baseTerm.getId());
			}
			
		}
		
	}
*/

}
