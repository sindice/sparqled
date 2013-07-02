/**
 * Copyright (c) 2012 National University of Ireland, Galway. All Rights Reserved.
 *
 *
 * This project is a free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * This project is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public
 * License along with this project. If not, see <http://www.gnu.org/licenses/>.
 */
package org.sindice.core.analytics.commons.webapps;


/**
 * @author thomas
 *
 */
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.concurrent.Future;

import net.spy.memcached.MemcachedClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * TODO://refactor this class to use advance version of getting values from memcached 
 * 
 * // Try to get a value, for up to 5 seconds, and cancel if it
      // doesn't return
      Object myObj = null;
      Future<Object> f = c.asyncGet("someKey");
      try {
          myObj = f.get(5, TimeUnit.SECONDS);
      // throws expecting InterruptedException, ExecutionException
      // or TimeoutException
      } catch (Exception e) {  /*  /
          // Since we don't need this, go ahead and cancel the operation.
          // This is not strictly necessary, but it'll save some work on
          // the server.  It is okay to cancel it if running.
          f.cancel(true);
          // Do other timeout related stuff
      }
 * 
 */

public class MemcachedClientWrapper {
        private static final Logger LOGGER = LoggerFactory.getLogger(MemcachedClientWrapper.class);

        
        private static final String CACHE_KEY_PREFIX = "sparql_editor";
        private static final char CAHCHE_KEY_REPLACE_CHAR = '_';
        private MemcachedClient mc=null;
        
        
        public MemcachedClientWrapper(MemcachedClient mc){
                this.mc=mc;
        }
        public boolean isActive(){
                if(this.mc!=null){
                        return true;
                }
                return false;
        }
        
        public Object get(String key) {
                return mc.get(key);
        }

        public void add(String key, int exp, Object o) {
                mc.add(key, exp, o);
        }

        public Future<Boolean> flush() {
            return mc.flush();
        }
        
        public void shutdown(){
        	mc.shutdown();
        }
        
        
    public static String getCacheKey(Map<String,String> list) {
        String key = CACHE_KEY_PREFIX;
        String uniquePart ="";
        for(Map.Entry<String,String> entry:list.entrySet()){
            uniquePart+=entry.getKey()+":"+entry.getValue();
        }
        uniquePart = uniquePart.replace(' ', CAHCHE_KEY_REPLACE_CHAR)
                               .replace('\n',CAHCHE_KEY_REPLACE_CHAR)
                               .replace('\r',CAHCHE_KEY_REPLACE_CHAR);
        key+=uniquePart;
        
        if(key.length()>=128){
			try {      
	            byte[] bytes = key.getBytes(Charset.forName("UTF-8"));
	            MessageDigest digest;
				digest = MessageDigest.getInstance("SHA-1");
	            BigInteger bigInt = new BigInteger(1, digest.digest(bytes));
	            String md5 = bigInt.toString(16);
	            key = CACHE_KEY_PREFIX+uniquePart.substring(0,80)+md5;
			} catch (NoSuchAlgorithmException e) {
				 LOGGER.warn("problem making hash",e);
			}
           
          
        }
        return key;
      }
}
