package com.javarush.island.malacion;

import com.javarush.island.malacion.app.Game;
import com.javarush.island.malacion.controller.Controller;
import com.javarush.island.malacion.service.Manager;
import com.javarush.island.malacion.view.ConsoleView;
import com.javarush.island.malacion.view.View;


public class EntryPoint {
    public static void main(String[] args){

        Island island = new Island(10, 5);
        Manager manager = new Manager(island);
        View view = new ConsoleView();
        Controller controller = new Controller(view);
        Game game = new Game(controller);
        game.start(manager);
    }
}