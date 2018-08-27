package gov.gsa.ocfo.aloha.model.entity.leave.recon;

import gov.gsa.ocfo.aloha.model.entity.leave.LeaveType;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

@Entity
@Table(name="LR_RECON_WIZARD_ITEM", schema="ALOHA")
public class LeaveReconWizardItem implements Serializable {

	private static final long serialVersionUID = 8621959554004031777L;
	
	public interface SystemIsCorrectValues {
		public static final int NOT_SELECTED 		= 0;
		public static final int ALOHA_IS_CORRECT 	= 1;
		public static final int ETAMS_IS_CORRECT 	= 2;
		public static final int NEITHER_IS_CORRECT 	= 3;
	}
	
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
	
	@Column(name="LV_HOURS_ALOHA", precision=2, scale=1)
	private BigDecimal alohaLeaveHours;
	
	@Column(name="LV_HOURS_ETAMS", precision=2, scale=1)
	private BigDecimal etamsLeaveHours;
	
	@Transient
	private String inputAlohaLeaveHours;
	
	@Transient
	private String inputEtamsLeaveHours;

	@Transient
	private BigDecimal displayEtamsLeaveHours;

	@Transient
	private BigDecimal displayAlohaLeaveHours;
	
	@Transient
	private BigDecimal correctLeaveHours;
	
	@Transient
	private String whichSystemIsCorrect = "0";
	
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

	public BigDecimal getAlohaLeaveHours() {
		return alohaLeaveHours;
	}
	
	public String getAlohaLeaveHoursId() {
		return "alohaLeaveHours_" + this.wizardItemId;
	}

	public void setAlohaLeaveHours(BigDecimal alohaLeaveHours) {
		this.alohaLeaveHours = alohaLeaveHours;
	}

	public BigDecimal getEtamsLeaveHours() {
		return etamsLeaveHours;
	}

	public String getEtamsLeaveHoursId() {
		return "etamsLeaveHours" + this.wizardItemId;
	}
	
	public void setEtamsLeaveHours(BigDecimal etamsLeaveHours) {
		this.etamsLeaveHours = etamsLeaveHours;
	}
	
	public String getWhichSystemIsCorrect() {
		return whichSystemIsCorrect;
	}

	public void setWhichSystemIsCorrect(String whichSystemIsCorrect) {
		/* System.out.println("SetWhichSystemIsCorrect: " + whichSystemIsCorrect); */
		this.whichSystemIsCorrect = whichSystemIsCorrect;
	}
	
	public int getAlohaIsCorrectEnum() {
		return SystemIsCorrectValues.ALOHA_IS_CORRECT;
	}

	public int getEtamsIsCorrectEnum() {
		return SystemIsCorrectValues.ETAMS_IS_CORRECT;
	}
	
	public int getNeitherIsCorrectEnum() {
		return SystemIsCorrectValues.NEITHER_IS_CORRECT;
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

	public String  getInputAlohaLeaveHours() {
		return inputAlohaLeaveHours;
	}

	public void setInputAlohaLeaveHours(String  inputAlohaLeaveHours) {
		this.inputAlohaLeaveHours = inputAlohaLeaveHours;
	}

	public String  getInputEtamsLeaveHours() {
		return inputEtamsLeaveHours;
	}

	public void setInputEtamsLeaveHours(String inputEtamsLeaveHours) {
		this.inputEtamsLeaveHours = inputEtamsLeaveHours;
	}
	
	
	public BigDecimal getDisplayEtamsLeaveHours() {
		return displayEtamsLeaveHours;
	}

	public void setDisplayEtamsLeaveHours(BigDecimal displayEtamsLeaveHours) {
		this.displayEtamsLeaveHours = displayEtamsLeaveHours;
	}
	
	public BigDecimal getDisplayAlohaLeaveHours() {
		return displayAlohaLeaveHours;
	}

	public void setDisplayAlohaLeaveHours(BigDecimal displayAlohaLeaveHours) {
		this.displayAlohaLeaveHours = displayAlohaLeaveHours;
	}	
	
	public String getLeaveType() {

		if ( this.leaveTypeObj != null ) {
			return this.leaveTypeObj.getLabel();
		}
		return null;
	} 

	public LeaveType getLeaveTypeObj() {
		return leaveTypeObj;
	}

	public void setLeaveTypeObj(LeaveType leaveTypeObj) {
		this.leaveTypeObj = leaveTypeObj;
	}
	
	public String getLeaveItemMapKey() {
		StringBuilder sb = new StringBuilder();
		sb.append(Long.valueOf(this.leaveTypeObj.getId()).toString());
		sb.append("-");
		sb.append(new SimpleDateFormat("yyyyMMdd").format(this.leaveDate));
		return sb.toString();
	}
	public boolean isNotSelected() {
		 
		 return SystemIsCorrectValues.NOT_SELECTED == Integer.valueOf(this.whichSystemIsCorrect).intValue();
	}
	
	public boolean isAlohaCorrect() {
		 
		 return SystemIsCorrectValues.ALOHA_IS_CORRECT == Integer.valueOf(this.whichSystemIsCorrect).intValue();
	}
	
	public boolean isEtamsCorrect() {
		return SystemIsCorrectValues.ETAMS_IS_CORRECT == Integer.valueOf(this.whichSystemIsCorrect).intValue();
	}
	
	public boolean isNeitherCorrect() {
		return SystemIsCorrectValues.NEITHER_IS_CORRECT == Integer.valueOf(this.whichSystemIsCorrect).intValue();
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
		LeaveReconWizardItem other = (LeaveReconWizardItem) obj;
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
		builder.append("LeaveReconWizardItem [wizardItemId=");
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
		builder.append(", alohaLeaveHours=");
		builder.append(alohaLeaveHours);
		builder.append(", etamsLeaveHours=");
		builder.append(etamsLeaveHours);
		builder.append(", correctLeaveHours=");
		builder.append(correctLeaveHours);
		builder.append(", whichSystemIsCorrect=");
		builder.append(whichSystemIsCorrect);
		builder.append("]");
		return builder.toString();
	}
	
	public boolean isDisabledVetLeaveViewItem() {
		
		if ( this.leaveTypeObj != null ) {
			return this.leaveTypeObj.getPrimaryCode().equals(LeaveType.HardCoding.DISABLED_VET_PRIMARY_CODE);
		}
		return false;
	}
}
