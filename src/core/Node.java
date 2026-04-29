package core;

/**
 * Interface représentant un nœud dans le Quadtree du jeu de la vie.
 * 
 * Un nœud peut être soit une feuille (LeafNode) qui représente un bloc 2×2 de cellules,
 * soit un nœud interne (InternalNode) composé de 4 sous-nœuds (NW, NE, SW, SE).
 * 
 * Cette interface suit le pattern Composite, permettant de traiter uniformément
 * les feuilles et les nœuds internes.
 * 
 * Les nœuds sont immutables : toute modification nécessite la création d'un nouveau nœud.
 */
public interface Node {
    
    /**
     * Retourne le niveau de ce nœud dans l'arbre.
     * 
     * Le niveau détermine la taille de la zone représentée :
     * - Niveau 0 (feuille) : zone 2×2
     * - Niveau 1 : zone 4×4
     * - Niveau 2 : zone 8×8
     * - Niveau n : zone 2^(n+1) × 2^(n+1)
     * 
     * @return le niveau du nœud (0 pour une feuille, > 0 pour un nœud interne)
     */
    int getLevel();
    
    /**
     * Compte le nombre total de cellules vivantes dans ce nœud.
     * 
     * Pour une feuille : somme des 4 cellules (0 à 4).
     * Pour un nœud interne : somme récursive des populations des 4 enfants.
     * 
     * @return le nombre de cellules vivantes
     */
    long getPopulation();
    
    /**
     * Calcule l'état futur de ce nœud après un nombre donné d'étapes.
     * 
     * C'est le cœur de l'algorithme HashLife. Cette méthode utilise la mémoïsation
     * pour éviter de recalculer les mêmes états, permettant de sauter exponentiellement
     * dans le temps.
     * 
     * Contraintes :
     * - Le nœud doit être de niveau au moins 2
     * - Le résultat est un nœud de niveau (n-1) représentant la zone centrale
     * - steps doit être une puissance de 2
     * 
     * @param steps le nombre de générations à calculer
     * @return le nœud représentant l'état futur
     */
    Node getResult(int steps);
    
    /**
     * Retourne le sous-nœud nord-ouest (quadrant supérieur gauche).
     * 
     * @return le sous-nœud NW
     */
    Node getNW();
    
    /**
     * Retourne le sous-nœud nord-est (quadrant supérieur droit).
     * 
     * @return le sous-nœud NE
     */
    Node getNE();
    
    /**
     * Retourne le sous-nœud sud-ouest (quadrant inférieur gauche).
     * 
     * @return le sous-nœud SW
     */
    Node getSW();
    
    /**
     * Retourne le sous-nœud sud-est (quadrant inférieur droit).
     * 
     * @return le sous-nœud SE
     */
    Node getSE();
    
    /**
     * Calcule le code de hachage de ce nœud.
     * 
     * Crucial pour le HashCache : deux nœuds représentant le même état
     * doivent avoir le même hashCode.
     * 
     * Pour une feuille : basé sur les valeurs des 4 cellules.
     * Pour un nœud interne : basé sur l'identité des 4 enfants et le niveau.
     * 
     * @return le code de hachage
     */
    int hashCode();
    
    /**
     * Vérifie l'égalité structurelle avec un autre objet.
     * 
     * Deux nœuds sont égaux s'ils représentent exactement le même état :
     * - Pour les feuilles : mêmes valeurs de cellules (nw, ne, sw, se)
     * - Pour les nœuds internes : mêmes enfants (comparaison par référence) et même niveau
     * 
     * Important : equals() et hashCode() doivent être cohérents pour le HashCache.
     * 
     * @param obj l'objet à comparer
     * @return true si les nœuds représentent le même état
     */
    boolean equals(Object obj);
}
