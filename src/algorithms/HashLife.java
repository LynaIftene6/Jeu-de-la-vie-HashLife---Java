package algorithms;

import core.HashCache;
import core.Node;
import rules.ConwayRule;

/**
 * Implémentation de HashLife pour le Jeu de la Vie de Conway.
 *
 * Cette classe manipule des quadtrees canonicalisés :
 * - niveau 0 : feuille 2x2
 * - niveau 1 : nœud 4x4
 * - niveau 2 : nœud 8x8
 * - etc.
 *
 * Le principe général est :
 * - join(...) assemble des nœuds en réutilisant le cache structurel
 * - getZero(...) construit des sous-arbres vides partagés
 * - successor(...) calcule le successeur naturel centré d'un nœud
 * - evolve(...) applique exactement un macro-step naturel
 *
 * Cette implémentation est spécialisée pour la règle de Conway.
 */
public final class HashLife {

    /** Cache global utilisé pour canonicaliser les nœuds et mémoriser successor(node). */
    private static HashCache CACHE = new HashCache();

    /** Règle unique utilisée par cette implémentation. */
    private static final ConwayRule RULE = new ConwayRule();

    private HashLife() {
        // Classe utilitaire.
    }

    /**
     * Réinitialise complètement le cache global.
     */
    public static void resetCache() {
        CACHE = new HashCache();
    }

    /**
     * Retourne une description textuelle de l'état du cache.
     *
     * @return résumé du cache
     */
    public static String getCacheStats() {
        return CACHE.toString();
    }

    /**
     * Fait évoluer un nœud d'exactement un macro-step naturel
     * du nœud d'origine.
     *
     * On commence par entourer le motif d'une marge vide avec pad(...),
     * puis on calcule le résultat après 2^(level-1) générations,
     * où level est le niveau du nœud d'origine.
     *
     * Important :
     * on ne demande pas le macro-step naturel du nœud paddé,
     * sinon on saute deux fois trop loin.
     *
     * @param node nœud de départ
     * @param steps nombre de générations demandé ; doit être exactement
     *              égal au macro-step naturel du nœud d'origine
     * @return nœud résultat de même taille logique que le nœud d'origine
     * @throws IllegalArgumentException si node est null, si steps inf 0,
     *                                  si steps n'est pas le macro-step naturel,
     *                                  ou si le niveau du nœud est trop petit
     */
    public static Node evolve(Node node, int steps) {
        AlgorithmChecks.requireNode(node);
        AlgorithmChecks.requireNonNegative(steps, "steps");

        if (steps == 0) {
            return node;
        }

        int macroStep = getMacroStepFor(node);
        if (steps != macroStep) {
            throw new IllegalArgumentException(
                "steps doit etre egal au macro-step naturel (" + macroStep + ")"
            );
        }

        if (node.getLevel() < 1) {
            throw new IllegalArgumentException(
                "HashLife exige un noeud de niveau >= 1"
            );
        }

        Node padded = pad(node);

        // On garde le saut naturel du noeud d'origine.
        return successor(padded, node.getLevel() - 1);
    }

    /**
     * Entoure un nœud d'une bordure vide pour laisser
     * une marge spatiale au calcul HashLife.
     *
     * Le nœud retourné a un niveau supérieur et place le motif initial
     * au centre logique d'une zone plus grande remplie de vide.
     *
     * Cette opération n'avance pas le temps.
     * Elle prépare simplement un calcul correct du centre utile.
     *
     * @param node nœud à entourer de vide
     * @return nœud paddé, de niveau supérieur
     * @throws IllegalArgumentException si node est null ou de niveau 0
     */
    private static Node pad(Node node) {
        AlgorithmChecks.requireNode(node);

        if (node.getLevel() == 0) {
            throw new IllegalArgumentException(
                "pad exige un noeud de niveau >= 1"
            );
        }

        Node empty = getZero(node.getLevel() - 1);

        Node newNW = join(empty, empty, empty, node.getNW());
        Node newNE = join(empty, empty, node.getNE(), empty);
        Node newSW = join(empty, node.getSW(), empty, empty);
        Node newSE = join(node.getSE(), empty, empty, empty);

        return join(newNW, newNE, newSW, newSE);
    }

    /**
     * Retourne le macro-step naturel d'un nœud.
     *
     * Dans HashLife, un nœud de niveau n peut calculer directement
     * l'évolution de son centre utile après 2^(n-1) générations.
     *
     * Exemples :
     * - niveau 0 (2x2)   -> 0
     * - niveau 1 (4x4)   -> 1
     * - niveau 2 (8x8)   -> 2
     * - niveau 3 (16x16) -> 4
     *
     * @param node nœud considéré
     * @return nombre de générations sautées naturellement
     */
    public static int getMacroStepFor(Node node) {
        AlgorithmChecks.requireNode(node);

        if (node.getLevel() <= 0) {
            return 0;
        }

        return 1 << (node.getLevel() - 1);
    }

    /**
     * Assemble quatre enfants de même niveau en un parent canonicalisé.
     *
     * Le parent n'est pas créé directement avec new :
     * il passe par le cache structurel afin de partager
     * les sous-arbres identiques déjà existants.
     *
     * Ce partage est essentiel pour HashLife,
     * car il réduit fortement la mémoire utilisée
     * et augmente les chances de réutiliser des résultats déjà calculés.
     *
     * @param nw quadrant nord-ouest
     * @param ne quadrant nord-est
     * @param sw quadrant sud-ouest
     * @param se quadrant sud-est
     * @return parent canonicalisé de niveau supérieur
     * @throws IllegalArgumentException si un enfant est null
     *                                  ou si les niveaux diffèrent
     */
    public static Node join(Node nw, Node ne, Node sw, Node se) {
        AlgorithmChecks.requireNode(nw);
        AlgorithmChecks.requireNode(ne);
        AlgorithmChecks.requireNode(sw);
        AlgorithmChecks.requireNode(se);

        if (nw.getLevel() != ne.getLevel()
            || nw.getLevel() != sw.getLevel()
            || nw.getLevel() != se.getLevel()) {
            throw new IllegalArgumentException("join exige quatre enfants de meme niveau");
        }

        return CACHE.getOrCreateInternal(nw, ne, sw, se);
    }

    /**
     * Retourne un nœud entièrement vide pour un niveau donné.
     *
     * Le résultat est lui aussi canonicalisé :
     * un même niveau vide est reconstruit via le cache
     * et réutilisé partout dans l'algorithme.
     *
     * Exemples :
     * - niveau 0 -> feuille 2x2 vide
     * - niveau 1 -> nœud 4x4 vide
     * - niveau 2 -> nœud 8x8 vide
     *
     * @param level niveau demandé
     * @return nœud vide de ce niveau
     * @throws IllegalArgumentException si level inf 0
     */
    public static Node getZero(int level) {
        AlgorithmChecks.requireNonNegative(level, "level");

        if (level == 0) {
            return CACHE.getOrCreateLeaf(0, 0, 0, 0);
        }

        Node zeroChild = getZero(level - 1);
        return join(zeroChild, zeroChild, zeroChild, zeroChild);
    }

    /**
     * Convertit un nœud en grille dense.
     *
     * @param node nœud source
     * @return grille correspondante
     */
    public static int[][] nodeToGrid(Node node) {
        return GridUtils.nodeToGrid(node);
    }

    /**
     * Copie profonde d'une grille.
     *
     * @param source grille source
     * @return copie indépendante
     */
    public static int[][] copyGrid(int[][] source) {
        return GridUtils.copyGrid(source);
    }

    /**
     * Compare deux grilles cellule par cellule.
     *
     * @param a première grille
     * @param b seconde grille
     * @return true si elles sont identiques
     */
    public static boolean sameGrid(int[][] a, int[][] b) {
        return GridUtils.sameGrid(a, b);
    }

    /**
     * Calcule le successeur HashLife naturel centré d'un nœud.
     *
     * Pour un nœud de niveau n, cette méthode renvoie le centre utile
     * après le macro-step naturel de ce nœud, soit 2^(n-1) générations
     * dans la convention actuelle du projet.
     *
     * Cette version est celle qui utilise le cache de résultats existant.
     *
     * @param node nœud source, de niveau au moins 1
     * @return centre utile après le macro-step naturel du nœud
     * @throws IllegalArgumentException si node est null ou de niveau inf 1
     */
    public static Node successor(Node node) {
        AlgorithmChecks.requireNodeLevelAtLeast(node, 1, "node");

        Node cachedResult = CACHE.getResult(node);
        if (cachedResult != null) {
            return cachedResult;
        }

        Node result;

        if (node.getPopulation() == 0) {
            result = getZero(node.getLevel() - 1);
        } else if (node.getLevel() == 1) {
            result = life4x4(node);
        } else {
            Node nw = node.getNW();
            Node ne = node.getNE();
            Node sw = node.getSW();
            Node se = node.getSE();

            Node n00 = successor(nw);
            Node n01 = successor(centeredHorizontal(nw, ne));
            Node n02 = successor(ne);

            Node n10 = successor(centeredVertical(nw, sw));
            Node n11 = successor(centeredMiddle(nw, ne, sw, se));
            Node n12 = successor(centeredVertical(ne, se));

            Node n20 = successor(sw);
            Node n21 = successor(centeredHorizontal(sw, se));
            Node n22 = successor(se);

            Node rNW = successor(join(n00, n01, n10, n11));
            Node rNE = successor(join(n01, n02, n11, n12));
            Node rSW = successor(join(n10, n11, n20, n21));
            Node rSE = successor(join(n11, n12, n21, n22));

            result = join(rNW, rNE, rSW, rSE);
        }

        CACHE.setResult(node, result);
        return result;
    }

    /**
     * Calcule le centre utile d'un nœud après 2^jumpPower générations.
     *
     * Cette version sert uniquement à demander un saut plus petit
     * que le macro-step naturel du nœud courant.
     *
     * Elle est utile après padding :
     * le nœud paddé est plus grand pour fournir de l'espace,
     * mais on veut garder le nombre de générations du nœud initial.
     *
     * Cette version ne modifie pas le cache existant.
     *
     * @param node nœud source
     * @param jumpPower exposant du saut demandé
     * @return centre utile après 2^jumpPower générations
     * @throws IllegalArgumentException si jumpPower est invalide
     */
    private static Node successor(Node node, int jumpPower) {
        AlgorithmChecks.requireNodeLevelAtLeast(node, 1, "node");
        AlgorithmChecks.requireNonNegative(jumpPower, "jumpPower");

        if (jumpPower > node.getLevel() - 1) {
            throw new IllegalArgumentException(
                "jumpPower trop grand pour ce noeud : " + jumpPower
            );
        }

        if (jumpPower == node.getLevel() - 1) {
            return successor(node);
        }

        if (node.getPopulation() == 0) {
            return getZero(node.getLevel() - 1);
        }

        if (node.getLevel() == 1) {
            if (jumpPower != 0) {
                throw new IllegalArgumentException(
                    "Un noeud de niveau 1 ne peut calculer qu'une generation"
                );
            }
            return life4x4(node);
        }

        Node nw = node.getNW();
        Node ne = node.getNE();
        Node sw = node.getSW();
        Node se = node.getSE();

        Node c1 = successor(join(nw.getNW(), nw.getNE(), nw.getSW(), nw.getSE()), jumpPower);
        Node c2 = successor(join(nw.getNE(), ne.getNW(), nw.getSE(), ne.getSW()), jumpPower);
        Node c3 = successor(join(ne.getNW(), ne.getNE(), ne.getSW(), ne.getSE()), jumpPower);

        Node c4 = successor(join(nw.getSW(), nw.getSE(), sw.getNW(), sw.getNE()), jumpPower);
        Node c5 = successor(join(nw.getSE(), ne.getSW(), sw.getNE(), se.getNW()), jumpPower);
        Node c6 = successor(join(ne.getSW(), ne.getSE(), se.getNW(), se.getNE()), jumpPower);

        Node c7 = successor(join(sw.getNW(), sw.getNE(), sw.getSW(), sw.getSE()), jumpPower);
        Node c8 = successor(join(sw.getNE(), se.getNW(), sw.getSE(), se.getSW()), jumpPower);
        Node c9 = successor(join(se.getNW(), se.getNE(), se.getSW(), se.getSE()), jumpPower);

        return join(
            join(c1.getSE(), c2.getSW(), c4.getNE(), c5.getNW()),
            join(c2.getSE(), c3.getSW(), c5.getNE(), c6.getNW()),
            join(c4.getSE(), c5.getSW(), c7.getNE(), c8.getNW()),
            join(c5.getSE(), c6.getSW(), c8.getNE(), c9.getNW())
        );
    }

    /**
     * Cas de base de HashLife sur un bloc 4x4.
     *
     * À ce niveau, HashLife ne poursuit plus la récursion.
     * On convertit simplement le nœud en grille dense 4x4,
     * on calcule une génération de Conway,
     * puis on extrait le centre 2x2 du résultat.
     *
     * Ce centre 2x2 est exactement le successeur naturel
     * d'un nœud de niveau 1.
     *
     * @param node nœud 4x4
     * @return feuille 2x2 correspondant au centre après 1 génération
     * @throws IllegalArgumentException si le nœud n'est pas de niveau 1
     */
    public static Node life4x4(Node node) {
        AlgorithmChecks.requireNode(node);

        if (node.getLevel() != 1) {
            throw new IllegalArgumentException("life4x4 exige un noeud de niveau 1");
        }

        int[][] grid = nodeToGrid(node);
        int[][] next = nextGrid(grid);

        return CACHE.getOrCreateLeaf(
            next[1][1],
            next[1][2],
            next[2][1],
            next[2][2]
        );
    }

    /**
     * Construit le sous-bloc central horizontal entre deux nœuds adjacents.
     *
     * Si west et east sont côte à côte, cette méthode extrait
     * la bande centrale qui chevauche leur frontière commune.
     * Ce sous-bloc fait partie des 9 blocs recouvrants utilisés
     * par successor(...).
     *
     * @param west nœud de gauche
     * @param east nœud de droite
     * @return sous-bloc central horizontal
     * @throws IllegalArgumentException si un nœud est null
     *                                  ou si leurs niveaux diffèrent
     */
    private static Node centeredHorizontal(Node west, Node east) {
        AlgorithmChecks.requireNode(west);
        AlgorithmChecks.requireNode(east);

        if (west.getLevel() != east.getLevel()) {
            throw new IllegalArgumentException(
                "centeredHorizontal exige deux noeuds de meme niveau"
            );
        }

        return join(
            west.getNE(),
            east.getNW(),
            west.getSE(),
            east.getSW()
        );
    }

    /**
     * Construit le sous-bloc central vertical entre deux nœuds adjacents.
     *
     * Si north et south sont superposés, cette méthode extrait
     * la bande centrale qui chevauche leur frontière commune.
     * Ce sous-bloc fait partie des 9 blocs recouvrants utilisés
     * par successor(...).
     *
     * @param north nœud du haut
     * @param south nœud du bas
     * @return sous-bloc central vertical
     * @throws IllegalArgumentException si un nœud est null
     *                                  ou si leurs niveaux diffèrent
     */
    private static Node centeredVertical(Node north, Node south) {
        AlgorithmChecks.requireNode(north);
        AlgorithmChecks.requireNode(south);

        if (north.getLevel() != south.getLevel()) {
            throw new IllegalArgumentException(
                "centeredVertical exige deux noeuds de meme niveau"
            );
        }

        return join(
            north.getSW(),
            north.getSE(),
            south.getNW(),
            south.getNE()
        );
    }

    /**
     * Construit le sous-bloc central recouvrant à partir de quatre quadrants.
     *
     * Ce bloc correspond à la zone au milieu de :
     * - nw en haut à gauche
     * - ne en haut à droite
     * - sw en bas à gauche
     * - se en bas à droite
     *
     * Il s'agit du bloc central parmi les 9 sous-blocs recouvrants
     * nécessaires au calcul récursif de successor(...).
     *
     * @param nw quadrant nord-ouest
     * @param ne quadrant nord-est
     * @param sw quadrant sud-ouest
     * @param se quadrant sud-est
     * @return sous-bloc central recouvrant
     * @throws IllegalArgumentException si un nœud est null
     *                                  ou si les niveaux diffèrent
     */
    private static Node centeredMiddle(Node nw, Node ne, Node sw, Node se) {
        AlgorithmChecks.requireNode(nw);
        AlgorithmChecks.requireNode(ne);
        AlgorithmChecks.requireNode(sw);
        AlgorithmChecks.requireNode(se);

        if (nw.getLevel() != ne.getLevel()
            || nw.getLevel() != sw.getLevel()
            || nw.getLevel() != se.getLevel()) {
            throw new IllegalArgumentException(
                "centeredMiddle exige quatre noeuds de meme niveau"
            );
        }

        return join(
            nw.getSE(),
            ne.getSW(),
            sw.getNE(),
            se.getNW()
        );
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
                next[y][x] = RULE.nextState(grid[y][x], neighbors);
            }
        }

        return next;
    }

    /**
     * Compte le nombre de voisins vivants d'une cellule dans une grille dense.
     *
     * @param grid grille étudiée
     * @param x abscisse de la cellule
     * @param y ordonnée de la cellule
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
}