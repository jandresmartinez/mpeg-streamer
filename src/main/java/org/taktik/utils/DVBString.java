/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2018 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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
package org.taktik.utils;

/**
 *
 * Representation of an DVB string, where 1th byte indicates length of string, and optional control codes to select a character table.
 *
 * In DVB strings are arrays of bytes. It is not necessary to copy them while constructing objects (save memory), and we don't always know how to use them (GUI or HTML)
 * This is just a reference to the original bytes, let the presentation logic decide what to do with it.
 *
 * first byte of array (data[offset]) always indicates length, so text may start at data[offset+1]
 * data[offset+1] may start with control code to select a character table.
 *
 * @see "ETSI EN 300 468, Annex A (normative):Coding of text characters"
 *
 *
 */
public class DVBString {

	private final byte[]data;
	private final int offset;

	public DVBString(final byte[] data, final int offset) {
		super();
		this.data = data;
		this.offset = offset;
	}















	public int getOffset() {
		return offset;
	}


	public byte[] getData() {
		return data;
	}

}
