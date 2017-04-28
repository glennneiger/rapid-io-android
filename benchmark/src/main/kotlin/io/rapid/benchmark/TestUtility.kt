package io.rapid.benchmark


import java.util.*
import kotlin.collections.ArrayList


object TestUtility {
    private val sRandom = Random()


    val randomString: String
        get() = "dasfjhgasdfkjhasgdkjhsadgf"


    val randomInt: Int
        get() = sRandom.nextInt()


    val randomDouble: Double
        get() = sRandom.nextDouble()


    val randomStringList: List<String>
        get() {
            val list = ArrayList<String>()
            (0..10).forEach {
                list.add(randomString)
            }
            return list
        }
}
