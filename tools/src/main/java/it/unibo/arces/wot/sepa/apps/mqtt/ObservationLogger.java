package it.unibo.arces.wot.sepa.apps.mqtt;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import it.unibo.arces.wot.sepa.commons.exceptions.SEPABindingsException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAPropertiesException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAProtocolException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPASecurityException;
import it.unibo.arces.wot.sepa.commons.response.ErrorResponse;
import it.unibo.arces.wot.sepa.commons.security.SEPASecurityManager;
import it.unibo.arces.wot.sepa.commons.sparql.ARBindingsResults;
import it.unibo.arces.wot.sepa.commons.sparql.Bindings;
import it.unibo.arces.wot.sepa.commons.sparql.BindingsResults;
import it.unibo.arces.wot.sepa.commons.sparql.RDFTermLiteral;
import it.unibo.arces.wot.sepa.commons.sparql.RDFTermURI;
import it.unibo.arces.wot.sepa.pattern.Aggregator;
import it.unibo.arces.wot.sepa.pattern.JSAP;

public class ObservationLogger extends Aggregator {
	private static final Logger logger = LogManager.getLogger();
	
	public ObservationLogger(JSAP jsap,SEPASecurityManager sm)
			throws SEPAProtocolException, SEPASecurityException, SEPAPropertiesException {
		super(jsap, "OBSERVATIONS", "LOG_QUANTITY",sm);
	}

	@Override
	public void onResults(ARBindingsResults results) {}

	@Override
	public void onAddedResults(BindingsResults results) {
		for (Bindings binding : results.getBindings()) {
			if (binding.getValue("value").equals("NaN")) continue;

			logger.info("Logging: "+binding.getValue("observation") +" "+binding.getValue("value"));
			
			try {
				this.setUpdateBindingValue("observation", new RDFTermURI(binding.getValue("observation")));
				this.setUpdateBindingValue("value", new RDFTermLiteral(binding.getValue("value"), binding.getDatatype("value")));
				
				update();
			} catch (SEPASecurityException | IOException | SEPAPropertiesException | SEPABindingsException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onRemovedResults(BindingsResults results) {}

	@Override
	public void onBrokenConnection() {}

	@Override
	public void onError(ErrorResponse errorResponse) {}

	@Override
	public void onSubscribe(String spuid, String alias) {}

	@Override
	public void onUnsubscribe(String spuid) {}
}
