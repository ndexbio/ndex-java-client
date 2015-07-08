package org.ndexbio.rest.test.utilities;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.ndexbio.model.exceptions.NdexException;
import org.ndexbio.model.object.NewUser;
import org.ndexbio.model.object.Permissions;
import org.ndexbio.model.object.Status;
import org.ndexbio.model.object.Task;
import org.ndexbio.model.object.User;
import org.ndexbio.model.object.network.Network;
import org.ndexbio.model.object.network.NetworkSummary;
import org.ndexbio.rest.client.NdexRestClientModelAccessLayer;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class NetworkUtils {
	
    public static void deleteNetworks(
    		NdexRestClientModelAccessLayer ndex, String accountName, 
    		Map<String, String> networksToUpload) {
    	
    	// list of all networks from the test account 
    	List<NetworkSummary> allNetworks = null;
    	
    	int count = (networksToUpload != null) ? networksToUpload.size() : 0;
    	
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
        			
    		try {
    			//get network summary
    			networkSummary = ndex.getNetworkSummaryById(networkUUID);
    		} catch (IOException | NdexException e1) {
    			fail("unable to get network summary for network " + networkUUID);
    		}
    		
            
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

        assertEquals("Node count doesn't match", 
        	network1.getNodeCount(), network2.getNodeCount());

        assertEquals("Node count doesn't match",
        	network1.getNodes().size(), network2.getNodes().size());

        assertEquals("Edge count doesn't match",
        	network1.getEdgeCount(), network2.getEdgeCount());

        assertEquals("Edge count doesn't match",
        	network1.getEdges().size(), network2.getEdges().size());       

        assertEquals("Base terms doesn't match",
        	network1.getBaseTerms().size(), network2.getBaseTerms().size());

        assertEquals("Name spaces count doesn't match",
        	network1.getNamespaces().size(), network2.getNamespaces().size());

        assertEquals("Citations count doesn't match",
        	network1.getCitations().size(), network2.getCitations().size());
     
        return;
	}
    
}
