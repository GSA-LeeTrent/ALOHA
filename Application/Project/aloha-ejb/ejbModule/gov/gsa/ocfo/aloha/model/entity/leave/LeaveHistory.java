package gov.gsa.ocfo.aloha.model.entity.leave;

import gov.gsa.ocfo.aloha.model.entity.AlohaUser;
import gov.gsa.ocfo.aloha.model.entity.AuditTrail;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

/**
 * The persistent class for the LR_HISTORY database table.
 * 
 */
@Entity
@Table(name="LR_HISTORY", schema="ALOHA")
public class LeaveHistory implements Serializable {

   /********************************************************************************************
  	************************************IMPORTANT***********************************************
  	********************************************************************************************
	Starting with the 0.7.5 release, ALOHA will NO LONGER write to the following tables:
	1) LR_APPROVER_COMMENT
	2) LR_SUBMITTER_COMMENT
	
	Instead, all remarks will be inserted into the LR_HISTORY table, using the REMARKS column.
	
	ALOHA will continue to read from these tables for backwards compatibility purposes. To display
	all previous remarks, ALOHA will read from the following tables:
	1) LR_APPROVER_COMMENT
	2) LR_SUBMITTER_COMMENT
	3) LR_HISTORY
	
	To see how this is being done in the Java ALOHA application, look at the "getAllRemarks()"  
	method in gov.gsa.ocfo.aloha.model.entity.leave.LeaveHeader.
	
	-- Lee Trent (09/23/2013)
    ********************************************************************************************/	

	private static final long serialVersionUID = 4634786466549111520L;

	@Id
	@SequenceGenerator(name="LR_HISTORY_ID_GENERATOR", sequenceName="ALOHA.SEQ_LR_HISTORY", allocationSize=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="LR_HISTORY_ID_GENERATOR")
	@Column(name="LR_HISTORY_ID", unique=true, nullable=false, precision=10)
	private long id;

	//bi-directional many-to-one association to LeaveDetail
    @ManyToOne
	@JoinColumn(name="LR_DETAIL_ID", referencedColumnName="LR_DETAIL_ID", nullable=false)
	private LeaveDetail leaveDetail;

	//uni-directional many-to-one association to LeaveStatusTransition
    @ManyToOne
	@JoinColumn(name="LR_STATUS_TRANSITION_ID", referencedColumnName="LR_STATUS_TRANSITION_ID", nullable=false)
	private LeaveStatusTransition leaveStatusTransition;
   
    @ManyToOne
	@JoinColumn(name="USER_ID",  referencedColumnName="USER_ID", nullable=false)	
	private AlohaUser actor;
	
    @Temporal( TemporalType.TIMESTAMP)
	@Column(name="ACTION_DATETIME")
	private Date actionDatetime;
    
	@Embedded
	private AuditTrail auditTrail;

	@Version
	@Column(name="OPT_LOCK_NBR")
	private long version;
	
	@Column(name="REMARKS", nullable=true, length=4000)
	private String remarks;
	
	@OneToOne
	@PrimaryKeyJoinColumn(name="LR_HISTORY_ID", referencedColumnName="LR_HISTORY_ID")
	private LeaveSupervisorChangeHistory supervisorChangeHistory;	
	
    public LeaveHistory() {
    }

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public LeaveDetail getLeaveDetail() {
		return leaveDetail;
	}

	public void setLeaveDetail(LeaveDetail leaveDetail) {
		this.leaveDetail = leaveDetail;
	}

	public LeaveStatusTransition getLeaveStatusTransition() {
		return leaveStatusTransition;
	}

	public void setLeaveStatusTransition(LeaveStatusTransition leaveStatusTransition) {
		this.leaveStatusTransition = leaveStatusTransition;
	}

	public AlohaUser getActor() {
		return actor;
	}

	public void setActor(AlohaUser actor) {
		this.actor = actor;
	}

	public Date getActionDatetime() {
		return actionDatetime;
	}

	public void setActionDatetime(Date actionDatetime) {
		this.actionDatetime = actionDatetime;
	}

	public AuditTrail getAuditTrail() {
		return auditTrail;
	}

	public void setAuditTrail(AuditTrail auditTrail) {
		this.auditTrail = auditTrail;
	}

	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public LeaveSupervisorChangeHistory getSupervisorChangeHistory() {
		return supervisorChangeHistory;
	}

	public void setSupervisorChangeHistory(
			LeaveSupervisorChangeHistory supervisorChangeHistory) {
		this.supervisorChangeHistory = supervisorChangeHistory;
	}	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
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
		LeaveHistory other = (LeaveHistory) obj;
		if (id != other.id)
			return false;
		return true;
	}
}