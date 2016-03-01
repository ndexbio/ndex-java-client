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
	    
	    // ndexConfigurationPath is /opt/ndex/conf/ndex.properties;  it is defined in 
	    // Run->Run Configurations, Environment tab
	    environment.put("ndexConfigurationPath", System.getenv("ndexConfigurationPath"));
	    environment.put("logback.configurationFile", "src/test/java/org/ndexbio/rest/test/JettyServer/jetty-logback.xml");	
	   
	    builder.inheritIO();
	    
		try {
			jettyServerProcess = builder.start();
		} catch (Exception e) {	
			fail("Unable to start JettyServer in a separate JVM : " + e.getMessage());
		}

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
	
	public static void shutdownServerRemoveDatabase() {
		
		// stop Jetty Server
		sendCommand("stopServerRemoveDatabase");

    	  
    	if (null != toServer)  {
    		toServer.close();
    		toServer = null;
    	}
    	
    	if (null != fromServer)
			try {
				fromServer.close();
				fromServer = null;
			} catch (IOException e) {}
    	
    	if (null != socket)
			try {
				socket.close();
				socket = null;
			} catch (IOException e) {}
    	
    	jettyServerProcess.destroy();
    	try {
    		// wait for the process to terminate
			jettyServerProcess.waitFor();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	jettyServerProcess = null;
	}
	
	
}
