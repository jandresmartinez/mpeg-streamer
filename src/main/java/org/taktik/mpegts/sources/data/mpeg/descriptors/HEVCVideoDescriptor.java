/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2015-2018 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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
import org.taktik.utils.Utils;

public class HEVCVideoDescriptor extends Descriptor {
	
	private final int profile_space;
	private final int tier_flag;
	private final int profile_idc;
	private final long profile_compatibility_indication;
	private final int progressive_source_flag;
	private final int interlaced_source_flag;
	private final int non_packed_constraint_flag;
	private final int frame_only_constraint_flag;
	private final long copied_44bits;
	private final int level_idc;
	private final int temporal_layer_subset_flag;
	private final int HEVC_still_present_flag;
	private final int HEVC_24hr_picture_present_flag;
	private final int sub_pic_hrd_params_not_present_flag;
	private final int reserved1;
	private final int HDR_WCG_idc;
	
	private int temporal_id_min;
	private int reserved2;
	private int temporal_id_max;
	private int reserved3;
	
	private static final String[] HDR_WCG_idc_Strings = {"SDR", "WCG only", "both WCG and HDR", "no indication"};

	public HEVCVideoDescriptor(byte[] b, int offset, TableSection parent) {
		super(b, offset, parent);
		profile_space = Utils.getInt(b, offset + 2, 1, 0xc0) >>> 6;
		tier_flag = Utils.getInt(b, offset + 2, 1, 0x20) >>> 5;
		profile_idc = Utils.getInt(b, offset + 2, 1, Utils.MASK_5BITS);
		profile_compatibility_indication = Utils.getLong(b, offset + 3, 4, Utils.MASK_32BITS);
		progressive_source_flag = Utils.getInt(b, offset + 7, 1, 0x80) >>> 7;
		interlaced_source_flag = Utils.getInt(b, offset + 7, 1, 0x40) >>> 6;
		non_packed_constraint_flag = Utils.getInt(b, offset + 7, 1, 0x20) >>> 5;
		frame_only_constraint_flag = Utils.getInt(b, offset + 7, 1, 0x10) >>> 4;
		copied_44bits = Utils.getLong(b, offset + 7, 6, 0xfffffffffffL);
		level_idc = Utils.getInt(b, offset + 13, 1, Utils.MASK_8BITS);
		temporal_layer_subset_flag = Utils.getInt(b, offset + 14, 1, 0x80) >>> 7;
		HEVC_still_present_flag = Utils.getInt(b, offset + 14, 1, 0x40) >>> 6;
		HEVC_24hr_picture_present_flag = Utils.getInt(b, offset + 14, 1, 0x20) >>> 5;
		sub_pic_hrd_params_not_present_flag = Utils.getInt(b, offset + 14, 1, 0x10) >>> 4;
		reserved1 = Utils.getInt(b, offset + 14, 1, 0x0c) >>> 2;
		HDR_WCG_idc = Utils.getInt(b, offset + 14, 1, Utils.MASK_2BITS);
		if(temporal_layer_subset_flag == 1) {
			temporal_id_min = Utils.getInt(b, offset + 15, 1, 0xe0) >>> 5;
			reserved2 = Utils.getInt(b, offset + 15, 1, Utils.MASK_5BITS);
			temporal_id_max = Utils.getInt(b, offset + 16, 1, 0xe0) >>> 5;
			reserved3 = Utils.getInt(b, offset + 16, 1, Utils.MASK_5BITS);
		}
	}
	
	public static final String getHdrWcgIdcString(int value){
		if(value < 0 || value > 3)
			throw new IllegalArgumentException("Invalid value in getHdrWcgIdcString:"+value);
		return HDR_WCG_idc_Strings[value];
	}
	

}
