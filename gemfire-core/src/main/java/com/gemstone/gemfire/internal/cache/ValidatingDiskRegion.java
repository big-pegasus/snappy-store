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
package com.gemstone.gemfire.internal.cache;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.gemstone.gemfire.CancelCriterion;
import com.gemstone.gemfire.InternalGemFireError;
import com.gemstone.gemfire.cache.CacheWriterException;
import com.gemstone.gemfire.cache.DiskAccessException;
import com.gemstone.gemfire.cache.EntryEvent;
import com.gemstone.gemfire.cache.EntryNotFoundException;
import com.gemstone.gemfire.cache.TimeoutException;
import com.gemstone.gemfire.distributed.internal.DM;
import com.gemstone.gemfire.internal.InternalStatisticsDisabledException;
import com.gemstone.gemfire.internal.cache.DistributedRegion.DiskPosition;
import com.gemstone.gemfire.internal.cache.InitialImageOperation.Entry;
import com.gemstone.gemfire.internal.cache.locks.LockMode;
import com.gemstone.gemfire.internal.cache.locks.LockingPolicy;
import com.gemstone.gemfire.internal.cache.lru.EnableLRU;
import com.gemstone.gemfire.internal.cache.persistence.DiskExceptionHandler;
import com.gemstone.gemfire.internal.cache.persistence.DiskRecoveryStore;
import com.gemstone.gemfire.internal.cache.persistence.DiskRegionView;
import com.gemstone.gemfire.internal.cache.versions.VersionSource;
import com.gemstone.gemfire.internal.cache.versions.VersionStamp;
import com.gemstone.gemfire.internal.cache.versions.VersionTag;
import com.gemstone.gemfire.internal.i18n.LocalizedStrings;
import com.gemstone.gemfire.internal.shared.Version;

/**
 * A disk region that is created when doing offline validation.
 * @since prPersistSprint3
 */
public class ValidatingDiskRegion extends DiskRegion implements DiskRecoveryStore {
  protected ValidatingDiskRegion(DiskStoreImpl ds,
                               DiskRegionView drv) {
    super(ds, drv.getName(), drv.isBucket(), true, false, true,
          new DiskRegionStats(ds.getCache().getDistributedSystem(), drv.getName()),
          new DummyCancelCriterion(),
          new DummyDiskExceptionHandler(),
          null, drv.getFlags(), drv.getUUID(),
          drv.getPartitionName(), drv.getStartingBucketId(),
          drv.getCompressorClassName(), drv.getEnableOffHeapMemory());
    setConfig(drv.getLruAlgorithm(), drv.getLruAction(), drv.getLruLimit(),
              drv.getConcurrencyLevel(), drv.getInitialCapacity(),
              drv.getLoadFactor(), drv.getStatisticsEnabled(),
              drv.isBucket(), drv.getFlags(), drv.getUUID(),
              drv.getPartitionName(), drv.getStartingBucketId(),
              drv.getCompressorClassName(), drv.getEnableOffHeapMemory());
  }
  
  static ValidatingDiskRegion create(DiskStoreImpl dsi, DiskRegionView drv) {
    assert dsi != null;
    ValidatingDiskRegion result = new ValidatingDiskRegion(dsi, drv);
    result.register();
    GemFireCacheImpl.StaticSystemCallbacks sysCb =
        GemFireCacheImpl.FactoryStatics.systemCallbacks;
    if (sysCb != null) {
      sysCb.initializeForOffline();
    }
    return result;
  }

  private final ConcurrentMap<Object, DiskEntry> map = new ConcurrentHashMap<Object, DiskEntry>();
  
  ///////////// DiskRecoveryStore methods ////////////////
  public DiskRegionView getDiskRegionView() {
    return this;
  }

  public DiskEntry getDiskEntry(Object key) {
    return this.map.get(key);
  }

  public DiskEntry initializeRecoveredEntry(Object key, DiskEntry.RecoveredEntry re) {
    ValidatingDiskEntry de = new ValidatingDiskEntry(key, re);
    if (this.map.putIfAbsent(key, de) != null) {
      throw new InternalGemFireError(LocalizedStrings.LocalRegion_ENTRY_ALREADY_EXISTED_0.toLocalizedString(key));
    }
    return de;
  }

  public DiskEntry updateRecoveredEntry(Object key, RegionEntry entry, DiskEntry.RecoveredEntry re) {
    ValidatingDiskEntry de = new ValidatingDiskEntry(key, re);
    this.map.put(key, de);
    return de;
  }
  public void destroyRecoveredEntry(Object key) {
    this.map.remove(key);
  }
  public void foreachRegionEntry(LocalRegion.RegionEntryCallback callback) {
    throw new IllegalStateException("foreachRegionEntry should not be called when validating disk store");
  }
  public boolean lruLimitExceeded() {
    return false;
  }
  public void copyRecoveredEntries(RegionMap rm, boolean entriesIncompatible) {
    throw new IllegalStateException("copyRecoveredEntries should not be called on ValidatingDiskRegion");
  }
  public void updateSizeOnFaultIn(Object key, int newSize, int bytesOnDisk) {
    throw new IllegalStateException("updateSizeOnFaultIn should not be called on ValidatingDiskRegion");
  }
  @Override
  public int calculateValueSize(Object val) {
    return 0;
  }
  @Override
  public int calculateRegionEntryValueSize(RegionEntry re) {
    return 0;
  }
  public RegionMap getRegionMap() {
    throw new IllegalStateException("getRegionMap should not be called on ValidatingDiskRegion");
  }
  public void handleDiskAccessException(DiskAccessException dae, boolean b) {
    throw new IllegalStateException("handleDiskAccessException should not be called on ValidatingDiskRegion");
  }
  public void initializeStats(long numEntriesInVM, long numOverflowOnDisk,
      long numOverflowBytesOnDisk) {
    throw new IllegalStateException("initializeStats should not be called on ValidatingDiskRegion");
  }

  public int size() {
    return this.map.size();
  }
  
  static class ValidatingDiskEntry implements DiskEntry, RegionEntry {
    private final Object key;
    private final DiskId diskId;

    public ValidatingDiskEntry(Object key, DiskEntry.RecoveredEntry re) {
      this.key = key;
      this.diskId = DiskId.createDiskId(1, true, false);
      this.diskId.setKeyId(re.getRecoveredKeyId());
      this.diskId.setOffsetInOplog(re.getOffsetInOplog());
      this.diskId.setOplogId(re.getOplogId());
      this.diskId.setUserBits(re.getUserBits());
      this.diskId.setValueLength(re.getValueLength());
    }
    public Object getKey() {
      return this.key;
    }
    public Object getKeyCopy() {
      return this.key;
    }
    public Object getRawKey() {
      return this.key;
    }
    public Object _getValue() {
      return null;
    }
    @Override
    public Token getValueAsToken() {
      return null;
    }
    public Object _getValueRetain(RegionEntryContext context, boolean decompress) {
      return null;
    }
    
    public boolean isValueNull() {
      throw new IllegalStateException("should never be called");
    }
    
    public void _setValue(RegionEntryContext context, Object value) {
      throw new IllegalStateException("should never be called");
    }
    
    @Override
    public void setValueWithContext(RegionEntryContext context,Object value) {
      throw new IllegalStateException("should never be called");
    }    
    @Override
    public void handleValueOverflow(RegionEntryContext context) {throw new IllegalStateException("should never be called");}
    
    @Override
    public void afterValueOverflow(RegionEntryContext context) {throw new IllegalStateException();}
    
    @Override
    public Object prepareValueForCache(RegionEntryContext r, Object val, boolean isEntryUpdate,
        boolean valHasMetadataForGfxdOffHeapUpdate) {
      throw new IllegalStateException("Should never be called");
    }

    public void _removePhase1(LocalRegion r) {
      throw new IllegalStateException("should never be called");
    }

    public DiskId getDiskId() {
      return this.diskId;
    }

    public long getLastModified() {
      throw new IllegalStateException("should never be called");
    }
    public void _setLastModified(long lastModifiedTime) {
      throw new IllegalStateException("should never be called");
    }

    public void setLastModified(long lastModifiedTime) {
      throw new IllegalStateException("should never be called");
    }

    public boolean isLockedForCreate() {
      return false;
    }

    public int updateAsyncEntrySize(EnableLRU capacityController) {
      throw new IllegalStateException("should never be called");
    }
  
    public DiskEntry getPrev() {
      throw new IllegalStateException("should never be called");
    }
    public DiskEntry getNext() {
      throw new IllegalStateException("should never be called");
    }
    public void setPrev(DiskEntry v) {
      throw new IllegalStateException("should never be called");
    }
    public void setNext(DiskEntry v) {
      throw new IllegalStateException("should never be called");
    }
    /* (non-Javadoc)
     * @see com.gemstone.gemfire.internal.cache.DiskEntry#getVersionStamp()
     */
    @Override
    public VersionStamp getVersionStamp() {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public boolean isRemovedFromDisk() {
      throw new IllegalStateException("should never be called");
    }
    @Override
    public boolean hasStats() {
      // TODO Auto-generated method stub
      return false;
    }
    @Override
    public long getLastAccessed() throws InternalStatisticsDisabledException {
      // TODO Auto-generated method stub
      return 0;
    }
    @Override
    public long getHitCount() throws InternalStatisticsDisabledException {
      // TODO Auto-generated method stub
      return 0;
    }
    @Override
    public long getMissCount() throws InternalStatisticsDisabledException {
      // TODO Auto-generated method stub
      return 0;
    }
    @Override
    public void updateStatsForPut(long lastModifiedTime) {
      // TODO Auto-generated method stub
    }
    @Override
    public VersionTag generateVersionTag(VersionSource member,
        boolean isRemoteVersionSource, boolean withDelta, LocalRegion region,
        EntryEventImpl event) {
      // TODO Auto-generated method stub
      return null;
    }
    @Override
    public boolean dispatchListenerEvents(EntryEventImpl event)
        throws InterruptedException {
      // TODO Auto-generated method stub
      return false;
    }
    @Override
    public void setRecentlyUsed() {
      // TODO Auto-generated method stub
    }
    @Override
    public void updateStatsForGet(boolean hit, long time) {
      // TODO Auto-generated method stub
    }
    @Override
    public void txDidDestroy(long currTime) {
      // TODO Auto-generated method stub
    }
    @Override
    public void resetCounts() throws InternalStatisticsDisabledException {
      // TODO Auto-generated method stub
    }
    @Override
    public void makeTombstone(LocalRegion r, VersionTag version)
        throws RegionClearedException {
      // TODO Auto-generated method stub
    }
    @Override
    public void removePhase1(LocalRegion r, boolean clear)
        throws RegionClearedException {
      // TODO Auto-generated method stub
    }
    @Override
    public void removePhase2(LocalRegion r) {
      // TODO Auto-generated method stub
    }
    @Override
    public boolean isRemoved() {
      // TODO Auto-generated method stub
      return false;
    }
    @Override
    public boolean isRemovedPhase2() {
      // TODO Auto-generated method stub
      return false;
    }
    @Override
    public boolean isTombstone() {
      // TODO Auto-generated method stub
      return false;
    }
    @Override
    public boolean fillInValue(LocalRegion r, Entry entry,
        DM mgr, Version targetVersion) {
      // TODO Auto-generated method stub
      return false;
    }
    @Override
    public boolean isOverflowedToDisk(LocalRegion r, DiskPosition dp,
        boolean alwaysFetchPosition) {
      // TODO Auto-generated method stub
      return false;
    }
    @Override
    public Object getValue(RegionEntryContext context) {
      // TODO Auto-generated method stub
      return null;
    }
    @Override
    public void setValue(RegionEntryContext context, Object value)
        throws RegionClearedException {
      // TODO Auto-generated method stub
    }
    @Override
    public void setValueWithTombstoneCheck(Object value, EntryEvent event)
        throws RegionClearedException {
      // TODO Auto-generated method stub
    }
    @Override
    public Object getValueInVM(RegionEntryContext context) {
      // TODO Auto-generated method stub
      return null;
    }
    @Override
    public Object getValueOnDisk(LocalRegion r) throws EntryNotFoundException {
      // TODO Auto-generated method stub
      return null;
    }
    @Override
    public Object getValueOnDiskOrBuffer(LocalRegion r)
        throws EntryNotFoundException {
      // TODO Auto-generated method stub
      return null;
    }
    @Override
    public boolean initialImagePut(LocalRegion region, long lastModified,
        Object newValue, boolean wasRecovered, boolean acceptedVersionTag)
        throws RegionClearedException {
      // TODO Auto-generated method stub
      return false;
    }
    @Override
    public boolean initialImageInit(LocalRegion region, long lastModified,
        Object newValue, boolean create, boolean wasRecovered,
        boolean acceptedVersionTag) throws RegionClearedException {
      // TODO Auto-generated method stub
      return false;
    }
    @Override
    public boolean destroy(LocalRegion region, EntryEventImpl event,
        boolean inTokenMode, boolean cacheWrite, Object expectedOldValue,
        boolean forceDestroy, boolean removeRecoveredEntry)
        throws CacheWriterException, EntryNotFoundException, TimeoutException,
        RegionClearedException {
      // TODO Auto-generated method stub
      return false;
    }
    @Override
    public Object getSerializedValueOnDisk(LocalRegion localRegion) {
      // TODO Auto-generated method stub
      return null;
    }
    @Override
    public Object getValueInVMOrDiskWithoutFaultIn(LocalRegion owner) {
      // TODO Auto-generated method stub
      return null;
    }
    @Override
    public Object getValueOffHeapOrDiskWithoutFaultIn(LocalRegion owner) {
      // TODO Auto-generated method stub
      return null;
    }
    @Override
    public boolean isUpdateInProgress() {
      // TODO Auto-generated method stub
      return false;
    }
    @Override
    public void setUpdateInProgress(boolean underUpdate) {
      // TODO Auto-generated method stub
    }
    @Override
    public boolean isMarkedForEviction() {
      // TODO Auto-generated method stub
      return false;
    }
    @Override
    public void setMarkedForEviction() {
      // TODO Auto-generated method stub
    }
    @Override
    public void clearMarkedForEviction() {
      // TODO Auto-generated method stub
    }
    @Override
    public boolean isInvalid() {
      // TODO Auto-generated method stub
      return false;
    }
    @Override
    public boolean isDestroyed() {
      // TODO Auto-generated method stub
      return false;
    }
    @Override
    public boolean isDestroyedOrRemoved() {
      // TODO Auto-generated method stub
      return false;
    }
    @Override
    public boolean isDestroyedOrRemovedButNotTombstone() {
      // TODO Auto-generated method stub
      return false;
    }
    @Override
    public boolean isInvalidOrRemoved() {
      // TODO Auto-generated method stub
      return false;
    }
    @Override
    public boolean isOffHeap() {
      return false;
    }
    @Override
    public void setValueToNull(RegionEntryContext context) {
      // TODO Auto-generated method stub
    }
    @Override
    public void returnToPool() {
      // TODO Auto-generated method stub
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public Object getOwnerId(Object context) {
      // TODO Auto-generated method stub
      return null;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean attemptLock(LockMode mode, int flags,
        LockingPolicy lockPolicy, long msecs, Object owner, Object context) {
      // TODO Auto-generated method stub
      return false;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public void releaseLock(LockMode mode, boolean releaseAll, Object owner,
        Object context) {
      // TODO Auto-generated method stub
      
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public int numSharedLocks() {
      // TODO Auto-generated method stub
      return 0;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public int numReadOnlyLocks() {
      // TODO Auto-generated method stub
      return 0;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasExclusiveLock(Object owner, Object context) {
      // TODO Auto-generated method stub
      return false;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasExclusiveSharedLock(Object ownerId, Object context) {
      // TODO Auto-generated method stub
      return false;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public int getState() {
      // TODO Auto-generated method stub
      return 0;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasAnyLock() {
      // TODO Auto-generated method stub
      return false;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public void setOwner(LocalRegion owner, Object previousOwner) {
      // TODO Auto-generated method stub
      
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public Object getContainerInfo() {
      // TODO Auto-generated method stub
      return null;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public Object setContainerInfo(LocalRegion owner, Object val) {
      // TODO Auto-generated method stub
      return null;
    }
    @Override
    public boolean isCacheListenerInvocationInProgress() {
      // TODO Auto-generated method stub
      return false;
    }
    @Override
    public void setCacheListenerInvocationInProgress(boolean isListenerInvoked) {
      // TODO Auto-generated method stub
      
    }
  }


  //////////////// DiskExceptionHandler methods ////////////////////

  public static class DummyDiskExceptionHandler implements DiskExceptionHandler {
    public void handleDiskAccessException(DiskAccessException dae, boolean stopBridgeServers) {
      // nothing needed
    }
    public boolean shouldStopServer() {
      return false;
    }
  }

  private static class DummyCancelCriterion extends CancelCriterion {

    @Override
    public String cancelInProgress() {
      return null;
    }

    @Override
    public RuntimeException generateCancelledException(Throwable e) {
      return new RuntimeException(e);
    }

  }
}
