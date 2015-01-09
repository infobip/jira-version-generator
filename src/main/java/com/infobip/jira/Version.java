/**
 *# Copyright 2014 Infobip
 #
 # Licensed under the Apache License, Version 2.0 (the "License");
 # you may not use this file except in compliance with the License.
 # You may obtain a copy of the License at
 #
 # http://www.apache.org/licenses/LICENSE-2.0
 #
 # Unless required by applicable law or agreed to in writing, software
 # distributed under the License is distributed on an "AS IS" BASIS,
 # WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 # See the License for the specific language governing permissions and
 # limitations under the License.
 */
package com.infobip.jira;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Date;

/**
 * @author lpandzic
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Version {

	private static final DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd");

	final String name;
	final Optional<ProjectKey> projectKey;
	final Boolean released;
	final String releaseDate;
	final Optional<Integer> id;

	public static Version of(String name, ProjectKey projectKey, Boolean released, Date releaseDate) {

		return new Version(name,
						   Optional.of(projectKey),
						   released,
						   formatter.print(new DateTime(releaseDate)),
						   Optional.<Integer>absent());
	}

	@JsonCreator
	public static Version of(@JsonProperty("name") String name,
							 @JsonProperty("released") Boolean released,
							 @JsonProperty("releaseDate") String releaseDate,
							 @JsonProperty("id") Integer id) {

		return new Version(name, Optional.<ProjectKey>absent(), released, releaseDate, Optional.fromNullable(id));
	}

	// Getters are used by jackson

	public String getName() {

		return name;
	}

	public String getProject() {

		if (!projectKey.isPresent()) {
			return null;
		}

		return projectKey.get().value;
	}

	public Boolean getReleased() {

		return released;
	}

	public String getReleaseDate() {

		return releaseDate;
	}

	public Integer getId() {

		return id.orNull();
	}

	@Override
	public String toString() {

		return "Version{" +
				"name='" + name + '\'' +
				", projectKey=" + projectKey +
				", released=" + released +
				", releaseDate='" + releaseDate + '\'' +
				", id=" + id +
				'}';
	}

	private Version(String name,
					Optional<ProjectKey> projectKey,
					Boolean released,
					String releaseDate,
					Optional<Integer> id) {

		this.name = name;
		this.projectKey = projectKey;
		this.released = released;
		this.releaseDate = releaseDate;
		this.id = id;
	}

	@Override
	public boolean equals(Object o) {

		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		final Version version = (Version) o;

		if (id != null ? !id.equals(version.id) : version.id != null) {
			return false;
		}
		if (name != null ? !name.equals(version.name) : version.name != null) {
			return false;
		}
		if (projectKey != null ? !projectKey.equals(version.projectKey) : version.projectKey != null) {
			return false;
		}
		if (releaseDate != null ? !releaseDate.equals(version.releaseDate) : version.releaseDate != null) {
			return false;
		}
		if (released != null ? !released.equals(version.released) : version.released != null) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {

		int result = name != null ? name.hashCode() : 0;
		result = 31 * result + (projectKey != null ? projectKey.hashCode() : 0);
		result = 31 * result + (released != null ? released.hashCode() : 0);
		result = 31 * result + (releaseDate != null ? releaseDate.hashCode() : 0);
		result = 31 * result + (id != null ? id.hashCode() : 0);
		return result;
	}
}
