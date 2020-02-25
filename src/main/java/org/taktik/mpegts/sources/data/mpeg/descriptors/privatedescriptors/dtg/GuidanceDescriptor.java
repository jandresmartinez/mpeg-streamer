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

package org.taktik.mpegts.sources.data.mpeg.descriptors.privatedescriptors.dtg;

import org.taktik.mpegts.sources.data.mpeg.descriptors.Descriptor;
import org.taktik.mpegts.sources.data.mpeg.psi.TableSection;

import static org.taktik.utils.Utils.*;

public class GuidanceDescriptor extends Descriptor {


	int reserved;
	int guidance_type;
	String iso_639_language_code;
	String guidance_char; // TODO should be DVBString
	int guidance_mode;
	int reserved2;
	byte[] reserved_for_future_use;


	public GuidanceDescriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset,parent);
		reserved = getInt(b, offset+2, 1, 0xFC)>>2;
		guidance_type = getInt(b, offset+2, 1, MASK_2BITS);
		if(guidance_type==0){
			iso_639_language_code=getISO8859_1String(b, offset+3, 3);
			guidance_char = getString(b, offset+6, descriptorLength-4);
		}else if(guidance_type==1){
			reserved2 = getInt(b, offset+3, 1, 0xFE)>>1;
			guidance_mode = getInt(b, offset+3, 1, MASK_1BIT);
			iso_639_language_code=getISO8859_1String(b, offset+4, 3);
			guidance_char = getString(b, offset+7, descriptorLength-5);
		}else{
			reserved_for_future_use = getBytes(b, offset+3, descriptorLength-3);
		}

	}



	@Override
	public String getDescriptorname(){
		return "DTG guidance_descriptor";
	}

}
