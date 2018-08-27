package gov.gsa.ocfo.aloha.web.model.leave;

import gov.gsa.ocfo.aloha.model.entity.leave.LeaveType;
import gov.gsa.ocfo.aloha.util.StringUtil;
import gov.gsa.ocfo.aloha.web.util.DateUtil;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LeaveViewItem implements Serializable {
	private static final long serialVersionUID = -3910937739807007916L;
	public static final String UNSELECTED_START_TIME_KEY = "99";

	private long leaveViewItemId;
	private Date leaveDate;
	private String leaveDateKey;
	private LeaveType leaveType;
	private String leaveTypeKey;
	private Date leaveStartTime;
	private String leaveStartTimeKey; 
	private BigDecimal leaveNumberOfHours;
	private String leaveNumberOfHoursAsString; 

	private static Map<String, LeaveViewItem> consolidate(List<LeaveViewItem> originalList) {
		
//		System.out.println("");
//		System.out.println("LeaveViewItem.consolidate(): BEGIN");
//		System.out.println("originalList.size(): " + originalList.size());
		
		Map<String, LeaveViewItem> lvItemMap = new HashMap<String, LeaveViewItem>();
		
		for (LeaveViewItem lvItem : originalList) {
			
//			System.out.println("lvItem.getMapKey(): " + lvItem.getMapKey() + " / leaveHours: " + lvItem.getLeaveNumberOfHours());
//			System.out.println("lvItem: " + lvItem);
//			System.out.println("lvItem.getMapKey(): " + lvItem.getMapKey());
			
			LeaveViewItem lvItemInMap = lvItemMap.get(lvItem.getMapKey());
			if (lvItemInMap == null) {
				lvItemMap.put(lvItem.getMapKey(), lvItem);
			} else {
				lvItemInMap.setLeaveNumberOfHours(lvItemInMap.getLeaveNumberOfHours().add(lvItem.getLeaveNumberOfHours()));	
			}
		}
		
//		System.out.println("lvItemMap.size(): " + lvItemMap.size());
//		System.out.println("LeaveViewItem.consolidate(): END");
//		System.out.println("");
		
		return lvItemMap;
	}
	
	public static List<LeaveViewItem> consolidateAndSort(List<LeaveViewItem> originalList) {
		
//		System.out.println("");
//		System.out.println("LeaveViewItem.consolidateAndSort(): BEGIN");
//		System.out.println("originalList.size(): " + originalList.size());
		
		List<LeaveViewItem> consolidatedAndSortedList = new ArrayList<LeaveViewItem>();
		Map<String, LeaveViewItem> lvItemMap = LeaveViewItem.consolidate(originalList);
		
		if ( lvItemMap != null) {
			Collection<LeaveViewItem> lvItemCollection = lvItemMap.values();
			consolidatedAndSortedList = new ArrayList<LeaveViewItem>(lvItemCollection);
		}

		Collections.sort(consolidatedAndSortedList, new Comparator<LeaveViewItem>() {
			@Override
			public int compare(LeaveViewItem lvItemOne, LeaveViewItem lvItemTwo) {
				String mapKeyOne = lvItemOne.getMapKey();
				String mapKeyTwo = lvItemTwo.getMapKey();
				
				long mayKeyOneAsLong = Long.valueOf(0);
				long mayKeyTwoAsLong = Long.valueOf(0);
				
				if (StringUtil.isNullOrEmpty(mapKeyOne) == false) {
					mayKeyOneAsLong = Long.valueOf(mapKeyOne).longValue();
				}
				
				if (StringUtil.isNullOrEmpty(mapKeyTwo) == false) {
					mayKeyTwoAsLong = Long.valueOf(mapKeyTwo).longValue();
				}
				
				//System.out.println("mayKeyOneAsLong: " + mayKeyOneAsLong + " / mayKeyTwoAsLong: " + mayKeyTwoAsLong);

				if (mayKeyOneAsLong > mayKeyTwoAsLong) {
					return 1;
				} else if (mayKeyOneAsLong < mayKeyTwoAsLong) {
					return -1;
				} else {
					return 0;
				}
			}	
		});	
		
//		System.out.println("consolidatedAndSortedList.size(): " + consolidatedAndSortedList.size());
//		System.out.println("LeaveViewItem.consolidate(): END");
//		System.out.println("");
		
		return consolidatedAndSortedList;
	}
	
	public long getLeaveViewItemId() {
		return leaveViewItemId;
	}
	public void setLeaveViewItemId(long leaveViewItemId) {
		this.leaveViewItemId = leaveViewItemId;
	}
	public Date getLeaveDate() {
		return leaveDate;
	}
	public void setLeaveDate(Date leaveDate) {
		this.leaveDate = leaveDate;
		this.leaveDateKey = null; // value will be derived when its getter is called
	}
	public String getLeaveDateKey() {
		if ( StringUtil.isNullOrEmpty(this.leaveDateKey) == true) {
			if ( this.getLeaveDate() != null ) {
				this.leaveDateKey  = DateUtil.formatDate(this.getLeaveDate(), DateUtil.DateFormats.YYYYMMDD);
			}
		}
		return this.leaveDateKey;
	}
	public void setLeaveDateKey(String leaveDateKey) {
		this.leaveDateKey = leaveDateKey;
	}
	public LeaveType getLeaveType() {
		return leaveType;
	}
	public void setLeaveType(LeaveType leaveType) {
		this.leaveType = leaveType;
		this.leaveTypeKey = null; // value will be derived when its getter is called
	}
	public String getLeaveTypeKey() {
		if ( StringUtil.isNullOrEmpty(this.leaveTypeKey) == true) {
			if ( this.getLeaveType() != null ) {
				this.leaveTypeKey  = this.getLeaveType().getValue();
			}
		}
		return this.leaveTypeKey;		
	}
	public void setLeaveTypeKey(String leaveTypeKey) {
		this.leaveTypeKey = leaveTypeKey;
	}
	public Date getLeaveStartTime() {
		return leaveStartTime;
	}
	public void setLeaveStartTime(Date leaveStartTime) {
		this.leaveStartTime = leaveStartTime;
		this.leaveStartTimeKey = null; // value will be derived when its getter is called
	}
	public String getLeaveStartTimeKey() {
		if ( StringUtil.isNullOrEmpty(this.leaveStartTimeKey) == true) {
			if ( this.getLeaveStartTime() != null ) {
				GregorianCalendar gregCal = new GregorianCalendar();
				gregCal.setTime(this.getLeaveStartTime());
				int hour = gregCal.get(Calendar.HOUR_OF_DAY);
				int minute = gregCal.get(Calendar.MINUTE);
				StringBuilder sb = new StringBuilder();
				sb.append( (hour < 10) ? ("0" + String.valueOf(hour)) : (String.valueOf(hour)) );
				sb.append( (minute < 10) ? ("0" + String.valueOf(minute)) : (String.valueOf(minute)) );
				this.leaveStartTimeKey = sb.toString();
			} else {
				this.leaveStartTimeKey = "0001";
			}
		}
		return this.leaveStartTimeKey;
	}
	public void setLeaveStartTimeKey(String leaveStartTimeKey) {
		this.leaveStartTimeKey = leaveStartTimeKey;
	}
	public BigDecimal getLeaveNumberOfHours() {
		return leaveNumberOfHours;
	}
	public void setLeaveNumberOfHours(BigDecimal leaveNumberOfHours) {
		this.leaveNumberOfHours = leaveNumberOfHours;
		this.leaveNumberOfHoursAsString = null;
	}
	
	public String getLeaveNumberOfHoursAsString() {
		if ( StringUtil.isNullOrEmpty(this.leaveNumberOfHoursAsString)) {
			if ( this.getLeaveNumberOfHours() != null ) {
				this.leaveNumberOfHoursAsString = this.getLeaveNumberOfHours().toString();
			}
		}
		return this.leaveNumberOfHoursAsString;
	}

	public void setLeaveNumberOfHoursAsString(String leaveNumberOfHoursAsString) {
		this.leaveNumberOfHoursAsString = leaveNumberOfHoursAsString;
	}

    public String getMapKey() {
        StringBuilder mapKey = new StringBuilder();
        
        String leaveDateKey         = this.getLeaveDateKey();
        String leaveStartTimeKey    = this.getLeaveStartTimeKey();
        String leaveTypeKey         = this.getLeaveTypeKey();
        
        if ( StringUtil.isNullOrEmpty(leaveDateKey) == false 
                && StringUtil.isNullOrEmpty(leaveStartTimeKey) == false
                && StringUtil.isNullOrEmpty(leaveTypeKey) == false ) 
        {
        
            mapKey.append(this.getLeaveDateKey());
            mapKey.append(this.getLeaveStartTimeKey());
            mapKey.append(leaveTypeKey);
            if (leaveTypeKey.length() == 2 ) {
                mapKey.append("00");
            } 
        } 
        else if (StringUtil.isNullOrEmpty(leaveDateKey) == false
                && StringUtil.isNullOrEmpty(leaveStartTimeKey) == true
                && StringUtil.isNullOrEmpty(leaveTypeKey) == false )
        {
            mapKey.append(this.getLeaveDateKey());
            mapKey.append(leaveTypeKey);
            if (leaveTypeKey.length() == 2 ) {
                mapKey.append("00");
            }           
        }
        else
        {
            mapKey.append (new String());
        }
        return mapKey.toString();
    }	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int)leaveViewItemId;
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
		LeaveViewItem other = (LeaveViewItem) obj;
		if (leaveViewItemId != other.leaveViewItemId)
			return false;
		return true;
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("LeaveViewItem [leaveViewItemId=");
		builder.append(leaveViewItemId);
		builder.append(", leaveDate=");
		builder.append(leaveDate);
		builder.append(", leaveDateKey=");
		builder.append(leaveDateKey);
		builder.append(", leaveType=");
		builder.append(leaveType);
		builder.append(", leaveTypeKey=");
		builder.append(leaveTypeKey);
		builder.append(", leaveStartTime=");
		builder.append(leaveStartTime);
		builder.append(", leaveStartTimeKey=");
		builder.append(leaveStartTimeKey);
		builder.append(", leaveNumberOfHours=");
		builder.append(leaveNumberOfHours);
		builder.append("]");
		return builder.toString();
	}
	
	public boolean isDisabledVetLeaveViewItem() {
		
		if ( this.leaveType != null ) {
			return this.leaveType.getPrimaryCode().equals(LeaveType.HardCoding.DISABLED_VET_PRIMARY_CODE);
		}
		return false;
	}
}