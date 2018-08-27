package gov.gsa.ocfo.aloha.model.entity.overtime;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Embeddable
public class OTAuditTrail implements Serializable {
	private static final long serialVersionUID = -5537293174620936367L;

	@Column(name="USER_CREATED")
	private long userCreated;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="DATE_CREATED")
	private Date dateCreated;

	public long getUserCreated() {
		return userCreated;
	}
	public void setUserCreated(long userCreated) {
		this.userCreated = userCreated;
	}
	public Date getDateCreated() {
		return dateCreated;
	}
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}
}