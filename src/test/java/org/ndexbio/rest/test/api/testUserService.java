package org.ndexbio.rest.test.api;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.ws.rs.Encoded;
import javax.ws.rs.PathParam;

import org.jboss.resteasy.spi.UnauthorizedException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runners.MethodSorters;
import org.ndexbio.common.models.dao.orientdb.CommonDAOValues;
import org.ndexbio.model.errorcodes.NDExError;
import org.ndexbio.model.exceptions.DuplicateObjectException;
import org.ndexbio.model.exceptions.NdexException;
import org.ndexbio.model.exceptions.ObjectNotFoundException;
import org.ndexbio.model.exceptions.UnauthorizedOperationException;
import org.ndexbio.model.object.Account;
import org.ndexbio.model.object.NewUser;
import org.ndexbio.model.object.User;
import org.ndexbio.rest.client.NdexRestClient;
import org.ndexbio.rest.client.NdexRestClientModelAccessLayer;
import org.ndexbio.rest.test.utilities.NetworkUtils;
import org.ndexbio.rest.test.utilities.PropertyFileUtils;
import org.ndexbio.rest.test.utilities.UserUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 *  This class contains JUNit tests for testing UserService APIs from the 
 *  UserService.java class located in src/main/java/org.ndexbio.rest.services package of ndexbio-rest module.
 *
 *  APIs tested in this class:
 *  
 *  √   1) public User authenticateUser(String, String) accountName,
 *      2) public User authenticateUserNoOp()
 *      3) public void changePassword(String password)
 *  √   4) public User createUser(final NewUser newUser)
 *  √   5) public void deleteUser()
 *      6) public Response emailNewPassword( final String accountName)
 *      7) public List<User> findUsers(SimpleUserQuery, int, int)
 *      8) public Membership getMembership((String, String, int)
 *      9) public List<Request> getPendingRequests(String, int, int)
 *     10) public List<Request> getSentRequest(String, int, int)
 *     11) public List<Task> getTasks((String, int, int)
 *     12) public List<Task> getTasks_aux(String, int, int)
 *  √  13) public User getUser(String)
 *     14) public List<Membership> getUserGroupMemberships(String, String, int, int)
 *     15) public List<Membership> getUserNetworkMemberships(Strring, String, int, int)
 *  √  16) public User updateUser(String, User)
 *  
 */


// The @FixMethodOrder(MethodSorters.NAME_ASCENDING) annotation sorts (and
// executes) the test methods by name in lexicographic order
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class testUserService
{
    private static NdexRestClient                 client;
    private static NdexRestClientModelAccessLayer ndex;
    
    //static private JUnitTestProperties configProperties = null;
    
    
    private static String accountName     = "aaa";
    private static String accountPassword = "aaa";
    
    //private static User   testAccount     = null;
    
   
    // userToCreate is the user that will be created on the NDEx server as part of testing
    // prior to testing, this account should not exist on this server
    private static NewUser userToCreate  = null;
    
    // this user is returned by a successful call to ndex.createUser(userToCreate) method
    //private static User userCreated = null;

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
        userToCreate = UserUtils.getNewUser(
				accountName,
				accountPassword,
		        "This account is used for testing UserService APIs",  // description
		        "user_service_apis@xxxxxx.com",                    // email address
		        "User",                                            // first name -- name of the test
		        "Service APIs",                                    // last name -- name of the test		        
		        "http://www.yahoo.com",                            // image
		        "http://www.yahoo.com/finance");                   // web-site
        
		// create ndex client and a test user account
        try {
            client = new NdexRestClient(accountName, accountPassword, JUnitTestSuite.testServerURL);
            ndex   = new NdexRestClientModelAccessLayer(client);
        } catch (Exception e) {
        	fail("Unable to create ndex client: " + e.getMessage());
        }
        
        // in case user account exists, delete it
    	UserUtils.deleteUser(ndex);
    }

    /**
     * Clean-up method.  
     * 
     * @throws  Exception
     * @param   void
     * @return  void
     */
    @AfterClass
    public static void tearDown() throws Exception {
    }    

    /**
     * Create a user account on the server.
     * Assumption: user account doesn't exist on the server.
     * 
     * API tested: public User createUser(final NewUser newUser)
     */
    @Test
    public void test0001CreateUser() {
        User userCreated = null, userCreated1 = null;

        try {
            userCreated = ndex.createUser(userToCreate);
        } catch (Exception e) {
            fail("Unable to create user account '" + userToCreate.getAccountName() + "' : " + e.getMessage());
        }    
        assertNotNull("Unable to create user account'" + userToCreate.getAccountName() + "'", userCreated);
        UserUtils.compareObjectsContents(userCreated, userToCreate);
  
        
        try {
        	// create the same account once again -- we expect to receive DuplicateObjectException
            userCreated1 = ndex.createUser(userToCreate);
        } catch (DuplicateObjectException e) {
        	assertEquals("wrong message received: ", e.getNDExError().getMessage(), CommonDAOValues.DUPLICATED_ACCOUNT_FLAG);
        	//System.out.println("e.getNDExError().getMessage()     = " + e.getNDExError().getMessage());
            //System.out.println("e.getNDExError().getDescription() = " + e.getNDExError().getDescription());
        	//System.out.println("e.getNDExError().getThreadId()    = " + e.getNDExError().getThreadId());
        	//System.out.println("e.getNDExError().getTimeStamp()   = " + e.getNDExError().getTimeStamp());
        	//System.out.println("e.getNDExError().getErrorCode()   = " + e.getNDExError().getErrorCode());        	
        	//System.out.println("e.getNDExError().getStackTrace()  = " + e.getNDExError().getStackTrace());
        }
        catch (Exception e) {
            fail("Unable to create user '" + userToCreate.getAccountName() + "' : " + e.getMessage());
        }
        // userCreated1 should be null
        assertNull("succeeded in creation of duplicate account '" + userToCreate.getAccountName() + "'", userCreated1);
    }

    /**
     * Authenticate a user account on the server.
     * 
     * API tested: public User authenticateUser(String, String)
     */
    @Test
    public void test0010AuthenticateUser() { 
    	User   user = null;
    	String nonExistentUser     = UUID.randomUUID().toString();   // random string to be used as user name
    	String nonExistentPassword = UUID.randomUUID().toString();   // random string to be used as password
    	
    	// try to authenticate an existent user with the valid password
    	try {
    		user = null;
    	    user = ndex.authenticateUser(accountName, accountPassword);
    	} catch (UnauthorizedOperationException e) {
    		fail("Unable to authenticate user '" + accountName + "': " + e.getNDExError().getMessage());
    	} catch (Exception e) {
    		fail("Unable to authenticate user '" + accountName + "': " + e.getMessage());
    	}
    	assertNotNull("Unable to retrieve valid user '" + accountName + 
    			"' after authenticating with valid password '" + accountPassword + "'", user);
        UserUtils.compareObjectsContents(user, userToCreate);
        
        
        // try to authenticate the same (valid) user with a randomly-generated string for password;
        // UnauthorizedOperationException is expected 
    	try {
    		user = null;
    	    user = ndex.authenticateUser(accountName, nonExistentPassword);
    	} catch (UnauthorizedOperationException e) {
    		assertEquals("wrong message received: ", e.getNDExError().getMessage(), "Invalid accountName or password.");
    	} catch (Exception e) {
    		fail("Unable to authenticate user '" + accountName + "': " + e.getMessage());
    	}
    	assertNull("Retrieved user '" + accountName + "' using invalid password '" + nonExistentPassword + "'", user);
    	
    	
        // try to authenticate a non-existent user with a valid (good) password;
        // UnauthorizedOperationException is expected 
    	try {
    		user = null;
    	    user = ndex.authenticateUser(nonExistentUser, accountPassword);
    	} catch (UnauthorizedOperationException e) {
    		assertEquals("wrong message received: ", e.getNDExError().getMessage(), "User not found.");
    	} catch (Exception e) {
    		fail("Unable to authenticate user '" + accountName + "': " + e.getMessage());
    	}
    	assertNull("Retrieved non-existant user '" + nonExistentUser + "' using valid password '" + accountPassword + "'", user);
    	
    	
        // try to authenticate a non-existent user with a a non-existent password;
        // UnauthorizedOperationException is expected 
    	try {
    		user = null;
    	    user = ndex.authenticateUser(nonExistentUser, nonExistentPassword);
    	} catch (UnauthorizedOperationException e) {
    		assertEquals("wrong message received: ", e.getNDExError().getMessage(), "User not found.");
    	} catch (Exception e) {
    		fail("Unable to authenticate user '" + accountName + "': " + e.getMessage());
    	}
    	assertNull("Retrieved non-existant user '" + nonExistentUser + "' using invalid password '" + nonExistentPassword + "'", user); 	
    }
    
    
    /**
     * Try to get non-existent user from the server.
     * 
     * API tested: public User getUser(String)
     */
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    
    @Test
    public void test0020GetUser() throws IOException, NdexException {
        String nonExistentUser = UUID.randomUUID().toString(); 
        
    	// ObjectNotFoundException is expected 
        thrown.expect(ObjectNotFoundException.class);
        
        // try to get a non-existent user -- should throw ObjectNotFoundException
		User user = ndex.getUser(nonExistentUser);

        assertNull("Retrieved non-existent user '" + nonExistentUser + "' by name. ", user);
        fail("execution should never get to this point!");
    }
    
    /**
     * Try to get non-existent user from the server.
     * 
     * API tested: public User getUser(String)
     */
    @Test
    public void test0021GetUser() throws IOException, NdexException {
        String nonExistentUser = "xyz";  
        
    	// ObjectNotFoundException is expected 
        thrown.expect(ObjectNotFoundException.class);
        
        // try to get a non-existent user -- should throw ObjectNotFoundException
		User user = ndex.getUser(nonExistentUser);

        assertNull("Retrieved non-existent user '" + nonExistentUser + "' by name. ", user);
        fail("execution should never get to this point!");
    }    
    
    
    /**
     * Get existing user by account name and by UUID.
     * 
     * APIs tested: public User createUser(NewUser)
     *              public User getUser(String)
     *              public void deleteUser()
     */
    @Test
    public void test0022GetUser()  {
    	User   newUser1 = null; 
    	User   newUser2 = null;
    	User   newUser3 = null;    	
    	String account  = "bbb"; //UUID.randomUUID().toString(); ///"bbb";
    	String password = "bbb";
    	
    	
    	// create user object; the properties describe the current test set-up
        NewUser newUserToCreate = UserUtils.getNewUser(
				account,
				password,
		        "New test account",              // description
		        account+"@xxxxxx.com",           // email address
		        "FirstName",                     // first name -- name of the test
		        "LastName",                      // last name -- name of the test		        
		        "http://www.yahoo.com",          // image
		        "http://www.yahoo.com/finance"); // web-site
    	
        // delete the test user account
    	UserUtils.deleteUser(ndex, account, password);
    	
        // create a new user account
        try {
            newUser1 = ndex.createUser(newUserToCreate);
        } catch (NdexException e) {
            fail("Unable to create user account '" + newUserToCreate.getAccountName() + "' : " + e.getNDExError().getMessage());
        } catch (Exception e) {
            fail("Unable to create user account '" + newUserToCreate.getAccountName() + "' : " + e.getMessage());
        }    
        assertNotNull("Unable to create user account'" + newUserToCreate.getAccountName() + "'", newUser1);
        UserUtils.compareObjectsContents(newUser1, newUserToCreate);


        // get the newly created account by name
        try {
        	newUser2 = ndex.getUser(newUser1.getAccountName());
        } catch (ObjectNotFoundException e) {
        	fail("Unable to get user '" + newUser1.getAccountName() + "' by name: " + e.getNDExError().getMessage());
        } catch (Exception e) {
        	fail("Unable to get user '" + newUser1.getAccountName() + "' by name: " + e.getMessage());
        }
        assertNotNull("Unable to get user '" + newUser1.getAccountName() + "' by name ", newUser2);
        UserUtils.compareObjectsContents(newUser1, newUser2);
        

        // get the newly created account by UUID
        try {
        	newUser3 = ndex.getUser(newUser1.getExternalId().toString());
        } catch (Exception e) {
        	fail("Unable to get user '" + newUser1.getAccountName() + "' by name UUID: " + e.getMessage());
        }
        assertNotNull("Unable to get user '" + newUser1.getAccountName() + "' by UUID ", newUser3);
        UserUtils.compareObjectsContents(newUser2, newUser3);


    	// delete the test user account
    	UserUtils.deleteUser(ndex, account, password);
    }

    
    /**
     * Update User information in database: Description, First Name, Last Name, Image and Web-Site.
     * 
     * APIs tested: public User createUser(NewUser)
     *              public User getUser(String)
     *              public void deleteUser()
     *              public User updateUser(String, User)
     */
    @Test
    public void test0023UserProfile()  {
    	User   newUser1 = null; 
    	User   newUser2 = null;  	
    	String account  = "ccc";
    	String password = "ccc";
    	String postFix  = " -- updated";
    	
    	
        // try to get non-existent user
        try {
            newUser1 = ndex.getUser(account);
    	} catch (ObjectNotFoundException e) {
    		assertEquals("wrong message received: ", e.getNDExError().getMessage(), "User not found.");
    	} catch (Exception e) {
    		fail("Unable to authenticate user '" + accountName + "': " + e.getMessage());
    	}
    	assertNull("Retrieved non-existent user '" + account + "' using valid password '" + password + "'", newUser1);


        // delete test account in case it exists on the server
    	UserUtils.deleteUser(ndex, account, password);

    	// create user object; the properties describe the current test set-up
        NewUser newUserToCreate = UserUtils.getNewUser(
				account,
				password,
		        "New test account",              // description
		        account+"@xxxxxx.com",           // email address
		        "FirstName",                     // first name -- name of the test
		        "LastName",                      // last name -- name of the test		        
		        "http://www.yahoo.com",          // image
		        "http://www.yahoo.com/finance"); // web-site

        // create a new user account
        try {
            newUser1 = ndex.createUser(newUserToCreate);
        } catch (NdexException e) {
            fail("Unable to create user account '" + newUserToCreate.getAccountName() + "' : " + e.getNDExError().getMessage());
        } catch (Exception e) {
            fail("Unable to create user account '" + newUserToCreate.getAccountName() + "' : " + e.getMessage());
        }    
        assertNotNull("Unable to create user account'" + newUserToCreate.getAccountName() + "'", newUser1);
        UserUtils.compareObjectsContents(newUser1, newUserToCreate);


        // get the newly created account by name
        try {
        	newUser2 = ndex.getUser(newUser1.getAccountName());
        } catch (ObjectNotFoundException e) {
        	fail("Unable to get user '" + newUser1.getAccountName() + "' by name: " + e.getNDExError().getMessage());
        } catch (Exception e) {
        	fail("Unable to get user '" + newUser1.getAccountName() + "' by name: " + e.getMessage());
        }
        assertNotNull("Unable to get user '" + newUser1.getAccountName() + "' by name ", newUser2);
        UserUtils.compareObjectsContents(newUser1, newUser2);
        

        // the properties we can modify and persist to the database are Description,
        // First Name, Last Name, Image and Web Site.  Let's try to update these properties.

        newUser1.setDescription(newUser1.getDescription() + postFix);
        newUser1.setFirstName(newUser1.getFirstName() + postFix);
        newUser1.setLastName(newUser1.getLastName() + postFix);
        newUser1.setImage(newUser1.getImage() + postFix);
        newUser1.setWebsite(newUser1.getWebsite() + postFix);        
        
        // update user information
        try {
        	newUser2 = null;
    	    ndex.setCredential(account, password);
        	newUser2 = ndex.updateUser(newUser1);
        } catch (ObjectNotFoundException e) {
        	fail("Unable to get user '" + newUser1.getAccountName() + "' by name: " + e.getNDExError().getMessage());
        } catch (Exception e) {
        	fail("Unable to get user '" + newUser1.getAccountName() + "' by name: " + e.getMessage());
        } finally {
        	 ndex.setCredential(accountName, accountPassword);
        }
        assertNotNull("Unable to get user '" + newUser1.getAccountName() + "' by name ", newUser2);
        UserUtils.compareObjectsContents(newUser1, newUser2);
        
  
        // update profile again
        newUser1.setDescription(newUser1.getDescription().replaceAll(postFix, ""));
        newUser1.setFirstName(newUser1.getFirstName().replaceAll(postFix, ""));
        newUser1.setLastName(newUser1.getLastName().replaceAll(postFix, ""));
        newUser1.setImage(newUser1.getImage().replaceAll(postFix, ""));
        newUser1.setWebsite(newUser1.getWebsite().replaceAll(postFix, ""));
        try {
        	newUser2 = null;
    	    ndex.setCredential(account, password);
        	newUser2 = ndex.updateUser(newUser1);
        } catch (ObjectNotFoundException e) {
        	fail("Unable to get user '" + newUser1.getAccountName() + "' by name: " + e.getNDExError().getMessage());
        } catch (Exception e) {
        	fail("Unable to get user '" + newUser1.getAccountName() + "' by name: " + e.getMessage());
        } finally {
        	 ndex.setCredential(accountName, accountPassword);
        }
        assertNotNull("Unable to get user '" + newUser1.getAccountName() + "' by name ", newUser2);
        UserUtils.compareObjectsContents(newUser1, newUser2);        


    	// delete the test user account
    	UserUtils.deleteUser(ndex, account, password);
    }

    
    /**
     *  Try to update User information that cannot be changed in the database once User object is persisted:
     *    User Name, User UUID, Email Address, Disk Quota, Disk Used and Type.
     * 
     * APIs tested: public User createUser(NewUser)
     *              public User getUser(String)
     *              public void deleteUser()
     *              public User updateUser(String, User)
     */
    @Test
    public void test0024UserProfile()  {
        fail("not yet implemented");
        
        // the properties below can be updated in memory, but the changes will not persist in the database:
        //newUser1.setAccountName(newAccountName);
        //newUser1.setDiskQuota(newUser1.getDiskQuota()+10);
        //newUser1.setDiskUsed(newUser1.getDiskUsed()+10);
        //newUser1.setEmailAddress("newEmailAddress@xxxxxx.com");
        //newUser1.setExternalId(UUID.randomUUID());
        //newUser1.setType(newUser1.getType() + " -- updated");
    }
    

    /**
     * Delete user account from the server.  
     * 
     * API tested: public User getUser(String)
     */
    @Test
    public void test9000DeleteUser() {
        try {
            ndex.deleteUser();
        } catch (Exception e) {
            fail("Unable to delete user '" + userToCreate.getAccountName() + "' : " + e.getMessage());
        }
        
        try {
            ndex.deleteUser();
        } catch (IOException e) {
        	// TODO: this should be changed from IOException to NotAuthorizedException
        	assertTrue("wrong message received: ", e.getMessage().startsWith("Server returned HTTP response code: 401"));	
        } catch (Exception e) {
            fail("Unable to delete user '" + userToCreate.getAccountName() + "' : " + e.getMessage());
        }
    }
}

        