package gov.gsa.ocfo.aloha.model.entity.overtime;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Version;

@Entity
@Table(name="OT_GROUP_HISTORY", schema="ALOHA")
public class OTGroupHistory implements Serializable {
	private static final long serialVersionUID = -534809596342415488L;

	@Id
	@SequenceGenerator(name="OT_GROUP_HISTORY_ID_GENERATOR", sequenceName="ALOHA.SEQ_OT_GROUP_HISTORY", allocationSize=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="OT_GROUP_HISTORY_ID_GENERATOR")
	@Column(name="GROUP_HISTORY_ID", unique=true, nullable=false, precision=10)
	private long id;
	
	@ManyToOne
	@JoinColumn(name="OT_GROUP_ID", referencedColumnName="OT_GROUP_ID", nullable=false)
	private OTGroup group;
	
    @ManyToOne
	@JoinColumn(name="STATUS_TRANSITION_ID", referencedColumnName="STATUS_TRANSITION_ID", nullable=false)
	private OTGroupStatusTrans statusTransition;
	
    @Column(name="HISTORY_SEQ", nullable=false, precision=5) 
    private int historySequence;

	@Version
	@Column(name="OPT_LOCK_NBR")
	private long version;

	@Embedded
	private OTCreatedBy createdBy = new OTCreatedBy();
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public OTGroup getGroup() {
		return group;
	}

	public void setGroup(OTGroup group) {
		this.group = group;
	}

	public OTGroupStatusTrans getStatusTransition() {
		return statusTransition;
	}

	public void setStatusTransition(OTGroupStatusTrans statusTransition) {
		this.statusTransition = statusTransition;
	}
	
	public int getHistorySequence() {
		return historySequence;
	}

	public void setHistorySequence(int historySequence) {
		this.historySequence = historySequence;
	}

	public OTCreatedBy getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(OTCreatedBy createdBy) {
		this.createdBy = createdBy;
	}

	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
	}

	public Date getDateCreated() {
		return this.createdBy.getDateCreated();
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((group == null) ? 0 : group.hashCode());
		result = prime * result + historySequence;
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime
				* result
				+ ((statusTransition == null) ? 0 : statusTransition.hashCode());
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
		OTGroupHistory other = (OTGroupHistory) obj;
		if (group == null) {
			if (other.group != null)
				return false;
		} else if (!group.equals(other.group))
			return false;
		if (historySequence != other.historySequence)
			return false;
		if (id != other.id)
			return false;
		if (statusTransition == null) {
			if (other.statusTransition != null)
				return false;
		} else if (!statusTransition.equals(other.statusTransition))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("OTGroupHistory [id=");
		builder.append(id);
		builder.append(", statusTransition=");
		builder.append(statusTransition);
		builder.append(", historySequence=");
		builder.append(historySequence);
		builder.append("]");
		return builder.toString();
	}
}