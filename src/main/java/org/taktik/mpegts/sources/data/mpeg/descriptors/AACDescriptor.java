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

package org.taktik.mpegts.sources.data.mpeg.descriptors;

import org.taktik.mpegts.sources.data.mpeg.psi.TableSection;

import static org.taktik.utils.Utils.*;

public class AACDescriptor extends Descriptor {


	private final int profile_and_level;
	private final int aac_type_flag;
	private int aac_type;
	private byte[] additional_info;


	public AACDescriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset,parent);
		profile_and_level = getInt(b, offset+2, 1, MASK_8BITS);
		aac_type_flag = getInt(b, offset+3, 1, 0x80)>>7;
		int t=offset+4;
		if(aac_type_flag==1){
			aac_type = getInt(b, t++, 1, MASK_8BITS);
		}
		if(t<descriptorLength){
			additional_info=getBytes(b, t, descriptorLength-t);
		}
	}




}
