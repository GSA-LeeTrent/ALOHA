package gov.gsa.ocfo.aloha.model.entity.overtime;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@Entity
@Table(name="OT_ENTITY_TYPE", schema="ALOHA")
@NamedQueries({
	@NamedQuery(name="retrieveAllOTEntityType", 
				query="SELECT row FROM OTEntityType row")	        	
})
public class OTEntityType implements Serializable {
	private static final long serialVersionUID = -2726598247930006096L;

	public interface QueryNames{
		public static final String RETRIEVE_ALL = "retrieveAllOTEntityType";
	}
	public interface CodeValues {
		public static final String INDIVIDUAL = "I";
		public static final String GROUP = "G";
	}
	
	@Id
	@Column(name="ENTITY_TYPE_CODE", unique=true, nullable=false)
	private String code;

	@Column(name="ENTITY_TYPE_NAME", unique=true, nullable=false)
	private String name;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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
		OTEntityType other = (OTEntityType) obj;
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
		builder.append("OTEntityType [code=");
		builder.append(code);
		builder.append(", name=");
		builder.append(name);
		builder.append("]");
		return builder.toString();
	}
}