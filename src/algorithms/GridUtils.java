package algorithms;

import core.HashCache;
import core.LeafNode;
import core.Node;
import core.QuadTree;

/**
 * Outils minimaux partagés pour manipuler
 * les grilles denses et les nœuds.
 */
public final class GridUtils {

    private GridUtils() {
        // Classe utilitaire.
    }

    /**
     * Convertit un nœud en grille dense.
     *
     * Convention du projet :
     * - niveau 0 = 2x2
     * - niveau 1 = 4x4
     * - niveau 2 = 8x8
     *
     * @param node nœud source
     * @return grille correspondante
     */
    public static int[][] nodeToGrid(Node node) {
        AlgorithmChecks.requireNode(node);

        int size = 1 << (node.getLevel() + 1);
        int[][] grid = new int[size][size];
        fillGrid(node, grid, 0, 0, size);
        return grid;
    }

    /**
     * Convertit un arbre en grille dense.
     *
     * @param tree arbre source
     * @return grille correspondante
     */
    public static int[][] treeToGrid(QuadTree tree) {
        AlgorithmChecks.requireTree(tree);
        return nodeToGrid(tree.getRacine());
    }

    /**
     * Copie profonde d'une grille.
     *
     * @param source grille source
     * @return copie indépendante
     */
    public static int[][] copyGrid(int[][] source) {
        AlgorithmChecks.requireSquareGrid(source, "source");

        int[][] copy = new int[source.length][source[0].length];
        for (int y = 0; y < source.length; y++) {
            System.arraycopy(source[y], 0, copy[y], 0, source[y].length);
        }
        return copy;
    }

    /**
     * Compare deux grilles cellule par cellule.
     *
     * @param a première grille
     * @param b seconde grille
     * @return true si elles sont identiques
     */
    public static boolean sameGrid(int[][] a, int[][] b) {
        AlgorithmChecks.requireSquareGrid(a, "a");
        AlgorithmChecks.requireSquareGrid(b, "b");

        if (a.length != b.length || a[0].length != b[0].length) {
            return false;
        }

        for (int y = 0; y < a.length; y++) {
            for (int x = 0; x < a[y].length; x++) {
                if (a[y][x] != b[y][x]) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Reconstruit un nœud canonicalisé depuis une grille
     * carrée de taille puissance de 2.
     *
     * @param grid grille source
     * @param cache cache utilisé pour canonicaliser
     * @return nœud reconstruit
     */
    public static Node buildNodeFromGrid(int[][] grid, HashCache cache) {
        AlgorithmChecks.requireSquarePowerOfTwoGrid(grid, "grid");
        AlgorithmChecks.requireNonNull(cache, "cache");

        return buildNodeFromGridRecursive(grid, 0, 0, grid.length, cache);
    }

    /**
     * Remplit récursivement une grille depuis un nœud.
     */
    private static void fillGrid(Node node, int[][] grid, int startX, int startY, int size) {
        if (node.getLevel() == 0) {
            LeafNode leaf = (LeafNode) node;
            grid[startY][startX] = leaf.getNWCell();
            grid[startY][startX + 1] = leaf.getNECell();
            grid[startY + 1][startX] = leaf.getSWCell();
            grid[startY + 1][startX + 1] = leaf.getSECell();
            return;
        }

        int half = size / 2;
        fillGrid(node.getNW(), grid, startX, startY, half);
        fillGrid(node.getNE(), grid, startX + half, startY, half);
        fillGrid(node.getSW(), grid, startX, startY + half, half);
        fillGrid(node.getSE(), grid, startX + half, startY + half, half);
    }

    /**
     * Reconstruction récursive depuis une sous-grille carrée.
     */
    private static Node buildNodeFromGridRecursive(
        int[][] grid,
        int startX,
        int startY,
        int size,
        HashCache cache
    ) {
        if (size == 2) {
            return cache.getOrCreateLeaf(
                grid[startY][startX],
                grid[startY][startX + 1],
                grid[startY + 1][startX],
                grid[startY + 1][startX + 1]
            );
        }

        int half = size / 2;

        Node nw = buildNodeFromGridRecursive(grid, startX, startY, half, cache);
        Node ne = buildNodeFromGridRecursive(grid, startX + half, startY, half, cache);
        Node sw = buildNodeFromGridRecursive(grid, startX, startY + half, half, cache);
        Node se = buildNodeFromGridRecursive(grid, startX + half, startY + half, half, cache);

        return cache.getOrCreateInternal(nw, ne, sw, se);
    }
}