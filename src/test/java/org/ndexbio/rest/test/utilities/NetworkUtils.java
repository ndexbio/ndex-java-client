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

import org.ndexbio.model.exceptions.NdexException;
import org.ndexbio.model.object.NdexPropertyValuePair;
import org.ndexbio.model.object.NewUser;
import org.ndexbio.model.object.Permissions;
import org.ndexbio.model.object.SimplePropertyValuePair;
import org.ndexbio.model.object.Status;
import org.ndexbio.model.object.Task;
import org.ndexbio.model.object.User;
import org.ndexbio.model.object.network.BaseTerm;
import org.ndexbio.model.object.network.Citation;
import org.ndexbio.model.object.network.Edge;
import org.ndexbio.model.object.network.FunctionTerm;
import org.ndexbio.model.object.network.Namespace;
import org.ndexbio.model.object.network.Network;
import org.ndexbio.model.object.network.NetworkSummary;
import org.ndexbio.model.object.network.Node;
import org.ndexbio.model.object.network.ReifiedEdgeTerm;
import org.ndexbio.model.object.network.Support;
import org.ndexbio.model.object.network.VisibilityType;
import org.ndexbio.rest.client.NdexRestClientModelAccessLayer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class NetworkUtils {
	
    public static void deleteNetworks(
    		NdexRestClientModelAccessLayer ndex, String accountName, 
    		Map<String, String> networks) {
    	
    	// list of all networks from the test account 
    	List<NetworkSummary> allNetworks = null;
    	
    	int count = (networks != null) ? networks.size() : 0;
    	
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
    
	public static void deleteNetwork(NdexRestClientModelAccessLayer ndex, String networkUUID) {
        try {
            ndex.deleteNetwork(networkUUID);
        } catch (Exception e) {
        	fail("Unable to delete network " + networkUUID + " : " + e.getMessage());
        }
        return;
	}    
    
	public static void startNetworkUpload(NdexRestClientModelAccessLayer ndex, File networkToUpload, Map<String,String> uploadedNetworks) {

		try {
    	    ndex.uploadNetwork(networkToUpload.getCanonicalPath());    
    	    // put name:size to map
    	    uploadedNetworks.put(networkToUpload.getName(), NumberFormat.getNumberInstance(Locale.US).format(networkToUpload.length()));		
	    } catch (Exception e) {
            fail("Unable to upload test network " + networkToUpload.getName() + " : " + e.getMessage());
        }
		
		return;
	}
	
	public static void startNetworkUpload(NdexRestClientModelAccessLayer ndex, File networkToUpload) {
		try {
    	    ndex.uploadNetwork(networkToUpload.getCanonicalPath());    		
	    } catch (Exception e) {
            fail("Unable to upload test network " + networkToUpload.getName() + " : " + e.getMessage());
        }
		return;
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
	public static Task waitForTaskToFinish(NdexRestClientModelAccessLayer ndex, User userAccount) {
		List<Task> userTasks = null;
		Status status;
		
        while (true) {
        	
            try {
            	// check if networks have uploaded
                userTasks = ndex.getUserTasks(userAccount.getAccountName(), Status.ALL.toString(), 0, 300);
            } catch (Exception e) {
            	fail("Unable to get list of user tasks: " +  e.getMessage());
            }
            
            if (userTasks.size() > 1) {
            	fail("More than one task exists for the current user");            	
            }

            status = userTasks.get(0).getStatus();
            	
            if ((status == Status.PROCESSING) || 
                (status == Status.QUEUED) || (status == Status.QUEUED_FOR_DELETION)) {
                // network not uploaded yet, sleep and then check again
                try {
                	Thread.sleep(10000); 
                } catch (Exception e) {}
            } else {
            	// task finished -- get another one  
            	break;
            }
  
        }   // while (true)	
        
        return userTasks.get(0);
	}

	public static List<Task> waitForNetworksToUpload(NdexRestClientModelAccessLayer ndex, User userAccount) {
		List<Task> userTasks = null;
		boolean allNetworksUploaded = true;
		Status status;
		
		
        while (true) {
        	
            try {
            	// check if networks have uploaded
                userTasks = ndex.getUserTasks(userAccount.getAccountName(), Status.ALL.toString(), 0, 300);
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
	
	public static void setReadOnlyFlag(NdexRestClientModelAccessLayer ndex, String networkUUID, boolean readOnly) {
        NetworkSummary networkSummary = null;
	
		String flagStr =  (true == readOnly) ? "true" : "false";

        try {
        	// set read-only flag to true or false
			ndex.setNetworkFlag(networkUUID, "readOnly", flagStr);
        } catch (Exception e) {
        	fail("unable to to set read-only flag to " + flagStr + " for network " + networkUUID);
        }
              
        while (true) {
        			
    		//get network summary
    		networkSummary = NetworkUtils.getNetworkSummaryById(ndex, networkUUID);

        
            // check if the flag is set
    		if (readOnly)  {
    			// trying to set readOnly flag to true; make sure that network is cached.
    			// if netwook is not cached, we want to wait until it is cached.
    			if ((networkSummary.getReadOnlyCommitId() > 0) && (networkSummary.getReadOnlyCacheId() > 0)) {
    				return;
    			}
    			
    		} else {
    			
    			// trying to set readOnly flag to false
    			if (networkSummary.getReadOnlyCommitId() <= 0) {
    				return;
    			}
    		}
    		
            //  sleep and then check again
            try {
            	Thread.sleep(10000); 
            } catch (Exception e) {

            }
        }   // while (true)	

	}

	
	public static void saveNetworkToFile(String fileName, Network network, boolean overwriteExistingFile)  {
		
		// check if the network already exists
		File f = new File(fileName);
		
		if (f.exists()) {
			if (overwriteExistingFile) {
				// delete the existing file and re-create it 
				f.delete();
			} else {
				// file exists, but overwrite flag is off
				return;
			}
		}

		ObjectMapper mapper = new ObjectMapper();
		
		try {
			mapper.writeValue(new File(fileName), network);
		} catch (IOException e) {
            fail("unable to create " + fileName + " : " + e.getMessage());
		}
	}

	public static Network readNetworkFromFile(String fileName) {

        ObjectMapper mapper = new ObjectMapper();
        Network network = null;
        
        try {
            network = mapper.readValue(new File(fileName), Network.class);
		} catch (IOException e) {
            fail("unable to create Network object from " + fileName + " : " + e.getMessage());
		}
        
		return network;
	}
	
	public static void compareObjectsContents(Network network1, Network network2) {

        assertEquals("supports count doesn't match",  network1.getSupports().size(), network2.getSupports().size());
        assertEquals("function terms count doesn't match",  network1.getFunctionTerms().size(), network2.getFunctionTerms().size());
        assertEquals("reifiedEdgeTerms count doesn't match",  network1.getReifiedEdgeTerms().size(), network2.getReifiedEdgeTerms().size());
        
        assertEquals("base terms count doesn't match",  network1.getBaseTerms().size(), network2.getBaseTerms().size());
        
        assertEquals("base terms count doesn't match",  network1.getBaseTerms().size(), network2.getBaseTerms().size());

        assertEquals("name spaces count doesn't match", network1.getNamespaces().size(), network2.getNamespaces().size());
        assertEquals("citations count doesn't match",   network1.getCitations().size(), network2.getCitations().size());
 
        assertEquals("description doesn't match", network1.getDescription(),  network2.getDescription());
        
        assertEquals("edge count doesn't match", network1.getEdgeCount(),    network2.getEdgeCount());
        assertEquals("edge count doesn't match", network1.getEdges().size(), network2.getEdgeCount());  
        assertEquals("edge count doesn't match", network1.getEdgeCount(),    network1.getEdges().size());
        assertEquals("edge count doesn't match", network2.getEdgeCount(),    network2.getEdges().size());
        
        assertEquals("node count doesn't match",  network1.getNodeCount(),    network2.getNodeCount());
        assertEquals("node count doesn't match",  network1.getNodes().size(), network2.getNodeCount());
        assertEquals("node count doesn't match",  network1.getNodeCount(),    network1.getNodes().size());
        assertEquals("node count doesn't match",  network2.getNodeCount(),    network2.getNodes().size());       
     
        assertEquals("isComplete doesn't match",  network1.getIsComplete(),   network2.getIsComplete());
        assertEquals("isLocked   doesn't match",  network1.getIsLocked(),     network2.getIsLocked());
        assertEquals("visibility doesn't match",  network1.getVisibility(),   network2.getVisibility());   
        assertEquals("readOnlyCommitId doesn't match",  network1.getReadOnlyCommitId(), network2.getReadOnlyCommitId());
        assertEquals("readOnlyCacheId doesn't match",  network1.getReadOnlyCacheId(),   network2.getReadOnlyCacheId());
        assertEquals("name doesn't match",    network1.getName(),    network2.getName());        
        assertEquals("owner doesn't match",   network1.getOwner(),   network2.getOwner());               
        assertEquals("URI doesn't match",     network1.getURI(),     network2.getURI()); 
        assertEquals("version doesn't match", network1.getVersion(), network2.getVersion());

        assertEquals("number of Properties doesn't match", network1.getProperties().size(), network2.getProperties().size());
    //    assertEquals("number of Presentation Properties doesn't match", 
    //    		network1.getPresentationProperties().size(), network2.getPresentationProperties().size()); 
        
        return;
	}

	public static Network getNetwork(NdexRestClientModelAccessLayer ndex, String networkUUID) {
		Network network = null;		
		try {
			network = ndex.getNetwork(networkUUID);
		} catch (IOException | NdexException e) {
			fail("unable to download network " + networkUUID + " : " + e.getMessage());				
		}
		return network;
	}

	public static NetworkSummary createNetwork(NdexRestClientModelAccessLayer ndex, Network network) {
    	NetworkSummary summary = null;
		try {
			summary = ndex.createNetwork(network);
		} catch (Exception e) {
            fail("unable to create network : " + e.getMessage() );
		}
		return summary;
	}

	public static Network getNeighborhood(NdexRestClientModelAccessLayer ndex, String networkUUID, String query, int depth) {
		
		Network subNetworkRetrieved = null;
		try {
			subNetworkRetrieved = ndex.getNeighborhood(networkUUID, query, depth);
		} catch (Exception e) {
            fail("unable to retrieve subnetwork : " + e.getMessage() );
		}
		return subNetworkRetrieved;
	}

	public static NetworkSummary getNetworkSummaryById(NdexRestClientModelAccessLayer ndex, String networkUUID) {
    	
		NetworkSummary networkSummary = null;
		try {
			networkSummary = ndex.getNetworkSummaryById(networkUUID);
		} catch (Exception e) {
            fail("unable to get network summary : " + e.getMessage() );
		}
		return networkSummary;
	}

	public static NetworkSummary updateNetwork(NdexRestClientModelAccessLayer ndex, Network network) {
		NetworkSummary networkSummary = null;
    	try {
			networkSummary = ndex.updateNetwork(network);
		} catch (Exception  e) {
			fail("Unable to update network " + network.getExternalId() + " : " + e.getMessage());
		}
		return networkSummary;
	}

	public static void compareObjectsContents(Network network, NetworkSummary networkSummary) {
		
        assertEquals("description doesn't match", network.getDescription(),  networkSummary.getDescription());
        
        assertEquals("edge count doesn't match", network.getEdgeCount(),    networkSummary.getEdgeCount());
        assertEquals("edge count doesn't match", network.getEdges().size(), networkSummary.getEdgeCount());  

        assertEquals("node count doesn't match",  network.getNodeCount(),    networkSummary.getNodeCount());
        assertEquals("node count doesn't match",  network.getNodes().size(), networkSummary.getNodeCount());
     
        assertEquals("isComplete doesn't match",  network.getIsComplete(),   networkSummary.getIsComplete());
        assertEquals("isLocked   doesn't match",  network.getIsLocked(),     networkSummary.getIsLocked());
        assertEquals("visibility doesn't match",  network.getVisibility(),   networkSummary.getVisibility());   
        assertEquals("readOnlyCommitId doesn't match",  network.getReadOnlyCommitId(), networkSummary.getReadOnlyCommitId());
        assertEquals("readOnlyCacheId doesn't match",  network.getReadOnlyCacheId(),   networkSummary.getReadOnlyCacheId());
        assertEquals("name doesn't match",    network.getName(),    networkSummary.getName());        
        assertEquals("owner doesn't match",   network.getOwner(),   networkSummary.getOwner());               
        assertEquals("URI doesn't match",     network.getURI(),     networkSummary.getURI()); 
        assertEquals("version doesn't match", network.getVersion(), networkSummary.getVersion());

        assertEquals("number of Properties doesn't match", network.getProperties().size(), networkSummary.getProperties().size());
   //     assertEquals("number of Presentation Properties doesn't match", 
   //     		network.getPresentationProperties().size(), networkSummary.getPresentationProperties().size());
        
		return;
	}

	public static void updateNetworkSummary(NdexRestClientModelAccessLayer ndex, 
			NetworkSummary newNetworkSummary, String networkUUID) {

    	try {
			ndex.updateNetworkSummary(newNetworkSummary, networkUUID);
		} catch (Exception  e) {
		    // here we most likely get the 
		    // "com.fasterxml.jackson.databind.JsonMappingException: No content to map due to end-of-input"
		    // ignore it 
		}
		return ;
	}

	public static void compareNetworkSummary(NetworkSummary networkSummary1, NetworkSummary networkSummary2) {
    	assertEquals("Network name didn't update correctly", networkSummary1.getName(), networkSummary2.getName());
    	assertEquals("Network description didn't update correctly", networkSummary1.getDescription(), networkSummary2.getDescription());
    	assertEquals("Network version didn't update correctly", networkSummary1.getVersion(), networkSummary2.getVersion());		
		
	}

	public static List<Namespace> getNetworkNamespaces(
			NdexRestClientModelAccessLayer ndex, String networkUUID, int skipBlocks, int blockSize) {
		List<Namespace> namespaces = null;
		try {
			namespaces = ndex.getNetworkNamespaces(networkUUID, skipBlocks, blockSize);
		} catch (Exception e) {
			fail("Unable to get network spaces :  " + e.getMessage());
		} 
		return namespaces;
	}
	
	public static void addNetworkNamespace(
			NdexRestClientModelAccessLayer ndex, String networkUUID, Namespace namespace) {
		try {
			ndex.addNetworkNamespace(networkUUID, namespace);
		} catch (Exception e) {
			// here we most likely get java.lang.AssertionError: Unable to get network spaces :  No content to map due to end-of-input
			// ignore it
		} 
		return;
	}
	
}
