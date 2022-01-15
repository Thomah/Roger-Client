package fr.thomah.roger.common;

public class Randomizer {

    public static int generateNumberBetween(int min, int max) {
        return min + (int) (Math.random() * ((max - min) + 1));
    }

}
