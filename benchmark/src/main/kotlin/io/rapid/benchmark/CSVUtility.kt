package io.rapid.benchmark

import com.opencsv.CSVReader
import java.io.InputStream
import java.io.InputStreamReader

object CSVUtility {
    fun loadCsv(inputStream: InputStream): List<Map<String, Any>> {
        val reader = CSVReader(InputStreamReader(inputStream))
        val entries = reader.readAll()
        val header = entries[0]
        val result = ArrayList<Map<String, Any>>()
        (1..entries.size - 1).forEach {
            val entry = entries[it]

            val item = HashMap<String, Any>()
            (0..header.size - 1).forEach {
                item.put(header[it], entry[it])
            }
            result.add(item)
        }
        return result
    }
}

