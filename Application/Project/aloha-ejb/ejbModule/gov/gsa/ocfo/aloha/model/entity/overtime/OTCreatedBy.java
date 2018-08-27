package gov.gsa.ocfo.aloha.model.entity.overtime;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Embeddable
public class OTCreatedBy implements Serializable {
	private static final long serialVersionUID = 5551782874551264984L;

	@Column(name="USER_CREATED", nullable=false)
	private long userCreated;

	@Temporal(TemporalType.DATE)
	@Column(name="DATE_CREATED", nullable=false)
	private Date dateCreated = new Date();

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}
	public Date getDateCreated() {
		return dateCreated;
	}
	public long getUserCreated() {
		return userCreated;
	}
	public void setUserCreated(long userCreated) {
		this.userCreated = userCreated;
	}
}