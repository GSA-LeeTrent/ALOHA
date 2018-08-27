package gov.gsa.ocfo.aloha.web.mb;

import gov.gsa.ocfo.aloha.ejb.UserEJB;
import gov.gsa.ocfo.aloha.exception.AlohaServerException;
import gov.gsa.ocfo.aloha.exception.AuthorizationException;
import gov.gsa.ocfo.aloha.model.entity.AlohaUser;
import gov.gsa.ocfo.aloha.model.entity.EtamsAdXref;
import gov.gsa.ocfo.aloha.util.StopWatch;
import gov.gsa.ocfo.aloha.web.security.NavigationOutcomes;
import gov.gsa.ocfo.aloha.web.util.AlohaURIs;
import gov.gsa.ocfo.aloha.web.util.ErrorMessages;
import gov.gsa.ocfo.aloha.web.util.NormalMessages;
import gov.gsa.ocfo.aloha.web.util.StringUtil;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.faces.validator.ValidatorException;

@ManagedBean(name="adXrefMB")
@ViewScoped
public class AdXrefMB implements Serializable {

	private static final long serialVersionUID = 7341422822399849916L;
	
	@EJB private UserEJB userEJB;
	
	@ManagedProperty(value="#{userMB}")
	private UserMB userMB;	
	
	private List<EtamsAdXref> users;
	
	private String searchLoginName;
	private String selectedRow; //used by delete
	
	//edit page variables
	private String editLoginName;
	private SelectItem[] editEtamsUserId;
	private String editEmailAddress;
	private String editSelectedEtamsUserId;
	private String editStartingEtamsUserLastName;
	private String editOriginalLoginName; //for update, save login name in case user updates it
	private String editOriginalEtamsUser;
	private String editOriginalEmailAddress;
	
	
	@PostConstruct
	public void init() throws IOException {
		try {
			this.doSecurityCheck();
			Map<String,String> params = 
                FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
			//System.out.println("Param edit Login Name: " + params.get("editLoginName"));
			if (params.get("editLoginName") != null) {
				this.editOriginalLoginName = params.get("editLoginName");
			}

			//System.out.println("Original Login Name: " + this.editOriginalLoginName);
			if (this.editOriginalLoginName != null) {
				EtamsAdXref ead = this.userEJB.getEtamsAdXref(this.editOriginalLoginName);
				if (ead != null) {
					this.editLoginName = ead.getLoginName();
					this.editEmailAddress = ead.getAlohaUser().getEmailAddress();
					this.editEtamsUserId = new SelectItem[1];
					editEtamsUserId[0] = new SelectItem(ead.getUserId(), 
								ead.getAlohaUser().getLastName() + " " + ead.getAlohaUser().getFirstName() + " " 
								+ (ead.getAlohaUser().getMiddleName()== null ? "" : ead.getAlohaUser().getMiddleName()));
					this.editOriginalEmailAddress = ead.getAlohaUser().getEmailAddress();
					this.editOriginalEtamsUser = ead.getAlohaUser().getLastName() + " " 
					+ ead.getAlohaUser().getFirstName() + " " 
					+ (ead.getAlohaUser().getMiddleName()== null ? "" : ead.getAlohaUser().getMiddleName());
				}
			}
			//System.out.println("init Login Name: " + this.editLoginName);
			users = this.userEJB.getEtamsAdXrefs("%");
		} catch (AuthorizationException ae) {
			try {
				//Navigation will not work so use redirect
				//FacesContext.getCurrentInstance().getExternalContext().redirect("/aloha/faces/pages/public/unauthorized.xhtml");
				FacesContext.getCurrentInstance().getExternalContext().redirect(AlohaURIs.UNAUTHORIZED);
			} catch( IOException ioe) {
				ioe.printStackTrace();
				FacesContext.getCurrentInstance().getExternalContext().redirect(AlohaURIs.SERVER_ERROR);
			}		
		} catch (AlohaServerException e) {
			e.printStackTrace();
			FacesContext.getCurrentInstance().getExternalContext().redirect(AlohaURIs.SERVER_ERROR);
		}
	}

	private void doSecurityCheck() throws AuthorizationException {
		//only approvers and submitothers can run reconciliation
		if ( !this.userMB.isAlohaAdmin()) {
			throw new AuthorizationException(ErrorMessages.getInstance().getMessage(ErrorMessages.ADXREF_NOTAUTH));
		}

	}
	
	public String search() {
		try {
			if (this.searchLoginName.isEmpty() ) {
				users = this.userEJB.getEtamsAdXrefs("%");
			} else {
				users = this.userEJB.getEtamsAdXrefs(this.searchLoginName.toUpperCase() + "%");
			}
		} catch (AlohaServerException e) {
			e.printStackTrace();
			return NavigationOutcomes.SERVER_ERROR;
		}
			return null;
		
	}
	
	//called from ajax edit
	public String getEtamsUser () {
		try {
			
			List<AlohaUser> etamsUsers = new ArrayList<AlohaUser>();
	
			etamsUsers = this.userEJB.getEtamsUserByName(editStartingEtamsUserLastName);
			//System.out.println("ajax...." + this.editStartingEtamsUserLastName + " return array size: " + etamsUsers.size());
			this.editEtamsUserId = new SelectItem[etamsUsers.size()];
			int index = 0;
			for (AlohaUser au : etamsUsers) {
				editEtamsUserId[index++] = new SelectItem(au.getUserId(), 
						au.getLastName() + " " + au.getFirstName() + " " 
						+ (au.getMiddleName() == null ? "" : au.getMiddleName()) + " "
						+ (au.getEmailAddress() == null ? "" : au.getEmailAddress()));
			}			
		} catch (AlohaServerException e) {
			e.printStackTrace();
			return NavigationOutcomes.SERVER_ERROR;
		}		
		return null;
	}
	public String cancel(){
		return NavigationOutcomes.LIST_ADXREF;
	}
	public String delete(){
		try {
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			Integer rc = this.userEJB.deleteEtamsAdXref(selectedRow); 
			stopWatch.stop();
			System.out.println("ELAPSED TIME (Delete ETAMS_AD_XREF): " + stopWatch.getElapsedTime() + " ms");
			stopWatch = null;
			
			if ( rc == 1) {
				Object[] p = new Object[] {selectedRow};
				FacesMessage facesMsg = new FacesMessage(NormalMessages.getInstance().getMessage(NormalMessages.ADXREF_DELETE_OK, p), 
						NormalMessages.getInstance().getMessage(NormalMessages.ADXREF_DELETE_OK, p));
				facesMsg.setSeverity(FacesMessage.SEVERITY_INFO);
				FacesContext.getCurrentInstance().addMessage(null, facesMsg);
				return NavigationOutcomes.LIST_ADXREF; 
			}
		} catch (AlohaServerException e) {
			e.printStackTrace();
			return NavigationOutcomes.SERVER_ERROR;
		}
		
		return null;
	}
	public String save() {
		try {
			doValidation();	
			EtamsAdXref ead = new EtamsAdXref();
			ead.setLoginName(this.editLoginName);
			ead.setUserId(Long.parseLong(this.editSelectedEtamsUserId));
			
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			
			EtamsAdXref outEad = this.userEJB.saveEtamsAdXref(this.editOriginalLoginName, ead, editEmailAddress);
			stopWatch.stop();
			System.out.println("ELAPSED TIME (Save ETAMS_AD_XREF): " + stopWatch.getElapsedTime() + " ms");
			stopWatch = null;			
			
			if (outEad != null) {
				this.editLoginName = outEad.getLoginName();
				this.editSelectedEtamsUserId = Long.toString(outEad.getUserId());
				
				//set success message
				Object[] p = new Object[] {outEad.getLoginName()};
				FacesMessage facesMsg = null;
				if (editOriginalLoginName == null || editOriginalLoginName.isEmpty()) {
					facesMsg = new FacesMessage(NormalMessages.getInstance().getMessage(NormalMessages.ADXREF_ADD_OK, p), 
							NormalMessages.getInstance().getMessage(NormalMessages.ADXREF_ADD_OK, p));
				} else {
					facesMsg = new FacesMessage(NormalMessages.getInstance().getMessage(NormalMessages.ADXREF_UPDATE_OK, p), 
							NormalMessages.getInstance().getMessage(NormalMessages.ADXREF_UPDATE_OK, p));
				}
				facesMsg.setSeverity(FacesMessage.SEVERITY_INFO);
				FacesContext.getCurrentInstance().addMessage(null, facesMsg);
				return NavigationOutcomes.LIST_ADXREF;
			}
		} catch (EJBException ee){
			if (ee.getCause().getCause().getMessage().equals("AlohaBadData")) {
				FacesMessage facesMsg = new FacesMessage(ee.getCause().getMessage(), ee.getCause().getMessage());
				facesMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
				FacesContext.getCurrentInstance().addMessage(null, facesMsg);
			}			
		} catch (ValidatorException ve) {
			return null;
		} catch (AlohaServerException e) {
				e.printStackTrace();	
		}	
		return null;
	}
	
	//Edit page validations
	private void doValidation() throws ValidatorException {
		int errorCount = 0;
		if ( StringUtil.isNullOrEmpty(editLoginName)) {
			this.addMsg(ErrorMessages.LOGIN_NAME_REQUIRED);
			errorCount++;
		}
		if ( StringUtil.isNullOrEmpty(editSelectedEtamsUserId)) {
			this.addMsg(ErrorMessages.ETAMS_USERID_REQUIRED);
			errorCount++;
		}
		if ( StringUtil.isNullOrEmpty(editEmailAddress)) {
			this.addMsg(ErrorMessages.EMAIL_ADDRESS_REQUIRED);
			errorCount++;
		}
		if ( errorCount > 0) {
			throw new ValidatorException(new FacesMessage());
		}	
	}
	
	private void addMsg(String key) {
		String errMsgText = ErrorMessages.getInstance().getMessage(key);
		FacesMessage facesMsg = new FacesMessage(errMsgText, errMsgText);
		facesMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
		FacesContext.getCurrentInstance().addMessage(null, facesMsg);
	}
	
	//---------------------------------------------------
	//GETTERS and SETTERS
	//---------------------------------------------------
	public void setUsers(List<EtamsAdXref> users) {
		this.users = users;
	}

	public List<EtamsAdXref> getUsers() {
		return users;
	}

	public void setUserEJB(UserEJB userEJB) {
		this.userEJB = userEJB;
	}

	public UserEJB getUserEJB() {
		return userEJB;
	}

	public void setSearchLoginName(String searchLoginName) {
		this.searchLoginName = searchLoginName;
	}

	public String getSearchLoginName() {
		return searchLoginName;
	}

	public void setEditLoginName(String editLoginName) {
		this.editLoginName = editLoginName;
	}

	public String getEditLoginName() {
		return editLoginName;
	}

	public void setEditEtamsUserId(SelectItem[] editEtamsUserId) {
		this.editEtamsUserId = editEtamsUserId;
	}

	public SelectItem[] getEditEtamsUserId() {
		return editEtamsUserId;
	}

	public void setEditEmailAddress(String editEmailAddress) {
		this.editEmailAddress = editEmailAddress;
	}

	public String getEditEmailAddress() {
		return editEmailAddress;
	}

	public void setEditSelectedEtamsUserId(String editSelectedEtamsUserId) {
		this.editSelectedEtamsUserId = editSelectedEtamsUserId;
	}

	public String getEditSelectedEtamsUserId() {
		return editSelectedEtamsUserId;
	}

	public String getEditStartingEtamsUser() {
		return editStartingEtamsUserLastName;
	}

	public void setEditStartingEtamsUser(String editStartingEtamsUser) {
		this.editStartingEtamsUserLastName = editStartingEtamsUser;
	}

	public void setSelectedRow(String selectedRow) {
		this.selectedRow = selectedRow;
	}

	public String getSelectedRow() {
		return selectedRow;
	}

	public void setEditOriginalLoginName(String editOriginalLoginName) {
		this.editOriginalLoginName = editOriginalLoginName;
	}

	public String getEditOriginalLoginName() {
		return editOriginalLoginName;
	}

	public String getEditOriginalEtamsUser() {
		return editOriginalEtamsUser;
	}

	public void setEditOriginalEtamsUser(String editOriginalEtamsUser) {
		this.editOriginalEtamsUser = editOriginalEtamsUser;
	}

	public String getEditOriginalEmailAddress() {
		return editOriginalEmailAddress;
	}

	public void setEditOriginalEmailAddress(String editOriginalEmailAddress) {
		this.editOriginalEmailAddress = editOriginalEmailAddress;
	}

	public UserMB getUserMB() {
		return userMB;
	}

	public void setUserMB(UserMB userMB) {
		this.userMB = userMB;
	}
	
	
}