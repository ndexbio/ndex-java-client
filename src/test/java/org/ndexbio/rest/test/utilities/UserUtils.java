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
package org.ndexbio.rest.test.utilities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.UUID;

import org.ndexbio.model.object.NewUser;
import org.ndexbio.model.object.User;
import org.ndexbio.rest.client.NdexRestClientModelAccessLayer;

public class UserUtils {

	/*public static void deleteUser(NdexRestClientModelAccessLayer ndex) {
        try {
            ndex.deleteUser();
        } catch (Exception e) {
        	// ignore this exception -- the account we try to delete may not exist
        	//fail("Unable to delete user account: " + e.getMessage());
        }
	}*/
	
	public static void deleteUser(NdexRestClientModelAccessLayer ndex, String user, String password) {
     /*   String previousUserName = ndex.getUserName();
        String previousPassword = ndex.getPassword();
        		
		ndex.setCredentials(user, password);
        try {
            ndex.deleteUser();
        } catch (Exception e) {
        	// ignore this exception -- the account we try to delete may not exist
        	// fail("Unable to delete user account: " + e.getMessage());
        } finally {
    	    ndex.setCredentials(previousUserName, previousPassword);
        } */
	}

	public static User getNewUser(String accountName,
			                         String accountPassword, String description, String email,
			                         String firstName, String lastName, String image, String webSite) {
	        
        User user = new User();

        user.setUserName(accountName);
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
		
        assertEquals("account names do not match: ",   user1.getUserName(),  user2.getAccountName());
        assertEquals("descriptions do not match: ",    user1.getDescription(),  user2.getDescription());
        assertEquals("email addresses do not match: ", user1.getEmailAddress(), user2.getEmailAddress());
        assertEquals("first names do not match: ",     user1.getFirstName(),    user2.getFirstName());
        assertEquals("last names do not match: ",      user1.getLastName(),     user2.getLastName());
        assertEquals("image URLs do not match: ",      user1.getImage(),        user2.getImage());
        assertEquals("web sites do not match: ",       user1.getWebsite(),      user2.getWebsite());
        
        return;
	}
	
	public static void compareObjectsContents(User user1, User user2) {
		
        assertEquals("account names do not match: ",   user1.getUserName(),  user2.getUserName());
        assertEquals("descriptions do not match: ",    user1.getDescription(),  user2.getDescription());
        assertEquals("disk quotas do not match: ",     user1.getDiskQuota(),    user2.getDiskQuota());
        assertEquals("disk used do not match: ",       user1.getDiskUsed(),     user2.getDiskUsed());
        assertEquals("email addresses do not match: ", user1.getEmailAddress(), user2.getEmailAddress());
        assertEquals("external IDs do not match: ",    user1.getExternalId(),   user2.getExternalId());        
        assertEquals("first names do not match: ",     user1.getFirstName(),    user2.getFirstName());
        assertEquals("last names do not match: ",      user1.getLastName(),     user2.getLastName());
  //      assertEquals("types do not match: ",           user1.getType(),         user2.getType());
        assertEquals("image URLs do not match: ",      user1.getImage(),        user2.getImage());
        assertEquals("web sites do not match: ",       user1.getWebsite(),      user2.getWebsite());
        
        return;
	}
	
/*	public static UUID createUserAccount (NdexRestClientModelAccessLayer ndex, NewUser user) {
	    try {
	    	return ndex.createUser(user);
	    } catch (Exception e) {
			fail("Unable to create user account: " + e.getMessage());
	    } 
	    return null;
	} */
}
