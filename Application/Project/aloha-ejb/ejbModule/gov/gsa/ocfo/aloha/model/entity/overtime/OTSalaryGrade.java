package gov.gsa.ocfo.aloha.model.entity.overtime;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@Entity
@Table(name="OT_SALARY_GRADE", schema="ALOHA")
@NamedQueries({
	@NamedQuery(name="retrieveAllOTSalaryGrade", 
				query="SELECT row FROM OTSalaryGrade row")	        	
})
public class OTSalaryGrade implements Serializable {
	private static final long serialVersionUID = -6767992411342516842L;
	
	public interface QueryNames{
		public static final String RETRIEVE_ALL = "retrieveAllOTSalaryGrade";
	}
	
	@Id
	@Column(name="SALARY_GRADE_KEY", unique=true, nullable=false, length=8)
	private String key;

	@Column(name="PAY_PLAN", unique=false, nullable=false, length=2)
	private String payPlan;
	
	@Column(name="OCCUP_CD", unique=false, nullable=false, length=4)
	private String occupationCode;

	@Column(name="GRADE", unique=false, nullable=false, length=2)
	private String grade;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getPayPlan() {
		return payPlan;
	}

	public void setPayPlan(String payPlan) {
		this.payPlan = payPlan;
	}

	public String getOccupationCode() {
		return occupationCode;
	}

	public void setOccupationCode(String occupationCode) {
		this.occupationCode = occupationCode;
	}

	public String getGrade() {
		return grade;
	}

	public void setGrade(String grade) {
		this.grade = grade;
	}

	public String getValue() {
		return this.key;
	}
	public String getLabel() {
		return (this.payPlan + this.grade);
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
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
		OTSalaryGrade other = (OTSalaryGrade) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("OTSalaryGrade [key=");
		builder.append(key);
		builder.append(", payPlan=");
		builder.append(payPlan);
		builder.append(", occupationCode=");
		builder.append(occupationCode);
		builder.append(", grade=");
		builder.append(grade);
		builder.append("]");
		return builder.toString();
	}
}
