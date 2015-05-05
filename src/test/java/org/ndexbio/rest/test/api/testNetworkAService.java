package org.ndexbio.rest.test.api;

import static org.junit.Assert.*;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;

/*
 * This class contains JUNit tests for UserService.java APIs located in
 * src/main/java/org.ndexbio.rest.services package of ndexbio-rest module.
 */
import org.junit.runners.MethodSorters;
import org.ndexbio.model.exceptions.NdexException;

import org.ndexbio.model.object.network.Network;
import org.ndexbio.model.object.network.NetworkSummary;
import org.ndexbio.rest.client.NdexRestClient;
import org.ndexbio.rest.client.NdexRestClientModelAccessLayer;
import org.ndexbio.rest.test.utilities.DownloadNetwork;
import org.ndexbio.rest.test.utilities.UpdateNetworkProfile;


// The @FixMethodOrder(MethodSorters.NAME_ASCENDING) annotation sorts (and
// executes) the test methods by name in lexicographic order
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class testNetworkAService {

    private static NdexRestClient                 client;
    private static NdexRestClientModelAccessLayer ndex;
    
	private static String  testNetworkUUID = null;
	
	private static final int NUM_OF_THREADS_TO_CREATE = 5;
    
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
        
/*
        NewUser testUser = new NewUser();
        User    createdTestUser;
        testUser.setAccountName("ttt");
        testUser.setPassword("ttt");
        
        //testUser.setAccountName(JUnitTestSuite.userName);
        testUser.setDescription("This user is used by JUnit tests");
        testUser.setEmailAddress("tester1@ttt.com");
        testUser.setFirstName("FirstName");
        testUser.setImage("http://www.yahoo.com");
        testUser.setLastName("LastName");
        //testUser.setPassword(JUnitTestSuite.password);
        testUser.setWebsite("http://www.yahoo.com/finance");
        
        
        try {
        	NdexRestClient nrc = new NdexRestClient("ttt", "ttt", JUnitTestSuite.testServerURL);
        	NdexRestClientModelAccessLayer nrcma = new NdexRestClientModelAccessLayer(nrc);
        	//ndc.
        	createdTestUser = nrcma.createUser(testUser);
        } catch (Exception e) {
        	e.printStackTrace();
        	System.exit(0);
        }
*/


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

    @AfterClass
    public static void tearDown() throws Exception {
	
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

    	// download test network from the server
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

        assertEquals("Node count in originalNetwork not same as in network", 
        		originalNetwork.getNodeCount(), network.getNodeCount());

        assertEquals("Node count in originalNetwork not same as in network",
        		originalNetwork.getNodes().size(), network.getNodes().size());

        assertEquals("Edge count in originalNetwork not same as in network",
        		originalNetwork.getEdgeCount(), network.getEdgeCount());

        assertEquals("Edge count in originalNetwork not same as in network",
        		originalNetwork.getEdges().size(), network.getEdges().size());       

        assertEquals("Base terms count in originalNetwork not same as in network",
        		originalNetwork.getBaseTerms().size(), network.getBaseTerms().size());

        assertEquals("Name spaces count in originalNetwork not same as in network",
        		originalNetwork.getNamespaces().size(), network.getNamespaces().size());

        assertEquals("Citations count in originalNetwork not same as in network",
        		originalNetwork.getCitations().size(), network.getCitations().size());
        
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
   
}
