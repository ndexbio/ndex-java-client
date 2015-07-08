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
        	// System.out.println(Thread.currentThread().getName() + ": check if profile updated");
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
                	// System.out.println(Thread.currentThread().getName() + ": not updated yet ; sleep for 1 sec");
                	// profile not updated yet -- sleep 1 seconds and check again
                	Thread.sleep(1000);
                	updatedNetworkSummary = ndex.getNetworkSummaryById(testNetworkUUID);
                } catch (Exception e) {
            		fail("Unable to download summary of network " + testNetworkUUID + " : " + e.getMessage());
                }
                
        	} else {
            	// profile has been modified on the server; break out of the loop
        		// System.out.println(Thread.currentThread().getName() + ": profile updated ; end the update thread");
            	break;
            } 	
        } 
	}
	
	public NetworkSummary getUpdatedNetworkSummary() {
		return this.updatedNetworkSummary; 
	}
	
}
