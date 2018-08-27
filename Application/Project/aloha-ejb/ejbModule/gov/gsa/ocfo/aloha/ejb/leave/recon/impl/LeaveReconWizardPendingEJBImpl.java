package gov.gsa.ocfo.aloha.ejb.leave.recon.impl;

import gov.gsa.ocfo.aloha.ejb.feddesk.DateTableEJB;
import gov.gsa.ocfo.aloha.ejb.leave.recon.LeaveReconWizardPendingEJB;
import gov.gsa.ocfo.aloha.exception.AlohaServerException;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveDetail;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveItem;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveType;
import gov.gsa.ocfo.aloha.model.feddesk.DateTable;
import gov.gsa.ocfo.aloha.util.SqlUtil;
import gov.gsa.ocfo.aloha.util.StringUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.sql.DataSource;

@Stateless
public class LeaveReconWizardPendingEJBImpl implements LeaveReconWizardPendingEJB {

	@Resource(name = "aloha-ds")
	private DataSource dataSource;
	
	@EJB
	DateTableEJB dateTableEJB;
	
	@TransactionAttribute(TransactionAttributeType.MANDATORY)
	public void prunePendingWizardItems(LeaveDetail leaveDetail) throws AlohaServerException {
		
		//System.out.println("BEGIN: LeaveReconWizardPendingEJB.prunePendingItems(LeaveDetail)");
		
		this.validate(leaveDetail);
		
		//System.out.println("# of Leave Items: " + leaveDetail.getLeaveItems().size());
		
		Connection conn = null;
		PreparedStatement prepStmt = null;
		
		//int deletecount = 0;
		try {
			conn = this.dataSource.getConnection();
			prepStmt = conn.prepareStatement(SqlUtil.DELETE_LR_RECON_WIZARD_PENDING_SQl);
			
			for ( LeaveItem leaveItem : leaveDetail.getLeaveItems() ) {
				
				// BUILD PRIMARY KEY
				String wizardItemKey = this.buildLeaveReconItemKey(leaveItem);
				//System.out.println("Attempting to DELETE: '" + wizardItemKey);
				
				prepStmt.setString(1, wizardItemKey);
				prepStmt.executeUpdate();
				//System.out.println("DELETE successful: " + rowsUpdated);
				
				//deletecount++;
				//System.out.println("deletecount: " + deletecount);
			}	
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new AlohaServerException(sqle);

		} catch (AlohaServerException ase) {
			//System.out.println(ase.getMessage());
			ase.printStackTrace();
			throw ase;
		} finally {
			SqlUtil.closePreparedStatement(prepStmt);
			SqlUtil.closeConnection(conn);
			prepStmt = null;
			conn = null;
		}
		
		//System.out.println("END: LeaveReconWizardPendingEJB.prunePendingItems(LeaveDetail)");
	}
	
	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	private String buildLeaveReconItemKey(LeaveItem leaveItem) throws AlohaServerException {
		
		//System.out.println("BEGIN: LeaveReconWizardPendingEJB.buildLeaveReconItemKey(LeaveItem)");
		
		// RETRIEVE THE CORRECT DATE_TABLE ROW FOR THIS LEAVE DATE
		// WE NEED BOTH LEAVE YEAR AND PAY PERIOD TO BUILD A PORTION
		// OF THE LR_RECON_WIZARD_ITEM KEY
		DateTable dateTableRow = this.dateTableEJB.retrieveDateTableForDate(leaveItem.getLeaveDate());
		
		// EMPLOYEE USER ID
		long emplUserId = leaveItem.getLeaveDetail().getEmployee().getUserId();

		// LEAVE YEAR
		int leaveYear = dateTableRow.getYear();
		
		// LEAVE PAY PERIOD
		int leavePayPeriod = dateTableRow.getPayPeriod();
		
		// LEAVE DATE STRING
		String leaveDateStr = new SimpleDateFormat("yyyyMMdd").format(leaveItem.getLeaveDate());
		
		// LEAVE TYPE CODE
		LeaveType leaveType = leaveItem.getLeaveType();
		String leaveTypeCode = ( StringUtil.isNullOrEmpty(leaveType.getSecondaryCode()) == true ) ? leaveType.getPrimaryCode() : leaveType.getSecondaryCode(); 			
			
		// BUILD LEAVE RECON WIZARD ITEM KEY
		StringBuilder key = new StringBuilder();
		key.append(emplUserId);
		key.append("-");
		key.append(leaveYear);
		key.append("-");
		key.append(leavePayPeriod);
		key.append("-");
		key.append(leaveDateStr);
		key.append("-");
		key.append(leaveTypeCode);
		
		//System.out.println("END: LeaveReconWizardPendingEJB.buildLeaveReconItemKey(LeaveItem)");
		
		return key.toString();
	}
	
	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	private void validate(LeaveDetail leaveDetail) throws AlohaServerException {

		//System.out.println("BEGIN: LeaveReconWizardPendingEJB.validate(LeaveDetail)");
		
		if (leaveDetail == null ) {
			
			StringBuilder errMsg = new StringBuilder();
			errMsg.append("NULL Parameter 'LeaveDetail' encountered in LeaveReconWizardPendingEJB.prunePendingItems(LeaveDetail)");
			errMsg.append("\nCANNOT CONTINUE");
			throw new AlohaServerException(errMsg.toString());
		}
		if ( leaveDetail.getLeaveHeader() == null ) {
			
			StringBuilder errMsg = new StringBuilder();
			errMsg.append("Leave Header is NULL for LeaveDetail with the following Leave Detail ID: '");
			errMsg.append(leaveDetail.getId());
			errMsg.append("'");
			errMsg.append("\nCANNOT CONTINUE with LeaveReconWizardPendingEJB.prunePendingItems(LeaveDetail)");
			throw new AlohaServerException(errMsg.toString());
		}
		if ( leaveDetail.isApproved() == false 
				&& leaveDetail.isWithdrawn() == false
				&& leaveDetail.isDenied() == false ) {

			StringBuilder errMsg = new StringBuilder();
			errMsg.append("The following Leave Request with the following identifiers is not in an APPROVED or WITHDRAWN state: ");
			errMsg.append("[Leave Header ID: '");
			errMsg.append(leaveDetail.getLeaveHeaderId());
			errMsg.append("', Leave Detail ID: ");
			errMsg.append(leaveDetail.getId());
			errMsg.append("]");
			errMsg.append("\nCANNOT CONTINUE with LeaveReconWizardPendingEJB.prunePendingItems(LeaveDetail)");
			throw new AlohaServerException(errMsg.toString());
		}
		
		//System.out.println("END: LeaveReconWizardPendingEJB.validate(LeaveDetail)");
	}
}