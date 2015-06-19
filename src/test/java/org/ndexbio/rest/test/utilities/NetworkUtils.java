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
package org.ndexbio.rest.test.utilities;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.ndexbio.model.object.Permissions;
import org.ndexbio.model.object.Status;
import org.ndexbio.model.object.Task;
import org.ndexbio.model.object.User;
import org.ndexbio.model.object.network.NetworkSummary;
import org.ndexbio.rest.client.NdexRestClientModelAccessLayer;
import org.ndexbio.rest.test.api.JUnitTestSuite;

public class NetworkUtils {
	
    public static void deleteNetworks(
    		NdexRestClientModelAccessLayer ndex, String accountName, 
    		Map<String, String> networksToUpload) {
    	
    	// list of all networks from the test account 
    	List<NetworkSummary> allNetworks = null;
    	
    	int count = (networksToUpload != null) ? networksToUpload.size() : 0;
    	
		try {
			allNetworks =
			    ndex.findNetworks("*", true,  accountName, Permissions.ADMIN, false, 0, count);
		} catch (IOException e) {
			e.printStackTrace();
		}
	
		if (allNetworks == null) {
			return;
		}
		
        for (NetworkSummary network : allNetworks) {
            String networkUUIDToDelete = network.getExternalId().toString();	  
            try {
                ndex.deleteNetwork(networkUUIDToDelete);
            } catch (Exception e) {
            	// here we may get the
            	// "com.fasterxml.jackson.databind.JsonMappingException: No content to map due to end-of-input"
            	// ignore it an keep deleting networks
            	continue;
            }  	    	
        }
    }
    
    public static  Map<String,String> startNetworksUpload(NdexRestClientModelAccessLayer ndex, Map<String, String> networksToUpload) {
    	
    	if (null == networksToUpload) {
    		return null;
    	}
    	
    	Map<String, String> map = new HashMap<String, String>();
    	
    	File f = null;
    	
		for (Map.Entry<String, String> entry : networksToUpload.entrySet()) {
			try {
        	    f = new File(entry.getValue());
                
        	    ndex.uploadNetwork(entry.getValue());
        	
        	    // put name:size to map
        	    map.put(f.getName(), NumberFormat.getNumberInstance(Locale.US).format(f.length()));
        			
		    } catch (Exception e) {
                //fail();
                System.out.println("Unable to upload test network " + f.getName() + " : " + e.getMessage());
                continue;
            }
		}
    	
    	return map;
    }

	public static List<Task> waitForNetworksToUpload(NdexRestClientModelAccessLayer ndex, User userAccount) {
		List<Task> userTasks = null;
		boolean allNetworksUploaded = true;
		Status status;
		
		
        while (true) {
        	
            try {
            	// check if networks have uploaded
                userTasks = ndex.getUserTasks(userAccount.getAccountName(), 
	                                          Status.ALL.toString(), 0, 300);
            } catch (Exception e) {
            	System.out.println("Exception trying to get list of user tasks: " +  e.getMessage());
                break;
            }
            
            allNetworksUploaded = true;
            
            for (Task task : userTasks) {
            	status = task.getStatus();
            	
            	if ((status == Status.PROCESSING) || 
                    (status == Status.QUEUED) || (status == Status.QUEUED_FOR_DELETION)) {
            		allNetworksUploaded = false;
            		break;
            	}
            }
            
            if (allNetworksUploaded) {
            	break;
            }

            // not all networks uploaded yet; sleep and then check again
            try {
            	Thread.sleep(10000); 
            } catch (Exception e) {

            }
        }   // while (true)	
        
        return userTasks;
	}
    
}
