package org.taktik.mpegts.sources.data.mpeg.psi;
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
 * based on ETSI ES 201 812 V1.1.1, ETSI TS 102 809 V1.1.1, ETSI TS 102 796 V1.1.1
 */

import org.taktik.mpegts.sources.PSI;

import java.util.HashMap;
import java.util.Map;

public class UNTs extends AbstractPSITabel{


	public UNTs(final PSI parentPSI) {
		super(parentPSI);

	}

	private Map<Integer, UNT> unts = new HashMap<Integer, UNT>();

	public void update(final UNTsection section){

		final int pid = section.getParentPID().getPid();
		UNT  unt= unts.get(pid);

		if(unt==null){
			unt = new UNT(parentPSI);
			unts.put(pid, unt);
		}
		unt.update(section);
	}



}
