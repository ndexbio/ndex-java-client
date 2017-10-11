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

import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.UUID;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runners.MethodSorters;
import org.ndexbio.model.exceptions.DuplicateObjectException;
import org.ndexbio.model.exceptions.NdexException;
import org.ndexbio.model.exceptions.ObjectNotFoundException;
import org.ndexbio.model.object.Group;
import org.ndexbio.model.object.NewUser;
import org.ndexbio.model.object.SimpleQuery;
import org.ndexbio.model.object.User;
import org.ndexbio.rest.client.NdexRestClient;
import org.ndexbio.rest.client.NdexRestClientModelAccessLayer;
import org.ndexbio.rest.test.utilities.GroupUtils;
import org.ndexbio.rest.test.utilities.JUnitTestSuiteProperties;
import org.ndexbio.rest.test.utilities.UserUtils;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 *  This class contains JUNit tests for testing GroupService APIs from the 
 *  UserService.java class located in src/main/java/org.ndexbio.rest.services package of ndexbio-rest module.
 *
 *  APIs tested in this class:
 *  
 *  √   1) public Group createGroup(Group)
 *  √   2) public void deleteGroup(String)
 *      3) public List<Group> findGroups(SimpleUserQuery, int, int)     
 *  √   4) public Group getGroup(String)
 *      5) public List<Membership> getGroupNetworkMemberships(String, String, int, int)
 *      6) public List<Membership> getGroupUserMemberships(String, String, int, int)    
 *      7) public Membership getNetworkMembership(String, String) 
 *      8) public void removeMember(String, String)   
 *      9) public Group updateGroup(Group, String)
 *     10) public void updateMember(String, Membership)   
 *     
 */
//The @FixMethodOrder(MethodSorters.NAME_ASCENDING) annotation sorts (and
//executes) the test methods by name in lexicographic order
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class testGroupService {

    private static NdexRestClient                 client;
    private static NdexRestClientModelAccessLayer ndex;
 
    private static String accountName     = "ggg";
    private static String accountPassword = "ggg";
    
    // URL of the test server
    private static String testServerURL = null;

    // testUser is the user that will be created on the NDEx server as part of testing
    // prior to testing, this account should not exist on this server
    private static User    testAccount    = null;
    private static User testUser       = null;
    
    private static String groupName       = "TestGroupName"; 
    
    private static Group newGroup         = null;
    private static Group group            = null;
    
    private static Process jettyServer    = null;
    
	/**
	 * This methods runs once before any of the test methods in the class.
	 * It creates ndex client used by other tests.
	 * 
     * @param   void
     * @return  void
     */
    @BeforeClass
    public static void setUp() throws Exception {
    	testServerURL = JUnitTestSuiteProperties.getTestServerURL();
    	
		// start Jetty server in a new instance of JVM
//		jettyServer = JettyServerUtils.startJettyInNewJVM();
		
    	// create user object; the properties describe the current test set-up
 /*       testUser = UserUtils.getNewUser(
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
            client = new NdexRestClient(accountName, accountPassword, testServerURL);
            ndex   = new NdexRestClientModelAccessLayer(client);
        } catch (Exception e) {
        	fail("Unable to create ndex client: " + e.getMessage());
        }
        
        // in case user account exists, delete it
    	UserUtils.deleteUser(ndex);
    	
		// create test account
    //	testAccount = UserUtils.createUserAccount(ndex, testUser);
    	
    	group = new Group();
    	
    	group.setGroupName(groupName);
    	group.setDescription("This group is used to test GroupService APIs");
    	group.setImage("http://imgur.com/gallery/ukfzg2C");
    	group.setGroupName("UCSD Cytoscape Consortium | NDEx Project");
    	group.setWebsite("http://www.ndexbio.org"); */
    }
    
    /**
     * Clean-up method.  The last method called in this class by JUnit framework.
     * 
     * @throws  Exception
     * @param   void
     * @return  void
     */
    @AfterClass
    public static void tearDown() throws Exception {
    	
    	// delete the test user account
    	//UserUtils.deleteUser(ndex);
    	
    	// stop the Jetty server, remove database; destroy Jetty Server process
  //      JettyServerUtils.shutdownServerRemoveDatabase();
    }

	/**
	 * This methods runs after every test case.
	 * It deletes test group that was created on the server.
	 * 
     * APIs tested: public void deleteGroup(String)
     * 
     * @param   void
     * @return  void
     */
    @After
    public void deleteGroup() {
    	// delete the test group
    	GroupUtils.deleteGroup(ndex, newGroup);
    } 

    /**
     * Create a group on the server, and delete it.
     * 
     * APIs tested: public Group createGroup(Group)
     *              public Group getGroup(String)
     */
    @Test
    public void test0001CreateGroup() {
    
    	// create new group
    	newGroup = GroupUtils.createGroup(ndex, group);
    	
    	// check the contents of the newly created  Group object
    	GroupUtils.compareGroupObjectsContents(group, newGroup);
    	
    	// now, get the newly created group using getGroup API
    	UUID groupId = newGroup.getExternalId();
    	Group createdGroup = GroupUtils.getGroup(ndex, groupId);
    	
    	// check the contents of the newly created  Group object
    	GroupUtils.compareGroupObjectsContents(group, createdGroup);
    }
    
    /**
     * Create a group on the server, and try to create it again.
     * DuplicateObjectException is expected.
     * 
     * APIs tested: public Group createGroup(Group)
     *            
     */
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    
    @Test
    public void test0002CreateDuplicateGroup() throws JsonProcessingException, IOException, NdexException {
    
    	// create new group
    	newGroup = GroupUtils.createGroup(ndex, group);
    	
    	// check the contents of the newly created  Group object
    	GroupUtils.compareGroupObjectsContents(group, newGroup);
    	
    	// expected exception is DuplicateObjectException
        thrown.expect(DuplicateObjectException.class);

    	// expected message of DuplicateObjectException
        thrown.expectMessage("Group with name " + groupName.toLowerCase() + " already exists.");
        
    	// create the same group again using ndex client
        // expect DuplicateObjectException because group already exists on the server 
    	UUID newGroupId = ndex.createGroup(group);
    	ndex.getGroup(newGroupId);
    	
    }
    
    /**
     * Try to create a group with the same name as user/account name. 
     * DuplicateObjectException is expected.
     * 
     * APIs tested: public Group createGroup(Group)
     *            
     */
    @Test
    public void test0003CreateGroupWithInvalidName() throws JsonProcessingException, IOException, NdexException {
    	
    	newGroup = new Group();

    	// set the group name to be user/account name
    	newGroup.setGroupName(accountName);
    	
    	// initialize other properties
    	newGroup.setDescription(group.getDescription());
    	newGroup.setImage(group.getImage());
    	newGroup.setGroupName(group.getGroupName());
    	newGroup.setWebsite(group.getWebsite());
    	
    	// expected exception is DuplicateObjectException
        thrown.expect(DuplicateObjectException.class);

    	// expected message of DuplicateObjectException
        thrown.expectMessage("Group with name " + accountName.toLowerCase() + " already exists.");
        
    	// try to create new group with the same name as user/account
        // exception is expected
    	ndex.createGroup(newGroup);
    }
    
    /**
     * Try to create a group with a bad account name (illegal character in name).
     * 
     * APIs tested: public Group createGroup(Group)
     *            
     */
    @Test
    public void test0004CreateGroupWithInvalidName() throws JsonProcessingException, IOException, NdexException {
    	
    	newGroup = new Group();
    	
    	// set the group name to be user/account name
    	newGroup.setGroupName("$!BadName!");
    	
    	// initialize other properties
    	newGroup.setDescription(group.getDescription());
    	newGroup.setImage(group.getImage());
    	newGroup.setGroupName(group.getGroupName());
    	newGroup.setWebsite(group.getWebsite());
    	
    	// expected exception is DuplicateObjectException
        thrown.expect(DuplicateObjectException.class);

    	// expected message of DuplicateObjectException
        // thrown.expectMessage("Group with name " + accountName.toLowerCase() + " already exists.");
        thrown.expectMessage("Is DuplicateObjectException correct exception for this test case? Discuss with Jing");
        
    	// try to create new group with the bad name
        // exception is expected
        Group groupWithBadName = GroupUtils.createGroup(ndex, newGroup);
        
        if (null != groupWithBadName) {
        	GroupUtils.deleteGroup(ndex, groupWithBadName);
        }
    }
    
    
    /**
     * Try to create a group with a non-existent user/account.
     * 
     * APIs tested: public Group createGroup(Group)
     *            
     */
    @Test
    public void test0005TryToCreateGroupWithNonExistentUser() throws JsonProcessingException, IOException, NdexException {
    	
    	// create new group
    	newGroup = GroupUtils.createGroup(ndex, group);
    	
    	// check the contents of the newly created  Group object
    	GroupUtils.compareGroupObjectsContents(group, newGroup);
    	
        Group newGroup1 = new Group();
        Group newGroup2 = null;

    	// initialize properties
    	newGroup1.setGroupName(group.getGroupName() + System.currentTimeMillis());
    	newGroup1.setDescription(group.getDescription());
    	newGroup1.setImage(group.getImage());
    	newGroup1.setGroupName(group.getGroupName());
    	newGroup1.setWebsite(group.getWebsite());
    	
    	// set user name to a random value
    	String userName = "RandomUserName" + System.currentTimeMillis();
    //	ndex.setCredentials(userName, accountPassword);
    	
    	// expected exception is ObjectNotFoundException
        thrown.expect(ObjectNotFoundException.class);
        
    	// expected message of ObjectNotFoundException
        // thrown.expectMessage("Group with name " + accountName.toLowerCase() + " already exists.");
        thrown.expectMessage("User " + userName.toLowerCase() + " not found.");

    	
    	try {
        	// since the user account doesn't exist on the server
        	// we expect authentication failure when trying to create the group
 //   		newGroup2 = ndex.createGroup(newGroup1);
    	} finally {	
    	    // set user name back to accountName
 //   	    ndex.setCredentials(accountName, accountPassword);
    	    assertNull(newGroup2);
    	}
    	
    }
    
    /**
     * Try to create a group with a non-existent user/account.
     * 
     * APIs tested: public List<Group> findGroups(SimpleUserQuery, int, int) 
     *            
     */
    //@Test
    public void test0006FindGroups() {
    	
    	//SimpleQuery simpleQuery = new SimpleQuery();
    	SimpleQuery simpleUserQuery = new SimpleQuery();
    	
    	//List<Group> listGroups = ndex.findGroups();
    	
    	/*
    	Group newGroup = new Group(), newGroup1 = null;

    	// initialize properties
    	newGroup.setAccountName("New"+group.getAccountName());
    	newGroup.setDescription(group.getDescription());
    	newGroup.setImage(group.getImage());
    	newGroup.setOrganizationName(group.getOrganizationName());
    	newGroup.setWebsite(group.getWebsite());
    	
    	// set user name to a random value
    	String userName = "RandomUserName" + System.currentTimeMillis();
    	ndex.setCredentials(userName, accountPassword);
    	
    	try {
        	// since the user account doesn't exist on the server
        	// we expect authentication failure when trying to create the group
    		newGroup1 = ndex.createGroup(newGroup);
    	} catch (Exception e) {
    		fail(e.getMessage());
    	} finally {	
    	    // set user name back to accountName
    	    ndex.setCredentials(accountName, accountPassword);
    	}
    	*/
    }    
    
}
