package client.ui

import client.model.CommunicationManager
import client.model.MenuScope
import common.Message
import common.OnErrorBehaviour
import common.chinesecheckers.ChineseCheckersClientMessage
import javafx.scene.control.Alert
import tornadofx.*
import kotlin.Exception

class WelcomeViewController : Controller() {
    private val view: AppWelcomeView by inject()
    private var client: CommunicationManager? = null

    fun connectButtonClickHandler() {
        runAsync {
            client = CommunicationManager.launch(
                this@WelcomeViewController::connectionEstablishedHandler,
                this@WelcomeViewController::connectionErrorHandler,
                view.serverIPTextField.text,
                handshake = "chinese checkers"
            )
        }
    }

    private fun connectionErrorHandler(exception: Exception?, fatal: Boolean): OnErrorBehaviour {
        exception?.printStackTrace()
        runLater {
            val errorWindow = Alert(Alert.AlertType.ERROR)
            errorWindow.headerText = null
            errorWindow.contentText =
                    "Connection error.\n" +
                    "Check if server ip is correct.\n" +
                    (exception?.message ?: "")
            //errorWindow.initOwner(view.currentWindow)
            //errorWindow.dialogPane.children[1].style { fontSize = 12.px }
            //errorWindow.dialogPane.children[2].style { maxWidth = 45.px ;fontSize = 12.px }
            errorWindow.showAndWait()
        }
        return OnErrorBehaviour.DIE
    }

    private fun connectionEstablishedHandler(message: Message) {
        primaryStage.setOnCloseRequest { client!!.close() }
        println("connectionEstablishedHandler called")
        if (message is ChineseCheckersClientMessage.ConnectionEstablished) {
            runLater {
                val menuScope = MenuScope(client!!, message.player)
                client!!.clearHandlers()
                view.replaceWith(find<AppMenuView>(menuScope))
            }
        }
    }
}