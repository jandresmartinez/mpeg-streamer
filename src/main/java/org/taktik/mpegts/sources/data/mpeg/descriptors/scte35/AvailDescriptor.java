package org.taktik.mpegts.sources.data.mpeg.descriptors.scte35;

import org.taktik.mpegts.sources.data.mpeg.psi.TableSection;

import static org.taktik.utils.Utils.getBytes;

public class AvailDescriptor extends SCTE35Descriptor {

	private byte[] provider_avail_id;


	public AvailDescriptor(byte[] b, int offset, TableSection parent) {
		super(b, offset, parent);
		provider_avail_id = getBytes(b,offset+6,4);
	}


}
