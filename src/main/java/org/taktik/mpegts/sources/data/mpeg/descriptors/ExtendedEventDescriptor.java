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
import org.taktik.utils.DVBString;

import java.util.ArrayList;
import java.util.List;

import static org.taktik.utils.Utils.*;

public class ExtendedEventDescriptor extends Descriptor {

	public static class Item{
		private DVBString itemDescription;
		private DVBString item;


		public Item(final DVBString itemDescription, final DVBString item) {
			super();
			this.itemDescription = itemDescription;
			this.item = item;
		}
		public DVBString getItem() {
			return item;
		}
		public void setItem(final DVBString item) {
			this.item = item;
		}
		public DVBString getItemDescription() {
			return itemDescription;
		}
		public void setItemDescription(final DVBString itemDescription) {
			this.itemDescription = itemDescription;
		}



	}

	private final int descriptorNumber;
	private final int lastDescriptorNumber;
	private String  iso639LanguageCode;
	private final int lengthOfItems;
	private final List<Item> itemList = new ArrayList<Item>();

	private final DVBString text;

	public ExtendedEventDescriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset,parent);
		descriptorNumber = getInt(b, offset+2, 1, 0xF0)>>4;
		lastDescriptorNumber = getInt(b, offset+2, 1, MASK_4BITS);
		iso639LanguageCode = getISO8859_1String(b,offset+3,3);
		lengthOfItems= getInt(b, offset+6, 1, MASK_8BITS);

		int t=offset+7;
		while (t<(lengthOfItems+offset+7)) {
			final int item_description_length = getInt(b, t, 1, MASK_8BITS);
			final DVBString item_descripton=new DVBString(b, t);
			final int item_length = getInt(b, t+1+item_description_length, 1, MASK_8BITS);
			final DVBString item=new DVBString(b, t+1+item_description_length);

			final Item i = new Item(item_descripton,item);
			itemList.add(i);
			t+=2+item_description_length+item_length;
		}

		text = new DVBString(b,offset+7 +lengthOfItems);

	}

	public String getIso639LanguageCode() {
		return iso639LanguageCode;
	}

	public void setIso639LanguageCode(final String networkName) {
		this.iso639LanguageCode = networkName;
	}

	@Override
	public String toString() {
		return super.toString() + " text="+text;
	}



	public int getDescriptorNumber() {
		return descriptorNumber;
	}

	public int getLastDescriptorNumber() {
		return lastDescriptorNumber;
	}

	public int getLengthOfItems() {
		return lengthOfItems;
	}

	public List<Item> getItemList() {
		return itemList;
	}

	public DVBString getText() {
		return text;
	}

}
