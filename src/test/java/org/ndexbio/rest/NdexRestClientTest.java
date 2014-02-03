package org.ndexbio.rest;

import java.io.IOException;
import java.util.Iterator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class NdexRestClientTest {

	private NdexRestClient client;
		
	@Before
	public void setUp() throws Exception {
		client = new NdexRestClient("dexterpratt", "insecure");
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testApi() throws Exception {
		JsonNode response = client.get("/networks/api", "");
		Iterator<JsonNode> elements = response.elements();
		while (elements.hasNext()){
			JsonNode resource = elements.next();
			System.out.println(resource.get("requestType") + "  " + resource.get("path"));
			System.out.println("implemented by: " + resource.get("methodName") + "(" + resource.get("parameterTypes") + ")");
            String consumes = resource.get("consumes").textValue();
            if (null != consumes){
            	System.out.println("consumes: " + consumes);
            }
			//System.out.println(response.toString());
			
		}
		
		
	}
	
	@Test
	public void testStatus() throws Exception {
		JsonNode status = client.get("/networks/status", "");
		System.out.println(status.get("networkCount") + " networks");
		System.out.println(status.get("userCount") + " users");
		System.out.println(status.get("groupCount") + " groups");
	}
	
	@Test
	public void testFindNetworksByProperty() throws Exception {
		Iterator<JsonNode> networks = findNetworksByProperty("Source", "Protein Interaction Database", "=", 10);
		while (networks.hasNext()){
			JsonNode network = networks.next();
			System.out.println(network.get("name") + "  " + network.get("edgeCount"));	
		}
	}

	private Iterator<JsonNode> findNetworksByProperty(String property, String value, String operator, Integer maxNetworks) throws JsonProcessingException, IOException{
		String route = "/networks/search/exact-match"; // exact-match is not relevant, but its a required part of the route
		String searchString = "[" + property + "]" + operator + "\"" + value + "\"";
		ObjectMapper mapper = new ObjectMapper();
		JsonNode searchParameters = mapper.createObjectNode(); // will be of type ObjectNode
		((ObjectNode) searchParameters).put("searchString", searchString);
		((ObjectNode) searchParameters).put("top", maxNetworks.toString());
		((ObjectNode) searchParameters).put("skip", "0");

		JsonNode response = client.post(route, searchParameters);
		Iterator<JsonNode> elements = response.elements();
		return elements;	
	}
	
	@Test
	public void testFindTermsInNetworkByNamespace() throws Exception {
		Iterator<JsonNode> networks = findNetworksByProperty("Source", "Protein Interaction Database", "=", 1);
		if (networks.hasNext()){
			JsonNode network = networks.next();
			System.out.println("\n______\n" + network.get("name").asText() + "  " + network.get("id").asText());
			String termQueryRoute = "/networks/" + network.get("id").asText() + "/namespaces";
			ObjectMapper mapper = new ObjectMapper();
			ArrayNode namespaces = mapper.createArrayNode(); // will be of type ObjectNode
			namespaces.add("HGNC");
			JsonNode response = client.post(termQueryRoute, namespaces);
			Iterator<JsonNode> elements = response.elements();
			while (elements.hasNext()){
				JsonNode term = elements.next();
				System.out.println(term.get("name").asText() + "  " + term.get("id").asText());
			}
			
		}
		
	}


}
