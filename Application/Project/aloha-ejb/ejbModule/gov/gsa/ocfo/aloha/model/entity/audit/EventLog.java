package gov.gsa.ocfo.aloha.model.entity.audit;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;


/**
 * The persistent class for the EVENT_LOG database table.
 * 
 */
@Entity
@Table(name="EVENT_LOG", schema="aloha")
public class EventLog implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@Id
	@SequenceGenerator(name="EVENT_LOG_EVENTLOGID_GENERATOR", sequenceName="aloha.SEQ_EVENT_LOG", allocationSize=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="EVENT_LOG_EVENTLOGID_GENERATOR")
	@Column(name="EVENT_LOG_ID", unique=true, nullable=false, precision=10)
	private long eventLogId;

		//uni-directional many-to-one association to EventType
    @ManyToOne
	@JoinColumn(name="EVENT_TYPE_ID", nullable=false)
	private EventType eventType;

	@Column(name="OLD_VALUE", nullable=true, length=4000)
	private String oldValue;
	
	@Column(name="NEW_VALUE", nullable=true, length=4000)
	private String newValue;
	
    @Temporal( TemporalType.DATE)
	@Column(name="DATE_CREATED", nullable=false)
	private Date dateCreated;

    @Column(name="USER_CREATED", nullable=false, length=50)
	private String userCreated;


    public EventLog() {
    }

	public long getEventLogId() {
		return this.eventLogId;
	}

	public void setEventLogId(long eventLogId) {
		this.eventLogId = eventLogId;
	}

	public Date getDateCreated() {
		return this.dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public String getUserCreated() {
		return this.userCreated;
	}

	public void setUserCreated(String userCreated) {
		this.userCreated = userCreated;
	}

	public EventType getEventType() {
		return this.eventType;
	}

	public void setEventType(EventType eventType) {
		this.eventType = eventType;
	}

	public String getOldValue() {
		return oldValue;
	}

	public void setOldValue(String oldValue) {
		this.oldValue = oldValue;
	}

	public String getNewValue() {
		return newValue;
	}

	public void setNewValue(String newValue) {
		this.newValue = newValue;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((dateCreated == null) ? 0 : dateCreated.hashCode());
		result = prime * result + (int) (eventLogId ^ (eventLogId >>> 32));
		result = prime * result
				+ ((eventType == null) ? 0 : eventType.hashCode());
		result = prime * result
				+ ((newValue == null) ? 0 : newValue.hashCode());
		result = prime * result
				+ ((oldValue == null) ? 0 : oldValue.hashCode());
		result = prime * result
				+ ((userCreated == null) ? 0 : userCreated.hashCode());
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
		EventLog other = (EventLog) obj;
		if (dateCreated == null) {
			if (other.dateCreated != null)
				return false;
		} else if (!dateCreated.equals(other.dateCreated))
			return false;
		if (eventLogId != other.eventLogId)
			return false;
		if (eventType == null) {
			if (other.eventType != null)
				return false;
		} else if (!eventType.equals(other.eventType))
			return false;
		if (newValue == null) {
			if (other.newValue != null)
				return false;
		} else if (!newValue.equals(other.newValue))
			return false;
		if (oldValue == null) {
			if (other.oldValue != null)
				return false;
		} else if (!oldValue.equals(other.oldValue))
			return false;
		if (userCreated == null) {
			if (other.userCreated != null)
				return false;
		} else if (!userCreated.equals(other.userCreated))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("EventLog [eventLogId=");
		builder.append(eventLogId);
		builder.append(", eventType=");
		builder.append(eventType.getEventTypeCode());
		builder.append(", oldValue=");
		builder.append(oldValue);
		builder.append(", newValue=");
		builder.append(newValue);
		builder.append(", userCreated=");
		builder.append(userCreated);
		builder.append(", dateCreated=");
		builder.append(dateCreated);
		builder.append("]");
		return builder.toString();
	}
}