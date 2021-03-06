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

package com.github.nmorel.gwtjackson.client.annotations;

import com.github.nmorel.gwtjackson.client.GwtJacksonTestCase;
import com.github.nmorel.gwtjackson.client.JsonDeserializationContext;
import com.github.nmorel.gwtjackson.client.ObjectMapper;
import com.github.nmorel.gwtjackson.shared.ObjectMapperTester;
import com.github.nmorel.gwtjackson.shared.annotations.JsonAutoDetectTester;
import com.github.nmorel.gwtjackson.shared.annotations.JsonAutoDetectTester.BeanOne;
import com.google.gwt.core.client.GWT;

/**
 * @author Nicolas Morel
 */
public class JsonAutoDetectGwtTest extends GwtJacksonTestCase {

    public interface JsonAutoDetectMapper extends ObjectMapper<BeanOne>, ObjectMapperTester<BeanOne> {

        static JsonAutoDetectMapper INSTANCE = GWT.create( JsonAutoDetectMapper.class );
    }

    private JsonAutoDetectTester tester = JsonAutoDetectTester.INSTANCE;

    public void testSerializeAutoDetection() {
        tester.testSerializeAutoDetection( JsonAutoDetectMapper.INSTANCE );
    }

    public void testDeserializeAutoDetection() {
        tester.testDeserializeAutoDetection( createMapper( JsonAutoDetectMapper.INSTANCE, new JsonDeserializationContext.Builder()
                .failOnUnknownProperties( false ).build(), newDefaultSerializationContext() ) );
    }
}
