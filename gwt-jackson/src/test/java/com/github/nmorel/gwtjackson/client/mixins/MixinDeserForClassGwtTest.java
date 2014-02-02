/*
 * Copyright 2014 Nicolas Morel
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

package com.github.nmorel.gwtjackson.client.mixins;

import com.github.nmorel.gwtjackson.client.GwtJacksonTestCase;
import com.github.nmorel.gwtjackson.client.ObjectReader;
import com.github.nmorel.gwtjackson.shared.ObjectReaderTester;
import com.github.nmorel.gwtjackson.shared.mixins.MixinDeserForClassTester;
import com.github.nmorel.gwtjackson.shared.mixins.MixinDeserForClassTester.BaseClass;
import com.github.nmorel.gwtjackson.shared.mixins.MixinDeserForClassTester.BaseClassToMixIn;
import com.github.nmorel.gwtjackson.shared.mixins.MixinDeserForClassTester.LeafClass;
import com.github.nmorel.gwtjackson.shared.mixins.MixinDeserForClassTester.LeafClassToMixIn;
import com.google.gwt.core.client.GWT;

/**
 * @author Nicolas Morel
 */
public class MixinDeserForClassGwtTest extends GwtJacksonTestCase {

    public interface LeafClassToMixInReader extends ObjectReader<LeafClassToMixIn>, ObjectReaderTester<LeafClassToMixIn> {

        static LeafClassToMixInReader INSTANCE = GWT.create( LeafClassToMixInReader.class );
    }

    public interface LeafClassReader extends ObjectReader<LeafClass>, ObjectReaderTester<LeafClass> {

        static LeafClassReader INSTANCE = GWT.create( LeafClassReader.class );
    }

    public interface BaseClassToMixInReader extends ObjectReader<BaseClassToMixIn>, ObjectReaderTester<BaseClassToMixIn> {

        static BaseClassToMixInReader INSTANCE = GWT.create( BaseClassToMixInReader.class );
    }

    public interface BaseClassReader extends ObjectReader<BaseClass>, ObjectReaderTester<BaseClass> {

        static BaseClassReader INSTANCE = GWT.create( BaseClassReader.class );
    }

    private MixinDeserForClassTester tester = MixinDeserForClassTester.INSTANCE;

    public void testClassMixInsTopLevel() {
        tester.testClassMixInsTopLevel( LeafClassToMixInReader.INSTANCE );
    }

    public void testClassMixInsMidLevel() {
        tester.testClassMixInsMidLevel( BaseClassToMixInReader.INSTANCE, LeafClassReader.INSTANCE );
    }

    public void testClassMixInsForObjectClass() {
        tester.testClassMixInsForObjectClass( BaseClassReader.INSTANCE, LeafClassReader.INSTANCE );
    }
}
