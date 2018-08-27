package gov.gsa.ocfo.aloha.model.entity.overtime;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Embeddable
public class OTUpdatedBy implements Serializable {
	private static final long serialVersionUID = -789714230697386936L;

	@Column(name="USER_LAST_UPDATED", nullable=true)
	private Long userLastUpdated;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="DATE_LAST_UPDATED", nullable=true)
	private Date dateLastUpdated;

	public Long getUserLastUpdated() {
		return userLastUpdated;
	}

	public void setUserLastUpdated(Long userLastUpdated) {
		this.userLastUpdated = userLastUpdated;
	}

	public Date getDateLastUpdated() {
		return dateLastUpdated;
	}

	public void setDateLastUpdated(Date dateLastUpdated) {
		this.dateLastUpdated = dateLastUpdated;
	}
}