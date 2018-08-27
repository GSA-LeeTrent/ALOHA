package gov.gsa.ocfo.aloha.ejb.leave.impl;

import gov.gsa.ocfo.aloha.ejb.leave.LeaveStatusEJB;
import gov.gsa.ocfo.aloha.exception.AlohaServerException;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveStatus;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveStatusTransition;

import java.util.List;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

@Stateless
public class LeaveStatusEJBImpl implements LeaveStatusEJB {
	@PersistenceContext (unitName="aloha-pu")
	private EntityManager entityManager;
	
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
	public List<LeaveStatus> getAllLeaveStatuses() throws AlohaServerException {
		try {
			Query query = this.entityManager.createNamedQuery(LeaveStatus.QueryNames.RETRIEVE_ALL);
			return (List<LeaveStatus>)query.getResultList();
		} catch (Exception e) {
			e.printStackTrace();
			throw new AlohaServerException(e);		
		}		
	}
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public LeaveStatusTransition getLeaveStatusTransition(String actionCode) throws AlohaServerException {
		try {
			Query query = this.entityManager.createNamedQuery(LeaveStatusTransition.QueryNames.FIND_BY_ACTION_CODE);
			query.setParameter(LeaveStatusTransition.QueryParamNames.FIND_BY_ACTION_CODE, actionCode);
			return (LeaveStatusTransition)query.getSingleResult();
		} catch (Exception e) {
			e.printStackTrace();
			throw new AlohaServerException(e);		
		}		
	}

	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	@SuppressWarnings("unchecked")
	public List<LeaveStatusTransition> getAllLeaveStatusTransitions() throws AlohaServerException {
		try {
			Query query = this.entityManager.createNamedQuery(LeaveStatusTransition.QueryNames.RETRIEVE_ALL);
			return (List<LeaveStatusTransition>)query.getResultList();
		} catch (Exception e) {
			e.printStackTrace();
			throw new AlohaServerException(e);		
		}		
	}
}