/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2012 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
 *
 *  This file is part of DVB Inspector.
 *
 *  DVB Inspector is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  DVB Inspector is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with DVB Inspector.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  The author requests that he be notified of any application, applet, or
 *  other binary that makes use of this code, but that's more out of curiosity
 *  than anything and is not required.
 *
 */

package org.taktik.mpegts.sources.data.mpeg.psi;

import org.taktik.mpegts.sources.PSI;
import org.taktik.mpegts.sources.data.mpeg.descriptors.Descriptor;

import java.util.ArrayList;
import java.util.List;

public class CAT extends AbstractPSITabel {

	private CAsection[]	cat	= null;

	public List<Descriptor> getDescriptorList() {
		final ArrayList<Descriptor> l = new ArrayList<Descriptor>();
		if(cat!=null){
			for (CAsection element : cat) {
				if (element != null) {
					l.addAll(element.getDescriptorList());
				}
			}
		}
		return l;
	}

	public CAT(final PSI parent) {
		super(parent);
	}

	/**
	 * @param section
	 */
	public void update(final CAsection section) {
		if (cat == null) {
			cat = new CAsection[section.getSectionLastNumber() + 1];
		}
		if (cat[section.getSectionNumber()] == null) {
			cat[section.getSectionNumber()] = section;
		} else {
			final TableSection last = cat[section.getSectionNumber()];
			updateSectionVersion(section, last);
		}

	}



}
