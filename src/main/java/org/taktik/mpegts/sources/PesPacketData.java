package org.taktik.mpegts.sources;
/**
 *

 *
 *  The author requests that he be notified of any application, applet, or
 *  other binary that makes use of this code, but that's more out of curiosity
 *  than anything and is not required.
 *
 */

import org.taktik.mpegts.sources.data.mpeg.GeneralPesHandler;
import org.taktik.mpegts.sources.data.mpeg.PesHeader;

import static org.taktik.utils.Utils.MASK_8BITS;
import static org.taktik.utils.Utils.getInt;

/**
 * Represent a PES packet, which can correspond to a MPEG frame, (or a complete group of frames) or Audio packet, or DVB
 * subtitle, or teletext. Subclasses are present for DVBsubtitles, MPEG2 video, MPEG2 audio, teletext.
 *
 * based on ISO/IEC 13818-1 2.4.3.6 and 2.4.3.7
 *
 * <img src="doc-files/pespacket.png">
 *
 * @author Eric Berendsen
 *
 */
public class PesPacketData  {

    /**
     * Data including packet_start_code_prefix, stream id, PES packet length, pesHeader
     *
     */
    protected byte [] data ;
    protected int stream_id = 0;
    /**
     * the number of bytes to be expected in this pes as defined by PES packet length. 0 for unbounded
     */
    protected int noBytes=0;
    private PesHeader pesHeader;

    protected int pes_header_data_length;

    protected GeneralPesHandler pesHandler;

    protected int pesDataStart;
    protected int pesDataLen;

    protected int bytesRead = 0;
    protected int startPacketNo;

    public static final int program_stream_map = 0xBC;
    public static final int private_stream_1 = 0xBD;
    public static final int padding_stream = 0xBE;
    public static final int private_stream_2 = 0xBF;
    public static final int ECM_stream = 0xF0;
    public static final int EMM_stream = 0xF1;
    public static final int DSMCC_stream = 0xF2;
    public static final int ISO_IEC_13522_stream = 0xF3;
    public static final int ITU_T_Rec_H_222_1typeA = 0xF4;
    public static final int ITU_T_Rec_H_222_1typeB = 0xF5;
    public static final int ITU_T_Rec_H_222_1typeC = 0xF6;
    public static final int ITU_T_Rec_H_222_1typeD = 0xF7;
    public static final int ITU_T_Rec_H_222_1typeE = 0xF8;
    public static final int ancillary_stream = 0xF9;
    public static final int ISO_IEC14496_1_SL_packetized_stream = 0xFA;
    public static final int ISO_IEC14496_1_FlexMux_stream = 0xFB;
    public static final int program_stream_directory = 0xFF;

    /**
     * Constructor used to start creating a new PesPacket. pesStreamID, pesLength and pesHandler have to be set, but the
     * data is to be added later by calling readBytes()
     *
     * @param pesStreamID
     * @param pesLength
     * @param pesHandler
     * @param packetNo
     */
    public PesPacketData(final int pesStreamID, final int pesLength, final GeneralPesHandler pesHandler, int packetNo) {
        this.stream_id = pesStreamID;
        this.noBytes = pesLength;
        this.pesHandler = pesHandler;
        this.startPacketNo = packetNo;
        if(pesLength==0){
            this.data= new byte[20000]; // start default for video, should be able to handle small frames.
        }else{
            this.data= new byte[pesLength+6];
        }

    }

    /**
     * This constructor is only used to 'wrap' an existing PesPacketData into a specialized form, like
     * VideoPESDataField.
     *
     * @param pesPacket
     */
    protected PesPacketData(final PesPacketData pesPacket) {
        this.stream_id = pesPacket.getPesStreamID();

        this.data = pesPacket.getData();
        this.noBytes = pesPacket.getNoBytes();
        this.pesHandler = pesPacket.getPesHandler();
        this.pesDataStart = pesPacket.getPesDataStart();
        this.pesDataLen = pesPacket.getPesDataLen();
        this.bytesRead = pesPacket.bytesRead;
        this.startPacketNo = pesPacket.startPacketNo;
        processPayload();

    }

    synchronized public PesHeader getPesHeader(){
        if(pesHeader==null){
            pesHeader = new PesHeader(data,0);
        }
        return pesHeader;
    }

    /**
     * Method to (partial) fill the data[] of this PesPacket. Data from single TSPacket is appended to the already
     * collected data
     *
     * @param payload data to be copied into PesPacketData
     * @param offset where in payload[] does the actual data start
     * @param available number of bytes available to be read
     */
    public void readBytes(final byte [] payload, final int offset, final int available){
        if(noBytes!=0){  //fixed length PES packet, we know how much to expect
            if(bytesRead<(noBytes+6)){
                final int bytesToRead = Math.min((noBytes - bytesRead) + 6, available); // we are going to read this number of bytes
                System.arraycopy(payload, offset, data, bytesRead, bytesToRead);
                bytesRead+=bytesToRead;
            }
        }else{ // noBytes==0, unbounded video packet, length unknown
            final int newcount = bytesRead + available;
            if (newcount > data.length) {
                final byte newbuf[] = new byte[Math.max(data.length * 2, newcount)];
                System.arraycopy(data, 0, newbuf, 0, bytesRead);
                data = newbuf;
            }
            System.arraycopy(payload, offset, data, bytesRead, available);
            bytesRead = newcount;
        }
    }


    /**
     * @return All Data of packet, including packet_start_code_prefix, stream id, PES packet length, pesHeader
     */
    public byte[] getData() {
        return data;
    }


    /**
     * @return the number of bytes to be expected in this pes as defined by PES packet length. 0 for unbounded
     */
    public int getNoBytes() {
        return noBytes;
    }


    /**
     * @return the stream_id
     */
    public int getPesStreamID() {
        return stream_id;
    }


    /**
     * Called when all data for this PesPacket has been read by the readBytes() method. Determines values for PTS and
     * DTS (if any), and sets pesDataStart and pesDataLen. iso 13818-1 2.4.3.6 PES packet
     */
    public final void processPayload() {
        if((stream_id!=program_stream_map)
                && (stream_id != padding_stream)
                && (stream_id != private_stream_2)
                && (stream_id != ECM_stream)
                && (stream_id != EMM_stream)
                && (stream_id != program_stream_directory)
                && (stream_id != DSMCC_stream)
                && (stream_id != ITU_T_Rec_H_222_1typeE))
        {

            pes_header_data_length  = getPes_header_data_length();

            pesDataStart=9+pes_header_data_length;
            if(noBytes==0){
                pesDataLen=bytesRead-pes_header_data_length-3;
            }else{
                pesDataLen=noBytes-pes_header_data_length-3;
            }
        }else{
            pesDataStart=6;
            pesDataLen=noBytes;
        }
    }

    /**
     * @return length of Pes header
     */
    public final int getPes_header_data_length() {
        return getInt(data, 8, 1, MASK_8BITS);
    }




    /**
     * @return the pesDataLen,the actual len of the payload (without prefix, stream_id, and header)
     */
    public int getPesDataLen() {
        return pesDataLen;
    }


    /**
     * @return the pesDataStart, the offset into data[], where the PES packet data bytes start (start of actual payload)
     */
    public int getPesDataStart() {
        return pesDataStart;
    }

    /**
     * @return the PesHandler that knows how to process the raw data in this type of PesPacket
     */
    public GeneralPesHandler getPesHandler() {
        return pesHandler;
    }

    public int getStartPacketNo() {
        return startPacketNo;
    }

    public boolean isComplete(){
        if(noBytes==0){
            return false;
        }else {
            return ((noBytes+6)==bytesRead);
        }
    }


}
