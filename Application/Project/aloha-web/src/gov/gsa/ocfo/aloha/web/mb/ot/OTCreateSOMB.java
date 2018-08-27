package gov.gsa.ocfo.aloha.web.mb.ot;

import gov.gsa.ocfo.aloha.exception.AlohaServerException;
import gov.gsa.ocfo.aloha.exception.AuthorizationException;
import gov.gsa.ocfo.aloha.model.entity.AlohaUser;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTHeader;
import gov.gsa.ocfo.aloha.web.security.NavigationOutcomes;
import gov.gsa.ocfo.aloha.web.util.AlohaConstants;
import gov.gsa.ocfo.aloha.web.util.ErrorMessages;
import gov.gsa.ocfo.aloha.web.util.FacesContextUtil;
import gov.gsa.ocfo.aloha.web.util.NormalMessages;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

@ManagedBean(name="otCreateSOMB")
@SessionScoped
public class OTCreateSOMB extends OTCreateAbstractMB  {
	private static final long serialVersionUID = 5124254070390510226L;
	
	@PostConstruct
	public String initCreate() {
		try {
			super.init();
			this.initApprovers();
			this.initOTBalances();
			this.initOTTypes();
			return NavigationOutcomes.OT_CREATE_SO;
		} catch (AuthorizationException ae) {
			return NavigationOutcomes.UNAUTHORIZED;
		} catch (NumberFormatException nfe) {
			return NavigationOutcomes.SERVER_ERROR;
		} catch (AlohaServerException e) {
			return NavigationOutcomes.SERVER_ERROR;		
		}	
	}
	
	private void initOTTypes()throws AlohaServerException {
		this.otTypesMB.buildOTTypesEmp();
	}
		
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	protected void checkAccessRights() throws AuthorizationException {
		if ( !this.userMB.isSubmitOwn() ) {
			Object[] params = { this.userMB.getFullName() };
			String errMsg = ErrorMessages.getInstance().getMessage(ErrorMessages.OT_CREATE_UNAUTHORIZED_SO, params);
			FacesContextUtil.getHttpSession().setAttribute(AlohaConstants.UNAUTHORIZED_MESSAGE, errMsg);
			throw new AuthorizationException(errMsg);
		}
	}	
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	public AlohaUser getEmployee() {
		return this.userMB.getUser();
	}	
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	protected String buildConfirmationMessage(OTHeader otHeader) {
		Object[] params = {otHeader.getType().getName(), 
				otHeader.getPayPeriod().getShortLabel(), 
				otHeader.getDetails().get(0).getSupervisor().getFullName()};
		return ( NormalMessages.getInstance().getMessage(NormalMessages.OT_CREATE_CONFIRM_MSG_SO, params) );		
	}
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	protected String getSuccessPage()	{
		return NavigationOutcomes.OT_LIST_SO;
	}	
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	protected String getValidationFailurePage()	{
		return NavigationOutcomes.OT_CREATE_SO;
	}		
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	protected void initOTBalances() throws AlohaServerException {
		if (this.getEmployee() != null) {
			super.otBalances = this.overtimeEJB.retrieveOTBalances(this.getEmployee().getUserId());			
		}
	}
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	protected String getStatusChangeOutcomeKey() {
		return AlohaConstants.OT_STATUS_CHANGE_OUTCOME_SO;	
	}
}	