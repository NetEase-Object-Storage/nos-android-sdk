package com.netease.cloud.nos.android.pipeline;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.HttpResponseDecoder;


public class HttpChannelInitializer extends ChannelInitializer<SocketChannel> {

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		ChannelPipeline pipeline = ch.pipeline();

		// add the text line codec on top.
		pipeline.addLast("decoder", new HttpResponseDecoder());
		pipeline.addLast("encoder", new HttpRequestEncoder());

		// pipeline.addLast("inflater", new HttpContentDecompressor());

		pipeline.addLast("aggregator", new HttpObjectAggregator(1024 * 1024));
		pipeline.addLast("handler",	new PipelineHttpClientHandler());
	}

}
