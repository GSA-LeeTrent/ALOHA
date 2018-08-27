package gov.gsa.ocfo.aloha.model.entity.overtime;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

@Entity
@Table(name="OT_STATUS_TRANSITION", schema="ALOHA")
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="ENTITY_TYPE_CODE", discriminatorType=DiscriminatorType.STRING, length=1)
public abstract class OTStatusTrans implements Serializable {
	private static final long serialVersionUID = -2896713723060188899L;

	@Id
	@Column(name="STATUS_TRANSITION_ID", unique=true, nullable=false, precision=10)
	private long id;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	@Column(name="ACTION_CODE", nullable=false, length=25)
	private String actionCode;

	@Column(name="ACTION_LABEL", nullable=false, length=25)
	private String actionLabel;

	@Column(name="ACTION_DESC", nullable=false, length=100)
	private String actionDescription;

	public String getActionCode() {
		return actionCode;
	}

	public void setActionCode(String actionCode) {
		this.actionCode = actionCode;
	}

	public String getActionLabel() {
		return actionLabel;
	}

	public void setActionLabel(String actionLabel) {
		this.actionLabel = actionLabel;
	}

	public String getActionDescription() {
		return actionDescription;
	}

	public void setActionDescription(String actionDescription) {
		this.actionDescription = actionDescription;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((actionCode == null) ? 0 : actionCode.hashCode());
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
		OTStatusTrans other = (OTStatusTrans) obj;
		if (actionCode == null) {
			if (other.actionCode != null)
				return false;
		} else if (!actionCode.equals(other.actionCode))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("OTStatusTrans [id=");
		builder.append(id);
		builder.append(", actionCode=");
		builder.append(actionCode);
		builder.append(", actionLabel=");
		builder.append(actionLabel);
		builder.append("]");
		return builder.toString();
	}
}
