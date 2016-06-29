package de.gwdg.europeanaqa.client.rest.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import de.gwdg.metadataqa.api.uniqueness.TfIdf;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Péter Király <peter.kiraly at gwdg.de>
 */
@JsonInclude(Include.NON_NULL)
public class Result {

	private String sessionId;
	private String status;
	private String result;

	private List<String> existingFields;
	private List<String> missingFields;
	private List<String> emptyFields;
	private Map<String, Object> results;
	private Map<String, Map<String, ? extends Object>> labelledResults;
	private Map<String, List<TfIdf>> termsCollection;
	private String message;

	public Result() {
	}

	public List<String> getExistingFields() {
		return existingFields;
	}

	public void setExistingFields(List<String> existingFields) {
		this.existingFields = existingFields;
	}

	public List<String> getMissingFields() {
		return missingFields;
	}

	public void setMissingFields(List<String> missingFields) {
		this.missingFields = missingFields;
	}

	public List<String> getEmptyFields() {
		return emptyFields;
	}

	public void setEmptyFields(List<String> emptyFields) {
		this.emptyFields = emptyFields;
	}

	public Map<String, Object> getResults() {
		return results;
	}

	public void setResults(Map<String, Object> results) {
		this.results = results;
	}

	public Map<String, List<TfIdf>> getTermsCollection() {
		return termsCollection;
	}

	public void setTermsCollection(Map<String, List<TfIdf>> termsCollection) {
		this.termsCollection = termsCollection;
	}

	public Map<String, Map<String, ? extends Object>> getLabelledResults() {
		return labelledResults;
	}

	public void setLabelledResults(Map<String, Map<String, ? extends Object>> labelledResults) {
		this.labelledResults = labelledResults;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
