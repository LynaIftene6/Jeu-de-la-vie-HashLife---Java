package core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Implémentation d’un QuadTree pour représenter une grille binaire.
 *
 * La grille est découpée récursivement en 4 quadrants jusqu’à atteindre
 * des blocs 2x2 (feuilles).
 *
 * Utilise un HashCache pour éviter de dupliquer les nœuds identiques
 * et optimiser la mémoire (principe de canonicalisation).
 */
public class QuadTree {

    private Node racine;     // racine de l’arbre
    private HashCache cache; // cache
    private int taille;      // taille de la grille

    /**
     * Construit un QuadTree à partir d’une grille.
     * @param grille matrice carrée 
     */
    public QuadTree(int[][] grille) {
        cache  = new HashCache();
        taille = grille.length;
        racine = construire(grille);
    }

    /**
     * Construit récursivement l’arbre à partir d’une grille.
     * Découpe en 4 jusqu’à atteindre des blocs 2x2.
     */
    private Node construire(int[][] grille) {
        int n = grille.length;

        // cas de base : feuille 2x2
        if (n == 2) {
            return cache.getOrCreateLeaf(
                grille[0][0], grille[0][1],
                grille[1][0], grille[1][1]);
        }

        int d = n / 2;

        // sous-grilles
        int[][] nw = new int[d][d];
        int[][] ne = new int[d][d];
        int[][] sw = new int[d][d];
        int[][] se = new int[d][d];

        // découpage
        for (int y = 0; y < d; y++) {
            for (int x = 0; x < d; x++) {
                nw[y][x] = grille[y][x];
                ne[y][x] = grille[y][x + d];
                sw[y][x] = grille[y + d][x];
                se[y][x] = grille[y + d][x + d];
            }
        }

        // construction récursive
        return cache.getOrCreateInternal(
            construire(nw),
            construire(ne),
            construire(sw),
            construire(se)
        );
    }

    /**
     * Crée un nœud entièrement vide (rempli de 0) de niveau donné.
     * Grâce au cache, toutes les branches pointent vers les mêmes objets.
     */
    private Node creerVide(int n) {
        if (n == 0) {
            return cache.getOrCreateLeaf(0, 0, 0, 0);
        }

        Node e = creerVide(n - 1);
        return cache.getOrCreateInternal(e, e, e, e);
    }

    /**
     * Agrandit la grille en ajoutant du vide autour.
     * Le contenu actuel est replacé au centre.
     */
    public void expand() {

        Node v = creerVide(racine.getLevel() - 1);

        racine = cache.getOrCreateInternal(
            cache.getOrCreateInternal(v, v, v, racine.getNW()),
            cache.getOrCreateInternal(v, v, racine.getNE(), v),
            cache.getOrCreateInternal(v, racine.getSW(), v, v),
            cache.getOrCreateInternal(racine.getSE(), v, v, v)
        );

        taille *= 2;
    }

    /**
     * Retourne la valeur d’une cellule (0 ou 1).
     * Si la position est hors grille donc retourne 0.
     */
    public int getCell(int x, int y) {
        if (x < 0 || y < 0 || x >= taille || y >= taille)
            return 0;

        return getCellRec(x, y, racine, taille);
    }

    /**
     * Parcours récursif pour atteindre une cellule.
     */
    private int getCellRec(int x, int y, Node n, int t) {

        // feuille → accès direct
        if (n.getLevel() == 0) {
            LeafNode l = (LeafNode) n;

            if (x == 0 && y == 0) return l.getNWCell();
            if (x == 1 && y == 0) return l.getNECell();
            if (x == 0 && y == 1) return l.getSWCell();
            return l.getSECell();
        }

        int d = t / 2;

        // on descend dans le bon quadrant
        if (x < d && y < d) return getCellRec(x, y, n.getNW(), d);
        if (x >= d && y < d) return getCellRec(x - d, y, n.getNE(), d);
        if (x < d) return getCellRec(x, y - d, n.getSW(), d);

        return getCellRec(x - d, y - d, n.getSE(), d);
    }

    /**
     * Affiche toute la grille dans la console.
     * '#' pour 1 et '.' pour 0.
     */
    public void afficherGrille() {
        for (int y = 0; y < taille; y++) {
            for (int x = 0; x < taille; x++) {
                System.out.print(
                    getCellRec(x, y, racine, taille) == 1 ? "#" : "."
                );
            }
            System.out.println();
        }
    }

    /**
     * Classe interne pour associer un nœud à sa position dans la grille.
     */
    private static class NoeudPos {
        Node n;
        int sx, sy, t;

        NoeudPos(Node n, int sx, int sy, int t) {
            this.n = n;
            this.sx = sx;
            this.sy = sy;
            this.t = t;
        }
    }

    /**
     * Affiche les différentes divisions du QuadTree niveau par niveau.
     * Permet de visualiser la structure et de détecter les doublons.
     */
    public void afficherEtapesQuadrants() {

        System.out.println("Grille de depart :");
        afficherGrille();
        System.out.println();

        List<NoeudPos> courant = new ArrayList<>();
        courant.add(new NoeudPos(racine, 0, 0, taille));

        int niveau = 1;

        // tant qu’il reste des nœuds internes
        while (courant.stream().anyMatch(np -> np.n.getLevel() > 0)) {

            List<NoeudPos> suivant = new ArrayList<>();

            // division en 4
            for (NoeudPos np : courant) {
                if (np.n.getLevel() == 0) continue;

                int d = np.t / 2;

                suivant.add(new NoeudPos(np.n.getNW(), np.sx, np.sy, d));
                suivant.add(new NoeudPos(np.n.getNE(), np.sx + d, np.sy, d));
                suivant.add(new NoeudPos(np.n.getSW(), np.sx, np.sy + d, d));
                suivant.add(new NoeudPos(np.n.getSE(), np.sx + d, np.sy + d, d));
            }

            // détection des doublons
            HashSet<Node> vus = new HashSet<>();
            int doublons = 0;

            for (NoeudPos np : suivant) {
                if (!vus.add(np.n)) doublons++;
            }

            System.out.println("niveau " + niveau +
                " | division " + courant.get(0).t + "x" + courant.get(0).t +
                " | doublons : " + doublons);

            // afficher chaque zone unique une seule fois
            HashSet<Node> affiches = new HashSet<>();
            for (NoeudPos np : suivant) {
                if (affiches.add(np.n)) {
                    afficherZone(np.sx, np.sy, np.t);
                }
            }

            courant = suivant;
            niveau++;
        }
    }

    /**
     * Affiche une sous-zone de la grille.
     */
    private void afficherZone(int sx, int sy, int t) {
        for (int y = 0; y < t; y++) {
            for (int x = 0; x < t; x++) {
                System.out.print(
                    getCellRec(sx + x, sy + y, racine, taille) == 1 ? "#" : "."
                );
            }
            System.out.println();
        }
        System.out.println();
    }

    // getters / setters
	/**
	 * Retourne le nœud racine du QuadTree.
	 * @return la racine de l'arbre
	 */
	public Node getRacine() {
        return racine;
    }
    
	/**
	 * Remplace la racine du QuadTree et met à jour la taille en conséquence.
	 * @param n le nouveau nœud racine
	 */
	public void setRacine(Node n) {
        this.racine = n;
        taille = (int) Math.pow(2, n.getLevel() + 1);
    }
    
	/**
	 * Retourne le HashCache utilisé par ce QuadTree.
	 * @return le cache de canonicalisation
	 */
	public HashCache getCache() {
        return cache;
    }
    
	/**
	 * Retourne la taille actuelle de la grille (en nombre de cellules par côté).
	 * @return la taille de la grille
	 */
	public int getTaille() {
        return taille;
    }
}
