package org.taktik.mpegts.sources.data.mpeg.psi;

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
 * 	based on EN 301 192 § 8.4.4
 */

import org.taktik.mpegts.sources.PID;
import org.taktik.mpegts.sources.PsiSectionData;
import org.taktik.mpegts.sources.data.mpeg.descriptors.Descriptor;
import org.taktik.mpegts.sources.data.mpeg.descriptors.DescriptorFactory;
import org.taktik.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class INTsection extends TableSectionExtendedSyntax {

	private List<Descriptor> platformdescriptorList;
	private List<TargetLoop> targetLoopList;
	private int action_type;
	private int platform_id_hash;
	private int platformID;
	private int processing_order;
	private int platform_descriptor_loop_length;

	public static class TargetLoop {
		private int target_descriptor_loop_length;
		private int operational_descriptor_loop_length;

		private List<Descriptor> target_descriptor_loop;
		private List<Descriptor> operational_descriptor_loop;








		public List<Descriptor> getOperational_descriptor_loop() {
			return operational_descriptor_loop;
		}




		public void setOperational_descriptor_loop(
				final List<Descriptor> operational_descriptor_loop) {
			this.operational_descriptor_loop = operational_descriptor_loop;
		}




		public int getOperational_descriptor_loop_length() {
			return operational_descriptor_loop_length;
		}




		public void setOperational_descriptor_loop_length(
				final int operational_descriptor_loop_length) {
			this.operational_descriptor_loop_length = operational_descriptor_loop_length;
		}




		public List<Descriptor> getTarget_descriptor_loop() {
			return target_descriptor_loop;
		}




		public void setTarget_descriptor_loop(final List<Descriptor> target_descriptor_loop) {
			this.target_descriptor_loop = target_descriptor_loop;
		}




		public int getTarget_descriptor_loop_length() {
			return target_descriptor_loop_length;
		}




		public void setTarget_descriptor_loop_length(final int target_descriptor_loop_length) {
			this.target_descriptor_loop_length = target_descriptor_loop_length;
		}

	}



	public INTsection(final PsiSectionData raw_data, final PID parent){
		super(raw_data,parent);

		action_type = Utils.getInt(raw_data.getData(), 3, 1, Utils.MASK_8BITS); //tableIdExtension first byte
		platform_id_hash = Utils.getInt(raw_data.getData(), 4, 1, Utils.MASK_8BITS); //tableIdExtension first byte

		platformID = Utils.getInt(raw_data.getData(), 8, 3, Utils.MASK_24BITS);
		processing_order = Utils.getInt(raw_data.getData(), 11, 1, Utils.MASK_8BITS);

		platform_descriptor_loop_length = Utils.getInt(raw_data.getData(), 12, 2, Utils.MASK_12BITS);
		platformdescriptorList = DescriptorFactory.buildDescriptorList(raw_data.getData(),14,platform_descriptor_loop_length,this);

		targetLoopList = buildTargetLoopList(raw_data.getData(), 14+platform_descriptor_loop_length , sectionLength - platform_descriptor_loop_length - 18);
	}


	@Override
	public String toString(){
		final StringBuilder b = new StringBuilder("INTsection section=");
		b.append(getSectionNumber()).append(", lastSection=").append(getSectionLastNumber()).append(", tableType=").append(getTableType(tableId)).append(", ");

		return b.toString();
	}




	private List<TargetLoop> buildTargetLoopList(final byte[] data, final int i, final int programInfoLength) {
		final ArrayList<TargetLoop> r = new ArrayList<TargetLoop>();
		int t =0;
		while(t<programInfoLength){
			final TargetLoop c = new TargetLoop();
			c.setTarget_descriptor_loop_length(Utils.getInt(data, i+t, 2, Utils.MASK_12BITS));
			c.setTarget_descriptor_loop(DescriptorFactory.buildDescriptorList(data,i+t+2,c.getTarget_descriptor_loop_length(),this));


			t+=2+c.getTarget_descriptor_loop_length();
			c.setOperational_descriptor_loop_length(Utils.getInt(data, i+t, 2, Utils.MASK_12BITS));
			c.setOperational_descriptor_loop(DescriptorFactory.buildDescriptorList(data,i+t+2,c.getOperational_descriptor_loop_length(),this));
			r.add(c);
			t+=2+c.getOperational_descriptor_loop_length();

		}

		return r;
	}







	public int getPlatformID() {
		return platformID;
	}


	public int getAction_type() {
		return action_type;
	}


}
