package gov.gsa.ocfo.aloha.model.leave;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

public class DisabledVetLeaveInfo implements Serializable {

	private static final long serialVersionUID = 7429570887251281548L;
	
	private Date expirationDate;
	private BigDecimal leaveBalance;
	
	public DisabledVetLeaveInfo(Date expirationDate, BigDecimal leaveBalance) {
		super();
		this.expirationDate = expirationDate;
		this.leaveBalance = leaveBalance;
	}

	public Date getExpirationDate() {
		return expirationDate;
	}

	public BigDecimal getLeaveBalance() {
		return leaveBalance;
	}	
}
