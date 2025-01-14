/*
 * Copyright 2012-2016 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at:
 *
 *     http://aws.amazon.com/apache2.0/
 *
 * or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */

package com.fasterxml.jackson.dataformat.ion.ionvalue;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ValueDeserializer;
import com.fasterxml.jackson.dataformat.ion.IonParser;

import com.amazon.ion.IonSystem;
import com.amazon.ion.IonValue;
import com.amazon.ion.Timestamp;

/**
 * Deserializer that knows how to deserialize an IonValue.
 */
class IonValueDeserializer extends ValueDeserializer<IonValue>
{
    @Override
    public IonValue deserialize(JsonParser jp, DeserializationContext ctxt)
        throws JacksonException
    {
        Object embeddedObject = jp.getEmbeddedObject();
        if (embeddedObject instanceof IonValue) {
            return (IonValue) embeddedObject;
        }
        // We rely on the IonParser's IonSystem to wrap supported types into an IonValue
        if (!(jp instanceof IonParser)) {
            throw DatabindException.from(jp, "Unsupported parser for deserializing "
                    + embeddedObject.getClass().getCanonicalName() + " into IonValue");
        }

        IonSystem ionSystem = ((IonParser) jp).getIonSystem();
        if (embeddedObject instanceof Timestamp) {
            return ionSystem.newTimestamp((Timestamp) embeddedObject);
        }
        if (embeddedObject instanceof byte[]) {
            // The parser provides no distinction between BLOB and CLOB, deserializing to a BLOB is the safest choice.
            return ionSystem.newBlob((byte[]) embeddedObject);
        }
        throw DatabindException.from(jp, "Cannot deserialize embedded object type "
                + embeddedObject.getClass().getCanonicalName() + " into IonValue");
    }
}
