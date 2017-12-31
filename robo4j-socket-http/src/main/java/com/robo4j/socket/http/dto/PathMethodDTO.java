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

package com.robo4j.socket.http.dto;

import com.robo4j.socket.http.HttpMethod;

import java.util.List;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class PathMethodDTO {

	private final String path;
	private final HttpMethod method;
	private final List<String> callbacks;

	public PathMethodDTO(String path, HttpMethod method, List<String> callbacks) {
		this.path = path;
		this.method = method;
		this.callbacks = callbacks;
	}

	public String getPath() {
		return path;
	}

	public HttpMethod getMethod() {
		return method;
	}

	public List<String> getCallbacks() {
		return callbacks;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof PathMethodDTO))
			return false;

		PathMethodDTO that = (PathMethodDTO) o;

		if (!path.equals(that.path))
			return false;
		return method == that.method;
	}

	@Override
	public int hashCode() {
		int result = path.hashCode();
		result = 31 * result + method.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return "PathMethodDTO{" + "path='" + path + '\'' + ", method=" + method + ", callbacks="
				+ callbacks + '}';
	}
}