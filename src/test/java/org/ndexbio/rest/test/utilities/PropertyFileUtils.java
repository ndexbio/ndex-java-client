package org.ndexbio.rest.test.utilities;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Enumeration;

import java.util.Properties;
import java.util.TreeMap;


public class PropertyFileUtils {
	public static TreeMap<String, String> parsePropertyFile(String path) {
		TreeMap<String, String> testNetworks = null;
		try {
			File file = new File(path);
			FileInputStream fileInput = new FileInputStream(file);
			Properties properties = new Properties();
			properties.load(fileInput);
			fileInput.close();
			
			Enumeration enuKeys = properties.keys();
			
			testNetworks = new TreeMap<String, String>();
			
			while (enuKeys.hasMoreElements()) {
				String key = (String) enuKeys.nextElement();
				String value = properties.getProperty(key);
				//System.out.println(key + ": " + value);
				testNetworks.put(key, value);
			}			
			
		} catch (FileNotFoundException e) {
			fail("Properties file with networks to upload not found: " + e.getMessage());
		} catch (IOException e) {
			fail("Unable to read properties file with networks to upload: " + e.getMessage());
		}
		
		return testNetworks;
	}
}
