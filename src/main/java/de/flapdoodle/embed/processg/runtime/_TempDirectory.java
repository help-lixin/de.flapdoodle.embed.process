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
package de.flapdoodle.embed.processg.runtime;

import de.flapdoodle.embed.process.types.Wrapped;
import de.flapdoodle.embed.process.types.Wrapper;
import org.immutables.value.Value;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;

@Value.Immutable
@Wrapped
public abstract class _TempDirectory extends Wrapper<Path> {

	@Value.Auxiliary
	public Path createDirectory(String prefix, FileAttribute<?> ... attrs) throws IOException {
		return Files.createTempDirectory(value(),prefix, attrs);
	}

	@Value.Auxiliary
	public Path createFile(String prefix, String suffix, FileAttribute<?> ... attrs) throws IOException {
		return Files.createTempFile(value(),prefix, suffix, attrs);
	}
}
