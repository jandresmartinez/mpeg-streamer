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
import org.taktik.utils.Utils;

/**
 *
 * Based on 2.6.64 AVC video descriptor, ISO/IEC 13818-1:2013 / Rec. ITU-T H.222.0 (06/2012)
 * @author Eric
 *
 */
public class AVCVideoDescriptor extends Descriptor {


	private final int profile_idc;
	private final int constraint_set0_flag;
	private final int constraint_set1_flag;
	private final int constraint_set2_flag;
	private final int constraint_set3_flag;
	private final int constraint_set4_flag;
	private final int constraint_set5_flag;
	private final int AVC_compatible_flags;
	private final int level_idc;
	private final int AVC_still_present;
	private final int AVC_24_hour_picture_flag;
	private final int frame_packing_SEI_not_present_flag;
	private final int reserved;


	public AVCVideoDescriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset,parent);
		profile_idc = Utils.getInt(b, offset+2, 1, Utils.MASK_8BITS);
		constraint_set0_flag = Utils.getInt(b, offset+3, 1, 0x80)>>7;
		constraint_set1_flag = Utils.getInt(b, offset+3, 1, 0x40)>>6;
		constraint_set2_flag = Utils.getInt(b, offset+3, 1, 0x20)>>5;
		constraint_set3_flag = Utils.getInt(b, offset+3, 1, 0x10)>>4;
		constraint_set4_flag = Utils.getInt(b, offset+3, 1, 0x08)>>3;
		constraint_set5_flag = Utils.getInt(b, offset+3, 1, 0x04)>>2;
		AVC_compatible_flags = Utils.getInt(b, offset+3, 1, Utils.MASK_2BITS);
		level_idc = Utils.getInt(b, offset+4, 1, Utils.MASK_8BITS);
		AVC_still_present = Utils.getInt(b, offset+5, 1, 0x80)>>7;
		AVC_24_hour_picture_flag = Utils.getInt(b, offset+5, 1, 0x40)>>6;
		frame_packing_SEI_not_present_flag = Utils.getInt(b, offset+5, 1, 0x20)>>5;
		reserved = Utils.getInt(b, offset+5, 1, Utils.MASK_5BITS);
	}



}
