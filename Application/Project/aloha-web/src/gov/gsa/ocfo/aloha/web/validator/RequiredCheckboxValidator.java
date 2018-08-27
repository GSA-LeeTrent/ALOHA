package gov.gsa.ocfo.aloha.web.validator;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

@FacesValidator("gov.gsa.ocfo.aloha.web.validator.RequiredCheckboxValidator")
public class RequiredCheckboxValidator implements Validator {

    public void validate(FacesContext context, UIComponent component, Object value)
        throws ValidatorException
    {
        if (value.equals(Boolean.FALSE)) {
            String requiredMessage = ((UIInput) component).getRequiredMessage();
            FacesMessage facesMsg = new FacesMessage(requiredMessage, requiredMessage);
            facesMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
            FacesContext.getCurrentInstance().addMessage(null, facesMsg);
            throw new ValidatorException(facesMsg);
        }
    }
}
