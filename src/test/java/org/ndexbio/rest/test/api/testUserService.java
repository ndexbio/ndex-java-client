package org.ndexbio.rest.test.api;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.ndexbio.model.object.NewUser;
import org.ndexbio.model.object.User;
import org.ndexbio.rest.client.NdexRestClient;
import org.ndexbio.rest.client.NdexRestClientModelAccessLayer;

import com.fasterxml.jackson.core.JsonProcessingException;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class testUserService {
	private static NdexRestClient client;
	private static NdexRestClientModelAccessLayer ndex;
	
	@BeforeClass
	public static void setUp() throws Exception {
//		client = new NdexRestClient("Support", "probably-insecure2"); //("dexterpratt", "insecure");
//		client = new NdexRestClient("cjtest", "guilan"); 
			
/*		client = new NdexRestClient("cjtest", "1234", 
				"http://localhost:8080/ndexbio-rest",
				"http://localhost:8080/AuthenticationService/AuthenticationService", AuthenticationType.SAML); */ 
		
		client = new NdexRestClient("vrynkov", 
				                    "aaa", 
				                    "http://localhost:8080/ndexbio-rest");
		
		ndex = new NdexRestClientModelAccessLayer(client);
		
	}

	@AfterClass
	public static void tearDown() throws Exception {
	}
	
	
	@Test
	public void test001CreateUser() {
		
		String userName = "tester";
		String password = "password";

		NewUser user = new NewUser();
		User userCreated = null;
		
		user.setAccountName(userName);
		user.setDescription("This user is used by JUnit tests");
		user.setEmailAddress("test@xxxxxxxxx.com");
		user.setFirstName("TesterFirstName");
		user.setImage("http://www.yahoo.com");
		user.setLastName("TesterLastName");
		user.setPassword(password);
		user.setWebsite("http://www.yahoo.com/finance");
			
		try {
			 userCreated = ndex.createUser(user);
		} catch (JsonProcessingException e) {
			fail("JsonProcessingException:" + e.getMessage());
		} catch (IOException e) {
			fail("IOException:" + e.getMessage());
		} catch (Exception e) {
		    fail("Exception:" + e.getMessage());
	    }

/*
		NdexRestClient client1 = new NdexRestClient(userName, 
				                                    password, 
				                                    "http://localhost:8080/ndexbio-rest");
		NdexRestClientModelAccessLayer ndex1 = new NdexRestClientModelAccessLayer(client1);
		
		try {
			ndex1.deleteUser(userName);
		} catch (JsonProcessingException e) {
			fail("JsonProcessingException:" + e.getMessage());
		} catch (IOException e) {
			fail("IOException:" + e.getMessage());
		} catch (Exception e) {
		    fail("Exception:" + e.getMessage());
	    }
	
		//System.out.println("userCreated = " + userCreated);	
*/
	}
	
	@Test
	public void test002GetUser() {
		fail("Not yet implemented");
	}
	
}
