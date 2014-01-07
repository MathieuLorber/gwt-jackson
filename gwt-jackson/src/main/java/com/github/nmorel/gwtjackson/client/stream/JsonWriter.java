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

package com.github.nmorel.gwtjackson.client.stream;

import java.io.IOException;

public interface JsonWriter {

    void setIndent( String indent );

    void setSerializeNulls( boolean serializeNulls );

    boolean getSerializeNulls();

    JsonWriter beginArray() throws IOException;

    JsonWriter endArray() throws IOException;

    JsonWriter beginObject() throws IOException;

    JsonWriter endObject() throws IOException;

    JsonWriter name( String name ) throws IOException;

    JsonWriter value( String value ) throws IOException;

    JsonWriter nullValue() throws IOException;

    JsonWriter cancelName();

    JsonWriter value( boolean value ) throws IOException;

    JsonWriter value( double value ) throws IOException;

    JsonWriter value( long value ) throws IOException;

    JsonWriter value( Number value ) throws IOException;

    JsonWriter rawValue( Object value ) throws IOException;

    void flush() throws IOException;

    void close() throws IOException;

    String getOutput();
}
