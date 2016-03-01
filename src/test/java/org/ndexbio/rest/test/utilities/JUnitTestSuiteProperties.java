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
