package gov.gsa.ocfo.aloha.model.entity.overtime;

import gov.gsa.ocfo.aloha.model.entity.AlohaUser;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

@Entity
@Table(name="OT_DETAIL_HISTORY", schema="ALOHA")
public class OTDetailHistory implements Serializable {
	private static final long serialVersionUID = 8931377791080693591L;

	@Id
	@SequenceGenerator(name="OT_DETAIL_HISTORY_ID_GENERATOR", sequenceName="ALOHA.SEQ_OT_DETAIL_HISTORY", allocationSize=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="OT_DETAIL_HISTORY_ID_GENERATOR")
	@Column(name="DETAIL_HISTORY_ID", unique=true, nullable=false, precision=10)
	private long id;
	
	@ManyToOne
	@JoinColumn(name="DETAIL_ID", referencedColumnName="DETAIL_ID", nullable=false)
	private OTDetail detail;
	
    @ManyToOne
	@JoinColumn(name="STATUS_TRANSITION_ID", referencedColumnName="STATUS_TRANSITION_ID", nullable=false)
	private OTIndivStatusTrans statusTransition;
    
    @ManyToOne
	@JoinColumn(name="USER_ID",  referencedColumnName="USER_ID", nullable=false)	
	private AlohaUser actor;
    
  	@Temporal( TemporalType.TIMESTAMP)
	@Column(name="ACTION_DATETIME")
	private Date actionDatetime;
	
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

	public OTIndivStatusTrans getStatusTransition() {
		return statusTransition;
	}

	public void setStatusTransition(OTIndivStatusTrans statusTransition) {
		this.statusTransition = statusTransition;
	}

	public Date getActionDatetime() {
		return actionDatetime;
	}

	public void setActionDatetime(Date actionDatetime) {
		this.actionDatetime = actionDatetime;
	}

	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
	}

	public AlohaUser getActor() {
		return actor;
	}

	public void setActor(AlohaUser actor) {
		this.actor = actor;
	}
}
