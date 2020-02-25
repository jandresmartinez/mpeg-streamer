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

package org.taktik.mpegts.sources.data.mpeg.descriptors.privatedescriptors.eaccam;

import org.taktik.mpegts.sources.data.mpeg.descriptors.Descriptor;
import org.taktik.mpegts.sources.data.mpeg.psi.TableSection;

import java.util.ArrayList;
import java.util.List;

import static org.taktik.utils.Utils.getInt;

public class HDSimulcastLogicalChannelDescriptor extends Descriptor {

	private final List<LogicalChannel> channelList = new ArrayList<LogicalChannel>();


	public class LogicalChannel {
		private int serviceID;
		private int visibleServiceFlag;

		private int logicalChannelNumber;

		public LogicalChannel(final int id, final int visibleService, final int type){
			serviceID = id;
			visibleServiceFlag = visibleService;
			logicalChannelNumber = type;
		}

		public int getServiceID() {
			return serviceID;
		}

		public void setServiceID(final int serviceID) {
			this.serviceID = serviceID;
		}

		public int getLogicalChannelNumber() {
			return logicalChannelNumber;
		}

		public void setLogicalChannelNumber(final int serviceType) {
			this.logicalChannelNumber = serviceType;
		}



		public int getVisibleServiceFlag() {
			return visibleServiceFlag;
		}

		public void setVisibleServiceFlag(final int visibleServiceFlag) {
			this.visibleServiceFlag = visibleServiceFlag;
		}

	}

	public HDSimulcastLogicalChannelDescriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset,parent);
		int t=0;
		while (t<descriptorLength) {

			final int serviceId=getInt(b, offset+t+2,2,0xFFFF);
			final int visisble = (b[offset+t+4] & 0x80) >>7;
			final int chNumber=getInt(b, offset+t+4,2,0x03FF);

			final LogicalChannel s = new LogicalChannel(serviceId, visisble, chNumber);
			channelList.add(s);
			t+=4;
		}
	}

	public int getNoServices(){
		return channelList.size();
	}


	@Override
	public String toString() {
		final StringBuilder buf = new StringBuilder(super.toString());
		for (int i = 0; i < getNoServices(); i++) {
			final LogicalChannel s = channelList.get(i);
			buf.append("(").append(i).append(";").append(s.getServiceID()).append(":").append(s.getLogicalChannelNumber()).append(":").append(s.getVisibleServiceFlag()).append("),");
		}


		return buf.toString();
	}



	@Override
	public String getDescriptorname(){
		return "HD_simulcast_logical_channel_descriptor";
	}

	public List<LogicalChannel> getChannelList() {
		return channelList;
	}

}
