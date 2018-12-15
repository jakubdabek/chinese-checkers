package client.ui

import client.model.CommunicationManager
import common.Message
import common.chinesecheckers.ChineseCheckersClientMessage
import tornadofx.Controller
import tornadofx.runLater

class WelcomeViewController : Controller() {
    private val view: AppWelcomeView by inject()
    private val client: CommunicationManager = CommunicationManager()
    fun connectButtonClickHandler() {
        //client.launch("localhost")
        //for testing
        find<MenuViewController>().initCommunicationManager(client)
        view.replaceWith<AppMenuView>()
        //---
    }

    fun connectionEstablishedHandler(message: Message) {
        if (message is ChineseCheckersClientMessage.ConnectionEstablished)
            runLater {
                find<MenuViewController>().initCommunicationManager(client)
                client.removeObserverFunction(this::connectionEstablishedHandler)
                view.replaceWith<AppMenuView>()
            }
    }
}