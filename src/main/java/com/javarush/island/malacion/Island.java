package com.javarush.island.malacion;

import com.javarush.island.malacion.entities.*;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.ReentrantLock;

public class Island {
    private int row;
    private int col;
    private ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, Location>> island;

    public Island(int row, int col) {
        this.row = row;
        this.col = col;
        island = new ConcurrentHashMap<>();
        for (int i = 0; i < row; i++) {
            island.put(i, new ConcurrentHashMap<>());
            for (int j = 0; j < col; j++) {
                island.get(i).put(j, new Location(i, j));
            }
        }
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                Location currentLocation = island.get(i).get(j);
                addNeighborsToLocation(currentLocation, i, j);
            }
        }
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, Location>> getIsland() {
        return island;
    }

    private void addNeighborsToLocation(Location location, int rowIndex, int colIndex) {
        try {
            if (colIndex > 0) {
                Location leftNeighbor = island.get(rowIndex).get(colIndex - 1);
                location.addNeighbor(leftNeighbor);
            }

            if (rowIndex > 0) {
                Location topNeighbor = island.get(rowIndex - 1).get(colIndex);
                location.addNeighbor(topNeighbor);
            }

            if (colIndex < col - 1) {
                Location rightNeighbor = island.get(rowIndex).get(colIndex + 1);
                location.addNeighbor(rightNeighbor);
            }

            if (rowIndex < row - 1) {
                Location bottomNeighbor = island.get(rowIndex + 1).get(colIndex);
                location.addNeighbor(bottomNeighbor);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Неверные индексы локации: [" + rowIndex + ", " + colIndex + "]", e);
        }
    }

    public class Location {
        private int rowIndex;
        private int colIndex;
        private final ReentrantLock lock = new ReentrantLock();
        private CopyOnWriteArrayList<Location> neighborhoods = new CopyOnWriteArrayList<>();
        private ConcurrentHashMap<Class<? extends Entity>, Set<Entity>> location;

        public Location(int rowIndex, int colIndex) {
            this.rowIndex = rowIndex;
            this.colIndex = colIndex;
            location = new ConcurrentHashMap<>();
            Set<Class<? extends Entity>> entities = new HashSet<>(Arrays.asList(
                    Wolf.class, Python.class, Fox.class, Bear.class, Eagle.class, Horse.class,
                    Deer.class, Rabbit.class, Mouse.class, Goat.class, Sheep.class, Boar.class,
                    Buffalo.class, Duck.class, Caterpillar.class, Plants.class)
            );
            for (Class<? extends Entity> entityClass : entities) {
                location.putIfAbsent(entityClass, ConcurrentHashMap.newKeySet());
            }

            try {
                Yaml yaml = new Yaml();
                FileInputStream inputStream = new FileInputStream("config.yaml");
                Map<String, Object> data = yaml.load(inputStream);
                Map<String, Map<String, Object>> limits = (Map<String, Map<String, Object>>) data.get("limits");

                for (Class<? extends Entity> entityClass : entities) {
                    Map<String, Object> entityData = limits.get(entityClass.getSimpleName());
                    if (entityData == null) {
                        continue;
                    }

                    int maxAmount = ((Number) entityData.get("maxAmount")).intValue();
                    int randomMax = ThreadLocalRandom.current().nextInt(maxAmount);

                    for (int i = 0; i < randomMax; i++) {
                        Constructor<? extends Entity> constructor = entityClass.getDeclaredConstructor();
                        Entity entity = constructor.newInstance();
                        entity.setCurrentLocation(this);
                        entity.setMaxAmount(maxAmount);
                        entity.setWeight((double) entityData.get("weight"));
                        if (entity instanceof Animal) {
                            Animal animal = (Animal) entity;
                            int moveSpeed = ((Number) entityData.get("moveSpeed")).intValue();
                            double satiationFoodAmount = (double) entityData.get("satiationFoodAmount");
                            animal.setMoveSpeed(moveSpeed);
                            animal.setSatiationFoodAmount(satiationFoodAmount);
                            addToLocation(entityClass, entity);
                        } else if (entity instanceof Plants) {
                            addToLocation(entityClass, entity);
                        }
                    }
                }

            } catch (FileNotFoundException e) {
                throw new RuntimeException("Файл конфигурации не найден", e);
            } catch (IOException e) {
                throw new RuntimeException("Ошибка при чтении файла конфигурации", e);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Ошибка при создании экземпляра сущности", e);
            }
        }

        public void acquireLock() {
            lock.lock();
        }

        public void releaseLock() {
            lock.unlock();
        }

        public List<Location> getNeighbors() {
            return neighborhoods;
        }

        public void addNeighbor(Location neighbor) {
            neighborhoods.add(neighbor);
        }

        public Map<Class<? extends Entity>, Set<Entity>> getLocation() {
            return location;
        }

        private void addToLocation(Class<? extends Entity> entityClass, Entity entity) {
            location.computeIfAbsent(entityClass, key -> ConcurrentHashMap.newKeySet()).add(entity);
        }

        public String getPosition() {
            return "[" + rowIndex + ", " + colIndex + "]";
        }
    }
}

