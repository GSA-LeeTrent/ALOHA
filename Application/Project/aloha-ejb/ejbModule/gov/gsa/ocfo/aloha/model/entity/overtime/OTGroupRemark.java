package gov.gsa.ocfo.aloha.model.entity.overtime;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Version;

@Entity
@Table(name="OT_GROUP_REMARK", schema="ALOHA")
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="ACTOR_TYPE_CODE", discriminatorType=DiscriminatorType.STRING, length=1)
public abstract class OTGroupRemark implements Serializable {
	private static final long serialVersionUID = -2602121697957428106L;
	
	@Id
	@SequenceGenerator(name="OT_GROUP_REMARK_ID_GENERATOR", sequenceName="ALOHA.SEQ_OT_GROUP_REMARK", allocationSize=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="OT_GROUP_REMARK_ID_GENERATOR")
	@Column(name="GROUP_REMARK_ID", unique=true, nullable=false, precision=10)
	private long id;
	
	@ManyToOne
	@JoinColumn(name="OT_GROUP_ID", referencedColumnName="OT_GROUP_ID", nullable=false)
	private OTGroup group;
	
    @ManyToOne
	@JoinColumn(name="STATUS_TRANSITION_ID", referencedColumnName="STATUS_TRANSITION_ID", nullable=false)
	private OTGroupStatusTrans statusTransition;
	
    @Column(name="REMARK_TEXT", nullable=false, length=4000)
	private String text;

    @Column(name="REMARK_SEQ", nullable=false, precision=5) 
    private int remarkSequence;

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

	public int getRemarkSequence() {
		return remarkSequence;
	}

	public void setRemarkSequence(int remarkSequence) {
		this.remarkSequence = remarkSequence;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
	}

	public OTCreatedBy getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(OTCreatedBy createdBy) {
		this.createdBy = createdBy;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((group == null) ? 0 : group.hashCode());
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result + remarkSequence;
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
		OTGroupRemark other = (OTGroupRemark) obj;
		if (group == null) {
			if (other.group != null)
				return false;
		} else if (!group.equals(other.group))
			return false;
		if (id != other.id)
			return false;
		if (remarkSequence != other.remarkSequence)
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
		builder.append("OTGroupRemark [id=");
		builder.append(id);
		builder.append(", group=");
		builder.append(group);
		builder.append(", statusTransition=");
		builder.append(statusTransition);
		builder.append(", remarkSequence=");
		builder.append(remarkSequence);
		builder.append("]");
		return builder.toString();
	}
}