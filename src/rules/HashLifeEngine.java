package rules;

import core.*;
import rules.Rules;

/**
 * Moteur de simulation HashLife : calcule la génération suivante d'un automate cellulaire
 * en utilisant la mémoïsation via un {@link HashCache}.
 *
 * <p>L'algorithme HashLife repose sur une représentation en quadtree des cellules :
 * chaque nœud ({@link Node}) représente un carré de cellules dont la taille est
 * une puissance de deux. La mémoïsation permet d'éviter de recalculer des sous-motifs
 * déjà rencontrés, ce qui confère à l'algorithme d'excellentes performances sur les
 * motifs avec de nombreuses répétitions.</p>
 *
 * <p>Actuellement, seul le niveau 0 (feuille 2×2, {@link LeafNode}) est pris en charge.
 * La gestion des {@code InternalNode} (niveaux supérieurs) sera ajoutée ultérieurement.</p>
 *
 * <p>Exemple d'utilisation :</p>
 * <pre>{@code
 * HashCache cache = new HashCache();
 * Rules conway = new ConwayRule();
 * LeafNode feuille = cache.getOrCreateLeaf(1, 1, 1, 0);
 * Node suivant = HashLifeEngine.nextGeneration(feuille, cache, conway);
 * }</pre>
 *
 * @author Équipe HashLife
 * @version 1.0
 * @see Rules
 * @see HashCache
 * @see LeafNode
 */
public class HashLifeEngine {

    /**
     * Calcule la génération suivante d'un nœud en utilisant les règles de Conway par défaut.
     *
     * <p>Délègue à {@link #nextGeneration(Node, HashCache, Rules)} avec une instance de
     * {@link ConwayRule}.</p>
     *
     * @param node  le nœud à faire évoluer ; ne doit pas être {@code null}
     * @param cache le cache de mémoïsation ; ne doit pas être {@code null}
     * @return      le nœud représentant l'état à la génération suivante
     * @throws IllegalArgumentException si {@code node} ou {@code cache} est {@code null}
     */
    public static Node nextGeneration(Node node, HashCache cache) {
        return nextGeneration(node, cache, new ConwayRule());
    }

    /**
     * Calcule la génération suivante d'un nœud selon la règle spécifiée.
     *
     * <p>La méthode commence par vérifier si le résultat est déjà mémoïsé dans le
     * {@code cache}. Si ce n'est pas le cas, elle délègue le calcul à
     * {@link #nextLeaf(LeafNode, HashCache, Rules)} pour les feuilles (niveau 0),
     * puis stocke le résultat dans le cache avant de le retourner.</p>
     *
     * <p><b>Limitation actuelle :</b> seuls les nœuds de niveau 0 ({@link LeafNode}) sont
     * supportés. Un {@code UnsupportedOperationException} est levé pour les niveaux supérieurs.</p>
     *
     * @param node  le nœud à faire évoluer ; ne doit pas être {@code null}
     * @param cache le cache de mémoïsation ; ne doit pas être {@code null}
     * @param rule  la règle d'automate cellulaire à appliquer ; ne doit pas être {@code null}
     * @return      le nœud représentant l'état à la génération suivante
     * @throws IllegalArgumentException      si l'un des paramètres est {@code null}
     * @throws UnsupportedOperationException si le nœud est un {@code InternalNode} (non implémenté)
     */
    public static Node nextGeneration(Node node, HashCache cache, Rules rule) {
        if (node == null)  throw new IllegalArgumentException("node ne doit pas être null");
        if (cache == null) throw new IllegalArgumentException("cache ne doit pas être null");
        if (rule == null)  throw new IllegalArgumentException("rule ne doit pas être null");

        Node cached = cache.getResult(node);
        if (cached != null) return cached;

        LeafNode nextLeaf;
        if (node.getLevel() == 0) {
            LeafNode leaf = (LeafNode) node;
            nextLeaf = nextLeaf(leaf, cache, rule);
        } else {
            throw new UnsupportedOperationException("InternalNode non implémenté");
        }

        cache.setResult(node, nextLeaf);
        return nextLeaf;
    }

    /**
     * Calcule la génération suivante d'une feuille 2×2.
     *
     * <p>Les quatre cellules de la feuille sont disposées comme suit :</p>
     * <pre>
     *   NW (0,0) | NE (1,0)
     *   ---------+---------
     *   SW (0,1) | SE (1,1)
     * </pre>
     *
     * <p>Pour chaque cellule, les voisins directs au sein de la feuille 2×2 sont les
     * trois autres cellules (voisinage de Moore restreint au carré) :</p>
     * <ul>
     *   <li><b>NW</b> : voisins = NE + SW + SE</li>
     *   <li><b>NE</b> : voisins = NW + SW + SE</li>
     *   <li><b>SW</b> : voisins = NW + NE + SE</li>
     *   <li><b>SE</b> : voisins = NW + NE + SW</li>
     * </ul>
     *
     * <p>La règle fournie est ensuite appliquée à chaque cellule pour obtenir son état
     * à la génération suivante. La nouvelle feuille est récupérée (ou créée) via le cache.</p>
     *
     * @param feuille la feuille 2×2 source ; ne doit pas être {@code null}
     * @param cache   le cache de mémoïsation utilisé pour créer ou réutiliser des feuilles ;
     *                ne doit pas être {@code null}
     * @param rule    la règle d'automate cellulaire à appliquer ; ne doit pas être {@code null}
     * @return        une nouvelle {@link LeafNode} représentant l'état à la génération suivante
     */
    public static LeafNode nextLeaf(LeafNode feuille, HashCache cache, Rules rule) {
        int nwCell = feuille.getNWCell(); // NW
        int neCell = feuille.getNECell(); // NE
        int swCell = feuille.getSWCell(); // SW
        int seCell = feuille.getSECell(); // SE

        // NW(0,0) : voisins = NE + SW + SE
        int nwNeighbors = neCell + swCell + seCell;
        int nextNW = rule.nextState(nwCell, nwNeighbors);

        // NE(1,0) : voisins = NW + SW + SE
        int neNeighbors = nwCell + swCell + seCell;
        int nextNE = rule.nextState(neCell, neNeighbors);

        // SW(0,1) : voisins = NW + NE + SE
        int swNeighbors = nwCell + neCell + seCell;
        int nextSW = rule.nextState(swCell, swNeighbors);

        // SE(1,1) : voisins = NW + NE + SW
        int seNeighbors = nwCell + neCell + swCell;
        int nextSE = rule.nextState(seCell, seNeighbors);

        return cache.getOrCreateLeaf(nextNW, nextNE, nextSW, nextSE);
    }
}