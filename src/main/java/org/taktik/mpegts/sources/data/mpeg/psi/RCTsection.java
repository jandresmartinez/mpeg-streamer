/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2014 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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
import org.taktik.mpegts.sources.data.mpeg.descriptors.DescriptorFactory;
import org.taktik.utils.BitSource;
import org.taktik.utils.DVBString;
import org.taktik.utils.LookUpList;
import org.taktik.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Eric
 *
 * based on ETSI TS 102 323 V1.5.1 (2012-01) Digital Video Broadcasting (DVB);
 * Carriage and signalling of TV-Anytime information
 * in DVB transport streams
 * 10.4 Related content table
 */
public class RCTsection extends TableSectionExtendedSyntax {

	private static LookUpList link_type_list = new LookUpList.Builder().
			add(0x0,"Link information is a URI string only").
			add(0x1,"Link information is a binary locator only").
			add(0x2,"Link information is both a binary locator and a URI string").
			add(0x3,"Link information is through means of a descriptor").
			add(0x4 ,0xF,"DVB reserved").
			build();

	private static LookUpList how_related_classification_scheme_id_list = new LookUpList.Builder().
			add(0x00,"urn:tva:metadata:HowRelatedCS:2004 [25], clause A.3.").
			add(0x01,"urn:tva:metadata:HowRelatedCS:2005 [26], clause A.3.").
			add(0x02,"urn:tva:metadata:HowRelatedCS:2007 [4], clause A.3.").
			add(0x03,0x2F,"DVB reserved").
			add(0x30,0x3F,"User Private").
			build();

	private int year_offset;
	private int link_count;
	private int descriptor_loop_length;
	private List<Descriptor>descriptor_loop;
	private List<LinkInfo>	links = new ArrayList<>();

	public class LinkInfo  {

		public class PromotionalText {

			private final String iso639LanguageCode;
			private final DVBString promotional_text;

			/**
			 * @param iso639LanguageCode
			 * @param promotional_text
			 */
			private PromotionalText(String iso639LanguageCode,
					DVBString promotional_text) {
				super();
				this.iso639LanguageCode = iso639LanguageCode;
				this.promotional_text = promotional_text;
			}


		}

		private byte[] data;
		private int offset;
		private int len;

		private final int link_type;
		private final int how_related_classification_scheme_id;

		private final int term_id;
		private final int group_id;
		private final int precedence;

		private int media_uri_length;
		private byte[] media_uri_byte;

		private int number_items;
		private List<PromotionalText> promotional_items = new ArrayList<>();

		private int default_icon_flag;
		private int icon_id;
		private int descriptor_loop_length;
		private List<Descriptor>descriptor_loop;

		protected LinkInfo(byte[] data, int offset, int len){
			this.data = data;
			this.len = len;
			this.offset = offset;
			final BitSource bs =new BitSource(this.data,offset,len);
			link_type = bs.readBits(4);
			bs.readBits(2); // reserved_future_use
			how_related_classification_scheme_id = bs.readBits(6);

			term_id  = bs.readBits(12);
			group_id  = bs.readBits(4);
			precedence = bs.readBits(4);

			if ((link_type == 0x00) || (link_type == 0x02)) {
				media_uri_length  = bs.readBits( 8);
				media_uri_byte = bs.readBytes(media_uri_length);
			}
			if((link_type == 0x01) ||(link_type == 0x02)) {
				throw new RuntimeException("Unimplemented dvb_binary_locator()");
			}
			bs.readBits(2); // reserved_future_use
			number_items = bs.readBits(6);
			for(int k = 0; k< number_items; k++){
				byte [] iso_639_2_language_code = bs.readBytes(3);
				//int promotional_text_length = bs.readBits(8);
				//DVBString promotional_text_char = bs.readDVBString();
				//PromotionalText pt = new PromotionalText(new String(iso_639_2_language_code),promotional_text_char);
				//promotional_items.add(pt);
			}
			default_icon_flag  = bs.readBits(1);
			icon_id  = bs.readBits(3);
			descriptor_loop_length  = bs.readBits(12);
			byte[] descriptorData = bs.readBytes(descriptor_loop_length);
			descriptor_loop = DescriptorFactory.buildDescriptorList(descriptorData, 0, descriptor_loop_length, RCTsection.this);

		}


	}

	public RCTsection(final PsiSectionData raw_data, final PID parent){
		super(raw_data, parent);

		byte[] b = raw_data.getData();

		year_offset = Utils.getInt(b, 8, 2, Utils.MASK_16BITS);
		link_count = Utils.getInt(b, 10, 1, Utils.MASK_8BITS);
		int localOffset = 11;
		for (int i = 0; i < link_count; i++) {
			int link_info_length = Utils.getInt(b, localOffset, 2, Utils.MASK_12BITS);
			LinkInfo linkInfo = new LinkInfo(b, localOffset+2, link_info_length);
			links.add(linkInfo);
			localOffset +=2+link_info_length;
		}

		descriptor_loop_length = Utils.getInt(b, localOffset, 2, Utils.MASK_12BITS);
		localOffset += 2;

		descriptor_loop = DescriptorFactory.buildDescriptorList(b, localOffset,
				descriptor_loop_length, this);
	}

	@Override
	public String toString() {
		final StringBuilder b = new StringBuilder("AITsection section=");
		b.append(getSectionNumber()).append(", lastSection=").append(getSectionLastNumber()).append(", tableType=")
		.append(getTableType(tableId)).append(", ");

		return b.toString();
	}



}
