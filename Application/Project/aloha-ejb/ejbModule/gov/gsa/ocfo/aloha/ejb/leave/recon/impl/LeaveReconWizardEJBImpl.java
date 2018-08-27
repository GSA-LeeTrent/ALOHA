package gov.gsa.ocfo.aloha.ejb.leave.recon.impl;

import gov.gsa.ocfo.aloha.ejb.leave.recon.EtamsAmendmentsEJB;
import gov.gsa.ocfo.aloha.ejb.leave.recon.LeaveReconAmendEJB;
import gov.gsa.ocfo.aloha.ejb.leave.recon.LeaveReconCreateEJB;
import gov.gsa.ocfo.aloha.ejb.leave.recon.LeaveReconRetrieveEJB;
import gov.gsa.ocfo.aloha.ejb.leave.recon.LeaveReconWithdrawCreateEJB;
import gov.gsa.ocfo.aloha.ejb.leave.recon.LeaveReconWithdrawEJB;
import gov.gsa.ocfo.aloha.ejb.leave.recon.LeaveReconWizardEJB;
import gov.gsa.ocfo.aloha.exception.LeaveReconException;
import gov.gsa.ocfo.aloha.model.entity.AlohaUser;
import gov.gsa.ocfo.aloha.model.entity.leave.recon.LeaveReconWizard;
import gov.gsa.ocfo.aloha.model.entity.leave.recon.LeaveReconWizardOutcome;
import gov.gsa.ocfo.aloha.util.StringUtil;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Stateless
public class LeaveReconWizardEJBImpl implements LeaveReconWizardEJB {
	
	@PersistenceContext (unitName="aloha-pu")
	private EntityManager entityManager;
	
	@EJB
	LeaveReconRetrieveEJB retrievelEJB;
	
	@EJB
	LeaveReconAmendEJB amendEJB;
	
	@EJB
	LeaveReconWithdrawCreateEJB withdrawCreateEJB;
	
	@EJB
	LeaveReconCreateEJB createEJB;
	
	@EJB
	LeaveReconWithdrawEJB withdrawEJB;
	
	@EJB
	EtamsAmendmentsEJB etamsAmendmentsEJB;

	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public List<LeaveReconWizard> retrieveByEmployeeUserId(long employeeUserId) throws LeaveReconException {
		return this.retrievelEJB.retrieveLeaveReconWizard(employeeUserId);
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public LeaveReconWizardOutcome doReconciliation(AlohaUser employeeSubmitter, LeaveReconWizard leaveReconWizard)
			throws LeaveReconException {

		//System.out.println("BEGIN: LeaveReconWizardEJB.doReconcillation()");

		/*************************************************************************************************
		 1) EXECUTE ALOHA TRANSACTIONS (PEND_AMEND, PEND_WITHDRAW, CREATE NEW LEAVE REQUEST)
		 2) PERSIST PENDING ITEMS (PENDING FIXES) TO THE LR_RECON_WIZARD_PENDING TABLE
				a) EACH ROW IN THE LR_RECON_WIZARD_PENDING TABLE REPRESENTS A DISCREPANCY FIX 
					ON AN "ITEM BASIS" FOR BOTH ETAMS AND ALOHA
				b) IN OTHER WORDS, THIS IS THE TABLE THAT STORES THE USER'S SELECTIONS
					WITH REGARD TO THE CORRECT LEAVE HOURS WHEN THE FOLLOWING SELECTIONS ARE MADE:
					(i) 	"ALOHA IS CORRECT"
					(ii) 	"ETAMS IS CORRECT"
					(iii) 	"NEITHER IS CORRECT"
		 3) UPDATE LR_RECON_WIZARD.FIX_PENDING COLUMN TO '1' SO ALOHA USER WON'T BE NAGGED ABOUT
		 		THE SAME DISCREPANCY IF THE USER HAPPENS TO LOG OUT AND LOG BACK INTO ALOHA AFTER 
				THE USER "FIXED" THE DISCREPANCY
		****************************************************************************************************/
		LeaveReconWizardOutcome wizardOutcome = this.execAlohaTransactions(employeeSubmitter, leaveReconWizard);
		
		/////////////////////////////////////////////////////////////////////////////		
		// CALL A FUNCTION IN A PACKAGE IN THE USR_FEDDESK_REQUIRED SCHEMA:
		/////////////////////////////////////////////////////////////////////////////			
		// SCHEMA:			USR_FEDDESK_REQUIRED
		// PACKAGE: 		ALOHA_AMENDMENT_UPDT
		// FUNCTION:		updt_leave
		// RETURNS:			VARCHAR (User readable message)
		/////////////////////////////////////////////////////////////////////////////		
		if ( wizardOutcome.isEtamsTransaction() ) {
			System.out.println("Calling execEtamsTransactions() ...");
			wizardOutcome.setEtamsMessage(this.execEtamsTransactions(leaveReconWizard));
			System.out.println("... returning from execEtamsTransactions()");
			System.out.println("wizardOutcome.getEtamsMessage(): " + wizardOutcome.getEtamsMessage());
		}
		
		//System.out.println("END: LeaveReconWizardEJB.doReconcillation()");
			
		return wizardOutcome;
	}


	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	private String execEtamsTransactions(LeaveReconWizard leaveReconWizard) throws LeaveReconException {
		
		if ( leaveReconWizard.isEtamsAmendmentRequired() ) { 
			return this.etamsAmendmentsEJB.processEtamsAmendments(leaveReconWizard);	
		} else {
			return null;	
		}
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	private LeaveReconWizardOutcome execAlohaTransactions(AlohaUser employeeSubmitter, LeaveReconWizard leaveReconWizard)
			throws LeaveReconException {

		try {
			
			this.doReconcillationValidation(employeeSubmitter, leaveReconWizard);
			
			this.debug(leaveReconWizard);
			
			////////////////////////////////////////////////////////////////
			// INSTANTIATE LeaveReconWizardOutcome AND INITIALIZE IT WITH
			// THE CORRECT TRANSACTION TYPE (ALOHA, ETAMS, BOTH)
			///////////////// TH///////////////////////////////////////////	
			LeaveReconWizardOutcome wizardOutcome = new LeaveReconWizardOutcome(leaveReconWizard);

			////////////////////////////////////////////////////////////
			// PROCESS ANY CHANGES THAT ARE REQUIRED ON THE ALOHA SIDE
			///////////////// TH///////////////////////////////////////	
			this.doAlohaReconcillation(employeeSubmitter, leaveReconWizard);

			////////////////////////////////////////////////////////////
			// PROCESS ANY CHANGES THAT ARE REQUIRED ON THE ETAMS SIDE
			///////////////// TH///////////////////////////////////////	
			this.doEtamsReconcillation(employeeSubmitter, leaveReconWizard);
			
			//////////////////////////////////////////////////////////////////////////////////////
			// ADD PENDING ITEMS TO THE LEAVE_RECON_WIZARD_PENDING TABLE FOR DISCREPANCY ITEMS
			// THAT HAVE A FIX PENDING (subject to supervisor approval).
			// PENDING ITEMS INCLUDE BOTH ALOHA AND ETAMS ITEMS THAT NEED TO BE CORRECTED
			//////////////////////////////////////////////////////////////////////////////////////
			leaveReconWizard.populatePendingItems();

			//////////////////////////////////////////////////////////////////////////////////////
			// SET THE FIX_PENDING FLAG IN THE LEAVE_RECON_WIZARD TABLE TO '1'
			////////////////////////////////////////////////////////////////////////////////////////
			leaveReconWizard.setFixPending(1);
			
			////////////////////////////////////////////////////////////////////////////////////////
			// MERGE THE STATE OF THE LeaveReconsWizard entity (and its child objects)
			// INTO THE PERSISTENCE CONTEXT
			////////////////////////////////////////////////////////////////////////////////////////
			this.entityManager.merge(leaveReconWizard);
			
			////////////////////////////////////////////////////////////////////////////////////////
			// SYNCHRONIZE THE PERSISTENCE CONTEXT TO THE UNDERLYING DATABASE
			////////////////////////////////////////////////////////////////////////////////////////
			this.entityManager.flush();
			
			//System.out.println("END: LeaveReconWizardEJB.execAlohaTransactions()");

			//////////////////////////////////
			// RETURN LeaveReconWizardOutcome
			/////////////////////////////////
			return wizardOutcome;
			
		} catch(LeaveReconException lre) {
			throw lre;
		} catch (Exception e) {
			//System.out.println(e.getMessage());
			e.printStackTrace();
			throw new LeaveReconException(e.getMessage());			
		}
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	private void doAlohaReconcillation(AlohaUser employeeSubmitter, LeaveReconWizard leaveReconWizard)
			throws LeaveReconException {
		
		//System.out.println("BEGIN: LeaveReconWizardEJB.doAlohaReconcillation()");
		
		if ( leaveReconWizard.incorrectAlohaLeaveHoursDetected() ) {
			
			if ( leaveReconWizard.isAlohaAmendmentRequired() ) {
				
				//////////////////////////////////////////////////////////////////////////////////////
				// PROCESS LEAVE REQUEST AMENDMENT
				//////////////////////////////////////////////////////////////////////////////////////
				this.amendEJB.processLeaveRequestAmendment(employeeSubmitter, leaveReconWizard);
				
			} else if (leaveReconWizard.isAlohaWithdrawCreateRequired() ) {

				//////////////////////////////////////////////////////////////////////////////////////
				// PROCESS LEAVE REQUEST WITHDRAWALS 
				// FOLLOWED BY THE CREATION OF A NEW LEAVE REQUEST
				//////////////////////////////////////////////////////////////////////////////////////
				this.withdrawCreateEJB.processWithdrawCreateLeaveRequests(employeeSubmitter, leaveReconWizard);

			} else if (leaveReconWizard.isBrandNewLeaveRequestRequired()) {
				
				//////////////////////////////////////////////////////////////////////////////////////
				// PROCESS NEW LEAVE REQUEST
				//////////////////////////////////////////////////////////////////////////////////////
				this.createEJB.processNewLeaveRequest(employeeSubmitter, leaveReconWizard);
				
			}  else if ( leaveReconWizard.isAlohaWithrawOnlyRequired() ) {
				
				//////////////////////////////////////////////////////////////////////////////////////
				// PROCESS LEAVE REQUEST WITHDRAWALS ONLY BECAUSE ALL CORRECT ALOHA LEAVE HOURS
				// ARE 'ZERO'. THIS CAN HAPPEN WHEN EMPLOYEE DIDN'T ACTUALLY TAKE LEAVE IN THE 
				// PAY PERIOD IN QUESTION.
				//////////////////////////////////////////////////////////////////////////////////////
				this.withdrawEJB.processWithdrawLeaveRequests(employeeSubmitter, leaveReconWizard);

			} else {
				StringBuilder errMsg = new StringBuilder();
				errMsg.append("LeaveReconWizard is in an invalid state: 1) # of Xref Items: [");
				errMsg.append(leaveReconWizard.getLeaveReconWizardXrefItems().size());
				errMsg.append("], ");
				errMsg.append("2) # of Aloha Items: [");
				errMsg.append(leaveReconWizard.getLeaveReconWizardAlohaItems().size());
				errMsg.append("], 3) # of Wizard Items: [");
				errMsg.append(leaveReconWizard.getLeaveReconWizardItems().size());
				errMsg.append("] \nCANNOT CONINTUE");
				throw new LeaveReconException(errMsg.toString());
			}			
		}
		
		//System.out.println("END: LeaveReconWizardEJB.doAlohaReconcillation()");
	}	

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	private void doEtamsReconcillation(AlohaUser employeeSubmitter, LeaveReconWizard leaveReconWizard)
			throws LeaveReconException {
		
		//System.out.println("BEGIN: LeaveReconWizardEJB.doEtamsReconcillation()");
		
		if ( leaveReconWizard.incorrectEtamsLeaveHoursDetected()) {
			leaveReconWizard.populateEtamsAmendmentItems();
		}
		
		//System.out.println("END: LeaveReconWizardEJB.doEtamsReconcillation()");
	}	
	
	
	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	private void debug(LeaveReconWizard wizard) {
		//System.out.println("Employee: " + wizard.getEmployeeUserId() + " | " + wizard.getEmployeeFullName());
		
		if ( wizard.wizardItemsAreNullOrEmpty() == false ) {
			//System.out.println("Wizard Items Count: " + wizard.getLeaveReconWizardItems().size());	
		} else {
			//System.out.println("Wizard Items Count: " + wizard.getLeaveReconWizardItems().size());
		}
		
		if ( wizard.xrefItemsAreNullOrEmpty() == false ) {
			//System.out.println("Xref Items Count: " + wizard.getLeaveReconWizardXrefItems().size());	
		} else {
			//System.out.println("Xref Items Count: " + wizard.getLeaveReconWizardXrefItems().size());
		}

		if ( wizard.alohaItemsAreNullOrEmpty() == false ) {
			//System.out.println("Aloha Items Count: " + wizard.getLeaveReconWizardAlohaItems().size());	
		} else {
			//System.out.println("Aloha Items Count: " + wizard.getLeaveReconWizardAlohaItems().size());
		}
		
		//System.out.println("");
		
	}
	
	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	private void doReconcillationValidation(AlohaUser alohaUser, LeaveReconWizard leaveReconWizard)
			throws LeaveReconException {
		
		//System.out.println("BEGIN: LeaveReconWizardEJB.doReconcillationValidation");

		int errorCount = 0;	
		StringBuilder errMsg = new StringBuilder();
		
		errMsg.append("Exception encountered in ");
		errMsg.append(this.getClass().getName());
		errMsg.append(".doReconcillation(LeaveReconWizard leaveReconWizard)");
		errMsg.append("\nREASON(s): ");
		
		if (alohaUser == null ) {
			errorCount++;
			errMsg.append("\n");
			errMsg.append(errorCount);
			errMsg.append(") Passed-in parameter 'AlohaUser' IS NULL");	
		}
		if (StringUtil.isNullOrEmpty(alohaUser.getLoginName()) == true ) {
			errorCount++;
			errMsg.append("\n");
			errMsg.append(errorCount);
			errMsg.append(") 'AlohaUser.getLoginName()' IS NULL");	
		}		
		if (leaveReconWizard == null ) {
			errorCount++;
			errMsg.append("\n");
			errMsg.append(errorCount);
			errMsg.append(") Passed-in parameter 'LeaveReconWizard' IS NULL");	
		}
		if ( leaveReconWizard.allCorrectLeaveHoursArePopulated() == false) {
			errorCount++;
			errMsg.append("\n");
			errMsg.append(errorCount);
			errMsg.append(") All 'correctLeaveHours' in each 'LeaveReconWizardItem' must be populated. At lease one NULL value was detected");	
		}
		
		/*******************************************************************************************
		/* IF XREF ITEMS ARE POPULATED AND ALOHA ITEMS ARE EMPTY, WE CAN'T HANDLE THE FOLLOWING 
		 * TWO SCENARIOS:
		 * 1) Leave Request Amendment
		 * 2) Leave Request Withdrawals, followed by Create New Leave Request
		 */
		 /*******************************************************************************************/
		if ( leaveReconWizard.xrefItemsArePopulated() && leaveReconWizard.alohaItemsAreNullOrEmpty() ) {
			
			if (leaveReconWizard.getLeaveReconWizardXrefItems().size() == 1) {
				errorCount++;
				errMsg.append("\n");
				errMsg.append(errorCount);
				errMsg.append(") 'leaveReconWizard.getLeaveReconWizardAlohaItems()' IS NULL OR EMPTY.");	
				errMsg.append(" Cannot create ALOHA Leave Request Amendment for the following: Employee ['");
				errMsg.append(leaveReconWizard.getEmployeeUserId());
				errMsg.append("'], Leave Year ['");
				errMsg.append(leaveReconWizard.getLeaveYear());
				errMsg.append("'], Pay Period ['");
				errMsg.append(leaveReconWizard.getPayPeriodNumber());
				errMsg.append("']");
			}
			
			if (leaveReconWizard.getLeaveReconWizardXrefItems().size() > 1) {
				errorCount++;
				errMsg.append("\n");
				errMsg.append(errorCount);
				errMsg.append(") 'leaveReconWizard.getLeaveReconWizardAlohaItems()' IS NULL OR EMPTY.");	
				errMsg.append(" Cannot withdraw existing ALOHA Leave Requests and create a new ALOHA Leave Request for the following: Employee ['");
				errMsg.append(leaveReconWizard.getEmployeeUserId());
				errMsg.append("'], Leave Year ['");
				errMsg.append(leaveReconWizard.getLeaveYear());
				errMsg.append("'], Pay Period ['");
				errMsg.append(leaveReconWizard.getPayPeriodNumber());
				errMsg.append("']");				
			}
		}

		/*******************************************************************************************
		/* IF ALOHA ITEMS ARE POPULATED AND XREF ITEMS ARE EMPTY, WE CAN'T HANDLE THE FOLLOWING 
		 * TWO SCENARIOS:
		 * 1) Leave Request Amendment
		 * 2) Leave Request Withdrawals, followed by Create New Leave Request
		 */
		 /*******************************************************************************************/
		if ( leaveReconWizard.xrefItemsAreNullOrEmpty() && leaveReconWizard.alohaItemsArePopulated() ) {
			
				errorCount++;
				errMsg.append("\n");
				errMsg.append(errorCount);
				errMsg.append(") 'leaveReconWizard.getLeaveReconWizardXrefItems()' IS NULL OR EMPTY.");	
				errMsg.append(" Cannot create an ALOHA Leave Request Amendment for the following: Employee ['");
				errMsg.append(leaveReconWizard.getEmployeeUserId());
				errMsg.append("'], Leave Year ['");
				errMsg.append(leaveReconWizard.getLeaveYear());
				errMsg.append("'], Pay Period ['");
				errMsg.append(leaveReconWizard.getPayPeriodNumber());
				errMsg.append("']");
		}		
		
		
		/*******************************************************************************************
		/* THIS CAN HAPPEN WHEN THERE IS LEAVE RECORDED ON THE ETAMS TIMECARD AND THERE IS
		 * NO CORRESPONDING ALOHA LEAVE REQUEST FOR THE EMPLOYEE AND PAY PERIOD
		 *******************************************************************************************
		if (leaveReconWizard.getLeaveReconWizardXrefItems() == null
				|| leaveReconWizard.getLeaveReconWizardXrefItems().isEmpty()) {
			errorCount++;
			errMsg.append("\n");
			errMsg.append(errorCount);
			errMsg.append(") 'leaveReconWizard.getLeaveReconWizardXrefItems()' IS NULL OR EMPTY");	
		}
		*******************************************************************************************/
		
		/*******************************************************************************************
		/* THIS CAN HAPPEN WHEN THERE IS LEAVE RECORDED ON THE ETAMS TIMECARD AND THERE IS
		 * NO CORRESPONDING ALOHA LEAVE REQUEST FOR THE EMPLOYEE AND PAY PERIOD
		 *******************************************************************************************
		if (leaveReconWizard.getLeaveReconWizardAlohaItems() == null
				|| leaveReconWizard.getLeaveReconWizardAlohaItems().isEmpty()) {
			errorCount++;
			errMsg.append("\n");
			errMsg.append(errorCount);
			errMsg.append(") 'leaveReconWizard.getLeaveReconWizardAlohaItems()' IS NULL OR EMPTY");	
		}	
		*******************************************************************************************/
		
		//System.out.println("errorCount: " + errorCount);
		//System.out.println("END: LeaveReconWizardEJB.doReconcillationValidation");
		
		if ( errorCount > 0 ) {
			errMsg.append("\nCANNOT CONTINUE");
			throw new LeaveReconException(errMsg.toString());
		}
	}
}