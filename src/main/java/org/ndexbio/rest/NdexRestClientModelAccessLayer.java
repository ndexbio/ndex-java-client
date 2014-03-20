package org.ndexbio.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.util.List;

import org.ndexbio.model.object.BaseTerm;
import org.ndexbio.model.object.Citation;
import org.ndexbio.model.object.Edge;
import org.ndexbio.model.object.Namespace;
import org.ndexbio.model.object.NdexDataModelService;
import org.ndexbio.model.object.Network;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class NdexRestClientModelAccessLayer implements NdexDataModelService 
{
	NdexRestClient ndexRestClient = null;
	ObjectMapper objectMapper = null;

	public NdexRestClientModelAccessLayer(NdexRestClient client) {
		super();
		ndexRestClient = client;
		objectMapper = new ObjectMapper();
	}

	public Network getNetworkById(String networkId) {
		// TODO Auto-generated method stub
		return null;
	}

	public Iterable<Citation> getCitationsByNetworkId(String networkId) {
		// TODO Auto-generated method stub
		return null;
	}

	public Network getSubnetworkByCitationId(String networkId, String citationId) {
		// TODO Auto-generated method stub
		return null;
	}

	public Iterable<Edge> getEdgesBySupportId(String supportId) {
		// TODO Auto-generated method stub
		return null;
	}

	public Iterable<Namespace> getNamespacesByNetworkId(String networkId) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<Network> findNetworks(String searchString) {
		try {
			return findNetworksByText(searchString, "exact-match", 1000,  0);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	// Simple search by name and description
	public List<Network> findNetworksByText(String searchString, String searchType, Integer blockSize, Integer skipBlocks) 
			throws JsonProcessingException, IOException {
		String route = "/networks/search/" + searchType;		
		JsonNode postData = objectMapper.createObjectNode(); // will be of type ObjectNode
		((ObjectNode) postData).put("searchString", searchString);
		((ObjectNode) postData).put("top", blockSize.toString());
		((ObjectNode) postData).put("skip", skipBlocks.toString());

		HttpURLConnection con = ndexRestClient.postReturningConnection(route, postData);
		InputStream inputStream = con.getInputStream();
		List<Network> networks = objectMapper.readValue(inputStream, new TypeReference<List<Network>>() { });
		inputStream.close();
		con.disconnect();

		return networks;
	}
	
	public List<Network> findNetworksByProperty(String property, String value, String operator, Integer maxNetworks) throws JsonProcessingException, IOException{
		String route = "/networks/search/exact-match"; // exact-match is not relevant, but its a required part of the route
		String searchString = "[" + property + "]" + operator + "\"" + value + "\"";
		JsonNode postData = objectMapper.createObjectNode(); // will be of type ObjectNode
		((ObjectNode) postData).put("searchString", searchString);
		((ObjectNode) postData).put("top", maxNetworks.toString());
		((ObjectNode) postData).put("skip", "0");
		
		HttpURLConnection con = ndexRestClient.postReturningConnection(route, postData);
		InputStream inputStream = con.getInputStream();
		List<Network> networks = objectMapper.readValue(inputStream, new TypeReference<List<Network>>() { });
		inputStream.close();
		con.disconnect();
		return networks;	
	}
	
	public List<BaseTerm> findBaseTermsInNetworkByNamespace(String namespacePrefix, String networkId) throws JsonProcessingException, IOException{
		String route = "/networks/" + networkId + "/namespaces";
		ArrayNode postData = objectMapper.createArrayNode(); // will be of type ObjectNode
		postData.add(namespacePrefix);
		
		HttpURLConnection con = ndexRestClient.postReturningConnection(route, postData);
		InputStream inputStream = con.getInputStream();
		List<BaseTerm> baseTerms = objectMapper.readValue(inputStream, new TypeReference<List<BaseTerm>>() { });
		inputStream.close();
		con.disconnect();

		return baseTerms;
	}

	public Network getEdges(String id, int skipBlocks, int edgesPerBlock) throws IOException {
		String route = "/networks/" + id + "/edges/" + skipBlocks + "/" + edgesPerBlock; 
		HttpURLConnection con = ndexRestClient.getReturningConnection(route, "");
		InputStream inputStream = con.getInputStream();
		Network network = objectMapper.readValue(inputStream, Network.class);
		//inputStream.close();
		con.disconnect();
		return network;
	}

	public Network createNetwork(Network network) throws Exception {
		String route = "/networks"; 
		HttpURLConnection con = ndexRestClient.putReturningConnection(route, network);
		return getPutNetworkFromConnection(con);
	}

	public Network addNetwork(String targetNetworkId, String equivalenceMethod,
			Network network) throws Exception {
		String route = "/networks/" + targetNetworkId + "/" + equivalenceMethod; 
		HttpURLConnection con = ndexRestClient.putReturningConnection(route, network);
		return getPutNetworkFromConnection(con);		
	}
	
	private Network getPutNetworkFromConnection(HttpURLConnection con) throws Exception{
		String responseMessage = con.getResponseMessage();
		int responseCode = con.getResponseCode();
		if (responseMessage.equals("OK")){
			// If responseMessage is OK (200+), you can read the body using urlconnection.getInputStream() or urlconnection.getContent() 
			InputStream inputStream = con.getInputStream();
			Network returnedNetwork = objectMapper.readValue(inputStream, Network.class);
			inputStream.close();
			con.disconnect();
			return returnedNetwork;
		} else {
			// If you get an error code, use urlconnection.getErrorStream()
			final BufferedReader errorReader = new BufferedReader(new InputStreamReader(con.getErrorStream()));
			throw new Exception("Error '" + responseMessage + "' code = " + responseCode + " in createNetwork with error " + errorReader.readLine());
		}
	}

	public Network getNetworkByNonEdgeNodes(String networkId, int skipBlocks,
			int nodesPerBlock) throws IOException {
		String route = "/networks/nodes/" + networkId + "/" + skipBlocks + "/" + nodesPerBlock; 
		HttpURLConnection con = ndexRestClient.getReturningConnection(route, "");
		InputStream inputStream = con.getInputStream();
		Network network = objectMapper.readValue(inputStream, Network.class);
		inputStream.close();
		con.getOutputStream().close();
		con.disconnect();
		return network;
	}





}
