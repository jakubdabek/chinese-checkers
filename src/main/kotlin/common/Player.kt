package common

import java.io.Serializable


class Player(id: Int, var nickname: String) : Serializable {
    data class Id(val value: Int) : Serializable
    val id = Id(id)
    override fun equals(other: Any?) = super.equals(other) || (other is Player && other.id == id)
    override fun hashCode() = id.hashCode()
}
