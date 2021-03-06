package com.example.mediacodectest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;

import com.example.mediacodectest2.DecodeEncodeMuxTest;
import com.example.mediacodectest2.DecodeEncodeMuxTest.CodecCallBack;

public class TestActivity extends Activity{
	private Surface mSurface = null;
	private TextView mStatus = null;
	String test_url = null;
	
	VideoDecoder mVideoDecoder = null;
	AudioDecoder mAudioDecoder = null;

	DecodeEncodeMuxTest mDecodeEncode = null;
	
	Handler mainHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 0:
				if (msg.arg1 == CodecCallBack.STATUS_FAIL) {
					mStatus.setText(R.string.codec_fail);
				} else if (msg.arg1 == CodecCallBack.STATUS_SUCCESS) {
					mStatus.setText(R.string.codec_success);
				}
				break;

			default:
				break;
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_test);
		mStatus = (TextView) findViewById(R.id.status);
		
		Intent data = getIntent();
		if(data != null){
			test_url = data.getStringExtra("url");
		}
		
		SurfaceView surfaceView = (SurfaceView) findViewById(R.id.video);
		SurfaceHolder mHolder = surfaceView.getHolder();
		mHolder.addCallback(surface_callback);
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		Log.d(TestActivity.class.getSimpleName(), "onPause");
		super.onPause();
	}
	
	@Override
	public boolean onKeyDown(int arg0, KeyEvent arg1) {
		// TODO Auto-generated method stub
		switch (arg1.getKeyCode()) {
		case KeyEvent.KEYCODE_BACK:
			if(mVideoDecoder != null){
				mVideoDecoder.reset();
			}
			if(mAudioDecoder != null){
				mAudioDecoder.reset();
			}
			if(mDecodeEncode != null){
				mDecodeEncode.reset();
			}
			finish();
			break;
		default:
			break;
		}
		return super.onKeyDown(arg0, arg1);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	SurfaceHolder.Callback surface_callback = new SurfaceHolder.Callback() {

		@Override
		public void surfaceDestroyed(SurfaceHolder arg0) {
			// TODO Auto-generated method stub
		}

		@Override
		public void surfaceCreated(SurfaceHolder arg0) {
			// TODO Auto-generated method stub
			mSurface = arg0.getSurface();
		}

		@Override
		public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2,
				int arg3) { // TODO Auto-generated method stub

		}
	};

	public void onConvertVideoTest(View arg0){
		mStatus.setText(R.string.codec_execute);
		testcodec(true,false);
	}
	
	public void onConvertAudioTest(View arg0){
		mStatus.setText(R.string.codec_execute);
		testcodec(false,true);
	}
	
	public void onConvertTest(View arg0){
		mStatus.setText(R.string.codec_execute);
		testcodec(true,true);
	}
	
	public void onDecodeTest(View arg0){
		 mStatus.setVisibility(View.GONE);
		 testcodec2();
	}

	public void testcodec(final boolean encode_video,final boolean encode_audio) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					mDecodeEncode = new DecodeEncodeMuxTest(test_url,
							callback,encode_video,encode_audio);
					mDecodeEncode.testExtractDecodeEncodeMuxAudioVideo();
				} catch (Throwable e) {
					// TODO Auto-generated catch block
					callback.onCallBack(CodecCallBack.STATUS_FAIL);
				}
			}
		}).start();
	}

	public void testcodec2() {		
		mVideoDecoder = new VideoDecoder(test_url, mSurface,
				null);
		mAudioDecoder = new AudioDecoder(test_url, mSurface,
				null);
		mVideoDecoder.start();
		mAudioDecoder.start();
	}

	CodecCallBack callback = new CodecCallBack() {

		@Override
		public void onCallBack(int status) {
			// TODO Auto-generated method stub
			Message msg = mainHandler.obtainMessage();
			msg.what = 0;
			msg.arg1 = status;
			mainHandler.sendMessage(msg);
		}
	};

}
