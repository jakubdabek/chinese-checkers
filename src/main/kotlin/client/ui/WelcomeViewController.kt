package client.ui

import client.model.CommunicationManager
import common.Message
import common.chinesecheckers.ChineseCheckerServerMessage
import common.chinesecheckers.ChineseCheckersClientMessage
import tornadofx.Controller
import tornadofx.runLater
import java.lang.Thread.sleep

class WelcomeViewController : Controller() {
    private val view: AppWelcomeView by inject()
    private val client: CommunicationManager = CommunicationManager()
    fun connectButtonClickHandler() {
        client.addObserverFunction(this::connectionEstablishedHandler)
        client.launch("localhost")
        sleep(1000)
        client.sendMessageToServer(ChineseCheckerServerMessage.ConnectionRequest("1"))

        //for testing
        //find<MenuViewController>().initCommunicationManager(client)
        //view.replaceWith<AppMenuView>()
        //---
    }

    fun connectionEstablishedHandler(message: Message) {
        println("connectionEstablishedHandler called")
        if (message is ChineseCheckersClientMessage.ConnectionEstablished)
            runLater {
                find<MenuViewController>().initCommunicationManager(client)
                client.removeObserverFunction(this::connectionEstablishedHandler)
                view.replaceWith<AppMenuView>()
            }
    }
}