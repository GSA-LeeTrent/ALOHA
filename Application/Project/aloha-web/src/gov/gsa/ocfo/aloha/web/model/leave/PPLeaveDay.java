package gov.gsa.ocfo.aloha.web.model.leave;

import gov.gsa.ocfo.aloha.model.entity.leave.LeaveType;
import gov.gsa.ocfo.aloha.web.util.DateUtil;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class PPLeaveDay implements Serializable {
	private static final long serialVersionUID = -5457487860883870051L;
	
	private String key;
	private Date day;
	private BigDecimal leaveHours = BigDecimal.ZERO;
	private Map<Long, LeaveType> leaveTypes = new HashMap<Long, LeaveType>();
	private Map<String, String> leaveTypeCodes = new HashMap<String, String>();
	
	public PPLeaveDay(Date d) {
		this.day = d;
		this.key = DateUtil.formatDate(this.day, DateUtil.DateFormats.YYYYMMDD);
	}
	
	public void addToLeaveHours(BigDecimal augend) {
		this.leaveHours = this.leaveHours.add(augend);
	}
	
	public void addLeaveType(LeaveType value) {
		Long key = Long.valueOf(value.getId());
		if ( ! this.leaveTypes.containsKey(key) ) {
			this.leaveTypes.put(key, value);
		}
	}

	public void addLeaveTypeCode(String keyAndValue) {
		if ( ! this.leaveTypeCodes.containsKey(keyAndValue) ) {
			this.leaveTypeCodes.put(keyAndValue, keyAndValue);
		}
	}

	public String getKey() {
		return key;
	}

	public Date getDay() {
		return day;
	}
	public BigDecimal getLeaveHours() {
		return leaveHours;
	}
	
	public Map<Long, LeaveType> getLeaveTypes() {
		return leaveTypes;
	}

	public Map<String, String> getLeaveTypeCodes() {
		return leaveTypeCodes;
	}

	public int getNumberOfLeaveTypes() {
		return this.leaveTypes.size();
	}
	public int getNumberOfLeaveTypeCodes() {
		return this.leaveTypeCodes.size();
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
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
		PPLeaveDay other = (PPLeaveDay) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("PPLeaveDay [key=");
		builder.append(key);
		builder.append(", day=");
		builder.append(day);
		builder.append(", leaveHours=");
		builder.append(leaveHours);
		builder.append(", leaveTypes=");
		builder.append(leaveTypes);
		builder.append("]");
		return builder.toString();
	}
}