package org.ndexbio.rest.test.utilities;

import static org.junit.Assert.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class JUnitTestSuiteProperties {

	// URL of the test server
	private static String testServerURL  = null;


	public static String getTestServerURL()  {

		if (testServerURL != null) {
			return testServerURL;
		}
		
		// JUnitTestSuite.properties is defined in Run->Run Configurations->JUnit->JUnitTestSuite, Arguments Tab:
		// -DJUnitTestSuite.properties=src/test/resources/JUnitTestSuite.properties
		// the properties file is src/test/resources/JUnitTestSuite.properties

		// open properties file and set/initialize properties
		String propertiesFile = System.getProperty("JUnitTestSuite.properties");

		assertNotNull(propertiesFile);

		FileInputStream fis = null;
		try {
			fis = new FileInputStream(propertiesFile);
		} catch (FileNotFoundException e) {
			fail("unable to open properties file: " + e.getMessage() );
			System.exit(0);
		}
		Properties p = new Properties();
		try {
			p.load(fis);
		} catch (IOException e) {
			fail("unable to load properties from properties file: " + e.getMessage() );
		}
		try {
			fis.close();
		} catch (IOException e) {}
		
		testServerURL = p.getProperty("testServerURL");
		
		if (null == testServerURL) {
			fail("testServerURL found in " + propertiesFile + " is null");
		}
		
		return testServerURL;
	}
	
}
