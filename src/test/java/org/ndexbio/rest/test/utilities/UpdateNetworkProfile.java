/**
 *   Copyright (c) 2013, 2015
 *  	The Regents of the University of California
 *  	The Cytoscape Consortium
 *
 *   Permission to use, copy, modify, and distribute this software for any
 *   purpose with or without fee is hereby granted, provided that the above
 *   copyright notice and this permission notice appear in all copies.
 *
 *   THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 *   WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 *   MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 *   ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 *   WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 *   ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 *   OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */
package org.ndexbio.rest.test.utilities;

import static org.junit.Assert.fail;

import org.ndexbio.model.object.network.NetworkSummary;
import org.ndexbio.rest.client.NdexRestClientModelAccessLayer;

public class UpdateNetworkProfile extends Thread {

	private NdexRestClientModelAccessLayer ndex;
	private String testNetworkUUID;
	private NetworkSummary networkSummary;
	private NetworkSummary updatedNetworkSummary;
	
	public UpdateNetworkProfile(NdexRestClientModelAccessLayer ndex,
			                    String testNetworkUUID,
			                    NetworkSummary networkSummary) {
		this.ndex                  = ndex;
		this.testNetworkUUID       = testNetworkUUID ;
		this.networkSummary        = networkSummary;
		this.updatedNetworkSummary = null;
	}
	
	public void run() {
		// send a request to the server for updating network profile
		try {
			updatedNetworkSummary = ndex.updateNetworkSummary(networkSummary, testNetworkUUID);
        } catch (Exception e) {
        	// here we most likely get the 
        	// "com.fasterxml.jackson.databind.JsonMappingException: No content to map due to end-of-input"
            // ignore it 
        }
		
		// wait till the network profile update is finished
        while (true) {
        	
        	try {
        	    updatedNetworkSummary = ndex.getNetworkSummaryById(testNetworkUUID);
        	} catch (Exception e) {
        		fail("Unable to download summary of network " + testNetworkUUID + " : " + e.getMessage());
            }
        		
        	if (!updatedNetworkSummary.getName().equals(networkSummary.getName()) ||
        		!updatedNetworkSummary.getDescription().equals(networkSummary.getDescription()) ||
        		!updatedNetworkSummary.getVersion().equals(networkSummary.getVersion()))	
        	{	
                try {
                	// profile not updated yet -- sleep 1 seconds and check again
                	Thread.sleep(1000);
                	updatedNetworkSummary = ndex.getNetworkSummaryById(testNetworkUUID);
                } catch (Exception e) {
            		fail("Unable to download summary of network " + testNetworkUUID + " : " + e.getMessage());
                }
                
        	} else {
            	// profile has been modified on the server; break out of the loop
            	break;
            } 	
        } 
	}
	
	public NetworkSummary getUpdatedNetworkSummary() {
		return this.updatedNetworkSummary; 
	}
	
}
