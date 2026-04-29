package core;

import java.util.HashMap;

/**
 * Cache central du Quadtree : garantit qu'il n'existe qu'une seule instance
 * de chaque nœud identique en mémoire (canonicalisation).
 *
 * Il gère 3 caches séparés :
 *   - un pour les feuilles (LeafNode)
 *   - un pour les nœuds internes (InternalNode)
 *   - un pour les résultats calculés par HashLife
 *
 * Grâce à ce cache, deux nœuds avec le même contenu partagent la même référence,
 * ce qui permet de comparer avec == au lieu de .equals() et d'économiser beaucoup de mémoire.
 */
public class HashCache {

    private HashMap<LeafKey, LeafNode> leafCache;         // cache des feuilles
    private HashMap<InternalKey, InternalNode> internalCache; // cache des nœuds internes
    private HashMap<Node, Node> resultCache;              // cache des résultats HashLife
    private int hits;   // nombre de fois où le nœud existait déjà dans le cache
    private int misses; // nombre de fois où on a dû créer un nouveau nœud

    /** Crée un cache vide, prêt à l'emploi. */
    public HashCache() {
        this.leafCache     = new HashMap<>();
        this.internalCache = new HashMap<>();
        this.resultCache   = new HashMap<>();
        this.hits   = 0;
        this.misses = 0;
    }

    /**
     * Retourne la feuille correspondant aux 4 valeurs données.
     * Si elle existe déjà dans le cache, on la réutilise (hit).
     * Sinon on en crée une nouvelle et on la mémorise (miss).
     *
     * @param nw cellule nord-ouest
     * @param ne cellule nord-est 
     * @param sw cellule sud-ouest 
     * @param se cellule sud-est 
     * @return la feuille unique correspondant à cet état
     */
    public LeafNode getOrCreateLeaf(int nw, int ne, int sw, int se) {
        LeafKey key = new LeafKey(nw, ne, sw, se);
        LeafNode existing = leafCache.get(key);

        if (existing != null) {
            hits++;         // nœud déjà connu, on le réutilise
            return existing;
        } else {
            LeafNode newLeaf = new LeafNode(nw, ne, sw, se);
            leafCache.put(key, newLeaf);
            misses++;       // nouveau nœud, on le met en cache
            return newLeaf;
        }
    }

    /**
     * Retourne le nœud interne correspondant aux 4 enfants donnés.
     * Même principe que getOrCreateLeaf : réutilise si déjà connu, crée sinon.
     * La clé est basée sur l'identité des enfants (référence mémoire),
     * car dans un Quadtree canonique deux enfants identiques sont toujours
     * la même instance.
     * @param nw enfant nord-ouest
     * @param ne enfant nord-est
     * @param sw enfant sud-ouest
     * @param se enfant sud-est
     * @return le nœud interne unique correspondant à ces 4 enfants
     */
    public InternalNode getOrCreateInternal(Node nw, Node ne, Node sw, Node se) {
        InternalKey key = new InternalKey(nw, ne, sw, se);
        InternalNode existing = internalCache.get(key);

        if (existing != null) {
            hits++;
            return existing;
        } else {
            InternalNode newNode = new InternalNode(nw, ne, sw, se);
            internalCache.put(key, newNode);
            misses++;
            return newNode;
        }
    }

    /**
     * Enregistre le résultat HashLife d'un nœud pour ne pas le recalculer.
     *
     * @param node   le nœud dont on a calculé l'état futur
     * @param result le nœud résultat 
     */
    public void setResult(Node node, Node result) {
        resultCache.put(node, result);
    }

    /**
     * Récupère le résultat HashLife déjà calculé pour ce nœud.
     * Retourne null si le résultat n'a pas encore été calculé.
     *
     * @param node le nœud dont on cherche le résultat
     * @return le résultat mis en cache, ou null
     */
    public Node getResult(Node node) {
        return resultCache.get(node);
    }

    /**
     * Retourne le nombre total de nœuds uniques connus dans le cache.
     * @return la somme du nombre de feuilles et de nœuds internes mis en cache
     */
    public int getCacheSize() {
        return leafCache.size() + internalCache.size();
    }

    /**
     * Retourne le nombre de fois où un nœud existant a été réutilisé (cache hit).
     * @return le nombre de hits
     */
    public int getHits() {
        return hits;
    }

    /**
     * Retourne le nombre de fois où un nouveau nœud a dû être créé (cache miss).
     * @return le nombre de misses
     */
    public int getMisses() {
        return misses;
    }

    /**
     * Retourne le taux de réutilisation du cache.
     * Plus ce taux est proche de 1, plus le cache est efficace.
     * @return hits / (hits + misses), ou 0.0 si aucune opération
     */
    public double getHitRate() {
        int total = hits + misses;
        if (total == 0) return 0.0;
        return (double) hits / total;
    }

    /** Vide tous les caches et remet les compteurs à zéro. */
    public void clear() {
        leafCache.clear();
        internalCache.clear();
        resultCache.clear();
        hits   = 0;
        misses = 0;
    }

    /**
     * Retourne une représentation lisible du cache pour le débogage.
     * Affiche la taille des 3 caches ainsi que les statistiques de hits/misses.
     * @return une chaîne décrivant l'état du cache
     */
    @Override
    public String toString() {
        return String.format(
            "HashCache[feuilles=%d, internes=%d, resultats=%d, " +
            "trouvailles=%d, creations=%d, taux=%.1f%%]",
            leafCache.size(),
            internalCache.size(),
            resultCache.size(),
            hits,
            misses,
            getHitRate() * 100
        );
    }

    /**
     * Clé pour identifier une feuille par le contenu de ses 4 cellules.
     * Deux LeafKey avec les mêmes valeurs sont considérées égales.
     */
    private static class LeafKey {
        private final int nw, ne, sw, se;

        public LeafKey(int nw, int ne, int sw, int se) {
            this.nw = nw; this.ne = ne;
            this.sw = sw; this.se = se;
        }

        @Override
        public int hashCode() {
            // Combinaison polynomiale simple des 4 valeurs (0 ou 1)
            return nw * 31 * 31 * 31 +
                   ne * 31 * 31 +
                   sw * 31 +
                   se;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof LeafKey)) return false;
            LeafKey other = (LeafKey) obj;
            return nw == other.nw && ne == other.ne &&
                   sw == other.sw && se == other.se;
        }
    }

    /**
     * Clé pour identifier un nœud interne par ses 4 enfants.
     * Utilise l'identité des références (System.identityHashCode et ==)
     * et non .equals(), car dans un Quadtree canonique deux nœuds identiques
     * sont toujours la même instance en mémoire.
     */
    private static class InternalKey {
        private final Node nw, ne, sw, se;

        public InternalKey(Node nw, Node ne, Node sw, Node se) {
            this.nw = nw; this.ne = ne;
            this.sw = sw; this.se = se;
        }

        @Override
        public int hashCode() {
            // On utilise l'adresse mémoire de chaque enfant comme base du hash
            return System.identityHashCode(nw) * 31 * 31 * 31 +
                   System.identityHashCode(ne) * 31 * 31 +
                   System.identityHashCode(sw) * 31 +
                   System.identityHashCode(se);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof InternalKey)) return false;
            InternalKey other = (InternalKey) obj;
            // == et pas .equals() : on compare les références, pas le contenu
            return nw == other.nw && ne == other.ne &&
                   sw == other.sw && se == other.se;
        }
    }
}
