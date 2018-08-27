package gov.gsa.ocfo.aloha.ejb.leave.recon.impl;

import gov.gsa.ocfo.aloha.ejb.leave.recon.EtamsAmendmentsEJB;
import gov.gsa.ocfo.aloha.exception.LeaveReconException;
import gov.gsa.ocfo.aloha.model.entity.leave.recon.LeaveReconWizard;
import gov.gsa.ocfo.aloha.util.SqlUtil;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.sql.DataSource;

@Stateless
public class EtamsAmendmentsEJBImpl implements EtamsAmendmentsEJB {

	@Resource(name = "aloha-ds")
	private DataSource dataSource;
	
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public String processEtamsAmendments(LeaveReconWizard leaveReconWizard) throws LeaveReconException {

		if ( leaveReconWizard.isEtamsAmendmentRequired() == false) { 
			return null;
		}
		
		Connection conn = null;
		CallableStatement callStmt = null;
		
		try {
			
			
			///////////////////////////////////////////////////////////////////////////////////////////////////////
			// DEBUG
			///////////////////////////////////////////////////////////////////////////////////////////////////////
			System.out.println("EtamsAmendmentsEJB.processEtamsAmendments(): BEGIN DEBUG");
			System.out.println("leaveReconWizard.getEmployeeUserId().: " + leaveReconWizard.getEmployeeUserId());
			System.out.println("leaveReconWizard.getLeaveYear()......: " + leaveReconWizard.getLeaveYear());
			System.out.println("leaveReconWizard.getPayPeriodNumber(): " + leaveReconWizard.getPayPeriodNumber());
			System.out.println("EtamsAmendmentsEJB.processEtamsAmendments(): END DEBUG");
			///////////////////////////////////////////////////////////////////////////////////////////////////////
			
			//////////////////////////////////////////
			// OBTAIN CONNECTION FROM DATA SOURCE
			//////////////////////////////////////////
			conn = this.dataSource.getConnection();

			//////////////////////////////////////////////////////////////////////////////////////////////////////			
			// SCHEMA:			USR_FEDDESK_REQUIRED
			// PACKAGE: 		ALOHA_AMENDMENT_UPDT
			// FUNCTION:		updt_leave
			// RETURNS:			VARCHAR (User readable message)
			// 1ST PARAMETER:	EMPLOYEE USER ID
			// 2ND PARAMETER:	LEAVE YEAR
			// 3RD PARAMATER: 	PAY PERIOD NUMBER
			//////////////////////////////////////////////////////////////////////////////////////////////////////
			callStmt = conn.prepareCall("{? = call USR_FEDDESK_REQUIRED.ALOHA_AMENDMENT_UPDT.updt_leave(?,?,?)}");
			
			/////////////////////////////////////////////////
			// VARCHAR THAT THIS FUNTION WILL RETURN
			/////////////////////////////////////////////////			
			callStmt.registerOutParameter(1, Types.VARCHAR);
			
			/////////////////////////////////////////////////			
			// EMPLOYEE'S USER ID (1st PARAMETER)
			/////////////////////////////////////////////////			
			callStmt.setInt(2, leaveReconWizard.getEmployeeUserId());
			
			/////////////////////////////////////////////////			
			// LEAVE YEAR (2nd PARAMETER)
			/////////////////////////////////////////////////			
			callStmt.setInt(3, leaveReconWizard.getLeaveYear());
			
			/////////////////////////////////////////////////			
			// PAY PERIOD NUMBER (3rd PARAMETER)
			/////////////////////////////////////////////////			
			callStmt.setInt(4, leaveReconWizard.getPayPeriodNumber());
			
			/////////////////////////////////////////////////			
			// EXECUTE THIS CALLABLE STATEMENT
			/////////////////////////////////////////////////			
			callStmt.executeUpdate();
			
			////////////////////////////////////////////////////			
			// RETRIEVE AND RETURN MESAGE RETURNED BY FUNCTION
			////////////////////////////////////////////////////			
			return callStmt.getString(1);
			
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			//throw new LeaveReconException(e.getMessage());
			return e.getMessage();
		} finally {
			SqlUtil.closeStatement(callStmt);
			SqlUtil.closeConnection(conn);
			callStmt = null;
			conn = null;
		}
	}
}