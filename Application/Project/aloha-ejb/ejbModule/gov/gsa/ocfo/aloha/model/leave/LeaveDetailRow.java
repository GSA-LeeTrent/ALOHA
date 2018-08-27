package gov.gsa.ocfo.aloha.model.leave;

import gov.gsa.ocfo.aloha.model.PayPeriod;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveStatus;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LeaveDetailRow implements Serializable {
	private static final long serialVersionUID = 3439403174226898581L;

	// USED FOR INTERNAL INPSECTION PURPOSES
	private long detailId;
	private long submitterUserId;
	private long approverUserId;
	private Date lastUpdated;
	private LeaveHeaderRow headerRow;
	
	// USED FOR DISPLAY PURPOSES
	private String submitterName;
	private String payPeriodDateRange;
	private String statusCode;
	private String statusLabel;
	
	public LeaveDetailRow(long lrDetailId, long submitterUserId, long approverUserId, Date ppStartDate, Date ppEndDate, String statusCode, String statusLabel, 
			String submitterFirstName, String submitterLastName, Date lastUpdated) {
		
		// LR_DETAIL_ID
		this.detailId = lrDetailId;
		
		// SUBMITTER_USER_ID
		this.submitterUserId = submitterUserId;
		
		//APPROVER_USER_ID
		this.approverUserId = approverUserId;

		// DATE LAST UPDATED (LR_DETAIL)
		this.lastUpdated = lastUpdated;

		// PAY PERIOD DATE RANGE
		SimpleDateFormat sdf = new SimpleDateFormat(PayPeriod.LABEL_FORMAT);
		StringBuilder sb = new StringBuilder();
		sb.append(sdf.format(ppStartDate));
		sb.append(" - ");
		sb.append(sdf.format(ppEndDate));
		this.payPeriodDateRange = sb.toString();

		// LEAVE STATUS CODE
		this.statusCode = statusCode;
		
		// LEAVE STATUS LABEL
		this.statusLabel = statusLabel;
		
		// SUBMITTER NAME
		this.submitterName = submitterFirstName + " " + submitterLastName;
	}

	public void setHeaderRow(LeaveHeaderRow headerRow) {
		this.headerRow = headerRow;
	}
	public String getRequestId() {
		return this.headerRow.getRequestId();
	}
	public String getEmployeeName() {
		return this.headerRow.getEmployeeName();
	}
	public String getSubmitterName() {
		return submitterName;
	}
	public String getPayPeriodDateRange() {
		return payPeriodDateRange;
	}
	public String getStatusCode() {
		return statusCode;
	}
	public String getStatusLabel() {
		return statusLabel;
	}
	public Date getDateSubmitted() {
		return this.headerRow.getDateSubmitted();
	}	
	public long getDetailId() {
		return detailId;
	}
	public long getSubmitterUserId() {
		return submitterUserId;
	}
	public long getApproverUserId() {
		return approverUserId;
	}
	public Date getLastUpdated() {
		return lastUpdated;
	}
	public boolean isCancelable() {
		return this.headerRow.isCancelable();
	}
	public boolean isAmendable() {
		return this.headerRow.isAmendable();
	}
	public boolean isWithdrawable() {
		return this.headerRow.isWithdrawable();
	}
	public boolean isApprovable() {
		return this.headerRow.isApprovable();
	}
	public boolean isChangeOfSupervisorAllowed() {
		return this.headerRow.isChangeOfSupervisorAllowed();
	}		
	public boolean isSubmitted() {
		return this.getStatusCode().equals(LeaveStatus.CodeValues.SUBMITTED);
	}
	public boolean isApproved() {
		return this.getStatusCode().equals(LeaveStatus.CodeValues.APPROVED);
	}
	public boolean isPendingAmendment() {
		return this.getStatusCode().equals(LeaveStatus.CodeValues.PEND_AMEND);
	}
	public boolean isPendingWithdrawal() {
		return this.getStatusCode().equals(LeaveStatus.CodeValues.PEND_WITHDRAW);
	}
}