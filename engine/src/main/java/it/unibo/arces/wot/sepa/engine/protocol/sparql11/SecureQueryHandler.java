package it.unibo.arces.wot.sepa.engine.protocol.sparql11;

import org.apache.http.Header;
import org.apache.http.HttpRequest;

import it.unibo.arces.wot.sepa.engine.dependability.AuthorizationResponse;
import it.unibo.arces.wot.sepa.engine.dependability.Dependability;
import it.unibo.arces.wot.sepa.engine.scheduling.Scheduler;

public class SecureQueryHandler extends QueryHandler implements SecureQueryHandlerMBean {

	public SecureQueryHandler(Scheduler scheduler) throws IllegalArgumentException {
		super(scheduler);
	}

	/**
	 * Operation when receiving a HTTP request at a protected endpoint
	 * 
<pre>
Specific to HTTP request:
1. Check if the request contains an Authorization header. 
2. Check if the request contains an Authorization: Bearer-header with non-null/empty contents 
3. Check if the value of the Authorization: Bearer-header is a JWT object 

Token validation:
4. Check if the JWT object is signed 
5. Check if the signature of the JWT object is valid. This is to be checked with AS public signature verification key 
6. Check the contents of the JWT object 
7. Check if the value of "iss" is https://wot.arces.unibo.it:8443/oauth/token 
8. Check if the value of "aud" contains https://wot.arces.unibo.it:8443/sparql 
9. Accept the request as well as "sub" as the originator of the request and process it as usual
 
Respond with 401 if not

According to RFC6749, the error member can assume the following values: invalid_request, invalid_client, invalid_grant, unauthorized_client, unsupported_grant_type, invalid_scope.
	
NOTE: In this implementation, the use of an expired token corresponds to a "invalid_grant" error
</pre>
	 */
	@Override
	protected AuthorizationResponse authorize(HttpRequest request) {
		// Extract Bearer authorization
		Header[] bearer = request.getHeaders("Authorization");

		if (bearer.length != 1) {
			logger.error("Authorization header is missing or multiple");
//			return new ErrorResponse(HttpStatus.SC_BAD_REQUEST, "invalid_request",
//					"Authorization header must be a single one");
			return new AuthorizationResponse("invalid_request","Authorization header must be a single one");
		}
		if (!bearer[0].getValue().startsWith("Bearer ")) {
			logger.error("Authorization must be ***Bearer JWT***");
//			return new ErrorResponse(HttpStatus.SC_BAD_REQUEST, "invalid_request",
//					"Authorization header must be ***Bearer JWT***");
			return new AuthorizationResponse("unsupported_grant_type","Authorization header must be ***Bearer JWT***");
		}

		// ******************
		// JWT validation
		// ******************
		String jwt = bearer[0].getValue().split(" ")[1];

		return Dependability.validateToken(jwt);
	}

	@Override
	public long getErrors_AuthorizingFailed() {
		return jmx.getErrors_AuthorizingFailed();
	}

}
