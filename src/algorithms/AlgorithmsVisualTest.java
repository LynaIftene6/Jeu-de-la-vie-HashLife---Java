package algorithms;

import core.QuadTree;

/**
 * Tests visuels pour comparer l'évolution naïve
 * et l'évolution HashLife sur quelques motifs classiques.
 *
 * Le but n'est pas d'automatiser une validation complète,
 * mais de pouvoir observer facilement des motifs connus :
 * - bloc stable
 * - blinker
 * - toad
 * - beacon
 * - glider
 *
 * Le naïf avance génération par génération.
 * HashLife avance par macro-step naturel.
 */
public final class AlgorithmsVisualTest {

    private AlgorithmsVisualTest() {
        // Classe utilitaire.
    }

    public static void main(String[] args) {
        runVisualSuite();
    }

    private static void runVisualSuite() {
        visualCompare("BLOCK (stable)", createBlockGrid(), 4);
        visualCompare("BLINKER (oscillateur periode 2)", createBlinkerGrid(), 4);
        visualCompare("TOAD (oscillateur periode 2)", createToadGrid(), 4);
        visualCompare("BEACON (oscillateur periode 2)", createBeaconGrid(), 4);
        visualCompare("GLIDER (translation diagonale)", createGliderGrid(), 4);
    }

    private static void visualCompare(String title, int[][] initialGrid, int totalNaiveSteps) {
        System.out.println("==================================================");
        System.out.println(title);
        System.out.println("==================================================");

        CellularAutomaton naiveAutomaton = new CellularAutomaton(
            new QuadTree(HashLife.copyGrid(initialGrid))
        );
        CellularAutomaton hashLifeAutomaton = new CellularAutomaton(
            new QuadTree(HashLife.copyGrid(initialGrid))
        );

        System.out.println("=== ETAT INITIAL ===");
        printGrid(initialGrid);

        int step = 0;
        while (step < totalNaiveSteps) {
            naiveAutomaton.step();
            step++;

            int macroStep = HashLife.getMacroStepFor(hashLifeAutomaton.getCurrent().getRacine());

            if (macroStep <= 0) {
                throw new IllegalStateException("macroStep invalide : " + macroStep);
            }

            if (step % macroStep == 0) {
                hashLifeAutomaton.run_hashlife(macroStep);

                int[][] naiveGrid = HashLife.nodeToGrid(naiveAutomaton.getCurrent().getRacine());
                int[][] hashLifeGrid = HashLife.nodeToGrid(hashLifeAutomaton.getCurrent().getRacine());

                System.out.println("=== STEP " + step + " ===");
                System.out.println("Naif :");
                printGrid(naiveGrid);

                System.out.println("HashLife :");
                printGrid(hashLifeGrid);

                System.out.println("Identiques : " + HashLife.sameGrid(naiveGrid, hashLifeGrid));
                System.out.println();
            }
        }
    }

    private static int[][] createEmptyGrid(int size) {
        return new int[size][size];
    }

    private static int[][] createBlockGrid() {
        int[][] grid = createEmptyGrid(16);
        grid[7][7] = 1;
        grid[7][8] = 1;
        grid[8][7] = 1;
        grid[8][8] = 1;
        return grid;
    }

    private static int[][] createBlinkerGrid() {
        int[][] grid = createEmptyGrid(16);
        grid[8][6] = 1;
        grid[8][7] = 1;
        grid[8][8] = 1;
        return grid;
    }

    private static int[][] createToadGrid() {
        int[][] grid = createEmptyGrid(16);
        grid[7][7] = 1;
        grid[7][8] = 1;
        grid[7][9] = 1;
        grid[8][6] = 1;
        grid[8][7] = 1;
        grid[8][8] = 1;
        return grid;
    }

    private static int[][] createBeaconGrid() {
        int[][] grid = createEmptyGrid(16);
        grid[6][6] = 1;
        grid[6][7] = 1;
        grid[7][6] = 1;
        grid[9][8] = 1;
        grid[8][9] = 1;
        grid[9][9] = 1;
        return grid;
    }

    private static int[][] createGliderGrid() {
        int[][] grid = createEmptyGrid(16);
        grid[5][6] = 1;
        grid[6][7] = 1;
        grid[7][5] = 1;
        grid[7][6] = 1;
        grid[7][7] = 1;
        return grid;
    }

    private static void printGrid(int[][] grid) {
        for (int y = 0; y < grid.length; y++) {
            for (int x = 0; x < grid[y].length; x++) {
                System.out.print(grid[y][x] == 1 ? "X " : ". ");
            }
            System.out.println();
        }
        System.out.println();
    }
}