package gov.gsa.ocfo.aloha.model.entity.overtime;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Version;

@Entity
@Table(name="OT_DETAIL_REMARK", schema="ALOHA")
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="ACTOR_TYPE_CODE", discriminatorType=DiscriminatorType.STRING, length=1)
public abstract class OTDetailRemark implements Serializable {
	private static final long serialVersionUID = -5930025656300747876L;

	@Id
	@SequenceGenerator(name="OT_DETAIL_REMARK_ID_GENERATOR", sequenceName="ALOHA.SEQ_OT_DETAIL_REMARK", allocationSize=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="OT_DETAIL_REMARK_ID_GENERATOR")
	@Column(name="DETAIL_REMARK_ID", unique=true, nullable=false, precision=10)
	private long id;

	@ManyToOne
	@JoinColumn(name="DETAIL_ID", referencedColumnName="DETAIL_ID", nullable=false)
	private OTDetail detail;
	
    @ManyToOne
	@JoinColumn(name="STATUS_TRANSITION_ID", referencedColumnName="STATUS_TRANSITION_ID", nullable=false)
	private OTIndivStatusTrans statusTransition;
	
    @Column(name="REMARK_SEQ", nullable=false, precision=5) 
    private int sequence;

    @Column(name="REMARK_TEXT", nullable=false, length=4000)
	private String text;
    
	@Version
	@Column(name="OPT_LOCK_NBR")
	private long version;
	
	@Column(name="USER_CREATED", nullable=true) //needs to be nullable to accomodate rows that
												//were inserted prior to the addition of this column
	private Long userCreated;

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
	
	public OTIndivStatusTrans getStatusTransition() {
		return statusTransition;
	}

	public void setStatusTransition(OTIndivStatusTrans statusTransition) {
		this.statusTransition = statusTransition;
	}

	public int getSequence() {
		return sequence;
	}

	public void setSequence(int sequence) {
		this.sequence = sequence;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
	}

	public Long getUserCreated() {
		return userCreated;
	}

	public void setUserCreated(Long userCreated) {
		this.userCreated = userCreated;
	}
}
