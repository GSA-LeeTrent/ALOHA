package gov.gsa.ocfo.aloha.model.entity.leave;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name="LR_SUPV_CHANGE_HIST", schema="ALOHA")
public class LeaveSupervisorChangeHistory implements Serializable {
	private static final long serialVersionUID = -1837514658810223361L;

	@Id
	@Column(name="LR_HISTORY_ID", unique=true, nullable=false, precision=10)
	private long leaveHistoryId;
	
	@Column(name="PREV_SUPV_USER_ID", unique=false, nullable=false, precision=10)
	private long previousSupervisorUserId;
	
	@Column(name="NEW_SUPV_USER_ID", unique=false, nullable=false, precision=10)
	private long newSupervisorUserId;	
	
	@Column(name="USER_CREATED_ID", unique=false, nullable=false, precision=10)
	private long userCreatedId;	
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="DATE_CREATED ")
	private Date dateCreated;

	public long getLeaveHistoryId() {
		return leaveHistoryId;
	}

	public void setLeaveHistoryId(long leaveHistoryId) {
		this.leaveHistoryId = leaveHistoryId;
	}

	public long getPreviousSupervisorUserId() {
		return previousSupervisorUserId;
	}

	public void setPreviousSupervisorUserId(long previousSupervisorUserId) {
		this.previousSupervisorUserId = previousSupervisorUserId;
	}

	public long getNewSupervisorUserId() {
		return newSupervisorUserId;
	}

	public void setNewSupervisorUserId(long newSupervisorUserId) {
		this.newSupervisorUserId = newSupervisorUserId;
	}

	public long getUserCreatedId() {
		return userCreatedId;
	}

	public void setUserCreatedId(long userCreatedId) {
		this.userCreatedId = userCreatedId;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ (int) (leaveHistoryId ^ (leaveHistoryId >>> 32));
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
		LeaveSupervisorChangeHistory other = (LeaveSupervisorChangeHistory) obj;
		if (leaveHistoryId != other.leaveHistoryId)
			return false;
		return true;
	}
}
