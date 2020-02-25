/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2019 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package org.taktik.mpegts.sources;


import org.taktik.mpegts.sources.data.mpeg.psi.*;

/**
 * Container for all PSI related data
 *
 * @author Eric Berendsen
 *
 */
public class PSI {


	private final PAT pat = new PAT(this);
	private final CAT cat = new CAT(this);
	private final BAT bat = new BAT(this);
	private final PMTs pmts = new PMTs(this);
	private final TDT tdt = new TDT(this);
	private final TOT tot = new TOT(this);
	private final SIT sit = new SIT(this);
	private final NetworkSync networkSync = new NetworkSync(this);
	private final INT int_table = new INT(this);
	private final UNTs unt_table = new UNTs(this);
	private final AITs ait_table = new AITs(this);
	private final RCTs rct_table = new RCTs(this);

	private final SCTE35 scte35_table = new SCTE35(this);


	public PMTs getPmts() {
		return pmts;
	}

	public PAT getPat() {
		return pat;
	}

	public CAT getCat() {
		return cat;
	}
	public TOT getTot() {
		return tot;
	}
	public TDT getTdt() {
		return tdt;
	}
	public BAT getBat() {
		return bat;
	}

	public INT getInt() {
		return int_table;
	}
	public UNTs getUnts() {
		return unt_table;
	}
	public AITs getAits() {
		return ait_table;
	}

	public NetworkSync getNetworkSync() {
		return networkSync;
	}
	/**
	 * TODO, work in progress
	 * @param packetNo
	 * @return the PAT valid at moment of packetNO, i.e. for which all sections have been received,
	 * and which has not yet been replaced with a complete new PAT. If no complete PAT has been received yet at moment packetNo, the next complete PAT will be returned.
	 */
	public PAT getPat(final long packetNo) {
		if(pat!=null){
			return pat.getPat(packetNo);
		}
		return null;
	}
	public RCTs getRcts() {
		return rct_table;
	}
	public SIT getSit() {
		return sit;
	}
	public INT getInt_table() {
		return int_table;
	}
	public UNTs getUnt_table() {
		return unt_table;
	}
	public AITs getAit_table() {
		return ait_table;
	}
	public RCTs getRct_table() {
		return rct_table;
	}

	public SCTE35 getScte35_table() {
		return scte35_table;
	}



}
