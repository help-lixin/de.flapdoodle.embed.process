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
package de.flapdoodle.embed.process.io;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SuccessMessageLineListener {

	private final List<Pattern> successPatterns;
	private final List<Pattern> errorPatterns;
	private final String errorMessageGroupName;

	private boolean successMessageFound = false;
	private String errorMessage = null;

	public SuccessMessageLineListener(
		List<Pattern> successPatterns,
		List<Pattern> errorPatterns,
		String errorMessageGroupName
	) {
		this.successPatterns = new ArrayList<>(successPatterns);
		this.errorPatterns = new ArrayList<>(errorPatterns);
		this.errorMessageGroupName = errorMessageGroupName;
	}

	public void inspect(String line) {
		if (!successMessageFound && errorMessage ==null) {
			for (Pattern successPattern : successPatterns) {
				if (successPattern.matcher(line).find()) {
					successMessageFound=true;
					break;
				}
			}

			if (!successMessageFound) {
				for (Pattern errorPattern : errorPatterns) {
					Matcher matcher = errorPattern.matcher(line);
					if (matcher.find()) {
						errorMessage = matcher.group(errorMessageGroupName);
					}
				}
			}
		}
		synchronized(this) {
			notify();
		}
	}

	public synchronized void waitForResult(long timeout) {
		try {
			wait(timeout);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public boolean successMessageFound() {
		return successMessageFound;
	}

	public Optional<String> errorMessage() {
		return Optional.ofNullable(errorMessage);
	}

	public static SuccessMessageLineListener of(String ... successPatterns) {
		return of(Arrays.asList(successPatterns), Collections.emptyList(),"");
	}

	public static SuccessMessageLineListener of(
		List<String> successPatterns,
		List<String> errorPatterns,
		String errorMessageGroupName
	)                              {
		return new SuccessMessageLineListener(
			successPatterns.stream().map(Pattern::compile).collect(Collectors.toList()),
			errorPatterns.stream().map(Pattern::compile).collect(Collectors.toList()),
			errorMessageGroupName
		);
	}
}
