package honey.mechanics;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/*
 * GameRandom.java *
 - Holds the master seed for a run and derives per-level generation randomness from it
 */
public final class GameRandom {

    private static long masterSeed;

    private GameRandom() {
    }

    public static void newSeed() {
        seed(ThreadLocalRandom.current().nextLong());
    }

    public static void seed(long newSeed) {
        masterSeed = newSeed;
        System.out.println("HoneySuckle: master seed = " + masterSeed);
    }

    public static long seed() {
        return masterSeed;
    }

    // Pure function of (masterSeed, level): always returns a Random that will produce the same
    // future output for the same inputs, so a level's generation never depends on save/load timing.
    public static Random forLevel(int level) {
        return new Random(mix(masterSeed, level));
    }

    // SplitMix64 finalizer, avoids the correlation java.util.Random shows between adjacent seeds
    private static long mix(long seed, int level) {
        long x = seed ^ (level * 0x9E3779B97F4A7C15L);
        x = (x ^ (x >>> 30)) * 0xBF58476D1CE4E5B9L;
        x = (x ^ (x >>> 27)) * 0x94D049BB133111EBL;
        return x ^ (x >>> 31);
    }
}
