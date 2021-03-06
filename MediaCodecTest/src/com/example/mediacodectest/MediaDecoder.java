package com.example.mediacodectest;

import java.io.File;
import java.io.IOException;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.view.Surface;

public class MediaDecoder extends Thread {

	protected String mVideoFilePath = null;

	public static final long TIME_US = 10000;

	protected MediaExtractor mExtractor = null;
	protected MediaCodec mDecoder = null;
	protected MediaFormat mediaFormat;
	protected UpstreamCallback mCallback;
	protected Surface mSurface;
	
	protected boolean interrupted = false;

	public MediaDecoder(String videoFilePath, Surface surface,
			UpstreamCallback callback) {
		this.mVideoFilePath = videoFilePath;
		this.mSurface = surface;
		this.mCallback = callback;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
		prepare();
	}

	public void prepare() {
		try {
			mExtractor = new MediaExtractor();
			if (mVideoFilePath != null && mVideoFilePath.startsWith("http")) {

				mExtractor.setDataSource(mVideoFilePath.toString());
			} else {
				File videoFile = new File(mVideoFilePath);
				mExtractor.setDataSource(videoFile.toString());
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void reset(){
		this.interrupted = true;
		this.interrupt();
		this.yield();
	}
	
}
