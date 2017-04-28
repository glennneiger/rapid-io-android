package io.rapid.benchmark


data class TestObject(
        var alpha: String? = null,
        var beta: Int? = null,
        var gamma: Double? = null,
        var delta: InnerObject? = null
){
    companion object{
        fun getRandom() = TestObject(TestUtility.randomString, TestUtility.randomInt, TestUtility.randomDouble, InnerObject.getRandom())
    }
}


data class InnerObject(
        var field1: String? = null,
        var field2: Double? = null,
        var field3: List<String>? = null
){
    companion object{
        fun getRandom() = InnerObject(TestUtility.randomString, TestUtility.randomDouble, TestUtility.randomStringList)
    }
}

