package gov.gsa.ocfo.aloha.ejb.leave.recon.impl;

import java.util.List;

import gov.gsa.ocfo.aloha.ejb.leave.LeaveRequestEJB;
import gov.gsa.ocfo.aloha.ejb.leave.LeaveStatusEJB;
import gov.gsa.ocfo.aloha.ejb.leave.LeaveTypeEJB;
import gov.gsa.ocfo.aloha.ejb.leave.recon.LeaveReconRetrieveEJB;
import gov.gsa.ocfo.aloha.exception.AlohaServerException;
import gov.gsa.ocfo.aloha.exception.LeaveReconException;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveDetail;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveStatus;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveStatusTransition;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveType;
import gov.gsa.ocfo.aloha.model.entity.leave.recon.LeaveReconWizard;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

@Stateless
public class LeaveReconRetrieveEJBImpl implements LeaveReconRetrieveEJB {
	
	@PersistenceContext (unitName="aloha-pu")
	private EntityManager entityManager;
	
	@EJB
	LeaveRequestEJB leaveRequestEJB;
	
	@EJB
	LeaveStatusEJB leaveStatusEJB;
	
	@EJB
	LeaveTypeEJB leaveTypeEJB;
	
	@SuppressWarnings("unchecked")
	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	public List<LeaveReconWizard> retrieveLeaveReconWizard(long employeeUserId) throws LeaveReconException {
		try {
			Query query = this.entityManager.createNamedQuery(LeaveReconWizard.QueryNames.RETRIEVE_BY_EMPLOYEE_USER_ID);
			query.setParameter(LeaveReconWizard.QueryParamNames.RETRIEVE_BY_EMPLOYEE_USER_ID, employeeUserId);
			return (List<LeaveReconWizard>)query.getResultList();
		} catch (Exception e) {
			e.printStackTrace();
			StringBuilder errMsg = new StringBuilder();
			errMsg.append("Exception encountered while attempting to retrieve LeaveReconWizard for Employee with USER_ID: '");
			errMsg.append(employeeUserId);
			errMsg.append("'");
			throw new LeaveReconException(errMsg.toString());		}	
	}

	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	public LeaveDetail retrieveLeaveDetail(long leaveDetailId) throws LeaveReconException {
		try {
			LeaveDetail leaveDetail = this.leaveRequestEJB.getLeaveDetail(leaveDetailId);
			if ( leaveDetail == null ) {
				StringBuilder errMsg = new StringBuilder();
				errMsg.append("LeaveDetail NOT FOUND for leaveDetailId: '");
				errMsg.append(leaveDetailId);
				errMsg.append("'");
				errMsg.append("'\nCANNOT CONTINUE");
				throw new LeaveReconException(errMsg.toString());
			}
			return leaveDetail;
		} catch (AlohaServerException ase) {
			StringBuilder errMsg = new StringBuilder();
			errMsg.append("Exception encountered when calling 'LeaveRequestEJB.getLeaveDetail(long leaveDetailId)'");
			errMsg.append("with the following Leave Detail ID parammater: '");
			errMsg.append(leaveDetailId);
			errMsg.append("'\nCANNOT CONTINUE");
			throw new LeaveReconException(errMsg.toString());
		}
	}
	
	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	public LeaveStatus retrieveLeaveStatus(String leaveStatusCode) throws LeaveReconException {
		try {
			LeaveStatus leaveStatus = this.leaveStatusEJB.getLeaveStatus(leaveStatusCode);
			if ( leaveStatus == null ) {
				StringBuilder errMsg = new StringBuilder();
				errMsg.append("LeaveStatus NOT FOUND for leaveStatusCode: '");
				errMsg.append(leaveStatusCode);
				errMsg.append("'");
				errMsg.append("'\nCANNOT CONTINUE");
				throw new LeaveReconException(errMsg.toString());
			}
			return leaveStatus;
		} catch (AlohaServerException ase) {
			StringBuilder errMsg = new StringBuilder();
			errMsg.append("Exception encountered when calling 'LeaveStatusEJB.getLeaveStatus(leaveStatusCode)'");
			errMsg.append("with the following Leave Status Code parammater: '");
			errMsg.append(leaveStatusCode);
			errMsg.append("'\nCANNOT CONTINUE");
			throw new LeaveReconException(errMsg.toString());
		}
	}
	
	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	public LeaveStatusTransition retrieveLeaveStatusTransition(String leaveStatusTransitionCode) throws LeaveReconException {
		try {
			LeaveStatusTransition leaveStatusTransition = this.leaveStatusEJB.getLeaveStatusTransition(leaveStatusTransitionCode);
			if ( leaveStatusTransition == null ) {
				StringBuilder errMsg = new StringBuilder();
				errMsg.append("LeaveStatusTransition NOT FOUND for leaveStatusTransitionCode: '");
				errMsg.append(leaveStatusTransitionCode);
				errMsg.append("'");
				errMsg.append("'\nCANNOT CONTINUE");
				throw new LeaveReconException(errMsg.toString());
			}			
			return leaveStatusTransition;
		} catch (AlohaServerException ase) {
			StringBuilder errMsg = new StringBuilder();
			errMsg.append("Exception encountered when calling 'LeaveStatusEJB.getLeaveStatus(leaveStatusCode)'");
			errMsg.append("with the following Leave Status Code parammater: '");
			errMsg.append(leaveStatusTransitionCode);
			errMsg.append("'\nCANNOT CONTINUE");
			throw new LeaveReconException(errMsg.toString());
		}
	}
	
	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	public LeaveType retrieveLeaveType(long leaveTypeId) throws LeaveReconException {
		try {
			LeaveType leaveType = this.leaveTypeEJB.getLeaveType(leaveTypeId);
			if ( leaveType == null ) {
				StringBuilder errMsg = new StringBuilder();
				errMsg.append("LeaveType NOT FOUND for leaveTypeId: '");
				errMsg.append(leaveTypeId);
				errMsg.append("'");
				errMsg.append("'\nCANNOT CONTINUE");
				throw new LeaveReconException(errMsg.toString());
			}			
			return leaveType;
		} catch (AlohaServerException ase) {
			StringBuilder errMsg = new StringBuilder();
			errMsg.append("Exception encountered when calling 'LeaveTypeEJB.getLeaveType(leaveTypeId)'");
			errMsg.append("with the following Leave Type ID parammater: '");
			errMsg.append(leaveTypeId);
			errMsg.append("'\nCANNOT CONTINUE");
			throw new LeaveReconException(errMsg.toString());
		}
	}	
}
