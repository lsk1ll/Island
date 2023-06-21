package com.javarush.island.malacion.interfaces;

import java.io.FileNotFoundException;

public interface Movable {
    void move(int steps) throws FileNotFoundException;
}
