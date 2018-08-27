package gov.gsa.ocfo.aloha.model;

import java.io.Serializable;

public class Facility implements Serializable {
	private static final long serialVersionUID = -1677472082168952475L;

	private String id;
	private String code;
	private String description;
	private String label;
		
	public Facility(String id, String code, String description) {
		super();
		this.id = id;
		this.code = code;
		this.description = description;
		
		StringBuilder sb = new StringBuilder();
		if ( this.code != null) {
			sb.append(this.code);
		}
		if ( this.description != null) {
			if ( this.code != null) {
				sb.append(" - ");
			}
			sb.append(this.description);
		}
		this.label = sb.toString();
	}

	public String getId() {
		return id;
	}

	public String getCode() {
		return code;
	}

	public String getDescription() {
		return description;
	}
	
	public String getLabel() {
		return label;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		Facility other = (Facility) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Facility [id=");
		builder.append(id);
		builder.append(", code=");
		builder.append(code);
		builder.append(", description=");
		builder.append(description);
		builder.append("]");
		return builder.toString();
	}
}