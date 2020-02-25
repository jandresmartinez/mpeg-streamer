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

public class ParentalRatingDescriptor extends Descriptor {

	private List<Rating> ratingList = new ArrayList<Rating>();


	public static class Rating {
		/**
		 *
		 */

		private final String countryCode;
		private final int rating;



		public Rating(final String countryCode, final int rating) {
			super();
			this.countryCode = countryCode;
			this.rating = rating;
		}




		@Override
		public String toString(){
			return "countryCode:'"+countryCode;
		}



		public int getRating() {
			return rating;
		}



		public String getCountryCode() {
			return countryCode;
		}


	}

	public ParentalRatingDescriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset,parent);
		int t=0;
		while (t<descriptorLength) {
			final String countryCode=getISO8859_1String(b, offset+t+2, 3);
			final int rating = getInt(b, offset+t+5, 1, MASK_8BITS);
			final Rating s = new Rating(countryCode,rating);
			ratingList.add(s);
			t+=4;
		}
	}

	@Override
	public String toString() {
		final StringBuilder buf = new StringBuilder(super.toString());
		for (Rating rating : ratingList) {
			buf.append(rating.toString());
		}


		return buf.toString();
	}

	public static String getRatingTypeAge(final int type) {

		if(type==0){
			return "undefined";
		}else if((1<=type)&&(type<=0x0F)){
			return "minimum age = "+ Integer.toString((type+3));
		}else{
			return "defined by the broadcaster";
		}
	}


	public List<Rating> getRatingList() {
		return ratingList;
	}
}
