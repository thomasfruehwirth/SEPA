package it.unibo.arces.wot.sepa.engine.scheduling;

import it.unibo.arces.wot.sepa.engine.dependability.ClientCredentials;

public class InternalUpdateRequest extends InternalUQRequest {

	public InternalUpdateRequest(String sparql, String defaultGraphUri, String namedGraphUri,ClientCredentials credentials) {
		super(sparql, defaultGraphUri, namedGraphUri,credentials);
	}

	@Override
	public String toString() {
		return "*UPDATE* "+sparql + " [[DEFAULT GRAPH URI: <"+defaultGraphUri + "> NAMED GRAPH URI: <" + namedGraphUri+">]]";
	}
}
