package io.rapid.benchmark


import android.app.Activity
import android.content.Intent
import android.databinding.Observable
import android.databinding.ObservableField
import android.databinding.ObservableInt
import android.preference.PreferenceManager
import android.widget.Toast
import com.google.firebase.database.FirebaseDatabase
import cz.kinst.jakub.viewmodelbinding.ViewModel
import io.rapid.ConnectionState
import io.rapid.Rapid
import io.rapid.benchmark.viewer.ViewerActivity
import java.io.FileDescriptor
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.InputStream


class MainViewModel : ViewModel() {
    private val RC_FILE = 1
    private val KEY_COLLECTION_NAME = "col_name"

    var delayEnabled = false
    var rapid = true
    var total = 0
    var success = 0
    var error = 0
    var testStarted = 0L
    var connectStarted = 0L
    var connectionTime = 0L
    var log: Log? = null
    val times = ArrayList<Long>()
    val console = ObservableField<String>()
    val collectionName = ObservableField<String>("")
    val csvImportProgress = ObservableInt()
    private var filePickerCallback: ((InputStream) -> Unit)? = null

    override fun onViewModelCreated() {
        super.onViewModelCreated()
        collectionName.set(PreferenceManager.getDefaultSharedPreferences(applicationContext).getString(KEY_COLLECTION_NAME, ""))
        collectionName.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(obs: Observable?, p1: Int) {
                PreferenceManager.getDefaultSharedPreferences(applicationContext).edit().putString(KEY_COLLECTION_NAME, collectionName.get()).apply()
            }
        })
    }

    fun performMutationsOnSingleConnection(n: Int) {
        val collectionName = "___stress_test_02"

        // init log
        log = Log(activity, "txt")
        log?.addLine("$n mutations on a single connection")
        log?.addLine("==============================================")
        log?.addLine("API key: ${Config.API_KEY}")
        log?.addLine("Collection name: $collectionName")
        log?.addLine("==============================================")

        total = n
        success = 0
        error = 0
        times.clear()
        testStarted = System.currentTimeMillis()

        updateConsole()

        if(rapid) {
            Rapid.initialize(Config.API_KEY)
            connectStarted = 0L
            Rapid.getInstance(Config.API_KEY).addConnectionStateListener { state ->
                when (state) {
                    ConnectionState.CONNECTING -> connectStarted = System.currentTimeMillis()
                    ConnectionState.CONNECTED -> connectionTime = System.currentTimeMillis() - connectStarted
                }
                updateConsole()
            }
        }

        Thread({
            (1..total).forEach {
                if(rapid) mutationToRapid(collectionName, it)
                else mutationToFirebase(collectionName, it)
            }
        }).start()
    }

    fun mutationToRapid(collectionName: String, pos: Int) {
        val start = System.currentTimeMillis()
        val doc = Rapid.getInstance(Config.API_KEY).collection(collectionName, TestObject::class.java).newDocument()
        val docId = doc.id
        doc.mutate(TestObject.getRandom())
                .onSuccess {
                    val time = System.currentTimeMillis() - start
                    synchronized(times, { times.add(time) })
                    success++
                    log?.addLine("$pos. Mutation SUCCESSFUL (Document $docId). Took ${time}ms")
                    updateConsole()
                    if (error + success == total)
                        onDone()
                }
                .onError { e ->
                    val time = System.currentTimeMillis() - start
                    synchronized(times, { times.add(time) })
                    log?.addLine("$pos. Mutation ERROR (Document $docId). Took ${time}ms. Reason: ${e.message}")
                    error++
                    updateConsole()
                    if (error + success == total)
                        onDone()
                }
        if (delayEnabled)
            Thread.sleep(2000)
    }

    fun mutationToFirebase(path: String, pos: Int) {
        val start = System.currentTimeMillis()
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference(path)
        val doc = myRef.push()

        doc.setValue(TestObject.getRandom(), { e, ref ->
            if(e == null) {
                val time = System.currentTimeMillis() - start
                synchronized(times, { times.add(time) })
                success++
                log?.addLine("$pos. Mutation SUCCESSFUL (Document ${doc.key}). Took ${time}ms")
                updateConsole()
                if (error + success == total)
                    onDone()
            }
            else {
                val time = System.currentTimeMillis() - start
                synchronized(times, { times.add(time) })
                log?.addLine("$pos. Mutation ERROR (Document ${doc.key}). Took ${time}ms. Reason: ${e.message}")
                error++
                updateConsole()
                if (error + success == total)
                    onDone()
            }
        })
        if (delayEnabled)
            Thread.sleep(2000)
    }

    fun onDone() {
        log?.addLine("==============================================")
        log?.addLine("DONE")
        log?.addLine("==============================================")
        log?.addLine(console.get())
        log?.share()
    }

    fun updateConsole() {
        synchronized(times, {
            console.set("${success + error}/$total done ($error errors).\n" +
                    "Average time: ${Math.round(times.average())}ms (Min: ${times.min()}ms, Max: ${times.max()}ms)\n" +
                    "Connection init took ${connectionTime}ms\n" +
                    "Whole time: ${System.currentTimeMillis() - testStarted}ms" )
        })
    }

    fun viewCollection() {
        activity.startActivity(ViewerActivity.getIntent(context, collectionName.get()))
    }

    fun loadFromCsv() {
        requestFile({ inputStream ->
            val items = CSVUtility.loadCsv(inputStream)
            val total = items.size
            var error = 0
            var success = 0
            csvImportProgress.set(0)
            showToast("Starting CSV import. Importing $total items.")
            items.forEach { item ->
                Rapid.getInstance(Config.API_KEY).collection(collectionName.get(), Map::class.java).newDocument().mutate(item).onSuccess {
                    android.util.Log.d("CSV mutate", "Success")
                    success++
                    csvImportProgress.set(100 * (error + success) / total)
                }.onError {
                    android.util.Log.e("CSV mutate", "Error")
                    error++
                    csvImportProgress.set(100 * (error + success) / total)
                }.onCompleted {
                    if (error + success == total)
                        showToast("CSV Import done. $error errors.")
                }
            }
        })
    }


    private fun requestFile(callback: (InputStream) -> Unit) {
        filePickerCallback = callback
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        view.startActivityForResult(intent, RC_FILE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_FILE) {
            if (resultCode == Activity.RESULT_OK) {
                val returnUri = data!!.data
                try {
                    val inputPFD = context.contentResolver.openFileDescriptor(returnUri, "r")
                    val fd: FileDescriptor = inputPFD.fileDescriptor
                    filePickerCallback?.invoke(FileInputStream(fd))
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                    return
                }
            }
        }
    }

    fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

}
