package server

const val DEFAULTPORT = 8888

fun main(args: Array<String>) {
    val communicationManager = CommunicationManager()
    communicationManager.launch(DEFAULTPORT)
}