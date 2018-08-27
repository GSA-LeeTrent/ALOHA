package gov.gsa.ocfo.aloha.model.leave;

import java.util.Comparator;

public class LeaveRemarkComparator implements Comparator<LeaveRemark> {

	@Override
	public int compare(LeaveRemark o1, LeaveRemark o2) {
		if (o1.getRemarkDate().compareTo(o2.getRemarkDate()) == 1) {
			return 1;
		} else if (o1.getRemarkDate().compareTo(o2.getRemarkDate()) == -1) {
			return -1;
		} else {
			return 0;
		}
	}
}
