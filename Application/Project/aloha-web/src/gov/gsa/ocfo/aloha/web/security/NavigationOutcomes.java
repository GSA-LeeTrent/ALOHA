package gov.gsa.ocfo.aloha.web.security;

public interface NavigationOutcomes {
	public final static String LOGOUT_SUCCESS = "logoutSuccess";

	public final static String HOME_PAGE = "home";
	public final static String LR_LIST = "lrList";
	public final static String LR_VIEW = "lrView";

	public final static String LR_CREATE = "lrCreate";
	public final static String LR_CREATE_PENDING = "lrCreatePending";
	public final static String LR_CREATE_CONFIRM = "lrCreateConfirm";

	public final static String LR_AMEND = "lrAmend";
	public final static String LR_AMEND_PENDING = "lrAmendPending";
	public final static String LR_AMEND_CONFIRM = "lrAmendConfirm";

	public final static String LR_CANCEL = "lrCancel";
	public final static String LR_CANCEL_PENDING = "lrCancelPending";
	public final static String LR_CANCEL_CONFIRM = "lrCancelConfirm";

	public final static String LR_WITHDRAW = "lrWithdraw";
	public final static String LR_WITHDRAW_PENDING = "lrWithdrawPending";
	public final static String LR_WITHDRAW_CONFIRM = "lrWithdrawConfirm";

	public final static String LR_APPROVE = "lrApprove";
	public final static String LR_APPROVE_AMEND = "lrApproveAmend";
	public final static String LR_APPROVE_PENDING = "lrApprovePending";
	public final static String LR_APPROVE_CONFIRM = "lrApproveConfirm";


	public final static String SERVER_ERROR = "serverError";
	public final static String USER_ERROR = "userError";
	public final static String UNAUTHORIZED = "unauthorized";
	public final static String ILLEGAL_STATE = "illegalState";

	public final static String LEAVE_REQUEST_RECONCILIATION = "runLeaveRequestReconciliation";
	public final static String LEAVE_REQUEST_REPORT = "runLeaveRequestReport";

	public final static String OVERTIME_REQUEST_RECONCILIATION = "runOvertimeRequestReconciliation";
	public final static String OVERTIME_REQUEST_REPORT = "runOvertimeRequestReport";

	public final static String EDIT_ADXREF = "editAdXref";
	public final static String LIST_ADXREF = "listAdXref";

	public final static String OT_CREATE_SO = "otCreateSO";
	public final static String OT_CREATE_OBO = "otCreateOBO";	
	
	//public final static String OT_SUPV_LIST = "otSupvList";
	//public final static String OT_LIST = "otList";

	public final static String OT_LIST_SO = "otListSO";
	public final static String OT_LIST_OBO = "otListOBO";
	public final static String OT_LIST_SUPV = "otListSupv";
	
	/***********************************************************************
	 * OVERTIME - VERSION 2
	 **********************************************************************/
	public final static String OT_MGR_LIST = "otMgrList";
}
