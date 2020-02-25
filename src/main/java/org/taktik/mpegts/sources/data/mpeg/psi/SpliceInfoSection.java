/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2019 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package org.taktik.mpegts.sources.data.mpeg.psi;

import org.taktik.mpegts.sources.PID;
import org.taktik.mpegts.sources.PsiSectionData;
import org.taktik.mpegts.sources.data.mpeg.descriptors.Descriptor;
import org.taktik.mpegts.sources.data.mpeg.descriptors.DescriptorFactory;
import org.taktik.utils.BitSource;
import org.taktik.utils.TreeNode;
import org.taktik.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;


/**
 * based on ANSI/SCTE 35 2017
 * 
 * http://www.scte.org/SCTEDocs/Standards/SCTE%2035%202017.pdf
 * 
 * @author Eric
 *
 */
public class SpliceInfoSection extends TableSection {
	
	public class BandwidthReservation  {
		
		private BandwidthReservation(BitSource bs){
			super();
		}




	}

	private static final Logger logger = Logger.getLogger(SpliceInfoSection.class.getName());
	
	public class SpliceTime  {
		
		private int time_specified_flag;
		private long pts_time;
		
		private SpliceTime(BitSource bs){
			time_specified_flag = bs.readBits(1);
			if(time_specified_flag==1){
				bs.readBits(6);
				pts_time = bs.readBitsLong(33);
			}else{
				bs.readBits(7);
			}
		}



		public int getTime_specified_flag() {
			return time_specified_flag;
		}

		public long getPts_time() {
			return pts_time;
		}
	}
	
	
	private class BreakDuration {

		private int auto_return;
		private int reserved;
		private long duration;

		private BreakDuration(BitSource bs) {
			auto_return = bs.readBits(1);
			reserved = bs.readBits(6);
			duration = bs.readBitsLong(33);
		}


	}
	
	public class SpliceInsert {
		
		private long splice_event_id;
		private int splice_event_cancel_indicator;
		private int out_of_network_indicator;
		private int program_splice_flag;
		private int duration_flag;
		private int splice_immediate_flag;
		
		private int unique_program_id;
		private int avail_num;
		private int avails_expected;
		private int component_count;
		private SpliceTime splice_time;
		private BreakDuration break_duration;
		private List<Integer> componentTags = new ArrayList<>();
		private List<SpliceTime> componentSpliceTimes = new ArrayList<>();
		
		private SpliceInsert(BitSource bitSource){
			splice_event_id =  bitSource.readBitsLong(32); //getLong(data, 14, 4, MASK_32BITS);
			splice_event_cancel_indicator = bitSource.readBits(1); //Utils.getInt(data,18,1,0x80)>>7;
			bitSource.readBits(7); // reserved
			if(splice_event_cancel_indicator==0){
				out_of_network_indicator =  bitSource.readBits(1); //Utils.getInt(data,19,1,0x80)>>7;
				program_splice_flag =  bitSource.readBits(1); //Utils.getInt(data,19,1,0x40)>>6;
				duration_flag =  bitSource.readBits(1); //Utils.getInt(data,19,1,0x20)>>5;
				splice_immediate_flag =  bitSource.readBits(1); //Utils.getInt(data,19,1,0x10)>>4;
				 bitSource.readBits(4); // reserved
				if((program_splice_flag == 1) && (splice_immediate_flag == 0)){
					splice_time = new SpliceTime(bitSource);
				}
				if(program_splice_flag == 0) {
					component_count = bitSource.readBits(8);// Utils.getInt(data,20,1,Utils.MASK_8BITS);
					for(int i=0;i<component_count;i++) {
						int component_tag = bitSource.readBits(8) ; //8 uimsbf
						componentTags .add(component_tag);
						 if(splice_immediate_flag == 0){
							 SpliceTime component_splice_time = new SpliceTime(bitSource);
							 componentSpliceTimes.add(component_splice_time);
						}
					}
				}
				if(duration_flag == 1){
					break_duration = new BreakDuration(bitSource);
				}
				unique_program_id = bitSource.readBits(16); // bs.readBits(16);
				avail_num =  bitSource.readBits(8); //bs.readBits(8);
				avails_expected =  bitSource.readBits(8); //bs.readBits(8);
			}
		}



		public long getSplice_event_id() {
			return splice_event_id;
		}

		public int getSplice_event_cancel_indicator() {
			return splice_event_cancel_indicator;
		}

		public int getOut_of_network_indicator() {
			return out_of_network_indicator;
		}

		public int getProgram_splice_flag() {
			return program_splice_flag;
		}

		public int getDuration_flag() {
			return duration_flag;
		}

		public int getSplice_immediate_flag() {
			return splice_immediate_flag;
		}

		public int getUnique_program_id() {
			return unique_program_id;
		}

		public int getAvail_num() {
			return avail_num;
		}

		public int getAvails_expected() {
			return avails_expected;
		}

		public int getComponent_count() {
			return component_count;
		}

		public SpliceTime getSplice_time() {
			return splice_time;
		}

		public BreakDuration getBreak_duration() {
			return break_duration;
		}

		public List<Integer> getComponentTags() {
			return componentTags;
		}

		public List<SpliceTime> getComponentSpliceTimes() {
			return componentSpliceTimes;
		}	
	}
	
	public class TimeSignal {
		
		private SpliceTime splice_time;

		public SpliceTime getSplice_time() {
			return splice_time;
		}

		private TimeSignal(BitSource bs){
			super();
			splice_time = new SpliceTime(bs);
		}
		

		
	}
	
	private class SpliceNull{

		private SpliceNull(BitSource bs){
			super();
		}
		

		
	}

	private int protocol_version;
	private int encrypted_packet;
	private int encryption_algorithm;
	private long pts_adjustment;
	private int cw_index;
	private int tier;
	private int splice_command_length;
	private int splice_command_type;
	private int descriptor_loop_length;
	
	List<Descriptor> splice_descriptors = new ArrayList<>();
	private TreeNode splice_command;


	public SpliceInfoSection(final PsiSectionData raw_data, final PID parent){
		super(raw_data,parent);
		final byte[] data = raw_data.getData();
		BitSource bitSource = new BitSource(data, 3, sectionLength);
		protocol_version = bitSource.readBits(8); // Utils.getInt(data,3,1,Utils.MASK_8BITS);
		encrypted_packet =  bitSource.readBits(1); //Utils.getInt(data,4,1,0x80)>>7;
		encryption_algorithm =  bitSource.readBits(6); //Utils.getInt(data,4,1,0x7E)>>1;
		
		pts_adjustment =  bitSource.readBitsLong(33); //getLong(data,4, 5, MASK_33BITS);
		cw_index =  bitSource.readBits(8); //Utils.getInt(data,9,1,Utils.MASK_8BITS);
		tier =  bitSource.readBits(12); //Utils.getInt(data,10,2,0xFFF0)>>4;
		splice_command_length =  bitSource.readBits(12); //Utils.getInt(data,11,2,Utils.MASK_12BITS);
		if(encrypted_packet==0){
			splice_command_type=  bitSource.readBits(8); //Utils.getInt(data,13,1,Utils.MASK_8BITS);
			if(splice_command_type==0){
				splice_command = (TreeNode) new SpliceNull(bitSource);
			}else if(splice_command_type==5){
				splice_command = (TreeNode) new SpliceInsert(bitSource);
			}else if(splice_command_type==6){
				splice_command = (TreeNode) new TimeSignal(bitSource);
			}else if(splice_command_type==7){
				splice_command = (TreeNode) new BandwidthReservation(bitSource);
			}else{
				logger.info("Not implemented: splice_command_type="+splice_command_type+" ("+getSpliceCommandTypeString(splice_command_type)+")");
			}
			
			int loopstart = 0;
			if ((splice_command_type == 0) || (splice_command_type == 5)|| (splice_command_type == 7)) {
				loopstart = bitSource.getNextFullByteOffset();
			} else if (splice_command_length != 0xFFF) {
				loopstart = 14 + splice_command_length;
			}
			if (loopstart != 0) {
				descriptor_loop_length = Utils.getInt(data, loopstart, 2, Utils.MASK_12BITS);
				splice_descriptors = DescriptorFactory.buildDescriptorList(data,
						loopstart + 2,
						descriptor_loop_length,
						this);
			}
		
		}
	}




	private static String getSpliceCommandTypeString(int splice_command_type){
		switch (splice_command_type) {
		case 0:
			return "splice_null";
		case 4:
			return "splice_schedule";
		case 5:
			return "splice_insert";
		case 6:
			return "time_signal";
		case 7:
			return "bandwidth_reservation";
		case 255:
			return "private_command";

		default:
			return "Reserved";
		}
	}


	public int getProtocol_version() {
		return protocol_version;
	}


	public int getEncrypted_packet() {
		return encrypted_packet;
	}


	public int getEncryption_algorithm() {
		return encryption_algorithm;
	}


	public long getPts_adjustment() {
		return pts_adjustment;
	}


	public int getCw_index() {
		return cw_index;
	}


	public int getTier() {
		return tier;
	}


	public int getSplice_command_length() {
		return splice_command_length;
	}


	public int getSplice_command_type() {
		return splice_command_type;
	}


	public int getDescriptor_loop_length() {
		return descriptor_loop_length;
	}


	public List<Descriptor> getSplice_descriptors() {
		return splice_descriptors;
	}


	public TreeNode getSplice_command() {
		return splice_command;
	}

}
