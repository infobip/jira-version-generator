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

import com.atlassian.stash.content.AttributeMap;
import com.atlassian.stash.content.MinimalChangeset;
import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.user.Person;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Set;

/**
 * @author lpandzic
 */
class Changeset implements com.atlassian.stash.content.Changeset {

	private final String message;
	private final String id;
	private final Date authorTimestamp;

	public static Changeset of(String message) {

		return new Changeset(message, null, null);
	}

	public static Changeset of(String id, String message) {

		return new Changeset(message, id, null);
	}

	public static Changeset of(String id, String message, String date) throws ParseException {

		return new Changeset(message, id, new SimpleDateFormat("yyyy-MM-dd").parse(date));
	}

	Changeset(String message, String id, Date date) {

		this.message = message;
		this.id = id;
		this.authorTimestamp = date;
	}

	@Override
	public Person getAuthor() {

		return null;
	}

	@Override
	public Date getAuthorTimestamp() {

		return authorTimestamp;
	}

	@Override
	public String getMessage() {

		return message;
	}

	@Override
	public Collection<MinimalChangeset> getParents() {

		return null;
	}

	@Nullable
	@Override
	public Repository getRepository() {

		return null;
	}

	@Nonnull
	@Override
	public AttributeMap getAttributes() {

		throw new UnsupportedOperationException();
	}

	@Nonnull
	@Override
	public Set<String> getAttributeValues(String s) {

		throw new UnsupportedOperationException();
	}

	@Override
	public String getDisplayId() {

		return null;
	}

	@Override
	public String getId() {

		return id;
	}
}
