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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author lpandzic
 */
public class VersionSerializationTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void shouldCorrectlySerializeVersion() throws JsonProcessingException {

        Version version = Version.of("1.0.0", new ProjectKey("ABC"), false, new Date(1405941314000L));

        String json = objectMapper.writeValueAsString(version);

        assertThat(json).isEqualTo(
                "{\"name\":\"1.0.0\",\"released\":false,\"releaseDate\":\"2014-07-21\",\"project\":\"ABC\"}");

    }
}
