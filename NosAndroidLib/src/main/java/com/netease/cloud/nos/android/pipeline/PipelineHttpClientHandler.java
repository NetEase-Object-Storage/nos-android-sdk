package com.netease.cloud.nos.android.pipeline;

import org.json.JSONObject;

import com.netease.cloud.nos.android.constants.Code;
import com.netease.cloud.nos.android.exception.InvalidOffsetException;
import com.netease.cloud.nos.android.http.HttpResult;
import com.netease.cloud.nos.android.utils.LogUtil;
import java.nio.charset.Charset;

// import im.yixin.net.http.HTTPUpload;
// import im.yixin.util.log.LogUtil;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpResponseStatus;

import io.netty.handler.codec.http.FullHttpResponse;


public class PipelineHttpClientHandler extends ChannelDuplexHandler {

	private static final String LOGTAG = LogUtil.makeLogTag(PipelineHttpClientHandler.class);

	
    public String getLogPrefix() {
        return "PipelineHttpClientHandler";
    }

    public PipelineHttpClientHandler() {
        super();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    	JSONObject nosInfo = null;

    	LogUtil.d(LOGTAG, "Do channelRead");

/*
    	
    	if (!(msg instanceof DefaultFullHttpResponse)) {
            LogUtil.w(LOGTAG, "pipeline NON-DefaultFullHttpResponse: " + msg.getClass().getSimpleName());
            return;
        }

*/
    	
        FullHttpResponse res = (FullHttpResponse) msg;

        PipelineHttpSession s = ctx.channel().attr(PipelineHttpClient.SESSION_KEY).get();
        if (s == null) {
    		LogUtil.w(LOGTAG, "pipeline no httpSession");
        	return;
        }

        /* read content */
        if (res.content() != null) {
            nosInfo = new JSONObject(res.content().toString(Charset.defaultCharset()));
			LogUtil.d(LOGTAG, "received nosInfo: " + nosInfo);
        } else {
        	nosInfo = new JSONObject();
        	LogUtil.w(LOGTAG, "no content in response");
        }
        
        int httpRespCode = res.getStatus().code();
        HttpResult rs = new HttpResult(httpRespCode, nosInfo, null);
        
    	if (!s.hasBreakQuery()) {
        	s.handleBreakInfo(httpRespCode, nosInfo);
        	return;
    	}  
    	
        // handle offset response in the following code    	
    	if (httpRespCode != HttpResponseStatus.OK.code()) {
            //error: stop this server
        	handlerError(ctx, rs, PipelineCode.FAILED_UPLOAD_RESP, "HTTP Response Code:" + httpRespCode);
            return;
        }

        if (nosInfo == null || !nosInfo.has("context") || !nosInfo.has("offset")) {
			// error
			HttpResult offsetRs = new HttpResult(Code.INVALID_RESPONSE_DATA, new JSONObject() /* nosInfo */,
					new InvalidOffsetException("context or offset is missing in response"));
        	handlerError(ctx, offsetRs, PipelineCode.INVALID_UPLOAD_RESP, "no context or offset in response");
			return;
        } 
        
		try {

			String newUploadContext = nosInfo.getString("context");
			int offset = Integer.parseInt(nosInfo.getString("offset"));

			s.setUploadContext(newUploadContext);
			s.handleOffset(offset, rs);

		} catch (/* JSONException */ Exception jsonException) {
			jsonException.printStackTrace();
			throw new Exception("post response has not context or offset");
		}

	}

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		HttpResult rs = new HttpResult(Code.HTTP_EXCEPTION, new JSONObject(), (Exception)cause);
		handlerError(ctx, rs, PipelineCode.CHANNEL_EXCEPTION, "pipeline exception Caught:" + cause.toString());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        HttpResult	rs = new HttpResult(Code.HTTP_EXCEPTION, new JSONObject(), null);
        handlerError(ctx, rs, PipelineCode.CHANNEL_INACTIVE, "pipeline channelInactive");
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        LogUtil.d(LOGTAG, "channelWritabilityChanged isWritable: " + ctx.channel().isWritable());
    	
        PipelineHttpSession s = ctx.channel().attr(PipelineHttpClient.SESSION_KEY).get();
        if (s == null) {
            return;
        }
    	
		LogUtil.d(LOGTAG, "get PipelineHttpSession from the channel");

		// notify channel is writable
		if (ctx.channel().isWritable()) {
			s.writeDone();
		}

	}

    private void handlerError(ChannelHandlerContext ctx, HttpResult rs, int errCode, String cause) {
        LogUtil.e(LOGTAG, "handlerError cause: " + cause);

        if (ctx.channel().isOpen()) {
            ctx.channel().close();
        }
        notifySessionResult(ctx, rs, errCode);
    }

    
    private void notifySessionResult(ChannelHandlerContext ctx, HttpResult rs, int isSuccess) {
        PipelineHttpSession s = ctx.channel().attr(PipelineHttpClient.SESSION_KEY).get();
        if (s == null) {
            return;
        }
        s.setSessionSuccess(isSuccess, rs);
    }
}
