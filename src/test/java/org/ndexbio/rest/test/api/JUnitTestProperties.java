package org.ndexbio.rest.test.api;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class JUnitTestProperties {
    private  String testServerURL   = null;
    
    public String getTestServerURL() {
        return testServerURL;
    }
    public void setTestServerURL(String testServerURL) {
        this.testServerURL = testServerURL;
    }        
    public JUnitTestProperties(String properties) {
            
        //String propertiesFile = System.getProperty("JUnitTestSuite.properties");
        String propertiesFile = System.getProperty(properties);         
            
        assert  (propertiesFile != null) : "JUnitTestSuite.properties is not defined; need this file to get tester name, password, server URL, etc.";
            
        FileInputStream fis = null;
            
        try {
            fis = new FileInputStream(propertiesFile);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
            
        Properties p = new Properties(); 
        try {
           p.load(fis);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        testServerURL = p.getProperty("testServerURL");
    }

}

