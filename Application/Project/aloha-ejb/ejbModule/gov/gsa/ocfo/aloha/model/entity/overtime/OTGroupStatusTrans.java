package gov.gsa.ocfo.aloha.model.entity.overtime;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

@Entity
@DiscriminatorValue(value="G")
@NamedQueries({
	@NamedQuery(name="retrieveAllOTGroupStatusTrans", 
				query="SELECT row FROM OTGroupStatusTrans row")	        	
})
public class OTGroupStatusTrans extends OTStatusTrans {
	private static final long serialVersionUID = -1952956954907025835L;
	
	public interface QueryNames{
		public static final String RETRIEVE_ALL = "retrieveAllOTGroupStatusTrans";
	}

	public interface ActionCodeValues {
		public static final String NONE_TO_PENDING 			= "NONE_TO_PENDING";
		public static final String PENDING_TO_CANCELLED 	= "PENDING_TO_CANCELLED";
		public static final String PENDING_TO_SUBMITTED 	= "PENDING_TO_SUBMITTED";
		public static final String SUBMITTED_TO_CANCELLED	= "SUBMITTED_TO_CANCELLED";
		public static final String SUBMITTED_TO_RECEIVED	= "SUBMITTED_TO_RECEIVED";
		public static final String RECEIVED_TO_CANCELLED	= "RECEIVED_TO_CANCELLED";
		public static final String RECEIVED_TO_FINAL		= "RECEIVED_TO_FINAL";
		public static final String SUBMITTED_TO_FINAL		= "SUBMITTED_TO_FINAL";
		public static final String PENDING_TO_FINAL			= "PENDING_TO_FINAL";
		public static final String FINAL_TO_CANCELLED		= "FINAL_TO_CANCELLED";
	}	

    @ManyToOne
	@JoinColumn(name="FROM_STATUS_ID", referencedColumnName="STATUS_ID", nullable=false)
	private OTGroupStatus fromStatus;

    @ManyToOne
	@JoinColumn(name="TO_STATUS_ID", referencedColumnName="STATUS_ID", nullable=false)
	private OTGroupStatus toStatus;

	public OTGroupStatus getFromStatus() {
		return fromStatus;
	}

	public void setFromStatus(OTGroupStatus fromStatus) {
		this.fromStatus = fromStatus;
	}

	public OTGroupStatus getToStatus() {
		return toStatus;
	}

	public void setToStatus(OTGroupStatus toStatus) {
		this.toStatus = toStatus;
	}
}
