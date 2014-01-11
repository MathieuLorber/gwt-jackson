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

package com.github.nmorel.gwtjackson.client.deser.bean;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.github.nmorel.gwtjackson.client.JsonDeserializationContext;
import com.github.nmorel.gwtjackson.client.JsonDeserializer;
import com.github.nmorel.gwtjackson.client.JsonDeserializerParameters;
import com.github.nmorel.gwtjackson.client.stream.JsonReader;
import com.github.nmorel.gwtjackson.client.stream.JsonToken;
import com.github.nmorel.gwtjackson.client.utils.FastMap;

/**
 * Base implementation of {@link JsonDeserializer} for beans.
 *
 * @author Nicolas Morel
 */
public abstract class AbstractBeanJsonDeserializer<T> extends JsonDeserializer<T> {

    private final FastMap<BeanPropertyDeserializer<T, ?>> deserializers = FastMap.createObject().cast();

    private final FastMap<BackReferenceProperty<T, ?>> backReferenceDeserializers = FastMap.createObject().cast();

    private final Map<Class<? extends T>, SubtypeDeserializer<? extends T>> subtypeClassToDeserializer = new IdentityHashMap<Class<?
            extends T>, SubtypeDeserializer<? extends T>>();

    private final Set<String> defaultIgnoredProperties = new HashSet<String>();

    private boolean defaultIgnoreUnknown = false;

    private final Set<String> requiredProperties = new HashSet<String>();

    private final InstanceBuilder<T> instanceBuilder;

    private final IdentityDeserializationInfo<T> defaultIdentityInfo;

    private final TypeDeserializationInfo<T> defaultTypeInfo;

    protected AbstractBeanJsonDeserializer( InstanceBuilder<T> instanceBuilder, IdentityDeserializationInfo<T> defaultIdentityInfo,
                                            TypeDeserializationInfo<T> defaultTypeInfo ) {
        this.instanceBuilder = instanceBuilder;
        this.defaultIdentityInfo = defaultIdentityInfo;
        this.defaultTypeInfo = defaultTypeInfo;
    }

    public abstract Class getDeserializedType();

    /**
     * Add a {@link BeanPropertyDeserializer}
     *
     * @param propertyName name of the property
     * @param deserializer deserializer
     */
    protected final void addPropertyDeserializer( String propertyName, boolean required, BeanPropertyDeserializer<T, ?> deserializer ) {
        deserializers.put( propertyName, deserializer );
        if ( required ) {
            requiredProperties.add( propertyName );
        }
    }

    /**
     * Add a {@link BackReferenceProperty}
     *
     * @param referenceName name of the reference
     * @param backReference backReference
     */
    protected final void addBackReferenceDeserializer( String referenceName, BackReferenceProperty<T, ?> backReference ) {
        backReferenceDeserializers.put( referenceName, backReference );
    }

    /**
     * Adds a {@link SubtypeDeserializer}.
     *
     * @param clazz {@link Class} associated to the deserializer
     * @param subtypeDeserializer the deserializer
     */
    protected <S extends T> void addSubtypeDeserializer( Class<S> clazz, SubtypeDeserializer<S> subtypeDeserializer ) {
        subtypeClassToDeserializer.put( clazz, subtypeDeserializer );
    }

    /**
     * Add an ignored property
     *
     * @param propertyName name of the property
     */
    protected final void addIgnoredProperty( String propertyName ) {
        defaultIgnoredProperties.add( propertyName );
    }

    /**
     * Defines whether encountering of unknown
     * properties should result in a failure (by throwing a
     * {@link com.github.nmorel.gwtjackson.client.exception.JsonDeserializationException}) or not.
     */
    protected void setIgnoreUnknown( boolean ignoreUnknown ) {
        this.defaultIgnoreUnknown = ignoreUnknown;
    }

    @Override
    public T doDeserialize( JsonReader reader, JsonDeserializationContext ctx, JsonDeserializerParameters params ) throws IOException {

        // Processing the parameters. We fallback to default if parameter is not present.
        final IdentityDeserializationInfo identityInfo = null == params.getIdentityInfo() ? defaultIdentityInfo : params.getIdentityInfo();
        final TypeDeserializationInfo typeInfo = null == params.getTypeInfo() ? defaultTypeInfo : params.getTypeInfo();
        final boolean ignoreUnknown = params.isIgnoreUnknown() || defaultIgnoreUnknown;
        final Set<String> ignoredProperties;
        if ( null == params.getIgnoredProperties() ) {
            ignoredProperties = defaultIgnoredProperties;
        } else {
            ignoredProperties = new HashSet<String>( defaultIgnoredProperties );
            ignoredProperties.addAll( params.getIgnoredProperties() );
        }

        // If it's not a json object, it must be an identifier
        if ( null != identityInfo && !JsonToken.BEGIN_OBJECT.equals( reader.peek() ) ) {
            Object id;
            if ( identityInfo.isProperty() ) {
                BeanPropertyDeserializer<T, ?> propertyDeserializer = deserializers.get( identityInfo.getPropertyName() );
                id = propertyDeserializer.getDeserializer( ctx ).deserialize( reader, ctx );
            } else {
                id = identityInfo.readId( reader, ctx );
            }
            Object instance = ctx.getObjectWithId( identityInfo.newIdKey( id ) );
            if ( null == instance ) {
                throw ctx.traceError( "Cannot find an object with id " + id, reader );
            }
            return (T) instance;
        }

        T result;

        if ( null != typeInfo ) {
            switch ( typeInfo.getInclude() ) {
                case PROPERTY:
                    // the type info is the first property of the object
                    reader.beginObject();
                    String name = reader.nextName();
                    if ( !typeInfo.getPropertyName().equals( name ) ) {
                        // the type info is always the first value. If we don't find it, we throw an error
                        throw ctx.traceError( "Cannot find the type info", reader );
                    }
                    String typeInfoProperty = reader.nextString();
                    result = deserializeSubtype( reader, ctx, typeInfoProperty, identityInfo, typeInfo, ignoreUnknown, ignoredProperties );
                    reader.endObject();
                    break;

                case WRAPPER_OBJECT:
                    // type info is included in a wrapper object that contains only one property. The name of this property is the type
                    // info and the value the object
                    reader.beginObject();
                    String typeInfoWrapObj = reader.nextName();
                    reader.beginObject();
                    result = deserializeSubtype( reader, ctx, typeInfoWrapObj, identityInfo, typeInfo, ignoreUnknown, ignoredProperties );
                    reader.endObject();
                    reader.endObject();
                    break;

                case WRAPPER_ARRAY:
                    // type info is included in a wrapper array that contains two elements. First one is the type
                    // info and the second one the object
                    reader.beginArray();
                    String typeInfoWrapArray = reader.nextString();
                    reader.beginObject();
                    result = deserializeSubtype( reader, ctx, typeInfoWrapArray, identityInfo, typeInfo, ignoreUnknown, ignoredProperties );
                    reader.endObject();
                    reader.endArray();
                    break;

                default:
                    throw ctx.traceError( "JsonTypeInfo.As." + typeInfo.getInclude() + " is not supported", reader );
            }
        } else if ( null != instanceBuilder ) {
            reader.beginObject();
            result = deserializeObject( reader, ctx, identityInfo, typeInfo, ignoreUnknown, ignoredProperties );
            reader.endObject();
        } else {
            throw ctx.traceError( "Cannot instantiate the type " + getDeserializedType().getName(), reader );
        }

        return result;
    }

    /**
     * Deserializes all the properties of the bean. The {@link JsonReader} must be in a json object.
     *
     * @param reader reader
     * @param ctx context of the deserialization process
     *
     * @throws IOException if an error occurs while reading a property
     */
    public final T deserializeObject( final JsonReader reader, final JsonDeserializationContext ctx,
                                      IdentityDeserializationInfo identityInfo, TypeDeserializationInfo typeInfo, boolean ignoreUnknown,
                                      Set<String> ignoredProperties ) throws IOException {

        // we will remove the properties read from this list and check at the end it's empty
        Set<String> requiredPropertiesLeft = requiredProperties.isEmpty() ? Collections
                .<String>emptySet() : new HashSet<String>( requiredProperties );

        // we first instantiate the bean. It might buffer properties if there are properties required for constructor and they are not in
        // first position
        Instance<T> instance = instanceBuilder.newInstance( reader, ctx );

        // we then look for identity. It can also buffer properties it is not in current reader position.
        readIdentityProperty( identityInfo, instance, reader, ctx, ignoredProperties );

        // we flush any buffered properties
        flushBufferedProperties( instance, requiredPropertiesLeft, ctx, ignoreUnknown, ignoredProperties );

        T bean = instance.getInstance();

        while ( JsonToken.NAME.equals( reader.peek() ) ) {
            String propertyName = reader.nextName();

            requiredPropertiesLeft.remove( propertyName );

            if ( ignoredProperties.contains( propertyName ) ) {
                reader.skipValue();
                continue;
            }

            BeanPropertyDeserializer<T, ?> property = getPropertyDeserializer( propertyName, ctx, ignoreUnknown );
            if ( null == property ) {
                reader.skipValue();
            } else {
                property.deserialize( reader, bean, ctx );
            }
        }

        if ( !requiredPropertiesLeft.isEmpty() ) {
            throw ctx.traceError( "Required properties are missing : " + requiredPropertiesLeft, reader );
        }
        return bean;
    }

    private void readIdentityProperty( IdentityDeserializationInfo identityInfo, Instance<T> instance, JsonReader reader,
                                       final JsonDeserializationContext ctx, Set<String> ignoredProperties ) throws IOException {
        if ( null == identityInfo ) {
            return;
        }

        JsonReader identityReader = null;

        // we look if it has not been already buffered
        String propertyValue = instance.getBufferedProperties().remove( identityInfo.getPropertyName() );
        if ( null != propertyValue ) {
            identityReader = ctx.newJsonReader( propertyValue );
        } else {
            // we search for the identity property
            while ( JsonToken.NAME.equals( reader.peek() ) ) {
                String name = reader.nextName();

                if ( ignoredProperties.contains( name ) ) {
                    reader.skipValue();
                    continue;
                }

                if ( identityInfo.getPropertyName().equals( name ) ) {
                    identityReader = reader;
                    break;
                } else {
                    instance.getBufferedProperties().put( name, reader.nextValue() );
                }
            }
        }

        if ( null != identityReader ) {
            Object id;
            if ( identityInfo.isProperty() ) {
                BeanPropertyDeserializer propertyDeserializer = deserializers.get( identityInfo.getPropertyName() );
                id = propertyDeserializer.getDeserializer( ctx ).deserialize( identityReader, ctx );
                if ( null != id ) {
                    propertyDeserializer.setValue( instance.getInstance(), id, ctx );
                }
            } else {
                id = identityInfo.readId( identityReader, ctx );
            }
            if ( null != id ) {
                ctx.addObjectId( identityInfo.newIdKey( id ), instance.getInstance() );
            }
        }
    }

    private void flushBufferedProperties( Instance<T> instance, Set<String> requiredPropertiesLeft, JsonDeserializationContext ctx,
                                          boolean ignoreUnknown, Set<String> ignoredProperties ) {
        if ( !instance.getBufferedProperties().isEmpty() ) {
            for ( Entry<String, String> bufferedProperty : instance.getBufferedProperties().entrySet() ) {
                String propertyName = bufferedProperty.getKey();

                requiredPropertiesLeft.remove( propertyName );

                if ( ignoredProperties.contains( propertyName ) ) {
                    continue;
                }

                BeanPropertyDeserializer<T, ?> property = getPropertyDeserializer( propertyName, ctx, ignoreUnknown );
                if ( null != property ) {
                    property.deserialize( ctx.newJsonReader( bufferedProperty.getValue() ), instance.getInstance(), ctx );
                }
            }
            instance.getBufferedProperties().clear();
        }
    }

    private BeanPropertyDeserializer<T, ?> getPropertyDeserializer( String propertyName, JsonDeserializationContext ctx,
                                                                    boolean ignoreUnknown ) {
        BeanPropertyDeserializer<T, ?> property = deserializers.get( propertyName );
        if ( null == property ) {
            if ( !ignoreUnknown && ctx.isFailOnUnknownProperties() ) {
                throw ctx.traceError( "Unknown property '" + propertyName + "'" );
            }
        }
        return property;
    }

    public final T deserializeSubtype( JsonReader reader, JsonDeserializationContext ctx, String typeInformation,
                                       IdentityDeserializationInfo identityInfo, TypeDeserializationInfo typeInfo, boolean ignoreUnknown,
                                       Set<String> ignoredProperties ) throws IOException {
        Class typeClass = typeInfo.getTypeClass( typeInformation );
        if ( null == typeClass ) {
            throw ctx.traceError( "Could not find the type associated to " + typeInformation, reader );
        }

        return getDeserializer( reader, ctx, typeClass )
                .deserializeObject( reader, ctx, identityInfo, typeInfo, ignoreUnknown, ignoredProperties );
    }

    private AbstractBeanJsonDeserializer<T> getDeserializer( JsonReader reader, JsonDeserializationContext ctx, Class typeClass ) {
        if ( typeClass == getDeserializedType() ) {
            return this;
        }

        SubtypeDeserializer deserializer = subtypeClassToDeserializer.get( typeClass );
        if ( null == deserializer ) {
            throw ctx.traceError( "No deserializer found for the type " + typeClass.getName(), reader );
        }
        return (AbstractBeanJsonDeserializer<T>) deserializer.getDeserializer( ctx );
    }

    @Override
    public void setBackReference( String referenceName, Object reference, T value, JsonDeserializationContext ctx ) {
        if ( null == value ) {
            return;
        }

        AbstractBeanJsonDeserializer<T> deserializer = getDeserializer( null, ctx, value.getClass() );
        if ( deserializer.getClass() != getClass() ) {
            // we test if it's not this deserializer to avoid an infinite loop
            deserializer.setBackReference( referenceName, reference, value, ctx );
            return;
        }

        BackReferenceProperty backReferenceProperty = backReferenceDeserializers.get( referenceName );
        if ( null == backReferenceProperty ) {
            throw ctx.traceError( "The back reference '" + referenceName + "' does not exist" );
        }
        backReferenceProperty.setBackReference( value, reference, ctx );
    }
}
