package common

import java.io.Serializable


interface Message : Serializable {
    val content: Any?
}
