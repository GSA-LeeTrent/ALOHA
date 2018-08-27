package gov.gsa.ocfo.aloha.web.validator;

import gov.gsa.ocfo.aloha.web.util.ErrorMessages;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

@FacesValidator("gov.gsa.ocfo.aloha.web.validator.PayPeriodValidator")
public class PayPeriodValidator implements Validator {
	@Override
	public void validate(FacesContext context, UIComponent compoenent, Object obj)
			throws ValidatorException {
		
			String selectedPayPeriod = obj.toString();
			if ( Integer.parseInt(selectedPayPeriod) < 1) {
			
				String errMsgText = ErrorMessages.getInstance().getMessage(ErrorMessages.PAY_PERIOD_REQUIRED);
				FacesMessage facesMsg = new FacesMessage(errMsgText, errMsgText);
				facesMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
				FacesContext.getCurrentInstance().addMessage(null, facesMsg);
				throw new ValidatorException(facesMsg);
			}			
	}
}