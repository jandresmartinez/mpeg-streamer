package org.taktik.mpegts.sources;

import com.google.common.base.Preconditions;
import org.taktik.mpegts.Constants;
import org.taktik.mpegts.MTSPacket;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class InputStreamMTSSource extends AbstractMTSSource {

	private InputStream inputStream;
	private boolean fixContinuity=true;
	private ContinuityFixer continuityFixer;
	private long packetCount=0;

	private InputStreamMTSSource(InputStream inputStream) throws IOException {
		this.inputStream = inputStream;
		if (fixContinuity) {
			continuityFixer = new ContinuityFixer();
		}
	}

	@Override
	protected MTSPacket nextPacketInternal() throws IOException {
		byte[] barray = new byte[Constants.MPEGTS_PACKET_SIZE];

		if (inputStream.read(barray) != Constants.MPEGTS_PACKET_SIZE) {
			inputStream.close();
			return null;
		}


		MTSPacket tsPacket= new MTSPacket(ByteBuffer.wrap(barray),packetCount);

		if (fixContinuity) {
			continuityFixer.fixContinuity(tsPacket);
		}
		packetCount++;
		// Parse the packet
		return tsPacket;
	}

	@Override
	protected void closeInternal() throws Exception {
		try (InputStream toClose = inputStream) {
		}
		inputStream = null;
	}

	public static InputStreamMTSSourceBuilder builder() {
		return new InputStreamMTSSourceBuilder();
	}

	public static class InputStreamMTSSourceBuilder {
		private InputStream inputStream;

		private InputStreamMTSSourceBuilder() {
		}

		public InputStreamMTSSourceBuilder setInputStream(InputStream inputStream) {
			this.inputStream = inputStream;
			return this;
		}

		public InputStreamMTSSource build() throws IOException {
			Preconditions.checkNotNull(inputStream, "InputStream cannot be null");
			return new InputStreamMTSSource(inputStream);
		}
	}
}
