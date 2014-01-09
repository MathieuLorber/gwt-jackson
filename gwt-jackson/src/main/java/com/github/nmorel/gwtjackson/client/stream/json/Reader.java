package com.github.nmorel.gwtjackson.client.stream.json;

import java.io.IOException;

import com.github.nmorel.gwtjackson.client.stream.JsonToken;
import com.google.gwt.json.client.JSONValue;

/**
 * @author Nicolas Morel
 */
public abstract class Reader {

    public abstract JSONValue getValue();

    public abstract boolean hasNext() throws IOException;

    public abstract JSONValue nextValue() throws IOException;

    public abstract void skipValue() throws IOException;

    public abstract String nextName() throws IOException;

    public String nextString() throws IOException {
        JSONValue value = nextValue();
        if(null == value.isString()) {
            throw new IllegalStateException( value + " is not a string" );
        }
        return value.isString().stringValue();
    }

    public boolean nextBoolean() throws IOException {
        JSONValue value = nextValue();
        if(null == value.isBoolean()) {
            throw new IllegalStateException( value + " is not a boolean" );
        }
        return value.isBoolean().booleanValue();
    }

    public void nextNull() throws IOException {
        JSONValue value = nextValue();
        if(null == value.isNull()) {
            throw new IllegalStateException( value + " is not null" );
        }
    }

    public double nextDouble() throws IOException {
        JSONValue value = nextValue();
        if(null == value.isNumber()) {
            throw new IllegalStateException( value + " is not a number" );
        }
        return value.isNumber().doubleValue();
    }

    public long nextLong() throws IOException {
        JSONValue value = nextValue();
        if(null == value.isNumber()) {
            throw new IllegalStateException( value + " is not a number" );
        }
        return Double.valueOf( value.isNumber().doubleValue() ).longValue();
    }

    public int nextInt() throws IOException {
        JSONValue value = nextValue();
        if(null == value.isNumber()) {
            throw new IllegalStateException( value + " is not a number" );
        }
        return Double.valueOf( value.isNumber().doubleValue() ).intValue();
    }

    public abstract JsonToken peek();
}
