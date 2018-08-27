package gov.gsa.ocfo.aloha.model.overtime.group;

import gov.gsa.ocfo.aloha.model.entity.overtime.OTGroup;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTHeader;

import java.math.BigDecimal;
import java.util.List;

public interface OTGroupRow {
	public String getPayPeriodDateRange();
	public Long getReceiverUserId();
	public long getSubmitterUserId();
	public String getSubmitterName();
	public String getReceiverName();
	public long getGroupId();
	public int getEmployeeCount();
	public BigDecimal getEstNbrOfHrs();
	public String getStatusName();
	public List<OTHeader> getAllEmployees();
	public List<OTGroup> getAllChildGroups();
}
