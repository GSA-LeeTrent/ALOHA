package gov.gsa.ocfo.aloha.model.entity.overtime;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value="R")
public class OTGroupReceiverRemark extends OTGroupRemark {
	private static final long serialVersionUID = 7638177525975325295L;
}