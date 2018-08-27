package gov.gsa.ocfo.aloha.ejb.overtime.impl;

import gov.gsa.ocfo.aloha.ejb.EventLoggerEJB;
import gov.gsa.ocfo.aloha.ejb.OTEmailNotificationEJB;
import gov.gsa.ocfo.aloha.ejb.overtime.OvertimeEJB;
import gov.gsa.ocfo.aloha.exception.AlohaServerException;
import gov.gsa.ocfo.aloha.model.KeyValuePair;
import gov.gsa.ocfo.aloha.model.entity.audit.EventLog;
import gov.gsa.ocfo.aloha.model.entity.audit.EventType;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTActorType;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTDetail;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTEntityType;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTHeader;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTIndivStatus;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTIndivStatusTrans;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTPayPeriod;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTSalaryGrade;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTType;
import gov.gsa.ocfo.aloha.util.SqlUtil;
import gov.gsa.ocfo.aloha.util.StringUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TransactionRequiredException;
import javax.sql.DataSource;

@Stateless
public class OvertimeEJBImpl implements OvertimeEJB {
	@PersistenceContext (unitName="aloha-pu")
	private EntityManager entityManager;
	
	@EJB
	OTEmailNotificationEJB otEmailNotificationEJB;
	
	@EJB
	EventLoggerEJB eventLoggerEJB;
	
	@Resource(name = "aloha-ds")
	private DataSource dataSource;
	
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void saveOvertimeRequest(OTHeader otHeader) throws AlohaServerException {
		try {
			//System.out.println("OvertimeEJBImpl.saveOvertimeRequest() : BEGIN");
			
			// STOP WATCHES
			//StopWatch outerStopWatch = new StopWatch();
			//StopWatch innerStopWatch = new StopWatch();
			
			//outerStopWatch.start();
			
			// PERSIST
			//innerStopWatch.start();
			this.entityManager.persist(otHeader);
			//innerStopWatch.stop();
			//System.out.println("ELAPSED TIME (persist OTHeader): " + innerStopWatch.getElapsedTime() + " ms");
			//innerStopWatch = null;
			
			// FLUSH
			//innerStopWatch = new StopWatch();
			//innerStopWatch.start();
			this.entityManager.flush();
			//innerStopWatch.stop();
			//System.out.println("ELAPSED TIME (flush OTHeader): " + innerStopWatch.getElapsedTime() + " ms");
			//innerStopWatch = null;

			// EMAIL NOTIFICATIONS
			this.otEmailNotificationEJB.sendEmailNotifications(otHeader);
			
			// EVENT LOG
			this.writeToEventLog(null, otHeader.getLatestDetail());
		
			//outerStopWatch.stop();
			//System.out.println("ELAPSED TIME (saveOvertimeRequest()): " + outerStopWatch.getElapsedTime() + " ms");
			//System.out.println("OvertimeEJBImpl.saveOvertimeRequest() : END");
			//outerStopWatch = null;
		} catch (EntityExistsException e) {
			e.printStackTrace();
			throw new AlohaServerException(e);
		} catch (IllegalStateException e) {
			e.printStackTrace();
			throw new AlohaServerException(e);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			throw new AlohaServerException(e);
		} catch (TransactionRequiredException e) {
			e.printStackTrace();
			throw new AlohaServerException(e);
		} catch (OptimisticLockException e) {
			e.printStackTrace();
			throw new AlohaServerException(e);
		} catch (Exception e) {
			e.printStackTrace();
			throw new AlohaServerException(e);
		}
	}
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public OTHeader updateOTHeader(OTHeader otHeader) throws AlohaServerException {
		try {

			// STOP WATCH - BEGIN
			//StopWatch outerStopWatch = new StopWatch();
			//outerStopWatch.start();
			
			// MERGE
			OTHeader mergedOTHeader = this.entityManager.merge(otHeader);

			// FLUSH
			this.entityManager.flush();
			
			// STOP WATCH - END
			//outerStopWatch.stop();
			//System.out.println("ELAPSED TIME (updateOTHeader()): " + outerStopWatch.getElapsedTime() + " ms");
			//outerStopWatch = null;

			// RETURN
			return mergedOTHeader;
			
		}  catch (IllegalStateException e) {
			e.printStackTrace();
			throw new AlohaServerException(e);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			throw new AlohaServerException(e);
		} catch (TransactionRequiredException e) {
			e.printStackTrace();
			throw new AlohaServerException(e);
		} catch (OptimisticLockException e) {
			e.printStackTrace();
			throw new AlohaServerException(e, AlohaServerException.ExceptionType.OPTIMISTIC_LOCK);
		} catch (Exception e) {
			e.printStackTrace();
			throw new AlohaServerException(e);
		}		
	}	
	
	
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public OTDetail updateOTDetail(OTDetail otDetail) throws AlohaServerException {
		try {
			//System.out.println("OvertimeEJBImpl.updateOTDetail() : BEGIN");

			// STOP WATCHES
			//StopWatch outerStopWatch = new StopWatch();
			//StopWatch innerStopWatch = new StopWatch();
			
			//outerStopWatch.start();
			
			OTDetail oldOTDetail = this.retrieveDetailByID(otDetail.getId());
			String oldValue = oldOTDetail.toEventLog();
			
			// MERGE
			//innerStopWatch.start();
			OTDetail mergedOTDetail = this.entityManager.merge(otDetail);
			//innerStopWatch.stop();
			//System.out.println("ELAPSED TIME (merge OTDetail): " + innerStopWatch.getElapsedTime() + " ms");
			//innerStopWatch = null;

			// FLUSH
			//innerStopWatch = new StopWatch();
			//innerStopWatch.start();
			this.entityManager.flush();
			//innerStopWatch.stop();
			//System.out.println("ELAPSED TIME (flush OTDetail): " + innerStopWatch.getElapsedTime() + " ms");
			//innerStopWatch = null;

			// EMAIL NOTIFICATIONS
			this.otEmailNotificationEJB.sendEmailNotifications(mergedOTDetail);			
			
			// EVENT LOG
			this.writeToEventLog(oldValue, mergedOTDetail);
			
			//outerStopWatch.stop();
			//System.out.println("ELAPSED TIME (updateOTDetail()): " + outerStopWatch.getElapsedTime() + " ms");
			//System.out.println("OvertimeEJBImpl.updateOTDetail() : END");
			//outerStopWatch = null;
			return mergedOTDetail; 			
		}  catch (IllegalStateException e) {
			e.printStackTrace();
			throw new AlohaServerException(e);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			throw new AlohaServerException(e);
		} catch (TransactionRequiredException e) {
			e.printStackTrace();
			throw new AlohaServerException(e);
		} catch (OptimisticLockException e) {
			e.printStackTrace();
			throw new AlohaServerException(e, AlohaServerException.ExceptionType.OPTIMISTIC_LOCK);
		} catch (Exception e) {
			e.printStackTrace();
			throw new AlohaServerException(e);
		}		
	}
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public OTDetail retrieveDetailByID(long otDetailId) throws AlohaServerException {
		try {
			return this.entityManager.find(OTDetail.class, otDetailId);
		} catch (IllegalStateException ise) {
			ise.printStackTrace();
			throw new AlohaServerException(ise);
		} catch(IllegalArgumentException iae) {
			iae.printStackTrace();
			throw new AlohaServerException(iae);
		} catch (Exception e) {
			e.printStackTrace();
			throw new AlohaServerException(e);
		}		
	}
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public OTHeader findById(long id) throws AlohaServerException {
		try {
			return this.entityManager.find(OTHeader.class, id);
		} catch (IllegalStateException e) {
			e.printStackTrace();
			throw new AlohaServerException(e);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			throw new AlohaServerException(e);
		} catch (Exception e) {
			e.printStackTrace();
			throw new AlohaServerException(e); 
		}	
	}		
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	@SuppressWarnings("unchecked")
	public List<OTDetail> retrieveEmployeeList(long employeeUserId) throws AlohaServerException {
		try {
			Query query = this.entityManager.createNamedQuery(OTDetail.QueryNames.RETRIEVE_FOR_EMPLOYEE);
			query.setParameter(OTDetail.QueryParamNames.EMPLOYEE_USER_ID, employeeUserId);
			return (List<OTDetail>)query.getResultList();
		} catch (IllegalStateException ise) {
			ise.printStackTrace();
			throw new AlohaServerException(ise);
		} catch(IllegalArgumentException iae) {
			iae.printStackTrace();
			throw new AlohaServerException(iae);
		} catch (Exception e) {
			e.printStackTrace();
			throw new AlohaServerException(e);
		}		
	}	
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	@SuppressWarnings("unchecked")
	public List<OTDetail> retrieveSubmitterList(long submitterUserId) throws AlohaServerException {
		try {
			Query query = this.entityManager.createNamedQuery(OTDetail.QueryNames.RETRIEVE_FOR_SUBMITTER);
			query.setParameter(OTDetail.QueryParamNames.SUBMITTER_USER_ID, submitterUserId);
			return (List<OTDetail>)query.getResultList();
		} catch (IllegalStateException ise) {
			ise.printStackTrace();
			throw new AlohaServerException(ise);
		} catch(IllegalArgumentException iae) {
			iae.printStackTrace();
			throw new AlohaServerException(iae);
		} catch (Exception e) {
			e.printStackTrace();
			throw new AlohaServerException(e);
		}		
	}		
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	@SuppressWarnings("unchecked")
	public List<OTDetail> retrieveSupervisorList(long supervisorUserId, String otStatusCode) throws AlohaServerException {
		try {
			Query query = this.entityManager.createNamedQuery(OTDetail.QueryNames.RETRIEVE_FOR_SUPERVISOR);
			query.setParameter(OTDetail.QueryParamNames.SUPERVISOR_USER_ID, supervisorUserId);
			query.setParameter(OTDetail.QueryParamNames.OT_STATUS_CODE, otStatusCode);
			return (List<OTDetail>)query.getResultList();
		} catch (IllegalStateException ise) {
			ise.printStackTrace();
			throw new AlohaServerException(ise);
		} catch(IllegalArgumentException iae) {
			iae.printStackTrace();
			throw new AlohaServerException(iae);
		} catch (Exception e) {
			e.printStackTrace();
			throw new AlohaServerException(e);
		}		
	}			
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	@SuppressWarnings("unchecked")
	public List<OTDetail> retrieveSupervisorList(long supervisorUserId) throws AlohaServerException {
		try {
			Query query = this.entityManager.createNamedQuery(OTDetail.QueryNames.RETRIEVE_ALL_FOR_SUPERVISOR);
			query.setParameter(OTDetail.QueryParamNames.SUPERVISOR_USER_ID, supervisorUserId);
			return (List<OTDetail>)query.getResultList();
		} catch (IllegalStateException ise) {
			ise.printStackTrace();
			throw new AlohaServerException(ise);
		} catch(IllegalArgumentException iae) {
			iae.printStackTrace();
			throw new AlohaServerException(iae);
		} catch (Exception e) {
			e.printStackTrace();
			throw new AlohaServerException(e);
		}		
	}			
	
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	@SuppressWarnings("unchecked")
	public List<OTSalaryGrade> retrieveAllOTSalaryGrade() throws AlohaServerException {
		try {
			Query query = this.entityManager.createNamedQuery(OTSalaryGrade.QueryNames.RETRIEVE_ALL);
			return (List<OTSalaryGrade>)query.getResultList();
		} catch (Exception e) {
			e.printStackTrace();
			throw new AlohaServerException(e);		
		}		
	}
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	@SuppressWarnings("unchecked")
	public List<OTPayPeriod> retrieveAllOTPayPeriod() throws AlohaServerException {
		try {
			Query query = this.entityManager.createNamedQuery(OTPayPeriod.QueryNames.RETRIEVE_ALL);
			return (List<OTPayPeriod>)query.getResultList();
		} catch (Exception e) {
			e.printStackTrace();
			throw new AlohaServerException(e);		
		}		
	}	
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	@SuppressWarnings("unchecked")
	public List<OTType> retrieveAllOTType() throws AlohaServerException {
		try {
			Query query = this.entityManager.createNamedQuery(OTType.QueryNames.RETRIEVE_ALL);
			return (List<OTType>)query.getResultList();
		} catch (Exception e) {
			e.printStackTrace();
			throw new AlohaServerException(e);		
		}		
	}
	
	//2012-10-11 JJM 48969: Add queries for restrictive codes for Employee
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	@SuppressWarnings("unchecked")
	public List<OTType> retrieveAllOTTypeEmp() throws AlohaServerException {
		try {
			Query query = this.entityManager.createNamedQuery(OTType.QueryNames.RETRIEVE_ALL_EMP);
			//query.setParameter(OTType.QueryParamNames.RETRIEVE_ALL_EMP, effDate);
			List<OTType> ot=query.getResultList();
			return (ot);
		} catch (Exception e) {
			e.printStackTrace();
			throw new AlohaServerException(e);		
		}		
	}
	//2012-10-11 JJM 48969: Add queries for non-restrictive codes for Timekeeper / Supervisor
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	@SuppressWarnings("unchecked")
	public List<OTType> retrieveAllOTTypeOBO() throws AlohaServerException {
		try {
			Query query = this.entityManager.createNamedQuery(OTType.QueryNames.RETRIEVE_ALL_OBO);
			//query.setParameter(OTType.QueryParamNames.RETRIEVE_ALL_OBO, effDate);
			List<OTType> ot=query.getResultList();
			return (ot);
		} catch (Exception e) {
			e.printStackTrace();
			throw new AlohaServerException(e);		
		}		
	}
	
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	@SuppressWarnings("unchecked")
	public List<OTEntityType> retrieveAllOTEntityType() throws AlohaServerException {
		try {
			Query query = this.entityManager.createNamedQuery(OTEntityType.QueryNames.RETRIEVE_ALL);
			return (List<OTEntityType>)query.getResultList();
		} catch (Exception e) {
			e.printStackTrace();
			throw new AlohaServerException(e);		
		}		
	}
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	@SuppressWarnings("unchecked")
	public List<OTActorType> retrieveAllOTActorType() throws AlohaServerException {
		try {
			Query query = this.entityManager.createNamedQuery(OTActorType.QueryNames.RETRIEVE_ALL);
			return (List<OTActorType>)query.getResultList();
		} catch (Exception e) {
			e.printStackTrace();
			throw new AlohaServerException(e);		
		}		
	}	
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	@SuppressWarnings("unchecked")
	public List<OTIndivStatus> retrieveAllOTIndivStatus() throws AlohaServerException {
		try {
			Query query = this.entityManager.createNamedQuery(OTIndivStatus.QueryNames.RETRIEVE_ALL);
			return (List<OTIndivStatus>)query.getResultList();
		} catch (Exception e) {
			e.printStackTrace();
			throw new AlohaServerException(e);		
		}		
	}		
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	@SuppressWarnings("unchecked")
	public List<OTIndivStatusTrans> retrieveAllOTIndivStatusTrans() throws AlohaServerException {
		try {
			Query query = this.entityManager.createNamedQuery(OTIndivStatusTrans.QueryNames.RETRIEVE_ALL);
			return (List<OTIndivStatusTrans>)query.getResultList();
		} catch (Exception e) {
			e.printStackTrace();
			throw new AlohaServerException(e);		
		}		
	}	
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public String retrieveSalaryGradeKey(long userId) throws AlohaServerException{
		String key = null;

		Connection conn = null;
		PreparedStatement ps = null;
		
		try {
			conn = dataSource.getConnection();
			ps = conn.prepareStatement(SqlUtil.GET_SALARY_GRADE_KEY_SQL);
			ps.setLong(1, userId);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				key = rs.getString("salary_grade_key");
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new AlohaServerException(sqle);
		} finally {
			SqlUtil.closePreparedStatement(ps);
			SqlUtil.closeConnection(conn);
			ps = null;
			conn = null;
		}		
		return key;
	}
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public List<KeyValuePair> retrieveOTBalances(long employeeId) throws AlohaServerException {
		List<KeyValuePair> list = new ArrayList<KeyValuePair>();
		
		Connection conn = null;
		PreparedStatement ps = null;
		
		try {
			conn = dataSource.getConnection();
			ps = conn.prepareStatement(SqlUtil.GET_OT_BALANCES);
			ps.setLong(1, employeeId);
			ps.setLong(2, employeeId);
			
			ResultSet rs = ps.executeQuery();
			while ( rs.next()) {
				KeyValuePair model = new KeyValuePair();
				
				// OT TYPE
				model.setKey(rs.getString("OTType"));

				// OT BALANCE
				String otBalance = rs.getString("OTBalance");
				if ( !StringUtil.isNullOrEmpty(otBalance)) {
					BigDecimal bd = new BigDecimal(otBalance);
					bd.setScale(1, RoundingMode.HALF_UP);
					NumberFormat nf = NumberFormat.getNumberInstance();
					model.setValue(nf.format(bd.doubleValue()));				
				}
				list.add(model);
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new AlohaServerException(sqle);
		} catch (Exception e) {
			e.printStackTrace();
			throw new AlohaServerException(e);
		} finally {
			SqlUtil.closePreparedStatement(ps);
			SqlUtil.closeConnection(conn);
			ps = null;
			conn = null;
		}					
		return list;
	}
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	private void writeToEventLog (String oldValue, OTDetail newOTDetail) {
		try {
			//System.out.println("OvertimeEJBImpl.writeToEventLog() : BEGIN");
			//StopWatch stopWatch = new StopWatch();
			//stopWatch.start();
			
			
			EventLog eventLog = new EventLog();
			EventType eventType = this.determinEventType(newOTDetail);
			eventLog.setEventType(eventType);
			eventLog.setUserCreated(newOTDetail.getLatestHistoricalEntry().getActor().getFullName());

			// SET NEW VALUE (can't be null)
			String newValue = newOTDetail.toEventLog();
			if ( newValue.length() > 4000) {
				eventLog.setNewValue(newValue.substring(0, 4000));
			} else {
				eventLog.setNewValue(newValue);
			}
			
			// OLD VALUE (if populated)
			if (oldValue != null) {
				if (oldValue.length() > 4000) {
					eventLog.setOldValue(oldValue.substring(0, 4000));
				} else {
					eventLog.setOldValue(oldValue);
				}	
			}
			this.eventLoggerEJB.logEvent(eventLog);
			
			//stopWatch.stop();
			//System.out.println("ELAPSED TIME (writeToEventLog): " + stopWatch.getElapsedTime() + " ms");
			//System.out.println("OvertimeEJBImpl.writeToEventLog() : END");
			//stopWatch = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}	
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	private EventType determinEventType(OTDetail otDetail) throws AlohaServerException {
		EventType eventType = null;
		if ( otDetail.isSubmitted()) {
			eventType = this.eventLoggerEJB.getEventType(EventType.EventTypeValue.OT_INDIV_SUBMIT.toString());; 
		} else if (otDetail.isCancelled()) {
			eventType = this.eventLoggerEJB.getEventType(EventType.EventTypeValue.OT_INDIV_CANCEL.toString());;			
		}  else if (otDetail.isReceived()) {
			eventType = this.eventLoggerEJB.getEventType(EventType.EventTypeValue.OT_INDIV_RECEIVE.toString());;
		} else if (otDetail.isApproved()) {
			eventType = this.eventLoggerEJB.getEventType(EventType.EventTypeValue.OT_INDIV_APPROVE.toString());;			
		} else if (otDetail.isDenied()) {
			eventType = this.eventLoggerEJB.getEventType(EventType.EventTypeValue.OT_INDIV_DENY.toString());;			
		}
		return eventType;
	}
}