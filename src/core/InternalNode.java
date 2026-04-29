package core;
import java.util.Objects;

/**
 * Un nœud interne du Quadtree : il regroupe 4 sous-nœuds (NW, NE, SW, SE).
 * Contrairement à une feuille, un InternalNode ne stocke pas directement des cellules.
 * Il délègue tout à ses 4 enfants, qui peuvent être des LeafNode ou d'autres InternalNode.
 * Le niveau d'un InternalNode est toujours égal au niveau de ses enfants + 1.
 * Un nœud de niveau n représente une grille de taille 2^(n+1) × 2^(n+1).
 */
public class InternalNode implements Node {

    private final Node nw; 
    private final Node ne; 
    private final Node sw; 
    private final Node se; 
    private final int level; // niveau de ce nœud = niveau des enfants + 1

    // Résultat HashLife mis en cache pour éviter de recalculer l'état futur
    private Node cachedResult;

    /**
     * Crée un nœud interne à partir de 4 enfants.
     * Tous les enfants doivent être non-null et avoir le même niveau.
     * @param nw enfant nord-ouest
     * @param ne enfant nord-est
     * @param sw enfant sud-ouest
     * @param se enfant sud-est
     * @throws IllegalArgumentException si un enfant est null ou si les niveaux diffèrent
     */
    public InternalNode(Node nw, Node ne, Node sw, Node se) {
        // Aucun enfant ne peut être null
        if (nw == null || ne == null || sw == null || se == null) {
            throw new IllegalArgumentException("Tous les nœuds enfants doivent être non-null");
        }

        // Tous les enfants doivent être au même niveau sinon l'arbre serait incohérent
        if (nw.getLevel() != ne.getLevel() ||
            nw.getLevel() != sw.getLevel() ||
            nw.getLevel() != se.getLevel()) {
            throw new IllegalArgumentException(
                "Tous les enfants doivent avoir le même niveau. " +
                "Reçu: nw=" + nw.getLevel() + ", ne=" + ne.getLevel() +
                ", sw=" + sw.getLevel() + ", se=" + se.getLevel());
        }

        this.nw = nw;
        this.ne = ne;
        this.sw = sw;
        this.se = se;

        // Ce nœud est un niveau au-dessus de ses enfants
        this.level = nw.getLevel() + 1;

        this.cachedResult = null;
    }

    // Niveau de ce nœud dans l'arbre (toujours >= 1 pour un nœud interne)
    /**
	 * Retourne le niveau de ce nœud interne dans l'arbre.
	 * Toujours supérieur ou égal à 1 (un nœud interne n'est jamais une feuille).
	 * @return le niveau de ce nœud (niveau des enfants + 1)
	 */
	@Override
    public int getLevel() {
        return level;
    }

    // La population totale est la somme des populations des 4 enfants (récursif)
    /**
	 * Calcule le nombre total de cellules vivantes dans ce nœud.
	 * Additionne récursivement la population des 4 enfants.
	 * @return le nombre de cellules vivantes dans la zone représentée
	 */
	@Override
	public long getPopulation() {
        return nw.getPopulation() +
               ne.getPopulation() +
               sw.getPopulation() +
               se.getPopulation();
    }

    /**
     * Calcule l'état futur de ce nœud (algorithme HashLife).
     * Le résultat retourné est un nœud de niveau (level - 1) représentant la zone centrale.
     * Pour l'instant, le résultat doit avoir été mis en cache au préalable.
     * @param steps nombre de générations à calculer (doit être une puissance de 2)
     * @return l'état futur mis en cache
     * @throws UnsupportedOperationException si le résultat n'est pas encore calculé
     */
    @Override
    public Node getResult(int steps) {
        if (cachedResult == null) {
            throw new UnsupportedOperationException(
                "getResult() pas encore implémenté pour InternalNode.");
        }
        return cachedResult;
    }

    // Getters sur les 4 enfants
    @Override 
    public Node getNW() {
		 return nw; 
		 }
    @Override 
    public Node getNE() {
		 return ne; 
		 }
    @Override 
    public Node getSW() {
		 return sw; 
		 }
    @Override 
    public Node getSE() {
		 return se; 
		 }

    /**
     * HashCode basé sur les 4 enfants et le niveau.
     * Essentiel pour le HashCache : deux nœuds identiques doivent avoir le même hash.
     */
    @Override
    public int hashCode() {
        return Objects.hash(nw, ne, sw, se, level);
    }

    /**
     * Deux InternalNode sont égaux s'ils ont le même niveau et les mêmes 4 enfants.
     * Note : on utilise .equals() sur les enfants (pas ==) car ce sont des objets.
     * L'égalité est structurelle, pas par référence.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        InternalNode other = (InternalNode) obj;

        return level == other.level &&
               nw.equals(other.nw) &&
               ne.equals(other.ne) &&
               sw.equals(other.sw) &&
               se.equals(other.se);
    }

    @Override
    public String toString() {
        return String.format(
            "InternalNode[level=%d, pop=%d, children=(nw:%s, ne:%s, sw:%s, se:%s)]",
            level,
            getPopulation(),
            nw.getClass().getSimpleName(),
            ne.getClass().getSimpleName(),
            sw.getClass().getSimpleName(),
            se.getClass().getSimpleName()
        );
    }
}
