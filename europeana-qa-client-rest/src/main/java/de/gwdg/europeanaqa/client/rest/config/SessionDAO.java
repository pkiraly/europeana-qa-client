package de.gwdg.europeanaqa.client.rest.config;

/**
 *
 * @author Péter Király <peter.kiraly at gwdg.de>
 */
public class SessionDAO {

	public enum State {

		MEASURING("measuring"),
		IDLE("idle"),
		ANALYZING("analyzing");

		private final String name;

		private State(String name) {
			this.name = name;
		}
	};

	String id;
	State state;
	Process analzyingProcess;

	public SessionDAO(String id, State state) {
		this.id = id;
		this.state = state;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

	public Process getAnalzyingProcess() {
		return analzyingProcess;
	}

	public void setAnalzyingProcess(Process analzyingProcess) {
		this.analzyingProcess = analzyingProcess;
	}
}
