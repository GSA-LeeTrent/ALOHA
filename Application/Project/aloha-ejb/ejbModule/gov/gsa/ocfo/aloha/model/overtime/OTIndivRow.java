package gov.gsa.ocfo.aloha.model.overtime;

import gov.gsa.ocfo.aloha.model.entity.overtime.OTIndivStatus;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTPayPeriod;
import gov.gsa.ocfo.aloha.util.StringUtil;
import gov.gsa.ocfo.aloha.util.ot.OTIndivStatusUtil;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

public class OTIndivRow implements OTListRow, Serializable {
	// CLASS MEMBERS
	private static final long serialVersionUID = 3511293231890912616L;

	// INSTANCE MEMBERS
	private final String submissionType = "Individual";
	private String clazz = this.getClass().getSimpleName();
	private long headerId;
	private long detailId;
	private boolean fundingRequired;
	private String typeCode;
	private String typeName;
	private String statusCode;
	private String statusName;
	private String payPeriodDateRange;
	private String salaryGradeKey;
	private String employeeName;
	private String submitterName;
	private String supervisorName;
	private BigDecimal estNbrOfHrs;
	
	public OTIndivRow(long headerId,
			long detailId,
			String emplSalaryGradeKey,
			String typeCode,
			String typeName,
			String fundingRequiredStr,
			Date ppStartDate,
			Date ppEndDate,
			String indivStatusCode,
			String indivStatusName,
			String emplFirstName,
			String emplMiddleName,
			String emplLastName,
			String submFirstName,
			String submMiddleName,
			String submLastName,
			String supvFirstName,
			String supvMiddleName,
			String supvLastName,			
			BigDecimal estNbrOfHrs
	) {
		// HEADER ID
		this.headerId = headerId;
		
		// DETAIL ID
		this.detailId = detailId;
		
		// OT TYPE
		this.typeCode = typeCode;
		this.typeName = typeName;

		// FUNDING REQUIRED?
		this.fundingRequired = ( "Y".equals(fundingRequiredStr)) ? true : false ;

		// PAY PERIOD DATE RANGE
		this.payPeriodDateRange = StringUtil.buildDateRange(ppStartDate, ppEndDate, OTPayPeriod.LABEL_FORMAT);
		
		// OT INDIV STATUS
		this.statusCode = indivStatusCode;
		this.statusName = indivStatusName;
		
		// EMPLOYEE NAME
		this.employeeName = StringUtil.buildFullName(emplFirstName, emplMiddleName, emplLastName);
	
		// SUBMITTER NAME
		this.submitterName = StringUtil.buildFullName(submFirstName, submMiddleName, submLastName);
		
		// SUPERVISOR NAME
		this.supervisorName = StringUtil.buildFullName(supvFirstName, supvMiddleName, supvLastName);

		// SALARY GRADE
		this.salaryGradeKey = emplSalaryGradeKey;
		
		// EST NBR OF HOURS
		this.estNbrOfHrs = estNbrOfHrs;		
	}

	public boolean isReviewableBySupervisor() {
		return ( OTIndivStatusUtil.isReviewableBySupervisor(this.getStatusCode()));
	}

	public boolean isReceivableBySupervisor() {
		return ( OTIndivStatusUtil.isReceivableBySupervisor(this.getStatusCode()));
	}	
	public boolean isCancellableBySubmitOwn() {
		return	(OTIndivStatusUtil.isCancellableBySubmitOwn(this.getStatusCode()));
	}	
	public boolean isCancellableByOnBehalfOf()  {
		return	(OTIndivStatusUtil.isCancellableByOnBehalfOf(this.getStatusCode()));
	}
	public boolean isCancellableBySupervisor() {
		return ( OTIndivStatusUtil.isCancellableBySupervisor(this.getStatusCode()));
	}			
	public boolean isModifiableBySubmitOwn() {
		return ( OTIndivStatusUtil.isModifiableBySubmitOwn(this.getStatusCode()));
	}	
	public boolean isModifiableByOnBehalfOf() {
		return ( OTIndivStatusUtil.isModifiableByOnBehalfOf(this.getStatusCode()));
	}		
	public boolean isModifiableBySupervisor() {
		return ( OTIndivStatusUtil.isModifiableBySupervisor(this.getStatusCode()));
	}	
	public boolean isDeniableBySupervisor() {
		return ( OTIndivStatusUtil.isDeniableBySupervisor(this.getStatusCode()));		
	}		
	public boolean isApprovableBySupervisor() {
		return ( OTIndivStatusUtil.isApprovableBySupervisor(this.getStatusCode()));	
	}		
	public boolean isSubmitted() {
		return OTIndivStatus.CodeValues.SUBMITTED.equals(this.statusCode);
	}
	public boolean isResubmitted() {
		return OTIndivStatus.CodeValues.RESUBMITTED.equals(this.statusCode);
	}	
	public boolean isReceived() {
		return OTIndivStatus.CodeValues.RECEIVED.equals(this.statusCode);
	}	
	public boolean isReceivedWithMods() {
		return OTIndivStatus.CodeValues.RECEIVED_WITH_MODS.equals(this.statusCode);
	}		
	public boolean isModified() {
		return OTIndivStatus.CodeValues.MODIFIED.equals(this.statusCode);
	}		
	public String getSubmissionType() {
		return submissionType;
	}

	public long getId() {
		return detailId;
	}
	public String getClazz() {
		return clazz;
	}

	public long getHeaderId() {
		return headerId;
	}

	public long getDetailId() {
		return detailId;
	}

	public boolean isFundingRequired() {
		return fundingRequired;
	}

	public String getTypeCode() {
		return typeCode;
	}

	public String getTypeName() {
		return typeName;
	}

	public String getStatusCode() {
		return statusCode;
	}

	public String getStatusName() {
		return statusName;
	}

	public String getPayPeriodDateRange() {
		return payPeriodDateRange;
	}

	public String getSalaryGradeKey() {
		return salaryGradeKey;
	}

	public String getEmployeeName() {
		return employeeName;
	}

	public String getSubmitterName() {
		return submitterName;
	}

	public String getSupervisorName() {
		return supervisorName;
	}
	public String getReceiverName() {
		return supervisorName;
	}	

	public BigDecimal getEstNbrOfHrs() {
		return estNbrOfHrs;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (detailId ^ (detailId >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		OTIndivRow other = (OTIndivRow) obj;
		if (detailId != other.detailId)
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("OTIndivRow [submissionType=");
		builder.append(submissionType);
		builder.append(", clazz=");
		builder.append(clazz);
		builder.append(", headerId=");
		builder.append(headerId);
		builder.append(", detailId=");
		builder.append(detailId);
		builder.append(", fundingRequired=");
		builder.append(fundingRequired);
		builder.append(", typeCode=");
		builder.append(typeCode);
		builder.append(", typeName=");
		builder.append(typeName);
		builder.append(", statusCode=");
		builder.append(statusCode);
		builder.append(", statusName=");
		builder.append(statusName);
		builder.append(", payPeriodDateRange=");
		builder.append(payPeriodDateRange);
		builder.append(", salaryGradeKey=");
		builder.append(salaryGradeKey);
		builder.append(", employeeName=");
		builder.append(employeeName);
		builder.append(", submitterName=");
		builder.append(submitterName);
		builder.append(", supervisorName=");
		builder.append(supervisorName);
		builder.append(", estNbrOfHrs=");
		builder.append(estNbrOfHrs);
		builder.append("]");
		return builder.toString();
	}
}
