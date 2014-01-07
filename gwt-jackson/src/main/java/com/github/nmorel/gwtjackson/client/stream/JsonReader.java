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

public interface JsonReader
{
    void beginArray() throws IOException;

    void endArray() throws IOException;

    void beginObject() throws IOException;

    void endObject() throws IOException;

    boolean hasNext() throws IOException;

    JsonToken peek() throws IOException;

    String nextName() throws IOException;

    String nextString() throws IOException;

    boolean nextBoolean() throws IOException;

    void nextNull() throws IOException;

    double nextDouble() throws IOException;

    long nextLong() throws IOException;

    int nextInt() throws IOException;

    void close() throws IOException;

    void skipValue() throws IOException;

    int getLineNumber();

    int getColumnNumber();

    String getInput();

    String nextValue() throws IOException;

    void setLenient( boolean lenient );
}
