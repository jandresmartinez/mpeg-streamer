package org.taktik.mpegts.sources.data.mpeg.descriptors.mpeg;

import org.taktik.mpegts.sources.data.mpeg.psi.TableSection;

import static org.taktik.utils.Utils.*;

public class HEVCTimingAndHRDDescriptor extends MPEGExtensionDescriptor {

	private final int hrd_management_valid_flag;
	private final int reserved1;
	private final int picture_and_timing_info_present;

	private int _90kHz_flag;
	private int reserved2;
	private long n;
	private long k;
	private long num_units_in_tick;
	
	public HEVCTimingAndHRDDescriptor(byte[] b, int offset, TableSection parent) {
		super(b, offset, parent);
		hrd_management_valid_flag = getInt(b, offset+2, 1, 0x80)>>>7;
		reserved1 = getInt(b, offset+2, 1, 0x7e)>>>1;
		picture_and_timing_info_present = getInt(b, offset+2, 1, MASK_1BIT);
		int t=0;
		if(picture_and_timing_info_present==1){
			_90kHz_flag = getInt(b, offset+3, 1, 0x80)>>>7;
			reserved2 = getInt(b, offset+3, 1, MASK_7BITS);
			t+=1;
			if(_90kHz_flag==0){
				n = getLong(b, offset+4, 4, MASK_32BITS);
				k = getLong(b, offset+8, 4, MASK_32BITS);
				t+=8;
			}
			num_units_in_tick = getLong(b, offset+3+t, 4, MASK_32BITS);
		}
	}


}
