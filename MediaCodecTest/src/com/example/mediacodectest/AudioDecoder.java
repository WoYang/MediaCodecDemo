package com.example.mediacodectest;

import java.nio.ByteBuffer;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;

public class AudioDecoder extends MediaDecoder {
	private static final String TAG = "AudioDecoder";
	private static final boolean DEBUG_AUDIO = false;

	private int mSampleRate = 0;
	private int channel = 0;

	public AudioDecoder(String videoFilePath, Surface surface,UpstreamCallback callback) {
		super(videoFilePath, surface, callback);
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
		AudioDecodePrepare();
	}

	public void AudioDecodePrepare() {
		try {
			for (int i = 0; i < mExtractor.getTrackCount(); i++) {
				MediaFormat format = mExtractor.getTrackFormat(i);
				String mime = format.getString(MediaFormat.KEY_MIME);
				if (mime.startsWith("audio/")) {
					mExtractor.selectTrack(i);
					mSampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE);
					channel = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
					mDecoder = MediaCodec.createDecoderByType(mime);
					mDecoder.configure(format, null, null, 0);
					break;
				}
			}

			if (mDecoder == null) {
				Log.e(TAG, "Can't find audio info!");
				return;
			}
			mDecoder.start();
			ByteBuffer[] inputBuffers = mDecoder.getInputBuffers();
			ByteBuffer[] outputBuffers = mDecoder.getOutputBuffers();
			BufferInfo info = new BufferInfo();
			int buffsize = AudioTrack.getMinBufferSize(mSampleRate,
					AudioFormat.CHANNEL_OUT_STEREO,
					AudioFormat.ENCODING_PCM_16BIT);
			AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
					mSampleRate, AudioFormat.CHANNEL_OUT_STEREO,
					AudioFormat.ENCODING_PCM_16BIT, buffsize,
					AudioTrack.MODE_STREAM);
			audioTrack.play();

			boolean isEOS = false;
			long startMs = System.currentTimeMillis();

			while (!Thread.interrupted() && !interrupted) {
				if (!isEOS) {
					int inIndex = mDecoder.dequeueInputBuffer(TIME_US);
					if (inIndex >= 0) {
						ByteBuffer buffer = inputBuffers[inIndex];
						int sampleSize = mExtractor.readSampleData(buffer, 0);
						if (sampleSize < 0) {
							// We shouldn't stop the playback at this point,
							// just pass the EOS
							// flag to mediaDecoder, we will get it again from
							// the
							// dequeueOutputBuffer
							Log.d(TAG, "InputBuffer BUFFER_FLAG_END_OF_STREAM");
							mDecoder.queueInputBuffer(inIndex, 0, 0, 0,
									MediaCodec.BUFFER_FLAG_END_OF_STREAM);
							isEOS = true;
						} else {
							mDecoder.queueInputBuffer(inIndex, 0,
									sampleSize, mExtractor.getSampleTime(), 0);
							mExtractor.advance();
						}
					}
				}
				int outIndex = mDecoder.dequeueOutputBuffer(info, TIME_US);
				switch (outIndex) {
				case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
					Log.d(TAG, "INFO_OUTPUT_BUFFERS_CHANGED");
					outputBuffers = mDecoder.getOutputBuffers();
					break;
				case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
					MediaFormat format = mDecoder.getOutputFormat();
					Log.d(TAG, "New format " + format);
					audioTrack.setPlaybackRate(format
							.getInteger(MediaFormat.KEY_SAMPLE_RATE));
					break;
				case MediaCodec.INFO_TRY_AGAIN_LATER:
					Log.d(TAG, "dequeueOutputBuffer timed out!");
					break;
				default:
					ByteBuffer buffer = outputBuffers[outIndex];
					
					if(DEBUG_AUDIO)Log.v(TAG,"We can't use this buffer but render it due to the API limit, "+ buffer);
					final byte[] chunk = new byte[info.size];
					buffer.get(chunk);
					if(mCallback != null){
						mCallback.UpstreamCallback(chunk,info.size);
					}
					//clear buffer,otherwise get the same buffer which is the last buffer
					buffer.clear();					
					// We use a very simple clock to keep the video FPS, or the
					// audio playback will be too fast
					while (info.presentationTimeUs / 1000 > System
							.currentTimeMillis() - startMs) {
						try {
							sleep(10);
						} catch (InterruptedException e) {
							e.printStackTrace();
							break;
						}
					}
					// AudioTrack write data
					audioTrack.write(chunk, info.offset, info.offset
							+ info.size);
					mDecoder.releaseOutputBuffer(outIndex, false);
					break;
				}
				// All decoded frames have been rendered, we can stop playing now
				if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
					Log.d(TAG, "OutputBuffer BUFFER_FLAG_END_OF_STREAM");
					break;
				}
			}
			mDecoder.stop();
			mDecoder.release();
			mExtractor.release();
			audioTrack.stop();
			audioTrack.release();
		} catch (Exception ioe) {
			throw new RuntimeException("failed init encoder", ioe);
		}
	}
	
}