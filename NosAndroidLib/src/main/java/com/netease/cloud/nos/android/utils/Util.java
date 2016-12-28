package com.netease.cloud.nos.android.utils;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.NoHttpResponseException;

import java.net.SocketTimeoutException;

import javax.net.ssl.SSLException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.netease.cloud.nos.android.constants.Code;
import com.netease.cloud.nos.android.constants.Constants;
import com.netease.cloud.nos.android.core.Callback;
import com.netease.cloud.nos.android.core.WanNOSObject;
import com.netease.cloud.nos.android.core.WanAccelerator;
import com.netease.cloud.nos.android.exception.InvalidParameterException;
import com.netease.cloud.nos.android.http.HttpResult;
import com.netease.cloud.nos.android.pipeline.PipelineHttpSession;
import com.netease.cloud.nos.android.utils.ValidIP;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Base64;
import io.netty.handler.codec.http.DefaultFullHttpRequest;

public class Util {

	private static final String LOGTAG = LogUtil.makeLogTag(Util.class);

	public static void setData(Context ctx, String key, String value) {
		SharedPreferences mPerferences = getDefaultPreferences(ctx);
		SharedPreferences.Editor mEditor = mPerferences.edit();
		mEditor.putString(key, value);
		mEditor.commit();
	}

	public static String getData(Context ctx, String key) {
		SharedPreferences mPerferences = getDefaultPreferences(ctx);
		return mPerferences.getString(key, null);
	}

	public static HttpPost newPost(String url) {
		return new HttpPost(url);
	}

	public static HttpGet newGet(String url) {
		return new HttpGet(url);
	}

	public static HttpClient getHttpClient(Context ctx) {
		return Http.getHttpClient(ctx);
	}

	public static HttpClient getLbsHttpClient(Context ctx) {
		return Http.getLbsHttpClient(ctx);
	}

	private static SharedPreferences getDefaultPreferences(Context ctx) {
		return PreferenceManager.getDefaultSharedPreferences(ctx);
	}

	public static CountDownLatch acquireLock() {
		CountDownLatch latch = new CountDownLatch(1);
		return latch;
	}

	public static void setLock(CountDownLatch latch) {
		try {
			latch.await();
		} catch (InterruptedException e) {
			LogUtil.e(LOGTAG, "set lock with interrupted exception", e);
		}
	}

	public static void releaseLock(CountDownLatch latch) {
		latch.countDown();
	}

	public static int getIntData(Context ctx, String key) {
		SharedPreferences mPerferences = getDefaultPreferences(ctx);
		return mPerferences.getInt(key, 0);  
	}

	public static void setBooleanData(Context ctx, String key, boolean value) {
		SharedPreferences mPerferences = getDefaultPreferences(ctx);
		SharedPreferences.Editor mEditor = mPerferences.edit();
		mEditor.putBoolean(key, value);
		mEditor.commit();
	}

	public static boolean getBooleanData(Context ctx, String key) {
		SharedPreferences mPerferences = getDefaultPreferences(ctx);
		return mPerferences.getBoolean(key, false);  
	}

	public static void setLongData(Context ctx, String key, Long value) {
		SharedPreferences mPerferences = getDefaultPreferences(ctx);
		SharedPreferences.Editor mEditor = mPerferences.edit();
		mEditor.putLong(key, value);
		mEditor.commit();
	}

	public static long getLongData(Context ctx, String key) {
		SharedPreferences mPerferences = getDefaultPreferences(ctx);
		return mPerferences.getLong(key, 0);  
	}

	public static void setBucketName(Context ctx, String bucketName) {
	    SharedPreferences mPerferences = getDefaultPreferences(ctx);
	    int num = mPerferences.getInt(Constants.BUCKET_NUMBER, 0);  

	    for (int i=0; i<num; i++) {
	        String bucket = mPerferences.getString(Constants.BUCKET_NAME + i, null);
	        if (bucket.equals(bucketName)) {
	        	return;
	        }
	    }		
		
		SharedPreferences.Editor mEditor = mPerferences.edit();
		mEditor.putString(Constants.BUCKET_NAME + num, bucketName);
		mEditor.putInt(Constants.BUCKET_NUMBER, num+1); 
		mEditor.commit();
	}
	
	public static HttpResult setLBSData(Context ctx, String bucketName, JSONObject rs) {
		try {
			String lbsString = rs.getString("lbs");
			JSONArray uploadArray = rs.getJSONArray("upload");
			String uploadString = transformString(uploadArray);
			LogUtil.d(LOGTAG, "lbsString: " + lbsString);
			LogUtil.d(LOGTAG, "upload server string: " + uploadString);
			if (lbsString != null) {
				Util.setData(ctx, bucketName + Constants.LBS_KEY, lbsString);
			}
			if (uploadString != null) {
				String httpsUploadString = replaceWithHttps(uploadString);
				LogUtil.d(LOGTAG, "https servers: " + httpsUploadString);
				Util.setData(ctx, bucketName + Constants.UPLOAD_SERVER_KEY, uploadString);
				Util.setData(ctx, bucketName + Constants.HTTPS_UPLOAD_SERVER_KEY, httpsUploadString);
				// set LBS query time
				Util.setLongData(ctx, bucketName + Constants.LBS_TIME, System.currentTimeMillis());
				Util.setBooleanData(ctx, bucketName + Constants.LBS_STATUS, true);
			}

			// save bucketName
			setBucketName(ctx, bucketName);
			return new HttpResult(Code.HTTP_SUCCESS, rs, null);
		} catch (JSONException e) {
			LogUtil.e(LOGTAG, "get json array exception", e);
			return new HttpResult(Code.INVALID_LBS_DATA, rs, null);
		}
	}

	public static String[] getUploadServer(Context ctx, String bucketName, boolean isHttps) {
		String str = null;
		if (!isHttps) {
			str = getData(ctx, bucketName + Constants.UPLOAD_SERVER_KEY);
		} else {
			str = getData(ctx, bucketName + Constants.HTTPS_UPLOAD_SERVER_KEY);
		}
		if (str == null) {
			return null;
		} else {
			return str.split(";");
		}
	}

	public static String buildLBSUrl(String url, String bucketName) {
		LogUtil.d(LOGTAG, "query lbs url: " + url);
		return url + "?version=" + Constants.LBS_VERSION + "&bucketname=" + bucketName;
	}

	public static String buildQueryUrl(String server, String bucketName,
			String fileName, String context)
			throws UnsupportedEncodingException {
		String queryString = null;
		if (context != null) {
			queryString = encode(bucketName) + "/" + encode(fileName)
					+ "?uploadContext&version=" + Constants.UPLOAD_VERSION
					+ "&context=" + context;
		} else {
			queryString = encode(bucketName) + "/" + encode(fileName)
					+ "?uploadContext&version=" + Constants.UPLOAD_VERSION;
		}
		return server + "/" + queryString;
	}

	public static String buildPostDataUrl(String server, String bucketName,
			String fileName, String context, long offset, boolean isLast)
			throws UnsupportedEncodingException {
		String queryString = null;
		if (context != null) {
			queryString = encode(bucketName) + "/" + encode(fileName)
					+ "?version=" + Constants.UPLOAD_VERSION + "&context="
					+ context + "&offset=" + offset + "&complete=" + isLast;
		} else {
			queryString = encode(bucketName) + "/" + encode(fileName)
					+ "?version=" + Constants.UPLOAD_VERSION + "&offset="
					+ offset + "&complete=" + isLast;
		}
		LogUtil.d(LOGTAG, "post data url server: " + server
				+ ", query string: " + queryString);
		return server + "/" + queryString;
	}

	public static HttpRequestBase setHeader(HttpRequestBase request,
			Map<String, String> map) {
		if (map == null) {
			return request;
		}
		Set<String> keys = map.keySet();
		for (String s : keys) {
			request.addHeader(s, map.get(s));
		}
		return request;
	}

	public static File getSDPath(Context context) {
		File sdDir = context.getCacheDir();
		boolean sdCardExist = Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED);
		if (sdCardExist) {
			sdDir = Environment.getExternalStorageDirectory();
		}
		return sdDir;
	}

	public static FileInput fromInputStream(Context context, File file,
			String filename) throws IOException {
		// InputStream is = new FileInputStream(f);
		// final File file = storeToFile(context, is);
		if (file == null) {
			return null;
		}
		FileInput isa = null;
		try {
			isa = new FileInput(file, filename);
		} catch (IOException e) {
		/*	if (file != null) {  // ldm：这里可不能删，这可是用户文件
				file.delete();
			}*/
			throw e;
		}
		return isa;
	}

	public static ExecutorService getExecutorService() {
		return Executors.newSingleThreadExecutor();
	}

	public static String getToken(String bucket, String object, long expires,
			String accessKey, String secretKey) throws NoSuchAlgorithmException,
			InvalidKeyException, JSONException {
		String token = "";

		JSONObject jsonObject = new JSONObject();
		if (bucket != null) {
			jsonObject.put("Bucket", bucket);
		}
		if (object != null) {
			jsonObject.put("Object", object);
		}
		if (expires != 0) {
			jsonObject.put("Expires", expires);
		}

		String jsonString = jsonObject.toString();
		String encodedPolicy = new String(Base64.encode(jsonString.getBytes(),
				Base64.NO_WRAP));
		SecretKeySpec signingKey = new SecretKeySpec(secretKey.getBytes(),
				"HmacSHA256");
		Mac mac = Mac.getInstance("HmacSHA256");
		mac.init(signingKey);
		byte[] signedPolicy = mac.doFinal(encodedPolicy.getBytes());

		String encodedSign = new String(Base64.encode(signedPolicy,
				Base64.NO_WRAP));

		token = "UPLOAD " + accessKey + ":" + encodedSign + ":" + encodedPolicy;
		return token;
	}

	public static int transformCode(int code) {
		switch (code) {
		case Constants.CODE_SERVER_ERROR:
			return Code.SERVER_ERROR;
		case Constants.CODE_HTTP_EXCEPTION:
			return Code.HTTP_EXCEPTION;
		case Constants.CODE_NO_RESPONSE:
			return Code.HTTP_NO_RESPONSE;
		case Constants.CODE_INVALID_TOKEN:
			return Code.INVALID_TOKEN;
		case Constants.CODE_RETRY:
			return Code.UNKNOWN_REASON;
		}
		return Code.UNKNOWN_REASON;
	}

	public static void deleteTempFiles(Context context) {
		File outputDir = getSDPath(context);
		File dir = new File(outputDir.getPath() + Constants.TEMP_FILE);
		if (dir.exists()) {
			File[] list = dir.listFiles();
			if (list != null) {
				for (File f : list) {
					f.delete();
				}
			}
		}
	}

	public static String getIPAddress() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf
						.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()
							&& (inetAddress instanceof Inet4Address)) {
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (SocketException ex) {
			LogUtil.e(LOGTAG, "get ip address socket exception");
		}
		return "";
	}

	public static String getMonitorUrl(String url) {
		return url + "/stat/sdk?version=1.0";
	}

	public static long ipToLong(String strIp) {
		if (strIp == null || strIp.equals("")) {
			return 0L;
		}
		long[] ip = new long[4];
		int position1 = strIp.indexOf(".");
		int position2 = strIp.indexOf(".", position1 + 1);
		int position3 = strIp.indexOf(".", position2 + 1);
		ip[0] = Long.parseLong(strIp.substring(0, position1));
		ip[1] = Long.parseLong(strIp.substring(position1 + 1, position2));
		ip[2] = Long.parseLong(strIp.substring(position2 + 1, position3));
		ip[3] = Long.parseLong(strIp.substring(position3 + 1));
		return (ip[0] << 24) + (ip[1] << 16) + (ip[2] << 8) + ip[3];
	}

	public static String getIPString(String srcIp) {
		if (srcIp == null || srcIp.equals("")) {
			return "";
		}
		if (srcIp.startsWith("https")) {
			srcIp = srcIp.replaceAll("https://", "");
		}
		if (srcIp.startsWith("http")) {
			srcIp = srcIp.replaceAll("http://", "");
		}
		String regexString = "^(\\d{1,3}(\\.\\d{1,3}){3}).*";
		String IPString = srcIp.replaceAll(regexString, "$1");
		return IPString;
	}

	public static boolean isValidLbsIP(String srcIp) {
		if (srcIp == null || srcIp.equals("")) {
			return false;
		}
		if (srcIp.startsWith("https://")) {
			srcIp = srcIp.replaceFirst("https://", "");
		} else if (srcIp.startsWith("http://")) {
			srcIp = srcIp.replaceFirst("http://", "");
		} else {
			return false;
		}

		if (!srcIp.endsWith("/lbs")) {
			return false;
		}
		
		srcIp = srcIp.replaceFirst("/lbs", "");

		if (srcIp.equals("0.0.0.0") || srcIp.equals("255.255.255.255")) {
			return false;
		}
		
		return ValidIP.validate(srcIp); 
	}
	
	
	public static void addHeaders(HttpPost post, WanNOSObject data) {
		if (data.getContentType() != null && !data.getContentType().equals("")) {
			post.addHeader("Content-Type", data.getContentType());
		}
		if (data.getUserMetadata() != null && data.getUserMetadata().size() > 0) {
			Map<String, String> userMap = data.getUserMetadata();
			for (String key : userMap.keySet()) {
				post.addHeader("x-nos-meta-" + key, userMap.get(key));
			}
		}
	}

	public static String getResultString(HttpResult result, String str) {
		String rs = "";
		if (result != null && result.getMsg() != null
				&& result.getMsg().has(str)) {
			try {
				rs = result.getMsg().getString(str);
			} catch (JSONException e) {
				LogUtil.e(LOGTAG, "get result string parse json failed", e);
			}
		}
		return rs;
	}

	public static void checkParameters(Context context, File file,
			Object fileParam, WanNOSObject obj, Callback callback)
			throws InvalidParameterException {
		String uploadToken = obj.getUploadToken();
		String nosBucketName = obj.getNosBucketName();
		String nosObjectName = obj.getNosObjectName();
		if (context == null || file == null || fileParam == null || obj == null
				|| callback == null || uploadToken == null
				|| nosBucketName == null || nosObjectName == null) {
			throw new InvalidParameterException("parameters could not be null");
		}
	}

	private static String transformString(JSONArray array) {
		if (array == null || array.length() == 0) {
			return null;
		}
		String str = "";
		try {
			for (int i = 0; i < array.length(); i++) {
				str += array.getString(i);
				if (i != array.length() - 1) {
					str += ";";
				}
			}
		} catch (JSONException e) {
			LogUtil.e(LOGTAG, "get json string exception", e);
		}
		return str;
	}

	private static String replaceWithHttps(String str) {
		return str.replaceAll("http://", "https://");
	}

	private static String encode(String str)
			throws UnsupportedEncodingException {
		return URLEncoder.encode(str, WanAccelerator.getConf().getCharset());
	}

	/* API for pipeline */

	public static String pipeBuildQueryUrl(String bucketName,
			String fileName, String context)
			throws UnsupportedEncodingException {
		String queryString = null;
		if (context != null) {
			queryString = encode(bucketName) + "/" + encode(fileName)
					+ "?uploadContext&version=" + Constants.UPLOAD_VERSION
					+ "&context=" + context;
		} else {
			queryString = encode(bucketName) + "/" + encode(fileName)
					+ "?uploadContext&version=" + Constants.UPLOAD_VERSION;
		}
		return "/" + queryString;
	}


	public static String pipeBuildPostDataUrl(String bucketName,
			String fileName, String context, long offset, boolean isLast)
			throws UnsupportedEncodingException {
		String queryString = null;
		if (context != null) {
			queryString = encode(bucketName) + "/" + encode(fileName)
					+ "?version=" + Constants.UPLOAD_VERSION + "&context="
					+ context + "&offset=" + offset + "&complete=" + isLast;
		} else {
			queryString = encode(bucketName) + "/" + encode(fileName)
					+ "?version=" + Constants.UPLOAD_VERSION + "&offset="
					+ offset + "&complete=" + isLast;
		}

		return "/" + queryString;
	}


	public static void pipeAddHeaders(DefaultFullHttpRequest request, WanNOSObject data) {
		if (data.getContentType() != null && !data.getContentType().equals("")) {
			request.headers().add("Content-Type", data.getContentType());
		}
		if (data.getUserMetadata() != null && data.getUserMetadata().size() > 0) {
			Map<String, String> userMap = data.getUserMetadata();
			for (String key : userMap.keySet()) {
				request.headers().add("x-nos-meta-" + key, userMap.get(key));
			}
		}
	}

	public static int getHttpCode(HttpResult httpResult) {
		int code = httpResult.getStatusCode();
		Exception e = null;

		if (code == Code.HTTP_SUCCESS) {
			return code;
		}
		
		if ((e = httpResult.getException()) == null) {
			return code;
		}

		if (e instanceof ConnectTimeoutException) {
			LogUtil.d(LOGTAG, "connection timeout Exception:" + e.getMessage());
			return Code.CONNECTION_TIMEOUT;
		} else if (e instanceof SocketTimeoutException) {
			LogUtil.d(LOGTAG, "Read Socket Timeout Exception:" + e.getMessage());
			return Code.SOCKET_TIMEOUT;
		} else if (e instanceof NoHttpResponseException) {
			LogUtil.d(LOGTAG, "No HttpResponse Exception:" + e.getMessage());
			return Code.HTTP_NO_RESPONSE;
		} else if (e instanceof SSLException) {
			LogUtil.d(LOGTAG, "SSL Exception:" + e.getMessage());
			return Code.SSL_FAILED;
		} else if (e instanceof SocketException) {
			LogUtil.d(LOGTAG, "Socket Exception" + e.getMessage());
			String errStr = e.getMessage().toLowerCase();
			if (errStr.contains("refused")) {
				return Code.CONNECTION_REFUSED;
			} else if (errStr.contains("reset")) {
				return Code.CONNECTION_RESET;
			}
		} else if (e instanceof JSONException) {
			LogUtil.d(LOGTAG, "JSON Exception" + e.getMessage());
			return Code.INVALID_RESPONSE_DATA;
		}

		return code;
	}

	// notify sdk network state change
	public static void netStateChange(Context context) {

		LogUtil.d(LOGTAG, "network connection change");
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = cm.getActiveNetworkInfo();
		if (cm != null && networkInfo != null
				&& networkInfo.isAvailable()
				&& networkInfo.isConnected()) {
			NetworkType netType = NetworkType.getNetWorkType(context);
			int bucketNum = Util.getIntData(context, Constants.BUCKET_NUMBER);
			LogUtil.d(LOGTAG, "bucketNum =" + bucketNum +
					", netType = " + netType.getNetworkType());
			for (int i = 0; i < bucketNum; i++) {
				String bucketName = Util.getData(context, Constants.BUCKET_NAME + i);
				if (bucketName != null) {
					Util.setBooleanData(context, bucketName + Constants.LBS_STATUS, false);
					Util.setData(context, bucketName + Constants.NET_TYPE, netType.getNetworkType());
				}
			}
			
			// restart pipeline
			PipelineHttpSession.reStart();
		}

	}

}
