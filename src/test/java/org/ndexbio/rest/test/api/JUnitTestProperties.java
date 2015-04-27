package org.ndexbio.rest.test.api;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.junit.Ignore;

public class JUnitTestProperties {
    private  String testerName      = null;
    private  String testerPassword  = null;
    private  String userName        = null;
    private  String password        = null;
    private  String testServerURL   = null;
        
    public String getTesterName() {
        return testerName;
    }
    public void setTesterName(String testerName) {
        this.testerName = testerName;
    }
    public String getTesterPassword() {
        return testerPassword;
    }
    public void setTesterPassword(String testerPassword) {
        this.testerPassword = testerPassword;
    }
    public String getUserName() {
        return userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
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
            
        testerName      = p.getProperty("testerName"); 
        testerPassword  = p.getProperty("testerPassword");
        userName        = p.getProperty("userName");
        password        = p.getProperty("password");
        testServerURL   = p.getProperty("testServerURL");
    }

}

