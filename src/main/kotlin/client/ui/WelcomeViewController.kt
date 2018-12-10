package client.ui

import tornadofx.Controller

class WelcomeViewController : Controller() {
    private val view: AppWelcomeView by inject()

    fun connectButtonClickHandler() {
        view.replaceWith<AppMenuView>()
    }
}