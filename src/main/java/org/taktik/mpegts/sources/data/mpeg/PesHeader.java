package org.taktik.mpegts.sources.data.mpeg;

/**
 *

 *  The author requests that he be notified of any application, applet, or
 *  other binary that makes use of this code, but that's more out of curiosity
 *  than anything and is not required.
 *
 */


import org.taktik.mpegts.sources.PesPacketData;

import static org.taktik.utils.Utils.*;

/**
 * @author Eric
 *
 */
public class PesHeader {

    private final byte[] data;
    private final int offset;

    /**
     * @param data
     * @param offset
     */
    public PesHeader(final byte[] data, final int offset) {
        super();
        this.data = data;
        this.offset = offset;
    }

    public boolean isValidPesHeader(){
        return (data.length>=offset+2)&&
                (data[offset]==0) &&
                (data[offset+1]==0) &&
                (data[offset+2]==1);
    }

    public int getStreamID(){
        return getInt(data, offset+3, 1, MASK_8BITS);
    }

    public int getPesPacketLength(){
        return getInt(data, offset+4, 2, MASK_16BITS);
    }



    /**
     * @param stream_id
     * @return
     */
    public static boolean hasExtendedHeader(final int stream_id) {
        return (stream_id != PesPacketData.program_stream_map)
                && (stream_id != PesPacketData.padding_stream)
                && (stream_id != PesPacketData.private_stream_2)
                && (stream_id != PesPacketData.ECM_stream)
                && (stream_id != PesPacketData.EMM_stream)
                && (stream_id != PesPacketData.program_stream_directory)
                && (stream_id != PesPacketData.DSMCC_stream)
                && (stream_id != PesPacketData.ITU_T_Rec_H_222_1typeE);
    }


    public boolean hasExtendedHeader(){
        return hasExtendedHeader(getStreamID());
    }
    /**
     * @return
     */
    public long getDts() {
        return getTimeStamp(data,offset+14);
    }

    /**
     * @return
     */
    public long getPts() {
        return getTimeStamp(data,offset+9);
    }

    /**
     * @return length of Pes header
     */
    public final int getPes_header_data_length() {
        return getInt(data, offset+8, 1, MASK_8BITS);
    }
    /**
     * @return
     */
    public int getPes_extension_flag() {
        return getInt(data, offset+7, 1, MASK_1BIT);
    }
    /**
     * @return
     */
    public int getPes_crc_flag() {
        return getInt(data, offset+7, 1, 0x02)>>1;
    }
    /**
     * @return
     */
    public int getAdditional_copy_info_flag() {
        return getInt(data, offset+7, 1, 0x04)>>2;
    }
    /**
     * @return
     */
    public int getDsm_trick_mode_flag() {
        return getInt(data, offset+7, 1, 0x08)>>3;
    }
    /**
     * @return
     */
    public int getEs_rate_flag() {
        return getInt(data, offset+7, 1, 0x10)>>4;
    }
    /**
     * @return
     */
    public int getEscr_flag() {
        return getInt(data, offset+7, 1, 0x20)>>5;
    }
    /**
     * @return
     */
    public final int getPts_dts_flags() {
        return getInt(data, offset+7, 1, 0xC0)>>6;
    }
    /**
     * @return
     */
    public int getOriginal_or_copy() {
        return getInt(data, offset+6, 1, MASK_1BIT);
    }
    /**
     * @return
     */
    public int getCopyright() {
        return getInt(data, offset+6, 1, 0x02) >>1;
    }
    /**
     * @return
     */
    public int getData_alignment_indicator() {
        return getInt(data, offset+6, 1, 0x04) >>2;
    }
    /**
     * @return
     */
    public int getPes_priority() {
        return getInt(data, offset+6, 1, 0x08) >>3;
    }
    /**
     * @return
     */
    public int getPes_scrambling_control() {
        return getInt(data, offset+6, 1, 0x30) >>4;
    }
    /**
     * @return
     */
    public int getMarkerBits() {
        return getInt(data, offset+6, 1, 0xC0) >>6;
    }

    /**
     * @param array
     * @param offset
     * @return the value of the PTS/DTS as described in 2.4.3.7 of iso 13813, prefix and marker bits are ignored
     */
    public final static long getTimeStamp(final byte[] array, final int offset) {

        long ts = getLong(array, offset, 1, 0x0E) << 29; // bits 32..30
        ts |= getLong(array, offset + 1, 2, 0xFFFE) << 14; // bits 29..15
        ts |= getLong(array, offset + 3, 2, 0xFFFE) >> 1; // bits 14..0

        return ts;
    }





    public static String getPts_dts_flagsString(final int pts_dts_flags) {
        switch (pts_dts_flags) {
            case 0:
                return "no PTS or DTS fields shall be present in the PES packet header";
            case 1:
                return "forbidden value";
            case 2:
                return "PTS fields shall be present in the PES packet header";
            case 3:
                return "both the PTS fields and DTS fields shall be present in the PES packet header";

            default:
                return "illegal value (program error)";
        }
    }

    public static String getStreamIDDescription(final int streamId){

        if((0xC0<=streamId)&&(streamId<0xE0)){
            return "ISO/IEC 13818-3 or ISO/IEC 11172-3 or ISO/IEC 13818-7 or ISO/IEC 14496-3 audio stream number "+ Integer.toHexString(streamId & 0x1F);
        }
        if((0xE0<=streamId)&&(streamId<0xF0)){
            return "ITU-T Rec. H.262 | ISO/IEC 13818-2 or ISO/IEC 11172-2 or ISO/IEC 14496-2 video stream number "+ Integer.toHexString(streamId & 0x0F);
        }

        switch (streamId) {
            case 0xBC :return "program_stream_map";
            case 0xBD :return "private_stream_1";
            case 0xBE :return "padding_stream";
            case 0xBF :return "private_stream_2";
            case 0xF0 :return "ECM_stream";
            case 0xF1 :return "EMM_stream";
            case 0xF2 :return "DSMCC_stream";
            case 0xF3 :return "ISO/IEC_13522_stream";
            case 0xF4 :return "ITU-T Rec. H.222.1 type A";
            case 0xF5 :return "ITU-T Rec. H.222.1 type B";
            case 0xF6 :return "ITU-T Rec. H.222.1 type C";
            case 0xF7 :return "ITU-T Rec. H.222.1 type D";
            case 0xF8 :return "ITU-T Rec. H.222.1 type E";
            case 0xF9 :return "ancillary_stream";
            case 0xFA :return "ISO/IEC14496-1_SL-packetized_stream";
            case 0xFB :return "ISO/IEC14496-1_FlexMux_stream";
            /* ISO/IEC 13818-1:2007/FPDAM5 */
            case 0xFC :return "metadata stream";
            case 0xFD :return "extended_stream_id";
            case 0xFE :return "reserved data stream";

            case 0xFF :return "program_stream_directory";
            default:
                return "??";
        }


    }


}
