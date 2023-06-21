package com.javarush.island.malacion.tasks;

import com.javarush.island.malacion.entities.Animal;
import com.javarush.island.malacion.entities.Entity;
import com.javarush.island.malacion.service.Manager;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class EatTask implements Runnable {
    private Animal animal;
    private Manager manager;

    public EatTask(Animal animal, Manager manager) {
        this.animal = animal;
        this.manager = manager;
    }

    @Override
    public void run() {
        animal.setHungry(true);
        try {
            Yaml yaml = new Yaml();
            FileInputStream inputStream = new FileInputStream("config.yaml");
            Map<String, Object> data = yaml.load(inputStream);
            Map<String, Map<String, Integer>> foodChance = (Map<String, Map<String, Integer>>) data.get("foodChance");
            Map<String, Integer> animals = foodChance.get(animal.getClass().getSimpleName());

            if (animals != null && !animals.isEmpty()) {

                List<String> availablePreys = getAvailablePreys(animals);

                if (!availablePreys.isEmpty()) {

                    String preyToEat = getRandomPrey(availablePreys);
                    if (preyToEat != null) {
                        Class<?> preyClass = Class.forName("com.javarush.island.malacion.entities." + preyToEat);
                        Set<Entity> entitySet = animal.getCurrentLocation().getLocation().get(preyClass);
                        Optional<Entity> randomEntity = entitySet.stream().findAny();
                        animal.eat(randomEntity.get());
                    }
                }
            }
        } catch (ClassNotFoundException | FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private List<String> getAvailablePreys(Map<String, Integer> animals) {
        List<String> availablePreys = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : animals.entrySet()) {
            String prey = entry.getKey();
            int chance = entry.getValue();
            if (chance >= ThreadLocalRandom.current().nextInt(100)) {
                availablePreys.add(prey);
            }
        }
        return availablePreys;
    }

    private String getRandomPrey(List<String> availablePreys) {
        if (!availablePreys.isEmpty()) {
            int randomIndex = ThreadLocalRandom.current().nextInt(availablePreys.size());
            return availablePreys.get(randomIndex);
        }
        return null;
    }
}