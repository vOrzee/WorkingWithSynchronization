package ru.netology;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

    public static final Map<Integer, Integer> sizeToFreq = new HashMap<>();

    public static final int countRepeat = 1000;

    public static final String defaultMask = "RLRFR";

    public static final int defaultContSymbolsInMask = 100;

    public static void main(String[] args) {

        ExecutorService threadPool = Executors.newFixedThreadPool(countRepeat);

        Thread threadObserver = getThreadObserver();

        Pair<Integer, Integer> finalLeader;

        for (int i = 0; i < countRepeat; i++) {
            threadPool.submit(() -> {
                String generatedString = generateRoute(defaultMask, defaultContSymbolsInMask);
                int countSearchedCharacters = 0;
                char searchingCharacter = 'R';
                for (int j = 0; j < generatedString.length(); j++) {
                    if (generatedString.charAt(j) == searchingCharacter) {
                        countSearchedCharacters++;
                    }
                }
                synchronized (sizeToFreq) {
                    sizeToFreq.put(countSearchedCharacters, sizeToFreq.getOrDefault(countSearchedCharacters, 0) + 1);
                    sizeToFreq.notify();
                }
            });
        }

        threadPool.shutdown();
        threadObserver.interrupt();

        finalLeader = maxInMap(sizeToFreq);
        System.out.println("Самое частое количество повторений " + finalLeader.getLeft() + " (встретилось " + finalLeader.getRight() + " раз)");
        sizeToFreq.forEach((frequency, count) -> {
            if (frequency != finalLeader.getLeft().intValue()) {
                System.out.println("- " + frequency + " (" + count + " раз)");
            }
        });
    }

    private static Thread getThreadObserver() {
        Thread threadObserver = new Thread(() -> {
            Pair<Integer, Integer> currentLeader = new Pair<>(0, 0);
            Pair<Integer, Integer> newValue;
            while (!Thread.interrupted()) {
                synchronized (sizeToFreq) {
                    try {
                        sizeToFreq.wait();
                    } catch (InterruptedException e) {
                        break;
                    }
                    newValue = maxInMap(sizeToFreq);
                }
                if (!Objects.equals(currentLeader.getLeft(), newValue.getLeft()) || !Objects.equals(currentLeader.getRight(), newValue.getRight())) {
                    currentLeader.setLeft(newValue.getLeft());
                    currentLeader.setRight(newValue.getRight());
                    System.out.println("Текущий лидер: " + currentLeader.getLeft() + "\nКоличество повторений: " + currentLeader.getRight() + "\n");
                }
            }
        });

        threadObserver.start();
        return threadObserver;
    }

    public static Pair<Integer, Integer> maxInMap(Map<Integer, Integer> map) {
        Pair<Integer, Integer> result = new Pair<>(0, 0);
        result.setRight(Collections.max(map.values())); // maxFrequency
        for (int i : map.keySet()) {
            if (Objects.equals(map.get(i), result.getRight())) {
                result.setLeft(i); // mostUsed
            }
        }
        return result;
    }

    public static String generateRoute(String letters, int length) {
        Random random = new Random();
        StringBuilder route = new StringBuilder();
        for (int i = 0; i < length; i++) {
            route.append(letters.charAt(random.nextInt(letters.length())));
        }
        return route.toString();
    }
}