package com.github.nmorel.gwtjackson.client.stream.json;

import com.github.nmorel.gwtjackson.client.stream.AbstractJsonReaderTest;
import com.github.nmorel.gwtjackson.client.stream.JsonReader;

/**
 * @author Nicolas Morel
 */
public class JSONReaderTest extends AbstractJsonReaderTest {

    @Override
    public JsonReader newJsonReader( String input ) {
        return new JSONReader( input );
    }
}
