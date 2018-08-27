package gov.gsa.ocfo.aloha.ejb.leave;

import gov.gsa.ocfo.aloha.model.entity.AlohaUser;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveDetail;

import javax.ejb.Local;

@Local
public interface LRChangeSupvNotifyEJB {
	public static final String LINK_TO_ALOHA = "https://apps.ocfo.gsa.gov/aloha/";
	public static final String FROM_VALUE = "aloha-do-not-reply@gsa.gov";
	
	public void sendEmailNotifications(LeaveDetail leaveDetail, AlohaUser oldSupvervisor);
}
