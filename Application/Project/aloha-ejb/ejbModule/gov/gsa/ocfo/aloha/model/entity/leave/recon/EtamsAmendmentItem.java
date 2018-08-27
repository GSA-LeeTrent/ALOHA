package gov.gsa.ocfo.aloha.model.entity.leave.recon;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name="ETAMS_AMENDMENTS", schema="ALOHA")
public class EtamsAmendmentItem implements Serializable {

	public interface StatusValues {
		public static final String UNPROCESSED = "U";
	}
	
	private static final long serialVersionUID = 4416152520423870264L;

	@Id
	@Column(name="WIZARD_ITEM_ID", unique=true)
	private String wizardItemId;
	
	@ManyToOne
	@JoinColumn(name="WIZARD_ID", referencedColumnName="WIZARD_ID", nullable=false)
	private LeaveReconWizard leaveReconWizard;
	
	@Column(name="USER_ID", nullable=false)
	private int employeeUserId;
	
	@Column(name="YEARX", nullable=false)
	private int leaveYear;
	
	@Column(name="PAY_PERIOD")
	private int payPeriodNumber;
	
	@Temporal(TemporalType.DATE)
	@Column(name="LEAVE_DATE")
	private Date leaveDate;
	
	@Column(name="HOURS", precision=2, scale=1)
	private BigDecimal correctLeaveHours;
	
	@Column(name="LEAVE_CODE")
	private String primaryLeaveTypeCode;
	
	@Column(name="SECONDARY_CODE")
	private String secondaryLeaveTypeCode;
	
	@Column(name="STATUS")
	private String status;

	public String getWizardItemId() {
		return wizardItemId;
	}

	public void setWizardItemId(String wizardItemId) {
		this.wizardItemId = wizardItemId;
	}

	public LeaveReconWizard getLeaveReconWizard() {
		return leaveReconWizard;
	}

	public void setLeaveReconWizard(LeaveReconWizard leaveReconWizard) {
		this.leaveReconWizard = leaveReconWizard;
	}

	public int getEmployeeUserId() {
		return employeeUserId;
	}

	public void setEmployeeUserId(int employeeUserId) {
		this.employeeUserId = employeeUserId;
	}

	public int getLeaveYear() {
		return leaveYear;
	}

	public void setLeaveYear(int leaveYear) {
		this.leaveYear = leaveYear;
	}

	public int getPayPeriodNumber() {
		return payPeriodNumber;
	}

	public void setPayPeriodNumber(int payPeriodNumber) {
		this.payPeriodNumber = payPeriodNumber;
	}

	public Date getLeaveDate() {
		return leaveDate;
	}

	public void setLeaveDate(Date leaveDate) {
		this.leaveDate = leaveDate;
	}

	public BigDecimal getCorrectLeaveHours() {
		return correctLeaveHours;
	}

	public void setCorrectLeaveHours(BigDecimal correctLeaveHours) {
		this.correctLeaveHours = correctLeaveHours;
	}

	public String getPrimaryLeaveTypeCode() {
		return primaryLeaveTypeCode;
	}

	public void setPrimaryLeaveTypeCode(String primaryLeaveTypeCode) {
		this.primaryLeaveTypeCode = primaryLeaveTypeCode;
	}

	public String getSecondaryLeaveTypeCode() {
		return secondaryLeaveTypeCode;
	}

	public void setSecondaryLeaveTypeCode(String secondaryLeaveTypeCode) {
		this.secondaryLeaveTypeCode = secondaryLeaveTypeCode;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((wizardItemId == null) ? 0 : wizardItemId.hashCode());
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
		EtamsAmendmentItem other = (EtamsAmendmentItem) obj;
		if (wizardItemId == null) {
			if (other.wizardItemId != null)
				return false;
		} else if (!wizardItemId.equals(other.wizardItemId))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("EtamsAmendmentItem [wizardItemId=");
		builder.append(wizardItemId);
		builder.append(", employeeUserId=");
		builder.append(employeeUserId);
		builder.append(", leaveYear=");
		builder.append(leaveYear);
		builder.append(", payPeriodNumber=");
		builder.append(payPeriodNumber);
		builder.append(", leaveDate=");
		builder.append(leaveDate);
		builder.append(", correctLeaveHours=");
		builder.append(correctLeaveHours);
		builder.append(", primaryLeaveTypeCode=");
		builder.append(primaryLeaveTypeCode);
		builder.append(", secondaryLeaveTypeCode=");
		builder.append(secondaryLeaveTypeCode);
		builder.append(", status=");
		builder.append(status);
		builder.append("]");
		return builder.toString();
	}
}
