package gov.gsa.ocfo.aloha.ejb.leave.impl;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import gov.gsa.ocfo.aloha.ejb.EventLoggerEJB;
import gov.gsa.ocfo.aloha.ejb.leave.LeaveRequestReportEJB;
import gov.gsa.ocfo.aloha.exception.AlohaServerException;
import gov.gsa.ocfo.aloha.model.LeaveRequestReport;
import gov.gsa.ocfo.aloha.model.PayPeriod;
import gov.gsa.ocfo.aloha.model.entity.AlohaUser;
import gov.gsa.ocfo.aloha.model.entity.audit.EventLog;
import gov.gsa.ocfo.aloha.model.entity.audit.EventType;
import gov.gsa.ocfo.aloha.util.SqlUtil;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.sql.DataSource;

/**
 * Session Bean implementation class LeaveRequestReportEJBImpl
 */
@Stateless
public class LeaveRequestReportEJBImpl implements LeaveRequestReportEJB {
	
	@Resource(name = "aloha-ds")
	private DataSource dataSource;
	
	@EJB
	EventLoggerEJB eventLoggerEJB;
	
	/*****************************************************************************************************************
	 * NOR USED
	 * ***************************************************************************************************************	
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	@Override
	public List<LeaveRequestReport> runRpt(int teamId, PayPeriod fromPP, PayPeriod toPP) throws AlohaServerException {
		List<LeaveRequestReport> rptLR = new ArrayList<LeaveRequestReport>();
		
		Connection conn = null;
		PreparedStatement ps = null;		
		
		try {
			conn = dataSource.getConnection();

			String sqlStmt = SqlUtil.GET_LR_REPORT_SQL;
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
				LeaveRequestReport leaveRequestReport = new LeaveRequestReport();
				
				//leaveRequestReport.setAreaCd(rs.getString("AreaCd"));
				//leaveRequestReport.setAreaDesc(rs.getString("AreaDesc"));
				leaveRequestReport.setEmpFName(rs.getString("first_name"));
				leaveRequestReport.setEmpId(rs.getInt("eeid"));
				leaveRequestReport.setEmpLName(rs.getString("last_name"));
				leaveRequestReport.setEmpMName(rs.getString("middle_name"));			
				
				//leaveRequestReport.setFacilityCd(rs.getString("FacilityCd"));
				//leaveRequestReport.setFacilityDesc(rs.getString("FacilityDesc"));
				leaveRequestReport.setLeaveDt(rs.getDate("leavedt"));
				//leaveRequestReport.setLeaveHrsEtams(rs.getFloat("leaveHrsEtams"));
				leaveRequestReport.setLeaveHrsAloha(rs.getFloat("leaveHrsAloha"));
				//leaveRequestReport.setLeaveType(rs.getString("leavetype"));
				leaveRequestReport.setPayPeriod(rs.getInt("PP"));
				//leaveRequestReport.setTeamCd(rs.getString("TeamCd"));
				//leaveRequestReport.setTeamDesc(rs.getString("TeamDesc"));
				leaveRequestReport.setYear(rs.getInt("YYYY"));
				leaveRequestReport.setFatCode(rs.getString("FATCode"));
				rptLR.add(leaveRequestReport);
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
		return rptLR;
	}
	******************************************************************************************************************/

	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	@Override
	public List<LeaveRequestReport> runRpt(AlohaUser alohaUser, String[] empIds, 
			String[] leaveStatus, String[] priLeaveTypes, String[] secLeaveTypes, 
			PayPeriod fromPP, PayPeriod toPP) throws AlohaServerException {
		List<LeaveRequestReport> rptLR = new ArrayList<LeaveRequestReport>();
		
		Connection conn = null;
		PreparedStatement ps = null;
		
		try {
			if (empIds.length <=0 || leaveStatus.length <= 0 || priLeaveTypes.length <= 0 || secLeaveTypes.length <= 0 || fromPP == null || toPP == null) {
				return null;
			}
			
			conn = dataSource.getConnection();

			String sqlStmt = SqlUtil.GET_LR_REPORT_SQL;
			
			//convert the empid array into a comma separated string
			StringBuilder strEmps = new StringBuilder();
			for (String empId : empIds) {
				//System.out.println("empId = " + empId.toString());
				strEmps.append(empId + ",");
				//System.out.println("strEmps = " + strEmps.toString());
			}
			strEmps.deleteCharAt(strEmps.length()-1); //remove the last comma
			
			//convert the leaveStatus array into a comma separated string for use in IN clause
			StringBuilder strLeaveStatus = new StringBuilder();
			for (String s : leaveStatus) {
				strLeaveStatus.append("" + s + "" + "," );
			}
			strLeaveStatus.deleteCharAt(strLeaveStatus.length()-1); //remove the last comma
			
			//convert the priLeaveTypes array into a comma separated string for use in IN clause
			StringBuilder strPriLeaveTypes = new StringBuilder();
			for (String s : priLeaveTypes) {
				strPriLeaveTypes.append("" + s + "" + "," );
			}
			strPriLeaveTypes.deleteCharAt(strPriLeaveTypes.length()-1); //remove the last comma

			//convert the secLeaveTypes array into a comma separated string for use in IN clause
			StringBuilder strSecLeaveTypes = new StringBuilder();
			for (String s : secLeaveTypes) {
				strSecLeaveTypes.append("" + s + "" + "," );
			}
			strSecLeaveTypes.deleteCharAt(strSecLeaveTypes.length()-1); //remove the last comma
			
			/************************************************************************************************************
			sqlStmt = sqlStmt.replaceAll(":x_emp_id", strEmps.toString());
			sqlStmt = sqlStmt.replaceAll(":x_leave_status", strLeaveStatus.toString());
			sqlStmt = sqlStmt.replaceAll(":x_primary_leave_code", strPriLeaveTypes.toString());
			sqlStmt = sqlStmt.replaceAll(":x_secondary_leave_code", strSecLeaveTypes.toString());
			//sqlStmt = sqlStmt.replaceAll(":x_from_year", Integer.toString(fromPP.getYear()));
			sqlStmt = sqlStmt.replaceAll(":x_from_pp_dt", new SimpleDateFormat("yyyyMMdd").format(fromPP.getFromDate()));
			//sqlStmt = sqlStmt.replaceAll(":x_to_year", Integer  .toString(toPP.getYear()));
			sqlStmt = sqlStmt.replaceAll(":x_to_pp_dt", new SimpleDateFormat("yyyyMMdd").format(toPP.getToDate()));
			**************************************************************************************************************/
			
			String employeeIds 			= strEmps.toString();
			String leaveStatuses		= strLeaveStatus.toString();
			String primaryLeaveTypes 	= strPriLeaveTypes.toString();
			String secondaryLeaveTypes 	= strSecLeaveTypes.toString();
			String fromPayPeriodDate 	= new SimpleDateFormat("yyyyMMdd").format(fromPP.getFromDate());
			String toPayPeriodDate 		= new SimpleDateFormat("yyyyMMdd").format(toPP.getToDate());
			
			sqlStmt = sqlStmt.replaceAll(":x_emp_id", employeeIds);
			sqlStmt = sqlStmt.replaceAll(":x_leave_status", leaveStatuses);
			sqlStmt = sqlStmt.replaceAll(":x_primary_leave_code", primaryLeaveTypes);
			sqlStmt = sqlStmt.replaceAll(":x_secondary_leave_code", secondaryLeaveTypes);
			sqlStmt = sqlStmt.replaceAll(":x_from_pp_dt", fromPayPeriodDate);
			sqlStmt = sqlStmt.replaceAll(":x_to_pp_dt", toPayPeriodDate);			
			
			//System.out.println(sqlStmt);						
			ps = conn.prepareStatement(sqlStmt);
			ResultSet rs = ps.executeQuery();
			
			while (rs.next()) {
				LeaveRequestReport leaveRequestReport = new LeaveRequestReport();
				
				//leaveRequestReport.setAreaCd(rs.getString("areaCd"));
				//leaveRequestReport.setAreaDesc(rs.getString("areaDesc"));
				leaveRequestReport.setEmpFName(rs.getString("first_name"));
				leaveRequestReport.setEmpId(rs.getInt("eeId"));
				leaveRequestReport.setEmpLName(rs.getString("last_name"));
				leaveRequestReport.setEmpMName(rs.getString("middle_name"));			
				
				//leaveRequestReport.setFacilityCd(rs.getString("facilityCd"));
				//leaveRequestReport.setFacilityDesc(rs.getString("facilityDesc"));
				leaveRequestReport.setLeaveDt(rs.getDate("leaveDt"));
				//leaveRequestReport.setLeaveHrsEtams(rs.getFloat("leaveHrsEtams"));
				leaveRequestReport.setLeaveHrsAloha(rs.getFloat("leaveHrsAloha"));
				//leaveRequestReport.setLeaveType(rs.getString("leaveType"));
				leaveRequestReport.setPayPeriod(rs.getInt("PP"));
				//leaveRequestReport.setTeamCd(rs.getString("teamCd"));
				//leaveRequestReport.setTeamDesc(rs.getString("teamDesc"));
				leaveRequestReport.setYear(rs.getInt("YYYY"));
				leaveRequestReport.setLeaveStatus(rs.getString("leaveStatus"));
				leaveRequestReport.setLrDetailId(rs.getLong("LRDID"));
				leaveRequestReport.setPrimaryLeaveType(rs.getString("leaveType"));
				leaveRequestReport.setSecondaryLeaveType(rs.getString("secleaveType"));
				//SAK 20120123 changed report to use LR Header
				leaveRequestReport.setLrHeaderId(rs.getLong("LRHID"));
				leaveRequestReport.setFatCode(rs.getString("FATCode"));
				leaveRequestReport.setLrdSeq(rs.getLong("LRDSeq"));
				leaveRequestReport.setLeaveStartTime(rs.getTimestamp("leaveStartTime"));
				rptLR.add(leaveRequestReport);
			}
			
			this.writeToEventLog(alohaUser, employeeIds, leaveStatuses, primaryLeaveTypes, secondaryLeaveTypes, fromPayPeriodDate, toPayPeriodDate);
			
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
		return rptLR;
	}
	
	private void writeToEventLog(AlohaUser alohaUser, String employeeIds,
			String leaveStatuses, String primaryLeaveTypes,
			String secondaryLeaveTypes, String fromPayPeriodDate,
			String toPayPeriodDate)
			throws AlohaServerException {

		EventLog eventLog = new EventLog();

		EventType eventType = this.eventLoggerEJB
				.getEventType(EventType.EventTypeValue.LR_REPORT.toString());
		eventLog.setEventType(eventType);
		eventLog.setUserCreated(alohaUser.getLoginName());

		StringBuilder sb = new StringBuilder();
		sb.append("loginName=");
		sb.append(alohaUser.getLoginName());
		sb.append(";userId=");
		sb.append(alohaUser.getUserId());
		sb.append(";employeeIds=");
		sb.append(employeeIds);
		sb.append(";leaveStatuses=");
		sb.append(leaveStatuses);
		sb.append(";primaryLeaveTypes=");
		sb.append(primaryLeaveTypes);
		sb.append(";secondaryLeaveTypes=");
		sb.append(secondaryLeaveTypes);
		sb.append(";fromPayPeriodDate=");
		sb.append(fromPayPeriodDate);
		sb.append(";toPayPeriodDate=");
		sb.append(toPayPeriodDate);		

		String newValue = sb.toString();
		if (newValue.length() > 4000) {
			newValue = newValue.substring(0, 4000);
		}
		eventLog.setNewValue(newValue);

		this.eventLoggerEJB.logEvent(eventLog);
	}	
}