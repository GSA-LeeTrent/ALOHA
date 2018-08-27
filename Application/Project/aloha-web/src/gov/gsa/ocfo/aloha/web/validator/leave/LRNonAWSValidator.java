package gov.gsa.ocfo.aloha.web.validator.leave;

import gov.gsa.ocfo.aloha.exception.AlohaValidationException;
import gov.gsa.ocfo.aloha.model.PayPeriod;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveItem;

import java.math.BigDecimal;
import java.util.List;

public class LRNonAWSValidator extends LRValidatorImpl {
	private static final long serialVersionUID = -1386977742734586981L;
	private static final BigDecimal MAX_HOURS_PER_DAY = BigDecimal.valueOf(8.0D); 
	private static final BigDecimal MAX_HOURS_PER_PAY_PERIOD = BigDecimal.valueOf(80.0D);
	
	public LRNonAWSValidator(PayPeriod payPeriod, List<LeaveItem> priorRequestItems) throws AlohaValidationException {
		super(payPeriod, priorRequestItems);
	}
	public BigDecimal getMaxLeaveHoursPerDay() {
		return MAX_HOURS_PER_DAY;
	}
	public BigDecimal getMaxLeaveHoursPerPayPeriod() {
		return MAX_HOURS_PER_PAY_PERIOD;
	}
}
