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
package org.ndexbio.rest.client;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;

import org.apache.http.auth.AuthenticationException;
import org.ndexbio.model.exceptions.NdexException;

import com.fasterxml.jackson.core.JsonProcessingException;

public class FileUploader {

	
    private static String _username = "cj2";
    private static String _password = "cj2";
	
	public FileUploader() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) throws IllegalStateException, Exception {
		_username = args[0];
		_password = args[1];
		
		//"http://dev2.ndexbio.org/rest"
		NdexRestClient client = new NdexRestClient(_username, _password, args[2]);
		NdexRestClientModelAccessLayer ndex = new NdexRestClientModelAccessLayer(client);

	 //  	InputStream in = ndex.getNetworkAsCXStream(args[4]);
	    // 	printInputStream(in);
	    // 	in.close(); 
	     	
	     	
	        FileInputStream s = new FileInputStream ( args[3]); //"/Users/chenjing/Downloads/small-corpus-test.cx");
	           UUID u = ndex.createCXNetwork( s);
	             System.out.println("network created. New UUID: " + u) ;

	}

}
