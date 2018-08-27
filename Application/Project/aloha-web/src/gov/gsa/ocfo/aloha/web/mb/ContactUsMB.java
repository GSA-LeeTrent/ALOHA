package gov.gsa.ocfo.aloha.web.mb;

import gov.gsa.ocfo.aloha.ejb.SendMailEJB;
import gov.gsa.ocfo.aloha.web.util.ErrorMessages;
import gov.gsa.ocfo.aloha.web.util.NormalMessages;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;

@ManagedBean(name="contactUsMB")
@RequestScoped
public class ContactUsMB {
	private String from;
	private String subject;
	private String body;
	private String confirmationMsg = null;
	private boolean emailAvailable;
	
	@EJB
	private SendMailEJB sendMailEJB;
	
	@ManagedProperty(value = "#{userMB}")
	private UserMB userMB;
	
	@PostConstruct
	public void init() {
		if (userMB.isLoggedIn()) {
			this.setEmailAvailable(true);
			this.setFrom(userMB.getEmailAddress());
			this.confirmationMsg = null;
		} else {
			this.setEmailAvailable(false);
		}
	}
	
	public String sendMail() {
		//String sendTo = "aloha-support@gsa.gov";
		//String sendTo = "OCFOServiceDesk@gsa.gov";
		//String sendTo = "david.peterman@gsa.gov";
		//String sendTo = "lee.trent@gsa.gov";
		String sendTo = "businessapps@gsa.gov";
		
		try {
			doValidation();
			sendMailEJB.sendMail(getFrom(),sendTo, getFrom(), "", getSubject(), getBody());
			this.setConfirmationMsg(NormalMessages.getInstance().getMessage(NormalMessages.CONTACT_US_MSG_SENT_OK));
		} catch (ValidatorException ve) {
			return null;			
		}
		return null;
	}
	
	
	private void doValidation() throws ValidatorException {
		int errorCount = 0;
		if ( getSubject().isEmpty()) {
			String errMsgText = ErrorMessages.getInstance().getMessage(ErrorMessages.TEAM_REQUIRED);
			FacesMessage facesMsg = new FacesMessage(errMsgText, errMsgText);
			facesMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
			FacesContext.getCurrentInstance().addMessage(null, facesMsg);
			errorCount++;
		}	
		if ( getBody().isEmpty()) {
			String errMsgText = ErrorMessages.getInstance().getMessage(ErrorMessages.PAY_PERIOD_REQUIRED);
			FacesMessage facesMsg = new FacesMessage(errMsgText, errMsgText);
			facesMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
			FacesContext.getCurrentInstance().addMessage(null, facesMsg);
			errorCount++;
		}

		if ( errorCount > 0) {
			throw new ValidatorException(new FacesMessage());
		}		
	}
	//----------------------------
	//Getters and Setters
	//----------------------------
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public String getSubject() {
		return subject;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public String getBody() {
		return body;
	}

	public void setConfirmationMsg(String confirmationMsg) {
		this.confirmationMsg = confirmationMsg;
	}

	public String getConfirmationMsg() {
		return confirmationMsg;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getFrom() {
		return from;
	}

	public void setEmailAvailable(boolean emailAvailable) {
		this.emailAvailable = emailAvailable;
	}

	public boolean isEmailAvailable() {
		return emailAvailable;
	}
	public UserMB getUserMB() {
		return userMB;
	}

	public void setUserMB(UserMB userMB) {
		this.userMB = userMB;
	}
}
