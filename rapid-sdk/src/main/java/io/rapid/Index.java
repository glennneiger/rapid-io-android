package io.rapid;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by Leos on 31.03.2017.
 */

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Index {
	String value() default "";


	class Cache {
		private static Cache sInstance;
		Map<String, List<String>> mCache = new HashMap<>();


		public static Cache getInstance() {
			if(sInstance == null)
				sInstance = new Cache();
			return sInstance;
		}


		public void put(String className, List<String> indexList) {
			mCache.put(className, indexList);
		}


		public List<String> get(String className) {
			return mCache.get(className);
		}
	}
}
