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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.infobip.infrastructure.ObjectMapperFactory;
import org.junit.Test;

import java.io.IOException;
import java.time.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.BDDAssertions.then;

public class SerializedVersionTest {

    private ObjectMapper objectMapper = ObjectMapperFactory.getInstance();

    private final LocalDate startOf2016 = LocalDate.of(2016, 1, 1);

    @Test
    public void shouldCorrectlySerializeVersion() throws JsonProcessingException {

        SerializedVersion version = new SerializedVersion(null, "1.0.0", "ABC", startOf2016, false);

        String actual = objectMapper.writeValueAsString(version);

        then(actual).isEqualTo(
                "{\"id\":null,\"name\":\"1.0.0\",\"project\":\"ABC\",\"releaseDate\":\"2016-01-01\",\"released\":false}");
    }

    @Test
    public void shouldCorrectlyDeserializeVersion() throws IOException {

        String json = "{\"id\":\"1\",\"name\":\"1.0.0\",\"project\":\"ABC\",\"releaseDate\":\"2016-01-01\",\"released\":false}";

        SerializedVersion actual = objectMapper.readValue(json, SerializedVersion.class);

        then(actual).isEqualTo(new SerializedVersion("1", "1.0.0", "ABC", startOf2016, false));
    }
}
