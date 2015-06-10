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
		//System.out.println("Starting thread " + Thread.currentThread().getName());
    	// download test network from the server
    	try {
			network = ndex.getNetwork(testNetworkUUID);
		} catch (IOException | NdexException  e) {
			fail("Unable to download network " + testNetworkUUID + " : " + e.getMessage());
		}
    	this.downloadComplete = true;
    	//System.out.println("Finishing thread " + Thread.currentThread().getName());
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
