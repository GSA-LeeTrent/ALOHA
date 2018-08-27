package gov.gsa.ocfo.aloha.model.overtime;

import java.math.BigDecimal;

public interface OTListRow {
	public long getHeaderId(); // HEADER ID
	public long getId(); // DETAIL ID
	public String getClazz();
	public String getSubmissionType() ;
	public String getPayPeriodDateRange();
	public String getTypeCode();
	public String getTypeName();
	public boolean isFundingRequired();
	public String getStatusCode();
	public String getStatusName();
	public String getEmployeeName();
	public String getSubmitterName();
	public String getSupervisorName();
	public String getReceiverName();
	public BigDecimal getEstNbrOfHrs();
}