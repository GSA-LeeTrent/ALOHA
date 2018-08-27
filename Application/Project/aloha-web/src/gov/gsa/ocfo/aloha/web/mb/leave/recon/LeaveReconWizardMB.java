package gov.gsa.ocfo.aloha.web.mb.leave.recon;

import gov.gsa.ocfo.aloha.ejb.leave.recon.LeaveReconWizardEJB;
import gov.gsa.ocfo.aloha.exception.AlohaServerException;
import gov.gsa.ocfo.aloha.exception.LeaveReconException;
import gov.gsa.ocfo.aloha.model.entity.AlohaUser;
import gov.gsa.ocfo.aloha.model.entity.leave.recon.LeaveReconWizard;
import gov.gsa.ocfo.aloha.model.entity.leave.recon.LeaveReconWizardItem;
import gov.gsa.ocfo.aloha.model.entity.leave.recon.LeaveReconWizardItem.SystemIsCorrectValues;
import gov.gsa.ocfo.aloha.model.entity.leave.recon.LeaveReconWizardOutcome;
import gov.gsa.ocfo.aloha.model.entity.leave.recon.LeaveReconWizardOutcome.LeaveReconTransactionType;
import gov.gsa.ocfo.aloha.web.mb.UserMB;
import gov.gsa.ocfo.aloha.web.security.NavigationOutcomes;
import gov.gsa.ocfo.aloha.web.util.AlohaURIs;
import gov.gsa.ocfo.aloha.web.util.ErrorMessages;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;

@ManagedBean(name=LeaveReconWizardMB.MANAGED_BEAN_NAME, eager=false)
@ViewScoped
public class LeaveReconWizardMB implements Serializable {

	/***********************************************
	 * Class Members
	 ***********************************************/
	private static final long serialVersionUID = -5196377268617163021L;
	public static final String MANAGED_BEAN_NAME = "lrReconWizardMB";
	
	/***********************************************
	 * Helper Enterprise Jave Beans (EJBs)
	 ***********************************************/
	@EJB 
	private LeaveReconWizardEJB leaveReconWizardEJB;
	
	/***********************************************
	 * Helper JSF Managed Beans (MBs)
	 ***********************************************/
	@ManagedProperty(value="#{userMB}")
	private UserMB userMB;
	public void setUserMB(UserMB userMB) {
		this.userMB = userMB;
	}
	
	/***********************************************
	 * Instance Members
	 ***********************************************/
	private List<LeaveReconWizard> leaveReconWizardList;
	private int currentDiscrepancyRecordIndex;
	
	
	private boolean pageZero;
	public boolean isPageZero() {
		return pageZero;
	}

	
	private boolean pageOne;
	public boolean isPageOne() {
		return pageOne;
	}

	private boolean pageTwo;
	public boolean isPageTwo() {
		return pageTwo;
	}

	
	private boolean pageThree;
	public boolean isPageThree() {
		return pageThree;
	}

	private boolean pageFour;
	public boolean isPageFour() {
		return pageFour;
	}

	
	private boolean skipWarningDisplayed = false;
	public boolean isSkipWarningDisplayed() {
		return skipWarningDisplayed;
	}

	private boolean homeWarningDisplayed = false;
	public boolean isHomeWarningDisplayed() {
		return homeWarningDisplayed;
	}
	
	////////////////////////////////////////////////////////////////////
	// BEGIN: DISABLED VETERAN LEAVE TYPE AND CERTIFICATION
	////////////////////////////////////////////////////////////////////
	protected boolean disabledVetCertified = false;
	public boolean isDisabledVetCertified() {
		return disabledVetCertified;
	}
	public void setDisabledVetCertified(boolean disabledVetCertified) {
		this.disabledVetCertified = disabledVetCertified;

		//////////////////////////////////////////////////////////////		
		// Set the correct model object (current discrepancy record)
		// with this boolean value
		//////////////////////////////////////////////////////////////
		LeaveReconWizard currentDiscrepancRecord = this.getCurrentDiscrepancyRecord();
		if ( currentDiscrepancRecord != null ) {
			currentDiscrepancRecord.setDisabledVetCertified(this.disabledVetCertified);
		}
	}

//	public boolean isHasDisabledVetLeaveViewItem() { 
//		LeaveReconWizard currentDiscrepancRecord = this.getCurrentDiscrepancyRecord();
//		if ( currentDiscrepancRecord != null ) {
//			
//			return currentDiscrepancRecord.isHasDisabledVetLeaveViewItem();
//		}
//		return false;
//	}

	public boolean isHasDisabledVetLeaveViewItem() { 
		
		LeaveReconWizard currentDiscrepancRecord = this.getCurrentDiscrepancyRecord();
		
		if ( currentDiscrepancRecord != null 
				&& currentDiscrepancRecord.isHasDisabledVetLeaveViewItem() ) {
			
			List<LeaveReconWizardItem> lrWizarditems = currentDiscrepancRecord.getLeaveReconWizardItems();
		
			for ( LeaveReconWizardItem lrItem: lrWizarditems) {
				
				if ( lrItem.isDisabledVetLeaveViewItem() ) {

					if ( Integer.parseInt(lrItem.getWhichSystemIsCorrect()) == SystemIsCorrectValues.ALOHA_IS_CORRECT // ALOHA IS CORRECT
							&& lrItem.getAlohaLeaveHours().compareTo(BigDecimal.ZERO) > 0 ) {
							return true;
					}
					if ( Integer.parseInt(lrItem.getWhichSystemIsCorrect()) == SystemIsCorrectValues.ETAMS_IS_CORRECT // ETAMS IS CORRECT
							&& lrItem.getEtamsLeaveHours().compareTo(BigDecimal.ZERO) > 0 ) {
							return true;
					}	
					if ( Integer.parseInt(lrItem.getWhichSystemIsCorrect()) == SystemIsCorrectValues.NEITHER_IS_CORRECT // NEITHER IS CORRECT
							&& ( lrItem.getAlohaLeaveHours().compareTo(BigDecimal.ZERO) > 0
									|| lrItem.getEtamsLeaveHours().compareTo(BigDecimal.ZERO) > 0 ) ) {
							return true;
					}
				}
			}
			return false;
		}
		return false;
	}	
	
	
	////////////////////////////////////////////////////////////////////
	// END: DISABLED VETERAN LEAVE TYPE AND CERTIFICATION
	////////////////////////////////////////////////////////////////////	

	/***********************************************
	 * Messages
	 ***********************************************/
	private String ALOHA_SUCCESS = "You have successfully submitted your ALOHA request(s).";
	private String ETAMS_SUCCESS = "You have successfully submitted your ETAMS amendment.";
	private String ETAMS_FAILURE = "ETAMS Amendment creation failed.";
	private String SUPERVISOR_ACTION_NEEDED = "Please work with your timekeeper or supervisor to create amendment manually in ETAMS.";
	private String ETAMS_SUCCESS_VALUE = "S";
	private String ETAMS_FAILURE_VALUE = "F";
	

	/***********************************************
	 * Lifecyle Method
	 ***********************************************/
	@PreDestroy
	public void cleanup() {
		this.leaveReconWizardList = null;
		this.currentDiscrepancyRecordIndex = -1;
	}
	
	/***********************************************
	 * Lifecyle Method
	 ***********************************************/	
	@PostConstruct
	public void init() {
		
		try{
			
			///////////////////////////////////////////////////////
			// Setting this flag false is no longer needed and
			// is causing an issue if user closes browser tab
			// when user is on the first page of the wizard.
			///////////////////////////////////////////////////////
			//this.userMB.getUser().setShowLeaveReconWizard(false);
			///////////////////////////////////////////////////////
			
			this.userMB.setShowHeaderAndFooterLinks(false);
			this.leaveReconWizardList = this.leaveReconWizardEJB.retrieveByEmployeeUserId(this.userMB.getUserId());

			if ( (this.leaveReconWizardList != null)
					&& (leaveReconWizardList.isEmpty() == false) ) {
				this.currentDiscrepancyRecordIndex = 0;
				//this.debug();
				
				
				/*System.out.println("Inside MB init -- showing page one");*/
				//if there are discrepancies show page one
				this.pageZero = false;
				this.pageOne = true;
				this.pageTwo = false;
				this.pageThree = false;
				this.pageFour = false;

			} else {
				this.currentDiscrepancyRecordIndex = -1;
				
				/*System.out.println("Inside MB init -- showing page zero");*/
				//if there are no discrepancies show page zero
				this.pageZero = true;
				this.pageOne = false;
				this.pageTwo = false;
				this.pageThree = false;
				this.pageFour = false;
			}
		} catch (LeaveReconException lre) {
			try {
				lre.printStackTrace();
				FacesContext.getCurrentInstance().getExternalContext().redirect(AlohaURIs.SERVER_ERROR);
			} catch (IOException ignore) {}
		} catch (Throwable t) {
			try {
				t.printStackTrace();
				FacesContext.getCurrentInstance().getExternalContext().redirect(AlohaURIs.SERVER_ERROR);
			} catch (IOException ignore) {}
		}
	}
	
//	private void debug() {
//		System.out.println("BEGIN: " + this.getClass().getName() );
//		System.out.println("currentDiscrepancyRecordIndex: " + this.currentDiscrepancyRecordIndex );
//		System.out.println("this.leaveReconWizardList.size(): " + this.leaveReconWizardList.size() );
//		
//		if ( (this.leaveReconWizardList != null)
//				&& (this.leaveReconWizardList.isEmpty() == false)) {
//			
//			int index = 0;
//			for ( LeaveReconWizard wizard: this.leaveReconWizardList) {
//				System.out.println("-------------------------------------------------------------------------");
//				System.out.println("Record # " + index);
//				System.out.println("-------------------------------------------------------------------------");
//				System.out.println(wizard);
//				System.out.println("-------------------------------------------------------------------------");
//				index++;
//			}
//		} else {
//			System.out.println("leaveReconWizardList is NULL");
//		}
//		System.out.println("END: " + this.getClass().getName() );
//	}


	public LeaveReconWizard getCurrentDiscrepancyRecord() {
		if ( (this.currentDiscrepancyRecordIndex >= 0)
				&& (this.currentDiscrepancyRecordIndex <= (this.leaveReconWizardList.size() - 1) ) ) {
			return this.leaveReconWizardList.get(this.currentDiscrepancyRecordIndex);
		} else {
			return null;
		}
	}
	
	public List<LeaveReconWizardItem> getCurrentDiscrepancyItems() {
		
		LeaveReconWizard currentDiscrepancyRecord = this.getCurrentDiscrepancyRecord();
		if ( currentDiscrepancyRecord != null) {
			return currentDiscrepancyRecord.getLeaveReconWizardItems();
		} else {
			return null;
		}
	}	

	public void onSkipToNextDiscrepancy() {
		homeWarningDisplayed = false;
		
		//if on page 4, don't check skip warning
		if (this.pageFour == true){
			//don't display warning
			skipWarningDisplayed = false;
			this.currentDiscrepancyRecordIndex++;
			this.pageZero = false;
			this.pageOne = false;
			this.pageTwo = true;
			this.pageThree = false;
			this.pageFour = false;		
		}
		else
			//all other pages check the skip warning first
		{

			//if skip warning is displayed, go ahead and skip to next discrepancy
			if (this.skipWarningDisplayed == true)
			{
				//after displaying it once, don't display it next time
				skipWarningDisplayed = false;
				this.currentDiscrepancyRecordIndex++;
				this.pageZero = false;
				this.pageOne = false;
				this.pageTwo = true;
				this.pageThree = false;
				this.pageFour = false;		
			}
			else {
				//if it's not displayed, display it before processing
				this.skipWarningDisplayed = true;
			}	
		}
	}

/* this method bypasses all pages of the wizard. It is used from page zero because there are no discrepancies*/
	public String onGoDirectlyToHomePage() {
		/*System.out.println("In onGoDirectlyToHomePage");*/
		
		try {
			this.userMB.setShowHeaderAndFooterLinks(true);
			return NavigationOutcomes.HOME_PAGE;

		} catch (Exception ignore) {
			System.out.println(ignore);
		}
		
		return null;
	}
	
	/* this method is used from within the wizard, and it displays a warning first*/	
	public String onContinueToHomePage() {
		
		skipWarningDisplayed = false;
	
		/*System.out.println("In onContinueToHomePage: "+ homeWarningDisplayed);*/
	
		//if on page 4, don't check home warning
		if (this.pageFour == true){
			//don't display warning
			/*System.out.println("Checkpoint A: " + homeWarningDisplayed);*/
			homeWarningDisplayed = false;

			//todo: possibly reset if all discrepancies are resolved
			
			
			try {
				this.userMB.setShowHeaderAndFooterLinks(true);
				return NavigationOutcomes.HOME_PAGE;

			} catch (Exception ignore) {
				
				System.out.println(ignore);
				
			}
		}
		else
			//all other pages check the home warning first
		{

			/* System.out.println("Checkpoint B: " + homeWarningDisplayed); */
			
			//if  warning is displayed, go ahead and continue to home
			if (this.homeWarningDisplayed == true)
			{
				//after displaying it once, don't display it next time
				homeWarningDisplayed = false;
				/*System.out.println("Checkpoint c: " + homeWarningDisplayed); */
				
				try {
					
					this.userMB.setShowHeaderAndFooterLinks(true);
					return NavigationOutcomes.HOME_PAGE;
				
				} catch (Exception ignore) {
					
					System.out.println(ignore);
				}
			}
			else {
				//if it's not displayed, display it before processing
				this.homeWarningDisplayed = true;
				/* System.out.println("Checkpoint D: " + homeWarningDisplayed);*/
				return null;
			}	
		}
		return null;
	}
	
	
	
	public boolean isHasNextDiscrepancy() {
		return ( this.currentDiscrepancyRecordIndex < (this.leaveReconWizardList.size() - 1) );
	}
	

	public void onReviewChanges() {
		/*System.out.println("IN: " + this.getClass().getName() + ".onReviewChanges()" );*/
		
		try {
			
			//reset warning
			skipWarningDisplayed = false;
			homeWarningDisplayed = false;
			
			this.performValidation();
			
			//set the input values to match the selected
			List<LeaveReconWizardItem> lrWizarditems = this.getCurrentDiscrepancyRecord().getLeaveReconWizardItems();
			for ( LeaveReconWizardItem lrItem: lrWizarditems) {
				
				if (Integer.parseInt(lrItem.getWhichSystemIsCorrect()) == SystemIsCorrectValues.ALOHA_IS_CORRECT)//aloha
				{
					//System.out.println("LeaveReconWizardMB - debug 1");
					lrItem.setDisplayAlohaLeaveHours(lrItem.getAlohaLeaveHours());
					lrItem.setDisplayEtamsLeaveHours(lrItem.getAlohaLeaveHours());
				}
				//if (lrItem.getWhichSystemIsCorrect().equals("2")) //etams
				if (Integer.parseInt(lrItem.getWhichSystemIsCorrect()) == SystemIsCorrectValues.ETAMS_IS_CORRECT) //etams
				{
					//System.out.println("LeaveReconWizardMB - debug 2");
					lrItem.setDisplayAlohaLeaveHours(lrItem.getEtamsLeaveHours());
					lrItem.setDisplayEtamsLeaveHours(lrItem.getEtamsLeaveHours());
				}
				//if (lrItem.getWhichSystemIsCorrect().equals("3"))//neither
				if (Integer.parseInt(lrItem.getWhichSystemIsCorrect()) == SystemIsCorrectValues.NEITHER_IS_CORRECT)//neither
				{
					lrItem.setDisplayEtamsLeaveHours( new BigDecimal(lrItem.getInputAlohaLeaveHours()) );
					lrItem.setDisplayAlohaLeaveHours( new BigDecimal(lrItem.getInputEtamsLeaveHours()) );
				}
			}	
		
			// If validation passes, take user to page 3
			
			this.pageZero = false;
			this.pageOne = false;
			this.pageTwo = false;
			this.pageThree = true;
			this.pageFour = false;	

		} catch(ValidatorException ve) {
			// Take user back to page 2 to fix validation errors
			this.pageOne = false;
			this.pageTwo = true;
			this.pageThree = false;
			this.pageFour = false;		
	
		} catch (AlohaServerException ase) {
			try {
				if ( this.userMB != null && this.userMB.getUser() != null) {
					System.out.println(this.userMB.getUser());
				}				
				ase.printStackTrace();
				FacesContext.getCurrentInstance().getExternalContext().redirect(AlohaURIs.SERVER_ERROR);
			} catch (IOException ignore) {
				
			}
		} catch (ParseException pe) {
			try {
				pe.printStackTrace();
				FacesContext.getCurrentInstance().getExternalContext().redirect(AlohaURIs.SERVER_ERROR);
			} catch (IOException ignore) {		
			}
		}
	} //on reviewChanges()
	
	public void onConfirm() {
		
		//reset warnings
		skipWarningDisplayed = false;
		homeWarningDisplayed = false;

		//make sure you set the correctLeaveHours on all the items in currentDiscrepancyRecord
		
		//loop through all the records
		//for each record
			//find out which one they said was correct
			//set the correctLeaveHours to that value

		List<LeaveReconWizardItem> lrWizarditems = this.getCurrentDiscrepancyRecord().getLeaveReconWizardItems();
		for ( LeaveReconWizardItem lrItem: lrWizarditems) {
			
			if (lrItem.getWhichSystemIsCorrect().equals("0"))
			{
				System.out.println( this.getClass().getName() + " - ERROR -- entered confirm when no choice was selected" );
			}
			if (lrItem.getWhichSystemIsCorrect().equals("1"))//aloha
			{
				
				lrItem.setCorrectLeaveHours(lrItem.getAlohaLeaveHours());
			}
			if (lrItem.getWhichSystemIsCorrect().equals("2")) //etams
			{
				lrItem.setCorrectLeaveHours(lrItem.getEtamsLeaveHours());
			}
			if (lrItem.getWhichSystemIsCorrect().equals("3"))//neither
			{
				if (lrItem.getInputEtamsLeaveHours().compareTo(lrItem.getInputAlohaLeaveHours()) == 0) {
					//lrItem.setCorrectLeaveHours(lrItem.getInputAlohaLeaveHours());
					lrItem.setCorrectLeaveHours( new BigDecimal(lrItem.getInputAlohaLeaveHours()) );
				} else {
					StringBuilder errMsg = new StringBuilder();
					errMsg.append("ALOHA Input Hours and ETAMS Input Hours must be 'EQUAL' when the 'Neither' drop-down option is selected.");
					errMsg.append("\nlrItem.getInputAlohaLeaveHours(): ");
					errMsg.append(lrItem.getInputAlohaLeaveHours());
					errMsg.append("\nlrItem.getInputEtamsLeaveHours(): ");
					errMsg.append(lrItem.getInputEtamsLeaveHours());
					errMsg.append("\nCANNOT CONTINUE");
					throw new IllegalStateException(errMsg.toString());
				}
			}
		}	
			
		//then call leaveReconWizardEJB.doReconcillation(currentDiscrepancyRecord) and catch LeaveReconException;
		try {
			
			////////////////////////////////////////////////////////////
			// Check to see if disabled veteran leave has been selected,
			// and if so, make sure that the certification check box
			// was checked
			////////////////////////////////////////////////////////////			
			this.validateDisabledVetCertification();
			////////////////////////////////////////////////////////////		

			LeaveReconWizardOutcome lrOutcome = leaveReconWizardEJB.doReconciliation(this.userMB.getUser(), this.getCurrentDiscrepancyRecord());
			
			//ETAMS transaction			
			if (lrOutcome.getLeaveReconTransactionType() == LeaveReconTransactionType.ETAMS) {

				/* 5-20-2016 moved duplicate code to method*/
				handleETAMSTransaction(lrOutcome);
			}
			else if (lrOutcome.getLeaveReconTransactionType() == LeaveReconTransactionType.ALOHA) {
				//ALOHA trans only expected
				//if got here without LeaveReconException, ALOHA was successful
				FacesMessage facesMsg = new FacesMessage(ALOHA_SUCCESS, null);
				facesMsg.setSeverity(FacesMessage.SEVERITY_INFO);
				FacesContext.getCurrentInstance().addMessage(null, facesMsg);
			}
			else if ((lrOutcome.getLeaveReconTransactionType() == LeaveReconTransactionType.BOTH)){
				//both trans expected
				//if got here without LeaveReconException, ALOHA was successful
				FacesMessage facesMsg = new FacesMessage(ALOHA_SUCCESS, null);
				facesMsg.setSeverity(FacesMessage.SEVERITY_INFO);
				FacesContext.getCurrentInstance().addMessage(null, facesMsg);

				/* 5-20-2016 moved duplicate code to method*/
				handleETAMSTransaction(lrOutcome);
			}
			
			//todo: if does not have next discrepancy, prepare them for page one next time

			this.pageZero = false;
			this.pageOne = false;
			this.pageTwo = false;
			this.pageThree = false;
			this.pageFour = true;
		} 
		catch (ValidatorException valEx) 
		{
			
			this.pageZero = false;
			this.pageOne = false;
			this.pageTwo = false;
			this.pageThree = true;
			this.pageFour = false;	
		}
		catch (LeaveReconException e) 
		{
			try {
				if ( this.userMB != null && this.userMB.getUser() != null) {
					System.out.println(this.userMB.getUser());
				}				
				e.printStackTrace();
				this.userMB.setShowHeaderAndFooterLinks(true);
				FacesContext.getCurrentInstance().getExternalContext().redirect(AlohaURIs.SERVER_ERROR);
			} catch (IOException ignore) {}
		}
	}
	
	public void onMakeRevisions() {
		/*System.out.println("IN: " + this.getClass().getName() + ".onMakeRevisions()" );*/

		skipWarningDisplayed = false;
		homeWarningDisplayed = false;
		this.pageOne = false;
		this.pageTwo = true;
		this.pageThree = false;
		this.pageFour = false;
	}

	public void onContinueFromPageOne() {
		/*System.out.println("IN: " + this.getClass().getName() + ".onContinueFromPageOne()" );*/
		skipWarningDisplayed = false;
		homeWarningDisplayed = false;
		this.pageZero = false;
		this.pageOne = false;
		this.pageTwo = true;
		this.pageThree = false;
		this.pageFour = false;		
	}

	private void performValidation() throws ValidatorException, AlohaServerException, ParseException {
		int errorCount = 0;
		
		/*System.out.println("IN: " + this.getClass().getName() + ".performValidation()" );*/
		
		List<LeaveReconWizardItem> lrWizarditems = getCurrentDiscrepancyRecord().getLeaveReconWizardItems();
		
		String inputAloha;
		String inputETAMS;
		 
		int numAlohaDecimals;
		int numEtamsDecimals;
		
		for ( LeaveReconWizardItem lrItem: lrWizarditems) {
	
				//lrItem = lrWizarditems.get(i);
				SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
				String dateString = format.format(lrItem.getLeaveDate());
					
				//none selected
				if ( lrItem.getWhichSystemIsCorrect().equals("0")) {
	
					//System.out.println("Point A" );
					String errMsgText = dateString + ": " +ErrorMessages.getInstance().getMessage(ErrorMessages.LR_RECON_NO_CHOICE_SELECTED);
					FacesMessage facesMsg = new FacesMessage(errMsgText, errMsgText);
					facesMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
					FacesContext.getCurrentInstance().addMessage(null, facesMsg);
					errorCount++;		
				}
				//if they picked neither
				//a. need to make sure ALOHA hours and ETAMS hours match
				if ( lrItem.getWhichSystemIsCorrect().equals("3")) {
					
					//System.out.println("Point B" );
					
					//if (lrItem.getInputAlohaLeaveHours() == null|| lrItem.getInputEtamsLeaveHours() == null) {
					if (lrItem.getInputAlohaLeaveHours().isEmpty()|| lrItem.getInputEtamsLeaveHours().isEmpty()) {
						
						//System.out.println("Point c" );
						
						String errMsgText = dateString + ": " +ErrorMessages.getInstance().getMessage(ErrorMessages.LR_RECON_NO_VALUE_ENTERED);
						FacesMessage facesMsg = new FacesMessage(errMsgText, errMsgText);
						facesMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
						FacesContext.getCurrentInstance().addMessage(null, facesMsg);
						errorCount++;	
				 }
				else if (lrItem.getInputAlohaLeaveHours().contains(" ")|| lrItem.getInputEtamsLeaveHours().contains(" ")) {
					
					String errMsgText = dateString + ": " +ErrorMessages.getInstance().getMessage(ErrorMessages.LR_RECON_NO_SPACES_ALLOWED);
					FacesMessage facesMsg = new FacesMessage(errMsgText, errMsgText);
					facesMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
					FacesContext.getCurrentInstance().addMessage(null, facesMsg);
					errorCount++;	
				}
				else if (!isNumeric(lrItem.getInputAlohaLeaveHours())|| !isNumeric(lrItem.getInputEtamsLeaveHours())) {
					//System.out.println("Point D" );
					
					String errMsgText =  dateString + ": " +ErrorMessages.getInstance().getMessage(ErrorMessages.LR_RECON_ALOHAETAMS_MUST_BE_NUMERIC);
					FacesMessage facesMsg = new FacesMessage(errMsgText, errMsgText);
					facesMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
					FacesContext.getCurrentInstance().addMessage(null, facesMsg);
					errorCount++;	
				}
				else if (lrItem.getInputEtamsLeaveHours().compareTo(lrItem.getInputAlohaLeaveHours()) != 0) {
						//System.out.println("Point D" );
						
						String errMsgText = dateString + ": " +ErrorMessages.getInstance().getMessage(ErrorMessages.LR_RECON_HOURS_DONT_MATCH);
						FacesMessage facesMsg = new FacesMessage(errMsgText, errMsgText);
						facesMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
						FacesContext.getCurrentInstance().addMessage(null, facesMsg);
						errorCount++;	
				}
				//b. need to make sure the hours are different than what came in (otherwise they should have picked something different from drop down)
				else if (lrItem.getEtamsLeaveHours().compareTo(new BigDecimal(lrItem.getInputEtamsLeaveHours())) == 0) {
					//System.out.println("Point E" );
					String errMsgText = dateString + ": " + ErrorMessages.getInstance().getMessage(ErrorMessages.LR_RECON_ETAMS_NOT_CHANGED);
					FacesMessage facesMsg = new FacesMessage(errMsgText, errMsgText);
					facesMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
					FacesContext.getCurrentInstance().addMessage(null, facesMsg);
					errorCount++;	
				}
				else if (lrItem.getAlohaLeaveHours().compareTo(new BigDecimal(lrItem.getInputAlohaLeaveHours())) == 0) {
					//System.out.println("Point F" );
					String errMsgText = dateString + ": " +ErrorMessages.getInstance().getMessage(ErrorMessages.LR_RECON_ALOHA_NOT_CHANGED);
					FacesMessage facesMsg = new FacesMessage(errMsgText, errMsgText);
					facesMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
					FacesContext.getCurrentInstance().addMessage(null, facesMsg);
					errorCount++;	
				}
				else if (new BigDecimal(lrItem.getInputAlohaLeaveHours()).compareTo(new BigDecimal(12.0)) == 1 
						|| new BigDecimal(lrItem.getInputEtamsLeaveHours()).compareTo(new BigDecimal(12.0)) == 1) {
							
					String errMsgText = dateString + ": " +ErrorMessages.getInstance().getMessage(ErrorMessages.LR_RECON_NUMBER_TOO_LARGE);
						FacesMessage facesMsg = new FacesMessage(errMsgText, errMsgText);
						facesMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
						FacesContext.getCurrentInstance().addMessage(null, facesMsg);
						errorCount++;	
				}
				/*System.out.println("ALOHA value: " + lrItem.getInputAlohaLeaveHours().toString());
				System.out.println("ETAMS value: " + lrItem.getInputEtamsLeaveHours().toString());*/
					
				else if (!lrItem.getInputAlohaLeaveHours().isEmpty()  && !lrItem.getInputEtamsLeaveHours().isEmpty()) {
	
					 /*inputAloha = lrItem.getInputAlohaLeaveHours().toString();
					 inputETAMS = lrItem.getInputEtamsLeaveHours().toString();*/
					 
					 inputAloha = lrItem.getInputAlohaLeaveHours();
					 inputETAMS = lrItem.getInputEtamsLeaveHours();
					
					 int inputAlohaIndex = inputAloha.indexOf(".");
					 int inputETAMSIndex = inputETAMS.indexOf(".");
					 
					 /*int index = string.indexOf(".");
					    return index < 0 ? 0 : string.length() - index - 1;*/
					 
					 numAlohaDecimals = (inputAlohaIndex < 0 ? 0 : inputAloha.length() - inputAloha.indexOf(".") - 1);
					 numEtamsDecimals = (inputETAMSIndex < 0? 0: inputETAMS.length() - inputETAMS.indexOf(".") - 1);
					
					//verify format of fields
					//else if (!isNumeric(lrItem.getInputAlohaLeaveHours().toString())|| !isNumeric(lrItem.getInputEtamsLeaveHours().toString())) {
					if (numAlohaDecimals > 1 || numEtamsDecimals > 1) {
						
						/*System.out.println("numAlohaDecimals part deux: " + numAlohaDecimals);
						System.out.println("numEtamsDecimals part deux: " + numEtamsDecimals);*/
						
						String errMsgText = dateString + ": " +ErrorMessages.getInstance().getMessage(ErrorMessages.LR_RECON_TOO_MANY_DECIMALS);
						FacesMessage facesMsg = new FacesMessage(errMsgText, errMsgText);
						facesMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
						FacesContext.getCurrentInstance().addMessage(null, facesMsg);
						errorCount++;
						
					}
				}  //if not null values
			}
		}

		if ( errorCount > 0) {

			throw new ValidatorException(new FacesMessage());
			}
	}
	
	
	boolean isNumeric(String str)  
	{  
	  try  
	  {  
	    //double d = Double.parseDouble(str);  
		 Double.parseDouble(str);
	  }  
	  catch(NumberFormatException nfe)  
	  {  
	    return false;  
	  }  
	  return true;  
	}

/*this is reusable code that handles the ETAMS outcome*/
	void handleETAMSTransaction(LeaveReconWizardOutcome outcome)
	{
		/*System.out.println("Inside handleETAMSTransaction");
		System.out.println("ETAMS Message: " + outcome.getEtamsMessage() );*/
		
		//scenario 1 -- ETAMS success
		//add standard success message to messages
		if (outcome.getEtamsMessage().equals(ETAMS_SUCCESS_VALUE)) {

			//add the ETAMS success flag returned
			FacesMessage facesMsg = new FacesMessage(ETAMS_SUCCESS, null);
			facesMsg.setSeverity(FacesMessage.SEVERITY_INFO);
			FacesContext.getCurrentInstance().addMessage(null, facesMsg);
		}
		else {
			
			//scenario 2 -- ETAMS failure flag returned
			//add standard failure message and supervisor action message to messsages
			if (outcome.getEtamsMessage().equals(ETAMS_FAILURE_VALUE))
			{
			//add the ETAMS failure message
			FacesMessage facesMsg = new FacesMessage(ETAMS_FAILURE, null);
			facesMsg.setSeverity(FacesMessage.SEVERITY_INFO);
			FacesContext.getCurrentInstance().addMessage(null, facesMsg);
			
			//Add the supervisor action message
			facesMsg = new FacesMessage(SUPERVISOR_ACTION_NEEDED,null);
			FacesContext.getCurrentInstance().addMessage(null, facesMsg);
			}

			//scenario 3 -- message other than success flag or failure flag returned 
			//add the message to the messages
			else {
				FacesMessage facesMsg = new FacesMessage(outcome.getEtamsMessage(), null);
				facesMsg.setSeverity(FacesMessage.SEVERITY_INFO);
				FacesContext.getCurrentInstance().addMessage(null, facesMsg);
			}
		}
	}
	
	public boolean isInSubmitOwnMode() {
		
		LeaveReconWizard currentDiscrepancRecord = this.getCurrentDiscrepancyRecord();
		AlohaUser currentUser = this.userMB.getUser();
		
		if ( currentDiscrepancRecord != null && currentUser != null ) {

			// If we're in "SubmitOwn" mode, then employee id and user id will be equal
			return ( ( (long) currentDiscrepancRecord.getEmployeeUserId() ) == currentUser.getUserId() );
		}
		return false;
	}
	
	public boolean isInOnBehalfOfMode() {
		
		LeaveReconWizard currentDiscrepancRecord = this.getCurrentDiscrepancyRecord();
		AlohaUser currentUser = this.userMB.getUser();
		
		if ( currentDiscrepancRecord != null && currentUser != null ) {
			
			// If we're in "OnBehalfOf" mode, then employee id and user id will NOT be equal
			return ( ( (long) currentDiscrepancRecord.getEmployeeUserId() ) != currentUser.getUserId() );
		}		
		
		return false;
		
	}

	private void validateDisabledVetCertification() throws ValidatorException {
		
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
	}
}
