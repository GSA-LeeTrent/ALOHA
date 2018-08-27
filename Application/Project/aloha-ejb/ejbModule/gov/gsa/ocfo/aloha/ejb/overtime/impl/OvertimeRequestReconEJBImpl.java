package gov.gsa.ocfo.aloha.ejb.overtime.impl;


import gov.gsa.ocfo.aloha.ejb.EventLoggerEJB;
import gov.gsa.ocfo.aloha.ejb.overtime.OvertimeRequestReconEJB;
import gov.gsa.ocfo.aloha.exception.AlohaServerException;
import gov.gsa.ocfo.aloha.model.OvertimeRequestReconciliation;
import gov.gsa.ocfo.aloha.model.entity.AlohaUser;
import gov.gsa.ocfo.aloha.model.entity.audit.EventLog;
import gov.gsa.ocfo.aloha.model.entity.audit.EventType;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTPayPeriod;
import gov.gsa.ocfo.aloha.util.SqlUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.sql.DataSource;

@Stateless
public class OvertimeRequestReconEJBImpl implements OvertimeRequestReconEJB {
	@Resource(name = "aloha-ds")
	private DataSource dataSource;
	
	@EJB
	EventLoggerEJB eventLoggerEJB;	

	//sak
	/******************************************************************************************************
	public String getXML(List<OvertimeRequestReconciliation> rptOTRecon) {
		String xmlReconRpt = "<?xml version=" + '"' + "1.0" + '"' + "?><OvertimeRequestReconciliations>";
		if (rptOTRecon != null) {
			for (LeaveRequestReconciliation otr : rptOTRecon) {
				xmlReconRpt = xmlReconRpt + "<OvertimeRequestReconciliation>";
				xmlReconRpt = xmlReconRpt + "<LastName>" + otr.getEmpLName() + "</LastName>";
				xmlReconRpt = xmlReconRpt + "<FirstName>" + otr.getEmpFName() + "</FirstName>";
				xmlReconRpt = xmlReconRpt + "</OvertimeRequestReconciliation>";
			}
		}
		xmlReconRpt = xmlReconRpt + "</OvertimeRequestReconciliations>";
		return xmlReconRpt;
	}
	******************************************************************************************************/
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	@Override
	public List<OvertimeRequestReconciliation> runRpt(AlohaUser alohaUser, String[] overtimeTypes, 
			String[] otPlanGrade, String[] empIds, OTPayPeriod fromPP,
			OTPayPeriod toPP) throws AlohaServerException {
		List<OvertimeRequestReconciliation> rptOT = new ArrayList<OvertimeRequestReconciliation>();
		
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			if (//overtimeTypes.length <= 0 || 
					empIds.length <= 0 || fromPP == null || toPP == null) {
				return null;
			}
			
			conn = dataSource.getConnection();

			String sqlStmt = SqlUtil.RUN_OT_RECON_SQL;
			
			//convert the overtimeTypes array into a comma separated string for use in IN clause
			StringBuilder strOvertimeTypes = new StringBuilder();
			for (String s : overtimeTypes) {
				strOvertimeTypes.append("" + s + "" + "," );
			}
			strOvertimeTypes.deleteCharAt(strOvertimeTypes.length()-1); //remove the last comma

			//convert the empid array into a comma separated string
			StringBuilder strEmps = new StringBuilder();
			for (String empId : empIds) {
				//System.out.println("empId = " + empId.toString());
				strEmps.append(empId + ",");
				//System.out.println("strEmps = " + strEmps.toString());
			}
			strEmps.deleteCharAt(strEmps.length()-1); //remove the last comma
			
			StringBuilder strPlanGrade = new StringBuilder();
			for (String planGrade : otPlanGrade) {
				strPlanGrade.append("'" + planGrade + "'" + ",");
			}
			strPlanGrade.deleteCharAt(strPlanGrade.length()-1); //remove the last comma
			
			String otTypes 				= strOvertimeTypes.toString();
			String employeeIds 			= strEmps.toString();
			String fromPayPeriodYear 	= Integer.toString(fromPP.getYear());
			String fromPayPeriodNbr		= Integer.toString(fromPP.getNumber());
			String toPayPeriodYear 		= Integer.toString(toPP.getYear());
			String toPayPeriodNbr	 	= Integer.toString(toPP.getNumber());
			String planGrade 			= strPlanGrade.toString();
			
			/********************************************************************************
			sqlStmt = sqlStmt.replaceAll(":x_overtime_type", strOvertimeTypes.toString());
			sqlStmt = sqlStmt.replaceAll(":x_emp_id", strEmps.toString());
			sqlStmt = sqlStmt.replaceAll(":x_from_year", Integer.toString(fromPP.getYear()));
			sqlStmt = sqlStmt.replaceAll(":x_from_pp", Integer.toString(fromPP.getNumber()));
			sqlStmt = sqlStmt.replaceAll(":x_to_year", Integer.toString(toPP.getYear()));
			sqlStmt = sqlStmt.replaceAll(":x_to_pp", Integer.toString(toPP.getNumber()));
			sqlStmt = sqlStmt.replaceAll(":x_plan_grade", strPlanGrade.toString());
			*********************************************************************************/
			
			sqlStmt = sqlStmt.replaceAll(":x_overtime_type", otTypes);
			sqlStmt = sqlStmt.replaceAll(":x_emp_id", employeeIds);
			sqlStmt = sqlStmt.replaceAll(":x_from_year", fromPayPeriodYear);
			sqlStmt = sqlStmt.replaceAll(":x_from_pp", fromPayPeriodNbr);
			sqlStmt = sqlStmt.replaceAll(":x_to_year", toPayPeriodYear);
			sqlStmt = sqlStmt.replaceAll(":x_to_pp", toPayPeriodNbr);
			sqlStmt = sqlStmt.replaceAll(":x_plan_grade", planGrade);			
			
//			System.out.println("-------------------------------------------------");
//			System.out.println(sqlStmt);						
//			System.out.println("-------------------------------------------------");
			
			ps = conn.prepareStatement(sqlStmt);
			ResultSet rs = ps.executeQuery();
			
			while (rs.next()) {
				OvertimeRequestReconciliation otr = new OvertimeRequestReconciliation();
				
				otr.setEmpFName(rs.getString("first_name"));
				otr.setEmpId(rs.getInt("eeId"));
				otr.setEmpLName(rs.getString("last_name"));
				otr.setEmpMName(rs.getString("middle_name"));			
				otr.setPlanGrade(rs.getString("plan_grade"));
				otr.setOvertimeDt(rs.getString("otDate"));
				otr.setOvertimeHrsEtams(rs.getFloat("otHrsEtams"));
				otr.setOvertimeHrsAloha(rs.getFloat("otHrsAloha"));
				otr.setOvertimeType(rs.getString("type"));
				otr.setPayPeriod(rs.getInt("PP"));
				otr.setYear(rs.getInt("YYYY"));
//				otr.setOtHeaderId(rs.getString("otHeaderId"));
				otr.setFATCode(rs.getString("FATCode"));
//				otr.setOtDetailId(rs.getString("ODID"));
				otr.setAppFName(rs.getString("app_fname"));
				otr.setAppMName(rs.getString("app_mname"));	
				otr.setAppLName(rs.getString("app_lname"));
				
				rptOT.add(otr);
			}
			
			// WRTIE TO EVENT LOG
			this.writeToEventLog(alohaUser, otTypes, employeeIds, fromPayPeriodYear, fromPayPeriodNbr, toPayPeriodYear, toPayPeriodNbr, planGrade);
			
		} catch (SQLException e) {
			e.printStackTrace();
			throw new AlohaServerException("SQLException encountered in " + this.getClass().getName() + ".runRpt(): " + e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			throw new AlohaServerException("Exception encountered in " + this.getClass().getName() + ".runRpt(): " + e.getMessage());
		} finally {
			SqlUtil.closePreparedStatement(ps);
			SqlUtil.closeConnection(conn);
			ps = null;
			conn = null;
		}		
		return rptOT;
	}
	
	public void writeToEventLog (AlohaUser alohaUser, String otTypes, String employeeIds, 
			String fromPayPeriodYear, String fromPayPeriodNbr, 
			String toPayPeriodYear, String toPayPeriodNbr,
			String planGrade) throws AlohaServerException {
		
		EventLog eventLog = new EventLog();
			
		EventType eventType = this.eventLoggerEJB.getEventType(EventType.EventTypeValue.OT_RECON.toString()); 
		eventLog.setEventType(eventType);
		eventLog.setUserCreated(alohaUser.getLoginName());	
		
		StringBuilder sb = new StringBuilder();
		sb.append("loginName=");
		sb.append(alohaUser.getLoginName());
		sb.append(";userId=");
		sb.append(alohaUser.getUserId());
		sb.append(";otTypes=");
		sb.append(otTypes);
		sb.append(";employeeIds=");
		sb.append(employeeIds);
		sb.append(";fromPayPeriodYear=");
		sb.append(fromPayPeriodYear);
		sb.append(";fromPayPeriodNbr=");
		sb.append(fromPayPeriodNbr);			
		sb.append(";toPayPeriodYear=");
		sb.append(toPayPeriodYear);
		sb.append(";toPayPeriodNbr=");
		sb.append(toPayPeriodNbr);					
		sb.append(";planGrade=");
		sb.append(planGrade);					
	
		String newValue = sb.toString();
		if ( newValue.length() > 4000) {
			newValue = newValue.substring(0, 4000);	
		}
		eventLog.setNewValue(newValue);
			
		this.eventLoggerEJB.logEvent(eventLog);
	}	
}
