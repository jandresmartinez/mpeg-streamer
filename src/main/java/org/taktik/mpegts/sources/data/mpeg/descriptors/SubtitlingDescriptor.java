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

import java.util.ArrayList;
import java.util.List;

public class SubtitlingDescriptor extends Descriptor {

	private final List<Subtitle> subtitleList = new ArrayList<Subtitle>();


	public static class Subtitle {
		/**
		 *
		 */
		private final String iso639LanguageCode;
		private final int subtitlingType ;
		private final int compositionPageId;
		private final int ancillaryPageId;


		public Subtitle(final String lCode, final int sType,final int sCompositionPageDd,final int aPageId){
			iso639LanguageCode = lCode;
			subtitlingType = sType;
			compositionPageId = sCompositionPageDd;
			ancillaryPageId = aPageId;
		}





		@Override
		public String toString(){
			return "code:'"+iso639LanguageCode;
		}


		public String getIso639LanguageCode() {
			return iso639LanguageCode;
		}


		public int getSubtitlingType() {
			return subtitlingType;
		}


		public int getCompositionPageId() {
			return compositionPageId;
		}


		public int getAncillaryPageId() {
			return ancillaryPageId;
		}


	}

	public SubtitlingDescriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset,parent);
		int t=0;
		while (t<descriptorLength) {
			final String languageCode= Utils.getISO8859_1String(b, offset+2+t, 3);
			final int subtitling_type = Utils.getInt(b, offset+5+t, 1, Utils.MASK_8BITS);
			final int composition_page_id = Utils.getInt(b, offset+6+t, 2, Utils.MASK_16BITS);
			final int ancillary_page_id = Utils.getInt(b, offset+8+t, 2, Utils.MASK_16BITS);
			final Subtitle s = new Subtitle(languageCode, subtitling_type,composition_page_id,ancillary_page_id);
			subtitleList.add(s);
			t+=8;
		}
	}

	@Override
	public String toString() {
		final StringBuilder buf = new StringBuilder(super.toString());
		for (Subtitle subtitle : subtitleList) {
			buf.append(subtitle.toString());
		}


		return buf.toString();
	}


	public List<Subtitle> getSubtitleList() {
		return subtitleList;
	}
}
