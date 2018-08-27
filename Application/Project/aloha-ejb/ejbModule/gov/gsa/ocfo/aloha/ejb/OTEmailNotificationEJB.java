package gov.gsa.ocfo.aloha.ejb;

import gov.gsa.ocfo.aloha.model.entity.overtime.OTDetail;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTGroup;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTHeader;

import javax.ejb.Local;

@Local
public interface OTEmailNotificationEJB {
	public void sendEmailNotifications(OTHeader otHeader);
	public void sendEmailNotifications(OTDetail otDetail);
	public void sendEmailNotifications(OTGroup otGroup);
}
