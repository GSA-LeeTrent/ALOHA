package gov.gsa.ocfo.aloha.model.entity;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Column;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.NamedQuery;

@Entity
@Table(name = "ETAMS_AD_XREF", schema="ALOHA")
@NamedQueries({
@NamedQuery(name = "findEtamsAdXrefByLoginName", 
		query = "Select row from EtamsAdXref row where row.loginName like :loginName order by row.loginName"),
@NamedQuery(name = "getEtamsAdXrefByUserId", 
		query = "Select row from EtamsAdXref row where row.userId = :userId")
})		
public class EtamsAdXref {
	public interface QueryNames{
		public static final String FIND_BY_LOGINNAME = "findEtamsAdXrefByLoginName";
		public static final String GET_BY_USERID = "getEtamsAdXrefByUserId";
	}
	public interface QueryParamNames {
		public static final String FIND_BY_LOGINNAME = "loginName";
		public static final String GET_BY_USERID = "userId";	
	}
	
	@Id
	@Column(name = "LOGIN_NAME")
	private String loginName;
	@Column(name = "USER_ID")
	private long userId;
	@Temporal(TemporalType.TIMESTAMP)
	private Date timestamp;
	
	@OneToOne
	@JoinColumn(name="USER_ID", insertable=false, updatable=false)
	private AlohaUser alohaUser;
	
	//---------------------------------------------------------
	//Getters and Setters
	//---------------------------------------------------------
	public String getLoginName() {
		return loginName;
	}
	public void setLoginName(String loginName) {
		this.loginName = loginName;
	}
	public long getUserId() {
		return userId;
	}
	public void setUserId(long userId) {
		this.userId = userId;
	}
	public Date getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	public void setAlohaUser(AlohaUser alohaUser) {
		this.alohaUser = alohaUser;
	}
	public AlohaUser getAlohaUser() {
		return alohaUser;
	}
	

}
