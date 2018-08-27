package gov.gsa.ocfo.aloha.comparator;

import gov.gsa.ocfo.aloha.model.entity.leave.LeaveHistory;

import java.util.Comparator;

public class LeaveHistoryComparator implements Comparator<LeaveHistory> {
//	@Override
//	public int compare(LeaveHistory o1, LeaveHistory o2) {
//		if (o1.getId() > o2.getId()) {
//			return 1;
//		} else if (o1.getId() < o2.getId()) {
//			return -1;
//		} else {
//			return 0;
//		}
//	}
	
	@Override
	public int compare(LeaveHistory o1, LeaveHistory o2) {
		if (o1.getActionDatetime().compareTo(o2.getActionDatetime()) == 1) {
			return 1;
		} else if (o1.getActionDatetime().compareTo(o2.getActionDatetime()) == -1) {
			return -1;
		} else {
			return 0;
		}
	}	
}