package org.taktik.mpegts.sources;

import com.google.common.base.Preconditions;
import org.taktik.mpegts.Constants;
import org.taktik.mpegts.MTSPacket;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import java.io.PipedInputStream;

public class PipedInputStreamMTSSource extends AbstractMTSSource {

	private PipedInputStream inputStream;

	private PipedInputStreamMTSSource(PipedInputStream inputStream) throws IOException {
		this.inputStream = inputStream;
	}

	@Override
	protected MTSPacket nextPacketInternal() throws IOException {
		byte[] barray = new byte[Constants.MPEGTS_PACKET_SIZE];
		if (inputStream.read(barray) != Constants.MPEGTS_PACKET_SIZE) {
			//inputStream.close();
			return null;
		}

		// Parse the packet
		return new MTSPacket(ByteBuffer.wrap(barray));
	}

	@Override
	protected void closeInternal() throws Exception {
		try (InputStream toClose = inputStream) {
		}
		inputStream = null;
	}

	public static PipedInputStreamMTSSourceBuilder builder() {

		return new PipedInputStreamMTSSourceBuilder();
	}

	public static class PipedInputStreamMTSSourceBuilder {
		private PipedInputStream inputStream;

		private PipedInputStreamMTSSourceBuilder() {
		}

		public PipedInputStreamMTSSourceBuilder setInputStream(PipedInputStream inputStream) {
			this.inputStream = inputStream;
			return this;
		}

		public PipedInputStreamMTSSource build() throws IOException {
			Preconditions.checkNotNull(inputStream, "InputStream cannot be null");
			return new PipedInputStreamMTSSource(inputStream);
		}
	}
}
