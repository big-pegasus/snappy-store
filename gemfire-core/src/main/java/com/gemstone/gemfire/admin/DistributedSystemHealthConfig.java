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
package com.gemstone.gemfire.admin;

/**
 * Provides configuration information relating to the health of an
 * entire GemFire distributed system.
 *
 * <P>
 *
 * If any of the following criteria is
 * true, then the distributed system is considered to be in
 * {@link GemFireHealth#OKAY_HEALTH OKAY_HEALTH}.
 *
 * <UL>
 *
 * </UL>
 *
 * If any of the following criteria is true, then the distributed
 * system is considered to be in {@link GemFireHealth#POOR_HEALTH
 * POOR_HEALTH}.
 *
 * <UL>
 *
 * <LI>Too many application members {@linkplain
 * #getMaxDepartedApplications unexpectedly leave} the distributed
 * system.</LI>
 *
 * <LI>Too many application members {@linkplain
 * #getMaxDepartedApplications unexpectedly leave} the distributed
 * system.</LI>
 *
 * </UL>
 *
 * @author David Whitlock
 *
 * @since 3.5
 * @deprecated as of 7.0 use the {@link com.gemstone.gemfire.management} package instead
 * */
public interface DistributedSystemHealthConfig {

  /** The default maximum number of application members that can
   * unexceptedly leave a healthy the distributed system. */
  public static final long DEFAULT_MAX_DEPARTED_APPLICATIONS = 10;

  ///////////////////////  Instance Methods  ///////////////////////

  /**
   * Returns the maximum number of application members that can
   * unexceptedly leave a healthy the distributed system.
   *
   * @see #DEFAULT_MAX_DEPARTED_APPLICATIONS
   */
  public long getMaxDepartedApplications();

  /**
   * Sets the maximum number of application members that can
   * unexceptedly leave a healthy the distributed system.
   *
   * @see #getMaxDepartedApplications
   */
  public void setMaxDepartedApplications(long maxDepartedApplications);
}
