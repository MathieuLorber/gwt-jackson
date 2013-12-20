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

package com.github.nmorel.gwtjackson.rebind;

import java.io.PrintWriter;

import com.github.nmorel.gwtjackson.client.JsonDeserializer;
import com.github.nmorel.gwtjackson.client.JsonSerializer;
import com.github.nmorel.gwtjackson.client.deser.array.ArrayJsonDeserializer.ArrayCreator;
import com.github.nmorel.gwtjackson.client.deser.bean.AbstractBeanJsonDeserializer;
import com.github.nmorel.gwtjackson.client.deser.map.key.KeyDeserializer;
import com.github.nmorel.gwtjackson.client.ser.bean.AbstractBeanJsonSerializer;
import com.github.nmorel.gwtjackson.client.ser.map.key.KeySerializer;
import com.github.nmorel.gwtjackson.rebind.RebindConfiguration.MapperInstance;
import com.github.nmorel.gwtjackson.rebind.RebindConfiguration.MapperType;
import com.github.nmorel.gwtjackson.rebind.type.JDeserializerType;
import com.github.nmorel.gwtjackson.rebind.type.JSerializerType;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.TreeLogger.Type;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JArrayType;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JEnumType;
import com.google.gwt.core.ext.typeinfo.JParameterizedType;
import com.google.gwt.core.ext.typeinfo.JType;
import com.google.gwt.core.ext.typeinfo.JTypeParameter;
import com.google.gwt.thirdparty.guava.common.base.Optional;
import com.google.gwt.user.rebind.AbstractSourceCreator;
import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;
import com.google.gwt.user.rebind.SourceWriter;

/**
 * @author Nicolas Morel
 */
public abstract class AbstractCreator extends AbstractSourceCreator {

    protected static final String JSON_DESERIALIZER_CLASS = "com.github.nmorel.gwtjackson.client.JsonDeserializer";

    protected static final String JSON_SERIALIZER_CLASS = "com.github.nmorel.gwtjackson.client.JsonSerializer";

    protected static final String JSON_DESERIALIZATION_CONTEXT_CLASS = "com.github.nmorel.gwtjackson.client.JsonDeserializationContext";

    protected static final String JSON_SERIALIZATION_CONTEXT_CLASS = "com.github.nmorel.gwtjackson.client.JsonSerializationContext";

    protected static final String TYPE_PARAMETER_DESERIALIZER_FIELD_NAME = "deserializer%d";

    protected static final String TYPE_PARAMETER_SERIALIZER_FIELD_NAME = "serializer%d";

    protected final TreeLogger logger;

    protected final GeneratorContext context;

    protected final RebindConfiguration configuration;

    protected final JacksonTypeOracle typeOracle;

    protected AbstractCreator( TreeLogger logger, GeneratorContext context, RebindConfiguration configuration,
                               JacksonTypeOracle typeOracle ) {
        this.logger = logger;
        this.context = context;
        this.configuration = configuration;
        this.typeOracle = typeOracle;
    }

    protected PrintWriter getPrintWriter( String packageName, String className ) {
        return context.tryCreate( logger, packageName, className );
    }

    protected SourceWriter getSourceWriter( PrintWriter printWriter, String packageName, String className, String superClass,
                                            String... interfaces ) {
        ClassSourceFileComposerFactory composer = new ClassSourceFileComposerFactory( packageName, className );
        if ( null != superClass ) {
            composer.setSuperclass( superClass );
        }
        for ( String interfaceName : interfaces ) {
            composer.addImplementedInterface( interfaceName );
        }
        return composer.createSourceWriter( context, printWriter );
    }

    /**
     * Build the string that instantiate a {@link JsonSerializer} for the given type. If the type is a bean,
     * the implementation of {@link AbstractBeanJsonSerializer} will
     * be created.
     *
     * @param type type
     *
     * @return the code instantiating the {@link JsonSerializer}. Examples:
     *         <ul>
     *         <li>ctx.getIntegerSerializer()</li>
     *         <li>new org.PersonBeanJsonSerializer()</li>
     *         </ul>
     */
    protected JSerializerType getJsonSerializerFromType( JType type ) throws UnableToCompleteException {
        JSerializerType.Builder builder = JSerializerType.builder().type( type );

        JTypeParameter typeParameter = type.isTypeParameter();
        if ( null != typeParameter ) {
            return builder.instance( String.format( TYPE_PARAMETER_SERIALIZER_FIELD_NAME, typeParameter.getOrdinal() ) ).build();
        }

        Optional<MapperInstance> configuredSerializer = configuration.getSerializer( type );
        if ( configuredSerializer.isPresent() ) {
            if ( null != type.isParameterized() ) {

                JSerializerType[] parametersSerializer = new JSerializerType[type.isParameterized().getTypeArgs().length];
                String[] params = new String[type.isParameterized().getTypeArgs().length];

                for ( int i = 0; i < params.length; i++ ) {
                    JSerializerType parameterSerializerType;
                    if ( MapperType.KEY_SERIALIZER == configuredSerializer.get().getParameters()[i] ) {
                        parameterSerializerType = getKeySerializerFromType( type.isParameterized().getTypeArgs()[i] );
                    } else {
                        parameterSerializerType = getJsonSerializerFromType( type.isParameterized().getTypeArgs()[i] );
                    }
                    parametersSerializer[i] = parameterSerializerType;
                    params[i] = parameterSerializerType.getInstance();
                }
                builder.parameters( parametersSerializer );
                builder.instance( String.format( configuredSerializer.get().getInstanceCreation(), params ) );

            } else {
                builder.instance( configuredSerializer.get().getInstanceCreation() );
            }
            return builder.build();
        }

        if ( typeOracle.isObject( type ) ) {
            logger.log( Type.ERROR, "java.lang.Object is not a supported type" );
            throw new UnableToCompleteException();
        }

        JEnumType enumType = type.isEnum();
        if ( null != enumType ) {
            return builder.instance( String.format( "ctx.<%s>getEnumJsonSerializer()", enumType.getQualifiedSourceName() ) ).build();
        }

        JArrayType arrayType = type.isArray();
        if ( null != arrayType ) {
            JSerializerType parameterSerializerType = getJsonSerializerFromType( arrayType.getComponentType() );
            builder.parameters( new JSerializerType[]{parameterSerializerType} );
            return builder.instance( String.format( "ctx.newArrayJsonSerializer(%s)", parameterSerializerType.getInstance() ) ).build();
        }

        JClassType classType = type.isClassOrInterface();
        JParameterizedType parameterizedType = type.isParameterized();
        if ( null != classType ) {
            // it's a bean
            JClassType baseClassType = classType;
            if ( null != parameterizedType ) {
                // it's a bean with generics, we create a serializer based on generic type
                baseClassType = typeOracle.findGenericType( parameterizedType );
            }
            BeanJsonSerializerCreator beanJsonSerializerCreator = new BeanJsonSerializerCreator( logger
                .branch( Type.DEBUG, "Creating serializer for " + baseClassType
                    .getQualifiedSourceName() ), context, configuration, typeOracle );
            BeanJsonMapperInfo info = beanJsonSerializerCreator.create( baseClassType );

            StringBuilder joinedTypeParameters = new StringBuilder();
            StringBuilder joinedTypeParameterSerializers = new StringBuilder();
            if ( null != parameterizedType ) {
                JSerializerType[] parametersSerializer = new JSerializerType[parameterizedType.getTypeArgs().length];
                joinedTypeParameters.append( '<' );
                for ( int i = 0; i < parameterizedType.getTypeArgs().length; i++ ) {
                    if ( i > 0 ) {
                        joinedTypeParameters.append( ", " );
                        joinedTypeParameterSerializers.append( ", " );
                    }
                    JClassType argType = parameterizedType.getTypeArgs()[i];
                    joinedTypeParameters.append( argType.getParameterizedQualifiedSourceName() );

                    JSerializerType parameterSerializerType = getJsonSerializerFromType( argType );
                    parametersSerializer[i] = parameterSerializerType;
                    joinedTypeParameterSerializers.append( parameterSerializerType.getInstance() );
                }
                joinedTypeParameters.append( '>' );
                builder.parameters( parametersSerializer );
            }

            builder.beanMapper( true );
            builder.instance( String.format( "new %s%s(%s)", info.getQualifiedSerializerClassName(), joinedTypeParameters
                .toString(), joinedTypeParameterSerializers ) );
            return builder.build();
        }

        logger.log( TreeLogger.Type.ERROR, "Type '" + type.getQualifiedSourceName() + "' is not supported" );
        throw new UnableToCompleteException();
    }

    /**
     * Build the string that instantiate a {@link KeySerializer} for the given type.
     *
     * @param type type
     *
     * @return the code instantiating the {@link KeySerializer}.
     */
    protected JSerializerType getKeySerializerFromType( JType type ) throws UnableToCompleteException {
        JSerializerType.Builder builder = JSerializerType.builder().type( type );

        Optional<MapperInstance> keySerializer = configuration.getKeySerializer( type );
        if ( keySerializer.isPresent() ) {
            builder.instance( keySerializer.get().getInstanceCreation() );
            return builder.build();
        }

        JEnumType enumType = type.isEnum();
        if ( null != enumType ) {
            builder.instance( String.format( "ctx.<%s>getEnumKeySerializer()", enumType.getQualifiedSourceName() ) );
            return builder.build();
        }

        logger.log( TreeLogger.Type.ERROR, "Type '" + type.getQualifiedSourceName() + "' is not supported as map's key" );
        throw new UnableToCompleteException();
    }

    /**
     * Build the string that instantiate a {@link JsonDeserializer} for the given type. If the type is a bean,
     * the implementation of {@link AbstractBeanJsonDeserializer} will
     * be created.
     *
     * @param type type
     *
     * @return the code instantiating the deserializer. Examples:
     *         <ul>
     *         <li>ctx.getIntegerDeserializer()</li>
     *         <li>new org .PersonBeanJsonDeserializer()</li>
     *         </ul>
     */
    protected JDeserializerType getJsonDeserializerFromType( JType type ) throws UnableToCompleteException {
        JDeserializerType.Builder builder = JDeserializerType.builder().type( type );

        JTypeParameter typeParameter = type.isTypeParameter();
        if ( null != typeParameter ) {
            return builder.instance( String.format( TYPE_PARAMETER_DESERIALIZER_FIELD_NAME, typeParameter.getOrdinal() ) ).build();
        }

        Optional<MapperInstance> configuredDeserializer = configuration.getDeserializer( type );
        if ( configuredDeserializer.isPresent() ) {
            if ( null != type.isParameterized() ) {

                JDeserializerType[] parametersDeserializer = new JDeserializerType[type.isParameterized().getTypeArgs().length];
                String[] params = new String[type.isParameterized().getTypeArgs().length];

                for ( int i = 0; i < params.length; i++ ) {
                    JDeserializerType parameterDeserializerType;
                    if ( MapperType.KEY_DESERIALIZER == configuredDeserializer.get().getParameters()[i] ) {
                        parameterDeserializerType = getKeyDeserializerFromType( type.isParameterized().getTypeArgs()[i] );
                    } else {
                        parameterDeserializerType = getJsonDeserializerFromType( type.isParameterized().getTypeArgs()[i] );
                    }

                    parametersDeserializer[i] = parameterDeserializerType;
                    params[i] = parameterDeserializerType.getInstance();
                }
                builder.parameters( parametersDeserializer );
                builder.instance( String.format( configuredDeserializer.get().getInstanceCreation(), params ) );

            } else {
                builder.instance( configuredDeserializer.get().getInstanceCreation() );
            }
            return builder.build();
        }

        if ( typeOracle.isObject( type ) ) {
            logger.log( Type.ERROR, "java.lang.Object is not a supported type" );
            throw new UnableToCompleteException();
        }

        JEnumType enumType = type.isEnum();
        if ( null != enumType ) {
            return builder.instance( "ctx.newEnumJsonDeserializer(" + enumType.getQualifiedSourceName() + ".class)" ).build();
        }

        JArrayType arrayType = type.isArray();
        if ( null != arrayType ) {
            String method = "ctx.newArrayJsonDeserializer(%s, %s)";
            String arrayCreator = "new " + ArrayCreator.class.getCanonicalName() + "<" + arrayType.getComponentType()
                .getParameterizedQualifiedSourceName() + ">(){\n" +
                "  @Override\n" +
                "  public " + arrayType.getParameterizedQualifiedSourceName() + " create( int length ) {\n" +
                "    return new " + arrayType.getComponentType().getParameterizedQualifiedSourceName() + "[length];\n" +
                "  }\n" +
                "}";

            JDeserializerType parameterDeserializerType = getJsonDeserializerFromType( arrayType.getComponentType() );
            builder.parameters( new JDeserializerType[]{parameterDeserializerType} );
            return builder.instance( String.format( method, parameterDeserializerType.getInstance(), arrayCreator ) ).build();
        }

        JParameterizedType parameterizedType = type.isParameterized();
        if ( null != parameterizedType ) {
            if ( typeOracle.isEnumSet( parameterizedType ) ) {

                JDeserializerType parameterDeserializerType = getJsonDeserializerFromType( parameterizedType.getTypeArgs()[0] );

                builder.parameters( new JDeserializerType[]{parameterDeserializerType} );
                // EnumSet needs the enum class as parameter
                builder.instance( String.format( "ctx.newEnumSetJsonDeserializer(%s.class, %s)", parameterizedType.getTypeArgs()[0]
                    .getQualifiedSourceName(), parameterDeserializerType.getInstance() ) );

                return builder.build();

            }

            if ( typeOracle.isEnumMap( parameterizedType ) ) {

                JDeserializerType keyDeserializerType = getKeyDeserializerFromType( parameterizedType.getTypeArgs()[0] );
                JDeserializerType valueDeserializerType = getJsonDeserializerFromType( parameterizedType.getTypeArgs()[1] );

                builder.parameters( new JDeserializerType[]{keyDeserializerType, valueDeserializerType} );
                // EnumMap needs the enum class as parameter
                builder.instance( String.format( "ctx.newEnumMapJsonDeserializer(%s.class, %s, %s)", parameterizedType.getTypeArgs()[0]
                    .getQualifiedSourceName(), keyDeserializerType.getInstance(), valueDeserializerType.getInstance() ) );

                return builder.build();
            }

            // other parameterized types are considered to be beans
        }

        JClassType classType = type.isClassOrInterface();
        if ( null != classType ) {
            // it's a bean
            JClassType baseClassType = classType;
            if ( null != parameterizedType ) {
                // it's a bean with generics, we create a deserializer based on generic type
                baseClassType = typeOracle.findGenericType( parameterizedType );
            }
            BeanJsonDeserializerCreator beanJsonDeserializerCreator = new BeanJsonDeserializerCreator( logger
                .branch( Type.DEBUG, "Creating deserializer for " + baseClassType
                    .getQualifiedSourceName() ), context, configuration, typeOracle );
            BeanJsonMapperInfo info = beanJsonDeserializerCreator.create( baseClassType );

            StringBuilder joinedTypeParameters = new StringBuilder();
            StringBuilder joinedTypeParameterDeserializers = new StringBuilder();
            if ( null != parameterizedType ) {
                JDeserializerType[] parametersDeserializer = new JDeserializerType[parameterizedType.getTypeArgs().length];
                joinedTypeParameters.append( '<' );
                for ( int i = 0; i < parameterizedType.getTypeArgs().length; i++ ) {
                    if ( i > 0 ) {
                        joinedTypeParameters.append( ", " );
                        joinedTypeParameterDeserializers.append( ", " );
                    }
                    JClassType argType = parameterizedType.getTypeArgs()[i];
                    joinedTypeParameters.append( argType.getParameterizedQualifiedSourceName() );

                    JDeserializerType parameterDeserializerType = getJsonDeserializerFromType( argType );
                    parametersDeserializer[i] = parameterDeserializerType;
                    joinedTypeParameterDeserializers.append( parameterDeserializerType.getInstance() );
                }
                joinedTypeParameters.append( '>' );
                builder.parameters( parametersDeserializer );
            }

            builder.beanMapper( true );
            builder.instance( String.format( "new %s%s(%s)", info
                .getQualifiedDeserializerClassName(), joinedTypeParameters, joinedTypeParameterDeserializers ) );
            return builder.build();
        }

        logger.log( TreeLogger.Type.ERROR, "Type '" + type.getQualifiedSourceName() + "' is not supported" );
        throw new UnableToCompleteException();
    }

    /**
     * Build the string that instantiate a {@link KeyDeserializer} for the given type.
     *
     * @param type type
     *
     * @return the code instantiating the {@link KeyDeserializer}.
     */
    protected JDeserializerType getKeyDeserializerFromType( JType type ) throws UnableToCompleteException {
        JDeserializerType.Builder builder = JDeserializerType.builder();
        builder.type( type );

        Optional<MapperInstance> keyDeserializer = configuration.getKeyDeserializer( type );
        if ( keyDeserializer.isPresent() ) {
            builder.instance( keyDeserializer.get().getInstanceCreation() );
            return builder.build();
        }

        JEnumType enumType = type.isEnum();
        if ( null != enumType ) {
            builder.instance( String.format( "ctx.newEnumKeyDeserializer(%s.class)", enumType.getQualifiedSourceName() ) );
            return builder.build();
        }

        logger.log( TreeLogger.Type.ERROR, "Type '" + type.getQualifiedSourceName() + "' is not supported as map's key" );
        throw new UnableToCompleteException();
    }

}
