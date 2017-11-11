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

package com.robo4j.socket.http.util;

import com.robo4j.socket.http.HttpByteWrapper;
import com.robo4j.socket.http.HttpHeaderFieldNames;
import com.robo4j.socket.http.units.BufferWrapper;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.robo4j.socket.http.util.HttpMessageUtil.HTTP_HEADER_BODY_DELIMITER;
import static com.robo4j.socket.http.util.HttpMessageUtil.HTTP_HEADER_SEP;
import static com.robo4j.socket.http.util.HttpMessageUtil.NEXT_LINE;
import static com.robo4j.socket.http.util.HttpMessageUtil.POSITION_BODY;
import static com.robo4j.socket.http.util.HttpMessageUtil.POSITION_HEADER;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class ChannelBufferUtils {

	public static final int INIT_BUFFER_CAPACITY = 4 * 4096;
	public static final byte CHAR_NEW_LINE = 0x0A;
	public static final byte CHAR_RETURN = 0x0D;
	public static final byte[] END_WINDOW = { CHAR_NEW_LINE, CHAR_NEW_LINE };
	private static final ByteBuffer buffer = ByteBuffer.allocateDirect(INIT_BUFFER_CAPACITY);

	public static ByteBuffer copy(ByteBuffer source, int start, int end) {
		ByteBuffer result = ByteBuffer.allocate(end);
		for (int i = start; i < end; i++) {
			result.put(source.get(i));
		}
		return result;
	}

	public static HttpByteWrapper getHttpByteWrapperByByteBufferString(BufferWrapper bufferWrapper) {
		final String[] headerAndBody = bufferWrapper.getMessage().split(HTTP_HEADER_BODY_DELIMITER);
		final String[] header = headerAndBody[POSITION_HEADER].split("[" + NEXT_LINE + "]+");

		return new HttpByteWrapper(header, headerAndBody.length == 2 ? headerAndBody[POSITION_BODY] : null);
	}

	public static BufferWrapper getBufferWrapperByChannel(ByteChannel channel) throws IOException {
		StringBuilder stringBuilder = new StringBuilder();
		int readBytes = channel.read(buffer);
		buffer.flip();
		addToStringBuilder(stringBuilder, buffer, readBytes);
		Integer messageSize = getTotalMessageSizeWithBody(stringBuilder);

		int totalReadBytes = readBytes;

		if (messageSize != null) {
			while (totalReadBytes < messageSize) {
				readBytes = channel.read(buffer);
				buffer.flip();
				addToStringBuilder(stringBuilder, buffer, readBytes);

				totalReadBytes += readBytes;
				buffer.clear();
			}

		}
		buffer.clear();

		return new BufferWrapper(totalReadBytes, stringBuilder.toString());

	}

	public static byte[] validArray(byte[] array, int size) {
		return validArray(array, 0, size);
	}

	public static byte[] validArray(byte[] array, int start, int size) {
		byte[] result = new byte[size];
		for (int i = start; i < size; i++) {
			result[i] = array[i];
		}
		return result;
	}

	public static boolean isBWindow(byte[] stopWindow, byte[] window) {
		return Arrays.equals(stopWindow, window);
	}

	private static Integer getTotalMessageSizeWithBody(StringBuilder sb) {
		final String[] headerAndBody = sb.toString().split(HTTP_HEADER_BODY_DELIMITER);
		final String[] header = headerAndBody[0].split("[" + NEXT_LINE + "]+");
		final String[] paramArray = Arrays.copyOfRange(header, 1, header.length);

		final Map<String, String> headerParams = new HashMap<>();

		for (int i = 1; i < paramArray.length; i++) {
			final String[] array = paramArray[i]
					.split(HttpMessageUtil.getHttpSeparator(HTTP_HEADER_SEP));

			String key = array[HttpMessageUtil.METHOD_KEY_POSITION].toLowerCase();
			String value = array[HttpMessageUtil.URI_VALUE_POSITION].trim();
			headerParams.put(key, value);
		}

		String valueBodySize = headerParams.get(HttpHeaderFieldNames.CONTENT_LENGTH);

		return valueBodySize != null
				? headerAndBody[POSITION_HEADER].length() + HTTP_HEADER_BODY_DELIMITER.length() + Integer.valueOf(valueBodySize)
				: null;
	}

	private static void addToStringBuilder(StringBuilder sb, ByteBuffer buffer, int size) {
		byte[] array = new byte[size];
		for (int i = 0; i < size; i++) {
			array[i] = buffer.get(i);
		}
		String message = new String(array);
		sb.append(message);

	}
}
