package gov.gsa.ocfo.aloha.web.validator.leave.v2;

import gov.gsa.ocfo.aloha.exception.AlohaValidationException;
import gov.gsa.ocfo.aloha.web.model.leave.LeaveViewItem;

import java.util.List;

public interface LRValidatorV2 {
	public static final int MAX_DAYS_IN_PAY_PERIOD = 14;
	public static final int MAX_LEAVE_TYPES_PER_DAY = 6;
	
	public void validateCurrentRequest(List<LeaveViewItem> currentRequest) throws AlohaValidationException;
}
