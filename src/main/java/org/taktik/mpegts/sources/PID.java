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

package org.taktik.mpegts.sources;


import org.taktik.mpegts.MTSPacket;
import org.taktik.mpegts.sources.data.mpeg.GeneralPidHandler;
import org.taktik.mpegts.sources.data.mpeg.PesHeader;
import org.taktik.mpegts.sources.data.mpeg.psi.GeneralPSITable;
import org.taktik.utils.PreferencesManager;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.Byte.toUnsignedInt;


/**
 * Collects all {@link MTSPacket}s with same packet_id, groups them together, and interprets them depending on type. For PSI packets tables are built, PES packets are (initially) only counted.
 * Does not store all data packets for this PID
 */
public class PID {
	
	
	private static final Logger logger = Logger.getLogger(PID.class.getName());

	public static final int PES = 1;
	public static final int PSI = 2;
	private int type=0;
	private boolean scrambled = false;

	private long bitRate = -1;

	/**
	 * if this PID is of type PSI, this table is used as a general representation of its data.
	 */
	private GeneralPSITable psi ;


	/**
	 * generalPesHandler that is able to interpret the PES_packet_data_byte, and turn it into something we can display
	 */
	private GeneralPidHandler generalPidHandler=null;

	/**
	 * number of TS packets in this PID
	 */
	private int packets = 0;
	/**
	 * number of different duplicate packets
	 */
	private int dup_packets = 0;
	/**
	 *  number of continuity_errors
	 */
	private long continuity_errors = 0;
	private int last_continuity_counter = -1;
	private int pid = -1;
	private long last_packet_no=-1;
	private MTSPacket last_packet = null;
	private int dup_found=0; // number of times current packet is duplicated. 
	private MTSPacket.AdaptationField.PCR lastPCR;
	private MTSPacket.AdaptationField.PCR firstPCR;
	private long lastPCRpacketNo = -1;
	private long firstPCRpacketNo =-1;
	private long pcr_count =-1;
	protected TransportStream parentTransportStream = null;

	private String label=null;
	private String shortLabel=null;

	private final GatherPIDData gatherer = new GatherPIDData();
	
	private final ArrayList<TimeStamp> pcrList = new ArrayList<>();
	private final ArrayList<TimeStamp> ptsList = new ArrayList<>();
	private final ArrayList<TimeStamp> dtsList = new ArrayList<>();
	
	private final HashMap<Integer, ArrayList<TemiTimeStamp>> temiList = new HashMap<Integer, ArrayList<TemiTimeStamp>>();

	/**
	 *
	 * This inner class is a helper that collects and groups MTSPackets for the containing PID into PsiSectionData's . If this PID contains PES data, the bytes are ignored.
	 * @author Eric Berendsen
	 *
	 */
	public class GatherPIDData {


		private PsiSectionData lastPSISection;

		public void reset(){
			lastPSISection = null;

		}

		private void processPayload(final MTSPacket packet, final TransportStream ts, final PID parentPID)
		{
			parentTransportStream = ts;
			final byte []data = new byte[ packet.getBuffer().remaining()];
			if((lastPSISection==null)){ // nothing started
				// sometimes PayloadUnitStartIndicator is 1, and there is no payload, so check AdaptationFieldControl
				if(packet.isPayloadUnitStartIndicator() &&
						(data.length>1) &&
						((packet.isAdaptationFieldExist())||(packet.isPayloadUnitStartIndicator()))){ //start something
					// at least one byte plus pointer available
					int start;
					int available;
					if((data[0]!=0)||((getPid() ==0)||(data[1]!=0))){ //starting PSI section after ofset
						// this is just an educated guess, it might still be private data of unspecified format
						type = PSI;

						start = 1 + toUnsignedInt(data[0]);
						available = data.length - start;
						while ((available > 0) && (toUnsignedInt(data[start]) != 0xFF)) {
							lastPSISection = new PsiSectionData(parentPID, packet.getPacketNo(), parentTransportStream);
							final int bytes_read = lastPSISection.readBytes(data, start, available);
							start += bytes_read;
							available -= bytes_read;
						}

						//	 could be starting PES stream, make sure it really is, Should start with packet_start_code_prefix -'0000 0000 0000 0000 0000 0001' (0x000001)
					}else if((data.length>2) && 
							(data[0]==0) &&
							(data[1]==0) &&
							(data[2]==1)){
						type = PES;
						
						if(PreferencesManager.isEnablePcrPtsView()) {
							try {
								// insert into PTS /DTS List
								PesHeader pesHeader = packet.getPesHeader();
								if((pesHeader!=null)&&(pesHeader.isValidPesHeader()&&pesHeader.hasExtendedHeader())){
	
									final int pts_dts_flags = pesHeader.getPts_dts_flags();
									if ((pts_dts_flags ==2) || (pts_dts_flags ==3)){ // PTS present,
										ptsList.add(new TimeStamp(packet.getPacketNo(), pesHeader.getPts()));
									}
									if (pts_dts_flags ==3){ // DTS present,
										dtsList.add(new TimeStamp(packet.getPacketNo(), pesHeader.getDts()));
									}
								}
							} catch (Exception e) {
								logger.log(Level.WARNING, "Error getting PTS/DTS from PESHeader in packet:"+packet.getPacketNo() + " from PID:"+parentPID.getPid(), e);
							}
						}
					}
				}
				//	something started
			}else if((packet.isAdaptationFieldExist())||(packet.isContainsPayload())){ // has payload?
				// are we in a PSI PID??
				if(type==PSI){
					int start;
					if(packet.isPayloadUnitStartIndicator()){ //first byte is pointer, skip pointer and continue with what we already got from previous MTSPacket
						start = 1;
					}else{
						start = 0;
					}
					int available = data.length -start;
					if(!lastPSISection.isComplete()){
						final int bytes_read=lastPSISection.readBytes(data, start, available);
						start+=bytes_read;
						available-=bytes_read;
					}
					while ((available > 0) && (toUnsignedInt(data[start]) != 0xFF)) {
						lastPSISection = new PsiSectionData(parentPID, packet.getPacketNo(), parentTransportStream);
						final int bytes_read = lastPSISection.readBytes(data, start, available);
						start += bytes_read;
						available -= bytes_read;
					}
				}
			}
		}
	}



	/**
	 * @param pid packet_id of packets in this PID
	 * @param ts
	 */
	public PID(final int pid, final TransportStream ts) {
		this.pid=pid;
		parentTransportStream = ts;
		psi = new GeneralPSITable(ts.getPsi());

	}

	/*public void updatePacket(final MTSPacket packet) {
		// handle 0x015 Mega-frame Initialization Packet (MIP)
		
		if(!packet.isTransportErrorIndicator()){
			updateNonErrorPacket(packet);
		}
		packets++;
	}*/

	/*private void updateNonErrorPacket(final MTSPacket packet) {
		if (pid == 0x015) {
			updateMegaFrameInitializationPacket(packet);
		} else {

			MTSPacket.AdaptationField adaptationField = null;
			if (packet.isAdaptationFieldExist()) {
				adaptationField = handleAdaptationField(packet);
			}
			if (isNormalPacket(packet, adaptationField)) {
				handleNormalPacket(packet);
			} else if (packet.isContainsPayload() && (last_continuity_counter == packet.getContinuityCounter())) {
				handleDuplicatePacket(packet);
			} else if (packet.isContainsPayload() || // not dup, and not consecutive, so error
					(last_continuity_counter != packet.getContinuityCounter()) // if no payload, counter should not
																				// increment
			) {
				handleContinuityError(packet);
			} // else{ // no payload, only adaptation. Don't Increase continuity_counter
		}
	}*/

	/**
	 * @param packet
	 * @param adaptationField
	 * @return true if this packet is expected here, i.e. first packet for this PID, or null packet, or has next continuityCounter, or disContinuity indicator has been set.
	 */
	private boolean isNormalPacket(final MTSPacket packet, MTSPacket.AdaptationField adaptationField) {
		return ((last_continuity_counter==-1)|| // first packet
				(pid==0x1fff)|| // null packet
				((((last_continuity_counter+1)%16)==packet.getContinuityCounter()))&&packet.isContainsPayload()) || // counter ok
				(adaptationField!=null && adaptationField.isDiscontinuityIndicator()) // discontinuity_indicator true
;
	}

	private void handleDuplicatePacket(final MTSPacket packet) {
		if(dup_found>=1){ // third or more dup packet (third total), illegal
			dup_found++;
			logger.warning("multiple dup packet ("+dup_found+"th total), illegal, PID="+pid+", last="+last_continuity_counter+", new="+packet.getContinuityCounter()+", last_no="+last_packet_no +", packet_no="+packet.getPacketNo());
		}else{ // just a dup, count it and ignore
			dup_found = 1;
			dup_packets++;
		}
	}

	private void handleNormalPacket(final MTSPacket packet) {
		last_continuity_counter = packet.getContinuityCounter();
		last_packet_no = packet.getPacketNo();
		last_packet = packet;
		dup_found = 0;

		if(packet.getScramblingControl()==0){ // not scrambled, or else payload is of no use
			gatherer.processPayload(packet,parentTransportStream,this);
		}else{
			scrambled=true;
		}
	}

	/*private MTSPacket.AdaptationField handleAdaptationField(final MTSPacket packet) {
		MTSPacket.AdaptationField adaptationField;
		try{
			adaptationField = packet.getAdaptationField();
			processAdaptationField(adaptationField,packet.getPacketNo());
		}catch(final RuntimeException re){ // might be some error in adaptation field, it is not well protected
			logger.log(Level.WARNING, "Error getting adaptationField", re);
			adaptationField = null;
		}
		return adaptationField;
	}*/

	private void handleContinuityError(final MTSPacket packet) {
		//logger.warning("continuity error, PID="+pid+", last="+last_continuity_counter+", new="+packet.getContinuityCounter()+", last_no="+last_packet_no +", packet_no="+packet.getPacketNo()+", adaptation_field_control="+packet.getAdaptationFieldControl());
		last_continuity_counter=packet.getContinuityCounter();
		last_packet_no = packet.getPacketNo();
		last_packet = packet;
		continuity_errors++;
		gatherer.reset();
	}

/*	private void updateMegaFrameInitializationPacket(final MTSPacket packet) {
		// MIP has only MTSPackets, no structure with PSISectionData
		if((packet.getPayload()!=null)&&(packet.getData().length>=14)){
			final MegaFrameInitializationPacket mip= new MegaFrameInitializationPacket(packet);
			parentTransportStream.getPsi().getNetworkSync().update(mip);
		}
	}*/


	/*private void processAdaptationField(MTSPacket.AdaptationField adaptationField, int packetNo) {
		processTEMI(adaptationField, temiList, packetNo);
		if (adaptationField.isPcrFlag()) {
			final MTSPacket.AdaptationField.PCR newPCR = adaptationField.getProgram_clock_reference();
			if(PreferencesManager.isEnablePcrPtsView()) {
				pcrList.add(new TimeStamp(packetNo, newPCR.getProgram_clock_reference_base()));
			}
			if ((firstPCR != null) && !adaptationField.isDiscontinuityIndicator()) {
				final long packetsDiff = packetNo - firstPCRpacketNo;

				// This will ignore single PCR packets that have lower values than previous.
				// when PCR wraps around we only use first part till wrap around for bitrate calculation
				// (unless PCR reaches value of firstPCR again, this would mean stream of > 24 hours)
				if ((newPCR.getProgram_clock_reference() - firstPCR.getProgram_clock_reference()) > 0) {
					bitRate = ((packetsDiff * parentTransportStream.getPacketLenghth() * system_clock_frequency * 8))
							/ (newPCR.getProgram_clock_reference() - firstPCR.getProgram_clock_reference());
				}
				lastPCR = newPCR;
				lastPCRpacketNo = packetNo;
				pcr_count++;

			} else { // start, or restart of discontinuity
				firstPCR = newPCR;
				firstPCRpacketNo = packetNo;
				lastPCR = null;
				lastPCRpacketNo = -1;
				pcr_count = 1;
			}
		}
	}*/



	/**
	 * @return number of TS packets found for this PID
	 */
	public int getPackets() {
		return packets;
	}
	@Override
	public String toString() {
		return "PID:"+pid+", packets:"+packets;
	}

	public int getPid() {
		return pid;
	}

	public boolean isDup_found() {
		return dup_found>1;
	}

	public int getDup_packets() {
		return dup_packets;
	}

	public int getLast_continuity_counter() {
		return last_continuity_counter;
	}

	public MTSPacket getLast_packet() {
		return last_packet;
	}

	public long getLast_packet_no() {
		return last_packet_no;
	}

	public TransportStream getParentTransportStream() {
		return parentTransportStream;
	}

	public void setParentTransportStream(final TransportStream parentTransportStream) {
		this.parentTransportStream = parentTransportStream;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(final String label) {
		this.label = label;
	}



	public GeneralPSITable getPsi() {
		return psi;
	}

	private String getRepetitionRate(final long count,final long last, final long  first) {
		final long bitrate=getParentTransportStream().getBitRate();
		if((bitrate>0)&&(count>=2)){
			@SuppressWarnings("resource")
			final Formatter formatter = new Formatter();
			final float repRate=((float)(last-first)*parentTransportStream.getPacketLenghth()*8)/((count-1)*bitrate);
			return "repetition rate: "+formatter.format("%3.3f seconds",repRate);
		}
		return null;
	}

	public void setPsi(final GeneralPSITable psi) {
		this.psi = psi;
	}

	public String getShortLabel(){
		return shortLabel;
	}

	public void setShortLabel(final String shortLabel) {
		this.shortLabel = shortLabel;
	}

	public long getBitRate() {
		return bitRate;
	}


	/**
	 * @return the generalPesHandler
	 */
	public GeneralPidHandler getPidHandler() {
		return generalPidHandler;
	}

	/**
	 * @param generalPidHandler the generalPesHandler to set
	 */
	public void setPidHandler(final GeneralPidHandler generalPidHandler) {
		this.generalPidHandler = generalPidHandler;
	}

	public int getType() {
		return type;
	}

	public boolean isScrambled() {
		return scrambled;
	}

	public long getContinuity_errors() {
		return continuity_errors;
	}

	public MTSPacket.AdaptationField.PCR getLastPCR() {
		return lastPCR;
	}

	public MTSPacket.AdaptationField.PCR getFirstPCR() {
		return firstPCR;
	}

	public long getLastPCRpacketNo() {
		return lastPCRpacketNo;
	}

	public long getFirstPCRpacketNo() {
		return firstPCRpacketNo;
	}

	public long getPcr_count() {
		return pcr_count;
	}

	public GatherPIDData getGatherer() {
		return gatherer;
	}

	public static Logger getLogger() {
		return logger;
	}

	public static int getPes() {
		return PES;
	}

	public ArrayList<TimeStamp> getPcrList() {
		return pcrList;
	}

	public ArrayList<TimeStamp> getPtsList() {
		return ptsList;
	}

	public ArrayList<TimeStamp> getDtsList() {
		return dtsList;
	}

	public HashMap<Integer, ArrayList<TemiTimeStamp>> getTemiList() {
		return temiList;
	}

}
