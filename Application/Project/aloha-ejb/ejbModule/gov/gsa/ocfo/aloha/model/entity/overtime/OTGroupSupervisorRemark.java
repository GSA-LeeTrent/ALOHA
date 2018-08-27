package gov.gsa.ocfo.aloha.model.entity.overtime;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value="S")
public class OTGroupSupervisorRemark extends OTGroupRemark {
	private static final long serialVersionUID = -102595520441847363L;

}
