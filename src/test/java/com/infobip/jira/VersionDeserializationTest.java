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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.extractProperty;

/**
 * @author lpandzic
 */
public class VersionDeserializationTest {

    private ObjectMapper objectMapper;

    @Before
    public void setUp() throws Exception {

        objectMapper = new ObjectMapper();
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    @Test
    public void shouldBeAbleToDeserializeVersionsJson() throws IOException {

        String json = "[\n" +
                "    {\n" +
                "        \"self\": \"https://jira.infobip.com/rest/api/2/version/11780\",\n" +
                "        \"id\": \"11780\",\n" +
                "        \"description\": \"first release\",\n" +
                "        \"name\": \"2.0.0\",\n" +
                "        \"archived\": false,\n" +
                "        \"released\": true,\n" +
                "        \"releaseDate\": \"2014-03-03\",\n" +
                "        \"userReleaseDate\": \"03/Mar/14\",\n" +
                "        \"projectId\": 10901\n" +
                "    },\n" +
                "    {\n" +
                "        \"self\": \"https://jira.infobip.com/rest/api/2/version/13720\",\n" +
                "        \"id\": \"13720\",\n" +
                "        \"name\": \"2.11.2\",\n" +
                "        \"archived\": false,\n" +
                "        \"released\": true,\n" +
                "        \"releaseDate\": \"2014-05-02\",\n" +
                "        \"userReleaseDate\": \"02/May/14\",\n" +
                "        \"projectId\": 10901\n" +
                "    }\n" +
                "]";

        Version[] versions = objectMapper.readValue(json, Version[].class);

        assertThat(extractProperty("name").from(versions)).containsOnly("2.0.0", "2.11.2");
    }
}
