package gov.gsa.ocfo.aloha.model.overtime.group;

import java.io.Serializable;
import java.math.BigDecimal;

public class OTRequestTypeGroup implements Serializable {
	private static final long serialVersionUID = -3450735165874460542L;
	private String name;
	private int count;
	private BigDecimal nbrOfHours;
	
	public OTRequestTypeGroup(String name, int count, BigDecimal hours) {
		this.name = name;
		this.count = count;
		this.nbrOfHours = hours;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getCount() {
		return count;
	}
	public BigDecimal getNbrOfHours() {
		return nbrOfHours;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		OTRequestTypeGroup other = (OTRequestTypeGroup) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("OTRequestTypeGroup [name=");
		builder.append(name);
		builder.append(", count=");
		builder.append(count);
		builder.append(", nbrOfHours=");
		builder.append(nbrOfHours);
		builder.append("]");
		return builder.toString();
	}
}
