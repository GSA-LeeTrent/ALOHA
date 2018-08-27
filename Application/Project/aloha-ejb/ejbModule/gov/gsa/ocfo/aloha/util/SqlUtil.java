package gov.gsa.ocfo.aloha.util;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class SqlUtil {
	// SQL Files
	private static final String GET_ALL_PAY_PERIODS_SQL_FILE 		= "GetAllPayPeriods.sql";	
	private static final String GET_LIMITED_PAY_PERIODS_SQL_FILE 	= "GetLimitedPayPeriods.sql";	
	private static final String GET_PAY_PERIODS_FOR_YEAR_SQL_FILE 	= "GetPayPeriodsForYear.sql";	
	private static final String GET_PAY_PERIOD_YEARS_SQL_FILE		= "GetPayPeriodYears.sql";	
	private static final String GET_USER_SQL_FILE 					= "GetUser.sql";
	private static final String GET_USER_BY_ID_SQL_FILE 			= "GetUserByUserid.sql";
	private static final String GET_ROLES_SQL_FILE 					= "GetRoles.sql";
	private static final String GET_LEAVE_APPROVERS_SQL_FILE 		= "GetLeaveApprovers.sql";
	private static final String GET_PRIM_TIMEKEEPERS_SQL_FILE 		= "GetPrimTimekeepers.sql";
	private static final String GET_PAY_PERIOD_SCHEDULE_SQL_FILE 	= "GetPayPeriodSchedule.sql";
	private static final String GET_LEAVE_BALANCES_SQL_FILE 		= "GetLeaveBalances.sql";
	private static final String GET_LEAVE_TYPES_SQL_FILE 			= "GetLeaveTypes.sql";
	private static final String RUN_LR_RECON_SQL_FILE				= "RunReconLR.sql";
	private static final String RUN_OT_RECON_SQL_FILE				= "RunReconOT.sql";
	private static final String GET_TEAMS_MANAGED_BY_USER_SQL_FILE	= "GetTeamsManagedByUser.sql";
	private static final String GET_ON_BEHALF_OF_EMPLOYEES_SQL_FILE	= "GetOnBehalfOfEmployees.sql";
	private static final String GET_LR_REPORT_SQL_FILE				= "GetLeaveRptByEmployee.sql";
	private static final String GET_OT_REPORT_SQL_FILE				= "GetOTRptByEmployee.sql";
	private static final String GET_TEAM_APPROVER_SQL_FILE			= "GetApproversForTeam.sql";
	private static final String GET_MANAGED_STAFF_SQL_FILE			= "GetManagedStaffForUser.sql";
	private static final String GET_ETAMS_AD_XREF_SQL_FILE			= "GetETAMSADXref.sql";
	private static final String UPDATE_ETAMS_AD_XREF_SQL_FILE		= "UpdateEtamsAdXref.sql";
	private static final String UPDATE_USER_DEMOGRAPHICS_SQL_FILE		= "UpdateUserDemographics.sql";
	private static final String GET_EMAIL_ADDR_BY_UID_SQL_FILE		= "GetEmailAddressByUserId.sql";
	private static final String INSERT_ETAMS_AD_XREF_SQL_FILE		= "InsertEtamsAdXref.sql";
	private static final String GET_ETAMS_GOVT_USER_BY_NAME_SQL_FILE		= "GetEtamsGovtUserByName.sql";
	private static final String GET_ETAMS_USER_BY_NAME_SQL_FILE		= "GetEtamsUserByName.sql";
	private static final String DELETE_ETAMS_AD_XREF_SQL_FILE		= "DeleteEtamsAdXref.sql";
	private static final String GET_LR_RECON_RPT_SQL_FILE			= "GetReconRpt.sql";
	private static final String GET_SALARY_GRADE_KEY_SQL_FILE		= "GetSalaryGradeKey.sql";
	private static final String GET_OT_BALANCES_SQL_FILE 			= "GetOTBalances.sql";
	private static final String GET_AWS_FLAG_FOR_EMPLOYEE_SQL_FILE 	= "GetAwsFlagForEmployee.sql";
	private static final String GET_LR_LIST_FOR_SUBMIT_OWN_SQL_FILE 	= "GetLRListForSubmitOwn.sql";
	private static final String GET_LR_LIST_FOR_ON_BEHALF_OF_SQL_FILE	= "GetLRListForOnBehalfOf.sql";
	private static final String GET_LR_LIST_FOR_APPROVER_SQL_FILE 		= "GetLRListForApprover.sql";	

	//////////////////////////////////////////////////////////////////////////////////////////////////////////
	//	OT APPROVAL WORKFLOW SQL FILES
	private static final String GET_OT_INDIV_LIST_FOR_SUPERVISOR_SQL_FILE 	= "GetOTIndivListForSupervisor.sql";
	private static final String GET_OT_INDIV_LIST_FOR_SUBMIT_OWN_SQL_FILE 	= "GetOTIndivListForSubmitOwn.sql";
	private static final String GET_OT_INDIV_LIST_FOR_ON_BEHALF_OF_SQL_FILE	= "GetOTIndivListForOnBehalfOf.sql";
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// LEAVE REQUEST VARIANCE REPORT SQL
	private static final String FIND_FACILITIES_FOR_FACILITY_COORDINATOR_SQL_FILE = "FindFacilitiesForFacilityCoordinator.sql";
	private static final String LR_VARIANCE_REPORT_SQL_FILE	= "LRVarianceReport.sql";
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// LEAVE REQUEST VARIANCE COUNT SQL FILE
	private static final String LR_VARIANCE_COUNT_SQL_FILE = "LRVarianceCount.sql";
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// LEAVE RECON WIZARD COUNT SQL FILE
	private static final String LEAVE_RECON_WIZARD_COUNT_SQL_FILE = "LeaveReconWizardCount.sql";
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// DATE TABLE  SQL FILES
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	private static final String DATE_TABLE_ROW_FOR_TODAY_SQL_FILE 				= "DateTableRowForToday.sql";
	private static final String MAX_PAY_PERIOD_IN_DATE_TABLE_FOR_YEAR_SQL_FILE 	= "MaxPayPeriodInDateTableForYear.sql";
	private static final String DATE_TABLE_ROW_FOR_YEAR_PAY_PERIOD_DAY_SQL_FILE = "DateTableRowForYearPayPeriodDay.sql";
	private static final String DATE_TABLE_ROW_FOR_DATE_SQL_FILE 				= "DateTableRowForDate.sql";
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// LR_RECON_WIZARD_PENDING SQL FILE
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	private static final String DELETE_LR_RECON_WIZARD_PENDING_SQL_FILE = "DeleteLRReconWizardPending.sql";
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// RETRIEVE LEAVE ITEMS TO SAFEGUARD AGAINST EXCESSIVE LEAVE HOURS TAKEN IN ONE DAY OR OVER THE COURSE OF A SINGLE PAY PERIOD
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	private static final String PRIOR_LEAVE_ITEMS_FOR_CREATE_SQL_FILE = "PriorLeaveItemsForCreate.sql";	
	private static final String PRIOR_LEAVE_ITEMS_FOR_AMEND_SQL_FILE = "PriorLeaveItemsForAmend.sql";
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////	
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// RETRIEVE LEAVE BALANCE INFO FROM FEDDESK'S PAR_EDS_LV TABLE FOR DISABLED VETERANS (LV_CD = 69)
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	private static final String GET_DISABLED_VET_LEAVE_INFO_SQL_FILE = "GetDisabledVetLeaveInfo.sql";	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////		
	
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// SQL Strings
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public static final String GET_ALL_PAY_PERIODS_SQL 		= readSqlFile(GET_ALL_PAY_PERIODS_SQL_FILE);
	public static final String GET_LIMITED_PAY_PERIODS_SQL 	= readSqlFile(GET_LIMITED_PAY_PERIODS_SQL_FILE);
	public static final String GET_PAY_PERIODS_FOR_YEAR_SQL = readSqlFile(GET_PAY_PERIODS_FOR_YEAR_SQL_FILE);
	public static final String GET_PAY_PERIODS_YEARS_SQL 	= readSqlFile(GET_PAY_PERIOD_YEARS_SQL_FILE);		
	public static final String GET_USER_SQL 				= readSqlFile(GET_USER_SQL_FILE);
	public static final String GET_USER_BY_ID_SQL 			= readSqlFile(GET_USER_BY_ID_SQL_FILE);
	public static final String GET_ROLES_SQL 				= readSqlFile(GET_ROLES_SQL_FILE);	
	public static final String GET_LEAVE_APPROVERS_SQL 		= readSqlFile(GET_LEAVE_APPROVERS_SQL_FILE);
	public static final String GET_PRIM_TIMEKEEPERS_SQL 	= readSqlFile(GET_PRIM_TIMEKEEPERS_SQL_FILE);
	public static final String GET_PAY_PERIOD_SCHEDULE_SQL 	= readSqlFile(GET_PAY_PERIOD_SCHEDULE_SQL_FILE);
	public static final String GET_LEAVE_BALANCES 			= readSqlFile(GET_LEAVE_BALANCES_SQL_FILE);	
	public static final String GET_LEAVE_TYPES_SQL 			= readSqlFile(GET_LEAVE_TYPES_SQL_FILE);	
	public static final String RUN_RECON_SQL				= readSqlFile(RUN_LR_RECON_SQL_FILE);
	public static final String RUN_OT_RECON_SQL				= readSqlFile(RUN_OT_RECON_SQL_FILE);
	public static final String GET_TEAMS_MANAGED_BY_USER_SQL	= readSqlFile(GET_TEAMS_MANAGED_BY_USER_SQL_FILE);	
	public static final String GET_ON_BEHALF_OF_EMPLOYEES_SQL	= readSqlFile(GET_ON_BEHALF_OF_EMPLOYEES_SQL_FILE);
	public static final String GET_LR_REPORT_SQL				= readSqlFile(GET_LR_REPORT_SQL_FILE);
	public static final String GET_OT_REPORT_SQL				= readSqlFile(GET_OT_REPORT_SQL_FILE);
	public static final String GET_APPROVER_FOR_TEAM_SQL		= readSqlFile(GET_TEAM_APPROVER_SQL_FILE);
	public static final String GET_MANAGED_STAFF_SQL			= readSqlFile(GET_MANAGED_STAFF_SQL_FILE);
	public static final String GET_ETAMS_AD_XREF_SQL			= readSqlFile(GET_ETAMS_AD_XREF_SQL_FILE);
	public static final String UPDATE_ETAMS_AD_XREF_SQL			= readSqlFile(UPDATE_ETAMS_AD_XREF_SQL_FILE);
	public static final String UPDATE_USER_DEMOGRAPHICS_SQL		= readSqlFile(UPDATE_USER_DEMOGRAPHICS_SQL_FILE);
	public static final String GET_EMAIL_ADDR_BY_UID_SQL		= readSqlFile(GET_EMAIL_ADDR_BY_UID_SQL_FILE);
	public static final String INSERT_ETAMS_AD_XREF_SQL			= readSqlFile(INSERT_ETAMS_AD_XREF_SQL_FILE);
	public static final String GET_ETAMS_GOVT_USER_BY_NAME_SQL	= readSqlFile(GET_ETAMS_GOVT_USER_BY_NAME_SQL_FILE);
	public static final String GET_ETAMS_USER_BY_NAME_SQL		= readSqlFile(GET_ETAMS_USER_BY_NAME_SQL_FILE);
	public static final String DELETE_ETAMS_AD_XREF_SQL			= readSqlFile(DELETE_ETAMS_AD_XREF_SQL_FILE);
	public static final String GET_LR_RECON_RPT_SQL				= readSqlFile(GET_LR_RECON_RPT_SQL_FILE);
	public static final String GET_SALARY_GRADE_KEY_SQL 		= readSqlFile(GET_SALARY_GRADE_KEY_SQL_FILE);
	public static final String GET_OT_BALANCES 					= readSqlFile(GET_OT_BALANCES_SQL_FILE);
	public static final String GET_AWS_FLAG_FOR_EMPLOYEE_SQL 	= readSqlFile(GET_AWS_FLAG_FOR_EMPLOYEE_SQL_FILE);
	public static final String GET_LR_LIST_FOR_SUBMIT_OWN_SQL 	= readSqlFile(GET_LR_LIST_FOR_SUBMIT_OWN_SQL_FILE);
	public static final String GET_LR_LIST_FOR_ON_BEHALF_OF_SQL	= readSqlFile(GET_LR_LIST_FOR_ON_BEHALF_OF_SQL_FILE);
	public static final String GET_LR_LIST_FOR_APPROVER_SQL 	= readSqlFile(GET_LR_LIST_FOR_APPROVER_SQL_FILE);
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//	OT APPROVAL WORKFLOW SQL
	public static final String GET_OT_INDIV_LIST_FOR_SUPERVISOR_SQL = readSqlFile(GET_OT_INDIV_LIST_FOR_SUPERVISOR_SQL_FILE);
	public static final String GET_OT_INDIV_LIST_FOR_SUBMIT_OWN_SQL 	= readSqlFile(GET_OT_INDIV_LIST_FOR_SUBMIT_OWN_SQL_FILE);
	public static final String GET_OT_INDIV_LIST_FOR_ON_BEHALF_OF_SQL	= readSqlFile(GET_OT_INDIV_LIST_FOR_ON_BEHALF_OF_SQL_FILE);
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// LEAVE REQUEST VARIANCE REPORT SQL
	public static final String FIND_FACILITIES_FOR_FACILITY_COORDINATOR_SQL = readSqlFile(FIND_FACILITIES_FOR_FACILITY_COORDINATOR_SQL_FILE);
	public static final String LR_VARIANCE_REPORT_SQL = readSqlFile(LR_VARIANCE_REPORT_SQL_FILE);	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// LEAVE REQUEST VARIANCE COUNT SQL
	public static final String LR_VARIANCE_COUNT_SQL = readSqlFile(LR_VARIANCE_COUNT_SQL_FILE);	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////	
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// LEAVE RECON WIZARD COUNT SQL
	public static final String LEAVE_RECON_WIZARD_COUNT_SQL = readSqlFile(LEAVE_RECON_WIZARD_COUNT_SQL_FILE);	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////		
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// DATE TABLE  SQL 
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public static final String DATE_TABLE_ROW_FOR_TODAY_SQL					= readSqlFile(DATE_TABLE_ROW_FOR_TODAY_SQL_FILE);	
	public static final String MAX_PAY_PERIOD_IN_DATE_TABLE_FOR_YEAR_SQL	= readSqlFile(MAX_PAY_PERIOD_IN_DATE_TABLE_FOR_YEAR_SQL_FILE);
	public static final String DATE_TABLE_ROW_FOR_YEAR_PAY_PERIOD_DAY_SQl	= readSqlFile(DATE_TABLE_ROW_FOR_YEAR_PAY_PERIOD_DAY_SQL_FILE);
	public static final String DATE_TABLE_ROW_FOR_DATE_SQL					= readSqlFile(DATE_TABLE_ROW_FOR_DATE_SQL_FILE);
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// LR_RECON_WIZARD_PENDING SQL FILE
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public static final String DELETE_LR_RECON_WIZARD_PENDING_SQl = readSqlFile(DELETE_LR_RECON_WIZARD_PENDING_SQL_FILE);
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////	
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// RETRIEVE LEAVE ITEMS TO SAFEGUARD AGAINST EXCESSIVE LEAVE HOURS TAKEN IN ONE DAY OR OVER THE COURSE OF A SINGLE PAY PERIOD
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public static final String PRIOR_LEAVE_ITEMS_FOR_CREATE_SQL = readSqlFile(PRIOR_LEAVE_ITEMS_FOR_CREATE_SQL_FILE);	
	public static final String PRIOR_LEAVE_ITEMS_FOR_AMEND_SQL 	= readSqlFile(PRIOR_LEAVE_ITEMS_FOR_AMEND_SQL_FILE);
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////	

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// RETRIEVE LEAVE BALANCE INFO FROM FEDDESK'S PAR_EDS_LV TABLE FOR DISABLED VETERANS (LV_CD = 69)
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////	
	public static final String GET_DISABLED_VET_LEAVE_INFO_SQL 	= readSqlFile(GET_DISABLED_VET_LEAVE_INFO_SQL_FILE);
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////		
		
	private static String readSqlFile(String fileName) {
		String contents = null;
		try {
			contents = FileUtil.readTextFile(fileName);
		} catch (IOException e) {
			e.printStackTrace();
		}	
		return contents;
	}
	public static void closePreparedStatement(PreparedStatement ps) {
		if (ps != null) {
			try {
				ps.close();
			} catch (SQLException sqle) {
				sqle.printStackTrace();
			}
		}
	}
	public static void closeStatement(Statement stmt) {
		if (stmt != null) {
			try {
				stmt.close();
			} catch (SQLException sqle) {
				sqle.printStackTrace();
			}
		}
	}
		
	public static void closeConnection(Connection conn) {
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException sqle) {
				sqle.printStackTrace();
			}
		}
	}
}