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

import org.taktik.mpegts.MTSPacket;

import static org.taktik.utils.Utils.*;


// based on TS 101 191 V1.4.1 (2004-06) DVB mega-frame for Single Frequency Network (SFN) synchronization
public class MegaFrameInitializationPacket {


	//TSPacket tsPacket;
	private byte[] data;

	public MegaFrameInitializationPacket(final MTSPacket tsPack){
		data=new byte[tsPack.getBuffer().remaining()];
	}



	@Override
	public String toString(){
		final StringBuilder b = new StringBuilder("MegaFrameInitializationPacket");
		return b.toString();
	}



	public int getSynchronizationId() {
		return getInt(data, 0, 1, MASK_8BITS) ;
	}

	public int getSectionLength() {
		return getInt(data, 1, 1, MASK_8BITS) ;
	}

	public int getPointer() {
		return getInt(data, 2, 2, MASK_16BITS) ;
	}

	public int getPeriodicFlag() {
		return getInt(data, 4, 1, 0x80)>>7 ;
	}

	public int getFutureUse() {
		return getInt(data, 4, 2, MASK_15BITS) ;
	}

	public int getSynchronizationTimeStamp() {
		return getInt(data, 6, 3, MASK_24BITS);
	}

	public int getMaximumDelay() {
		return getInt(data, 9, 3, MASK_24BITS);
	}

	public long getTPS_MIP() {
		return getLong(data, 12, 4, MASK_32BITS);
	}

	public int getConstellation() {
		return getInt(data, 12, 1, 0xC0)>>6;
	}

	public static String getConstellationString(final int c){
		switch (c) {
		case 0:
			return "QPSK";
		case 1:
			return "16-QAM";
		case 2:
			return "64-QAM";
		case 3:
			return "Reserved";

		default:
			return "Illegal value";
		}
	}

	private int getHierarchy() {
		return getInt(data, 12, 1, 0x38)>>3;
	}

	public static String getHierarchyString(final int c){
		switch (c) {
		case 0:
			return "Non hierarchical";
		case 1:
			return "α = 1";
		case 2:
			return "α = 2";
		case 3:
			return "α = 4";

		default:
			return "see annex F of ETSI EN 300 744";
		}
	}

	public int getCodeRate() {
		return getInt(data, 12, 1, MASK_3BITS);
	}


	public static String getCodeRateString(final int c){
		switch (c) {
		case 0:
			return "1/2";
		case 1:
			return "2/3";
		case 2:
			return "3/4";
		case 3:
			return "5/6";
		case 4:
			return "7/8";

		default:
			return "reserved";
		}
	}

	public int getGuardInterval() {
		return getInt(data, 13, 1, 0xC0)>>6;
	}

	public static String getGuardIntervalString(final int c){
		switch (c) {
		case 0:
			return "1/32";
		case 1:
			return "1/16";
		case 2:
			return "1/8";
		case 3:
			return "1/4";

		default:
			return "Illegal value";
		}
	}


	public int getTransmissionMode() {
		return getInt(data, 13, 1, 0x30)>>4;
	}

	public static String getTransmissionModeString(final int c){
		switch (c) {
		case 0:
			return "2K mode";
		case 1:
			return "8K mode";
		case 2:
			return "see annex F of ETSI EN 300 744";
		case 3:
			return "reserved";

		default:
			return "Illegal value";
		}
	}


	public int getBandwidthChannel() {
		return getInt(data, 13, 1, 0x0C)>>2;
	}

	public static String getBandwidthChannelString(final int c){
		switch (c) {

		// DVBSnoop has 0 = 8MHZ, 1 = 7 MHZ.
		// who is right???
		case 0:
			return "7 MHz";
		case 1:
			return "8 MHz";
		case 2:
			return "6 MHz";
		case 3:
			return "bandwidth optionally signalled via bandwidth_function, see clause 6.1.7 of ETSI EN 300 744";

		default:
			return "Illegal value";
		}
	}

	public int getPriority() {
		return getInt(data, 13, 1, 0x02)>>1;
	}

	public int getDVBHSignalling() {
		return getInt(data, 13, 2, 0x0180)>>7;
	}

	public static String getDVBHSignallingString(final int c){
		switch (c) {
		case 0:
			return "Time Slicing is not used, MPE-FEC not used";
		case 1:
			return "Time Slicing is not used, At least one elementary stream uses MPE-FEC";
		case 2:
			return "At least one elementary stream uses Time Slicing, MPE-FEC not used";
		case 3:
			return "At least one elementary stream uses Time Slicing, At least one elementary stream uses MPE-FEC";

		default:
			return "Illegal value";
		}
	}

	public int getReserved() {
		return getInt(data, 14, 2, MASK_15BITS);
	}
	public int getIndividualAddressingLength() {
		return getInt(data, 16, 1, MASK_8BITS);
	}
}
