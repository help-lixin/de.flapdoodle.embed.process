package de.flapdoodle.embed.process.hash;

import de.flapdoodle.types.Try;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class Hasher {

	private final MessageDigest digest;

	private Hasher(MessageDigest digest) {
		this.digest = digest;
	}

	public Hasher update(String content, Charset charset) {
		digest.update(content.getBytes(charset));
		return this;
	}

	public Hasher update(String content) {
		return update(content, StandardCharsets.UTF_8);
	}

	public Hasher update(byte[] content) {
		digest.update(content);
		return this;
	}

	public String hashAsString() {
		return byteArrayToHex(digest.digest());
	}

	public static Hasher instance() {
		return new Hasher(Try.get(() -> MessageDigest.getInstance("SHA-256")));
	}

	public static Hasher md5Instance() {
		return new Hasher(Try.get(() -> MessageDigest.getInstance("MD5")));
	}

	private static String byteArrayToHex(byte[] a) {
		StringBuilder sb = new StringBuilder(a.length * 2);
		for (byte b : a) sb.append(String.format("%02x", b));
		return sb.toString();
	}
}
