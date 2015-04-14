package org.ndexbio.rest.test.api;

import java.io.FileInputStream;
import java.util.Properties;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.ndexbio.common.access.NdexDatabase;
import org.ndexbio.rest.NdexHttpServletDispatcher;




@RunWith(Suite.class)
@Suite.SuiteClasses ({
	testUserService.class,
	testTaskService.class
})



public class JUnitTestSuite {
	private  static Server server;
	
	// the following parameters are read from the configuration file
	
	// with startServer=true; a Jetty server will be started for testing;
	// with startServer=false; the external server will be used (URL is specified in task* classes)	
	private  static boolean useJettyServer = true;
	
	// the testerName account should exist on the server prior to testing
	public static String testerName      = null;
	public static String testerPassword  = null;
	
	// the userName account will be created by the test suites in the course of testing
	public static String userName        = null;
	public static String password        = null;
	
	// URL of the test server
	public static String testServerURL   = null;
	
	
	@BeforeClass
	public static void setUp() throws Exception {
		
		// JUnitTestSuite.properties is defined in Run->Run Configurations->JUnit->JUnitTestSuite, Arguments Tab:
		// -DJUnitTestSuite.properties=src/main/resources/JUnitTestSuite.properties
		// the properties file is src/main/resources/JUnitTestSuite.properties
		
		// open properties file and set/initialize properties
		String propertiesFile = System.getProperty("JUnitTestSuite.properties");
		
		assert  (propertiesFile != null) : "JUnitTestSuite.properties is not defined; need this file to get tester name, password, server URL, etc.";
		
		FileInputStream fis = new FileInputStream(propertiesFile);
        Properties p = new Properties(); 
		p.load(fis);
			
        useJettyServer  = Boolean.parseBoolean(p.getProperty("useJettyServer"));
        testerName      = p.getProperty("testerName"); 
        testerPassword  = p.getProperty("testerPassword");
        userName        = p.getProperty("userName");
        password        = p.getProperty("password");
        testServerURL   = p.getProperty("testServerURL");

		if (useJettyServer) {
		
			if (!startServer()) {
			    System.out.println("Unable to start Jetty server");
			    System.exit(0);
		    }
		
		    System.out.println("Server started succesfully.");	
		}
	}

	private static boolean startServer() {
		boolean success = true;
        server = new Server(8080);
		
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setContextPath("/ndexbio-rest");
        ServletHolder h = new ServletHolder(new NdexHttpServletDispatcher());
		h.setInitParameter("javax.ws.rs.Application", "org.ndexbio.rest.NdexRestApi");
		context.addServlet(h, "/*");
		server.setHandler(context);
		
		try {
			server.start();
			//server.join();
			
		} catch (Exception e) {
			
			e.printStackTrace();
			success = false;
		}
		return success;
	}
	
	@AfterClass
	public static void tearDown() throws Exception {
		if (useJettyServer) 
		{
		    System.out.println("Shutting down server");
		    NdexDatabase.close();
		    server.stop();
		}
	}

}
