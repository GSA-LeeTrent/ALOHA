package gov.gsa.ocfo.aloha.model.entity.leave.recon;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="LR_RECON_WIZARD_XREF", schema="ALOHA")
public class LeaveReconWizardXref implements Serializable {
	private static final long serialVersionUID = 1690010761711763535L;

	@Id
	@Column(name="PK_ID", unique=true)
	private String primaryKey;
	
	@ManyToOne
	@JoinColumn(name="WIZARD_ID", referencedColumnName="WIZARD_ID", nullable=false)
	private LeaveReconWizard leaveReconWizard;
	
	@Column(name="LR_DETAIL_ID", unique=true)
	private long leaveDetailId;

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

	public long getLeaveDetailId() {
		return leaveDetailId;
	}

	public void setLeaveDetailId(long leaveDetailId) {
		this.leaveDetailId = leaveDetailId;
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
		LeaveReconWizardXref other = (LeaveReconWizardXref) obj;
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
		builder.append("LeaveReconWizardXref [primaryKey=");
		builder.append(primaryKey);
		builder.append(", leaveDetailId=");
		builder.append(leaveDetailId);
		builder.append("]");
		return builder.toString();
	}
}
