package gov.gsa.ocfo.aloha.web.mb.leave;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

@ManagedBean(name=LeaveViewItemMB.MANAGED_BEAN_NAME, eager=false)
@SessionScoped
public class LeaveViewItemMB implements Serializable {
	private static final long serialVersionUID = -3551086419203257586L;
	public static final String MANAGED_BEAN_NAME = "leaveViewItemMB";
	
//	@ManagedProperty(value="#{leaveTypeMB}")
//	protected LeaveTypeMB leaveTypeMB;
//	public void setLeaveTypeMB(LeaveTypeMB leaveTypeMB) {
//		this.leaveTypeMB = leaveTypeMB;
//	}
//	
//	public interface LeaveViewItemApplyToValue {
//		public static final int SPECIFIC_DATE 		= 1;
//		public static final int ENTIRE_PAY_PERIOD 	= 2;
//		public static final int FIRST_PAY_PERIOD 	= 3;
//		public static final int SECOND_PAY_PERIOD 	= 4;
//	}
//	
//	
//	public interface LeaveViewItemMode {
//		public static final int ADD_MODE	= 1;
//		public static final int AEDIT_MODE 	= 2;
//	}	
//	
//	public void validateLeaveViewItem(LeaveViewItem lvItem, int leaveItemApplyToValueSelection) throws ValidatorException {
//		int errorCount = 0;
//		
//		// LEAVE DATE
//		if 	( (leaveItemApplyToValueSelection == LeaveViewItemApplyToValue.SPECIFIC_DATE) 
//				&& ( (StringUtil.isNullOrEmpty(lvItem.getLeaveDateKey()) == true)
//						|| ( lvItem.getLeaveDateKey().equals("0") == true) )
//			) 
//		{
//			String errMsgText = ErrorMessages.getInstance().getMessage(ErrorMessages.LR_REQUIRED_LEAVE_DATE);
//			FacesMessage facesMsg = new FacesMessage(errMsgText, errMsgText);
//			facesMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
//			FacesContext.getCurrentInstance().addMessage(null, facesMsg);
//			errorCount++;
//		}
//		
//		// LEAVE TYPE
//		if ( (StringUtil.isNullOrEmpty(lvItem.getLeaveTypeKey()) == true )
//				|| ( lvItem.getLeaveTypeKey().equals("0") == true )) 
//		{
//			String errMsgText = ErrorMessages.getInstance().getMessage(ErrorMessages.LR_REQUIRED_LEAVE_TYPE);
//			FacesMessage facesMsg = new FacesMessage(errMsgText, errMsgText);
//			facesMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
//			FacesContext.getCurrentInstance().addMessage(null, facesMsg);
//			errorCount++;				
//		}
//		
//		// LEAVE HOURS
//		if ( StringUtil.isNullOrEmpty(lvItem.getLeaveNumberOfHoursAsString()) == true) 
//		{
//			String errMsgText = ErrorMessages.getInstance().getMessage(ErrorMessages.LR_REQUIRED_LEAVE_HOURS);
//			FacesMessage facesMsg = new FacesMessage(errMsgText, errMsgText);
//			facesMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
//			FacesContext.getCurrentInstance().addMessage(null, facesMsg);
//			errorCount++;					
//		} else 
//		{
//			if ( NumberUtil.isBigDecimal(lvItem.getLeaveNumberOfHoursAsString()) == false)
//			{
//				String errMsgText = ErrorMessages.getInstance().getMessage(ErrorMessages.LR_LEAVE_HOURS_NAN);
//				FacesMessage facesMsg = new FacesMessage(errMsgText, errMsgText);
//				facesMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
//				FacesContext.getCurrentInstance().addMessage(null, facesMsg);
//				errorCount++;					
//			}
//			else 
//			{
//				if ( (new BigDecimal(lvItem.getLeaveNumberOfHoursAsString()).compareTo(LeaveItem.MIN_LEAVE_HOURS) == -1) ) {
//					String errMsgText = ErrorMessages.getInstance().getMessage(ErrorMessages.LR_LEAVE_HOURS_ZERO);
//					FacesMessage facesMsg = new FacesMessage(errMsgText, errMsgText);
//					facesMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
//					FacesContext.getCurrentInstance().addMessage(null, facesMsg);
//					errorCount++;
//				}
//			}
//		}
//		
//		if ( errorCount > 0) {
//			throw new ValidatorException(new FacesMessage());
//		}	
//    }	
//	
//	public void populateLeaveViewItem( LeaveViewItem lvItem, int leaveItemApplyToValueSelection) throws ValidatorException, ParseException, AlohaServerException {
//    	
//		// VALIDATE FIRST
//		this.validateLeaveViewItem(lvItem, leaveItemApplyToValueSelection);
//    	
//		// THEN POPULATE
//		if 	(	( StringUtil.isNullOrEmpty(lvItem.getLeaveTypeKey()) == false )
//    			&& ( lvItem.getLeaveTypeKey().equals("0") == false )
//    			&& ( StringUtil.isNullOrEmpty(lvItem.getLeaveNumberOfHoursAsString()) == false) 
//    			&& ( NumberUtil.isBigDecimal(lvItem.getLeaveNumberOfHoursAsString()) == true)
//    			&& ( (new BigDecimal(lvItem.getLeaveNumberOfHoursAsString()).compareTo(LeaveItem.MIN_LEAVE_HOURS) == 1))
//    		) 
//    	{
//	    	// LEAVE DATE
//    		if 	( (leaveItemApplyToValueSelection == LeaveViewItemApplyToValue.SPECIFIC_DATE) ) {
//    			lvItem.setLeaveDate(
//    					DateUtil.convertStringToDate(
//    							lvItem.getLeaveDateKey(), DateUtil.DateFormats.YYYYMMDD));    			
//    		}
//	    		
//			// LEAVE TYPE
//			String selectedLeaveTypeCode = lvItem.getLeaveTypeKey();
//			if ( selectedLeaveTypeCode.length() == 2) {
//				lvItem.setLeaveType(leaveTypeMB.getLeaveType(selectedLeaveTypeCode));
//			} else if (selectedLeaveTypeCode.length() == 4) {
//				lvItem.setLeaveType(
//					leaveTypeMB.getLeaveType(
//							selectedLeaveTypeCode.substring(0,2), selectedLeaveTypeCode.substring(2)));
//			}
//
//	    		
//			// NUMBER OF HOURS
//			lvItem.setLeaveNumberOfHours(
//    				new BigDecimal(lvItem.getLeaveNumberOfHoursAsString()));
//
//			// START TIME
//			if ( (StringUtil.isNullOrEmpty(lvItem.getLeaveStartTimeKey()) == false) 
//					&& (!lvItem.getLeaveStartTimeKey().equals(LeaveViewItem.UNSELECTED_START_TIME_KEY)) ) {
//				GregorianCalendar startCal = new GregorianCalendar();
//				startCal.setTime(new Date());
//				startCal.set(Calendar.HOUR_OF_DAY, 0);
//				startCal.set(Calendar.MINUTE, 0);					
//				startCal.set(Calendar.SECOND, 0);
//				startCal.set(Calendar.MILLISECOND, 0);
//				
//				// SET HOUR
//				String strHour = lvItem.getLeaveStartTimeKey().substring(0, 2);
//				Integer intHour = Integer.valueOf(strHour);
//				startCal.set(Calendar.HOUR_OF_DAY, intHour.intValue());
//				
//				// SET MINUTE
//				String strMinute = lvItem.getLeaveStartTimeKey().substring(2, 4);
//				Integer intMinute = Integer.valueOf(strMinute);
//				startCal.set(Calendar.MINUTE, intMinute.intValue());
//
//				lvItem.setLeaveStartTime(startCal.getTime());
//			} else {
//				lvItem.setLeaveStartTime(null);
//				lvItem.setLeaveStartTimeKey(null);
//			}
//    	} 		
//	}
//	
//    public void fillLeaveRequestUsingLeaveViewItemAndEmployeeBaseSchedule(List<LeaveViewItem> leaveViewItemList, LeaveViewItem lvItem, 
//    		List<PayPeriodSchedule> employeeBaseSchedule, int startIndex, int endIndex) throws AlohaServerException {
//		for (int index = startIndex; index < endIndex; index++) {
//			PayPeriodSchedule payPeriodDay = employeeBaseSchedule.get(index);
//			
//			if ( payPeriodDay.isWorkDay() ) {
//				LeaveViewItem newLeaveViewItem = new LeaveViewItem();
//				newLeaveViewItem.setLeaveDate(payPeriodDay.getCalendarDate());
//				newLeaveViewItem.setLeaveType(lvItem.getLeaveType());
//				newLeaveViewItem.setLeaveStartTime(lvItem.getLeaveStartTime());
//				newLeaveViewItem.setLeaveNumberOfHours(lvItem.getLeaveNumberOfHours());
//				leaveViewItemList.add(newLeaveViewItem);	 
//				newLeaveViewItem.setLeaveViewItemId(leaveViewItemList.size());
//			}
//		}
//    }	
//    
//    public BigDecimal sumLeaveHours(List<LeaveViewItem> lvItemList) {
//    	BigDecimal sum = BigDecimal.ZERO;
//    	for (LeaveViewItem lvItem : lvItemList) {
//    		sum = sum.add(lvItem.getLeaveNumberOfHours());
//    	}
//    	return sum;
//    }
}
