/*
 * Copyright (c) 2014, 2017, Marcus Hirt, Miroslav Wengner
 *
 * Robo4J is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Robo4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Robo4J. If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.socket.http.channel;

import com.robo4j.RoboContext;
import com.robo4j.socket.http.SocketException;
import com.robo4j.socket.http.message.HttpDecoratedRequest;
import com.robo4j.socket.http.request.RoboRequestCallable;
import com.robo4j.socket.http.request.RoboRequestFactory;
import com.robo4j.socket.http.request.RoboResponseProcess;
import com.robo4j.socket.http.units.HttpCodecRegistry;
import com.robo4j.socket.http.util.ChannelBufferUtils;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class ReadSelectionKeyHandler implements SelectionKeyHandler {

	private final RoboContext context;
	private final HttpCodecRegistry codecRegistry;
	private final Map<SelectionKey, RoboResponseProcess> outBuffers;
	private final SelectionKey key;

	public ReadSelectionKeyHandler(RoboContext context, HttpCodecRegistry codecRegistry,
                                   Map<SelectionKey, RoboResponseProcess> outBuffers, SelectionKey key) {
		this.context = context;
		this.codecRegistry = codecRegistry;
		this.outBuffers = outBuffers;
		this.key = key;
	}

	@Override
	public SelectionKey handle() {
		SocketChannel channel = (SocketChannel) key.channel();
		final HttpDecoratedRequest decoratedRequest = ChannelBufferUtils.getHttpDecoratedRequestByChannel(channel);
		final RoboRequestFactory factory = new RoboRequestFactory(codecRegistry);
		final RoboRequestCallable callable = new RoboRequestCallable(context, decoratedRequest, factory);
		final Future<RoboResponseProcess> futureResult = context.getScheduler().submit(callable);
		final RoboResponseProcess result = extractRoboResponseProcess(futureResult);
		outBuffers.put(key, result);
		registerSelectionKey(channel);
		return key;
	}

	private RoboResponseProcess extractRoboResponseProcess(Future<RoboResponseProcess> future){
		try {
			return future.get();
		}catch (Exception e){
			throw new SocketException("extract robo response", e);
		}
	}

	private void registerSelectionKey(SocketChannel channel){
		try {
			channel.register(key.selector(), SelectionKey.OP_WRITE);
		} catch (Exception e){
			throw  new SocketException("register selection key", e);
		}
	}
}