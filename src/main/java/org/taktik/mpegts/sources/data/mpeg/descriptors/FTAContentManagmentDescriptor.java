/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2014 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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
 * @author Eric
 */

package org.taktik.mpegts.sources.data.mpeg.descriptors;

import org.taktik.mpegts.sources.data.mpeg.psi.TableSection;

import static org.taktik.utils.Utils.getInt;

public class FTAContentManagmentDescriptor extends Descriptor {


	private final int user_defined;
	private int reserved_future_use;
	private int do_not_scramble;
	private int control_remote_access_over_internet;
	private int do_not_apply_revocation;


	public FTAContentManagmentDescriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset,parent);
		user_defined = getInt(b, offset+2, 1, 0X80)>>7;
		reserved_future_use = getInt(b, offset+2, 1, 0X70)>>4;
		do_not_scramble = getInt(b, offset+2, 1, 0X08)>>3;
		control_remote_access_over_internet = getInt(b, offset+2, 1, 0X06)>>1;
		do_not_apply_revocation = getInt(b, offset+2, 1, 0X01);
	}



	public static String getControlRemoteAccesOverInternetString(final int control_remote_access_over_internet) {
		switch (control_remote_access_over_internet) {

		case 0x00:
			 return "Redistribution over the Internet is enabled";
		case 0x01:
			return "Redistribution over the Internet is enabled but only within a managed domain";
		case 0x02:
			 return "Redistribution over the Internet is enabled but only within a managed domain and after a certain short period of time (e.g. 24 hours)";
		case 0x03:
			return "Redistribution over the Internet is not allowed with the following exception: Redistribution over the Internet within a managed domain is enabled after a specified long (possibly indefinite) period of time";
		default:
			return "Illegal value";

		}
	}
}
