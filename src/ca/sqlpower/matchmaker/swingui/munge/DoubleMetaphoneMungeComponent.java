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

package ca.sqlpower.matchmaker.swingui.munge;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import ca.sqlpower.matchmaker.munge.DoubleMetaphoneMungeStep;
import ca.sqlpower.matchmaker.munge.MungeStep;

public class DoubleMetaphoneMungeComponent extends AbstractMungeComponent {

	private JCheckBox useAlt;

	public DoubleMetaphoneMungeComponent(MungeStep step) {
		super(step);
	}

	@Override
	protected JPanel buildUI() {
		JPanel content = new JPanel();
		useAlt = new JCheckBox("Use Alternate Encoding");
		useAlt.setBackground(getBg());
		useAlt.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				DoubleMetaphoneMungeStep step = (DoubleMetaphoneMungeStep) getStep();
				step.setParameter(step.USE_ALTERNATE_PARAMETER_NAME, useAlt.isSelected());
			}
			
		});
		content.add(useAlt);
		return content;
	}
}