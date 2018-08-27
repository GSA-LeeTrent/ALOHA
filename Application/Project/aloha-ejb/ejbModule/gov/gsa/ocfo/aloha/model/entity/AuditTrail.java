package gov.gsa.ocfo.aloha.model.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Embeddable
public class AuditTrail implements Serializable{
	private static final long serialVersionUID = 2123312660024964150L;
	
	@Column(name="LOGIN_LAST_UPDATED")
	private String userLastUpdated;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="DATE_LAST_UPDATED")
	private Date dateLastUpdated;
	
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
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("AuditTrail [userLastUpdated=");
		builder.append(userLastUpdated);
		builder.append(", dateLastUpdated=");
		builder.append(dateLastUpdated);
		builder.append("]");
		return builder.toString();
	}
}
