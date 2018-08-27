package gov.gsa.ocfo.aloha.model.entity.overtime;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

@Entity
@DiscriminatorValue(value="I")
@NamedQueries({
	@NamedQuery(name="retrieveAllOTIndivStatusTrans", 
				query="SELECT row FROM OTIndivStatusTrans row")	        	
})
public class OTIndivStatusTrans extends OTStatusTrans {
	private static final long serialVersionUID = -8810054792534158939L;

	public interface QueryNames{
		public static final String RETRIEVE_ALL = "retrieveAllOTIndivStatusTrans";
	}

	public interface ActionCodeValues {
		public static final String NONE_TO_SUBMITTED 		= "NONE_TO_SUBMITTED";
		
		public static final String APPROVED_TO_CANCELLED	= "APPROVED_TO_CANCELLED";		
		
		public static final String SUBMITTED_TO_RECEIVED 	= "SUBMITTED_TO_RECEIVED";
		public static final String SUBMITTED_TO_APPROVED 	= "SUBMITTED_TO_APPROVED";
		public static final String SUBMITTED_TO_DENIED 		= "SUBMITTED_TO_DENIED";
		public static final String SUBMITTED_TO_CANCELLED	= "SUBMITTED_TO_CANCELLED";
		public static final String SUBMITTED_TO_RESUBMITTED = "SUBMITTED_TO_RESUBMITTED";
		public static final String SUBMITTED_TO_MODIFIED 	= "SUBMITTED_TO_MODIFIED";
		
		public static final String RESUBMITTED_TO_RESUBMITTED	= "RESUBMITTED_TO_RESUBMITTED";
		public static final String RESUBMITTED_TO_CANCELLED 	= "RESUBMITTED_TO_CANCELLED";
		public static final String RESUBMITTED_TO_DENIED 		= "RESUBMITTED_TO_DENIED";
		public static final String RESUBMITTED_TO_RECEIVED 		= "RESUBMITTED_TO_RECEIVED";
		public static final String RESUBMITTED_TO_APPROVED 		= "RESUBMITTED_TO_APPROVED";
		public static final String RESUBMITTED_TO_MODIFIED 		= "RESUBMITTED_TO_MODIFIED";
		
		public static final String RECEIVED_TO_MODIFIED			= "RECEIVED_TO_MODIFIED";
		public static final String RECEIVED_TO_APPROVED			= "RECEIVED_TO_APPROVED";
		public static final String RECEIVED_TO_DENIED			= "RECEIVED_TO_DENIED";
		public static final String RECEIVED_TO_CANCELLED		= "RECEIVED_TO_CANCELLED";			
		
		public static final String RECEIVED_TO_RECEIVED_WITH_MODS 	= "RECEIVED_TO_RECEIVED_WITH_MODS";
		public static final String RECEIVED_WITH_MODS_TO_RECEIVED_WITH_MODS = "RECEIVED_WITH_MODS_TO_RECEIVED_WITH_MODS";
		public static final String RECEIVED_WITH_MODS_TO_CANCELLED 	= "RECEIVED_WITH_MODS_TO_CANCELLED";
		public static final String RECEIVED_WITH_MODS_TO_APPROVED 	= "RECEIVED_WITH_MODS_TO_APPROVED";
		public static final String RECEIVED_WITH_MODS_TO_DENIED 	= "RECEIVED_WITH_MODS_TO_DENIED";
		
		public static final String MODIFIED_TO_APPROVED 	= "MODIFIED_TO_APPROVED";
		public static final String MODIFIED_TO_DENIED 		= "MODIFIED_TO_DENIED";
		public static final String MODIFIED_TO_MODIFIED 	= "MODIFIED_TO_MODIFIED";
		public static final String MODIFIED_TO_RECEIVED 	= "MODIFIED_TO_RECEIVED";
		public static final String MODIFIED_TO_CANCELLED	= "MODIFIED_TO_CANCELLED";
	}	
	
    @ManyToOne
	@JoinColumn(name="FROM_STATUS_ID", referencedColumnName="STATUS_ID", nullable=false)
	private OTIndivStatus fromStatus;

    @ManyToOne
	@JoinColumn(name="TO_STATUS_ID", referencedColumnName="STATUS_ID", nullable=false)
	private OTIndivStatus toStatus;

	public OTIndivStatus getFromStatus() {
		return fromStatus;
	}

	public void setFromStatus(OTIndivStatus fromStatus) {
		this.fromStatus = fromStatus;
	}

	public OTIndivStatus getToStatus() {
		return toStatus;
	}

	public void setToStatus(OTIndivStatus toStatus) {
		this.toStatus = toStatus;
	}
}
