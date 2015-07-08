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
