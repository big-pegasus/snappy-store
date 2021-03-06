/*

   Derby - Class com.pivotal.gemfirexd.internal.impl.sql.execute.rts.RealHashLeftOuterJoinStatistics

   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to you under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */

package com.pivotal.gemfirexd.internal.impl.sql.execute.rts;

import com.pivotal.gemfirexd.internal.iapi.reference.SQLState;
import com.pivotal.gemfirexd.internal.iapi.services.i18n.MessageService;


/**
  ResultSetStatistics implemenation for HashLeftOuterJoinResultSet.


*/
public class RealHashLeftOuterJoinStatistics 
	extends RealNestedLoopLeftOuterJoinStatistics
{


	// CONSTRUCTORS

	/**
	 * 
	 *
	 */
    public	RealHashLeftOuterJoinStatistics(
								int numOpens,
								int rowsSeen,
								int rowsFiltered,
								long constructorTime,
								long openTime,
								long nextTime,
								long closeTime,
								int resultSetNumber,
								int rowsSeenLeft,
								int rowsSeenRight,
								int rowsReturned,
								long restrictionTime,
								double optimizerEstimatedRowCount,
								double optimizerEstimatedCost,
								String userSuppliedOptimizerOverrides,
								ResultSetStatistics leftResultSetStatistics,
								ResultSetStatistics rightResultSetStatistics,
								int emptyRightRowsReturned
								)
	{
		super(
			numOpens,
			rowsSeen,
			rowsFiltered,
			constructorTime,
			openTime,
			nextTime,
			closeTime,
			resultSetNumber,
			rowsSeenLeft,
			rowsSeenRight,
			rowsReturned,
			restrictionTime,
			optimizerEstimatedRowCount,
			optimizerEstimatedCost,
			userSuppliedOptimizerOverrides,
			leftResultSetStatistics,
			rightResultSetStatistics,
			emptyRightRowsReturned
			);
	}

	// ResultSetStatistics methods

	// Class implementation
	protected void setNames()
	{
		nodeName = MessageService.getTextMessage(SQLState.RTS_HASH_LEFT_OJ);
		resultSetName =
			MessageService.getTextMessage(SQLState.RTS_HASH_LEFT_OJ_RS);
	}
}
