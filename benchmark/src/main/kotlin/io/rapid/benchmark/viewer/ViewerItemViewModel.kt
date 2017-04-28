package io.rapid.benchmark.viewer

import io.rapid.RapidDocument


class ViewerItemViewModel(val item: RapidDocument<Map<String, Any>>) {
    val items: List<String> = ArrayList<String>(item.body.values.map { it.toString() }).apply { add(0, item.id) }
}