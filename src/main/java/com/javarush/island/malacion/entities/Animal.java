package com.javarush.island.malacion.entities;

import com.javarush.island.malacion.Island;
import com.javarush.island.malacion.interfaces.EatAbility;
import com.javarush.island.malacion.interfaces.Movable;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public abstract class Animal extends Entity implements EatAbility, Movable {
    private int moveSpeed;
    private double satiationFoodAmount;
    private boolean isHungry = true;


    public int getMoveSpeed() {
        return moveSpeed;
    }

    public double getSatiationFoodAmount() {
        return satiationFoodAmount;
    }

    public void setMoveSpeed(int moveSpeed) {
        this.moveSpeed = moveSpeed;
    }

    public void setSatiationFoodAmount(double satiationFoodAmount) {
        this.satiationFoodAmount = satiationFoodAmount;
    }

    @Override
    public void eat(Entity entity) throws FileNotFoundException {
        getCurrentLocation().acquireLock();
        try {
            getCurrentLocation().getLocation().get(entity.getClass()).remove(entity);
            setHungry(false);
        } finally {
            getCurrentLocation().releaseLock();
        }
    }

    @Override
    public void move(int steps) throws FileNotFoundException {
        Island.Location temp = getCurrentLocation();
        Island.Location location = findLocation(steps);
        Island.Location first = (System.identityHashCode(location) < System.identityHashCode(temp)) ? location : temp;
        Island.Location second = (first == location) ? temp : location;
        Yaml yaml = new Yaml();
        FileInputStream inputStream = new FileInputStream("config.yaml");
        Map<String, Object> data = yaml.load(inputStream);
        Map<String, Map<String, Object>> limits = (Map<String, Map<String, Object>>) data.get("limits");
        int maxAmount = (int) limits.get(this.getClass().getSimpleName()).get("maxAmount");

        List<Island.Location> locations = Arrays.asList(first, second);
        Collections.sort(locations, Comparator.comparingInt(System::identityHashCode));

        locations.get(0).acquireLock();
        try {
            locations.get(1).acquireLock();
            if (getCurrentLocation().getLocation().get(this.getClass()).size() < maxAmount) {
                getCurrentLocation().getLocation().get(this.getClass()).remove(this);
                setCurrentLocation(location);
                location.getLocation().get(this.getClass()).add(this);
            }
        } finally {
            locations.get(1).releaseLock();
            locations.get(0).releaseLock();
        }
    }
    public Island.Location findLocation(int steps)
    {
        Island.Location initialLocation = getCurrentLocation();
        Island.Location newLocation = getCurrentLocation();
        Island.Location temp = null;
        for (int i = 0; i < steps; i++) {
            newLocation = newLocation.getNeighbors().get(ThreadLocalRandom.current().nextInt(newLocation.getNeighbors().size()));
            if (!newLocation.equals(initialLocation)) {
                temp = newLocation;
            }
        }
        return temp;
    }


    public void death() {
        getCurrentLocation().acquireLock();
        try {
            if (isHungry()) {
                Island.Location location = getCurrentLocation();
                location.getLocation().get(this.getClass()).remove(this);
            }
        } finally {
            getCurrentLocation().releaseLock();
        }
    }

    public void setHungry(boolean isHungry) {
        this.isHungry = isHungry;
    }

    public boolean isHungry() {
        return this.isHungry;
    }
}
