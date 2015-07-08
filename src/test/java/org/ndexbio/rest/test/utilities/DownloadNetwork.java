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
		//System.out.println("\nStarting thread " + Thread.currentThread().getName());
    	// download test network from the server
    	try {
			network = ndex.getNetwork(testNetworkUUID);
		} catch (IOException | NdexException  e) {
			fail("Unable to download network " + testNetworkUUID + " : " + e.getMessage());
		}
    	this.downloadComplete = true;
        //System.out.println("\nFinishing thread " + Thread.currentThread().getName());
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
