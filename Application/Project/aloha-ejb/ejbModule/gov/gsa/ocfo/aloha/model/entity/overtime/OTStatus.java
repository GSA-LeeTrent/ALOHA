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
import javax.persistence.Transient;

@Entity
@Table(name="OT_STATUS", schema="ALOHA")
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="ENTITY_TYPE_CODE", discriminatorType=DiscriminatorType.STRING, length=1)
public abstract class OTStatus implements Serializable {
	private static final long serialVersionUID = 7011834692370610414L;

	@Id
	@Column(name="STATUS_ID", unique=true, nullable=false, precision=10)
	private long id;

	@Column(name="STATUS_CODE", unique=true, nullable=false, length=50)
	private String code;
	
	@Column(name="STATUS_NAME", unique=true, nullable=false, length=100)
	private String name;
	
	@Column(name="DISPLAY_ORDER")
	private int displayOrder;
	
	@Transient
	private String label;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
	
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}			

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getDisplayOrder() {
		return displayOrder;
	}

	public void setDisplayOrder(int displayOrder) {
		this.displayOrder = displayOrder;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((code == null) ? 0 : code.hashCode());
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
		OTStatus other = (OTStatus) obj;
		if (code == null) {
			if (other.code != null)
				return false;
		} else if (!code.equals(other.code))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("OTStatus [id=");
		builder.append(id);
		builder.append(", code=");
		builder.append(code);
		builder.append(", name=");
		builder.append(name);
		builder.append(", displayOrder=");
		builder.append(displayOrder);
		builder.append("]");
		return builder.toString();
	}
}