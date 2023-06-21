package com.javarush.island.malacion.interfaces;

import com.javarush.island.malacion.entities.Entity;

import java.io.FileNotFoundException;

public interface EatAbility {
    void eat(Entity entity) throws FileNotFoundException;
}
