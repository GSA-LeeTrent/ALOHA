package gov.gsa.ocfo.aloha.web.model;

import gov.gsa.ocfo.aloha.model.entity.leave.LeaveItem;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LRSideBySideItem implements Serializable {

	private static final long serialVersionUID = 6371578894915192691L;

	private Date date;
	private List<LeaveItem> approvedLeaveItems = new ArrayList<LeaveItem>();
	private List<LeaveItem> pendAmendLeaveItems = new ArrayList<LeaveItem>();
	
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public List<LeaveItem> getApprovedLeaveItems() {
		return approvedLeaveItems;
	}
	public List<LeaveItem> getPendAmendLeaveItems() {
		return pendAmendLeaveItems;
	}
	
	public boolean addApprovedLeaveItem(LeaveItem li) {
		return this.approvedLeaveItems.add(li);
	}
	
	public boolean addPendAmendLeaveItem(LeaveItem li) {
		return this.pendAmendLeaveItems.add(li);
	}
	public boolean isPopulated() {
		return ( (!this.approvedLeaveItems.isEmpty()) 
					|| (!this.pendAmendLeaveItems.isEmpty()) 
				);
	}
}