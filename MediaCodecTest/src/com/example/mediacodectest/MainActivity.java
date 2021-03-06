package com.example.mediacodectest;

import java.util.ArrayList;

import com.example.util.FileUtil;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

public class MainActivity extends Activity {
	private final static String TAG = MainActivity.class.getSimpleName();

	private GridView fileListView = null;
	private FilesAdpater adapter = null;

	String root_path = "/storage/external_storage/sda1/联通2016集采_统一片源";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		fileListView = (GridView) findViewById(R.id.gridView);

		initChannelAdapter();
	}

	private void initChannelAdapter() {
		ArrayList<String> filelist = FileUtil.getFileList(root_path);
		adapter = new FilesAdpater(filelist);
		fileListView.setAdapter(adapter);
		fileListView.setOnItemClickListener(listener);
		adapter.notifyDataSetChanged();
	}

	public class FilesAdpater extends BaseAdapter {
		ArrayList<String> filelist = null;

		public FilesAdpater(ArrayList<String> list) {
			filelist = list;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return filelist.size();
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return filelist.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return arg0;
		}

		@Override
		public View getView(int arg0, View arg1, ViewGroup arg2) {
			// TODO Auto-generated method stub
			View view = View.inflate(MainActivity.this, R.layout.gridview_item,
					null);
			if (arg0 > filelist.size()) {

			} else {
				String full_path = filelist.get(arg0);
				TextView item = (TextView) view.findViewById(R.id.file);
				item.setText(full_path.replace(root_path, ""));
				view.setTag(full_path);
			}
			return view;
		}

	}

	OnItemClickListener listener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			// TODO Auto-generated method stub;
			String url = (String) arg1.getTag();
			Intent intent = new Intent(MainActivity.this,TestActivity.class);
			intent.putExtra("url", url);
			startActivity(intent);
		}

	};

}
