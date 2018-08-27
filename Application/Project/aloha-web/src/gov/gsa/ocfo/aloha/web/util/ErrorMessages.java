package gov.gsa.ocfo.aloha.web.util;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;

public class ErrorMessages {

	/* The volatile keyword ensures that multiple threads 
	 * handle the unique instance variable correctly 
	 * when it is being initialized to the Singleton instance
	 ***********************************************************/ 
	private volatile static ErrorMessages uniqueInstance;
	/**********************************************************/
	
	private static final String BASE_NAME = "gov.gsa.ocfo.aloha.web.errors";
	public static final String PAY_PERIOD_REQUIRED = "payPeriodRequired";
	public static final String EMPLOYEE_REQUIRED = "employeeRequired";
	public static final String APPROVER_REQUIRED = "approverRequired";
	public static final String LEAVE_TYPE_FOR_ROW_REQUIRED = "leaveTypeForRowRequired";
	public static final String LEAVE_HOURS_FOR_ROW_REQUIRED = "leaveHoursForRowRequired";	
	public static final String ZERO_TOTAL_LEAVE_HOURS = "zeroTotalLeaveHours";
	public static final String UNSIGNED_LEAVE_REQUEST = "unsignedLeaveRequest";
	public static final String START_HOUR_REQUIRED = "startHourRequired";
	public static final String START_MINUTE_REQUIRED = "startMinuteRequired";
	public static final String START_MERIDIEM_REQUIRED= "startMeridiemRequired";
	//SAK added below for LR Recon
	public static final String TEAM_REQUIRED = "teamSelectionRequired";
	public static final String PPFROM_LE_PPTO = "payPeriodFromBeforOrEqualPayPeriodTo";	
	public static final String OT_RECON_NOTAUTH = "overtimeRequest.Recon.notauthmsg";
	public static final String OT_REPORT_NOTAUTH = "overtimeRequest.Report.notauthmsg";
	public static final String LR_RECON_NOTAUTH = "leaveRequest.Recon.notauthmsg";
	public static final String LR_REPORT_NOTAUTH = "leaveRequest.Report.notauthmsg";
	public static final String LOGIN_NAME_REQUIRED = "adXref.loginName.required";
	public static final String ETAMS_USERID_REQUIRED = "adXref.etamsUserId.required";
	public static final String EMAIL_ADDRESS_REQUIRED = "adXref.emailAddress.required";
	public static final String ADXREF_NOTAUTH = "adXref.notauth";
	
	public static final String SUBMIT_OWN_UNAUTHORIZED = "submitOwn.unauthorized";
	public static final String ON_BEHALF_OF_UNAUTHORIZED = "onBehalfOf.unauthorized";
	public static final String APPROVER_UNAUTHORIZED = "approver.unauthorized";
	public static final String LR_CREATE_UNAUTHORIZED = "lrCreate.unauthorized";
	public static final String LR_ALREADY_APPROVED_DENIED = "leaveRequest.alreadyApprovedOrDenied";
	public static final String LR_NEW_LR_ALREADY_SUBMITTED = "leaveRequest.newLeaveRequestAlreadySubmitted";
	public static final String LR_ALREADY_AMENDED = "leaveRequest.alreadyAmended";
	public static final String LR_ALREADY_CANCELED = "leaveRequest.alreadyCanceled";
	public static final String LR_ALREADY_PEND_WITHDRAW = "leaveRequest.alreadyPendWithdraw";
	public static final String LR_PEND_AMEND_ALREADY_SUBMITTED = "leaveRequest.pendAmendAlreadySubmitted";
	
	public static final String LR_AMEND_UNAUTHORIZED = "leaveRequest.amend.unauthorized";
	public static final String LR_AMEND_NO_CHANGE = "leaveRequest.amend.noChange";
	public static final String LR_APPROVE_INVALID_STATUS = "leaveRequest.approve.invalidStatus";
	public static final String LR_APPROVE_OPT_LOCK_EXCEPTION = "leaveRequest.approve.optimisticLockException";
	
	public static final String LR_CANCEL_UNAUTHORIZED = "leaveRequest.cancel.unauthorized";
	public static final String LR_CANCEL_OPT_LOCK_EXCEPTION = "leaveRequest.cancel.optimisticLockException";
	
	public static final String LR_WITHDRAW_UNAUTHORIZED = "leaveRequest.withdraw.unauthorized";
	public static final String LR_WITHDRAW_OPT_LOCK_EXCEPTION = "leaveRequest.withdraw.optimisticLockException";

	public static final String LR_VIEW_UNAUTHORIZED = "leaveRequest.view.unauthorized";
	public static final String EMAIL_FAILURE = "email.failure.msg";
	public static final String LR_TOO_MANY_HOURS = "leaveRequest.tooManyHours";
	public static final String LR_DENIAL_NO_REMARKS = "leaveDenial.noRemarks";

	public static final String OT_ZERO_TOTAL_EST_HOURS = "ot.zeroTotalEstHours";	
	public static final String OT_MISSING_EST_HOURS_ROW = "ot.missingEstHoursRow";
	public static final String OT_MINIMUM_TIME_FOR_TASK = "ot.minimumTimeForTask";
	public static final String OT_MISSING_TASK_DESC_ROW = "ot.missingTaskDescRow";
	public static final String OT_APPROVER_UNAUTHORIZED = "ot.approver.unauthorized";
	public static final String OT_VIEW_UNAUTHORIZED = "ot.view.unauthorized";
	
	public static final String OT_SUPERVISOR_UNAUTHORIZED = "ot.supervisor.unauthorized";
	public static final String OT_SUPERVISOR_UNAUTHORIZED_DETAIL = "ot.supervisor.unauthorized.detail";	
	
	public static final String OT_LIST_UNAUTHORIZED = "ot.list.unauthorized";
	
	public static final String OT_TYPE_REQUIRED = "ot.type.required";
	
	public static final String OPTIMISTIC_LOCK_MSG = "optimistic.locking.msg";
	
	public static final String OT_CREATE_UNAUTHORIZED_SO = "ot.create.unauthorized.so";
	public static final String OT_CREATE_UNAUTHORIZED_OBO = "ot.create.unauthorized.obo";
	
	public static final String OT_LIST_UNAUTHORIZED_SO = "ot.list.unauthorized.so";
	public static final String OT_LIST_UNAUTHORIZED_OBO = "ot.list.unauthorized.obo";
	public static final String OT_LIST_UNAUTHORIZED_MGR = "ot.list.unauthorized.mgr";

	public static final String OT_CANCEL_UNAUTHORIZED_SO = "ot.cancel.unauthorized.so";	
	public static final String OT_CANCEL_UNAUTHORIZED_SO_SPECIFIC = "ot.cancel.unauthorized.so.specific";	
	public static final String OT_CANCEL_INVALID_STATUS_SO = "ot.cancel.invalidStatus.so";
		
	public static final String OT_CANCEL_UNAUTHORIZED_OBO = "ot.cancel.unauthorized.obo";	
	public static final String OT_CANCEL_UNAUTHORIZED_OBO_SPECIFIC = "ot.cancel.unauthorized.obo.specific";
	public static final String OT_CANCEL_INVALID_STATUS_OBO = "ot.cancel.invalidStatus.obo";
	
	public static final String OT_CANCEL_UNAUTHORIZED_SUPV = "ot.cancel.unauthorized.supv";	
	public static final String OT_CANCEL_UNAUTHORIZED_SUPV_SPECIFIC = "ot.cancel.unauthorized.supv.specific";
	public static final String OT_CANCEL_INVALID_STATUS_SUPV = "ot.cancel.invalidStatus.supv";
		
	public static final String OT_REVIEW_INVALID_STATUS_SUPV = "ot.review.invalidStatus.supv";

	public static final String OT_DENY_UNAUTHORIZED_SUPV = "ot.deny.unauthorized.supv";	
	public static final String OT_DENY_UNAUTHORIZED_SUPV_SPECIFIC = "ot.deny.unauthorized.supv.specific";
	public static final String OT_DENY_INVALID_STATUS_SUPV = "ot.deny.invalidStatus.supv";

	public static final String OT_APPROVE_UNAUTHORIZED_SUPV = "ot.approve.unauthorized.supv";	
	public static final String OT_APPROVE_UNAUTHORIZED_SUPV_SPECIFIC = "ot.approve.unauthorized.supv.specific";
	public static final String OT_APPROVE_INVALID_STATUS_SUPV = "ot.approve.invalidStatus.supv";
	
	
	public static final String OT_SALARY_GRADE_MISSING = "ot.salaryGrade.missing";
	
	public static final String OT_MGR_MODIFY_UNAUTHORIZED = "ot.mgr.modify.unauthorized";
	public static final String OT_MGR_MODIFY_INVALID_STATUS = "ot.mgr.modify.invalidStatus";
	public static final String OT_MGR_MODIFY_INVALID_TYPE = "ot.mgr.modify.invalidType";
	
	
	public static final String OT_MODIFY_UNAUTHORIZED_SO = "ot.modify.unauthorized.so";	
	public static final String OT_MODIFY_UNAUTHORIZED_SO_SPECIFIC = "ot.modify.unauthorized.so.specific";	
	public static final String OT_MODIFY_INVALID_STATUS_SO = "ot.modify.invalidStatus.so";
		
	public static final String OT_MODIFY_UNAUTHORIZED_OBO = "ot.modify.unauthorized.obo";	
	public static final String OT_MODIFY_UNAUTHORIZED_OBO_SPECIFIC = "ot.modify.unauthorized.obo.specific";
	public static final String OT_MODIFY_INVALID_STATUS_OBO = "ot.modify.invalidStatus.obo";
	
/*
 * 	public static final String OT_CANCEL_UNAUTHORIZED_SUPV = "ot.cancel.unauthorized.supv";	
	public static final String OT_CANCEL_UNAUTHORIZED_SUPV_SPECIFIC = "ot.cancel.unauthorized.supv.specific";
	public static final String OT_CANCEL_INVALID_STATUS_SUPV = "ot.cancel.invalidStatus.supv";

 */
	
	public static final String OT_GROUP_VIEW_UNAUTHORIZED = "ot.group.view.unauthorized";
	public static final String OT_GROUP_VIEW_UNAUTHORIZED_SPECIFIC = "ot.group.view.unauthorized.specific";
	
	public static final String OT_GROUP_SUBMIT_UNAUTHORIZED = "ot.group.submit.unauthorized";
	public static final String OT_GROUP_SUBMIT_UNAUTHORIZED_SPECIFIC = "ot.group.submit.unauthorized.specific";
	public static final String OT_GROUP_SUBMIT_INVALID_STATUS = "ot.group.submit.invalidStatus";
	
	public static final String OT_GROUP_RECEIVE_UNAUTHORIZED = "ot.group.receive.unauthorized";
	public static final String OT_GROUP_RECEIVE_UNAUTHORIZED_SPECIFIC = "ot.group.receive.unauthorized.specific";
	public static final String OT_GROUP_RECEIVE_INVALID_STATUS = "ot.group.receive.invalidStatus";
	
	public static final String OT_GROUP_REVIEW_UNAUTHORIZED = "ot.group.review.unauthorized";
	public static final String OT_GROUP_REVIEW_UNAUTHORIZED_SPECIFIC = "ot.group.review.unauthorized.specific";
	public static final String OT_GROUP_REVIEW_INVALID_STATUS = "ot.group.review.invalidStatus";	
	
	
	public static final String OT_GROUP_CANCEL_UNAUTHORIZED = "ot.group.cancel.unauthorized";
	public static final String OT_GROUP_CANCEL_UNAUTHORIZED_SPECIFIC = "ot.group.cancel.unauthorized.specific";
	public static final String OT_GROUP_CANCEL_INVALID_STATUS = "ot.group.cancel.invalidStatus";	
	
	public static final String OT_GROUP_MODIFY_UNAUTHORIZED = "ot.group.modify.unauthorized";
	public static final String OT_GROUP_MODIFY_UNAUTHORIZED_SPECIFIC = "ot.group.modify.unauthorized.specific";	
	public static final String OT_GROUP_MODIFY_INVALID_STATUS = "ot.group.modify.invalidStatus";
	
	
	public static final String OT_GROUP_FINALIZE_UNAUTHORIZED = "ot.group.finalize.unauthorized";
	public static final String OT_GROUP_FINALIZE_UNAUTHORIZED_SPECIFIC = "ot.group.finalize.unauthorized.specific";
	public static final String OT_GROUP_FINALIZE_INVALID_STATUS = "ot.group.finalize.invalidStatus";

	
	public static final String OT_GROUP_SUBMIT_RECEIVER_REQUIRED = "ot.group.submit.receiverRequired";
	
	public static final String OT_MODIFY_NO_TASKS = "ot.modify.no.tasks";
	public static final String OT_EMTPY_TASK_DESC = "ot.empty.taskDesc";
	public static final String OT_EMTPY_EST_HOURS = "ot.empty.estHours";
	public static final String OT_INVALID_EST_HOURS = "ot.invalid.estHours";
	
	public static final String OT_TASK_LESS_THAN_ONE_TENTH_OF_AN_HOUR = "ot.task.lessThanOneTenthOfAnHour";
	
	public static final String LR_CHANGE_SUPV_UNAUTHORIZED = "lr.changeSupv.unauthorized";
	public static final String LR_CHANGE_SUPV_INVALID_STATUS = "lr.changeSupv.invalidStatus";
	public static final String LR_CHANGE_SUPV_OPT_LOCK_EXCEPTION = "lr.changeSupv.optimisticLockException";
		
	public static final String LR_CHANGE_SUPV_NEW_SUPV_NOT_SELECTED = "lr.changeSupv.newSupervisorNotSelected";
	public static final String LR_CHANGE_SUPV_NO_CHANGES_MADE = "lr.changeSupv.noChangesMade";
	
	public static final String LR_REQUIRED_LEAVE_DATE = "lr.required.leaveDate";
	public static final String LR_REQUIRED_LEAVE_TYPE = "lr.required.leaveType";
	public static final String LR_REQUIRED_LEAVE_HOURS = "lr.required.leaveHours";
	
	public static final String LR_LEAVE_HOURS_NAN = "lr.leaveHours.NAN";	
	public static final String LR_LEAVE_HOURS_ZERO = "lr.leaveHours.ZERO";
	
	public static final String LR_REPORT_VARIANCE_UNAUTHORIZED = "lr.report.variance.unauthorized";
	public static final String LR_REPORT_VARIANCE_INVALID_DATE_RANGE = "lr.report.variance.invalidDateRange";
	
	public static final String LR_RECON_NO_CHOICE_SELECTED = "lr.recon.noChoiceSelected";
	public static final String LR_RECON_HOURS_DONT_MATCH = "lr.recon.hoursDontMatch";
	public static final String LR_RECON_ETAMS_NOT_CHANGED = "lr.recon.EtamsNotChanged";
	public static final String LR_RECON_ALOHA_NOT_CHANGED = "lr.recon.AlohaNotChanged";
	public static final String LR_RECON_NO_VALUE_ENTERED = "lr.recon.noValueEntered";
	public static final String LR_RECON_TOO_MANY_DECIMALS = "lr.recon.tooManyDecimals";
	public static final String LR_RECON_NUMBER_TOO_LARGE = "lr.recon.numberTooLarge";
	public static final String LR_RECON_ALOHAETAMS_MUST_BE_NUMERIC = "lr.recon.alohaEtamsMustBeNumeric";
	public static final String	LR_RECON_NO_SPACES_ALLOWED = "lr.recon.noSpacesAllowed";
	
	public static final String LR_DISABLED_VET_RECORD_NOT_FOUND = "lr.disabledVet.recordNotFound";
	public static final String LR_DISABLED_VET_RECORD_RECORD_EXPIRED = "lr.disabledVet.recordExpired";
	public static final String LR_DISABLED_VET_RECORD_INSUFFICIENT_LEAVE_BALANCES = "lr.disabledVet.insufficientLeaveBalances";	
	
	public static final String LR_DISABLED_VET_NOT_CERTIFIED_SO = "lr.disabledVet.notCertified.SO";
	public static final String LR_DISABLED_VET_NOT_CERTIFIED_OBO = "lr.disabledVet.notCertified.OBO";
	
	public static ErrorMessages getInstance() {
		// Check for an instance and if there isn't one, enter a synchronized block
		if ( uniqueInstance == null ) { 
			// We only synchronize the first time through
			synchronized(ErrorMessages.class) { 
				// Once in the synchronized block, check again and if the instance is still null, 
				// create an instance of this Singleton
				if ( uniqueInstance == null ) { 
					uniqueInstance = new ErrorMessages();		
				}
			}
		}
		return uniqueInstance;
	}	
	private ResourceBundle bundle;
	
	private ErrorMessages() {
		FacesContext context = FacesContext.getCurrentInstance();
		UIViewRoot viewRoot = context.getViewRoot();
		Locale locale = viewRoot.getLocale();
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		this.bundle = ResourceBundle.getBundle(BASE_NAME, locale, loader);
	}
	
	public String getMessage(String key) {
		return this.bundle.getString(key);
	}
	
	public String getMessage(String key, Object[] params) {
		try {
			String resource = this.bundle.getString(key);
			if ( !StringUtil.isNullOrEmpty(resource)) {
				MessageFormat mf = new MessageFormat(resource);
				return mf.format(params);
			} else {
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}