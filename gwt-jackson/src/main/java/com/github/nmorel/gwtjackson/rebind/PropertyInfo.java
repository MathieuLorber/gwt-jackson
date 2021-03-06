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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.github.nmorel.gwtjackson.rebind.property.PropertyAccessors;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.TreeLogger.Type;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JField;
import com.google.gwt.core.ext.typeinfo.JMethod;
import com.google.gwt.core.ext.typeinfo.JType;
import com.google.gwt.thirdparty.guava.common.base.Optional;
import com.google.gwt.user.rebind.SourceWriter;

import static com.github.nmorel.gwtjackson.rebind.CreatorUtils.findAnnotationOnAnyAccessor;
import static com.github.nmorel.gwtjackson.rebind.CreatorUtils.findFirstEncounteredAnnotationsOnAllHierarchy;

/**
 * @author Nicolas Morel
 */
public final class PropertyInfo {

    public static interface AdditionalMethod {

        void write( SourceWriter source );
    }

    public static PropertyInfo process( TreeLogger logger, JacksonTypeOracle typeOracle, PropertyAccessors propertyAccessors,
                                        BeanJsonMapperInfo mapperInfo ) throws UnableToCompleteException {
        PropertyInfo result = new PropertyInfo();

        // find the type of the property
        result.type = findType( logger, propertyAccessors );
        result.propertyName = propertyAccessors.getPropertyName();

        // determine the property name
        JsonProperty jsonProperty = findAnnotationOnAnyAccessor( propertyAccessors, JsonProperty.class );
        result.required = null != jsonProperty && jsonProperty.required();

        result.ignored = isPropertyIgnored( propertyAccessors, mapperInfo, result.type, result.propertyName );
        if ( result.ignored ) {
            return result;
        }

        JsonManagedReference jsonManagedReference = findAnnotationOnAnyAccessor( propertyAccessors, JsonManagedReference.class, true );
        result.managedReference = Optional.fromNullable( null == jsonManagedReference ? null : jsonManagedReference.value() );

        JsonBackReference jsonBackReference = findAnnotationOnAnyAccessor( propertyAccessors, JsonBackReference.class, true );
        result.backReference = Optional.fromNullable( null == jsonBackReference ? null : jsonBackReference.value() );

        // if an accessor has jackson annotation, the property is considered auto detected.
        // TODO can we do a search on @JacksonAnnotation instead of enumerating all of them ?
        boolean hasAnyAnnotation = null != findAnnotationOnAnyAccessor( propertyAccessors, JsonProperty.class,
                true ) || null != jsonManagedReference || null != jsonBackReference;

        boolean getterAutoDetected = propertyAccessors.getGetter()
                .isPresent() && (hasAnyAnnotation || isGetterAutoDetected( propertyAccessors.getGetter().get(), mapperInfo.getBeanInfo() ));
        boolean setterAutoDetected = propertyAccessors.getSetter()
                .isPresent() && (hasAnyAnnotation || isSetterAutoDetected( propertyAccessors.getSetter().get(), mapperInfo.getBeanInfo() ));
        boolean fieldAutoDetected = propertyAccessors.getField().isPresent() && (hasAnyAnnotation || isFieldAutoDetected( propertyAccessors
                .getField().get(), mapperInfo.getBeanInfo() ));

        if ( !getterAutoDetected && !setterAutoDetected && !fieldAutoDetected && !propertyAccessors.getParameter().isPresent() ) {
            // none of the field have been auto-detected, we ignore the field
            result.visible = false;
            return result;
        }

        if ( !result.backReference.isPresent() ) {
            determineGetter( propertyAccessors, getterAutoDetected, fieldAutoDetected, result );

            JsonRawValue jsonRawValue = findAnnotationOnAnyAccessor( propertyAccessors, JsonRawValue.class );
            result.rawValue = null != jsonRawValue && jsonRawValue.value();
        }
        determineSetter( propertyAccessors, setterAutoDetected, fieldAutoDetected, result );

        result.identityInfo = Optional.fromNullable( BeanIdentityInfo.process( logger, typeOracle, result.type, propertyAccessors ) );
        result.typeInfo = Optional.fromNullable( BeanTypeInfo.process( logger, typeOracle, result.type, propertyAccessors ) );

        result.format = Optional.fromNullable( findAnnotationOnAnyAccessor( propertyAccessors, JsonFormat.class ) );

        JsonInclude jsonInclude = findAnnotationOnAnyAccessor( propertyAccessors, JsonInclude.class );
        if ( null != jsonInclude ) {
            result.include = Optional.of( jsonInclude.value() );
        }

        JsonIgnoreProperties jsonIgnoreProperties = findAnnotationOnAnyAccessor( propertyAccessors, JsonIgnoreProperties.class );
        if ( null != jsonIgnoreProperties ) {
            result.ignoreUnknown = Optional.of( jsonIgnoreProperties.ignoreUnknown() );
            if ( null != jsonIgnoreProperties.value() && jsonIgnoreProperties.value().length > 0 ) {
                result.ignoredProperties = Optional.of( jsonIgnoreProperties.value() );
            }
        }

        return result;
    }

    private static JType findType( TreeLogger logger, PropertyAccessors fieldAccessors ) throws UnableToCompleteException {
        if ( fieldAccessors.getGetter().isPresent() ) {
            return fieldAccessors.getGetter().get().getReturnType();
        } else if ( fieldAccessors.getSetter().isPresent() ) {
            return fieldAccessors.getSetter().get().getParameters()[0].getType();
        } else if ( fieldAccessors.getField().isPresent() ) {
            return fieldAccessors.getField().get().getType();
        } else if ( fieldAccessors.getParameter().isPresent() ) {
            return fieldAccessors.getParameter().get().getType();
        } else {
            logger.log( Type.ERROR, "Cannot find the type of the property " + fieldAccessors.getPropertyName() );
            throw new UnableToCompleteException();
        }
    }

    private static boolean isPropertyIgnored( PropertyAccessors fieldAccessors, BeanJsonMapperInfo mapperInfo, JType type,
                                              String propertyName ) {
        // we first check if the property is ignored
        JsonIgnore jsonIgnore = findAnnotationOnAnyAccessor( fieldAccessors, JsonIgnore.class );
        if ( null != jsonIgnore && jsonIgnore.value() ) {
            return true;
        }

        // if type is ignored, we ignore the property
        if ( null != type.isClassOrInterface() ) {
            JsonIgnoreType jsonIgnoreType = findFirstEncounteredAnnotationsOnAllHierarchy( type
                    .isClassOrInterface(), JsonIgnoreType.class );
            if ( null != jsonIgnoreType && jsonIgnoreType.value() ) {
                return true;
            }
        }

        // we check if it's not in the ignored properties
        return mapperInfo.getBeanInfo().getIgnoredFields().contains( propertyName );

    }

    private static boolean isGetterAutoDetected( JMethod getter, BeanInfo info ) {
        JsonAutoDetect.Visibility visibility;
        if ( getter.getName().startsWith( "is" ) ) {
            visibility = info.getIsGetterVisibility();
        } else {
            visibility = info.getGetterVisibility();
        }
        return isAutoDetected( visibility, getter.isPrivate(), getter.isProtected(), getter.isPublic(), getter.isDefaultAccess() );
    }

    private static boolean isSetterAutoDetected( JMethod setter, BeanInfo info ) {
        return isAutoDetected( info.getSetterVisibility(), setter.isPrivate(), setter.isProtected(), setter.isPublic(), setter
                .isDefaultAccess() );
    }

    private static boolean isFieldAutoDetected( JField field, BeanInfo info ) {
        return isAutoDetected( info.getFieldVisibility(), field.isPrivate(), field.isProtected(), field.isPublic(), field
                .isDefaultAccess() );
    }

    private static boolean isAutoDetected( JsonAutoDetect.Visibility visibility, boolean isPrivate, boolean isProtected,
                                           boolean isPublic, boolean isDefaultAccess ) {
        switch ( visibility ) {
            case ANY:
                return true;
            case NONE:
                return false;
            case NON_PRIVATE:
                return !isPrivate;
            case PROTECTED_AND_PUBLIC:
                return isProtected || isPublic;
            case PUBLIC_ONLY:
            case DEFAULT:
                return isPublic;
            default:
                return false;
        }
    }

    private static void determineGetter( final PropertyAccessors fieldAccessors, final boolean getterAutoDetect, boolean fieldAutoDetect,
                                         final PropertyInfo result ) {
        if ( getterAutoDetect || fieldAutoDetect ) {
            result.getterAccessor = Optional.of( new FieldReadAccessor( result.propertyName, fieldAutoDetect ? fieldAccessors.getField()
                    .get() : null, getterAutoDetect ? fieldAccessors.getGetter().get() : null ) );
        }
    }

    private static void determineSetter( final PropertyAccessors fieldAccessors, final boolean setterAutoDetect,
                                         final boolean fieldAutoDetect, final PropertyInfo result ) {
        if ( setterAutoDetect || fieldAutoDetect ) {
            result.setterAccessor = Optional.of( new FieldWriteAccessor( result.propertyName, fieldAutoDetect ? fieldAccessors.getField()
                    .get() : null, setterAutoDetect ? fieldAccessors.getSetter().get() : null ) );
        }
    }

    private boolean ignored;

    private boolean visible = true;

    private JType type;

    private boolean required;

    private String propertyName;

    private boolean rawValue = false;

    private Optional<String> managedReference = Optional.absent();

    private Optional<String> backReference = Optional.absent();

    private Optional<FieldReadAccessor> getterAccessor = Optional.absent();

    private Optional<FieldWriteAccessor> setterAccessor = Optional.absent();

    private Optional<BeanIdentityInfo> identityInfo = Optional.absent();

    private Optional<BeanTypeInfo> typeInfo = Optional.absent();

    private Optional<JsonFormat> format = Optional.absent();

    private Optional<Include> include = Optional.absent();

    private Optional<Boolean> ignoreUnknown = Optional.absent();

    private Optional<String[]> ignoredProperties = Optional.absent();

    private PropertyInfo() {
    }

    public boolean isIgnored() {
        return ignored;
    }

    public boolean isVisible() {
        return visible;
    }

    public JType getType() {
        return type;
    }

    public boolean isRequired() {
        return required;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public boolean isRawValue() {
        return rawValue;
    }

    public Optional<String> getManagedReference() {
        return managedReference;
    }

    public Optional<String> getBackReference() {
        return backReference;
    }

    public Optional<FieldReadAccessor> getGetterAccessor() {
        return getterAccessor;
    }

    public Optional<FieldWriteAccessor> getSetterAccessor() {
        return setterAccessor;
    }

    public Optional<BeanIdentityInfo> getIdentityInfo() {
        return identityInfo;
    }

    public Optional<BeanTypeInfo> getTypeInfo() {
        return typeInfo;
    }

    public Optional<JsonFormat> getFormat() {
        return format;
    }

    public Optional<Include> getInclude() {
        return include;
    }

    public Optional<Boolean> getIgnoreUnknown() {
        return ignoreUnknown;
    }

    public Optional<String[]> getIgnoredProperties() {
        return ignoredProperties;
    }
}
