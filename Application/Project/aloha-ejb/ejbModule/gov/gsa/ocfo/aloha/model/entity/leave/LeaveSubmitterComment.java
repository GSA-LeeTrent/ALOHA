package gov.gsa.ocfo.aloha.model.entity.leave;

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
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Version;

/**
 * The persistent class for the LR_SUBMITTER_COMMENT database table.
 * 
 */
@Entity
@Table(name="LR_SUBMITTER_COMMENT", schema="ALOHA")
public class LeaveSubmitterComment implements Serializable {

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

	private static final long serialVersionUID = 774570524880031081L;

	@Id
	@SequenceGenerator(name="LR_SUBMITTER_COMMENT_ID_GENERATOR", sequenceName="ALOHA.SEQ_LR_SUBMITTER_COMMENT", allocationSize=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="LR_SUBMITTER_COMMENT_ID_GENERATOR")
	@Column(name="LR_SUBMITTER_COMMENT_ID", unique=true, nullable=false, precision=10)
	private long id;

    @ManyToOne
	@JoinColumn(name="LR_DETAIL_ID", referencedColumnName="LR_DETAIL_ID", nullable=false)
	private LeaveDetail leaveDetail;	

    @Column(name="COMMENT_SEQUENCE", unique=true, nullable=false, precision=5)
	private int sequence;

	@Column(name="COMMENT_TEXT", nullable=false, length=4000)
	private String comment;
	
	@Embedded
	private AuditTrail auditTrail;
	
	@Version
	@Column(name="OPT_LOCK_NBR")
	private long version;

    public LeaveSubmitterComment() {
    }
    public LeaveSubmitterComment(String lsComment) {
    	this.comment = lsComment;
    }
    public LeaveSubmitterComment(String lsComment, int seq) {
    	this.comment = lsComment;
    	this.sequence = seq;
    }
    public LeaveSubmitterComment(String lsComment, int seq, LeaveDetail detail) {
    	this.comment = lsComment;
    	this.sequence = seq;
    	this.leaveDetail = detail;
    }

    
	public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public int getSequence() {
		return this.sequence;
	}

	public void setSequence(int sequence) {
		this.sequence = sequence;
	}

	public String getComment() {
		return this.comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public AuditTrail getAuditTrail() {
		return auditTrail;
	}

	public void setAuditTrail(AuditTrail auditTrail) {
		this.auditTrail = auditTrail;
	}

	public LeaveDetail getLeaveDetail() {
		return leaveDetail;
	}

	public void setLeaveDetail(LeaveDetail leaveDetail) {
		this.leaveDetail = leaveDetail;
	}
	
	public Date getCommentDate() {
		return this.auditTrail.getDateLastUpdated();
	}
	
	public long getVersion() {
		return version;
	}
	public void setVersion(long version) {
		this.version = version;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result + sequence;
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
		LeaveSubmitterComment other = (LeaveSubmitterComment) obj;
		if (id != other.id)
			return false;
		if (sequence != other.sequence)
			return false;
		return true;
	}
}