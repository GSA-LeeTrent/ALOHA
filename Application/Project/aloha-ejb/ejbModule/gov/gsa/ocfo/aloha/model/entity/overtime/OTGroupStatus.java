package gov.gsa.ocfo.aloha.model.entity.overtime;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

@Entity
@DiscriminatorValue(value="G")
@NamedQueries({
	@NamedQuery(name="retrieveAllOTGroupStatus", 
				query="SELECT row FROM OTGroupStatus row WHERE row.displayOrder <> 0 ORDER BY row.displayOrder ASC")	        	
})
public class OTGroupStatus extends OTStatus {
	private static final long serialVersionUID = -5022054264305846040L;
	
	public interface QueryNames{
		public static final String RETRIEVE_ALL = "retrieveAllOTGroupStatus";
	}
	
	public interface CodeValues {
		public static final String NONE 		= "NONE";
		public static final String PENDING 		= "PENDING";
		public static final String CANCELLED	= "CANCELLED";
		public static final String RECEIVED 	= "RECEIVED";
		public static final String SUBMITTED 	= "SUBMITTED";
		public static final String FINAL		= "FINAL";
	}
}
