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

package com.github.nmorel.gwtjackson.client.ser.array.dd;

import javax.annotation.Nonnull;

import com.github.nmorel.gwtjackson.client.JsonSerializationContext;
import com.github.nmorel.gwtjackson.client.JsonSerializer;
import com.github.nmorel.gwtjackson.client.JsonSerializerParameters;
import com.github.nmorel.gwtjackson.client.stream.JsonWriter;

/**
 * Default {@link JsonSerializer} implementation for 2D array of float.
 *
 * @author Nicolas Morel
 */
public class PrimitiveFloatArray2dJsonSerializer extends JsonSerializer<float[][]> {

    private static final PrimitiveFloatArray2dJsonSerializer INSTANCE = new PrimitiveFloatArray2dJsonSerializer();

    /**
     * @return an instance of {@link PrimitiveFloatArray2dJsonSerializer}
     */
    public static PrimitiveFloatArray2dJsonSerializer getInstance() {
        return INSTANCE;
    }

    private PrimitiveFloatArray2dJsonSerializer() { }

    @Override
    public void doSerialize( JsonWriter writer, @Nonnull float[][] values, JsonSerializationContext ctx, JsonSerializerParameters params ) {
        if ( !ctx.isWriteEmptyJsonArrays() && values.length == 0 ) {
            writer.cancelName();
            return;
        }

        writer.beginArray();
        for ( float[] array : values ) {
            writer.beginArray();
            for ( float value : array ) {
                writer.value( value );
            }
            writer.endArray();
        }
        writer.endArray();
    }
}
