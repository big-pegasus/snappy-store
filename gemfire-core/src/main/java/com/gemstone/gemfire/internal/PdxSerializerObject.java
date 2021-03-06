/*
 * Copyright (c) 2010-2015 Pivotal Software, Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You
 * may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License. See accompanying
 * LICENSE file.
 */
package com.gemstone.gemfire.internal;

/**
 * Marker interface for object used in PdxSerializer Tests that are in the
 * com.gemstone package. If an object implements this interface, it will be
 * passed to a PdxSerializer even if it is in the com.gemstone package.
 * 
 * This is necessary because we exclude all other objects from the com.gemstone
 * package.
 * See {@link InternalDataSerializer#writePdx(java.io.DataOutput, com.gemstone.gemfire.internal.cache.GemFireCacheImpl, Object, com.gemstone.gemfire.pdx.PdxSerializer)} 
 * 
 * @author dsmith
 * 
 */
public interface PdxSerializerObject {

}
