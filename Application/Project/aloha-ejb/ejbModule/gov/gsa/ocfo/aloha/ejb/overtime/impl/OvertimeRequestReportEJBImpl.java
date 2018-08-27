package gov.gsa.ocfo.aloha.ejb.overtime.impl;


import gov.gsa.ocfo.aloha.ejb.EventLoggerEJB;
import gov.gsa.ocfo.aloha.ejb.overtime.OvertimeRequestReportEJB;
import gov.gsa.ocfo.aloha.exception.AlohaServerException;
import gov.gsa.ocfo.aloha.model.OvertimeRequestReport;
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

/**
 * Session Bean implementation class OvertimeRequestReportEJBImpl
 */
@Stateless
public class OvertimeRequestReportEJBImpl implements OvertimeRequestReportEJB {
	
	@Resource(name = "aloha-ds")
	private DataSource dataSource;
	
	@EJB
	EventLoggerEJB eventLoggerEJB;	

	/********************************************************************************************************************
	 * NOT BEING USED
	 ********************************************************************************************************************
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	@Override
	public List<OvertimeRequestReport> runRpt(int teamId, PayPeriod fromPP, PayPeriod toPP) throws AlohaServerException {
		List<OvertimeRequestReport> rptOT = new ArrayList<OvertimeRequestReport>();
		
		Connection conn = null;
		PreparedStatement ps = null;
		
		try {
			conn = dataSource.getConnection();

			String sqlStmt = SqlUtil.GET_OT_REPORT_SQL;
			//Step 1: Build a list of all the bind variables in sequence order of appearance.
			//The list will be used to replace them with bind variables in step 3
			List<String> bVars = new ArrayList<String>();
			int nxtBV = 0;	int endNxtBV = 0;
			while (true) {
				//get index of next bind var -- begins with :x_
				nxtBV = sqlStmt.indexOf(":x_", endNxtBV);
				if (nxtBV == -1) break;
				endNxtBV =  sqlStmt.indexOf(" ", nxtBV); //ensure bind vars in sql have a space at end
				bVars.add(sqlStmt.substring(nxtBV, endNxtBV));
			}
			
			//Step 2: replace bind variables with ?
			for (String bV : bVars) {
				sqlStmt = sqlStmt.replaceAll(bV, "?");
			}

			ps = conn.prepareStatement(sqlStmt);
			//Step 3: Substitute bind variables. The list bVars contains all the bind variables in sequence of occurence
			//Loop thru list, check variable and substitute the corresponding value
			int i = 1;
			for (String bV : bVars) {
				if (bV.equals(":x_teamid"))  ps.setInt(i, teamId);
				if (bV.equals(":x_from_year")) 	ps.setInt(i, fromPP.getYear());
				if (bV.equals(":x_from_pp")) 	ps.setInt(i, fromPP.getPayPeriod());
				if (bV.equals(":x_to_year")) 	ps.setInt(i, toPP.getYear());
				if (bV.equals(":x_to_pp")) 	ps.setInt(i, toPP.getPayPeriod());
				
				i=i+1;
			}
//			sqlStmt = sqlStmt.replaceAll(":x_teamid", Integer.toString(teamId));
//			sqlStmt = sqlStmt.replaceAll(":x_from_year", Integer.toString(fromPP.getYear()));
//			sqlStmt = sqlStmt.replaceAll(":x_from_pp", Integer.toString(fromPP.getPayPeriod()));
//			sqlStmt = sqlStmt.replaceAll(":x_to_year", Integer.toString(toPP.getYear()));
//			sqlStmt = sqlStmt.replaceAll(":x_to_pp", Integer.toString(toPP.getPayPeriod()));
			
									
			ResultSet rs = ps.executeQuery();
			
			while (rs.next()) {
				OvertimeRequestReport overtimeRequestReport = new OvertimeRequestReport();
				
				overtimeRequestReport.setEmpFName(rs.getString("first_name"));
				overtimeRequestReport.setEmpId(rs.getInt("eeid"));
				overtimeRequestReport.setEmpLName(rs.getString("last_name"));
				overtimeRequestReport.setEmpMName(rs.getString("middle_name"));			
				
				overtimeRequestReport.setOvertimeDt(rs.getString("otdt"));
				overtimeRequestReport.setOvertimeHrsAloha(rs.getFloat("otHrsAloha"));
				overtimeRequestReport.setPayPeriod(rs.getInt("PP"));
				overtimeRequestReport.setYear(rs.getInt("YYYY"));
				
				rptOT.add(overtimeRequestReport);
			}
			
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
	
	**************************************************************************************************************************************/

	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	@Override
	public List<OvertimeRequestReport> runRpt(AlohaUser alohaUser, String[] empIds, 
			String[] selectedOvertimeStatuses, String[] overtimeTypes,
			String[] selectedOtPlanGrade, OTPayPeriod fromPP,OTPayPeriod toPP) throws AlohaServerException {
		List<OvertimeRequestReport> rptOT = new ArrayList<OvertimeRequestReport>();
		
		Connection conn = null;
		PreparedStatement ps = null;
		
		try {
			if (empIds.length <=0 || //selectedOvertimeStatuses.length <= 0 ||  //overtimeTypes.length <= 0 || 
					fromPP == null || toPP == null) {
				return null;
			}
			
			conn = dataSource.getConnection();

			String sqlStmt = SqlUtil.GET_OT_REPORT_SQL;
			
			//convert the empid array into a comma separated string
			StringBuilder strEmps = new StringBuilder();
			for (String empId : empIds) {
				//System.out.println("empId = " + empId.toString());
				strEmps.append(empId + ",");
				//System.out.println("strEmps = " + strEmps.toString());
			}
			strEmps.deleteCharAt(strEmps.length()-1); //remove the last comma
			
			StringBuilder strPlanGrade = new StringBuilder();
			for (String planGrade : selectedOtPlanGrade) {
				strPlanGrade.append("'" + planGrade + "'" + ",");
			}
			strPlanGrade.deleteCharAt(strPlanGrade.length()-1); //remove the last comma
			
			//convert the overtimeStatus array into a comma separated string for use in IN clause
			StringBuilder strOvertimeStatus = new StringBuilder();
			for (String s : selectedOvertimeStatuses) {
				strOvertimeStatus.append("" + s + "" + "," );
			}
			strOvertimeStatus.deleteCharAt(strOvertimeStatus.length()-1); //remove the last comma
			
			StringBuilder strOvertimeTypes = new StringBuilder();
			for (String otType : overtimeTypes) {
				strOvertimeTypes.append("'" + otType + "'" + "," );
			}
			strOvertimeTypes.deleteCharAt(strOvertimeTypes.length()-1); //remove the last comma

			String employeeIds 			= strEmps.toString();
			String otStatuses 			= strOvertimeStatus.toString();
			String otTypes 				= strOvertimeTypes.toString();
			String planGrade 			= strPlanGrade.toString();
			String fromPayPeriodYear 	= Integer.toString(fromPP.getYear());
			String fromPayPeriodNbr		= Integer.toString(fromPP.getNumber());
			String toPayPeriodYear 		= Integer.toString(toPP.getYear());
			String toPayPeriodNbr	 	= Integer.toString(toPP.getNumber());
			
			/*********************************************************************************
			sqlStmt = sqlStmt.replaceAll(":x_emp_id", strEmps.toString());
			sqlStmt = sqlStmt.replaceAll(":x_ot_status", strOvertimeStatus.toString());
			sqlStmt = sqlStmt.replaceAll(":x_overtime_type", strOvertimeTypes.toString());
			sqlStmt = sqlStmt.replaceAll(":x_plan_grade", strPlanGrade.toString());
			sqlStmt = sqlStmt.replaceAll(":x_from_year", Integer.toString(fromPP.getYear()));
			sqlStmt = sqlStmt.replaceAll(":x_from_pp", Integer.toString(fromPP.getNumber()));
			sqlStmt = sqlStmt.replaceAll(":x_to_year", Integer.toString(toPP.getYear()));
			sqlStmt = sqlStmt.replaceAll(":x_to_pp", Integer.toString(toPP.getNumber()));
			**********************************************************************************/
			
			sqlStmt = sqlStmt.replaceAll(":x_emp_id", employeeIds);
			sqlStmt = sqlStmt.replaceAll(":x_ot_status", otStatuses);
			sqlStmt = sqlStmt.replaceAll(":x_overtime_type", otTypes);
			sqlStmt = sqlStmt.replaceAll(":x_plan_grade", planGrade);
			sqlStmt = sqlStmt.replaceAll(":x_from_year", fromPayPeriodYear);
			sqlStmt = sqlStmt.replaceAll(":x_from_pp", fromPayPeriodNbr);
			sqlStmt = sqlStmt.replaceAll(":x_to_year", toPayPeriodYear);
			sqlStmt = sqlStmt.replaceAll(":x_to_pp", toPayPeriodNbr);			
			
			//System.out.println("-------------------------------------------------");
			//System.out.println(sqlStmt);						
			//System.out.println("-------------------------------------------------");
			
			ps = conn.prepareStatement(sqlStmt);
			ResultSet rs = ps.executeQuery();
			
			while (rs.next()) {
				OvertimeRequestReport overtimeRequestReport = new OvertimeRequestReport();
				
				overtimeRequestReport.setEmpFName(rs.getString("emp_fname"));
				overtimeRequestReport.setEmpId(rs.getInt("eeId"));
				overtimeRequestReport.setEmpLName(rs.getString("emp_lname"));
				overtimeRequestReport.setEmpMName(rs.getString("emp_mname"));			
				overtimeRequestReport.setPlanGrade(rs.getString("plan_grade"));	
				overtimeRequestReport.setOvertimeDt(rs.getString("otDt"));
				overtimeRequestReport.setOvertimeHrsAloha(rs.getFloat("aloha_othrs"));
				overtimeRequestReport.setTaskDesc(rs.getString("task_desc"));
				overtimeRequestReport.setAppFName(rs.getString("app_fname"));
				overtimeRequestReport.setAppLName(rs.getString("app_lname"));
				overtimeRequestReport.setAppMName(rs.getString("app_mname"));	
				overtimeRequestReport.setPayPeriod(rs.getInt("PP"));
				overtimeRequestReport.setYear(rs.getInt("YYYY"));
				overtimeRequestReport.setOvertimeStatus(rs.getString("otStatus"));
				overtimeRequestReport.setOtHeaderId(rs.getLong("OHID"));
				overtimeRequestReport.setOvertimeType(rs.getString("type"));
				overtimeRequestReport.setOtDetailId(rs.getLong("ODID"));
				//overtimeRequestReport.setOtdSeq(rs.getLong("dotseq"));
				rptOT.add(overtimeRequestReport);
			}
			ps.close();
			
			this.writeToEventLog(alohaUser, employeeIds, otStatuses, otTypes, planGrade, fromPayPeriodYear, fromPayPeriodNbr, toPayPeriodYear, toPayPeriodNbr);
			
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

	public void writeToEventLog (AlohaUser alohaUser, String employeeIds, 
			String otStatuses, String otTypes, String planGrade,  
			String fromPayPeriodYear, String fromPayPeriodNbr, 
			String toPayPeriodYear, String toPayPeriodNbr
			) throws AlohaServerException {
		
		EventLog eventLog = new EventLog();
			
		EventType eventType = this.eventLoggerEJB.getEventType(EventType.EventTypeValue.OT_REPORT.toString()); 
		eventLog.setEventType(eventType);
		eventLog.setUserCreated(alohaUser.getLoginName());	
		
		StringBuilder sb = new StringBuilder();
		sb.append("loginName=");
		sb.append(alohaUser.getLoginName());
		sb.append(";userId=");
		sb.append(alohaUser.getUserId());;
		sb.append(";employeeIds=");
		sb.append(employeeIds);
		sb.append(";otStatuses=");
		sb.append(otStatuses);	
		sb.append(";otTypes=");
		sb.append(otTypes);	
		sb.append(";planGrade=");
		sb.append(planGrade);			
		sb.append(";fromPayPeriodYear=");
		sb.append(fromPayPeriodYear);
		sb.append(";fromPayPeriodNbr=");
		sb.append(fromPayPeriodNbr);			
		sb.append(";toPayPeriodYear=");
		sb.append(toPayPeriodYear);
		sb.append(";toPayPeriodNbr=");
		sb.append(toPayPeriodNbr);					
				
		String newValue = sb.toString();
		if ( newValue.length() > 4000) {
			newValue = newValue.substring(0, 4000);	
		}
		eventLog.setNewValue(newValue);
			
		this.eventLoggerEJB.logEvent(eventLog);
	}	
}
