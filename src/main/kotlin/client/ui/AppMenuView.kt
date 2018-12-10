package client.ui

import javafx.geometry.Pos
import javafx.scene.image.ImageView
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import tornadofx.*

class AppMenuView : View("Chinese checkers") {
    companion object {
        const val DEFAULT_HEIGHT = 600.0
        const val DEFAULT_WIDTH = 800.0
        const val MIN_HEIGHT = 600.0
        const val MIN_WIDTH = 800.0
    }
    private val menuViewController: MenuViewController by inject()
    private lateinit var rootVBox: VBox
    lateinit var menuVBox: VBox
    lateinit var imageLogo: ImageView
    override val root = with(this) {
        primaryStage.height = DEFAULT_HEIGHT
        primaryStage.width = DEFAULT_WIDTH
        primaryStage.minHeight = MIN_HEIGHT
        primaryStage.minWidth = MIN_WIDTH
        return@with vbox {
            rootVBox = this
            vbox {
                menuVBox = this
                alignment = Pos.TOP_CENTER
                vboxConstraints {
                    spacing = 20.0
                    vGrow = Priority.ALWAYS
                }
                style {
                    backgroundColor += c("orange")
                }
                imageview("chinesecheckers.png") {
                    fitToParentSize()
                    scaleY = 0.70
                    scaleX = 0.70
                    imageLogo = this
                }
                fieldset {
                    alignment = Pos.TOP_CENTER
                    spacing = 20.0
                    button("Play with computer") {
                        addClass(Styles.button)
                        action(menuViewController::playWithComputerClickHandler)
                    }
                    button("Play with human players") {
                        addClass(Styles.button)
                        action(menuViewController::playWithHumanPlayersClickHandler)
                    }

                }

            }
        }
    }
}