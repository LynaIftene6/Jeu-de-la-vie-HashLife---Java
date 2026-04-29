package ui;

import algorithms.CellularAutomaton;
import algorithms.HashLife;
import core.QuadTree;
import java.util.Random;

/**
 * Benchmark minimal pour comparer :
 * - l'évolution naïve
 * - l'évolution via HashLife
 *
 * <p>Avec l'API actuelle, HashLife est benchmarké
 * sur son macro-step naturel uniquement.
 *
 * <p>Version allégée de {@code algorithms.BenchmarkTest} pour l'interface utilisateur :
 * moins de runs, pas de cas 128→512, retourne un {@code String} au lieu d'afficher en console.
 */
public class BenchmarkTestUI {

    /** Nombre de runs de chauffe. */
    private static final int WARMUP_RUNS = 1;

    /** Nombre de runs mesurés. */
    private static final int MEASURE_RUNS = 3;

    /**
     * Classe utilitaire — instanciation interdite.
     */
    private BenchmarkTestUI() {}

    /**
     * Lance quelques benchmarks essentiels et retourne les résultats formatés.
     *
     * @return une chaîne contenant les résultats de chaque benchmark
     */
    public static String run() {
        StringBuilder sb = new StringBuilder();
        runBenchmark(sb, "RANDOM 16 CENTERED", generateRandomGrid(16, 0.15, 42L), 64);
        runBenchmark(sb, "RANDOM 32 CENTERED", generateRandomGrid(32, 0.15, 42L), 128);
        runBenchmark(sb, "BLOCKS 32 CENTERED", generateRepeatingBlocksGrid(32), 128);
        return sb.toString();
    }

    /**
     * Compare le temps moyen du naïf et de HashLife
     * sur le macro-step naturel de la grille.
     *
     * <p>Le benchmark exige que les deux résultats soient identiques.
     * Si ce n'est pas le cas, on arrête immédiatement avec une erreur.
     *
     * @param sb       le {@code StringBuilder} dans lequel écrire les résultats
     * @param label    le nom affiché pour ce benchmark
     * @param pattern  la grille de départ (avant centrage)
     * @param gridSize la taille de la grille finale (après centrage)
     * @throws IllegalStateException si naïf et HashLife produisent des résultats différents
     */
    private static void runBenchmark(StringBuilder sb, String label, int[][] pattern, int gridSize) {
        int[][] centered = centerInLargerGrid(pattern, gridSize);

        QuadTree baseTree = new QuadTree(HashLife.copyGrid(centered));
        int generations = HashLife.getMacroStepFor(baseTree.getRacine());

        warmup(centered, generations);

        long naiveTime    = measureAverageTimeNs(() -> executeNaive(centered, generations));
        long hashLifeTime = measureAverageTimeNs(() -> executeHashLife(centered, generations));

        QuadTree naiveResult    = executeNaive(centered, generations);
        QuadTree hashLifeResult = executeHashLife(centered, generations);

        if (!sameTree(naiveResult, hashLifeResult)) {
            throw new IllegalStateException(
                label
                    + " -> resultats differents entre naif et HashLife "
                    + "pour " + generations + " generations"
            );
        }

        sb.append("====================================\n");
        sb.append(label).append("\n");
        sb.append("Taille      : ").append(gridSize).append("x").append(gridSize).append("\n");
        sb.append("Generations : ").append(generations).append(" (macro-step naturel)\n");
        sb.append("Naif        : ").append(naiveTime).append(" ns\n");
        sb.append("HashLife    : ").append(hashLifeTime).append(" ns\n");
        sb.append("Speedup     : x")
                .append(String.format("%.2f", (double) naiveTime / Math.max(hashLifeTime, 1)))
                .append("\n");
        sb.append("Identiques  : true\n\n");
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