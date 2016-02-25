package org.ndexbio.rest.test.api;


import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
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
import org.ndexbio.model.network.query.EdgeByEdgePropertyFilter;
import org.ndexbio.model.network.query.EdgeByNodePropertyFilter;
import org.ndexbio.model.network.query.EdgeCollectionQuery;
import org.ndexbio.model.network.query.NetworkPropertyFilter;
import org.ndexbio.model.network.query.PropertySpecification;
import org.ndexbio.model.object.Membership;
import org.ndexbio.model.object.MembershipType;
import org.ndexbio.model.object.NdexPropertyValuePair;
import org.ndexbio.model.object.NdexProvenanceEventType;
import org.ndexbio.model.object.NewUser;
import org.ndexbio.model.object.Permissions;
import org.ndexbio.model.object.ProvenanceEntity;
import org.ndexbio.model.object.SimpleNetworkQuery;
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
 *      3) public NetworkSummary createNetwork(PropertyGraphNetwork newNetwork)
 *  √   4) public void deleteNetwork(String id)
 *  √   5) public int deleteNetworkMembership(String networkId, String  userUUID)
 *  √   6) public String exportNetwork(String networkId, String format)
 *  √   7) public List<BaseTerm> getBaseTerms(String networkId, int skipBlocks, int blockSize)
 *  √   8) public Response getCompleteNetwork(String networkId)
 *      9) public PropertyGraphNetwork getCompleteNetworkAsPropertyGraph(String networkId)
 *  √  10) public Network getEdges(String networkId, int skipBlocks, int blockSize)
 *  √  11) public List<Namespace> getNamespaces(String networkId, int skipBlocks, int blockSize)
 *  √  12) public NetworkSummary getNetworkSummary(String networkId)
 *  √  13) public List<Membership> getNetworkUserMemberships(String networkId, String permissions, int skipBlocks, int blockSize)
 *     14) public PropertyGraphNetwork getPropertyGraphEdges(String networkId, int skipBlocks,  int blockSize)
 *  √  15) public ProvenanceEntity getProvenance(String networkId)
 *  √  16) public Network queryNetwork(String networkId, SimplePathQuery queryParameters)
 *     17) public PropertyGraphNetwork queryNetworkAsPropertyGraph(String networkId, SimplePathQuery queryParameters)
 *  √  18) public Network queryNetworkByEdgeFilter(String networkId, EdgeCollectionQuery query)			
 *  √  19) public Collection<NetworkSummary> searchNetwork(SimpleNetworkQuery query, int skipBlocks, int blockSize)			
 *  √  20) public Collection<NetworkSummary> searchNetworkByPropertyFilter(NetworkPropertyFilter query)			
 *  √  21) public String setNetworkFlag(String networkId, String parameter, String value)			
 *     22) public int setNetworkPresentationProperties(String networkId, List<SimplePropertyValuePair> properties)
 *  √  23) public int setNetworkProperties(String networkId, List<NdexPropertyValuePair> properties)		
 *  √  24) public ProvenanceEntity setProvenance(String networkId, ProvenanceEntity provenance)   					
 *  √  25) public NetworkSummary updateNetwork(Network newNetwork)
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
    @Test  
    public void test0010createNetwork()  {
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
    	///NetworkUtils.deleteNetwork(ndex, networkSummary.getExternalId().toString());        
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
    @Test  
    public void test0020uploadNetwork()  {
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
     * make it read-only, download it and make sure that the read-only properties (readOnlyCacheId
     * and readOnlyCommitId) are set correctly, make the network read-write again and delete it.
     * 
     * Loop through these steps 10 times, sleeping 2 seconds after every iteration.
     * 
     * APIs tested: public NetworkSummary createNetwork(Network newNetwork)
     *              public Response getCompleteNetwork(String networkId)
     *              public String setNetworkFlag(String networkId, String parameter, String value)
     *              public void deleteNetwork(String id)
     * 
     * @param   void
     * @return  void
     */
    @Test  
    public void test0030getCompleteReadOnlyNetwork()  {
    	// network in JSON format to be created on the Server via API
    	TreeMap<String, String> testJSONNetworkToCreate = 
    				PropertyFileUtils.parsePropertyFile(networksAServicePropertyFile);
		
    	// absolute path name of the file containing network in JSON format; defined in properties file: "createNetwork = ..."
		String absoluteNetworkPath = testJSONNetworkToCreate.get("createNetwork");
		assertNotNull("network path is null; check properties file", absoluteNetworkPath);

    	// construct Network object to be created on the server by createNetwork()
    	Network network = NetworkUtils.readNetworkFromFile(absoluteNetworkPath); 
    
    	
        for (int i = 0; i < 10; i++) {
        	
            NetworkSummary networkSummary = NetworkUtils.createNetwork(ndex, network);
            assertNotNull("createNetwork returned null", networkSummary);
            assertNotNull("network UUID is null", networkSummary.getExternalId());
            
            String networkUUID = networkSummary.getExternalId().toString();
           
    	    // set network to read-only mode, and download it
            NetworkUtils.setReadOnlyFlag(ndex, networkUUID, true);
        	
            // download the newly created network in read-only mode
		    network = NetworkUtils.getNetwork(ndex, networkUUID); 
		    
		    long readOnlyCacheId  = network.getReadOnlyCacheId();
		    long readOnlyCommitId = network.getReadOnlyCommitId();
		    
			assert  (readOnlyCacheId > 0)  : "readOnlyCacheId = "  + readOnlyCacheId;
			assert  (readOnlyCommitId > 0) : "readOnlyCommitId = " + readOnlyCommitId;
			assert  (readOnlyCommitId == network.getReadOnlyCacheId()) : 
				"readOnlyCommitId = " + readOnlyCommitId + " readOnlyCacheId = "  + readOnlyCacheId;
			
			
		    // set network back to read-write mode before deleting it
	        NetworkUtils.setReadOnlyFlag(ndex, networkUUID, false);
	 
	    	// delete network from the test account
	    	NetworkUtils.deleteNetwork(ndex, networkUUID.toString());
			
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }    
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
    @Test  
    public void test0040getCompleteNetwork()  {
    	// network in JSON format to be created on the Server via API
    	TreeMap<String, String> testJSONNetworkToCreate = 
    				PropertyFileUtils.parsePropertyFile(networksAServicePropertyFile);
		
    	// absolute path name of the file containing network in JSON format; defined in properties file: "createNetwork = ..."
		String absoluteNetworkPath = testJSONNetworkToCreate.get("createNetwork");
		assertNotNull("network path is null; check properties file", absoluteNetworkPath);

    	// construct Network object to be created on the server by createNetwork()
    	Network network1 = NetworkUtils.readNetworkFromFile(absoluteNetworkPath); 
    	//System.out.println("\n\n\ngetReadOnlyCommitId="+network1.getReadOnlyCommitId() + "  getReadOnlyCacheId=" + network1.getReadOnlyCacheId());
    
        NetworkSummary networkSummary = NetworkUtils.createNetwork(ndex, network1);
        assertNotNull("createNetwork returned null", networkSummary);
        assertNotNull("network UUID is null", networkSummary.getExternalId());
        
        String networkUUID = networkSummary.getExternalId().toString();
        
        // download the newly created network
		network1 = NetworkUtils.getNetwork(ndex, networkUUID);
		Network network2 = NetworkUtils.getNetwork(ndex, networkUUID);
		

	    // set network to read-only mode, and download it again
        NetworkUtils.setReadOnlyFlag(ndex, networkUUID, true);
        
        // download the newly created network in read-only mode
		Network network3 = NetworkUtils.getNetwork(ndex, networkUUID); 
		//System.out.println("\n\nreadOnlyCacheId=" + network3.getReadOnlyCacheId() + "  readOnlyCommitId=" + network3.getReadOnlyCommitId());
		
		networkSummary = NetworkUtils.getNetworkSummaryById(ndex, networkUUID);
		//System.out.println("\n\nreadOnlyCacheId=" + networkSummary.getReadOnlyCacheId() + "  readOnlyCommitId=" + networkSummary.getReadOnlyCommitId());

		network3 = NetworkUtils.getNetwork(ndex, networkUUID); 
		//System.out.println("\n\nreadOnlyCacheId=" + network3.getReadOnlyCacheId() + "  readOnlyCommitId=" + network3.getReadOnlyCommitId());		
		
		networkSummary = NetworkUtils.getNetworkSummaryById(ndex, networkUUID);
		//System.out.println("\n\nreadOnlyCacheId=" + networkSummary.getReadOnlyCacheId() + "  readOnlyCommitId=" + networkSummary.getReadOnlyCommitId());
		
        boolean compareReadOnly = true;  // should be false when comparing read-only and read-write networks
        NetworkUtils.compareObjectsContents(network1, network2, compareReadOnly);
        NetworkUtils.compareObjectsContents(network2, network3, compareReadOnly = false);
        NetworkUtils.compareObjectsContents(network1, network3, compareReadOnly = false);

        
	    // set network back to read-write mode, and download it again
        NetworkUtils.setReadOnlyFlag(ndex, networkUUID, false);
		Network network4 = NetworkUtils.getNetwork(ndex, networkUUID);
		
        NetworkUtils.compareObjectsContents(network1, network4, compareReadOnly = true);
        NetworkUtils.compareObjectsContents(network2, network4, compareReadOnly = true);
        NetworkUtils.compareObjectsContents(network3, network4, compareReadOnly = true); 
        
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
    
    @Test  
    public void test0050getCompleteNonExistentNetwork() throws IOException, NdexException  {
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
        assertNotNull("createNetwork() returned null", networkSummary);
        assertNotNull("network UUID is null", networkSummary.getExternalId());
        
        String networkUUID = networkSummary.getExternalId().toString();
        
        // download the newly created network
        network1 = NetworkUtils.getNetwork(ndex, networkUUID);
		Network network2 = NetworkUtils.getNetwork(ndex, networkUUID);
		boolean compareReadOnly = true;
        NetworkUtils.compareObjectsContents(network1, network2, compareReadOnly);
        
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
    @Test  
    public void test0060deleteNonExistentNetwork() throws IOException, NdexException  {
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
        network1 = NetworkUtils.getNetwork(ndex, networkUUID);
		Network network2 = NetworkUtils.getNetwork(ndex, networkUUID);
		boolean compareReadOnly = true;
        NetworkUtils.compareObjectsContents(network1, network2, compareReadOnly);
        
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
    @Test  
    public void test0070updateNetworkSummary()  {
    	// network to be uploaded to the Server
    	TreeMap<String, String> testNetworkToUpload = 
    			PropertyFileUtils.parsePropertyFile(networksAServicePropertyFile);

    	String networkUUID = uploadNetworkFile(testNetworkToUpload.get("uploadNetwork"));
		
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
    @Test  
    public void test0080testNeighborhoodQuery() throws JSONException  {
    	// network to be uploaded to the Server
    	TreeMap<String, String> testNetworkToUpload = 
    			PropertyFileUtils.parsePropertyFile(networksAServicePropertyFile);
		
    	// absolute path name of the query parameters; defined in properties file: "neighborHoodQuery = ..."
		String neighborHoodQuery = testNetworkToUpload.get("neighborHoodQuery");
		assertNotNull("neighborHoodQuery is null; check properties file", neighborHoodQuery);
		
		
        String JSONString = neighborHoodQuery.toString();
        JSONObject jsonObj =  new JSONObject(JSONString);
        String absoluteNetworkPath = jsonObj.getString("path");
        String query = jsonObj.getString("query");

        String networkUUID = uploadNetworkFile(absoluteNetworkPath);
		
		int depth = 1;
    	Network subNetworkRetrieved = NetworkUtils.getNeighborhood(ndex, networkUUID, query, depth);
    	assertEquals(85, subNetworkRetrieved.getEdgeCount());
    	assertEquals(87, subNetworkRetrieved.getNodeCount());

    	depth = 2;
    	subNetworkRetrieved = NetworkUtils.getNeighborhood(ndex, networkUUID, query, depth);
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
    @Test  
    public void test0090getNamespaces()  {
    	// network to be uploaded to the Server
    	TreeMap<String, String> testNetworkToUpload = 
    			PropertyFileUtils.parsePropertyFile(networksAServicePropertyFile);
		
    	// absolute path name of the network; defined in properties file: "uploadNetworkForNameSpace = ..."
    	String networkUUID = uploadNetworkFile(testNetworkToUpload.get("uploadNetworkForNameSpace"));

		// retrieve all namespaces and check some 
		List<Namespace> namespaces = NetworkUtils.getNetworkNamespaces(ndex, networkUUID);
		assertEquals("Wrong namespaces count: ", 39, namespaces.size());
		assertEquals("Wrong namespace name: ", "bel", namespaces.get(0).getPrefix());
		assertEquals("Wrong namespace name: ", "PFR", namespaces.get(1).getPrefix());		
		assertEquals("Wrong namespace name: ", "NCR", namespaces.get(2).getPrefix());
		assertEquals("Wrong namespace name: ", "IntegumentarySystem", namespaces.get(38).getPrefix());	
		
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
    @Test  
    public void test0100addNamespace()  {
    	// network to be uploaded to the Server
    	TreeMap<String, String> testNetworkToUpload = 
    			PropertyFileUtils.parsePropertyFile(networksAServicePropertyFile);
		
    	// absolute path name of the network; defined in properties file: "uploadNetworkForNameSpace = ..."
    	String networkUUID = uploadNetworkFile(testNetworkToUpload.get("uploadNetworkForNameSpace"));
		
		// retrieve all namespaces and check some 
		List<Namespace> namespaces = NetworkUtils.getNetworkNamespaces(ndex, networkUUID);
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
		namespaces = NetworkUtils.getNetworkNamespaces(ndex, networkUUID);
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
    @Test  
    public void test0110networkMembership()  {
    	// network to be uploaded to the Server
    	TreeMap<String, String> testNetworkToUpload = 
    			PropertyFileUtils.parsePropertyFile(networksAServicePropertyFile);
		
    	// absolute path name of the network; defined in properties file: "uploadNetworkForMembership = ..."
    	String networkUUID = uploadNetworkFile(testNetworkToUpload.get("uploadNetworkForNameSpace"));
		
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
    @Test  
    public void test0120getBaseTerms() {
    	// network to be uploaded to the Server
    	TreeMap<String, String> testNetworkToUpload = 
    				PropertyFileUtils.parsePropertyFile(networksAServicePropertyFile);
		
    	// absolute path name of the file containing network in JSON format; defined in properties file: "uploadNetworkForBaseTerms = ..."
    	String networkUUID = uploadNetworkFile(testNetworkToUpload.get("uploadNetworkForBaseTerms"));

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
    @Test  
    public void test0130getEdges()  {
    	// network to be uploaded to the Server
    	TreeMap<String, String> testNetworkToUpload = 
    				PropertyFileUtils.parsePropertyFile(networksAServicePropertyFile);
		
    	// absolute path name of the file containing network in JSON format; defined in properties file: "uploadNetworkForEdges = ..."
    	String networkUUID = uploadNetworkFile(testNetworkToUpload.get("uploadNetworkForEdges"));
		
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
     * Create another export task on the server this time specifying network type
     * in lower case -- the task will not be created. 
     * Delete this network from the server.
     *      
     * APIs tested: public void uploadNetwork(UploadedFile uploadedNetwork)
     *              public String exportNetwork(String networkId, String format)
     *              public void deleteNetwork(String id)
     * 
     * @param   void
     * @return  void
     */
    @Test  
    public void test0140exportNetwork() {
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
    	assertNull("created task with wrong format argument", taskId);	
    	
    	// delete network from the test account
    	NetworkUtils.deleteNetwork(ndex, networkUUID.toString());    
    }   

    /** 
     * Upload network specified in the properties file to the server, 
     * and query it by ( Edge Property Name = value ) 
     * Delete this network from the server.
     * 
     * APIs tested: public void uploadNetwork(UploadedFile uploadedNetwork)
     *              public Network queryNetworkByEdgeFilter(String networkId, EdgeCollectionQuery query)
     *              public void deleteNetwork(String id)
     * 
     * @param   void
     * @return  void
     */
    @Test
    public void test0150queryNetworkByEdgeFilter()  {
    	// network to be uploaded to the Server
    	TreeMap<String, String> testNetworkToUpload = 
    				PropertyFileUtils.parsePropertyFile(networksAServicePropertyFile);
		
    	// absolute path name of the file containing network in JSON format; defined in properties file: "uploadNetworkForQueryNetworkByEdgeFilter = ..."
    	String networkUUID = uploadNetworkFile(testNetworkToUpload.get("uploadNetworkForQueryNetworkByEdgeFilter"));
    	
		// restart server without removing the database; we need this to make
		// sure there is only one user task on the server that we can track
    	String responseFromServer = JettyServerUtils.sendCommand("restartServerWithoutCleaningDatabase");
    	assertEquals("unable to restart Jetty Server: ",  "done", responseFromServer);
		 
    	
    	// build the queryNetworkByEdgeFilter query
    	EdgeCollectionQuery query = new EdgeCollectionQuery();
    	query.setEdgeLimit(-1);
    	EdgeByEdgePropertyFilter edgeByNodeFilter = new EdgeByEdgePropertyFilter();
    	ArrayList<PropertySpecification> properties = new ArrayList<PropertySpecification>();
    	PropertySpecification property = new PropertySpecification();
    	property.setName("Disease");
    	property.setValue("Atherosclerosis");
    	properties.add(property);
    	edgeByNodeFilter.setPropertySpecifications(properties);
    	query.setEdgeFilter(edgeByNodeFilter);
    	
    	
    	// issue the queryNetworkByEdgeFilter query
    	Network network = NetworkUtils.queryNetworkByEdgeFilter(ndex, networkUUID, query);
    	assertNotNull("no network retrieved", network);
    	assertEquals("wrong number of nodes", 4, network.getNodeCount());
    	assertEquals("wrong number of edges", 3, network.getEdgeCount());
    	
    	
    	// re-issue the same query with lower edge limit
    	query.setEdgeLimit(2);
    	network = NetworkUtils.queryNetworkByEdgeFilter(ndex, networkUUID, query);
    	assertNotNull("no network retrieved", network);
    	assertEquals("wrong number of nodes", 3, network.getNodeCount());
    	assertEquals("wrong number of edges", 2, network.getEdgeCount());
 
    	
    	// query by predicate with no edge limit
    	property.setName("ndex:predicate");
    	property.setValue("increases");
    	query.setEdgeLimit(-1);
    	network = NetworkUtils.queryNetworkByEdgeFilter(ndex, networkUUID, query);
    	assertNotNull("no network retrieved", network);
    	assertEquals("wrong number of nodes", 21, network.getNodeCount());
    	assertEquals("wrong number of edges", 22, network.getEdgeCount());
    	
    	
    	// delete network from the test account
    	NetworkUtils.deleteNetwork(ndex, networkUUID.toString());	 
    }    

    
    /** 
     * Upload 6 networks.  Assign property 1 to all 6 networks, property 2 to 5 networks,
     * property 3 to 4 networks, ... , property 6 to 1 network.
     * Query database for networks with specific properties and expect to receive 6 
     * NetworkSummary objects when querying for property 1; 5 objects when querying for
     * property 2, etc.
     * 
     * APIs tested: public void uploadNetwork(UploadedFile uploadedNetwork)
     *              public int setNetworkProperties(String networkId, List<NdexPropertyValuePair> properties)		
     *              public Collection<NetworkSummary> searchNetworkByPropertyFilter(NetworkPropertyFilter query)
     *              public void deleteNetwork(String id)
     * 
     * @param   void
     * @return  void
     * @throws NdexException 
     * @throws IOException 
     */
    @Test
    public void test0160networkProperties() throws IOException, NdexException  {
    	// network to be uploaded to the Server
    	TreeMap<String, String> testNetworkToUpload = PropertyFileUtils.parsePropertyFile(networksAServicePropertyFile);

		// start uploading networks to the server
		startUploadingNetworkFile(testNetworkToUpload.get("uploadNetworkForNetworkProperties1"));
		startUploadingNetworkFile(testNetworkToUpload.get("uploadNetworkForNetworkProperties2"));
		startUploadingNetworkFile(testNetworkToUpload.get("uploadNetworkForNetworkProperties3"));
		startUploadingNetworkFile(testNetworkToUpload.get("uploadNetworkForNetworkProperties4"));
		startUploadingNetworkFile(testNetworkToUpload.get("uploadNetworkForNetworkProperties5"));
		startUploadingNetworkFile(testNetworkToUpload.get("uploadNetworkForNetworkProperties6"));
    	
    	// wait for the networks to finish uploading; get their UUIDs
		ArrayList <String> networkUUIDs =  NetworkUtils.waitForNetworksUploadToFinish(ndex, testAccount, 6);
		assertEquals("wrong number of network IDs", 6, networkUUIDs.size());
    	
		// uploaded networks' UUIDs
		String networkUUID1 = networkUUIDs.get(0);
		String networkUUID2 = networkUUIDs.get(1);
		String networkUUID3 = networkUUIDs.get(2);
		String networkUUID4 = networkUUIDs.get(3);
		String networkUUID5 = networkUUIDs.get(4);
		String networkUUID6 = networkUUIDs.get(5);
		
		// ArrayList of the uploaded networks UUIDs
		ArrayList<String> networkIds = new ArrayList<String>();
		networkIds.add(networkUUID1);
		networkIds.add(networkUUID2);
		networkIds.add(networkUUID3);
		networkIds.add(networkUUID4);
		networkIds.add(networkUUID5);
		networkIds.add(networkUUID6);
		

		// create ArrayList of properties that we'll assign to the uploaded networks 
		ArrayList<NdexPropertyValuePair> properties = new ArrayList<NdexPropertyValuePair>();
		NdexPropertyValuePair property1 = new NdexPropertyValuePair("director","Dexter");
		NdexPropertyValuePair property2 = new NdexPropertyValuePair("chiefArchitect","Jing");		
		NdexPropertyValuePair property3 = new NdexPropertyValuePair("publicRelations","Rudi");
		NdexPropertyValuePair property4 = new NdexPropertyValuePair("UILead","David");
		NdexPropertyValuePair property5 = new NdexPropertyValuePair("developer","vrynkov");
		NdexPropertyValuePair property6 = new NdexPropertyValuePair("stakeholder","Aleksandar");		
		
		properties.add(property1);
		properties.add(property2);
		properties.add(property3);
		properties.add(property4);		
		properties.add(property5);	
		properties.add(property6);
		
		
		// network networkUUID1 will have all 6 properties assigned
		int propertyCount = NetworkUtils.setNetworkProperties(ndex, networkUUID1, properties);
		assertEquals(6, propertyCount);
		
		// network networkUUID2 will have properties 1, 2, 3, 4, 5 assigned
		properties.remove(5);
		propertyCount = NetworkUtils.setNetworkProperties(ndex, networkUUID2, properties);
		assertEquals(5, propertyCount);

		// network networkUUID3 will have properties 1, 2, 3, 4
		properties.remove(4);
		propertyCount = NetworkUtils.setNetworkProperties(ndex, networkUUID3, properties);
		assertEquals(4, propertyCount);

		// network networkUUID4 will have properties 1, 2, 3
		properties.remove(3);
		propertyCount = NetworkUtils.setNetworkProperties(ndex, networkUUID4, properties);
		assertEquals(3, propertyCount);

		// network networkUUID5 will have properties 1 and 2
		properties.remove(2);
		propertyCount = NetworkUtils.setNetworkProperties(ndex, networkUUID5, properties);
		assertEquals(2, propertyCount);
		
		// network networkUUID6 will only have property 1 assigned to it
		properties.remove(1);
		propertyCount = NetworkUtils.setNetworkProperties(ndex, networkUUID6, properties);
		assertEquals(1, propertyCount);
		
		
		// create property filter for querying the database for networks with specific properties
		NetworkPropertyFilter propertyFilter = new NetworkPropertyFilter();
		Collection<PropertySpecification> propertySpecs = new ArrayList<PropertySpecification>();
		PropertySpecification propertySpec1 = new PropertySpecification("director","Dexter");
		PropertySpecification propertySpec2 = new PropertySpecification("chiefArchitect","Jing");
		PropertySpecification propertySpec3 = new PropertySpecification("publicRelations","Rudi");
		PropertySpecification propertySpec4 = new PropertySpecification("UILead","David");
		PropertySpecification propertySpec5 = new PropertySpecification("developer","vrynkov");
		PropertySpecification propertySpec6 = new PropertySpecification("stakeholder","Aleksandar");
		
		
		// property 1 is assigned to all 6 networks; we expect to retrieve 6 NetworkSummaries
		propertySpecs.add(propertySpec1);
		propertyFilter.setProperties(propertySpecs);
		ArrayList<NetworkSummary> networkSummaries = (ArrayList<NetworkSummary>) NetworkUtils.searchNetworkByPropertyFilter(ndex, propertyFilter);
		assertEquals(6, networkSummaries.size());
		// verify UUIDs of retrieved 6 Network Summary objects 
		NetworkUtils.verifyNetworkSummaries(networkSummaries, networkIds);
		
		// property 2 is assigned to 5 networks; we expect to retrieve 5 NetworkSummaries	    
		propertySpecs.clear();
		propertySpecs.add(propertySpec2);
		propertyFilter.setProperties(propertySpecs);
		networkSummaries = (ArrayList<NetworkSummary>) NetworkUtils.searchNetworkByPropertyFilter(ndex, propertyFilter);
		assertEquals(5, networkSummaries.size());
		// verify UUIDs of retrieved 5 Network Summary objects  
		NetworkUtils.verifyNetworkSummaries
			(networkSummaries, new ArrayList<String>(networkIds.subList(0, 5)));
		
		// property 3 is assigned to 4 networks; we expect to retrieve 4 NetworkSummaries	
		propertySpecs.clear();
		propertySpecs.add(propertySpec3);
		propertyFilter.setProperties(propertySpecs);
		networkSummaries = (ArrayList<NetworkSummary>) NetworkUtils.searchNetworkByPropertyFilter(ndex, propertyFilter);
		assertEquals(4, networkSummaries.size());
		// verify UUIDs of retrieved 4 Network Summary objects  
		NetworkUtils.verifyNetworkSummaries(networkSummaries, new ArrayList<String>(networkIds.subList(0, 4)));	    	
		
		// property 4 is assigned to 3 networks; we expect to retrieve 3 NetworkSummaries		
		propertySpecs.clear();
		propertySpecs.add(propertySpec4);
		propertyFilter.setProperties(propertySpecs);
		networkSummaries = (ArrayList<NetworkSummary>) NetworkUtils.searchNetworkByPropertyFilter(ndex, propertyFilter);
		assertEquals(3, networkSummaries.size());
		// verify UUIDs of retrieved 3 Network Summary objects  
		NetworkUtils.verifyNetworkSummaries(networkSummaries, new ArrayList<String>(networkIds.subList(0, 3)));	   

		// property 5 is assigned to 2 networks; we expect to retrieve 2 NetworkSummaries	
		propertySpecs.clear();
		propertySpecs.add(propertySpec5);
		propertyFilter.setProperties(propertySpecs);
		networkSummaries = (ArrayList<NetworkSummary>) NetworkUtils.searchNetworkByPropertyFilter(ndex, propertyFilter);
		assertEquals(2, networkSummaries.size());
		// verify UUIDs of retrieved 2 Network Summary objects  
		NetworkUtils.verifyNetworkSummaries(networkSummaries, new ArrayList<String>(networkIds.subList(0, 2)));	   
		
		// property 6 is assigned to 1 networks; only 1 NetworkSummary is expected	
		propertySpecs.clear();
		propertySpecs.add(propertySpec6);
		propertyFilter.setProperties(propertySpecs);
		networkSummaries = (ArrayList<NetworkSummary>) NetworkUtils.searchNetworkByPropertyFilter(ndex, propertyFilter);
		assertEquals(1, networkSummaries.size());
		// verify UUIDs of retrieved 1 Network Summary object
		NetworkUtils.verifyNetworkSummaries(networkSummaries, new ArrayList<String>(networkIds.subList(0, 1)));	    

		
		// try to retrieve networks with 0 properties; none should be retreived
		propertySpecs.clear();
		propertyFilter.setProperties(propertySpecs);
		networkSummaries = (ArrayList<NetworkSummary>) NetworkUtils.searchNetworkByPropertyFilter(ndex, propertyFilter);
		assertEquals(0, networkSummaries.size());
		
		
    	// delete networks from the test account
		for (String networkUUID : networkIds) {
			NetworkUtils.deleteNetwork(ndex, networkUUID);	
		}
    }    
 
    /** 
     * Upload a network. Get its' provenance. Set some network properties.
     * Get networks' provenance again and check the provenance event type.
     * Remove provenance from the network (set it to "empty" provenance).
     * Then download the network, remove some of the networks' properties, and update
     * the network on the server.  Check the networks' provenance again.
     * Delete the network.
     * 
     * APIs tested: public void uploadNetwork(UploadedFile uploadedNetwork)
     *              public ProvenanceEntity getProvenance(String networkId) 
     *              public int setNetworkProperties(String networkId, List<NdexPropertyValuePair> properties)		
     *              public ProvenanceEntity setProvenance(String networkId, ProvenanceEntity provenance)   	
     *              public Response getCompleteNetwork(String networkId)
     *              public NetworkSummary updateNetwork(Network newNetwork)
     *              public void deleteNetwork(String id)
     * 
     * @param   void
     * @return  void
     */
    @Test
    public void test0170provenanceAndNetworkUpdate()   {
    	// network to be uploaded to the Server
    	TreeMap<String, String> testNetworkToUpload = 
    			PropertyFileUtils.parsePropertyFile(networksAServicePropertyFile);
    	
    	// absolute path name of the file containing network in JSON format; defined in properties file: "uploadNetworkForQueryNetworkByEdgeFilter = ..."
    	String networkUUID = uploadNetworkFile(testNetworkToUpload.get("uploadNetworkForProvenance"));
    	
    	ProvenanceEntity provenance = NetworkUtils.getProvenance(ndex, networkUUID);
    	assertNotNull("provenace is null", provenance);
    	assertEquals("wrong provenance event type :",
    			provenance.getCreationEvent().getEventType(), NdexProvenanceEventType.FILE_UPLOAD);

    	
		// create ArrayList of properties that we'll assign to the uploaded network
		ArrayList<NdexPropertyValuePair> properties = new ArrayList<NdexPropertyValuePair>();
		NdexPropertyValuePair property1 = new NdexPropertyValuePair("director","Dexter");
		NdexPropertyValuePair property2 = new NdexPropertyValuePair("chiefArchitect","Jing");		
		NdexPropertyValuePair property3 = new NdexPropertyValuePair("publicRelations","Rudi");
		NdexPropertyValuePair property4 = new NdexPropertyValuePair("UILead","David");
		NdexPropertyValuePair property5 = new NdexPropertyValuePair("developer","vrynkov");
		NdexPropertyValuePair property6 = new NdexPropertyValuePair("stakeholder","Aleksandar");		
		
		properties.add(property1);
		properties.add(property2);
		properties.add(property3);
		properties.add(property4);		
		properties.add(property5);	
		properties.add(property6);
		

		// assign these 6 properties to the network 
		int propertyCount = NetworkUtils.setNetworkProperties(ndex, networkUUID, properties);
		assertEquals("wrong number of properties : ", 6, propertyCount);
   
		provenance = NetworkUtils.getProvenance(ndex, networkUUID);
    	assertNotNull("provenace is null", provenance);
    	assertNotNull("provenance creation event is null :", provenance.getCreationEvent()); 
    	assertEquals("wrong provenance event type :",
    			provenance.getCreationEvent().getEventType(), 
    			NdexProvenanceEventType.SET_NETWORK_PROPERTIES);
		
    	// clear the network provenance 
    	ProvenanceEntity newProvenance = new ProvenanceEntity();
    	ProvenanceEntity setProvenance = NetworkUtils.setProvenance(ndex, networkUUID, newProvenance);
    	assertNotNull("failed to set provenance : ", setProvenance);
    	
		
    	Network network = NetworkUtils.getNetwork(ndex, networkUUID);
		assertNotNull("network number of properties : ", network);
    	
		properties.remove(5);
		properties.remove(4);
		properties.remove(3);	
		network.setProperties(properties);
		
		NetworkSummary updatedNetwork = NetworkUtils.updateNetwork(ndex, network);
		assertNotNull("failed to update network : ", updatedNetwork);
    	
		provenance = NetworkUtils.getProvenance(ndex, networkUUID);
    	assertNotNull("provenace is null", provenance);
    	assertNotNull("provenance creation event is null :", provenance.getCreationEvent()); 
    	assertEquals("wrong provenance event type :",
    			provenance.getCreationEvent().getEventType(), 
    			NdexProvenanceEventType.UPDATE_NETWORK_PROFILE);
    	
    	
    	// add the properties back
		properties.add(property4);		
		properties.add(property5);	
		properties.add(property6);
		network.setProperties(properties);
		
		updatedNetwork = NetworkUtils.updateNetwork(ndex, network);
		assertNotNull("failed to update network : ", updatedNetwork);
    	
		provenance = NetworkUtils.getProvenance(ndex, networkUUID);
    	assertNotNull("provenace is null", provenance);
    	assertNotNull("provenance creation event is null :", provenance.getCreationEvent());    	
    	assertEquals("wrong provenance event type :",
    			provenance.getCreationEvent().getEventType(), 
    			NdexProvenanceEventType.UPDATE_NETWORK_PROFILE);
		
    	NetworkUtils.deleteNetwork(ndex, networkUUID);
    }    
    
    /** 
     * Upload 6 networks.  Search these networks for simple search term.
     * 
     * APIs tested: public void uploadNetwork(UploadedFile uploadedNetwork)
     *              public Collection<NetworkSummary> searchNetwork(SimpleNetworkQuery query, int skipBlocks, int blockSize)			
     *              public void deleteNetwork(String id)
     * 
     * @param   void
     * @return  void
     * @throws NdexException 
     * @throws IOException 
     */
    @Test
    public void test0180searchNetwork() throws IOException, NdexException  {
    	// network to be uploaded to the Server
    	TreeMap<String, String> testNetworkToUpload = PropertyFileUtils.parsePropertyFile(networksAServicePropertyFile);

		// start uploading networks to the server
		startUploadingNetworkFile(testNetworkToUpload.get("uploadNetworkForSearchNetwork1"));
		startUploadingNetworkFile(testNetworkToUpload.get("uploadNetworkForSearchNetwork2"));
		startUploadingNetworkFile(testNetworkToUpload.get("uploadNetworkForSearchNetwork3"));
		startUploadingNetworkFile(testNetworkToUpload.get("uploadNetworkForSearchNetwork4"));
		startUploadingNetworkFile(testNetworkToUpload.get("uploadNetworkForSearchNetwork5"));
		startUploadingNetworkFile(testNetworkToUpload.get("uploadNetworkForSearchNetwork6"));
    	
    	// wait for the networks to finish uploading; get their UUIDs
		ArrayList <String> networkUUIDs =  NetworkUtils.waitForNetworksUploadToFinish(ndex, testAccount, 6);
		assertEquals("wrong number of network IDs", 6, networkUUIDs.size());
    	
		// uploaded networks' UUIDs
		String networkUUID1 = networkUUIDs.get(0);
		String networkUUID2 = networkUUIDs.get(1);
		String networkUUID3 = networkUUIDs.get(2);
		String networkUUID4 = networkUUIDs.get(3);
		String networkUUID5 = networkUUIDs.get(4);
		String networkUUID6 = networkUUIDs.get(5);
		
		// ArrayList of the uploaded networks UUIDs
		ArrayList<String> networkIds = new ArrayList<String>();
		networkIds.add(networkUUID1);
		networkIds.add(networkUUID2);
		networkIds.add(networkUUID3);
		networkIds.add(networkUUID4);
		networkIds.add(networkUUID5);
		networkIds.add(networkUUID6);
		
		
		SimpleNetworkQuery query = new SimpleNetworkQuery();
		
		// search for word "BEL" in the name
		query.setSearchString("BEL");
		
		int skipBlocks = 0;
		int blockSize  = 500;
		
		ArrayList<NetworkSummary> networkSummaries = 
				(ArrayList<NetworkSummary>) NetworkUtils.searchNetwork(ndex, query, skipBlocks, blockSize);
		assertEquals("retrieved wrong number of networkSummary objects : ", 1, networkSummaries.size());
		assertEquals("wrong number of nodes", 37, networkSummaries.get(0).getNodeCount());
		assertEquals("wrong number of edges", 37, networkSummaries.get(0).getEdgeCount());		
		assertEquals("wrong description : ", 
				"Approximately 2000 hand curated statements drawn from 57 PubMeds", 
				networkSummaries.get(0).getDescription());
		assertEquals("wrong network name : ", "BEL Framework Small Corpus Document", 
				networkSummaries.get(0).getName());
		
		
		// search for word "network" in the name
		query.setSearchString("network");
		networkSummaries = 
				(ArrayList<NetworkSummary>) NetworkUtils.searchNetwork(ndex, query, skipBlocks, blockSize);
		assertEquals("retrieved wrong number of networkSummary objects : ", 2, networkSummaries.size());
		
		assertEquals("wrong number of nodes", 15, networkSummaries.get(0).getNodeCount());
		assertEquals("wrong number of edges", 11, networkSummaries.get(0).getEdgeCount());		
		assertEquals("wrong description : ", "N/A", networkSummaries.get(0).getDescription());
		assertEquals("wrong network name : ", "Network 0", networkSummaries.get(0).getName());
		
		assertEquals("wrong number of nodes", 109, networkSummaries.get(1).getNodeCount());
		assertEquals("wrong number of edges", 317, networkSummaries.get(1).getEdgeCount());		
		assertEquals("wrong description : ", "", networkSummaries.get(1).getDescription());
		assertEquals("wrong network name : ", "Glucocorticoid receptor regulatory network", 
				networkSummaries.get(1).getName());
	
		
		// search for word "network" or "bel" in the name
		query.setSearchString("network bel");
		networkSummaries = 
				(ArrayList<NetworkSummary>) NetworkUtils.searchNetwork(ndex, query, skipBlocks, blockSize);
		assertEquals("retrieved wrong number of networkSummary objects : ", 3, networkSummaries.size());

		
		// search for word "network" or "bel" in the name; 
		// use the loop to retrieve 1, 2 or 3 NetworkSummary objects
		query.setSearchString("network bel");
		for (blockSize = 1; blockSize < 4; blockSize++) {
		    networkSummaries = 
				(ArrayList<NetworkSummary>) NetworkUtils.searchNetwork(ndex, query, skipBlocks, blockSize);
		    assertEquals("retrieved wrong number of networkSummary objects : ", blockSize, networkSummaries.size());
		}
		
    	// delete networks from the test account
		for (String networkUUID : networkIds) {
			NetworkUtils.deleteNetwork(ndex, networkUUID);	
		}
    }    

    
    
    private static void startUploadingNetworkFile(String path) {
   
		assertNotNull("network path is null; check properties file", path);
    	
    	File fileToUpload = new File(path);
    	NetworkUtils.startNetworkUpload(ndex, fileToUpload);  
    	
    	return;
    }


    private static String uploadNetworkFile(String path) {
    	
		assertNotNull("network path is null; check properties file", path);
    	
    	File fileToUpload = new File(path);
    	NetworkUtils.startNetworkUpload(ndex, fileToUpload);   	
  
        // wait for the network upload task to finish
    	Task task = NetworkUtils.waitForTaskToFinish(ndex, testAccount);        	
        Object networkUUIDobj = task.getAttribute("networkUUID");
		assertNotNull("network UUID of uploaded network is null", networkUUIDobj);
		
    	String responseFromServer = JettyServerUtils.sendCommand("restartServerWithoutCleaningDatabase");
    	assertEquals("unable to restart Jetty Server: ",  "done", responseFromServer);
    	
    	return networkUUIDobj.toString();
    }   
}

