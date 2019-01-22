package controllers;

import common.Player;
import play.*;
import play.mvc.*;

import views.html.*;

public class Application extends Controller {

    public Result index() {
        Player player = new Player(1, "1");
        return ok(index.render(String.format("Your new application is ready. %s", player.toString())));
    }

}
