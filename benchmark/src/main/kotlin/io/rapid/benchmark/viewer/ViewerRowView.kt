package io.rapid.benchmark.viewer


import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import io.rapid.benchmark.databinding.ItemViewerHeaderBinding
import io.rapid.benchmark.databinding.ItemViewerValueBinding


class ViewerRowView : LinearLayout {
    var header: Boolean = false

    constructor(context: Context) : super(context) {
        init()
    }


    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }


    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    fun setItems(items: Collection<String>?) {
        removeAllViews()
        items?.forEach {
            if (header) {
                val binding = ItemViewerHeaderBinding.inflate(LayoutInflater.from(context), this, true)
                binding.value = it
            } else {
                val binding = ItemViewerValueBinding.inflate(LayoutInflater.from(context), this, true)
                binding.value = it
            }
        }
    }

    private fun init() {

    }
}
