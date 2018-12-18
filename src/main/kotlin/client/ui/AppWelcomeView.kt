package client.ui

import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.TextField
import javafx.scene.image.Image
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import tornadofx.*

class AppWelcomeView : View("Chinese checkers") {
    companion object {
        const val DEFAULT_HEIGHT = 620.0
        const val DEFAULT_WIDTH = 800.0
        const val MIN_HEIGHT = 620.0
        const val MIN_WIDTH = 800.0
    }

    private val welcomeViewController: WelcomeViewController by inject()
    private lateinit var rootVBox: VBox
    private lateinit var menuVBox: VBox
    internal lateinit var serverIPTextField: TextField
    private lateinit var applyIPButton: Button
    override val root = with(this) {
        primaryStage.height = DEFAULT_HEIGHT
        primaryStage.width = DEFAULT_WIDTH
        primaryStage.minHeight = MIN_HEIGHT
        primaryStage.minWidth = MIN_WIDTH
        primaryStage.icons.add(Image("/icon2.png"))
        return@with vbox {
            rootVBox = this
            vbox {
                addClass(Styles.mainVBox)
                menuVBox = this
                alignment = Pos.TOP_CENTER
                vboxConstraints {
                    spacing = 20.0
                    vGrow = Priority.ALWAYS
                }
                imageview("chinesecheckers.png") {
                    fitToParentSize()
                    scaleY = 0.70
                    scaleX = 0.70
                }
                fieldset {
                    alignment = Pos.TOP_CENTER
                    spacing = 20.0
                    text("Enter server ip:") {
                        addClass(Styles.label)
                    }
                    textfield {
                        isFillWidth = false
                        prefWidthProperty().bind(menuVBox.widthProperty() / 3)
                        text = "localhost"
                        serverIPTextField = this
                    }
                }
                button {
                    applyIPButton = this
                    text = "CONNECT"
                    action(welcomeViewController::connectButtonClickHandler)
                    style(append = true) {
                        shortcut("Enter")
                    }
                }
            }
        }
    }
}