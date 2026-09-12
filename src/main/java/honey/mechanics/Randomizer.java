package honey.mechanics;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public final class Randomizer {

    private static long masterSeed;

    public static void newSeed() {
        seed(ThreadLocalRandom.current().nextLong());
    }

    public static void seed(long newSeed) {
        masterSeed = newSeed;
    }

    public static long seed() {
        return masterSeed;
    }

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
