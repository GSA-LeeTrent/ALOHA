package gov.gsa.ocfo.aloha.web.mb.leave;

import gov.gsa.ocfo.aloha.exception.AlohaServerException;
import gov.gsa.ocfo.aloha.model.PayPeriodSchedule;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveItem;
import gov.gsa.ocfo.aloha.model.leave.DisabledVetLeaveInfo;
import gov.gsa.ocfo.aloha.util.StringUtil;
import gov.gsa.ocfo.aloha.web.model.leave.LeaveViewItem;
import gov.gsa.ocfo.aloha.web.util.DateUtil;
import gov.gsa.ocfo.aloha.web.util.ErrorMessages;
import gov.gsa.ocfo.aloha.web.util.NumberUtil;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Random;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;

public abstract class LRBaseV2MB implements Serializable {
	private static final long serialVersionUID = -8561938974525171331L;
	
	
	////////////////////////////////////////////////////////////////////
	// BEGIN: DISABLED VETERAN LEAVE TYPE AND CERTIFICATION
	////////////////////////////////////////////////////////////////////
	protected boolean disabledVetCertified = false;
	public boolean isDisabledVetCertified() {
		return disabledVetCertified;
	}
	public void setDisabledVetCertified(boolean disabledVetCertified) {
		this.disabledVetCertified = disabledVetCertified;
	}
	public boolean isHasDisabledVetLeaveViewItem() { 
		return this.getLrItems().isHasDisabledVetLeaveViewItem();
	}
	////////////////////////////////////////////////////////////////////
	// END: DISABLED VETERAN LEAVE TYPE AND CERTIFICATION
	////////////////////////////////////////////////////////////////////

	protected abstract LRItems getLrItems();
	protected abstract LeaveTypeMB getLeaveTypeMB();
	protected abstract List<PayPeriodSchedule> getPayPeriodScheduleList() throws AlohaServerException;
	protected abstract DisabledVetLeaveInfo retrieveDisabledVetLeaveInfo() throws AlohaServerException;
	public abstract boolean isInSubmitOwnMode();
	public abstract boolean isInOnBehalfOfMode();
	
	protected interface LeaveViewItemApplyToValue {
		public static final int SPECIFIC_DATE 		= 1;
		public static final int ENTIRE_PAY_PERIOD 	= 2;
		public static final int FIRST_PAY_PERIOD 	= 3;
		public static final int SECOND_PAY_PERIOD 	= 4;
	}	
	
	protected interface LeaveViewItemMode {
		public static final int ADD_MODE	= 1;
		public static final int EDIT_MODE 	= 2;
	}
	
	protected int leaveViewItemMode;
	public int getLeaveViewItemMode() {
		return leaveViewItemMode;
	}
	public boolean isInLeaveViewItemAddMode() {
		return (this.leaveViewItemMode == LeaveViewItemMode.ADD_MODE
					|| (this.getCollectedLeaveItems().isEmpty())
				);
	}
	public boolean isInLeaveViewItemEditMode() {
		return ( this.leaveViewItemMode == LeaveViewItemMode.EDIT_MODE );
	}
	
	protected String leaveItemApplyToSelection;
	public String getLeaveItemApplyToSelection() {
		return leaveItemApplyToSelection;
	}
	
	public void setLeaveItemApplyToSelection(String leaveItemApplyToSelection) {
		this.leaveItemApplyToSelection = leaveItemApplyToSelection;
	}
	private Random leaveViewItemIdGenerator;
    private long assignLeaveViewItemId(List<LeaveViewItem> lviList ) {
    	long nextLeaveViewItemId = -1L;
    	boolean done = false;
    	int matchCount = 0;
    	while ( done == false) {
    		nextLeaveViewItemId = this.leaveViewItemIdGenerator.nextInt();
    		if ( nextLeaveViewItemId == 0L) {
    			nextLeaveViewItemId = nextLeaveViewItemId + 1L;
    		}
    		if ( nextLeaveViewItemId < 0L ) {
    			nextLeaveViewItemId = (nextLeaveViewItemId * -1L);
    		}
    		
    		//System.out.println("nextLeaveViewItemId: " + nextLeaveViewItemId);
    		for ( LeaveViewItem lviObject: lviList ) {
    			//System.out.println("lviObject.getLeaveViewItemId(): " + lviObject.getLeaveViewItemId());
    			if ( nextLeaveViewItemId == lviObject.getLeaveViewItemId()) {
    				matchCount++;
    			}
    		}
    		if (matchCount == 0) {
    			done = true;
    		}
    	}
    	return nextLeaveViewItemId;
    }	
	protected void init() {
		this.leaveItemApplyToSelection = String.valueOf(LeaveViewItemApplyToValue.SPECIFIC_DATE);
		this.leaveViewItemMode = LeaveViewItemMode.ADD_MODE;
		this.leaveViewItemIdGenerator = new Random();

	}
	
	protected void onMakeRevisions() {
		this.getLrItems().clearSelectedLeaveItem();
		this.leaveItemApplyToSelection = String.valueOf(LeaveViewItemApplyToValue.SPECIFIC_DATE);
		this.leaveViewItemMode = LeaveViewItemMode.ADD_MODE;
	}
	protected void cleanup() {
		this.leaveItemApplyToSelection = null;
		this.leaveViewItemIdGenerator = null;
	}
	
	public LeaveViewItem getSelectedLeaveItem() {
		return this.getLrItems().getSelectedLeaveItem();
	}
	public List<LeaveViewItem> getCollectedLeaveItems() {
		return this.getLrItems().getCollectedLeaveItems();
	}

	public void setEditLeaveItemId(long editLeaveItemId) {
		this.getLrItems().setEditLeaveItemId(editLeaveItemId);
	}
	public void setRemoveLeaveItemId(long removeLeaveItemId) {
		this.getLrItems().setRemoveLeaveItemId(removeLeaveItemId);
	}
	public List<LeaveViewItem>getCollectedLeaveViewItems() {
		return this.getLrItems().getCollectedLeaveItems();
	}

	public void addLeaveViewItems() throws ValidatorException, AlohaServerException, ParseException {
		this.getLrItems().addLeaveViewItems();	
		this.leaveItemApplyToSelection = String.valueOf(LeaveViewItemApplyToValue.SPECIFIC_DATE);
		this.leaveViewItemMode = LeaveViewItemMode.ADD_MODE;
	}

	public void editLeaveViewItem() throws ValidatorException, ParseException, AlohaServerException {
		this.getLrItems().editLeaveViewItem();	
		this.cancelEditLeaveViewItem();
	}	

	public void cancelEditLeaveViewItem() {
		this.getLrItems().cancelEditLeaveViewItem();
		this.leaveItemApplyToSelection = String.valueOf(LeaveViewItemApplyToValue.SPECIFIC_DATE);
		this.leaveViewItemMode = LeaveViewItemMode.ADD_MODE;
	}	
	
	public void prepareLeaveViewItemForEdit() {
		this.leaveViewItemMode = LeaveViewItemMode.EDIT_MODE;
		this.getLrItems().prepareLeaveViewItemForEdit();
	}

	public void removeNewLeaveItem() {
		this.getLrItems().removeNewLeaveItem();
		if (this.getLrItems().getCollectedLeaveItems().isEmpty() ) {
			this.leaveViewItemMode = LeaveViewItemMode.ADD_MODE;
		}
	}	
	public BigDecimal getTotalLeaveHours() {
		return (this.getLrItems().sumLeaveHours());
	}

	
	protected void validateDisabledVetCertification() throws ValidatorException {
		
//		System.out.println("BEGIN: validateDisabledVetCertification()");
//		System.out.println("isHasDisabledVetLeaveViewItem: " + isHasDisabledVetLeaveViewItem());
//		System.out.println("this.disabledVetCertified: " + this.disabledVetCertified);
	
		
		//////////////////////////////////////////////////////////////////////////////
		// IF user has selected the disabled veteran leave type, we need to make sure
		// that user has checked the check box certifying that employee will use 
		// disabled veteran leave for its intended purpose.
		//////////////////////////////////////////////////////////////////////////////			
		if ( this.isHasDisabledVetLeaveViewItem() ) {
			
			if ( this.disabledVetCertified == false ) {
				
				String errMsgText = null;
				if ( this.isInSubmitOwnMode() ) {
					
					errMsgText = ErrorMessages.getInstance().getMessage(ErrorMessages.LR_DISABLED_VET_NOT_CERTIFIED_SO);
				} else {
					errMsgText = ErrorMessages.getInstance().getMessage(ErrorMessages.LR_DISABLED_VET_NOT_CERTIFIED_OBO);
				}
				
				FacesMessage facesMsg = new FacesMessage(errMsgText, errMsgText);
				facesMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
				FacesContext.getCurrentInstance().addMessage(null, facesMsg);
				throw new ValidatorException(new FacesMessage());				
				
			}
		}	
		
		//System.out.println("END: validateDisabledVetCertification()");
	
	}	
	
	

	/****************************************************************************************************
	 *
	 * class LRItemsMgr
	 * @author LeeTTrent
	 *
	 ****************************************************************************************************/
	protected class LRItems {		
		private LeaveViewItem selectedLeaveItem;
		protected List<LeaveViewItem> collectedLeaveItems;
		private Long removeLeaveItemId;
		private Long editLeaveItemId;
		
		protected void cleanup() {
			this.selectedLeaveItem = null;
			this.collectedLeaveItems = null;
			this.removeLeaveItemId = null;
			this.editLeaveItemId = null;
		}
		
		
		public boolean isHasDisabledVetLeaveViewItem() { 
			
			if ( this.collectedLeaveItems != null ) {
				
				for ( LeaveViewItem lvi : this.collectedLeaveItems ) {
					if ( lvi.isDisabledVetLeaveViewItem() ) {
						return true;						
					}
				}			
			}

			return false;
		}
		
		private void clearSelectedLeaveItem() {
			this.selectedLeaveItem = null;
		}
		protected void cancelEditLeaveViewItem() {
			this.selectedLeaveItem = null;
		}
		protected Long getRemoveLeaveItemId() {
			return removeLeaveItemId;
		}

		protected void setRemoveLeaveItemId(Long removeLeaveItemId) {
			this.removeLeaveItemId = removeLeaveItemId;
		}

		protected Long getEditLeaveItemId() {
			return this.editLeaveItemId;
		}

		protected void setEditLeaveItemId(Long editLeaveItemId) {
			this.editLeaveItemId = editLeaveItemId;
		}

		protected LeaveViewItem getSelectedLeaveItem() {
			if ( this.selectedLeaveItem == null ) {
				this.selectedLeaveItem = new LeaveViewItem();
			}
			return this.selectedLeaveItem;
		}

		public void setCollectedLeaveItems(List<LeaveViewItem> collectedLeaveItems) {
			this.collectedLeaveItems = collectedLeaveItems;
		}

		protected List<LeaveViewItem> getCollectedLeaveItems() {
			if ( this.collectedLeaveItems == null) {
				this.collectedLeaveItems = new ArrayList<LeaveViewItem>();
			}
			return this.collectedLeaveItems;
		}
		
		protected LeaveViewItem getLeaveItemById(long id) {
	        for (LeaveViewItem leaveItem : this.getCollectedLeaveItems()) {
	            if (id == leaveItem.getLeaveViewItemId()) {
	                return leaveItem;
	            }
	        }
	        return null;
	    }
	    
		protected void performValidation() throws ValidatorException, AlohaServerException, ParseException {
    		
			///////////////////////////////////////////////////////////////////////////////////			
			// Validate to make sure that user has selected leave items for this leave request
			///////////////////////////////////////////////////////////////////////////////////
			if ( this.getCollectedLeaveItems().isEmpty() ) 
    		{
    			/* IF THE DATA ENTRY FORM IS EMPTY 
    			 * AND THE LEAVE ITEMS COLLECTION IS EMPTY,
    			 * PROMPT THE USER TO ADD A LEAVE ITEM.
    			 * THE CALL TO "addOrEditNewLeaveItem()"
    			 * WILL HANDLE THIS CORRECTLY
    			 */
    			String errMsgText = ErrorMessages.getInstance().getMessage(ErrorMessages.ZERO_TOTAL_LEAVE_HOURS);
    			FacesMessage facesMsg = new FacesMessage(errMsgText, errMsgText);
    			facesMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
    			FacesContext.getCurrentInstance().addMessage(null, facesMsg);
    			throw new ValidatorException(new FacesMessage());
    		}	
    		
			//////////////////////////////////////////////////////////////////////////////////////
			// Check to see if the Disabled Veteran Leave type was selected (leave type code '69')
			// and if so, validate against it.
			//////////////////////////////////////////////////////////////////////////////////////			
    		this.performDisabledVetValidation();
		}
		
		private void performDisabledVetValidation() throws ValidatorException, AlohaServerException {
			
			//////////////////////////////////////////////////////////////////////////////
			// IF user has selected the disabled veteran leave type, we need to validate:
			//////////////////////////////////////////////////////////////////////////////			
			if ( this.isHasDisabledVetLeaveViewItem() ) {
				
				DisabledVetLeaveInfo vetInfo = retrieveDisabledVetLeaveInfo();	
				
				/////////////////////////////////////////////////////////////////////////////////////////////////
				// If there isn't a row in the PAR_EDS_LV table for this employee that contains the 
				// Disabled Veteran Leave type code ('69'), then employee is not eligible for Disabled Veteran 
				// Leave so we need to throw a validation error
				/////////////////////////////////////////////////////////////////////////////////////////////////		
				if ( vetInfo == null ) {
					
					String errMsgText = ErrorMessages.getInstance().getMessage(ErrorMessages.LR_DISABLED_VET_RECORD_NOT_FOUND);
					FacesMessage facesMsg = new FacesMessage(errMsgText, errMsgText);
					facesMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
					FacesContext.getCurrentInstance().addMessage(null, facesMsg);
					throw new ValidatorException(new FacesMessage());
				}
				
				//////////////////////////////////////////////////////////////////////////////////////
				// If the last day of the selected pay period is later than the date held 
				// in the par_eds_lv.lv_used_beg_date column for this employee, then employee
				// is not eligible for Disabled Veteran Leave so we need to throw a validation error
				/////////////////////////////////////////////////////////////////////////////////////						
				
//				List<PayPeriodSchedule> ppSchedule = getPayPeriodScheduleList();
//				PayPeriodSchedule lastDayInPP = ppSchedule.get( ( ppSchedule.size() ) - 1 );
//				
//				if ( lastDayInPP.getCalendarDate().compareTo(vetInfo.getExpirationDate()) > 0 ) {
//					
//					//////////////////////////////////////////////////////////////////////////////////////////////////////////
//					// Build error message, passing in the expiration date contained in the par_eds_lv.lv_used_beg_date column
//					//////////////////////////////////////////////////////////////////////////////////////////////////////////					
//					Object[] params = { DateUtil.formatDate(vetInfo.getExpirationDate()) };
//					String errMsgText = ErrorMessages.getInstance().getMessage(ErrorMessages.LR_DISABLED_VET_RECORD_RECORD_EXPIRED, params);
//					
//					FacesMessage facesMsg = new FacesMessage(errMsgText, errMsgText);
//					facesMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
//					FacesContext.getCurrentInstance().addMessage(null, facesMsg);
//					throw new ValidatorException(new FacesMessageec());					
//				}
				
				////////////////////////////////////////////////////////////////////////
				// Check to make sure that Disabled Veteran Leave Record hasn't expired
				////////////////////////////////////////////////////////////////////////
				
				for ( LeaveViewItem lvi: this.getCollectedLeaveItems() ) {
					
					if ( lvi.isDisabledVetLeaveViewItem()) {

						if ( lvi.getLeaveDate().compareTo(vetInfo.getExpirationDate()) > 0 ) {
							
							Object[] params = { DateUtil.formatDate(lvi.getLeaveDate()), DateUtil.formatDate(vetInfo.getExpirationDate()) };
							String errMsgText = ErrorMessages.getInstance().getMessage(ErrorMessages.LR_DISABLED_VET_RECORD_RECORD_EXPIRED, params);
							
							FacesMessage facesMsg = new FacesMessage(errMsgText, errMsgText);
							facesMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
							FacesContext.getCurrentInstance().addMessage(null, facesMsg);
							throw new ValidatorException(new FacesMessage());							
						}						
					}
				}
				
				
				////////////////////////////////////////////////////////////////////////////////////////////////////				
				// If sum of all the Disabled Veteran Leave time requested for this pay period exceeds the
				// Disabled Veteran Leave balance held in the par_eds_lv_lv_hrs_bal column for this employee,
				// then we need to throw a validation error.
				////////////////////////////////////////////////////////////////////////////////////////////////////				
			
				if ( this.sumDiabledVetLeaveHours().compareTo( vetInfo.getLeaveBalance() ) > 0 ) {
					
					Object[] params = { vetInfo.getLeaveBalance() };
					String errMsgText = ErrorMessages.getInstance().getMessage(ErrorMessages.LR_DISABLED_VET_RECORD_INSUFFICIENT_LEAVE_BALANCES, params);
					FacesMessage facesMsg = new FacesMessage(errMsgText, errMsgText);
					facesMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
					FacesContext.getCurrentInstance().addMessage(null, facesMsg);
					throw new ValidatorException(new FacesMessage());						
				}
			}
		}
		
	    public BigDecimal sumDiabledVetLeaveHours() {
	    	return this.sumDiabledVetLeaveHours(this.getCollectedLeaveItems());
	    }
		
	    private BigDecimal sumDiabledVetLeaveHours(List<LeaveViewItem> lvItemList) {
	    	
	    	BigDecimal sum = BigDecimal.ZERO;
	    	
	    	for (LeaveViewItem lvItem : lvItemList) {
	    		
	    		if ( lvItem.isDisabledVetLeaveViewItem() ) {
	    			sum = sum.add(lvItem.getLeaveNumberOfHours());
	    		}
	    	}
	    	return sum;
	    }
		
	    protected void addOrEditNewLeaveItem() throws ValidatorException, AlohaServerException, ParseException {
	    	if (isInLeaveViewItemEditMode() ) {
	    		this.editLeaveViewItem();
	    	} else {
	    		this.addLeaveViewItems();
	    	}	    	
	    }
	    
	    protected void addLeaveViewItems() throws ValidatorException, AlohaServerException, ParseException {
	    	
	    	//System.out.println("LeaveViewItem.addLeaveViewItems(): BEGIN");
	    	
	    	int leaveItemApplyToValueSelection = Integer.parseInt(getLeaveItemApplyToSelection());
	    	this.populateLeaveViewItem(this.getSelectedLeaveItem(), leaveItemApplyToValueSelection);
	    	
	    	
	    	//System.out.println("this.getSelectedLeaveItem(): " + this.getSelectedLeaveItem());
	    	
	    	List<PayPeriodSchedule> employeeBaseSchedule = getPayPeriodScheduleList();
	    		    	
	    	switch(leaveItemApplyToValueSelection) {
	    		case LeaveViewItemApplyToValue.SPECIFIC_DATE:
	    			this.getCollectedLeaveItems().add(this.getSelectedLeaveItem());
	    			//this.getSelectedLeaveItem().setLeaveViewItemId(this.getCollectedLeaveItems().size());
	    			this.getSelectedLeaveItem().setLeaveViewItemId(assignLeaveViewItemId(this.getCollectedLeaveItems()));
	    			break;
	    		case LeaveViewItemApplyToValue.ENTIRE_PAY_PERIOD:
	    			this.fillLeaveRequestUsingLeaveViewItemAndEmployeeBaseSchedule
	    				(this.getCollectedLeaveItems(),  this.getSelectedLeaveItem(), employeeBaseSchedule, 0, 14);
	    			break;	
	    		case LeaveViewItemApplyToValue.FIRST_PAY_PERIOD:
	    			this.fillLeaveRequestUsingLeaveViewItemAndEmployeeBaseSchedule
   					(this.getCollectedLeaveItems(),  this.getSelectedLeaveItem(), employeeBaseSchedule, 0, 7);
	    			break;	 
	    		case LeaveViewItemApplyToValue.SECOND_PAY_PERIOD:
	    			this.fillLeaveRequestUsingLeaveViewItemAndEmployeeBaseSchedule
   					(this.getCollectedLeaveItems(),  this.getSelectedLeaveItem(), employeeBaseSchedule, 8, 14);
	    			break;	    			
	    	}
	    	
	    	// CONSOLIDATE AND SORT LIST
	        this.setCollectedLeaveItems(LeaveViewItem.consolidateAndSort(this.getCollectedLeaveItems()));
	        
	        this.selectedLeaveItem = new LeaveViewItem();
	        
	        //for ( LeaveViewItem lvItem : this.getCollectedLeaveItems() ) {
	        	//System.out.println("lvItem.getLeaveViewItemId(): " + lvItem.getLeaveViewItemId());
	        //}	  
	        
	        //System.out.println("LeaveViewItem.addLeaveViewItems(): END");
	    }
	    
	    protected void editLeaveViewItem() throws ValidatorException, ParseException, AlohaServerException {
	    	
	    	LeaveViewItem selectedLeaveViewItem = this.getSelectedLeaveItem();
	    	
	    	this.populateLeaveViewItem(selectedLeaveViewItem, LeaveViewItemApplyToValue.SPECIFIC_DATE);
	    	
	    	LeaveViewItem leaveItemAlreadyInList = this.getLeaveItemById(this.getEditLeaveItemId());
	    	
			leaveItemAlreadyInList.setLeaveDate(selectedLeaveViewItem.getLeaveDate());
			leaveItemAlreadyInList.setLeaveType(selectedLeaveViewItem.getLeaveType());
			leaveItemAlreadyInList.setLeaveStartTime(selectedLeaveViewItem.getLeaveStartTime());
			leaveItemAlreadyInList.setLeaveNumberOfHours(selectedLeaveViewItem.getLeaveNumberOfHours());
			
	    	// CONSOLIDATE AND SORT LIST
	        this.setCollectedLeaveItems(LeaveViewItem.consolidateAndSort(this.getCollectedLeaveItems()));
	    }
	    
	    public BigDecimal sumLeaveHours() {
	    	return this.sumLeaveHours(this.getCollectedLeaveItems());
	    }
	   
	    protected void prepareLeaveViewItemForEdit() {
	    	this.selectedLeaveItem = this.getLeaveItemById(this.getEditLeaveItemId());
	    }	    

	    protected void removeNewLeaveItem() {
	    	this.getCollectedLeaveItems().remove(this.getLeaveItemById(this.getRemoveLeaveItemId()));
	    	if ( this.getSelectedLeaveItem().getLeaveViewItemId() == this.getRemoveLeaveItemId()) {
	    		this.selectedLeaveItem = null;
	    		leaveViewItemMode = LeaveViewItemMode.ADD_MODE;
	    	}
	    }
	    
	    protected BigDecimal sumLeaveHours(List<LeaveViewItem> lvItemList) {
	    	BigDecimal sum = BigDecimal.ZERO;
	    	for (LeaveViewItem lvItem : lvItemList) {
	    		sum = sum.add(lvItem.getLeaveNumberOfHours());
	    	}
	    	return sum;
	    }
		protected void validateLeaveViewItem(LeaveViewItem lvItem, int leaveItemApplyToValueSelection) throws ValidatorException {
			int errorCount = 0;
			
			// LEAVE DATE
			if 	( (leaveItemApplyToValueSelection == LeaveViewItemApplyToValue.SPECIFIC_DATE) 
					&& ( (StringUtil.isNullOrEmpty(lvItem.getLeaveDateKey()) == true)
							|| ( lvItem.getLeaveDateKey().equals("0") == true) )
				) 
			{
				String errMsgText = ErrorMessages.getInstance().getMessage(ErrorMessages.LR_REQUIRED_LEAVE_DATE);
				FacesMessage facesMsg = new FacesMessage(errMsgText, errMsgText);
				facesMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
				FacesContext.getCurrentInstance().addMessage(null, facesMsg);
				errorCount++;
			}
			
			// LEAVE TYPE
			if ( (StringUtil.isNullOrEmpty(lvItem.getLeaveTypeKey()) == true )
					|| ( lvItem.getLeaveTypeKey().equals("0") == true )) 
			{
				String errMsgText = ErrorMessages.getInstance().getMessage(ErrorMessages.LR_REQUIRED_LEAVE_TYPE);
				FacesMessage facesMsg = new FacesMessage(errMsgText, errMsgText);
				facesMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
				FacesContext.getCurrentInstance().addMessage(null, facesMsg);
				errorCount++;				
			}
			
			// LEAVE HOURS
			if ( StringUtil.isNullOrEmpty(lvItem.getLeaveNumberOfHoursAsString()) == true) 
			{
				String errMsgText = ErrorMessages.getInstance().getMessage(ErrorMessages.LR_REQUIRED_LEAVE_HOURS);
				FacesMessage facesMsg = new FacesMessage(errMsgText, errMsgText);
				facesMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
				FacesContext.getCurrentInstance().addMessage(null, facesMsg);
				errorCount++;					
			} else 
			{
				if ( NumberUtil.isBigDecimal(lvItem.getLeaveNumberOfHoursAsString()) == false)
				{
					String errMsgText = ErrorMessages.getInstance().getMessage(ErrorMessages.LR_LEAVE_HOURS_NAN);
					FacesMessage facesMsg = new FacesMessage(errMsgText, errMsgText);
					facesMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
					FacesContext.getCurrentInstance().addMessage(null, facesMsg);
					errorCount++;					
				}
				else 
				{
					if ( (new BigDecimal(lvItem.getLeaveNumberOfHoursAsString()).compareTo(LeaveItem.MIN_LEAVE_HOURS) == -1) ) {
						String errMsgText = ErrorMessages.getInstance().getMessage(ErrorMessages.LR_LEAVE_HOURS_ZERO);
						FacesMessage facesMsg = new FacesMessage(errMsgText, errMsgText);
						facesMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
						FacesContext.getCurrentInstance().addMessage(null, facesMsg);
						errorCount++;
					}
				}
			}
			
			if ( errorCount > 0) {
				throw new ValidatorException(new FacesMessage());
			}	
	    }	
		
	    protected void fillLeaveRequestUsingLeaveViewItemAndEmployeeBaseSchedule(List<LeaveViewItem> leaveViewItemList, LeaveViewItem lvItem, 
	    		List<PayPeriodSchedule> employeeBaseSchedule, int startIndex, int endIndex) throws AlohaServerException {
			for (int index = startIndex; index < endIndex; index++) {
				PayPeriodSchedule payPeriodDay = employeeBaseSchedule.get(index);
				
				if ( payPeriodDay.isWorkDay() ) {
					LeaveViewItem newLeaveViewItem = new LeaveViewItem();
					newLeaveViewItem.setLeaveDate(payPeriodDay.getCalendarDate());
					newLeaveViewItem.setLeaveType(lvItem.getLeaveType());
					newLeaveViewItem.setLeaveStartTime(lvItem.getLeaveStartTime());
					newLeaveViewItem.setLeaveNumberOfHours(lvItem.getLeaveNumberOfHours());
					newLeaveViewItem.setLeaveViewItemId(assignLeaveViewItemId(leaveViewItemList));
					leaveViewItemList.add(newLeaveViewItem);	 
					
				}
			}
	    }	

		public void populateLeaveViewItem( LeaveViewItem lvItem, int leaveItemApplyToValueSelection) throws ValidatorException, ParseException, AlohaServerException {
	    	
			// VALIDATE FIRST
			this.validateLeaveViewItem(lvItem, leaveItemApplyToValueSelection);
	    	
			// THEN POPULATE
			if 	(	( StringUtil.isNullOrEmpty(lvItem.getLeaveTypeKey()) == false )
	    			&& ( lvItem.getLeaveTypeKey().equals("0") == false )
	    			&& ( StringUtil.isNullOrEmpty(lvItem.getLeaveNumberOfHoursAsString()) == false) 
	    			&& ( NumberUtil.isBigDecimal(lvItem.getLeaveNumberOfHoursAsString()) == true)
	    			&& ( (new BigDecimal(lvItem.getLeaveNumberOfHoursAsString()).compareTo(LeaveItem.MIN_LEAVE_HOURS) >= 0))
	    		) 
	    	{
		    	// LEAVE DATE
	    		if 	( (leaveItemApplyToValueSelection == LeaveViewItemApplyToValue.SPECIFIC_DATE) ) {
	    			lvItem.setLeaveDate(
	    					DateUtil.convertStringToDate(
	    							lvItem.getLeaveDateKey(), DateUtil.DateFormats.YYYYMMDD));    			
	    		}
		    		
				// LEAVE TYPE
				String selectedLeaveTypeCode = lvItem.getLeaveTypeKey();
				if ( selectedLeaveTypeCode.length() == 2) {
					lvItem.setLeaveType(getLeaveTypeMB().getLeaveType(selectedLeaveTypeCode));
				} else if (selectedLeaveTypeCode.length() == 4) {
					lvItem.setLeaveType(
							getLeaveTypeMB().getLeaveType(
								selectedLeaveTypeCode.substring(0,2), selectedLeaveTypeCode.substring(2)));
				}

		    		
				// NUMBER OF HOURS
				BigDecimal leaveHours = new BigDecimal(lvItem.getLeaveNumberOfHoursAsString());
				lvItem.setLeaveNumberOfHours(leaveHours.setScale(1, RoundingMode.HALF_UP));

				// START TIME
				if ( (StringUtil.isNullOrEmpty(lvItem.getLeaveStartTimeKey()) == false) 
						&& (!lvItem.getLeaveStartTimeKey().equals(LeaveViewItem.UNSELECTED_START_TIME_KEY)) ) {
					GregorianCalendar startCal = new GregorianCalendar();
					startCal.setTime(new Date());
					startCal.set(Calendar.HOUR_OF_DAY, 0);
					startCal.set(Calendar.MINUTE, 0);					
					startCal.set(Calendar.SECOND, 0);
					startCal.set(Calendar.MILLISECOND, 0);
					
					// SET HOUR
					String strHour = lvItem.getLeaveStartTimeKey().substring(0, 2);
					Integer intHour = Integer.valueOf(strHour);
					startCal.set(Calendar.HOUR_OF_DAY, intHour.intValue());
					
					// SET MINUTE
					String strMinute = lvItem.getLeaveStartTimeKey().substring(2, 4);
					Integer intMinute = Integer.valueOf(strMinute);
					startCal.set(Calendar.MINUTE, intMinute.intValue());

					lvItem.setLeaveStartTime(startCal.getTime());
				} else {
					lvItem.setLeaveStartTime(null);
				}
	    	} 		
		}
	}
}