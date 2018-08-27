package gov.gsa.ocfo.aloha.web.validator.leave.v2;

import gov.gsa.ocfo.aloha.exception.AlohaValidationException;
import gov.gsa.ocfo.aloha.model.PayPeriod;
import gov.gsa.ocfo.aloha.model.ScheduleItem;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveItem;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveType;
import gov.gsa.ocfo.aloha.web.model.leave.LeaveViewItem;
import gov.gsa.ocfo.aloha.web.model.leave.PPLeaveDay;
import gov.gsa.ocfo.aloha.web.util.DateUtil;
import gov.gsa.ocfo.aloha.web.validator.leave.LRValidator;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public abstract class LRValidatorV2Impl  implements LRValidatorV2, Serializable {
	private static final long serialVersionUID = -2970315897967937863L;

	public abstract BigDecimal getMaxLeaveHoursPerDay();
	public abstract BigDecimal getMaxLeaveHoursPerPayPeriod();

	private PayPeriod payPeriod;
	private Map<String, PPLeaveDay> ppLeaveDays = new TreeMap<String, PPLeaveDay>();

	public LRValidatorV2Impl(PayPeriod payPeriod, List<LeaveItem> priorRequestItems) throws AlohaValidationException {
		this.payPeriod = payPeriod;
		this.initLeaveDaysForPayPeriod();

		this.validateDateRangeForPriorRequestItems(priorRequestItems);
		this.populateLeaveDaysForPayPeriod(priorRequestItems);
	}

	@Override
	public void validateCurrentRequest(List<LeaveViewItem> currentRequest) throws AlohaValidationException {
		this.validateDateRangeForCurrentRequest(currentRequest);
		this.validateTotalHoursForPayPeriod(currentRequest);
		this.validateHoursForEachPayPeriodDay(currentRequest);
		this.validateLeaveTypesPerDay(currentRequest);
	}
	private void initLeaveDaysForPayPeriod() {
		GregorianCalendar ppCal = new GregorianCalendar();
		ppCal.setTime(this.payPeriod.getFromDate());
		ppCal.set(Calendar.HOUR_OF_DAY, 0);
		ppCal.set(Calendar.MINUTE, 0);
		ppCal.set(Calendar.SECOND, 0);
		ppCal.set(Calendar.MILLISECOND, 0);

		for ( int ii = 0; ii < LRValidator.MAX_DAYS_IN_PAY_PERIOD; ii++ ) {
			Date ppDateValue = ppCal.getTime();
			String ppDateKey = DateUtil.formatDate(ppDateValue, DateUtil.DateFormats.YYYYMMDD);
			this.ppLeaveDays.put(ppDateKey, new PPLeaveDay(ppDateValue));
			ppCal.add(Calendar.DATE, 1);
		}
		//this.debug();
	}
	private void validateDateRangeForPriorRequestItems(List<LeaveItem> priorRequestItems) throws AlohaValidationException {
		AlohaValidationException valException = new AlohaValidationException();

		for ( LeaveItem leaveItem : priorRequestItems ) {
			String liDateKey = DateUtil.formatDate(leaveItem.getLeaveDate(), DateUtil.DateFormats.YYYYMMDD);
			if ( ! this.ppLeaveDays.containsKey(liDateKey)) {
				StringBuilder errMsg = new StringBuilder();
				errMsg.append("Leave Item date does not fall inside Pay Period date range under consideration: " );
				errMsg.append("Pay Period Date Range (");
				errMsg.append(DateUtil.formatDate(this.payPeriod.getToDate(), DateUtil.DateFormats.DEFAULT_FORMAT));
				errMsg.append(" to ");
				errMsg.append(DateUtil.formatDate(this.payPeriod.getFromDate(), DateUtil.DateFormats.DEFAULT_FORMAT));
				errMsg.append("). ");
				errMsg.append("Leave Item Date (");
				errMsg.append(DateUtil.formatDate(leaveItem.getLeaveDate(), DateUtil.DateFormats.DEFAULT_FORMAT));
				errMsg.append("). ");
				valException.addErrorMessage(errMsg.toString());
			}
		}
		if ( valException.hasErrorMessages() ) {
			throw valException;
		}
	}
	private void populateLeaveDaysForPayPeriod(List<LeaveItem> priorRequestItems) {
		for ( LeaveItem leaveItem : priorRequestItems ) {
			String key = DateUtil.formatDate(leaveItem.getLeaveDate(), DateUtil.DateFormats.YYYYMMDD);
			if ( this.ppLeaveDays.containsKey(key)) {
				PPLeaveDay ppLeaveDay = this.ppLeaveDays.get(key);
				ppLeaveDay.addLeaveType(leaveItem.getLeaveType());
				ppLeaveDay.addToLeaveHours(leaveItem.getLeaveHours());
				LeaveType leaveType = leaveItem.getLeaveType();
				ppLeaveDay.addLeaveTypeCode(leaveType.getPrimaryCode());
				if ( leaveType.hasSecondaryCode()) {
					ppLeaveDay.addLeaveTypeCode(leaveType.getSecondaryCode());
				}

			}
		}
	}
	private void validateDateRangeForCurrentRequest(List<LeaveViewItem> currentRequest) throws AlohaValidationException {
		AlohaValidationException valException = new AlohaValidationException();

		for ( LeaveViewItem lvItem :  currentRequest) {
			String lviDateKey = DateUtil.formatDate(lvItem.getLeaveDate(), DateUtil.DateFormats.YYYYMMDD);
			if ( ! this.ppLeaveDays.containsKey(lviDateKey)) {
				StringBuilder errMsg = new StringBuilder();
				errMsg.append("Date for submitted leave request does not fall inside pay period date range under consideration: " );
				errMsg.append("Pay Period Date Range (");
				errMsg.append(DateUtil.formatDate(payPeriod.getToDate(), DateUtil.DateFormats.DEFAULT_FORMAT));
				errMsg.append(" to ");
				errMsg.append(DateUtil.formatDate(payPeriod.getFromDate(), DateUtil.DateFormats.DEFAULT_FORMAT));
				errMsg.append("). ");
				errMsg.append("Submitted Leave Request Date (");
				errMsg.append(DateUtil.formatDate(lvItem.getLeaveDate(), DateUtil.DateFormats.DEFAULT_FORMAT));
				errMsg.append("). ");
				valException.addErrorMessage(errMsg.toString());
			}
		}
		if ( valException.hasErrorMessages() ) {
			throw valException;
		}
	}
	private void validateTotalHoursForPayPeriod(List<LeaveViewItem> currentRequest) throws AlohaValidationException {
		// VALIDATE TOTAL LEAVE HOURS FOR PAY PERIOD
		BigDecimal priorLeaveHoursForPP = this.sumLeaveHoursForPriorRequests();
		priorLeaveHoursForPP.setScale(1, RoundingMode.HALF_DOWN);

		BigDecimal newLeaveHoursForPP = this.sumLeaveHoursForCurrentRequest(currentRequest);
		newLeaveHoursForPP.setScale(1, RoundingMode.HALF_DOWN);

		BigDecimal remainingLeaveHoursForPP = this.getMaxLeaveHoursPerPayPeriod().subtract(priorLeaveHoursForPP);
		remainingLeaveHoursForPP.setScale(1, RoundingMode.HALF_DOWN);

//		System.out.println("\n");
//		System.out.println("----------------------------------------------------");
//		System.out.println("priorLeaveHoursForPP: " + priorLeaveHoursForPP);
//		System.out.println("newLeaveHoursForPP: " + newLeaveHoursForPP);
//		System.out.println("remainingLeaveHoursForPP: " + remainingLeaveHoursForPP);
//		System.out.println("----------------------------------------------------");

		if ( newLeaveHoursForPP.compareTo(remainingLeaveHoursForPP) == 1 ) {
			StringBuilder errMsg = new StringBuilder();

			errMsg.append("The maximum total number of leave hours allowed for a pay period has been exceeded. " );
			errMsg.append("Maximum allowed: ");
			errMsg.append(this.getMaxLeaveHoursPerPayPeriod());
			errMsg.append("; ");

			if ( priorLeaveHoursForPP.compareTo(BigDecimal.ZERO) == 1) {
				errMsg.append("Previously requested: ");
				errMsg.append(priorLeaveHoursForPP);
				errMsg.append("; ");
			}

			errMsg.append("Currently requested: ");
			errMsg.append(newLeaveHoursForPP);
			errMsg.append("; ");

			if ( priorLeaveHoursForPP.compareTo(BigDecimal.ZERO) == 1) {
				errMsg.append("; Remaining leave hours allowed: ");
				errMsg.append(remainingLeaveHoursForPP);
				errMsg.append(";");
			}
			AlohaValidationException valException = new AlohaValidationException();
			valException.addErrorMessage(errMsg.toString());
			throw valException;
		}

	}
	private void validateHoursForEachPayPeriodDay(List<LeaveViewItem> currentRequest) throws AlohaValidationException {
		AlohaValidationException valException = new AlohaValidationException();
		// VALIDATE LEAVE HOURS FOR EACH PAY PERIOD DAY
		for ( LeaveViewItem lvItem : currentRequest ) {
			String siDateKey = DateUtil.formatDate(lvItem.getLeaveDate(), DateUtil.DateFormats.YYYYMMDD);
			PPLeaveDay ppLeaveDay = this.ppLeaveDays.get(siDateKey);

			BigDecimal priorLeaveHoursForDay = ppLeaveDay.getLeaveHours();
			priorLeaveHoursForDay.setScale(1, RoundingMode.HALF_DOWN);

			BigDecimal remainingLeaveHoursForDay = this.getMaxLeaveHoursPerDay().subtract(priorLeaveHoursForDay);
			remainingLeaveHoursForDay.setScale(1, RoundingMode.HALF_DOWN);

			BigDecimal newLeaveHoursForDay = lvItem.getLeaveNumberOfHours();
			if ( newLeaveHoursForDay != null) {
				newLeaveHoursForDay.setScale(1, RoundingMode.HALF_DOWN);
			}

//			System.out.println("\n");
//			System.out.println("----------------------------------------------------");
//			System.out.println("getMaxLeaveHoursPerDay(): " + this.getMaxLeaveHoursPerDay());
//			System.out.println("priorLeaveHoursForDay: " + priorLeaveHoursForDay);
//			System.out.println("remainingLeaveHoursForDay: " + remainingLeaveHoursForDay);
//			System.out.println("newLeaveHoursForDay: " + newLeaveHoursForDay);
//			System.out.println("----------------------------------------------------");

			if ( newLeaveHoursForDay != null) {
				// ADD NEW LEAVE HOURS PER DAY TO THE LEAVE HOUR BUCKET IF THERE'S ROOM
				if ( newLeaveHoursForDay.compareTo(remainingLeaveHoursForDay) <= 0 ) {
					ppLeaveDay.addToLeaveHours(newLeaveHoursForDay);
				} else {
					String formattedLeaveDate = DateUtil.formatDate(lvItem.getLeaveDate(), DateUtil.DateFormats.DEFAULT_FORMAT);
					StringBuilder errMsg = new StringBuilder();

					errMsg.append("Total allowable leave hours exceeded for " );
					errMsg.append(formattedLeaveDate);
					errMsg.append(". ");

					if ( priorLeaveHoursForDay.compareTo(BigDecimal.ZERO) == 1) {
						errMsg.append("Previously requested: ");
						errMsg.append(priorLeaveHoursForDay);
						errMsg.append(" hours; ");
					}

					errMsg.append("Currently requested: ");
					errMsg.append(newLeaveHoursForDay);
					errMsg.append(" hours; ");

					errMsg.append("Please review leave request for the ");
					errMsg.append(this.payPeriod.getLabel());
					errMsg.append(" pay period to ensure proper leave hours for ");
					errMsg.append(formattedLeaveDate);

					/*
					if ( priorLeaveHoursForDay.compareTo(BigDecimal.ZERO) == 1) {
						errMsg.append("Remaining leave hours allowed for ");
						errMsg.append(formattedLeaveDate);
						errMsg.append(": ");
						errMsg.append(remainingLeaveHoursForDay);
						errMsg.append(" hours.");
					}
					*/

					valException.addErrorMessage(errMsg.toString());
				}
			}
		}
		if ( valException.hasErrorMessages()) {
			throw valException;
		}
	}

	private void validateLeaveTypesPerDay(List<LeaveViewItem> currentRequest) throws AlohaValidationException {
		AlohaValidationException valException = new AlohaValidationException();
		// VALIDATE NUMBER OF LEAVE TYPES PER DAY
		for ( LeaveViewItem lvItem : currentRequest ) {
			String siDateKey = DateUtil.formatDate(lvItem.getLeaveDate(), DateUtil.DateFormats.YYYYMMDD);
			PPLeaveDay ppLeaveDay = this.ppLeaveDays.get(siDateKey);

//			System.out.println("siDateKey: " + siDateKey);
//			System.out.println("si.getSelectedLeaveTypeCode(): " + si.getSelectedLeaveTypeCode());
//			System.out.println("ppLeaveDay.getLeaveTypeCodes().size(): " + ppLeaveDay.getLeaveTypeCodes().size());
//			System.out.println("ppLeaveDay.getLeaveTypeCodes().containsKey(si.getSelectedLeaveTypeCode()): " + ppLeaveDay.getLeaveTypeCodes().containsKey(si.getSelectedLeaveTypeCode()));

			String selectedLeaveTypeCode = lvItem.getLeaveTypeKey();
			if	( ( selectedLeaveTypeCode != null )
					&& ( ! selectedLeaveTypeCode.equals(ScheduleItem.UNSELECTED_LEAVE_TYPE_CODE) )
				) {
				// PRIMARY CODE
				String primaryCode = selectedLeaveTypeCode.substring(0,2);
				if ( ppLeaveDay.getLeaveTypeCodes().size() < LRValidator.MAX_LEAVE_TYPES_PER_DAY) {
					ppLeaveDay.addLeaveTypeCode(primaryCode);
				} else {
					String formattedLeaveDate = DateUtil.formatDate(lvItem.getLeaveDate(), DateUtil.DateFormats.DEFAULT_FORMAT);
					StringBuilder errMsg = new StringBuilder();
					errMsg.append("Number of allowed leave types (");
					errMsg.append(LRValidator.MAX_LEAVE_TYPES_PER_DAY);
					errMsg.append(") exceeded for ");
					errMsg.append(formattedLeaveDate);
					errMsg.append(".");
					valException.addErrorMessage(errMsg.toString());
					break;
				}

				// SECONDARY CODE
				if (selectedLeaveTypeCode.length() == 4) {
					String secondaryCode = selectedLeaveTypeCode.substring(2,4);
					if ( ppLeaveDay.getLeaveTypeCodes().size() < LRValidator.MAX_LEAVE_TYPES_PER_DAY) {
						ppLeaveDay.addLeaveTypeCode(secondaryCode);
					} else {
						String formattedLeaveDate = DateUtil.formatDate(lvItem.getLeaveDate(), DateUtil.DateFormats.DEFAULT_FORMAT);
						StringBuilder errMsg = new StringBuilder();
						errMsg.append("Number of allowed leave types (");
						errMsg.append(LRValidator.MAX_LEAVE_TYPES_PER_DAY);
						errMsg.append(") exceeded for ");
						errMsg.append(formattedLeaveDate);
						errMsg.append(".");
						valException.addErrorMessage(errMsg.toString());
					}
				}
			}
		}
		if ( valException.hasErrorMessages()) {
			throw valException;
		}
	}
	private BigDecimal sumLeaveHoursForPriorRequests() {
		BigDecimal total = BigDecimal.ZERO;
		for (Map.Entry<String, PPLeaveDay> entry : this.ppLeaveDays.entrySet()) {
			PPLeaveDay ppLeaveDay = entry.getValue();
			total = total.add(ppLeaveDay.getLeaveHours());
		}
		return total;
	}

	private BigDecimal sumLeaveHoursForCurrentRequest(List<LeaveViewItem> currentRequest ) {
		BigDecimal total = BigDecimal.ZERO;
		for ( LeaveViewItem lvi : currentRequest) {
			if ( lvi.getLeaveNumberOfHours() != null) {
				total = total.add(lvi.getLeaveNumberOfHours());
			}
		}
		return total;
	}
}
