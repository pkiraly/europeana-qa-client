package de.gwdg.europeanaqa.client.rest.config;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 *
 * @author Péter Király <peter.kiraly at gwdg.de>
 */
public class SessionManager {
	
	private final Map<String, SessionDAO> sessionIds;

	public SessionManager() {
		this.sessionIds = new HashMap<>();
	}

	public String create() {
		String sessionId = UUID.randomUUID().toString();
		sessionIds.put(sessionId, new SessionDAO(sessionId, SessionDAO.State.MEASURING));
		return sessionId;
	}

	public boolean validate(String sessionId) {
		return sessionIds.containsKey(sessionId);
	}

	public void delete(String sessionId) {
		if (!validate(sessionId)) {
			throw new IllegalArgumentException("The session identifier is not valid");
		}
		sessionIds.remove(sessionId);
	}

	public SessionDAO get(String sessionId) {
		if (!validate(sessionId)) {
			throw new IllegalArgumentException("The session identifier is not valid");
		}
		return sessionIds.get(sessionId);
	}

	public SessionDAO.State getState(String sessionId) {
		if (!validate(sessionId)) {
			throw new IllegalArgumentException("The session identifier is not valid");
		}
		return sessionIds.get(sessionId).getState();
	}

	public void setState(String sessionId, SessionDAO.State state) {
		if (!validate(sessionId)) {
			throw new IllegalArgumentException("The session identifier is not valid");
		}
		sessionIds.get(sessionId).setState(state);
	}

	public Process getAnalyzingProcess(String sessionId) {
		if (!validate(sessionId)) {
			throw new IllegalArgumentException("The session identifier is not valid");
		}
		return sessionIds.get(sessionId).getAnalzyingProcess();
	}

	public void setAnalyzingProcess(String sessionId, Process analyzingProcess) {
		if (!validate(sessionId)) {
			throw new IllegalArgumentException("The session identifier is not valid");
		}
		sessionIds.get(sessionId).setAnalzyingProcess(analyzingProcess);
	}

}
