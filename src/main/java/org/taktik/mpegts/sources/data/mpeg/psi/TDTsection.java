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

import org.taktik.mpegts.sources.PID;
import org.taktik.mpegts.sources.PsiSectionData;
import org.taktik.utils.Utils;


public class TDTsection extends TableSection {

	private byte[] UTC_time;

	public TDTsection(final PsiSectionData raw_data, final PID parent){
		super(raw_data,parent);
		UTC_time= Utils.copyOfRange(raw_data.getData(),3,8 );
	}



	@Override
	public String toString(){
		final StringBuilder b = new StringBuilder("TDTsection UTC_Time=");
		b.append(Utils.toHexString(UTC_time)).append(", UTC_timeString=").append(getUTC_timeString()).append(", length=").append(getSectionLength());
		return b.toString();
	}




	public byte[] getUTC_time() {
		return UTC_time;
	}



	public String getUTC_timeString() {
		return Utils.getUTCFormattedString(UTC_time);
	}

}
