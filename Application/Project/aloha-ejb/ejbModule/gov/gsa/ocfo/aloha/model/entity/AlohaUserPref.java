package gov.gsa.ocfo.aloha.model.entity;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the AL_USER_PREF database table.
 * 
 */
@Entity
@Table(name="AL_USER_PREF", schema="ALOHA")
@NamedQueries({
	@NamedQuery(name="findUserPrefByUserId",
	        	query="SELECT row FROM AlohaUserPref row WHERE row.userId = :userId")
})
public class AlohaUserPref implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public interface QueryNames{
		public static final String FIND_BY_USERID = "findUserPrefByUserId";
	}
	public interface QueryParamNames {
		public static final String FIND_BY_USERID = "userId";	
	}
	
	@Id
	@SequenceGenerator(name="AL_USER_PREF_ID_GENERATOR", sequenceName="ALOHA.SEQ_AL_USER_PREF", allocationSize=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="AL_USER_PREF_ID_GENERATOR")
	//@Column(unique=true, nullable=false, precision=22)
	@Column(name="AL_USER_PREF_ID", unique=true)
	private long id;

	@Column(name="DEFAULT_APPROVER_USER_ID")
	private Long defaultApproverUserId;
	
    @Column(name="PRIM_TK_USER_ID")
	private Long defaultTimekeeperUserId;

	@Column(name="USER_ID", unique=true, nullable=false)
	private long userId;

	@Version
	@Column(name="OPT_LOCK_NBR")
	private long version;
	
    public AlohaUserPref() {
    }
    
    
	public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Long getDefaultApproverUserId() {
		return this.defaultApproverUserId;
	}

	public void setDefaultApproverUserId(Long defaultApproverUserId) {
		this.defaultApproverUserId = defaultApproverUserId;
		
	}	
		
    public Long getDefaultTimekeeperUserId() {
		return this.defaultTimekeeperUserId;
	}

	public void setDefaultTimekeeperUserId(Long defaultTimekeeperUserId) {
		this.defaultTimekeeperUserId = defaultTimekeeperUserId;
	}

	public long getUserId() {
		return this.userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (int) (prime * result
				+  userId);
		result = prime * result + (int) (id ^ (id >>> 32));
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
		AlohaUserPref other = (AlohaUserPref) obj;
		if (userId != other.userId)
			return false;
		if (id != other.id)
			return false;
		if (id == other.id && defaultApproverUserId == other.defaultApproverUserId && userId == other.userId && defaultTimekeeperUserId == other.defaultTimekeeperUserId)
		return true;
		return false;
	}


	public void setVersion(long version) {
		this.version = version;
	}


	public long getVersion() {
		return version;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("AlohaUserPref [id=");
		builder.append(id);
		builder.append(", defaultApproverUserId=");
		builder.append(defaultApproverUserId);
		builder.append(", defaultTimekeeperUserId=");
		builder.append(defaultTimekeeperUserId);
		builder.append(", userId=");
		builder.append(userId);
		builder.append(", version=");
		builder.append(version);
		builder.append("]");
		return builder.toString();
	}
}