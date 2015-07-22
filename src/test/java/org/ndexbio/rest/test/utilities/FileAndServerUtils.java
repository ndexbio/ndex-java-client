package org.ndexbio.rest.test.utilities;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.eclipse.jetty.server.Server;
import org.ndexbio.rest.test.api.JUnitTestSuite;
import org.ndexbio.task.Configuration;

public class FileAndServerUtils {
	public static void cleanDatabase() {
        
		stopServer(); 
		
		Configuration configuration = null;
		try {
			configuration = Configuration.getInstance();
		} catch (Exception e) {
			fail("unable to get Configuration instance.");
		}
	
		String dbURL = configuration.getDBURL().replaceAll("plocal:", "");
		
		// remove the database directory from the filesystem
		try {
			FileUtils.deleteDirectory(new File(dbURL));
		} catch (IOException e1) {
			fail("unable to delete " + dbURL);
		}

		startServer();
	}
	
	public static void stopServer() {
		//if (!JUnitTestSuite.getUseJettyServer()) {
		//	fail("need to use Jetty Server for performance benchmarking.");
		//}
		
		Server server = JUnitTestSuite.getServer();
		
	    try {
	    	server.stop();
	    } catch (Exception e) {
	    	fail("can't stop server: " + e.getMessage());
	    }
		
        // wait for the server to stop 
		while (true) {
			if (server.isStopped()) {
				return;
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {}
		}
	}
	
	public static void startServer() {
		if (!JUnitTestSuite.getUseJettyServer()) {
			fail("need to use Jetty Server for performance benchmarking.");
		}
		
		Server server = JUnitTestSuite.getServer();
		
	    try {
	    	server.start();
	    } catch (Exception e) {
	    	fail("can't start server: " + e.getMessage());
	    }
	    
        // wait for the server to start 
		while (true) {
			if (server.isStarted()) {
				return;
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {}
		}
	}

}
