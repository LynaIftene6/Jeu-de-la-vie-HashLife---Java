package algorithms;

import java.util.Random;

import core.QuadTree;

/**
 * Benchmark minimal pour comparer :
 * - l'évolution naïve
 * - l'évolution via HashLife
 *
 * Avec l'API actuelle, HashLife est benchmarké
 * sur son macro-step naturel uniquement.
 */
public final class BenchmarkTest {

    /** Nombre de runs de chauffe. */
    private static final int WARMUP_RUNS = 2;

    /** Nombre de runs mesurés. */
    private static final int MEASURE_RUNS = 5;

    private BenchmarkTest() {
        // Classe utilitaire.
    }

    /**
     * Point d'entrée principal.
     */
    public static void main(String[] args) {
        runBenchmarks();
    }

    /**
     * Lance quelques benchmarks essentiels.
     */
    private static void runBenchmarks() {
        benchmark(
            "RANDOM 16",
            centerInLargerGrid(generateRandomGrid(16, 0.15, 42L), 64)
        );
        benchmark(
            "RANDOM 32",
            centerInLargerGrid(generateRandomGrid(32, 0.15, 42L), 128)
        );
        benchmark(
            "RANDOM 128",
            centerInLargerGrid(generateRandomGrid(128, 0.15, 42L), 512)
        );
        benchmark(
            "BLOCKS 32",
            centerInLargerGrid(generateRepeatingBlocksGrid(32), 128)
        );
    }
    
    /**
     * Compare le temps moyen du naïf et de HashLife
     * sur le macro-step naturel de la grille.
     *
     * Le benchmark exige que les deux résultats soient identiques.
     * Si ce n'est pas le cas, on arrête immédiatement avec une erreur.
     */
    private static void benchmark(String label, int[][] initialGrid) {
        AlgorithmChecks.requireSquareGrid(initialGrid, "initialGrid");

        QuadTree baseTree = new QuadTree(HashLife.copyGrid(initialGrid));
        int generations = HashLife.getMacroStepFor(baseTree.getRacine());

        warmup(initialGrid, generations);

        long naiveTime = measureAverageTimeNs(() -> executeNaive(initialGrid, generations));
        long hashLifeTime = measureAverageTimeNs(() -> executeHashLife(initialGrid, generations));

        QuadTree naiveResult = executeNaive(initialGrid, generations);
        QuadTree hashLifeResult = executeHashLife(initialGrid, generations);

        if (!sameTree(naiveResult, hashLifeResult)) {
            throw new IllegalStateException(
                label
                    + " -> resultats differents entre naif et HashLife "
                    + "pour " + generations + " generations"
            );
        }

        System.out.println("==================================================");
        System.out.println(label);
        System.out.println("Taille      : " + initialGrid.length + "x" + initialGrid.length);
        System.out.println("Generations : " + generations + " (macro-step naturel)");
        System.out.println("Naif        : " + naiveTime + " ns");
        System.out.println("HashLife    : " + hashLifeTime + " ns");
        System.out.println("Identiques  : true");
        System.out.println();
    }

    /**
     * Exécute quelques runs non mesurés pour stabiliser un peu la JVM.
     */
    private static void warmup(int[][] initialGrid, int generations) {
        for (int i = 0; i < WARMUP_RUNS; i++) {
            executeNaive(initialGrid, generations);
            executeHashLife(initialGrid, generations);
        }
    }

    /**
     * Mesure un temps moyen sur plusieurs runs.
     */
    private static long measureAverageTimeNs(Runnable runnable) {
        long total = 0L;

        for (int i = 0; i < MEASURE_RUNS; i++) {
            long start = System.nanoTime();
            runnable.run();
            long end = System.nanoTime();
            total += (end - start);
        }

        return total / MEASURE_RUNS;
    }

    /**
     * Exécute la version naïve.
     */
    private static QuadTree executeNaive(int[][] initialGrid, int generations) {
        CellularAutomaton automaton = new CellularAutomaton(
            new QuadTree(HashLife.copyGrid(initialGrid))
        );
        automaton.run_naive(generations);
        return automaton.getCurrent();
    }

    /**
     * Exécute la version HashLife.
     */
    private static QuadTree executeHashLife(int[][] initialGrid, int generations) {
        CellularAutomaton automaton = new CellularAutomaton(
            new QuadTree(HashLife.copyGrid(initialGrid))
        );
        automaton.run_hashlife(generations);
        return automaton.getCurrent();
    }

    /**
     * Compare deux arbres cellule par cellule.
     */
    private static boolean sameTree(QuadTree a, QuadTree b) {
        if (a.getTaille() != b.getTaille()) {
            return false;
        }

        int size = a.getTaille();
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                if (a.getCell(x, y) != b.getCell(x, y)) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Place un motif au centre d'une grille plus grande remplie de zéros.
     *
     * @param pattern motif à recopier
     * @param targetSize taille de la nouvelle grille
     * @return nouvelle grille contenant le motif centré
     */
    private static int[][] centerInLargerGrid(int[][] pattern, int targetSize) {
        AlgorithmChecks.requireSquareGrid(pattern, "pattern");
        AlgorithmChecks.requirePowerOfTwo(targetSize, "targetSize");

        if (targetSize < pattern.length) {
            throw new IllegalArgumentException(
                "targetSize doit etre >= a la taille du motif"
            );
        }

        int[][] result = new int[targetSize][targetSize];
        int offset = (targetSize - pattern.length) / 2;

        for (int y = 0; y < pattern.length; y++) {
            for (int x = 0; x < pattern[y].length; x++) {
                result[offset + y][offset + x] = pattern[y][x];
            }
        }

        return result;
    }

    /**
     * Génère une grille aléatoire carrée.
     */
    private static int[][] generateRandomGrid(int size, double density, long seed) {
        AlgorithmChecks.requirePositive(size, "size");
        AlgorithmChecks.requireBetweenZeroAndOne(density, "density");

        Random random = new Random(seed);
        int[][] grid = new int[size][size];

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                grid[y][x] = random.nextDouble() < density ? 1 : 0;
            }
        }

        return grid;
    }

    /**
     * Génère une grille structurée de blocs 2x2.
     */
    private static int[][] generateRepeatingBlocksGrid(int size) {
        AlgorithmChecks.requirePositive(size, "size");

        int[][] grid = new int[size][size];

        for (int y = 0; y < size; y += 4) {
            for (int x = 0; x < size; x += 4) {
                if (y + 1 < size && x + 1 < size) {
                    grid[y][x] = 1;
                    grid[y][x + 1] = 1;
                    grid[y + 1][x] = 1;
                    grid[y + 1][x + 1] = 1;
                }
            }
        }

        return grid;
    }
}