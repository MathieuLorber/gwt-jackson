package com.github.nmorel.gwtjackson.client.stream.jso;

import java.io.IOException;

import com.github.nmorel.gwtjackson.client.stream.JsonReader;
import com.github.nmorel.gwtjackson.client.stream.JsonToken;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsonUtils;

/**
 * @author Nicolas Morel
 */
public class JsoReader implements JsonReader {

    private final String input;

    private final JavaScriptObject jso;

    public JsoReader( String input ) {
        this.input = input;
        this.jso = JsonUtils.safeEval( input );
    }

    @Override
    public void beginArray() throws IOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void endArray() throws IOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void beginObject() throws IOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void endObject() throws IOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean hasNext() throws IOException {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public JsonToken peek() throws IOException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String nextName() throws IOException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String nextString() throws IOException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean nextBoolean() throws IOException {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void nextNull() throws IOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public double nextDouble() throws IOException {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public long nextLong() throws IOException {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int nextInt() throws IOException {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void close() throws IOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void skipValue() throws IOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int getLineNumber() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int getColumnNumber() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getInput() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String nextValue() throws IOException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setLenient( boolean lenient ) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
