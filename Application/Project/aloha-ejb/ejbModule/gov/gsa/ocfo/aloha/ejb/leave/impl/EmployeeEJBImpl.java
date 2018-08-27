package gov.gsa.ocfo.aloha.ejb.leave.impl;

import gov.gsa.ocfo.aloha.ejb.leave.EmployeeEJB;
import gov.gsa.ocfo.aloha.exception.AlohaServerException;
import gov.gsa.ocfo.aloha.model.KeyValuePair;
import gov.gsa.ocfo.aloha.model.PayPeriodSchedule;
import gov.gsa.ocfo.aloha.model.ScheduleItem;
import gov.gsa.ocfo.aloha.model.entity.AlohaUser;
import gov.gsa.ocfo.aloha.model.leave.DisabledVetLeaveInfo;
import gov.gsa.ocfo.aloha.util.DayUtil;
import gov.gsa.ocfo.aloha.util.SqlUtil;
import gov.gsa.ocfo.aloha.util.StringUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.sql.DataSource;

@Stateless
public class EmployeeEJBImpl implements EmployeeEJB {
	@Resource(name = "aloha-ds")
	private DataSource dataSource;
	
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public List<AlohaUser> getLeaveApprovers(long employeeId) throws AlohaServerException {
		List<AlohaUser> approvers = new ArrayList<AlohaUser>();
		
		Connection conn = null;
		PreparedStatement ps = null;
		
		try {
			conn = dataSource.getConnection();
			ps = conn.prepareStatement(SqlUtil.GET_LEAVE_APPROVERS_SQL);
			
			ps.setLong(1, employeeId);
			ps.setLong(2, employeeId);
			ps.setLong(3, employeeId);			
			
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				AlohaUser approver = new AlohaUser();
				approver.setUserId(rs.getLong("user_id"));
				approver.setFirstName(rs.getString("first_name"));
				approver.setMiddleName(rs.getString("middle_name"));
				approver.setLastName(rs.getString("last_name"));
				if ( (!StringUtil.isNullOrEmpty(approver.getFirstName())) && (!StringUtil.isNullOrEmpty(approver.getLastName()))) {
					approver.setFullName(approver.getFirstName() + " " + (approver.getMiddleName()== null ? "" : approver.getMiddleName().substring(0,1).toUpperCase())+
							  " " + approver.getLastName());
				}				
				approver.setEmailAddress(rs.getString("email_address"));
				approvers.add(approver);
			}
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
		return approvers;
	}
	
	
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public List<AlohaUser> getPrimTimekeeper(long employeeId) throws AlohaServerException {
		List<AlohaUser> timekeepers = new ArrayList<AlohaUser>();
		
		Connection conn = null;
		PreparedStatement ps = null;
		
		try {
			conn = dataSource.getConnection();
			ps = conn.prepareStatement(SqlUtil.GET_PRIM_TIMEKEEPERS_SQL);
			
			ps.setLong(1, employeeId);
			ps.setLong(2, employeeId);
			ps.setLong(3, employeeId);			
			
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				AlohaUser timekeeper = new AlohaUser();
				timekeeper.setUserId(rs.getLong("user_id"));
				timekeeper.setFirstName(rs.getString("first_name"));
				timekeeper.setMiddleName(rs.getString("middle_name"));
				timekeeper.setLastName(rs.getString("last_name"));
				if ( (!StringUtil.isNullOrEmpty(timekeeper.getFirstName())) && (!StringUtil.isNullOrEmpty(timekeeper.getLastName()))) {
					timekeeper.setFullName(timekeeper.getFirstName() + " " + (timekeeper.getMiddleName()== null ? "" : timekeeper.getMiddleName().substring(0,1).toUpperCase())+
							" " + timekeeper.getLastName());
				}				
				timekeeper.setEmailAddress(rs.getString("email_address"));
				timekeepers.add(timekeeper);
			}
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
		return timekeepers;
	}

	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public List<ScheduleItem> getPayPeriodSchedule(long employeeId, String payPeriodStartDate) throws AlohaServerException {
		List<ScheduleItem> scheduleItems = new ArrayList<ScheduleItem>();
		
		Connection conn = null;
		PreparedStatement ps = null;
		
		try {
			conn = dataSource.getConnection();
			ps = conn.prepareStatement(SqlUtil.GET_PAY_PERIOD_SCHEDULE_SQL);
			ps.setLong(1, employeeId);
			ps.setString(2, payPeriodStartDate);
			ps.setString(3, payPeriodStartDate);			
			
			ResultSet rs = ps.executeQuery();
		
			int sequence = 1;
			while (rs.next()) {
				ScheduleItem scheduleItem = new ScheduleItem();
				scheduleItem.setSequence(sequence++);
				scheduleItem.setDuplicate(false);
				scheduleItem.setCalendarDate(rs.getDate("calendar_date"));

				if ( !StringUtil.isNullOrEmpty(rs.getString("day_of_week"))) {
					String dayOfWeek = rs.getString("day_of_week").trim();
					if (dayOfWeek.length() > 1) {
						StringBuilder sb = new StringBuilder(dayOfWeek.substring(0,1));
						sb.append(dayOfWeek.substring(1).toLowerCase());
						scheduleItem.setDayOfWeek(sb.toString());						
					}
				}
				scheduleItem.setDayOfWeekAbbrv(DayUtil.getAbbreviation(scheduleItem.getDayOfWeek()));
				scheduleItem.setHoursScheduled(rs.getInt("hours_scheduled"));
				Integer holidayIndicator = rs.getInt("holiday_ind");
				if ( (holidayIndicator != null) && (holidayIndicator.intValue() == 1) ) {
					scheduleItem.setHoliday(true);
					scheduleItem.setHolidayDesc(rs.getString("holiday_desc"));
				}
				scheduleItems.add(scheduleItem);
				
				// ADD DUPLICATE TO FACILIATE "2 LEAVE TYPES PER DAY"

				ScheduleItem duplicateScheduleItem = new ScheduleItem();
				duplicateScheduleItem.setSequence(sequence++);
				duplicateScheduleItem.setDuplicate(true);
				duplicateScheduleItem.setCalendarDate(rs.getDate("calendar_date"));
				duplicateScheduleItem.setDayOfWeek(rs.getString("day_of_week").trim());
				duplicateScheduleItem.setDayOfWeekAbbrv(DayUtil.getAbbreviation(scheduleItem.getDayOfWeek()));
				duplicateScheduleItem.setHoursScheduled(rs.getInt("hours_scheduled"));
				if ( (holidayIndicator != null) && (holidayIndicator.intValue() == 1) ) {
					duplicateScheduleItem.setHoliday(true);
					duplicateScheduleItem.setHolidayDesc(rs.getString("holiday_desc"));
				}
				scheduleItems.add(duplicateScheduleItem);
			}
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
		return scheduleItems;		
	}

	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public List<KeyValuePair> getLeaveBalances(long employeeId) throws AlohaServerException {
		List<KeyValuePair> list = new ArrayList<KeyValuePair>();
		
		Connection conn = null;
		PreparedStatement ps = null;		
		
		try {
			conn = dataSource.getConnection();
			ps = conn.prepareStatement(SqlUtil.GET_LEAVE_BALANCES);
			
			/////////////////////////////////////////////////			
			// There are 17 query parameter in the SQL ('&')
			/////////////////////////////////////////////////
			for ( int ii = 1; ii <= 17; ii++) {
				ps.setLong(ii, employeeId);
			}
			ResultSet rs = ps.executeQuery();
			while ( rs.next()) {
				KeyValuePair model = new KeyValuePair();
				
				// LEAVE TYPE
				model.setKey(rs.getString("LeaveType"));

				// LEAVE BALANCE
				String leaveBalance = rs.getString("LeaveBalance");
				if ( !StringUtil.isNullOrEmpty(leaveBalance)) {
					BigDecimal bd = new BigDecimal(leaveBalance);
					bd.setScale(1, RoundingMode.HALF_UP);
					NumberFormat nf = NumberFormat.getNumberInstance();
					model.setValue(nf.format(bd.doubleValue()));				
				}
				list.add(model);
			}
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
		return list;
	}
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public List<AlohaUser> getOnBehalfOfEmployees(long employeeId) throws AlohaServerException {
		return this.getManagedStaffForUser(employeeId);
	}

	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public List<AlohaUser> getManagedStaffForUser(long employeeId) throws AlohaServerException {
		List<AlohaUser> managedStaffList = new ArrayList<AlohaUser>();
		
		Connection conn = null;
		PreparedStatement ps = null;		
		
		try {
			conn = dataSource.getConnection();
			ps = conn.prepareStatement(SqlUtil.GET_MANAGED_STAFF_SQL);
			
			ps.setLong(1, employeeId);
			ps.setLong(2, employeeId);
			ps.setLong(3, employeeId);
			
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				AlohaUser managedStaffMember = new AlohaUser();
				managedStaffMember.setLoginName(rs.getString("login_name"));
				managedStaffMember.setTeamId(rs.getLong("member_of_team"));
				managedStaffMember.setUserId(rs.getLong("user_id"));
				managedStaffMember.setFirstName(rs.getString("first_name"));
				managedStaffMember.setMiddleName(rs.getString("middle_name"));
				managedStaffMember.setLastName(rs.getString("last_name"));
				if ( (!StringUtil.isNullOrEmpty(managedStaffMember.getFirstName())) && (!StringUtil.isNullOrEmpty(managedStaffMember.getLastName()))) {
					managedStaffMember.setFullName(managedStaffMember.getFirstName() + " " + (managedStaffMember.getMiddleName()== null ? "" : managedStaffMember.getMiddleName().substring(0,1).toUpperCase())+
					   " "+ managedStaffMember.getLastName());
				}				
				managedStaffMember.setEmailAddress(rs.getString("email_address"));
				
				String awsInd = rs.getString("aws_ind");
				if ( (awsInd != null) && (awsInd.equals("Y")) ) {
					managedStaffMember.setAws(true);
				} else {
					managedStaffMember.setAws(false);
				}
				managedStaffList.add(managedStaffMember);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new AlohaServerException(e);
		} catch (Exception e) {
			e.printStackTrace();
			throw new AlohaServerException(e);
		} finally {
			SqlUtil.closePreparedStatement(ps);
			SqlUtil.closeConnection(conn);
			ps = null;
			conn = null;
		}				
		return managedStaffList;		
	}	
	
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public boolean getAwsFlagForEmployee(long employeeUserId) throws AlohaServerException {
		boolean awsFlag = false;

		Connection conn = null;
		PreparedStatement ps = null;
		
		try {
			conn = dataSource.getConnection();
			ps = conn.prepareStatement(SqlUtil.GET_AWS_FLAG_FOR_EMPLOYEE_SQL);
			
			ps.setLong(1, employeeUserId);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				String awsInd = rs.getString("aws_ind");
				if ( (awsInd != null) && (awsInd.equals("Y")) ) {
					awsFlag = true;
				} 
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new AlohaServerException(sqle);
		} finally {
			SqlUtil.closePreparedStatement(ps);
			SqlUtil.closeConnection(conn);
			ps = null;
			conn = null;
		}			
		return awsFlag;
	}
	
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public List<PayPeriodSchedule> retrievePayPeriodScheduleForEmployeeAndPayPeriodStartDate
		(long employeeId, String payPeriodStartDate) throws AlohaServerException {
		
		List<PayPeriodSchedule> payPeriodScheduleList = new ArrayList<PayPeriodSchedule>();
		Connection conn = null;
		PreparedStatement ps = null;
		
		try {
			conn = dataSource.getConnection();
			ps = conn.prepareStatement(SqlUtil.GET_PAY_PERIOD_SCHEDULE_SQL);
			ps.setLong(1, employeeId);
			ps.setString(2, payPeriodStartDate);
			ps.setString(3, payPeriodStartDate);			
			
			ResultSet rs = ps.executeQuery();
		
			while (rs.next()) {
				PayPeriodSchedule payPeriodSchedule = new PayPeriodSchedule();
				payPeriodSchedule.setCalendarDate(rs.getDate("calendar_date"));

				if ( !StringUtil.isNullOrEmpty(rs.getString("day_of_week"))) {
					String dayOfWeek = rs.getString("day_of_week").trim();
					if (dayOfWeek.length() > 1) {
						StringBuilder sb = new StringBuilder(dayOfWeek.substring(0,1));
						sb.append(dayOfWeek.substring(1).toLowerCase());
						payPeriodSchedule.setDayOfWeek(sb.toString());						
					}
				}
				payPeriodSchedule.setHoursScheduled(rs.getInt("hours_scheduled"));
				Integer holidayIndicator = rs.getInt("holiday_ind");
				if ( (holidayIndicator != null) && (holidayIndicator.intValue() == 1) ) {
					payPeriodSchedule.setHolidayIndicator(1);
					payPeriodSchedule.setHolidayDescription(rs.getString("holiday_desc"));
				} else {
					payPeriodSchedule.setHolidayIndicator(0);
				}
				payPeriodScheduleList.add(payPeriodSchedule);
			}
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
		return payPeriodScheduleList;		
	}	
	
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public DisabledVetLeaveInfo getDisabledVetLeaveInfo(long employeeUserId) throws AlohaServerException {
		
		DisabledVetLeaveInfo disabledVetLeaveInfo = null;

		Connection conn = null;
		PreparedStatement ps = null;
		
		try {
			conn = dataSource.getConnection();
			ps = conn.prepareStatement(SqlUtil.GET_DISABLED_VET_LEAVE_INFO_SQL);
			
			ps.setLong(1, employeeUserId);
			ResultSet rs = ps.executeQuery();
			
			if ( rs.next() ) {
			
				Date expirationDate = rs.getDate("LV_USED_BEG_DATE");
				BigDecimal leaveBalance = rs.getBigDecimal("LV_HRS_BAL");
				
				disabledVetLeaveInfo = new DisabledVetLeaveInfo(expirationDate, leaveBalance);
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new AlohaServerException(sqle);
		} finally {
			SqlUtil.closePreparedStatement(ps);
			SqlUtil.closeConnection(conn);
			ps = null;
			conn = null;
		}			
		return disabledVetLeaveInfo;
	}	
}