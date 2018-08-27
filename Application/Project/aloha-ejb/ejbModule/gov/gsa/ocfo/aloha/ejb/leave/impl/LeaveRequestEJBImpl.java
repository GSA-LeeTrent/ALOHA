package gov.gsa.ocfo.aloha.ejb.leave.impl;

import gov.gsa.ocfo.aloha.ejb.EmailNotificationEJB;
import gov.gsa.ocfo.aloha.ejb.EventLoggerEJB;
import gov.gsa.ocfo.aloha.ejb.leave.LRChangeSupvNotifyEJB;
import gov.gsa.ocfo.aloha.ejb.leave.LeaveRequestEJB;
import gov.gsa.ocfo.aloha.ejb.leave.LeaveTypeEJB;
import gov.gsa.ocfo.aloha.ejb.leave.recon.LeaveReconWizardPendingEJB;
import gov.gsa.ocfo.aloha.exception.AlohaServerException;
import gov.gsa.ocfo.aloha.model.entity.AlohaUser;
import gov.gsa.ocfo.aloha.model.entity.audit.EventLog;
import gov.gsa.ocfo.aloha.model.entity.audit.EventType;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveDetail;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveHeader;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveHistory;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveItem;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveSupervisorChangeHistory;
import gov.gsa.ocfo.aloha.model.leave.LeaveDetailRow;
import gov.gsa.ocfo.aloha.model.leave.LeaveHeaderRow;
import gov.gsa.ocfo.aloha.util.SqlUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
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
import javax.persistence.TransactionRequiredException;
import javax.sql.DataSource;

@Stateless
public class LeaveRequestEJBImpl implements LeaveRequestEJB {
	@PersistenceContext (unitName="aloha-pu")
	private EntityManager entityManager;

	@Resource(name = "aloha-ds")
	private DataSource dataSource;
	
	@EJB
	EmailNotificationEJB emailNotificationEJB;
	
	@EJB
	LRChangeSupvNotifyEJB lrChangeSupvNotifyEJB;
	
	@EJB
	EventLoggerEJB eventLoggerEJB;
	
	@EJB
	LeaveReconWizardPendingEJB leaveReconWizardPendingEJB;

	@EJB
	LeaveTypeEJB leaveTypeEJB;
	
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void saveLeaveRequest(LeaveHeader leaveHeader) throws AlohaServerException {
		try {
			//System.out.println("LeaveRequestEJBImpl.saveLeaveRequest() : BEGIN");
			
			// STOP WATCHES
			//StopWatch outerStopWatch = new StopWatch();
			//StopWatch innerStopWatch = new StopWatch();
			
			//outerStopWatch.start();
			
			// PERSIST
			//innerStopWatch.start();
			this.entityManager.persist(leaveHeader);
			//innerStopWatch.stop();
			//System.out.println("ELAPSED TIME (persist LeaveHeader): " + innerStopWatch.getElapsedTime() + " ms");
			//innerStopWatch = null;
			
			// FLUSH
			//innerStopWatch = new StopWatch();
			//innerStopWatch.start();
			this.entityManager.flush();
			//innerStopWatch.stop();
			//System.out.println("ELAPSED TIME (flush LeaveHeader): " + innerStopWatch.getElapsedTime() + " ms");
			//innerStopWatch = null;

			// EMAIL NOTIFICATIONS
			this.emailNotificationEJB.sendEmailNotifications(leaveHeader);
			
			// EVENT LOG
			this.writeToEventLog(null, leaveHeader.getLatestDetail());
			
			//outerStopWatch.stop();
			//System.out.println("ELAPSED TIME (saveLeaveRequest()): " + outerStopWatch.getElapsedTime() + " ms");
			//System.out.println("LeaveRequestEJBImpl.saveLeaveRequest() : END");
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
			throw new AlohaServerException(e, AlohaServerException.ExceptionType.OPTIMISTIC_LOCK);
		} catch (Exception e) {
			e.printStackTrace();
			throw new AlohaServerException(e);
		}
	}
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public LeaveDetail updateLeaveDetail(LeaveDetail leaveDetail) throws AlohaServerException {
		try {
			//System.out.println("LeaveRequestEJBImpl.updateLeaveDetail() : BEGIN");

			// STOP WATCHES
			//StopWatch outerStopWatch = new StopWatch();
			//StopWatch innerStopWatch = new StopWatch();
			
			//outerStopWatch.start();
			
			LeaveDetail oldLeaveDetail = this.getLeaveDetail(leaveDetail.getId());
			String oldValue = oldLeaveDetail.toEventLog();

			// MERGE
			//innerStopWatch.start();
			LeaveDetail mergedLeaveDetail = this.entityManager.merge(leaveDetail);
			//innerStopWatch.stop();
			//System.out.println("ELAPSED TIME (merge LeaveDetail): " + innerStopWatch.getElapsedTime() + " ms");
			//innerStopWatch = null;

			// FLUSH
			//innerStopWatch = new StopWatch();
			//innerStopWatch.start();
			this.entityManager.flush();
			//innerStopWatch.stop();
			//System.out.println("ELAPSED TIME (flush LeaveDetail): " + innerStopWatch.getElapsedTime() + " ms");
			//innerStopWatch = null;

			// EMAIL NOTIFICATIONS
			this.emailNotificationEJB.sendEmailNotifications(mergedLeaveDetail);			
			
			// EVENT LOG
			this.writeToEventLog(oldValue, mergedLeaveDetail);
			
			//outerStopWatch.stop();
			//System.out.println("ELAPSED TIME (updateLeaveDetail()): " + outerStopWatch.getElapsedTime() + " ms");
			//System.out.println("LeaveRequestEJBImpl.updateLeaveDetail() : END");
			//outerStopWatch = null;
			
			///////////////////////////////////////
			// PRUNE LR_RECON_WIZARD_PENDING ITEMS
			///////////////////////////////////////
			if (mergedLeaveDetail.isApproved() == true 
					|| mergedLeaveDetail.isWithdrawn() == true 
					|| mergedLeaveDetail.isDenied() == true) {
				this.leaveReconWizardPendingEJB.prunePendingWizardItems(mergedLeaveDetail);
			}
			
			return mergedLeaveDetail;
			
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
	public LeaveHeader updateLeaveHeader(LeaveHeader leaveHeader) throws AlohaServerException {
		try {
			//System.out.println("LeaveRequestEJBImpl.updateLeaveHeader() : BEGIN");

			// STOP WATCHES
			//StopWatch outerStopWatch = new StopWatch();
			//StopWatch innerStopWatch = new StopWatch();
			
			//outerStopWatch.start();
			
			LeaveDetail nextToLastDetail = leaveHeader.getNextToLastDetail();
			String oldValue = null;
			if ( nextToLastDetail != null) {
				oldValue = nextToLastDetail.toEventLog();	
			}

			// MERGE
			//innerStopWatch.start();
			LeaveHeader mergedLeaveHeader = this.entityManager.merge(leaveHeader);
			//innerStopWatch.stop();
			//System.out.println("ELAPSED TIME (merge LeaveHeader): " + innerStopWatch.getElapsedTime() + " ms");
			//innerStopWatch = null;

			// FLUSH
			//innerStopWatch = new StopWatch();
			//innerStopWatch.start();
			this.entityManager.flush();
			//innerStopWatch.stop();
			//System.out.println("ELAPSED TIME (flush LeaveHeader): " + innerStopWatch.getElapsedTime() + " ms");
			//innerStopWatch = null;

			// EMAIL NOTIFICATIONS
			this.emailNotificationEJB.sendEmailNotifications(mergedLeaveHeader);			
			
			// EVENT LOG
			this.writeToEventLog(oldValue, mergedLeaveHeader.getLatestDetail());			

			//outerStopWatch.stop();
			//System.out.println("ELAPSED TIME (updateLeaveHeader()): " + outerStopWatch.getElapsedTime() + " ms");
			//System.out.println("LeaveRequestEJBImpl.updateLeaveHeader() : END");
			//outerStopWatch = null;
			
			///////////////////////////////////////
			// PRUNE LR_RECON_WIZARD_PENDING ITEMS
			///////////////////////////////////////
			if ( mergedLeaveHeader.getLastestApprovedDetail() != null ) {
				this.leaveReconWizardPendingEJB.prunePendingWizardItems(mergedLeaveHeader.getLastestApprovedDetail());
			}
			
			return mergedLeaveHeader;
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
	public LeaveHeader findById(long id) throws AlohaServerException {
		try {
			return this.entityManager.find(LeaveHeader.class, id);
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
	public LeaveDetail getLeaveDetail(long leaveDetailId) throws AlohaServerException {
		try {
			return this.entityManager.find(LeaveDetail.class, leaveDetailId);
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
	
//	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
//	@SuppressWarnings("unchecked")
//	public List<LeaveItem> getLeaveItems(long employeeUserId, Date ppStartDate, List<String> leaveStatusCodes) throws AlohaServerException {
//		try {
//			Query query = this.entityManager.createNamedQuery(LeaveItem.QueryNames.RETRIEVE_BY_EMPLOYEE_PAY_PERIOD_LEAVE_STATUS);
//			query.setParameter(LeaveItem.QueryParamNames.EMPLOYEE_USER_ID, employeeUserId);
//			query.setParameter(LeaveItem.QueryParamNames.PAY_PERIOD_START_DATE, ppStartDate);
//			//query.setParameter(LeaveItem.QueryParamNames.LEAVE_STATUS_CODE_LIST, leaveStatusCodes);
//			query.setParameter(LeaveItem.QueryParamNames.LEAVE_STATUS_CODE_LIST, leaveStatusCodes.toArray().toString());
//			return (List<LeaveItem>)query.getResultList();
//		} catch (IllegalStateException ise) {
//			ise.printStackTrace();
//			throw new AlohaServerException(ise);
//		} catch(IllegalArgumentException iae) {
//			iae.printStackTrace();
//			throw new AlohaServerException(iae);
//		} catch (Exception e) {
//			e.printStackTrace();
//			throw new AlohaServerException(e);
//		}		
//	}		
	

	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public List<LeaveItem> getPriorLeaveItemsForCreate(long employeeUserId, Date ppStartDate) throws AlohaServerException {
		
		List<LeaveItem> leaveItemList = new ArrayList<LeaveItem>();

		Connection conn = null;
		PreparedStatement prepStmt = null;
		
		try {
			conn = dataSource.getConnection();
			String sql = SqlUtil.PRIOR_LEAVE_ITEMS_FOR_CREATE_SQL;
			
			prepStmt = conn.prepareStatement(sql);
			
			////////////////////////////////////////
			// EMPLOYEE USER ID
			////////////////////////////////////////
			prepStmt.setLong(1 , employeeUserId);
			
			////////////////////////////////////////
			// PAY PERIOD START DATE
			////////////////////////////////////////
			prepStmt.setDate(2, new java.sql.Date(ppStartDate.getTime()));
			
			ResultSet resultSet = prepStmt.executeQuery();
			while (resultSet.next()) {
				
				LeaveItem leaveItem = new LeaveItem();
				
				leaveItem.setId(resultSet.getLong("LR_ITEM_ID"));
				leaveItem.setLeaveDetail(this.getLeaveDetail(resultSet.getLong("LR_DETAIL_ID")));
				leaveItem.setLeaveType(this.leaveTypeEJB.getLeaveType(resultSet.getLong("LR_TYPE_ID")));
				leaveItem.setLeaveDate(resultSet.getDate("LEAVE_DATE"));
				leaveItem.setLeaveHours(resultSet.getBigDecimal("LEAVE_HOURS"));
				
				leaveItemList.add(leaveItem);
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new AlohaServerException(sqle);
		} catch (Exception e) {
			e.printStackTrace();
			throw new AlohaServerException(e);
		} finally {
			SqlUtil.closePreparedStatement(prepStmt);
			SqlUtil.closeConnection(conn);
			prepStmt = null;
			conn = null;
		}		
		return leaveItemList;
	}
	
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public List<LeaveItem> getPriorLeaveItemsForAmend(long employeeUserId, Date ppStartDate, long leaveDetailIdToExclude) throws AlohaServerException {
		
		List<LeaveItem> leaveItemList = new ArrayList<LeaveItem>();

		Connection conn = null;
		PreparedStatement prepStmt = null;
		
		try {
			conn = dataSource.getConnection();
			String sql = SqlUtil.PRIOR_LEAVE_ITEMS_FOR_AMEND_SQL;
			
			prepStmt = conn.prepareStatement(sql);
			
			////////////////////////////////////////
			// EMPLOYEE USER ID
			////////////////////////////////////////
			prepStmt.setLong(1 , employeeUserId);
			
			////////////////////////////////////////
			// LEAVE DETAIL ID TO EXCLUDE
			////////////////////////////////////////
			prepStmt.setLong(2 , leaveDetailIdToExclude);

			////////////////////////////////////////
			// PAY PERIOD START DATE
			////////////////////////////////////////
			prepStmt.setDate(3, new java.sql.Date(ppStartDate.getTime()));
			
			ResultSet resultSet = prepStmt.executeQuery();
			while (resultSet.next()) {
				
				LeaveItem leaveItem = new LeaveItem();
				
				leaveItem.setId(resultSet.getLong("LR_ITEM_ID"));
				leaveItem.setLeaveDetail(this.getLeaveDetail(resultSet.getLong("LR_DETAIL_ID")));
				leaveItem.setLeaveType(this.leaveTypeEJB.getLeaveType(resultSet.getLong("LR_TYPE_ID")));
				leaveItem.setLeaveDate(resultSet.getDate("LEAVE_DATE"));
				leaveItem.setLeaveHours(resultSet.getBigDecimal("LEAVE_HOURS"));
				
				leaveItemList.add(leaveItem);
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new AlohaServerException(sqle);
		} catch (Exception e) {
			e.printStackTrace();
			throw new AlohaServerException(e);
		} finally {
			SqlUtil.closePreparedStatement(prepStmt);
			SqlUtil.closeConnection(conn);
			prepStmt = null;
			conn = null;
		}		
		return leaveItemList;
	}	
	
	
	
	
//	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
//	@SuppressWarnings("unchecked")
//	public List<LeaveItem> getLeaveItems(long employeeUserId, Date ppStartDate, List<String> leaveStatusCodes, long leaveDetailIdToExclude) throws AlohaServerException {
//		try {
//			Query query = this.entityManager.createNamedQuery(LeaveItem.QueryNames.RETRIEVE_BY_EMPLOYEE_PAY_PERIOD_LEAVE_STATUS_EXCLUDE_LEAVE_DETAIL_ID);
//			query.setParameter(LeaveItem.QueryParamNames.EMPLOYEE_USER_ID, employeeUserId);
//			query.setParameter(LeaveItem.QueryParamNames.PAY_PERIOD_START_DATE, ppStartDate);
//			//query.setParameter(LeaveItem.QueryParamNames.LEAVE_STATUS_CODE_LIST, leaveStatusCodes);
//			query.setParameter(LeaveItem.QueryParamNames.LEAVE_STATUS_CODE_LIST, leaveStatusCodes.toArray().toString());
//			query.setParameter(LeaveItem.QueryParamNames.LEAVE_DETAIL_ID_TO_EXCLUDE, leaveDetailIdToExclude);
//			return (List<LeaveItem>)query.getResultList();
//		} catch (IllegalStateException ise) {
//			ise.printStackTrace();
//			throw new AlohaServerException(ise);
//		} catch(IllegalArgumentException iae) {
//			iae.printStackTrace();
//			throw new AlohaServerException(iae);
//		} catch (Exception e) {
//			e.printStackTrace();
//			throw new AlohaServerException(e);
//		}		
//	}			
	
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	private void writeToEventLog (String oldValue, LeaveDetail newLeaveDetail) {
		try {
			//System.out.println("LeaveRequestEJBImpl.writeToEventLog() : BEGIN");
			//StopWatch stopWatch = new StopWatch();
			//stopWatch.start();

			EventLog eventLog = new EventLog();
			EventType eventType;
	
			eventType = this.determinEventType(newLeaveDetail);
			eventLog.setEventType(eventType);
			eventLog.setUserCreated(newLeaveDetail.getLatestHistoricalEntry().getActor().getFullName());
	
			// SET NEW VALUE (can't be null)
			String newValue = newLeaveDetail.toEventLog();
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
			//System.out.println("LeaveRequestEJBImpl.writeToEventLog() : END");
			//stopWatch = null;
		} catch (AlohaServerException e) {
			e.printStackTrace();
		}
	}	
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	private EventType determinEventType(LeaveDetail leaveDetail) throws AlohaServerException {
		EventType eventType = null;
		if ( leaveDetail.isSubmitted()) {
			eventType = this.eventLoggerEJB.getEventType(EventType.EventTypeValue.LR_SUBMITTED.toString());; 
		} else if (leaveDetail.isCancelled()) {
			eventType = this.eventLoggerEJB.getEventType(EventType.EventTypeValue.LR_CANCELED.toString());;			
		} else if (leaveDetail.isApproved()) {
			eventType = this.eventLoggerEJB.getEventType(EventType.EventTypeValue.LR_APPROVED.toString());;			
		} else if (leaveDetail.isDenied()) {
			eventType = this.eventLoggerEJB.getEventType(EventType.EventTypeValue.LR_DENIED.toString());;			
		} else if (leaveDetail.isPendingWithdrawal()) {
			eventType = this.eventLoggerEJB.getEventType(EventType.EventTypeValue.LR_PEND_WITHDRAWAL.toString());;			
		} else if (leaveDetail.isWithdrawn()) {
			eventType = this.eventLoggerEJB.getEventType(EventType.EventTypeValue.LR_WITHDRAWN.toString());;			
		} else if (leaveDetail.isPendingAmendment()) {
			eventType = this.eventLoggerEJB.getEventType(EventType.EventTypeValue.LR_PEND_AMEND.toString());;			
		} else if (leaveDetail.isAmended()) {
			eventType = this.eventLoggerEJB.getEventType(EventType.EventTypeValue.LR_AMENDED.toString());;			
		}		
		return eventType;
	}
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public List<LeaveHeaderRow> getSubmitOwnList(AlohaUser alohaUser) throws AlohaServerException {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = dataSource.getConnection();
			ps = conn.prepareStatement(SqlUtil.GET_LR_LIST_FOR_SUBMIT_OWN_SQL);
			ps.setLong(1, alohaUser.getUserId());
			return this.buildLRList(alohaUser, ps.executeQuery());
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
	}
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public List<LeaveHeaderRow> getOnBehalfOfList(AlohaUser alohaUser) throws AlohaServerException {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = dataSource.getConnection();
			ps = conn.prepareStatement(SqlUtil.GET_LR_LIST_FOR_ON_BEHALF_OF_SQL);
			ps.setLong(1, alohaUser.getUserId());
			return this.buildLRList(alohaUser, ps.executeQuery());
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
	}	
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public List<LeaveHeaderRow> getApproverList(AlohaUser alohaUser) throws AlohaServerException {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = dataSource.getConnection();
			ps = conn.prepareStatement(SqlUtil.GET_LR_LIST_FOR_APPROVER_SQL);
			ps.setLong(1, alohaUser.getUserId());
			return this.buildLRList(alohaUser, ps.executeQuery());
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
	}		
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	private List<LeaveHeaderRow> buildLRList(AlohaUser alohaUser, ResultSet rs) throws SQLException {
		List<LeaveHeaderRow> lrList = new ArrayList<LeaveHeaderRow>();
		LeaveHeaderRow headerRow = null;
		long lastRequestId = 0L;
		while (rs.next()) {
			long requestId = rs.getLong("REQUEST_ID");
			if (requestId != lastRequestId) {
				headerRow = new LeaveHeaderRow(	alohaUser,
												requestId, 
												rs.getLong("EMPLOYEE_USER_ID"),
												rs.getString("EMPLOYEE_FIRST_NAME"), 
												rs.getString("EMPLOYEE_LAST_NAME"), 
												rs.getDate("HDR_DATE_SUBMITTED"));
				lrList.add(headerRow);
				lastRequestId = requestId;
			}
			LeaveDetailRow detailRow = new LeaveDetailRow(	rs.getLong("DETAIL_ID"), 
															rs.getLong("SUBMITTER_USER_ID"),
															rs.getLong("APPROVER_USER_ID"),
															rs.getDate("PP_START_DATE"), 
															rs.getDate("PP_END_DATE"),
															rs.getString("STATUS_CODE"),
															rs.getString("STATUS_LABEL"),
															rs.getString("SUBMITTER_FIRST_NAME"), 
															rs.getString("SUBMITTER_LAST_NAME"),
															rs.getDate("DTL_LAST_UPDATED"));
			detailRow.setHeaderRow(headerRow);
			headerRow.addLeaveDetail(detailRow);
		}
		return lrList;
	}
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public LeaveDetail updateLeaveDetail(LeaveDetail leaveDetail,
			LeaveSupervisorChangeHistory supervisorChangeHistory, AlohaUser oldSupvervisor) throws AlohaServerException {
		try {

			LeaveDetail oldLeaveDetail = this.getLeaveDetail(leaveDetail.getId());
			String oldEventLogValue = oldLeaveDetail.toEventLog();
			List<LeaveHistory> oldHistory = oldLeaveDetail.getLeaveHistories();
			
			// MERGE (LeaveDetail)
			LeaveDetail mergedLeaveDetail = this.entityManager.merge(leaveDetail);

			// FLUSH (LeaveDetail)
			this.entityManager.flush();
			
			List<LeaveHistory> newHistory = mergedLeaveDetail.getLeaveHistories();
		
//			System.out.println("oldHistory.size(): " + oldHistory.size());
//			System.out.println("newHistory.size(): " + newHistory.size());
//			this.debug("OLD HISTORY IDs: ", oldHistory);
//			this.debug("NEW HISTORY IDs: ", newHistory);
			
			LeaveHistory justAddedLeaveHistory = null;
			for ( LeaveHistory newLeaveHistory: newHistory) {
				if ( !oldHistory.contains(newLeaveHistory) ) {
					justAddedLeaveHistory = newLeaveHistory;
				} 
			}	

			//System.out.println("justAddedLeaveHistory.getId(): " + justAddedLeaveHistory.getId());
			
			supervisorChangeHistory.setLeaveHistoryId(justAddedLeaveHistory.getId());
			
			// PERSIST (LeaveSupervisorChangeHistory)
			this.entityManager.persist(supervisorChangeHistory);
			
			// FLUSH (LeaveSupervisorChangeHistory)
			this.entityManager.flush();
			
			// EMAIL NOTIFICATIONS
			this.lrChangeSupvNotifyEJB.sendEmailNotifications(mergedLeaveDetail, oldSupvervisor);			
			
			// EVENT LOG
			EventType eventType = this.eventLoggerEJB.getEventType(EventType.EventTypeValue.LR_CHANGE_SUPV.toString());
			this.writeToEventLog(oldEventLogValue, mergedLeaveDetail, eventType);
			
			///////////////////////////////////////
			// PRUNE LR_RECON_WIZARD_PENDING ITEMS
			///////////////////////////////////////
			if (mergedLeaveDetail.isApproved() == true 
					|| mergedLeaveDetail.isWithdrawn() == true 
					|| mergedLeaveDetail.isDenied() == true) {
				this.leaveReconWizardPendingEJB.prunePendingWizardItems(mergedLeaveDetail);
			}
			
			return mergedLeaveDetail;
			
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
	
//	private void debug(String label,  List<LeaveHistory> leaveHistoryList) {
//		StringBuilder sb = new StringBuilder();
//		int index = 0;
//		for (LeaveHistory leaveHistory: leaveHistoryList) {
//			if (index > 0) {
//				sb.append(", ");
//			}
//			sb.append(leaveHistory.getId());
//			index++;	
//		}	
//		System.out.println(label + sb.toString());
//	}
	
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	private void writeToEventLog (String oldValue, LeaveDetail newLeaveDetail, EventType eventType) {
		EventLog eventLog = new EventLog();
		eventLog.setEventType(eventType);
		eventLog.setUserCreated(newLeaveDetail.getLatestHistoricalEntry().getActor().getFullName());

		this.writeToEventLog(eventLog, oldValue, newLeaveDetail.toEventLog());
	}
	
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	private void writeToEventLog (EventLog eventLog, String oldValue, String newValue) {
		// SET NEW VALUE (can't be null)
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
	}
}