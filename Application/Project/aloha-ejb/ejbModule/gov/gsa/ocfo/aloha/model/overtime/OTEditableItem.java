package gov.gsa.ocfo.aloha.model.overtime;

import java.io.Serializable;

public class OTEditableItem implements Serializable {
	// CLASS MEMBERS
	private static final long serialVersionUID = -1934643266527518051L;
	
	// INSTANCE MEMBERS
	private int id;
	private String taskDescription;
	private String estimatedHoursAsText;
	
	public OTEditableItem() {
		//com.icesoft.faces.facelets.component.table.editable.EditableTableBean REQUIRES NO-ARG CONSTRUCTOR
	}
	public OTEditableItem(int id, String taskDesc, String estHoursAsText) {
		this.id = id;
		this.taskDescription = taskDesc;
		this.estimatedHoursAsText = estHoursAsText;
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getTaskDescription() {
		return taskDescription;
	}
	public void setTaskDescription(String taskDescription) {
		this.taskDescription = taskDescription;
	}
	public String getEstimatedHoursAsText() {
		return estimatedHoursAsText;
	}
	public void setEstimatedHoursAsText(String estimatedHoursAsText) {
		this.estimatedHoursAsText = estimatedHoursAsText;
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("OTEditableItem [id=");
		builder.append(id);
		builder.append(", taskDescription=");
		builder.append(taskDescription);
		builder.append(", estimatedHoursAsText=");
		builder.append(estimatedHoursAsText);
		builder.append("]");
		return builder.toString();
	}
}
