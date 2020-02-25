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

public class AudioStreamDescriptor extends Descriptor {

	private final int freeFormatFlag;
	private final int id;
	private final int layer;
	private final int variableRateAudioIndicator;
	private final int reserved;


	public AudioStreamDescriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset,parent);
		freeFormatFlag = Utils.getInt(b, offset+2, 1, 0x80)>>7;
		id = Utils.getInt(b, offset+2, 1, 0x40)>>6;
		layer = Utils.getInt(b, offset+2, 1, 0x30)>>4;
		variableRateAudioIndicator = Utils.getInt(b, offset+2, 1, 0x08)>>3;
		reserved = Utils.getInt(b, offset+2, 1, 0x07);


	}

	@Override
	public String toString() {
		return super.toString() + " freeFormatFlag"+freeFormatFlag ;
	}



	public int getFreeFormatFlag() {
		return freeFormatFlag;
	}


	public int getLayer() {
		return layer;
	}

	public int getId() {
		return id;
	}

	public int getVariableRateAudioIndicator() {
		return variableRateAudioIndicator;
	}

	public int getReserved() {
		return reserved;
	}

}
