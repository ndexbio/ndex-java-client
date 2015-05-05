package org.ndexbio.rest.client;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.List;

import org.ndexbio.model.exceptions.NdexException;
import org.ndexbio.model.object.Group;
import org.ndexbio.model.object.Membership;
import org.ndexbio.model.object.NdexPropertyValuePair;
import org.ndexbio.model.object.NdexStatus;
import org.ndexbio.model.object.NewUser;
import org.ndexbio.model.object.Permissions;
import org.ndexbio.model.object.ProvenanceEntity;
import org.ndexbio.model.object.Request;
import org.ndexbio.model.object.RestResource;
import org.ndexbio.model.object.SimplePathQuery;
import org.ndexbio.model.object.SimplePropertyValuePair;
import org.ndexbio.model.object.SimpleQuery;
import org.ndexbio.model.object.SimpleUserQuery;
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
import org.ndexbio.model.object.network.PropertyGraphNetwork;
import org.ndexbio.model.object.network.Support;
import org.ndexbio.rest.helpers.UploadedFile;

import com.fasterxml.jackson.core.JsonProcessingException;
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
    
	/*-----------------------------------------
	 * 
	 *          Credentials
	 *          
	 * -----------------------------------------
	 */
	
	public void setCredential(String username, String password) {
		ndexRestClient.setCredential(username, password);
		
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
	public Group updateGroup(Group group) throws JsonProcessingException, IOException{
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
	
	// Get network permissions of group as list of memberships
//			group	GET	/group/{groupUUID}/network/{permission}/{skipBlocks}/{blockSize}		Membership[]
	@SuppressWarnings("unchecked")
	public List<Membership> findGroupNetworks(String groupId, Permissions permission, int skipBlocks, int blockSize) throws JsonProcessingException, IOException{
		return (List<Membership>)ndexRestClient.getNdexObjectList("/group/" + groupId + "/network/" + permission + "/" + skipBlocks  + "/" + blockSize , "", Membership.class);
	}
	
	// Create a group
//			group	POST	/group	
	public Group createGroup(Group group) throws JsonProcessingException, IOException{
		JsonNode postData = objectMapper.valueToTree(group);
		return (Group)ndexRestClient.postNdexObject("/group", postData, Group.class);
	}
	
	// Delete a group
//			group	DELETE	/group/{groupUUID}	
	public void deleteGroup(String id) throws JsonProcessingException, IOException{
		ndexRestClient.delete("/grop/" + id);
	}	
	
	// Add or modify account permission for a group by posting a membership
//			group	POST	/group/{groupUUID}/member	Membership	
	public Membership setGroupPermission(
			String groupId, 
			Membership membership) throws JsonProcessingException, IOException{
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
	public Request createRequest(Request request) throws JsonProcessingException, IOException{
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
	public Request updateRequest(Request request) throws JsonProcessingException, IOException{
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
	public Task updateTask(Task task) throws JsonProcessingException, IOException{
		JsonNode postData = objectMapper.valueToTree(task);
		return (Task)ndexRestClient.postNdexObject("/task/" + task.getExternalId() , postData, Task.class);
	}
	
	// Create a task
//			task	POST	/task	Task	UUID
	public Task createTask(Task task) throws JsonProcessingException, IOException{
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
	public Task updateTaskStatus(Task task, Status status) throws JsonProcessingException, IOException{
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
//			user	GET	/user/authenticate/{username}/{password}	
	public User authenticateUser(String username, String password) throws IOException, NdexException {
		return (User) ndexRestClient.getNdexObject("/user/authenticate/"+ username + "/" + password, "", User.class);
	}
	
	// Get group permissions of user as list of memberships
//			user	GET	/user/{userUUID}/group/{permission}/{skipBlocks}/{blockSize}		Membership[]
	@SuppressWarnings("unchecked")
	public List<Membership> getUserGroupPermissions(String userId, String permission, int skipBlocks, int blockSize) throws IOException {
		return (List<Membership>) ndexRestClient.getNdexObjectList("/user/"+ userId + "/group/" + permission  + "/" + skipBlocks  + "/" + blockSize , "", Membership.class);
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
	public User createUser(NewUser user) throws JsonProcessingException, IOException{
		JsonNode postData = objectMapper.valueToTree(user);
		return (User)ndexRestClient.postNdexObject("/user", postData, User.class);
	}
	
	// Update a user
//			user	POST	/user/{UUID}	User	User
	public User updateUser(User user) throws JsonProcessingException, IOException{
		JsonNode postData = objectMapper.valueToTree(user);
		return (User)ndexRestClient.postNdexObject("/user/" + user.getExternalId() , postData, User.class);
	}
	
	// Search for users
//			user	POST	/user/search/{skipBlocks}/{blockSize}	SimpleUserQuery	User[]
	@SuppressWarnings("unchecked")
	public List<User> findUsers(SimpleUserQuery query, int skipBlocks, int blockSize) throws JsonProcessingException, IOException{
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
	/*
	public User updateUserPassword(User user, String newPassword) throws JsonProcessingException, IOException{
		String postData = newPassword;
		return (User)ndexRestClient.postNdexObject("/user/" + user.getExternalId() , postData, User.class);
	}
*/
	
	// Delete user (actually implemented as deprecate)
	// Fails unless the authenticated user is the user to delete...
//			user	DELETE	/user	
	public void deleteUser(String id) throws JsonProcessingException, IOException{
		ndexRestClient.delete("/user/" + id);
	}
	
	// delete the authenticated user (self)
	public void deleteUser() throws JsonProcessingException, IOException {
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
	public Membership setNetworkPermission(
			String networkId, 
			Membership membership) throws JsonProcessingException, IOException{
		JsonNode postData = objectMapper.valueToTree(membership);
		return (Membership)ndexRestClient.postNdexObject("/network/" + networkId + "/member", postData, Membership.class);
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
	 */
//	network	POST	/network/search/{skipBlocks}/{blockSize}	SimpleNetworkQuery	NetworkSummary[]
	@SuppressWarnings("unchecked")
	public List<NetworkSummary> findNetworks(
			String searchString,
            boolean canRead,
			String accountName,
			int skipBlocks, 
			int blockSize) 
			throws JsonProcessingException, IOException {
		String route = "/network/search/" + skipBlocks+"/"+ blockSize;		
		JsonNode postData = objectMapper.createObjectNode(); // will be of type ObjectNode
		((ObjectNode) postData).put("searchString", searchString);
        ((ObjectNode) postData).put("canRead", Boolean.toString(canRead));
		if (accountName != null) ((ObjectNode) postData).put("accountName", accountName);
		return (List<NetworkSummary>) ndexRestClient.postNdexObjectList(route, postData, NetworkSummary.class);
/*
		HttpURLConnection con = ndexRestClient.postReturningConnection(route, postData);
		InputStream inputStream = con.getInputStream();
		List<NetworkSummary> networks = objectMapper.readValue(inputStream, new TypeReference<List<NetworkSummary>>() { });
		inputStream.close();
		con.disconnect();

		return networks;
		*/
	}

	
	@SuppressWarnings("unchecked")
	public List<NetworkSummary> findNetworks(
			String searchString,
            boolean canRead,
			String accountName,
			Permissions permissionOnAcc,
			boolean includeGroups,
			int skipBlocks, 
			int blockSize) 
			throws JsonProcessingException, IOException {
		String route = "/network/search/" + skipBlocks+"/"+ blockSize;		
		JsonNode postData = objectMapper.createObjectNode(); // will be of type ObjectNode
		((ObjectNode) postData).put("searchString", searchString);
        ((ObjectNode) postData).put("canRead", Boolean.toString(canRead));
        ((ObjectNode) postData).put("includeGroups", Boolean.toString(includeGroups));
		if (accountName != null) ((ObjectNode) postData).put("accountName", accountName);
		((ObjectNode) postData).put("permission", permissionOnAcc.toString());
		return (List<NetworkSummary>) ndexRestClient.postNdexObjectList(route, postData, NetworkSummary.class);
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
	public void deleteNetwork(String id) throws JsonProcessingException, IOException{
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

	// TODO: Get edges linked to citations as a network
//	network	POST	/network/{networkUUID}/citation/asNetwork/{skipBlocks}/{blockSize}	List<Long>	Network
	
	// TODO: Get a block of nodes as a network
//	network	GET	/network/{networkUUID}/node/asNetwork/{skipBlocks}/{blockSize}		Network
	
	// Update network properties and other metadata
//	network	POST	/network/{networkUUID}/metadata	Network	NetworkSummary
	public NetworkSummary updateNetwork(Network network) throws Exception {
		String route = "/network/" + network.getExternalId() + "/metadata";
		JsonNode postData = objectMapper.valueToTree(network);
		return (NetworkSummary) ndexRestClient.postNdexObject(route, postData, NetworkSummary.class);
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
	
//	network	GET	/network/{networkUUID}/namespace/{skipBlocks}/{blockSize}		Namespace[]
	@SuppressWarnings("unchecked")
	public List<Namespace> getNetworkNamespaces(
			String networkId,
			int skipBlocks, 
			int blockSize) 
			throws JsonProcessingException, IOException {
		String route = "/network/" + networkId + "/namespace/" + skipBlocks + "/" + blockSize;		
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
    public Network getNeighborhood(String networkId, String searchString, int depth) throws JsonProcessingException, IOException {
    	SimplePathQuery query = new SimplePathQuery();
    	query.setSearchString(searchString);
    	query.setSearchDepth(depth);
    	return getNeighborhood(networkId, query);
    }
    	
    public Network getNeighborhood(String networkId, SimplePathQuery query) throws JsonProcessingException, IOException {
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
    
    // Utility to strip UUID before writing, ensure that we create new network
    private void removeUUIDFromNetwork(PropertyGraphNetwork network) {
	    int counter=0;
	  	for ( NdexPropertyValuePair p : network.getProperties()) {
				  if ( p.getPredicateString().equals(PropertyGraphNetwork.uuid)) {
					  network.getProperties().remove(counter);
					  return ;
				  }
				  counter++;
		}
   }

//	network	GET	/network/{networkUUID}/edge/asPropertyGraph/{skipBlocks}/{blockSize}		PropertyGraphNetwork
	public PropertyGraphNetwork getPropertyGraphNetwork(String networkId, int skipBlocks, int blockSize) throws JsonProcessingException, IOException {
/*		String route = "/network/"+networkUUID + "/asPropertyGraph/query/" + skipBlocks +"/" +blockSize ;		
		JsonNode postData = objectMapper.createObjectNode(); // will be of type ObjectNode
		((ObjectNode) postData).put("searchString", "");
		((ObjectNode) postData).put("top", blockSize);
		((ObjectNode) postData).put("skip", skipBlocks);

		HttpURLConnection con = ndexRestClient.postReturningConnection(route, postData);
*/
		String route = "/network/"+ networkId + "/edge/asPropertyGraph/" + skipBlocks +"/" +blockSize ;		
		InputStream input = null;
		HttpURLConnection con = null;
		try {
			con = ndexRestClient.getReturningConnection(route, "");
			input = con.getInputStream();
			// TODO 401 error handling
			return objectMapper.readValue(input, PropertyGraphNetwork.class);

		} finally {
			if ( input != null ) input.close();
			if ( con != null )con.disconnect();
		}
		/*
    	HttpURLConnection con = this.ndexRestClient.getReturningConnection(route,"");
		InputStream inputStream = con.getInputStream();
		PropertyGraphNetwork network = objectMapper.readValue(inputStream, PropertyGraphNetwork.class);
		inputStream.close();
		con.disconnect();

		return network;
		*/

	}

//	network	GET	/network/{networkUUID}/asPropertyGraph		PropertyGraphNetwork
	public PropertyGraphNetwork getPropertyGraphNetwork(String networkId) throws JsonProcessingException, IOException {
		String route = "/network/"+ networkId + "/asPropertyGraph";
		
		InputStream input = null;
		HttpURLConnection con = null;
		try {
			con = ndexRestClient.getReturningConnection(route, "");
			input = con.getInputStream();
			// TODO 401 error handling
			return objectMapper.readValue(input, PropertyGraphNetwork.class);

		} finally {
			if ( input != null) input.close();
			if  (con!=null) con.disconnect();
		}
		/*
    	HttpURLConnection con = this.ndexRestClient.getReturningConnection(route,"");
		InputStream inputStream = con.getInputStream();
		PropertyGraphNetwork network = objectMapper.readValue(inputStream, PropertyGraphNetwork.class);
		inputStream.close();
		con.disconnect();

		return network;
		*/
	}

//	network	POST	/network/asPropertyGraph	PropertyGraphNetwork	NetworkSummary
	public NetworkSummary insertPropertyGraphNetwork(PropertyGraphNetwork network) throws JsonProcessingException, IOException {
		String route = "/network/asPropertyGraph";
		removeUUIDFromNetwork(network);
		JsonNode postData = objectMapper.valueToTree(network);
		return (NetworkSummary) ndexRestClient.postNdexObject(route, postData, NetworkSummary.class);

		/*
		JsonNode node = objectMapper.valueToTree(network);
		HttpURLConnection con = ndexRestClient.postReturningConnection(route, node);
		InputStream inputStream = con.getInputStream();
		NetworkSummary summary = objectMapper.readValue(inputStream, NetworkSummary.class);
		inputStream.close();
		con.disconnect();
		return summary;
		*/
	}

//	network	POST	/network/asPropertyGraph/group/{group UUID}	PropertyGraphNetwork	NetworkSummary
	public NetworkSummary createNetworkForGroupFromPropertyGraphNetwork(
			PropertyGraphNetwork network, 
			String groupId) throws JsonProcessingException, IOException {
		String route = "/network/asPropertyGraph/group/" + groupId;
		removeUUIDFromNetwork(network);
		JsonNode postData = objectMapper.valueToTree(network);
		return (NetworkSummary) ndexRestClient.postNdexObject(route, postData, NetworkSummary.class);

		/*
		
		JsonNode node = objectMapper.valueToTree(network);
		HttpURLConnection con = ndexRestClient.postReturningConnection(route, node);
		InputStream inputStream = con.getInputStream();
		NetworkSummary summary = objectMapper.readValue(inputStream, NetworkSummary.class);
		inputStream.close();
		con.disconnect();
		return summary;
		*/
	}	
	
	// Neighborhood PathQuery
//	network	POST	/network/{networkUUID}/asPropertyGraph/query	SimplePathQuery	PropertyGraphNetwork	
    public PropertyGraphNetwork getNeighborhoodAsPropertyGraph(String networkId, String queryTerm, int depth) throws JsonProcessingException, IOException {
		String route = "/network/" + networkId +"/asPropertyGraph/query";		
		
		InputStream input = null;
		HttpURLConnection con = null;
		try {
			
			JsonNode postData = objectMapper.createObjectNode(); // will be of type ObjectNode
			((ObjectNode) postData).put("searchString", queryTerm);
			((ObjectNode) postData).put("searchDepth", depth);

			con = ndexRestClient.postReturningConnection(route, postData);
			input = con.getInputStream();
			// TODO 401 error handling
			return objectMapper.readValue(input, PropertyGraphNetwork.class);

		} finally {
			if ( input !=null ) input.close();
			if ( con != null ) con.disconnect();
		}
/*
		HttpURLConnection con = ndexRestClient.postReturningConnection(route, postData);
		InputStream inputStream = con.getInputStream();
		PropertyGraphNetwork network = objectMapper.readValue(inputStream, PropertyGraphNetwork.class);
		inputStream.close();
		con.disconnect();

		return network;
		*/
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
