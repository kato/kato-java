package me.danwi.kato.example.argument

data class TestEntity(val id: Int?, val name: String?)
data class TestEntity2(val id: Int?, val unknown: String?)
data class TestEntity3(val id: Int?, val obj: TestEntity?)

data class TestEntityAll(
    val id: Int? = null,
    val obj: TestEntity2? = null,
    val unknown: String? = null,
    val name: String? = null
)