package client.ui

import javafx.event.EventTarget
import javafx.geometry.Pos
import javafx.scene.image.ImageView
import javafx.scene.layout.HBox
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
                addClass(Styles.mainVBox)
                vboxConstraints {
                    spacing = 20.0
                    vGrow = Priority.ALWAYS
                }
                imageview("chinesecheckers.png") {
                    fitToParentSize()
                    scaleY = 0.70
                    scaleX = 0.70
                    imageLogo = this
                }
                hbox {
                    alignment = Pos.CENTER
                    spacing = 60.0
                    vbox {
                        alignment = Pos.CENTER
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
                    vbox {
                        alignment = Pos.TOP_CENTER
                        spacing = 10.0
                        prefWidthProperty().bind(this@hbox.widthProperty() / 6)

                        text("Number of players") { addClass(Styles.label15) }
                        hbox {
                            alignment = Pos.BASELINE_CENTER
                            spacing = 20.0
                            text("2") {addClass(Styles.label15)}
                            togglebutton {
                                addClass(Styles.checkbox)
                                prefWidth = 30.0
                                prefHeight = 30.0
                            }
                        }
                        createHBoxForCheckBox("3",this@vbox)
                        createHBoxForCheckBox("4",this@vbox)
                        createHBoxForCheckBox("6",this@vbox)
                    }
                }
            }
        }
    }
    private fun createHBoxForCheckBox(str: String,parent: EventTarget) : HBox {
         return opcr(parent,hbox {
            alignment = Pos.BASELINE_CENTER
            spacing = 20.0
            text(str){addClass(Styles.label15)}
            togglebutton {
                isSelected = false
                addClass(Styles.checkbox)
                prefWidth = 30.0
                prefHeight = 30.0
            }
        })
    }
}