package gov.gsa.ocfo.aloha.model.overtime.group;

import gov.gsa.ocfo.aloha.model.entity.overtime.OTItem;

import java.math.BigDecimal;
import java.util.List;

public interface OTIndivRow {
	public String getTypeName();
	public String getTypeLabel();
	public String getStatusCode();
	public String getStatusName();
	public String getEmployeeName();
	public BigDecimal getEstNbrOfHours();
	public String getGroupOwnerName();
	public List<OTItem> getTaskItems();
	public String getPayPeriodDateRange();
	public boolean isFundingRequired();
	public String getSubmitterName();
	public String getSupervisorName();
	public String getReceiverName();
	public String getRequestId();
	public long getDetailId();
	public Long getGroupId();
	
	public boolean isModifiableBySupervisor();
	public boolean isCancellableBySupervisor();
	
	public boolean isApprovalComanded();
	public void setApprovalComanded(boolean approvalComanded);
	
	public String getApprovalCommandStatusChangeOutcome();
	public void setApprovalCommandStatusChangeOutcome(String approvalCommandStatusChangeOutcome); 
}
