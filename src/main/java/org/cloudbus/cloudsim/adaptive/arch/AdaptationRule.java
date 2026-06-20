package org.cloudbus.cloudsim.adaptive.arch;

public class AdaptationRule {
	
	private String Id;
	private String description;
	private String qualityAttribute;
	private String adaptationTacticActionTag;
	private int priority;

	
	public AdaptationRule() {
		// TODO Auto-generated constructor stub
	}

	public AdaptationRule(
			String id, String description, String qualityAttribute, String actionTag, int priority) {

		Id = id;
		this.description = description;
		this.qualityAttribute = qualityAttribute;
		this.adaptationTacticActionTag = actionTag;
		this.priority = priority;
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
	public String getQualityAttribute() {
		return qualityAttribute;
	}
	public void setQualityAttribute(String qualityAttribute) {
		this.qualityAttribute = qualityAttribute;
	}
	public String getAdaptationTacticActionTag() {
		return adaptationTacticActionTag;
	}
	public void setActionTag(String actionTag) {
		this.adaptationTacticActionTag = actionTag;
	}
	public int getPriority() {
		return priority;
	}
	public void setPriority(int priority) {
		this.priority = priority;
	}

}


