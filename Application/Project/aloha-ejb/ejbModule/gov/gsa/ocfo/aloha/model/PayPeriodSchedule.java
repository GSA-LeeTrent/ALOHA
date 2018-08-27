package gov.gsa.ocfo.aloha.model;

import java.io.Serializable;
import java.util.Date;

public class PayPeriodSchedule implements Serializable {
	private static final long serialVersionUID = 57234227669473788L;

	private Date calendarDate;
	private String dayOfWeek;
	private int hoursScheduled;
	private int holidayIndicator;
	private String holidayDescription;

	public Date getCalendarDate() {
		return calendarDate;
	}
	public void setCalendarDate(Date calendarDate) {
		this.calendarDate = calendarDate;
	}
	public String getDayOfWeek() {
		return dayOfWeek;
	}
	public void setDayOfWeek(String dayOfWeek) {
		this.dayOfWeek = dayOfWeek;
	}
	public int getHoursScheduled() {
		return hoursScheduled;
	}
	public void setHoursScheduled(int hoursScheduled) {
		this.hoursScheduled = hoursScheduled;
	}
	public int getHolidayIndicator() {
		return holidayIndicator;
	}
	public void setHolidayIndicator(int holidayIndicator) {
		this.holidayIndicator = holidayIndicator;
	}
	public String getHolidayDescription() {
		return holidayDescription;
	}
	public void setHolidayDescription(String holidayDescription) {
		this.holidayDescription = holidayDescription;
	}
	
	public boolean isHoliday() {
		return (this.holidayIndicator == 1);
	}
	
	public boolean isWorkDay() {
		return (this.hoursScheduled > 0 );
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((calendarDate == null) ? 0 : calendarDate.hashCode());
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
		PayPeriodSchedule other = (PayPeriodSchedule) obj;
		if (calendarDate == null) {
			if (other.calendarDate != null)
				return false;
		} else if (!calendarDate.equals(other.calendarDate))
			return false;
		return true;
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("PayPeriodSchedule [calendarDate=");
		builder.append(calendarDate);
		builder.append(", dayOfWeek=");
		builder.append(dayOfWeek);
		builder.append(", hoursScheduled=");
		builder.append(hoursScheduled);
		builder.append(", holidayIndicator=");
		builder.append(holidayIndicator);
		builder.append(", holidayDescription=");
		builder.append(holidayDescription);
		builder.append("]");
		return builder.toString();
	}
}