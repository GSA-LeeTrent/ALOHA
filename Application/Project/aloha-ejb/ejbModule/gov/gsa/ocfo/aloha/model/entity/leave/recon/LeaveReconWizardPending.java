package gov.gsa.ocfo.aloha.model.entity.leave.recon;

import gov.gsa.ocfo.aloha.model.entity.leave.LeaveType;

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
@Table(name="LR_RECON_WIZARD_PENDING", schema="ALOHA")
public class LeaveReconWizardPending implements Serializable {

	private static final long serialVersionUID = 7713934652871511183L;

	@Id
	@Column(name="WIZARD_ITEM_ID", unique=true)
	private String wizardItemId;
	
	@ManyToOne
	@JoinColumn(name="WIZARD_ID", referencedColumnName="WIZARD_ID", nullable=false)
	private LeaveReconWizard leaveReconWizard;
	
	@Column(name="EMPL_USER_ID")
	private int employeeUserId;
	
	@Column(name="LV_YEAR")
	private int leaveYear;
	
	@Column(name="PP_NO")
	private int payPeriodNumber;
	
	@Temporal(TemporalType.DATE)
	@Column(name="LV_DATE")
	private Date leaveDate;
	
    @ManyToOne
	@JoinColumn(name="LR_TYPE_ID",  referencedColumnName="LR_TYPE_ID", nullable=false)
	private LeaveType leaveTypeObj;
	
	@Column(name="LV_TYPE_CODE")
	private String leaveTypeCode;
	
	@Column(name="LV_CORRECT_HOURS", precision=2, scale=1)
	private BigDecimal correctLeaveHours;
	
	@Column(name="LV_ALOHA_HOURS", precision=2, scale=1)
	private BigDecimal alohaLeaveHours;
	
	@Column(name="LV_ETAMS_HOURS", precision=2, scale=1)
	private BigDecimal etamsLeaveHours;

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

	public LeaveType getLeaveTypeObj() {
		return leaveTypeObj;
	}

	public void setLeaveTypeObj(LeaveType leaveTypeObj) {
		this.leaveTypeObj = leaveTypeObj;
	}

	public String getLeaveTypeCode() {
		return leaveTypeCode;
	}

	public void setLeaveTypeCode(String leaveTypeCode) {
		this.leaveTypeCode = leaveTypeCode;
	}

	public BigDecimal getCorrectLeaveHours() {
		return correctLeaveHours;
	}

	public void setCorrectLeaveHours(BigDecimal correctLeaveHours) {
		this.correctLeaveHours = correctLeaveHours;
	}

	public BigDecimal getAlohaLeaveHours() {
		return alohaLeaveHours;
	}

	public void setAlohaLeaveHours(BigDecimal alohaLeaveHours) {
		this.alohaLeaveHours = alohaLeaveHours;
	}

	public BigDecimal getEtamsLeaveHours() {
		return etamsLeaveHours;
	}

	public void setEtamsLeaveHours(BigDecimal etamsLeaveHours) {
		this.etamsLeaveHours = etamsLeaveHours;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
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
		LeaveReconWizardPending other = (LeaveReconWizardPending) obj;
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
		builder.append("LeaveReconWizardPending [wizardItemId=");
		builder.append(wizardItemId);
		builder.append(", employeeUserId=");
		builder.append(employeeUserId);
		builder.append(", leaveYear=");
		builder.append(leaveYear);
		builder.append(", payPeriodNumber=");
		builder.append(payPeriodNumber);
		builder.append(", leaveDate=");
		builder.append(leaveDate);
		builder.append(", leaveTypeCode=");
		builder.append(leaveTypeCode);
		builder.append(", correctLeaveHours=");
		builder.append(correctLeaveHours);
		builder.append(", alohaLeaveHours=");
		builder.append(alohaLeaveHours);
		builder.append(", etamsLeaveHours=");
		builder.append(etamsLeaveHours);
		builder.append("]");
		return builder.toString();
	}
}