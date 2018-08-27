package gov.gsa.ocfo.aloha.web.validator.ot;

import gov.gsa.ocfo.aloha.util.StringUtil;
import gov.gsa.ocfo.aloha.web.util.ErrorMessages;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

@FacesValidator("otTaskDescValidator")
public class OTTaskDescValidator implements Validator {

    public void validate(FacesContext fc, UIComponent comp, Object obj)
            throws ValidatorException {
    	String taskDesc = (String) obj;
    	if ( StringUtil.isNullOrEmpty(taskDesc)) {
        	String errMsgText = ErrorMessages.getInstance().getMessage(ErrorMessages.OT_EMTPY_TASK_DESC);
        	FacesMessage facesMsg = new FacesMessage(errMsgText, errMsgText);
        	//facesMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
        	//FacesContext.getCurrentInstance().addMessage(null, facesMsg);	
        	throw new ValidatorException(facesMsg);
    	}   
    }
}


