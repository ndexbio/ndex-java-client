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
package org.ndexbio.rest.test.performance;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FilenameUtils;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.ndexbio.model.object.NewUser;
import org.ndexbio.model.object.Task;
import org.ndexbio.model.object.User;
import org.ndexbio.model.object.network.Network;
import org.ndexbio.model.object.network.NetworkSummary;
import org.ndexbio.rest.client.NdexRestClient;
import org.ndexbio.rest.client.NdexRestClientModelAccessLayer;
import org.ndexbio.rest.test.utilities.JUnitTestSuiteProperties;
import org.ndexbio.rest.test.utilities.JettyServerUtils;
import org.ndexbio.rest.test.utilities.NetworkUtils;
import org.ndexbio.rest.test.utilities.PropertyFileUtils;
import org.ndexbio.rest.test.utilities.UserUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 *  This class contains JUNit tests for benchmarking the following NetworkAService.java APIs:
 *  
 *     1) public NetworkSummary createNetwork(Network newNetwork)
 *     2) public void uploadNetwork(UploadedFile uploadedNetwork)	  
 *     3) public Response getCompleteNetwork(String networkId)
 *     4) public String setNetworkFlag(String networkId, String parameter, String value)
 *     5) public Network queryNetwork(String networkId, SimplePathQuery queryParameters) 
 *  
 *  
 *  NetworkAService APIs from the NetworkAService.java class located in src/main/java/org.ndexbio.rest.services 
 *  package of ndexbio-rest module.    					
 */ 


//The @FixMethodOrder(MethodSorters.NAME_ASCENDING) annotation sorts (and
//executes) the test methods by name in lexicographic order
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class testPerformanceNetworkAPIs {
 
	static String networksToCreatePropertyFile = "src/test/resources/testPerformanceCreatingNetworks.properties";
	static TreeMap<String, String> testJSONNetworksToCreate;
	
	static String networksToUploadPropertyFile = "src/test/resources/testPerformanceUploadingNetworks.properties";	
	static TreeMap<String, String> testNetworksToUpload;

	static String networksToDownloadPropertyFile = "src/test/resources/testPerformanceDownloadingNetworks.properties";	
	static TreeMap<String, String> testNetworksToDownload;
	
	static String networksNeighborhoodQueryPropertyFile = "src/test/resources/testPerformanceNeighborhoodQuery.properties";	
	static TreeMap<String, String> testNeighorhoodQueryNetworksToUpload;	

    // URL of the test server
    private static String testServerURL = null;
	
    private static NdexRestClient                 client;
    private static NdexRestClientModelAccessLayer ndex;
    
    private static String accountName     = "uuu";
    private static String accountPassword = "uuu";
    
    private static User    testAccount    = null;
    private static NewUser testUser       = null;
    
	DecimalFormat df1 = new DecimalFormat("#,###");
	DecimalFormat df2 = new DecimalFormat("#,##0.000");	
	
	
	private static boolean generateExcelReport = true;
	
    private static Process jettyServer = null;
    
    private static Map<String, HashMap> clientDataForStatisticsReport  = new HashMap<String, HashMap>();
	

	/**
	 * This methods runs once before any of the test methods in the class.
	 * It builds Maps of networks for testing from the property files, creates a test user 
	 * account (accountName) with password (accountPassword),
	 * and ndex client used by other tests.
	 * 
     * @param   void
     * @return  void
     */
    @BeforeClass
    public static void setUp()  {
    	testServerURL = JUnitTestSuiteProperties.getTestServerURL();
    	
		// start Jetty server in a new instance of JVM
		jettyServer = JettyServerUtils.startJettyInNewJVM();

    	// build Map of networks in JSON format to be created on the Server via API
		testJSONNetworksToCreate = PropertyFileUtils.parsePropertyFile(networksToCreatePropertyFile);

    	// build Map of networks to be uploaded to the Server
		testNetworksToUpload = PropertyFileUtils.parsePropertyFile(networksToUploadPropertyFile);		
		
    	// build Map of networks to be uploaded to the Server and then downloaded
		testNetworksToDownload = PropertyFileUtils.parsePropertyFile(networksToDownloadPropertyFile);	

    	// build Map of networks to be uploaded to the Server and then run neighborhood queries against		
		testNeighorhoodQueryNetworksToUpload = 
				PropertyFileUtils.parsePropertyFile(networksNeighborhoodQueryPropertyFile);	
		
		// this is passed as argument to the script that parses ndex.log and creates a tect file wioth performance data
		clientDataForStatisticsReport = new HashMap();
	
		
		// create user object; the properties describe the current test
		testUser = UserUtils.getNewUser(
				accountName,
				accountPassword,
		        "This account is used for network APIs benchmark testing",  // description
		        "benchmark@ucsd.com",                 // email address
		        "Network",                            // first name -- name of the test
		        "Benchmark",                          // last name -- name of the test		        
		        "http://www.yahoo.com",               // image
		        "http://www.yahoo.com/finance");      // web-site

        try {
            client = new NdexRestClient(accountName, accountPassword, testServerURL);
        } catch (Exception e) {
			fail("Unable to create client: " + e.getMessage());
        }
        
        try {
            ndex = new NdexRestClientModelAccessLayer(client);
        } catch (Exception e) {
			fail("Unable to create ndex rest client model access layer: " + e.getMessage());
        } 
    }


    /** 
     * Clean-up method.  The last method called in this class by JUnit framework.
     * 
     * @param   void
     * @return  void
     */
    @AfterClass
    public static void tearDown() {
 
    	// stop the Jetty server, remove database; destroy Jetty Server process
        JettyServerUtils.shutdownServerRemoveDatabase();
    }

    
	/**
	 * This test reads networks in JSON format listed in the properties file (networksToCreatePropertyFile), 
	 * directly creates these networks on the server using createNetwork() API, calculates and prints 
	 * how long it took every network to be created.  It also saves these times in clientDataForStatisticsReport.
	 * The clientDataForStatisticsReport is then passed to the script that parses ndex.log
	 * and generates performance report for createNetwork().
     *
     * API tested/benchmarked : public NetworkSummary createNetwork(Network newNetwork)
	 * 
     * @param   void
     * @return  void
     */
    @Test  
    public void test0001BenchmarkNetworkCreate()  {
		Map<String, Map<String, String>> memoryBefore  = new HashMap<String, Map<String, String>>();
		Map<String, Map<String, String>> memoryAfter   = new HashMap<String, Map<String, String>>();
		Map<String, Map<String, String>> benchmarkData = new HashMap<String, Map<String, String>>();
		
		ArrayList <String>creationTime = new ArrayList<String>();

        for (Entry<String, String> entry : testJSONNetworksToCreate.entrySet()) {
            	    	
            // stop Jetty server if it is runs, remove database from file system, start Jetty server
        	// (i.e., (re)start server with clean database)
        	String responseFromServer = JettyServerUtils.sendCommand("restartServerWithCleanDatabase");
        	assertEquals("unable to restart Jetty Server: ", responseFromServer, "done");
        	
    		// re-create test account since it was deleted at previous step
    		testAccount = UserUtils.createUserAccount(ndex, testUser);

        	// absolute path name of the file containing network in JSON format
        	String absoluteNetworkPath = entry.getValue().toString();
        	String networkName = FilenameUtils.getName(absoluteNetworkPath);
        	
        	// construct Network object to be created on the server by createNetwork()
        	Network network = NetworkUtils.readNetworkFromFile(absoluteNetworkPath); 
        	
        	// get memory statistics before creating network
        	memoryBefore.put(entry.getKey(), getMemoryUtiliztaion());
        	
        	// create network on the server
            long timeBeforeCreate = System.currentTimeMillis();
            NetworkSummary networkSummary = NetworkUtils.createNetwork(ndex, network);        	
			long createTimeInMs = System.currentTimeMillis() - timeBeforeCreate;
            String formattedCreateTime = formatOutput(createTimeInMs);
        	
        	// get memory statistics after creating network
        	memoryAfter.put(entry.getKey(), getMemoryUtiliztaion());
        	
        	HashMap<String, String> benchmark = new HashMap<String, String>();
        	benchmark.put("name",   networkName);        	
        	benchmark.put("size",   NumberFormat.getNumberInstance(Locale.US).format(new File(absoluteNetworkPath).length()));
        	benchmark.put("nodes",  NumberFormat.getNumberInstance(Locale.US).format(network.getNodeCount()));
        	benchmark.put("edges",  NumberFormat.getNumberInstance(Locale.US).format(network.getEdgeCount()));
        	benchmark.put("upload", formattedCreateTime);
        	
            if (generateExcelReport) {
            	creationTime.add(df2.format(TimeUnit.MILLISECONDS.toSeconds(createTimeInMs) + (createTimeInMs % 1000)/1000.0));
            }

        	benchmarkData.put(entry.getKey(), benchmark);
        }
		
        
        printNetworkCreateReport(memoryBefore, memoryAfter, benchmarkData);
        
        if (generateExcelReport) {
        	
        	// save the time in the structure to be passed to the script that generated performance report
        	if (!clientDataForStatisticsReport.containsKey("NetworkAService.createNetwork")) {
        		HashMap<String, ArrayList<String>> h = new HashMap<String, ArrayList<String>>();
        		h.put("clientRTT", creationTime);
        		clientDataForStatisticsReport.put("NetworkAService.createNetwork", h);
        	} else {
        		HashMap<String, ArrayList<String>> h = clientDataForStatisticsReport.get("NetworkAService.createNetwork");
        		ArrayList <String>clientRTT = (ArrayList<String>) h.get("clientRTT");
        		clientRTT.addAll(creationTime);
        		h.put("clientRTT", clientRTT);
        		clientDataForStatisticsReport.put("NetworkAService.createNetwork", h);
        	}
        }
    }


	/**
	 * This test uploads networks listed in the properties file (networksToUploadPropertyFile) 
	 * to the server, calculates and prints how long it took every network to uploaded.  It also saves 
	 * these times in clientDataForStatisticsReport.
	 * The clientDataForStatisticsReport is then passed to the script that parses ndex.log
	 * and generates performance report for uploadNetwork().
     *
     * API tested/benchmarked : public void uploadNetwork(UploadedFile uploadedNetwork)
     * 
     * Important: uploadNetwork() server API creates a task that saves the file to the disk and 
     *            then persists it to the database.  Because uploadNetwork() doesn't return Task ID,
     *            we have to query for the status of this task with NetworkUtils.waitForTaskToFinish.
     *            Therefore, there should only be only process running on the server for the user;
     *            otherwise, NetworkUtils.waitForTaskToFinish() will fail.
	 * 
     * @param   void
     * @return  void
     */
	@Test  
    public void test0002BenchmarkNetworkUpload() {

		Map<String, Map<String, String>> benchmarkData = new HashMap<String, Map<String, String>>();

		ArrayList <String>creationTime = new ArrayList<String>();	
    	
        for (Entry<String, String> entry : testNetworksToUpload.entrySet()) {
            	    	
            // stop Jetty server if it is running, remove database from file system, start Jetty server
        	// (i.e., (re)start server with clean database)
        	String responseFromServer = JettyServerUtils.sendCommand("restartServerWithCleanDatabase");
        	assertEquals("unable to restart Jetty Server: ", responseFromServer, "done");
        	
    		// re-create test account since it was deleted at previous step
    		testAccount = UserUtils.createUserAccount(ndex, testUser);

        	// absolute path name of the file containing network in JSON format
        	String absoluteNetworkPath = entry.getValue().toString();
        	String networkName = FilenameUtils.getName(absoluteNetworkPath);
        	
        	//System.out.println("absoluteNetworkPath=" + absoluteNetworkPath + "   networkName to upload=" + networkName);
        	
        	File fileToUpload = new File(absoluteNetworkPath);
        	
        	
        	long clientUploadTimeStart = System.currentTimeMillis();
        	// upload network to the test account
        	NetworkUtils.startNetworkUpload(ndex, fileToUpload);
			long clientUploadTime = System.currentTimeMillis() - clientUploadTimeStart;
            String formattedClientUploadTime = formatOutput(clientUploadTime);
        	
            // wait for the network upload task to finish
        	Task task = NetworkUtils.waitForTaskToFinish(ndex, testAccount);        	
        	
            long serverUploadTime = task.getFinishTime().getTime() - task.getStartTime().getTime();
            String formattedServerUploadTime = formatOutput(serverUploadTime);
            
            //String networkUUID = task.getAttribute("networkUUID").toString();
            
        	
        	HashMap<String, String> benchmark = new HashMap<String, String>();
        	benchmark.put("name",     networkName); // or use task.getDescription()) to get network name     	
        	benchmark.put("size",     NumberFormat.getNumberInstance(Locale.US).format(fileToUpload.length()));	
        	//benchmark.put("nodes",    NumberFormat.getNumberInstance(Locale.US).format(entireNetwork.getNodeCount()));
        	//benchmark.put("edges",    NumberFormat.getNumberInstance(Locale.US).format(entireNetwork.getEdgeCount()));
        	benchmark.put("clientUploadTime",   formattedClientUploadTime);
        	benchmark.put("serverUploadTime",   formattedServerUploadTime);  	

        	benchmarkData.put(entry.getKey(), benchmark);
        		
        	
            if (generateExcelReport) {
            	creationTime.add(df2.format(TimeUnit.MILLISECONDS.toSeconds(clientUploadTime) + (clientUploadTime % 1000)/1000.0));
            }
        }

        printNetworkUploadReport(benchmarkData);

        if (generateExcelReport) {
        	
        	if (!clientDataForStatisticsReport.containsKey("NetworkAService.uploadNetwork")) {      		
        		HashMap<String, ArrayList<String>> h = new HashMap<String, ArrayList<String>>();
        		h.put("clientRTT", creationTime);
        		clientDataForStatisticsReport.put("NetworkAService.uploadNetwork", h);
        	} else {
        		HashMap <String, ArrayList<String>>h = clientDataForStatisticsReport.get("NetworkAService.uploadNetwork");
        		ArrayList <String>clientRTT = (ArrayList<String>)h.get("clientRTT");
        		clientRTT.addAll(creationTime);
        		h.put("clientRTT", clientRTT);
        		clientDataForStatisticsReport.put("NetworkAService.uploadNetwork", h);
        	}
        }
    }
	

	/**
	 * This test takes a network listed in the properties file (testNetworksToDownload), uploads it to the server and
	 * measures the upload time, then downloads it and measures the download time, then makes the network
	 * read-only and downloads and measures the read-only download time.  After that, it takes the next
	 * network from testNetworksToDownload and repeats the above steps. 
     *
     * The results of the test are saved in clientDataForStatisticsReport which is then passed to the script 
     * that parses ndex.log and generates performance report for getCompleteNetwork() and setNetworkFlag().
     *
     * APIs tested/benchmarked :  public Response getCompleteNetwork(String networkId)
     *                            public String setNetworkFlag(String networkId, String parameter, String value)
     * 
     * Important: uploadNetwork() server API creates a task that saves the file to the disk and 
     *            then persists it to the database.  Because uploadNetwork() doesn't return Task ID,
     *            we have to query for the status of this task with NetworkUtils.waitForTaskToFinish.
     *            Therefore, there should only be only process running on the server for the user;
     *            otherwise, NetworkUtils.waitForTaskToFinish() will fail.
	 * 
     * @param   void
     * @return  void
     */
	@Test  
    public void test0003BenchmarkNetworkDownloadWithReadonlyOnOff() {
		Map<String, Map<String, String>> memoryBefore  = new HashMap<String, Map<String, String>>();
		Map<String, Map<String, String>> memoryAfter   = new HashMap<String, Map<String, String>>();
		Map<String, Map<String, String>> benchmarkData = new HashMap<String, Map<String, String>>();
        
		ArrayList <String>clientUploadTime   = new ArrayList<String>();
		ArrayList <String>clientDownloadTime = new ArrayList<String>();
		ArrayList <String>networkFileName    = new ArrayList<String>();
		ArrayList <String>fileSize           = new ArrayList<String>();
		
		// same networks as for upload performance testing
        for (Entry<String, String> entry : testNetworksToDownload.entrySet()) {
            	    	
            // stop Jetty server if it is running, remove database from file system, start Jetty server
        	// (i.e., (re)start server with clean database)
        	String responseFromServer = JettyServerUtils.sendCommand("restartServerWithCleanDatabase");
        	assertEquals("unable to restart Jetty Server: ", responseFromServer, "done");

    		// re-create test account since it was deleted at previous step
    		testAccount = UserUtils.createUserAccount(ndex, testUser);

        	// absolute path name of the file containing network in JSON format
        	String absoluteNetworkPath = entry.getValue().toString();
        	String networkName = FilenameUtils.getName(absoluteNetworkPath);
        	
        	//System.out.println("absoluteNetworkPath=" + absoluteNetworkPath + "   networkName to upload=" + networkName);
        	
        	File fileToUpload = new File(absoluteNetworkPath);
        	
        	
        	// upload network to the test account
        	long clientUploadTimeStart = System.currentTimeMillis();
        	NetworkUtils.startNetworkUpload(ndex, fileToUpload);
			long clientUploadTimeInMs = System.currentTimeMillis() - clientUploadTimeStart;
        	Task task = NetworkUtils.waitForTaskToFinish(ndex, testAccount); 
        	String networkUUID = task.getAttribute("networkUUID").toString();  
            if (generateExcelReport) {
            	clientUploadTime.add(df2.format(
            			TimeUnit.MILLISECONDS.toSeconds(clientUploadTimeInMs) + (clientUploadTimeInMs % 1000)/1000.0));
            }
        	
        	// at this point, the network is uploaded to the server; let's restart the server and download this network
        	responseFromServer = JettyServerUtils.sendCommand("restartServerWithoutCleaningDatabase");
        	assertEquals("unable to start Jetty Server: ", responseFromServer, "done");

        	// get memory statistics before creating network
        	memoryBefore.put(entry.getKey(), getMemoryUtiliztaion());
        	

            long timeBeforeDownload = System.currentTimeMillis();
			Network network = NetworkUtils.getNetwork(ndex, networkUUID);
			long downloadTimeInMs = System.currentTimeMillis() - timeBeforeDownload;
            String formattedDownloadTime = formatOutput(downloadTimeInMs);
            if (generateExcelReport)  {
            	clientDownloadTime.add(df2.format(
         			   TimeUnit.MILLISECONDS.toSeconds(downloadTimeInMs) + (downloadTimeInMs % 1000)/1000.0));
            	networkFileName.add(networkName);
            	fileSize.add(NumberFormat.getNumberInstance(Locale.US).format(fileToUpload.length()));
            }
        	
            
		    // set target network to read-only mode, and download it again
            NetworkUtils.setReadOnlyFlag(ndex, networkUUID, true);
            timeBeforeDownload = System.currentTimeMillis();
			network = NetworkUtils.getNetwork(ndex, networkUUID);
			long downloadReadOnlyTimeInMs = System.currentTimeMillis() - timeBeforeDownload;
            String formattedDownloadReadOnlyTime = formatOutput(downloadReadOnlyTimeInMs);
            if (generateExcelReport)  {
            	clientDownloadTime.add(df2.format(
            			TimeUnit.MILLISECONDS.toSeconds(downloadReadOnlyTimeInMs) + (downloadReadOnlyTimeInMs % 1000)/1000.0));
            	networkFileName.add(networkName+"_Read-Only_");
            	fileSize.add(NumberFormat.getNumberInstance(Locale.US).format(fileToUpload.length()));
            }

        	// get memory statistics after creating network
        	memoryAfter.put(entry.getKey(), getMemoryUtiliztaion());
        	
		    // set target network to read-write mode, i.e., remove cache on the server to get
        	// performance of deleting the cache
            NetworkUtils.setReadOnlyFlag(ndex, networkUUID, false);        	
        	
            
        	HashMap<String, String> benchmark = new HashMap<String, String>();
        	benchmark.put("name",     networkName);
        	benchmark.put("size",     NumberFormat.getNumberInstance(Locale.US).format(fileToUpload.length()));	
        	benchmark.put("nodes",    NumberFormat.getNumberInstance(Locale.US).format(network.getNodeCount()));
        	benchmark.put("edges",    NumberFormat.getNumberInstance(Locale.US).format(network.getEdgeCount()));
        	benchmark.put("download", formattedDownloadTime); 
        	benchmark.put("readonly", formattedDownloadReadOnlyTime);        	

        	benchmarkData.put(entry.getKey(), benchmark);
        }

        printNetworkDownloadReport(memoryBefore, memoryAfter, benchmarkData);

        if (generateExcelReport) {
        	if (!clientDataForStatisticsReport.containsKey("NetworkAService.uploadNetwork")) {
        		
        		HashMap<String, ArrayList<String>> h = new HashMap<String, ArrayList<String>>();
        		h.put("clientRTT", clientUploadTime);
        		clientDataForStatisticsReport.put("NetworkAService.uploadNetwork", h);
        		
        	} else {
        		HashMap<String, ArrayList<String>> h = clientDataForStatisticsReport.get("NetworkAService.uploadNetwork");
        		ArrayList <String> clientRTT = (ArrayList<String>) h.get("clientRTT");
        		clientRTT.addAll(clientUploadTime);
        		h.put("clientRTT", clientRTT);
         		clientDataForStatisticsReport.put("NetworkAService.uploadNetwork", h);	
        	}
        	
        	if (!clientDataForStatisticsReport.containsKey("NetworkAService.getCompleteNetwork")) {
        		
        		HashMap<String,  ArrayList<String>> h = new HashMap<String, ArrayList<String>>();
        		h.put("clientRTT",           clientDownloadTime);
        		//h.put("clientReadOnlyRTT",   clientDownloadReadOnlyTime);
        		h.put("fileSize",            fileSize);
        		h.put("networkName",         networkFileName);
        		
        		clientDataForStatisticsReport.put("NetworkAService.getCompleteNetwork", h);
        		
        	} else {
        		HashMap<String, ArrayList<String>> h = clientDataForStatisticsReport.get("NetworkAService.getCompleteNetwork");
        		
        		ArrayList <String> clientRTT = (ArrayList<String>)h.get("clientRTT");
        		clientRTT.addAll(clientDownloadTime);
        		h.put("clientRTT", clientRTT);
        		
        		ArrayList <String> fs = (ArrayList<String>)h.get("fileSize");
        		fs.addAll(fileSize);
        		h.put("fileSize", fs);        		
        		
        		ArrayList <String> nn = (ArrayList<String>)h.get("networkName");
        		nn.addAll(networkFileName);
        		h.put("networkName", nn);        		
        		
         		clientDataForStatisticsReport.put("NetworkAService.getCompleteNetwork", h);	
        	}
        	
        	if (!clientDataForStatisticsReport.containsKey("NetworkAService.setNetworkFlag")) {
        		
        		HashMap<String, ArrayList<String>> h = new HashMap<String, ArrayList<String>>();
        		h.put("fileSize",    fileSize);
        		h.put("networkName", networkFileName);
        		
        		clientDataForStatisticsReport.put("NetworkAService.setNetworkFlag", h);
        		
        	} else {
        		HashMap<String, ArrayList<String>> h = clientDataForStatisticsReport.get("NetworkAService.setNetworkFlag");
        		
        		ArrayList<String> fs = (ArrayList<String>)h.get("fileSize");
        		fs.addAll(fileSize);
        		h.put("fileSize", fs);
        		
        		ArrayList <String> nn = (ArrayList<String>)h.get("networkName");
        		nn.addAll(networkFileName);
        		h.put("networkName", nn);         		
        		
         		clientDataForStatisticsReport.put("NetworkAService.setNetworkFlag", h);	
        	}
        }
    }
	

    
	/**
	 * This test takes a network, query and depth from the properties file (testNeighorhoodQueryNetworksToUpload), 
	 * uploads network to the server, and issues query with the specified depth (currently, 1 or 2). 
     *
     * The results of the test are saved in clientDataForStatisticsReport which is then passed to the script 
     * that parses ndex.log and generates performance report for getCompleteNetwork() and setNetworkFlag().
     *
     * APIs tested/benchmarked :  public Network queryNetwork(String networkId, SimplePathQuery queryParameters) 
     * 
     * Important: uploadNetwork() server API creates a task that saves the file to the disk and 
     *            then persists it to the database.  Because uploadNetwork() doesn't return Task ID,
     *            we have to query for the status of this task with NetworkUtils.waitForTaskToFinish.
     *            Therefore, there should only be only process running on the server for the user;
     *            otherwise, NetworkUtils.waitForTaskToFinish() will fail.
	 * 
     * @param   void
     * @return  void
     */
//	@Test  
 /*   public void test0004BenchmarkNeighborhoodQuery() throws JSONException {
		Map<String, Map<String, String>> memoryBefore  = new HashMap<String, Map<String, String>>();
		Map<String, Map<String, String>> memoryAfter   = new HashMap<String, Map<String, String>>();
		Map<String, Map<String, String>> benchmarkData = new HashMap<String, Map<String, String>>();
		
		String previousPath = "";
		String networkUUID = null;
		boolean uploadNetwork = true;
		File fileToUpload = null;
		
		ArrayList<String> networkFileName  = new ArrayList<String>();
		ArrayList<String> fileSize         = new ArrayList<String>();
		ArrayList<String> clientQueryTime  = new ArrayList<String>();
		ArrayList<String> nodes            = new ArrayList<String>();
		ArrayList<String> edges            = new ArrayList<String>();
		ArrayList<String> nodesRetrieved   = new ArrayList<String>();
		ArrayList<String> edgesRetrieved   = new ArrayList<String>();
		ArrayList <String>clientUploadTime = new ArrayList<String>();

		ArrayList<String> clientRTTArr     = new ArrayList<String>();
		
		
        for (Entry<String, String> entry : testNeighorhoodQueryNetworksToUpload.entrySet()) {

            String JSONString = entry.getValue().toString();
            JSONObject jsonObj =  new JSONObject(JSONString);

            String absoluteNetworkPath = jsonObj.getString("path");
            String query = jsonObj.getString("query");
            int depth = jsonObj.getInt("depth");
           
            uploadNetwork = absoluteNetworkPath.equals(previousPath) ? false : true;
            
            
            // stop Jetty server if it is running, remove database from file system, start Jetty server
        	// (i.e., (re)start server with clean database);  if we uploaded current netwrok at 
            // previous iteration, then do not remove it from the database
        	String responseFromServer = (uploadNetwork) ? 
        			  JettyServerUtils.sendCommand("restartServerWithCleanDatabase")
        	        : JettyServerUtils.sendCommand("restartServerWithoutCleaningDatabase");
        	assertEquals("unable to restart Jetty Server: ", responseFromServer, "done");
            
        	previousPath = absoluteNetworkPath;
        	
        	if (uploadNetwork) {
    		    // re-create test account since it was deleted at previous step
    		    testAccount = UserUtils.createUserAccount(ndex, testUser);

        	    fileToUpload = new File(absoluteNetworkPath);
        	
            	long clientUploadTimeStart = System.currentTimeMillis();
        	    // upload network to the test account
        	    NetworkUtils.startNetworkUpload(ndex, fileToUpload);
        	    long clientUploadTimeInMs = System.currentTimeMillis() - clientUploadTimeStart;
                if (generateExcelReport) {
                	clientUploadTime.add(df2.format(
                			TimeUnit.MILLISECONDS.toSeconds(clientUploadTimeInMs) + (clientUploadTimeInMs % 1000)/1000.0));
                }
        	    
        	    Task task = NetworkUtils.waitForTaskToFinish(ndex, testAccount); 
        	    networkUUID = task.getAttribute("networkUUID").toString();  

        	    // at this point, the network is uploaded to the server; let's restart the server and issue query
        	    responseFromServer = JettyServerUtils.sendCommand("restartServerWithoutCleaningDatabase");
        	    assertEquals("unable to start Jetty Server: ", responseFromServer, "done");
        	}

        	
        	
        	// get memory statistics before querying network neighborhood network
        	memoryBefore.put(entry.getKey(), getMemoryUtiliztaion());
            long timeBefore = System.currentTimeMillis();
            
        	Network subNetworkRetrieved = NetworkUtils.getNeighborhood(ndex, networkUUID, query, depth);
        	
			long queryTime = System.currentTimeMillis() - timeBefore;
            String formattedQueryTime = formatOutput(queryTime);		
        	memoryAfter.put(entry.getKey(), getMemoryUtiliztaion());
 
        	// get summary of the network we queried
        	NetworkSummary networkSummary = NetworkUtils.getNetworkSummaryById(ndex, networkUUID);
        	
        	String networkName = FilenameUtils.getName(absoluteNetworkPath);
        	
        	HashMap<String, String> benchmark = new HashMap<String, String>();
        	benchmark.put("name",     networkName);
        	benchmark.put("size",     NumberFormat.getNumberInstance(Locale.US).format(fileToUpload.length()));	
        	benchmark.put("nodes",    NumberFormat.getNumberInstance(Locale.US).format(networkSummary.getNodeCount()));
        	benchmark.put("edges",    NumberFormat.getNumberInstance(Locale.US).format(networkSummary.getEdgeCount()));    	
        	benchmark.put("query",    query);
        	benchmark.put("depth",    NumberFormat.getNumberInstance(Locale.US).format(depth));
        	benchmark.put("time",     formattedQueryTime);  	
        	benchmark.put("retrieved_nodes",    NumberFormat.getNumberInstance(Locale.US).format(subNetworkRetrieved.getNodeCount()));
        	benchmark.put("retrieved_edges",    NumberFormat.getNumberInstance(Locale.US).format(subNetworkRetrieved.getEdgeCount()));

        	benchmarkData.put(entry.getKey(), benchmark);   


            if (generateExcelReport) {
            	networkFileName.add(networkName);
            	fileSize.add(NumberFormat.getNumberInstance(Locale.US).format(fileToUpload.length()));
            	clientQueryTime.add(df2.format(
             			   TimeUnit.MILLISECONDS.toSeconds(queryTime) + (queryTime % 1000)/1000.0));
            	nodes.add(NumberFormat.getNumberInstance(Locale.US).format(networkSummary.getNodeCount()));
            	edges.add(NumberFormat.getNumberInstance(Locale.US).format(networkSummary.getEdgeCount()));         	    	
            	nodesRetrieved.add(
            			NumberFormat.getNumberInstance(Locale.US).format(subNetworkRetrieved.getNodeCount()));
            	edgesRetrieved.add(
            			NumberFormat.getNumberInstance(Locale.US).format(subNetworkRetrieved.getEdgeCount()));
         	    
         	    clientRTTArr.add(df2.format(
         	    		TimeUnit.MILLISECONDS.toSeconds(queryTime) + (queryTime % 1000)/1000.0));
            }
        }

        printNetworkNeighborhoodQueryReport(memoryBefore, memoryAfter, benchmarkData);
        
        
        if (generateExcelReport) {
        	
    	    if (!clientDataForStatisticsReport.containsKey("NetworkAService.queryNetwork")) {
    		
    		    HashMap<String, ArrayList<String>> h = new HashMap<String, ArrayList<String>>();    
 
    		    h.put("clientRTT",      clientRTTArr);
    		    h.put("fileSize",       fileSize);
    		    h.put("networkName",    networkFileName);
    		    h.put("nodes",          nodes);
    		    h.put("edges",          edges);
    		    h.put("nodesRetrieved", nodesRetrieved);
    		    h.put("edgesRetrieved", edgesRetrieved);
    		    
    		    clientDataForStatisticsReport.put("NetworkAService.queryNetwork", h);
    		
    	    } else {
    		    HashMap<String, ArrayList<String>> h = clientDataForStatisticsReport.get("NetworkAService.queryNetwork");
        		
    		    ArrayList<String> clientRTT = (ArrayList<String>)h.get("clientRTT");
    		    clientRTT.addAll(clientRTTArr);
        		h.put("clientRTT", clientRTT);
        		
        		ArrayList<String> fs = (ArrayList<String>)h.get("fileSize");
        		fs.addAll(fileSize);
        		h.put("fileSize", fs);
        		
        		ArrayList <String> nn = (ArrayList<String>)h.get("networkName");
        		nn.addAll(networkFileName);
        		h.put("networkName", nn);     		    

        		ArrayList <String> n = (ArrayList<String>)h.get("nodes");
        		n.addAll(nodes);
        		h.put("nodes", n); 
        		
        		ArrayList <String> e = (ArrayList<String>)h.get("edges");
        		e.addAll(edges);
        		h.put("edges", e); 
        		
        		ArrayList <String> nr = (ArrayList<String>)h.get("nodesRetrieved");
        		nr.addAll(nodesRetrieved);
        		h.put("nodesRetrieved", nr); 
        		
        		ArrayList <String> er = (ArrayList<String>)h.get("edgesRetrieved");
        		er.addAll(edgesRetrieved);
        		h.put("edgesRetrieved", er);
    		    
     		    clientDataForStatisticsReport.put("NetworkAService.queryNetwork", h);	
    	    }
    	    
        	if (!clientDataForStatisticsReport.containsKey("NetworkAService.uploadNetwork")) {
        		
        		HashMap<String, ArrayList<String>> h = new HashMap<String, ArrayList<String>>();
        		h.put("clientRTT", clientUploadTime);
        		clientDataForStatisticsReport.put("NetworkAService.uploadNetwork", h);
        		
        	} else {
        		HashMap<String, ArrayList<String>> h = clientDataForStatisticsReport.get("NetworkAService.uploadNetwork");
        		ArrayList <String> clientRTT = (ArrayList<String>) h.get("clientRTT");
        		clientRTT.addAll(clientUploadTime);
        		h.put("clientRTT", clientRTT);
         		clientDataForStatisticsReport.put("NetworkAService.uploadNetwork", h);	
        	}
        }
    } */
   
	/**
	 * This test just calls the script that parses ndex.log and generates 
	 * performance report. 
     *
     * @param   void
     * @return  void
	 * @throws IOException 
     */
    @Test
	public void test9999GenerateExcelReport() throws IOException {

    	ObjectMapper objectMapper = new ObjectMapper();
    	JsonNode args = objectMapper.valueToTree(clientDataForStatisticsReport);
 			
    	//System.out.println("args=" + args);
    	//for (String key: clientDataForStatisticsReport.keySet()) {
    	//	System.out.println(key + "=" + clientDataForStatisticsReport.get(key));
    	//}

		Process proc = null;
		try {
			// call python script that parses ndex.log and generates a text, tab-separated file with
			// performance statistics
			proc = Runtime.getRuntime().exec("python src/test/java/org/ndexbio/rest/test/utilities/mine.py -i logs/ndex.log " + args);
		} catch (IOException e) {
			fail("unable to start/run ndex.log parsing script: " + e.getMessage());
		}
		
		/*
		/* uncomment this to get output from running "python src/test/java/org/ndexbio/rest/test/utilities/mine.py -i logs/ndex.log " 
		 */ 
		BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
		BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

		// read the output from running the python script command
		String s = null;
		while ((s = stdInput.readLine()) != null) { System.out.println(s); }

		// read any errors from the attempted command, if any
		while ((s = stdError.readLine()) != null) { System.out.println(s); }
		/**/

		// wait for the python scrript process to finish
		try {
			proc.waitFor();
		} catch (InterruptedException e) { e.printStackTrace(); } 
	}	

	private void printNetworkNeighborhoodQueryReport(
			Map<String, Map<String, String>> memoryBefore,
			Map<String, Map<String, String>> memoryAfter,
			Map<String, Map<String, String>> benchmarkData) {
		
        for (Entry<String, String> entry : testNeighorhoodQueryNetworksToUpload.entrySet()) {
        	String key = entry.getKey();
        	
            printRuntimeMemoryUsage("\n--- Memory before running test0004BenchmarkNeighborhoodQuery, Bytes ---", memoryBefore.get(key));
            
            System.out.println(
                    benchmarkData.get(key).get("name")                + "\t" + 
                    "size: "   +  benchmarkData.get(key).get("size")  + "\t" + 
                    "nodes: "  +  benchmarkData.get(key).get("nodes") + "\t" + 
                    "edges: "  +  benchmarkData.get(key).get("edges") + "\t" + 
                    "query: "  +  benchmarkData.get(key).get("query") + "\t" + 
                    "depth: "  +  benchmarkData.get(key).get("depth") + "\t" + 
                    "time: "   +  benchmarkData.get(key).get("time")  + "\t" + 
                    "retrieved: nodes: "  +  benchmarkData.get(key).get("retrieved_nodes") + "\t" + 
                    "edges: "  +  benchmarkData.get(key).get("retrieved_edges") );
 
        	printRuntimeMemoryUsage("--- Memory after  running test0004BenchmarkNeighborhoodQuery, Bytes ---", memoryAfter.get(key));
        }
		
	}


	private void printNetworkCreateReport(
			Map<String, Map<String, String>> memoryBefore,
			Map<String, Map<String, String>> memoryAfter,
			Map<String, Map<String, String>> benchmarkData) {
		
        for (Entry<String, String> entry : testJSONNetworksToCreate.entrySet()) {
        	String key = entry.getKey();
        	
            printRuntimeMemoryUsage("\n--- Memory before running test0001BenchmarkNetworkCreate, Bytes ---", memoryBefore.get(key));
            
            System.out.println(
                benchmarkData.get(key).get("name") + "\t" + 
                "size: "   +  benchmarkData.get(key).get("size") + "\t" + 		
                "nodes: " + benchmarkData.get(key).get("nodes") + "\t" + 
                "edges: " +  benchmarkData.get(key).get("edges") + "\t" + 
                "creation time: " + benchmarkData.get(key).get("upload") + "\t" );
 
        	printRuntimeMemoryUsage("--- Memory after  running test0001BenchmarkNetworkCreate, Bytes ---", memoryAfter.get(key));
        }
	}	
	
	private void printNetworkUploadReport(
			Map<String, Map<String, String>> benchmarkData) {
		
		System.out.println("\n\n--- Statistics for running  test0002BenchmarkNetworkUpload ---");
		
        for (Entry<String, String> entry : testNetworksToUpload.entrySet()) {
        	String key = entry.getKey();
            
            System.out.println(
                benchmarkData.get(key).get("name") + "\t" + 
                "size: "   +  benchmarkData.get(key).get("size") + "\t" + 		        	
                "client upload time: " + benchmarkData.get(key).get("clientUploadTime") + "\t" +
                "server upload task time: " + benchmarkData.get(key).get("serverUploadTime") + "\t" );
        }
	}

	private void printNetworkDownloadReport(
			Map<String, Map<String, String>> memoryBefore,
			Map<String, Map<String, String>> memoryAfter,
			Map<String, Map<String, String>> benchmarkData) {
		
        for (Entry<String, String> entry : testJSONNetworksToCreate.entrySet()) {
        	String key = entry.getKey();
        	
            printRuntimeMemoryUsage("\n--- Memory before running test0003BenchmarkNetworkDownloadWithReadonlyOnOff, Bytes ---", memoryBefore.get(key));
            
            System.out.println(
                    benchmarkData.get(key).get("name") + "\t" + 
                    "size: "   +  benchmarkData.get(key).get("size") + "\t" + 
                    "nodes: " + benchmarkData.get(key).get("nodes") + "\t" + 
                    "edges: " +  benchmarkData.get(key).get("edges") + "\t" + 
                    "download time: " + benchmarkData.get(key).get("download") + "\t" + 
                    "download read-only time: " + benchmarkData.get(key).get("readonly") );
 
        	printRuntimeMemoryUsage("--- Memory after  running test0003BenchmarkNetworkDownloadWithReadonlyOnOff, Bytes ---", memoryAfter.get(key));
        }
	}
	
	private Map<String, String> getMemoryUtiliztaion() {
		
		Map<String, String> memory = new HashMap<String,String>();

	    Runtime runtime = Runtime.getRuntime();

		memory.put("heap", df1.format(runtime.totalMemory()));
		memory.put("max",  df1.format(runtime.maxMemory()));
		memory.put("used", df1.format(runtime.totalMemory() - runtime.freeMemory()));
		memory.put("free", df1.format(runtime.freeMemory()));
		
		return memory;
	}
	
	private void printRuntimeMemoryUsage(String header, Map<String, String> memory) {

	    System.out.println(header);

	    System.out.println("   Heap Size (Total Memory): " + memory.get("heap"));
	    System.out.println("                 Max Memory: " + memory.get("max"));	    
	    System.out.println("                Used Memory: " + memory.get("used"));
	    System.out.println("                Free Memory: " + memory.get("free"));
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
