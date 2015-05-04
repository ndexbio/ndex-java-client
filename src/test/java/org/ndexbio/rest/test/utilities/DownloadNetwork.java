package org.ndexbio.rest.test.utilities;

import static org.junit.Assert.fail;

import java.io.IOException;

import org.ndexbio.model.exceptions.NdexException;
import org.ndexbio.model.object.network.Network;
import org.ndexbio.rest.client.NdexRestClientModelAccessLayer;

public class DownloadNetwork extends Thread {

	private NdexRestClientModelAccessLayer ndex;
	private String testNetworkUUID;
	private Network network;
	private boolean downloadComplete;
	
	public DownloadNetwork(NdexRestClientModelAccessLayer ndex, String testNetworkUUID) {
		this.ndex             = ndex;
		this.testNetworkUUID  = testNetworkUUID ;
		this.network          = null;
		this.downloadComplete = false;
	}
	
	public void run() {
    	// download test network from the server
    	try {
			network = ndex.getNetwork(testNetworkUUID);
		} catch (IOException | NdexException  e) {
			fail("Unable to download network " + testNetworkUUID + " : " + e.getMessage());
		}
    	this.downloadComplete = true;
	}
	
	public Network getNetwork() {
		return this.network; 
	}
	public boolean getDownloadComplete() {
		return this.downloadComplete;
	}
    public boolean isDownloadComplete() {	
        return this.getDownloadComplete();
    }
}
