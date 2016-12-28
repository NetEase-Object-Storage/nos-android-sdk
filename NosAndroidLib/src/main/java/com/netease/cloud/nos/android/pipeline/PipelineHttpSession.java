package com.netease.cloud.nos.android.pipeline;

import java.io.File;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import com.netease.cloud.nos.android.utils.LogUtil;
import com.netease.cloud.nos.android.utils.Util;
import com.netease.cloud.nos.android.constants.Code;
import com.netease.cloud.nos.android.constants.Constants;
import com.netease.cloud.nos.android.core.Callback;
import com.netease.cloud.nos.android.core.UploadTask;
import com.netease.cloud.nos.android.core.WanAccelerator;
import com.netease.cloud.nos.android.core.WanNOSObject;
import com.netease.cloud.nos.android.exception.InvalidOffsetException;
import com.netease.cloud.nos.android.http.HttpResult;

import io.netty.channel.ChannelFuture;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;

import java.util.concurrent.TimeUnit;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.channels.FileChannel;


public class PipelineHttpSession {
	private static boolean isStop = false;
	private static long stopTime = 0;
	
	private static final int EACH_PART_SIZE = 1024 * 128;
	private static final String LOGTAG = LogUtil.makeLogTag(PipelineHttpSession.class);

	private String bucketName;
	private String fileName = null;
	private String token = null;
	private WanNOSObject meta = null;
	private Callback callback = null;
	private Object fileParam;
    private long totalLength = 0;
	private File file = null;
	private String MD5 = null;
	
	private volatile String uploadContext = null;  /* need to sync read/write it ? */
    private volatile long sendOffset = 0;
    private volatile long responseOffset = 0;
    private volatile long respNum = 0;
    private volatile boolean isComplete = false;
    private volatile int isSuccess = 0;
    private volatile boolean hasBreakQuery = false;
	private volatile long lastResponseTime = 0;
	private volatile boolean upCancelled = false;
	private volatile HttpResult rs = null;    
    
    private	UploadTask uploadTask = null;
    private PipelineHttpClient client = null;
	
	private int chunkSize = PipelineHttpSession.EACH_PART_SIZE;
	private int timeout = 30 * 1000;
	private boolean isHttps = false;
    private Object completeCondition = new Object();
    
	public PipelineHttpSession(String token, String bucketName,
			String fileName, Object fileParam, File file, String uploadContext,
			boolean isHttps, WanNOSObject meta, String MD5, Callback callback,
			int chunkSize, UploadTask uploadTask) {

		this.bucketName = bucketName;
		this.fileName = fileName;
		this.uploadContext = uploadContext;
		this.callback = callback;
		this.fileParam = fileParam;
		this.totalLength = file.length();
		this.file = file;
		
		this.token = token;
		this.meta = meta;
		this.isHttps = isHttps;
		this.MD5 = MD5;
		
		this.uploadTask = uploadTask;
		this.timeout = WanAccelerator.getConf().getSoTimeout();
		this.chunkSize = chunkSize;

		int port = isHttps ? 443 : 80;
		client = new PipelineHttpClient(port, isHttps, this);
	}


	private boolean uploadContextExist() {
		return (uploadContext != null && !uploadContext.equals(""));
	}

	private void waitForContext() {

		try {

			synchronized (completeCondition) {
				lastResponseTime = System.currentTimeMillis();
				while (!uploadContextExist() && !isComplete
						&& (System.currentTimeMillis() < lastResponseTime + timeout)) {
					completeCondition.wait(timeout /* - (System.currentTimeMillis() - lastResponseTime) */ );
				}

			}

		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		if (!uploadContextExist() && !isComplete) {
			LogUtil.e(LOGTAG, "no uploadContext received");
			// some error happen
			HttpResult rs = new HttpResult(Code.HTTP_NO_RESPONSE, new JSONObject(), null);
			setSessionSuccess(PipelineCode.NO_UPLOAD_RESP, rs);
			return;
		}

	}

	private void waitForComplete() {

		try {
		
			// 等待上传完成
			if (!isComplete) {
				synchronized (completeCondition) {
					lastResponseTime = System.currentTimeMillis();
					while (!isComplete && (System.currentTimeMillis() < lastResponseTime + timeout)) {
						completeCondition.wait(timeout  /* - (System.currentTimeMillis() - lastResponseTime) */ );
					}

				}
			}

		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		if (!isComplete) {
			/* upload timeout */
			HttpResult rs = new HttpResult(Code.HTTP_NO_RESPONSE, new JSONObject(), null);
			handlerError(rs, PipelineCode.NO_UPLOAD_RESP, "upload timeout for " + timeout + "ms, close channel");
		}

	}
	

	public HttpResult upload(String ip) throws IOException, InterruptedException {
		long count = 0;
		long totalSize = 0;
		FileInputStream inputStream = new FileInputStream(file);
		
		LogUtil.d(LOGTAG, "start pipeline upload to uploadServer ip: " + ip);
		long tStart = System.currentTimeMillis();

		while (!upCancelled) {
			long sendSize = oneUpload(ip, inputStream);
			totalSize += sendSize;

			if (upCancelled) {
				break;
			}

			if (isSuccess == PipelineCode.BACK_OFFSET) {
				// need to Continue
			} else if (isSuccess == PipelineCode.CHANNEL_INACTIVE
					&& (count == 0 || respNum != 0)) {
				// if channel is inactive, will create new channel to retry
				// uploading this logic is for uploading large file
			} else {
				// stop retry for other conditions
				break;
			}

			LogUtil.w(LOGTAG, "retry to upload for reason:" + isSuccess 
					+ " count:"	+ count + ", current respNum:" + respNum);
			count++;
		}

		inputStream.close();
		long duration = System.currentTimeMillis() - tStart;
		float speed = (float) ((totalSize / 1024.0) / (duration / 1000.0));

		LogUtil.w(LOGTAG, "pipeline upload isSuccess:" + isSuccess + " duration:" 
				+ duration + " totalSize:" + totalSize + " speed:" + speed + "KB/S");
		
		if (rs == null) {
			rs = new HttpResult(isSuccess == PipelineCode.SUCCESS ? Code.HTTP_SUCCESS : Code.HTTP_EXCEPTION, new JSONObject(), null);
		}

		return rs;
	}		
	
	
	private long oneUpload(String ip, FileInputStream inputStream) throws IOException, InterruptedException {
		LogUtil.d(LOGTAG, "pipeline one upload start");

		// init all vaiables 
		isComplete = false;
		isSuccess = PipelineCode.UNKNOWN_REASON;
		hasBreakQuery = false;
		responseOffset = 0;
		respNum = 0;
		rs = null;
		
		// connect to upload server
		if (null == client.connect(ip)) {
			LogUtil.d(LOGTAG, "failed to connect uploadServer:" + ip);
			rs = new HttpResult(Code.CONNECTION_TIMEOUT, new JSONObject(), null);
			return 0;
		}

		// connect后检查cancel状态
		if (upCancelled) {
			return 0;
		}

		LogUtil.d(LOGTAG, "uploadContext:" + uploadContext + ", uploadContextExist:" + uploadContextExist());
		/* check break */
		if (uploadContextExist()) {
			// breakquery完成后可以得到responseOffset(根据服务器返回)
			breakQuery();
			if (!hasBreakQuery) {
				return 0;
			}

		} else {
			// no upload context means no need to handle breakQuery 
			hasBreakQuery = true;
		}

		// breakQuery后检查cancel状态
		if (upCancelled) {
			return 0;
		}

		long breakQueryOffset = responseOffset;
		if (!isComplete) {
			sendOffset = responseOffset;
            // set read position 
            FileChannel fc = inputStream.getChannel();
            fc.position(sendOffset);// set the file pointer to byte position;            
		}

		lastResponseTime = System.currentTimeMillis();
		int count = 0;
		while (!isComplete && (sendOffset < totalLength || (sendOffset == 0 && totalLength == 0))) {
			// 第一请求之前检查cancel状态
			if (upCancelled) {
				break;
			}

			// write but not completed block number
			count++;
			ChannelFuture cf = sendPost(inputStream, sendOffset, chunkSize);
			if (null == cf) {
				// some condition is full just quit
				break;
			}

			try {
				cf.await(timeout, TimeUnit.MILLISECONDS); // wait for block writing completed
			} catch (InterruptedException e) {
				if (!upCancelled) {
					e.printStackTrace();
				}
				LogUtil.w(LOGTAG, "pipeline upload is interrupted:" + e.getCause());
			}

			// check if cancelled after one block upload
			if (upCancelled) {
				break;
			}
			
			LogUtil.d(LOGTAG, "pipeline one block upload isDone:" + cf.isDone());
			
			if (!cf.isDone() && (System.currentTimeMillis() > lastResponseTime + timeout + 800)) {
				// no response for long time, stop and exit
				HttpResult rs = new HttpResult(Code.HTTP_NO_RESPONSE, new JSONObject(), null);
				handlerError(rs, PipelineCode.NO_UPLOAD_RESP, "upload timeout for " + timeout + "ms, close channel");
				break;
			}
			
			if (totalLength == 0) {
				// empty file
				break;
			}
			
			if (!(cf.channel().isWritable())) {
				LogUtil.w(LOGTAG, "channel is not wirtable" + ", sendCount:" + count);
				waitForWriteDone(cf, count);
			}
			
			if (cf.channel().isActive()) {
				// wait for uploadContext before sending next part 
				if (1 == count && (sendOffset < totalLength)) {
					waitForContext();
				}

				LogUtil.d(LOGTAG, "pipeline http post success, sendOffset: "
							+ sendOffset + ", totalLength: " + totalLength + ", this is "
							+ count + " block uploaded");
			} else { // some error happen
				HttpResult rs = new HttpResult(Code.HTTP_EXCEPTION, new JSONObject(), null);
				handlerError(rs, PipelineCode.CHANNEL_INACTIVE, "Channel is not active");
				break;
			}
		}

		waitForComplete();
		// upload completed or error happened 
		long sendSize = responseOffset > breakQueryOffset ? responseOffset - breakQueryOffset : 0;
		LogUtil.d(LOGTAG, "pipeline one upload isSuccess:" + isSuccess + " sendSize:" + sendSize);
		
		return sendSize;
	}

	private void waitForBreakResp() {

		try {

			// 等待BreakQuery完成
			if (!hasBreakQuery && !isComplete) {
				synchronized (completeCondition) {
					lastResponseTime = System.currentTimeMillis();
					while (!hasBreakQuery && !isComplete &&
							(System.currentTimeMillis() < lastResponseTime + timeout)) {
						completeCondition.wait(timeout);
					}

				}
			}

		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		if (!hasBreakQuery && !isComplete) {
			LogUtil.e(LOGTAG, "no breakQuery response");
			// some error happen
			HttpResult rs = new HttpResult(Code.HTTP_NO_RESPONSE, new JSONObject(), null);
			setSessionSuccess(PipelineCode.NO_BREAK_RESP, rs);
			return;
		}

	}

	
	private HttpRequest buildBreakRequest(String url) {
    	HttpRequest request = new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_1,
                HttpMethod.GET,
                url);
        request.headers().add("Host", client.ip);
//                .add("Connection", "Keep-Alive");
        request.headers().add(Constants.HEADER_TOKEN, token);

		return request;
	}
	
	public void breakQuery() {
		long tStart, tEnd, tDuration;
	    String breakQueryUrl = null;
		
		try {
			breakQueryUrl = (isHttps ? ("https://" + client.ip + ":443") : "") 
					+ Util.pipeBuildQueryUrl(bucketName, fileName, uploadContext);
		} catch (Exception ex) {
			LogUtil.e(LOGTAG, "build breakQueryUrl exception", ex);
			rs = new HttpResult(Code.HTTP_EXCEPTION, new JSONObject(), ex);
			return;
		}

		LogUtil.d(LOGTAG, "break query upload server url: " + breakQueryUrl);
		tStart = System.currentTimeMillis();
		client.get(buildBreakRequest(breakQueryUrl));
		waitForBreakResp();
		
		tEnd = System.currentTimeMillis();
		tDuration = tEnd - tStart;
		LogUtil.d(LOGTAG, "breakQuery duration: " + tDuration);

		return;
	}


	private DefaultFullHttpRequest buildUploadRequest(InputStream inputStream, int length, String postUrl) {
    	DefaultFullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, postUrl);
        request.headers().add("Host", client.ip)
//                .add("Connection", "Keep-Alive")
                .add("Content-Length", length);

        request.headers().add(Constants.HEADER_TOKEN, token);
		if (MD5 != null && !MD5.equals("")) {
			request.headers().add("Content-MD5", MD5);
		}
        if (meta != null) {
			Util.pipeAddHeaders(request, meta);
		}

		try {
			request.content().writeBytes(inputStream, length);
		} catch (Exception e) {
			e.printStackTrace();

			// failed to read file
			setSessionSuccess(PipelineCode.FAILED_READFILE, rs);
			LogUtil.e(LOGTAG, "failed to read file" + ", readlength:" + length
					+ ", totalLength:" + this.totalLength);
			return null;
		}

		return request;
	}
	
	
	public ChannelFuture sendPost(FileInputStream inputStream, long offset, int part_size) throws IOException {
		if (isComplete) {
			LogUtil.d(LOGTAG, "iscomplete offset: " + offset + ", totalLength: " + this.totalLength);
			return null;
		}

		if (totalLength != 0 && offset == totalLength) {
			handlerComplete(rs);
			LogUtil.d(LOGTAG, "sendPost complete offset: " + offset + "= totalLength: " + this.totalLength);
			return null;
		} else if (offset > this.totalLength) {
			setSessionSuccess(PipelineCode.INVALID_SENDOFFSET, rs);
			LogUtil.e(LOGTAG, "sendPost Error offset: " + offset + ", totalLength: " + this.totalLength);
			return null;
		}

		int length = (int) Math.min(part_size, this.totalLength - offset);
		LogUtil.d(LOGTAG, "upload block size is: " + length + ", part_size:" + part_size);

		sendOffset = length + offset;
		boolean isLast = false;
		if (length + offset == this.totalLength) {
			isLast = true;
		}

		String url = (isHttps ? ("https://" + client.ip + ":443") : "") 
				+ Util.pipeBuildPostDataUrl(bucketName, fileName, this.uploadContext, offset, isLast);
		LogUtil.d(LOGTAG, "post data url: " + url);
		
		ChannelFuture cf = client.post(buildUploadRequest(inputStream, length, url));
		if (null == cf) {
			HttpResult rs = new HttpResult(Code.HTTP_EXCEPTION, new JSONObject(), null);
			handlerError(rs, PipelineCode.CHANNEL_EXCEPTION, "pipeline exception: ChannelFuture is null");
		}

		return cf;
	}

	public void setSessionSuccess(int isSuccess, HttpResult rs) {
		client.reset();

		if (this.isSuccess == PipelineCode.UNKNOWN_REASON) {
			this.isSuccess = isSuccess;
		}

		if (this.rs == null) {
			this.rs = rs;
		}

		synchronized (completeCondition) {
			this.isComplete = true;
			completeCondition.notify();
		}

	}

	public void cancel() {
		LogUtil.d(LOGTAG, "pipeline uploading is canceling");
		upCancelled = true;

		if (client != null) {
			handlerError(rs, PipelineCode.UPLOAD_CANCELLED, "pipeline upload is cancelled");
		}
	}

	public void setUploadContext(String newUploadContext) {
		if (!newUploadContext.equals(uploadContext)) {
			callback.onUploadContextCreate(fileParam, uploadContext, newUploadContext);

			synchronized (completeCondition) {
				uploadContext = newUploadContext;
				completeCondition.notify();
			}

			LogUtil.d(LOGTAG, "received new uploadContext: " + newUploadContext);
		}
	}
	
    
	public void handleBreakInfo(int httpRespCode, JSONObject nosInfo) throws JSONException {

		if (httpRespCode == Code.CACHE_EXPIRED) {
			uploadContext = null;
		} else if (httpRespCode == Code.HTTP_SUCCESS) {
	        if (nosInfo == null || !nosInfo.has("offset")) {
				// error
				HttpResult offsetRs = new HttpResult(Code.INVALID_OFFSET, nosInfo,
						new InvalidOffsetException("offset is missing in breakQuery response"));
	        	handlerError(offsetRs, PipelineCode.INVALID_BREAK_OFFSET, "no offset in breakQuery response");
	        	responseOffset = 0;
	        	return;
	        } 
			
			responseOffset = nosInfo.getInt("offset");
		} else {
	        HttpResult rs = new HttpResult(httpRespCode, nosInfo, null);
			handlerError(rs, PipelineCode.FAILED_BREAK_RESP, "HTTP Response Code:" + httpRespCode);
			return;
		}
			
		if ((responseOffset >= totalLength && totalLength != 0) || responseOffset < 0) {
			HttpResult breakRs = new HttpResult(Code.INVALID_OFFSET, new JSONObject() /* nosInfo */,
					new InvalidOffsetException("offset is invalid in server side, with offset: " + responseOffset
							+ ", file length: " + totalLength));			
			handlerError(breakRs, PipelineCode.INVALID_BREAK_OFFSET, "HTTP Response Code:" + httpRespCode);
			responseOffset = 0;
			return;
		}
        
		synchronized (completeCondition) {
			hasBreakQuery = true;
			completeCondition.notify();
		}

    }

	
	public void handleOffset(int offset, HttpResult rs) {
    	/* should be included in sync ? */
		lastResponseTime = System.currentTimeMillis();
		respNum++;
		
    	if (offset == totalLength) {
            //complete
            responseOffset = offset;
    		handlerComplete(rs);
        } else if (offset > totalLength || offset < 0) {
            //error
            handlerError(rs, PipelineCode.INVALID_UPLOAD_OFFSET, "offset error");
        } else if (offset <= responseOffset) {
       		LogUtil.w(LOGTAG, "pipeline backoff, offset: " + offset 
       				+ ", current responseOffset: " + responseOffset);
           	// backoff happen
			handlerError(rs, PipelineCode.BACK_OFFSET, "pipeline offset backoff");
		} else {
			// success
			responseOffset = offset;
		}

		uploadTask.getUploadProgress(offset, totalLength);
		LogUtil.d(LOGTAG, "pipeline http response, offset: " 
				+ offset + ", totalLength: " + totalLength 
				+ ", this is " + respNum + " block response");
	}


    private void handlerComplete(HttpResult rs) {
		LogUtil.d(LOGTAG, "pipeline http post Complete");
		setSessionSuccess(PipelineCode.SUCCESS, rs);
    }

    
    private void handlerError(HttpResult rs, int errCode, String cause) {
        LogUtil.e(LOGTAG, "handlerError cause: " + cause);
		client.channelClose();
        setSessionSuccess(errCode, rs);
    }


    public boolean hasBreakQuery() {
    	return hasBreakQuery;
    }

    
    public String getUploadContext() {
    	return uploadContext;
    }

	public void waitForWriteDone(ChannelFuture cf, int count) {
		try {
			// wait for channel become writable
			if (!(cf.channel().isWritable()) && !isComplete) {
				synchronized (completeCondition) {
					lastResponseTime = System.currentTimeMillis();
					while (!(cf.channel().isWritable()) && !isComplete &&
							(System.currentTimeMillis() < lastResponseTime + timeout)) {
					  	completeCondition.wait(timeout);
					}

				}
			}

		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		if (!(cf.channel().isWritable()) && !isComplete) {
			LogUtil.e(LOGTAG, "wait for channel writable long time");
			// some error happen
			HttpResult rs = new HttpResult(Code.HTTP_EXCEPTION, new JSONObject(), null);
			handlerError(rs, PipelineCode.CHANNEL_EXCEPTION, "pipeline exception: channel is not writable");
			return;
		}
			
	}

	
	public void writeDone() {

		synchronized (completeCondition) {
			completeCondition.notify();
		}
        		
	}
	
	public boolean isUpCancelled() {
		return upCancelled;
	}
	
	public static void stop() {
		PipelineHttpSession.isStop = true;
		PipelineHttpSession.stopTime = System.currentTimeMillis();
		LogUtil.w(LOGTAG, "pipeline stopped for a while");
	}

	public static boolean isStop() {
		if (isStop && (stopTime	+ WanAccelerator.getConf().getPipelineFailoverPeriod()
				       <= System.currentTimeMillis())) {
			isStop = false;
		}

		return PipelineHttpSession.isStop;
	}

	public static void reStart() {
		if (isStop) {
			isStop = false;
			LogUtil.w(LOGTAG, "pipeline restart");
		}
	}

}
