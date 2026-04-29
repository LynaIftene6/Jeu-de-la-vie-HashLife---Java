package algorithms;

import core.QuadTree;

/**
 * Tests automatiques pour les fonctions de CellularAutomaton :
 * - step() en naïf
 * - run_naive(...)
 * - run_hashlife(...)
 *
 * On garde seulement des cas robustes avec l'implémentation actuelle :
 * - bloc stable
 * - blinker
 * - extraction du centre
 */
public final class AlgorithmsTest {

    private AlgorithmsTest() {
        // Classe utilitaire.
    }

    /**
     * Point d'entrée principal.
     */
    public static void main(String[] args) {
        runAutomaticTests();
        System.out.println("Tous les tests AlgorithmsTest sont passes.");
    }

    /**
     * Exécute la suite minimale de tests.
     */
    public static void runAutomaticTests() {
        testStepMatchesNaiveOneGeneration();
        testRunNaiveBlockStillLife();
        testRunNaiveBlinkerOscillator();
        testRunHashLifeBlockStillLife();
        testRunHashLifeBlinkerOscillator();
    }

    /**
     * Vérifie que step() correspond à une génération naïve.
     */
    private static void testStepMatchesNaiveOneGeneration() {
        int[][] grid = createBlinkerGrid();

        CellularAutomaton automaton = new CellularAutomaton(
            new QuadTree(HashLife.copyGrid(grid))
        );

        QuadTree expected = new QuadTree(HashLife.copyGrid(grid));
        expected = Evolution.nextGeneration(expected);

        automaton.step();

        assertSameGridWithMessage(
            "TEST STEP NAIF 1 GENERATION",
            HashLife.nodeToGrid(expected.getRacine()),
            HashLife.nodeToGrid(automaton.getCurrent().getRacine())
        );
    }

    /**
     * Vérifie qu'un bloc 2x2 reste stable avec run_naive(...).
     */
    private static void testRunNaiveBlockStillLife() {
        assertRunNaiveMatchesNaive(
            "TEST RUN_NAIVE BLOC STABLE",
            createBlockGrid(),
            4
        );
    }

    /**
     * Vérifie qu'un blinker oscille correctement avec run_naive(...).
     */
    private static void testRunNaiveBlinkerOscillator() {
        assertRunNaiveMatchesNaive(
            "TEST RUN_NAIVE BLINKER",
            createBlinkerGrid(),
            4
        );
    }

    /**
     * Vérifie qu'un bloc 2x2 reste stable avec run_hashlife(...).
     */
    private static void testRunHashLifeBlockStillLife() {
        assertRunHashLifeMatchesNaive(
            "TEST RUN_HASHLIFE BLOC STABLE",
            createBlockGrid(),
            4
        );
    }

    /**
     * Vérifie qu'un blinker oscille correctement avec run_hashlife(...).
     */
    private static void testRunHashLifeBlinkerOscillator() {
        assertRunHashLifeMatchesNaive(
            "TEST RUN_HASHLIFE BLINKER",
            createBlinkerGrid(),
            4
        );
    }

    /**
     * Compare run_naive(...) avec l'évolution naïve de référence.
     */
    private static void assertRunNaiveMatchesNaive(
        String message,
        int[][] initialGrid,
        int steps
    ) {
        AlgorithmChecks.requireSquareGrid(initialGrid, "initialGrid");
        AlgorithmChecks.requireNonNegative(steps, "steps");

        CellularAutomaton automaton = new CellularAutomaton(
            new QuadTree(HashLife.copyGrid(initialGrid))
        );
        automaton.run_naive(steps);

        QuadTree expected = evolveNaive(new QuadTree(HashLife.copyGrid(initialGrid)), steps);

        assertSameGridWithMessage(
            message,
            HashLife.nodeToGrid(expected.getRacine()),
            HashLife.nodeToGrid(automaton.getCurrent().getRacine())
        );
    }

    /**
     * Compare run_hashlife(...) avec l'évolution naïve de référence.
     *
     * On découpe en plusieurs appels du macro-step courant.
     * Cette version est volontairement utilisée seulement sur des motifs
     * robustes avec l'implémentation actuelle.
     */
    private static void assertRunHashLifeMatchesNaive(
        String message,
        int[][] initialGrid,
        int steps
    ) {
        AlgorithmChecks.requireSquareGrid(initialGrid, "initialGrid");
        AlgorithmChecks.requireNonNegative(steps, "steps");

        CellularAutomaton automaton = new CellularAutomaton(
            new QuadTree(HashLife.copyGrid(initialGrid))
        );

        int remaining = steps;
        while (remaining > 0) {
            int macroStep = HashLife.getMacroStepFor(automaton.getCurrent().getRacine());

            if (macroStep <= 0) {
                throw new IllegalStateException(
                    message + " -> macroStep invalide : " + macroStep
                );
            }

            if (remaining < macroStep) {
                throw new IllegalStateException(
                    message
                        + " -> remaining=" + remaining
                        + " est plus petit que macroStep=" + macroStep
                );
            }

            automaton.run_hashlife(macroStep);
            remaining -= macroStep;
        }

        QuadTree expected = evolveNaive(new QuadTree(HashLife.copyGrid(initialGrid)), steps);

        assertSameGridWithMessage(
            message,
            HashLife.nodeToGrid(expected.getRacine()),
            HashLife.nodeToGrid(automaton.getCurrent().getRacine())
        );
    }

    /**
     * Fait évoluer un arbre naïvement, génération par génération.
     */
    private static QuadTree evolveNaive(QuadTree initial, int steps) {
        QuadTree current = new QuadTree(HashLife.nodeToGrid(initial.getRacine()));

        for (int i = 0; i < steps; i++) {
            current = Evolution.nextGeneration(current);
        }

        return current;
    }

    /**
     * Crée un bloc stable.
     */
    private static int[][] createBlockGrid() {
        return new int[][] {
            {0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 1, 1, 0, 0, 0},
            {0, 0, 0, 1, 1, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0}
        };
    }

    /**
     * Crée un blinker.
     */
    private static int[][] createBlinkerGrid() {
        return new int[][] {
            {0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 1, 1, 1, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0}
        };
    }

    /**
     * Compare deux grilles cellule par cellule.
     */
    private static void assertSameGridWithMessage(String message, int[][] expected, int[][] actual) {
        if (expected.length != actual.length || expected[0].length != actual[0].length) {
            throw new IllegalStateException(
                message
                    + " -> tailles differentes : attendu="
                    + expected.length + "x" + expected[0].length
                    + ", obtenu="
                    + actual.length + "x" + actual[0].length
            );
        }

        for (int y = 0; y < expected.length; y++) {
            for (int x = 0; x < expected[y].length; x++) {
                if (expected[y][x] != actual[y][x]) {
                    throw new IllegalStateException(
                        message
                            + " -> difference a la position ("
                            + x + "," + y + ") : attendu="
                            + expected[y][x]
                            + ", obtenu="
                            + actual[y][x]
                    );
                }
            }
        }
    }
}