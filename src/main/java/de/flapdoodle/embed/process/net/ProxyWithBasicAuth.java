package de.flapdoodle.embed.process.net;

import de.flapdoodle.checks.Preconditions;

import java.net.Proxy;
import java.net.SocketAddress;
import java.util.Objects;

public class ProxyWithBasicAuth extends Proxy {
	private String username;
	private String password;

	public ProxyWithBasicAuth(Type type, SocketAddress sa, String username, String password) {
		super(type, sa);
		this.username = Preconditions.checkNotNull(username,"username is null");
		this.password = Preconditions.checkNotNull(password, "password is null");
	}

	public String username() {
		return username;
	}

	public String password() {
		return password;
	}
}
