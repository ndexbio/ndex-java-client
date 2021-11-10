package org.ndexbio.rest.client;

enum NdexApiVersion {
		//v1("rest"),
		v2("v2"),
		v3("v3");
		
		private final String _s;

	    private NdexApiVersion(final String s) {
	        _s = s;
	    }
	    
	    public String toString() {
	    	return _s;
	    }
}