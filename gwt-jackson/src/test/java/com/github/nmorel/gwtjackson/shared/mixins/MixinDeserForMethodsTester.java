/*
 * Copyright 2014 Nicolas Morel
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

package com.github.nmorel.gwtjackson.shared.mixins;

import java.io.IOException;
import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.nmorel.gwtjackson.shared.AbstractTester;
import com.github.nmorel.gwtjackson.shared.ObjectReaderTester;

public final class MixinDeserForMethodsTester extends AbstractTester {
    /*
    /**********************************************************
    /* Helper bean classes
    /**********************************************************
     */

    public static class BaseClass {

        protected HashMap<String, Object> values = new HashMap<String, Object>();

        public BaseClass() { }

        protected void addValue( String key, Object value ) {
            values.put( key, value );
        }
    }

    public interface MixIn {

        @JsonAnySetter
        void addValue( String key, Object value );
    }

    public static final MixinDeserForMethodsTester INSTANCE = new MixinDeserForMethodsTester();

    private MixinDeserForMethodsTester() {
    }

    /*
    /**********************************************************
    /* Unit tests
    /**********************************************************
     */

    /**
     * Unit test that verifies that we can mix in @JsonAnySetter
     * annotation, as expected.
     */
    public void testWithAnySetter(ObjectReaderTester<BaseClass> reader) {
        BaseClass result = reader.read( "{ \"a\" : 3, \"b\" : true }");
        assertNotNull( result );
        assertEquals( 2, result.values.size() );
        assertEquals( Integer.valueOf( 3 ), result.values.get( "a" ) );
        assertEquals( Boolean.TRUE, result.values.get( "b" ) );
    }
}
