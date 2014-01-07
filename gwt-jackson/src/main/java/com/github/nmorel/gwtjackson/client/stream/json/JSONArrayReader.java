package com.github.nmorel.gwtjackson.client.stream.json;

import java.io.IOException;

import com.github.nmorel.gwtjackson.client.stream.JsonToken;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONValue;

/**
 * @author Nicolas Morel
 */
public class JSONArrayReader extends Reader {

    private final JSONArray array;

    private int pos;

    public JSONArrayReader( JSONArray value ) {
        this.array = value;
        this.pos = 0;
    }

    @Override
    public JSONValue getValue() {
        return array;
    }

    @Override
    public boolean hasNext() throws IOException {
        return array.size() > pos;
    }

    @Override
    public String nextName() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public JsonToken peek() {
        if ( array.size() > pos ) {
            JSONValue value = array.get( pos );
            if ( null != value.isArray() ) {
                return JsonToken.BEGIN_ARRAY;
            }
            if ( null != value.isObject() ) {
                return JsonToken.BEGIN_OBJECT;
            }
            if ( null != value.isBoolean() ) {
                return JsonToken.BOOLEAN;
            }
            if ( null != value.isNull() ) {
                return JsonToken.NULL;
            }
            if ( null != value.isNumber() ) {
                return JsonToken.NUMBER;
            }
            if ( null != value.isString() ) {
                return JsonToken.STRING;
            }
            throw new IllegalArgumentException( "bad formatted array : " + array );
        } else {
            return JsonToken.END_ARRAY;
        }
    }

    @Override
    public JSONValue nextValue() throws IOException {
        return array.get( pos++ );
    }

    @Override
    public void skipValue() throws IOException {
        pos++;
    }
}
