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

/**
 * @author Eric
 *
 */
public class S2SatelliteDeliverySystemDescriptor extends Descriptor {

	private final int scrambling_sequence_selector;
	private final int multiple_input_stream_flag;
	private final int backwards_compatibility_indicator;
	private int scrambling_sequence_index = 0;
	private int input_stream_identifier = 0;

	public S2SatelliteDeliverySystemDescriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset, parent);
		scrambling_sequence_selector = getInt(b, offset + 2, 1, 0x80) >> 7;
		multiple_input_stream_flag = getInt(b, offset + 2, 1, 0x40) >> 6;
		backwards_compatibility_indicator = getInt(b, offset + 2, 1, 0x20) >> 5;
		int off = offset+3;
		if (scrambling_sequence_selector == 1){
			// Reserved 6 bslbf
			scrambling_sequence_index = getInt(b, off, 3, MASK_18BITS) ;
			off+=3;
		}
		if (multiple_input_stream_flag == 1){
			input_stream_identifier = getInt(b, off, 1, MASK_8BITS) ;
			off+=1;
		}
	}


}
