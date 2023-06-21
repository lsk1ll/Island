package com.javarush.island.malacion.tasks;

import com.javarush.island.malacion.entities.Animal;
import com.javarush.island.malacion.service.Manager;


public class DeathTask implements Runnable {
    private Animal animal;
    private Manager manager;


    public DeathTask(Animal animal, Manager manager) {
        this.animal = animal;
        this.manager = manager;
    }

    @Override
    public void run() {
        animal.death();
    }
}
