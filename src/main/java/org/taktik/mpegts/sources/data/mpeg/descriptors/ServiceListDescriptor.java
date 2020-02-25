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

import java.util.ArrayList;
import java.util.List;

import static org.taktik.utils.Utils.*;

public class ServiceListDescriptor extends Descriptor {

	private final List<Service> serviceList = new ArrayList<Service>();


	public class Service {
		private int serviceID;
		private int serviceType;

		public Service(final int id, final int type){
			serviceID = id;
			serviceType = type;
		}

		public int getServiceID() {
			return serviceID;
		}

		public void setServiceID(final int serviceID) {
			this.serviceID = serviceID;
		}

		public int getServiceType() {
			return serviceType;
		}

		public String getServiceTypeString() {
			return 	Descriptor.getServiceTypeString(serviceType);
		}



		public void setServiceType(final int serviceType) {
			this.serviceType = serviceType;
		}



	}

	public ServiceListDescriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset,parent);
		int t=0;
		while (t<descriptorLength) {
			final int serviceId=getInt(b, offset+2+t,2,MASK_16BITS);
			final int serviceType=getInt(b, offset+4+t,1,MASK_8BITS);
			final Service s = new Service( serviceId,serviceType);
			serviceList.add(s);
			t+=3;
		}
	}

	public int getNoServices(){
		return serviceList.size();
	}


	@Override
	public String toString() {
		final StringBuilder buf = new StringBuilder(super.toString());
		for (int i = 0; i < getNoServices(); i++) {
			final Service s = serviceList.get(i);
			buf.append("(").append(i).append(";").append(s.getServiceID()).append(":").append(s.getServiceTypeString()).append("),");
		}

		return buf.toString();
	}


	public List<Service> getServiceList() {
		return serviceList;
	}

}
