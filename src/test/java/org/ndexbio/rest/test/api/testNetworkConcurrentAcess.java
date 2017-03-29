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
package org.ndexbio.rest.test.api;

import static org.junit.Assert.*;

import java.io.File;

import java.util.TreeMap;

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
import org.ndexbio.model.object.NewUser;
import org.ndexbio.model.object.Task;
import org.ndexbio.model.object.User;
import org.ndexbio.model.object.network.Network;
import org.ndexbio.model.object.network.NetworkSummary;
import org.ndexbio.rest.client.NdexRestClient;
import org.ndexbio.rest.client.NdexRestClientModelAccessLayer;
import org.ndexbio.rest.test.utilities.DownloadNetwork;
import org.ndexbio.rest.test.utilities.JUnitTestSuiteProperties;
import org.ndexbio.rest.test.utilities.JettyServerUtils;
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
	
    // URL of the test server
    private static String testServerURL = null;
    
	static File fileToUpload = null;
	
    private static NdexRestClient                 client;
    private static NdexRestClientModelAccessLayer ndex;
 
    private static String accountName     = "aaa";
    private static String accountPassword = "aaa";	

    private static User    testAccount    = null;
    private static NewUser testUser       = null;

	private static String  testNetworkUUID = null;
	
	private static final int NUM_OF_THREADS_TO_CREATE = 5;
	
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
            client = new NdexRestClient(accountName, accountPassword, testServerURL);
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
	 * This methods runs before every test case.
	 * It restarts the server with clean database (removes the database), and uploads test network to the server.
	 * 
     * @param   void
     * @return  void
     */
    @Before public void loadNetwork() {
        // stop Jetty server if it is running, remove database from file system, start Jetty server
    	// (i.e., (re)start server with clean database)
    	String responseFromServer = JettyServerUtils.sendCommand("restartServerWithCleanDatabase");
    	assertEquals("unable to restart Jetty Server: ", responseFromServer, "done");
    	
		// create test account
		testAccount = UserUtils.createUserAccount(ndex, testUser);
    	
    	// start uploading network to the test account
    	NetworkUtils.startNetworkUpload(ndex, fileToUpload);
    	
    	// wait till network is uploaded
    	Task task = NetworkUtils.waitForTaskToFinish(ndex, testAccount);
    	
    	// get UUID of the network we just uploaded
    	testNetworkUUID = task.getAttribute("networkUUID").toString();
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
		Network originalNetwork = NetworkUtils.getNetwork(ndex, testNetworkUUID);
    	
    		
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
        NetworkSummary updatedNetworkSummary = NetworkUtils.getNetworkSummaryById(ndex, testNetworkUUID);
    	
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
    
    	updatedNetworkSummary = NetworkUtils.getNetworkSummaryById(ndex, testNetworkUUID);
    	
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
    	Network originalNetwork = NetworkUtils.getNetwork(ndex, testNetworkUUID);
 	
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
    	Network network = NetworkUtils.getNetwork(ndex, testNetworkUUID);

    	// modify network profile 
    	network.setName("Modified -- " + network.getName());
    	network.setDescription("Modified -- " + network.getDescription());
    	network.setVersion("Modified -- " + network.getVersion());   	
    	
    	// send the modified network back to the server (update it on the server)
    	NetworkUtils.updateNetwork(ndex, network);

    	// check if the network summary updated correctly
  //  	assertEquals("Failed to update network name",        network.getName(),        updatedNetworkSummary.getName());
  //  	assertEquals("Failed to update network description", network.getDescription(), updatedNetworkSummary.getDescription());
  //  	assertEquals("Failed to update network version",     network.getVersion(),     updatedNetworkSummary.getVersion());
    }       
}
