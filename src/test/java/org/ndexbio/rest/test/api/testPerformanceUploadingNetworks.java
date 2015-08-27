/**
 * Copyright (c) 2013, 2015, The Regents of the University of California, The Cytoscape Consortium
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

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.eclipse.jetty.server.Server;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.ndexbio.model.exceptions.NdexException;
import org.ndexbio.model.object.NewUser;
import org.ndexbio.model.object.Task;
import org.ndexbio.model.object.User;
import org.ndexbio.model.object.network.Network;
import org.ndexbio.model.object.network.NetworkSummary;
import org.ndexbio.rest.client.NdexRestClient;
import org.ndexbio.rest.client.NdexRestClientModelAccessLayer;
import org.ndexbio.rest.test.utilities.FileAndServerUtils;
import org.ndexbio.rest.test.utilities.NetworkUtils;
import org.ndexbio.rest.test.utilities.PropertyFileUtils;
import org.ndexbio.rest.test.utilities.UserUtils;
import org.ndexbio.task.Configuration;

//The @FixMethodOrder(MethodSorters.NAME_ASCENDING) annotation sorts (and
//executes) the test methods by name in lexicographic order
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class testPerformanceUploadingNetworks {
    
	static String resourcePath = "src/test/resources/";
	static String networksFile = resourcePath + "testPerformanceUploadingNetworks.properties";
	static TreeMap<String, String> testNetworks;
	
    private static NdexRestClient                 client;
    private static NdexRestClientModelAccessLayer ndex;
    
    private static String accountName     = "uuu";
    private static String accountPassword = "uuu";
    
    private static User    testAccount    = null;
    private static NewUser testUser       = null;
    
	DecimalFormat df = new DecimalFormat("#,###");
	
	private static boolean overwriteExistingNetwork = true;
	private static String fileNameExtension = ".json";

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
		testUser = UserUtils.getNewUser(
				accountName,
				accountPassword,
		        "This account is used for network uploading benchmark testing",  // description
		        "benchmark@ucsd.com",                 // email address
		        "Upload",                             // first name -- name of the test
		        "Network Benchmark",                  // last name -- name of the test		        
		        "http://www.yahoo.com",               // image
		        "http://www.yahoo.com/finance");      // web-site

        try {
            client = new NdexRestClient(accountName, accountPassword, JUnitTestSuite.testServerURL);
        } catch (Exception e) {
			fail("Unable to create client: " + e.getMessage());
        }
        
        try {
            ndex = new NdexRestClientModelAccessLayer(client);
        } catch (Exception e) {
			fail("Unable to create ndex rest client model access layer: " + e.getMessage());
        }
        
        testAccount = UserUtils.createUserAccount(ndex, testUser);
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
	 * This methods uploads networks listed in the properties file (networksToUpload), 
	 * calculates and prints how long it took for every network to upload. 
	 * 
     * @param   void
     * @return  void
     */
	@Test
    public void test0001BenchmarkNetworkUploadAndDownload() {
		Map<String, Map<String, String>> memoryBefore  = new HashMap<String, Map<String, String>>();
		Map<String, Map<String, String>> memoryAfter   = new HashMap<String, Map<String, String>>();
		Map<String, Map<String, String>> benchmarkData = new HashMap<String, Map<String, String>>();
  
        Map<String,String> uploadedNetworks = new HashMap<String, String>();
        
        for (Entry<String, String> entry : testNetworks.entrySet()) {
            	
        	// shut down the server and database, and remove the database files from the filesystem
    		FileAndServerUtils.cleanDatabase();
    		
    		// re-create test account since it was deleted at previous step by cleanDatabase()
    		testAccount = UserUtils.createUserAccount(ndex, testUser);
        	
        	String networkPath = entry.getValue().toString();
        	//System.out.println("networkPath="+networkPath);
        	File fileToUpload = new File(networkPath);
        	
        	// get memory statistics before running the benchmark
        	memoryBefore.put(entry.getKey(), getMemoryUtiliztaion());

        	// upload network to the test account
        	NetworkUtils.startNetworkUpload(ndex, fileToUpload, uploadedNetworks);
        	Task task = NetworkUtils.waitForTaskToFinish(ndex, testAccount);
        	
            long uploadTimeInMs = task.getFinishTime().getTime() - task.getStartTime().getTime();
            String formattedUploadTime = formatOutput(uploadTimeInMs);
            
            String networkUUID = task.getAttribute("networkUUID").toString(); 
            
		    	    
		    // download network with ReadOnly flag set to false
			try {
		        // set target network to read-write mode
				ndex.setNetworkFlag(networkUUID, "readOnly", "false");
			} catch (Exception e) {
				fail("can't set read-only flag to false");
			}
			

			Network entireNetwork = null;

            long timeBeforeDownload = System.currentTimeMillis();

			try {
				 entireNetwork = ndex.getNetwork(networkUUID);
			} catch (IOException | NdexException e) {
				entireNetwork = null;
				fail("can't download entire network");				
			}
			
			long downloadTimeInMs = System.currentTimeMillis() - timeBeforeDownload;
            String formattedDownloadTime = formatOutput(downloadTimeInMs);
		   

            entireNetwork = null;
            
		    // download network with ReadOnly flag set to true
			try {
		        // set target network to read-only mode
				NetworkUtils.setReadOnlyFlag(ndex, networkUUID, true);
			} catch (Exception e) {
				fail("can't set read-only flag to false");
			}

            timeBeforeDownload = System.currentTimeMillis();

			try {
				entireNetwork = ndex.getNetwork(networkUUID);
			} catch (IOException | NdexException e) {
				entireNetwork = null;
				fail("can't download entire network");				
			}
			
			long downloadTimeReadOnlyInMs = System.currentTimeMillis() - timeBeforeDownload;
            String formattedDownloadReadOnlyTime = formatOutput(downloadTimeReadOnlyInMs);
            
        	// get memory statistics after running the benchmark
        	memoryAfter.put(entry.getKey(), getMemoryUtiliztaion());

        	HashMap<String, String> benchmark = new HashMap<String, String>();
        	benchmark.put("name",     task.getDescription());        	
        	benchmark.put("size",     uploadedNetworks.get(task.getDescription()));
        	benchmark.put("nodes",    NumberFormat.getNumberInstance(Locale.US).format(entireNetwork.getNodeCount()));
        	benchmark.put("edges",    NumberFormat.getNumberInstance(Locale.US).format(entireNetwork.getEdgeCount()));
        	benchmark.put("upload",   formattedUploadTime);
        	benchmark.put("download", formattedDownloadTime); 
        	benchmark.put("readonly", formattedDownloadReadOnlyTime);         	

        	benchmarkData.put(entry.getKey(), benchmark);
        	
			try {
				NetworkUtils.setReadOnlyFlag(ndex, networkUUID, false);
			} catch (Exception e) {
				fail("can't set read-only flag to false");
			}
			
			// save the network to the file system so that we could re-use it for the next benchmark
			NetworkUtils.saveNetworkToFile(resourcePath+fileToUpload.getName()+fileNameExtension, entireNetwork, overwriteExistingNetwork);
        }
		
        printNetworkUploadAndDownloadReport(memoryBefore, memoryAfter, benchmarkData);
        memoryBefore  = null;
        memoryAfter   = null;
        benchmarkData = null;
    }
	
	/**
	 * This methods uploads networks listed in the properties file (networksToUpload), 
	 * calculates and prints how long it took for every network to upload. 
	 * 
     * @param   void
     * @return  void
     */
	@Test
    public void test0002BenchmarkNetworkCreate() {
		Map<String, Map<String, String>> memoryBefore  = new HashMap<String, Map<String, String>>();
		Map<String, Map<String, String>> memoryAfter   = new HashMap<String, Map<String, String>>();
		Map<String, Map<String, String>> benchmarkData = new HashMap<String, Map<String, String>>();
  
         
        for (Entry<String, String> entry : testNetworks.entrySet()) {
            	
        	// shut down the server and database, and remove the database files from the filesystem
    		FileAndServerUtils.cleanDatabase();
    		
    		// re-create test account since it was deleted at previous step by cleanDatabase()
    		testAccount = UserUtils.createUserAccount(ndex, testUser);
        	
        	String networkPath = entry.getValue().toString();
        	String networkName = FilenameUtils.getName(networkPath);
        	
        	// name of the file containing network in JSON format; this file is created by previous test
        	String fileName = resourcePath + networkName + fileNameExtension;
        	
        	// construct Network object 
        	Network network = NetworkUtils.readNetworkFromFile(fileName); 
            NetworkSummary networkSummary = null;
        	
        	// get memory statistics before creating network
        	memoryBefore.put(entry.getKey(), getMemoryUtiliztaion());
        	
            long timeBeforeCreate = System.currentTimeMillis();
            
        	try {
				networkSummary = ndex.createNetwork(network);
			} catch (Exception e) {
                fail("unable to create network " + networkName + " : " + e.getMessage() );
			}
        	
			long createTimeInMs = System.currentTimeMillis() - timeBeforeCreate;
            String formattedCreateTime = formatOutput(createTimeInMs);
        	
        	// get memory statistics after creating network
        	memoryAfter.put(entry.getKey(), getMemoryUtiliztaion());
        	
        	HashMap<String, String> benchmark = new HashMap<String, String>();
        	benchmark.put("name",   networkName);        	
        	benchmark.put("size",   NumberFormat.getNumberInstance(Locale.US).format(new File(networkPath).length()));
        	benchmark.put("json",   NumberFormat.getNumberInstance(Locale.US).format(new File(fileName).length()));
        	benchmark.put("nodes",  NumberFormat.getNumberInstance(Locale.US).format(network.getNodeCount()));
        	benchmark.put("edges",  NumberFormat.getNumberInstance(Locale.US).format(network.getEdgeCount()));
        	benchmark.put("upload", formattedCreateTime);

        	benchmarkData.put(entry.getKey(), benchmark);
        }
		
        printNetworkCreateReport(memoryBefore, memoryAfter, benchmarkData);
    }

	
	
	private void printNetworkUploadAndDownloadReport(
			Map<String, Map<String, String>> memoryBefore,
			Map<String, Map<String, String>> memoryAfter,
			Map<String, Map<String, String>> benchmarkData) {
		
        for (Entry<String, String> entry : testNetworks.entrySet()) {
        	String key = entry.getKey();
        	
            printRuntimeMemoryUsage("\n--- Memory before running test0001BenchmarkNetworkUploadAndDownload, Bytes ---", memoryBefore.get(key));
            
            System.out.println(
                benchmarkData.get(key).get("name") + "\t" + 
                "size: "   +  benchmarkData.get(key).get("size") + "\t" + 
                "nodes: " + benchmarkData.get(key).get("nodes") + "\t" + 
                "edges: " +  benchmarkData.get(key).get("edges") + "\t" + 
                "upload time: " + benchmarkData.get(key).get("upload") + "\t" + 
                "download time: " + benchmarkData.get(key).get("download") + "\t" + 
                "download read-only time: " + benchmarkData.get(key).get("readonly") );
 
        	printRuntimeMemoryUsage("--- Memory after  running test0001BenchmarkNetworkUploadAndDownload, Bytes ---", memoryAfter.get(key));
        }
	}

	private void printNetworkCreateReport(
			Map<String, Map<String, String>> memoryBefore,
			Map<String, Map<String, String>> memoryAfter,
			Map<String, Map<String, String>> benchmarkData) {
		
        for (Entry<String, String> entry : testNetworks.entrySet()) {
        	String key = entry.getKey();
        	
            printRuntimeMemoryUsage("\n--- Memory before running test0002BenchmarkNetworkCreate, Bytes ---", memoryBefore.get(key));
            
            System.out.println(
                benchmarkData.get(key).get("name") + "\t" + 
                "size: "   +  benchmarkData.get(key).get("size") + "\t" + 
                "JSON size: "   +  benchmarkData.get(key).get("json") + "\t" + 		
                "nodes: " + benchmarkData.get(key).get("nodes") + "\t" + 
                "edges: " +  benchmarkData.get(key).get("edges") + "\t" + 
                "creation time: " + benchmarkData.get(key).get("upload") + "\t" );
 
        	printRuntimeMemoryUsage("--- Memory after  running test0002BenchmarkNetworkCreate, Bytes ---", memoryAfter.get(key));
        }
	}	

	private Map<String, String> getMemoryUtiliztaion() {
		
		Map<String, String> memory = new HashMap<String,String>();

	    Runtime runtime = Runtime.getRuntime();

		memory.put("heap", df.format(runtime.totalMemory()));
		memory.put("max",  df.format(runtime.maxMemory()));
		memory.put("used", df.format(runtime.totalMemory() - runtime.freeMemory()));
		memory.put("free", df.format(runtime.freeMemory()));
		
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
