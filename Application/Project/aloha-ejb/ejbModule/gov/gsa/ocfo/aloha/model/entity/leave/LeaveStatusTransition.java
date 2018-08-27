package gov.gsa.ocfo.aloha.model.entity.leave;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;


/**
 * The persistent class for the LR_STATE_TRANSITION database table.
 * 
 */
@Entity
@Table(name="LR_STATUS_TRANSITION", schema="ALOHA")
@NamedQueries({
	@NamedQuery(name="findByActionCode",
	        	query="SELECT row FROM LeaveStatusTransition row WHERE row.actionCode = :actionCode"),
	@NamedQuery(name="retrieveAllStatusTransitions", 
				query="SELECT row FROM LeaveStatusTransition row")	        	
})
public class LeaveStatusTransition implements Serializable {
	private static final long serialVersionUID = -3678901326461557015L;

	public interface QueryNames{
		public static final String FIND_BY_ACTION_CODE = "findByActionCode";
		public static final String RETRIEVE_ALL = "retrieveAllStatusTransitions";
	}
	public interface QueryParamNames {
		public static final String FIND_BY_ACTION_CODE = "actionCode";	
	}
	public interface ActionCodeValues {
		public static final String NONE_TO_DRAFT 				= "NONE_TO_DRAFT";	
		public static final String NONE_TO_SUBMIT 				= "NONE_TO_SUBMIT";
		public static final String DRAFT_TO_SUBMIT 				= "DRAFT_TO_SUBMIT";
		public static final String SUBMIT_TO_CANCELLED 			= "SUBMIT_TO_CANCELLED";
		public static final String SUBMIT_TO_APPROVED 			= "SUBMIT_TO_APPROVED";
		public static final String SUBMIT_TO_DENIED 			= "SUBMIT_TO_DENIED";
		public static final String APPROVED_TO_PEND_WITHDRAW	= "APPROVED_TO_PEND_WITHDRAW";
		public static final String PEND_WITHDRAW_TO_WITHDRAWN 	= "PEND_WITHDRAW_TO_WITHDRAWN";
		public static final String PEND_WITHDRAW_TO_APPROVED 	= "PEND_WITHDRAW_TO_APPROVED";
		public static final String APPROVED_TO_PEND_AMEND 		= "APPROVED_TO_PEND_AMEND";
		public static final String PEND_ANEND_TO_AMENDED 		= "PEND_ANEND_TO_AMENDED";
		public static final String PEND_AMEND_TO_APPROVED 		= "PEND_AMEND_TO_APPROVED";
		public static final String PEND_AMEND_TO_DENIED 		= "PEND_AMEND_TO_DENIED";
		
		public static final String SUBMIT_TO_SUBMIT 				= "SUBMIT_TO_SUBMIT";
		public static final String PEND_AMENED_TO_PEND_AMEND 		= "PEND_AMENED_TO_PEND_AMEND";
		public static final String PEND_WITHDRAW_TO_PEND_WITHDRAW 	= "PEND_WITHDRAW_TO_PEND_WITHDRAW";
	}	

	@Id
	@Column(name="LR_STATUS_TRANSITION_ID", unique=true, nullable=false, precision=10)
	private long id;

    @ManyToOne
	@JoinColumn(name="FROM_STATUS_ID", referencedColumnName="LR_STATUS_ID", nullable=false)
	private LeaveStatus fromStatus;

    @ManyToOne
	@JoinColumn(name="TO_STATUS_ID", referencedColumnName="LR_STATUS_ID", nullable=false)
	private LeaveStatus toStatus;

    @Column(name="ACTION_CODE", nullable=false, length=25)
	private String actionCode;

	@Column(name="ACTION_LABEL", nullable=false, length=25)
	private String actionLabel;

	@Column(name="ACTION_DESC", nullable=false, length=100)
	private String actionDescription;
	
    public LeaveStatusTransition() {
    }

	public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public LeaveStatus getFromStatus() {
		return fromStatus;
	}

	public void setFromStatus(LeaveStatus fromStatus) {
		this.fromStatus = fromStatus;
	}

	public LeaveStatus getToStatus() {
		return toStatus;
	}

	public void setToStatus(LeaveStatus toStatus) {
		this.toStatus = toStatus;
	}

	public String getActionCode() {
		return actionCode;
	}

	public void setActionCode(String actionCode) {
		this.actionCode = actionCode;
	}

	public String getActionLabel() {
		return actionLabel;
	}

	public void setActionLabel(String actionLabel) {
		this.actionLabel = actionLabel;
	}

	public String getActionDescription() {
		return actionDescription;
	}

	public void setActionDescription(String actionDescription) {
		this.actionDescription = actionDescription;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((actionCode == null) ? 0 : actionCode.hashCode());
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
		LeaveStatusTransition other = (LeaveStatusTransition) obj;
		if (actionCode == null) {
			if (other.actionCode != null)
				return false;
		} else if (!actionCode.equals(other.actionCode))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("LeaveStatusTransition [id=");
		builder.append(id);
		builder.append(", fromStatus=");
		builder.append(fromStatus);
		builder.append(", toStatus=");
		builder.append(toStatus);
		builder.append(", actionCode=");
		builder.append(actionCode);
		builder.append(", actionLabel=");
		builder.append(actionLabel);
		builder.append(", actionDescription=");
		builder.append(actionDescription);
		builder.append("]");
		return builder.toString();
	}
}