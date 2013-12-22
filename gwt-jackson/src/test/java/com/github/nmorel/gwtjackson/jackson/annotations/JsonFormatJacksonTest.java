/*
 * Copyright 2013 Nicolas Morel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.nmorel.gwtjackson.jackson.annotations;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.nmorel.gwtjackson.jackson.AbstractJacksonTest;
import com.github.nmorel.gwtjackson.shared.annotations.JsonFormatTester;
import org.junit.Test;

/**
 * @author Nicolas Morel
 */
public class JsonFormatJacksonTest extends AbstractJacksonTest {

    @Test
    public void testFormatDate() {
        objectMapper.configure( SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false );
        JsonFormatTester.INSTANCE.testFormatDate( createMapper( JsonFormatTester.FormatDateBean.class ) );
    }
}
