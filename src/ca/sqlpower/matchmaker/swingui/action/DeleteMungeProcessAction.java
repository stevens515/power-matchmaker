/*
 * Copyright (c) 2007, SQL Power Group Inc.
 *
 * This file is part of Power*MatchMaker.
 *
 * Power*MatchMaker is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Power*MatchMaker is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package ca.sqlpower.matchmaker.swingui.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.tree.TreePath;

import ca.sqlpower.matchmaker.munge.MungeProcess;
import ca.sqlpower.matchmaker.swingui.MatchMakerSwingSession;
import ca.sqlpower.matchmaker.swingui.MatchMakerTreeModel;


public class DeleteMungeProcessAction extends AbstractAction {

    private final MatchMakerSwingSession swingSession;
    private final MungeProcess mungeProcess;

	public DeleteMungeProcessAction(MatchMakerSwingSession swingSession, MungeProcess mungeProcess) {
		super("Delete Munge Process");
        this.swingSession = swingSession;
		this.mungeProcess = mungeProcess;
	}

	public void actionPerformed(ActionEvent e) {
		int responds = JOptionPane.showConfirmDialog(swingSession.getFrame(),
		"Are you sure you want to delete the munge process?");
		if (responds != JOptionPane.YES_OPTION)
			return;
		MatchMakerTreeModel treeModel = (MatchMakerTreeModel)swingSession.getTree().getModel();
		TreePath treePath = treeModel.getPathForNode(mungeProcess.getParent());
		swingSession.getTree().setSelectionPath(treePath);
		swingSession.delete(mungeProcess);
		swingSession.setCurrentEditorComponent(null);
	}
}