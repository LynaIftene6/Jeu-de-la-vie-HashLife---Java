package algorithms;

import core.Node;
import core.QuadTree;

/**
 * Implémentation naïve minimale du Jeu de la Vie.
 */
public final class Evolution {

    private Evolution() {
        // Classe utilitaire.
    }

    /**
     * Calcule naïvement la génération suivante.
     *
     * @param tree arbre courant
     * @return arbre suivant
     */
    public static QuadTree nextGeneration(QuadTree tree) {
        AlgorithmChecks.requireTree(tree);

        int[][] currentGrid = HashLife.nodeToGrid(tree.getRacine());
        int[][] nextGrid = nextGrid(currentGrid);

        return new QuadTree(nextGrid);
    }

    /**
     * Fait évoluer naïvement un nœud sur plusieurs générations.
     *
     * @param node nœud de départ
     * @param steps nombre de générations
     * @return nœud final reconstruit
     */
    public static Node evolveDense(Node node, int steps) {
        AlgorithmChecks.requireNode(node);
        AlgorithmChecks.requireNonNegative(steps, "steps");

        int[][] current = HashLife.copyGrid(HashLife.nodeToGrid(node));

        for (int i = 0; i < steps; i++) {
            current = nextGrid(current);
        }

        return new QuadTree(current).getRacine();
    }

    /**
     * Calcule la génération suivante d'une grille dense avec la règle de Conway.
     *
     * @param grid grille courante
     * @return grille suivante
     */
    private static int[][] nextGrid(int[][] grid) {
        AlgorithmChecks.requireSquareGrid(grid, "grid");

        int size = grid.length;
        int[][] next = new int[size][size];

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                int neighbors = countNeighbors(grid, x, y);
                next[y][x] = neighbors == 3 || (grid[y][x] == 1 && neighbors == 2) ? 1 : 0;
            }
        }

        return next;
    }

    /**
     * Compte les voisins vivants d'une cellule.
     *
     * @param grid grille étudiée
     * @param x abscisse
     * @param y ordonnée
     * @return nombre de voisins vivants
     */
    private static int countNeighbors(int[][] grid, int x, int y) {
        int total = 0;

        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                if (dx == 0 && dy == 0) {
                    continue;
                }

                int nx = x + dx;
                int ny = y + dy;

                if (nx >= 0 && ny >= 0 && ny < grid.length && nx < grid[0].length) {
                    total += grid[ny][nx];
                }
            }
        }

        return total;
    }

    /**
     * Si une cellule vivante touche le bord, on agrandit la grille
     * avant de calculer la génération suivante.
     *
     * @param tree arbre courant
     * @return arbre suivant
     */
    public static QuadTree prepareAndNextGeneration(QuadTree tree) {
        AlgorithmChecks.requireTree(tree);

        if (hasLiveCellOnBorder(tree)) {
            tree.expand();
        }

        return nextGeneration(tree);
    }

    /**
     * Vérifie si une cellule vivante est présente sur le bord.
     *
     * @param tree arbre testé
     * @return true si le bord contient une cellule vivante
     */
    private static boolean hasLiveCellOnBorder(QuadTree tree) {
        int size = tree.getTaille();
        int last = size - 1;

        for (int x = 0; x < size; x++) {
            if (tree.getCell(x, 0) == 1 || tree.getCell(x, last) == 1) {
                return true;
            }
        }

        for (int y = 0; y < size; y++) {
            if (tree.getCell(0, y) == 1 || tree.getCell(last, y) == 1) {
                return true;
            }
        }

        return false;
    }
}