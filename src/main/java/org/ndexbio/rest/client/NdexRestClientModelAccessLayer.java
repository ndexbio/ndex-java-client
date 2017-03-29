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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
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
import org.ndexbio.model.errorcodes.NDExError;
import org.ndexbio.model.exceptions.DuplicateObjectException;
import org.ndexbio.model.exceptions.ForbiddenOperationException;
import org.ndexbio.model.exceptions.NdexException;
import org.ndexbio.model.exceptions.ObjectNotFoundException;
import org.ndexbio.model.exceptions.UnauthorizedOperationException;
import org.ndexbio.model.network.query.EdgeCollectionQuery;
import org.ndexbio.model.network.query.NetworkPropertyFilter;
import org.ndexbio.model.object.CXSimplePathQuery;
import org.ndexbio.model.object.Group;
import org.ndexbio.model.object.Membership;
import org.ndexbio.model.object.NdexPropertyValuePair;
import org.ndexbio.model.object.NdexStatus;
import org.ndexbio.model.object.NetworkSearchResult;
import org.ndexbio.model.object.NewUser;
import org.ndexbio.model.object.Permissions;
import org.ndexbio.model.object.ProvenanceEntity;
import org.ndexbio.model.object.Request;
import org.ndexbio.model.object.RestResource;
import org.ndexbio.model.object.SimpleNetworkQuery;
import org.ndexbio.model.object.SimplePathQuery;
import org.ndexbio.model.object.SimplePropertyValuePair;
import org.ndexbio.model.object.SimpleQuery;
import org.ndexbio.model.object.SolrSearchResult;
import org.ndexbio.model.object.Status;
import org.ndexbio.model.object.Task;
import org.ndexbio.model.object.User;
import org.ndexbio.model.object.network.BaseTerm;
import org.ndexbio.model.object.network.Citation;
import org.ndexbio.model.object.network.Edge;
import org.ndexbio.model.object.network.FunctionTerm;
import org.ndexbio.model.object.network.Namespace;
import org.ndexbio.model.object.network.Network;
import org.ndexbio.model.object.network.NetworkSummary;
import org.ndexbio.model.object.network.Node;
import org.ndexbio.model.object.network.Support;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
	
	public String getBaseRoute(){
		return ndexRestClient.getBaseroute();
	}

	/*-----------------------------------------
	 * 
	 *          Server
	 *          
	 * -----------------------------------------
	 */
	
    public NdexStatus getServerStatus() throws IOException {
    	String route = "/admin/status" ;

    	HttpURLConnection con = this.ndexRestClient.getReturningConnection(route,"");
		InputStream inputStream = con.getInputStream();
		NdexStatus status = this.objectMapper.readValue(inputStream, NdexStatus.class);
		inputStream.close();
		con.disconnect();

		return status;	
    }
    
  /*  public void shutDownJettyServer() throws IOException  {
    	String route = "/admin/shutdown";

    	HttpURLConnection con = this.ndexRestClient.getReturningConnection(route,"");
   
		InputStream inputStream = null;
		try {
			inputStream = con.getInputStream();
		} catch (IOException e) {
			// we expect this exception since server is shut down
			System.out.println("e.getMessage()=" + e.getMessage());
		}
		con.disconnect();

		return;
    }
*/    
    
	/*-----------------------------------------
	 * 
	 *          Credentials
	 *          
	 * -----------------------------------------
	 */
	
	public void setCredentials(String username, String password) {
		ndexRestClient.setCredentials(username, password);	
	}
	public void setPassword(String newPassword) {
		ndexRestClient.setPassword(newPassword);
	}
	public String getUserName() {
		return ndexRestClient.getUsername();
	}
	public String getPassword() {
		return ndexRestClient.getPassword();
	}
	
	public boolean checkCredential() throws NdexException{
		try {
			if (null == ndexRestClient.getUsername() || null == ndexRestClient.getPassword()) return false;
			User currentUser = authenticateUser(ndexRestClient.getUsername(), ndexRestClient.getPassword());
			if (null == currentUser || null == currentUser.getExternalId()) return false;
			ndexRestClient.setUserUid(currentUser.getExternalId());
			return true;
		} catch (JsonProcessingException e) {
			System.out.println("JSON processing error in checking credential");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("IOException in checking credential");
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			System.out.println("Illegal argument in checking credential");
			e.printStackTrace();	
		}
		return false;
		
	}

	/*-----------------------------------------
	 * 
	 *          Group
	 *          
	 * -----------------------------------------
	 */

	// Get API Documentation for group service
//			group	GET	/group/api		RestResource[]
	@SuppressWarnings("unchecked")
	public List<RestResource> getGroupApi() throws IOException {
		return (List<RestResource>) ndexRestClient.getNdexObjectList("/group/api", "", RestResource.class);
	}
	
	// Get group by id
//			group	GET	/group/{groupUUID}		Group
	public Group getGroup(String groupId) throws IOException, NdexException {
		return (Group) ndexRestClient.getNdexObject("/group/"+groupId, "", Group.class);
	}
	
	// Update a group
//			group	POST	/group/{groupUUID}	Group	Group
	public Group updateGroup(Group group) throws JsonProcessingException, IOException, NdexException{
		JsonNode postData = objectMapper.valueToTree(group);
		return (Group)ndexRestClient.postNdexObject("/group/" + group.getExternalId() , postData, Group.class);
	}
	
	// Search for groups
//			group	POST	/group/search/{skipBlocks}/{blockSize}	GroupQuery	Group[]
	@SuppressWarnings("unchecked")
	public List<Group> findGroups(SimpleQuery query, int skipBlocks, int blockSize) throws JsonProcessingException, IOException{
		JsonNode postData = objectMapper.valueToTree(query);
		return (List<Group>)ndexRestClient.postNdexObjectList("/group/search"  + skipBlocks  + "/" + blockSize , postData, Group.class);
	}
	

//	group	POST	/network/searchByProperties	 Collection<NetworkSummary>
	@SuppressWarnings("unchecked")
	public List<NetworkSummary> searchNetworkByPropertyFilter(NetworkPropertyFilter query) throws JsonProcessingException, IOException{
		JsonNode postData = objectMapper.valueToTree(query);
		return (List<NetworkSummary>)ndexRestClient.postNdexObjectList("/network/searchByProperties", postData, NetworkSummary.class);
	}
	// Get network permissions of group as list of memberships
//			group	GET	/group/{groupUUID}/network/{permission}/{skipBlocks}/{blockSize}		Membership[]
	@SuppressWarnings("unchecked")
	public List<Membership> findGroupNetworks(String groupId, Permissions permission, int skipBlocks, int blockSize) throws JsonProcessingException, IOException{
		return (List<Membership>)ndexRestClient.getNdexObjectList("/group/" + groupId + "/network/" + permission + "/" + skipBlocks  + "/" + blockSize , "", Membership.class);
	}
	
	// Create a group
//			group	POST	/group	
	public Group createGroup(Group group) throws JsonProcessingException, IOException, NdexException{
		JsonNode postData = objectMapper.valueToTree(group);
		return (Group)ndexRestClient.postNdexObject("/group", postData, Group.class);
	}
	
	// Delete a group
//			group	DELETE	/group/{groupUUID}	
	public void deleteGroup(String id) throws JsonProcessingException, IOException{
		ndexRestClient.delete("/group/" + id);
	}	
	
	// Add or modify account permission for a group by posting a membership
//			group	POST	/group/{groupUUID}/member	Membership	
	public Membership setGroupPermission(
			String groupId, 
			Membership membership) throws JsonProcessingException, IOException, NdexException{
		JsonNode postData = objectMapper.valueToTree(membership);
		return (Membership)ndexRestClient.postNdexObject("/group/" + groupId + "/member", postData, Membership.class);
	}
	
	// Revoke account permission for a group by deleting a membership
	// note: *not* implemented with membership records on server. 
	// membership objects are a construct for the API only.
//			group	DELETE	/group/{groupUUID}/member/{userUUID}		
	public void revokeGroupPermission(String groupId, String userId) throws JsonProcessingException, IOException{
		ndexRestClient.delete("/group/" + groupId + "/member/" + userId);
	}
	
	/*-----------------------------------------
	 * 
	 *          Request
	 *          
	 * -----------------------------------------
	 */
	
	// Get the API documentation for the request service
//			request	GET	/request/api		RestResource[]
	@SuppressWarnings("unchecked")
	public List<RestResource> getRequestApi() throws IOException {
		return (List<RestResource>) ndexRestClient.getNdexObjectList("/request/api", "", RestResource.class);
	}
	
	// Create a request
//			request	POST	/request	
	public Request createRequest(Request request) throws JsonProcessingException, IOException, NdexException{
		JsonNode postData = objectMapper.valueToTree(request);
		return (Request)ndexRestClient.postNdexObject("/request", postData, Request.class);
	}
	
	// Delete a request
//			request	DELETE	/request/{requestUUID}	
	public void deleteRequest(String id) throws JsonProcessingException, IOException{
		ndexRestClient.delete("/request/" + id);
	}	
	
	// Get a request
//			request	GET	/request/{requestUUID}	
	public Request getRequest(String requestId) throws IOException, NdexException {
		return (Request) ndexRestClient.getNdexObject("/request/"+ requestId, "", Request.class);
	}
	
	// Update a request
//			request	POST	/request/{requestUUID}	
	public Request updateRequest(Request request) throws JsonProcessingException, IOException, NdexException{
		JsonNode postData = objectMapper.valueToTree(request);
		return (Request)ndexRestClient.postNdexObject("/request/" + request.getExternalId() , postData, Request.class);
	}	

	/*-----------------------------------------
	 * 
	 *          Task
	 *          
	 * -----------------------------------------
	 */

	// Get the API documentation for the task service
//			task	GET	/task/api		RestResource[]
	@SuppressWarnings("unchecked")
	public List<RestResource> getTaskApi() throws IOException {
		return (List<RestResource>) ndexRestClient.getNdexObjectList("/task/api", "", RestResource.class);
	}
	
	// Get a task by id
//			task	GET	/task/{taskUUID}		Task
	public Task getTask(String taskId) throws IOException, NdexException {
		return (Task) ndexRestClient.getNdexObject("/task/"+ taskId, "", Task.class);
	}
	
	// Update a task
//			task	POST	/task/{taskUUID}	Task	
	public Task updateTask(Task task) throws JsonProcessingException, IOException, NdexException{
		JsonNode postData = objectMapper.valueToTree(task);
		return (Task)ndexRestClient.postNdexObject("/task/" + task.getExternalId() , postData, Task.class);
	}
	
	// Create a task
//			task	POST	/task	Task	UUID
	public Task createTask(Task task) throws JsonProcessingException, IOException, NdexException{
		JsonNode postData = objectMapper.valueToTree(task);
		return (Task)ndexRestClient.postNdexObject("/task", postData, Task.class);
	}
	
	// Delete a task
	// HOWEVER: in most cases, the idea is that a background process will delete task with status QUEUED_FOR_DELETION
	// This API function is appropriate for situations where you are implementing a custom task processor
//			task	DELETE	/task/{taskUUID}
	public void deleteTask(String id) throws JsonProcessingException, IOException{
		ndexRestClient.delete("/task/" + id);
	}	
	
	// Update the status of the task
	// Much more common case than the general update task
//			task	PUT	/task/{taskUUID}/status/{status}	Task	
	public Task updateTaskStatus(Task task, Status status) throws JsonProcessingException, IOException, NdexException{
		JsonNode postData = objectMapper.valueToTree(task);
		return (Task)ndexRestClient.postNdexObject("/task/" + task.getExternalId() + "/status/" + status, postData, Task.class);
	}
	
	/*-----------------------------------------
	 * 
	 *          User
	 *          
	 * -----------------------------------------
	 */
	
	// Get the API documentation for the user service
//			user	GET	/user/api		RestResource[]
	@SuppressWarnings("unchecked")
	public List<RestResource> getUserApi() throws IOException {
		return (List<RestResource>) ndexRestClient.getNdexObjectList("/user/api", "", RestResource.class);
	}
	
	// Get user by username OR id
//			user	GET	/user/{userIdentifier}		User
	public User getUser(String userId) throws IOException, NdexException {
		return (User) ndexRestClient.getNdexObject("/user/"+userId, "", User.class);
	}
	
	// Authenticate user 
//			user	GET	/user/authenticate	
	public User authenticateUser(String userName, String password) throws IOException, NdexException {
		return (User) ndexRestClient.getNdexObject("/user?valid=true", userName,  password, "", User.class);
	}
	
	
	
	// Get group permissions of user as list of memberships
//			user	GET	/user/{userUUID}/group/{permission}/{skipBlocks}/{blockSize}		Membership[]
	@SuppressWarnings("unchecked")
	public Map<UUID,Permissions> getUserGroupPermissions(String userId, String permission, int skipBlocks, int blockSize) throws IOException, NdexException {
		return (Map<UUID,Permissions>) ndexRestClient.getNdexObjectWithTypeReference("/user/"+ userId + "/membership?type=" + permission  + 
				"&start=" + skipBlocks  + "&size=" + blockSize , "", (new TypeReference<Map<UUID,Permissions>>() {}));
	}
	
	// Get network permissions of user as list of memberships
//			user	GET	/user/{userUUID}/network/{permission}/{skipBlocks}/{blockSize}		Membership[]
	@SuppressWarnings("unchecked")
	public List<Membership> getUserNetworkPermissions(String userId, String permission, int skipBlocks, int blockSize) throws IOException {
		return (List<Membership>) ndexRestClient.getNdexObjectList("/user/"+ userId + "/network/" + permission  + "/" + skipBlocks  + "/" + blockSize , "", Membership.class);
	}	
	
	// Get tasks owned by user, filtered by status
//			user	GET	/user/{userUUID}/task/{status}/{skipBlocks}/{blockSize}		Task[]
	@SuppressWarnings("unchecked")
	public List<Task> getUserTasks(String userId, String status, int skipBlocks, int blockSize) throws IOException {
		return (List<Task>) ndexRestClient.getNdexObjectList("/user/"+ userId + "/task/" + status  + "/" + skipBlocks  + "/" + blockSize , "", Task.class);
	}	
	
	// Get requests related to user that are pending
//			user	GET	/user/{userUUID}/request/pending/{skipBlocks}/{blockSize}		Request[]
	@SuppressWarnings("unchecked")
	public List<Request> getUserPendingRequests(String userId, int skipBlocks, int blockSize) throws IOException {
		return (List<Request>) ndexRestClient.getNdexObjectList("/user/"+ userId + "/request/pending/"  + skipBlocks  + "/" + blockSize , "", Request.class);
	}
	
	// Get requests related to user
//			user	GET	/user/{userUUID}/request/{skipBlocks}/{blockSize}		Request[]
	@SuppressWarnings("unchecked")
	public List<Request> getUserRequests(String userId, int skipBlocks, int blockSize) throws IOException {
		return (List<Request>) ndexRestClient.getNdexObjectList("/user/"+ userId + "/request/"  + skipBlocks  + "/" + blockSize , "", Request.class);
	}
	
	// Get permission of user for resource as a membership
//			user	GET	/user/{userUUID}/membership/{resourceUUID}		Membership
	public Membership getResourcePermission(String userId, String resourceId) throws IOException, NdexException {
		return (Membership) ndexRestClient.getNdexObject("/user/"+ userId + "/membership/" + resourceId, "", Membership.class);
	}
	
	// Create a user
//			user	POST	/user	NewUser	User
	public User createUser(NewUser user) throws JsonProcessingException, IOException, NdexException{
		JsonNode postData = objectMapper.valueToTree(user);
		return (User)ndexRestClient.postNdexObject("/user", postData, User.class);
	}
	
	// Update a user
//			user	POST	/user/{UUID}	User	User
	public User updateUser(User user) throws JsonProcessingException, IOException, NdexException{
		JsonNode postData = objectMapper.valueToTree(user);
		return (User)ndexRestClient.postNdexObject("/user/" + user.getExternalId() , postData, User.class);
	}
	
	// Search for users
//			user	POST	/user/search/{skipBlocks}/{blockSize}	SimpleUserQuery	User[]
	@SuppressWarnings("unchecked")
	public List<User> findUsers(SimpleQuery query, int skipBlocks, int blockSize) throws JsonProcessingException, IOException{
		JsonNode postData = objectMapper.valueToTree(query);
		return (List<User>)ndexRestClient.postNdexObjectList("/user/search"  + skipBlocks  + "/" + blockSize , postData, User.class);
	}
	
	// Generate forgotten password email to user
//			user	GET	/user/{UUID}/forgotPassword	
	public User generateForgottenPasswordEmail(String userId) throws IOException, NdexException {
		return (User) ndexRestClient.getNdexObject("/user/"+ userId + "/forgotPassword", "", User.class);
	}
	
	// Update user email address, alerting user via old email
	// TODO
//			user	POST	/user/emailAddress	string	
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
	public boolean changePassword(String newPassword) throws JsonProcessingException, IOException, NdexException {
		
		boolean success = false;
		
		int returnCode = ndexRestClient.postString("/user/password", newPassword, User.class);
		
		if (HttpURLConnection.HTTP_NO_CONTENT == returnCode) {
			success = true;
			this.setPassword(newPassword);
		}
		return success;
	}

	
	// Delete user (actually implemented as deprecate)
	// Fails unless the authenticated user is the user to delete...
//			user	DELETE	/user	
	public void deleteUser(String id) throws JsonProcessingException, IOException{
		ndexRestClient.delete("/user/" + id);
	}
	
	// delete the authenticated user (self)
	public void deleteUser() throws JsonProcessingException, IOException, NdexException {
		ndexRestClient.delete();
	}
	
	/*-----------------------------------------
	 * 
	 *          Network
	 *          
	 * -----------------------------------------
	 */


	// Get the API documentation for the network service
//	network	GET	/network/api		RestResource[]
	@SuppressWarnings("unchecked")
	public List<RestResource> getNetworkApi() throws IOException {
		return (List<RestResource>) ndexRestClient.getNdexObjectList("/network/api", "", RestResource.class);
	}
	
	
	// Network permissions
	
	// Assign permissions by posting a membership object
//	network	POST	/network/{networkUUID}/member	Membership
	public int setNetworkPermission(
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
	public void revokeNetworkPermission(String networkId, String userId) throws JsonProcessingException, IOException{
		ndexRestClient.delete("/network/" + networkId + "/member/" + userId);
	}
	
	// Get network permissions as list of memberships
//	network	GET	/network/{networkUUID}/membership/{skipBlocks}/{blockSize}		List<Membership>
	@SuppressWarnings("unchecked")
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
	
	// Network Summary objects
	
//	network	GET	/network/{networkUUID}		NetworkSummary
	public NetworkSummary getNetworkSummaryById(String networkId) throws IOException, NdexException {
		return (NetworkSummary) ndexRestClient.getNdexObject("/network/"+networkId, "", NetworkSummary.class);
		//String route = "/network/"+networkId;			
    	//HttpURLConnection con = this.ndexRestClient.getReturningConnection(route,"");
		//InputStream inputStream = con.getInputStream();
		//NetworkSummary networkSummary = objectMapper.readValue(inputStream, NetworkSummary.class);
		//inputStream.close();
		//con.disconnect();

		//return networkSummary;
	}

    public boolean isServerRunningNdexServer()
    {
        String route = "/admin/status";
        try
        {
            JsonNode node = ndexRestClient.get(route, "");
            //Hack: If network count is not null in the JSON returned by the
            //request, assume it is an NDEx server.
            return node.get("networkCount") != null;
        }
        catch (IOException e)
        {
            return false;
        }
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
//	network	POST	/network/search/{skipBlocks}/{blockSize}	SimpleNetworkQuery	NetworkSummary[]
	@SuppressWarnings("unchecked")
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
	//	return (SolrSearchResult<NetworkSummary> )ndexRestClient.postNdexObject(route, postData, SolrSearchResult.class);
		return (NetworkSearchResult)ndexRestClient.postNdexObject(route, postData, NetworkSearchResult.class);
		
/*
		HttpURLConnection con = ndexRestClient.postReturningConnection(route, postData);
		InputStream inputStream = con.getInputStream();
		List<NetworkSummary> networks = objectMapper.readValue(inputStream, new TypeReference<List<NetworkSummary>>() { });
		inputStream.close();
		con.disconnect();

		return networks;
		*/
	}

	public List<NetworkSummary> getMyNetworks(UUID userId) 
			throws JsonProcessingException, IOException {
		
		String route = "/user/"+ userId.toString() + "/summary";		
		return (List<NetworkSummary>) ndexRestClient.getNdexObjectList(route,"", NetworkSummary.class);

	}

	
	
//	network	POST	/network/search/{skipBlocks}/{blockSize}	SimpleNetworkQuery	NetworkSummary[]
	@SuppressWarnings("unchecked")
	public ArrayList<NetworkSummary> searchNetwork(
			SimpleNetworkQuery query,
			int skipBlocks, 
			int blockSize) 
			throws JsonProcessingException, IOException {
		String route = "/network/search/" + skipBlocks+"/"+ blockSize;		
		JsonNode postData = objectMapper.valueToTree(query);
		return (ArrayList<NetworkSummary>) ndexRestClient.postNdexObjectList(route, postData, NetworkSummary.class);
	}
	
	@SuppressWarnings("unchecked")
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

//		return (SolrSearchResult<NetworkSummary> )ndexRestClient.postNdexObject(route, postData, SolrSearchResult.class);
		
	}
	
	// Networks in standard NDEx object model
	
//	network	POST	/network/asNetwork	Network	NetworkSummary	
	public NetworkSummary createNetwork(Network network) throws Exception {
		String route = "/network/asNetwork";
		JsonNode postData = objectMapper.valueToTree(network);
		return (NetworkSummary) ndexRestClient.postNdexObject(route, postData, NetworkSummary.class);
/*
		HttpURLConnection con = ndexRestClient.postReturningConnection(route, postData);
		InputStream inputStream = con.getInputStream();
		NetworkSummary networkSummary = objectMapper.readValue(inputStream, NetworkSummary.class);
		inputStream.close();
		con.disconnect();
		
		return networkSummary;
		*/
	}


	// Create a network with a group as the admin
//	network	POST	/network/asNetwork/group/{group UUID}	Network	NetworkSummary
	public NetworkSummary createNetworkForGroup(Network network, String groupId) throws Exception {
		String route = "/network/asNetwork/group/" + groupId;
		JsonNode postData = objectMapper.valueToTree(network);
		return (NetworkSummary) ndexRestClient.postNdexObject(route, postData, NetworkSummary.class);
/*
		HttpURLConnection con = ndexRestClient.postReturningConnection(route, postData);
		InputStream inputStream = con.getInputStream();
		NetworkSummary networkSummary = objectMapper.readValue(inputStream, NetworkSummary.class);
		inputStream.close();
		con.disconnect();
		
		return networkSummary;
		*/
	}

	// Delete a network (actually implemented as deprecation)
//	network	DELETE	/network/{networkUUID}
	public void deleteNetwork(String id) throws IOException{
		ndexRestClient.delete("/network/" + id);
	}
	
	// Make the network "published" - no longer mutable.
//	network	POST	/network/{networkUUID}/publish		NetworkSummary
	public NetworkSummary makeNetworkImmutable(String networkId) throws Exception {
		String route = "/network/" + networkId + "/publish";
		JsonNode postData = objectMapper.createObjectNode(); 
		return (NetworkSummary) ndexRestClient.postNdexObject(route, postData, NetworkSummary.class);
	}
	
//	network	GET	/network/{networkUUID}/asNetwork		Network
	public Network getNetwork(String id) throws IOException, NdexException {
		String route = "/network/" + id + "/asNetwork/"; 
		return (Network) ndexRestClient.getNdexObject(route, "", Network.class);
		/*
		HttpURLConnection con = ndexRestClient.getReturningConnection(route, "");
		InputStream inputStream = con.getInputStream();
		Network network = objectMapper.readValue(inputStream, Network.class);
		inputStream.close();
		con.disconnect();
		return network;
		*/
	}
	
	public InputStream getNetworkAsCXStream(String id) throws JsonProcessingException, IOException, NdexException {
		String route = "/network/" + id ;
		return  ndexRestClient.getStream(route, "");
	}

	public InputStream getNetworkAspects(String id, Collection<String> aspects) throws JsonProcessingException, IOException, NdexException {
		String route = "/network/" + id + "/aspects";
	//	return  ndexRestClient.getStream(route, "");
	  	JsonNode postData = objectMapper.valueToTree(aspects);
    	return  ndexRestClient.postNdexObject(route, postData);

	}
	
	public InputStream getNeighborhoodAsCXStream(String id, CXSimplePathQuery query) throws JsonProcessingException, IOException, NdexException {
		String route = "/network/" + id + "/asCX/query";
		JsonNode postData = objectMapper.valueToTree(query);
	    return  ndexRestClient.postNdexObject(route, postData);
	}
	
	public InputStream getNetworkAspectElements(String id, String aspectName, int limit) throws JsonProcessingException, IOException, NdexException {

		String route = "/network/" + id + "/aspect/" + aspectName + "/" + limit;
		return  ndexRestClient.getStream(route, "");
	}
	
	// Get block of edges as network
//	network	GET	/network/{networkUUID}/edge/asNetwork/{skipBlocks}/{blockSize}		Network
	public Network getEdges(String id, int skipBlocks, int edgesPerBlock) throws IOException, NdexException {
		String route = "/network/" + id + "/edge/asNetwork/" + skipBlocks + "/" + edgesPerBlock; 
		return (Network) ndexRestClient.getNdexObject(route, "", Network.class);
		/*		
		HttpURLConnection con = ndexRestClient.getReturningConnection(route, "");
		InputStream inputStream = con.getInputStream();
		BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
		String inputLine;
		while ((inputLine = in.readLine()) != null)
		    System.out.println(inputLine);
		in.close(); 
		Network network = objectMapper.readValue(inputStream, Network.class);
		inputStream.close();
		con.disconnect();
		return network;
		*/
	}
	

// network	GET	/export/{networkId}/{format} String
	public String exportNetwork(String networkId, String fileFormat)  {
		String route = "/network/export/" + networkId + "/" + fileFormat;
        String value = null;
        try {
        	value = ndexRestClient.getString(route, "");
        } catch (IOException e) {
        	System.out.println("e.getMessage()=" + e.getMessage());
        }
		return value;
	}	
	

	// TODO: Get edges linked to citations as a network
//	network	POST	/network/{networkUUID}/citation/asNetwork/{skipBlocks}/{blockSize}	List<Long>	Network
	
	// TODO: Get a block of nodes as a network
//	network	GET	/network/{networkUUID}/node/asNetwork/{skipBlocks}/{blockSize}		Network
	
	// Update network
//	network	PUT	/network/asNetwork	Network	NetworkSummary
	public NetworkSummary updateNetwork(Network network) throws Exception {
		String route = "/network/asNetwork";
		JsonNode postData = objectMapper.valueToTree(network);
		return (NetworkSummary) ndexRestClient.putNdexObject(route, postData, NetworkSummary.class);
	}
	
	// Update network profile
//	network	POST	/network/{networkUUID}/summary	Network	NetworkSummary
	public NetworkSummary updateNetworkSummary(NetworkSummary networkSummary, String networkId) throws Exception {
		String route = "/network/" + networkId + "/summary";
		JsonNode postData = objectMapper.valueToTree(networkSummary);
		return (NetworkSummary) ndexRestClient.postNdexObject(route, postData, NetworkSummary.class);
	}	
	
	// Get network presentation properties
	// These are simple properties, they are not resolved to controlled vocabularies
	// in the object model or in the storage model
	//
	//	network	GET	/network/{networkUUID}/presentationProperties		Property[]
	//
	@SuppressWarnings("unchecked")
	public List<SimplePropertyValuePair> getNetworkPresentationProperties(
			String networkId) 
			throws JsonProcessingException, IOException {
		String route = "/network/" + networkId + "/presentationProperties";		
		return (List<SimplePropertyValuePair>) ndexRestClient.getNdexObjectList(route, "", SimplePropertyValuePair.class);
	}
		
	// Get network properties
	// These are NDEx properties that are resolved to the controlled vocabulary
	// terms (BaseTerm objects) of the network - even if the namespace is just
	// the default local namespace of the network
	//
	//	network	GET	/network/{networkUUID}/properties		Property[]
	//
	@SuppressWarnings("unchecked")
	public List<NdexPropertyValuePair> getNetworkProperties(
			String networkId) 
			throws JsonProcessingException, IOException {
		String route = "/network/" + networkId + "/properties";		
		return (List<NdexPropertyValuePair>) ndexRestClient.getNdexObjectList(route, "", NdexPropertyValuePair.class);
	}
	

	//	network	PUT	/network/{networkUUID}/properties		
	public int setNetworkProperties(String networkId,
			 List<NdexPropertyValuePair> properties) throws JsonProcessingException, IOException {
		String route = "/network/" + networkId + "/properties";	
		JsonNode putData = objectMapper.valueToTree(properties);
		Object obj = ndexRestClient.putNdexObject(route, putData, int.class); 
		return (null == obj) ? -1 : ((Integer)obj).intValue();
	}
	
	// Get network provenance object
//	network	GET	/network/{networkUUID}/provenance		Provenance
	public ProvenanceEntity getNetworkProvenance(
			String networkId) 
			throws JsonProcessingException, IOException, NdexException {
		String route = "/network/" + networkId + "/provenance";		
		return (ProvenanceEntity) ndexRestClient.getNdexObject(route, "", ProvenanceEntity.class);
	}
	
	// Update network provenance object
//	network	PUT	/network/{networkUUID}/provenance	Provenance	
	public ProvenanceEntity setNetworkProvenance(
			String networkId,
			ProvenanceEntity provenance) 
			throws JsonProcessingException, IOException {
		String route = "/network/" + networkId + "/provenance";	
		JsonNode putData = objectMapper.valueToTree(provenance);
		return (ProvenanceEntity) ndexRestClient.putNdexObject(route, putData, ProvenanceEntity.class);
	}
	
    //-----------------------------------
	// Network Elements 
    //-----------------------------------


//	network	GET	/network/{networkUUID}/citation/{skipBlocks}/{blockSize}		Citation[]
	@SuppressWarnings("unchecked")
	public List<Citation> getNetworkCitations(
			String networkId,
			int skipBlocks, 
			int blockSize) 
			throws JsonProcessingException, IOException {
		String route = "/network/" + networkId + "/citations/" + skipBlocks + "/" + blockSize;		
		return (List<Citation>) ndexRestClient.getNdexObjectList(route, "", Citation.class);
	}

//	network	GET	/network/{networkUUID}/functionTerm/{skipBlocks}/{blockSize}		FunctionTerm[]
	@SuppressWarnings("unchecked")
	public List<FunctionTerm> getNetworkFunctionTerms(
			String networkId,
			int skipBlocks, 
			int blockSize) 
			throws JsonProcessingException, IOException {
		String route = "/network/" + networkId + "/functionTerm/" + skipBlocks + "/" + blockSize;		
		return (List<FunctionTerm>) ndexRestClient.getNdexObjectList(route, "", FunctionTerm.class);
	}
	
	@SuppressWarnings("unchecked")
	public void addNetworkNamespace(
			String networkId,
			Namespace nameSpace) 
			throws JsonProcessingException, IOException, NdexException {
		String route = "/network/" + networkId + "/namespace";	
		JsonNode postData = objectMapper.valueToTree(nameSpace);	
		ndexRestClient.postNdexObject(route, postData, Namespace.class);
		return;
	}
	
	@SuppressWarnings("unchecked")
	public List<Namespace> getNetworkNamespaces(
			String networkId) 
			throws JsonProcessingException, IOException {
		String route = "/network/" + networkId + "/namespace";		
		return (List<Namespace>) ndexRestClient.getNdexObjectList(route, "", Namespace.class);
	}
	
	// Search for namespaces in network by search parameters
	// TODO: DO WE NEED THIS?
//	network	POST	/network/{networkUUID}/namespace/{skipBlocks}/{blockSize}	SearchParameters	Namespace[]
	
//	network	GET	/network/{networkUUID}/node/{skipBlocks}/{blockSize}		Node[]
	@SuppressWarnings("unchecked")
	public List<Node> getNetworkNodes(
			String networkId,
			int skipBlocks, 
			int blockSize) 
			throws JsonProcessingException, IOException {
		String route = "/network/" + networkId + "/node/" + skipBlocks + "/" + blockSize;		
		return (List<Node>) ndexRestClient.getNdexObjectList(route, "", Node.class);
	}
	
	
//	network	GET	/network/{networkUUID}/support/{skipBlocks}/{blockSize}		Support[]
	@SuppressWarnings("unchecked")
	public List<Support> getNetworkSupports(
			String networkId,
			int skipBlocks, 
			int blockSize) 
			throws JsonProcessingException, IOException {
		String route = "/network/" + networkId + "/support/" + skipBlocks + "/" + blockSize;		
		return (List<Support>) ndexRestClient.getNdexObjectList(route, "", Support.class);
	}
	
	// Get baseterms in network
//	network	GET	/network/{networkUUID}/baseTerm/{skipBlocks}/{blockSize}		BaseTerm[]
	@SuppressWarnings("unchecked")
	public List<BaseTerm> getNetworkBaseTerms(
			String networkId,
			int skipBlocks, 
			int blockSize) 
			throws JsonProcessingException, IOException {
		String route = "/network/" + networkId + "/baseTerm/" + skipBlocks + "/" + blockSize;		
		return (List<BaseTerm>) ndexRestClient.getNdexObjectList(route, "", BaseTerm.class);
	}
	
	// Get baseterms matching search parameters in network
//	network	POST	/network/{networkUUID}/baseTerm/search/{skipBlocks}/{blockSize}	SearchParameters	BaseTerm[]

	// get baseterms in namespace in network
	/*
	@SuppressWarnings("unchecked")
	public List<BaseTerm> getNetworkBaseTermsByNamespace(String namespacePrefix, String networkId) throws JsonProcessingException, IOException{
		String route = "/network/" + networkId + "/namespaces";
		ArrayNode postData = objectMapper.createArrayNode(); // will be of type ObjectNode
		postData.add(namespacePrefix);
		return (List<BaseTerm>) ndexRestClient.postNdexObjectList(route, postData, BaseTerm.class);
	}
*/

	// Get block of edges as list
//	network	GET	/network/{networkUUID}/edge/{skipBlocks}/{blockSize}		Edge[]
	@SuppressWarnings("unchecked")
	public List<Edge> getNetworkEdges(
			String networkId,
			int skipBlocks, 
			int blockSize) 
			throws JsonProcessingException, IOException {
		String route = "/network/" + networkId + "/edge/" + skipBlocks + "/" + blockSize;		
		return (List<Edge>) ndexRestClient.getNdexObjectList(route, "", Edge.class);
	}
	
	// Upload a file to be processed as a network to be loaded into NDEx
	// TODO
	
    // network	POST	/network/upload	File
	public void uploadNetwork(String networkToUpload) throws Exception {
		String route = "/network/upload";
		ndexRestClient.postNetworkAsMultipartObject(route, networkToUpload);
	}
	 
//	network	POST	/network/{networkUUID}/asNetwork/query	SimplePathQuery	Network	
	// Neighborhood PathQuery
    public Network getNeighborhood(String networkId, String searchString, int depth) throws JsonProcessingException, IOException, NdexException {
    	SimplePathQuery query = new SimplePathQuery();
    	query.setSearchString(searchString);
    	query.setSearchDepth(depth);
    	return getNeighborhood(networkId, query);
    }
    	
    public Network getNeighborhood(String networkId, SimplePathQuery query) throws JsonProcessingException, IOException, NdexException {
    	String route = "/network/" + networkId +"/asNetwork/query";	
    	JsonNode postData = objectMapper.valueToTree(query);
    	return (Network) ndexRestClient.postNdexObject(route, postData, Network.class);
    	
    	/*
		JsonNode postData = objectMapper.createObjectNode(); // will be of type ObjectNode
		((ObjectNode) postData).put("searchString", queryTerm);
		((ObjectNode) postData).put("searchDepth", depth);

		HttpURLConnection con = ndexRestClient.postReturningConnection(route, postData);
		InputStream inputStream = con.getInputStream();
		Network network = objectMapper.readValue(inputStream, Network.class);
		inputStream.close();
		con.disconnect();

		return network;
		*/
    	
    }
    
    
    //-----------------------------------
	// Networks as Property Graphs 
    //-----------------------------------
    

    //	network	GET	/network/{networkId}/setFlag/{parameter}={value}	
    //  current supported parameters are   "readOnly={true|false}"
	public String setNetworkFlag(String networkId, String parameter, String value)  {
		String route = "/network/" + networkId +"/setFlag/" + parameter + "=" + value;

        try {
            JsonNode node = ndexRestClient.put(route, null);     // set network flag 
            return (null == node) ? null : node.asText();      // return old value of the flag received from server
            
        } catch (IOException e) {
            e.printStackTrace();
        }
		
		return null;
	}

	//@POST
	//@Path("/{networkId}/asNetwork/prototypeNetworkQuery")
	//@Produces("application/json")
	
//  network	POST	/network/{networkUUID}/asNetwork/prototypeNetworkQuery
	public Network queryNetworkByEdgeFilter(String networkUUID, EdgeCollectionQuery query) throws JsonProcessingException, IOException, NdexException {
		String route = "/network/" + networkUUID +"/asNetwork/prototypeNetworkQuery";
		JsonNode postData = objectMapper.valueToTree(query);
		return (Network) ndexRestClient.postNdexObject(route, postData, Network.class);
	}
/*	
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

    public UUID createCXNetwork (InputStream input) throws IllegalStateException, Exception {
    	  CloseableHttpClient client = HttpClients.createDefault();
    	  HttpPost httpPost = new HttpPost( ndexRestClient.getBaseroute() + "/network/asCX");

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
      	    CloseableHttpResponse response = client.execute(httpPost);
               
              //Verify response if any
              if (response != null)
              {
                  if ( response.getStatusLine().getStatusCode() != 200) {
                	  
                	  throw createNdexSpecificException(response);
                  }
                  InputStream in = response.getEntity().getContent();
                  StringWriter writer = new StringWriter();
                  IOUtils.copy(in, writer, "UTF-8");
                  String theString = writer.toString();
                  System.out.println(theString);
                  return UUID.fromString(theString);
              }
              
              throw new NdexException ("No response from the server.");
          }  finally {
        	    client.close();
          }

    }
	
	private  Exception createNdexSpecificException(
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

	
	   public UUID updateCXNetwork (UUID networkUUID, InputStream input) throws IllegalStateException, Exception {
	    	  CloseableHttpClient client = HttpClients.createDefault();
	    	  HttpPut httpPost = new HttpPut(ndexRestClient.getBaseroute() + "/network/asCX/" + networkUUID.toString());

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
	      	    CloseableHttpResponse response = client.execute(httpPost);
	               
	              //Verify response if any
	              if (response != null)
	              {
	                  if ( response.getStatusLine().getStatusCode() != 200) {
	                	  
	                	  throw createNdexSpecificException(response);
	                  }
	                  InputStream in = response.getEntity().getContent();
	                  StringWriter writer = new StringWriter();
	                  IOUtils.copy(in, writer, "UTF-8");
	                  String theString = writer.toString();
	                  System.out.println(theString);
	                  return UUID.fromString(theString);
	              }
	              
	              throw new NdexException ("No response from the server.");
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
*/
	





   
   /*
    // Old methods, incremental copy idea
     
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
		//inputStream.close();
		con.getOutputStream().close();
		con.disconnect();
		return network;
	}

*/
   
   
	/*
	public List<Citation> getCitationsByNetworkId(String networkId) {
		
		return null;
	}

	public Network getSubnetworkByCitationId(String networkId, String citationId) {
		
		return null;
	}

	public List<Edge> getEdgesBySupportId(String supportId) {
		
		return null;
	}
	
	public List<Namespace> getNamespacesByNetworkId(String networkId) {
		
		return null;
	}
	*/


}
