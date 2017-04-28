package io.rapid.benchmark

import android.app.Activity
import android.content.Intent
import android.support.v4.app.ShareCompat
import android.support.v4.content.FileProvider
import java.io.File
import java.io.IOException
import java.io.OutputStreamWriter
import java.util.*


class Log(val activity: Activity, val extension: String) {
    var file: File? = null
    var writer: OutputStreamWriter? = null

    init {
        initFile()
    }

    private fun initFile() {
        File(activity.cacheDir, "outputs").mkdirs()
        file = File(activity.cacheDir, "outputs/output+${UUID.randomUUID()}.$extension")
        writer = OutputStreamWriter(file!!.outputStream())
    }

    fun addLine(line: String) {
        try {
            writer!!.write(line + "\n")
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun dump(): File {
        writer!!.close()
        val f = file!!
        initFile()
        return f
    }

    fun share() {
        val uri = FileProvider.getUriForFile(activity, "io.rapid.benchmarks.fileprovider", dump())

        val sendIntent = ShareCompat.IntentBuilder.from(activity)
                .setType("application/pdf")
                .setStream(uri)
                .setChooserTitle("Share output file")
                .createChooserIntent()
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        activity.startActivity(sendIntent)
    }
}