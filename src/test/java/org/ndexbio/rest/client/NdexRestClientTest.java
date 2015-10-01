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
package org.ndexbio.rest.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.jaxrs.BasicAuthentication;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataOutput;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.ndexbio.model.object.NdexStatus;
import org.ndexbio.model.object.network.Network;
import org.ndexbio.model.object.network.NetworkSummary;


public class NdexRestClientTest {
	
	private NdexRestClient client;
	private NdexRestClientModelAccessLayer ndex;
    private static String _username = "cj2";
    private static String _password = "cj2";
		
	@Before
	public void setUp() throws Exception {
//		client = new NdexRestClient("Support", "probably-insecure2"); //("dexterpratt", "insecure");
		
        client = new NdexRestClient(_username, _password);

		/*
		client = new NdexRestClient("cjtest", "guilan"); 
			
/*		client = new NdexRestClient("cjtest", "1234", 
				"http://localhost:8080/ndexbio-rest",
				"http://localhost:8080/AuthenticationService/AuthenticationService", AuthenticationType.SAML);  */
		ndex = new NdexRestClientModelAccessLayer(client);
	}

	@After
	public void tearDown() throws Exception {
	}

    @Test
    public void testCreateCXNetwork() throws IOException {
            FileInputStream s = new FileInputStream ( "/Users/chenjing/working/cx/ligand.cx");
            UUID u = ndex.createCXNetwork(s );
            System.out.println("network created. New UUID: " + u) ;
            s.close();
    }

    
    public  void testResteasy() throws UnsupportedEncodingException, IOException
    {
          ResteasyProviderFactory factory =
                   ResteasyProviderFactory.getInstance();

          // this line is only needed if you run this as a java console app.
         //  in tomcat and jboss initialization should work without this
          ResteasyProviderFactory.pushContext(javax.ws.rs.ext.Providers.class, factory);


             ResteasyClient client =null;
             Response r = null;
          try {


                 ResteasyClientBuilder resteasyClientBuilder = new
                             ResteasyClientBuilder().providerFactory(factory);

                  client = resteasyClientBuilder.build();
             //     client.register(new BasicAuthentication(_username,_password));

                  // insert the url of the webservice here
               ResteasyWebTarget target = client.target("http://localhost:8080/ndexbio-rest/network/asCX");
               target.register(new BasicAuthentication(_username,_password));
                MultipartFormDataOutput mdo = new MultipartFormDataOutput();

                mdo.addFormData("file1", new FileInputStream(new File(
                       //       "/Users/chenjing/Downloads/5a81ae28-679e-11e5-aba2-2e70fd96076e.cx"
                                "/Users/chenjing/working/cx/ligand.cx"
                                )),
                                MediaType.APPLICATION_OCTET_STREAM_TYPE);

                GenericEntity<MultipartFormDataOutput> entity = new GenericEntity<MultipartFormDataOutput>(mdo) {};

                //Upload File
                r = target.request().post(       Entity.entity(entity, MediaType.MULTIPART_FORM_DATA_TYPE));

                // Read File Response
                String  input =  r.readEntity(String.class);



          System.out.println("DONE'" + input);

         } catch (Exception e) {

                e.printStackTrace();
         }
          finally
          {
                 if (r != null) r.close();
                 if (client != null) client.close();
          }
          
    }     


	
	@Test
	public void testAuthentication() throws Exception {
		
	/*	NetworkSummary networksummary = mal.getNetworkSummaryById("d8c5b86a-1997-11e4-8f64-90b11c72aefa");
		
		System.out.println(networksummary);
		
		//Network n0 = mal.getNeighborhood("d750c790-199e-11e4-86bd-90b11c72aefa","YGR218W", 1);
		PropertyGraphNetwork n0 = mal.getNeighborhoodAsPropertyGraph("d750c790-199e-11e4-86bd-90b11c72aefa","YGR218W", 1);
		System.out.println(n0);
		
		Network network = mal.getEdges("f6ecda26-18fc-11e4-8590-90b11c72aefa", 0, 12);
		ObjectMapper mapper = new ObjectMapper();
		System.out.println(mapper.writeValueAsString(network));
		
		PropertyGraphNetwork pn = 
		mal.getPropertyGraphNetwork("d9ed6aa1-1364-11e4-8b0d-90b11c72aefa", 0,12);
		for ( PropertyGraphNode n : pn.getNodes().values()) {
			System.out.println ("node id: "+ n.getId());
			for (NdexProperty p : n.getProperties()) {
				System.out.println("\t" + p.getPredicateString() + ": " + p.getValue());
			}
			
		}

		for (PropertyGraphEdge e : pn.getEdges()) {
			System.out.println("Edge:" + e.getSubjectId() + "->" + e.getPredicate() + "->" + e.getObjectId());
		}
		System.out.println(pn);
		
		int i = 0;
		for ( NdexProperty p : pn.getProperties()) {
			if ( p.getPredicateString().equals(PropertyGraphNetwork.uuid) ) {
				break;
			}
			i++;
		}
		pn.getProperties().remove(i);
		
		NdexProperty pname = new NdexProperty();
		pname.setPredicateString(PropertyGraphNetwork.name);
		pname.setValue("my test network1");
		pn.getProperties().add(pname);
		
		NetworkSummary summary = mal.insertPropertyGraphNetwork(pn);
		
        System.out.println(summary);
*/		
//		boolean b = ndex.checkCredential();
		
//		Assert.assertTrue(b);
		
		
		
		/*
		Network n = ndex.getNetwork("f717cacf-7fbf-11e4-a6f2-90b11c72aefa");
	
		if (n != null)
			System.out.println("foo");
			// example of search.
		List<NetworkSummary> s = ndex.findNetworks("*", true, "Support", 0,3);
		System.out.println(s.get(0).getName());
		
		// example of get server status.
		NdexStatus status = ndex.getServerStatus();
		System.out.println(status.getNetworkCount());*/
	}

	
	
/*	
	@Test
	public void testApi() throws Exception {
		JsonNode response = client.get("/networks/api", "");
		Iterator<JsonNode> elements = response.elements();
		while (elements.hasNext()){
			JsonNode resource = elements.next();
			System.out.println(resource.get("requestType") + "  " + resource.get("path"));
			System.out.println("   implemented by: " + resource.get("methodName") + "(" + resource.get("parameterTypes") + ")");
            String consumes = resource.get("consumes").textValue();
            if (null != consumes){
            	System.out.println("   consumes: " + consumes);
            }
			//System.out.println(response.toString());
			
		}
		
		
	}
	*/

	/*
	@Test
	public void testStatus() throws Exception {
		JsonNode status = client.get("/networks/status", "");
		System.out.println(status.get("networkCount") + " networks");
		System.out.println(status.get("userCount") + " users");
		System.out.println(status.get("groupCount") + " groups");
	}
	*/
/*	
	@Test
	public void testFindNetworksByName() throws Exception {
		List<Network> networks = mal.findNetworksByText("BEL", "contains", 10, 0);
		System.out.println("\n______\nTesting Finding Networks by text contains BEL:");
		for(Network network : networks){
			System.out.println(network.getName() + "  (edge count = " + network.getEdgeCount() + ")");	
		}
	}
	
	@Test
	public void testFindNetworksByProperty() throws Exception {
		List<Network> networks = mal.findNetworksByProperty("Format", "BEL_DOCUMENT", "=", 10);
		System.out.println("\n______\nTesting Finding Networks by Property Format = BEL_DOCUMENT:");
		for(Network network : networks){
			System.out.println(network.getName() + "  (edge count = " + network.getEdgeCount() + ")");	
		}
	}

	@Test
	public void testFindTermsInNetworkByNamespace() throws Exception {
		List<Network> networks = mal.findNetworksByProperty("Format", "BEL_DOCUMENT", "=", 10);
		for(Network network : networks){
			System.out.println("\n______\n" + network.getName() + "  id = " + network.getId() + "\nTerms:");
			
			List<BaseTerm> baseTerms = mal.findBaseTermsInNetworkByNamespace("HGNC", network.getId());
			for (BaseTerm baseTerm : baseTerms){
				System.out.println(" " + baseTerm.getName() + "\t  id = " + baseTerm.getId());
			}
			
		}
		
	}
*/

}
