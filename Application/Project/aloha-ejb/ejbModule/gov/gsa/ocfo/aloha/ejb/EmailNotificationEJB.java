package gov.gsa.ocfo.aloha.ejb;

import gov.gsa.ocfo.aloha.model.entity.leave.LeaveDetail;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveHeader;

import javax.ejb.Local;

@Local
public interface EmailNotificationEJB {
	public void sendEmailNotifications(LeaveHeader leaveHeader);
	public void sendEmailNotifications(LeaveDetail leaveDetail);
}
