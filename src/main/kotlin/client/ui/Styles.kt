package client.ui

import tornadofx.*
import java.awt.Font

class Styles : Stylesheet() {
    companion object {
        val button by cssclass()
        val label by cssclass()
        val label15 by cssclass()
        val mainVBox by cssclass()
        val gamePanel by cssclass()
        val checkbox by cssclass()
        val unselectedField by cssclass()
        val selectedField by cssclass()
        val colorPicker by cssclass()
        val highlightedField by cssclass()
        val chosenAsDestination by cssclass()
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
            backgroundColor += c("lightgray")
        }
        gamePanel {
            backgroundColor += c("lightgray")
        }
        checkbox {
            fontSize = 15.px
            backgroundRadius = multi(box(15.px))
            backgroundColor += c("red")
            and(selected) {
                backgroundColor += c("green")
            }
        }
        label15 {
            fontSize = 15.px
            fontFamily = Font.SANS_SERIF
        }
        colorPicker {
            backgroundColor += c("transparent")
            fontSize = 10.px
            colorLabelVisible = false
            arrowsVisible = false
        }
        selectedField {
            strokeWidth = 5.px
            stroke = c("black")
        }
        unselectedField {
            fill = c("603BB7")
        }
        highlightedField {
            fill = c("603BB7")
            strokeWidth = 5.px
            stroke = c("white")
        }
        chosenAsDestination {
            strokeWidth = 5.px
            stroke = c("yellow")
        }
    }
}