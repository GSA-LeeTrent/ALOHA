package gov.gsa.ocfo.aloha.model;

import java.io.Serializable;
import java.util.List;

public class PayPeriodYear implements Serializable {
	private static final long serialVersionUID = -3371245721942609757L;

	private int value;
	private List<PayPeriod> payPeriods;
	
	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}
	
	public List<PayPeriod> getPayPeriods() {
		return payPeriods;
	}

	public void setPayPeriods(List<PayPeriod> payPeriods) {
		this.payPeriods = payPeriods;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + value;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PayPeriodYear other = (PayPeriodYear) obj;
		if (value != other.value)
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("PayPeriodYear [value=");
		builder.append(value);
		builder.append("]");
		return builder.toString();
	}

}
