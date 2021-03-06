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
/**
 * 
 */
package com.gemstone.gemfire.pdx;

/**
 * Marker interface for an object that GemFire creates and returns
 * from {@link PdxReader#readUnreadFields() readUnreadFields}.
 * If you call readUnreadFields then you must also call
 * {@link PdxWriter#writeUnreadFields(PdxUnreadFields) writeUnreadFields} when
 * that object is reserialized. If you do not call {@link PdxWriter#writeUnreadFields(PdxUnreadFields) writeUnreadFields}
 * but you did call {@link PdxReader#readUnreadFields() readUnreadFields} the unread fields will not be written.
 * <p>Unread fields are those that are not explicitly read with a {@link PdxReader} readXXX method.
 * This should only happen when a domain class has changed by adding or removing one or more fields.
 * Unread fields will be preserved automatically (unless you turn this feature off using
 * {@link com.gemstone.gemfire.cache.CacheFactory#setPdxIgnoreUnreadFields(boolean) setPdxIgnoreUnreadFields}
 * or {@link com.gemstone.gemfire.cache.client.ClientCacheFactory#setPdxIgnoreUnreadFields(boolean) client setPdxIgnoreUnreadFields})
 * but to reduce the performance and memory overhead of automatic preservation it is recommended
 * that use {@link PdxReader#readUnreadFields() readUnreadFields} if possible.
 * 
 * @author darrel
 * @since 6.6
 *
 */
public interface PdxUnreadFields {
}
