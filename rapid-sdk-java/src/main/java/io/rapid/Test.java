package io.rapid;

import java.io.IOException;


public class Test {
	public static void main(String[] attrs) {
		Rapid.setLoggerOutput(new LoggerOutput() {
			@Override
			public void error(String message, Throwable throwable) {
				System.err.println(message);
				if(throwable != null)
					throwable.printStackTrace();
			}


			@Override
			public void info(String message, Throwable throwable) {
				System.out.println(message);
				if(throwable != null)
					throwable.printStackTrace();
			}


			@Override
			public void warning(String message, Throwable throwable) {
				System.out.println(message);
				if(throwable != null)
					throwable.printStackTrace();
			}
		});

		Rapid.setCacheProvider(new CacheProvider() {
			@Override
			public DiskCache getNewDiskCache(String apiKey) {
				return new DiskCache() {
					@Override
					public void delete() throws IOException {

					}


					@Override
					public void setMaxSize(int maxSizeInBytes) {

					}


					@Override
					public void remove(String key) throws IOException {

					}


					@Override
					public String get(String key) throws IOException {
						return null;
					}


					@Override
					public void put(String key, String value) throws IOException {

					}
				};
			}


			@Override
			public <T> MemoryCache<T> getNewMemoryCache(int maxValue) {
				return new MemoryCache<T>() {
					@Override
					public void put(String key, T value) {

					}


					@Override
					public T get(String key) {
						return null;
					}


					@Override
					public void evictAll() {

					}


					@Override
					public void remove(String key) {

					}
				};
			}
		});

		Rapid.setExecutor(new RapidJavaExecutor());

		Rapid.initialize("ZGV2LXdzLXNlcnZpY2UucmFwaWQuaW8=");
		Rapid.getInstance().authorize("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJydWxlcyI6W3siY2hhbm5lbCI6eyJwYXR0ZXJuIjoiLioifSwicmVhZCI6dHJ1ZSwid3JpdGUiOnRydWV9LHsiY29sbGVjdGlvbiI6eyJwYXR0ZXJuIjoiLioifSwicmVhZCI6dHJ1ZSwiY3JlYXRlIjp0cnVlLCJ1cGRhdGUiOnRydWUsImRlbGV0ZSI6dHJ1ZX1dfQ.MdQbdW958yzRQk46qj7_bY92A60pxtSkDgy9yJV7Vd8");
		Rapid.getInstance().collection("___java_tests_002", String.class).subscribe(rapidDocuments -> {
			System.out.println(rapidDocuments.toString());
		});
	}
}
