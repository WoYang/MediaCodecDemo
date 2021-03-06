package com.example.mediacodectest2;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Environment;
import android.util.Log;
import android.view.Surface;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicReference;

@TargetApi(18)
public class DecodeEncodeMuxTest {
	
	/** lots of logging */
	private static final String TAG = "DecodeEncodeMuxTest";
	private static final boolean VERBOSE = true; 

	/** How long to wait for the next buffer to become available. */
	private static final int TIMEOUT_USEC = 10000;

	/** parameters for the video encoder */
	private static final String OUTPUT_VIDEO_MIME_TYPE = "video/avc"; 						// H.264 Advanced Video Coding
	private static final int OUTPUT_VIDEO_BIT_RATE = 512 * 1024; 							// 512 kbps maybe better
	private static final int OUTPUT_VIDEO_FRAME_RATE = 25; 									// 25fps
	private static final int OUTPUT_VIDEO_IFRAME_INTERVAL = 10; 							// 10 seconds between I-frames
	private static final int OUTPUT_VIDEO_COLOR_FORMAT = MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar;
	
	/** parameters for the audio encoder */
	private String OUTPUT_AUDIO_MIME_TYPE = "audio/mp4a-latm"; 								// Advanced Audio Coding
	private int OUTPUT_AUDIO_BIT_RATE = 64 * 1024;											// 64 kbps
	private int OUTPUT_AUDIO_AAC_PROFILE = MediaCodecInfo.CodecProfileLevel.AACObjectLC;	//better then AACObjectHE?
	/**parameters for the audio encoder config from input stream */
	private int OUTPUT_AUDIO_CHANNEL_COUNT = 1; 	 										// Must match the input stream.can not config
	private int OUTPUT_AUDIO_SAMPLE_RATE_HZ = 48000; 										// Must match the input stream.can not config

	/** Whether to copy the video from the test video. */
	private boolean mCopyVideo = false;
	/** Whether to copy the audio from the test audio. */
	private boolean mCopyAudio = false;
	
	/** Width of the output frames. */
	private int mWidth = -1;
	/** Height of the output frames. */
	private int mHeight = -1;

	/** The raw resource used as the input file. */
	private String mBaseFileRoot;
	/** The raw resource used as the input file. */
	private String mBaseFile;
	/** The destination file for the encoded output. */
	private String mOutputFile;
	
	private boolean interrupted = false;
	
	public abstract interface CodecCallBack{
		public final static int STATUS_FAIL = 0;
		public final static int STATUS_SUCCESS = 1;
		public abstract void onCallBack(int status);
	}
	public CodecCallBack mCallback;
	public String media_url;
	
	public DecodeEncodeMuxTest(String url ,CodecCallBack callback){
		this(url,callback,true,true);	
	}
	
	public DecodeEncodeMuxTest(String url ,CodecCallBack callback,boolean encode_video,boolean encode_audio){
		this.mCallback = callback;
		this.media_url = url;
		if(encode_video){
			setCopyVideo();
		}
		if(encode_audio){
			setCopyAudio();
		}		
	}
	
	public void testExtractDecodeEncodeMuxAudioVideo() throws Throwable {
		String path = Environment.getExternalStorageDirectory().getPath();
		Log.e(TAG, "getExternalStorageDirectory : "+path);
		//video size ratio is 16:9
//		setSize(1920, 1080);		//(16:9)
//		setSize(1280, 720);			//(16:9)
//		setSize(1024, 576);			//(16:9)
//		setSize(640, 360);			//(16:9)
//		setSize(256, 144);			//(16:9)
		
		//video size ratio is 4:3
//		setSize(1440, 1080)			//(4:3)
//		setSize(960, 720);			//(4:3)
//		setSize(768, 576);			//(4:3)
//		setSize(480, 360);			//(4:3)
//		setSize(192, 144);			//(4:3)
		
		//############## local file test ################
//		setSource(path,"/storage/emulated/0/demo.mpg");

		//############## net source test ################
		
		//avc Baseline@L3 + AAC LC		//OK
//		setSource(path,"http://192.168.20.146/video_480x360_mp4_h264_500kbps_30fps_aac_stereo_128kbps_44100hz.mp4");
		
		//H.263 Baseline@1.0 + AAC LC	//OK
//		setSource(path,"http://192.168.20.146/video_176x144_3gp_h263_300kbps_25fps_aac_stereo_24kbps_11025hz.3gp");
		
		//avc Main@L3 + MPEG Audio V1L2 		//OK file from iptv.pcap
//		setSource(path,"http://192.168.20.146/demo.mpg");
		
		//MPEG Video V2(Main@Main) + MPEG Audio V1L2	//OK
//		setSource(path,"http://192.168.20.146/1793_320.ts");
				
		//avc Main@L3 + MPEG Audio V1L2 		// OK
//		setSource(path,"http://192.168.20.146/shandongBQ-UDP8148.ts");
		
		//avc Baseline@L3 + MPEG Audio V1L2 	//OK
//		setSource(path,"http://192.168.20.146/yulejingxuan2.ts");
		
		//avc Main@L4 + AAC V2LC				//OK
//		setSource(path,"http://192.168.20.146/Batman1_CBR_AVC_Main_L4_AAC.ts");	
		
		//avc Main@L4/avc Main@L4 +  MPEG Audio V1L2 //why must decode by original width height decode?
//		setSource(path,"http://192.168.20.146/main_profile.ts");
		
		//avc Main@L4.1 + AC3 + AC3				//fail to extractor 2 audio channel
//		setSource(path,"http://192.168.20.146/Final_Point_3d_1080i_8M_Main@L4.1_AC3_5.1channel.ts");
		
		//MPEG-4 Visual(Simple@L1) + AAC LC		//why must decode by original width height decode?
//		setSource(path,"http://192.168.20.146/MPEG4_HD_ZTE_AXON.mp4");
				
		//avc Main@L3.1 + MPEG Audio V1L2 		//why must decode by original width height decode?
//		setSource(path,"http://192.168.20.146/PAL_H264_Main@L3.1_Mpeg1layer2.ts");	
	
		//avc High@L4 + MPEG Audio V1L2 		//fail to decode avc High@L4?why need decode by original width height decode
//		setSource(path,"http://192.168.20.146/hunanweishi_VBR_AVC_HighL4_7_40M.ts");
		
		//avc High@L4 + AAC V2LC 				// fail to decode avc High@L4?why need decode by original width height decode
//		setSource(path,"http://192.168.20.146/BTV_1080i.ts");
		
		setSource(path,media_url);
		TestWrapper.runTest(this);
	}

	/** Wraps testExtractDecodeEditEncodeMux() */
	private static class TestWrapper implements Runnable {
		private Throwable mThrowable;
		private DecodeEncodeMuxTest mTest;

		private TestWrapper(DecodeEncodeMuxTest test) {
			mTest = test;
		}

		@Override
		public void run() {
			try {
				mTest.extractDecodeEncodeMux();
			} catch (Throwable th) {
				mThrowable = th;
			}
		}

		/**
		 * Entry point.
		 */
		public static void runTest(DecodeEncodeMuxTest test)
				throws Throwable {
			TestWrapper wrapper = new TestWrapper(test);
			Thread th = new Thread(wrapper, "codec test");
			th.start();
			th.join();
			if (wrapper.mThrowable != null) {
				throw wrapper.mThrowable;
			}
		}
	}

	/**
	 * Sets the test to copy the video stream.
	 */
	private void setCopyVideo() {
		mCopyVideo = true;
	}

	/**
	 * Sets the test to copy the video stream.
	 */
	private void setCopyAudio() {
		mCopyAudio = true;
	}

	/**
	 * Sets the desired frame size.
	 */
	private void setSize(int width, int height) {
		if ((width % 16) != 0 || (height % 16) != 0) {
			Log.e(TAG, "WARNING: width or height not multiple of 16");
		}
		mWidth = width;
		mHeight = height;
	}

	/**
	 * Sets the raw resource used as the source video.
	 */
	private void setSource(String root,String filename) {
		mBaseFileRoot = root;
		mBaseFile = filename;
	}

	/**
	 * Sets the name of the output file based on the other parameters.
	 * 
	 * <p>
	 * Must be called after {@link #setSize(int, int)} and
	 * {@link #setSource(int)}.
	 */
	private void setOutputFile() {
		StringBuilder sb = new StringBuilder();
		sb.append(mBaseFileRoot);
		sb.append("/encode");
		sb.append('-');
		sb.append(System.currentTimeMillis());
		if (mCopyVideo) {
			sb.append('-');
			sb.append("video");
			sb.append('-');
			sb.append(mWidth);
			sb.append('x');
			sb.append(mHeight);
			sb.append('-');
			sb.append(OUTPUT_VIDEO_FRAME_RATE+"fps");
			sb.append('-');
			sb.append(OUTPUT_VIDEO_BIT_RATE/1024+"kps");
		}
		if (mCopyAudio) {
			sb.append('-');
			sb.append("audio");
			sb.append('-');
			sb.append(OUTPUT_AUDIO_BIT_RATE/1024+"kps");
		}
		sb.append(".mp4");
		mOutputFile = sb.toString();
	}

	/**
	 * Tests encoding and subsequently decoding video from frames generated into
	 * a buffer.
	 * <p>
	 * We encode several frames of a video test pattern using MediaCodec, then
	 * decode the output with MediaCodec and do some simple checks.
	 */
	private void extractDecodeEncodeMux() throws Exception {
		// Exception that may be thrown during release.
		Exception exception = null;
		MediaExtractor videoExtractor = null;
		MediaExtractor audioExtractor = null;
		OutputSurface outputSurface = null;
		MediaCodec videoDecoder = null;
		MediaCodec audioDecoder = null;
		MediaCodec videoEncoder = null;
		MediaCodec audioEncoder = null;
		MediaMuxer muxer = null;

		InputSurface inputSurface = null;
		
		try {
			if (mCopyVideo) {
				videoExtractor = createExtractor();
				int videoInputTrack = getAndSelectVideoTrackIndex(videoExtractor);
				Log.d(TAG, " video track in test video " + videoInputTrack);
				MediaFormat inputFormat = videoExtractor.getTrackFormat(videoInputTrack);
				if (VERBOSE)Log.d(TAG, "video base input format: " + inputFormat);
				//make sure decode buffer size equals encode buffer size
				inputFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT,OUTPUT_VIDEO_COLOR_FORMAT);
				if(mWidth != -1){
					inputFormat.setInteger(MediaFormat.KEY_WIDTH, mWidth);
				}else{
					mWidth = inputFormat.getInteger(MediaFormat.KEY_WIDTH);
				}
				if(mHeight != -1){
					inputFormat.setInteger(MediaFormat.KEY_HEIGHT, mHeight);
				}else{
					mHeight = inputFormat.getInteger(MediaFormat.KEY_HEIGHT);
				}
				if (VERBOSE)Log.d(TAG, "video match input format: " + inputFormat);
				MediaCodecInfo videoCodecInfo = selectCodec(OUTPUT_VIDEO_MIME_TYPE);
				if (videoCodecInfo == null) {
					// Don't fail CTS if they don't have an AVC codec (not here,anyway).
					Log.e(TAG, "Unable to find an appropriate codec for "
							+ OUTPUT_VIDEO_MIME_TYPE);
					return;
				}
				if (VERBOSE)Log.d(TAG, "video found codec: " + videoCodecInfo.getName());

				MediaFormat outputVideoFormat = MediaFormat.createVideoFormat(
						OUTPUT_VIDEO_MIME_TYPE, mWidth, mHeight);
				outputVideoFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT,
						OUTPUT_VIDEO_COLOR_FORMAT);
				outputVideoFormat.setInteger(MediaFormat.KEY_BIT_RATE,
						OUTPUT_VIDEO_BIT_RATE);
				outputVideoFormat.setInteger(MediaFormat.KEY_FRAME_RATE,
						OUTPUT_VIDEO_FRAME_RATE);
				outputVideoFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL,
						OUTPUT_VIDEO_IFRAME_INTERVAL);
				if (VERBOSE)
					Log.d(TAG, "video encode format: " + outputVideoFormat);
				videoEncoder = createVideoEncoder(videoCodecInfo,
						outputVideoFormat, null);
				videoDecoder = createVideoDecoder(inputFormat, null);
				
//				showSupportedColorFormat(videoDecoder.getCodecInfo().getCapabilitiesForType(inputFormat.getString(MediaFormat.KEY_MIME)));
			}

			if (mCopyAudio) {
				audioExtractor = createExtractor();
				int audioInputTrack = getAndSelectAudioTrackIndex(audioExtractor);
				Log.d(TAG, " audio track in test audio " + audioInputTrack);
				// assertTrue("missing audio track in test video", audioInputTrack != -1);
				MediaFormat inputFormat = audioExtractor.getTrackFormat(audioInputTrack);
				if (VERBOSE)Log.d(TAG, "audio base input format: " + inputFormat);
//				inputFormat.setInteger(MediaFormat.KEY_AAC_PROFILE,OUTPUT_AUDIO_AAC_PROFILE);			
				OUTPUT_AUDIO_SAMPLE_RATE_HZ = inputFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE,
						OUTPUT_AUDIO_SAMPLE_RATE_HZ);
				OUTPUT_AUDIO_CHANNEL_COUNT = inputFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT,
						OUTPUT_AUDIO_CHANNEL_COUNT);
				if (VERBOSE)Log.d(TAG, "audio match input format: " + inputFormat);
				MediaCodecInfo audioCodecInfo = selectCodec(OUTPUT_AUDIO_MIME_TYPE);
				if (audioCodecInfo == null) {
					// Don't fail CTS if they don't have an AAC codec (not here,anyway).
					Log.e(TAG, "Unable to find an appropriate codec for "
							+ OUTPUT_AUDIO_MIME_TYPE);
					return;
				}
				if (VERBOSE)Log.d(TAG, "audio found codec: " + audioCodecInfo.getName());
				
				MediaFormat outputAudioFormat = MediaFormat.createAudioFormat(
						OUTPUT_AUDIO_MIME_TYPE, OUTPUT_AUDIO_SAMPLE_RATE_HZ,
						OUTPUT_AUDIO_CHANNEL_COUNT);
				outputAudioFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 100 * 1024);
				outputAudioFormat.setInteger(MediaFormat.KEY_BIT_RATE,
						OUTPUT_AUDIO_BIT_RATE);
				outputAudioFormat.setInteger(MediaFormat.KEY_AAC_PROFILE,
						OUTPUT_AUDIO_AAC_PROFILE);
				if (VERBOSE)Log.d(TAG, "audio encode format: " + outputAudioFormat);
				// Create a MediaCodec for the desired codec, then configure it
				// as an encoder with our desired properties. 
				audioEncoder = createAudioEncoder(audioCodecInfo,
						outputAudioFormat);
				// Create a MediaCodec for the decoder, based on the extractor's format.
				audioDecoder = createAudioDecoder(inputFormat);
			}
			
			setOutputFile();
			// Creates a muxer but do not start or add tracks just yet.
			muxer = createMuxer();

			doExtractDecodeEncodeMux(videoExtractor, audioExtractor,
					videoDecoder, videoEncoder, audioDecoder, audioEncoder,
					muxer, inputSurface, outputSurface);
			if(mCallback != null){
				mCallback.onCallBack(CodecCallBack.STATUS_SUCCESS);
			}
		} finally {
			if (VERBOSE)
				Log.d(TAG, "releasing extractor, decoder, encoder, and muxer");
			// Try to release everything we acquired, even if one of the
			// releases fails, in which
			// case we save the first exception we got and re-throw at the end
			// (unless something
			// other exception has already been thrown). This guarantees the
			// first exception thrown
			// is reported as the cause of the error, everything is (attempted)
			// to be released, and
			// all other exceptions appear in the logs.
			try {
				if (videoExtractor != null) {
					videoExtractor.release();
				}
			} catch (Exception e) {
				Log.e(TAG, "error while releasing videoExtractor", e);
//				if (exception == null) {
//					exception = e;
//				}
			}
			try {
				if (audioExtractor != null) {
					audioExtractor.release();
				}
			} catch (Exception e) {
				Log.e(TAG, "error while releasing audioExtractor", e);
//				if (exception == null) {
//					exception = e;
//				}
			}
			try {
				if (videoDecoder != null) {
					videoDecoder.stop();
					videoDecoder.release();
				}
			} catch (Exception e) {
				Log.e(TAG, "error while releasing videoDecoder", e);
//				if (exception == null) {
//					exception = e;
//				}
			}
			try {
				if (outputSurface != null) {
					outputSurface.release();
				}
			} catch (Exception e) {
				Log.e(TAG, "error while releasing outputSurface", e);
//				if (exception == null) {
//					exception = e;
//				}
			}
			try {
				if (videoEncoder != null) {
					videoEncoder.stop();
					videoEncoder.release();
				}
			} catch (Exception e) {
				Log.e(TAG, "error while releasing videoEncoder", e);
//				if (exception == null) {
//					exception = e;
//				}
			}
			try {
				if (audioDecoder != null) {
					audioDecoder.stop();
					audioDecoder.release();
				}
			} catch (Exception e) {
				Log.e(TAG, "error while releasing audioDecoder", e);
//				if (exception == null) {
//					exception = e;
//				}
			}
			try {
				if (audioEncoder != null) {
					audioEncoder.stop();
					audioEncoder.release();
				}
			} catch (Exception e) {
				Log.e(TAG, "error while releasing audioEncoder", e);
//				if (exception == null) {
//					exception = e;
//				}
			}
			try {
				if (muxer != null) {
					muxer.stop();
					muxer.release();
				}
			} catch (Exception e) {
				Log.e(TAG, "error while releasing muxer", e);
//				if (exception == null) {
//					exception = e;
//				}
			}
			try {
				if (inputSurface != null) {
					inputSurface.release();
				}
			} catch (Exception e) {
				Log.e(TAG, "error while releasing inputSurface", e);
//				if (exception == null) {
//					exception = e;
//				}
			}
		}
		if (exception != null) {
			throw exception;
		}
		
	}

	/**
	 * Creates an extractor that reads its frames from {@link #mSourceResId}.
	 */
	private MediaExtractor createExtractor() throws IOException {
		//net source
		MediaExtractor extractor = new MediaExtractor();
		if(mBaseFile.contains(":")){
			extractor.setDataSource(mBaseFile);
		}else{
			File mFile = new File(mBaseFile);
			extractor.setDataSource(mFile.toString());
		}
		return extractor;
	}

	/**
	 * Creates a decoder for the given format, which outputs to the given
	 * surface.
	 * 
	 * @param inputFormat
	 *            the format of the stream to decode
	 * @param surface
	 *            into which to decode the frames
	 */
	private MediaCodec createVideoDecoder(MediaFormat inputFormat,
			Surface surface) {
		MediaCodec decoder = MediaCodec
				.createDecoderByType(getMimeTypeFor(inputFormat));
		decoder.configure(inputFormat, surface, null, 0);
		decoder.start();
		return decoder;
	}
	
	/**
	 * Creates an encoder for the given format using the specified codec, taking
	 * input from a surface.
	 * 
	 * <p>
	 * The surface to use as input is stored in the given reference.
	 * 
	 * @param codecInfo
	 *            of the codec to use
	 * @param format
	 *            of the stream to be produced
	 * @param surfaceReference
	 *            to store the surface to use as input
	 */
	private MediaCodec createVideoEncoder(MediaCodecInfo codecInfo,
			MediaFormat format, AtomicReference<Surface> surfaceReference) {
		MediaCodec encoder = MediaCodec.createByCodecName(codecInfo.getName());
		encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
		// Must be called before start() is.
		if (surfaceReference != null) {
			surfaceReference.set(encoder.createInputSurface());
		}
		encoder.start();
		return encoder;
	}

	/**
	 * Creates a decoder for the given format.
	 * 
	 * @param inputFormat
	 *            the format of the stream to decode
	 */
	private MediaCodec createAudioDecoder(MediaFormat inputFormat) {
		MediaCodec decoder = MediaCodec
				.createDecoderByType(getMimeTypeFor(inputFormat));
		decoder.configure(inputFormat, null, null, 0);
		decoder.start();
		return decoder;
	}

	/**
	 * Creates an encoder for the given format using the specified codec.
	 * 
	 * @param codecInfo
	 *            of the codec to use
	 * @param format
	 *            of the stream to be produced
	 */
	private MediaCodec createAudioEncoder(MediaCodecInfo codecInfo,
			MediaFormat format) {
		MediaCodec encoder = MediaCodec.createByCodecName(codecInfo.getName());
		encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
		encoder.start();
		return encoder;
	}

	/**
	 * Creates a muxer to write the encoded frames.
	 * 
	 * <p>
	 * The muxer is not started as it needs to be started only after all streams
	 * have been added.
	 */
	private MediaMuxer createMuxer() throws IOException {
		return new MediaMuxer(mOutputFile,
				MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
	}

	private int getAndSelectVideoTrackIndex(MediaExtractor extractor) {
		for (int index = 0; index < extractor.getTrackCount(); ++index) {
			if (VERBOSE) {
				Log.d(TAG, "format for track " + index + " is "
						+ getMimeTypeFor(extractor.getTrackFormat(index)));
			}
			if (isVideoFormat(extractor.getTrackFormat(index))) {
				extractor.selectTrack(index);
				return index;
			}
		}
		return -1;
	}

	private int getAndSelectAudioTrackIndex(MediaExtractor extractor) {
		for (int index = 0; index < extractor.getTrackCount(); ++index) {
			if (VERBOSE) {
				Log.d(TAG, "format for track " + index + " is "
						+ getMimeTypeFor(extractor.getTrackFormat(index)));
			}
			if (isAudioFormat(extractor.getTrackFormat(index))) {
				extractor.selectTrack(index);
				return index;
			}
		}
		return -1;
	}

	/**
	 * Does the actual work for extracting, decoding, encoding and muxing.
	 */
	private void doExtractDecodeEncodeMux(MediaExtractor videoExtractor,
			MediaExtractor audioExtractor, MediaCodec videoDecoder,
			MediaCodec videoEncoder, MediaCodec audioDecoder,
			MediaCodec audioEncoder, MediaMuxer muxer,
			InputSurface inputSurface, OutputSurface outputSurface) {
		ByteBuffer[] videoDecoderInputBuffers = null;
		ByteBuffer[] videoDecoderOutputBuffers = null;
		ByteBuffer[] videoEncoderInputBuffers = null;
		ByteBuffer[] videoEncoderOutputBuffers = null;
		MediaCodec.BufferInfo videoDecoderOutputBufferInfo = null;
		MediaCodec.BufferInfo videoEncoderOutputBufferInfo = null;
		if (mCopyVideo) {
			videoDecoderInputBuffers = videoDecoder.getInputBuffers();
			videoDecoderOutputBuffers = videoDecoder.getOutputBuffers();
			videoEncoderInputBuffers = videoEncoder.getInputBuffers();
			videoEncoderOutputBuffers = videoEncoder.getOutputBuffers();
			videoDecoderOutputBufferInfo = new MediaCodec.BufferInfo();
			videoEncoderOutputBufferInfo = new MediaCodec.BufferInfo();
		}
		ByteBuffer[] audioDecoderInputBuffers = null;
		ByteBuffer[] audioDecoderOutputBuffers = null;
		ByteBuffer[] audioEncoderInputBuffers = null;
		ByteBuffer[] audioEncoderOutputBuffers = null;
		MediaCodec.BufferInfo audioDecoderOutputBufferInfo = null;
		MediaCodec.BufferInfo audioEncoderOutputBufferInfo = null;
		if (mCopyAudio) {
			audioDecoderInputBuffers = audioDecoder.getInputBuffers();
			audioDecoderOutputBuffers = audioDecoder.getOutputBuffers();
			audioEncoderInputBuffers = audioEncoder.getInputBuffers();
			audioEncoderOutputBuffers = audioEncoder.getOutputBuffers();
			audioDecoderOutputBufferInfo = new MediaCodec.BufferInfo();
			audioEncoderOutputBufferInfo = new MediaCodec.BufferInfo();
		}
		// We will get these from the decoders when notified of a format change.
		MediaFormat decoderOutputVideoFormat = null;
		MediaFormat decoderOutputAudioFormat = null;
		// We will get these from the encoders when notified of a format change.
		MediaFormat encoderOutputVideoFormat = null;
		MediaFormat encoderOutputAudioFormat = null;
		// We will determine these once we have the output format.
		int outputVideoTrack = -1;
		int outputAudioTrack = -1;
		// Whether things are done on the video side.
		boolean videoExtractorDone = false;
		boolean videoDecoderDone = false;
		boolean videoEncoderDone = false;
		// Whether things are done on the audio side.
		boolean audioExtractorDone = false;
		boolean audioDecoderDone = false;
		boolean audioEncoderDone = false;

		// The video decoder output buffer to process, -1 if none.
		int pendingVideoDecoderOutputBufferIndex = -1;
		// The audio decoder output buffer to process, -1 if none.
		int pendingAudioDecoderOutputBufferIndex = -1;

		boolean muxing = false;

		int videoExtractedFrameCount = 0;
		int videoDecodedFrameCount = 0;
		int videoEncodedFrameCount = 0;

		int audioExtractedFrameCount = 0;
		int audioDecodedFrameCount = 0;
		int audioEncodedFrameCount = 0;
		
		boolean mVideoConfig = false;
		boolean mainVideoFrame = false;
		long mLastVideoSampleTime = 0;
		long mVideoSampleTime = 0;
		
		boolean mAudioConfig = false;
		boolean mainAudioFrame = false;
		long mLastAudioSampleTime = 0;
		long mAudioSampleTime = 0;

		while (!interrupted && ((mCopyVideo && !videoEncoderDone)
				|| (mCopyAudio && !audioEncoderDone))) {
			if (VERBOSE) {
				Log.d(TAG, String.format("loop: "

				+ "V(%b){" + "extracted:%d(done:%b) " + "decoded:%d(done:%b) "
						+ "encoded:%d(done:%b)} "

						+ "A(%b){" + "extracted:%d(done:%b) "
						+ "decoded:%d(done:%b) " + "encoded:%d(done:%b) "
						+ "pending:%d} "

						+ "muxing:%b(V:%d,A:%d)",

				mCopyVideo, videoExtractedFrameCount, videoExtractorDone,
						videoDecodedFrameCount, videoDecoderDone,
						videoEncodedFrameCount, videoEncoderDone,

						mCopyAudio, audioExtractedFrameCount,
						audioExtractorDone, audioDecodedFrameCount,
						audioDecoderDone, audioEncodedFrameCount,
						audioEncoderDone, pendingAudioDecoderOutputBufferIndex,

						muxing, outputVideoTrack, outputAudioTrack));
			}

			//###########################Video###################################	
			// Extract video from file and feed to decoder.
			// Do not extract video if we have determined the output format but
			// we are not yet ready to mux the frames.
			while (mCopyVideo && !videoExtractorDone
					&& (encoderOutputVideoFormat == null || muxing)) {
				int decoderInputBufferIndex = videoDecoder
						.dequeueInputBuffer(TIMEOUT_USEC);
				if (decoderInputBufferIndex <= MediaCodec.INFO_TRY_AGAIN_LATER) {
					if (VERBOSE)Log.d(TAG, "no video decoder input buffer: "+decoderInputBufferIndex);
					break;
				}
				if (VERBOSE) {
					Log.d(TAG, "video decoder dequeueInputBuffer: returned input buffer: "+ decoderInputBufferIndex);
				}
				ByteBuffer decoderInputBuffer = videoDecoderInputBuffers[decoderInputBufferIndex];
				int size = videoExtractor.readSampleData(decoderInputBuffer, 0);
				if(videoExtractor.getSampleFlags() == MediaExtractor.SAMPLE_FLAG_SYNC){
					Log.d(TAG, " video decoder SAMPLE_FLAG_SYNC ");
				}
				long presentationTime = videoExtractor.getSampleTime();
				if (VERBOSE) {
					Log.d(TAG, "video extractor: returned buffer of size "
							+ size);
					Log.d(TAG, "video extractor: returned buffer for time "
							+ presentationTime);
				}
				if (size > 0) {
					videoDecoder.queueInputBuffer(decoderInputBufferIndex, 0,
							size, presentationTime,videoExtractor.getSampleFlags());
				}
				videoExtractorDone = !videoExtractor.advance();
				if (videoExtractorDone) {
					if (VERBOSE)Log.d(TAG, "video extractor: EOS");
					videoDecoder.queueInputBuffer(decoderInputBufferIndex, 0,
							0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
				}
				videoExtractedFrameCount++;
				// We extracted a frame, let's try something else next.
				break;
			}
			
			//###########################Audio###################################
			// Extract audio from file and feed to decoder.
			// Do not extract audio if we have determined the output format but
			// we are not yet ready to mux the frames.
			while (mCopyAudio && !audioExtractorDone
					&& (encoderOutputAudioFormat == null || muxing)) {
				int decoderInputBufferIndex = audioDecoder.dequeueInputBuffer(TIMEOUT_USEC);
				if (decoderInputBufferIndex <= MediaCodec.INFO_TRY_AGAIN_LATER) {
					if (VERBOSE)Log.d(TAG, "no audio decoder input buffer: "+decoderInputBufferIndex);
					break;
				}
				if (VERBOSE) {
					Log.d(TAG, "audio decoder dequeueInputBuffer: returned input buffer: "
							+ decoderInputBufferIndex);
				}
				ByteBuffer decoderInputBuffer = audioDecoderInputBuffers[decoderInputBufferIndex];
				int size = audioExtractor.readSampleData(decoderInputBuffer, 0);
				long presentationTime = audioExtractor.getSampleTime();
				if (VERBOSE) {
					Log.d(TAG, "audio extractor: returned buffer of size "
							+ size);
					Log.d(TAG, "audio extractor: returned buffer for time "
							+ presentationTime);
				}
				if (size > 0) {
					audioDecoder.queueInputBuffer(decoderInputBufferIndex, 0,
							size, presentationTime,
							audioExtractor.getSampleFlags());
				}
				audioExtractorDone = !audioExtractor.advance();
				if (audioExtractorDone) {
					if (VERBOSE)Log.d(TAG, "audio extractor: EOS");
					audioDecoder.queueInputBuffer(decoderInputBufferIndex, 0,
							0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
				}
				audioExtractedFrameCount++;
				// We extracted a frame, let's try something else next.
				break;
			}

			// Poll output frames from the video decoder and feed the encoder.
			while (mCopyVideo && !videoDecoderDone
					&& pendingVideoDecoderOutputBufferIndex == -1
					&& (encoderOutputVideoFormat == null || muxing)) {
				int decoderOutputBufferIndex = videoDecoder.dequeueOutputBuffer(videoDecoderOutputBufferInfo,
								TIMEOUT_USEC);
				if (decoderOutputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
					if (VERBOSE)Log.d(TAG, "no video decoder output buffer");
					break;
				}else if (decoderOutputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
					//do what for this?
					decoderOutputVideoFormat = videoDecoder.getOutputFormat();
					if (VERBOSE) {
						Log.d(TAG, "video decoder: output format changed: " + decoderOutputVideoFormat);
					}
					break;
				}else if (decoderOutputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
					if (VERBOSE)Log.d(TAG, "video decoder: output buffers changed");
					videoDecoderOutputBuffers = videoDecoder.getOutputBuffers();
					break;
				}
				
				if (VERBOSE) {
					Log.d(TAG, "video decoder: returned output buffer: "
							+ decoderOutputBufferIndex);
					Log.d(TAG, "video decoder: returned buffer of size "
							+ videoDecoderOutputBufferInfo.size);
				}
				if ((videoDecoderOutputBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
					if (VERBOSE)Log.d(TAG, "video decoder: codec config buffer");
					videoDecoder.releaseOutputBuffer(decoderOutputBufferIndex,false);
					break;
				}
				if (VERBOSE) {
					Log.d(TAG, "video decoder: returned buffer for time "
							+ videoDecoderOutputBufferInfo.presentationTimeUs);
				}

				pendingVideoDecoderOutputBufferIndex = decoderOutputBufferIndex;
				videoDecodedFrameCount++;
				// We extracted a pending frame, let's try something else next.
				break;
			}

			// Feed the pending decoded audio buffer to the video encoder.
			while (mCopyVideo && pendingVideoDecoderOutputBufferIndex != -1) {
				if (VERBOSE) {
					Log.d(TAG,"video decoder: attempting to process pending buffer: "
									+ pendingVideoDecoderOutputBufferIndex);
				}
				int encoderInputBufferIndex = videoEncoder.dequeueInputBuffer(TIMEOUT_USEC);
				if (encoderInputBufferIndex <= MediaCodec.INFO_TRY_AGAIN_LATER) {
					if (VERBOSE)Log.d(TAG, "no video encoder input buffer: "
							+encoderInputBufferIndex);
					break;
				}
				if (VERBOSE) {
					Log.d(TAG, "video encoder: returned input buffer: "
							+ encoderInputBufferIndex);
				}
				ByteBuffer encoderInputBuffer = videoEncoderInputBuffers[encoderInputBufferIndex];
				int size = videoDecoderOutputBufferInfo.size;
				long presentationTime = videoDecoderOutputBufferInfo.presentationTimeUs;
				if (VERBOSE) {
					Log.d(TAG, "video decoder: processing pending buffer: "
							+ pendingVideoDecoderOutputBufferIndex);
					Log.d(TAG, "video decoder: pending buffer of size " + size);
					Log.d(TAG, "video decoder: pending buffer for time "
							+ presentationTime);
				}
				if (size >= 0) {
					
					try {
						ByteBuffer decoderOutputBuffer = videoDecoderOutputBuffers[pendingVideoDecoderOutputBufferIndex]
								.duplicate();
						decoderOutputBuffer
								.position(videoDecoderOutputBufferInfo.offset);
						decoderOutputBuffer
								.limit(videoDecoderOutputBufferInfo.offset + size);
						encoderInputBuffer.position(0);
						encoderInputBuffer.put(decoderOutputBuffer);
						//size not enable
						videoEncoder.queueInputBuffer(encoderInputBufferIndex, 0,
								size, presentationTime,
								videoDecoderOutputBufferInfo.flags);
					} catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
					}
					
				}
				videoDecoder.releaseOutputBuffer(
						pendingVideoDecoderOutputBufferIndex, false);
				pendingVideoDecoderOutputBufferIndex = -1;
				if ((videoDecoderOutputBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
					if (VERBOSE)Log.d(TAG, "video decoder: EOS");
					videoDecoderDone = true;
				}
				// We enqueued a pending frame, let's try something else next.
				break;
			}
			// Poll frames from the video encoder and send them to the muxer.
			while (mCopyVideo && !videoEncoderDone
					&& (encoderOutputVideoFormat == null || muxing)) {
				// can not get avilabel outputBuffers?
				int encoderOutputBufferIndex = videoEncoder.dequeueOutputBuffer(videoEncoderOutputBufferInfo,
								TIMEOUT_USEC);
				if (encoderOutputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
					if (VERBOSE)Log.d(TAG, "no video encoder output buffer");
					break;
				}else if (encoderOutputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
					if (VERBOSE)Log.d(TAG, "video encoder: output format changed");
					if (outputVideoTrack >= 0) {
						// fail("video encoder changed its output format again?");
						Log.d(TAG,"video encoder changed its output format again?");
					}
					encoderOutputVideoFormat = videoEncoder.getOutputFormat();
					break;
				}else if (encoderOutputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
					if (VERBOSE)Log.d(TAG, "video encoder: output buffers changed");
					videoEncoderOutputBuffers = videoEncoder.getOutputBuffers();
					break;
				}
				
				// assertTrue("should have added track before processing output", muxing);
				if (VERBOSE) {
					Log.d(TAG, "video encoder: returned output buffer: "
							+ encoderOutputBufferIndex);
					Log.d(TAG, "video encoder: returned buffer of size "
							+ videoEncoderOutputBufferInfo.size);
				}
				ByteBuffer encoderOutputBuffer = videoEncoderOutputBuffers[encoderOutputBufferIndex];
				if ((videoEncoderOutputBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
					if (VERBOSE)Log.d(TAG, "video encoder: codec config buffer");
					// Simply ignore codec config buffers.
					mVideoConfig = true;
					videoEncoder.releaseOutputBuffer(encoderOutputBufferIndex,false);
					break;
				}
				if (VERBOSE) {
					Log.d(TAG, "video encoder: returned buffer for time "
							+ videoEncoderOutputBufferInfo.presentationTimeUs);
				}
				
				if(mVideoConfig){
					if(!mainVideoFrame){
						mLastVideoSampleTime = videoEncoderOutputBufferInfo.presentationTimeUs;
						mainVideoFrame = true;
					}else{
						if(mVideoSampleTime == 0){
							mVideoSampleTime = videoEncoderOutputBufferInfo.presentationTimeUs - mLastVideoSampleTime;
						}
					}
				}
				videoEncoderOutputBufferInfo.presentationTimeUs = mLastVideoSampleTime + mVideoSampleTime;
				if (videoEncoderOutputBufferInfo.size != 0) {
					muxer.writeSampleData(outputVideoTrack,
							encoderOutputBuffer, videoEncoderOutputBufferInfo);
					mLastVideoSampleTime = videoEncoderOutputBufferInfo.presentationTimeUs;
				}
				
				if ((videoEncoderOutputBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
					if (VERBOSE)Log.d(TAG, "video encoder: EOS");
					videoEncoderDone = true;
				}
				videoEncoder.releaseOutputBuffer(encoderOutputBufferIndex,
						false);
				videoEncodedFrameCount++;
				// We enqueued an encoded frame, let's try something else next.
				break;
			}

			// Poll output frames from the audio decoder.
			// Do not poll if we already have a pending buffer to feed to the
			// encoder.
			while (mCopyAudio && !audioDecoderDone
					&& pendingAudioDecoderOutputBufferIndex == -1
					&& (encoderOutputAudioFormat == null || muxing)) {
				int decoderOutputBufferIndex = audioDecoder
						.dequeueOutputBuffer(audioDecoderOutputBufferInfo,TIMEOUT_USEC);
				if (decoderOutputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
					if (VERBOSE)Log.d(TAG, "no audio decoder output buffer");
					break;
				}else if (decoderOutputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
					 decoderOutputAudioFormat = audioDecoder.getOutputFormat();
					if (VERBOSE) {
						Log.d(TAG, "audio decoder: output format changed: " + decoderOutputAudioFormat);
					}
					break;
				}else if (decoderOutputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
					if (VERBOSE)Log.d(TAG, "audio decoder: output buffers changed");
					audioDecoderOutputBuffers = audioDecoder.getOutputBuffers();
					break;
				}
				
				if (VERBOSE) {
					Log.d(TAG, "audio decoder: returned output buffer: "
							+ decoderOutputBufferIndex);
					Log.d(TAG, "audio decoder: returned buffer of size "
							+ audioDecoderOutputBufferInfo.size);
					Log.d(TAG, "audio decoder: returned buffer for time "
							+ audioDecoderOutputBufferInfo.presentationTimeUs);
				}
				
				if ((audioDecoderOutputBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
					if (VERBOSE)Log.d(TAG, "audio decoder: codec config buffer");
					audioDecoder.releaseOutputBuffer(decoderOutputBufferIndex,false);
					break;
				}
				
				if (VERBOSE) {
					Log.d(TAG, "audio decoder: output buffer is now pending: "
							+ decoderOutputBufferIndex);
				}
				pendingAudioDecoderOutputBufferIndex = decoderOutputBufferIndex;
				audioDecodedFrameCount++;
				// We extracted a pending frame, let's try something else next.
				break;
			}

			// Feed the pending decoded audio buffer to the audio encoder.
			while (mCopyAudio && pendingAudioDecoderOutputBufferIndex != -1) {
				if (VERBOSE) {
					Log.d(TAG,"audio decoder: attempting to process pending buffer: "+ pendingAudioDecoderOutputBufferIndex);
				}
				int encoderInputBufferIndex = audioEncoder.dequeueInputBuffer(TIMEOUT_USEC);
				if (encoderInputBufferIndex <= MediaCodec.INFO_TRY_AGAIN_LATER) {
					if (VERBOSE)Log.d(TAG, "no audio encoder input buffer: "+encoderInputBufferIndex);
					break;
				}
				if (VERBOSE) {
					Log.d(TAG, "audio encoder: returned input buffer: "+ encoderInputBufferIndex);
				}
				ByteBuffer encoderInputBuffer = audioEncoderInputBuffers[encoderInputBufferIndex];
				int size = audioDecoderOutputBufferInfo.size;
				long presentationTime = audioDecoderOutputBufferInfo.presentationTimeUs;
				if (VERBOSE) {
					Log.d(TAG, "audio decoder: processing pending buffer: "+ pendingAudioDecoderOutputBufferIndex);
					Log.d(TAG, "audio decoder: pending buffer of size " + size);
					Log.d(TAG, "audio decoder: pending buffer for time "+ presentationTime);
				}
				if (size >= 0) {
					try {
						ByteBuffer decoderOutputBuffer = audioDecoderOutputBuffers[pendingAudioDecoderOutputBufferIndex]
								.duplicate();
						decoderOutputBuffer
								.position(audioDecoderOutputBufferInfo.offset);
						decoderOutputBuffer
								.limit(audioDecoderOutputBufferInfo.offset + size);
						encoderInputBuffer.position(0);
						encoderInputBuffer.put(decoderOutputBuffer);
						audioEncoder.queueInputBuffer(encoderInputBufferIndex, 0,
								audioDecoderOutputBufferInfo.offset + size, presentationTime,
								audioDecoderOutputBufferInfo.flags);
						
					} catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
					}
					
				}				
				audioDecoder.releaseOutputBuffer(pendingAudioDecoderOutputBufferIndex, false);
				pendingAudioDecoderOutputBufferIndex = -1;
				if ((audioDecoderOutputBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
					if (VERBOSE)Log.d(TAG, "audio decoder: EOS");
					audioDecoderDone = true;
				}
				// We enqueued a pending frame, let's try something else next.
				break;
			}
			
			// Poll frames from the audio encoder and send them to the muxer.
			while (mCopyAudio && !audioEncoderDone
					&& (encoderOutputAudioFormat == null || muxing)) {
				int encoderOutputBufferIndex = audioEncoder
						.dequeueOutputBuffer(audioEncoderOutputBufferInfo,TIMEOUT_USEC);
				if (encoderOutputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
					if (VERBOSE)Log.d(TAG, "no audio encoder output buffer");
					break;
				}else if (encoderOutputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
					if (VERBOSE)Log.d(TAG, "audio encoder: output format changed");
					if (outputAudioTrack >= 0) {
						// fail("audio encoder changed its output format again?");
						Log.d(TAG,"audio encoder changed its output format again?");
					}
					encoderOutputAudioFormat = audioEncoder.getOutputFormat();
					break;
				}else if (encoderOutputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
					if (VERBOSE)Log.d(TAG, "audio encoder: output buffers changed");
					audioEncoderOutputBuffers = audioEncoder.getOutputBuffers();
					break;
				}
				// assertTrue("should have added track before processing output",muxing);
				if (VERBOSE) {
					Log.d(TAG, "audio encoder: returned output buffer: "
							+ encoderOutputBufferIndex);
					Log.d(TAG, "audio encoder: returned buffer of size "
							+ audioEncoderOutputBufferInfo.size);
				}
				ByteBuffer encoderOutputBuffer = audioEncoderOutputBuffers[encoderOutputBufferIndex];
				if ((audioEncoderOutputBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
					if (VERBOSE)Log.d(TAG, "audio encoder: codec config buffer");
					// Simply ignore codec config buffers.
					mAudioConfig = true;
					audioEncoder.releaseOutputBuffer(encoderOutputBufferIndex,false);
					break;
				}
				if (VERBOSE) {
					Log.d(TAG, " audio encoder: returned buffer for time "
							+ audioEncoderOutputBufferInfo.presentationTimeUs);
				}
				
				if(mAudioConfig){
					if(!mainAudioFrame){
						mLastAudioSampleTime = audioEncoderOutputBufferInfo.presentationTimeUs;
						mainAudioFrame = true;
					}else{
						if(mAudioSampleTime == 0){
							mAudioSampleTime = audioEncoderOutputBufferInfo.presentationTimeUs - mLastAudioSampleTime;
						}
					}
				}
				
				audioEncoderOutputBufferInfo.presentationTimeUs = mLastAudioSampleTime + mAudioSampleTime;
				if (audioEncoderOutputBufferInfo.size != 0) {
					muxer.writeSampleData(outputAudioTrack,
							encoderOutputBuffer, audioEncoderOutputBufferInfo);
					mLastAudioSampleTime = audioEncoderOutputBufferInfo.presentationTimeUs;
				}
				
				if ((audioEncoderOutputBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
					if (VERBOSE)Log.d(TAG, "audio encoder: EOS");
					audioEncoderDone = true;
				}
				audioEncoder.releaseOutputBuffer(encoderOutputBufferIndex,false);
				audioEncodedFrameCount++;
				// We enqueued an encoded frame, let's try something else next.
				break;
			}

			if (!muxing && (!mCopyAudio || encoderOutputAudioFormat != null)
					&& (!mCopyVideo || encoderOutputVideoFormat != null)) {
				if (mCopyVideo) {
					Log.d(TAG, "muxer: adding video track.");
					outputVideoTrack = muxer.addTrack(encoderOutputVideoFormat);
				}
				if (mCopyAudio) {
					Log.d(TAG, "muxer: adding audio track.");
					outputAudioTrack = muxer.addTrack(encoderOutputAudioFormat);
				}
				Log.d(TAG, "muxer: starting");
				muxer.start();
				muxing = true;
			}
		}
		Log.d(TAG, "exit looper");
	}

	private static boolean isVideoFormat(MediaFormat format) {
		return getMimeTypeFor(format).startsWith("video/");
	}

	private static boolean isAudioFormat(MediaFormat format) {
		return getMimeTypeFor(format).startsWith("audio/");
	}

	private static String getMimeTypeFor(MediaFormat format) {
		return format.getString(MediaFormat.KEY_MIME);
	}

	/**
	 * Returns the first codec capable of encoding the specified MIME type, or
	 * null if no match was found.
	 */
	private static MediaCodecInfo selectCodec(String mimeType) {
		int numCodecs = MediaCodecList.getCodecCount();
		for (int i = 0; i < numCodecs; i++) {
			MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);

			if (!codecInfo.isEncoder()) {
				continue;
			}

			String[] types = codecInfo.getSupportedTypes();
			for (int j = 0; j < types.length; j++) {
				if (types[j].equalsIgnoreCase(mimeType)) {
					return codecInfo;
				}
			}
		}
		return null;
	}
	
	//do what for video yuv
	public static void NV21toI420SemiPlanar(byte[] nv21bytes, byte[] i420bytes,
			int width, int height) {
		System.arraycopy(nv21bytes, 0, i420bytes, 0, width * height);
		for (int i = width * height; i < nv21bytes.length; i += 2) {
			i420bytes[i] = nv21bytes[i + 1];
			i420bytes[i + 1] = nv21bytes[i];
		}
	}
	
	//do what for audio pcm
	private void addADTStoPacket(byte[] packet, int packetLen) {
		int profile = 2; // AAC LC
		int freqIdx = 4; // 44.1KHz
		int chanCfg = 2; // CPE

		// fill in ADTS data
		packet[0] = (byte) 0xFF;
		packet[1] = (byte) 0xF9;
		packet[2] = (byte) (((profile - 1) << 6) + (freqIdx << 2) + (chanCfg >> 2));
		packet[3] = (byte) (((chanCfg & 3) << 6) + (packetLen >> 11));
		packet[4] = (byte) ((packetLen & 0x7FF) >> 3);
		packet[5] = (byte) (((packetLen & 7) << 5) + 0x1F);
		packet[6] = (byte) 0xFC;
	}
	
	private void showSupportedColorFormat(MediaCodecInfo.CodecCapabilities caps) {
	    for (int c : caps.colorFormats) {
	    	if(VERBOSE)Log.d(TAG,"supported color format: " + c);
	    }
	} 
	
	public void reset(){
		this.interrupted = true;
	}

}
