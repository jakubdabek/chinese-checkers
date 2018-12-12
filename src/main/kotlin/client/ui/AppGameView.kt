package client.ui

import javafx.beans.property.SimpleObjectProperty
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.layout.HBox
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import tornadofx.*
import java.awt.event.MouseEvent

class AppGameView : View() {
    private lateinit var controller: GameViewController
    private lateinit var footer: HBox
    //private var chosenColor = Color.DARKSLATEGREY
    val chosenColorProperty:SimpleObjectProperty<Color> = SimpleObjectProperty(Color.DARKSLATEGREY)
    val chosenColor by chosenColorProperty
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
            center = vbox {

                alignment = Pos.CENTER
                style { backgroundColor += c("darkgray") }
                stackpane {
                    prefHeightProperty().bind(primaryStage.heightProperty())
                    prefWidthProperty().bind(primaryStage.widthProperty())
                    alignment = Pos.CENTER
                    val cp = colorpicker {
                        chosenColorProperty.bind(valueProperty())
                        addClass(Styles.colorPicker)
                    }
                    circle(0.0, 0.0, 120.0) {
                        onMouseClicked = EventHandler { cp.show() }
                        fillProperty().bind(cp.valueProperty())
                    }
                    text("Choose your color") { addClass(Styles.label) }
                }
                hbox {
                    alignment = Pos.CENTER_RIGHT
                    paddingRightProperty.bind(this@hbox.widthProperty() / 8)
                    paddingBottomProperty.bind(this@vbox.heightProperty() / 8)
                    text("READY") { addClass(Styles.label15) }
                    togglebutton {
                        isSelected = false
                        addClass(Styles.checkbox)
                        prefWidth = 30.0
                        prefHeight = 30.0
                    }
                }
            }
            bottom {
                hbox {
                    footer = this
                    pane {
                        fitToParentWidth()
                        prefHeight = 35.0
                        style {
                            backgroundColor += c("black")
                        }
                    }
                }
            }
        }
    }
    fun startGame() {
        root.center = controller.getBoard()
    }
}