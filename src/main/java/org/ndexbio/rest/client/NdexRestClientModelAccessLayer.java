package org.ndexbio.rest.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.UUID;

import org.ndexbio.model.object.network.BaseTerm;
import org.ndexbio.model.object.network.Citation;
import org.ndexbio.model.object.network.Edge;
import org.ndexbio.model.object.network.Namespace;
import org.ndexbio.model.object.network.NetworkSummary;
import org.ndexbio.model.object.network.PropertyGraphNetwork;
import org.ndexbio.model.object.NdexStatus;
import org.ndexbio.model.object.network.Network;
import org.ndexbio.model.object.User;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class NdexRestClientModelAccessLayer // implements NdexDataModelService 
{
	NdexRestClient ndexRestClient = null;
	ObjectMapper objectMapper = null;
	User currentUser = null;

	public NdexRestClientModelAccessLayer(NdexRestClient client) {
		super();
		ndexRestClient = client;
		objectMapper = new ObjectMapper();
	}

	public Network getNetworkById(String networkId) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public PropertyGraphNetwork getPropertyGraphNetwork(String networkUUID, int skipBlocks, int blockSize) throws JsonProcessingException, IOException {
/*		String route = "/network/"+networkUUID + "/asPropertyGraph/query/" + skipBlocks +"/" +blockSize ;		
		JsonNode postData = objectMapper.createObjectNode(); // will be of type ObjectNode
		((ObjectNode) postData).put("searchString", "");
		((ObjectNode) postData).put("top", blockSize);
		((ObjectNode) postData).put("skip", skipBlocks);

		HttpURLConnection con = ndexRestClient.postReturningConnection(route, postData);
*/
		String route = "/network/"+networkUUID + "/edge/asPropertyGraph/" + skipBlocks +"/" +blockSize ;		
		
    	HttpURLConnection con = this.ndexRestClient.getReturningConnection(route,"");
		InputStream inputStream = con.getInputStream();
		PropertyGraphNetwork network = objectMapper.readValue(inputStream, PropertyGraphNetwork.class);
		inputStream.close();
		con.disconnect();

		return network;

	}

	public List<Citation> getCitationsByNetworkId(String networkId) {
		// TODO Auto-generated method stub
		return null;
	}

	public Network getSubnetworkByCitationId(String networkId, String citationId) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<Edge> getEdgesBySupportId(String supportId) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<Namespace> getNamespacesByNetworkId(String networkId) {
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
	
	public void setCredential(String username, String password) {
		ndexRestClient.setCredential(username, password);
		
	}
	
	public boolean checkCredential(){
		try {
			if (null == ndexRestClient.getUsername() || null == ndexRestClient.getPassword()) return false;
			JsonNode currentUser = ndexRestClient.get("/users/authenticate/" + ndexRestClient.getUsername() + "/" + ndexRestClient.getPassword(), "");
			if (null == currentUser || null == currentUser.get("externalId")) return false;
			ndexRestClient.setUserUid(UUID.fromString(currentUser.get("externalId").textValue()));
			return true;
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
		return false;
		
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

	// Simple search
	public List<NetworkSummary> findNetworkSummariesByText(String searchString,  int skipBlocks, int blockSize) 
			throws JsonProcessingException, IOException {
		String route = "/network/search/" + blockSize+"/"+skipBlocks;		
		JsonNode postData = objectMapper.createObjectNode(); // will be of type ObjectNode
		((ObjectNode) postData).put("searchString", searchString);
		((ObjectNode) postData).put("top", blockSize);
		((ObjectNode) postData).put("skip", skipBlocks);

		HttpURLConnection con = ndexRestClient.postReturningConnection(route, postData);
		InputStream inputStream = con.getInputStream();
		List<NetworkSummary> networks = objectMapper.readValue(inputStream, new TypeReference<List<NetworkSummary>>() { });
		inputStream.close();
		con.disconnect();

		return networks;
	}
	
    public NdexStatus getServerStatus() throws IOException {
    	String route = "/admin/status" ;

    	HttpURLConnection con = this.ndexRestClient.getReturningConnection(route,"");
		InputStream inputStream = con.getInputStream();
		NdexStatus status = this.objectMapper.readValue(inputStream, NdexStatus.class);
		inputStream.close();
		con.disconnect();

		return status;
    	
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
	
	public Network getNetworkByNonEdgeNodes(String networkId, int skipBlocks,
			int nodesPerBlock) throws IOException {
		String route = "/networks/nodes/" + networkId + "/" + skipBlocks + "/" + nodesPerBlock; 
		HttpURLConnection con = ndexRestClient.getReturningConnection(route, "");
		InputStream inputStream = con.getInputStream();
		Network network = objectMapper.readValue(inputStream, Network.class);
		//inputStream.close();
		con.getOutputStream().close();
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

 









}
