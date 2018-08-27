package gov.gsa.ocfo.aloha.model.leave;

import gov.gsa.ocfo.aloha.model.PayPeriod;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LRVariancePayPeriod implements Serializable {

	private static final long serialVersionUID = -5212726792101629757L;

	private int year;
	private int payPeriodNumber;
	private Date startDate;
	private Date endDate;
	
	public LRVariancePayPeriod(int year, int payPeriodNumber, Date startDate,
			Date endDate) {
		super();
		this.year = year;
		this.payPeriodNumber = payPeriodNumber;
		this.startDate = startDate;
		this.endDate = endDate;
	}

	public int getYear() {
		return year;
	}

	public int getPayPeriodNumber() {
		return payPeriodNumber;
	}

	public Date getStartDate() {
		return startDate;
	}

	public Date getEndDate() {
		return endDate;
	}
	
	public String getDateRange() {
		
		String dateRange = null;
		
		if ( this.startDate != null && this.endDate != null) {
			SimpleDateFormat sdf = new SimpleDateFormat(PayPeriod.LABEL_FORMAT);
			StringBuilder sb = new StringBuilder();
			sb.append(sdf.format(this.startDate));
			sb.append(" - ");
			sb.append(sdf.format(this.endDate));
			dateRange = sb.toString();
		}
		return dateRange;
	}	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((endDate == null) ? 0 : endDate.hashCode());
		result = prime * result + payPeriodNumber;
		result = prime * result
				+ ((startDate == null) ? 0 : startDate.hashCode());
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
		LRVariancePayPeriod other = (LRVariancePayPeriod) obj;
		if (endDate == null) {
			if (other.endDate != null)
				return false;
		} else if (!endDate.equals(other.endDate))
			return false;
		if (payPeriodNumber != other.payPeriodNumber)
			return false;
		if (startDate == null) {
			if (other.startDate != null)
				return false;
		} else if (!startDate.equals(other.startDate))
			return false;
		if (year != other.year)
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("LRVariancePayPeriod [year=");
		builder.append(year);
		builder.append(", payPeriodNumber=");
		builder.append(payPeriodNumber);
		builder.append(", startDate=");
		builder.append(startDate);
		builder.append(", endDate=");
		builder.append(endDate);
		builder.append("]");
		return builder.toString();
	}
}
