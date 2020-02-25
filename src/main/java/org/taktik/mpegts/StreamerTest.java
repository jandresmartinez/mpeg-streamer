package org.taktik.mpegts;

import org.taktik.mpegts.sinks.MTSSink;
import org.taktik.mpegts.sinks.UDPTransport;
import org.taktik.mpegts.sources.MTSSource;
import org.taktik.mpegts.sources.MTSSources;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class StreamerTest {
	public static void main(String[] args) throws Exception {

		// Set up mts sink
		MTSSink transport = UDPTransport.builder()
				//.setAddress("239.222.1.1")
				.setAddress(args[0])
				.setPort(Integer.valueOf(args[1]))
				.setSoTimeout(5000)
				.setTtl(2)
				.build();

		// Set up mts sink
		/*MTSSink transport = UDPTransport.builder()
				//.setAddress("239.222.1.1")
				.setAddress("127.0.0.1")
				.setPort(5500)
				.setSoTimeout(5000)
				.setTtl(2)
				.build();


		ResettableMTSSource ts1 = MTSSources.from(new File("C:\\Users\\Jorge\\Videos\\OCS_sub.ts"));*/
		//ResettableMTSSource ts2 = MTSSources.from(new File("/home/media-files/agent.ts"));


        //MTSSource sourcePiped = MTSSources.from(input);


       // System.out.println("Streaming file =>" + args[2]);

		//ResettableMTSSource source = MTSSources.from(new File("C:\\Nahual\\files\\NOC.ts"));

		InputStream is=new FileInputStream(new File(args[2]));
		MTSSource source = MTSSources.from(is);




        // Infinite looping, continuity fix
       /*MTSSource playLoop = MultiMTSSource.builder()
                .setSources(ts2,ts2)
                .loop()
                .setFixContinuity(true)
                .build();*/


		// Build source
		//MTSSource source = MTSSources.loop(ts1);


		// build streamer
        Streamer streamer = Streamer.builder()
                .setSource(source)
                //.setSink(ByteChannelSink.builder().setByteChannel(fc).build())
                .setSink(transport)
                .build();



		// Start streaming
		streamer.stream();



	}
}
