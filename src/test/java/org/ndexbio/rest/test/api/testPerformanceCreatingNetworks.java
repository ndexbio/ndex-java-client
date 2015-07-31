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

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.ndexbio.model.exceptions.NdexException;
import org.ndexbio.model.object.NdexStatus;
import org.ndexbio.model.object.NewUser;
import org.ndexbio.model.object.Task;
import org.ndexbio.model.object.User;
import org.ndexbio.model.object.network.Network;
import org.ndexbio.model.object.network.NetworkSummary;
import org.ndexbio.rest.NdexHttpServletDispatcher;
import org.ndexbio.rest.client.NdexRestClient;
import org.ndexbio.rest.client.NdexRestClientModelAccessLayer;
import org.ndexbio.rest.server.StandaloneServer;
import org.ndexbio.rest.test.JettyServer.JettyServer;
import org.ndexbio.rest.test.utilities.FileAndServerUtils;
import org.ndexbio.rest.test.utilities.JettyServerUtils;
import org.ndexbio.rest.test.utilities.NetworkUtils;
import org.ndexbio.rest.test.utilities.PropertyFileUtils;
import org.ndexbio.rest.test.utilities.UserUtils;
import org.ndexbio.task.Configuration;

//The @FixMethodOrder(MethodSorters.NAME_ASCENDING) annotation sorts (and
//executes) the test methods by name in lexicographic order
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class testPerformanceCreatingNetworks {
    
	static String resourcePath = "src/test/resources/";
	static String networksFile = resourcePath + "testPerformanceCreatingNetworks.properties";
	static TreeMap<String, String> testNetworks;
	
    private static NdexRestClient                 client;
    private static NdexRestClientModelAccessLayer ndex;
    
    private static String accountName     = "uuu";
    private static String accountPassword = "uuu";
    
    private static User    testAccount    = null;
    private static NewUser testUser       = null;
    
	DecimalFormat df = new DecimalFormat("#,###");
	
	private static boolean overwriteExistingNetwork = true;
	
    private static Process jettyServer = null;
	

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
		
		// start Jetty server in a new instance of JVM
		jettyServer = JettyServerUtils.startJettyInNewJVM();

    	// build Map of networks for testing from the property file
		testNetworks = PropertyFileUtils.parsePropertyFile(networksFile);
		
		// create user object; the properties describe the current test
		testUser = UserUtils.getNewUser(
				accountName,
				accountPassword,
		        "This account is used for network creating benchmark testing",  // description
		        "benchmark@ucsd.com",                 // email address
		        "Create",                             // first name -- name of the test
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
    }

    
    
    
    /**
     * @throws IOException 
     * 
     * Clean-up method.  The last method called in this class by JUnit framework.
     * 
     * @throws  Exception
     * @param   void
     * @return  void
     * @throws  
     */
    @AfterClass
    public static void tearDown() {
    	
    	// stop the Jetty server, remove database; destroy Jetty Server process
        JettyServerUtils.shutdown();
    }
    
  
    
    @Test
    public void test0002BenchmarkNetworkCreate() {
    	//fail("implement me");
    }
    
	/**
	 * This methods creates networks in JSON format listed in the properties file (networksFile), 
	 * calculates and prints how long it took for every network to upload. 
	 * 
     * @param   void
     * @return  void
	 * @throws IOException 
     */
	@Test  
    public void test0001BenchmarkNetworkCreate() throws IOException {
		Map<String, Map<String, String>> memoryBefore  = new HashMap<String, Map<String, String>>();
		Map<String, Map<String, String>> memoryAfter   = new HashMap<String, Map<String, String>>();
		Map<String, Map<String, String>> benchmarkData = new HashMap<String, Map<String, String>>();

        for (Entry<String, String> entry : testNetworks.entrySet()) {
            	    	
            // stop Jetty server if it is runs, remove database from file system, start Jetty server
        	// (i.e., (re)start server with clean database)
        	String responseFromServer = JettyServerUtils.sendCommand("restartServerWithCleanDatabase");

    		// re-create test account since it was deleted at previous step by cleanDatabase()
    		testAccount = UserUtils.createUserAccount(ndex, testUser);

        	// absolute path name of the file containing network in JSON format
        	String absoluteNetworkPath = entry.getValue().toString();
        	String networkName = FilenameUtils.getName(absoluteNetworkPath);
        	
        	
        	// construct Network object 
        	Network network = NetworkUtils.readNetworkFromFile(absoluteNetworkPath); 
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
        	benchmark.put("size",   NumberFormat.getNumberInstance(Locale.US).format(new File(absoluteNetworkPath).length()));
        	benchmark.put("nodes",  NumberFormat.getNumberInstance(Locale.US).format(network.getNodeCount()));
        	benchmark.put("edges",  NumberFormat.getNumberInstance(Locale.US).format(network.getEdgeCount()));
        	benchmark.put("upload", formattedCreateTime);

        	benchmarkData.put(entry.getKey(), benchmark);
        }
		
        printNetworkCreateReport(memoryBefore, memoryAfter, benchmarkData);
    }


	private void printNetworkCreateReport(
			Map<String, Map<String, String>> memoryBefore,
			Map<String, Map<String, String>> memoryAfter,
			Map<String, Map<String, String>> benchmarkData) {
		
        for (Entry<String, String> entry : testNetworks.entrySet()) {
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
	
	

	
	
	/**
	 *  -- previous version of setUp -- keep it for reference for now;
	 * 
     * @param   void
     * @return  void
     *
    @BeforeClass
    public static void setUp() throws Exception {
		    			
     	//FileAndServerUtils.stopServer();
        
        // --------------------------------------------------------------------
    	/*
        // start JettyServer in a separate JVM
		String javaHome = System.getProperty("java.home");
		String javaBin = javaHome +
		                File.separator + "bin" +
		                File.separator + "java";
		String classpath = System.getProperty("java.class.path");
        */
		
		
		
		//System.out.println(" javaHome=" + javaHome);
		//System.out.println("  javaBin=" + javaBin);		 

        //final String userDir = System.getProperty("user.dir");
        //System.out.println("current userDir = " + userDir);  

		//String jettyServerStartCommand =  javaBin + " -classpath " + classpath + " org/ndexbio/rest/test/JettyServer/JettyServer";

		//System.out.println("jettyServerStartCommand=" + jettyServerStartCommand);		
    	//Process p = Runtime.getRuntime().exec("ps -ef ");
    	
        
         //
         // another way to start JettyServer is to 
         // 
         //    1) generate StandaloneServer.jar in the ndex-rest project -- its' pom.xml should
         //       be modified accordingly for that : 
         //      1.1 change packaging of the project from war to jar (line 8 of pom.xml)
         //      1.2 uncomment plugin on lines 222245; change the finalName on line 233 to StandaloneServer
         //      1.3 uncomment plugin on lines 247-279
         //      
         //   2) after that, build ndex-rest project; it will generate target/shaded-jar.jar and org.ndexbio.rest.server.StandaloneServer
         //   
         //   3) from command line, start StandaloneServer from ndex-rest directory  using the following command:
         //         ~/git/ndex-rest > java -classpath  ./:target/shaded-jar.jar:./src/main/java/org/ndexbio/rest/server:/opt/ndex/apache-tomcat-7.0.62/webapps/ndexbio-rest/WEB-INF/lib/orientdb-lucene-2.0.12.jar:/Users/vrynkov/.m2/repository/org/jboss/resteasy/resteasy-jackson2-provider/3.0.9.Final/resteasy-jackson2-provider-3.0.9.Final.jar  org.ndexbio.rest.server.StandaloneServer
         //     
         //     note that in the command above, we need to specify the following jars in the classpath: shaded-jar.jar, orientdb-lucene-2.0.12.jar, resteasy-jackson2-provider-3.0.9.Final.jar
         //     
         //   4) we can also start the generated StandaloneServer programmatically :    
         //
         //   String jettyServerStartCommand = javaBin + 
         //    " -classpath  ../ndex-rest/:../ndex-rest/target/shaded-jar.jar:../ndex-rest/src/main/java/org/ndexbio/rest/server:/opt/ndex/apache-tomcat-7.0.62/webapps/ndexbio-rest/WEB-INF/lib/orientdb-lucene-2.0.12.jar:/Users/vrynkov/.m2/repository/org/jboss/resteasy/resteasy-jackson2-provider/3.0.9.Final/resteasy-jackson2-provider-3.0.9.Final.jar  org.ndexbio.rest.server.StandaloneServer";
         //			
         //   p = Runtime.getRuntime().exec(jettyServerStartCommand);
         //

        /* -----------------------------------
		
	    List < String > command = new ArrayList <String>();
	    command.add(javaBin);
	    command.add("org/ndexbio/rest/test/JettyServer/JettyServer");
	        
		ProcessBuilder builder = new ProcessBuilder(command);
	    Map< String, String > environment = builder.environment();
	    
	    environment.put("CLASSPATH", classpath);    
	    environment.put("ndexConfigurationPath", "/opt/ndex/conf/ndex.properties");	
	    environment.put("logback.configurationFile", "src/test/java/org/ndexbio/rest/test/JettyServer/jetty-logback.xml");	
	   
	    builder.inheritIO();
	    
		try {
			jettyServer = builder.start();
		} catch (IOException e) {	
			fail("Unable to start JettyServer in a separate JVM : " + e.getMessage());
		}
        ----------------------------------- */

/**
    	//FileAndServerUtils.stopServer();
        
        // --------------------------------------------------------------------
        // start JettyServer in a separate JVM
		String javaHome = System.getProperty("java.home");
		String javaBin = javaHome +
		                File.separator + "bin" +
		                File.separator + "java";
		String classpath = System.getProperty("java.class.path");
        
		System.out.println(" javaHome=" + javaHome);
		System.out.println("  javaBin=" + javaBin);		 

        final String userDir = System.getProperty("user.dir");
        System.out.println("current userDir = " + userDir);
        
		classpath = userDir + "/src/test/java/org/ndexbio/rest/test/JettyServer:" + classpath;
		System.out.println("classpath=" + classpath);		   
			
	    List < String > command = new ArrayList <String>();
	    command.add(javaBin);
	    command.add("org/ndexbio/rest/test/JettyServer/JettyServer");

		ProcessBuilder builder = new ProcessBuilder(command);
	    Map< String, String > environment = builder.environment();
	    environment.put("CLASSPATH", classpath);
	    ///environment.put("CLASSPATH", userDir);	    
	    environment.put("ndexConfigurationPath", "/opt/ndex/conf/ndex.properties");		
	    
	    builder.inheritIO();
	    
		try {
			process = builder.start();
		} catch (IOException e) {	
			fail("Unable to start JettyServer in a separate JVM : " + e.getMessage());
		}
		
        // --------------------------------------------------------------------
		// create client socket to communicate to JettyProcess
		host = InetAddress.getByName("localhost"); 
		//System.out.println("Connecting to server on port " + JettyServer.getServerPort()); 

		
		for (int i = 0; i < 20; i++) {
		    try {
		        socket = new Socket(host, JettyServer.getServerPort()); 
		    } catch (IOException e) {
			    // unable to create socket -- chances are the server hasn't started yet
		    }
		    
		    if (null != socket) {
		    	// socket created -- get out of the loop
		    	break;
		    } else {
				try {
					// socket is not created yet -- sleep for one sec and try again
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					
				}
		    }
		}
		
		if (null == socket) {
			fail("unable to create socket to Jetty server");
		}
		
		toServer = new PrintWriter(socket.getOutputStream(), true);
		fromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		
/**
    	// build Map of networks for testing from the property file
		testNetworks = PropertyFileUtils.parsePropertyFile(networksFile);
		
		// create user object; the properties describe the current test
		testUser = UserUtils.getNewUser(
				accountName,
				accountPassword,
		        "This account is used for network creating benchmark testing",  // description
		        "benchmark@ucsd.com",                 // email address
		        "Create",                             // first name -- name of the test
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
        
		//testAccount = UserUtils.createUserAccount(ndex, testUser);
    }
    */
  
	/**
	 * This methods uploads networks in JSON format listed in the properties file (networksFile), 
	 * calculates and prints how long it took for every network to upload. 
	 * 
     * @param   void
     * @return  void
     *
	//@Test
    public void test0001BenchmarkNetworkCreateAndDownload() {
		Map<String, Map<String, String>> memoryBefore  = new HashMap<String, Map<String, String>>();
		Map<String, Map<String, String>> memoryAfter   = new HashMap<String, Map<String, String>>();
		Map<String, Map<String, String>> benchmarkData = new HashMap<String, Map<String, String>>();
  
        Map<String,String> uploadedNetworks = new HashMap<String, String>();
        
        for (Entry<String, String> entry : testNetworks.entrySet()) {
            	
        	// shut down the server and database, and remove the database files from the filesystem
    		cleanDatabase();
    		
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
					
			// delete network on the test server
	    	NetworkUtils.deleteNetwork(ndex, networkUUID);
	    	
	    	entireNetwork = null;
        }
		
        printNetworkUploadAndDownloadReport(memoryBefore, memoryAfter, benchmarkData);
        memoryBefore  = null;
        memoryAfter   = null;
        benchmarkData = null;
    }
    */
	
    /**
     *   -- previous version of tearDown -- keep it for reference for now;
     * 
     * @throws  Exception
     * @param   void
     * @return  void
     * @throws  
     *
    @AfterClass
    public static void tearDown() throws IOException  {
    	
    	// delete all networks from the test account
    	// NetworkUtils.deleteNetworks(ndex, accountName, testNetworks);
    	
    	// delete the test user account
    	//UserUtils.deleteUser(ndex);
			
    	/*
    	NdexStatus status = ndex.getServerStatus();
    	
    	ndex.shutDownJettyServer();
    	
    	if ( null != p ) {
    		p.destroy();
    	}
    	
    	status = ndex.getServerStatus();
    	
    	System.out.println("done"); 
    	*/
    	
    	
        //JettyServerUtils.shutdown();
        
        /*
		toServer.println("shutdownAndQuit");

    	try {
			responseFromServer = fromServer.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			fail("Unable to read response from Jetty server : " + e.getMessage());
		}
  
    	if (null != toServer)  toServer.close();
    	
    	if (null != fromServer)
			try {
				fromServer.close();
			} catch (IOException e) {}
    	
    	if (null != socket)
			try {
				socket.close();
			} catch (IOException e) {}

    	//jettyServer.destroy();
    }
*/

}
