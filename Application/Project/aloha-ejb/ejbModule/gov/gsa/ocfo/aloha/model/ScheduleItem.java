package gov.gsa.ocfo.aloha.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

public class ScheduleItem implements Serializable {
	private static final long serialVersionUID = 1110338605989212141L;
	
	public static final String AM = "AM";
	public static final String PM = "PM";
	
	public static final String UNSELECTED_START_TIME = "99";
	public static final String UNSELECTED_LEAVE_TYPE_CODE = "0";	
	public static final BigDecimal ZERO_LEAVE_HOURS = BigDecimal.ZERO;
	
	private int sequence;
	private Date calendarDate;
	private String dayOfWeek;
	private String dayOfWeekAbbrv;
	private int hoursScheduled;
	private String selectedLeaveTypeCode;
	private BigDecimal numberOfLeaveHours;
	private Boolean fullDayOff;
	private String startTime;	
	
	private boolean holiday = false; // default
	private String holidayDesc;
	private boolean duplicate;
	private boolean duplicatePopulated;
	
	public ScheduleItem() {
		this.selectedLeaveTypeCode = ScheduleItem.UNSELECTED_LEAVE_TYPE_CODE;
		this.startTime = ScheduleItem.UNSELECTED_START_TIME;
	}
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
	public BigDecimal getNumberOfLeaveHours() {
		return numberOfLeaveHours;
	}
	public void setNumberOfLeaveHours(BigDecimal numberOfLeaveHours) {
		this.numberOfLeaveHours = numberOfLeaveHours;
	}
	public boolean isWorkDay() {
		return (this.hoursScheduled > 0);
	}
	public Boolean getFullDayOff() {
		return fullDayOff;
	}
	public void setFullDayOff(Boolean fullDayOff) {
		this.fullDayOff = fullDayOff;
	}
	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}
	public String getStartTime() {
		return startTime;
	}
	public String getSelectedLeaveTypeCode() {
		return selectedLeaveTypeCode;
	}
	public void setSelectedLeaveTypeCode(String selectedLeaveTypeCode) {
		this.selectedLeaveTypeCode = selectedLeaveTypeCode;
	}
	public String getDayOfWeekAbbrv() {
		return dayOfWeekAbbrv;
	}
	public void setDayOfWeekAbbrv(String dayOfWeekAbbrv) {
		this.dayOfWeekAbbrv = dayOfWeekAbbrv;
	}
	public boolean isFirstRow() {
		return (this.sequence == 1);
	}
	public int getSequence() {
		return sequence;
	}
	public void setSequence(int sequence) {
		this.sequence = sequence;
	}
	public String getScheduleItemId () {
		return ("scheduleItem_" + sequence);
	}
	public String getOutputLinkId() {
		return ("outputLink_" + sequence);
	}
	public String getHoursScheduledId() {
		return ("hoursScheduled_" + sequence);
	}
	public String getSelectedLeaveTypeCodeId() {
		return ("selectedLeaveTypeCode_" + sequence);
	}
	public String getNumberOfLeaveHoursId() {
		return ("numberOfLeaveHours_" + sequence);
	}	
	public String getCopyIconId() {
		return ("copyIcon_" + sequence);
	}	
	public String getEraserIconId() {
		return ("eraserIcon_" + sequence);
	}	
	public String getFullDayOffId() {
		return ("fullDayOff_" + sequence);
	}	
	public String getStartHourId() {
		return ("startHour_" + sequence);
	}
	public String getStartTimeId() {
		return ("startTime_" + sequence);
	}
	public String getStartMinuteId() {
		return ("startMinute_" + sequence);
	}	
	public String getSelectedMeridiemId() {
		return ("selectedMeridiem_" + sequence);
	}
	public String getCalendarDateId() {
		return ("calendarDate_" + sequence);
	}
	public boolean isHoliday() {
		return holiday;
	}
	public void setHoliday(boolean holiday) {
		this.holiday = holiday;
	}
	public String getHolidayDesc() {
		return holidayDesc;
	}
	public void setHolidayDesc(String holidayDesc) {
		this.holidayDesc = holidayDesc;
	}
	
	public boolean isDuplicate() {
		return duplicate;
	}
	public void setDuplicate(boolean duplicate) {
		this.duplicate = duplicate;
	}

	public boolean isDuplicatePopulated() {
		return duplicatePopulated;
	}
	public void setDuplicatePopulated(boolean duplicatePopulated) {
		this.duplicatePopulated = duplicatePopulated;
	}
	public boolean isPopulated() {
		return	(  ( (this.numberOfLeaveHours != null) && (this.numberOfLeaveHours.compareTo(BigDecimal.ZERO) > 0) ) 
					|| ( (this.selectedLeaveTypeCode != null) && ( ! this.selectedLeaveTypeCode.equals(UNSELECTED_LEAVE_TYPE_CODE)) )
					|| ( (this.startTime != null) && ( ! this.startTime.equals(UNSELECTED_START_TIME)) )					
				);
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
		ScheduleItem other = (ScheduleItem) obj;
		if (calendarDate == null) {
			if (other.calendarDate != null)
				return false;
		} else if (!calendarDate.equals(other.calendarDate))
			return false;
		return true;
	}
}