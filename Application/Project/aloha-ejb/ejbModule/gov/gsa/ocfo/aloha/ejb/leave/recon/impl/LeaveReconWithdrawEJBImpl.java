package gov.gsa.ocfo.aloha.ejb.leave.recon.impl;

import gov.gsa.ocfo.aloha.ejb.leave.LeaveRequestEJB;
import gov.gsa.ocfo.aloha.ejb.leave.recon.LeaveReconWithdrawCreateEJB;
import gov.gsa.ocfo.aloha.ejb.leave.recon.LeaveReconWithdrawEJB;
import gov.gsa.ocfo.aloha.exception.AlohaServerException;
import gov.gsa.ocfo.aloha.exception.LeaveReconException;
import gov.gsa.ocfo.aloha.model.entity.AlohaUser;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveDetail;
import gov.gsa.ocfo.aloha.model.entity.leave.recon.LeaveReconWizard;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

@Stateless
public class LeaveReconWithdrawEJBImpl implements LeaveReconWithdrawEJB {

	@EJB
	LeaveReconWithdrawCreateEJB withdrawCreateEJB;
	
	@EJB
	LeaveRequestEJB leaveRequestEJB;
	

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void processWithdrawLeaveRequests(AlohaUser employeeSubmitter, LeaveReconWizard leaveReconWizard)
			throws LeaveReconException {
		
		//System.out.println("BEGIN: LeaveReconWithdrawEJB.processWithdrawLeaveRequests()");
		
		try {
			
			//////////////////////////////////////////////////////////////////////////////////////			
			// 1) RETRIEVE ALL PREVIOUSLY APPROVED LEAVE REQUESTS FOR THIS EMPLOYEE AND PAY PERIOD
			// 2) CHANGE THE STATUS OF EACH RETRIEVED LEAVE REQUEST TO 'PEND WITHDRAW'
			// 3) ADD THEM TO THE 'withdrawList'
			//////////////////////////////////////////////////////////////////////////////////////
			List<LeaveDetail> withdrawList = this.withdrawCreateEJB.createLeaveWithdrawals(employeeSubmitter, leaveReconWizard);
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
		} catch (AlohaServerException ase) {
			StringBuilder errMsg = new StringBuilder();
			errMsg.append("Exception encountered in ");
			errMsg.append(this.getClass().getName());
			errMsg.append(".processWithdrawLeaveRequests(AlohaUser employeeSubmitter, LeaveReconWizard leaveReconWizard)");
			errMsg.append("\nwhen attempting to process leave request withdrawals.");
			errMsg.append("\nTRANSACTIN ROLLED BACK");
			throw new LeaveReconException(errMsg.toString());
		}
		//System.out.println("END: LeaveReconWithdrawEJB.processWithdrawLeaveRequests()");
	}
}
