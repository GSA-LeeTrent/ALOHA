package gov.gsa.ocfo.aloha.web.mb.ot.modify;

import gov.gsa.ocfo.aloha.ejb.overtime.GroupOvertimeEJB;
import gov.gsa.ocfo.aloha.ejb.overtime.OvertimeEJB;
import gov.gsa.ocfo.aloha.exception.AlohaServerException;
import gov.gsa.ocfo.aloha.exception.AuthorizationException;
import gov.gsa.ocfo.aloha.exception.IllegalOperationException;
import gov.gsa.ocfo.aloha.model.KeyValuePair;
import gov.gsa.ocfo.aloha.model.entity.AlohaUser;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTDetail;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTDetailHistory;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTDetailSubmitterRemark;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTDetailSupervisorRemark;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTHeader;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTIndivStatus;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTIndivStatusTrans;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTItem;
import gov.gsa.ocfo.aloha.model.overtime.OTEditableItem;
import gov.gsa.ocfo.aloha.util.StringUtil;
import gov.gsa.ocfo.aloha.web.mb.UserMB;
import gov.gsa.ocfo.aloha.web.mb.ot.group.OTGroupMB;
import gov.gsa.ocfo.aloha.web.mb.overtime.OTUtilMB;
import gov.gsa.ocfo.aloha.web.model.overtime.OTStatusChangeOutcome;
import gov.gsa.ocfo.aloha.web.security.NavigationOutcomes;
import gov.gsa.ocfo.aloha.web.util.AlohaConstants;
import gov.gsa.ocfo.aloha.web.util.AlohaURIs;
import gov.gsa.ocfo.aloha.web.util.ErrorMessages;
import gov.gsa.ocfo.aloha.web.util.FacesContextUtil;
import gov.gsa.ocfo.aloha.web.util.NormalMessages;
import gov.gsa.ocfo.aloha.web.util.OTGroupStatusChangeHelper;
import gov.gsa.ocfo.aloha.web.validator.ot.OTEstHoursValidatorUtil;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.FacesContext;
import javax.faces.model.DataModel;
import javax.faces.validator.ValidatorException;

import com.icesoft.faces.facelets.component.table.editable.EditableTableBean;
import com.icesoft.faces.facelets.component.table.editable.EditableTableException;
import com.icesoft.faces.facelets.component.table.editable.IEditableTableEventListener;

public abstract class OTModifyIndivReqAbstractMB implements Serializable {
	private static final long serialVersionUID = -3677049149362733126L;

	protected abstract void checkAccessRights() throws AuthorizationException;
	protected abstract void checkIfLegalOperation()	throws IllegalOperationException;
	protected abstract String getNewStatusCode();
	protected abstract AlohaUser getSubmitter();
	protected abstract AlohaUser getSupervisor();
	protected abstract String getSubmitterRemarks();
	protected abstract String getSupervisorRemarks();
	protected abstract String getSuccessPage();
	protected abstract String getStatusChangeOutcomeKey();

	@EJB
	protected OvertimeEJB overtimeEJB;
	
	@EJB
	protected GroupOvertimeEJB groupOvertimeEJB;
	
	// JSF: MANAGED BEAN INJECTION
	@ManagedProperty(value = "#{userMB}")
	protected UserMB userMB;
	
	// JSF: MANAGED BEAN INJECTION
	@ManagedProperty(value = "#{otUtilMB}")
	protected OTUtilMB otUtilMB;
	
	// JSF: MANAGED BEAN INJECTION
	@ManagedProperty(value = "#{otGroupMB}")
	protected OTGroupMB otGroupMB;

	// INSTANCE MEMBERS
	protected OTDetail otDetail;
	private List<KeyValuePair> otBalances = new ArrayList<KeyValuePair>();
	private EditableTableBean editableTableBean;
	private EditableTableBean lastValidEditableTableBean;
	protected String modificationRemarks;
	
	// INIT METHOD
	protected void init() {
		try {
			String paramId = FacesContextUtil.getHttpServletRequest().getParameter(AlohaConstants.OT_DETAIL_ID);

			// ----------------------------------------------------------------------------------------------------------
			// IMPORTANT:
			// ----------------------------------------------------------------------------------------------------------
			// With View Scoped Managed Beans, the @PostConstruct method is
			// called even when a user tries to get off the page.
			// For example, this method will get called even when the user
			// clicks the "Quit" button
			// or tries to link off the page by clicking links such as "Home"
			// and "Sitemap".
			// As such, we check for the HTTP Request Parameter before
			// continuing.
			if (!StringUtil.isNullOrEmpty(paramId)) {
				this.otDetail = this.overtimeEJB.retrieveDetailByID(Long.parseLong(paramId));
				
				OTHeader otHeader = this.otDetail.getHeader();
				
				boolean modifiableBySupervisor = OTGroupStatusChangeHelper.determineModifiablityOfSingleIndividualRequest(this.userMB.getUser(), otHeader);
				boolean cancellableBySupervisor = OTGroupStatusChangeHelper.determineCancelablityOfSingleIndividualRequest(this.userMB.getUser(), otHeader);				
				
				otHeader.setModifiableBySupervisor(modifiableBySupervisor);
				otHeader.setCancellableBySupervisor(cancellableBySupervisor);
				
				this.editableTableBean = new EditableTableBean(
						this.convertToEditableItems(this.otDetail.getItems()),
						new EditableTableEventListener());
				
//				this.lastValidEditableTableBean = new EditableTableBean(
//						this.convertToEditableItems(this.otDetail.getItems()),
//						new EditableTableEventListener());				
				
				
				this.checkAccessRights();
				this.checkIfLegalOperation();
				this.otBalances = this.overtimeEJB.retrieveOTBalances(this.otDetail.getEmployee().getUserId());
			} else {
				FacesContextUtil.callHome();
			}
		} catch (NumberFormatException nfe) {
			try {
				nfe.printStackTrace();
				FacesContext.getCurrentInstance().getExternalContext().redirect(AlohaURIs.SERVER_ERROR);
			} catch (IOException ignore) {}
		} catch (AlohaServerException ase) {
			try {
				FacesContext.getCurrentInstance().getExternalContext().redirect(AlohaURIs.SERVER_ERROR);
			} catch (IOException ignore) {}
		} catch (AuthorizationException ae) {
			try {
				FacesContext.getCurrentInstance().getExternalContext().redirect(AlohaURIs.UNAUTHORIZED);
			} catch (IOException ignore) {}
		} catch (IllegalOperationException ise) {
			try {
				FacesContext.getCurrentInstance().getExternalContext().redirect(AlohaURIs.ILLEGAL_OPERATION);
			} catch (IOException ignore) {}
		} catch (Throwable t) {
			t.printStackTrace();
			try {
				t.printStackTrace();
				FacesContext.getCurrentInstance().getExternalContext().redirect(AlohaURIs.SERVER_ERROR);
			} catch (IOException ignore) {}
		}
	}

	public String onModify() {
		String navigationOutcome = null;
		
		try {
			OTIndivStatus otIndivStatus = this.otUtilMB.getOTIndivStatus(this.getNewStatusCode());		
			OTIndivStatusTrans otIndivStatusTrans = this.otUtilMB.getOTIndivStatusTrans(this.determineOTIndivStatusTransitionCode());
			
			// MAKE SURE THE EDITABLE TABLE BEAN DATA CONTAINS GOOD DATA
			this.validateEditableTableBeanData();
			
			// CREATE NEW OT DETAIL RELFECTING MODIFICATIONS
			OTDetail newOTDetail = this.createOTDetail(otIndivStatus, otIndivStatusTrans);
			
			// DOUBLE CHECK THE TASK ITEMS DATA FOR VALIDITY
			this.validateNewOTDetail(newOTDetail);
			
			// BACK-UP THE NEWLY CREATED GOOD DATA SET
			this.lastValidEditableTableBean = new EditableTableBean
					(this.convertToEditableItems(newOTDetail.getItems()), new EditableTableEventListener());

			// ADD THE NEWLY CREATED OT DETAIL TO THE EXISTING OT HEADER
			OTHeader otHeader = this.otDetail.getHeader();
			otHeader.addDetail(newOTDetail);
			
			// MAKE SURE THAT THE NEWLY CREATED OT DETAIL HAS A REFERENCE TO THE EXISTING OT HEADER
			newOTDetail.setHeader(otHeader);
			
			// SET THE SEQUENCE VALUE FOR THIS NEWLY CREATED OT DETAIL
			newOTDetail.setSequence(otHeader.getDetails().size());
			
			// UPDATE DATABASE WITH NEWLY CREATED OT DETAIL (THIS IS DONE AT THE OT HEADER (ROOT) LEVEL)
			this.overtimeEJB.updateOTHeader(otHeader);
			
			// PAGE NAVIGATION
			navigationOutcome = this.getSuccessPage();
			FacesContextUtil.getHttpSession().setAttribute(this.getStatusChangeOutcomeKey(), this.buildStatusChangeOutcome(newOTDetail));
		} catch (ValidatorException ve) {
			//this.editableTableBean = this.lastValidEditableTableBean;
			return null;
		} catch (AlohaServerException ase) {
			navigationOutcome = NavigationOutcomes.SERVER_ERROR;
		} catch (Exception e) {
			e.printStackTrace();
			navigationOutcome = NavigationOutcomes.SERVER_ERROR;
		}  
		return navigationOutcome;
	}
	private void validateEditableTableBeanData() throws ValidatorException {
		DataModel dataModel = this.editableTableBean.getData();
		int rowCount = dataModel.getRowCount();
		
		if ( rowCount < 1 ) {
			// THERE AREN'T ANY OT TASK ROWS, SO RESTORE THE LAST VALID EDITABLE TABLE BEAN
			this.lastValidEditableTableBean = new EditableTableBean
					(this.convertToEditableItems(this.otDetail.getItems()), new EditableTableEventListener());
			this.editableTableBean = this.lastValidEditableTableBean;
			
			String errMsgText = ErrorMessages.getInstance().getMessage(ErrorMessages.OT_MODIFY_NO_TASKS);
			FacesMessage facesMsg = new FacesMessage(errMsgText, errMsgText);
			facesMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
			FacesContext.getCurrentInstance().addMessage(null, facesMsg);	
			throw new ValidatorException(facesMsg);			
		}
		
		int errorCount = 0;
		for (int ii = 0; ii < rowCount; ii++) {
			dataModel.setRowIndex(ii);
			OTEditableItem otEditableItem = (OTEditableItem)dataModel.getRowData();

			// VALIDATE TASK DESCRIPTION
			if ( StringUtil.isNullOrEmpty(otEditableItem.getTaskDescription())) {
				String errMsgText = ErrorMessages.getInstance().getMessage(ErrorMessages.OT_EMTPY_TASK_DESC);
				FacesMessage facesMsg = new FacesMessage(errMsgText, errMsgText);
				facesMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
				FacesContext.getCurrentInstance().addMessage(null, facesMsg);
				errorCount++;		            
	        }
	        
			// VALIDATE TASK ESTIMATED NUMBER OF HOURS
			List<String> validationErrors = OTEstHoursValidatorUtil.validate(otEditableItem.getEstimatedHoursAsText());
			if ( ! validationErrors.isEmpty() ) {
				for ( String valError : validationErrors) {
					FacesMessage facesMsg = new FacesMessage(valError, valError);
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
	private void validateNewOTDetail(OTDetail newOTDetail) throws ValidatorException {
		if ( newOTDetail.getItems().size() == 0) {		
			String errMsgText = ErrorMessages.getInstance().getMessage(ErrorMessages.OT_MODIFY_NO_TASKS);
			FacesMessage facesMsg = new FacesMessage(errMsgText, errMsgText);
			facesMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
			FacesContext.getCurrentInstance().addMessage(null, facesMsg);	
			throw new ValidatorException(facesMsg);
		}
		
		for ( OTItem otItem : newOTDetail.getItems()) {
			if ( StringUtil.isNullOrEmpty(otItem.getTaskDescription())) {
				String errMsgText = ErrorMessages.getInstance().getMessage(ErrorMessages.OT_EMTPY_TASK_DESC);
				FacesMessage facesMsg = new FacesMessage(errMsgText, errMsgText);
				facesMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
				FacesContext.getCurrentInstance().addMessage(null, facesMsg);	
				throw new ValidatorException(facesMsg);
	        }		
			if ( otItem.getEstimatedHours() == null ) {
				String errMsgText = ErrorMessages.getInstance().getMessage(ErrorMessages.OT_EMTPY_EST_HOURS);
				FacesMessage facesMsg = new FacesMessage(errMsgText, errMsgText);
				facesMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
				FacesContext.getCurrentInstance().addMessage(null, facesMsg);	
				throw new ValidatorException(facesMsg);
	        }						
		}
	}		
	private OTDetail createOTDetail(OTIndivStatus otIndivStatus, OTIndivStatusTrans otIndivStatusTrans) throws ValidatorException {
		
		// OVERTIME DETAIL
		OTDetail otDetail = new OTDetail();
		otDetail.setStatus(otIndivStatus);
		otDetail.setSubmitter(this.getSubmitter());
		otDetail.setSupervisor(this.getSupervisor());
		otDetail.setUserCreated(this.userMB.getUserId());
		otDetail.setDateCreated(new Date());

		// SUBMITTER REMARK
		if ( !StringUtil.isNullOrEmpty(this.getSubmitterRemarks()) ) {
			OTDetailSubmitterRemark otSubmitterRemark = new OTDetailSubmitterRemark();
			otSubmitterRemark.setDetail(otDetail);
			otSubmitterRemark.setStatusTransition(otIndivStatusTrans);
			otSubmitterRemark.setSequence(otDetail.getSubmitterRemarks().size() + 1);
			otSubmitterRemark.setText(this.getSubmitterRemarks());
			otSubmitterRemark.setUserCreated(this.userMB.getUserId());
			otDetail.addSubmitterRemark(otSubmitterRemark);
		}
		
		// SUPERVISOR REMARK
		if ( !StringUtil.isNullOrEmpty(this.getSupervisorRemarks()) ) {
			OTDetailSupervisorRemark otSupervisorRemark = new OTDetailSupervisorRemark();
			otSupervisorRemark.setDetail(otDetail);
			otSupervisorRemark.setStatusTransition(otIndivStatusTrans);
			otSupervisorRemark.setSequence(otDetail.getSupervisorRemarks().size() + 1);
			otSupervisorRemark.setText(this.getSupervisorRemarks());
			otSupervisorRemark.setUserCreated(this.userMB.getUserId());			
			otDetail.addSupervisorRemark(otSupervisorRemark);
		}
		
		// HISTORY
		OTDetailHistory otHistory = new OTDetailHistory();
		otHistory.setDetail(otDetail);
		otHistory.setStatusTransition(otIndivStatusTrans);
		otHistory.setActor(this.userMB.getUser());
		otHistory.setActionDatetime(new Date());
		otDetail.addDetailHistory(otHistory);

		// ITEMS
		DataModel dataModel = this.editableTableBean.getData();
		int rowCount = dataModel.getRowCount();
		
		//System.out.println("dataModel.getRowCount(): " + dataModel.getRowCount());
		
		if ( rowCount != -1) {
			for (int ii = 0; ii < rowCount; ii++) {
				dataModel.setRowIndex(ii);
				OTEditableItem otEditableItem = (OTEditableItem)dataModel.getRowData();
				OTItem otItem = new OTItem();
				otItem.setDetail(otDetail);
				otItem.setTaskDescription(otEditableItem.getTaskDescription().trim());
				otItem.setEstimatedHours( new BigDecimal(otEditableItem.getEstimatedHoursAsText()) );
				otDetail.addItem(otItem);	
			}			
		}
		
		//System.out.println("otDetail.getItems().size(): " + otDetail.getItems().size());
		
		return otDetail;
	}

	// HELPER METHOD
	private String determineOTIndivStatusTransitionCode() {
		return (this.otUtilMB.determineOTIndivStatusTransCode(this.otDetail.getStatus().getCode(), this.getNewStatusCode()));
	}

	// HELPER METHOD
	private OTStatusChangeOutcome buildStatusChangeOutcome(OTDetail newOTDetail) {
		return new OTStatusChangeOutcome(this.getNewStatusCode(),
				this.buildConfirmationMessage(newOTDetail));
	}

	// HELPER METHOD
	private String buildConfirmationMessage(OTDetail newOTDetail) {
		String employeeName = null;
		if (newOTDetail.getEmployee().getFullName().endsWith("s")) {
			employeeName = newOTDetail.getEmployee().getFullName() + "'";
		} else {
			employeeName = newOTDetail.getEmployee().getFullName() + "'s";
		}
		Object[] params = { newOTDetail.getType().getName(), employeeName,
				newOTDetail.getPayPeriod().getShortLabel(),
				newOTDetail.getStatus().getName() };
		return (NormalMessages.getInstance().getMessage(
				NormalMessages.OT_MSG_STATUS_CHANGE, params));
	}
	
	// HELPER METHOD
	private List<OTEditableItem> convertToEditableItems(List<OTItem> otItemList) {
		List<OTEditableItem> otEditableItemRowList = new ArrayList<OTEditableItem>();		
		int count = 0;
		for ( OTItem otItem : otItemList) {
			otEditableItemRowList.add(new OTEditableItem(++count, otItem.getTaskDescription(), otItem.getEstimatedHours().toPlainString()));
		}
		return otEditableItemRowList;
	}

	/*****************************************
	 * GETTERS:
	 *****************************************/
	public OTDetail getOtDetail() {
		return otDetail;
	}
	public List<KeyValuePair> getOtBalances() {
		return otBalances;
	}
	public EditableTableBean getEditableTableBean() {
		return editableTableBean;
	}
	public String getModificationRemarks() {
		return modificationRemarks;
	}
	/*****************************************
	 * SETTERS
	 *****************************************/
	// JSF: MANAGED BEAN INJECTION
	public void setUserMB(UserMB userMB) {
		this.userMB = userMB;
	}
	// JSF: MANAGED BEAN INJECTION
	public void setOtUtilMB(OTUtilMB otUtilMB) {
		this.otUtilMB = otUtilMB;
	}
	// JSF: MANAGED BEAN INJECTION
	public void setOtGroupMB(OTGroupMB otGroupMB) {
		this.otGroupMB = otGroupMB;
	}			
	public void setModificationRemarks(String modificationRemarks) {
		this.modificationRemarks = modificationRemarks;
	}

	/*****************************************
	 * INNER CLASS
	 *****************************************/
	class EditableTableEventListener implements IEditableTableEventListener, Serializable {
		private static final long serialVersionUID = -4928166345069785495L;

		public Object createNewRow() {
			System.out.println(this.getClass().getSimpleName() + ".createNewRow():");
			OTEditableItem otEditableItem = new OTEditableItem();
			return otEditableItem;
		}
		public void rowDeleted(Object row) {
			System.out.println(this.getClass().getSimpleName() + ".rowDeleted(): row deleted ---> " + row);
		}
		public void rowSelected(Object row) {
			System.out.println(this.getClass().getSimpleName() + ".rowDeleted(): row selected ---> " + row);
		}
		public void rowUpdated(Object oldBean, Object newBean) {
			System.out.println(this.getClass().getSimpleName() + ".rowUpdated(): row updated: " + oldBean + " ---> " + newBean);
		}
		public void rowEditCanceled(Object oldBean, Object newBean)
				throws EditableTableException {
			System.out.println(this.getClass().getSimpleName() + ".rowEditCanceled(): row edit canceled");
		}
	}
}