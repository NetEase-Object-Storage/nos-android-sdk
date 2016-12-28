package com.netease.cloud.nos.android.pipeline;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Vector;

import com.netease.cloud.nos.android.core.WanAccelerator;
import com.netease.cloud.nos.android.utils.LogUtil;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.util.AttributeKey;


public class PipelineHttpClient {
	private static final String LOGTAG = LogUtil.makeLogTag(PipelineHttpClient.class);
    public static final AttributeKey<PipelineHttpSession> SESSION_KEY = AttributeKey.valueOf("PipelineHttpSession");

    // will retry outside if connection failed
    protected static final int retryLimit = 1;

    private static List<Channel> httpChannelList = new Vector<Channel>();
    private static Bootstrap httpCbs = getBootstrap(new HttpChannelInitializer());

    private static List<Channel> httpsChannelList = new Vector<Channel>();
    private static Bootstrap httpsCbs = getBootstrap(new HttpsChannelInitializer());
    
    private List<Channel> connectedChannelList = null;
    private Bootstrap cbs = null;
    protected String ip = null;
    protected int port = 0;
    private Channel connectChannel;
    private PipelineHttpSession session;
    
    public PipelineHttpClient(
        int port,
        boolean isHttps,
        PipelineHttpSession session) {
//      this.ip = ip;  
        this.port = port;
        this.session = session;

        if (isHttps) {
			connectedChannelList = httpsChannelList;
			cbs = httpsCbs;
        } else {
			connectedChannelList = httpChannelList;
			cbs = httpCbs;
        }
    }

    private static Bootstrap getBootstrap(ChannelInitializer<SocketChannel> channelInitializer) {
		Bootstrap cbs = new Bootstrap();

		cbs.group(new NioEventLoopGroup())
				.channel(NioSocketChannel.class)
				.option(ChannelOption.TCP_NODELAY, true) // disable Nagle
				.option(ChannelOption.SO_SNDBUF, 1024 * 1024)
				.option(ChannelOption.WRITE_BUFFER_HIGH_WATER_MARK, 1024 * 1024)
				.option(ChannelOption.WRITE_BUFFER_LOW_WATER_MARK, 1024 * 1024)
				.option(ChannelOption.CONNECT_TIMEOUT_MILLIS,
						WanAccelerator.getConf().getConnectionTimeout())
				.handler(channelInitializer);

		return cbs;
	}
    
    public Channel connect(String ip) {
        connectChannel = null;
        int retry = 0;

        this.ip = ip;  
        
        while (retry < PipelineHttpClient.retryLimit) {
            Channel channel = doConnect();
            if (channel != null) {
                connectChannel = channel;
                return connectChannel;
            } else {
                retry++;
            }
        }

//        LogUtil.pipeline(session.getLogPrefix(), "connect retry over limit");
        return null;
    }

    public void reset() {
        synchronized (connectedChannelList) {
            if (connectChannel != null) {
                connectChannel.attr(SESSION_KEY).set(null);

                //注释掉这一段，Channel不关闭,复用
//                connectedChannelList.remove(connectChannel);
//                connectChannel.close();
//                connectChannel = null;
            }
        }
    }


    public void channelClose() {
        synchronized (connectedChannelList) {
            if (connectChannel != null) {
                connectChannel.attr(SESSION_KEY).set(null);
                // 关闭Channel
                connectedChannelList.remove(connectChannel);
                connectChannel.close()  /*  .syncUninterruptibly()  */  ;
                connectChannel = null;
            }
        }
    }

    
    private Channel doConnect() {
        synchronized (connectedChannelList) {
            for (int i = 0; i < connectedChannelList.size(); i++) {
                Channel channel = connectedChannelList.get(i);
                if (channel.isActive()) {
					String host = ((InetSocketAddress) channel.remoteAddress()).getAddress().getHostAddress();
					int port = ((InetSocketAddress) channel.remoteAddress()).getPort();

					if (channel.attr(SESSION_KEY).get() == null && host.equals(this.ip) && port == this.port) {
						LogUtil.d(LOGTAG, "reuse active connection to uploadServer ip: " + ip);
						// set handler context
						channel.attr(PipelineHttpClient.SESSION_KEY).set(this.session);
						return channel;
					}

                } else {
                    LogUtil.d(LOGTAG, "doConnect close inactive channel");
                    connectedChannelList.remove(i--);
                    if (channel.isOpen()) {
                        channel.close();
                    }
                }
            }

        }            
            
        LogUtil.d(LOGTAG, "doConnect new connect start: " + System.currentTimeMillis());
        // connect is thread-safe, so it can be called without syncronization 
        ChannelFuture future = cbs.connect(new InetSocketAddress(this.ip, this.port));
        // wait until connection is setup or timeout 
        future.awaitUninterruptibly();
        LogUtil.d(LOGTAG, "doConnect to uploadServer ip: " + ip + ", end:" + System.currentTimeMillis());

        synchronized (connectedChannelList) {
                
            if (future.isSuccess()) {
                // connect success 
                Channel channel = future.channel();
                channel.attr(PipelineHttpClient.SESSION_KEY).set(this.session);// set handler context
                
                connectedChannelList.add(channel);
                return channel;
            } else {
              	// connect failed 
               	future.channel().close();
                return null;
            }
        
        }
    }

    public void get(HttpRequest request) {
		
        if (null != connectChannel) {
            synchronized (this) {
                if (null != connectChannel) {
                    connectChannel.writeAndFlush(request);
                }
            }
        }
    }

    
    public ChannelFuture post(DefaultFullHttpRequest request) {
    	if (request == null) {
    		return null;
    	}
    		
        ChannelFuture cf = null;
        if (null != connectChannel) {
            synchronized (this) {
                if (null != connectChannel) {
					cf = connectChannel.writeAndFlush(request);
                }
            }
        }

        return cf;
    }

}
