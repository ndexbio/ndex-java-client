/**
 * Copyright (c) 2013, 2015, The Regents of the University of California, The Cytoscape Consortium
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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class JUnitTestProperties {
    private  String testerName      = null;
    private  String testerPassword  = null;
    private  String userName        = null;
    private  String password        = null;
    private  String testServerURL   = null;
    private  String networkToUpload = null;
    
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
    public String getNetworkToUpload() {
        return networkToUpload;
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
    public void setNetworkToUpload(String networkToUpload) {
        this.networkToUpload = networkToUpload;
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
        networkToUpload = p.getProperty("networkToUpload");
    }

}

