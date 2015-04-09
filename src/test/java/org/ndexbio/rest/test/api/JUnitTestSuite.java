package org.ndexbio.rest.test.api;

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
	private static Server server;
	
	// with startServer=true; a Jetty server will be started for testing;
	// with startServer=false; the external server will be used (URL is specified in task* classes)
	private static boolean startServer = false;
	
	@BeforeClass
	public static void setUp() throws Exception {

		if (startServer) {
		
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
		if (startServer) 
		{
		    System.out.println("Shutting down server");
		    NdexDatabase.close();
		    server.stop();
		}
	}

}
