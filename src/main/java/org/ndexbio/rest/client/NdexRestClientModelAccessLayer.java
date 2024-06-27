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
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.ndexbio.cxio.core.readers.NiceCXNetworkReader;
import org.ndexbio.cxio.metadata.MetaDataCollection;
import org.ndexbio.model.cx.NiceCXNetwork;
import org.ndexbio.model.exceptions.BadRequestException;
import org.ndexbio.model.exceptions.NdexException;
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.FileInputStream;
import java.util.HashMap;
import org.ndexbio.model.object.NdexObjectUpdateStatus;
import org.ndexbio.model.object.NetworkSet;

/**
 * Provides wrapper methods for NDEx REST server api. 
 * 
 * For more information about the api visit: http://ndexbio.org
 * 
 * @author chenjing
 */
public class NdexRestClientModelAccessLayer 
{
	NdexRestClient ndexRestClient = null;
	ObjectMapper objectMapper = null;
	User currentUser = null;
	HashMap<String, String> jsonAcceptContentRequestProperties = new HashMap<>();
	HashMap<String, String> txtAcceptJsonContentRequestProperties = new HashMap<>();

	/**
	 * 
	 * @param client
	 */
	public NdexRestClientModelAccessLayer(NdexRestClient client) {
		super();
		ndexRestClient = client;
		objectMapper = new ObjectMapper();
		jsonAcceptContentRequestProperties.put("Content-Type", "application/json");
		jsonAcceptContentRequestProperties.put("Accept", "application/json");
		
		txtAcceptJsonContentRequestProperties.put("Content-Type", "application/json");
		txtAcceptJsonContentRequestProperties.put("Accept", "text/plain");
		
	}
	

	/*-----------------------------------------
	 * 
	 *          Server
	 *          
	 * -----------------------------------------
	 */
	
	/**
	 * Get the client attached to the this data access object.
	 * @return
	 */
	public NdexRestClient getNdexRestClient() { return ndexRestClient; }
	
	/**
	 * Get the current status of the NDEx server in the standard format.
	 * @return
	 * @throws IOException
	 * @throws NdexException
	 */
    public NdexStatus getServerStatus() throws IOException, NdexException {
		return getServerStatus(false);	
    }

    /**
     * Get the status of the NDEx server.
     * @param fullFormat status in 'full format' contains more information about the server such registered importers and exporters on this server.
     * @return
     * @throws IOException
     * @throws NdexException
     */
    public NdexStatus getServerStatus(boolean fullFormat) throws IOException, NdexException {
    	final String route = NdexApiVersion.v2 + "/admin/status?format=" + (fullFormat? "full": "standard") ;

    	NdexStatus status = this.ndexRestClient.getNdexObject(route, "", NdexStatus.class);
    	
		return status;	
    }

	/*-----------------------------------------
	 * 
	 *          Group
	 *          
	 * -----------------------------------------
	 */
	

    /**
     * Get group by id.
     * @param groupId
     * @return
     * @throws IOException
     * @throws NdexException
     */
	public Group getGroup(UUID groupId) throws IOException, NdexException {
		return ndexRestClient.getNdexObject(NdexApiVersion.v2 + "/group/"+groupId, "", Group.class);
	}
	
	
	/**
	 * Update a group.  
	 * @param group
	 * @throws IllegalStateException
	 * @throws Exception
	 */
	public void updateGroup(Group group) throws IllegalStateException, Exception{
		JsonNode postData = objectMapper.valueToTree(group);
		ndexRestClient.putNdexObject(NdexApiVersion.v2 + "/group/" + group.getExternalId() , postData);
	}
	

	/**
	 * Find NDEx groups that meet the search criteria. 
	 * @param query
	 * @param skipBlocks specifies that the result is the nth page of the requested data. 
	 * @param blockSize  specifies the number of data items in each page.
	 * @return   Returns a SolrSearchResult object which contains an array of Group objects and the total hit count of the search. 
	 * @throws JsonProcessingException
	 * @throws IOException
	 * @throws NdexException
	 */
	public SolrSearchResult<Group> findGroups(SimpleQuery query, int skipBlocks, int blockSize) throws JsonProcessingException, IOException, NdexException{
		JsonNode postData = objectMapper.valueToTree(query);
		return ndexRestClient.postSearchQuery(NdexApiVersion.v2 + "/search/group?start="  + skipBlocks  + "&size=" + blockSize , postData, Group.class);
	}
	


	/**
	 * Create a NDEx group.
	 * @param group
	 * @return
	 * @throws JsonProcessingException
	 * @throws IOException
	 * @throws NdexException
	 */
	public UUID createGroup(Group group) throws JsonProcessingException, IOException, NdexException{
		JsonNode postData = objectMapper.valueToTree(group);
		return ndexRestClient.createNdexObjectByPost(NdexApiVersion.v2 + "/group", postData);
	}
	
	// Delete a group
//			group	DELETE	/group/{groupUUID}	
	/**
	 * Delete the NDEx group by its id.
	 * @param groupId
	 * @throws JsonProcessingException
	 * @throws IOException
	 * @throws NdexException
	 */
	public void deleteGroup(UUID groupId) throws JsonProcessingException, IOException, NdexException{
		ndexRestClient.delete(NdexApiVersion.v2 + "/group/" + groupId.toString());
	}	
	
	// Add or modify account permission for a group by posting a membership
//			group	POST	/group/{groupUUID}/member	Membership	
/*	public Membership setGroupPermission(
			String groupId, 
			Membership membership) throws JsonProcessingException, IOException, NdexException{
		JsonNode postData = objectMapper.valueToTree(membership);
		return (Membership)ndexRestClient.postNdexObject("/group/" + groupId + "/member", postData, Membership.class);
	}  */
	
	/*
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


	/**
	 * Get a task object by its Id.
	 * @param taskId
	 * @return
	 * @throws IOException
	 * @throws NdexException
	 */
	public Task getTask(UUID taskId) throws IOException, NdexException {
		return ndexRestClient.getNdexObject(NdexApiVersion.v2 + "/task/"+ taskId, "", Task.class);
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
		ndexRestClient.delete(NdexApiVersion.v2 + "/task/" + taskId);
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
		return ndexRestClient.getNdexObject(NdexApiVersion.v2 + "/user/"+userCreated, "", User.class);
	}
	
	/**
	 * This is a function designed to support “My Account” pages in NDEx web applications. It returns a list of NetworkSummary objects that are 
	 *  displayed in the user's home page.
 	 *	Only an authenticated user can use this function,
	 *  otherwise an NdexException will be raised.
	 * @return A list of NetworkSummary.
	 * @throws JsonProcessingException
	 * @throws IOException
	 * @throws NdexException 
	 */
	public List<NetworkSummary> getMyNetworks() 
			throws JsonProcessingException, IOException, NdexException {
		return getMyNetworks(0,-1);

	}

	/**
	 * This is a function designed to support “My Account” pages in NDEx web applications. It returns a list of NetworkSummary objects that are 
	 *  displayed in the user's home page.  
	 * @param offset
	 * @param limit
	 * @return
	 * @throws JsonProcessingException
	 * @throws IOException
	 * @throws NdexException
	 */
	public List<NetworkSummary> getMyNetworks(int offset, int limit) 
			throws JsonProcessingException, IOException, NdexException {
		
		UUID userId = ndexRestClient.getUserUid();
		if ( userId == null) {
			throw new NdexException("Client object has not sign in to NDEx server yet.");
		}
		
		final String route = NdexApiVersion.v2 + "/user/"+ userId + "/networksummary?offset="+offset+"&limit="+ limit;		
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

	/**
	 * Get the type(s) of permission assigned to the authenticated user for the specified network. 
	 * @param userId
	 * @param networkId
	 * @param directOnly If directonly is set to true, permissions granted through groups are not included in the result. 
	 * @return A map which maps a network UUID to the highest permission assigned to the authenticated user. 
	 * @throws IOException
	 * @throws NdexException 
	 */
	public Map<String,Permissions> getUserNetworkPermission(UUID userId, UUID networkId, boolean directOnly) throws IOException, NdexException {
		
		return  ndexRestClient.getHashMap(NdexApiVersion.v2 + "/user/"+ userId + "/permission?networkid=" + networkId  + "&directonly=" + directOnly,
				  "", String.class, Permissions.class);
	}	

	/**
	 * Returns a list of Task objects owned by the authenticated user with the specified status. 
	 * @param status 
	 * @param skipBlocks
	 * @param blockSize
	 * @return
	 * @throws IOException
	 * @throws NdexException 
	 */
	public List<Task> getUserTasks( Status status, int skipBlocks, int blockSize) throws IOException, NdexException {
		final String route = NdexApiVersion.v2 + "/task?start=" + skipBlocks  + "&size=" + blockSize + 
				   (status == null ? "" : "&status="+status); 
		return ndexRestClient.getNdexObjectList(route , "", Task.class);
	}	
	
	// Search for users
//	@SuppressWarnings("unchecked")
	public SolrSearchResult<User> findUsers(SimpleQuery query, int skipBlocks, int blockSize) throws JsonProcessingException, IOException, NdexException{
		JsonNode postData = objectMapper.valueToTree(query);
		return ndexRestClient.postSearchQuery(NdexApiVersion.v2 + "/search/user?start="  + skipBlocks  + "&size=" + blockSize , postData, User.class);
				
				//SolrSearchResult.class);
	}
	
        /* ----------------------------------------
         *
         *          Network Set
         *
         * ----------------------------------------
         */
        
        /**
         * 
         * @param networkSetId id of network set
         * @param accessKey optional key that allows any user to have read 
         *                  access to this network set regardless if that user 
         *                  has the READ privilege on this network. The access
         *                  key function must be enabled. 
         * @return NetworkSet object upon success otherwise null
         * @throws IOException if there was an error with query
         * @throws NdexException if there was an error with query
         * @throws IllegalArgumentException if networkSetId is null
         */
        public NetworkSet getNetworkSetById(UUID networkSetId, 
                                            final String accessKey) throws IllegalArgumentException, 
                IOException, NdexException {
            
            if (networkSetId == null){
                throw new IllegalArgumentException("networkSetId is null");
            }
            String query = "";
            if (accessKey != null){
                query = "?accesskey=" + accessKey;
            }
            return ndexRestClient.getNdexObject(NdexApiVersion.v2 + "/networkset/"+networkSetId,
                                                             query, NetworkSet.class);
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
		return ndexRestClient.getNdexObject(NdexApiVersion.v2 + "/network/"+networkId + "/summary", "", NetworkSummary.class);
	}

	public NetworkSummary getNetworkSummaryById(UUID networkId, String accessKey) throws IOException, NdexException {
		
		String route = NdexApiVersion.v2 + "/network/"+networkId + "/summary";
		if ( accessKey != null) {
			route += "?accesskey="+accessKey;
		}
		
		return ndexRestClient.getNdexObject(route, "", NetworkSummary.class);
	}

	
	public List<NetworkSummary> getNetworkSummariesByIds(Collection<UUID> networkIds) throws IOException, NdexException {
		
		final String route = NdexApiVersion.v2 + "/batch/network/summary";
		
		ArrayNode array = objectMapper.createArrayNode();
		
		array.addAll(networkIds.stream().map(e -> objectMapper.createObjectNode().textNode(e.toString())).collect(Collectors.toList()) ) ;
		return  ndexRestClient.postNdexList(route, array, NetworkSummary.class);
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
		final String route = NdexApiVersion.v2 + "/search/network?start=" + skipBlocks+"&size="+ blockSize;		
		JsonNode postData = objectMapper.createObjectNode(); // will be of type ObjectNode
		((ObjectNode) postData).put("searchString", searchString);
		if (accountName != null) ((ObjectNode) postData).put("accountName", accountName);
		return ndexRestClient.postNdexObject(route, postData, NetworkSearchResult.class);
		
	}

	
	public NetworkSearchResult findNetworks(
			String searchString,
			String accountName,
			Permissions permissionOnAcc,
			boolean includeGroups,
			int skipBlocks, 
			int blockSize) 
			throws JsonProcessingException, IOException, NdexException {
		final String route = NdexApiVersion.v2 + "/search/network?start=" + skipBlocks+"&size="+ blockSize;		
		JsonNode postData = objectMapper.createObjectNode(); // will be of type ObjectNode
		((ObjectNode) postData).put("searchString", searchString);
        ((ObjectNode) postData).put("includeGroups", Boolean.toString(includeGroups));
		if (accountName != null) ((ObjectNode) postData).put("accountName", accountName);
		if ( permissionOnAcc !=null) ((ObjectNode) postData).put("permission", permissionOnAcc.toString());
		return ndexRestClient.postNdexObject(route, postData, NetworkSearchResult.class);
		
	}
	
	public void deleteNetwork(UUID id) throws IOException, NdexException{
		ndexRestClient.delete(NdexApiVersion.v2 + "/network/" + id);
	}
	
	public InputStream getNetworkAsCXStream(UUID id) throws JsonProcessingException, IOException, NdexException {
		final String route = NdexApiVersion.v2 + "/network/" + id ;
		return  ndexRestClient.getStream(route, "");
	}

	public InputStream getNetworkAsCX2Stream(UUID id) throws JsonProcessingException, IOException, NdexException {
		final String route = NdexApiVersion.v3 + "/networks/" + id ;
		return  ndexRestClient.getStream(route, "");
	}
	
	public InputStream getNetworkAsCXStream(UUID id, String accessKey) throws JsonProcessingException, IOException, NdexException {
		String route = NdexApiVersion.v2 + "/network/" + id ;
		if ( accessKey != null) {
			route += "?accesskey="+accessKey;
		}
		return  ndexRestClient.getStream(route, "");
	}

	public InputStream getNetworkAsCX2Stream(UUID id, String accessKey) throws JsonProcessingException, IOException, NdexException {
		String route = NdexApiVersion.v3 + "/networks/" + id ;
		if ( accessKey != null) {
			route += "?accesskey="+accessKey;
		}
		return  ndexRestClient.getStream(route, "");
	}

	public NiceCXNetwork getNetwork(UUID id) throws JsonProcessingException, IOException, NdexException {
		
		try (InputStream is = getNetworkAsCXStream(id)) {
			NiceCXNetworkReader reader = new NiceCXNetworkReader();
			return reader.readNiceCXNetwork(is);
			//return NdexRestClientUtilities.getCXNetworkFromStream(is);
		}
	}

	public InputStream getNetworkAspectElements(UUID id, String aspectName, int limit) throws JsonProcessingException, IOException, NdexException {

		final String route = NdexApiVersion.v2 + "/network/" + id + "/aspect/"+aspectName + "?size=" + limit;
		return  ndexRestClient.getStream(route, "");
	}

	public <T> List<T> getNetworkAspect(UUID id, String aspect, int limit, Class<T> mappedClass)
			    throws JsonProcessingException, IOException, NdexException {
		final String route = NdexApiVersion.v2 + "/network/" + id + "/aspect/"+aspect + "?size=" + limit;
		return  ndexRestClient.getNdexObjectList(route, "", mappedClass);

	}
	
	public MetaDataCollection getNetworkMetadata(UUID id)
		    throws JsonProcessingException, IOException, NdexException {
	final String route = NdexApiVersion.v2 + "/network/" + id + "/aspect";
	return  ndexRestClient.getNdexObject(route, "", MetaDataCollection.class);

}

/*	public InputStream getNetworkAspects(UUID id, Collection<String> aspects) throws JsonProcessingException, IOException, NdexException {
		String route = "/network/" + id + "/aspects";
	//	return  ndexRestClient.getStream(route, "");
	  	JsonNode postData = objectMapper.valueToTree(aspects);
    	return  ndexRestClient.postNdexObject(route, postData);

	} */

	public void setSampleNetwork (UUID networkId, InputStream sampleNetworkStream) throws IOException, NdexException {
		ndexRestClient.putStream(NdexApiVersion.v2 + "/network/" + networkId + "/sample", sampleNetworkStream );
	}
	
/*	public void setSampleNetwork (UUID networkId, String sampleNetworkString) {
		
	} */
	
	public NiceCXNetwork getSampleNetwork ( UUID networkId ) throws JsonProcessingException, IOException, NdexException {
		try (InputStream s = ndexRestClient.getStream(NdexApiVersion.v2 + "/network/" , networkId + "/sample")) {
			
			NiceCXNetworkReader reader = new NiceCXNetworkReader();
			return reader.readNiceCXNetwork(s);
			//return NdexRestClientUtilities.getCXNetworkFromStream(s);
		}
	}
	
	public UUID cloneNetwork (UUID networkId) throws JsonProcessingException, IOException, NdexException {
		return ndexRestClient.createNdexObjectByPost(NdexApiVersion.v2 +"/network/" + networkId + "/copy", null);
		
	}
	
	
	public InputStream getNeighborhoodAsCXStream(String id, CXSimplePathQuery query) throws JsonProcessingException, IOException, NdexException {
		
		int depth = query.getSearchDepth();
		if ( depth <1 || depth >5) {
			throw new BadRequestException ("Received query depth "+ depth + ". It has to be an integer between 1 and 4.");
		}
		final String route = NdexApiVersion.v2 + "/network/" + id + "/asCX/query";
		JsonNode postData = objectMapper.valueToTree(query);
	    return  ndexRestClient.postNdexObject(route, postData);
	}
	
	

	// Update network profile
//	network	POST	/network/{networkUUID}/summary	Network	NetworkSummary
	public NetworkSummary updateNetworkSummary(NetworkSummary networkSummary, String networkId) throws Exception {
		final String route = NdexApiVersion.v2 + "/network/" + networkId + "/summary";
		JsonNode postData = objectMapper.valueToTree(networkSummary);
		return ndexRestClient.postNdexObject(route, postData, NetworkSummary.class);
	}	
	

	//	network	PUT	/network/{networkUUID}/properties		
	public void setNetworkProperties(UUID networkId,
			 List<NdexPropertyValuePair> properties) throws IllegalStateException, Exception {
		final String route = NdexApiVersion.v2 + "/network/" + networkId + "/properties";	
		JsonNode putData = objectMapper.valueToTree(properties);
		ndexRestClient.putNdexObject(route, putData); 
	}
	
	@Deprecated
	public ProvenanceEntity getNetworkProvenance(
			UUID networkId) 
			throws JsonProcessingException, IOException, NdexException {
		final String route = NdexApiVersion.v2 + "/network/" + networkId + "/provenance";		
		return  ndexRestClient.getNdexObject(route, "", ProvenanceEntity.class);
	}	

	@Deprecated
	public void setNetworkProvenance(
			UUID networkId,
			ProvenanceEntity provenance) 
			throws IllegalStateException, Exception {
		final String route = NdexApiVersion.v2 + "/network/" + networkId + "/provenance";	
		JsonNode putData = objectMapper.valueToTree(provenance);
	    ndexRestClient.putNdexObject(route, putData);
	}
	


    //	network	GET	/network/{networkId}/setFlag/{parameter}={value}	
    //  current supported parameters are   "readOnly={true|false}"
	public void setNetworkSystemProperty(UUID networkId, Map<String,Object> properties) throws IllegalStateException, Exception  {
		final String route = NdexApiVersion.v2 + "/network/" + networkId +"/systemproperty" ;
        ndexRestClient.putNdexObject(route, objectMapper.valueToTree(properties));    
	}


    public UUID createCXNetwork (InputStream input) throws IllegalStateException, Exception {
    	return createNetwork(input, NdexApiVersion.v2 + "/network", txtAcceptJsonContentRequestProperties).getUuid();
    }
    
    public UUID createCX2Network (InputStream input) throws IllegalStateException, Exception {
  	  return createNetwork(input, NdexApiVersion.v3 + "/networks", jsonAcceptContentRequestProperties).getUuid();
    }
    
	private NdexObjectUpdateStatus createNetwork(InputStream input, final String route,
			final Map<String, String> requestProperties) throws IllegalStateException, Exception {

		ObjectMapper mapper = new ObjectMapper();
		HttpURLConnection con = ndexRestClient.createReturningConnection(route, input, "POST",
				requestProperties);
		if (con.getResponseCode() != HttpURLConnection.HTTP_CREATED){
			ndexRestClient.processNdexSpecificException(con.getInputStream(), con.getResponseCode(), mapper);
		}
		try (InputStream in = con.getInputStream()) {
			StringWriter writer = new StringWriter();
			IOUtils.copy(in, writer, "UTF-8");
			String theString = writer.toString();
			
			// if Accept is text/plain that we should expect a URL
			// with network id
			if (requestProperties.get("Accept").startsWith("text")){
				int pos = theString.lastIndexOf("/");
				String uuidStr = theString.substring(pos+1);
				NdexObjectUpdateStatus status = new NdexObjectUpdateStatus();
				status.setUuid(UUID.fromString(uuidStr));
				return status;
			}
			return mapper.readValue(theString, NdexObjectUpdateStatus.class);
		}
	}
	
	/**
    private UUID createNetworkOld (InputStream input, boolean isCX2) throws IllegalStateException, Exception {
    	  CloseableHttpClient client = HttpClients.createDefault();
    	  HttpPost httpPost = new HttpPost( ndexRestClient.getBaseroute() + 
    			  (isCX2? (NdexApiVersion.v3 + "/networks"):(NdexApiVersion.v2 + "/network")) );

    	  try
          {
              //Set various attributes
    		  HttpEntity multiPartEntity = MultipartEntityBuilder.create()
            		  				.addBinaryBody("CXNetworkStream", input,ContentType.create("application/octet-stream"), "filename").build();
   
              //Set to request body
              httpPost.setEntity (multiPartEntity) ;
              httpPost.addHeader(HttpHeaders.AUTHORIZATION, ndexRestClient.getAuthenticationString());
              httpPost.addHeader( "User-Agent", ndexRestClient.getUserAgent());
                       
              //Send request
      	    try (CloseableHttpResponse response = client.execute(httpPost) ) {
               
              //Verify response if any
              if (response != null)
              {
                  if ( response.getCode() != HttpStatus.SC_CREATED) {
					  ndexRestClient.processNdexSpecificException(response.getEntity().getContent(), response.getCode(), new ObjectMapper());
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
	*/
		public void updateCXNetwork (UUID networkUUID, InputStream input) throws IllegalStateException, Exception {
			updateNetwork (networkUUID, input, NdexApiVersion.v2 + "/network");
		}	   

		public void updateCX2Network (UUID networkUUID, InputStream input) throws IllegalStateException, Exception {
			updateNetwork (networkUUID, input,NdexApiVersion.v3 + "/networks");
		}	   
	   
		private void updateNetwork(UUID networkUUID, InputStream input, final String route) throws IllegalStateException, Exception {
			HttpURLConnection con = ndexRestClient.createReturningConnection(route + "/" + networkUUID.toString(),
					input, "PUT",
					jsonAcceptContentRequestProperties);
			if (con.getResponseCode() != HttpURLConnection.HTTP_NO_CONTENT){
				ndexRestClient.processNdexSpecificException(con.getInputStream(), con.getResponseCode(), new ObjectMapper());
			}
	   }
	
		/**
	   private void updateNetworkOld (UUID networkUUID, InputStream input, boolean isCX2) throws IllegalStateException, Exception {
	    	  CloseableHttpClient client = HttpClients.createDefault();
		      HttpPut httpPost = new HttpPut(ndexRestClient.getBaseroute() + (isCX2 ? (NdexApiVersion.v3 + "/networks/"): (NdexApiVersion.v2 + "/network/"))	  
	    			  + networkUUID.toString());

	    	  try
	          {
	              //Set various attributes
	    		  HttpEntity multiPartEntity = MultipartEntityBuilder.create()
	            		  				.addBinaryBody("CXNetworkStream", input,ContentType.create("application/octet-stream"), "filname").build();
	   
	              //Set to request body
	              httpPost.setEntity(multiPartEntity) ;
	              httpPost.addHeader(HttpHeaders.AUTHORIZATION, ndexRestClient.getAuthenticationString());
	              httpPost.addHeader( "User-Agent", ndexRestClient.getUserAgent());
         
	              //Send request
	      	      try (CloseableHttpResponse response = client.execute(httpPost)) {
	               
	      	    	  //Verify response code
	      	    	  if (response != null)
	      	    	  {
	      	    		  if ( response.getCode() != HttpStatus.SC_NO_CONTENT) {
							  ndexRestClient.processNdexSpecificException(response.getEntity().getContent(), response.getCode(), new ObjectMapper());

	      	    		  }
	      	    		  return;
	      	    	  }
	      	    	  throw new NdexException ("No response from the server.");
	      	      }
	          }  finally {
	        	    client.close();
	          }

	    }
        */
	public static void main(String[] args) throws Exception {
		NdexRestClient rawclient = new NdexRestClient(args[0], args[1], args[2]);
		NdexRestClientModelAccessLayer client = new NdexRestClientModelAccessLayer(rawclient);
		
		System.out.println("Network count: " + client.getServerStatus().getNetworkCount());
		try (InputStream targetStream = new FileInputStream(args[3])){
			if (args[0].endsWith(".cx2")){
				UUID networkId = client.createCX2Network(targetStream);
				System.out.println("Id of new cx2 network: " + networkId.toString());
			} else {
				UUID networkId = client.createCXNetwork(targetStream);
				System.out.println("Id of new cx network: " + networkId.toString());
			}
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
//		user	POST	/user/emailAddress	string	
/*
public User updateUserEmail(User user, String newEmail) throws JsonProcessingException, IOException{
	JsonNode postData = TBD;
	return (User)ndexRestClient.postNdexObject("/user/" + user.getExternalId() , postData, User.class);
}
*/

// Change user password

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
	   

//		group	POST	/network/searchByProperties	 Collection<NetworkSummary>
	/*	@SuppressWarnings("unchecked")
		public List<NetworkSummary> searchNetworkByPropertyFilter(NetworkPropertyFilter query) throws JsonProcessingException, IOException{
			JsonNode postData = objectMapper.valueToTree(query);
			return (List<NetworkSummary>)ndexRestClient.postNdexObjectList("/network/searchByProperties", postData, NetworkSummary.class);
		} */
		// Get network permissions of group as list of memberships
//				group	GET	/group/{groupUUID}/network/{permission}/{skipBlocks}/{blockSize}		Membership[]
	/*	@SuppressWarnings("unchecked")
		public List<Membership> findGroupNetworks(String groupId, Permissions permission, int skipBlocks, int blockSize) throws JsonProcessingException, IOException{
			return (List<Membership>)ndexRestClient.getNdexObjectList("/group/" + groupId + "/network/" + permission + "/" + skipBlocks  + "/" + blockSize , "", Membership.class);
		} */
		
}
