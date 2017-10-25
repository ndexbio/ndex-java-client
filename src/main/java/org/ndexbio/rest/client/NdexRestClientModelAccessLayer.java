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

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.ndexbio.model.cx.NiceCXNetwork;
import org.ndexbio.model.errorcodes.NDExError;
import org.ndexbio.model.exceptions.DuplicateObjectException;
import org.ndexbio.model.exceptions.ForbiddenOperationException;
import org.ndexbio.model.exceptions.NdexException;
import org.ndexbio.model.exceptions.ObjectNotFoundException;
import org.ndexbio.model.exceptions.UnauthorizedOperationException;
import org.ndexbio.model.object.CXSimplePathQuery;
import org.ndexbio.model.object.Group;
import org.ndexbio.model.object.NdexPropertyValuePair;
import org.ndexbio.model.object.NdexStatus;
import org.ndexbio.model.object.NetworkSearchResult;
import org.ndexbio.model.object.Permissions;
import org.ndexbio.model.object.ProvenanceEntity;
import org.ndexbio.model.object.SimpleQuery;
import org.ndexbio.model.object.SolrSearchResult;
import org.ndexbio.model.object.Status;
import org.ndexbio.model.object.Task;
import org.ndexbio.model.object.User;
import org.ndexbio.model.object.network.NetworkSummary;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class NdexRestClientModelAccessLayer 
{
	NdexRestClient ndexRestClient = null;
	ObjectMapper objectMapper = null;
	User currentUser = null;

	public NdexRestClientModelAccessLayer(NdexRestClient client) {
		super();
		ndexRestClient = client;
		objectMapper = new ObjectMapper();
	}
	
/*	public String getBaseRoute(){
		return ndexRestClient.getBaseroute();
	} */

	/*-----------------------------------------
	 * 
	 *          Server
	 *          
	 * -----------------------------------------
	 */
	
	
	public NdexRestClient getNdexRestClient() { return ndexRestClient; }
	
    public NdexStatus getServerStatus() throws IOException, NdexException {
		return getServerStatus(false);	
    }

    public NdexStatus getServerStatus(boolean fullFormat) throws IOException, NdexException {
    	String route = "/admin/status?format=" + (fullFormat? "full": "standard") ;

    	NdexStatus status = this.ndexRestClient.getNdexObject(route, "", NdexStatus.class);
    	
		return status;	
    }

	/*-----------------------------------------
	 * 
	 *          Group
	 *          
	 * -----------------------------------------
	 */
	
	// Get group by id
//			group	GET	/group/{groupUUID}		Group
	public Group getGroup(UUID groupId) throws IOException, NdexException {
		return ndexRestClient.getNdexObject("/group/"+groupId, "", Group.class);
	}
	
	// Update a group
//			group	POST	/group/{groupUUID}	Group	Group
	public void updateGroup(Group group) throws IllegalStateException, Exception{
		JsonNode postData = objectMapper.valueToTree(group);
		ndexRestClient.putNdexObject("/group/" + group.getExternalId() , postData);
	}
	

	public SolrSearchResult<Group> findGroups(SimpleQuery query, int skipBlocks, int blockSize) throws JsonProcessingException, IOException, NdexException{
		JsonNode postData = objectMapper.valueToTree(query);
		return ndexRestClient.postSearchQuery("/search/group?start="  + skipBlocks  + "&size=" + blockSize , postData, Group.class);
	}
	

//	group	POST	/network/searchByProperties	 Collection<NetworkSummary>
/*	@SuppressWarnings("unchecked")
	public List<NetworkSummary> searchNetworkByPropertyFilter(NetworkPropertyFilter query) throws JsonProcessingException, IOException{
		JsonNode postData = objectMapper.valueToTree(query);
		return (List<NetworkSummary>)ndexRestClient.postNdexObjectList("/network/searchByProperties", postData, NetworkSummary.class);
	} */
	// Get network permissions of group as list of memberships
//			group	GET	/group/{groupUUID}/network/{permission}/{skipBlocks}/{blockSize}		Membership[]
/*	@SuppressWarnings("unchecked")
	public List<Membership> findGroupNetworks(String groupId, Permissions permission, int skipBlocks, int blockSize) throws JsonProcessingException, IOException{
		return (List<Membership>)ndexRestClient.getNdexObjectList("/group/" + groupId + "/network/" + permission + "/" + skipBlocks  + "/" + blockSize , "", Membership.class);
	} */
	
	// Create a group
//			group	POST	/group	
	public UUID createGroup(Group group) throws JsonProcessingException, IOException, NdexException{
		JsonNode postData = objectMapper.valueToTree(group);
		return ndexRestClient.createNdexObjectByPost("/group", postData);
	}
	
	// Delete a group
//			group	DELETE	/group/{groupUUID}	
	public void deleteGroup(UUID groupId) throws JsonProcessingException, IOException, NdexException{
		ndexRestClient.delete("/group/" + groupId.toString());
	}	
	
	// Add or modify account permission for a group by posting a membership
//			group	POST	/group/{groupUUID}/member	Membership	
/*	public Membership setGroupPermission(
			String groupId, 
			Membership membership) throws JsonProcessingException, IOException, NdexException{
		JsonNode postData = objectMapper.valueToTree(membership);
		return (Membership)ndexRestClient.postNdexObject("/group/" + groupId + "/member", postData, Membership.class);
	}  */
	
	/**
	 * Removes the member specified by userId from the group specified by groupId. 
	 * @param groupId Group id of 
	 * @param userId
	 * @throws JsonProcessingException
	 * @throws IOException
	 * @throws NdexException 
	 */
/*	public void removeGroupMember(UUID groupId, UUID userId) throws JsonProcessingException, IOException, NdexException{
		ndexRestClient.delete("/group/" + groupId + "/membership?userid=" + userId);
	} */
	

	/*-----------------------------------------
	 * 
	 *          Task
	 *          
	 * -----------------------------------------
	 */


	// Get a task by id
//			task	GET	/task/{taskUUID}		Task
	public Task getTask(UUID taskId) throws IOException, NdexException {
		return (Task) ndexRestClient.getNdexObject("/task/"+ taskId, "", Task.class);
	}
	
	// Update a task
//			task	POST	/task/{taskUUID}	Task	
/*	public Task updateTask(Task task) throws JsonProcessingException, IOException, NdexException{
		JsonNode postData = objectMapper.valueToTree(task);
		return (Task)ndexRestClient.postNdexObject("/task/" + task.getExternalId() , postData, Task.class);
	} */
	
   /**
    * Delete the task specified by taskId.
    * @param taskId
    * @throws JsonProcessingException
    * @throws IOException
    * @throws NdexException
    */
	public void deleteTask(UUID taskId) throws JsonProcessingException, IOException, NdexException{
		ndexRestClient.delete("/task/" + taskId);
	}	
	
	/*-----------------------------------------
	 * 
	 *          User
	 *          
	 * -----------------------------------------
	 */
	
    /**
     * Get a user by UUID.
     * @param userCreated
     * @return
     * @throws IOException
     * @throws NdexException
     */
	public User getUser(UUID userCreated) throws IOException, NdexException {
		return ndexRestClient.getNdexObject("/user/"+userCreated, "", User.class);
	}
	
	/**
	 * Get the list of network
	 * @param userId
	 * @return
	 * @throws JsonProcessingException
	 * @throws IOException
	 * @throws NdexException 
	 */
	public List<NetworkSummary> getMyNetworks() 
			throws JsonProcessingException, IOException, NdexException {
		return getMyNetworks(0,-1);

	}

	public List<NetworkSummary> getMyNetworks(int offset, int limit) 
			throws JsonProcessingException, IOException, NdexException {
		
		UUID userId = ndexRestClient.getUserUid();
		if ( userId == null) {
			throw new NdexException("Client object has not sign in to NDEx server yet.");
		}
		
		String route = "/user/"+ userId + "/networksummary?offset="+offset+"&limit="+ limit;		
		return ndexRestClient.getNdexObjectList(route,"", NetworkSummary.class);

	}
	
/*	
	public User authenticateUser(String userName, String password) throws IOException, NdexException {
		return ndexRestClient.getNdexObject("/user?valid=true", userName,  password, "", User.class);
	} 
*/	
	
	
	// Get group permissions of user as list of memberships
//			user	GET	/user/{userUUID}/group/{permission}/{skipBlocks}/{blockSize}		Membership[]
/*	@SuppressWarnings("unchecked")
	public Map<UUID,Permissions> getUserGroupPermissions(String userId, String permission, int skipBlocks, int blockSize) throws IOException, NdexException {
		return (Map<UUID,Permissions>) ndexRestClient.getNdexObjectWithTypeReference("/user/"+ userId + "/membership?type=" + permission  + 
				"&start=" + skipBlocks  + "&size=" + blockSize , "", (new TypeReference<Map<UUID,Permissions>>() {}));
	}
	
	// Get network permissions of user as list of memberships
//			user	GET	/user/{userUUID}/network/{permission}/{skipBlocks}/{blockSize}		Membership[]
	@SuppressWarnings("unchecked")
	public List<Membership> getUserNetworkPermissions(String userId, String permission, int skipBlocks, int blockSize) throws IOException {
		return (List<Membership>) ndexRestClient.getNdexObjectList("/user/"+ userId + "/network/" + permission  + "/" + skipBlocks  + "/" + blockSize , "", Membership.class);
	}	*/

	public Map<String,Permissions> getUserNetworkPermission(UUID userId, UUID networkId, boolean directOnly) throws IOException {
		
		return  ndexRestClient.getHashMap("/user/"+ userId + "/permission?networkid=" + networkId  + "&directonly=" + directOnly,
				  "", String.class, Permissions.class);
	}	

	
	public List<Task> getUserTasks( Status status, int skipBlocks, int blockSize) throws IOException {
		String route = "/task?start=" + skipBlocks  + "&size=" + blockSize + 
				   (status == null ? "" : "&status="+status); 
		return (List<Task>) ndexRestClient.getNdexObjectList(route , "", Task.class);
	}	
	
	// Search for users
//	@SuppressWarnings("unchecked")
	public SolrSearchResult<User> findUsers(SimpleQuery query, int skipBlocks, int blockSize) throws JsonProcessingException, IOException, NdexException{
		JsonNode postData = objectMapper.valueToTree(query);
		return ndexRestClient.postSearchQuery("/search/user?start="  + skipBlocks  + "&size=" + blockSize , postData, User.class);
				
				//SolrSearchResult.class);
	}
	

	

	
	/*-----------------------------------------
	 * 
	 *          Network
	 *          
	 * -----------------------------------------
	 */

	
	// Network permissions
	
	// Assign permissions by posting a membership object
//	network	POST	/network/{networkUUID}/member	Membership
/*	public int setNetworkPermission(
			String networkId, 
			Membership membership) throws JsonProcessingException, IOException, NdexException{
		JsonNode postData = objectMapper.valueToTree(membership);
		Object o = null;
		try {
		    o = ndexRestClient.postNdexObject("/network/" + networkId + "/member", postData, int.class);
		} catch (Exception e){
			//System.out.println(e.getMessage());
		}
		
		return(null == o) ? -1 : ((Integer)o).intValue();
	}
	
	// Revoke permissions by deleting a membership object 
	// (note: not actually implemented as an object on the server side)
//	network	DELETE	/network/{networkUUID}/member/{userUUID}
	public void revokeNetworkPermission(String networkId, String userId) throws JsonProcessingException, IOException, NdexException{
		ndexRestClient.delete("/network/" + networkId + "/member/" + userId);
	}
*/	
	// Get network permissions as list of memberships
//	network	GET	/network/{networkUUID}/membership/{skipBlocks}/{blockSize}		List<Membership>
/*	@SuppressWarnings("unchecked")
	public List<Membership> getNetworkPermissions(String networkId, int skipBlocks, int blockSize) throws JsonProcessingException, IOException{
		return (List<Membership>) ndexRestClient.getNdexObjectList("/network/"+ networkId + "/membership/" +  skipBlocks  + "/" + blockSize , "", Membership.class);
	}

	
	// Get network permissions as list of memberships
//	network GET /{networkId}/user/{permission}/{skipBlocks}/{blockSize}		List<Membership>
	@SuppressWarnings("unchecked")
	public List<Membership> getNetworkUserMemberships(
			String networkId, String permission, int skipBlocks, int blockSize) throws JsonProcessingException, IOException{
		return (List<Membership>) 
				ndexRestClient.getNdexObjectList("/network/"+ networkId + "/user/" + permission + "/" +  skipBlocks  + "/" + blockSize , "", Membership.class);
	}
*/	
	// Network Summary objects
	
//	network	GET	/network/{networkUUID}		NetworkSummary
	public NetworkSummary getNetworkSummaryById(UUID networkId) throws IOException, NdexException {
		return (NetworkSummary) ndexRestClient.getNdexObject("/network/"+networkId + "/summary", "", NetworkSummary.class);
	}

	/**
	 * Search for networks by keywords
	 * @param searchString
	 * @param accountName if the accountName is not null, only networks administered by this account
	 *        are returned. This argument will be ignored if it is null. 
	 * @param skipBlocks
	 * @param blockSize
	 * @return
	 * @throws JsonProcessingException
	 * @throws IOException
	 * @throws NdexException 
	 */
public NetworkSearchResult findNetworks(
			String searchString,
			String accountName,
			int skipBlocks, 
			int blockSize) 
			throws JsonProcessingException, IOException, NdexException {
		String route = "/search/network?start=" + skipBlocks+"&size="+ blockSize;		
		JsonNode postData = objectMapper.createObjectNode(); // will be of type ObjectNode
		((ObjectNode) postData).put("searchString", searchString);
		if (accountName != null) ((ObjectNode) postData).put("accountName", accountName);
		return (NetworkSearchResult)ndexRestClient.postNdexObject(route, postData, NetworkSearchResult.class);
		
	}

	
//	network	POST	/network/search/{skipBlocks}/{blockSize}	SimpleNetworkQuery	NetworkSummary[]
/*	@SuppressWarnings("unchecked")
	public ArrayList<NetworkSummary> searchNetwork(
			SimpleNetworkQuery query,
			int skipBlocks, 
			int blockSize) 
			throws JsonProcessingException, IOException {
		String route = "/network/search/" + skipBlocks+"/"+ blockSize;		
		JsonNode postData = objectMapper.valueToTree(query);
		return (ArrayList<NetworkSummary>) ndexRestClient.postNdexObjectList(route, postData, NetworkSummary.class);
	}  */
	
	public NetworkSearchResult findNetworks(
			String searchString,
			String accountName,
			Permissions permissionOnAcc,
			boolean includeGroups,
			int skipBlocks, 
			int blockSize) 
			throws JsonProcessingException, IOException, NdexException {
		String route = "/search/network?start=" + skipBlocks+"&size="+ blockSize;		
		JsonNode postData = objectMapper.createObjectNode(); // will be of type ObjectNode
		((ObjectNode) postData).put("searchString", searchString);
        ((ObjectNode) postData).put("includeGroups", Boolean.toString(includeGroups));
		if (accountName != null) ((ObjectNode) postData).put("accountName", accountName);
		if ( permissionOnAcc !=null) ((ObjectNode) postData).put("permission", permissionOnAcc.toString());
		return (NetworkSearchResult )ndexRestClient.postNdexObject(route, postData, NetworkSearchResult.class);
		
	}
	
	public void deleteNetwork(UUID id) throws IOException, NdexException{
		ndexRestClient.delete("/network/" + id);
	}
	

	

	
	public InputStream getNetworkAsCXStream(UUID id) throws JsonProcessingException, IOException, NdexException {
		String route = "/network/" + id ;
		return  ndexRestClient.getStream(route, "");
	}

	public NiceCXNetwork getNetwork(UUID id) throws JsonProcessingException, IOException, NdexException {
		
		try (InputStream is = getNetworkAsCXStream(id)) {
			return NdexRestClientUtilities.getCXNetworkFromStream(is);
		}
	}

	public InputStream getNetworkAspectElements(UUID id, String aspectName, int limit) throws JsonProcessingException, IOException, NdexException {

		String route = "/network/" + id + "/aspect/"+aspectName + "?size=" + limit;
		return  ndexRestClient.getStream(route, "");
	}

	public <T> List<T> getNetworkAspect(UUID id, String aspect, int limit, Class<T> mappedClass)
			    throws JsonProcessingException, IOException, NdexException {
		String route = "/network/" + id + "/aspect/"+aspect + "?size=" + limit;
		return  ndexRestClient.getNdexObjectList(route, "", mappedClass);

	}
	
/*	public InputStream getNetworkAspects(UUID id, Collection<String> aspects) throws JsonProcessingException, IOException, NdexException {
		String route = "/network/" + id + "/aspects";
	//	return  ndexRestClient.getStream(route, "");
	  	JsonNode postData = objectMapper.valueToTree(aspects);
    	return  ndexRestClient.postNdexObject(route, postData);

	} */

	public void setSampleNetwork (UUID networkId, InputStream sampleNetworkStream) throws IOException, NdexException {
		ndexRestClient.putStream("/network/" + networkId + "/sample", sampleNetworkStream );
	}
	
/*	public void setSampleNetwork (UUID networkId, String sampleNetworkString) {
		
	} */
	
	public NiceCXNetwork getSampleNetwork ( UUID networkId ) throws JsonProcessingException, IOException, NdexException {
		try (InputStream s = ndexRestClient.getStream("/network/" , networkId + "/sample")) {
			return NdexRestClientUtilities.getCXNetworkFromStream(s);
		}
	}
	
	public InputStream getNeighborhoodAsCXStream(String id, CXSimplePathQuery query) throws JsonProcessingException, IOException, NdexException {
		String route = "/network/" + id + "/asCX/query";
		JsonNode postData = objectMapper.valueToTree(query);
	    return  ndexRestClient.postNdexObject(route, postData);
	}
	
	
		

// network	GET	/export/{networkId}/{format} String
	//TODO: Need to be rewritten
/*	public String exportNetwork(String networkId, String fileFormat)  {
		String route = "/network/export/" + networkId + "/" + fileFormat;
        String value = null;
        try {
        	value = ndexRestClient.getString(route, "");
        } catch (IOException e) {
        	System.out.println("e.getMessage()=" + e.getMessage());
        }
		return value;
	}	*/
	

	// Update network profile
//	network	POST	/network/{networkUUID}/summary	Network	NetworkSummary
	public NetworkSummary updateNetworkSummary(NetworkSummary networkSummary, String networkId) throws Exception {
		String route = "/network/" + networkId + "/summary";
		JsonNode postData = objectMapper.valueToTree(networkSummary);
		return (NetworkSummary) ndexRestClient.postNdexObject(route, postData, NetworkSummary.class);
	}	
	

	//	network	PUT	/network/{networkUUID}/properties		
	public void setNetworkProperties(UUID networkId,
			 List<NdexPropertyValuePair> properties) throws IllegalStateException, Exception {
		String route = "/network/" + networkId + "/properties";	
		JsonNode putData = objectMapper.valueToTree(properties);
		ndexRestClient.putNdexObject(route, putData); 
	}
	
	// Get network provenance object
//	network	GET	/network/{networkUUID}/provenance		Provenance
	public ProvenanceEntity getNetworkProvenance(
			UUID networkId) 
			throws JsonProcessingException, IOException, NdexException {
		String route = "/network/" + networkId + "/provenance";		
		return (ProvenanceEntity) ndexRestClient.getNdexObject(route, "", ProvenanceEntity.class);
	}
	
	// Update network provenance object
//	network	PUT	/network/{networkUUID}/provenance	Provenance	
	public void setNetworkProvenance(
			UUID networkId,
			ProvenanceEntity provenance) 
			throws IllegalStateException, Exception {
		String route = "/network/" + networkId + "/provenance";	
		JsonNode putData = objectMapper.valueToTree(provenance);
	    ndexRestClient.putNdexObject(route, putData);
	}
	


    //	network	GET	/network/{networkId}/setFlag/{parameter}={value}	
    //  current supported parameters are   "readOnly={true|false}"
	public void setNetworkSystemProperty(UUID networkId, Map<String,Object> properties) throws IllegalStateException, Exception  {
		String route = "/network/" + networkId +"/systemproperty" ;
        ndexRestClient.putNdexObject(route, objectMapper.valueToTree(properties));    
	}

	//@POST
	//@Path("/{networkId}/asNetwork/prototypeNetworkQuery")
	//@Produces("application/json")
	
//  network	POST	/network/{networkUUID}/asNetwork/prototypeNetworkQuery
/*	public Network queryNetworkByEdgeFilter(String networkUUID, EdgeCollectionQuery query) throws JsonProcessingException, IOException, NdexException {
		String route = "/network/" + networkUUID +"/asNetwork/prototypeNetworkQuery";
		JsonNode postData = objectMapper.valueToTree(query);
		return (Network) ndexRestClient.postNdexObject(route, postData, Network.class);
	} */


    public UUID createCXNetwork (InputStream input) throws IllegalStateException, Exception {
    	  CloseableHttpClient client = HttpClients.createDefault();
    	  HttpPost httpPost = new HttpPost( ndexRestClient.getBaseroute() + "/network");

    	  try
          {
              //Set various attributes
    		  HttpEntity multiPartEntity = MultipartEntityBuilder.create()
            		  				.addBinaryBody("CXNetworkStream", input,ContentType.create("application/octet-stream"), "filename").build();
   
              //Set to request body
              httpPost.setEntity(multiPartEntity) ;
 
           	  UsernamePasswordCredentials creds = 
            	      new UsernamePasswordCredentials(ndexRestClient.getUsername(),ndexRestClient.getPassword());
              httpPost.addHeader(new BasicScheme().authenticate(creds, httpPost, null));
                       
              //Send request
      	    try (CloseableHttpResponse response = client.execute(httpPost) ) {
               
              //Verify response if any
              if (response != null)
              {
                  if ( response.getStatusLine().getStatusCode() != 201) {
                	  
                	  Exception e = createNdexSpecificException(response);
                	  throw e;
                  }
                  try (InputStream in = response.getEntity().getContent()) {
                	  StringWriter writer = new StringWriter();
                	  IOUtils.copy(in, writer, "UTF-8");
                	  String theString = writer.toString();
                	  int pos = theString.lastIndexOf("/");
                	  String uuidStr = theString.substring(pos+1);
             //     System.out.println(uuidStr);
                  
                	  UUID networkId = UUID.fromString(uuidStr);
                  	return networkId;
                  }
              }
              
              throw new NdexException ("No response from the server.");
      	    }
          }  finally {
        	    client.close();
          }

    }
	
	private static  Exception createNdexSpecificException(
			CloseableHttpResponse response) throws JsonParseException, JsonMappingException, IllegalStateException, IOException {

			ObjectMapper mapper = new ObjectMapper();
			NDExError ndexError = mapper.readValue(response.getEntity().getContent(), NDExError.class);
			
			switch (response.getStatusLine().getStatusCode() ) {
	            case (HttpURLConnection.HTTP_UNAUTHORIZED):
	    	        // httpServerResponseCode is HTTP Status-Code 401: Unauthorized.
	    	        return new UnauthorizedOperationException(ndexError);
	        
		        case (HttpURLConnection.HTTP_NOT_FOUND):
		    	    // httpServerResponseCode is HTTP Status-Code 404: Not Found.
		    	    return  new ObjectNotFoundException(ndexError);
		    
			    case (HttpURLConnection.HTTP_CONFLICT):
			    	// httpServerResponseCode is HTTP Status-Code 409: Conflict.
			    	return new DuplicateObjectException(ndexError);
			    
			    case (HttpURLConnection.HTTP_FORBIDDEN):
			    	// httpServerResponseCode is HTTP Status-Code 403: Forbidden.
			    	return  new ForbiddenOperationException(ndexError);
			    
			    default:
			    	// default case is: HTTP Status-Code 500: Internal Server Error.
			    	return new NdexException(ndexError);
			}
		}



	
	   public void updateCXNetwork (UUID networkUUID, InputStream input) throws IllegalStateException, Exception {
	    	  CloseableHttpClient client = HttpClients.createDefault();
	    	  HttpPut httpPost = new HttpPut(ndexRestClient.getBaseroute() + "/network/" + networkUUID.toString());

	    	  try
	          {
	              //Set various attributes
	    		  HttpEntity multiPartEntity = MultipartEntityBuilder.create()
	            		  				.addBinaryBody("CXNetworkStream", input,ContentType.create("application/octet-stream"), "filname").build();
	   
	              //Set to request body
	              httpPost.setEntity(multiPartEntity) ;
	 
	           	  UsernamePasswordCredentials creds = 
	            	      new UsernamePasswordCredentials(ndexRestClient.getUsername(),ndexRestClient.getPassword());
	              httpPost.addHeader(new BasicScheme().authenticate(creds, httpPost, null));
	                       
	              //Send request
	      	      try (CloseableHttpResponse response = client.execute(httpPost)) {
	               
	      	    	  //Verify response code
	      	    	  if (response != null)
	      	    	  {
	      	    		  if ( response.getStatusLine().getStatusCode() != 204) {
	      	    			  throw createNdexSpecificException(response);
	      	    		  }
	      	    		  return;
	      	    	  }
	      	    	  throw new NdexException ("No response from the server.");
	      	      }
	          }  finally {
	        	    client.close();
	          }

	    }
	
	
	/*-----------------------------------------
	 * 
	 *          Archive
	 *          
	 * -----------------------------------------
	 */
 
    /*
     
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
	
    // update when we enable property search again
     
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
	

    private UUID createCXNetworkRestClient (InputStream input) {
        ResteasyProviderFactory factory = ResteasyProviderFactory.getInstance();

   // this line is only needed if you run this as a java console app.
   //  in tomcat and jboss initialization should work without this
   ResteasyProviderFactory.pushContext(javax.ws.rs.ext.Providers.class, factory);

   ResteasyClient client =null;
   Response r = null;
          ResteasyClientBuilder resteasyClientBuilder = new
                      ResteasyClientBuilder().providerFactory(factory);

           client = resteasyClientBuilder.build();

           // insert the url of the webservice here
        ResteasyWebTarget target = client.target( ndexRestClient.getBaseroute() + "/network/asCX");
        target.register(new BasicAuthentication(ndexRestClient.getUsername(),ndexRestClient.getPassword()));
         MultipartFormDataOutput mdo = new MultipartFormDataOutput();

         mdo.addFormData("CXNetworkStream", input, MediaType.APPLICATION_OCTET_STREAM_TYPE);

         GenericEntity<MultipartFormDataOutput> entity = new GenericEntity<MultipartFormDataOutput>(mdo) {};

         //Upload File
         r = target.request().post(Entity.entity(entity, MediaType.MULTIPART_FORM_DATA_TYPE));

         // Read File Response
         String  response =  r.readEntity(String.class);

          if (r != null) r.close();
          if (client != null) client.close();

          return UUID.fromString(response);
    }
	
*/
	   
	   /*	
		// Get requests related to user that are pending
//				user	GET	/user/{userUUID}/request/pending/{skipBlocks}/{blockSize}		Request[]
		@SuppressWarnings("unchecked")
		public List<Request> getUserPendingRequests(String userId, int skipBlocks, int blockSize) throws IOException {
			return (List<Request>) ndexRestClient.getNdexObjectList("/user/"+ userId + "/request/pending/"  + skipBlocks  + "/" + blockSize , "", Request.class);
		}
		
		// Get requests related to user
//				user	GET	/user/{userUUID}/request/{skipBlocks}/{blockSize}		Request[]
		@SuppressWarnings("unchecked")
		public List<Request> getUserRequests(String userId, int skipBlocks, int blockSize) throws IOException {
			return (List<Request>) ndexRestClient.getNdexObjectList("/user/"+ userId + "/request/"  + skipBlocks  + "/" + blockSize , "", Request.class);
		}
		
		// Get permission of user for resource as a membership
//				user	GET	/user/{userUUID}/membership/{resourceUUID}		Membership
		public Membership getResourcePermission(String userId, String resourceId) throws IOException, NdexException {
			return (Membership) ndexRestClient.getNdexObject("/user/"+ userId + "/membership/" + resourceId, "", Membership.class);
		}
	*/	
	   
		// Generate forgotten password email to user
//		user	GET	/user/{UUID}/forgotPassword	
/*	public User generateForgottenPasswordEmail(String userId) throws IOException, NdexException {
	return (User) ndexRestClient.getNdexObject("/user/"+ userId + "/forgotPassword", "", User.class);
}  */

// Update user email address, alerting user via old email
// TODO
//		user	POST	/user/emailAddress	string	
/*
public User updateUserEmail(User user, String newEmail) throws JsonProcessingException, IOException{
	JsonNode postData = TBD;
	return (User)ndexRestClient.postNdexObject("/user/" + user.getExternalId() , postData, User.class);
}
*/

// Change user password
// TODO
//			user	POST	/user/password	string	
// 
// ATTENTION: in case password has been successfully changed on the server, it will be changed on the 
//            client side as well.
//
/*	public boolean changePassword(String newPassword) throws JsonProcessingException, IOException, NdexException {
	
	boolean success = false;
	
	int returnCode = ndexRestClient.postString("/user/password", newPassword, User.class);
	
	if (HttpURLConnection.HTTP_NO_CONTENT == returnCode) {
		success = true;
		this.ndexRestClient.setCredentials( this.ndexRestClient.getUsername(), newPassword);
	}
	return success;
} */

		// Create a user
//		user	POST	/user	NewUser	User
/*	public UUID createUser(User user) throws JsonProcessingException, IOException, NdexException{
	JsonNode postData = objectMapper.valueToTree(user);
	return ndexRestClient.createNdexObjectByPost("/user", postData);
} */

// Update a user
//		user	POST	/user/{UUID}	User	User
/*	public User updateUser(User user) throws JsonProcessingException, IOException, NdexException{
	JsonNode postData = objectMapper.valueToTree(user);
	return (User)ndexRestClient.postNdexObject("/user/" + user.getExternalId() , postData, User.class);
}  */
	   
		// Delete user (actually implemented as deprecate)
		// Fails unless the authenticated user is the user to delete...
//				user	DELETE	/user	
	/*	public void deleteUser(UUID id) throws JsonProcessingException, IOException, NdexException{
			ndexRestClient.delete("/user/" + id);
		} */
		
		// delete the authenticated user (self)
		/*public void deleteUser() throws JsonProcessingException, IOException, NdexException {
			if (ndexRestClient.getUserUid() == null)
				throw new UnauthorizedOperationException ("Anonymous user can't be deleted.");
			else 
				deleteUser(ndexRestClient.getUserUid());
		}*/
	   
}
