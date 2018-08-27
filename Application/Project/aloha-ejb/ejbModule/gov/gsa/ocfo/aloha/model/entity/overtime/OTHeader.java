package gov.gsa.ocfo.aloha.model.entity.overtime;

import gov.gsa.ocfo.aloha.model.entity.AlohaUser;
import gov.gsa.ocfo.aloha.model.overtime.group.OTIndivRow;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;

@Entity
@Table(name="OT_HEADER", schema="ALOHA")
public class OTHeader implements OTIndivRow, Serializable {
	private static final long serialVersionUID = 4234154016191828900L;
	
	@Id
	@SequenceGenerator(name="OT_HEADER_ID_GENERATOR", sequenceName="ALOHA.SEQ_OT_HEADER", allocationSize=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="OT_HEADER_ID_GENERATOR")
	@Column(name="HEADER_ID", unique=true)
	private long id;

    @ManyToOne
	@JoinColumn(name="TYPE_ID", referencedColumnName="TYPE_ID", nullable=false)
	private OTType type;
	
    @ManyToOne
	@JoinColumn(name="PAY_PERIOD_KEY",  referencedColumnName="PAY_PERIOD_KEY", nullable=false)
	private OTPayPeriod payPeriod;
	
    @ManyToOne
	@JoinColumn(name="EMPL_USER_ID",  referencedColumnName="USER_ID", nullable=false)	
	private AlohaUser employee;

    @ManyToOne
	@JoinColumn(name="SALARY_GRADE_KEY",  referencedColumnName="SALARY_GRADE_KEY", nullable=false)
	private OTSalaryGrade salaryGrade;

	@ManyToOne
	@JoinColumn(name="OT_GROUP_ID", referencedColumnName="OT_GROUP_ID", nullable=true)
	private OTGroup group;
	
	@Version
	@Column(name="OPT_LOCK_NBR")
	private long version;
    
	@OneToMany(mappedBy="header", fetch=FetchType.EAGER, cascade=CascadeType.ALL)
	@OrderBy("sequence ASC")
	private List<OTDetail> details = new ArrayList<OTDetail>();

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	
	public OTType getType() {
		return type;
	}

	public void setType(OTType type) {
		this.type = type;
	}

	public OTPayPeriod getPayPeriod() {
		return payPeriod;
	}

	public void setPayPeriod(OTPayPeriod payPeriod) {
		this.payPeriod = payPeriod;
	}

	public AlohaUser getEmployee() {
		return employee;
	}

	public void setEmployee(AlohaUser employee) {
		this.employee = employee;
	}

	public OTSalaryGrade getSalaryGrade() {
		return salaryGrade;
	}

	public void setSalaryGrade(OTSalaryGrade salaryGrade) {
		this.salaryGrade = salaryGrade;
	}

	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
	}

	public List<OTDetail> getDetails() {
		return details;
	}

	public void setDetails(List<OTDetail> details) {
		this.details = details;
	}
	
	public boolean addDetail(OTDetail detail) {
		return this.details.add(detail);
	}
	
	public OTGroup getGroup() {
		return group;
	}

	public void setGroup(OTGroup group) {
		this.group = group;
	}

	public OTDetail getLatestDetail() {
		return ( (this.details.size() > 0 ) ? this.details.get(this.details.size() - 1): null);
	}

	public List<OTDetailHistory> getDetailHistory() {
		List<OTDetailHistory> historyList = new ArrayList<OTDetailHistory>();
		for ( OTDetail detail : this.details) {
			for ( OTDetailHistory history: detail.getHistoricalEntries()) {
					historyList.add(history);
			}
		}
		return historyList;
	}
	public List<OTDetailSupervisorRemark> getAllSupervisorRemarks() {
		List<OTDetailSupervisorRemark> supvRemarksList = new ArrayList<OTDetailSupervisorRemark>();
		for ( OTDetail otDetail : this.details) {
			for ( OTDetailSupervisorRemark supvRemark: otDetail.getSupervisorRemarksForThisDetailOnly()) {
				supvRemarksList.add(supvRemark);
			}
		}
		return supvRemarksList;
	}
	public List<OTDetailSubmitterRemark> getAllSubmitterRemarks() {
		List<OTDetailSubmitterRemark> submRemarksList = new ArrayList<OTDetailSubmitterRemark>();
		for ( OTDetail otDetail : this.details) {
			for ( OTDetailSubmitterRemark submRemark: otDetail.getSubmitterRemarksForThisDetailOnly()) {
				submRemarksList.add(submRemark);
			}
		}
		return submRemarksList;
	}
	public boolean isEmployeeUserId(long userId) {
		return 	( (this.employee != null) 
					&& (this.employee.getUserId() == userId)
				);
	}
	// METHOD REQUIRED BY OTIndivRow INTERFACE
	public String getRequestId() {
		return String.valueOf(this.id);
	}
	
	// METHOD REQUIRED BY OTIndivRow INTERFACE
	public String getTypeName() {
		if ( this.type != null) {
			return this.type.getCode();
		} else {
			return null;
		}
	}
	// METHOD REQUIRED BY OTIndivRow INTERFACE
	public String getTypeLabel() {
		if ( this.type != null) {
			return this.type.getLabel();
		} else {
			return null;
		}
	}		
	// METHOD REQUIRED BY OTIndivRow INTERFACE
	public long getDetailId() {
		OTDetail latestDetail = this.getLatestDetail();
		if ( latestDetail != null ) {
			return latestDetail.getId();
		} else {
			return 0L;
		}		
	}
	// METHOD REQUIRED BY OTIndivRow INTERFACE
	public String getStatusCode() {
		OTDetail latestDetail = this.getLatestDetail();
		if ( (latestDetail != null)
				&& (latestDetail.getStatus() != null)) {
			return latestDetail.getStatus().getCode();
		} else {
			return null;
		}
	}		
	// METHOD REQUIRED BY OTIndivRow INTERFACE	
	public String getStatusName() {
		OTDetail latestDetail = this.getLatestDetail();
		if ( (latestDetail != null)
				&& (latestDetail.getStatus() != null)) {
			return latestDetail.getStatus().getName();
		} else {
			return null;
		}
	}	
	// METHOD REQUIRED BY OTIndivRow INTERFACE
	public String getEmployeeName() {
		if ( this.employee != null) {
			return this.employee.getFullName();
		} else {
			return null;
		}
	}
	// METHOD REQUIRED BY OTIndivRow INTERFACE
	public BigDecimal getEstNbrOfHours() {
		return this.sumEstNbrOfHours();
	}
	// METHOD REQUIRED BY OTIndivRow INTERFACE
	public Long getGroupId() {
		if ( this.group != null ) { 
			return this.group.getId();
		} else {
			return null;
		}
	}
	// METHOD REQUIRED BY OTIndivRow INTERFACE
	public String getGroupOwnerName() {
		if ( (this.group != null) 
				&& this.group.getSubmitter() != null) {
			return this.group.getSubmitter().getFullName();
		} else {
			return null;
		}
	}
	// METHOD REQUIRED BY OTIndivRow INTERFACE
	public String getReceiverName() {
		if ( (this.group != null) 
				&& this.group.getReceiver() != null) {
			return this.group.getReceiver().getFullName();
		} else {
			return null;
		}
	}	
	// METHOD REQUIRED BY OTIndivRow INTERFACE
	public List<OTItem> getTaskItems() {
		OTDetail latestDetail = this.getLatestDetail();
		if ( (latestDetail != null)
				&& (latestDetail.getItems() != null)) {
			return latestDetail.getItems();
		} else {
			return null;
		}
	}	
	// METHOD REQUIRED BY OTIndivRow INTERFACE
	public String getPayPeriodDateRange() {
		if ( this.payPeriod != null) {
			return this.payPeriod.getShortLabel();
		} else {
			return null;
		}
	}
	// METHOD REQUIRED BY OTIndivRow INTERFACE
	public String getSubmitterName() {
		OTDetail latestDetail = this.getLatestDetail();
		if 	( (latestDetail != null)
				&& (latestDetail.getSubmitter() != null)
			) {
			return latestDetail.getSubmitter().getFullName();
		} else {
			return null;
		}
	}	
	// METHOD REQUIRED BY OTIndivRow INTERFACE
	public String getSupervisorName() {
		OTDetail latestDetail = this.getLatestDetail();
		if 	( (latestDetail != null)
				&& (latestDetail.getSupervisor() != null)
			) {
			return latestDetail.getSupervisor().getFullName();
		} else {
			return null;
		}
	}	
	
	// METHOD REQUIRED BY OTIndivRow INTERFACE
	@Transient
	private boolean modifiableBySupervisor;
	public boolean isModifiableBySupervisor() {
		return modifiableBySupervisor;
	}
	public void setModifiableBySupervisor(boolean modifiableBySupervisor) {
		this.modifiableBySupervisor = modifiableBySupervisor;
	}
	
	// METHOD REQUIRED BY OTIndivRow INTERFACE
	@Transient
	private boolean cancellableBySupervisor;
	public boolean isCancellableBySupervisor() {
		return cancellableBySupervisor;
	}
	public void setCancellableBySupervisor(boolean cancellableBySupervisor) {
		this.cancellableBySupervisor = cancellableBySupervisor;
	}

	// METHOD REQUIRED BY OTIndivRow INTERFACE
//	public boolean isCancellableBySupervisor() {
//		OTDetail latestDetail = this.getLatestDetail();
//		if ( latestDetail != null ) {
//			return latestDetail.isCancellableBySupervisor();
//		} else {
//			return false;
//		}		
//	}
	// METHOD REQUIRED BY OTIndivRow INTERFACE
	@Transient
	private boolean approvalComanded;
	public boolean isApprovalComanded() {
		return approvalComanded;
	}
	public void setApprovalComanded(boolean approvalComanded) {
		this.approvalComanded = approvalComanded;
		if (this.approvalComanded) {
			this.approvalCommandStatusChangeOutcome = OTIndivStatus.CodeValues.APPROVED;
		} else {
			this.approvalCommandStatusChangeOutcome = OTIndivStatus.CodeValues.DENIED;
		}
	}
	public String getStyleForNewStatus() {
		if (this.approvalComanded) {
			return "color:#3E7D3E;font-weight:bold;";
		} else {
			return "color:red;font-weight:bold;";
		}
	}
	// METHOD REQUIRED BY OTIndivRow INTERFACE
	@Transient
	private String approvalCommandStatusChangeOutcome;
	public String getApprovalCommandStatusChangeOutcome() {
		return this.approvalCommandStatusChangeOutcome;
	}	
	public void setApprovalCommandStatusChangeOutcome(
			String approvalCommandStatusChangeOutcome) {
		this.approvalCommandStatusChangeOutcome = approvalCommandStatusChangeOutcome;
	}

	// HELPER METHOD
	private BigDecimal sumEstNbrOfHours() {
		BigDecimal sum = BigDecimal.ZERO;
		OTDetail latestDetail = this.getLatestDetail();
		if ( latestDetail != null) {
			for ( OTItem otItem : latestDetail.getItems()) {
				sum = sum.add(otItem.getEstimatedHours());
			}
		}
		return sum;
	
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((employee == null) ? 0 : employee.hashCode());
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result
				+ ((payPeriod == null) ? 0 : payPeriod.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	public boolean isFundingRequired() {
		return 	( (this.type != null)
					&& (this.type.isFundingRequired())
				);
	}

	public List<AlohaUser> getAuthorizedIndividualsInGroup() {
		if ( this.group != null ) {
			return ( this.group.getAllAuthorizedIndividuals());
		} else {
			return null;
		}
	}
	public boolean isInGroup() {
		return (this.group != null);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		OTHeader other = (OTHeader) obj;
		if (employee == null) {
			if (other.employee != null)
				return false;
		} else if (!employee.equals(other.employee))
			return false;
		if (id != other.id)
			return false;
		if (payPeriod == null) {
			if (other.payPeriod != null)
				return false;
		} else if (!payPeriod.equals(other.payPeriod))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("OTHeader [id=");
		builder.append(id);
		builder.append(", type=");
		builder.append(type);
		builder.append(", payPeriod=");
		builder.append(payPeriod);
		builder.append(", employee=");
		builder.append(employee);
		builder.append(", salaryGrade=");
		builder.append(salaryGrade);
		builder.append(", version=");
		builder.append(version);
		builder.append(", details=");
		builder.append(details);
		builder.append("]");
		return builder.toString();
	}
}