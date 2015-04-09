package org.ndexbio.rest.test.api;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.ndexbio.model.object.NewUser;
import org.ndexbio.model.object.User;
import org.ndexbio.rest.client.NdexRestClient;
import org.ndexbio.rest.client.NdexRestClientModelAccessLayer;

/*
 * This class contains JUNit tests for UserService.java APIs located in 
 * src/main/java/org.ndexbio.rest.services package of ndexbio-rest module.
 */


// The @FixMethodOrder(MethodSorters.NAME_ASCENDING) annotation sorts (and 
// executes) the test methods by name in lexicographic order
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class testUserService 
{
	private static NdexRestClient                 client;
	private static NdexRestClientModelAccessLayer ndex;
	
	// the testerName account should exist on the server prior to testing
	private static String testerName     = "vrynkov";
	private static String testerPassword = "aaa";
	
	// userToCreate is the user that will be created on the NDEx server as part of testing
	// the userToCreate has name "tester1" and password "aaa" 
	// prior to testing, this account should not exist on this server
	private static NewUser userToCreate  = null;
	private static String  userName      = "tester1";
	private static String  password      = "aaa";
	
	// URL of the test server
    private static String  testServerURL = "http://localhost:8080/ndexbio-rest";
	
 
	@BeforeClass
	public static void setUp() throws Exception {
		client = new NdexRestClient(testerName, testerPassword, testServerURL);
		
		ndex = new NdexRestClientModelAccessLayer(client);
		  
		// create and initialize user for testing
        userToCreate = new NewUser();
        
        userToCreate.setAccountName(userName);
        userToCreate.setDescription("This user is used by JUnit tests");
        userToCreate.setEmailAddress("tester1@xxxxxx.com");
		userToCreate.setFirstName("FirstName");
		userToCreate.setImage("http://www.yahoo.com");
		userToCreate.setLastName("LastName");
		userToCreate.setPassword(password);
		userToCreate.setWebsite("http://www.yahoo.com/finance");		  
	}

	@AfterClass
	public static void tearDown() throws Exception {
	}
	

	/*
	 * Create a user account on the server.
	 * Assumption: user account doesn't exist on the server.
	 */
	@Test
	public void test0001CreateUser() {
		User userCreated = null;	
		
		try {
            userCreated = ndex.createUser(userToCreate);
		} catch (Exception e) {
            fail("Unable to create user '" + userToCreate.getAccountName() + "' : " + e.getMessage());
		}

		assertEquals("account names do not match: ",   userCreated.getAccountName(),  userToCreate.getAccountName());
        assertEquals("descriptions do not match: ",    userCreated.getDescription(),  userToCreate.getDescription());
        assertEquals("email addresses do not match: ", userCreated.getEmailAddress(), userToCreate.getEmailAddress());
        assertEquals("first names do not match: ",     userCreated.getFirstName(),    userToCreate.getFirstName());
        assertEquals("last names do not match: ",      userCreated.getLastName(),     userToCreate.getLastName());
        assertEquals("image URLs do not match: ",      userCreated.getImage(),        userToCreate.getImage());
        assertEquals("web sites do not match: ",       userCreated.getWebsite(),      userToCreate.getWebsite());
	}
	
	/*
	 * Create a user account on the server.
	 * Assumption: user account already exists on the server; we expect exception to be thrown.
	 */
	@Test
	public void test0002CreateUser() {
		User userCreated = null;	
		
		try {
			 userCreated = ndex.createUser(userToCreate);
		} catch (Exception e) {
			assertEquals(e.getMessage(), e.getMessage(), "Server returned HTTP response code: 409 for URL: http://localhost:8080/ndexbio-rest/user");
		}
		
		assertNull("succeeded to create account '" + userToCreate.getAccountName() + "' that already existed ", userCreated);
	}
	
	/*
	 * Delete user account from the server.  The user account to be deleted is the one
	 * currently authenticated (self).
     * Assumption: user account exists on the server and the delete command is sent with this user's credentials.
	 */	
	@Test
	public void test9000DeleteUser() {
		NdexRestClient client1 = new NdexRestClient(userName, password, testServerURL);
		
        NdexRestClientModelAccessLayer ndex1 = new NdexRestClientModelAccessLayer(client1);

        try {
            ndex1.deleteUser();
        } catch (Exception e) {
			fail("Unable to delete user '" + userToCreate.getAccountName() + "' : " + e.getMessage());
        }
	}
	
	/*
	 * Delete user account from the server.  
     * Assumption: the user account to be deleted doesn't exist on the server and the delete command is sent 
     * with this user's name.
     * We expect exception to be thrown.
	 */	
	@Test
	public void test9001DeleteUser() {
		NdexRestClient client1 = new NdexRestClient(userName, password, testServerURL);
		
        NdexRestClientModelAccessLayer ndex1 = new NdexRestClientModelAccessLayer(client1);

        try {
            ndex1.deleteUser();
        } catch (Exception e) {
        	assertEquals(e.getMessage(), e.getMessage(), "Server returned HTTP response code: 401 for URL: http://localhost:8080/ndexbio-rest/user");
        }
	}
	
	
}
