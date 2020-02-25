package org.taktik.mpegts.sources.data.mpeg;

/**
 *

 *
 *  The author requests that he be notified of any application, applet, or
 *  other binary that makes use of this code, but that's more out of curiosity
 *  than anything and is not required.
 *
 */


import org.taktik.mpegts.MTSPacket;
import org.taktik.mpegts.sources.PesPacketData;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static org.taktik.utils.Utils.MASK_8BITS;
import static org.taktik.utils.Utils.getInt;

/**
 * @author Eric Berendsen
 *
 */
public class GeneralPesHandler extends GeneralPidHandler{

    private static final Logger logger = Logger.getLogger(GeneralPesHandler.class.getName());

    private PesPacketData pesData= null;
    private int pesStreamID =-1;
    private int pesLength =-1;

    protected final List<PesPacketData>	pesPackets	= new ArrayList<PesPacketData>();
    protected int DEFAULT_BUF_LEN = 10;
    protected byte[] pesDataBuffer = new byte[DEFAULT_BUF_LEN];
    protected int bufStart = 0;
    protected int bufEnd = 0;


    /**
     * Each specialist PesHandler has its own implementation of this method, responsible for decoding pictures, teletext or whatever.
     * This generic implementation just adds the pesData packet to the pesPackets List
     * @param pesData
     */
    protected void processPesDataBytes(final PesPacketData pesData){
        pesPackets.add(pesData);
    }


    @Override
    public void processTSPacket(final MTSPacket packet)
    {
        initialized = true;
        final byte []data = new byte[packet.getBuffer().remaining()];
        if((pesData==null)){ // nothing started
            // sometimes PayloadUnitStartIndicator is 1, and there is no payload, so check AdaptationFieldControl
            if(packet.isPayloadUnitStartIndicator() &&
                    ((packet.isAdaptationFieldExist())||(packet.isContainsPayload()))){ //start something
                // at least one byte plus pointer available
                if((data[0]!=0)||(data[1]!=0)){ //starting PSI section after ofset
                    // type = PSI;
                    throw new IllegalStateException("Found PSI data in PESHandler, PID type changed over time (not illegal, however not supported...)");

                    //	 could be starting PES stream, make sure it really is, Should start with packet_start_code_prefix -'0000 0000 0000 0000 0000 0001' (0x000001)
                }else if((data[0]==0)&&(data[1]==0)&&(data[2]==1)){
                    //type = PES;
                    pesStreamID = getInt(data, 3, 1, MASK_8BITS);
                    pesLength=getInt(data,4,2, 0xFFFF);
                    //for PES there can be only one pesPacket per TSpacket, and it always starts on first byte of payload.
                    pesData = new PesPacketData(pesStreamID,pesLength,this,packet.getPacketNo());
                    pesData.readBytes(data, 0, data.length);
                    handlePesPacketIfComplete();

                }
            }
            //	something started
        }else if((packet.isAdaptationFieldExist())||(packet.isContainsPayload())){ // has payload?
            if(packet.isPayloadUnitStartIndicator()){ // previous pesPacket Finished, tell it to process its data
                if(pesData!=null){
                    processPesDataBytes(pesData);
                }
                //start a new pesPacket
                // sometime a TSPacket has payload_unit_start_indicator set, and adaptation_field has 184 bytes, leaving nothing left for payload.
                // i think it is illegal (2.4.3.3 Semantic definition of fields in Transport Stream packet layer), but we should handle it.
                // TODO handle start of a PES packet when there are 1-5 bytes in the TS packet.
                // This is legal, but we can not handle it
                if(data.length>=6){
                    pesStreamID = getInt(data, 3, 1, MASK_8BITS);
                    pesLength=getInt(data,4,2, 0xFFFF);
                    // for PES there can be only one pesPacket per TSpacket, and it always starts on first byte of payload.
                    pesData = new PesPacketData(pesStreamID,pesLength,this,packet.getPacketNo());
                    pesData.readBytes(data, 0, data.length);
                    handlePesPacketIfComplete();
                }else{
                    logger.warning("Not supported: Found PES packet start with data.length<6 at TSPacket: "+packet.getPacketNo());
                }
            }else if (pesData!=null){
                // already in a packet,needs more data
                pesData.readBytes(data, 0, data.length);
                handlePesPacketIfComplete();

            }
        }
    }

    private void handlePesPacketIfComplete() {
        if(pesData.isComplete()){
            pesData.processPayload();
            processPesDataBytes(pesData);
            pesData = null;
        }
    }



    public List<PesPacketData> getPesPackets() {
        return pesPackets;
    }



    /**
     * @param pesDataField
     */
    protected void copyIntoBuf(final PesPacketData pesDataField) {
        final int len = pesDataField.getPesDataLen();
        // clean if needed, remove used bytes from start en append new space at end
        if ((len + bufEnd) > pesDataBuffer.length) {
            final byte[] newBuf = new byte[Math.max((len + bufEnd) - bufStart,DEFAULT_BUF_LEN)];
            System.arraycopy(pesDataBuffer, bufStart, newBuf, 0, bufEnd - bufStart);
            pesDataBuffer = newBuf;
            bufEnd = bufEnd - bufStart;
            bufStart = 0;
        }

        // now copy new data into buf
        System.arraycopy(pesDataField.getData(),
                pesDataField.getPesDataStart(),
                pesDataBuffer,
                bufEnd,
                pesDataField.getPesDataLen());
        bufEnd+= pesDataField.getPesDataLen();
    }


}
