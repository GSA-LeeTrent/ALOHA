package gov.gsa.ocfo.aloha.model.feddesk;

import java.io.Serializable;
import java.util.Date;

public class DateTable implements Serializable {

	private static final long serialVersionUID = -5328863908889037977L;
	
	private int year;
	private int payPeriod;
	private int day;
	private Date calendarDate;
	
	public DateTable(int year, int payPeriod, int day, Date calendarDate) {
		super();
		this.year = year;
		this.payPeriod = payPeriod;
		this.day = day;
		this.calendarDate = calendarDate;
	}

	public int getYear() {
		return year;
	}

	public int getPayPeriod() {
		return payPeriod;
	}

	public int getDay() {
		return day;
	}

	public Date getCalendarDate() {
		return calendarDate;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + day;
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
		DateTable other = (DateTable) obj;
		if (day != other.day)
			return false;
		if (payPeriod != other.payPeriod)
			return false;
		if (year != other.year)
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DateTable [year=");
		builder.append(year);
		builder.append(", payPeriod=");
		builder.append(payPeriod);
		builder.append(", day=");
		builder.append(day);
		builder.append(", calendarDate=");
		builder.append(calendarDate);
		builder.append("]");
		return builder.toString();
	}
}
