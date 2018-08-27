package gov.gsa.ocfo.aloha.model.leave;

import gov.gsa.ocfo.aloha.model.entity.AlohaUser;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveStatus;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LeaveHeaderRow implements Serializable {
	private static final long serialVersionUID = -117016852402350410L;

	private LeaveDetailRow viewableLeaveDetail;
	private AlohaUser loggedInUser;
	private String requestId; // LR_HEADER_ID
	private long employeeUserId;
	private String employeeName;
	private Date dateSubmitted ;
	private List<LeaveDetailRow> leaveDetails = new ArrayList<LeaveDetailRow>();

	public LeaveHeaderRow(AlohaUser loggedInUser, long headerId, long employeeUserId, String emplFirstName, String emplLastName, Date hdrDateLastUpdated) {
		this.viewableLeaveDetail = null;
		
		this.loggedInUser = loggedInUser;
		this.requestId = String.valueOf(headerId);
		this.employeeUserId = employeeUserId;
		this.employeeName = emplFirstName + " " + emplLastName;
		this.dateSubmitted = hdrDateLastUpdated;
	}
	public String getRequestId() {
		return requestId;
	}
	public long getEmployeeUserId() {
		return employeeUserId;
	}
	public String getEmployeeName() {
		return employeeName;
	}
	public Date getDateSubmitted() {
		return dateSubmitted;
	}
	public List<LeaveDetailRow> getLeaveDetails() {
		return leaveDetails;
	}
	public boolean addLeaveDetail(LeaveDetailRow ldr) {
		return this.leaveDetails.add(ldr);
	}

	/***********************************************************************
	 'VIEWABLE' INCLUDES LEAVE REQUESTS THAT ARE IN A FINAL STATE
	 SUCH AS 'CANCELED' AND 'WITHDRAWN'. FOR THESE LEAVE REQUESTS THE 
	 'AVAILABLE ACTION' IS TO VIEW IT ('VIEWABLE'). 
	 'DENIED' CAN BE A FINAL STATE BUT ONLY WHEN THE 'DENY ACTION'
	 DOES NOT APPLY TO A PENDING AMENDMENT.
	 **********************************************************************/
	public LeaveDetailRow getViewableLeaveDetail() {
		if ( this.viewableLeaveDetail == null ) 
		{
			if ( (this.leaveDetails == null) || (this.leaveDetails.isEmpty()) ) 
			{
				this.setViewableLeaveDetail(null);
			} 
			else if ( (this.leaveDetails.size() > 1)
						&& (this.leaveDetails.get(0).getStatusCode().equals(LeaveStatus.CodeValues.DENIED)) ) 
			{
				// IF THE LAST ACTION TAKEN ON A LEAVE REQUEST WAS TO DENY A PENDING AMENDMENT
				// OR PENDING WITHDRAWAL, THEN THE DENIED LEAVE REQUEST IS NOT IN A FINAL STATE. 
				// IN CASES SUCH AS THESE, WE HAVE TO GO BACK AND FIND THE LEAVE REQUEST THAT 
				// PRECEDED THE DENIED LEAVE REQUEST AND RETURN THIS RECORD AS AN 'VIEWABLE/ACTIONABLE' ITEM.
				this.setViewableLeaveDetail(this.leaveDetails.get(1)); // RETURN THE LEAVE REQUEST RECORD PRIOR TO THE 'DENIED' ONE
			} 
			else 
			{
				this.setViewableLeaveDetail(this.leaveDetails.get(0)); // OTHERWISE, THE LATEST DETAIL RECORD IS 'ACTIONABLE/VIEWABLE'			
			}
		}
		return this.viewableLeaveDetail;
	}

	private void setViewableLeaveDetail(LeaveDetailRow vld) {
		this.viewableLeaveDetail = vld;
	}
	
	public boolean isCancelable() {
		LeaveDetailRow viewableDetail = this.getViewableLeaveDetail();
		return ( (viewableDetail.isSubmitted()) 
					&& (this.loggedInUserIsSubmitter(viewableDetail))
			    );
	}
	public boolean isAmendable() {
		LeaveDetailRow viewableDetail = this.getViewableLeaveDetail();
		return ( (viewableDetail.isApproved()) 
					&& (this.loggedInUserIsSubmitter(viewableDetail))
			    );
	}
	public boolean isWithdrawable() {
		LeaveDetailRow viewableDetail = this.getViewableLeaveDetail();
		return ( (viewableDetail.isApproved()) 
					&& (this.loggedInUserIsSubmitter(viewableDetail))
			    );
	}	
	public boolean isApprovable() {
		LeaveDetailRow viewable = this.getViewableLeaveDetail();
		return	( ( this.loggedInUserIsApprover(viewable) ) 
					&&	( viewable.isSubmitted() 
							|| viewable.isPendingAmendment() 
							|| viewable.isPendingWithdrawal() 
						)
			);
	}

	public boolean isChangeOfSupervisorAllowed() {
		LeaveDetailRow  viewable = this.getViewableLeaveDetail();
		return	(	( this.loggedInUserIsEmployee(viewable)	|| this.loggedInUserIsSubmitter(viewable) ) 
						&&	( viewable.isSubmitted() 
								|| viewable.isPendingAmendment() 
								|| viewable.isPendingWithdrawal()
							)
		);
	}
	
	private boolean loggedInUserIsEmployee(LeaveDetailRow lrDetail) {
		return ( this.loggedInUser.getUserId() == this.employeeUserId);
	}
	
	private boolean loggedInUserIsSubmitter(LeaveDetailRow lrDetail) {
		return ( this.loggedInUser.getUserId() == lrDetail.getSubmitterUserId());
	}
	
	private boolean loggedInUserIsApprover(LeaveDetailRow lrDetail) {
		return ( this.loggedInUser.getUserId() == lrDetail.getApproverUserId());
	}	
}