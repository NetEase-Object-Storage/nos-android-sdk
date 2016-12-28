package com.netease.cloud.nos.android.pipeline;

import com.netease.cloud.nos.android.ssl.EasySSLSocketFactory;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.HttpResponseDecoder;
import io.netty.handler.ssl.SslHandler;
import javax.net.ssl.SSLEngine;


public class HttpsChannelInitializer extends ChannelInitializer<SocketChannel> {

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		ChannelPipeline pipeline = ch.pipeline();

		// Add SSL handler first to encrypt and decrypt everything.
		SSLEngine engine = /* ((SSLTrustAllSocketFactory) SSLTrustAllSocketFactory
				.getSocketFactory())  */ new EasySSLSocketFactory() .getSslEngine();
		engine.setUseClientMode(true);
		pipeline.addLast("ssl", new SslHandler(engine));

		// On top of the SSL handler, add the text line codec.
		pipeline.addLast("decoder", new HttpResponseDecoder());
		pipeline.addLast("encoder", new HttpRequestEncoder());

		// pipeline.addLast("inflater", new HttpContentDecompressor());

		pipeline.addLast("aggregator", new HttpObjectAggregator(1024 * 1024));
		pipeline.addLast("handler",	new PipelineHttpClientHandler());
	}

}
