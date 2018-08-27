package gov.gsa.ocfo.aloha.model;

import java.io.Serializable;
import java.util.Date;

public class PayPeriod implements Serializable  {
	public static final String VALUE_FORMAT = "yyyyMMdd";
	public static final String LABEL_FORMAT = "MM/dd/yyyy";
	
	private static final long serialVersionUID = 6701724733920430135L;
	
	private int year;
	private int payPeriod;
	private Date fromDate;
	private Date toDate;
	private String label;
	private String value;
	
	public int getYear() {
		return year;
	}
	public void setYear(int year) {
		this.year = year;
	}
	public int getPayPeriod() {
		return payPeriod;
	}
	public void setPayPeriod(int payPeriod) {
		this.payPeriod = payPeriod;
	}
	public Date getFromDate() {
		return fromDate;
	}
	public void setFromDate(Date fromDate) {
		this.fromDate = fromDate;
	}
	public Date getToDate() {
		return toDate;
	}
	public void setToDate(Date toDate) {
		this.toDate = toDate;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + payPeriod;
		result = prime * result + year;
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
		PayPeriod other = (PayPeriod) obj;
		if (payPeriod != other.payPeriod)
			return false;
		if (year != other.year)
			return false;
		return true;
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("PayPeriod [year=");
		builder.append(year);
		builder.append(", payPeriod=");
		builder.append(payPeriod);
		builder.append(", fromDate=");
		builder.append(fromDate);
		builder.append(", toDate=");
		builder.append(toDate);
		builder.append(", label=");
		builder.append(label);
		builder.append(", value=");
		builder.append(value);
		builder.append("]");
		return builder.toString();
	}
}
