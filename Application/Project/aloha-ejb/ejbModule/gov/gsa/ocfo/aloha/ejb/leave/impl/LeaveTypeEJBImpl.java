package gov.gsa.ocfo.aloha.ejb.leave.impl;

import gov.gsa.ocfo.aloha.ejb.leave.LeaveTypeEJB;
import gov.gsa.ocfo.aloha.exception.AlohaServerException;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveType;

import java.util.Date;
import java.util.List;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

@Stateless
public class LeaveTypeEJBImpl implements LeaveTypeEJB {
	@PersistenceContext (unitName="aloha-pu")
	private EntityManager entityManager;

	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public LeaveType getLeaveType(long leaveTypeId) throws AlohaServerException {
		try {
			return this.entityManager.find(LeaveType.class, leaveTypeId);
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
	public List<LeaveType> getLeaveTypes(String primaryCode) throws AlohaServerException {
		try {
			Query query = this.entityManager.createNamedQuery(LeaveType.QueryNames.FIND_BY_PRIMARY_CODE);
			query.setParameter(LeaveType.QueryParamNames.FIND_BY_PRIMARY_CODE, primaryCode);
			return (List<LeaveType>)query.getResultList();
		} catch (Exception e) {
			e.printStackTrace();
			throw new AlohaServerException("Exception encountered in " + this.getClass().getName()	+ ".getLeaveTypes(String primaryCode): " + e.getMessage());
		}		
	}	

	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public LeaveType getLeaveType(String primaryCode, String secondaryCode) throws AlohaServerException {
		try {
			Query query = this.entityManager.createNamedQuery(LeaveType.QueryNames.FIND_BY_PRIMARY_AND_SECONDARY_CODE);
			query.setParameter(LeaveType.QueryParamNames.FIND_BY_PRIMARY_CODE, primaryCode);
			query.setParameter(LeaveType.QueryParamNames.FIND_BY_SECONDARY_CODE, secondaryCode);			
			return (LeaveType)query.getSingleResult();
		} catch (Exception e) {
			e.printStackTrace();
			throw new AlohaServerException("Exception encountered in " + this.getClass().getName() 
										+ "getLeaveType(String primaryCode, String secondaryCode): " + e.getMessage());
		}		
	}

	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	@SuppressWarnings("unchecked")
	public List<LeaveType> getAllLeaveTypes() throws AlohaServerException {
		try {
			Query query = this.entityManager.createNamedQuery(LeaveType.QueryNames.RETRIEVE_ALL);
			return (List<LeaveType>)query.getResultList();
		} catch (Exception e) {
			e.printStackTrace();
			throw new AlohaServerException("Exception encountered in " + this.getClass().getName()	+ ".getAllLeaveTypes(): " + e.getMessage());
		}		
	}

	//2012-10-02 JJM 48969: Add list of Effective LeaveTypes for Employee
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	@SuppressWarnings("unchecked")
	public List<LeaveType> getAllLeaveTypesEff(Date ppEffDate) throws AlohaServerException {
		try {
			Query query = this.entityManager.createNamedQuery(LeaveType.QueryNames.RETRIEVE_ALL_EFF);
			//2012-10-02 JJM 48969: Added parm for eff date
			query.setParameter(LeaveType.QueryParamNames.RETRIEVE_ALL_EFF, ppEffDate);
			List<LeaveType> lt=query.getResultList();
			return (lt);
		} catch (Exception e) {
			e.printStackTrace();
			throw new AlohaServerException("Exception encountered in " + this.getClass().getName()	+ ".getAllLeaveTypesEff(): " + e.getMessage());
		}		
	}

	//2012-10-02 JJM 48969: Add list of Effective LeaveTypes for Timekeepers / Supervisors
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	@SuppressWarnings("unchecked")
	public List<LeaveType> getAllLeaveTypesEffOBO(Date ppEffDate) throws AlohaServerException {
		try {
			Query query = this.entityManager.createNamedQuery(LeaveType.QueryNames.RETRIEVE_ALL_EFF_OBO);
			//2012-10-02 JJM 48969: Added parm for eff date
			query.setParameter(LeaveType.QueryParamNames.RETRIEVE_ALL_EFF_OBO, ppEffDate);
			List<LeaveType> lt=query.getResultList();
			return (lt);
		} catch (Exception e) {
			e.printStackTrace();
			throw new AlohaServerException("Exception encountered in " + this.getClass().getName()	+ ".getAllLeaveTypesEffOBO(): " + e.getMessage());
		}		
	}


}
