package com.github.nmorel.gwtjackson.client.stream.json;

import java.io.IOException;
import java.util.Stack;

import com.github.nmorel.gwtjackson.client.stream.JsonReader;
import com.github.nmorel.gwtjackson.client.stream.JsonToken;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;

/**
 * @author Nicolas Morel
 */
public class JSONReader implements JsonReader {

    private final String input;

    private final JSONValue root;

    private final Stack<Reader> stack = new Stack<Reader>();

    private Reader currentReader;

    public JSONReader( String input ) {
        this.input = input;
        this.root = JSONParser.parseStrict( input );
    }

    @Override
    public void beginArray() throws IOException {
        if ( null == currentReader ) {
            JSONArray array = root.isArray();
            if ( null == array ) {
                throw new IOException( "root element isn't an array : " + root );
            }
            currentReader = new JSONArrayReader( array );
        } else {
            JSONValue value = currentReader.getValue();
            JSONArray array = value.isArray();
            if ( null == array ) {
                throw new IOException( "current element isn't an array : " + value );
            }
            stack.push( currentReader );
            currentReader = new JSONArrayReader( array );
        }
    }

    @Override
    public void endArray() throws IOException {
        if ( null == currentReader || null == currentReader.getValue().isArray() ) {
            throw new IOException( "not in an array" );
        }
        if ( stack.isEmpty() ) {
            // end of the root
        } else {
            currentReader = stack.pop();
        }
    }

    @Override
    public void beginObject() throws IOException {
        if ( null == currentReader ) {
            JSONObject object = root.isObject();
            if ( null == object ) {
                throw new IOException( "root element isn't an object : " + root );
            }
            currentReader = new JSONObjectReader( object );
        } else {
            JSONValue value = currentReader.getValue();
            JSONObject object = value.isObject();
            if ( null == object ) {
                throw new IOException( "current element isn't an object : " + value );
            }
            stack.push( currentReader );
            currentReader = new JSONObjectReader( object );
        }
    }

    @Override
    public void endObject() throws IOException {
        if ( null == currentReader || null == currentReader.getValue().isObject() ) {
            throw new IOException( "not in an object" );
        }
        if ( stack.isEmpty() ) {
            // end of the root
        } else {
            currentReader = stack.pop();
        }
    }

    @Override
    public boolean hasNext() throws IOException {
        return null != currentReader && currentReader.hasNext();
    }

    @Override
    public JsonToken peek() throws IOException {
        if ( null == currentReader ) {
            if ( null != root.isArray() ) {
                return JsonToken.BEGIN_ARRAY;
            }
            if ( null != root.isObject() ) {
                return JsonToken.BEGIN_OBJECT;
            }
            if ( null != root.isBoolean() ) {
                return JsonToken.BOOLEAN;
            }
            if ( null != root.isNull() ) {
                return JsonToken.NULL;
            }
            if ( null != root.isNumber() ) {
                return JsonToken.NUMBER;
            }
            if ( null != root.isString() ) {
                return JsonToken.STRING;
            }
        } else {
            return currentReader.peek();
        }
        return JsonToken.END_DOCUMENT;
    }

    @Override
    public String nextName() throws IOException {
        if ( null == currentReader || null == currentReader.getValue().isObject() ) {
            throw new IOException( "not in an object" );
        }
        return currentReader.nextName();
    }

    @Override
    public String nextString() throws IOException {
        if ( null == currentReader ) {
            JSONString string = root.isString();
            if ( null == string ) {
                throw new IOException( "not a string : " + root );
            }
            return string.stringValue();
        }
        return currentReader.nextString();
    }

    @Override
    public boolean nextBoolean() throws IOException {
        if ( null == currentReader ) {
            JSONBoolean bool = root.isBoolean();
            if ( null == bool ) {
                throw new IOException( "not a boolean : " + root );
            }
            return bool.booleanValue();
        }
        return currentReader.nextBoolean();
    }

    @Override
    public void nextNull() throws IOException {
        if ( null == currentReader ) {
            if ( null != root.isNull() ) {
                throw new IOException( "not a null : " + root );
            }
            return;
        }
        currentReader.nextNull();
    }

    @Override
    public double nextDouble() throws IOException {
        if ( null == currentReader ) {
            JSONNumber number = root.isNumber();
            if ( null == number ) {
                throw new IOException( "not a number : " + root );
            }
            return number.doubleValue();
        }
        return currentReader.nextDouble();
    }

    @Override
    public long nextLong() throws IOException {
        if ( null == currentReader ) {
            JSONNumber number = root.isNumber();
            if ( null == number ) {
                throw new IOException( "not a number : " + root );
            }
            return Double.valueOf( number.doubleValue() ).longValue();
        }
        return currentReader.nextLong();
    }

    @Override
    public int nextInt() throws IOException {
        if ( null == currentReader ) {
            JSONNumber number = root.isNumber();
            if ( null == number ) {
                throw new IOException( "not a number : " + root );
            }
            return Double.valueOf( number.doubleValue() ).intValue();
        }
        return currentReader.nextInt();
    }

    @Override
    public void close() throws IOException {
    }

    @Override
    public void skipValue() throws IOException {
        if ( null != currentReader ) {
            currentReader.skipValue();
        }
    }

    @Override
    public int getLineNumber() {
        return 0;
    }

    @Override
    public int getColumnNumber() {
        return 0;
    }

    @Override
    public String getInput() {
        return input;
    }

    @Override
    public String nextValue() throws IOException {
        if ( null == currentReader ) {
            return root.toString();
        }
        return currentReader.nextValue().toString();
    }

    @Override
    public void setLenient( boolean lenient ) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
