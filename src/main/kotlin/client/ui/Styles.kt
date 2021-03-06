package client.ui

import tornadofx.*


class Styles : Stylesheet() {
    companion object {
        val gamePanelColor = c("#272727" )
        val lightenGamePanelColor = c("#5e5e5e")

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
        val gameButton by cssclass()
        val textfield by cssclass()
    }

    init {
        button {
            prefWidth = 300.px
            fontSize = 23.px
            textFill = c("white")
            backgroundColor += lightenGamePanelColor
            and(hover) {
                backgroundColor += c("#6e6e6e")
            }
        }
        label {
            fontSize = 23.px
            fontFamily = "SansSerif"
            fill = c("white")
        }
        mainVBox {
            backgroundColor += gamePanelColor
        }
        gamePanel {
            backgroundColor += gamePanelColor
        }
        checkbox {
            fontSize = 15.px
            backgroundRadius = multi(box(15.px))
            backgroundColor += c("darkred")
            and(selected) {
                backgroundColor += c("darkgreen")
            }
        }
        label15 {
            fontSize = 15.px
            fontFamily = "SansSerif"
            fill = c("white")
        }
        colorPicker {
            backgroundColor += c("transparent")
            fontSize = 10.px
            textFill = c("white")
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
        gameButton {
            prefWidth = 100.px
            fontSize = 15.px
            textFill = c("white")
            backgroundColor += c("#5e5e5e")
            and(hover) {
                backgroundColor += c("#7f7f7f")
            }
        }
        textfield {
            backgroundColor += lightenGamePanelColor
            textFill = c("white")
            fontSize = 15.px
            accentColor = gamePanelColor
        }
    }
}
