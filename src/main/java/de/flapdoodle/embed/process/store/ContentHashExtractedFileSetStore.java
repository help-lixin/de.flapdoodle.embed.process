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
package de.flapdoodle.embed.process.store;

import de.flapdoodle.checks.Preconditions;
import de.flapdoodle.embed.process.archives.ExtractedFileSet;
import de.flapdoodle.embed.process.archives.ImmutableExtractedFileSet;
import de.flapdoodle.embed.process.config.store.FileSet;
import de.flapdoodle.embed.process.hash.Hasher;
import de.flapdoodle.embed.process.nio.Directories;
import de.flapdoodle.types.Try;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ContentHashExtractedFileSetStore implements ExtractedFileSetStore {

	private final Path basePath;

	public ContentHashExtractedFileSetStore(Path basePath) {
		this.basePath = basePath;
		if (!Files.exists(basePath)) {
			Try.run(() -> Files.createDirectory(basePath));
		}
	}

	@Override
	public Optional<ExtractedFileSet> extractedFileSet(Path archive, FileSet fileSet) {
		String hash = hash(archive, fileSet);
		Path fileSetBasePath = basePath.resolve(hash);
		if (Files.isDirectory(fileSetBasePath)) {
			return Try.supplier(() -> Optional.of(readFileSet(fileSetBasePath, fileSet)))
				.fallbackTo(ex -> Optional.empty())
				.get();
		}
		
		return Optional.empty();
	}

	@Override
	public ExtractedFileSet store(Path archive, FileSet fileSet, ExtractedFileSet src) throws IOException {
		String hash = hash(archive, fileSet);
		Path fileSetBasePath = basePath.resolve(hash);
		Preconditions.checkArgument(!Files.exists(fileSetBasePath),"hash collision for %s (hash=%s)",archive, hash);
		Files.createDirectory(fileSetBasePath);
		return makeCopyOf(fileSetBasePath, fileSet, src);
	}
	private static ExtractedFileSet makeCopyOf(Path fileSetBasePath, FileSet fileSet, ExtractedFileSet src) throws IOException {
		try {
			Map<String, Path> nameMap = src.libraryFiles()
				.stream()
				.collect(Collectors.toMap(it -> src.baseDir().relativize(it).toString(), Function.identity()));

			ImmutableExtractedFileSet.Builder builder = ExtractedFileSet.builder(fileSetBasePath);
			for (FileSet.Entry entry : fileSet.entries()) {
				Path dest = fileSetBasePath.resolve(entry.destination());
				switch (entry.type()) {
					case Executable:
						if (!Files.exists(dest.getParent())) Files.createDirectory(dest.getParent());
						Files.copy(src.executable(), dest, StandardCopyOption.COPY_ATTRIBUTES);
						builder.executable(dest);
						break;
					case Library:
						Path srcPath = nameMap.get(entry.destination());
						if (srcPath==null) throw new IOException("could not find entry for "+entry.destination()+" in "+nameMap);
						if (!Files.exists(dest.getParent())) Files.createDirectory(dest.getParent());
						Files.copy(srcPath, dest, StandardCopyOption.COPY_ATTRIBUTES);
						builder.addLibraryFiles(dest);
						break;
				}
			}
			return builder.build();
		} catch (IOException iox) {
			Directories.deleteAll(fileSetBasePath);
			throw iox;
		}
	}

	private static String hash(Path archive, FileSet fileSet) {
		Hasher digest = Hasher.instance();
		fileSet.entries().forEach(entry -> {
			digest.update(entry.type().name().getBytes(StandardCharsets.UTF_8));
			digest.update(entry.destination().getBytes(StandardCharsets.UTF_8));
			digest.update(entry.matchingPattern().toString().getBytes(StandardCharsets.UTF_8));
		});
		digest.update("--".getBytes(StandardCharsets.UTF_8));
		digest.update(Try.get(() -> Files.readAllBytes(archive)));
		return digest.hashAsString();

//		return Try.get(() -> {
//			MessageDigest digest = MessageDigest.getInstance("SHA-256");
//			fileSet.entries().forEach(entry -> {
//				digest.update(entry.type().name().getBytes(StandardCharsets.UTF_8));
//				digest.update(entry.destination().getBytes(StandardCharsets.UTF_8));
//				digest.update(entry.matchingPattern().toString().getBytes(StandardCharsets.UTF_8));
//			});
//			digest.update("--".getBytes(StandardCharsets.UTF_8));
//			digest.update(Files.readAllBytes(archive));
//			return byteArrayToHex(digest.digest());
//		});
	}

//	private static String byteArrayToHex(byte[] a) {
//		StringBuilder sb = new StringBuilder(a.length * 2);
//		for(byte b: a)			sb.append(String.format("%02x", b));
//		return sb.toString();
//	}
//
	private static ExtractedFileSet readFileSet(Path fileSetBasePath, FileSet fileSet) {
		ImmutableExtractedFileSet.Builder builder = ExtractedFileSet.builder(fileSetBasePath);
		fileSet.entries().forEach(entry -> {
			Path entryPath = fileSetBasePath.resolve(entry.destination());
			Preconditions.checkArgument(Files.exists(entryPath),"could not find matching file: %s", entryPath);
			switch (entry.type()) {
				case Executable:
					builder.executable(entryPath);
					break;
				case Library:
					builder.addLibraryFiles(entryPath);
					break;
			}
		});
		return builder.build();
	}
}
