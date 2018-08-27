package gov.gsa.ocfo.aloha.ejb.leave.impl;

import gov.gsa.ocfo.aloha.ejb.EventLoggerEJB;
import gov.gsa.ocfo.aloha.ejb.leave.LRVarianceReportEJB;
import gov.gsa.ocfo.aloha.exception.AlohaServerException;
import gov.gsa.ocfo.aloha.model.LeaveRequestVariance;
import gov.gsa.ocfo.aloha.model.entity.AlohaUser;
import gov.gsa.ocfo.aloha.model.entity.audit.EventLog;
import gov.gsa.ocfo.aloha.model.entity.audit.EventType;
import gov.gsa.ocfo.aloha.util.SqlUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.sql.DataSource;

@Stateless
public class LRVarianceReportEJBImpl implements LRVarianceReportEJB {

	@Resource(name = "aloha-ds")
	private DataSource dataSource;
	
	@EJB
	EventLoggerEJB eventLoggerEJB;

	@Override
	public List<LeaveRequestVariance> runReport(AlohaUser alohaUser, String[] facilityIds, Date fromDate,
			Date toDate) throws AlohaServerException {
		
		List<LeaveRequestVariance> lrVarianceList = new ArrayList<LeaveRequestVariance>();

		Connection conn = null;
		PreparedStatement ps = null;
		
		try {
		
			conn = dataSource.getConnection();
			
			String sql = SqlUtil.LR_VARIANCE_REPORT_SQL;
			
			/********************************************
			* BEGIN: BUILD "IN" CLAUSE FOR FACILITY IDS
			*********************************************/
			StringBuilder sbFacilities = new StringBuilder();
			int count = 0;
			for (String facilityId : facilityIds) {
				if (count > 0 ) {
					sbFacilities.append(",");
				}
				sbFacilities.append(facilityId);
				count++;
			}
			sql = sql.replaceFirst(":x_facility_id_list", sbFacilities.toString());
			/********************************************
			* END: BUILD "IN" CLAUSE FOR FACILITY IDS
			*********************************************/

			sql = sql.replaceFirst(":x_from_date", new SimpleDateFormat("yyyyMMdd").format(fromDate));
			sql = sql.replaceFirst(":x_to_date", new SimpleDateFormat("yyyyMMdd").format(toDate));
			
			//System.out.println("sql = " + sql);

			ps = conn.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				
				float alohaLeaveHours = rs.getFloat("lv_hrs_aloha");
				float etamsLeaveHours = rs.getFloat("lv_hrs_etams");
				
				if ( alohaLeaveHours != etamsLeaveHours) {

					LeaveRequestVariance lrVariance = new LeaveRequestVariance();
					
					lrVariance.setFacilityId(rs.getLong("facility_id"));
					lrVariance.setLeaveDt(rs.getDate("lv_date"));
					lrVariance.setEmpId(rs.getInt("user_id"));
					lrVariance.setEmpLName(rs.getString("last_name"));
					lrVariance.setEmpFName(rs.getString("first_name"));
					lrVariance.setEmpMName(rs.getString("middle_name"));
					lrVariance.setFATCode(rs.getString("fat_code"));
					lrVariance.setOrgLocCode(rs.getString("org_loc"));
					lrVariance.setYear(rs.getInt("lv_year"));
					lrVariance.setPayPeriod(rs.getInt("pp_no"));
					lrVariance.setLrHeaderId(rs.getString("lr_header_id"));
					lrVariance.setLeaveType(rs.getString("lv_type"));
					lrVariance.setLeaveHrsAloha(alohaLeaveHours);
					lrVariance.setLeaveHrsEtams(etamsLeaveHours);
					
					lrVarianceList.add(lrVariance);					
				}
			}
			
			this.writeToEventLog(alohaUser, fromDate, toDate, sbFacilities.toString());
			
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new AlohaServerException(sqle);
		} catch (Exception e) {
			e.printStackTrace();
			throw new AlohaServerException(e);
		} finally {
			SqlUtil.closePreparedStatement(ps);
			SqlUtil.closeConnection(conn);
			ps = null;
			conn = null;
		}		
		
		return lrVarianceList;
	}
	
	public void writeToEventLog (AlohaUser alohaUser, Date fromDate, 
			Date toDate, String facilityIds) throws AlohaServerException {
		
		EventLog eventLog = new EventLog();
			
		EventType eventType = this.eventLoggerEJB.getEventType(EventType.EventTypeValue.LR_VARIANCE_REPORT.toString()); 
		eventLog.setEventType(eventType);
		eventLog.setUserCreated(alohaUser.getLoginName());	
		
		StringBuilder sb = new StringBuilder();
		sb.append("loginName=");
		sb.append(alohaUser.getLoginName());
		sb.append(";userId=");
		sb.append(alohaUser.getUserId());
		sb.append(";fromDate=");
		sb.append(new SimpleDateFormat("yyyyMMdd").format(fromDate));
		sb.append(";toDate=");
		sb.append(new SimpleDateFormat("yyyyMMdd").format(toDate));
		sb.append(";facilityIDs=");
		sb.append(facilityIds);
	
		String newValue = sb.toString();
		if ( newValue.length() > 4000) {
			newValue = newValue.substring(0, 4000);	
		}
		eventLog.setNewValue(newValue);
			
		this.eventLoggerEJB.logEvent(eventLog);
	}		
}