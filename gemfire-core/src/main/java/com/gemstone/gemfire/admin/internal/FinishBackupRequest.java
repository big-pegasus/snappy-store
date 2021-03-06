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
package com.gemstone.gemfire.admin.internal;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.gemstone.gemfire.CancelException;
import com.gemstone.gemfire.DataSerializer;
import com.gemstone.gemfire.cache.persistence.PersistentID;
import com.gemstone.gemfire.distributed.DistributedMember;
import com.gemstone.gemfire.distributed.internal.DM;
import com.gemstone.gemfire.distributed.internal.DistributionManager;
import com.gemstone.gemfire.distributed.internal.DistributionMessage;
import com.gemstone.gemfire.distributed.internal.ReplyException;
import com.gemstone.gemfire.internal.admin.remote.AdminFailureResponse;
import com.gemstone.gemfire.internal.admin.remote.AdminMultipleReplyProcessor;
import com.gemstone.gemfire.internal.admin.remote.AdminRequest;
import com.gemstone.gemfire.internal.admin.remote.AdminResponse;
import com.gemstone.gemfire.internal.cache.GemFireCacheImpl;

/**
 * A request send from an admin VM to all of the peers to indicate
 * that that should complete the backup operation.
 * 
 * @author dsmith
 *
 */
public class FinishBackupRequest  extends AdminRequest {

  public static final byte DISKSTORE_DD = 1;
  public static final byte DISKSTORE_ALL_BUT_DD = 2;
  public static final byte DISKSTORE_ALL = 3;

  private File targetDir;
  private File baselineDir;
  private byte diskstoresToBackup;

  public FinishBackupRequest() {
    super();
  }

  public FinishBackupRequest(File targetDir, File baselineDir, byte dses) {
    this.targetDir = targetDir;
    this.baselineDir = baselineDir;
    this.diskstoresToBackup = dses;
  }
  
  public static Map<DistributedMember, Set<PersistentID>> send(DM dm,
      Set recipients, File targetDir, File baselineDir, byte diskstores) {
    FinishBackupRequest request = new FinishBackupRequest(targetDir, baselineDir, diskstores);
    request.setRecipients(recipients);

    FinishBackupReplyProcessor replyProcessor = new FinishBackupReplyProcessor(dm, recipients);
    request.msgId = replyProcessor.getProcessorId();
    dm.putOutgoing(request);
    try {
      replyProcessor.waitForReplies();
    } catch (ReplyException e) {
      if(!(e.getCause() instanceof CancelException)) {
        e.handleAsUnexpected();
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    AdminResponse response = request.createResponse((DistributionManager)dm);
    response.setSender(dm.getDistributionManagerId());
    replyProcessor.process(response);
    return replyProcessor.results;
  }
  
  @Override
  protected AdminResponse createResponse(DistributionManager dm) {
    GemFireCacheImpl cache = GemFireCacheImpl.getInstance();
    HashSet<PersistentID> persistentIds;
    if(cache == null) {
      persistentIds = new HashSet<PersistentID>();
    } else {
      try {
        persistentIds = cache.getBackupManager().finishBackup(targetDir, baselineDir, diskstoresToBackup);
      } catch (IOException e) {
        return AdminFailureResponse.create(dm, getSender(), e);
      }
    }
    return new FinishBackupResponse(this.getSender(), persistentIds);
  }

  public int getDSFID() {
    return FINISH_BACKUP_REQUEST;
  }
  
  @Override
  public void fromData(DataInput in) throws IOException, ClassNotFoundException {
    super.fromData(in);
    targetDir = DataSerializer.readFile(in);
    baselineDir = DataSerializer.readFile(in);
    this.diskstoresToBackup = in.readByte();
  }

  @Override
  public void toData(DataOutput out) throws IOException {
    super.toData(out);
    DataSerializer.writeFile(targetDir, out);
    DataSerializer.writeFile(baselineDir, out);
    out.writeByte(diskstoresToBackup);
  }

  private static class FinishBackupReplyProcessor extends AdminMultipleReplyProcessor {
    Map<DistributedMember, Set<PersistentID>> results = Collections.synchronizedMap(new HashMap<DistributedMember, Set<PersistentID>>());
    public FinishBackupReplyProcessor(DM dm, Collection initMembers) {
      super(dm, initMembers);
    }
    
    @Override
    protected boolean stopBecauseOfExceptions() {
      return false;
    }

    
    
    @Override
    protected int getAckWaitThreshold() {
      //Disable the 15 second warning if the backup is taking a long time
      return 0;
    }

    @Override
    public long getAckSevereAlertThresholdMS() {
      //Don't log severe alerts for backups either
      return Long.MAX_VALUE;
    }

    @Override
    protected void process(DistributionMessage msg, boolean warn) {
      if(msg instanceof FinishBackupResponse) {
        final HashSet<PersistentID> persistentIds = ((FinishBackupResponse) msg).getPersistentIds();
        if(persistentIds != null && !persistentIds.isEmpty()) {
          results.put(msg.getSender(), persistentIds);
        }
      }
      super.process(msg, warn);
    }
    
    

  }
}
