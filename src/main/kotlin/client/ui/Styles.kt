package client.ui

import tornadofx.*
import java.awt.Font

class Styles : Stylesheet() {
    companion object {
        val button by cssclass()
        val label by cssclass()
        val mainVBox by cssclass()
    }
    init {
        button {
            prefWidth = 300.px
            fontSize = 23.px
            textFill = c("white")
            backgroundColor += c("darkred")
            and(hover) {
                backgroundColor += c("black")
                textFill = c("pink")
            }
        }
        label {
            fontSize = 23.px
            fontFamily = Font.SANS_SERIF
        }
        mainVBox {
            backgroundColor += c("orange")
        }
    }
}