package io.rapid.benchmark.viewer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import cz.kinst.jakub.viewmodelbinding.ViewModelActivity
import io.rapid.benchmark.R
import io.rapid.benchmark.databinding.ActivityViewerBinding


class ViewerActivity : ViewModelActivity<ActivityViewerBinding, ViewerViewModel>() {

    companion object {
        private val EXTRA_COLLECTION_NAME = "collection"
        fun getIntent(context: Context, collectionName: String) = Intent(context, ViewerActivity::class.java).apply {
            putExtra(EXTRA_COLLECTION_NAME, collectionName)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setupViewModel(R.layout.activity_viewer, ViewerViewModel::class.java)
        super.onCreate(savedInstanceState)
    }

    override fun onViewModelInitialized(viewModel: ViewerViewModel) {
        super.onViewModelInitialized(viewModel)
        viewModel.collectionName.set(intent.getStringExtra(EXTRA_COLLECTION_NAME))
    }
}
