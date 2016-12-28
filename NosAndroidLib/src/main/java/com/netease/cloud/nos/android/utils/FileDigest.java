package com.netease.cloud.nos.android.utils;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;
import java.security.DigestInputStream;


public class FileDigest {
	/** 
	 * Convert the hash bytes to hex digits string 
	 * @param hashBytes 
	 * @return The converted hex digits string 
	 */  
	private static String byteArrayToHex(byte[] hashBytes) {  
	    String returnVal = "";  
	    for (int i = 0; i < hashBytes.length; i++) {  
	        returnVal += Integer.toString(( hashBytes[i] & 0xff) + 0x100, 16).substring(1);  
	    }  
	    return returnVal.toLowerCase();  
	}  
	
	
	public static String getFileMD5(File file) {
		// 缓冲区大小（这个可以抽出一个参数）
		int bufferSize = 512 * 1024;
		FileInputStream fileInputStream = null;
		DigestInputStream digestInputStream = null;

		try {
			// 拿到一个MD5转换器（同样，这里可以换成SHA1）
			MessageDigest messageDigest = MessageDigest.getInstance("MD5");
			// 使用DigestInputStream
			fileInputStream = new FileInputStream(file);
			digestInputStream = new DigestInputStream(fileInputStream, messageDigest);

			// read的过程中进行MD5处理，直到读完文件
			byte[] buffer = new byte[bufferSize];
			while (digestInputStream.read(buffer) > 0)
				;

			// 获取最终的MessageDigest
			messageDigest = digestInputStream.getMessageDigest();

			// 拿到结果，也是字节数组，包含16个元素
			byte[] resultByteArray = messageDigest.digest();

			// 同样，把字节数组转换成字符串
			return byteArrayToHex(resultByteArray);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			try {
				if (digestInputStream != null)
					digestInputStream.close();
				
				if (fileInputStream != null)
					fileInputStream.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
