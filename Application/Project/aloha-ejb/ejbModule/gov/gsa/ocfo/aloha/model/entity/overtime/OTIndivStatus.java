package gov.gsa.ocfo.aloha.model.entity.overtime;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

@Entity
@DiscriminatorValue(value="I")
@NamedQueries({
	@NamedQuery(name="retrieveAllOTIndivStatus", 
				query="SELECT row FROM OTIndivStatus row WHERE row.displayOrder <> 0 ORDER BY row.displayOrder ASC")	        	
})
public class OTIndivStatus extends OTStatus {
	private static final long serialVersionUID = 6150022423388623573L;

	public interface QueryNames{
		public static final String RETRIEVE_ALL = "retrieveAllOTIndivStatus";
	}

	public interface CodeValues {
		public static final String NONE 		= "NONE";
		public static final String SUBMITTED 	= "SUBMITTED";
		public static final String RECEIVED 	= "RECEIVED";
		public static final String APPROVED		= "APPROVED";
		public static final String DENIED 		= "DENIED";
		public static final String CANCELLED	= "CANCELLED";
		public static final String RESUBMITTED 	= "RESUBMITTED";
		public static final String MODIFIED 	= "MODIFIED";
		public static final String RECEIVED_WITH_MODS = "RECEIVED_WITH_MODS";		
	}
	public interface NameValues {
		public static final String NONE 		= "None";
		public static final String SUBMITTED 	= "Submitted";
		public static final String RECEIVED 	= "Received";
		public static final String APPROVED		= "Approved";
		public static final String DENIED 		= "Denied";
		public static final String CANCELLED	= "Cancelled";
		public static final String RESUBMITTED =  "Resubmitted";
		public static final String MODIFIED 	= "Modified";
		public static final String RECEIVED_WITH_MODS = "Received with Modifications";
	}	
}