package gov.gsa.ocfo.aloha.model.entity.overtime;

import gov.gsa.ocfo.aloha.util.StringUtil;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

@Entity
@Table(name="OT_PAY_PERIOD", schema="ALOHA")
@NamedQueries({
	@NamedQuery(name="retrieveAllOTPayPeriod", 
				query="SELECT row FROM OTPayPeriod row ORDER BY row.year DESC, row.number ASC")	        	
})
public class OTPayPeriod implements Serializable {
	private static final long serialVersionUID = 871316246753062323L;
	public static final String LABEL_FORMAT = "MM/dd/yyyy";
	
	public interface QueryNames{
		public static final String RETRIEVE_ALL = "retrieveAllOTPayPeriod";
	}	
	
	@Id
	@Column(name="PAY_PERIOD_KEY", unique=true, nullable=false, precision=6)
	private long key;
	
	@Column(name="PP_YEAR", unique=false, nullable=false, precision=4)
	private int year;
	
	@Column(name="PP_NUMBER", unique=false, nullable=false, precision=2)
	private int number;
	
	@Temporal(TemporalType.DATE)
	@Column(name="START_DATE")
	private Date startDate;

	@Temporal(TemporalType.DATE)
	@Column(name="END_DATE")
	private Date endDate;
	
	@Transient
	private String label;
	
	@Transient
	private String shortLabel;

	public long getKey() {
		return key;
	}

	public void setKey(long key) {
		this.key = key;
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public String getLabel() {
		if ( StringUtil.isNullOrEmpty(this.label)) {
			if ( (this.startDate != null) && (this.endDate != null) ) {
				SimpleDateFormat dateFormat = new SimpleDateFormat(OTPayPeriod.LABEL_FORMAT);
				StringBuilder sb = new StringBuilder();
				sb.append("[");
				if ( this.number < 10 ) {sb.append("0");}
				sb.append(this.number);
				sb.append("]: ");
				sb.append(dateFormat.format(this.startDate));
				sb.append(" - ");
				sb.append(dateFormat.format(this.endDate));
				this.label = sb.toString();
			}
		}
		return label;
	}
	
	public String getShortLabel() {
		if ( StringUtil.isNullOrEmpty(this.shortLabel)) {
			if ( (this.startDate != null) && (this.endDate != null) ) {
				SimpleDateFormat dateFormat = new SimpleDateFormat(OTPayPeriod.LABEL_FORMAT);
				StringBuilder sb = new StringBuilder();
				sb.append(dateFormat.format(this.startDate));
				sb.append(" - ");
				sb.append(dateFormat.format(this.endDate));
				this.shortLabel = sb.toString();
			}
		}
		return shortLabel;
	}

	public void setLabel(String label) {
		this.label = label;
	}
	public String getPayPeriodDateRange() {
		return StringUtil.buildDateRange(this.startDate, this.endDate, OTPayPeriod.LABEL_FORMAT);
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (key ^ (key >>> 32));
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
		OTPayPeriod other = (OTPayPeriod) obj;
		if (key != other.key)
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("OTPayPeriod [key=");
		builder.append(key);
		builder.append(", year=");
		builder.append(year);
		builder.append(", number=");
		builder.append(number);
		builder.append(", startDate=");
		builder.append(startDate);
		builder.append(", endDate=");
		builder.append(endDate);
		builder.append(", label=");
		builder.append(label);
		builder.append("]");
		return builder.toString();
	}


}
