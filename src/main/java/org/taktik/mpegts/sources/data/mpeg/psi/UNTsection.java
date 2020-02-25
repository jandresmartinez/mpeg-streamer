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

 *
 *
 */

package org.taktik.mpegts.sources.data.mpeg.psi;

import org.taktik.mpegts.sources.PID;
import org.taktik.mpegts.sources.PsiSectionData;
import org.taktik.mpegts.sources.data.mpeg.descriptors.Descriptor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Eric Berendsen Represents a section of the Updata Notification Table.
 * @see PsiSectionData#isUNTSection(int) based on TS 102 006, § 9.4
 */
public class UNTsection extends TableSectionExtendedSyntax {

	private List<PlatformLoop>	platformLoopList	= new ArrayList<PlatformLoop>();
	private int					action_type;
	private int					oui_hash;
	private int					oui;
	private int					processing_order;
	private int					common_descriptor_loop_length;

	private List<Descriptor>	common_descriptor_loop;

	/**
	 * @param raw_data
	 * @param parent
	 */
	public UNTsection(PsiSectionData raw_data, PID parent) {
		super(raw_data, parent);
	}

	public static class PlatformLoop  {


		private int						platform_loop_length;
		private List<TargetLoop>		target_loop;




		public int getPlatform_loop_length() {
			return platform_loop_length;
		}

		public void setPlatform_loop_length(final int platform_loop_length) {
			this.platform_loop_length = platform_loop_length;
		}

		public List<TargetLoop> getTarget_loop() {
			return target_loop;
		}

		public void setTarget_loop(final List<TargetLoop> target_loop) {
			this.target_loop = target_loop;
		}

	}

	public static class TargetLoop  {

		private int			target_descriptor_loop_length;
		private int			operational_descriptor_loop_length;

		private List<Descriptor>	target_descriptor_loop;
		private List<Descriptor>	operational_descriptor_loop;



		public List<Descriptor> getOperational_descriptor_loop() {
			return operational_descriptor_loop;
		}

		public void setOperational_descriptor_loop(final List<Descriptor> operational_descriptor_loop) {
			this.operational_descriptor_loop = operational_descriptor_loop;
		}

		public int getOperational_descriptor_loop_length() {
			return operational_descriptor_loop_length;
		}

		public void setOperational_descriptor_loop_length(final int operational_descriptor_loop_length) {
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




	@Override
	public String toString() {
		final StringBuilder b = new StringBuilder("UNTsection section=");
		b.append(getSectionNumber()).append(", lastSection=").append(getSectionLastNumber()).append(", tableType=")
		.append(getTableType(tableId)).append(", ");

		return b.toString();
	}




	public int getOui() {
		return oui;
	}

	public int getAction_type() {
		return action_type;
	}

}
