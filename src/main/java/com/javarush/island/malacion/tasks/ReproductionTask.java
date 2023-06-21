package com.javarush.island.malacion.tasks;

import com.javarush.island.malacion.entities.Entity;
import com.javarush.island.malacion.service.Manager;

import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;

public class ReproductionTask implements Runnable {
    private Entity entity;
    private Manager manager;

    public ReproductionTask(Entity entity, Manager manager) {
        this.entity = entity;
        this.manager = manager;
    }

    @Override
    public void run() {
        try {
            entity.reproduction();
        } catch (FileNotFoundException | InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}