package client.ui

import client.model.CommunicationManager
import common.Message
import common.chinesecheckers.ChineseCheckerServerMessage
import common.chinesecheckers.ChineseCheckersClientMessage
import javafx.event.EventHandler
import javafx.scene.control.Alert
import tornadofx.Controller
import tornadofx.runLater
import java.lang.Exception
import java.lang.Thread.sleep
import java.net.ConnectException

class WelcomeViewController : Controller() {
    private val view: AppWelcomeView by inject()
    private val client: CommunicationManager = CommunicationManager()
    fun connectButtonClickHandler() {
        client.addObserverFunction(this::connectionEstablishedHandler)
        try {
            client.launch(view.serverIPTextField.text)
            primaryStage.onCloseRequest = EventHandler { client.close() }
            sleep(1000)
            client.sendMessageToServer(ChineseCheckerServerMessage.ConnectionRequest("1"))
        } catch (e: Exception) {
            val errorWindow = Alert(Alert.AlertType.ERROR)
            e.printStackTrace()
            errorWindow.headerText = null
            errorWindow.contentText = "Connection error.\nCheck if server ip is correct.\n" + e.message
            //errorWindow.initOwner(view.currentWindow)
            //errorWindow.dialogPane.children[1].style { fontSize = 12.px }
            //errorWindow.dialogPane.children[2].style { maxWidth = 45.px ;fontSize = 12.px }
            errorWindow.showAndWait()
        }

        //for testing
        //find<MenuViewController>().initCommunicationManager(client)
        //view.replaceWith<AppMenuView>()
        //---
    }

    fun connectionEstablishedHandler(message: Message) {
        println("connectionEstablishedHandler called")
        if (message is ChineseCheckersClientMessage.ConnectionEstablished)
            runLater {
                find<MenuViewController>().initCommunicationManager(client,message.player)
                client.removeObserverFunction(this::connectionEstablishedHandler)
                view.replaceWith<AppMenuView>()
            }
    }
}