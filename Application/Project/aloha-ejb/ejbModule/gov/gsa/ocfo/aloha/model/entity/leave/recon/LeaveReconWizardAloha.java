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

@Entity
@Table(name="LR_RECON_WIZARD_ALOHA", schema="ALOHA")
public class LeaveReconWizardAloha implements Serializable {

	private static final long serialVersionUID = 5242999171812363580L;

	@Id
	@Column(name="PK_ID", unique=true)
	private String primaryKey;
	
	@ManyToOne
	@JoinColumn(name="WIZARD_ID", referencedColumnName="WIZARD_ID", nullable=false)
	private LeaveReconWizard leaveReconWizard;
	
	@Temporal( TemporalType.TIMESTAMP)
	@Column(name="LEAVE_DATE")
	private Date leaveDate;
	
    @ManyToOne
	@JoinColumn(name="LR_TYPE_ID",  referencedColumnName="LR_TYPE_ID", nullable=false)
	private LeaveType leaveType;
	
	@Column(name="LEAVE_HOURS")
	private BigDecimal leaveHours;

	public String getPrimaryKey() {
		return primaryKey;
	}

	public void setPrimaryKey(String primaryKey) {
		this.primaryKey = primaryKey;
	}

	public LeaveReconWizard getLeaveReconWizard() {
		return leaveReconWizard;
	}

	public void setLeaveReconWizard(LeaveReconWizard leaveReconWizard) {
		this.leaveReconWizard = leaveReconWizard;
	}

	public Date getLeaveDate() {
		return leaveDate;
	}

	public void setLeaveDate(Date leaveDate) {
		this.leaveDate = leaveDate;
	}

	public LeaveType getLeaveType() {
		return leaveType;
	}

	public void setLeaveType(LeaveType leaveType) {
		this.leaveType = leaveType;
	}

	public BigDecimal getLeaveHours() {
		return leaveHours;
	}

	public void setLeaveHours(BigDecimal leaveHours) {
		this.leaveHours = leaveHours;
	}
	
	public String getLeaveItemMapKey() {
		StringBuilder sb = new StringBuilder();
		sb.append(Long.valueOf(this.leaveType.getId()).toString());
		sb.append("-");
		sb.append(new SimpleDateFormat("yyyyMMdd").format(this.leaveDate));
		return sb.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((primaryKey == null) ? 0 : primaryKey.hashCode());
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
		LeaveReconWizardAloha other = (LeaveReconWizardAloha) obj;
		if (primaryKey == null) {
			if (other.primaryKey != null)
				return false;
		} else if (!primaryKey.equals(other.primaryKey))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("LeaveReconWizardAloha [primaryKey=");
		builder.append(primaryKey);
		builder.append(", leaveDate=");
		builder.append(leaveDate);
		builder.append(", leaveType=");
		builder.append(leaveType);
		builder.append(", leaveHours=");
		builder.append(leaveHours);
		builder.append("]");
		return builder.toString();
	}
}
