package com.javarush.island.malacion.service;

import com.javarush.island.malacion.Island;
import com.javarush.island.malacion.entities.Animal;
import com.javarush.island.malacion.entities.Entity;
import com.javarush.island.malacion.entities.Plants;
import com.javarush.island.malacion.tasks.DeathTask;
import com.javarush.island.malacion.tasks.EatTask;
import com.javarush.island.malacion.tasks.MoveTask;
import com.javarush.island.malacion.tasks.ReproductionTask;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

public class Manager {
    private Map<Class<Entity>, ExecutorService> executorMap;
    private Map<Class<? extends Entity>, Set<Entity>> entityMap;
    private Map<String, Map<String, Object>> limits;
    private Island island;
    private ReentrantLock lock;

    public Manager(Island island) {
        this.island = island;
        executorMap = new HashMap<>();
        entityMap = new HashMap<>();
        Yaml yaml = new Yaml();
        lock = new ReentrantLock();
        try {
            FileInputStream inputStream = new FileInputStream("config.yaml");
            Map<String, Object> data = yaml.load(inputStream);
            limits = (Map<String, Map<String, Object>>) data.get("limits");
            ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, Island.Location>> locations = island.getIsland();

            for (ConcurrentHashMap<Integer, Island.Location> row : locations.values()) {
                for (Island.Location location : row.values()) {
                    Set<Entity> entities = new HashSet<>();
                    Map<Class<? extends Entity>, Set<Entity>> locationEntities = location.getLocation();
                    for (Set<Entity> entitySet : locationEntities.values()) {
                        entities.addAll(entitySet);
                    }
                    addEntities(entities);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        initializeExecutors();
    }

    public void printStatistics() {
        int totalAnimals = 0;
        int totalPlants = 0;
        ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, Island.Location>> locations = island.getIsland();

        for (ConcurrentHashMap<Integer, Island.Location> row : locations.values()) {
            for (Island.Location location : row.values()) {
                for (Set<Entity> entities : location.getLocation().values()) {
                    for (Entity entity : entities) {
                        if (entity instanceof Animal) {
                            totalAnimals++;
                        } else if (entity instanceof Plants) {
                            totalPlants++;
                        }
                    }
                }
            }
        }

        System.out.println("Общее количество животных: " + totalAnimals);
        System.out.println("Общее количество растений: " + totalPlants);
    }
    public void printDetailedStatistics() throws FileNotFoundException {
        for (ConcurrentHashMap<Integer, Island.Location> row : island.getIsland().values()) {
            for (Island.Location location : row.values()) {
                System.out.println("Подробная статистика на локации " + location.getPosition() + ":");

                Map<Class<? extends Entity>, Integer> entityCountMap = new HashMap<>();
                int totalEntities = 0;

                for (Set<Entity> entities : location.getLocation().values()) {
                    for (Entity entity : entities) {
                        Class<? extends Entity> entityClass = entity.getClass();
                        entityCountMap.put(entityClass, entityCountMap.getOrDefault(entityClass, 0) + 1);
                        totalEntities++;
                    }
                }

                Yaml yaml = new Yaml();
                FileInputStream fileInputStream = new FileInputStream("config.yaml");
                Map<String, Object> data = yaml.load(fileInputStream);
                Map<String, Map<String, Character>> icons = (Map<String, Map<String, Character>>) data.get("icons");

                System.out.println("Статистика существ:");
                for (Map.Entry<Class<? extends Entity>, Integer> entry : entityCountMap.entrySet()) {
                    Class<? extends Entity> entityClass = entry.getKey();
                    int count = entry.getValue();
                    System.out.println("Существо: " + icons.get(entityClass.getSimpleName()) + ", Количество: " + count);
                }

                System.out.println("Общее количество существ на локации: " + totalEntities);
            }
        }
    }


    public void printStatisticsLocation(Island.Location location) {
        int totalAnimals = 0;
        int totalPlants = 0;

        for (Set<Entity> entities : location.getLocation().values()) {
            for (Entity entity : entities) {
                if (entity instanceof Animal) {
                    totalAnimals++;
                } else if (entity instanceof Plants) {
                    totalPlants++;
                }
            }
        }

        System.out.println("Количество животных на локации " + location.getPosition() + ": " + totalAnimals);
        System.out.println("Количество растений на локации " + location.getPosition() + ": " + totalPlants);
    }

    public void printDetailedStatisticsLocation(Island.Location location) throws FileNotFoundException {
        System.out.println("Подробная статистика на локации " + location.getPosition() + ":");

        Map<Class<? extends Entity>, Integer> entityCountMap = new HashMap<>();
        int totalEntities = 0;

        for (Set<Entity> entities : location.getLocation().values()) {
            for (Entity entity : entities) {
                Class<? extends Entity> entityClass = entity.getClass();
                entityCountMap.put(entityClass, entityCountMap.getOrDefault(entityClass, 0) + 1);
                totalEntities++;
            }
        }

        Yaml yaml = new Yaml();
        FileInputStream fileInputStream = new FileInputStream("config.yaml");
        Map<String, Object> data = yaml.load(fileInputStream);
        Map<String, Map<String, Character>> icons = (Map<String, Map<String, Character>>) data.get("icons");

        System.out.println("Статистика существ:");
        for (Map.Entry<Class<? extends Entity>, Integer> entry : entityCountMap.entrySet()) {
            Class<? extends Entity> entityClass = entry.getKey();
            int count = entry.getValue();
            System.out.println("Существо: " + icons.get(entityClass.getSimpleName()) + ", Количество: " + count);
        }

        System.out.println("Общее количество существ на локации: " + totalEntities);
    }

    public void addEntities(Set<Entity> entities) {
        for (Entity entity : entities) {
            Class<? extends Entity> entityClass = entity.getClass();
            Set<Entity> entityList = entityMap.computeIfAbsent(entityClass, k -> new HashSet<>());
            entityList.add(entity);
        }
    }

    private void initializeExecutors() {
        for (Map.Entry<Class<? extends Entity>, Set<Entity>> entry : entityMap.entrySet()) {
            Class<? extends Entity> entityClass = entry.getKey();
            if (Entity.class.isAssignableFrom(entityClass)) {
                ExecutorService executorService = Executors.newSingleThreadExecutor();
                executorMap.put((Class<Entity>) entityClass, executorService);
            }
        }
    }

    public void startMoveTasks() {

        for (Map.Entry<Class<Entity>, ExecutorService> entry : executorMap.entrySet()) {
            Class<Entity> entityClass = entry.getKey();
            ExecutorService entityExecutor = entry.getValue();
            Set<Entity> entityList = entityMap.get(entityClass);

            for (Entity entity : entityList) {
                if(entity instanceof Animal animal) {
                    Runnable moveTask = new MoveTask(animal, limits, this);
                    entityExecutor.submit(moveTask);
                }
            }
        }
    }

    public void startEatTasks() {
        for (Map.Entry<Class<Entity>, ExecutorService> entry : executorMap.entrySet()) {
            Class<Entity> entityClass = entry.getKey();
            ExecutorService entityExecutor = entry.getValue();
            Set<Entity> entityList = entityMap.get(entityClass);

            for (Entity entity : entityList) {
                if(entity instanceof Animal animal) {
                    Runnable eatTask = new EatTask(animal, this);
                    entityExecutor.submit(eatTask);
                }
            }
        }
    }

    public void startReproductionTasks() {
        for (Map.Entry<Class<Entity>, ExecutorService> entry : executorMap.entrySet()) {
            Class<Entity> entityClass = entry.getKey();
            ExecutorService entityExecutor = entry.getValue();
            Set<Entity> entityList = entityMap.get(entityClass);

            for (Entity entity : entityList) {
                Runnable reproductionTask = new ReproductionTask(entity, this);
                entityExecutor.submit(reproductionTask);
            }
        }
    }

    public void startDeathTasks() {
        for (Map.Entry<Class<Entity>, ExecutorService> entry : executorMap.entrySet()) {
            Class<Entity> entityClass = entry.getKey();
            ExecutorService entityExecutor = entry.getValue();
            Set<Entity> entityList = entityMap.get(entityClass);

            for (Entity entity : entityList) {
                if(entity instanceof Animal animal) {
                    Runnable deathTask = new DeathTask(animal, this);
                    entityExecutor.submit(deathTask);
                }
            }
        }
    }
    public void runSimulation(String s) {
        startReproductionTasks();
        startEatTasks();
        startDeathTasks();
        startMoveTasks();

        if (s.equals("DEFAULT")) {
            printStatistics();
        } else if (s.equals("DETAILED_DEFAULT")) {
            try {
                printDetailedStatistics();
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }


    public void runSimulation(String s, int posX, int posY) {
        startReproductionTasks();
        startEatTasks();
        startDeathTasks();
        startMoveTasks();

        ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, Island.Location>> location = island.getIsland();

        if (s.equals("LOCATION")) {
            printStatisticsLocation(location.get(posX).get(posY));
        } else if (s.equals("DETAILED_LOCATION")) {
            try {
                printDetailedStatisticsLocation(location.get(posX).get(posY));
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }



}
