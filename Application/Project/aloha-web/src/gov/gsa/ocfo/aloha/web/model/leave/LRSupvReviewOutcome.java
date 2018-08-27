package gov.gsa.ocfo.aloha.web.model.leave;

import gov.gsa.ocfo.aloha.model.entity.leave.LeaveDetail;

import java.io.Serializable;

public class LRSupvReviewOutcome implements Serializable {
	private static final long serialVersionUID = -7365771399927884144L;

	private LeaveDetail leaveDetail;
	private boolean approved;
	private boolean amendmentRequest;
	private boolean withdrawalRequest;
	private boolean newRequest;
	private String approverRemarks;
	
	public LRSupvReviewOutcome(LeaveDetail ld) {
		this.leaveDetail = ld;
		this.amendmentRequest = this.leaveDetail.isPendingAmendment();
		this.withdrawalRequest = this.leaveDetail.isPendingWithdrawal();
		this.newRequest = this.leaveDetail.isSubmitted();
	}

	public LeaveDetail getLeaveDetail() {
		return leaveDetail;
	}

	public boolean isApproved() {
		return approved;
	}
	
	public void setApproved(boolean approved) {
		this.approved = approved;
	}

	public boolean isAmendmentRequest() {
		return amendmentRequest;
	}

	public boolean isWithdrawalRequest() {
		return withdrawalRequest;
	}

	public boolean isNewRequest() {
		return newRequest;
	}

	public String getApproverRemarks() {
		return approverRemarks;
	}

	public void setApproverRemarks(String approverRemarks) {
		this.approverRemarks = approverRemarks;
	}
}