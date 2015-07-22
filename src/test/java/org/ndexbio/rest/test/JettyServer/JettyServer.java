package org.ndexbio.rest.test.JettyServer;

//import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.ndexbio.rest.NdexHttpServletDispatcher;
import org.ndexbio.task.Configuration;

public class JettyServer {
	
	private static Server server = null;
	
	private static int serverPort = 8023;
	
	ServerSocket serverSocket = null;
	
	String command = null;

	private boolean successOfOperation = false;
	
	
	public void run() {
		
		if (!initializeAndStartServer()) {
            System.out.println("Unable to start Jetty server");
            System.exit(0);			
		}
		
		try {
			try {
			    serverSocket = new ServerSocket(serverPort);
			} catch (Exception e) {
                //System.out.println("Unable to create ServerSocket : " + e.getMessage());
                stopServer();
                System.exit(0);
			}
			

		    //System.out.println("Waiting for client on port " + serverSocket + " ....."); 
	        Socket server = serverSocket.accept();
	        //System.out.println("Got connection from " + server.getRemoteSocketAddress()); 
	        
			while(true) {

			    PrintWriter toClient = new PrintWriter(server.getOutputStream(), true);
				BufferedReader fromClient = new BufferedReader(new InputStreamReader(server.getInputStream()));
				    
				command = fromClient.readLine();
				//System.out.println("command = " + command);			    
				
				if (null == command) {
					// didn't receive anything (is it possible?) -- wait for another connection
					continue;
                }
				
				successOfOperation = false;
				
				switch(command) {		  	    
				    case "shutdown":
                        // shut down the server and database
				    	successOfOperation = stopServer();
				    	break;
				    	
				    case "shutdownAndQuit":
                        // shut down the server and database and close sockets
				    	successOfOperation = stopServer();
				    	break;	
				    	
				    case "start":
                        // start the server and database				    	
				    	successOfOperation = startServer();
					    break;
					    
				    case "cleanDatabase":
			        	// shut down the server and database, remove the database files from the filesystem
				    	// and restart the server and database
				    	successOfOperation = cleanDatabase();
				  	    break;
				  	    
			        default:
			            continue;
			    	    // break;
				}

                // send "success" or "failure" to the client
                toClient.println(successOfOperation ? "done" : "failed");
		
				if ((command != null) && (command.equalsIgnoreCase("shutdownAndQuit"))) {
			        if (null != toClient)   toClient.close();
			    	if (null != fromClient) fromClient.close();
			    	if (null != server)     server.close();
			    	break;
				}
			 } 
		}	
		catch(UnknownHostException e) { e.printStackTrace(); }
		catch(IOException e) { e.printStackTrace(); }
	}
			
	public static void main(String[] args) {
		JettyServer srv = new JettyServer();
		srv.run();
	}
	

    private static boolean initializeAndStartServer() {

        server = new Server(8080);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/ndexbio-rest");
        ServletHolder h = new ServletHolder(new NdexHttpServletDispatcher());
        h.setInitParameter("javax.ws.rs.Application", "org.ndexbio.rest.NdexRestApi");
        context.addServlet(h, "/*");
        server.setHandler(context);
        
        return startServer();
    }	
	
	private static boolean stopServer() {
		
	    try {
	    	server.stop();
	    } catch (Exception e) {
	    	System.out.println("can't stop server: " + e.getMessage());
	    	return false;
	    }
		
        // wait for the server to stop 
		while (true) {
			if (server.isStopped()) {
				return true;
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) { return false; }
		}
	}
	
	private static boolean startServer() {

	    try {
	    	server.start();
	    } catch (Exception e) {
	    	System.out.println("can't start server: " + e.getMessage());
	    	return false;
	    }
	    
        // wait for the server to start 
		while (true) {
			if (server.isStarted()) {
				return true;
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) { return false; }
		}
	}
	
	private static boolean cleanDatabase() {
        
		if (!stopServer()) {
			return false;
		}
		
		Configuration configuration = null;
		try {
			configuration = Configuration.getInstance();
		} catch (Exception e) {
			System.out.println("unable to get Configuration instance.");
			return false;
		}
	
		String dbURL = configuration.getDBURL().replaceAll("plocal:", "");
		
		// remove the database directory from the filesystem
		try {
			FileUtils.deleteDirectory(new File(dbURL));
		} catch (IOException e1) {
			System.out.println("unable to delete " + dbURL);
			return false;
		}

		return startServer();
	}
	
	public static int getServerPort() {
		return serverPort;
	}
}
