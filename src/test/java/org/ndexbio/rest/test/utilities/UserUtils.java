package org.ndexbio.rest.test.utilities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.ndexbio.model.object.NewUser;
import org.ndexbio.model.object.User;
import org.ndexbio.rest.client.NdexRestClientModelAccessLayer;

public class UserUtils {

	public static void deleteUser(NdexRestClientModelAccessLayer ndex) {
        try {
            ndex.deleteUser();
        } catch (Exception e) {
        	// ignore this exception -- the account we try to delete may not exist
        	//fail("Unable to delete user account: " + e.getMessage());
        }
	}
	
	public static void deleteUser(NdexRestClientModelAccessLayer ndex, String user, String password) {
        String previousUserName = ndex.getUserName();
        String previousPassword = ndex.getPassword();
        		
		ndex.setCredential(user, password);
        try {
            ndex.deleteUser();
        } catch (Exception e) {
        	// ignore this exception -- the account we try to delete may not exist
        	// fail("Unable to delete user account: " + e.getMessage());
        } finally {
    	    ndex.setCredential(previousUserName, previousPassword);
        }
	}

	public static NewUser getNewUser(String accountName,
			                         String accountPassword, String description, String email,
			                         String firstName, String lastName, String image, String webSite) {
	        
        NewUser user = new NewUser();

        user.setAccountName(accountName);
        user.setPassword(accountPassword);
        user.setDescription(description);
        user.setEmailAddress(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setImage(image);
        user.setWebsite(webSite);
        
		return user;
	}
	
	public static void compareObjectsContents(User user1, NewUser user2) {
		
        assertEquals("account names do not match: ",   user1.getAccountName(),  user2.getAccountName());
        assertEquals("descriptions do not match: ",    user1.getDescription(),  user2.getDescription());
        assertEquals("email addresses do not match: ", user1.getEmailAddress(), user2.getEmailAddress());
        assertEquals("first names do not match: ",     user1.getFirstName(),    user2.getFirstName());
        assertEquals("last names do not match: ",      user1.getLastName(),     user2.getLastName());
        assertEquals("image URLs do not match: ",      user1.getImage(),        user2.getImage());
        assertEquals("web sites do not match: ",       user1.getWebsite(),      user2.getWebsite());
        
        return;
	}
	
	public static void compareObjectsContents(User user1, User user2) {
		
        assertEquals("account names do not match: ",   user1.getAccountName(),  user2.getAccountName());
        assertEquals("descriptions do not match: ",    user1.getDescription(),  user2.getDescription());
        assertEquals("disk quotas do not match: ",     user1.getDiskQuota(),    user2.getDiskQuota());
        assertEquals("disk used do not match: ",       user1.getDiskUsed(),     user2.getDiskUsed());
        assertEquals("email addresses do not match: ", user1.getEmailAddress(), user2.getEmailAddress());
        assertEquals("external IDs do not match: ",    user1.getExternalId(),   user2.getExternalId());        
        assertEquals("first names do not match: ",     user1.getFirstName(),    user2.getFirstName());
        assertEquals("last names do not match: ",      user1.getLastName(),     user2.getLastName());
        assertEquals("types do not match: ",           user1.getType(),         user2.getType());
        assertEquals("image URLs do not match: ",      user1.getImage(),        user2.getImage());
        assertEquals("web sites do not match: ",       user1.getWebsite(),      user2.getWebsite());
        
        return;
	}
	
	public static User createUserAccount(
        NdexRestClientModelAccessLayer ndex, NewUser user) {

        User newUser = null;
        
	    try {
	    	newUser = ndex.createUser(user);
	    } catch (Exception e) {
			fail("Unable to create user account: " + e.getMessage());
	    }        
	    return newUser;
	}
}
