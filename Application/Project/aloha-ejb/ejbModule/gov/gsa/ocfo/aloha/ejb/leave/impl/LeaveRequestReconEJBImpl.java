package gov.gsa.ocfo.aloha.ejb.leave.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.sql.DataSource;

import gov.gsa.ocfo.aloha.ejb.EventLoggerEJB;
import gov.gsa.ocfo.aloha.ejb.leave.LeaveRequestReconEJB;
import gov.gsa.ocfo.aloha.exception.AlohaServerException;
import gov.gsa.ocfo.aloha.model.LeaveRequestReconciliation;
import gov.gsa.ocfo.aloha.model.PayPeriod;
import gov.gsa.ocfo.aloha.model.entity.AlohaUser;
import gov.gsa.ocfo.aloha.model.entity.audit.EventLog;
import gov.gsa.ocfo.aloha.model.entity.audit.EventType;
import gov.gsa.ocfo.aloha.util.SqlUtil;

@Stateless
public class LeaveRequestReconEJBImpl implements LeaveRequestReconEJB {
	@Resource(name = "aloha-ds")
	private DataSource dataSource;

	@EJB
	EventLoggerEJB eventLoggerEJB;

	// sak
	/******************************************************************************************************
	 * public String getXML(List<LeaveRequestReconciliation> rptLRRecon) {
	 * String xmlReconRpt = "<?xml version=" + '"' + "1.0" + '"' +
	 * "?><LeaveRequestReconciliations>"; if (rptLRRecon != null) { for
	 * (LeaveRequestReconciliation lrr : rptLRRecon) { xmlReconRpt = xmlReconRpt
	 * + "<LeaveRequestReconciliation>"; xmlReconRpt = xmlReconRpt +
	 * "<LastName>" + lrr.getEmpLName() + "</LastName>"; xmlReconRpt =
	 * xmlReconRpt + "<FirstName>" + lrr.getEmpFName() + "</FirstName>";
	 * xmlReconRpt = xmlReconRpt + "</LeaveRequestReconciliation>"; } }
	 * xmlReconRpt = xmlReconRpt + "</LeaveRequestReconciliations>"; return
	 * xmlReconRpt; }
	 ******************************************************************************************************/
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	@Override
	public List<LeaveRequestReconciliation> runRpt(AlohaUser alohaUser,
			String[] priLeaveTypes, String[] secLeaveTypes, String[] empIds,
			PayPeriod fromPP, PayPeriod toPP) throws AlohaServerException {
		
		List<LeaveRequestReconciliation> rptLR = new ArrayList<LeaveRequestReconciliation>();

		Connection conn = null;
		PreparedStatement ps = null;

		try {
			if (priLeaveTypes.length <= 0 || secLeaveTypes.length <= 0
					|| empIds.length <= 0 || fromPP == null || toPP == null) {
				return null;
			}

			conn = dataSource.getConnection();

			String sqlStmt = SqlUtil.GET_LR_RECON_RPT_SQL;

			// convert the priLeaveTypes array into a comma separated string for
			// use in IN clause
			StringBuilder strPriLeaveTypes = new StringBuilder();
			for (String s : priLeaveTypes) {
				strPriLeaveTypes.append("" + s + "" + ",");
			}
			strPriLeaveTypes.deleteCharAt(strPriLeaveTypes.length() - 1); // remove
																			// the
																			// last
																			// comma

			// convert the secLeaveTypes array into a comma separated string for
			// use in IN clause
			StringBuilder strSecLeaveTypes = new StringBuilder();
			for (String s : secLeaveTypes) {
				strSecLeaveTypes.append("" + s + "" + ",");
			}
			strSecLeaveTypes.deleteCharAt(strSecLeaveTypes.length() - 1); // remove
																			// the
																			// last
																			// comma

			// convert the empid array into a comma separated string
			StringBuilder strEmps = new StringBuilder();
			for (String empId : empIds) {
				// System.out.println("empId = " + empId.toString());
				strEmps.append(empId + ",");
				// System.out.println("strEmps = " + strEmps.toString());
			}
			strEmps.deleteCharAt(strEmps.length() - 1); // remove the last comma

			/************************************************************************************************************
			 * sqlStmt = sqlStmt.replaceAll(":x_pri_leave_type",
			 * strPriLeaveTypes.toString()); sqlStmt =
			 * sqlStmt.replaceAll(":x_sec_leave_type",
			 * strSecLeaveTypes.toString()); sqlStmt =
			 * sqlStmt.replaceAll(":x_emp_id", strEmps.toString()); //sqlStmt =
			 * sqlStmt.replaceAll(":x_from_year",
			 * Integer.toString(fromPP.getYear())); sqlStmt =
			 * sqlStmt.replaceAll(":x_from_pp_dt", new
			 * SimpleDateFormat("yyyyMMdd").format(fromPP.getFromDate()));
			 * //sqlStmt = sqlStmt.replaceAll(":x_to_year",
			 * Integer.toString(toPP.getYear())); sqlStmt =
			 * sqlStmt.replaceAll(":x_to_pp_dt", new
			 * SimpleDateFormat("yyyyMMdd").format(toPP.getToDate()));
			 *************************************************************************************************************/

			String primaryLeaveTypes 	= strPriLeaveTypes.toString();
			String secondaryLeaveTypes 	= strSecLeaveTypes.toString();
			String employeeIds 			= strEmps.toString();
			String fromPayPeriodDate 	= new SimpleDateFormat("yyyyMMdd").format(fromPP.getFromDate());
			String toPayPeriodDate 		= new SimpleDateFormat("yyyyMMdd").format(toPP.getToDate());

			sqlStmt = sqlStmt.replaceAll(":x_pri_leave_type", primaryLeaveTypes);
			sqlStmt = sqlStmt.replaceAll(":x_sec_leave_type", secondaryLeaveTypes);
			sqlStmt = sqlStmt.replaceAll(":x_emp_id", employeeIds);
			sqlStmt = sqlStmt.replaceAll(":x_from_pp_dt", fromPayPeriodDate);
			sqlStmt = sqlStmt.replaceAll(":x_to_pp_dt", toPayPeriodDate);

			// System.out.println(sqlStmt);
			ps = conn.prepareStatement(sqlStmt);
			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				LeaveRequestReconciliation lrr = new LeaveRequestReconciliation();

				// lrr.setAreaCd(rs.getString("areaCd"));
				// lrr.setAreaDesc(rs.getString("areaDesc"));
				lrr.setEmpFName(rs.getString("first_name"));
				lrr.setEmpId(rs.getInt("eeId"));
				lrr.setEmpLName(rs.getString("last_name"));
				lrr.setEmpMName(rs.getString("middle_name"));

				// lrr.setFacilityCd(rs.getString("facilityCd"));
				// lrr.setFacilityDesc(rs.getString("facilityDesc"));
				lrr.setLeaveDt(rs.getDate("leaveDt"));
				lrr.setLeaveHrsEtams(rs.getFloat("leaveHrsEtams"));
				lrr.setLeaveHrsAloha(rs.getFloat("leaveHrsAloha"));
				lrr.setLeaveType(rs.getString("leaveType"));
				lrr.setPayPeriod(rs.getInt("PP"));
				// lrr.setTeamCd(rs.getString("teamCd"));
				// lrr.setTeamDesc(rs.getString("teamDesc"));
				lrr.setYear(rs.getInt("YYYY"));
				// lrr.setLrDetailId(rs.getString("lrdetailid"));
				lrr.setLrHeaderId(rs.getString("lrheaderid"));
				lrr.setFATCode(rs.getString("FATCode"));
				rptLR.add(lrr);
			}
			
			this.writeToEventLog(alohaUser, primaryLeaveTypes, secondaryLeaveTypes, employeeIds, fromPayPeriodDate, toPayPeriodDate);
			
		} catch (SQLException e) {
			e.printStackTrace();
			throw new AlohaServerException("SQLException encountered in "
					+ this.getClass().getName() + ".runRpt(): "
					+ e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			throw new AlohaServerException("Exception encountered in "
					+ this.getClass().getName() + ".runRpt(): "
					+ e.getMessage());
		} finally {
			SqlUtil.closePreparedStatement(ps);
			SqlUtil.closeConnection(conn);
			ps = null;
			conn = null;
		}
		return rptLR;
	}

	private void writeToEventLog(AlohaUser alohaUser, String primaryLeaveTypes,
			String secondaryLeaveTypes, String employeeIds,
			String fromPayPeriodDate, String toPayPeriodDate)
			throws AlohaServerException {

		EventLog eventLog = new EventLog();

		EventType eventType = this.eventLoggerEJB
				.getEventType(EventType.EventTypeValue.LR_RECON.toString());
		eventLog.setEventType(eventType);
		eventLog.setUserCreated(alohaUser.getLoginName());

		StringBuilder sb = new StringBuilder();
		sb.append("loginName=");
		sb.append(alohaUser.getLoginName());
		sb.append(";userId=");
		sb.append(alohaUser.getUserId());
		sb.append(";primaryLeaveTypes=");
		sb.append(primaryLeaveTypes);
		sb.append(";secondaryLeaveTypes=");
		sb.append(secondaryLeaveTypes);
		sb.append(";employeeIds=");
		sb.append(employeeIds);
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