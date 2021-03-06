/*

   Derby - Class com.pivotal.gemfirexd.internal.impl.sql.catalog.DDdependableFinder

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

/*
 * Changes for GemFireXD distributed data platform (some marked by "GemStone changes")
 *
 * Portions Copyright (c) 2010-2015 Pivotal Software, Inc. All rights reserved.
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

package	com.pivotal.gemfirexd.internal.impl.sql.catalog;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import com.pivotal.gemfirexd.internal.catalog.Dependable;
import com.pivotal.gemfirexd.internal.catalog.DependableFinder;
import com.pivotal.gemfirexd.internal.catalog.UUID;
import com.pivotal.gemfirexd.internal.iapi.error.StandardException;
import com.pivotal.gemfirexd.internal.iapi.reference.SQLState;
import com.pivotal.gemfirexd.internal.iapi.services.io.Formatable;
import com.pivotal.gemfirexd.internal.iapi.services.sanity.SanityManager;
import com.pivotal.gemfirexd.internal.iapi.sql.dictionary.ColumnDescriptor;
import com.pivotal.gemfirexd.internal.iapi.sql.dictionary.DataDictionary;
import com.pivotal.gemfirexd.internal.iapi.sql.dictionary.DefaultDescriptor;
import com.pivotal.gemfirexd.internal.iapi.sql.dictionary.DependencyDescriptor;
import com.pivotal.gemfirexd.internal.shared.common.StoredFormatIds;

/**
 *	Class for most DependableFinders in the core DataDictionary.
 * This class is stored in SYSDEPENDS for the finders for
 * the provider and dependent. It stores no state, its functionality
 * is driven off its format identifier.
 *
 *
 */

public class DDdependableFinder implements	DependableFinder, Formatable
{
	////////////////////////////////////////////////////////////////////////
	//
	//	STATE
	//
	////////////////////////////////////////////////////////////////////////

        // Gemstone changes BEGIN -- removed final as it is modified after
        //                           zero-arg constructor is called
        private int formatId;
        // Gemstone changes END
        //private final int formatId;

	////////////////////////////////////////////////////////////////////////
	//
	//	CONSTRUCTORS
	//
	////////////////////////////////////////////////////////////////////////

	/**
	  *	Public constructor for Formatable hoo-hah.
	  */
	public	DDdependableFinder(int formatId)
	{
		this.formatId = formatId;
	}

        // GemStone changes BEGIN
        /**
          *     Zero-arg constructor for Formatable interface.
          */
        public  DDdependableFinder()
        {
        }
        // GemStone changes END

	//////////////////////////////////////////////////////////////////
	//
	//	OBJECT SUPPORT
	//
	//////////////////////////////////////////////////////////////////

	public	String	toString()
	{
		return	getSQLObjectType();
	}

	//////////////////////////////////////////////////////////////////
	//
	//	VACUOUS FORMATABLE INTERFACE. ALL THAT A VACUOUSDEPENDABLEFINDER
	//	NEEDS TO DO IS STAMP ITS FORMAT ID ONTO THE OUTPUT STREAM.
	//
	//////////////////////////////////////////////////////////////////

	/**
	 * Read this object from a stream of stored objects. Nothing to
	 * do. Our persistent representation is just a 2-byte format id.
	 *
	 * @param in read this.
	 */
    public void readExternal( ObjectInput in )
			throws IOException, ClassNotFoundException
	{
           // Gemstone changes BEGIN
           // Read in the formatId as an int
           formatId = in.readInt();
           // Gemstone changes END
	}

	/**
	 * Write this object to a stream of stored objects. Again, nothing
	 * to do. We just stamp the output stream with our Format id.
	 *
	 * @param out write bytes here.
	 */
    public void writeExternal( ObjectOutput out )
			throws IOException
	{
           // Gemstone changes BEGIN
           // Write out the formatId as an int
           out.writeInt( formatId );
           // Gemstone changes END
	}

	/**
	 * Get the formatID which corresponds to this class.
	 *
	 *	@return	the formatID of this class
	 */
	public	final int	getTypeFormatId()	
	{
		return formatId;
	}

	////////////////////////////////////////////////////////////////////////
	//
	//	DDdependable METHODS
	//
	////////////////////////////////////////////////////////////////////////

	/**
	  * @see DependableFinder#getSQLObjectType
	  */
	public	String	getSQLObjectType()
	{
		switch (formatId)
		{
			case StoredFormatIds.ALIAS_DESCRIPTOR_FINDER_V01_ID:
				return Dependable.ALIAS;

			case StoredFormatIds.CONGLOMERATE_DESCRIPTOR_FINDER_V01_ID:
				return Dependable.CONGLOMERATE;

			case StoredFormatIds.CONSTRAINT_DESCRIPTOR_FINDER_V01_ID:
				return Dependable.CONSTRAINT;

			case StoredFormatIds.DEFAULT_DESCRIPTOR_FINDER_V01_ID:
				return Dependable.DEFAULT;

			case StoredFormatIds.FILE_INFO_FINDER_V01_ID:
				return Dependable.FILE;

			case StoredFormatIds.SCHEMA_DESCRIPTOR_FINDER_V01_ID:
				return Dependable.SCHEMA;

			case StoredFormatIds.SPS_DESCRIPTOR_FINDER_V01_ID:
				return Dependable.STORED_PREPARED_STATEMENT;

			case StoredFormatIds.TABLE_DESCRIPTOR_FINDER_V01_ID:
				return Dependable.TABLE;

			case StoredFormatIds.COLUMN_DESCRIPTOR_FINDER_V01_ID:
				return Dependable.COLUMNS_IN_TABLE;

			case StoredFormatIds.TRIGGER_DESCRIPTOR_FINDER_V01_ID:
				return Dependable.TRIGGER;

			case StoredFormatIds.VIEW_DESCRIPTOR_FINDER_V01_ID:
				return Dependable.VIEW;

			case StoredFormatIds.TABLE_PERMISSION_FINDER_V01_ID:
				return Dependable.TABLE_PERMISSION;
			
			case StoredFormatIds.COLUMNS_PERMISSION_FINDER_V01_ID:
				return Dependable.COLUMNS_PERMISSION;

			case StoredFormatIds.ROUTINE_PERMISSION_FINDER_V01_ID:
				return Dependable.ROUTINE_PERMISSION;

// GemStone changes BEGIN
	                    case StoredFormatIds.JAR_DEPENDENTS_FINDER_V01_ID:
                              return Dependable.JAR_DROP_REPLACE;
                              
	                    case StoredFormatIds.ASYNC_EVENT_LISTENER_DESCRIPTOR_FINDER_V01_ID:
	                      return Dependable.ASYNC_LISTENER;
// GemStone changes END
			default:
				if (SanityManager.DEBUG)
				{
					SanityManager.THROWASSERT(
						"getSQLObjectType() called with unexpeced formatId = " + formatId);
				}
				return null;
		}
	}

	/**
		Get the dependable for the given UUID
		@exception StandardException thrown on error
	*/
	public final Dependable getDependable(DataDictionary dd, UUID dependableObjectID)
		throws StandardException
	{
        Dependable dependable = findDependable(dd, dependableObjectID);
        if (dependable == null)
            throw StandardException.newException(SQLState.LANG_OBJECT_NOT_FOUND,
                    getSQLObjectType(), dependableObjectID);
        return dependable;
    }
        
       
    /**
     * Find the dependable for getDependable.
     * Can return a null references, in which case getDependable()
     * will thrown an exception.
     */
    Dependable findDependable(DataDictionary dd, UUID dependableObjectID)
        throws StandardException
    {     
		switch (formatId)
		{
			case StoredFormatIds.ALIAS_DESCRIPTOR_FINDER_V01_ID:
                return dd.getAliasDescriptor(dependableObjectID);

			case StoredFormatIds.CONGLOMERATE_DESCRIPTOR_FINDER_V01_ID:
                return dd.getConglomerateDescriptor(dependableObjectID);

			case StoredFormatIds.CONSTRAINT_DESCRIPTOR_FINDER_V01_ID:
                return dd.getConstraintDescriptor(dependableObjectID);

			case StoredFormatIds.DEFAULT_DESCRIPTOR_FINDER_V01_ID:
				ColumnDescriptor	cd = dd.getColumnDescriptorByDefaultId(dependableObjectID);
                if (cd != null)
                    return new DefaultDescriptor(
												dd, 
												cd.getDefaultUUID(), cd.getReferencingUUID(), 
												cd.getPosition());
                return null;

			case StoredFormatIds.FILE_INFO_FINDER_V01_ID:
                return dd.getFileInfoDescriptor(dependableObjectID);

			case StoredFormatIds.SCHEMA_DESCRIPTOR_FINDER_V01_ID:
                return dd.getSchemaDescriptor(dependableObjectID, null);

			case StoredFormatIds.SPS_DESCRIPTOR_FINDER_V01_ID:
                return dd.getSPSDescriptor(dependableObjectID);

			case StoredFormatIds.TABLE_DESCRIPTOR_FINDER_V01_ID:
                return dd.getTableDescriptor(dependableObjectID);

			case StoredFormatIds.TRIGGER_DESCRIPTOR_FINDER_V01_ID:
                return dd.getTriggerDescriptor(dependableObjectID);
 
			case StoredFormatIds.VIEW_DESCRIPTOR_FINDER_V01_ID:
                return dd.getViewDescriptor(dependableObjectID);

            case StoredFormatIds.COLUMNS_PERMISSION_FINDER_V01_ID:
                return dd.getColumnPermissions(dependableObjectID);

			case StoredFormatIds.TABLE_PERMISSION_FINDER_V01_ID:
                return dd.getTablePermissions(dependableObjectID);

			case StoredFormatIds.ROUTINE_PERMISSION_FINDER_V01_ID:
                return dd.getRoutinePermissions(dependableObjectID);

//GemStone changes BEGIN
           case StoredFormatIds.ASYNC_EVENT_LISTENER_DESCRIPTOR_FINDER_V01_ID:
	        return dd.getAsyncEventListenerDescriptor(dependableObjectID);
//GemStone changes END
			default:
				if (SanityManager.DEBUG)
				{
					SanityManager.THROWASSERT(
						"getDependable() called with unexpeced formatId = " + formatId);
				}
                return null;
		}
    }
}
