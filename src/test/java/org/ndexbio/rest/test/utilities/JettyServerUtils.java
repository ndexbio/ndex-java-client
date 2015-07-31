package org.ndexbio.rest.test.utilities;

import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jetty.server.Server;
import org.ndexbio.rest.test.JettyServer.JettyServer;

public class JettyServerUtils {

	static Process jettyServerProcess = null;
	
    private static Socket         socket     = null;
    private static PrintWriter    toServer   = null;
    private static BufferedReader fromServer = null;
    
	
	public static Process startJettyInNewJVM() {
		
        // start JettyServer in a separate JVM
		String javaHome = System.getProperty("java.home");
		String javaBin = javaHome +
		                File.separator + "bin" +
		                File.separator + "java";
		String classpath = System.getProperty("java.class.path");
        
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
			jettyServerProcess = builder.start();
		} catch (IOException e) {	
			fail("Unable to start JettyServer in a separate JVM : " + e.getMessage());
		}
		
        // --------------------------------------------------------------------
		// create client socket to communicate to JettyProcess
	    InetAddress host = null;
		try {
			host = InetAddress.getByName("localhost");
		} catch (UnknownHostException e) {
			fail("Unable to get address of localhost : " + e.getMessage());
		} 

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
		
		try {
			toServer = new PrintWriter(socket.getOutputStream(), true);
		} catch (IOException e) {
			fail("unable to create writer (output stream) to server : " + e.getMessage());
		}
		try {
			fromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch (IOException e) {
			fail("unable to create reader (input stream) from server : " + e.getMessage());
		}
		
		return jettyServerProcess;
	}
	
	public static String sendCommand(String command) {
    	String responseFromServer = null;
    	
    	toServer.println(command);

		try {
			responseFromServer = fromServer.readLine();
		} catch (IOException e) {
			fail("Unable to read response from Jetty server : " + e.getMessage());
		}
    	//System.out.println("responseFromServer = " + responseFromServer);
    	if ((responseFromServer == null) || (responseFromServer.equalsIgnoreCase("failed"))) {
    		fail("Unable to restart Jetty Server and clean database");
    	}
    	return responseFromServer;
	}
	
	public static void shutdown() {
		
		// stop Jetty Server
		sendCommand("stopServer");

    	  
    	if (null != toServer)  toServer.close();
    	
    	if (null != fromServer)
			try {
				fromServer.close();
			} catch (IOException e) {}
    	
    	if (null != socket)
			try {
				socket.close();
			} catch (IOException e) {}
    	
    	jettyServerProcess.destroy();
    	jettyServerProcess = null;
	}
	
	
}
