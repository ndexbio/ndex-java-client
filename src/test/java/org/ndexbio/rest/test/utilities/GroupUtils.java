package org.ndexbio.rest.test.utilities;

import org.ndexbio.model.exceptions.NdexException;
import org.ndexbio.model.object.Group;
import org.ndexbio.rest.client.NdexRestClientModelAccessLayer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertTrue;

public class GroupUtils {

	public static Group createGroup(NdexRestClientModelAccessLayer ndex, Group group) {
        Group newGroup = null;
		try {
			newGroup = ndex.createGroup(group);
		} catch (NdexException e) {
			fail("Unable to create group : " + e.getMessage());
        } catch (Exception e) {
        	fail("Unable to create group : " + e.getMessage());
        } 
		return newGroup;
	}
	
	public static void deleteGroup(NdexRestClientModelAccessLayer ndex, Group group) {
		try {
			ndex.deleteGroup(group.getExternalId().toString());
        } catch (Exception e) {
        	fail("Unable to delete group : " + e.getMessage());
        }		
	}
	public static void compareGroupObjectsContents(Group group, Group newGroup) {
		
		assertTrue("group names do not match: ", group.getAccountName().equalsIgnoreCase(newGroup.getAccountName()));
		
		assertEquals("expect group name not in lower-case : ",   group.getAccountName().toLowerCase(),  newGroup.getAccountName());
		
		assertEquals("descriptions do not match: ",    group.getDescription(),      group.getDescription());
		assertEquals("image URLs do not match: ",      group.getImage(),            newGroup.getImage());
		assertEquals("organizations  do not match: ",  group.getOrganizationName(), newGroup.getOrganizationName());
		assertEquals("web sites do not match: ",       group.getWebsite(),          newGroup.getWebsite());	        
	//	assertEquals("types do not match: ",           group.getType(),             newGroup.getType());
	
		assertNotEquals("external IDs are same",       group.getExternalId(),       newGroup.getExternalId());
		
		return;
	}

	public static Group getGroup(
			NdexRestClientModelAccessLayer ndex, String groupId) {
		Group group = null;
		try {
			group = ndex.getGroup(groupId);
        } catch (Exception e) {
        	fail("Unable to get group " + groupId + " : " +  e.getMessage());
        }	
		
		return group;
	}

}
