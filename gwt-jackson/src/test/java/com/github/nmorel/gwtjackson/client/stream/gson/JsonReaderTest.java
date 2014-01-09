package com.github.nmorel.gwtjackson.client.stream.gson;

import com.github.nmorel.gwtjackson.client.stream.*;
import com.github.nmorel.gwtjackson.client.stream.JsonReader;

/**
 * @author Nicolas Morel
 */
public class JsonReaderTest extends AbstractJsonReaderTest {

    @Override
    public JsonReader newJsonReader( String input ) {
        return new JsonReaderNonBuffered( input );
    }
}
