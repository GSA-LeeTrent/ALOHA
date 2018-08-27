package gov.gsa.ocfo.aloha.ejb.impl;

import gov.gsa.ocfo.aloha.ejb.EventLoggerEJB;
import gov.gsa.ocfo.aloha.exception.AlohaServerException;
import gov.gsa.ocfo.aloha.model.entity.audit.EventLog;
import gov.gsa.ocfo.aloha.model.entity.audit.EventType;

import java.util.Date;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

@Stateless
public class EventLoggerEJBImpl implements EventLoggerEJB {
	
	@PersistenceContext (unitName="aloha-pu")
	private EntityManager entityManager;

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void logEvent(EventLog eventLog) {
		try {
			//System.out.println("EventLoggerEJBImpl.logEvent() : BEGIN");

			eventLog.setDateCreated(new Date());
			
			System.out.println(eventLog);
			
			// PERSIST
			//StopWatch stopWatch = new StopWatch();
			//stopWatch.start();
			this.entityManager.persist(eventLog);
			//stopWatch.stop();
			//System.out.println("ELAPSED TIME (persist EventLog): " + stopWatch.getElapsedTime() + " ms");
			//stopWatch = null;
			
			// FLUSH
			//stopWatch = new StopWatch();
			//stopWatch.start();
			this.entityManager.flush();
			//stopWatch.stop();
			//System.out.println("ELAPSED TIME (flush EventLog): " + stopWatch.getElapsedTime() + " ms");
			//stopWatch = null;
			//System.out.println("EventLoggerEJBImpl.logEvent() : END");
			
			//System.out.println(eventLog);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public EventType getEventType(String eventTypeCode) throws AlohaServerException {
		Query query = this.entityManager.createNamedQuery(EventType.EVENT_TYPE_CODE_QUERY);
		query.setParameter(EventType.EVENT_TYPE_CODE_QUERY_PARAM, eventTypeCode);
		return (EventType)query.getSingleResult();
	}
}