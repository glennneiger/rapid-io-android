package io.rapid;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by Leos on 31.03.2017.
 */

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Index {
	class Cache {
		private static Cache sInstance;
		Map<String, Boolean> mCache = new HashMap<>();


		public static Cache getInstance() {
			if(sInstance == null)
				sInstance = new Cache();
			return sInstance;
		}


		public void put(String className, String fieldName, boolean indexed) {
			mCache.put(getKey(className, fieldName), indexed);
		}


		public Boolean get(String className, String fieldName) {
			return mCache.get(getKey(className, fieldName));
		}


		private String getKey(String className, String fieldName) {
			return className + "$" + fieldName;
		}
	}
}
