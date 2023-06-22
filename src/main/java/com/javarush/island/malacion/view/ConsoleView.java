package com.javarush.island.malacion.view;

import com.javarush.island.malacion.enums.Mode;
import com.javarush.island.malacion.exception.InvalidCoordinatesException;
import com.javarush.island.malacion.service.Manager;

import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ConsoleView implements View {

    @Override
    public void initialize(Manager manager) {
        ScheduledExecutorService counterExecutor = Executors.newSingleThreadScheduledExecutor();
        Scanner scanner = new Scanner(System.in);
        System.out.println("Выберите режим: " +
                System.lineSeparator() + "1 - Вывод общего количества животных и растений" +
                System.lineSeparator() + "2 - Вывод количества каждого животного и растений" +
                System.lineSeparator() + "3 - Вывод общего количества животных и растений на определённой локации" +
                System.lineSeparator() + "4 - Вывод количества каждого животного и растений на определённой локации"
        );

        int mode = scanner.nextInt();
        switch (mode) {
            case 1:
                counterExecutor.scheduleAtFixedRate(() -> {
                    manager.runSimulation(Mode.DEFAULT.name());
                }, 0, 3, TimeUnit.SECONDS);
                //printLoop(manager);
                break;
            case 2:
                counterExecutor.scheduleAtFixedRate(() -> {
                    manager.runSimulation(Mode.DETAILED_DEFAULT.name());
                }, 0, 3, TimeUnit.SECONDS);
                break;
            case 3:
                System.out.println("Введите координату X: ");
                int posX = scanner.nextInt();
                System.out.println("Введите координату Y: ");
                int posY = scanner.nextInt();
                if(posX  >= manager.getIsland().getRow() || posY >= manager.getIsland().getCol())
                {
                    throw new InvalidCoordinatesException();
                }
                counterExecutor.scheduleAtFixedRate(() -> {
                    manager.runSimulation(Mode.LOCATION.name(), posX, posY);
                }, 0, 3, TimeUnit.SECONDS);
                break;
            case 4:
                System.out.println("Введите координату X: ");
                posX = scanner.nextInt();
                System.out.println("Введите координату Y: ");
                posY = scanner.nextInt();
                if(posX  >= manager.getIsland().getRow() || posY >= manager.getIsland().getCol())
                {
                    throw new InvalidCoordinatesException();
                }
                counterExecutor.scheduleAtFixedRate(() -> {
                    manager.runSimulation(Mode.DETAILED_LOCATION.name(), posX, posY);
                }, 0, 3, TimeUnit.SECONDS);
                break;
        }


    }
}
