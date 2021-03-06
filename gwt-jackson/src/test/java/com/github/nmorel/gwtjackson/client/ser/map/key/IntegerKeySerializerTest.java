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

package com.github.nmorel.gwtjackson.client.ser.map.key;

import com.github.nmorel.gwtjackson.client.ser.map.key.BaseNumberKeySerializer.IntegerKeySerializer;

/**
 * @author Nicolas Morel
 */
public class IntegerKeySerializerTest extends AbstractKeySerializerTest<Integer> {

    @Override
    protected IntegerKeySerializer createSerializer() {
        return IntegerKeySerializer.getInstance();
    }

    public void testSerializeValue() {
        assertSerialization( "34", 34 );
        assertSerialization( "-1", -1 );
        assertSerialization( "-2147483648", Integer.MIN_VALUE );
        assertSerialization( "2147483647", Integer.MAX_VALUE );
    }
}
