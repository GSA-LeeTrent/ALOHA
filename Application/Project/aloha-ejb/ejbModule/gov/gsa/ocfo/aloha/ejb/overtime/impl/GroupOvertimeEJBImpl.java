package gov.gsa.ocfo.aloha.ejb.overtime.impl;

import gov.gsa.ocfo.aloha.ejb.OTEmailNotificationEJB;
import gov.gsa.ocfo.aloha.ejb.overtime.GroupOvertimeEJB;
import gov.gsa.ocfo.aloha.exception.AlohaServerException;
import gov.gsa.ocfo.aloha.model.entity.AlohaUser;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveStatus;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTGroup;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTGroupStatus;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTGroupStatusTrans;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTIndivStatusTrans;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTPayPeriod;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TransactionRequiredException;

@Stateless
public class GroupOvertimeEJBImpl implements GroupOvertimeEJB {
	@PersistenceContext (unitName="aloha-pu")
	private EntityManager entityManager;
	
	@EJB
	OTEmailNotificationEJB otEmailNotificationEJB;
	
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void saveOTGroup(OTGroup otGroup) throws AlohaServerException {
		try {
			//StopWatch stopWatch = new StopWatch();
			//stopWatch.start();
			this.entityManager.persist(otGroup);
			this.entityManager.flush();
			//stopWatch.stop();
			//System.out.println("ELAPSED TIME (saveOTGroup()): " + stopWatch.getElapsedTime() + " ms");
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
	public OTGroup updateOTGroup(OTGroup otGroup) throws AlohaServerException {
		try {

			// STOP WATCH - BEGIN
			//StopWatch stopWatch = new StopWatch();
			//stopWatch.start();
			
			// MERGE
			OTGroup mergedOTGroup = this.entityManager.merge(otGroup);

			// FLUSH
			this.entityManager.flush();
			
			// STOP WATCH - END
			//stopWatch.stop();
			//System.out.println("ELAPSED TIME (updateOTGroup()): " + stopWatch.getElapsedTime() + " ms");
			//stopWatch = null;

			// EMAIL NOTIFICATIONS
			this.otEmailNotificationEJB.sendEmailNotifications(mergedOTGroup);
			
			// RETURN
			return mergedOTGroup;
			
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
	public OTGroup retrieveByGroupId(long otGroupId) throws AlohaServerException {
		try {
			return this.entityManager.find(OTGroup.class, otGroupId);
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
	public List<OTGroupStatus> retrieveAllOTGroupStatus() throws AlohaServerException {
		try {
			Query query = this.entityManager.createNamedQuery(OTGroupStatus.QueryNames.RETRIEVE_ALL);
			return (List<OTGroupStatus>)query.getResultList();
		} catch (IllegalStateException ise) {
			ise.printStackTrace();
			throw new AlohaServerException(ise);		
		} catch (Exception e) {
			e.printStackTrace();
			throw new AlohaServerException(e);		
		}		
	}		
	
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	@SuppressWarnings("unchecked")
	public List<OTGroupStatusTrans> retrieveAllOTGroupStatusTrans() throws AlohaServerException {
		try {
			Query query = this.entityManager.createNamedQuery(OTGroupStatusTrans.QueryNames.RETRIEVE_ALL);
			return (List<OTGroupStatusTrans>)query.getResultList();
		} catch (IllegalStateException ise) {
			ise.printStackTrace();
			throw new AlohaServerException(ise);		
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
	
	@Override
	public OTGroup retrieveOTGroupForSubmitter(AlohaUser otGroupSubmitter,
			OTPayPeriod otPayPeriod, OTGroupStatus otGroupStatus) throws AlohaServerException {
		try {
			Query query = this.entityManager.createNamedQuery(OTGroup.QueryNames.RETRIEVE_OT_GROUP_FOR_SUBMITTER);
			query.setParameter(OTGroup.QueryParamNames.OT_GROUP_SUBMITTER_USER_ID, Long.valueOf(otGroupSubmitter.getUserId()));
			query.setParameter(OTGroup.QueryParamNames.OT_PAY_PERIOD_KEY, Long.valueOf(otPayPeriod.getKey()));
			query.setParameter(OTGroup.QueryParamNames.OT_GROUP_STATUS_ID, Long.valueOf(otGroupStatus.getId()));
			return (OTGroup)query.getSingleResult();
		} catch (NoResultException nre) {
			return null; // no data found
		} catch (NonUniqueResultException nure) {
			nure.printStackTrace();
			throw new AlohaServerException(nure);
		} catch (IllegalStateException ise) {
			ise.printStackTrace();
			throw new AlohaServerException(ise);
		} catch (Exception e) {
			e.printStackTrace();
			throw new AlohaServerException(e);
		}	
	}
	
//	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
//	@SuppressWarnings("unchecked")
//	public List<OTDetail> retrieveSubmitterList(long submitterUserId) throws AlohaServerException {
//		try {
//			Query query = this.entityManager.createNamedQuery(OTGroup.QueryNames.RETRIEVE_OT_GROUP_FOR_SUBMITTER);
//			query.setParameter(OTDetail.QueryParamNames.SUBMITTER_USER_ID, submitterUserId);
//			return (List<OTDetail>)query.getResultList();
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
	public LeaveStatus getLeaveStatus(String code) throws AlohaServerException {
		try {
			Query query = this.entityManager.createNamedQuery(LeaveStatus.QueryNames.FIND_BY_CODE);
			query.setParameter(LeaveStatus.QueryParamNames.FIND_BY_CODE, code);
			return (LeaveStatus)query.getSingleResult();
		} catch (Exception e) {
			e.printStackTrace();
			throw new AlohaServerException(e);
		}		
	}
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	@SuppressWarnings("unchecked")
	public List<OTGroup> retrieveOTGroupsForSubmitterOrReceiver(long mgrUserId) throws AlohaServerException {
		try {
			Query query = this.entityManager.createNamedQuery(OTGroup.QueryNames.RETRIEVE_OT_GROUPS_FOR_SUBMITTER_OR_RECEIVER);
			query.setParameter(OTGroup.QueryParamNames.OT_GROUP_SUBMITTER_USER_ID, mgrUserId);
			query.setParameter(OTGroup.QueryParamNames.OT_GROUP_RECEIVER_USER_ID, mgrUserId);
			return (List<OTGroup>)query.getResultList();
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
	public List<OTGroup> retrieveAllOTGroupsForSubmitter(long submitterUserId) throws AlohaServerException {
		try {
			Query query = this.entityManager.createNamedQuery(OTGroup.QueryNames.RETRIEVE_ALL_OT_GROUP_FOR_SUBMITTER);
			query.setParameter(OTGroup.QueryParamNames.OT_GROUP_SUBMITTER_USER_ID, submitterUserId);
			return (List<OTGroup>)query.getResultList();
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
	public List<OTGroup> retrieveAllOTGroupsForReceiver(long receiverUserId) throws AlohaServerException {
		try {
			Query query = this.entityManager.createNamedQuery(OTGroup.QueryNames.RETRIEVE_ALL_OT_GROUP_FOR_RECEIVER);
			query.setParameter(OTGroup.QueryParamNames.OT_GROUP_RECEIVER_USER_ID, receiverUserId);
			return (List<OTGroup>)query.getResultList();
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
	public List<OTGroup> retrieveOTGroupsForManager(long otMgrUserId) throws AlohaServerException {
		try {
			Query query = this.entityManager.createNamedQuery(OTGroup.QueryNames.RETRIEVE_OT_GROUPS_FOR_MGR);
			query.setParameter(OTGroup.QueryParamNames.OT_MGR_USER_ID, otMgrUserId);
			return (List<OTGroup>)query.getResultList();
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
}