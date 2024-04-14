package ru.netology;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

    public static final Map<Integer, Integer> sizeToFreq = new HashMap<>();

    public static final int countRepeat = 1000;

    public static final String defaultMask = "RLRFR";

    public static final int defaultContSymbolsInMask = 100;

    public static void main(String[] args) {

        ExecutorService threadPool = Executors.newFixedThreadPool(countRepeat);

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
                }
            });
        }

        threadPool.shutdown();

        int maxFrequency = Collections.max(sizeToFreq.values());
        int mostUsed = 0;
        for (int i : sizeToFreq.keySet()) {
            if (sizeToFreq.get(i) == maxFrequency) {
                mostUsed = i;
            }
        }

        System.out.println("Самое частое количество повторений " + mostUsed + " (встретилось " + maxFrequency + " раз)");
        int finalMostUsed = mostUsed;
        sizeToFreq.forEach((frequency, count) -> {
            if (frequency != finalMostUsed) {
                System.out.println("- " + frequency + " (" + count + " раз)");
            }
        });
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