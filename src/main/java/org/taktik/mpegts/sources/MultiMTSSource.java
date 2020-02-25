package org.taktik.mpegts.sources;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.taktik.mpegts.MTSPacket;

import java.util.Collection;
import java.util.List;

public class MultiMTSSource extends AbstractMTSSource {
	static final Logger log = LoggerFactory.getLogger("multisource");
	private List<MTSSource> sources;
	private MTSSource currentSource;
	private int idx;
	private boolean fixContinuity;


	private ContinuityFixer continuityFixer;
	private int maxLoops;
	private int currentLoop;
	private boolean closeCurrentSource;

	protected MultiMTSSource(boolean fixContinuity, int maxloops, Collection<MTSSource> sources) {
		Preconditions.checkArgument(sources.size() > 0, "Multisource must at least contain one source");
		Preconditions.checkArgument(maxloops != 0, "Cannot loop zero times");
		this.sources = Lists.newArrayList(sources);
		this.fixContinuity = fixContinuity;
		idx = 0;
		currentSource = this.sources.get(0);
		if (fixContinuity) {
			continuityFixer = new ContinuityFixer();
		}
		this.maxLoops = maxloops;
		if (maxloops != 1) {
			checkLoopingPossible(sources);
		}
		this.currentLoop = 1;
		this.closeCurrentSource = false;
	}

	private void checkLoopingPossible(Collection<MTSSource> sources) {
		for (MTSSource source : sources) {
			checkLoopingPossible(source);
		}
	}

	private void checkLoopingPossible(MTSSource source) {
		if (!(source instanceof ResettableMTSSource)) {
			throw new IllegalStateException("Sources must be resettable for looping");
		}
	}

	@Override
	protected MTSPacket nextPacketInternal() throws Exception {
		if (currentSource == null) {
			return null;
		}
		currentSource.setPacketCount(packetCount);
		MTSPacket tsPacket = currentSource.nextPacket();
		if (tsPacket != null) {
			if (fixContinuity) {
				continuityFixer.fixContinuity(tsPacket);
			}
			packetCount=tsPacket.getPacketCount();
			return tsPacket;
		} else {
			nextSource();
			return nextPacket();
		}
	}

	public synchronized void updateSources(List<MTSSource> newSources) {
		checkLoopingPossible(newSources);
		List<MTSSource> oldSources = this.sources;
		this.sources = newSources;
		for (MTSSource oldSource : oldSources) {
			if (!newSources.contains(oldSource) && oldSource != currentSource) {
				try {
					oldSource.close();
				} catch (Exception e) {
					log.error("Error closing source", e);

				}
			}
		}
		closeCurrentSource = !newSources.contains(currentSource);
		// Force next call to nextSource() to pick source 0 (first source)
		idx = -1;
	}

	@Override
	protected synchronized void closeInternal() throws Exception {
		for (MTSSource source : sources) {
			source.close();
		}
		if (closeCurrentSource && currentSource != null && !sources.contains(currentSource)) {
			currentSource.close();
		}
	}


	private synchronized void nextSource() {
		if (closeCurrentSource) {
			try {
				currentSource.close();
			} catch (Exception e) {
				log.error("Error closing source", e);
			} finally {
				closeCurrentSource = false;
			}
		}
		if (fixContinuity) {
			continuityFixer.nextSource();
		}
		idx++;
		if (idx >= sources.size()) {
			currentLoop++;
			if (maxLoops > 0 && currentLoop > maxLoops) {
				currentSource = null;
			} else {
				idx = 0;
				for (MTSSource source : sources) {
					try {
						((ResettableMTSSource) source).reset();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				currentSource = sources.get(idx);
			}
		} else {
			currentSource = sources.get(idx);
		}
		if (idx < sources.size()) {
			log.info("Switched to source #{}", idx);
		}
	}

	public static MultiMTSSourceBuilder builder() {
		return new MultiMTSSourceBuilder();
	}

	public static class MultiMTSSourceBuilder {
		private List<MTSSource> sources = Lists.newArrayList();
		boolean fixContinuity = false;
		private int maxLoops = 1;

		private MultiMTSSourceBuilder() {
		}

		public MultiMTSSourceBuilder addSource(MTSSource source) {
			sources.add(source);
			return this;
		}

		public MultiMTSSourceBuilder addSources(Collection<MTSSource> sources) {
			sources.addAll(sources);
			return this;
		}

		public MultiMTSSourceBuilder setSources(MTSSource... sources) {
			this.sources = Lists.newArrayList(sources);
			return this;
		}

		public MultiMTSSourceBuilder setSources(Collection<MTSSource> sources) {
			this.sources = Lists.newArrayList(sources);
			return this;
		}

		public MultiMTSSourceBuilder setFixContinuity(boolean fixContinuity) {
			this.fixContinuity = fixContinuity;
			return this;
		}

		public MultiMTSSourceBuilder loop() {
			this.maxLoops = -1;
			return this;
		}

		public MultiMTSSourceBuilder loops(int count) {
			this.maxLoops = count;
			return this;
		}

		public MultiMTSSourceBuilder noLoop() {
			this.maxLoops = 1;
			return this;
		}

		public MultiMTSSource build() {
			return new MultiMTSSource(fixContinuity, maxLoops, sources);
		}
	}
}
