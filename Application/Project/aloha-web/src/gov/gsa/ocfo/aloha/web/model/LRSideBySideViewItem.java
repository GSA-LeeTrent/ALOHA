package gov.gsa.ocfo.aloha.web.model;

import gov.gsa.ocfo.aloha.web.model.leave.LeaveViewItem;

import java.io.Serializable;
import java.util.Date;

public class LRSideBySideViewItem implements Serializable {

	private static final long serialVersionUID = 4395124900234432856L;

	private Date leaveDate;
	private LeaveViewItem approvedLeaveViewItem;
	private LeaveViewItem pendAmendLeaveViewItem;
	
	public LRSideBySideViewItem(Date leaveDate) {
		this.leaveDate = leaveDate;
	}
	
	public Date getLeaveDate() {
		return leaveDate;
	}
	
	public LeaveViewItem getApprovedLeaveViewItem() {
		return approvedLeaveViewItem;
	}

	public void setApprovedLeaveViewItem(LeaveViewItem approvedLeaveViewItem) {
		this.approvedLeaveViewItem = approvedLeaveViewItem;
	}

	public LeaveViewItem getPendAmendLeaveViewItem() {
		return pendAmendLeaveViewItem;
	}

	public void setPendAmendLeaveViewItem(LeaveViewItem pendAmendLeaveViewItem) {
		this.pendAmendLeaveViewItem = pendAmendLeaveViewItem;
	}

	public boolean isPopulated() {
		return ( (this.approvedLeaveViewItem != null) 
					|| (this.pendAmendLeaveViewItem != null) 
				);
	}
	
	public String getMapKey() {
		String mapKey = new String();
		if ( this.approvedLeaveViewItem != null) 
		{
			mapKey = this.approvedLeaveViewItem.getMapKey();
		}
		else if ( this.pendAmendLeaveViewItem != null) 
		{
			mapKey = this.pendAmendLeaveViewItem.getMapKey();
		} 
		return mapKey;
	}	
}
