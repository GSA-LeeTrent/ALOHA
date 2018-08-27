package gov.gsa.ocfo.aloha.web.listener;

import com.icesoft.faces.facelets.component.treetable.ITreeTableListener;
import com.icesoft.faces.facelets.component.treetable.TreeTableNode;

public class GroupTreeTableListener implements ITreeTableListener {
	public boolean isNodeMoveAllowed(TreeTableNode parent,	TreeTableNode child) {
		return false;
	}
}