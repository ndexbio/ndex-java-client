package org.ndexbio.rest.test.api;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.TreeMap;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;

/*
 * This class contains JUNit tests for UserService.java APIs located in
 * src/main/java/org.ndexbio.rest.services package of ndexbio-rest module.
 */
import org.junit.runners.MethodSorters;
import org.ndexbio.model.exceptions.NdexException;
import org.ndexbio.model.object.NewUser;
import org.ndexbio.model.object.Task;
import org.ndexbio.model.object.User;
import org.ndexbio.model.object.network.Network;
import org.ndexbio.model.object.network.NetworkSummary;
import org.ndexbio.rest.client.NdexRestClient;
import org.ndexbio.rest.client.NdexRestClientModelAccessLayer;
import org.ndexbio.rest.test.utilities.DownloadNetwork;
import org.ndexbio.rest.test.utilities.NetworkUtils;
import org.ndexbio.rest.test.utilities.PropertyFileUtils;
import org.ndexbio.rest.test.utilities.UpdateNetworkProfile;
import org.ndexbio.rest.test.utilities.UserUtils;


// The @FixMethodOrder(MethodSorters.NAME_ASCENDING) annotation sorts (and
// executes) the test methods by name in lexicographic order
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class testNetworkConcurrentAcess {
	static String resourcePath = "src/test/resources/";
	static String networksFile = resourcePath + "testNetworkConcurrentAcess.properties";
	static TreeMap<String, String> testNetworks;
	
	static File fileToUpload = null;
	
    private static NdexRestClient                 client;
    private static NdexRestClientModelAccessLayer ndex;
 
    private static String accountName     = "aaa";
    private static String accountPassword = "aaa";	

    private static User    testAccount    = null;
    private static NewUser testUser       = null;

	private static String  testNetworkUUID = null;
	
	private static final int NUM_OF_THREADS_TO_CREATE = 5;
    
	/**
	 * This methods runs once before any of the test methods in the class.
	 * It creates ndex client used by other tests.
	 * 
     * @param   void
     * @return  void
     */
    @BeforeClass
    public static void setUp() throws Exception {
    	// build Map of networks for testing from the property file
		testNetworks = PropertyFileUtils.parsePropertyFile(networksFile);
		
		if (null == testNetworks) {
			fail("No network to upload specified in " + networksFile);
		}
		
    	// create user object; the properties describe the current test set-up
        testUser = UserUtils.getNewUser(
				accountName,
				accountPassword,
		        "This account is used for testing concurrent access to network profile",  // description
		        "concurrent_acess@xxxxxx.com",                     // email address
		        "User",                                            // first name -- name of the test
		        "Concurrent Access",                               // last name -- name of the test		        
		        "http://www.yahoo.com",                            // image
		        "http://www.yahoo.com/finance");                   // web-site
        
		// create ndex client and a test user account
        try {
            client = new NdexRestClient(accountName, accountPassword, JUnitTestSuite.testServerURL);
            ndex   = new NdexRestClientModelAccessLayer(client);
        } catch (Exception e) {
        	fail("Unable to create ndex client: " + e.getMessage());
        }
        
        // in case user account exists, delete it
    	UserUtils.deleteUser(ndex);
    	
		// network file to upload
    	String networkPath = testNetworks.firstEntry().getValue();
        fileToUpload = new File(networkPath);
    }
    
    /**
     * Clean-up method.  The last method called in this class by JUnit framework.
     * It removes all networks uploaded to the test account, and removes the test
     * account itself.
     * 
     * @throws  Exception
     * @param   void
     * @return  void
     */
    @AfterClass
    public static void tearDown() throws Exception {	
    	// delete the test user account
    	// UserUtils.deleteUser(ndex);
    }
    
	/**
	 * This methods runs before every test case.
	 * It uploads test network to the server.
	 * 
     * @param   void
     * @return  void
     */
    @Before public void loadNetwork() {
		// create test account
		testAccount = UserUtils.createUserAccount(ndex, testUser);
    	
    	// start uploading network to the test account
    	NetworkUtils.startNetworkUpload(ndex, fileToUpload);
    	
    	// wait till network is uploaded
    	Task task = NetworkUtils.waitForTaskToFinish(ndex, testAccount);
    	
    	// get UUID of the network we just uploaded
    	testNetworkUUID = task.getAttribute("networkUUID").toString();
    }
    
    
	/**
	 * This methods runs after every test case.
	 * It deletes test network that was uploaded to the server.
	 * 
     * @param   void
     * @return  void
     */
    @After public void deleteNetwork() {
    	// delete the network we uploaded before
    	NetworkUtils.deleteNetwork(ndex, testNetworkUUID);
    	
    	// wait till network is deleted
    	Task task = NetworkUtils.waitForTaskToFinish(ndex, testAccount);
    	
    	// delete the test user account
    	UserUtils.deleteUser(ndex);
    }
    
	
    /*
     * Test whether NDEx can handle concurrent access (multiple clients/threads) to the same network correctly. 
     * Steps:
     * 1) download network from the test server
     * 2) create (but don't start yet) Thread 1.  This thread modifies profile of a large network on the test server.
     * 3) create (but don't start yet) Thread 2.  This thread downloads the large network from the test server.
     * 4) start Thread 1 and Thread 2. Wait for them to finish.
     * 5) compare properties of networks downloaded at Step 1 and Step 4 (number of nodes, edges, 
     *    base terms, name space, citations).  They should be the same.
     * 6) get the network summary/profile (name, description, version) from the server and check 
     *    if it updated correctly.
     * 7) lastly, restore the original values of the network profile on the test server.   
     */
    @Test
    public void test0001DownloadNetworkModifyProfile() {    	
    	
    	// now, download test network from the server
    	Network originalNetwork = null;
    	try {
			originalNetwork = ndex.getNetwork(testNetworkUUID);
		} catch (IOException | NdexException  e) {
			fail("Unable to download network " + testNetworkUUID + " : " + e.getMessage());
		}
    		
    	// modify profile of the network on the server
    	NetworkSummary networkSummary = new NetworkSummary();
    	
    	networkSummary.setName("Modified -- " + originalNetwork.getName());
    	networkSummary.setDescription("Modified -- " + originalNetwork.getDescription());
        networkSummary.setVersion("Modified -- " + originalNetwork.getVersion());
        
    	UpdateNetworkProfile updateThread = 
                new UpdateNetworkProfile(ndex, testNetworkUUID, networkSummary);
    	
    	DownloadNetwork downloadThread = new DownloadNetwork(ndex, testNetworkUUID);
    	
    	// start network profile update and network download threads
        updateThread.start();
        downloadThread.start();
    	try {
    		// wait till profile update is finished
			updateThread.join();
			
			// wait till network is downloaded
			downloadThread.join();
		} catch (InterruptedException e) {
			fail("Unable to update or download network " + testNetworkUUID + " : " + e.getMessage());
		}
    	
    	// get the summary of updated network
        //NetworkSummary updatedNetworkSummary = update.getUpdatedNetworkSummary();
 
    	// get the network downloaded in the download thread; compare its properties
        // with the properties of the original network
        Network network = downloadThread.getNetwork();

        NetworkUtils.compareObjectsContents(originalNetwork, network);

        // now, let's check if the network profile has updated correctly
        NetworkSummary updatedNetworkSummary = null;
    	try {
    	    updatedNetworkSummary = ndex.getNetworkSummaryById(testNetworkUUID);
    	} catch (Exception e) {
			fail("Unable to download summary of network " + testNetworkUUID + " : " + e.getMessage());
        }
    	
    	assertEquals("Network name didn't update correctly", updatedNetworkSummary.getName(), networkSummary.getName());
    	assertEquals("Network description didn't update correctly", updatedNetworkSummary.getDescription(), networkSummary.getDescription());
    	assertEquals("Network version didn't update correctly", updatedNetworkSummary.getVersion(), networkSummary.getVersion());


    	// now, restore the original profile values
    	networkSummary.setName(originalNetwork.getName());
    	networkSummary.setDescription(originalNetwork.getDescription());
        networkSummary.setVersion(originalNetwork.getVersion());
        updateThread = new UpdateNetworkProfile(ndex, testNetworkUUID, networkSummary);
        updateThread.start();
    	try {
    		// wait till profile update is finished
			updateThread.join();
		} catch (InterruptedException e) {
			fail("Unable to restore original profile values for  network " + testNetworkUUID + " : " + e.getMessage());
		}
    
    	try {
    	    updatedNetworkSummary = ndex.getNetworkSummaryById(testNetworkUUID);
    	} catch (Exception e) {
			fail("Unable to download summary of network " + testNetworkUUID + " : " + e.getMessage());
        }
    	
    	assertEquals("Failed to restore original name of network", updatedNetworkSummary.getName(), networkSummary.getName());
    	assertEquals("Failed to restore original summary of network", updatedNetworkSummary.getDescription(), networkSummary.getDescription());
    	assertEquals("Failed to restore original version of network", updatedNetworkSummary.getVersion(), networkSummary.getVersion());
    }
    
    /*
     * Test whether NDEx can handle concurrent access (multiple clients/threads) to the same network correctly. 
     * Steps:
     * 1) create (but don't start yet) downloadThread.  This thread downloads a large network from the test server.
     * 2) create (but don't start yet) updateThread.  This thread modifies profile (name, description, version) 
     *    of the same large network on the server as downloadThread progresses. 
     * 3) start downloadThread and updateThread. Wait for updateThread to finish.  
     * 4) create and start NUM_OF_THREADS_TO_CREATE of downloadThreads that will download the network.
     * 5) After all NUM_OF_THREADS_TO_CREATE threads have finished, make sure they have modified profiles.
     */    
    @Test
    public void test0010DownloadNetworkModifyProfile() {
    	// download test network from the server
    	Network originalNetwork = null;
    	try {
			originalNetwork = ndex.getNetwork(testNetworkUUID);
		} catch (IOException | NdexException  e) {
			fail("Unable to download network " + testNetworkUUID + " : " + e.getMessage());
		}   	
    	
    	DownloadNetwork downloadThread = new DownloadNetwork(ndex, testNetworkUUID);
    	
    	// modify profile of the network on the server
    	NetworkSummary networkSummary = new NetworkSummary();
    	
    	networkSummary.setName("Modified -- " + originalNetwork.getName());
    	networkSummary.setDescription("Modified -- " + originalNetwork.getDescription());
        networkSummary.setVersion("Modified -- " + originalNetwork.getVersion());
        
    	UpdateNetworkProfile updateThread = 
                new UpdateNetworkProfile(ndex, testNetworkUUID, networkSummary);
    	
    	downloadThread.start();
    	updateThread.start();
    	try {
    		// wait till profile update is finished
			updateThread.join();
		} catch (InterruptedException e) {
			fail("Unable to update profile of network " + testNetworkUUID + " : " + e.getMessage());
		}

    	Thread[] threads = new Thread[NUM_OF_THREADS_TO_CREATE];
    	for (int i=0; i<threads.length; i++) {
    		threads[i] = new DownloadNetwork(ndex, testNetworkUUID);
    	}
    	for (int i=0; i<threads.length; i++) {
    		threads[i].start();
    	}    	

    	try {
    		// wait till network download is finished
    		downloadThread.join();
		} catch (InterruptedException e) {
			fail("Unable download network " + testNetworkUUID + " : " + e.getMessage());
		}
   
    	try {
    		// wait till networks with modified profile finish downloading
        	for (int i=0; i<threads.length; i++) {
        		threads[i].join();
        	}
		} catch (InterruptedException e) {
			fail("Unable to download network " + testNetworkUUID + " : " + e.getMessage());
		}

    	for (int i=0; i<threads.length; i++) {
     		
    		Network network = ((DownloadNetwork)threads[i]).getNetwork();
    		
        	// check profiles of downloaded networks 
        	assertEquals("Network name didn't update correctly",        network.getName(),        networkSummary.getName());
        	assertEquals("Network description didn't update correctly", network.getDescription(), networkSummary.getDescription());
        	assertEquals("Network version didn't update correctly",     network.getVersion(),     networkSummary.getVersion());    	
    	}
    }
 

    /*
     * Test whether NDEx can handle concurrent access (multiple clients/threads) to the same network correctly. 
     * Steps:
     * 1) upload network to the server
     * 2) download the uploaded network
     * 3) modify its' profile
     * 4) send it back to the server (update it on the server)
     * 5) check if it updated correctly
     */    
    @Test
    public void test0020UpdateNetwork() throws InterruptedException {
    	
    	// download test network from the server
    	Network network = null;
    	try {
			network = ndex.getNetwork(testNetworkUUID);
		} catch (IOException | NdexException  e) {
			fail("Unable to download network " + testNetworkUUID + " : " + e.getMessage());
		}

    	// modify network profile 
    	network.setName("Modified -- " + network.getName());
    	network.setDescription("Modified -- " + network.getDescription());
    	network.setVersion("Modified -- " + network.getVersion());
    	
    	
    	// send the modified network back to the server (update it on the server)
    	NetworkSummary updatedNetworkSummary = null;
    	try {
			updatedNetworkSummary = ndex.updateNetwork(network);
		} catch (Exception  e) {
			fail("Unable to update network " + testNetworkUUID + " : " + e.getMessage());
		}
   	
    	// check if the network summary updated correctly
    	assertEquals("Failed to update network name",        network.getName(),        updatedNetworkSummary.getName());
    	assertEquals("Failed to update network description", network.getDescription(), updatedNetworkSummary.getDescription());
    	assertEquals("Failed to update network version",     network.getVersion(),     updatedNetworkSummary.getVersion());
    }   
    
    
    
    
	/*
    @BeforeClass
    public static void setUp() throws Exception {

        // JUnitTestSuite.properties is defined in Run->Run Configurations->JUnit->JUnitTestSuite, Arguments Tab:
        // -DJUnitTestSuite.properties=src/main/resources/JUnitTestSuite.properties
        // the properties file is src/main/resources/JUnitTestSuite.properties

        //configProperties = new JUnitTestProperties("JUnitTestSuite.properties");

        client = new NdexRestClient(JUnitTestSuite.testerName,
                                    JUnitTestSuite.testerPassword,
                                    JUnitTestSuite.testServerURL);

        ndex = new NdexRestClientModelAccessLayer(client);
        

        // get a list of "test" networks from the servers, i.e, networks with the same name as the one used for testing
        List<NetworkSummary> networkList = null;
        try {
        	networkList = ndex.findNetworks(JUnitTestSuite.networkToUploadName, true, JUnitTestSuite.testerName, 0, 300);
        } catch (Exception e) {
        	e.printStackTrace();
        	System.exit(0);
        }
        
        // delete the existing test networks on the server
        if ((networkList != null) && (networkList.size() > 0)) {
        	
    	    for (NetworkSummary networkSummary : networkList) {
    	    	String networkUUIDToDelete = networkSummary.getExternalId().toString();	  
    	        try {
    	        	ndex.deleteNetwork(networkUUIDToDelete);
    	        } catch (Exception e) {
    	        	// here we may get the "com.fasterxml.jackson.databind.JsonMappingException: No content to map due to end-of-input" exception;
    	        	// ignore it and keep deleting the networks
    	        }  	    	
    	    }
        }
    
        // upload network to the server for testing 
        try {
            ndex.uploadNetwork(JUnitTestSuite.networkToUpload);
        } catch (Exception e) {
        	System.out.println("Unable to upload test network " + JUnitTestSuite.networkToUpload + ";" + e.getMessage());
        	System.exit(0);
        }    

        networkList = null;
        
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        
        System.out.println(dateFormat.format(Calendar.getInstance().getTime()) + 
        		": started uploading network " +  JUnitTestSuite.networkToUploadName + " to " + JUnitTestSuite.testServerURL);
        
        // before we start testing, we need to wait for the test network upload to finish 
        while (true) {
	
            try {
            	// check if network has uploaded
            	networkList = ndex.findNetworks(JUnitTestSuite.networkToUploadName, true, JUnitTestSuite.testerName, 0, 300);
            } catch (Exception e) {
            	System.out.println("Unable to upload test network " + JUnitTestSuite.networkToUpload + ";" + e.getMessage());
            	System.exit(0);
            }
            
            if ((networkList == null) || (networkList.size() == 0)) {
            	// network not uploaded yet -- sleep 10 seconds and check again
            	Thread.sleep(10000); 
            } else {
            	// network uploaded; get its ID and break out of the loop
            	testNetworkUUID = networkList.get(0).getExternalId().toString();
            	break;
            }
            	
        }
        System.out.println(dateFormat.format(Calendar.getInstance().getTime()) + 
        		": finished uploading network " +  JUnitTestSuite.networkToUploadName + " to " + JUnitTestSuite.testServerURL);
    }
    */
    
}
