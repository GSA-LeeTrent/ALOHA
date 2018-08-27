package gov.gsa.ocfo.aloha.model.entity.audit;


import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;


/**
 * The persistent class for the EVENT_TYPE database table.
 * 
 */
@Entity
@Table(name="EVENT_TYPE", schema="aloha")
@NamedQueries({
	@NamedQuery(name="findEventTypeByEventTypeCode",
	        	query="SELECT et FROM EventType et WHERE et.eventTypeCode = :eventTypeCode")
})
public class EventType implements Serializable {
	private static final long serialVersionUID = 1L;
	public static final String EVENT_TYPE_CODE_QUERY = "findEventTypeByEventTypeCode";
	public static final String EVENT_TYPE_CODE_QUERY_PARAM = "eventTypeCode";
	
	// ENUMS
	public enum EventTypeValue {USER_LOGIN, USER_LOGOUT, SESSION_TIMEOUT, 
		OT_INDIV_SUBMIT, OT_INDIV_CANCEL, OT_INDIV_RECEIVE, OT_INDIV_APPROVE, OT_INDIV_DENY,
		LR_SUBMITTED, LR_MODIFIED, LR_CANCELED, LR_APPROVED, LR_DENIED, LR_PEND_WITHDRAWAL, 
		LR_WITHDRAWN, LR_PEND_AMEND, LR_AMENDED, OT_POLICY_ACKNOWLEDGED, LR_CHANGE_SUPV,
		LR_VARIANCE_REPORT, LR_VARIANCE_ACKNOWLEDGED, LR_REPORT, LR_RECON, OT_REPORT, OT_RECON,
		USER_PREF_CREATED_NEW_RECORD, USER_PREF_CHANGED_DEFAULT_APPROVER, USER_PREF_CHANGED_PRIMARY_TIMEKEEPER
	};	
	
	
	@Id
	@SequenceGenerator(name="EVENT_TYPE_EVENTTYPEID_GENERATOR", sequenceName="aloha.SEQ_EVENT_TYPE")
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="EVENT_TYPE_EVENTTYPEID_GENERATOR")
	@Column(name="EVENT_TYPE_ID", unique=true, nullable=false, precision=10)
	private long eventTypeId;

	@Column(name="EVENT_TYPE_CODE", nullable=false, length=25)
	private String eventTypeCode;

	@Column(name="EVENT_TYPE_NAME", nullable=false, length=50)
	private String eventTypeName;
	
	@Column(name="USER_LAST_UPDATED")
	private String userLastUpdated;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="DATE_LAST_UPDATED")
	private Date dateLastUpdated;

    public EventType() {
    }

	public long getEventTypeId() {
		return this.eventTypeId;
	}

	public void setEventTypeId(long eventTypeId) {
		this.eventTypeId = eventTypeId;
	}

	public String getEventTypeCode() {
		return this.eventTypeCode;
	}

	public void setEventTypeCode(String eventTypeCode) {
		this.eventTypeCode = eventTypeCode;
	}

	public String getEventTypeName() {
		return this.eventTypeName;
	}

	public void setEventTypeName(String eventTypeName) {
		this.eventTypeName = eventTypeName;
	}

	public String getUserLastUpdated() {
		return userLastUpdated;
	}

	public void setUserLastUpdated(String userLastUpdated) {
		this.userLastUpdated = userLastUpdated;
	}

	public Date getDateLastUpdated() {
		return dateLastUpdated;
	}

	public void setDateLastUpdated(Date dateLastUpdated) {
		this.dateLastUpdated = dateLastUpdated;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((eventTypeCode == null) ? 0 : eventTypeCode.hashCode());
		result = prime * result + (int) (eventTypeId ^ (eventTypeId >>> 32));
		result = prime * result
				+ ((eventTypeName == null) ? 0 : eventTypeName.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof EventType)) {
			return false;
		}
		EventType other = (EventType) obj;
		if (eventTypeCode == null) {
			if (other.eventTypeCode != null) {
				return false;
			}
		} else if (!eventTypeCode.equals(other.eventTypeCode)) {
			return false;
		}
		if (eventTypeId != other.eventTypeId) {
			return false;
		}
		if (eventTypeName == null) {
			if (other.eventTypeName != null) {
				return false;
			}
		} else if (!eventTypeName.equals(other.eventTypeName)) {
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("EventType [eventTypeId==");
		builder.append(eventTypeId);
		builder.append(", eventTypeCode=");
		builder.append(eventTypeCode);
		builder.append(", eventTypeName=");
		builder.append(eventTypeName);
		builder.append("]");
		return builder.toString();
	}
}