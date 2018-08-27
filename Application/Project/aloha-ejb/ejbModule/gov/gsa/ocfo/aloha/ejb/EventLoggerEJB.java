package gov.gsa.ocfo.aloha.ejb;

import gov.gsa.ocfo.aloha.exception.AlohaServerException;
import gov.gsa.ocfo.aloha.model.entity.audit.EventLog;
import gov.gsa.ocfo.aloha.model.entity.audit.EventType;

import javax.ejb.Local;

@Local
public interface EventLoggerEJB {
	public void logEvent(EventLog eventLog);
	public EventType getEventType(String eventTypeCode) throws AlohaServerException;
}
