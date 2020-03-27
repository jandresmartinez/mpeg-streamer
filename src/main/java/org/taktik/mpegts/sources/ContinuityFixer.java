package org.taktik.mpegts.sources;

import com.google.common.collect.Maps;
import org.taktik.mpegts.Constants;
import org.taktik.mpegts.MTSPacket;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.Map;


/**
 * This class will attempt to fix timestamp discontinuities
 * when switching from one source to another.
 * This should allow for smoother transitions between videos.<br>
 * This class does 3 things:
 * <ol>
 * <li> Rewrite the PCR to be continuous with the previous source</li>
 * <li> Rewrite the PTS of the PES to be continuous with the previous source</li>
 * <li> Rewrite the continuity counter to be continuous with the previous source</li>
 * </ol>
 *
 * Code using this class should call {@link #fixContinuity(org.taktik.mpegts.MTSPacket)} for each source packet,
 * then {@link #nextSource()} after the last packet of the current source and before the first packet of the next source.
 */
public class ContinuityFixer {
	private Map<Integer, MTSPacket> pcrPackets;
	private Map<Integer, MTSPacket> allPackets;
	private Map<Integer, Long> ptss;
    private Map<Integer, Long> videoPts;
	private Map<Integer, Long> dtss;
	private Map<Integer, Long> lastPTSsOfPreviousSource;
	private Map<Integer, Long> lastDTSsOfPreviousSource;
	private Map<Integer, Long> lastPCRsOfPreviousSource;
    private Map<Integer, Long> lastPCRs;
	private Map<Integer, Long> firstPCRsOfCurrentSource;
	private Map<Integer, Long> firstPTSsOfCurrentSource;
	private Map<Integer, Long> firstDTSsOfCurrentSource;
	private int pcrPID=-1;

	private Map<Integer, MTSPacket> lastPacketsOfPreviousSource = Maps.newHashMap();
	private Map<Integer, MTSPacket> firstPacketsOfCurrentSource = Maps.newHashMap();
	private Map<Integer, Integer> continuityFixes = Maps.newHashMap();
	private long tsRate;
	private int i;

	private boolean firstSource;


	public ContinuityFixer() {
		pcrPackets = Maps.newHashMap();
		allPackets = Maps.newHashMap();
		ptss = Maps.newHashMap();
        videoPts = Maps.newHashMap();
		dtss = Maps.newHashMap();
		lastPTSsOfPreviousSource = Maps.newHashMap();
		lastDTSsOfPreviousSource = Maps.newHashMap();
		lastPCRsOfPreviousSource = Maps.newHashMap();
        lastPCRs = Maps.newHashMap();
		firstPCRsOfCurrentSource = Maps.newHashMap();
		firstPTSsOfCurrentSource = Maps.newHashMap();
		firstDTSsOfCurrentSource = Maps.newHashMap();

		lastPacketsOfPreviousSource = Maps.newHashMap();
		firstPacketsOfCurrentSource = Maps.newHashMap();
		continuityFixes = Maps.newHashMap();
		firstSource = true;
		tsRate=0L;
	}

	/**
	 * Signals the {@link org.taktik.mpegts.sources.ContinuityFixer} that the following
	 * packet will be from another source.
	 *
	 * Call this method after the last packet of the current source and before the first packet of the next source.
	 */
	public void nextSource() {
		firstPCRsOfCurrentSource.clear();
		lastPCRsOfPreviousSource.clear();
        lastPCRs.clear();
		firstPTSsOfCurrentSource.clear();
		lastPTSsOfPreviousSource.clear();
		firstDTSsOfCurrentSource.clear();
		lastDTSsOfPreviousSource.clear();
		firstPacketsOfCurrentSource.clear();
		lastPacketsOfPreviousSource.clear();
		Iterator it = pcrPackets.entrySet().iterator();
		while(it.hasNext()){
			Map.Entry pair = (Map.Entry)it.next();
			long pcrVal=((MTSPacket)pair.getValue()).getAdaptationField().getPcr().getValue();
			lastPCRsOfPreviousSource.put((int)pair.getKey(), pcrVal );
		}
		lastPTSsOfPreviousSource.putAll(ptss);
		lastDTSsOfPreviousSource.putAll(dtss);
		lastPacketsOfPreviousSource.putAll(allPackets);
		//pcrPackets.clear();
		//ptss.clear();
		dtss.clear();
		allPackets.clear();
		firstSource = false;
		int i=0;
	}

	/**
	 * Fix the continuity of the packet.
	 *
	 * Call this method for each source packet, in order.
	 *
	 * @param tsPacket The packet to fix.
	 */
	public void fixContinuity(MTSPacket tsPacket) {
		int pid = tsPacket.getPid();

		/*if (!firstPacketsOfCurrentSource.containsKey(pid)) {
			firstPacketsOfCurrentSource.put(pid, tsPacket);
			if (!firstSource) {
				MTSPacket lastPacketOfPreviousSource = lastPacketsOfPreviousSource.get(pid);
				int continuityFix = lastPacketOfPreviousSource == null ? 0 : lastPacketOfPreviousSource.getContinuityCounter() - tsPacket.getContinuityCounter();
				if (tsPacket.isContainsPayload()) {
					continuityFix++;
				}
				continuityFixes.put(pid, continuityFix);
			}
		}
		if (!firstSource&&continuityFixes.containsKey(pid)) {
			tsPacket.setContinuityCounter((tsPacket.getContinuityCounter() + continuityFixes.get(pid)) % 16);
		}

		if (pid!=8191){
            int continuityCounter=tsPacket.getContinuityCounter();

        if (continuityFixes.containsKey(pid)){
        if (tsPacket.isContainsPayload()&&continuityFixes.get(pid)+1!=continuityCounter&&continuityFixes.get(pid)+1<16){
            continuityCounter=continuityFixes.get(pid)+1==16?0:continuityFixes.get(pid)+1;
            System.out.println("Changing cont val=>"+continuityCounter+ " for PID:"+pid+" . Original val=>"+tsPacket.getContinuityCounter());
            tsPacket.setContinuityCounter(continuityCounter);
        }

        }
        if (tsPacket.isContainsPayload())
         continuityFixes.put(pid,continuityCounter);

           */
        /*if (continuityFixes.containsKey(pid)){
            int contExpectedVal=continuityFixes.get(pid)+1;
            if (tsPacket.isContainsPayload()&&contExpectedVal!=continuityCounter){
                continuityCounter=contExpectedVal>15?0:contExpectedVal;
                tsPacket.setContinuityCounter(continuityCounter);
            }

        }

}*/

        if (pid!=8191){
            if (continuityFixes.containsKey(pid)){
                int contExpectedVal=continuityFixes.get(pid);
                if (tsPacket.isContainsPayload()){
                    contExpectedVal=continuityFixes.get(pid)+1;
                    contExpectedVal=contExpectedVal>15?0:contExpectedVal;
                    continuityFixes.put(pid,contExpectedVal);
                }

                tsPacket.setContinuityCounter(contExpectedVal);

                if (tsPacket.getContinuityCounter()!=contExpectedVal)
                    System.out.println("PID=>"+pid+"Cont exp val:"+contExpectedVal+" while original val is:"+tsPacket.getContinuityCounter());
            }else
                continuityFixes.put(pid,tsPacket.getContinuityCounter());

        }
		fixPCR(tsPacket, pid);
        fixPTSAndDTS(tsPacket, pid);

	}

	private void fixPCR(MTSPacket tsPacket, int pid) {

		if (tsPacket.isAdaptationFieldExist() && tsPacket.getAdaptationField() != null) {
			if (tsPacket.getAdaptationField().isPcrFlag()) {
				if (!firstPCRsOfCurrentSource.containsKey(pid)) {
					firstPCRsOfCurrentSource.put(pid, tsPacket.getAdaptationField().getPcr().getValue());
				}
                lastPCRs.put(pid, tsPacket.getAdaptationField().getPcr().getValue());
				if (!firstSource)
					rewritePCR(tsPacket);
				i++;
				if (i==200||i>10000){
					i=0;
					if (tsRate==0)
					 estimateBitRate(tsPacket);
					firstSource=false;
				}
				if (pcrPID==-1)
                        pcrPID=pid;
				pcrPackets.put(pid, tsPacket);




			}

		}

	}

	private void fixPTSAndDTS(MTSPacket tsPacket, int pid) {


		if (tsPacket.isContainsPayload()&&tsPacket.getPesHeader()!=null&&
				(tsPacket.getPesHeader().getPts_dts_flags()==3||tsPacket.getPesHeader().getPts_dts_flags()==2)) {
				ByteBuffer payload = tsPacket.getPayload();
					// PTS is present
					// TODO add payload size check to avoid indexoutofboundexception
					//long pts = (((payload.get(9) & 0xE)) << 29) | (((payload.getShort(10) & 0xFFFE)) << 14) | ((payload.getShort(12) & 0xFFFE) >> 1);

					long pts=tsPacket.getPesHeader().getPts();
					long oldPts=pts;
					if (!firstPTSsOfCurrentSource.containsKey(pid)) {
						firstPTSsOfCurrentSource.put(pid, pts);
					}
					long newPts	=0L;
					if (!firstSource) {
                        if (tsPacket.getAdaptationField()!=null&&tsPacket.getAdaptationField().getPcr()!=null){
                            newPts=estimatePTSFromPCR(pts,lastPCRs.get(pid),tsPacket.getAdaptationField().getPcr().getValue());
                            videoPts.put(pid,oldPts);
                        }
                        else {
                            if (videoPts.containsKey(pcrPID))
                                newPts=estimatePTSFromVideoPTS(videoPts.get(pcrPID),pts,ptss.get(pcrPID),pid);
                            if (pid==pcrPID)
                                videoPts.put(pid,oldPts);
                        }

						    //newPts = Math.round(pts + (getPTSTimeGap(pid) / 300.0) + 100 * ((27_000_000 / 300.0) / 1_000));

						payload.put(9, (byte) (0x20 | ((newPts & 0x1C0000000l) >> 29) | 0x1));
						payload.putShort(10, (short) (0x1 | ((newPts & 0x3FFF8000) >> 14)));
						payload.putShort(12, (short) (0x1 | ((newPts & 0x7FFF) << 1)));
						payload.rewind();
						pts = newPts;
					}

					ptss.put(pid, pts);

				if (tsPacket.getPesHeader().getPts_dts_flags()==3){
					long dts=tsPacket.getPesHeader().getDts();
					if (!firstDTSsOfCurrentSource.containsKey(pid)) {
						firstDTSsOfCurrentSource.put(pid, dts);
					}
					if (!firstSource) {
						long diff = oldPts - dts;
						long newDts=newPts-diff;

						payload.put(14, (byte) (0x20 | ((newDts & 0x1C0000000l) >> 29) | 0x1));
						payload.putShort(15, (short) (0x1 | ((newDts & 0x3FFF8000) >> 14)));
						payload.putShort(17, (short) (0x1 | ((newDts & 0x7FFF) << 1)));
						payload.rewind();
						dts = newDts;
					}

					dtss.put(pid, dts);
				}

		}


	}


	private long estimatePTSFromPCR(long oldPTS,long oldPCR,long newPCR){
	    long diff=oldPTS-(oldPCR/300);
	    return (newPCR/300)+diff;
    }

    private long estimatePTSFromVideoPTS(long oldVideoPTS,long pts,long newVideoPTS,int pid){
        long diff=pts-oldVideoPTS;
        return newVideoPTS+diff;
    }

	private void fixDTS(MTSPacket tsPacket, int pid) {
		if (tsPacket.isContainsPayload()&&tsPacket.getAdaptationField()!=null&&tsPacket.getPesHeader()!=null&&
				tsPacket.getPesHeader().getPts_dts_flags()==3) {

				 ByteBuffer payload = tsPacket.getPayload();



		}
	}

	private long getPCRTimeGap(int pid) {
		// Try with PCR of the same PID
		Long lastPCROfPreviousSource = lastPCRsOfPreviousSource.get(pid);
		if (lastPCROfPreviousSource == null) {
			lastPCROfPreviousSource = 0l;
		}
		Long firstPCROfCurrentSource = firstPCRsOfCurrentSource.get(pid);
		if (firstPCROfCurrentSource != null) {
			return lastPCROfPreviousSource - firstPCROfCurrentSource;
		}

		// Try with any PCR
		if (!lastPCRsOfPreviousSource.isEmpty()) {
			int pcrPid = lastPCRsOfPreviousSource.keySet().iterator().next();
			lastPCROfPreviousSource = lastPCRsOfPreviousSource.get(pcrPid);
			if (lastPCROfPreviousSource == null) {
				lastPCROfPreviousSource = 0l;
			}
			firstPCROfCurrentSource = firstPCRsOfCurrentSource.get(pcrPid);
			if (firstPCROfCurrentSource != null) {
				return lastPCROfPreviousSource - firstPCROfCurrentSource;
			}
		}

		return 0;


	}

	private long estimateNextPCRTimeGapDiff(long byteDiff,long oldPCR) {
		//Estimate PCR from bytes sent and previous PCR value
		return oldPCR+(byteDiff*188*8* Constants.PCR_CLOCK)/tsRate;



	}

	private void estimateBitRate(MTSPacket tsPacket){
		MTSPacket lastPcrPacket=pcrPackets.get(tsPacket.getPid());
		long pcrVal=tsPacket.getAdaptationField().getPcr().getValue();
		long oldPCRVal=lastPcrPacket.getAdaptationField().getPcr().getValue();
		try{
			tsRate=((tsPacket.getPacketCount()-lastPcrPacket.getPacketCount())*188*8*27000000)/(pcrVal-oldPCRVal);
		}catch (Exception ex){
			System.out.println(ex.getMessage());
		}
	}

	private long getPCRValue(MTSPacket tsPacket){

		MTSPacket lastPcrPacket=pcrPackets.get(tsPacket.getPid());
		long byteDiff=tsPacket.getPacketCount()-lastPcrPacket.getPacketCount();
		//System.out.println("Packet Number "+tsPacket.getPacketCount()+". Byte diff is "+byteDiff+" and last packet PCR val=>"+lastPcrPacket.getAdaptationField().getPcr().getValue());
		if (tsPacket.getPacketCount()<lastPcrPacket.getPacketCount())
			byteDiff=tsPacket.getPacketCount()-0;
		return (estimateNextPCRTimeGapDiff(byteDiff,lastPcrPacket.getAdaptationField().getPcr().getValue()));

	}

	private long getPTSTimeGap(int pid) {
		// Try with PTS of the same PID
		Long lastPTSOfPreviousSource = lastPTSsOfPreviousSource.get(pid);
		if (lastPTSOfPreviousSource == null) {
			lastPTSOfPreviousSource = 0l;
		}

		Long firstPTSofCurrentSource = firstPTSsOfCurrentSource.get(pid);
		if (firstPTSofCurrentSource != null) {
			return (lastPTSOfPreviousSource - firstPTSofCurrentSource) * 300;
		}

		// Try with any PTS
		if (!lastPTSsOfPreviousSource.isEmpty()) {
			int randomPid = lastPTSsOfPreviousSource.keySet().iterator().next();
			lastPTSOfPreviousSource = lastPTSsOfPreviousSource.get(randomPid);
			if (lastPTSOfPreviousSource == null) {
				lastPTSOfPreviousSource = 0l;
			}

			firstPTSofCurrentSource = firstPTSsOfCurrentSource.get(randomPid);
			if (firstPTSofCurrentSource != null) {
				return (lastPTSOfPreviousSource - firstPTSofCurrentSource) * 300;
			}
		}

		return 0;
	}

	private long getDTSTimeGap(int pid) {
		// Try with PTS of the same PID
		Long lastDTSOfPreviousSource = lastDTSsOfPreviousSource.get(pid);
		if (lastDTSOfPreviousSource == null) {
			lastDTSOfPreviousSource = 0l;
		}

		Long firstDTSofCurrentSource = firstDTSsOfCurrentSource.get(pid);
		if (firstDTSofCurrentSource != null) {
			return (lastDTSOfPreviousSource - firstDTSofCurrentSource) * 300;
		}

		// Try with any PTS
		if (!lastDTSsOfPreviousSource.isEmpty()) {
			int randomPid = lastDTSsOfPreviousSource.keySet().iterator().next();
			lastDTSOfPreviousSource = lastDTSsOfPreviousSource.get(randomPid);
			if (lastDTSOfPreviousSource == null) {
				lastDTSOfPreviousSource = 0l;
			}

			firstDTSofCurrentSource = firstDTSsOfCurrentSource.get(randomPid);
			if (firstDTSofCurrentSource != null) {
				return (lastDTSOfPreviousSource - firstDTSofCurrentSource) * 300;
			}
		}

		return 0;
	}

	private void rewritePCR(MTSPacket tsPacket) {
/*
		long timeGap = getPCRTimeGap(tsPacket.getPid());
		long pcr = tsPacket.getAdaptationField().getPcr().getValue();
		long newPcr = pcr + timeGap + 100 * ((27_000_000) / 1_000);
		tsPacket.getAdaptationField().getPcr().setValue(newPcr);

		long lastPCRVal=pcrPackets.isEmpty()?lastPCRsOfPreviousSource.get(tsPacket.getPid()):pcrPackets.get(tsPacket.getPid());
		long diffPCR=newPcr-lastPCRVal;
		if (diffPCR> Constants.PCR_ACU&&pcrPackets.isEmpty()) {
			System.out.println("PCR_ACU=>" + diffPCR);
			//tsPacket.getAdaptationField().getPcr().setValue(lastPCRVal+Constants.PCR_ACU);
		}*/
			tsPacket.getAdaptationField().getPcr().setValue(getPCRValue(tsPacket));
	}
}
