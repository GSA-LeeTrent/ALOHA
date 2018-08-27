package gov.gsa.ocfo.aloha.ejb.leave.recon.impl;

import gov.gsa.ocfo.aloha.ejb.leave.LeaveRequestEJB;
import gov.gsa.ocfo.aloha.ejb.leave.recon.LeaveReconAmendEJB;
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
public class LeaveReconAmendEJBImpl implements LeaveReconAmendEJB {
	
	@EJB
	LeaveReconRetrieveEJB retrieveEJB;
	
	@EJB
	LeaveRequestEJB leaveRequestEJB;

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void processLeaveRequestAmendment(AlohaUser employeeSubmitter, LeaveReconWizard leaveReconWizard) 
	throws LeaveReconException {
		
		//System.out.println("BEGIN: LeaveReconAmendEJB.processLeaveRequestAmendment");
		
		// RETRIEVE PREVIOUSLY APPROVED LEAVE DETAIL
		long previouslyApprovedLeaveDetailId = leaveReconWizard.getLeaveReconWizardXrefItems().get(0).getLeaveDetailId();
		//System.out.println("previouslyApprovedLeaveDetailId: " + previouslyApprovedLeaveDetailId);
		
		LeaveDetail previouslyApprovedLeaveDetail = this.retrieveEJB.retrieveLeaveDetail(previouslyApprovedLeaveDetailId);
		//System.out.println("previouslyApprovedLeaveDetail: " + previouslyApprovedLeaveDetail);
		//System.out.println("previouslyApprovedLeaveDetail.isApproved(): " + previouslyApprovedLeaveDetail.isApproved());
		
		// MAKE SURE THAT RETRIEVED LEAVE DETAIL IS IN FACT "APPROVED"
		if ( previouslyApprovedLeaveDetail.isApproved() == false) {
			
			StringBuilder errMsg = new StringBuilder();
			
			errMsg.append("Leave Request identified by leaveDetailID '");
			errMsg.append(previouslyApprovedLeaveDetailId);
			errMsg.append("' is NOT in an APPROVED status. ");
			errMsg.append("\nActual Leave Status for Leave Request identified by leaveDetailID '");
			errMsg.append(previouslyApprovedLeaveDetailId);
			errMsg.append("' is '");  
			errMsg.append(previouslyApprovedLeaveDetail.getLeaveStatus().getLabel());
			errMsg.append("'");
			errMsg.append("\nCANNOT CONTINUE");
			
			throw new LeaveReconException(errMsg.toString());
		}
		
		// CREATE A PENDING AMENDMENT 
		LeaveDetail newlyCreatedPendAmendLeaveDetail = this.createLeaveDetailForPendAmend(employeeSubmitter, leaveReconWizard);
		
		// MERGE THE CORRECTED ITEMS INTO THE LEAVE ITEMS FOR THIS NEWLY CREATED PEND AMEND LEAVE DETAIL
		//this.mergeCollections(newlyCreatedPendAmendLeaveDetail.getLeaveItems(), leaveReconWizard.getLeaveReconWizardItems());
		
		// GET THE LEAVE HEADER FOR THIS LEAVE REQUEST
		LeaveHeader leaveHeaderForOriginalRequest = previouslyApprovedLeaveDetail.getLeaveHeader();
		//System.out.println("leaveHeaderForOriginalRequest.getId(): " + leaveHeaderForOriginalRequest.getId());
		//System.out.println("leaveHeaderForOriginalRequest.getLeaveDetails().size(): " + leaveHeaderForOriginalRequest.getLeaveDetails().size());
		
		// ADD THE NEWLY CREATED PENDING AMENDMENT LEAVE DETAIL TO THE ORGINAL LEAVE HEADER
		leaveHeaderForOriginalRequest.addLeaveDetail(newlyCreatedPendAmendLeaveDetail);
		
		// SET THE LEAVE HEADER PROPOERTY ON THE NEWLY CREATED PENDING AMENDMENT 
		// LEAVE DETAIL WITH A REFERENCE TO THE ORIGINAL LEAVE REQUEST HEADER (FOREIGN KEY)
		newlyCreatedPendAmendLeaveDetail.setLeaveHeader(leaveHeaderForOriginalRequest);
		
		// ASSIGN A 'SEQUENCE' TO THE NEWLY LEAVE DETAIL. 
		// THIS VALUE SERVES AS A 'SEQUENCE OF EVENTS'.
		newlyCreatedPendAmendLeaveDetail.setSequence(leaveHeaderForOriginalRequest.getLeaveDetails().size());
		//System.out.println("leaveHeaderForOriginalRequest.getLeaveDetails().size(): " + leaveHeaderForOriginalRequest.getLeaveDetails().size());
		
		try {
			
			//System.out.println("Calling LeaveRequestEJB.updateLeaveHeader(LeaveHeader) ...");
			this.leaveRequestEJB.updateLeaveHeader(leaveHeaderForOriginalRequest);
			//System.out.println("... Returning from LeaveRequestEJB.updateLeaveHeader(LeaveHeader) ...");
		} catch (AlohaServerException e) {
			StringBuilder errMsg = new StringBuilder();
			errMsg.append("Exception encountered in ");
			errMsg.append(this.getClass().getName());
			errMsg.append(".processLeaveRequestAmendment(AlohaUser employeeSubmitter, AlohaUser supervisor, LeaveReconWizard leaveReconWizard)");
			errMsg.append("\nwhen attempting to update leave header by calling 'LeaveRequestEJB.updateLeaveHeader(LeaveHeader leaveHeader)' ");
			throw new LeaveReconException(errMsg.toString());
		}
		//System.out.println("END: LeaveReconAmendEJB.processLeaveRequestAmendment");
	}

	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	private LeaveDetail createLeaveDetailForPendAmend(AlohaUser employeeSubmitter, LeaveReconWizard leaveReconWizard) throws LeaveReconException {
		
		//System.out.println("BEGIN: LeaveReconAmendEJB.createLeaveDetailForPendAmend()");
		
		//////////////////////////////////////////////////////////////////////////////////////		
		// AUDIT TRAIL
		//////////////////////////////////////////////////////////////////////////////////////		
		AuditTrail auditTrail = new AuditTrail();
		auditTrail.setUserLastUpdated(employeeSubmitter.getLoginName());
		auditTrail.setDateLastUpdated(new Date());
		
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
		// LEAVE STATUS
		//////////////////////////////////////////////////////////////////////////////////////		
		LeaveStatus leaveStatus = this.retrieveEJB.retrieveLeaveStatus(LeaveStatus.CodeValues.PEND_AMEND);
		leaveDetail.setLeaveStatus(leaveStatus);

		//////////////////////////////////////////////////////////////////////////////////////		
		// LEAVE STATUS TRANSITION
		//////////////////////////////////////////////////////////////////////////////////////		
		LeaveStatusTransition leaveStatusTransition	= this.retrieveEJB.retrieveLeaveStatusTransition(LeaveStatusTransition.ActionCodeValues.APPROVED_TO_PEND_AMEND);

		//////////////////////////////////////////////////////////////////////////////////////		
		// LEAVE HISTORY (Includes Amendment Remarks)
		//////////////////////////////////////////////////////////////////////////////////////
		LeaveHistory leaveHistory = new LeaveHistory();
		leaveHistory.setAuditTrail(auditTrail);
		leaveHistory.setLeaveDetail(leaveDetail);
		leaveHistory.setLeaveStatusTransition(leaveStatusTransition);
		leaveHistory.setActionDatetime(new Date());
		leaveHistory.setActor(leaveDetail.getSubmitter());

		// LEAVE HISTORY >> REMARKS
		String amendmentRemarks = leaveReconWizard.getEmployeeRemarks();
		if (  StringUtil.isNullOrEmpty(amendmentRemarks) == false ) {
			if (amendmentRemarks.length() > 4000 ) {
				leaveHistory.setRemarks(amendmentRemarks.substring(0, 4000));
			} else {
				leaveHistory.setRemarks(amendmentRemarks);
			}
		}		
		leaveDetail.addLeaveHistory(leaveHistory);			
		
		//////////////////////////////////////////////////////////////////////////////////////
		// LEAVE ITEMS
		//////////////////////////////////////////////////////////////////////////////////////
		List<LeaveItem> leaveItemList = new ArrayList<LeaveItem>();
		Map<String, LeaveItem> leaveItemMap = leaveReconWizard.buildLeaveItemMap(leaveDetail);
		
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
		
		////////////////////////////////////////////////////////////////////////////////////////
		// SET LEAVE DETAIL WITH LEAVE ITEM LIST 
		//////////////////////////////////////////////////////////////////////////////////////		
		leaveDetail.setLeaveItems(leaveItemList);
		
		//////////////////////////////////////////////////////////////////////////////////////
		// RETURN FULLY LOADED LEAVE DETAIL
		//////////////////////////////////////////////////////////////////////////////////////
		//System.out.println("END: LeaveReconAmendEJB.createLeaveDetailForPendAmend()");
		return leaveDetail;
	}	

	/*
	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	private Map<String, LeaveItem> buildLeaveItemMap(LeaveReconWizard leaveReconWizard, LeaveDetail leaveDetail) throws LeaveReconException {
		
		//System.out.println("BEGIN: LeaveReconWizard.buildLeaveItemMap(LeaveDetail)");
		
		if ( leaveReconWizard.alohaItemsAreNullOrEmpty() ) {
			StringBuilder errMsg = new StringBuilder();
			errMsg.append("List<LeaveReconWizardAlohaItem> IS NULL OR EMPTY.");	
			errMsg.append(" Cannot create ALOHA Leave Item Map for: Employee ['");
			errMsg.append(leaveReconWizard.getEmployeeUserId());
			errMsg.append("'], Leave Year ['");
			errMsg.append(leaveReconWizard.getLeaveYear());
			errMsg.append("'], Pay Period ['");
			errMsg.append(leaveReconWizard.getPayPeriodNumber());
			errMsg.append("']");
			throw new LeaveReconException(errMsg.toString());
		}
		
		if ( leaveReconWizard.wizardItemsAreNullOrEmpty() ) {
			StringBuilder errMsg = new StringBuilder();
			errMsg.append("List<LeaveReconWizardItem> IS NULL OR EMPTY.");	
			errMsg.append(" Cannot create ALOHA Leave Item Map for: Employee ['");
			errMsg.append(leaveReconWizard.getEmployeeUserId());
			errMsg.append("'], Leave Year ['");
			errMsg.append(leaveReconWizard.getLeaveYear());
			errMsg.append("'], Pay Period ['");
		
			errMsg.append(leaveReconWizard.getPayPeriodNumber());
			errMsg.append("']");
			throw new LeaveReconException(errMsg.toString());
		}		
		
		//System.out.println("Wizard Items Count: " + this.getLeaveReconWizardItems().size());
		//System.out.println("Aloha Items Count: " + this.getLeaveReconWizardAlohaItems().size());
		
		Map<String, LeaveItem> leaveItemMap = new HashMap<String, LeaveItem>();

		//System.out.println("leaveItemMap.size(): " + leaveItemMap.size());

		// INITIALIZE LEAVE ITEM MAP WITH PREVIOUSLY APPROVED LEAVE ITEMS
		// FOR THIS EMPLOYEE AND PAY PERIOD
		for ( LeaveReconWizardAloha alohaItem : leaveReconWizard.getLeaveReconWizardAlohaItems()) {
			
			/////////////////////////////////////////////////////////
			// LEAVE HOURS IN ALOHA SHOULD BE GREATER THAN ZERO 
			// FOR PREVIOULSY APPROVED LEAVE REQUESTS BUT MAKE SURE 
			// ANYWAY
			/////////////////////////////////////////////////////////
			if ( alohaItem.getLeaveHours().compareTo(BigDecimal.ZERO) == 1 ) {	
				
				LeaveItem leaveItem = new LeaveItem();
				
				leaveItem.setLeaveDetail(leaveDetail);
				leaveItem.setAuditTrail(leaveDetail.getAuditTrail());
				leaveItem.setLeaveDate(alohaItem.getLeaveDate());
				leaveItem.setLeaveType(alohaItem.getLeaveType());
				leaveItem.setLeaveHours(alohaItem.getLeaveHours());
				leaveItem.setStartTime(null); // WIZARD IS NOT HANDLING START TIME
				
				leaveItemMap.put(alohaItem.getLeaveItemMapKey(), leaveItem);				
			}
		}

		//System.out.println("leaveItemMap.size()" + leaveItemMap.size());
		
		// NOW OVERLAY WHAT'S ALREADY IN THIS MAP WITH LEAVE ITEMS THAT
		// WERE CORRECTED BY THE WIZARD
		for ( LeaveReconWizardItem wizardItem : leaveReconWizard.getLeaveReconWizardItems() ) {
			
			////////////////////////////////////////////////////////////
			// ONLY OVERLAY THOSE LEAVE ITEMS THAT NEED TO BE CORRECTED			
			// IT'S OKAY TO ALLOW ZERO HOURS HERE BECAUSE THE CORRECT
			// LEAVE HOURS COULD IN FACT BE ZERO. THIS COULD HAPPEN
			// WHEN EMPLOYEE SELECTS "ETAMS IS CORRECT" AND ETAMS HOURS 
			// ARE ZERO. THIS MEANS THAT EMPLOYEE DIDN'TAKE THE LEAVE 
			// RECORDED BY ALOHA
			/////////////////////////////////////////////////////////
			
			if (  wizardItem.getCorrectLeaveHours().equals(wizardItem.getAlohaLeaveHours()) == false) {
				
				LeaveItem leaveItem = new LeaveItem();
				
				leaveItem.setLeaveDetail(leaveDetail);
				leaveItem.setAuditTrail(leaveDetail.getAuditTrail());
				leaveItem.setLeaveDate(wizardItem.getLeaveDate());
				leaveItem.setLeaveType(wizardItem.getLeaveTypeObj());
				leaveItem.setLeaveHours(wizardItem.getCorrectLeaveHours());
				leaveItem.setStartTime(null); // WIZARD IS NOT HANDLING START TIME
				
				leaveItemMap.put(wizardItem.getLeaveItemMapKey(), leaveItem);				
			}
		}		
		
		//System.out.println("leaveItemMap.size(): " + leaveItemMap.size());
		//System.out.println("END: LeaveReconWizard.buildLeaveItemMap(LeaveDetail)");
		return leaveItemMap;
	}
	*/	
}