/* This class represents a query solution of a SPARQL 1.1 Query
 * 
 * Author: Luca Roffia (luca.roffia@unibo.it)

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package it.unibo.arces.wot.sepa.commons.sparql;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import it.unibo.arces.wot.sepa.commons.exceptions.SEPABindingsException;

/**
 * This class represents a query solution of a SPARQL 1.1 Query. An example of the internal representation as JSON object follows:
 * 
 <pre>
 
 {
 "x" : { 
 	"type": "bnode", 
 	"value": "r2" }, 
 "hpage" : { 
 	"type": "uri", 
 	"value":"http://work.example.org/alice/" }, 
 "blurb" : { 
 	"datatype": "http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral", 
 	"type": "literal",
 	"value": "<p xmlns=\"http://www.w3.org/1999/xhtml\">My name is <b>alice</b></p>" },
 "name" : { 
 	"type": "literal", 
 	"value": "Bob", 
 	"xml:lang": "en" } }
  
  NOTE: Virtuoso uses "typed-literal" as type when the "datatype" is specified
 */

public class Bindings {	
	/** The solution. */
	private JsonObject solution;

	/**
	 * Instantiates a new bindings.
	 *
	 * @param solution
	 *            the solution
	 */
	public Bindings(JsonObject solution) {
		this.solution = solution;
	}

	/**
	 * Instantiates a new bindings.
	 */
	public Bindings() {
		solution = new JsonObject();
	}

	/**
	 * Gets the variables.
	 *
	 * @return the variables
	 */
	public Set<String> getVariables() {
		Set<String> ret = new HashSet<String>();
		for (Entry<String, JsonElement> entry : solution.entrySet()) {
			ret.add(entry.getKey());
		}
		return ret;
	}
	
	public RDFTerm getRDFTerm(String variable) throws SEPABindingsException {
		if (!solution.has(variable)) throw new SEPABindingsException(String.format("Variable not found: %s",variable));
		
		try {
			String type = solution.getAsJsonObject(variable).get("type").getAsString();
			String value = solution.getAsJsonObject(variable).get("value").getAsString();
			
			switch(type) {
			case "uri":
				return new RDFTermURI(value);
			case "literal":
			case "typed-literal":
				if(solution.getAsJsonObject(variable).get("datatype") == null) return new RDFTermLiteral(value);
				return new RDFTermLiteral(value,solution.getAsJsonObject(variable).get("datatype").getAsString());				
			case "bnode":
				return new RDFTermBNode(value);
			}
		}
		catch(Exception e) {
			throw new SEPABindingsException(e);
		}
		
		return null;
	}

	/**
	 * Sets the binding value.
	 *
	 * @param variable the variable
	 * @param value the value
	 * @param datatype the datatype
	 */
	public void setBindingValue(String variable,String value,String datatype) throws SEPABindingsException {
		if (variable == null || value == null) throw new SEPABindingsException("One or more arguments are null");
		try {
			solution.getAsJsonObject(variable).add("value", new JsonPrimitive(value));
			solution.getAsJsonObject(variable).add("datatype",new JsonPrimitive(datatype));
		}
		catch(Exception e) {
			throw new SEPABindingsException(String.format("Variable not found: %s",variable));
		}		
	}
	
	/**
	 * Sets the binding value.
	 *
	 * @param variable the variable
	 * @param value the RDFTerm
	 * @see RDFTerm
	 */
	public void setBindingValue(String variable,RDFTerm value) throws SEPABindingsException  {
		if (variable == null || value == null) throw new SEPABindingsException("One or more arguments are null");
			
		try {
			if (solution.getAsJsonObject(variable).get("type").getAsString().equals("literal") || solution.getAsJsonObject(variable).get("type").getAsString().equals("typed-literal")) {
				if(!value.getClass().equals(RDFTermLiteral.class)) throw new SEPABindingsException("Value of ariable: "+variable+" must be a literal");
				if (((RDFTermLiteral) value).getDatatype() != null) solution.getAsJsonObject(variable).add("datatype", new JsonPrimitive(((RDFTermLiteral) value).getDatatype()));
			}
			if (solution.getAsJsonObject(variable).get("type").getAsString().equals("uri") && !value.getClass().equals(RDFTermURI.class))throw new SEPABindingsException("Value of ariable: "+variable+" must be an URI");
			if (solution.getAsJsonObject(variable).get("type").getAsString().equals("bnode")  && !value.getClass().equals(RDFTermBNode.class)) throw new SEPABindingsException("Value of ariable: "+variable+" must be a b-node");
			
			solution.getAsJsonObject(variable).add("value", new JsonPrimitive(value.getValue()));
		}
		catch(Exception e) {
			throw new SEPABindingsException(String.format("Variable not found or bad type: %s",variable));
		}		
	}
	
	/**
	 * Gets the binding value.
	 *
	 * @param variable
	 *            the variable
	 * @return the binding value
	 */
	public String getValue(String variable) {
		try {
			return solution.getAsJsonObject(variable).get("value").getAsString();	
		}
		catch(Exception e) {
			return null;
		}		
	}

	/**
	 * Gets the datatype.
	 *
	 * @param variable
	 *            the variable
	 * @return the datatype or null if the variable is not found, the term is not a
	 *         literal or the datatype has not been specified
	 */
	public String getDatatype(String variable) {
		try {
			return solution.getAsJsonObject(variable).get("datatype").getAsString();
		}
		catch(Exception e) {
			return null;
		}
	}

	/**
	 * Gets the language.
	 *
	 * @param variable
	 *            the variable
	 * @return the language or null if the variable is not found, the term is not a
	 *         literal or the language has not been specified
	 */
	public String getLanguage(String variable) {
		try {
			return solution.getAsJsonObject(variable).get("xml:lang").getAsString();	
		}
		catch(Exception e) {
			return null;
		}
	}

	/**
	 * Checks if is literal.
	 *
	 * @param variable
	 *            the variable
	 * @return true, if is literal
	 */
	public boolean isLiteral(String variable) throws SEPABindingsException {
		if (!solution.has(variable))
			throw new SEPABindingsException("Variable not found "+variable);

		return (solution.getAsJsonObject(variable).get("type").getAsString().equals("literal") || solution.getAsJsonObject(variable).get("type").getAsString().equals("typed-literal"));
	}

	/**
	 * Checks if is uri.
	 *
	 * @param variable
	 *            the variable
	 * @return true, if is uri
	 */
	public boolean isURI(String variable) throws SEPABindingsException {
		if (!solution.has(variable))
			throw new SEPABindingsException("Variable not found "+variable);

		return solution.getAsJsonObject(variable).get("type").getAsString().equals("uri");
	}

	/**
	 * Checks if is b node.
	 *
	 * @param variable
	 *            the variable
	 * @return true, if is b node
	 */
	public boolean isBNode(String variable) throws SEPABindingsException  {
		if (!solution.has(variable))
			throw new SEPABindingsException("Variable not found "+variable);

		return solution.getAsJsonObject(variable).get("type").getAsString().equals("bnode");
	}

	/**
	 * Adds the binding.
	 *
	 * @param variable
	 *            the variable
	 * @param value
	 *            the value
	 */
	public void addBinding(String variable, RDFTerm value) {
		solution.add(variable, value.toJson());
	}

	/**
	 * Equals.
	 *
	 * @param qs
	 *            the qs
	 * @return true, if successful
	 */
	public boolean equals(Bindings qs) {
		return this.solution.equals(qs.solution);
	}

	/**
	 * To json.
	 *
	 * @return the json object
	 */
	public JsonObject toJson() {
		return solution;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return solution.toString();
	}

	/**
	 * Checks if is empty.
	 *
	 * @return true, if is empty
	 */
	public boolean isEmpty() {
		return solution.isJsonNull();
	}
}
