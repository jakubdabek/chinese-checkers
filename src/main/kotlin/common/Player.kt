package common

class Player(id: Int, var nickname: String) {
    data class Id(val value: Int)
    val id = Id(id)
    override fun equals(other: Any?) = super.equals(other) || (other is Player && other.id == id)
    override fun hashCode() = id.hashCode()
}
