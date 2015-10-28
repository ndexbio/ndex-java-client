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
