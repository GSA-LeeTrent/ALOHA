package gov.gsa.ocfo.aloha.ejb.leave.recon.impl;

import gov.gsa.ocfo.aloha.ejb.leave.LeaveRequestEJB;
import gov.gsa.ocfo.aloha.ejb.leave.recon.LeaveReconCreateEJB;
import gov.gsa.ocfo.aloha.ejb.leave.recon.LeaveReconRetrieveEJB;
import gov.gsa.ocfo.aloha.ejb.leave.recon.LeaveReconWithdrawCreateEJB;
import gov.gsa.ocfo.aloha.exception.AlohaServerException;
import gov.gsa.ocfo.aloha.exception.LeaveReconException;
import gov.gsa.ocfo.aloha.model.entity.AlohaUser;
import gov.gsa.ocfo.aloha.model.entity.AuditTrail;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveDetail;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveHeader;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveHistory;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveStatus;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveStatusTransition;
import gov.gsa.ocfo.aloha.model.entity.leave.recon.LeaveReconWizard;
import gov.gsa.ocfo.aloha.model.entity.leave.recon.LeaveReconWizardXref;
import gov.gsa.ocfo.aloha.util.StringUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

@Stateless
public class LeaveReconWithdrawCreateEJBImpl implements LeaveReconWithdrawCreateEJB {

	@EJB
	LeaveReconRetrieveEJB retrieveEJB;
	
	@EJB
	LeaveRequestEJB leaveRequestEJB;
	
	@EJB
	LeaveReconCreateEJB createEJB;
	
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void processWithdrawCreateLeaveRequests(AlohaUser employeeSubmitter, LeaveReconWizard leaveReconWizard)
			throws LeaveReconException {
		
		//System.out.println("BEGIN: LeaveReconWithdrawCreateEJB.processWithdrawCreateLeaveRequests()");
		
		try {
			
			//////////////////////////////////////////////////////////////////////////////////////			
			// 1) RETRIEVE ALL PREVIOUSLY APPROVED LEAVE REQUESTS FOR THIS EMPLOYEE AND PAY PERIOD
			// 2) CHANGE THE STATUS OF EACH RETRIEVED LEAVE REQUEST TO 'PEND WITHDRAW'
			// 3) ADD THEM TO THE 'withdrawList'
			//////////////////////////////////////////////////////////////////////////////////////
			List<LeaveDetail> withdrawList = this.createLeaveWithdrawals(employeeSubmitter, leaveReconWizard);
			//System.out.println("withdrawList.size(): " + withdrawList.size());
			
			//////////////////////////////////////////////////////////////////////////////////////
			// PROCESS WITHDRAWALS FIRST
			//////////////////////////////////////////////////////////////////////////////////////			
			for (LeaveDetail withdrawLeaveDetail : withdrawList ) {
				//System.out.println("Header ID: " + withdrawLeaveDetail.getLeaveHeaderId() + " / Detail ID: " + withdrawLeaveDetail.getId() );
				//System.out.println("Calling LeaveRequestEJB.updateLeaveDetail(LeaveDetail) ...");
				this.leaveRequestEJB.updateLeaveDetail(withdrawLeaveDetail);
				//System.out.println("... returning from LeaveRequestEJB.updateLeaveDetail(LeaveDetail) ...");
			} // END WITHDRAW LOOP	
			
			//////////////////////////////////////////////////////////////////////////////////////			
			// NOW CREATE AND PERSIST A NEW LEAVE REQUEST
			//////////////////////////////////////////////////////////////////////////////////////
			LeaveHeader newLeaveRequst = this.createEJB.createNewLeaveRequest(employeeSubmitter, leaveReconWizard);
			//System.out.println("Calling LeaveRequestEJB.saveLeaveRequest(LeaveHeader) ...");
			this.leaveRequestEJB.saveLeaveRequest(newLeaveRequst);
			//System.out.println("... returning from LeaveRequestEJB.saveLeaveRequest(LeaveHeader) ...");
		} catch (AlohaServerException ase) {
			StringBuilder errMsg = new StringBuilder();
			errMsg.append("Exception encountered in ");
			errMsg.append(this.getClass().getName());
			errMsg.append(".processWithdrawCreateLeaveRequests(AlohaUser employeeSubmitter, LeaveReconWizard leaveReconWizard)");
			errMsg.append("\nwhen attempting to process leave withdrawals / new leave request.");
			errMsg.append("\nTRANSACTIN ROLLED BACK");
			throw new LeaveReconException(errMsg.toString());
		}
		
		//System.out.println("END: LeaveReconWithdrawCreateEJB.processWithdrawCreateLeaveRequests()");
	}

	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	public List<LeaveDetail> createLeaveWithdrawals(AlohaUser employeeSubmitter, LeaveReconWizard leaveReconWizard) throws LeaveReconException {
	
		//System.out.println("BEGIN: LeaveReconWithdrawCreateEJB.createLeaveWithdrawals()");
		
		List<LeaveDetail> withdrawList = new ArrayList<LeaveDetail>();
		
		for ( LeaveReconWizardXref xrefItem: leaveReconWizard.getLeaveReconWizardXrefItems()) {
				
			//////////////////////////////////////////////////////////////////////////////////////		
			// LEAVE DETAIL
			//////////////////////////////////////////////////////////////////////////////////////				
			LeaveDetail leaveDetail = this.retrieveEJB.retrieveLeaveDetail(xrefItem.getLeaveDetailId());
			leaveDetail.setApprover(leaveReconWizard.getSupervisor());
			
			//////////////////////////////////////////////////////////////////////////////////////		
			// AUDIT TRAIL
			//////////////////////////////////////////////////////////////////////////////////////					
			AuditTrail auditTrail = new AuditTrail();
			auditTrail.setUserLastUpdated(employeeSubmitter.getLoginName());
			auditTrail.setDateLastUpdated(new Date());
			leaveDetail.setAuditTrail(auditTrail);
			
			//////////////////////////////////////////////////////////////////////////////////////		
			// LEAVE STATUS
			//////////////////////////////////////////////////////////////////////////////////////				
			LeaveStatus leaveStatus = this.retrieveEJB.retrieveLeaveStatus(LeaveStatus.CodeValues.PEND_WITHDRAW);
			leaveDetail.setLeaveStatus(leaveStatus);
			
			//////////////////////////////////////////////////////////////////////////////////////		
			// LEAVE STATUS TRANSITION
			//////////////////////////////////////////////////////////////////////////////////////	
			LeaveStatusTransition leaveStatusTransition	= this.retrieveEJB.retrieveLeaveStatusTransition
															(LeaveStatusTransition.ActionCodeValues.APPROVED_TO_PEND_WITHDRAW);			
			
			///////////////////////////////////////////////////////////////
			// LEAVE HISTORY (INCLUDES REMARKS AND LEAVE STATUS TRANSITION)
			///////////////////////////////////////////////////////////////
			LeaveHistory leaveHistory = new LeaveHistory();
			leaveHistory.setAuditTrail(auditTrail);
			leaveHistory.setLeaveDetail(leaveDetail);
			leaveHistory.setLeaveStatusTransition(leaveStatusTransition);
			leaveHistory.setActionDatetime(new Date());
			leaveHistory.setActor(employeeSubmitter);
			
			///////////////////////////////////////////////////////////////			
			// LEAVE HISTORY REMARKS
			///////////////////////////////////////////////////////////////			
			String remarks = leaveReconWizard.getEmployeeRemarks();
			if (  StringUtil.isNullOrEmpty(remarks) == false ) {
				if (remarks.length() > 4000 ) {
					leaveHistory.setRemarks(remarks.substring(0, 4000));
				} else {
					leaveHistory.setRemarks(remarks);
				}
			}
			leaveDetail.addLeaveHistory(leaveHistory);

			///////////////////////////////////////////////////////////////////////////////			
			// ADD FULLY LOADED LEAVE DETAIL WITH 'PEND_WITHDRAW' STATUS TO 'WITHDRAW LIST'
			//////////////////////////////////////////////////////////////////////////////			
			withdrawList.add(leaveDetail);
		}
		
		//////////////////////////////////////////////////////////////////////////////////////
		// RETURN FULLY LOADED WITHDRAW LIST
		//////////////////////////////////////////////////////////////////////////////////////
		//System.out.println("END: LeaveReconWithdrawCreateEJB.createLeaveWithdrawals()");
		return withdrawList;
	}
}
