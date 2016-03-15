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
package org.ndexbio.rest.test.api;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses ({
	testGroupService.class,
	testNetworkAService.class,
	testNetworkConcurrentAcess.class,
	testTaskService.class,
	testUserService.class
})


public class JUnitTestSuite {

}

	/*
    private  static Server server;

    // the following parameters are read from the configuration file

    // with startServer=true; a Jetty server will be started for testing;
    // with startServer=false; the external server will be used (URL is specified in task* classes)
    private  static boolean useJettyServer = true;

    // URL of the test server
    public static String testServerURL   = null;
    
    private  static boolean deleteDatabase = true;
    

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
        fis.close();

        useJettyServer  = Boolean.parseBoolean(p.getProperty("useJettyServer"));
        testServerURL   = p.getProperty("testServerURL");
        deleteDatabase  = Boolean.parseBoolean(p.getProperty("deleteDatabase"));
        
        if (useJettyServer) {

            if (!startServer()) {
                System.out.println("Unable to start Jetty server");
                System.exit(0);
            }

            System.out.println("Server started successfully.");
        }
    }

    public static boolean startServer() {
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

    public static boolean getUseJettyServer() {
    	return useJettyServer;
    };
    public static Server getServer() {
    	return server;
    }
    public static boolean deleteDatabase() {
    	return deleteDatabase;
    }
*/
 
