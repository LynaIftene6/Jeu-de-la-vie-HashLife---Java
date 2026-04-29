package algorithms;

import core.Node;
import core.QuadTree;

/**
 * Représente une simulation d'automate cellulaire basée sur un QuadTree.
 *
 * Cette classe encapsule l'état courant et propose :
 * - exécution naïve (dont pas à pas)
 * - exécution HashLife
 */
public final class CellularAutomaton {

    /** État courant de la simulation. */
    private QuadTree current;

    public CellularAutomaton(QuadTree initial) {
        this.current = AlgorithmChecks.requireTree(initial);
    }

    public QuadTree getCurrent() {
        return current;
    }

    public void setCurrent(QuadTree newCurrent) {
        this.current = AlgorithmChecks.requireTree(newCurrent);
    }

    /**
     * =========================
     * NAIF
     * =========================
     */

    /**
     * Avance d'une génération avec l'algorithme naïf.
     */
    public void step() {
        current = Evolution.nextGeneration(current);
    }

    /**
     * Exécution naïve sur plusieurs étapes.
     */
    public void run_naive(int steps) {
        AlgorithmChecks.requireNonNegative(steps, "steps");

        for (int i = 0; i < steps; i++) {
            current = Evolution.nextGeneration(current);
        }
    }

    /**
     * =========================
     * HASHLIFE
     * =========================
     */

    /**
     * Exécution HashLife directe.
     */
    public void run_hashlife(int steps) {
        AlgorithmChecks.requireNonNegative(steps, "steps");

        int macro = HashLife.getMacroStepFor(current.getRacine());

        if (steps != macro) {
            throw new IllegalArgumentException(
                "steps doit être égal au macro-step (" + macro + ")"
            );
        }

        Node result = HashLife.evolve(current.getRacine(), steps);
        current.setRacine(result);
    }

    /**
     * =========================
     * CACHE
     * =========================
     */

    public void resetCaches() {
        HashLife.resetCache();
    }
}