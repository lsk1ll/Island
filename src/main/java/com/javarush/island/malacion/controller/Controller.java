package com.javarush.island.malacion.controller;

import com.javarush.island.malacion.view.View;

public class Controller {
    View view;
    public Controller(View view)
    {
        this.view = view;
    }
    public View getView(){
        return this.view;
    }
}
