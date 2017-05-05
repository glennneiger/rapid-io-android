package io.rapid.benchmark.viewer

import android.databinding.ObservableField
import cz.kinst.jakub.viewmodelbinding.ViewModel
import io.rapid.Rapid
import io.rapid.benchmark.BR
import io.rapid.benchmark.Config
import io.rapid.benchmark.R
import me.tatarka.bindingcollectionadapter2.ItemBinding


class ViewerViewModel : ViewModel() {
    val collectionName = ObservableField<String>()
    val itemBinding: ItemBinding<ViewerItemViewModel> = ItemBinding.of(BR.viewModel, R.layout.item_viewer)
    val items = ObservableField<List<ViewerItemViewModel>>()

    val headerItems = ObservableField<Collection<String>>()

    override fun onViewModelCreated() {
        super.onViewModelCreated()
        Rapid.getInstance(Config.API_KEY).collection(collectionName.get())
                .subscribe { items ->
                    if (!items.isEmpty()) {
                        val firstDoc = items[0]
                        headerItems.set(ArrayList<String>(firstDoc.body.keys).apply { add(0, "\$ID") })
                        val tmp = ArrayList<ViewerItemViewModel>()
                        for (i in 0..items.size - 1) {
                            tmp.add(ViewerItemViewModel(items[i]))
                        }
                        this.items.set(tmp)
                    }

                }
    }
}