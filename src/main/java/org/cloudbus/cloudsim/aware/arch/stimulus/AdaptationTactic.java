package org.cloudbus.cloudsim.aware.arch.stimulus;

public class AdaptationTactic {
	
	private String Id;
	private String description;
	private String object;
	private String change;
	private int min;
	private int max;
	private String actionTag;
	
	
	public AdaptationTactic() {

	}
	
	public AdaptationTactic(String id, String description, String object, String change, int min, int max, String tag) {
		this.Id = id;
		this.description = description;
		this.object = object;
		this.setChange(change);
		this.min = min;
		this.max = max;
		this.actionTag = tag;
	}
	

	public String getId() {
		return Id;
	}
	
	public void setId(String id) {
		Id = id;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}

	public String getObject() {
		return object;
	}

	public void setObject(String object) {
		this.object = object;
	}

	public String getChange() {
		return change;
	}

	public void setChange(String change) {
		this.change = change;
	}

	public int getMin() {
		return min;
	}

	public void setMin(int min) {
		this.min = min;
	}

	public int getMax() {
		return max;
	}

	public void setMax(int max) {
		this.max = max;
	}
	
	public String getActionTag() {
		return actionTag;
	}

	public void setActionTag(String tag) {
		this.actionTag = tag;
	}
		
}
