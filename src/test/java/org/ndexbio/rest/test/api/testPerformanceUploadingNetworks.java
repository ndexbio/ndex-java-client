package org.ndexbio.rest.test.api;

import static org.junit.Assert.fail;


import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ndexbio.model.object.NewUser;
import org.ndexbio.model.object.Task;
import org.ndexbio.model.object.User;
import org.ndexbio.rest.client.NdexRestClient;
import org.ndexbio.rest.client.NdexRestClientModelAccessLayer;
import org.ndexbio.rest.test.utilities.NetworkUtils;
import org.ndexbio.rest.test.utilities.PropertyFileUtils;
import org.ndexbio.rest.test.utilities.UserUtils;

public class testPerformanceUploadingNetworks {
    
	static String networksFile = "src/test/resources/testPerformanceUploadingNetworks.properties";
	static Map<String, String> testNetworks;
	
    private static NdexRestClient                 client;
    private static NdexRestClientModelAccessLayer ndex;
    
    private static String accountName     = "uuu";
    private static String accountPassword = "uuu";
    
    private static User   testAccount     = null;
	
    
	/**
	 * This methods uploads networks listed in the properties file (networksFile), 
	 * calculates and prints how long it took for every network to upload. 
	 * 
     * @param   void
     * @return  void
     */
	@Test
    public void test0001BechmarkNetworkUpload() {
		
		// start uploading networks from testNetworks
	    // NetworkUtils.startNetworksUpload returns map with the entries in the form  "file_name":"file_size_in_Bytes"
		Map<String,String> uploadedNetworks = NetworkUtils.startNetworksUpload(ndex, testNetworks);

		// wait for all networks to upload
        List<Task> userTasks = NetworkUtils.waitForNetworksToUpload(ndex, testAccount);
        
        // all networks uploaded; print the statistics
        for (Task task : userTasks) {
        	
            long uploadTimeInMs = task.getFinishTime().getTime() - task.getStartTime().getTime();
            String formattedUploadTime = formatOutput(uploadTimeInMs);

            /*
            // it would be nice to know IDs of networks that we uploaded in order to get their node/edge count
            // but at present we cannot do this yet
    		try {
    			// note: we are searching by filename without extension; this may report wrong number of
    			// edges/nodes in case there are multiple networks with the same name.
                // For this test we use different network names, so it is ok
    			networkSummary =
    			    ndex.findNetworks(FilenameUtils.removeExtension(task.getDescription()), true, accountName, Permissions.ADMIN, false, 0, 1);
    		} catch (IOException e) {
    			networkSummary = null;
    		}            
            
    		if ((null == networkSummary) ||(0  == networkSummary.size())) {
    			edges = "unknown";
    			nodes = "unknown";
    			id    = UUID.randomUUID();
    		} else {
    			edges = String.valueOf(networkSummary.get(0).getEdgeCount());
    			nodes = String.valueOf(networkSummary.get(0).getNodeCount());
    			id    = networkSummary.get(0).getExternalId();
    		}
            */
            System.out.println(
                task.getDescription() + "\t\t" + 
                "size: " + uploadedNetworks.get(task.getDescription()) + "\t\t" + 
            	//"nodes: " + nodes + "\t" +
                //"edges: " + edges + "\t" +
                "duration: " + formattedUploadTime);

        }
    }

	/**
	 * This methods runs once before any of the test methods in the class.
	 * It builds a Map of networks for testing from the property file, creates a test user 
	 * account (accountName) with password (accountPassword),
	 * and ndex client used by other tests.
	 * 
     * @param   void
     * @return  void
     */
    @BeforeClass
    public static void setUp() throws Exception {
		
    	// build Map of networks for testing from the property file
		testNetworks = PropertyFileUtils.parsePropertyFile(networksFile);
		
		// create user object; the properties describe the current test
		NewUser testUser = UserUtils.getNewUser(
				accountName,
				accountPassword,
		        "This account is used for network uploading benchmark testing",  // description
		        "benchmark@ucsd.com",                 // email address
		        "Upload",                             // first name -- name of the test
		        "Network Benchmark",                  // last name -- name of the test		        
		        "http://www.yahoo.com",               // image
		        "http://www.yahoo.com/finance");      // web-site

		// create ndex client and a test user account
        try {
            client = new NdexRestClient(accountName, accountPassword, JUnitTestSuite.testServerURL);
            ndex   = new NdexRestClientModelAccessLayer(client);
            testAccount = ndex.createUser(testUser);
        } catch (Exception e) {
			fail("Unable to create test user account: " + e.getMessage());
        }
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
    	
    	// delete all networks from the test account
    	NetworkUtils.deleteNetworks(ndex, accountName, testNetworks);
    	
    	// delete the test user account
    	UserUtils.deleteUser(ndex);
    }
    
    /**
     * This method takes as an argument a long value representing milliseconds, and
     * converts it to the formatted string of the form "HHh:MMm:SSs:MMMms".
     * 
     * Example:  
     *     
     *     String timeInterval = formatOutput(18774345);
     *     // the value of timeInterval is  "05h:12m:54s:345ms"
     * 
     * @param    millisecondsToConvert milliseconds to be converted into formatted string
     * @return   string in the format "HHh:MMm:SSs:MMMms" (for example, "01h:12m:41s:574ms")
     */
	private static String formatOutput (long millisecondsToConvert) {
		
        long milliseconds = millisecondsToConvert % 1000;
        long seconds      = (millisecondsToConvert / 1000) % 60;
        long minutes      = ( (millisecondsToConvert / 1000) / 60 ) % 60;
        long hours        = ( ( (millisecondsToConvert / 1000) / 60 ) / 60 ) % 60;
        
        return String.format("%02dh:%02dm:%02ds:%03dms", hours, minutes, seconds, milliseconds);
	}
    
}
