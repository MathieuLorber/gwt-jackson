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
        return value.isString().stringValue();
    }

    public boolean nextBoolean() throws IOException {
        JSONValue value = nextValue();
        return value.isBoolean().booleanValue();
    }

    public void nextNull() throws IOException {
        JSONValue value = nextValue();
    }

    public double nextDouble() throws IOException {
        JSONValue value = nextValue();
        return value.isNumber().doubleValue();
    }

    public long nextLong() throws IOException {
        JSONValue value = nextValue();
        return Double.valueOf( value.isNumber().doubleValue() ).longValue();
    }

    public int nextInt() throws IOException {
        JSONValue value = nextValue();
        return Double.valueOf( value.isNumber().doubleValue() ).intValue();
    }

    public abstract JsonToken peek();
}
