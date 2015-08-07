package org.ndexbio.rest.test.JettyServer;

//import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Map;


import org.apache.commons.io.FileUtils;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.ndexbio.common.access.NdexDatabase;
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
			
	        Socket server = serverSocket.accept();
	        
			while(true) {

			    PrintWriter toClient = new PrintWriter(server.getOutputStream(), true);
				BufferedReader fromClient = new BufferedReader(new InputStreamReader(server.getInputStream()));
				    
				command = fromClient.readLine();			    
				
				if (null == command) {
					// didn't receive anything (is it possible?) -- wait for another connection
					continue;
                }
				
				successOfOperation = false;
				
				switch(command) {
			        case "restartServerWithCleanDatabase":
		        	    // shut down the server and database, remove the database files from the filesystem,
			    	    // and restart the server and database   	
			    	    successOfOperation = restartServerWithCleanDatabase();
			  	        break;
			  	        
				    case "stopServerRemoveDatabase":
                        // shut down the server and database, and close sockets after that
				    	successOfOperation = stopServerRemoveDatabase();
				    	break;	
				    	
				    case "stopServer":
                        // shut down the server and database
				    	successOfOperation = stopServer();
				    	break;
				    		
				    case "start":
                        // start the server and database				    	
				    	successOfOperation = startServer();
					    break;
					    				  	    
			        default:
			            continue;
			    	    // break;
				}

                // send "success" or "failure" to the client
                toClient.println(successOfOperation ? "done" : "failed");
		
				if ((command != null) && 
					( (command.equalsIgnoreCase("stopServerRemoveDatabase") ||
					   command.equalsIgnoreCase("stopServer")) )) 
				{
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
		
	    Map<String, String> env = System.getenv();
	    String logbackConfig = env.get("logback.configurationFile");
    	System.out.println("logback.configurationFile=" + logbackConfig);	    
	    if (null != logbackConfig) {
	    	System.out.println("Settinng property logback.configurationFile=" + logbackConfig);
	    	System.setProperty("logback.configurationFile", logbackConfig);
	    }

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
	    	NdexDatabase.close();
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
	
	private static boolean restartServerWithCleanDatabase() {
        
        if (!stopServerRemoveDatabase() ){
        	return false;
        }

		return startServer();
	}

	private static boolean stopServerRemoveDatabase() {
        
		stopServer(); 

		return removeDatabase();
	}
	
	
	private static boolean removeDatabase() {
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
		
		return true;
	}
	
	public static int getServerPort() {
		return serverPort;
	}
	
	public static Server getJettyServer() {
		return server;
	}
}