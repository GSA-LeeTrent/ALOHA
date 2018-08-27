package gov.gsa.ocfo.aloha.model.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@Entity
@Table(name = "USER_DEMOGRAPHICS", schema="USR_FEDDESK_REQUIRED")
@NamedQueries({
	@NamedQuery(name = "getUserDemographicsByUserId",
			query = "Select row from EtamsUser row where row.etamsUserId = :userId")
	})

public class EtamsUser {
	public interface QueryNames{
		public static final String GET_ETAMS_USER_BY_USERID = "getUserDemographicsByUserId";
	}

	public interface QueryParamNames {
		public static final String GET_ETAMS_USER_BY_USERID = "userId";
	}

	@Id
	@Column(name = "USER_ID")
	private Long etamsUserId;
	@Column(name = "EMAIL_ADDRESS")
	private String email;

	public Long getUserId() {
		return etamsUserId;
	}
	public void setUserId(Long etamsUserId) {
		this.etamsUserId = etamsUserId;
	}

	public String getEmailAddress() {
		return email;
	}
	public void setEmailAddress(String email) {
		this.email = email;
	}

}