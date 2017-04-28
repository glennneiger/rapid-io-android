package io.rapid.benchmark.viewer

import android.databinding.ObservableField
import cz.kinst.jakub.viewmodelbinding.ViewModel
import io.rapid.Rapid
import io.rapid.RapidDocument
import io.rapid.benchmark.BR
import io.rapid.benchmark.Config
import io.rapid.benchmark.R
import me.tatarka.bindingcollectionadapter2.ItemBinding
import me.tatarka.bindingcollectionadapter2.collections.DiffObservableList


class ViewerViewModel : ViewModel() {
    val collectionName = ObservableField<String>()
    val itemBinding: ItemBinding<ViewerItemViewModel> = ItemBinding.of(BR.viewModel, R.layout.item_viewer);
    val items = DiffObservableList<ViewerItemViewModel>(object : DiffObservableList.Callback<ViewerItemViewModel> {
        override fun areItemsTheSame(oldItem: ViewerItemViewModel, newItem: ViewerItemViewModel) = oldItem.item.id == newItem.item.id
        override fun areContentsTheSame(oldItem: ViewerItemViewModel, newItem: ViewerItemViewModel) = oldItem.item == newItem.item
    })
    val headerItems = ObservableField<Collection<String>>()

    override fun onViewModelCreated() {
        super.onViewModelCreated()
        Rapid.getInstance(Config.API_KEY).collection(collectionName.get(), Map::class.java)
                .subscribe { items ->
                    val firstDoc = items[0] as RapidDocument<Map<String, Any>>
                    headerItems.set(ArrayList<String>(firstDoc.body.keys).apply { add(0, "\$ID") })
                    this.items.update(items.map { ViewerItemViewModel(it as RapidDocument<Map<String, Any>>) })
                }
    }
}