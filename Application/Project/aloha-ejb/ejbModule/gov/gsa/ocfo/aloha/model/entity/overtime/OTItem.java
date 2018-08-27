package gov.gsa.ocfo.aloha.model.entity.overtime;

import gov.gsa.ocfo.aloha.model.overtime.group.OTTaskRow;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Version;

@Entity
@Table(name="OT_ITEM", schema="ALOHA")
public class OTItem implements OTTaskRow, Serializable {

	private static final long serialVersionUID = 2942537349261063604L;
	public static final BigDecimal MIN_OT_HOURS = new BigDecimal(0.1D).setScale(1, RoundingMode.HALF_DOWN);
	
	@Id
	@SequenceGenerator(name="OT_ITEM_ID_GENERATOR", sequenceName="ALOHA.SEQ_OT_ITEM", allocationSize=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="OT_ITEM_ID_GENERATOR")
	@Column(name="ITEM_ID", unique=true, nullable=false, precision=10)
	private long id;
    
	@ManyToOne
	@JoinColumn(name="DETAIL_ID", referencedColumnName="DETAIL_ID", nullable=false)
	private OTDetail detail;
	
    @Column(name="TASK_DESC", nullable=false, length=4000)
	private String taskDescription;

	@Column(name="EST_HOURS", nullable=false, precision=2, scale=1)
	private BigDecimal estimatedHours;
	
	@Version
	@Column(name="OPT_LOCK_NBR")
	private long version;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public OTDetail getDetail() {
		return detail;
	}

	public void setDetail(OTDetail detail) {
		this.detail = detail;
	}

	public String getTaskDescription() {
		return taskDescription;
	}

	public void setTaskDescription(String taskDescription) {
		this.taskDescription = taskDescription;
	}

	public BigDecimal getEstimatedHours() {
		return estimatedHours;
	}

	public void setEstimatedHours(BigDecimal estimatedHours) {
		this.estimatedHours = estimatedHours;
	}

	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
	}
	
	public long getItemId() {
		return this.id;
	}
	// METHOD REQUIRED BY OTTaskRow INTERFACE
	public String getTaskDesc() {
		return this.taskDescription;
	}
	// METHOD REQUIRED BY OTTaskRow INTERFACE
	public BigDecimal getEstNbrOfHours() {
		return this.estimatedHours;
	}
}
