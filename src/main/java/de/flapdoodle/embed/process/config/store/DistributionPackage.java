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
package de.flapdoodle.embed.process.config.store;

import de.flapdoodle.embed.process.Legacy;
import de.flapdoodle.embed.process.distribution.ArchiveType;
import org.immutables.value.Value;
import org.immutables.value.Value.Parameter;

/**
 * DON'T DELETE, still needed for legacy support
 * @see Package
 */
@Deprecated
@Legacy
@Value.Immutable
public interface DistributionPackage {
	
	@Parameter
	ArchiveType archiveType();
	
	@Parameter
	FileSet fileSet();
	
	@Parameter
	String archivePath();
	
	static DistributionPackage of(ArchiveType archiveType, FileSet fileSet, String path) {
		return ImmutableDistributionPackage.of(archiveType, fileSet, path);
	}
	
	static ImmutableDistributionPackage.Builder builder() {
		return ImmutableDistributionPackage.builder();
	}
}
