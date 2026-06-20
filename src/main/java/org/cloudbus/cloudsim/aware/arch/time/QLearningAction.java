package org.cloudbus.cloudsim.aware.arch.time;

import org.cloudbus.cloudsim.aware.arch.stimulus.AdaptationTactic;

public class QLearningAction {

	private int Id;
	private AdaptationTactic tactic;
	private int configuration;
	
	public QLearningAction() {
		super();
		// TODO Auto-generated constructor stub
	}

	public QLearningAction(int id, AdaptationTactic tactic, int configuration) {
		super();
		this.setId(id);
		this.tactic = tactic;
		this.configuration = configuration;
	}

	public int getId() {
		return Id;
	}

	public void setId(int id) {
		Id = id;
	}
	public AdaptationTactic getTactic() {
		return tactic;
	}
	public void setTactic(AdaptationTactic tactic) {
		this.tactic = tactic;
	}
	public int getConfiguration() {
		return configuration;
	}
	public void setConfiguration(int configuration) {
		this.configuration = configuration;
	}
	
	
}
