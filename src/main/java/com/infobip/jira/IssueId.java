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

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import java.util.Objects;

/**
 * JIRA issue id. For example in JIRA issue key ABC-123 123 represents the issue id.
 *
* @author lpandzic
*/
@Immutable
public class IssueId {

    final String value;

    IssueId(@Nonnull String value) {

        this.value = Objects.requireNonNull(value);
    }

    @Override
    public String toString() {

        return value;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final IssueId issueId = (IssueId) o;

        if (!value.equals(issueId.value)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {

        return value.hashCode();
    }
}
