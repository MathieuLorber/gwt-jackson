package com.github.nmorel.gwtjackson.client.stream.json;

import java.io.IOException;
import java.util.Iterator;

import com.github.nmorel.gwtjackson.client.stream.JsonToken;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;

/**
 * @author Nicolas Morel
 */
public class JSONObjectReader extends Reader {

    private final JSONObject object;

    private final Iterator<String> iterator;

    private String name;

    public JSONObjectReader( JSONObject value ) {
        this.object = value;
        this.iterator = object.keySet().iterator();
    }

    @Override
    public JSONValue getValue() {
        return object;
    }

    @Override
    public boolean hasNext() throws IOException {
        return iterator.hasNext() || null != name;
    }

    @Override
    public String nextName() throws IOException {
        name = iterator.next();
        return name;
    }

    @Override
    public JsonToken peek() {
        if ( null == name ) {
            if ( iterator.hasNext() ) {
                return JsonToken.NAME;
            } else {
                return JsonToken.END_OBJECT;
            }
        }

        JSONValue value = object.get( name );
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
        throw new IllegalArgumentException( "bad formatted object : " + object );
    }

    @Override
    public JSONValue nextValue() throws IOException {
        JSONValue value = object.get( name );
        name = null;
        return value;
    }

    @Override
    public void skipValue() throws IOException {
        name = null;
    }
}
