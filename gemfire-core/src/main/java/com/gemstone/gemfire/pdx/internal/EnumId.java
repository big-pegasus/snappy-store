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
package com.gemstone.gemfire.pdx.internal;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import com.gemstone.gemfire.internal.DataSerializableFixedID;
import com.gemstone.gemfire.internal.shared.Version;

public class EnumId implements DataSerializableFixedID {

  private int id;
  
  public EnumId(int id) {
    this.id = id;
  }

  public EnumId() {
  }

  public int getDSFID() {
    return ENUM_ID;
  }

  public void toData(DataOutput out) throws IOException {
    out.writeInt(this.id);
  }

  public void fromData(DataInput in) throws IOException, ClassNotFoundException {
    this.id = in.readInt();
  }

  public int intValue() {
    return this.id;
  }
  
  public int getDSId() {
    return this.id >> 24;
  }
  
  public int getEnumNum() {
    return this.id & 0x00FFFFFF;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + id;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    EnumId other = (EnumId) obj;
    if (id != other.id)
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "EnumId[dsid=" + getDSId() + ", enumnum=" + getEnumNum() + "]";
  }

  @Override
  public Version[] getSerializationVersions() {
    return null;
  }
  
  

}
