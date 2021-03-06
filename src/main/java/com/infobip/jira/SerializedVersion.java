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

import lombok.Value;

import javax.annotation.Nullable;
import java.time.LocalDate;

@Value
class SerializedVersion {

    private final String id;
    private final String name;
    private final String project;
    private final LocalDate releaseDate;
    private final Boolean released;

    SerializedVersion(@Nullable  String id, String name, String project, @Nullable LocalDate releaseDate, @Nullable Boolean released) {
        this.id = id;
        this.name = name;
        this.project = project;
        this.releaseDate = releaseDate;
        this.released = released;
    }
}
