/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2017 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

public abstract class AbstractPSITabel {

	protected PSI parentPSI;

	protected AbstractPSITabel(final PSI parentPSI) {
		super();
		this.parentPSI = parentPSI;
	}

	public PSI getParentPSI() {
		return parentPSI;
	}

	public void setParentPSI(final PSI parentPSI) {
		this.parentPSI = parentPSI;
	}


	public static void updateSectionVersion(final TableSection newSection, final TableSection section) {

		TableSection last = section;

		while(last.getNextVersion()!=null){
			last = last.getNextVersion();
		}
		if(last.equals(newSection)){ // already have an instance if this section, just update the stats on the existing section
			int previousPacketNo = last.getLast_packet_no();
			int distance = newSection.getPacket_no() - previousPacketNo;
			if(distance>last.getMaxPacketDistance()){
				last.setMaxPacketDistance(distance);
			}
			if(distance<last.getMinPacketDistance()){
				last.setMinPacketDistance(distance);
			}
			last.setLast_packet_no(newSection.getPacket_no());
			last.setOccurrence_count(last.getOccurrence_count()+1);
		}else{
			last.setNextVersion(newSection);
		}
	}

}
