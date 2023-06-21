package com.javarush.island.malacion.entities;

import com.javarush.island.malacion.Island;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public abstract class Entity {
    private volatile Island.Location currentLocation;
    private int maxAmount;
    private double weight;
    private final ReentrantLock lock = new ReentrantLock();

    public double getWeight() {
        return this.weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public int getMaxAmount() {
        return this.maxAmount;
    }

    public void setMaxAmount(int maxAmount) {
        this.maxAmount = maxAmount;
    }

    public void setCurrentLocation(Island.Location location) {
        this.currentLocation = location;
    }

    public Island.Location getCurrentLocation() {
        return this.currentLocation;
    }

    public void reproduction() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, FileNotFoundException {
        try {
            getCurrentLocation().acquireLock();
            Yaml yaml = new Yaml();
            FileInputStream inputStream = new FileInputStream("config.yaml");
            Map<String, Object> data = yaml.load(inputStream);
            Map<String, Map<String, Object>> limits = (Map<String, Map<String, Object>>) data.get("limits");
            Class<? extends Entity> entityClass = this.getClass();
            Map<String, Object> entityData = limits.get(entityClass.getSimpleName());

            int maxAmount = ((Number) entityData.get("maxAmount")).intValue();
            Island.Location location = getCurrentLocation();
            List<Entity> animals = new ArrayList<>(location.getLocation().get(this.getClass()));

            if (animals.size() >= 2 && animals.size() < maxAmount) {
                Entity entity = entityClass.getDeclaredConstructor().newInstance();
                entity.setCurrentLocation(getCurrentLocation());
                entity.setMaxAmount(maxAmount);
                entity.setWeight((double) entityData.get("weight"));
                if (entity instanceof Animal) {
                    Animal animal = (Animal) entity;
                    int moveSpeed = ((Number) entityData.get("moveSpeed")).intValue();
                    double satiationFoodAmount = (double) entityData.get("satiationFoodAmount");
                    animal.setMoveSpeed(moveSpeed);
                    animal.setSatiationFoodAmount(satiationFoodAmount);
                    location.getLocation().get(this.getClass()).add(entity);
                } else if (entity instanceof Plants) {
                    location.getLocation().get(this.getClass()).add(entity);
                }
            }
        } finally {
            getCurrentLocation().releaseLock();
        }
    }
}
