package gov.gsa.ocfo.aloha.model.leave;

import gov.gsa.ocfo.aloha.model.entity.AlohaUser;

import java.io.Serializable;
import java.util.Date;

public class LeaveRemark implements Serializable {
	private static final long serialVersionUID = 5611803271609312858L;

	private Date remarkDate;
	private AlohaUser actor;
	private String eventDescription;
	private String remark;
	
	public LeaveRemark(Date remarkDate, AlohaUser actor, String eventDesc, String remark) {
		this.remarkDate = remarkDate;
		this.actor = actor;
		this.eventDescription = eventDesc;
		this.remark = remark;
	}
	
	public LeaveRemark(Date remarkDate, AlohaUser actor, String remark ) {
		this(remarkDate, actor, null, remark);
	}

	public Date getRemarkDate() {
		return remarkDate;
	}

	public String getRemark() {
		return remark;
	}

	public AlohaUser getActor() {
		return actor;
	}

	public String getEventDescription() {
		return eventDescription;
	}
}
