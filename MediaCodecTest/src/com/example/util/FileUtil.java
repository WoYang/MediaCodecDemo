package com.example.util;

import java.io.File;
import java.util.ArrayList;

public class FileUtil {
	private final static String TAG = FileUtil.class.getSimpleName();

	private static ArrayList<String> filelist = new ArrayList<String>();

	public static ArrayList<String> getFileList(String path) {

		if (path != null && !"".equals(path)) {
			File root = new File(path);
			File[] files = root.listFiles();
			if (files != null) {
				for (File file : files) {
					if (file.isDirectory()) {
						getFileList(file.getPath());
					} else {
						filelist.add(file.getAbsolutePath());
					}
				}

			}
		}
		return filelist;
	}
}
