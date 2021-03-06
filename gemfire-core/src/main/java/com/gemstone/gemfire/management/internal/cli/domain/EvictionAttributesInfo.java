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
package com.gemstone.gemfire.management.internal.cli.domain;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.gemstone.gemfire.cache.EvictionAction;
import com.gemstone.gemfire.cache.EvictionAlgorithm;
import com.gemstone.gemfire.cache.EvictionAttributes;
import com.gemstone.gemfire.management.internal.cli.util.RegionAttributesDefault;
import com.gemstone.gemfire.management.internal.cli.util.RegionAttributesNames;

public class EvictionAttributesInfo implements Serializable{
	
	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;
	private String evictionAction = "";
	private String evictionAlgorithm = "";
	private int  evictionMaxValue = 0;
	private Map<String, String> nonDefaultAttributes;
	
	public EvictionAttributesInfo(EvictionAttributes ea) {
		EvictionAction evictAction = ea.getAction();
		
		if (evictAction != null) {
				evictionAction = evictAction.toString();
		}
		EvictionAlgorithm evictionAlgo = ea.getAlgorithm();
		if (evictionAlgo != null){
			evictionAlgorithm = evictionAlgo.toString();
		}
		if (!EvictionAlgorithm.LRU_HEAP.equals(evictionAlgo)) {
	    evictionMaxValue = ea.getMaximum();
    }
	}

	public String getEvictionAction() {
		return evictionAction;
	}

	public String getEvictionAlgorithm() {
		return evictionAlgorithm;
	}

	public int getEvictionMaxValue() {
		return evictionMaxValue;
	}
	
	public boolean equals(Object obj) {
	  if (obj instanceof EvictionAttributesInfo) {
	    EvictionAttributesInfo their = (EvictionAttributesInfo) obj;
	    return this.evictionAction.equals(their.getEvictionAction()) 
	          && this.evictionAlgorithm.equals(their.getEvictionAlgorithm())
	          && this.evictionMaxValue == their.getEvictionMaxValue();
	  } else {
	    return false;
	  }
	}
	
	public Map<String, String> getNonDefaultAttributes() {
	  if (nonDefaultAttributes == null) {
	    nonDefaultAttributes = new HashMap<String, String>();
	  }
	  
	  if (this.evictionMaxValue != RegionAttributesDefault.EVICTION_MAX_VALUE) {
	    nonDefaultAttributes.put(RegionAttributesNames.EVICTION_MAX_VALUE, Long.toString(evictionMaxValue));
	  }
	  if (this.evictionAction != null && !this.evictionAction.equals(RegionAttributesDefault.EVICTION_ACTION)) {
	    nonDefaultAttributes.put(RegionAttributesNames.EVICTION_ACTION, this.evictionAction);
	  }
	  if (this.evictionAlgorithm != null && !this.evictionAlgorithm.equals(RegionAttributesDefault.EVICTION_ALGORITHM)) {
	    nonDefaultAttributes.put(RegionAttributesNames.EVICTION_ALGORITHM, this.evictionAlgorithm);
	  }
	  return nonDefaultAttributes;
	}
}
