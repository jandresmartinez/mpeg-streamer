/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2018 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

import org.taktik.mpegts.sources.data.mpeg.descriptors.mpeg.HEVCTimingAndHRDDescriptor;
import org.taktik.mpegts.sources.data.mpeg.descriptors.mpeg.MPEGExtensionDescriptor;
import org.taktik.mpegts.sources.data.mpeg.descriptors.privatedescriptors.casema.ZiggoPackageDescriptor;
import org.taktik.mpegts.sources.data.mpeg.descriptors.privatedescriptors.casema.ZiggoVodDeliveryDescriptor;
import org.taktik.mpegts.sources.data.mpeg.descriptors.privatedescriptors.casema.ZiggoVodURLDescriptor;
import org.taktik.mpegts.sources.data.mpeg.descriptors.privatedescriptors.ciplus.CIProtectionDescriptor;
import org.taktik.mpegts.sources.data.mpeg.descriptors.privatedescriptors.dtg.GuidanceDescriptor;
import org.taktik.mpegts.sources.data.mpeg.descriptors.privatedescriptors.eaccam.EACEMStreamIdentifierDescriptor;
import org.taktik.mpegts.sources.data.mpeg.descriptors.privatedescriptors.eaccam.HDSimulcastLogicalChannelDescriptor;
import org.taktik.mpegts.sources.data.mpeg.descriptors.privatedescriptors.eaccam.LogicalChannelDescriptor;
import org.taktik.mpegts.sources.data.mpeg.descriptors.privatedescriptors.m7fastscan.M7LogicalChannelDescriptor;
import org.taktik.mpegts.sources.data.mpeg.descriptors.privatedescriptors.nordig.NordigLogicalChannelDescriptorV1;
import org.taktik.mpegts.sources.data.mpeg.descriptors.privatedescriptors.nordig.NordigLogicalChannelDescriptorV2;
import org.taktik.mpegts.sources.data.mpeg.descriptors.privatedescriptors.opencable.EBPDescriptor;
import org.taktik.mpegts.sources.data.mpeg.descriptors.privatedescriptors.scte.SCTEAdaptationFieldDataDescriptor;
import org.taktik.mpegts.sources.data.mpeg.descriptors.privatedescriptors.upc.UPCLogicalChannelDescriptor;
import org.taktik.mpegts.sources.data.mpeg.descriptors.scte35.AvailDescriptor;
import org.taktik.mpegts.sources.data.mpeg.descriptors.scte35.SCTE35Descriptor;
import org.taktik.mpegts.sources.data.mpeg.descriptors.scte35.SegmentationDescriptor;
import org.taktik.mpegts.sources.data.mpeg.psi.TableSection;
import org.taktik.utils.PreferencesManager;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static java.lang.Byte.toUnsignedInt;

public final class DescriptorFactory {

	/**
	 *
	 */
	private DescriptorFactory() {
		// static only
	}

	// TODO check which descriptors are allowed in which PSI tables
	private static final Logger	logger	= Logger.getLogger(DescriptorFactory.class.getName());


	/**
	 * @param data
	 * @param offset
	 * @param len
	 * @param tableSection
	 * @return List of Descriptor
	 */
	public static List<Descriptor> buildDescriptorList(final byte[] data, final int offset, final int len,
			final TableSection tableSection) {
		long private_data_specifier = PreferencesManager.getDefaultPrivateDataSpecifier();
		final List<Descriptor> r = new ArrayList<Descriptor>();
		int t = 0;

		while (t < len) {

			Descriptor d;
			final int descriptorTag = toUnsignedInt(data[t + offset]);
			try {
				if(descriptorTag == 0xE9) {
					// OpenCable™ Specifications 
					// Encoder Boundary Point Specification 
					// OC-SP-EBP-I01-130118 
					// Should be user private descriptor, but no private_data_specifier
					// exists for OpenCable / SCTE
					// For now no conflict with other private descriptors
					// 
					// TODO Make this switchable (user preferences)
					//
					d = new EBPDescriptor(data, t + offset, tableSection);
				}else if (descriptorTag == 0x97) {
					// OpenCable™ Specifications 
					// Encoder Boundary Point Specification 
					// OC-SP-EBP-I01-130118 
					// Should be user private descriptor, but no private_data_specifier
					// exists for OpenCable / SCTE
					// For now no conflict with other private descriptors
					// 
					// TODO Make this switchable (user preferences)
					//
					
					d = new SCTEAdaptationFieldDataDescriptor(data, t + offset, tableSection);
				}else if(descriptorTag == 0x83 && tableSection.getTableId() == 0xBC && PreferencesManager.isEnableM7Fastscan() ) { // FNTsection
					d = new M7LogicalChannelDescriptor(data, t + offset, tableSection);
				} else if (descriptorTag <= 0x3f) {
				 if (tableSection.getTableId() == 0xFC) {
						d = getSCTE35Descriptor(data, offset, tableSection, t);
					} else {
						d = getMPEGDescriptor(data, offset, tableSection, t);
					}
				}  else {
					d = getPrivateDVBSIDescriptor(data, offset, tableSection, t, private_data_specifier);

				}
			} catch (final RuntimeException iae) {
				// this can happen because there is an error in our code (constructor of a descriptor), OR the stream is invalid.
				// fall back to a standard Descriptor (this is highly unlikely to fail), so processing can continue
				d = new Descriptor(data, t + offset, tableSection);
				logger.warning("Fall back for descriptor:" + toUnsignedInt(data[t + offset]) + " ("
						+ Descriptor.getDescriptorname(toUnsignedInt(data[t + offset]), tableSection)
						+ ")in section " + TableSection.getTableType(tableSection.getTableId()) + " (" + tableSection
						+ ",) data=" + d.getRawDataString()+", RuntimeException:"+iae);
			}

			t += d.getDescriptorLength() + 2;
			r.add(d);
			if (d instanceof PrivateDataSpecifierDescriptor) {
				final PrivateDataSpecifierDescriptor privateDescriptor = (PrivateDataSpecifierDescriptor) d;
				private_data_specifier = privateDescriptor.getPrivateDataSpecifier();
			}
			if (d instanceof PrivateDataIndicatorDescriptor) { // TODO check is this interchangeable with
				// PrivateDataSpecifierDescriptor?
				final PrivateDataIndicatorDescriptor privateDescriptor = (PrivateDataIndicatorDescriptor) d;
				private_data_specifier = privateDescriptor.getPrivateDataIndicator();
			}
		}

		return r;
	}

	/**
	 * @param data
	 * @param offset
	 * @param tableSection
	 * @param t
	 * @param private_data_specifier
	 * @return
	 */
	private static Descriptor getPrivateDVBSIDescriptor(final byte[] data, final int offset, final TableSection tableSection, final int t,
			final long private_data_specifier) {
		Descriptor d = null;

		if (private_data_specifier == 0x600) { // UPC1
			switch (toUnsignedInt(data[t + offset])) {
			case 0x81:
				d = new UPCLogicalChannelDescriptor(data, t + offset, tableSection);
				break;
			case 0x87:
				d = new ZiggoVodDeliveryDescriptor(data, t + offset, tableSection);
				break;
			}
		} else if (private_data_specifier == 0x16) { // Casema / Ziggo
			switch (toUnsignedInt(data[t + offset])) {
			case 0x87:
				d = new ZiggoVodDeliveryDescriptor(data, t + offset, tableSection);
				break;
			case 0x93:
				d = new ZiggoVodURLDescriptor(data, t + offset, tableSection);
				break;
			case 0xD4:
				d = new ZiggoPackageDescriptor(data, t + offset, tableSection);
				break;
			}
		} else if (private_data_specifier == 0x28) { // EACEM
			switch (toUnsignedInt(data[t + offset])) {
			case 0x83:
				d = new LogicalChannelDescriptor(data, t + offset, tableSection);
				break;
			case 0x86:
				d = new EACEMStreamIdentifierDescriptor(data, t + offset, tableSection);
				break;
			case 0x88:
				d = new HDSimulcastLogicalChannelDescriptor(data, t + offset, tableSection);
				break;
			}
		} else if (private_data_specifier == 0x29) { // Nordig
			switch (toUnsignedInt(data[t + offset])) {
			case 0x83:
				d = new NordigLogicalChannelDescriptorV1(data, t + offset, tableSection);
				break;
			case 0x87:
				d = new NordigLogicalChannelDescriptorV2(data, t + offset, tableSection);
				break;
			}


		} else if (private_data_specifier == 0x40) { // CI Plus LLP
			switch (toUnsignedInt(data[t + offset])) {
			case 0xCE:
				d = new CIProtectionDescriptor(data, t + offset, tableSection);
				break;
			}
		} else if (private_data_specifier == 0x233a) { // DTG
			switch (toUnsignedInt(data[t + offset])) {

			case 0x89:
				d = new GuidanceDescriptor(data, t + offset, tableSection);
				break;
			}
		}
		if (d == null) {
			logger.info("Unimplemented private descriptor, private_data_specifier=" + private_data_specifier
					+ ", descriptortag=" + toUnsignedInt(data[t + offset]) + ", tableSection=" + tableSection);
			d = new Descriptor(data, t + offset, tableSection);
		}

		return d;
	}

	private static Descriptor getMPEGDescriptor(final byte[] data, final int offset, final TableSection tableSection, final int t) {
		Descriptor d;
		switch (toUnsignedInt(data[t + offset])) {
		case 0x02:
			d = new VideoStreamDescriptor(data, t + offset, tableSection);
			break;
		case 0x03:
			d = new AudioStreamDescriptor(data, t + offset, tableSection);
			break;
		case 0x04:
			d = new HierarchyDescriptor(data, t + offset, tableSection);
			break;
		case 0x05:
			d = new RegistrationDescriptor(data, t + offset, tableSection);
			break;
		case 0x06:
			d = new DataStreamAlignmentDescriptor(data, t + offset, tableSection);
			break;
		case 0x07:
			d = new TargetBackGroundDescriptor(data, t + offset, tableSection);
			break;
		case 0x08:
			d = new VideoWindowDescriptor(data, t + offset, tableSection);
			break;
		case 0x09:
			d = new CADescriptor(data, t + offset, tableSection);
			break;
		case 0x0A:
			d = new ISO639LanguageDescriptor(data, t + offset, tableSection);
			break;
		case 0x0B:
			d = new SystemClockDescriptor(data, t + offset, tableSection);
			break;
		case 0x0C:
			d = new MultiplexBufferUtilizationDescriptor(data, t + offset, tableSection);
			break;
		case 0x0E:
			d = new MaximumBitrateDescriptor(data, t + offset, tableSection);
			break;
		case 0x0F:
			d = new PrivateDataIndicatorDescriptor(data, t + offset, tableSection);
			break;
		case 0x10:
			d = new SmoothingBufferDescriptor(data, t + offset, tableSection);
			break;
		case 0x11:
			d = new STDDescriptor(data, t + offset, tableSection);
			break;
			// 0x12 IBP_descriptor as found in iso/conformance/hhi.m2t
		case 0x12:
			d = new IBPDescriptor(data, t + offset, tableSection);
			break;
		case 0x13:
			d = new CarouselIdentifierDescriptor(data, t + offset, tableSection);
			break;
		case 0x14:
			d = new AssociationTagDescriptor(data, t + offset, tableSection);
			break;
		case 0x1A:
			d = new StreamEventDescriptor(data, t + offset, tableSection);
			break;
		case 0x1C:
			d = new Mpeg4AudioDescriptor(data, t + offset, tableSection);
			break;
		case 0x25:
			d = new MetaDataPointerDescriptor(data, t + offset, tableSection);
			break;
		case 0x26:
			d = new MetaDataDescriptor(data, t + offset, tableSection);
			break;
		case 0x28:
			d = new AVCVideoDescriptor(data, t + offset, tableSection);
			break;
		case 0x2A:
			d = new AVCTimingAndHRDDescriptor(data, t + offset, tableSection);
			break;
		case 0x2B:
			d = new AACMpeg2Descriptor(data, t + offset, tableSection);
			break;
		case 0x32:
			d = new JPEG2000VideoDescriptor(data, t + offset, tableSection);
			break;
		case 0x38:
			d = new HEVCVideoDescriptor(data, t + offset, tableSection);
			break;
		case 0x3F:
			d = getMPEGExtendedDescriptor(data, offset, tableSection, t);
			break;
		default:
			d = new Descriptor(data, t + offset, tableSection);
			logger.info("Not implemented descriptor:" + toUnsignedInt(data[t + offset]) + " ("
					+ Descriptor.getDescriptorname(toUnsignedInt(data[t + offset]), tableSection)
					+ ")in section " + TableSection.getTableType(tableSection.getTableId()) + " (" + tableSection
					+ ",) data=" + d.getRawDataString());
			break;
		}
		return d;
	}
	
	private static MPEGExtensionDescriptor getMPEGExtendedDescriptor(final byte[] data, final int offset,
																	 final TableSection tableSection, final int t) {

		MPEGExtensionDescriptor d;
		final int descriptor_tag_extension = toUnsignedInt(data[t + offset+2]);
		switch(descriptor_tag_extension){
		
		case 0x03:
			d = new HEVCTimingAndHRDDescriptor(data, t + offset, tableSection);
			break;
		default:
			d = new MPEGExtensionDescriptor(data, t + offset, tableSection);
			logger.warning("unimplemented MPEGExtensionDescriptor:" +
					d.getDescriptorTagString() +
					", TableSection:" + tableSection);
		}

		return d;
	}





	private static Descriptor getSCTE35Descriptor(final byte[] data, final int offset, final TableSection tableSection, final int t) {
		Descriptor d;
		switch (toUnsignedInt(data[t + offset])) {
		case 0x00:
			d = new AvailDescriptor(data, t + offset, tableSection);
			break;
		case 0x02:
			d = new SegmentationDescriptor(data, t + offset, tableSection);
			break;
		default:
			d = new SCTE35Descriptor(data, t + offset, tableSection);
			logger.info("Not implemented SCTE35Descriptor:" + toUnsignedInt(data[t + offset]) + " ("
					+ SCTE35Descriptor.getDescriptorname(toUnsignedInt(data[t + offset]), tableSection)
					+ ")in section " + TableSection.getTableType(tableSection.getTableId()) + " (" + tableSection
					+ ",) data=" + d.getRawDataString());
			break;
		}
		return d;
	}

}
