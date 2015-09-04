package org.ndexbio.rest.test.api;


import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.apache.commons.io.FilenameUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runners.MethodSorters;
import org.ndexbio.model.exceptions.DuplicateObjectException;
import org.ndexbio.model.exceptions.NdexException;
import org.ndexbio.model.exceptions.ObjectNotFoundException;
import org.ndexbio.model.object.Membership;
import org.ndexbio.model.object.MembershipType;
import org.ndexbio.model.object.NewUser;
import org.ndexbio.model.object.Permissions;
import org.ndexbio.model.object.Status;
import org.ndexbio.model.object.Task;
import org.ndexbio.model.object.User;
import org.ndexbio.model.object.network.BaseTerm;
import org.ndexbio.model.object.network.Namespace;
import org.ndexbio.model.object.network.Network;
import org.ndexbio.model.object.network.NetworkSummary;
import org.ndexbio.rest.client.NdexRestClient;
import org.ndexbio.rest.client.NdexRestClientModelAccessLayer;
import org.ndexbio.rest.test.utilities.JUnitTestSuiteProperties;
import org.ndexbio.rest.test.utilities.JettyServerUtils;
import org.ndexbio.rest.test.utilities.NetworkUtils;
import org.ndexbio.rest.test.utilities.PropertyFileUtils;
import org.ndexbio.rest.test.utilities.UserUtils;

import com.fasterxml.jackson.core.JsonProcessingException;



/**
 *  This class contains JUNit tests for testing NetworkAService APIs from the 
 *  NetworkAService.java class located in src/main/java/org.ndexbio.rest.services package of ndexbio-rest module.
 *
 *  APIs tested in this class:
 *  
 *  √   1) public void addNamespace(String networkId, Namespace namespace)
 *  √   2) public NetworkSummary createNetwork(Network newNetwork)
 *  -   3) public NetworkSummary createNetwork(PropertyGraphNetwork newNetwork)
 *  √   4) public void deleteNetwork(String id)
 *  √   5) public int deleteNetworkMembership(String networkId, String  userUUID)
 *  √   6) public String exportNetwork(String networkId, String format)
 *  √   7) public List<BaseTerm> getBaseTerms(String networkId, int skipBlocks, int blockSize)
 *  √   8) public Response getCompleteNetwork(String networkId)
 *  -   9) public PropertyGraphNetwork getCompleteNetworkAsPropertyGraph(String networkId)
 *  √  10) public Network getEdges(String networkId, int skipBlocks, int blockSize)
 *  √  11) public List<Namespace> getNamespaces(String networkId, int skipBlocks, int blockSize)
 *  √  12) public NetworkSummary getNetworkSummary(String networkId)
 *  √  13) public List<Membership> getNetworkUserMemberships(String networkId, String permissions, int skipBlocks, int blockSize)
 *  -  14) public PropertyGraphNetwork getPropertyGraphEdges(String networkId, int skipBlocks,  int blockSize)
 *     15) public ProvenanceEntity getProvenance(String networkId)
 *  √  16) public Network queryNetwork(String networkId, SimplePathQuery queryParameters)
 *  -  17) public PropertyGraphNetwork queryNetworkAsPropertyGraph(String networkId, SimplePathQuery queryParameters)
 *     18) public Network queryNetworkByEdgeFilter(String networkId, EdgeCollectionQuery query)			
 *     19) public Collection<NetworkSummary> searchNetwork(SimpleNetworkQuery query, int skipBlocks, int blockSize)			
 *     20) public Collection<NetworkSummary> searchNetworkByPropertyFilter(NetworkPropertyFilter query)			
 *  √  21) public String setNetworkFlag(String networkId, String parameter, String value)			
 *  -  22) public int setNetworkPresentationProperties(String networkId, List<SimplePropertyValuePair> properties)
 *     23) public int setNetworkProperties(String networkId, List<NdexPropertyValuePair> properties)		
 *     24) public ProvenanceEntity setProvenance(String networkId, ProvenanceEntity provenance)   					
 *     25) public NetworkSummary updateNetwork(Network newNetwork)
 *  √  26) public int updateNetworkMembership(String networkId, Membership membership)
 *  √  27) public void updateNetworkProfile(String networkId, NetworkSummary summary)
 *  √  28) public void uploadNetwork(UploadedFile uploadedNetwork)	    					
 *  
 */
//The @FixMethodOrder(MethodSorters.NAME_ASCENDING) annotation sorts (and
//executes) the test methods by name in lexicographic order
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class testNetworkAService {
    // URL of the test server
    private static String testServerURL = null;
    
    private static NdexRestClient                 client;
    private static NdexRestClientModelAccessLayer ndex;
    
	static String networksAServicePropertyFile = "src/test/resources/testNetworkAService.properties";
 
    private static String accountName     = "fff";
    private static String accountPassword = "fff";

    // testUser is the user that will be created on the NDEx server as part of testing
    private static User    testAccount    = null;
    private static NewUser testUser       = null;
    
    private static Process jettyServer    = null;
    
	/**
	 * This methods runs once before any of the test methods in the class.
	 * It starts Jetty server process and creates ndex client used by other tests.
	 * 
     * @param   void
     * @return  void
     */
    @BeforeClass
    public static void setUp() throws Exception {
    	
    	testServerURL = JUnitTestSuiteProperties.getTestServerURL();
    	
		// start Jetty server in a new instance of JVM
		jettyServer = JettyServerUtils.startJettyInNewJVM(); 
		
    	// create user object; the properties describe the current test set-up
        testUser = UserUtils.getNewUser(
				accountName,
				accountPassword,
		        "This account is used for testing Network  APIs",  // description
		        "network_apis@xxxxxx.com",                         // email address
		        "User",                                            // first name -- name of the test
		        "Network Service APIs",                            // last name -- name of the test		        
		        "http://imgur.com/gallery/ukfzg2C",                // image
		        "http://www.ndexbio.org");                         // web-site
        
		// create ndex client and a test user account
        try {
            client = new NdexRestClient(accountName, accountPassword, testServerURL);
            ndex   = new NdexRestClientModelAccessLayer(client);
        } catch (Exception e) {
        	fail("Unable to create ndex client: " + e.getMessage());
        }
        
        // in case user account exists, delete it
    	//UserUtils.deleteUser(ndex);
    	
		// create test account
    	//testAccount = UserUtils.createUserAccount(ndex, testUser);
    }
    
    /**
     * Clean-up method.  The last method called in this class by JUnit framework.
     * 
     * @throws  Exception
     * @param   void
     * @return  void
     */
    @AfterClass
    public static void tearDown() throws Exception {

    	// stop the Jetty server, remove database; destroy Jetty Server process
        JettyServerUtils.shutdownServerRemoveDatabase();
    }
    
    /**
     * This method runs before every test (before every method annotated with @Test).
     * It shuts down database, stops Jetty server, physically removes database, and restarts Jetty.
     * 
     * @param   void
     * @return  void
     */
    @Before
    public void restartWithCleanDatabase() {
        // stop Jetty server, remove database from file system, start Jetty server
    	// (i.e., (re)start server with clean database)
    	String responseFromServer = JettyServerUtils.sendCommand("restartServerWithCleanDatabase");
    	assertEquals("unable to restart Jetty Server: ", responseFromServer, "done");
    	
		// re-create test account since it was deleted at previous step
		testAccount = UserUtils.createUserAccount(ndex, testUser);
    }
     
    /**
     * Read network in JSON format from the file specified in the properties file,
     * create this network on the server using API, and then delete this network from the server.
     * 
     * APIs tested: public NetworkSummary createNetwork(Network newNetwork)
     *              public void deleteNetwork(String id)
     * 
     * @param   void
     * @return  void
     */
    //@Test  
    public void test0001createNetwork()  {
    	// network in JSON format to be created on the Server via API
    	TreeMap<String, String> testJSONNetworkToCreate = 
    				PropertyFileUtils.parsePropertyFile(networksAServicePropertyFile);
		
    	// absolute path name of the file containing network in JSON format; defined in properties file: "createNetwork = ..."
		String absoluteNetworkPath = testJSONNetworkToCreate.get("createNetwork");
		assertNotNull("network path is null; check properties file", absoluteNetworkPath);

    	// construct Network object to be created on the server by createNetwork()
    	Network network = NetworkUtils.readNetworkFromFile(absoluteNetworkPath); 
    
        NetworkSummary networkSummary = NetworkUtils.createNetwork(ndex, network);
        assertNotNull("createNetwork returned null", networkSummary); 	
    
    	// delete network from the test account
    	NetworkUtils.deleteNetwork(ndex, networkSummary.getExternalId().toString());        
    }

    /**
     * Upload network specified in the properties file to the server, 
     * and then delete this network from the server.
     * 
     * APIs tested: public void uploadNetwork(UploadedFile uploadedNetwork)
     *              public void deleteNetwork(String id)
     * 
     * @param   void
     * @return  void
     */
    //@Test  
    public void test0002uploadNetwork()  {
    	// network to be uploaded to the Server
    	TreeMap<String, String> testNetworkToUpload = 
    			PropertyFileUtils.parsePropertyFile(networksAServicePropertyFile);
		
    	// absolute path name of the network; defined in properties file: "uploadNetwork = ..."
		String absoluteNetworkPath = testNetworkToUpload.get("uploadNetwork");
		assertNotNull("network path is null; check properties file", absoluteNetworkPath);
    	
    	File fileToUpload = new File(absoluteNetworkPath);
    	NetworkUtils.startNetworkUpload(ndex, fileToUpload);
  
        // wait for the network upload task to finish
    	Task task = NetworkUtils.waitForTaskToFinish(ndex, testAccount);        	
        Object networkUUID = task.getAttribute("networkUUID");
		assertNotNull("network UUID of uploaded network is null", networkUUID);
		
    	// delete network from the test account
    	NetworkUtils.deleteNetwork(ndex, networkUUID.toString());
    }


    /**
     * Upload network specified in the properties file to the server, 
     * and then delete this network from the server.
     * 
     * APIs tested: public NetworkSummary createNetwork(Network newNetwork)
     *              public Response getCompleteNetwork(String networkId)
     *              public String setNetworkFlag(String networkId, String parameter, String value)
     *              public void deleteNetwork(String id)
     * 
     * @param   void
     * @return  void
     */
    //@Test  
    public void test0003getCompleteNetwork()  {
    	// network in JSON format to be created on the Server via API
    	TreeMap<String, String> testJSONNetworkToCreate = 
    				PropertyFileUtils.parsePropertyFile(networksAServicePropertyFile);
		
    	// absolute path name of the file containing network in JSON format; defined in properties file: "createNetwork = ..."
		String absoluteNetworkPath = testJSONNetworkToCreate.get("createNetwork");
		assertNotNull("network path is null; check properties file", absoluteNetworkPath);

    	// construct Network object to be created on the server by createNetwork()
    	Network network1 = NetworkUtils.readNetworkFromFile(absoluteNetworkPath); 
    
        NetworkSummary networkSummary = NetworkUtils.createNetwork(ndex, network1);
        assertNotNull("createNetwork returned null", networkSummary);
        assertNotNull("network UUID is null", networkSummary.getExternalId());
        
        String networkUUID = networkSummary.getExternalId().toString();
        
        // download the newly created network
		Network network2 = NetworkUtils.getNetwork(ndex, networkUUID);
		
	    // set network to read-only mode, and download it again
        NetworkUtils.setReadOnlyFlag(ndex, networkUUID, true);
        
        // download the newly created network in read-only mode
		Network network3 = NetworkUtils.getNetwork(ndex, networkUUID); 
		
        NetworkUtils.compareObjectsContents(network1, network2);
        NetworkUtils.compareObjectsContents(network1, network3);
        NetworkUtils.compareObjectsContents(network2, network3);
        
	    // set network back to read-write mode, and download it again
        NetworkUtils.setReadOnlyFlag(ndex, networkUUID, false);
		Network network4 = NetworkUtils.getNetwork(ndex, networkUUID);
		
        NetworkUtils.compareObjectsContents(network1, network4);
        NetworkUtils.compareObjectsContents(network2, network4);
        NetworkUtils.compareObjectsContents(network3, network4);
        
    	// delete network from the test account
    	NetworkUtils.deleteNetwork(ndex, networkUUID.toString());
    }    

    
    /**
     * Create network specified in the properties file on the server, then download it from the server.  
     * Delete this network from the server, and try to get it again.  
     * Expect to receive ObjectNotFoundException.
     *
     * APIs tested: public NetworkSummary createNetwork(Network newNetwork)
     *              public Response getCompleteNetwork(String networkId)
     *              public void deleteNetwork(String id)
     * 
     * @param   void
     * @return  void
     */
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    
    //@Test  
    public void test0004getCompleteNonExistentNetwork() throws IOException, NdexException  {
    	// network in JSON format to be created on the Server via API
    	TreeMap<String, String> testJSONNetworkToCreate = 
    				PropertyFileUtils.parsePropertyFile(networksAServicePropertyFile);
		
    	// absolute path name of the file containing network in JSON format; defined in properties file: "createNetwork = ..."
		String absoluteNetworkPath = testJSONNetworkToCreate.get("createNetwork");
		assertNotNull("network path is null; check properties file", absoluteNetworkPath);

    	// construct Network object to be created on the server by createNetwork()
    	Network network1 = NetworkUtils.readNetworkFromFile(absoluteNetworkPath); 
    
    	// create network on the server and check if it was created fine
        NetworkSummary networkSummary = NetworkUtils.createNetwork(ndex, network1);
        assertNotNull("createNetwork returned null", networkSummary);
        assertNotNull("network UUID is null", networkSummary.getExternalId());
        
        String networkUUID = networkSummary.getExternalId().toString();
        
        // download the newly created network
		Network network2 = NetworkUtils.getNetwork(ndex, networkUUID);
        NetworkUtils.compareObjectsContents(network1, network2);
        
    	// delete network from the test account
    	NetworkUtils.deleteNetwork(ndex, networkUUID);
    	
    	// expected exception is ObjectNotFoundException
    	thrown.expect(ObjectNotFoundException.class);  	

    	// try to get the deleted network -- expect ObjectNotFoundException
		Network network3 = ndex.getNetwork(networkUUID);   	
		assertNull("error -- retrieved deleted network", network3);
    }
    
    /**
     * Create network specified in the properties file on the server, then download it from the server. 
     * Delete this network from the server, and try to delete it again.  
     * Expect to receive ObjectNotFoundException.
     * 
     * APIs tested: public NetworkSummary createNetwork(Network newNetwork)
     *              public Response getCompleteNetwork(String networkId)
     *              public void deleteNetwork(String id)
     * 
     * @param   void
     * @return  void
     */
    //@Test  
    public void test0005deleteNonExistentNetwork() throws IOException, NdexException  {
    	// network in JSON format to be created on the Server via API
    	TreeMap<String, String> testJSONNetworkToCreate = 
    				PropertyFileUtils.parsePropertyFile(networksAServicePropertyFile);
		
    	// absolute path name of the file containing network in JSON format; defined in properties file: "createNetwork = ..."
		String absoluteNetworkPath = testJSONNetworkToCreate.get("createNetwork");
		assertNotNull("network path is null; check properties file", absoluteNetworkPath);

    	// construct Network object to be created on the server by createNetwork()
    	Network network1 = NetworkUtils.readNetworkFromFile(absoluteNetworkPath); 
    
    	// create network on the server and check if it was created fine
        NetworkSummary networkSummary = NetworkUtils.createNetwork(ndex, network1);
        assertNotNull("createNetwork returned null", networkSummary);
        assertNotNull("network UUID is null", networkSummary.getExternalId());
        
        String networkUUID = networkSummary.getExternalId().toString();
        
        // download the newly created network
		Network network2 = NetworkUtils.getNetwork(ndex, networkUUID);
        NetworkUtils.compareObjectsContents(network1, network2);
        
    	// delete network from the test account
    	NetworkUtils.deleteNetwork(ndex, networkUUID);
    	
    	// expected exception is ObjectNotFoundException
    	thrown.expect(ObjectNotFoundException.class);  	

    	// try to delete the deleted network again -- expect ObjectNotFoundException
		ndex.deleteNetwork(networkUUID);   	
    }   
    
    /**
     * Upload network specified in the properties file to the server, then download it from the server.
     * Get the network summary from the server, and compare the contents of two NetworkSummary objects.
     * Update network profile on the server.  Get the updated summary and network from the server
     * and check if they updated correctly.
     * Delete network from the server.
     *   
     * APIs tested: public void uploadNetwork(UploadedFile uploadedNetwork)
     *              public Response getCompleteNetwork(String networkId)
     *              public NetworkSummary getNetworkSummary(String networkId)
     *              public void updateNetworkProfile(String networkId, NetworkSummary summary)
     *              public void deleteNetwork(String id)
     * 
     * @param   void
     * @return  void
     */
    //@Test  
    public void test0006updateNetworkSummary()  {
    	// network to be uploaded to the Server
    	TreeMap<String, String> testNetworkToUpload = 
    			PropertyFileUtils.parsePropertyFile(networksAServicePropertyFile);
		
    	// absolute path name of the network; defined in properties file: "uploadNetwork = ..."
		String absoluteNetworkPath = testNetworkToUpload.get("uploadNetwork");
		assertNotNull("network path is null; check properties file", absoluteNetworkPath);
    	
    	File fileToUpload = new File(absoluteNetworkPath);
    	NetworkUtils.startNetworkUpload(ndex, fileToUpload);
  
        // wait for the network upload task to finish
    	Task task = NetworkUtils.waitForTaskToFinish(ndex, testAccount);        	
        Object networkUUIDObj = task.getAttribute("networkUUID");
		assertNotNull("network UUID of uploaded network is null", networkUUIDObj);
		String networkUUID = networkUUIDObj.toString();
		
		
        // download the newly created network and its summary
		Network network1 = NetworkUtils.getNetwork(ndex, networkUUID);
		NetworkSummary network1Summary = NetworkUtils.getNetworkSummaryById(ndex, networkUUID);
	    // compare the contents of two NetworkSummary objects
		NetworkUtils.compareObjectsContents(network1, network1Summary);
	
		
    	// modify profile of the network on the server
    	NetworkSummary newNetworkSummary = new NetworkSummary();
    	
    	newNetworkSummary.setName("Modified -- " + network1Summary.getName());
    	newNetworkSummary.setDescription("Modified -- " + network1Summary.getDescription());
    	newNetworkSummary.setVersion("Modified -- " + network1Summary.getVersion());
    	

    	NetworkUtils.updateNetworkSummary(ndex, newNetworkSummary, networkUUID);
    	
        // download the updated network and its summary
		Network network2 = NetworkUtils.getNetwork(ndex, networkUUID);
		NetworkSummary network2Summary = NetworkUtils.getNetworkSummaryById(ndex, networkUUID);
		
	    // compare the contents of two NetworkSummary objects
		NetworkUtils.compareObjectsContents(network2, network2Summary);
    	
    	// check if Name, Description and Version of network updated correctly
		NetworkUtils.compareNetworkSummary(newNetworkSummary, network2Summary);
    		
    
    	// delete network from the server
    	NetworkUtils.deleteNetwork(ndex, networkUUID);   	
    }     

    
    /**
     * Upload network specified in the properties file to the server, then use the search term and 
     * depth specified in the properties file to query the network.   
     * Delete network from the server.
     *   
     * APIs tested: public void uploadNetwork(UploadedFile uploadedNetwork)
     *              public Network queryNetwork(String networkId, SimplePathQuery queryParameters) 
     *              public void deleteNetwork(String id)
     * 
     * @param   void
     * @return  void
     * @throws JSONException 
     */
    //@Test  
    public void test0007testNeighborhoodQuery() throws JSONException  {
    	// network to be uploaded to the Server
    	TreeMap<String, String> testNetworkToUpload = 
    			PropertyFileUtils.parsePropertyFile(networksAServicePropertyFile);
		
    	// absolute path name of the query parameters; defined in properties file: "neighborHoodQuery01 = ..."
		String neighborHoodQuery01 = testNetworkToUpload.get("neighborHoodQuery01");
		assertNotNull("neighborHoodQuery01 is null; check properties file", neighborHoodQuery01);
		
    	// absolute path name of the query parameters; defined in properties file: "neighborHoodQuery02 = ..."
		String neighborHoodQuery02 = testNetworkToUpload.get("neighborHoodQuery02");
		assertNotNull("neighborHoodQuery01 is null; check properties file", neighborHoodQuery02);
    	
		
        String JSONString1 = neighborHoodQuery01.toString();
        JSONObject jsonObj1 =  new JSONObject(JSONString1);
        String absoluteNetworkPath1 = jsonObj1.getString("path");
        String query1 = jsonObj1.getString("query");
        int depth1 = jsonObj1.getInt("depth");
		
        
        String JSONString2 = neighborHoodQuery02.toString();
        JSONObject jsonObj2 =  new JSONObject(JSONString2);
        String absoluteNetworkPath2 = jsonObj2.getString("path");
        String query2 = jsonObj2.getString("query");
        int depth2 = jsonObj2.getInt("depth");
		
		
		
    	File fileToUpload = new File(absoluteNetworkPath1);
    	NetworkUtils.startNetworkUpload(ndex, fileToUpload);
  
        // wait for the network upload task to finish
    	Task task = NetworkUtils.waitForTaskToFinish(ndex, testAccount);        	
        Object networkUUIDObj = task.getAttribute("networkUUID");
		assertNotNull("network UUID of uploaded network is null", networkUUIDObj);
		String networkUUID = networkUUIDObj.toString();
		
		
    	Network subNetworkRetrieved = NetworkUtils.getNeighborhood(ndex, networkUUID, query1, depth1);
    	assertEquals(85, subNetworkRetrieved.getEdgeCount());
    	assertEquals(87, subNetworkRetrieved.getNodeCount());
    	
    	if (!absoluteNetworkPath1.equals(absoluteNetworkPath2)) {
        	// delete network from the server
        	NetworkUtils.deleteNetwork(ndex, networkUUID);
        	
        	// load a different network
        	fileToUpload = new File(absoluteNetworkPath2);
        	NetworkUtils.startNetworkUpload(ndex, fileToUpload);
        	
        	task = NetworkUtils.waitForTaskToFinish(ndex, testAccount);        	
            networkUUIDObj = task.getAttribute("networkUUID");
    		assertNotNull("network UUID of uploaded network is null", networkUUIDObj);
    		networkUUID = networkUUIDObj.toString();
    	}
    	
    	subNetworkRetrieved = NetworkUtils.getNeighborhood(ndex, networkUUID, query2, depth2);
    	assertEquals(6731, subNetworkRetrieved.getEdgeCount());  // 6731
    	assertEquals(4142, subNetworkRetrieved.getNodeCount());  // 4142
    	
    	
    	// delete network from the server
    	NetworkUtils.deleteNetwork(ndex, networkUUID); 
    }
    
    /**
     * Upload network specified in the properties file to the server, 
     * Get namespaces and check them. Delete this network from the server.
     * 
     * APIs tested: public void uploadNetwork(UploadedFile uploadedNetwork)
     *              public List<Namespace> getNamespaces(String networkId, int skipBlocks, int blockSize)
     *              public void deleteNetwork(String id)
     * 
     * @param   void
     * @return  void
     */
    //@Test  
    public void test0008getNamespaces()  {
    	// network to be uploaded to the Server
    	TreeMap<String, String> testNetworkToUpload = 
    			PropertyFileUtils.parsePropertyFile(networksAServicePropertyFile);
		
    	// absolute path name of the network; defined in properties file: "uploadNetwork = ..."
		String absoluteNetworkPath = testNetworkToUpload.get("uploadNetworkForNameSpace");
		assertNotNull("network path is null; check properties file", absoluteNetworkPath);
    	
    	File fileToUpload = new File(absoluteNetworkPath);
    	NetworkUtils.startNetworkUpload(ndex, fileToUpload);
  
        // wait for the network upload task to finish
    	Task task = NetworkUtils.waitForTaskToFinish(ndex, testAccount);        	
        Object networkUUIDobj = task.getAttribute("networkUUID");
		assertNotNull("network UUID of uploaded network is null", networkUUIDobj);
		String networkUUID = networkUUIDobj.toString(); 
		
		
		// retrieve all namespaces and check some 
		List<Namespace> namespaces = NetworkUtils.getNetworkNamespaces(ndex, networkUUID, 0, 500);
		assertEquals("Wrong namespaces count: ", 39, namespaces.size());
		assertEquals("Wrong namespace name: ", "bel", namespaces.get(0).getPrefix());
		assertEquals("Wrong namespace name: ", "PFR", namespaces.get(1).getPrefix());		
		assertEquals("Wrong namespace name: ", "NCR", namespaces.get(2).getPrefix());
		assertEquals("Wrong namespace name: ", "IntegumentarySystem", namespaces.get(38).getPrefix());	
		
		
		// now, retrieve only 5 namespaces
		namespaces = NetworkUtils.getNetworkNamespaces(ndex, networkUUID, 0, 5);
		assertEquals("Wrong namespaces count: ", 5, namespaces.size());
		assertEquals("Wrong namespace name: ", "bel", namespaces.get(0).getPrefix());
		assertEquals("Wrong namespace name: ", "PFR", namespaces.get(1).getPrefix());		
		assertEquals("Wrong namespace name: ", "NCR", namespaces.get(2).getPrefix());
		assertEquals("Wrong namespace name: ", "PFM", namespaces.get(3).getPrefix());
		assertEquals("Wrong namespace name: ", "NCM", namespaces.get(4).getPrefix());		

		
		// retrieve next 5 namespaces
		namespaces = NetworkUtils.getNetworkNamespaces(ndex, networkUUID, 5, 5);
		assertEquals("Wrong namespaces count: ", namespaces.size(), 5);
		assertEquals("Wrong namespace name: ", "PFH",   namespaces.get(0).getPrefix());
		assertEquals("Wrong namespace name: ", "NCH",   namespaces.get(1).getPrefix());		
		assertEquals("Wrong namespace name: ", "RGD",   namespaces.get(2).getPrefix());
		assertEquals("Wrong namespace name: ", "MGI",   namespaces.get(3).getPrefix());
		assertEquals("Wrong namespace name: ", "MESHD", namespaces.get(4).getPrefix());	
		
		
    	// delete network from the test account
    	NetworkUtils.deleteNetwork(ndex, networkUUID.toString());
    }
    
    /**
     * Upload network specified in the properties file to the server, 
     * Get namespaces and check their count. Create a new namespace and add it to
     * the network on the server. Get namespaces again and check that the new namespace
     * has been added.
     * 
     * APIs tested: public void uploadNetwork(UploadedFile uploadedNetwork)
     *              public List<Namespace> getNamespaces(String networkId, int skipBlocks, int blockSize)
     *              public void addNamespace(String networkId, Namespace namespace)
     *              public void deleteNetwork(String id)
     * 
     * @param   void
     * @return  void
     */
    //@Test  
    public void test0009addNamespace()  {
    	// network to be uploaded to the Server
    	TreeMap<String, String> testNetworkToUpload = 
    			PropertyFileUtils.parsePropertyFile(networksAServicePropertyFile);
		
    	// absolute path name of the network; defined in properties file: "uploadNetwork = ..."
		String absoluteNetworkPath = testNetworkToUpload.get("uploadNetworkForNameSpace");
		assertNotNull("network path is null; check properties file", absoluteNetworkPath);
    	
    	File fileToUpload = new File(absoluteNetworkPath);
    	NetworkUtils.startNetworkUpload(ndex, fileToUpload);
  
        // wait for the network upload task to finish
    	Task task = NetworkUtils.waitForTaskToFinish(ndex, testAccount);        	
        Object networkUUIDobj = task.getAttribute("networkUUID");
		assertNotNull("network UUID of uploaded network is null", networkUUIDobj);
		String networkUUID = networkUUIDobj.toString(); 
		
		
		// retrieve all namespaces and check some 
		List<Namespace> namespaces = NetworkUtils.getNetworkNamespaces(ndex, networkUUID, 0, 500);
		assertEquals("Wrong namespaces count: ", 39, namespaces.size(), 39);
		assertEquals("Wrong namespace name: ", "bel", namespaces.get(0).getPrefix());
		assertEquals("Wrong namespace name: ", "PFR", namespaces.get(1).getPrefix());		
		assertEquals("Wrong namespace name: ", "NCR", namespaces.get(2).getPrefix());
		assertEquals("Wrong namespace name: ", "IntegumentarySystem", namespaces.get(38).getPrefix());	
		
		
		
		String prefix = "JunitTestPrefix";
		String uri    = "http://belframework.org/schema/1.0/xbel";
		
		
		// create new namespace and add it to the network
		Namespace newNameSpace = new Namespace();
		newNameSpace.setPrefix(prefix);
		newNameSpace.setUri(uri);	

		NetworkUtils.addNetworkNamespace(ndex, networkUUID, newNameSpace);
		
		// now we should have 40 workspaces
		namespaces = NetworkUtils.getNetworkNamespaces(ndex, networkUUID, 0, 500);
		assertEquals("Wrong namespaces count: ", 40,     namespaces.size());
		assertEquals("Wrong namespace prefix: ", prefix, namespaces.get(39).getPrefix());
		assertEquals("Wrong namespace URI: ",    uri,    namespaces.get(39).getUri());		
		
    	// delete network from the test account
    	NetworkUtils.deleteNetwork(ndex, networkUUID.toString());
    }

    /**
     * Upload network specified in the properties file to the server.
     * Get membership of this network, try to assign the same membership again.
     * Create a new user, grant this user membership for the network; now the
     * network has two memberships. Revoke membership from the newly created account, 
     * and check that network only has one membership.
     * 
     * APIs tested: public void uploadNetwork(UploadedFile uploadedNetwork)
     *              public List<Membership> getNetworkUserMemberships(String networkId, String permissions, int skipBlocks, int blockSize)
     *              public int updateNetworkMembership(String networkId, Membership membership)
     *              public int deleteNetworkMembership(String networkId, String  userUUID)
     *              public void deleteNetwork(String id)
     * 
     * @param   void
     * @return  void
     */
    //@Test  
    public void test0010networkMembership()  {
    	// network to be uploaded to the Server
    	TreeMap<String, String> testNetworkToUpload = 
    			PropertyFileUtils.parsePropertyFile(networksAServicePropertyFile);
		
    	// absolute path name of the network; defined in properties file: "uploadNetwork = ..."
		String absoluteNetworkPath = testNetworkToUpload.get("uploadNetworkForMembership");
		assertNotNull("network path is null; check properties file", absoluteNetworkPath);
    	
    	File fileToUpload = new File(absoluteNetworkPath);
    	NetworkUtils.startNetworkUpload(ndex, fileToUpload);
  
        // wait for the network upload task to finish
    	Task task = NetworkUtils.waitForTaskToFinish(ndex, testAccount);        	
        Object networkUUIDobj = task.getAttribute("networkUUID");
		assertNotNull("network UUID of uploaded network is null", networkUUIDobj);
		String networkUUID = networkUUIDobj.toString(); 
		
		
		// retrieve all network memberships
		List<Membership> memberships = NetworkUtils.getNetworkUserMemberships(ndex, networkUUID, "ALL", 0, 500);
		Membership m = memberships.get(0);
		assertEquals("network and resource UUIDs not same : ", networkUUID, m.getResourceUUID().toString());	
		assertEquals("account name mismatch : ", accountName, m.getMemberAccountName());	
		assertEquals("membership type is wrong : ", MembershipType.NETWORK, m.getMembershipType());
		assertEquals("permissions are wrong : ", Permissions.ADMIN, m.getPermissions());
		
		// try to set ADMIN permissions again -- should receive 0 (meaning no update was performed)
		int status = NetworkUtils.setNetworkPermission(ndex, networkUUID, m);
		assertEquals("managed to set ADMIN permissions : ", 0, status);
		
		// try to change account name -- should receive 0 (meaning no update was performed)
		m.setMemberAccountName(accountName+accountName);
		status = NetworkUtils.setNetworkPermission(ndex, networkUUID, m);
		assertEquals("managed to change acoount name  : ", 0, status);		
		
		
		String accountName1 =  accountName+accountName;
		
		// create new account on the server
        NewUser testUser1 = UserUtils.getNewUser(
				accountName1,
				accountPassword+accountPassword,
		        "This account is used for testing Network  APIs",  // description
		        "network_apis1@xxxxxx.com",                        // email address
		        "User",                                            // first name -- name of the test
		        "Network Service APIs",                            // last name -- name of the test		        
		        "http://imgur.com/gallery/ukfzg2C",                // image
		        "http://www.ndexbio.org");  
		
		User testAccount1 = UserUtils.createUserAccount(ndex, testUser1);
		
		// grant membership to the newly created account/user
		m.setMemberUUID(testAccount1.getExternalId());
		status = NetworkUtils.setNetworkPermission(ndex, networkUUID, m);
		assertEquals("unable to grant membershit to new user : ", 1, status);			
		
		// now, there should be 2 memberships for this network
		memberships = NetworkUtils.getNetworkUserMemberships(ndex, networkUUID, "ALL", 0, 500);
		assertEquals("wrong number of memberships : ", 2, memberships.size());
		m = memberships.get(0);
		assertEquals("network and resource UUIDs not same : ", networkUUID, m.getResourceUUID().toString());	
		assertEquals("account name mismatch : ", accountName, m.getMemberAccountName());	
		assertEquals("membership type is wrong : ", MembershipType.NETWORK, m.getMembershipType());
		assertEquals("permissions are wrong : ", Permissions.ADMIN, m.getPermissions());

		m = memberships.get(1);
		assertEquals("UUIDs should be same : ", testAccount1.getExternalId(), m.getMemberUUID());
		assertEquals("network and resource UUIDs not same : ", networkUUID, m.getResourceUUID().toString());	
		assertEquals("account name mismatch : ", accountName1, m.getMemberAccountName());	
		assertEquals("membership type is wrong : ", MembershipType.NETWORK, m.getMembershipType());
		assertEquals("permissions are wrong : ", Permissions.ADMIN, m.getPermissions());
		
		
		// now, delete membership we just granted
		NetworkUtils.deleteNetworkMembership(ndex, networkUUID, testAccount1.getExternalId());
		
		// there should only be one membership now
		memberships = NetworkUtils.getNetworkUserMemberships(ndex, networkUUID, "ALL", 0, 500);
		assertEquals("wrong number of memberships : ", 1, memberships.size());
		m = memberships.get(0);
		assertEquals("network and resource UUIDs not same : ", networkUUID, m.getResourceUUID().toString());	
		assertEquals("account name mismatch : ", accountName, m.getMemberAccountName());	
		assertEquals("membership type is wrong : ", MembershipType.NETWORK, m.getMembershipType());
		assertEquals("permissions are wrong : ", Permissions.ADMIN, m.getPermissions());


    	// delete network from the test account
    	NetworkUtils.deleteNetwork(ndex, networkUUID.toString());
    }

    /**
     * Upload network specified in the properties file to the server, 
     * Get baseterms and make sue that the length of list of baseterms is not 0.
     * Delete this network from the server.
     * 
     * APIs tested: public void uploadNetwork(UploadedFile uploadedNetwork)
     *              public List<BaseTerm> getBaseTerms(String networkId, int skipBlocks, int blockSize)
     *              public void deleteNetwork(String id)
     * 
     * ATTENTION: getBaseTerms() seems to be broken since it returns no base terms for large_corpus_unzip.xbel
     * 
     * @param   void
     * @return  void
     */
    //@Test  
    public void test0011getBaseTerms() {
    	// network to be uploaded to the Server
    	TreeMap<String, String> testNetworkToUpload = 
    				PropertyFileUtils.parsePropertyFile(networksAServicePropertyFile);
		
    	// absolute path name of the file containing network in JSON format; defined in properties file: "uploadNetworkForBaseTerms = ..."
		String absoluteNetworkPath = testNetworkToUpload.get("uploadNetworkForBaseTerms");
		assertNotNull("network path is null; check properties file", absoluteNetworkPath);
		
    	File fileToUpload = new File(absoluteNetworkPath);
    	NetworkUtils.startNetworkUpload(ndex, fileToUpload);
  
        // wait for the network upload task to finish
    	Task task = NetworkUtils.waitForTaskToFinish(ndex, testAccount);        	
        Object networkUUIDobj = task.getAttribute("networkUUID");
		assertNotNull("network UUID of uploaded network is null", networkUUIDobj);
		String networkUUID = networkUUIDobj.toString(); 
    	

        // get base terms of the newly created network
		List<BaseTerm> baseTerms = NetworkUtils.getBaseTerms(ndex, networkUUID, 0, 500);
		assertNotEquals("no base terms found : ", 0, baseTerms.size());

		
		// add more test cases here, for example, try to retrieve smaller number of baseTerms,
		// like baseTerms = NetworkUtils.getBaseTerms(ndex, networkUUID, 0, 3); and check that 
		// the size of the returned list is 3, etc.


        
    	// delete network from the test account
    	NetworkUtils.deleteNetwork(ndex, networkUUID.toString());    
    }

    /**
     * Upload network specified in the properties file to the server, 
     * Get baseterms and make sure that the length of list of baseterms is not 0.
     * Delete this network from the server.
     * 
     * APIs tested: public void uploadNetwork(UploadedFile uploadedNetwork)
     *              public Network getEdges(String networkId, int skipBlocks, int blockSize)
     *              public void deleteNetwork(String id)
     * 
     * ATTENTION: getEdges() seems to be broken; returns one extra node if (skipBlock != 0)
     * 
     * @param   void
     * @return  void
     */
    //@Test  
    public void test0012getEdges()  {
    	// network to be uploaded to the Server
    	TreeMap<String, String> testNetworkToUpload = 
    				PropertyFileUtils.parsePropertyFile(networksAServicePropertyFile);
		
    	// absolute path name of the file containing network in JSON format; defined in properties file: "uploadNetworkForEdges = ..."
		String absoluteNetworkPath = testNetworkToUpload.get("uploadNetworkForEdges");
		assertNotNull("network path is null; check properties file", absoluteNetworkPath);
		
    	File fileToUpload = new File(absoluteNetworkPath);
    	NetworkUtils.startNetworkUpload(ndex, fileToUpload);
  
        // wait for the network upload task to finish
    	Task task = NetworkUtils.waitForTaskToFinish(ndex, testAccount);        	
        Object networkUUIDobj = task.getAttribute("networkUUID");
		assertNotNull("network UUID of uploaded network is null", networkUUIDobj);
		String networkUUID = networkUUIDobj.toString(); 
    
		
        // get edges of the newly created network
		Network network = NetworkUtils.getEdges(ndex, networkUUID, 0, 5);
		assertEquals("retrieved wrong number of edges : ", 5, network.getEdgeCount());
		
		// get next portion of edges from the newly created network
		network = NetworkUtils.getEdges(ndex, networkUUID, 1, 1);
		assertEquals("retrieved wrong number of edges : ", 1, network.getEdgeCount());		
		
		// get next portion of edges from the newly created network
		network = NetworkUtils.getEdges(ndex, networkUUID, 2, 3);
		assertEquals("retrieved wrong number of edges : ", 3, network.getEdgeCount());			
		
        
    	// delete network from the test account
    	NetworkUtils.deleteNetwork(ndex, networkUUID.toString());    
    }
    
    /**
     * Upload network specified in the properties file to the server, 
     * start export task on the server, and wait for this task to finish.
     * Create another export task on the server this time specifyingnetwok type
     * in lower case -- the task will not be created. 
     * 
     * APIs tested: public void uploadNetwork(UploadedFile uploadedNetwork)
     *              public String exportNetwork(String networkId, String format)
     *              public void deleteNetwork(String id)
     * 
     * @param   void
     * @return  void
     */
    @Test  
    public void test0013exportNetwork()  {
    	// network to be uploaded to the Server
    	TreeMap<String, String> testNetworkToUpload = 
    				PropertyFileUtils.parsePropertyFile(networksAServicePropertyFile);
		
    	// absolute path name of the file containing network in JSON format; defined in properties file: "uploadNetworkForEdges = ..."
		String absoluteNetworkPath = testNetworkToUpload.get("uploadNetworkForExport");
		assertNotNull("network path is null; check properties file", absoluteNetworkPath);
		
    	File fileToUpload = new File(absoluteNetworkPath);
    	NetworkUtils.startNetworkUpload(ndex, fileToUpload);
    	
    	String networkFileNameExtension = FilenameUtils.getExtension(absoluteNetworkPath);
    	assertNotNull("network extension is NULL; should be one of the following: SIF, XBEL, XGMML, BIOPAX : ",  networkFileNameExtension);
  
        // wait for the network upload task to finish
    	Task task = NetworkUtils.waitForTaskToFinish(ndex, testAccount);        	
        Object networkUUIDobj = task.getAttribute("networkUUID");
		assertNotNull("network UUID of uploaded network is null", networkUUIDobj);
		String networkUUID = networkUUIDobj.toString(); 
    
		
		// restart server without removing the database; we need this to make
		// sure there is only one user task on the server that we can track
    	String responseFromServer = JettyServerUtils.sendCommand("restartServerWithoutCleaningDatabase");
    	assertEquals("unable to restart Jetty Server: ",  "done", responseFromServer);
		
        // export network; note that the network extension is in UPPER CASE
    	String taskId = NetworkUtils.exportNetwork(ndex, networkUUID, networkFileNameExtension.toUpperCase());
    	assertNotNull("unable to create export network task", taskId);
    	Status status = NetworkUtils.waitForTaskToFinish(ndex, taskId); 
    	assertEquals("export task didn't complete cleanly", Status.COMPLETED, status);	


        // export network again; this time network extension is in LOWER CASE -- this will fail
    	taskId = NetworkUtils.exportNetwork(ndex, networkUUID, networkFileNameExtension.toLowerCase());
    	assertNotNull("unable to create export network task", taskId);
    	status = NetworkUtils.waitForTaskToFinish(ndex, taskId); 
    	assertEquals("export task didn't complete cleanly", Status.COMPLETED, status);
    	
    	
    	// delete network from the test account
    	NetworkUtils.deleteNetwork(ndex, networkUUID.toString());    
    }   
    
    // still to do:

   /*     
    *     15) public ProvenanceEntity getProvenance(String networkId)
    *     
    *     18) public Network queryNetworkByEdgeFilter(String networkId, EdgeCollectionQuery query)		
    *     	
    *     19) public Collection<NetworkSummary> searchNetwork(SimpleNetworkQuery query, int skipBlocks, int blockSize)	
    *     		
    *     20) public Collection<NetworkSummary> searchNetworkByPropertyFilter(NetworkPropertyFilter query)	
    *     		
    *     23) public int setNetworkProperties(String networkId, List<NdexPropertyValuePair> properties)		
    *     
    *     24) public ProvenanceEntity setProvenance(String networkId, ProvenanceEntity provenance) 
    *       					
    *     25) public NetworkSummary updateNetwork(Network newNetwork)
    */
    
}

