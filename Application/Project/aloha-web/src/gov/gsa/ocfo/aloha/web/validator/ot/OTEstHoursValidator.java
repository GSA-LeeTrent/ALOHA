package gov.gsa.ocfo.aloha.web.validator.ot;

import java.math.BigDecimal;
import java.math.RoundingMode;

import gov.gsa.ocfo.aloha.util.StringUtil;
import gov.gsa.ocfo.aloha.web.util.ErrorMessages;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

@FacesValidator("otEstHoursValidator")
public class OTEstHoursValidator implements Validator {
	
    public void validate(FacesContext fc, UIComponent comp, Object obj)
            throws ValidatorException {

    	String estHoursAsText = (String) obj;
		
    	if ( StringUtil.isNullOrEmpty(estHoursAsText)) {
			String errMsgText = ErrorMessages.getInstance().getMessage(ErrorMessages.OT_EMTPY_EST_HOURS);
			FacesMessage facesMsg = new FacesMessage(errMsgText, errMsgText);
			throw new ValidatorException(facesMsg);
		}

		if ( !OTEstHoursValidatorUtil.isInCorrectFormat(estHoursAsText)) {
			Object[] params = {estHoursAsText};
			String errMsgText = ErrorMessages.getInstance().getMessage(ErrorMessages.OT_INVALID_EST_HOURS, params);
			FacesMessage facesMsg = new FacesMessage(errMsgText, errMsgText);
			throw new ValidatorException(facesMsg);    	   
		}
		if ( !OTEstHoursValidatorUtil.isConvertibleToBigDecimal(estHoursAsText)) {
			Object[] params = {estHoursAsText};
			String errMsgText = ErrorMessages.getInstance().getMessage(ErrorMessages.OT_INVALID_EST_HOURS, params);
			FacesMessage facesMsg = new FacesMessage(errMsgText, errMsgText);
			throw new ValidatorException(facesMsg);    	   
		}  
		
		BigDecimal estHours = new BigDecimal(estHoursAsText);
		estHours.setScale(1, RoundingMode.HALF_DOWN);
		
		if ( ! OTEstHoursValidatorUtil.isGreaterThanOrEqualToMinValue(estHours)) {
			String errMsgText = ErrorMessages.getInstance().getMessage(ErrorMessages.OT_TASK_LESS_THAN_ONE_TENTH_OF_AN_HOUR);
			FacesMessage facesMsg = new FacesMessage(errMsgText, errMsgText);
			throw new ValidatorException(facesMsg);
		
		} 		
    }
}