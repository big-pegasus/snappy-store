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

package com.gemstone.gemfire.internal.i18n;

import com.gemstone.gemfire.InternalGemFireException;
import com.gemstone.gemfire.i18n.StringIdImpl;
import com.gemstone.gemfire.internal.ClassPathLoader;
import com.gemstone.org.jgroups.util.StringId;
import io.snappydata.collection.IntObjectHashMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;


/**
 * Baseclass for all {@link StringId} based ResourceBundles
 * @see java.util.ResourceBundle 
 *
 * @author kbanks 
 * @since 5.7 
 */
public class AbstractStringIdResourceBundle {
  private IntObjectHashMap<String> data;

  /** 
   * The {@link java.util.ResourceBundle} that implements the message lookup
   * English has a special implementation for speed.
   */
  private static AbstractStringIdResourceBundle messageBundle;
  
  /**
   * Init method to populate the IntObjectHashMap for Non-english locales
   * <code>data = IntObjectHashMap.withExpectedSize(...);</code>
   *
   * The default bundle, English, will be
   * <code>data = null</code>
   */
  private void initData(String baseName, Locale l) {
    StringBuffer sb = new StringBuffer(baseName);
    sb.append("_").append(l.getLanguage()).append(".txt");
    String resource = sb.toString();

    InputStream is = null;
    try {
      is = ClassPathLoader.getLatest().getResourceAsStream(getClass(), resource);
    } catch (SecurityException se) {
      //We do not have a logger yet
      System.err.println("A SecurityException occurred while attempting to load the resource bundle, defaulting to English." + se.toString());
      se.printStackTrace();
      System.err.flush();
    }
    if ( is == null ) {
      //No matching data file for the requested langauge, 
      //defaulting to English
      data = null;
    } else {
      data = readDataFile(is);
    }
  }
	
  private IntObjectHashMap<String> readDataFile(InputStream is) {
    IntObjectHashMap<String> map = IntObjectHashMap.withExpectedSize(128);
    boolean complete = false;
    BufferedReader input = null;
    try {
       input = new BufferedReader(new InputStreamReader(is, "UTF-8"));
       String line = null;
       while((line = input.readLine()) != null ) {
         int equalSign = line.indexOf('=');
         String idAsString = line.substring(0, equalSign-1).trim();
         //The +2 is because we need to skip the "= ", we dont use trim because some messages want leading whitespace
         String message = line.substring(equalSign+2).replaceAll("\\\\n", "\n");
         try {
           int id = Integer.parseInt(idAsString);
           map.justPut(id, message);
         } catch(NumberFormatException nfe) {
           //unit tests should prevent this from happening in a customer situation
           throw new InternalGemFireException(nfe);
         }
         complete = true;
       }
    } catch( IOException ioe ) {
      //@TODO log this exception
    } finally {
      if ( ! complete ) {
        //something went wrong, clean up and revert back to English
        try {
          if ( input != null ) {
            input.close();
          } else {
            is.close();
          }
        } catch (IOException ignore ) {}
        //set map back to null so we default to English
        map = null;
      }
    }
    return map;
  }
  
  private AbstractStringIdResourceBundle() {
    //Intentionally blank  
  }
  
  /**
   * @param key
   *        StringId passed to {@link #getString java.util.ResourceBundle} 
   *        for translation.
   * @return a String translated to the current {@link java.util.Locale}
   */
  public String getString(StringId key) {
    if (usingRawMode())
      return key.getRawText();
    String txt = data.get(((StringIdImpl)key).id);
    if( txt != null ) {
      return txt;
    } else {
      //found an untranslated message, use the English as a fall back
      return key.getRawText();
    }
    
  }

  /**
   * Returns true if this resource bundle will always return english strings.
   */
  public boolean usingRawMode() {
    return this.data == null;
  }

  public static AbstractStringIdResourceBundle getBundle(String baseName, Locale l) {
    AbstractStringIdResourceBundle newMessageBundle = new AbstractStringIdResourceBundle();
    newMessageBundle.initData(baseName, l);
    messageBundle = newMessageBundle;
    return messageBundle;
  }
   
}
