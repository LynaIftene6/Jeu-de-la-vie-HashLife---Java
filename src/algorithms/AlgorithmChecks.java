package algorithms;

import core.Node;
import core.QuadTree;

/**
 * Vérifications minimales partagées par les algorithmes.
 *
 * Cette classe ne garde que les contrôles réellement utiles
 * au projet actuel.
 */
public final class AlgorithmChecks {

    private AlgorithmChecks() {
        // Classe utilitaire.
    }

    /**
     * Vérifie qu'une valeur n'est pas null.
     *
     * @param value valeur testée
     * @param name nom du paramètre
     * @return la valeur si valide
     */
    public static <T> T requireNonNull(T value, String name) {
        if (value == null) {
            throw new IllegalArgumentException(name + " ne doit pas etre null");
        }
        return value;
    }

    /**
     * Vérifie qu'un entier est >= 0.
     *
     * @param value valeur testée
     * @param name nom du paramètre
     * @return la valeur si valide
     */
    public static int requireNonNegative(int value, String name) {
        if (value < 0) {
            throw new IllegalArgumentException(name + " doit etre >= 0");
        }
        return value;
    }

    /**
     * Vérifie qu'un entier est > 0.
     *
     * @param value valeur testée
     * @param name nom du paramètre
     * @return la valeur si valide
     */
    public static int requirePositive(int value, String name) {
        if (value <= 0) {
            throw new IllegalArgumentException(name + " doit etre > 0");
        }
        return value;
    }

    /**
     * Vérifie qu'une densité est dans [0, 1].
     *
     * @param value valeur testée
     * @param name nom du paramètre
     * @return la valeur si valide
     */
    public static double requireBetweenZeroAndOne(double value, String name) {
        if (value < 0.0 || value > 1.0) {
            throw new IllegalArgumentException(name + " doit etre entre 0.0 et 1.0");
        }
        return value;
    }

    /**
     * Vérifie qu'un état de cellule vaut 0 ou 1.
     *
     * @param value état testé
     * @param name nom du paramètre
     * @return la valeur si valide
     */
    public static int requireBinaryCellState(int value, String name) {
        if (value != 0 && value != 1) {
            throw new IllegalArgumentException(name + " doit valoir 0 ou 1");
        }
        return value;
    }

    /**
     * Vérifie qu'un arbre n'est pas null.
     *
     * @param tree arbre testé
     * @return l'arbre si valide
     */
    public static QuadTree requireTree(QuadTree tree) {
        return requireNonNull(tree, "tree");
    }

    /**
     * Vérifie qu'un nœud n'est pas null.
     *
     * @param node nœud testé
     * @return le nœud si valide
     */
    public static Node requireNode(Node node) {
        return requireNonNull(node, "node");
    }

    /**
     * Vérifie qu'un nœud a au moins un certain niveau.
     *
     * @param node nœud testé
     * @param minLevel niveau minimal
     * @param name nom du paramètre
     * @return le nœud si valide
     */
    public static Node requireNodeLevelAtLeast(Node node, int minLevel, String name) {
        requireNonNull(node, name);
        if (node.getLevel() < minLevel) {
            throw new IllegalArgumentException(
                name + " exige un noeud de niveau >= " + minLevel
            );
        }
        return node;
    }

    /**
     * Vérifie qu'une grille est carrée et non vide.
     *
     * @param grid grille testée
     * @param name nom du paramètre
     * @return la grille si valide
     */
    public static int[][] requireSquareGrid(int[][] grid, String name) {
        requireNonNull(grid, name);

        if (grid.length == 0) {
            throw new IllegalArgumentException(name + " ne doit pas etre vide");
        }

        requireNonNull(grid[0], name + "[0]");
        if (grid[0].length == 0) {
            throw new IllegalArgumentException(name + " ne doit pas contenir de ligne vide");
        }

        int width = grid[0].length;
        for (int y = 0; y < grid.length; y++) {
            requireNonNull(grid[y], name + "[" + y + "]");
            if (grid[y].length != width) {
                throw new IllegalArgumentException(name + " doit etre rectangulaire");
            }
        }

        if (grid.length != width) {
            throw new IllegalArgumentException(name + " doit etre carree");
        }

        return grid;
    }

    /**
     * Vérifie qu'une taille est une puissance de 2.
     *
     * @param value taille testée
     * @param name nom du paramètre
     * @return la valeur si valide
     */
    public static int requirePowerOfTwo(int value, String name) {
        requirePositive(value, name);

        if ((value & (value - 1)) != 0) {
            throw new IllegalArgumentException(name + " doit etre une puissance de 2");
        }

        return value;
    }

    /**
     * Vérifie qu'une grille carrée a une taille puissance de 2.
     *
     * @param grid grille testée
     * @param name nom du paramètre
     * @return la grille si valide
     */
    public static int[][] requireSquarePowerOfTwoGrid(int[][] grid, String name) {
        requireSquareGrid(grid, name);
        requirePowerOfTwo(grid.length, name + ".length");
        return grid;
    }
}