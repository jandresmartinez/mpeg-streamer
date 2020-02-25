package org.taktik.mpegts.sources;

public class TimeStamp {

    private final int packetNo;
    private final long time;

    public TimeStamp(int packetNo, long time) {
        super();
        this.packetNo = packetNo;
        this.time = time;
    }

    public int getPacketNo() {
        return packetNo;
    }

    public long getTime() {
        return time;
    }


}

