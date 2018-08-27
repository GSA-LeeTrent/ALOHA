package gov.gsa.ocfo.aloha.web.validator.leave;

import gov.gsa.ocfo.aloha.exception.AlohaValidationException;
import gov.gsa.ocfo.aloha.model.ScheduleItem;

import java.util.List;

public interface LRValidator {
	public static final int MAX_DAYS_IN_PAY_PERIOD = 14;
	public static final int MAX_LEAVE_TYPES_PER_DAY = 6;
	
	public void validateCurrentRequest(List<ScheduleItem> currentRequest) throws AlohaValidationException;
}
