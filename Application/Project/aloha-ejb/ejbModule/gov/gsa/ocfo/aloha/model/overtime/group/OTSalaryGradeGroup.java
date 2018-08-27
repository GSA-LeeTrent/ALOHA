package gov.gsa.ocfo.aloha.model.overtime.group;

import java.io.Serializable;
import java.math.BigDecimal;

public class OTSalaryGradeGroup implements Serializable {
	private static final long serialVersionUID = -7641797745340214860L;
	private String salaryGradeKey;
	private int requestCount;
	private BigDecimal estNbrOfHrs = BigDecimal.ZERO;
	
	public OTSalaryGradeGroup() {}
	
	public OTSalaryGradeGroup(String key) {
		this.salaryGradeKey = key;
	}

	public String getSalaryGradeKey() {
		return salaryGradeKey;
	}
	public int getRequestCount() {
		return requestCount;
	}
	public BigDecimal getEstNbrOfHrs() {
		return estNbrOfHrs;
	}

	public void incrementRequestCount() {
		this.requestCount++;
	}
	
	public void addToEstNbrOfHours(BigDecimal hours) {
		this.estNbrOfHrs = this.estNbrOfHrs.add(hours);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((salaryGradeKey == null) ? 0 : salaryGradeKey.hashCode());
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
		OTSalaryGradeGroup other = (OTSalaryGradeGroup) obj;
		if (salaryGradeKey == null) {
			if (other.salaryGradeKey != null)
				return false;
		} else if (!salaryGradeKey.equals(other.salaryGradeKey))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("OTSalaryGradeGroup [salaryGradeKey=");
		builder.append(salaryGradeKey);
		builder.append(", requestCount=");
		builder.append(requestCount);
		builder.append(", estNbrOfHrs=");
		builder.append(estNbrOfHrs);
		builder.append("]");
		return builder.toString();
	}
}
