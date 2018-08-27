package gov.gsa.ocfo.aloha.comparator;

import gov.gsa.ocfo.aloha.model.entity.leave.LeaveDetail;

import java.util.Comparator;

public class LeaveDetailComparator implements Comparator<LeaveDetail> {
	@Override
	public int compare(LeaveDetail o1, LeaveDetail o2) {
		int seqOne = o1.getSequence();
		int seqTwo = o2.getSequence();
		
		if (seqOne > seqTwo) {
			return 1;
		} else if (seqOne < seqTwo) {
			return -1;
		} else {
			return 0;
		}
	}
}
