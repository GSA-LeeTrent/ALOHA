package gov.gsa.ocfo.aloha.web.mb.ot;

import gov.gsa.ocfo.aloha.exception.AlohaServerException;
import gov.gsa.ocfo.aloha.exception.AuthorizationException;
import gov.gsa.ocfo.aloha.model.entity.AlohaUser;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTHeader;
import gov.gsa.ocfo.aloha.util.StringUtil;
import gov.gsa.ocfo.aloha.web.security.NavigationOutcomes;
import gov.gsa.ocfo.aloha.web.util.AlohaConstants;
import gov.gsa.ocfo.aloha.web.util.ErrorMessages;
import gov.gsa.ocfo.aloha.web.util.FacesContextUtil;
import gov.gsa.ocfo.aloha.web.util.NormalMessages;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

@ManagedBean(name="otCreateOBOMB")
@SessionScoped
public class OTCreateOBOMB extends OTCreateAbstractMB  {
	private static final long serialVersionUID = 4476469366932433172L;

	// EMPLOYEE
	private String selectedEmployee;
	private List<AlohaUser> dbEmployees = new ArrayList<AlohaUser>();
	private Map<String, Object> employees = new TreeMap<String, Object>();
	
	@PostConstruct
	public String initCreate() {
		try {
			super.init();
			this.selectedEmployee = null;
			this.initEmployees();
			this.initOTTypes();
			return NavigationOutcomes.OT_CREATE_OBO;			
		} catch (AuthorizationException ae) {
			return NavigationOutcomes.UNAUTHORIZED;
		} catch (NumberFormatException nfe) {
			return NavigationOutcomes.SERVER_ERROR;
		} catch (AlohaServerException e) {
			return NavigationOutcomes.SERVER_ERROR;		
		}	
	}
	private void initEmployees() throws AlohaServerException {
		this.dbEmployees = this.employeeEJB.getOnBehalfOfEmployees(this.userMB.getUserId());
		for ( AlohaUser employee : this.dbEmployees) {
			this.employees.put(employee.getLabel(), employee.getValue());
		}
	}	

	private void initOTTypes()throws AlohaServerException {
		this.otTypesMB.buildOTTypesOBO();
	}	

	
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	protected void checkAccessRights() throws AuthorizationException {
		if ( !this.userMB.isOnBehalfOf() ) {
			Object[] params = { this.userMB.getFullName() };
			String errMsg = ErrorMessages.getInstance().getMessage(ErrorMessages.OT_CREATE_UNAUTHORIZED_OBO, params);
			FacesContextUtil.getHttpSession().setAttribute(AlohaConstants.UNAUTHORIZED_MESSAGE, errMsg);
			throw new AuthorizationException(errMsg);
		}
	}	
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	public AlohaUser getEmployee() {
		if (!StringUtil.isNullOrEmpty(this.selectedEmployee)){
			return this.findEmployee(Long.valueOf(this.selectedEmployee));	
		} else {
			return null;
		}
	}	
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	protected String buildConfirmationMessage(OTHeader otHeader) {
		Object[] params = {otHeader.getType().getName(),
				otHeader.getDetails().get(0).getSupervisor().getFullName(), 
				otHeader.getPayPeriod().getShortLabel(), 
				otHeader.getEmployee().getFullName()};
		return (NormalMessages.getInstance().getMessage(NormalMessages.OT_CREATE_CONFIRM_MSG_OBO, params));

	}
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	protected String getSuccessPage()	{
		return NavigationOutcomes.OT_LIST_OBO;
	}
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	protected String getValidationFailurePage()	{
		return NavigationOutcomes.OT_CREATE_OBO;
	}		

	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	protected void initOTBalances() throws AlohaServerException {
		if (this.getEmployee() != null) {
			super.otBalances = this.overtimeEJB.retrieveOTBalances(this.getEmployee().getUserId());			
		}
	}		
	
	private AlohaUser findEmployee(long userId) {
		for ( AlohaUser employee: this.dbEmployees) {
			if ( employee.getUserId() == userId) {
				return employee;
			}
		}
		return null;
	}	

	/*****************************************
	 * EVENTS
	 *****************************************/	
	public String onEmployeeSelected() {
		try {
			if ( this.isEmployeeSelected() ) {
				this.checkAccessRights();
				this.initApprovers();
				this.initOTBalances();
				return NavigationOutcomes.OT_CREATE_OBO;
			} else {
				String errMsgText = ErrorMessages.getInstance().getMessage(ErrorMessages.EMPLOYEE_REQUIRED);
				FacesMessage facesMsg = new FacesMessage(errMsgText, errMsgText);
				facesMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
				FacesContext.getCurrentInstance().addMessage(null, facesMsg);
				return null;
			}
		} catch (AuthorizationException ae) {
			return NavigationOutcomes.UNAUTHORIZED;
		} catch (Exception e) {
			e.printStackTrace();
			return NavigationOutcomes.SERVER_ERROR;
		}		
	}	

	/*****************************************
	 * SETTERS
	 *****************************************/	
	public void setSelectedEmployee(String selectedEmployee) {
		this.selectedEmployee = selectedEmployee;
	}	

	/*****************************************
	 * GETTERS
	 *****************************************/	
	public boolean isEmployeeSelected() {
		return (this.getEmployee() != null);
	}
	public String getSelectedEmployee() {
		return selectedEmployee;
	}
	public Map<String, Object> getEmployees() {
		return employees;
	}
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	protected String getStatusChangeOutcomeKey() {
		return AlohaConstants.OT_STATUS_CHANGE_OUTCOME_OBO;	
	}
}