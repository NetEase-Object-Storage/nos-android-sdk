package com.netease.cloud.nos.android.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class FileInput {

	private static final String LOGTAG = LogUtil.makeLogTag(FileInput.class);

	private final RandomAccessFile randomAccessFile;
	private final File file;
	private final String filename;

	public FileInput(File file) throws FileNotFoundException {
		this(file, null);
	}

	public FileInput(File file, String aliasFilename)
			throws FileNotFoundException {
		this.file = file;
		this.randomAccessFile = new RandomAccessFile(file, "r");
		this.filename = (aliasFilename != null && aliasFilename.trim().length() > 0) ? aliasFilename
				: file.getName();
	}

	public long length() {
		return file.length();
	}

	public String getFilename() {
		return filename;
	}

	public void doClose() {
		if (randomAccessFile != null) {
			try {
				randomAccessFile.close();
			} catch (IOException e) {
				LogUtil.e(LOGTAG, "close file exception", e);
			}
		}
	}

	public byte[] read(long offset, int len) throws IOException {
		if (offset == 0 && len == 0 && length() == 0) {
			return new byte[0];
		}
		
		if (offset >= length()) {
			return null;
		}
		byte[] bs = new byte[len];
		randomAccessFile.seek(offset);
		randomAccessFile.read(bs);
		offset += len;
		return bs;
	}

	public void delete() {
		if (file != null) {
			file.delete();
		}
	}

}
