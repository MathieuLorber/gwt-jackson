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

package com.github.nmorel.gwtjackson.client.ser.map;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import com.github.nmorel.gwtjackson.client.JsonSerializationContext;
import com.github.nmorel.gwtjackson.client.JsonSerializer;
import com.github.nmorel.gwtjackson.client.JsonSerializerParameters;
import com.github.nmorel.gwtjackson.client.ser.map.key.KeySerializer;
import com.github.nmorel.gwtjackson.client.stream.JsonWriter;

/**
 * Default {@link JsonSerializer} implementation for {@link Map}.
 *
 * @param <M> Type of the {@link Map}
 * @param <K> Type of the keys inside the {@link Map}
 * @param <V> Type of the values inside the {@link Map}
 *
 * @author Nicolas Morel
 */
public class MapJsonSerializer<M extends Map<K, V>, K, V> extends JsonSerializer<M> {

    /**
     * @param keySerializer {@link KeySerializer} used to serialize the keys.
     * @param valueSerializer {@link JsonSerializer} used to serialize the values.
     * @param <M> Type of the {@link Map}
     * @param <K> Type of the keys inside the {@link Map}
     * @param <V> Type of the values inside the {@link Map}
     *
     * @return a new instance of {@link MapJsonSerializer}
     */
    public static <M extends Map<K, V>, K, V> MapJsonSerializer<M, K, V> newInstance( KeySerializer<K> keySerializer,
                                                                                      JsonSerializer<V> valueSerializer ) {
        return new MapJsonSerializer<M, K, V>( keySerializer, valueSerializer );
    }

    protected final KeySerializer<K> keySerializer;

    protected final JsonSerializer<V> valueSerializer;

    /**
     * @param keySerializer {@link KeySerializer} used to serialize the keys.
     * @param valueSerializer {@link JsonSerializer} used to serialize the values.
     */
    protected MapJsonSerializer( KeySerializer<K> keySerializer, JsonSerializer<V> valueSerializer ) {
        if ( null == keySerializer ) {
            throw new IllegalArgumentException( "keySerializer cannot be null" );
        }
        if ( null == valueSerializer ) {
            throw new IllegalArgumentException( "valueSerializer cannot be null" );
        }
        this.keySerializer = keySerializer;
        this.valueSerializer = valueSerializer;
    }

    @Override
    public void doSerialize( JsonWriter writer, @Nonnull M values, JsonSerializationContext ctx, JsonSerializerParameters params ) throws
        IOException {
        writer.beginObject();

        if ( !values.isEmpty() ) {
            Map<K, V> map = values;
            if ( ctx.isOrderMapEntriesByKeys() && !(values instanceof SortedMap<?, ?>) ) {
                map = new TreeMap<K, V>( map );
            }

            if ( ctx.isWriteNullMapValues() ) {

                for ( Entry<K, V> entry : map.entrySet() ) {
                    writer.name( keySerializer.serialize( entry.getKey(), ctx ) );
                    valueSerializer.serialize( writer, entry.getValue(), ctx, params );
                }

            } else {

                for ( Entry<K, V> entry : map.entrySet() ) {
                    if ( null != entry.getValue() ) {
                        writer.name( keySerializer.serialize( entry.getKey(), ctx ) );
                        valueSerializer.serialize( writer, entry.getValue(), ctx, params );
                    }
                }

            }
        }
        writer.endObject();
    }
}
