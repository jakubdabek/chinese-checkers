package client.ui

import client.model.CommunicationManager
import common.chinesecheckers.ChineseCheckersClientMessage
import tornadofx.Controller
import tornadofx.runLater

class WelcomeViewController : Controller() {
    private val view: AppWelcomeView by inject()
    private val manager: CommunicationManager = CommunicationManager()
    fun connectButtonClickHandler() {
        //manager.launch("localhost")
        //for testing
        find<MenuViewController>().initCommunicationManager(manager)
        view.replaceWith<AppMenuView>()
        //---
    }

    fun connectionEstablishedHandler(message: ChineseCheckersClientMessage) {
        if (message is ChineseCheckersClientMessage.ConnectionEstablished)
            runLater {
                find<MenuViewController>().initCommunicationManager(manager)
                view.replaceWith<AppMenuView>()
            }
    }
}