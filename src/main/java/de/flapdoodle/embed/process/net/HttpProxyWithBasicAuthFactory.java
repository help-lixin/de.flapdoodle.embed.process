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

import org.immutables.value.Value;

import java.net.InetSocketAddress;
import java.net.Proxy;

@Value.Immutable
public abstract class HttpProxyWithBasicAuthFactory implements ProxyFactory {

	protected abstract String hostName();
	protected abstract int port();
	@Value.Redacted
	protected abstract String username();
	@Value.Redacted
	protected abstract String password();

	@Override
	@Value.Auxiliary
	public Proxy createProxy() {
		return new ProxyWithBasicAuth(Proxy.Type.HTTP, new InetSocketAddress(hostName(), port()), username(), password());
	}

	public static ImmutableHttpProxyWithBasicAuthFactory.Builder builder() {
		return ImmutableHttpProxyWithBasicAuthFactory.builder();
	}
}
