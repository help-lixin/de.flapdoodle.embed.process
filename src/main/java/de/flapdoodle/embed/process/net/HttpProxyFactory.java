/**
 * Copyright (C) 2011
 *   Michael Mosmann <michael@mosmann.de>
 *   Martin Jöhren <m.joehren@googlemail.com>
 *
 * with contributions from
 * 	konstantin-ba@github,
 Archimedes Trajano (trajano@github),
 Kevin D. Keck (kdkeck@github),
 Ben McCann (benmccann@github)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.flapdoodle.embed.process.net;

import de.flapdoodle.checks.Preconditions;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Objects;

public class HttpProxyFactory implements ProxyFactory {

	private final String hostName;
	private final int port;

	public HttpProxyFactory(String hostName, int port) {
		this.hostName = Preconditions.checkNotNull(hostName,"hostname is null");
		this.port = port;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		HttpProxyFactory that = (HttpProxyFactory) o;
		return port == that.port && hostName.equals(that.hostName);
	}

	@Override
	public int hashCode() {
		return Objects.hash(hostName, port);
	}

	@Override public String toString() {
		return "HttpProxyFactory{" +
			"hostName='" + hostName + '\'' +
			", port=" + port +
			'}';
	}

	@Override
	public Proxy createProxy() {
		return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(hostName, port));
	}

	public static HttpProxyFactory with(String hostName, int port) {
		return new HttpProxyFactory(hostName, port);
	}

}
