package gov.gsa.ocfo.aloha.web.validator.ot;

import gov.gsa.ocfo.aloha.model.entity.overtime.OTItem;
import gov.gsa.ocfo.aloha.util.StringUtil;
import gov.gsa.ocfo.aloha.web.util.ErrorMessages;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OTEstHoursValidatorUtil {
	public static final Pattern EST_HOURS_PATTERN = Pattern.compile("\\d{1,2}+([.]\\d)?");

	public static List<String> validate(String value) {
		
		List<String> validationErrors = new ArrayList<String>();
		
    	if ( StringUtil.isNullOrEmpty(value)) {
    		validationErrors.add(ErrorMessages.getInstance().getMessage(ErrorMessages.OT_EMTPY_EST_HOURS));
    		return validationErrors;
		}
    	
		if ( ! isInCorrectFormat(value)) {
			Object[] params = {value};
			validationErrors.add(ErrorMessages.getInstance().getMessage(ErrorMessages.OT_INVALID_EST_HOURS, params));	
			return validationErrors;
		}

		if ( ! isConvertibleToBigDecimal(value)) {
			Object[] params = {value};
			validationErrors.add(ErrorMessages.getInstance().getMessage(ErrorMessages.OT_INVALID_EST_HOURS, params));  
			return validationErrors;
		}   
		
		BigDecimal estHours = new BigDecimal(value);
		estHours.setScale(1, RoundingMode.HALF_DOWN);
		
		if ( ! isGreaterThanOrEqualToMinValue(estHours)) {
			validationErrors.add(ErrorMessages.getInstance().getMessage(ErrorMessages.OT_TASK_LESS_THAN_ONE_TENTH_OF_AN_HOUR));  
			return validationErrors;
		}  
		
		return validationErrors;
	}
	
	public static boolean isInCorrectFormat(String value) {
		boolean correctFormat = false;
		if ( !StringUtil.isNullOrEmpty(value)) {
			Matcher matcher = EST_HOURS_PATTERN.matcher(value);
			correctFormat = matcher.matches();
		}
		return correctFormat;
	}
	
	public static boolean isConvertibleToBigDecimal(String value) {
		try {
			@SuppressWarnings("unused")
			BigDecimal estHours = new BigDecimal(value);
			return true;
		} catch (NumberFormatException nfe) {
			return false;
		}
	}	
	
	public static boolean isGreaterThanOrEqualToMinValue(BigDecimal value) {
		if (value != null) {
			return ( value.compareTo(OTItem.MIN_OT_HOURS) >= 0 );
		}
		return false;
	}
}
