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
		
		assertTrue("group names do not match: ", group.getGroupName().equalsIgnoreCase(newGroup.getGroupName()));
		
		assertEquals("expect group name not in lower-case : ",   group.getGroupName().toLowerCase(),  newGroup.getGroupName());
		
		assertEquals("descriptions do not match: ",    group.getDescription(),      group.getDescription());
		assertEquals("image URLs do not match: ",      group.getImage(),            newGroup.getImage());
		assertEquals("organizations  do not match: ",  group.getGroupName(), newGroup.getGroupName());
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
