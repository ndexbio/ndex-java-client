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

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ndexbio.model.object.Group;
import org.ndexbio.model.object.NewUser;
import org.ndexbio.model.object.User;
import org.ndexbio.rest.client.NdexRestClient;
import org.ndexbio.rest.client.NdexRestClientModelAccessLayer;
import org.ndexbio.rest.test.utilities.GroupUtils;
import org.ndexbio.rest.test.utilities.UserUtils;

/**
 *  This class contains JUNit tests for testing GroupService APIs from the 
 *  UserService.java class located in src/main/java/org.ndexbio.rest.services package of ndexbio-rest module.
 *
 *  APIs tested in this class:
 *  
 *  √   1) public Group createGroup(Group)
 *  √   2) public void deleteGroup(String)
 *      3) public List<Group> findGroups(SimpleUserQuery, int, int)     
 *      4) public Group getGroup(String)
 *      5) public List<Membership> getGroupNetworkMemberships(String, String, int, int)
 *      6) public List<Membership> getGroupUserMemberships(String, String, int, int)    
 *      7) public Membership getNetworkMembership(String, String) 
 *      8) public void removeMember(String, String)   
 *      9) public Group updateGroup(Group, String)
 *     10) public void updateMember(String, Membership)   
 *     
 */

public class testGroupService {

    private static NdexRestClient                 client;
    private static NdexRestClientModelAccessLayer ndex;
 
    private static String accountName     = "ggg";
    private static String accountPassword = "ggg";

    // testUser is the user that will be created on the NDEx server as part of testing
    // prior to testing, this account should not exist on this server
    private static User    testAccount    = null;
    private static NewUser testUser       = null;
    
    private static String groupName       = "TestGroupName"; 

	/**
	 * This methods runs once before any of the test methods in the class.
	 * It creates ndex client used by other tests.
	 * 
     * @param   void
     * @return  void
     */
    @BeforeClass
    public static void setUp() throws Exception {
		
    	// create user object; the properties describe the current test set-up
        testUser = UserUtils.getNewUser(
				accountName,
				accountPassword,
		        "This account is used for testing Group Service APIs",  // description
		        "group_service_apis@xxxxxx.com",                        // email address
		        "User",                                                 // first name -- name of the test
		        "Service APIs",                                         // last name -- name of the test		        
		        "http://www.yahoo.com",                                 // image
		        "http://www.yahoo.com/finance");                        // web-site
        
		// create ndex client and a test user account
        try {
            client = new NdexRestClient(accountName, accountPassword, JUnitTestSuite.testServerURL);
            ndex   = new NdexRestClientModelAccessLayer(client);
        } catch (Exception e) {
        	fail("Unable to create ndex client: " + e.getMessage());
        }
        
        // in case user account exists, delete it
    	UserUtils.deleteUser(ndex);
    	
		// create test account
    	testAccount = UserUtils.createUserAccount(ndex, testUser);
    }
    
    /**
     * Clean-up method.  The last method called in this class by JUnit framework.
     * It removes all networks uploaded to the test account, and removes the test
     * account itself.
     * 
     * @throws  Exception
     * @param   void
     * @return  void
     */
    @AfterClass
    public static void tearDown() throws Exception {
    	
    	// delete the test user account
    	UserUtils.deleteUser(ndex);
    } 
    
    
    /**
     * Create a group on the server, and delete it.
     * 
     * APIs tested: public Group createGroup(Group)
     *              public void deleteGroup(String)              
     */
    @Test
    public void test0001CreateGroup() {
    	Group group = new Group();
    	
    	group.setAccountName(groupName);
    	group.setDescription("This group is used to test GroupService APIs");
    	group.setImage("http://imgur.com/gallery/ukfzg2C");
    	group.setOrganizationName("UCSD Cytoscape Consortium | NDEx Project");
    	group.setWebsite("http://www.ndexbio.org");
    
    	// create new group
    	Group newGroup = GroupUtils.createGroup(ndex, group);
    	
    	// check the contents of the newly created  Group obect
    	GroupUtils.compareGroupObjectsContents(group, newGroup);
    	
    	// create the same group again (group already exists on server)
    	// Group anotherNewGroup = GroupUtils.createGroup(ndex, group);    	
    	
    	GroupUtils.deleteGroup(ndex, newGroup);
    }
    
    

}