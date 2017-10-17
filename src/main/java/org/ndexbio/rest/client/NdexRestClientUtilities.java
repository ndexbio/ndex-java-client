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

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.TimeZone;

import org.cxio.aspects.datamodels.CartesianLayoutElement;
import org.cxio.aspects.datamodels.EdgeAttributesElement;
import org.cxio.aspects.datamodels.EdgesElement;
import org.cxio.aspects.datamodels.NetworkAttributesElement;
import org.cxio.aspects.datamodels.NodeAttributesElement;
import org.cxio.aspects.datamodels.NodesElement;
import org.cxio.aspects.readers.CartesianLayoutFragmentReader;
import org.cxio.aspects.readers.CyGroupsFragmentReader;
import org.cxio.aspects.readers.CyTableColumnFragmentReader;
import org.cxio.aspects.readers.CyViewsFragmentReader;
import org.cxio.aspects.readers.CyVisualPropertiesFragmentReader;
import org.cxio.aspects.readers.EdgeAttributesFragmentReader;
import org.cxio.aspects.readers.EdgesFragmentReader;
import org.cxio.aspects.readers.GeneralAspectFragmentReader;
import org.cxio.aspects.readers.HiddenAttributesFragmentReader;
import org.cxio.aspects.readers.NetworkAttributesFragmentReader;
import org.cxio.aspects.readers.NetworkRelationsFragmentReader;
import org.cxio.aspects.readers.NodeAttributesFragmentReader;
import org.cxio.aspects.readers.NodesFragmentReader;
import org.cxio.aspects.readers.SubNetworkFragmentReader;
import org.cxio.core.CxElementReader2;
import org.cxio.core.interfaces.AspectElement;
import org.cxio.core.interfaces.AspectFragmentReader;
import org.cxio.metadata.MetaDataCollection;
import org.cxio.metadata.MetaDataElement;
import org.ndexbio.model.cx.CitationElement;
import org.ndexbio.model.cx.EdgeCitationLinksElement;
import org.ndexbio.model.cx.EdgeSupportLinksElement;
import org.ndexbio.model.cx.FunctionTermElement;
import org.ndexbio.model.cx.NamespacesElement;
import org.ndexbio.model.cx.NdexNetworkStatus;
import org.ndexbio.model.cx.NiceCXNetwork;
import org.ndexbio.model.cx.NodeCitationLinksElement;
import org.ndexbio.model.cx.NodeSupportLinksElement;
import org.ndexbio.model.cx.Provenance;
import org.ndexbio.model.cx.SupportElement;

public class NdexRestClientUtilities {

	  // used for creating a randomly generated string
	  private static Random random = new Random();
	  private static final char[] charMapping = {
	    'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o',
	    'p'};
	  private static final DateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	  static {
	    DATE_TIME_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
	  }
	
	
	
	public static final String serviceProvider = "ndexbio.org";

	public static final String serviceURL = "http://www.ndexbio.org/rest/";
	
	public static final String SAMLRequestTemplate = 
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
	"<samlp:AuthnRequest xmlns:samlp=\"urn:oasis:names:tc:SAML:2.0:protocol\" ID=\"<AUTHN_ID>\" " + 
		"Version=\"2.0\" IssueInstant=\"<ISSUE_INSTANT>\" " +
		"ProtocolBinding=\"urn:oasis:names.tc:SAML:2.0:bindings:HTTP-Redirect\" "+ 
		" ProviderName=\""+ serviceProvider + "\" "+
		"AssertionConsumerServiceURL=\"" + serviceURL +"\"/>";
	private CartesianLayoutFragmentReader CyViewFragmentReader;


	
	  /**
	   * Create a randomly generated string conforming to the xsd:ID datatype.
	   * containing 160 bits of non-cryptographically strong pseudo-randomness, as
	   * suggested by SAML 2.0 core 1.2.3. This will also apply to version 1.1
	   * 
	   * @return the randomly generated string
	   */
	/*  public static String createID() {
	    byte[] bytes = new byte[20]; // 160 bits
	    random.nextBytes(bytes);

	    char[] chars = new char[40];

	    for (int i = 0; i < bytes.length; i++) {
	      int left = (bytes[i] >> 4) & 0x0f;
	      int right = bytes[i] & 0x0f;
	      chars[i * 2] = charMapping[left];
	      chars[i * 2 + 1] = charMapping[right];
	    }

	    return String.valueOf(chars);
	  } */

	  /**
	   * Gets the current date and time in the format specified by xsd:dateTime in
	   * UTC form, as described in SAML 2.0 core 1.3.3 This will also apply to
	   * Version 1.1
	   * 
	   * @return the date and time as a String
	   */
/*	  public static String getDateAndTime() {
	    Date date = new Date();
	    return DATE_TIME_FORMAT.format(date);
	  }
*/
	  
	  /**
	   * Generates an encoded and compressed String from the specified XML-formatted
	   * String. The String is encoded in the following order:
	   * <p>
	   * 1. URL encode <br>
	   * 2. Base64 encode <br>
	   * 3. Deflate <br>
	   * 
	   * @param xmlString XML-formatted String that is to be encoded
	   * @return String containing the encoded contents of the specified XML String
	   */
	/*  public static String encodeMessage(String xmlString) throws IOException,
	      UnsupportedEncodingException {
	    // first DEFLATE compress the document (saml-bindings-2.0,
	    // section 3.4.4.1)
	    byte[] xmlBytes = xmlString.getBytes("UTF-8");
	    ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
	    DeflaterOutputStream deflaterOutputStream = new DeflaterOutputStream(
	      byteOutputStream);
	    deflaterOutputStream.write(xmlBytes, 0, xmlBytes.length);
	    deflaterOutputStream.close();

	    // next, base64 encode it
	    Base64 base64Encoder = new Base64();
	    byte[] base64EncodedByteArray = base64Encoder.encode(byteOutputStream
	      .toByteArray());
	    String base64EncodedMessage = new String(base64EncodedByteArray);

	    // finally, URL encode it
	    String urlEncodedMessage = URLEncoder.encode(base64EncodedMessage, "UTF-8");

	    return urlEncodedMessage;
	  }  */
	
	
	  public static NiceCXNetwork getCXNetworkFromStream( final InputStream in) throws IOException {
		  Set<AspectFragmentReader> readers = new HashSet<>(20);
		  
		  readers.add(EdgesFragmentReader.createInstance());
		  readers.add(EdgeAttributesFragmentReader.createInstance());
		  readers.add(NetworkAttributesFragmentReader.createInstance());
		  readers.add(NodesFragmentReader.createInstance());
		  readers.add(NodeAttributesFragmentReader.createInstance());
		  
		  readers.add(new GeneralAspectFragmentReader (NdexNetworkStatus.ASPECT_NAME,
				NdexNetworkStatus.class));
		  readers.add(new GeneralAspectFragmentReader (NamespacesElement.ASPECT_NAME,NamespacesElement.class));
		  readers.add(new GeneralAspectFragmentReader (FunctionTermElement.ASPECT_NAME,FunctionTermElement.class));
		  readers.add(new GeneralAspectFragmentReader (CitationElement.ASPECT_NAME,CitationElement.class));
		  readers.add(new GeneralAspectFragmentReader (SupportElement.ASPECT_NAME,SupportElement.class));
//		  readers.add(new GeneralAspectFragmentReader (ReifiedEdgeElement.ASPECT_NAME,ReifiedEdgeElement.class));
		  readers.add(new GeneralAspectFragmentReader (EdgeCitationLinksElement.ASPECT_NAME,EdgeCitationLinksElement.class));
		  readers.add(new GeneralAspectFragmentReader (EdgeSupportLinksElement.ASPECT_NAME,EdgeSupportLinksElement.class));
		  readers.add(new GeneralAspectFragmentReader (NodeCitationLinksElement.ASPECT_NAME,NodeCitationLinksElement.class));
		  readers.add(new GeneralAspectFragmentReader (NodeSupportLinksElement.ASPECT_NAME,NodeSupportLinksElement.class));
		  readers.add(new GeneralAspectFragmentReader (Provenance.ASPECT_NAME,Provenance.class));
		  
		  readers.add( CyVisualPropertiesFragmentReader.createInstance());
		  readers.add( CartesianLayoutFragmentReader.createInstance());
		  readers.add( NetworkRelationsFragmentReader.createInstance());
		  readers.add( SubNetworkFragmentReader.createInstance());
		  readers.add( CyGroupsFragmentReader.createInstance());
		  readers.add( HiddenAttributesFragmentReader.createInstance());
		  readers.add( CyTableColumnFragmentReader.createInstance());
		  readers.add( CyViewsFragmentReader.createInstance());
		  	        
	        
	        CxElementReader2 r = new CxElementReader2(in, readers, true);
	        
	        MetaDataCollection metadata = r.getPreMetaData();
			
	        long nodeIdCounter = 0;
	        long edgeIdCounter = 0;
	        
	        NiceCXNetwork niceCX = new NiceCXNetwork ();
	        
	     	for ( AspectElement elmt : r ) {
	     		switch ( elmt.getAspectName() ) {
	     			case NodesElement.ASPECT_NAME :       //Node
	     				    NodesElement n = (NodesElement) elmt;
	     					niceCX.addNode(n);
	                        if (n.getId() > nodeIdCounter )
	                        	nodeIdCounter = n.getId();
	     					break;
	     				case NdexNetworkStatus.ASPECT_NAME:   //ndexStatus we ignore this in CX
	     					break; 
	     				case EdgesElement.ASPECT_NAME:       // Edge
	     					EdgesElement ee = (EdgesElement) elmt;
	     					niceCX.addEdge(ee);
	     					if( ee.getId() > edgeIdCounter)
	     						edgeIdCounter = ee.getId();
	     					break;
	     				case NodeAttributesElement.ASPECT_NAME:  // node attributes
	     					niceCX.addNodeAttribute((NodeAttributesElement) elmt );
	     					break;
	     				case NetworkAttributesElement.ASPECT_NAME: //network attributes
	     					niceCX.addNetworkAttribute(( NetworkAttributesElement) elmt);
	     					break;
	     					
	     				case EdgeAttributesElement.ASPECT_NAME:
	     					niceCX.addEdgeAttribute((EdgeAttributesElement)elmt);
	     					break;
	     				case CartesianLayoutElement.ASPECT_NAME:
	     					CartesianLayoutElement e = (CartesianLayoutElement)elmt;
	     					niceCX.addNodeAssociatedAspectElement(Long.valueOf(e.getNode()), e);
	     					break;
	     				case Provenance.ASPECT_NAME:
	     					Provenance prov = (Provenance) elmt;
	     					niceCX.setProvenance(prov);
	     					break;
	     				case NamespacesElement.ASPECT_NAME:
	     					NamespacesElement ns = (NamespacesElement) elmt;
	     					niceCX.setNamespaces(ns);
	     					break;
	     				default:    // opaque aspect
	     					niceCX.addOpapqueAspect(elmt);
	     			}

	     	} 
	     	
	     	MetaDataCollection postmetadata = r.getPostMetaData();
	  	    if ( postmetadata !=null) {
			  if( metadata == null) {
				  metadata = postmetadata;
			  } else {
				  for (MetaDataElement e : postmetadata.toCollection()) {
					  Long cnt = e.getIdCounter();
					  if ( cnt !=null) {
						 metadata.setIdCounter(e.getName(),cnt);
					  }
					  cnt = e.getElementCount() ;
					  if ( cnt !=null) {
							 metadata.setElementCount(e.getName(),cnt);
					  }
				  }
			  }
		    }
	  	    
	  	    Long cxNodeIdCounter = metadata.getIdCounter(NodesElement.ASPECT_NAME);
	  	    if (cxNodeIdCounter == null || cxNodeIdCounter.longValue() < nodeIdCounter)
	  	    	metadata.setIdCounter(NodesElement.ASPECT_NAME, Long.valueOf(nodeIdCounter));
	  	    
	  	    Long cxEdgeIdCounter = metadata.getIdCounter(EdgesElement.ASPECT_NAME);
	  	    if (cxEdgeIdCounter == null || cxEdgeIdCounter.longValue() < edgeIdCounter)
	  	        metadata.setIdCounter(EdgesElement.ASPECT_NAME, Long.valueOf(edgeIdCounter));
	  	
	  	    niceCX.setMetadata(metadata);
	  	    
	        return niceCX;
	    }
	    

}
