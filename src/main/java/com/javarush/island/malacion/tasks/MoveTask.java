package com.javarush.island.malacion.tasks;

import com.javarush.island.malacion.entities.Animal;
import com.javarush.island.malacion.service.Manager;

import java.io.FileNotFoundException;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class MoveTask implements Runnable {
    private Animal animal;
    private Map<String, Map<String, Object>> limits;
    private Manager manager;

    public MoveTask(Animal animal, Map<String, Map<String, Object>> limits, Manager manager) {
        this.animal = animal;
        this.limits = limits;
        this.manager = manager;
    }

    @Override
    public void run() {
        int moveSpeed;
        moveSpeed = ThreadLocalRandom.current().nextInt((int) limits.get(animal.getClass().getSimpleName()).get("moveSpeed") + 1);

        try {
            if (moveSpeed > 0) {
                animal.move(moveSpeed);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }
}
