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

package com.github.nmorel.gwtjackson.client.ser;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Iterator;

import com.github.nmorel.gwtjackson.client.JsonSerializationContext;
import com.github.nmorel.gwtjackson.client.JsonSerializer;
import com.github.nmorel.gwtjackson.client.JsonSerializerParameters;
import com.github.nmorel.gwtjackson.client.stream.JsonWriter;

/**
 * Default {@link JsonSerializer} implementation for {@link Iterable}.
 *
 * @param <T> Type of the elements inside the {@link Iterable}
 *
 * @author Nicolas Morel
 */
public class IterableJsonSerializer<I extends Iterable<T>, T> extends JsonSerializer<I> {

    /**
     * @param serializer {@link JsonSerializer} used to serialize the objects inside the {@link Iterable}.
     * @param <T> Type of the elements inside the {@link Iterable}
     *
     * @return a new instance of {@link IterableJsonSerializer}
     */
    public static <I extends Iterable<T>, T> IterableJsonSerializer<I, T> newInstance( JsonSerializer<T> serializer ) {
        return new IterableJsonSerializer<I, T>( serializer );
    }

    protected final JsonSerializer<T> serializer;

    /**
     * @param serializer {@link JsonSerializer} used to serialize the objects inside the {@link Iterable}.
     */
    protected IterableJsonSerializer( JsonSerializer<T> serializer ) {
        if ( null == serializer ) {
            throw new IllegalArgumentException( "serializer cannot be null" );
        }
        this.serializer = serializer;
    }

    @Override
    public void doSerialize( JsonWriter writer, @Nonnull I values, JsonSerializationContext ctx, JsonSerializerParameters params ) throws
        IOException {
        Iterator<T> iterator = values.iterator();

        if ( !iterator.hasNext() ) {
            if ( ctx.isWriteEmptyJsonArrays() ) {
                writer.beginArray();
                writer.endArray();
            } else {
                writer.cancelName();
            }
            return;
        }

        if ( ctx.isWriteSingleElemArraysUnwrapped() ) {

            T first = iterator.next();

            if ( iterator.hasNext() ) {
                // there is more than one element, we write the array normally
                writer.beginArray();
                serializer.serialize( writer, first, ctx, params );
                while ( iterator.hasNext() ) {
                    serializer.serialize( writer, iterator.next(), ctx, params );
                }
                writer.endArray();
            } else {
                // there is only one element, we write it directly
                serializer.serialize( writer, first, ctx, params );
            }

        } else {
            writer.beginArray();
            while ( iterator.hasNext() ) {
                serializer.serialize( writer, iterator.next(), ctx, params );
            }
            writer.endArray();
        }
    }
}
