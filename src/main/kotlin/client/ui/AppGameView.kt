package client.ui

import javafx.event.EventHandler
import javafx.event.EventTarget
import javafx.geometry.Pos
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import tornadofx.*
import java.lang.Math.*

class AppGameView : View() {
    private lateinit var controller: GameViewController
    private lateinit var footer: HBox
    //private var chosenColor = Color.DARKSLATEGREY

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
                style { backgroundColor += c("E8C8C1") }
                stackpane {
                    prefHeightProperty().bind(primaryStage.heightProperty())
                    prefWidthProperty().bind(primaryStage.widthProperty())
                    alignment = Pos.CENTER
//                    val cp = colorpicker {
//                        value = c("00E244")
//                        chosenColorProperty.bind(valueProperty())
//                        addClass(Styles.colorPicker)
//                    }
                    val cp = createColorPickerHBox(controller.availableColors,this@stackpane)
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
                    togglebutton {
                        isSelected = false
                        addClass(Styles.checkbox)
                        prefWidth = 30.0
                        prefHeight = 30.0
                        action({startGame()})
                    }
                }
            }
            bottom {
                hbox {
                    fitToParentWidth()
                    footer = this
                    pane {
                        prefWidthProperty().bind(primaryStage.widthProperty())
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
        val board= controller.getBoard()
//        board.prefHeightProperty().bind(primaryStage.heightProperty())
//        board.prefWidthProperty().bind(primaryStage.widthProperty())
        root.center = board
    }

    private fun createColorPickerHBox(colors: List<Color>,parent: EventTarget) : HBox {
        return opcr(parent, hbox {
            pane {
            prefWidthProperty().bind(this@hbox.widthProperty())
            paddingTop = 10.0
            var counter: Double = 0.0
            for (col in colors) {
                val c = circle(0.0 ,primaryStage.height/2 - 270 * sin(PI/5 * counter),40) {
                    fill = col
                    centerXProperty().bind(this@pane.widthProperty()/2.0 - colors.size/2.0*80+40 +counter * 80 - 50*cos(PI/5 * counter))
                    onMouseClicked = EventHandler {controller.chosenColorProperty.set(this.fill); this@hbox.visibleProperty().set(false)}
                }
                println(c)
                counter++
            }
            this@hbox.visibleProperty().set(false)
        }})
    }
}