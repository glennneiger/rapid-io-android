package io.rapid;

import android.support.annotation.NonNull;


interface FutureResolver<T> {
	@NonNull
	T resolve();
}
