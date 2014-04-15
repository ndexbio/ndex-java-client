package org.ndexbio.rest;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.ndexbio.model.object.BaseTerm;
import org.ndexbio.model.object.Citation;
import org.ndexbio.model.object.Edge;
import org.ndexbio.model.object.MetadataObject;
import org.ndexbio.model.object.Namespace;
import org.ndexbio.model.object.NdexObject;
import org.ndexbio.model.object.Network;
import org.ndexbio.model.object.Node;

public class NetworkBuilder {
	
	private Map<Long, NdexObject> idMap;
	private Map<String, Long> identifierMap;
	private Long maxId;
	private Network network;
	private Namespace localNamespace;
	
	
	public NetworkBuilder() {
		super();
		network = new Network();
		
		maxId = (long) 0;
		idMap = new HashMap<Long, NdexObject>();
		identifierMap = new HashMap<String, Long>();
		try {
			localNamespace = findOrCreateNamespace(null, null);
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private Long getNextId(){
		return maxId++;
	}
	
	public Long getMaxId() {
		return maxId;
	}

	public Network getNetwork() {
		return network;
	}
	
	/*
	 *  Properties
	 */
	
	public void addNetworkProperty(String property, Object value) {
		network.getMetadata().put(property, value.toString());
		
	}
	
	public void addProperty(MetadataObject object, String property, Object value) {
		object.getMetadata().put(property, value.toString());
		
	}

	/*
	 *  Namespaces
	 */

	public Namespace findOrCreateNamespace(String uri, String prefix)
			throws ExecutionException {
		
		String myURI = uri;
		String myPrefix = prefix;
		if (null == myURI && null == myPrefix){
			// both null: local namespace, no URI
			myPrefix = "LOCAL";
		} else if (null != myPrefix){
			// URI null but have prefix, add common URIs
			myURI = findURIForNamespacePrefix(myPrefix);
		} else if (null != myURI){
			// Prefix null but have URI, add common prefix
			myPrefix = this.findPrefixForNamespaceURI(uri);
		}

		String namespaceIdentifier = getNamespaceIdentifier(uri, prefix);
		
		Namespace namespace;
		Long internalId = identifierMap.get(namespaceIdentifier);
		if (null != internalId){
			namespace = (Namespace) idMap.get(internalId);
		} else {
			internalId = getNextId();
			namespace = new Namespace();
			namespace.setId(internalId.toString());
			namespace.setJdexId(internalId.toString());
			if (myPrefix != null) namespace.setPrefix(myPrefix);
			if (myURI != null) namespace.setUri(myURI);
			addNamespace(namespace);
			identifierMap.put(namespaceIdentifier, internalId);
			idMap.put(internalId, namespace);	
		}
		//logger.info("Created namespace " + namespace.getPrefix() + " " + namespace.getUri());

		return namespace;
		
	}

	private void addNamespace(Namespace namespace) {
		network.getNamespaces().put(namespace.getId(), namespace);	
	}
	
	public Namespace findNamespace(String uri, String prefix) throws ExecutionException{
		String namespaceIdentifier = getNamespaceIdentifier(uri, prefix);
		Long internalId = identifierMap.get(namespaceIdentifier);
		if (null != internalId){
			return (Namespace) idMap.get(internalId);
		}	
		return null;
	}
	
	private String getNamespaceIdentifier(String uri, String prefix){
		if (null == uri && null == prefix){
			return "NAMESPACE:LOCAL";
		} else if (null != prefix){
			return "NAMESPACE:" + prefix;
		} else {
			return "NAMESPACE:" + uri;	
		}	
	}

	private String findPrefixForNamespaceURI(String uri) {
		if (uri.equals("http://biopax.org/generated/group/")) return "GROUP";
		if (uri.equals("http://identifiers.org/uniprot/")) return "UniProt";
		if (uri.equals("http://purl.org/pc2/4/")) return "PathwayCommons2";
		//System.out.println("No Prefix for " + uri);
		
		return null;
	}
	
	private String findURIForNamespacePrefix(String prefix){
		if (prefix.equals("UniProt")) return "http://identifiers.org/uniprot/";
		return null;
	}
	
	/*
	 *  BaseTerms
	 */
	
	
	public BaseTerm findOrCreateBaseTerm(String name) throws ExecutionException {
		return findOrCreateBaseTerm(name, localNamespace);
	}
	
	public BaseTerm findOrCreateBaseTerm(String name, Namespace namespace)
			throws ExecutionException {
		String baseTermIdentifier = "BASETERM:" + name + ":" + getNamespaceIdentifier(namespace.getUri(), namespace.getPrefix());
		Long internalId = identifierMap.get(baseTermIdentifier);
		BaseTerm baseTerm;
		if (null != internalId){
			baseTerm = (BaseTerm) idMap.get(internalId);
		} else {
			internalId = getNextId();
			baseTerm = new BaseTerm();
			baseTerm.setId(internalId.toString());
			baseTerm.setName(name);
			baseTerm.setNamespace(namespace.getId());
			addBaseTerm(baseTerm);
			identifierMap.put(baseTermIdentifier, internalId);
			idMap.put(internalId, baseTerm);
		}
		return baseTerm;		
	}
	
	private void addBaseTerm(BaseTerm baseTerm) {
		network.getTerms().put(baseTerm.getId(), baseTerm);		
	}

	public Node findOrCreateNode(BaseTerm baseTerm)
			throws ExecutionException {
		String nodeIdentifier = ("NODE:" + baseTerm.getName());
		Long internalId = identifierMap.get(nodeIdentifier);
		Node node;
		if (null != internalId){
			node = (Node) idMap.get(internalId);
		} else {
			node = new Node();
			internalId = getNextId();
			node.setId(internalId.toString());
			node.setRepresents(baseTerm.getId());
			addNode(node);
			identifierMap.put(nodeIdentifier, internalId);
			idMap.put(internalId, node);	
		}
		return node;
	}

	private void addNode(Node node) {
		network.getNodes().put(node.getId(), node);	
	}
	

	
	public Citation findOrCreateCitation(String type, String identifier) throws ExecutionException {
		String citationIdentifier = "CITATION:" + type + ":" + identifier;
		Long internalId = identifierMap.get(citationIdentifier);
		Citation citation;
		if (null != internalId){
			citation = (Citation) idMap.get(internalId);
		} else {
			citation = new Citation();
			internalId = getNextId();
			citation.setId(internalId.toString());
			citation.setType(type);
			citation.setIdentifier(identifier);
			addCitation(citation);
			identifierMap.put(citationIdentifier, internalId);
			idMap.put(internalId, citation);
			//logger.info("Created citation " + iCitation.getType() + ":" + iCitation.getIdentifier());
		}
		return citation;
	}

	private void addCitation(Citation citation) {
		network.getCitations().put(citation.getId(), citation);	
	}

	public Edge createEdge(Node subjectNode, Node objectNode,
			BaseTerm predicate)
			throws ExecutionException {
		if (null != objectNode && null != subjectNode && null != predicate) {
			Long internalId = getNextId();
			Edge edge = new Edge();
			edge.setId(internalId.toString());
			edge.setS(subjectNode.getId());
			edge.setP(predicate.getId());
			edge.setO(objectNode.getId());
			addEdge(edge);
			//System.out.println("Created edge " + edge.getinternalId());
			return edge;
		} 
		return null;
	}
	
	private void addEdge(Edge edge) {
		network.getEdges().put(edge.getId(), edge);	
		
	}


	





	

}
