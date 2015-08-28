package org.ndexbio.rest.test.api;


import static org.junit.Assert.*;


import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import org.ndexbio.model.object.NewUser;
import org.ndexbio.model.object.User;

import org.ndexbio.rest.client.NdexRestClient;
import org.ndexbio.rest.client.NdexRestClientModelAccessLayer;
import org.ndexbio.rest.test.utilities.JUnitTestSuiteProperties;
import org.ndexbio.rest.test.utilities.JettyServerUtils;
import org.ndexbio.rest.test.utilities.UserUtils;



/**
 *  This class contains JUNit tests for testing NetworkAService APIs from the 
 *  NetworkAService.java class located in src/main/java/org.ndexbio.rest.services package of ndexbio-rest module.
 *
 *  APIs tested in this class:
 *
 *  APIs tested in this class:
 *  
 *      1) public NetworkAService(HttpServletRequest)
 *      2) public void addNamespace(String networkId, Namespace namespace)
 *      3) public NetworkSummary createNetwork(Network newNetwork)
 *      4) public NetworkSummary createNetwork(PropertyGraphNetwork newNetwork)
 *      5) public void deleteNetwork(String id)
 *      6) public int deleteNetworkMembership(String networkId, String  userUUID)
 *      7) public String exportNetwork(String networkId, String format)
 *      8) public List<BaseTerm> getBaseTerms(String networkId, int skipBlocks, int blockSize)
 *      9) public Response getCompleteNetwork(String networkId)
 *     10) public PropertyGraphNetwork getCompleteNetworkAsPropertyGraph( String networkId)
 *     11) public Network getEdges(String networkId, int skipBlocks, int blockSize)
 *     12) public List<Namespace> getNamespaces(String networkId, int skipBlocks, int blockSize)
 *     13) public NetworkSummary getNetworkSummary( String networkId)
 *     14) public List<Membership> getNetworkUserMemberships(String networkId, String permissions, int skipBlocks, int blockSize)
 *     15) public PropertyGraphNetwork getPropertyGraphEdges(String networkId, int skipBlocks,  int blockSize)
 *     16) public ProvenanceEntity getProvenance( String networkId)
 *     17) public Network queryNetwork(String networkId, SimplePathQuery queryParameters)
 *     18) public PropertyGraphNetwork queryNetworkAsPropertyGraph(String networkId, SimplePathQuery queryParameters)
 *     19) public Network queryNetworkByEdgeFilter(String networkId, EdgeCollectionQuery query)			
 *     20) public Collection<NetworkSummary> searchNetwork(SimpleNetworkQuery query, int skipBlocks, int blockSize)			
 *     21) public Collection<NetworkSummary> searchNetworkByPropertyFilter(NetworkPropertyFilter query)			
 *     22) public String setNetworkFlag(String networkId, String parameter, String value)			
 *     23) public int setNetworkPresentationProperties(String networkId, List<SimplePropertyValuePair> properties)
 *     24) public int setNetworkProperties(String networkId, List<NdexPropertyValuePair> properties)		
 *     25) public ProvenanceEntity setProvenance(String networkId,  ProvenanceEntity provenance)   					
 *     26) public NetworkSummary updateNetwork(Network newNetwork)
 *     27) public int updateNetworkMembership(String networkId, Membership membership)
 *     28) public void updateNetworkProfile(String networkId, NetworkSummary summary)
 *     29) public void uploadNetwork(UploadedFile uploadedNetwork)	    					
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
 
    private static String accountName     = "fff";
    private static String accountPassword = "fff";

    // testUser is the user that will be created on the NDEx server as part of testing
    // prior to testing, this account should not exist on this server
    private static User    testAccount    = null;
    private static NewUser testUser       = null;
    
    private static Process jettyServer    = null;
    
	/**
	 * This methods runs once before any of the test methods in the class.
	 * It creates ndex client used by other tests.
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
    	UserUtils.deleteUser(ndex);
    	
		// create test account
    	testAccount = UserUtils.createUserAccount(ndex, testUser);
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
    
    @Test
    public void test() {
        fail("Not yet implemented");
    }

}

