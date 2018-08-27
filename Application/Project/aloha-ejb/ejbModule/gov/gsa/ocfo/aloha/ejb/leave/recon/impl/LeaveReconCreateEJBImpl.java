package gov.gsa.ocfo.aloha.ejb.leave.recon.impl;

import gov.gsa.ocfo.aloha.ejb.leave.LeaveRequestEJB;
import gov.gsa.ocfo.aloha.ejb.leave.recon.LeaveReconCreateEJB;
import gov.gsa.ocfo.aloha.ejb.leave.recon.LeaveReconRetrieveEJB;
import gov.gsa.ocfo.aloha.exception.AlohaServerException;
import gov.gsa.ocfo.aloha.exception.LeaveReconException;
import gov.gsa.ocfo.aloha.model.entity.AlohaUser;
import gov.gsa.ocfo.aloha.model.entity.AuditTrail;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveDetail;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveHeader;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveHistory;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveItem;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveStatus;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveStatusTransition;
import gov.gsa.ocfo.aloha.model.entity.leave.recon.LeaveReconWizard;
import gov.gsa.ocfo.aloha.util.StringUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

@Stateless
public class LeaveReconCreateEJBImpl implements LeaveReconCreateEJB {
	
	@EJB
	LeaveReconRetrieveEJB retrieveEJB;
	
	@EJB
	LeaveRequestEJB leaveRequestEJB;

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void processNewLeaveRequest(AlohaUser employeeSubmitter, LeaveReconWizard leaveReconWizard) throws LeaveReconException {
		
		//System.out.println("BEGIN: LeaveReconCreateEJB.processNewLeaveRequest()");
		
		try {
			LeaveHeader leaveRequest = this.createNewLeaveRequest(employeeSubmitter, leaveReconWizard);
			
			//System.out.println("Calling LeaveRequestEJB.saveLeaveRequest(LeaveHeader) ...");
			this.leaveRequestEJB.saveLeaveRequest(leaveRequest);
			//System.out.println("... Returning from LeaveRequestEJB.saveLeaveRequest(LeaveHeader) ...");
			
		} catch (AlohaServerException e) {
			StringBuilder errMsg = new StringBuilder();
			errMsg.append("Exception encountered in ");
			errMsg.append(this.getClass().getName());
			errMsg.append(".processNewLeaveRequest(AlohaUser employeeSubmitter, AlohaUser supervisor, LeaveReconWizard leaveReconWizard)");
			errMsg.append("\nwhen attempting to save new leave request by calling 'LeaveRequestEJB.saveLeaveRequest(LeaveHeader leaveHeader)' ");
			throw new LeaveReconException(errMsg.toString());
		}
		
		//System.out.println("END: LeaveReconCreateEJB.processNewLeaveRequest()");
	}
	
	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	public LeaveHeader createNewLeaveRequest(AlohaUser employeeSubmitter, LeaveReconWizard leaveReconWizard) throws LeaveReconException {
		
		//System.out.println("BEGIN: LeaveReconCreateEJB.createNewLeaveRequest()");
		
		//////////////////////////////////////////////////////////////////////////////////////		
		// AUDIT TRAIL
		//////////////////////////////////////////////////////////////////////////////////////			
		AuditTrail auditTrail = new AuditTrail();
		auditTrail.setUserLastUpdated(employeeSubmitter.getLoginName());
		auditTrail.setDateLastUpdated(new Date());
		
		//////////////////////////////////////////////////////////////////////////////////////		
		// LEAVE HEADER
		//////////////////////////////////////////////////////////////////////////////////////		
		LeaveHeader leaveHeader = new LeaveHeader();
		leaveHeader.setAuditTrail(auditTrail);
		leaveHeader.setEmployee(employeeSubmitter);
		
		//////////////////////////////////////////////////////////////////////////////////////		
		// LEAVE DETAIL
		//////////////////////////////////////////////////////////////////////////////////////		
		LeaveDetail leaveDetail = new LeaveDetail();
		leaveDetail.setAuditTrail(auditTrail);
		leaveDetail.setSubmitter(employeeSubmitter);
		leaveDetail.setApprover(leaveReconWizard.getSupervisor());
		leaveDetail.setPayPeriodStartDate(leaveReconWizard.getPayPeriodStartDate());
		leaveDetail.setPayPeriodEndDate(leaveReconWizard.getPayPeriodEndDate());
		leaveDetail.setDisabledVeteranCertified(leaveReconWizard.isDisabledVetCertified());
		
		//////////////////////////////////////////////////////////////////////////////////////		
		// CONNECT LEAVE HEADER AND LEAVE DETAIL (bi-directional)
		//////////////////////////////////////////////////////////////////////////////////////		
		leaveHeader.addLeaveDetail(leaveDetail);
		leaveDetail.setLeaveHeader(leaveHeader);
		leaveDetail.setSequence(leaveHeader.getLeaveDetails().size());
		
		//////////////////////////////////////////////////////////////////////////////////////		
		// LEAVE STATUS
		//////////////////////////////////////////////////////////////////////////////////////		
		LeaveStatus leaveStatus = this.retrieveEJB.retrieveLeaveStatus(LeaveStatus.CodeValues.SUBMITTED);
		leaveDetail.setLeaveStatus(leaveStatus);

		//////////////////////////////////////////////////////////////////////////////////////		
		// LEAVE STATUS TRANSITION
		//////////////////////////////////////////////////////////////////////////////////////		
		LeaveStatusTransition leaveStatusTransition	= this.retrieveEJB.retrieveLeaveStatusTransition(LeaveStatusTransition.ActionCodeValues.NONE_TO_SUBMIT);

		//////////////////////////////////////////////////////////////////////////////////////		
		// LEAVE HISTORY (Includes Leave Status Transition and Remarks)
		//////////////////////////////////////////////////////////////////////////////////////		
		LeaveHistory leaveHistory = new LeaveHistory();
		leaveHistory.setAuditTrail(auditTrail);
		leaveHistory.setLeaveDetail(leaveDetail);
		leaveHistory.setLeaveStatusTransition(leaveStatusTransition);
		leaveHistory.setActionDatetime(new Date());
		leaveHistory.setActor(leaveDetail.getSubmitter());

		//////////////////////////////////////////////////////////////////////////////////////		
		// LEAVE HISTORY REMARKS
		//////////////////////////////////////////////////////////////////////////////////////		
		String remarks = leaveReconWizard.getEmployeeRemarks();
		if (  StringUtil.isNullOrEmpty(remarks) == false ) {
			if (remarks.length() > 4000 ) {
				leaveHistory.setRemarks(remarks.substring(0, 4000));
			} else {
				leaveHistory.setRemarks(remarks);
			}
		}		
		
		//////////////////////////////////////////////////////////////////////////////////////
		// ADD LEAVE HISTORY TO LEAVE DETAIL
		//////////////////////////////////////////////////////////////////////////////////////		
		leaveDetail.addLeaveHistory(leaveHistory);			
		
		//////////////////////////////////////////////////////////////////////////////////////		
		// LEAVE ITEMS
		//////////////////////////////////////////////////////////////////////////////////////		
//		List<LeaveItem> leaveItemList = new ArrayList<LeaveItem>();
//		for ( LeaveReconWizardItem wizardItem : leaveReconWizard.getLeaveReconWizardItems() ) {
//			
//			if ( wizardItem.getCorrectLeaveHours().compareTo(BigDecimal.ZERO) == 1  ) {
//
//				LeaveItem leaveItem = new LeaveItem();
//				
//				leaveItem.setLeaveDetail(leaveDetail);
//				leaveItem.setAuditTrail(auditTrail);
//				leaveItem.setLeaveDate(wizardItem.getLeaveDate());
//				leaveItem.setLeaveType(wizardItem.getLeaveTypeObj());
//				leaveItem.setLeaveHours(wizardItem.getCorrectLeaveHours());
//				leaveItem.setStartTime(null); // WIZARD IS NOT HANDLING START TIME
//				
//				leaveItemList.add(leaveItem);
//			}
//		}
		
		List<LeaveItem> leaveItemList = new ArrayList<LeaveItem>();
		Map<String, LeaveItem> leaveItemMap = leaveReconWizard.buildLeaveItemMap(leaveDetail);
		System.out.println(" leaveItemMap.size(): " + leaveItemMap.size());
		
		for ( Map.Entry<String, LeaveItem> mapEntry : leaveItemMap.entrySet() ) {
			
			LeaveItem leaveItem = mapEntry.getValue();
			
			/////////////////////////////////////////////////////////////////////
			// LEAVE ITEM MAP CAN CONTAIN LEAVE ITEMS THAT HAVE A VALUE OF ZERO 
			// FOR LEAVE HOURS. WE DON'T WANT TO ADD THESE LEAVE ITEMS TO THE 
			// NEWLY CREATED LEAVE REQUEST AMENDMENT
			/////////////////////////////////////////////////////////////////////
			if ( leaveItem.getLeaveHours().compareTo(BigDecimal.ZERO) == 1 ) {
				leaveItemList.add(mapEntry.getValue());	
			}
		}		
		
		System.out.println(" leaveItemList.size(): " + leaveItemList.size());
		
		if ( leaveItemList.isEmpty()) {
			StringBuilder errMsg = new StringBuilder();
			errMsg.append("INVALID STATE: List<LeaveItem> IS EMPTY in ");
			errMsg.append(this.getClass().getName());
			errMsg.append(".createNewLeaveRequest()");
			errMsg.append(" Cannot create ALOHA Leave Request for: Employee ['");
			errMsg.append(leaveReconWizard.getEmployeeUserId());
			errMsg.append("'], Leave Year ['");
			errMsg.append(leaveReconWizard.getLeaveYear());
			errMsg.append("'], Pay Period ['");
			errMsg.append(leaveReconWizard);
			errMsg.append("']");
			throw new LeaveReconException(errMsg.toString());
		} else {
			leaveDetail.setLeaveItems(leaveItemList);	
		}
		
		//System.out.println("END: LeaveReconCreateEJB.createNewLeaveRequest()");
		
		return leaveHeader;
	}
}