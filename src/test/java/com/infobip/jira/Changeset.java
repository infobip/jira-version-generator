/**
 *# Copyright 2016 Infobip
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

import com.atlassian.bitbucket.commit.Commit;
import com.atlassian.bitbucket.commit.MinimalCommit;
import com.atlassian.bitbucket.property.PropertyMap;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.user.Person;

import javax.annotation.Nullable;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.Date;

class Changeset implements Commit {

	private final String message;
	private final String id;
	private final Date authorTimestamp;

	Changeset(String id, String message, LocalDate authorTimestamp) {

		this.message = message;
		this.id = id;
		this.authorTimestamp = Date.from(authorTimestamp.atStartOfDay().toInstant(ZoneOffset.UTC));
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
	public Collection<MinimalCommit> getParents() {

		return null;
	}

	@Nullable
	@Override
	public Repository getRepository() {

		return null;
	}

	@Override
	public String getDisplayId() {

		return null;
	}

	@Override
	public String getId() {

		return id;
	}

	@Override
	public PropertyMap getProperties() {
		return PropertyMap.EMPTY;
	}
}
