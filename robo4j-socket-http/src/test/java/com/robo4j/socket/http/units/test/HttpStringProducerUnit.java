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

package com.robo4j.socket.http.units.test;

import com.robo4j.AttributeDescriptor;
import com.robo4j.ConfigurationException;
import com.robo4j.RoboContext;
import com.robo4j.configuration.Configuration;
import com.robo4j.socket.http.HttpHeaderFieldNames;
import com.robo4j.socket.http.HttpMethod;
import com.robo4j.socket.http.HttpVersion;
import com.robo4j.socket.http.ProtocolType;
import com.robo4j.socket.http.util.HttpDenominator;
import com.robo4j.socket.http.util.HttpMessageBuilder;
import com.robo4j.socket.http.util.RequestDenominator;
import com.robo4j.socket.http.util.RoboHttpUtils;

import java.util.concurrent.atomic.AtomicInteger;

import static com.robo4j.socket.http.util.RoboHttpUtils.HTTP_PROPERTY_HOST;

/**
 * Http String producer unit over Http protocol with default port
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class HttpStringProducerUnit extends StringProducerUnit {

	/* default sent messages */
	private static final int DEFAULT = 0;
	private static final String SEND_GET_MESSAGE = "sendGetMessage";
	public static final String SEND_POST_MESSAGE = "sendPostMessage";
	private AtomicInteger counter;
	private String target;
	private ProtocolType protocol;
	private String uri;
	private String host;

	/**
	 * @param context
	 * @param id
	 */
	public HttpStringProducerUnit(RoboContext context, String id) {
		super(context, id);
	}

	@Override
	protected void onInitialization(Configuration configuration) throws ConfigurationException {
		super.onInitialization(configuration);

		target = configuration.getString("target", null);
		protocol = ProtocolType.HTTP;
		uri = configuration.getString("uri", null);
		host = configuration.getString(HTTP_PROPERTY_HOST, null);

		if (host == null) {
			throw ConfigurationException.createMissingConfigNameException("host");
		}

		counter = new AtomicInteger(DEFAULT);

	}

	@Override
	public void onMessage(String message) {
		if (message == null) {
			System.out.println("No Message!");
		} else {
			counter.incrementAndGet();
			String[] input = message.split("::");
			String inMessageType = input[0];
			String inMessage = input[1].trim();
			switch (inMessageType) {
			case SEND_GET_MESSAGE:
				sendGetSimpleMessage();
				break;
			case SEND_POST_MESSAGE:
				sendPostSimpleMessage(uri, inMessage);
				break;
			default:
				System.out.println("don't understand message: " + message);

			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public synchronized <R> R onGetAttribute(AttributeDescriptor<R> attribute) {
		if (attribute.getAttributeName().equals("getNumberOfSentMessages")
				&& attribute.getAttributeType() == Integer.class) {
			return (R) (Integer) counter.get();
		}
		return null;
	}

	private void sendGetSimpleMessage() {
		final HttpDenominator denominator = new RequestDenominator(HttpMethod.GET, HttpVersion.HTTP_1_1);
		final String request = HttpMessageBuilder.Build().setDenominator(denominator)
				.addHeaderElement(HttpHeaderFieldNames.HOST, RoboHttpUtils.createHost(host, protocol.getPort()))
				.build();
		getContext().getReference(target).sendMessage(request);
	}

	private void sendPostSimpleMessage(String uri, String message) {
		if (uri == null) {
			throw new IllegalStateException("uri not available");
		}

		final HttpDenominator denominator = new RequestDenominator(HttpMethod.POST, uri, HttpVersion.HTTP_1_1);
		final String postMessage = HttpMessageBuilder.Build().setDenominator(denominator)
				.addHeaderElement(HttpHeaderFieldNames.HOST, RoboHttpUtils.createHost(host, protocol.getPort()))
				.addHeaderElement(HttpHeaderFieldNames.CONTENT_LENGTH, String.valueOf(message.length())).build(message);
		getContext().getReference(target).sendMessage(postMessage);
	}
}