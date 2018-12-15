package client.ui

import javafx.event.EventHandler
import javafx.event.EventTarget
import javafx.geometry.Pos
import javafx.scene.control.ToggleButton
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.scene.transform.Affine
import tornadofx.*


class AppGameView : View("Chinese checkers") {
    private lateinit var controller: GameViewController
    private lateinit var footer: HBox
    internal lateinit var readyButton: ToggleButton

    init {
        controller = find()
    }

    override fun onDock() {
        controller = find()
        //root.center = controller.getBoard()
    }


    override val root = with(this) {
        primaryStage.height = AppMenuView.DEFAULT_HEIGHT
        primaryStage.width = AppMenuView.DEFAULT_WIDTH
        primaryStage.minHeight = AppMenuView.MIN_HEIGHT
        primaryStage.minWidth = AppMenuView.MIN_WIDTH
        return@with borderpane {
            addClass(Styles.gamePanel)
            top {
                hbox {
                    style {
                        backgroundColor += c("black")
                    }
                    visibleProperty().set(false)
                    prefWidthProperty().bind(primaryStage.widthProperty())
                    button("END TURN") { }

                }
            }
            center = vbox {
                alignment = Pos.CENTER
                addClass(Styles.gamePanel)
                stackpane {
                    prefHeightProperty().bind(primaryStage.heightProperty())
                    prefWidthProperty().bind(primaryStage.widthProperty())
                    alignment = Pos.CENTER
//                    val cp = colorpicker {
//                        value = c("00E244")
//                        chosenColorProperty.bind(valueProperty())
//                        addClass(Styles.colorPicker)
//                    }
                    val cp = createColorPickerHBox(controller.availableColors, this@stackpane)
                    circle(0.0, 0.0, 120.0) {
                        onMouseClicked = EventHandler { cp.visibleProperty().set(true) }
                        fillProperty().bind(controller.chosenColorProperty)
                    }
                    text("Choose your color") { addClass(Styles.label) }
                }
                hbox {
                    alignment = Pos.CENTER_RIGHT
                    spacing = 10.0
                    paddingRightProperty.bind(this@vbox.widthProperty() / 8)
                    paddingBottomProperty.bind(this@vbox.heightProperty() / 8)
                    text("READY") { addClass(Styles.label15) }
                    readyButton = togglebutton {
                        isSelected = false
                        addClass(Styles.checkbox)
                        prefWidth = 30.0
                        prefHeight = 30.0
                        action(controller::performReadyClicked)
                    }
                }
            }
            bottom {
                hbox {
                    visibleProperty().set(false)
                    footer = this
                    pane {
                        prefWidthProperty().bind(primaryStage.widthProperty())
                        prefHeight = 25.0
                        style {
                            backgroundColor += c("black")
                        }
                    }
                }
            }
        }
    }

    private fun createColorPickerHBox(colors: List<Color>, parent: EventTarget): Pane {
        return opcr(parent,
            pane {
                paddingTop = 10.0
                for ((i, col) in colors.withIndex()) {
                    circle(radius = 40) {
                        fill = col
                        transforms.add(Affine().apply {
                            val oofset = 170.0
                            appendTranslation(0.0, -oofset)
                            appendRotation(360.0 / colors.size * i, 0.0, oofset)
                        })
                        layoutXProperty().bind(this@pane.widthProperty() / 2)
                        layoutYProperty().bind(this@pane.heightProperty() / 2)
                        onMouseClicked = EventHandler {
                            controller.chosenColorProperty.set(this.fill)
                            this@pane.isVisible = false
                        }
                    }
                }
                this@pane.isVisible = false
        })
    }
}
