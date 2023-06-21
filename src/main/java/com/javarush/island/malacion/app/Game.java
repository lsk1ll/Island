package com.javarush.island.malacion.app;

import com.javarush.island.malacion.controller.Controller;
import com.javarush.island.malacion.service.Manager;

public class Game {
    private final Controller controller;
    public Game(Controller controller){
        this.controller = controller;
    }
    public void start(Manager manager) {
        controller.getView().initialize(manager);
    }
}
