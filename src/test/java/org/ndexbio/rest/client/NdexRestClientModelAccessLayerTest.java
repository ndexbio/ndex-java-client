/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ndexbio.rest.client;

import java.io.IOException;
import java.util.UUID;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.mock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.ndexbio.model.exceptions.NdexException;
import org.ndexbio.model.object.NetworkSet;

/**
 *
 * @author churas
 */
public class NdexRestClientModelAccessLayerTest {
    
    public NdexRestClientModelAccessLayerTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void test_getNetworkSetByIdPassingNullId() throws IOException, NdexException {
        NdexRestClientModelAccessLayer ndex = new NdexRestClientModelAccessLayer(null);
        try {
            ndex.getNetworkSetById(null, null);
            fail("Expected IllegalArgumentException");
            
        } catch(IllegalArgumentException iae){
            assertTrue(iae.getMessage().equals("networkSetId is null"));
        }
    }
    
    @Test
    public void test_getNetworkSetByIdNoAccessKeySuccess() throws IOException, NdexException {
        NdexRestClient mock = mock(NdexRestClient.class);
        UUID id = UUID.fromString("E2AE10B4-DFBA-4A15-BDB1-91C2257E12AC");
        NetworkSet myset = new NetworkSet();
        myset.setName("hi");
        expect(mock.getNdexObject("/networkset/" + id.toString(), "", NetworkSet.class)).andReturn(myset);
        replay(mock);
        NdexRestClientModelAccessLayer ndex = new NdexRestClientModelAccessLayer(mock);
        
        NetworkSet res = ndex.getNetworkSetById(id, null);
        assertTrue(res.getName().equals(myset.getName()));
        verify(mock);
            
    }

    @Test
    public void test_getNetworkSetByIdWithAccessKeySuccess() throws IOException, NdexException {
        NdexRestClient mock = mock(NdexRestClient.class);
        UUID id = UUID.fromString("E2AE10B4-DFBA-4A15-BDB1-91C2257E12AC");
        NetworkSet myset = new NetworkSet();
        myset.setName("hi");
        expect(mock.getNdexObject("/networkset/" + id.toString(), "?accesskey=somekey",
                                  NetworkSet.class)).andReturn(myset);
        replay(mock);
        NdexRestClientModelAccessLayer ndex = new NdexRestClientModelAccessLayer(mock);
        
        NetworkSet res = ndex.getNetworkSetById(id, "somekey");
        assertTrue(res.getName().equals(myset.getName()));
        verify(mock);
            
    }    
}
