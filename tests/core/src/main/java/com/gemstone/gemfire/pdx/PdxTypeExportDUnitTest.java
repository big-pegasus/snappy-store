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
package com.gemstone.gemfire.pdx;

import java.util.Collection;

import com.examples.snapshot.MyObjectPdx;
import com.examples.snapshot.MyObjectPdx.MyEnumPdx;
import com.examples.snapshot.MyPdxSerializer;
import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.RegionShortcut;
import com.gemstone.gemfire.cache.client.ClientCache;
import com.gemstone.gemfire.cache.client.ClientCacheFactory;
import com.gemstone.gemfire.cache.client.ClientRegionShortcut;
import com.gemstone.gemfire.cache.server.CacheServer;
import com.gemstone.gemfire.cache30.CacheTestCase;
import com.gemstone.gemfire.internal.AvailablePortHelper;
import com.gemstone.gemfire.internal.cache.GemFireCacheImpl;
import com.gemstone.gemfire.pdx.internal.EnumInfo;
import com.gemstone.gemfire.pdx.internal.PdxType;
import com.gemstone.gemfire.pdx.internal.TypeRegistry;

import dunit.Host;
import dunit.SerializableCallable;

public class PdxTypeExportDUnitTest extends CacheTestCase {
  public PdxTypeExportDUnitTest(String name) {
    super(name);
  }

  public void testNothing() {
  }
  
  public void xtestPeer() throws Exception {
    Region r = getCache().getRegion("pdxtest");
    r.get(1);

    TypeRegistry tr = ((GemFireCacheImpl) getCache()).getPdxRegistry();
    Collection<PdxType> types = tr.typeMap().values();
    assertEquals(MyObjectPdx.class.getName(), types.iterator().next().getClassName());
    
    Collection<EnumInfo> enums = tr.enumMap().values();
    assertEquals(MyEnumPdx.const1.name(), enums.iterator().next().getEnum().name());
  }
  
  public void xtestClient() throws Exception {
    SerializableCallable test = new SerializableCallable() {
      @Override
      public Object call() throws Exception {
        xtestPeer();
        return null;
      }
    };
    
    Host.getHost(0).getVM(3).invoke(test);
  }
  
  public void setUp() throws Exception {
    super.setUp();
    loadCache();
  }
  
  @SuppressWarnings("serial")
  public void loadCache() throws Exception {
    SerializableCallable peer = new SerializableCallable() {
      @Override
      public Object call() throws Exception {
        CacheFactory cf = new CacheFactory()
          .setPdxSerializer(new MyPdxSerializer());
    
        Cache cache = getCache(cf);
        Region r = cache.createRegionFactory(RegionShortcut.REPLICATE).create("pdxtest");
        r.put(1, new MyObjectPdx(1, "test", MyEnumPdx.const1));

        return null;
      }
    };

    final Host host = Host.getHost(0);
    host.getVM(1).invoke(peer);

    SerializableCallable server = new SerializableCallable() {
      @Override
      public Object call() throws Exception {
        CacheFactory cf = new CacheFactory()
          .setPdxSerializer(new MyPdxSerializer());
        
        CacheServer server = getCache().addCacheServer();
        int port = AvailablePortHelper.getRandomAvailableTCPPort();
        server.setPort(port);
        server.start();

        Region r = getCache().createRegionFactory(RegionShortcut.REPLICATE).create("pdxtest");
        return port;
      }
    };

    final int port = (Integer) host.getVM(2).invoke(server);

    SerializableCallable client = new SerializableCallable() {
      @Override
      public Object call() throws Exception {
        ClientCacheFactory cf = new ClientCacheFactory()
          .setPdxSerializer(new MyPdxSerializer())
          .addPoolServer(getServerHostName(host), port);
    
        ClientCache cache = getClientCache(cf);
        Region r = cache.createClientRegionFactory(ClientRegionShortcut.PROXY).create("pdxtest");
        return null;
      }
    };

    host.getVM(3).invoke(client);
    peer.call();
  }
}
