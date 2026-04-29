package core;
import java.util.Objects;

/**
 * Représente une feuille dans le Quadtree, c'est-à-dire un bloc 2×2 de cellules.
 * C'est le niveau le plus bas de l'arbre (niveau 0).
 * Chaque feuille stocke directement l'état de 4 cellules : nw, ne, sw, se.
 * Les feuilles sont immutables : une fois créées, leurs valeurs ne peuvent pas changer.
 * Pour modifier une cellule, il faut créer une nouvelle feuille.
 */
public class LeafNode implements Node {
    
    private final int nw;  
    private final int ne;  
    private final int sw; 
    private final int se;
    
    // Résultat calculé par HashLife
    private Node cachedResult;
    
    /**
     * Crée une nouvelle feuille avec 4 cellules.
     * @param nw état de la cellule nord-ouest (0 ou 1)
     * @param ne état de la cellule nord-est (0 ou 1)
     * @param sw état de la cellule sud-ouest (0 ou 1)
     * @param se état de la cellule sud-est (0 ou 1)
     * @throws IllegalArgumentException si un état n'est pas 0 ou 1
     */
    public LeafNode(int nw, int ne, int sw, int se) {
        if (!isValidState(nw) || !isValidState(ne) || !isValidState(sw) || !isValidState(se)) {
            throw new IllegalArgumentException("Les états doivent être 0 ou 1. Reçu: nw=" + nw + 
                ", ne=" + ne + ", sw=" + sw + ", se=" + se);
        }
        this.nw = nw;
        this.ne = ne;
        this.sw = sw;
        this.se = se;
        this.cachedResult = null;
    }
    
    /**
     * Retourne l'état d'une cellule spécifique dans la grille 2×2
     * @param x coordonnée x (0 ou 1)
     * @param y coordonnée y (0 ou 1)
     * @return l'état de la cellule (0 ou 1)
     * @throws IllegalArgumentException si les coordonnées sont invalides
     */
    public int getCell(int x, int y) {
        if (x < 0 || x > 1 || y < 0 || y > 1) {
            throw new IllegalArgumentException(
                "Coordonnées invalides: x=" + x + ", y=" + y + 
                " (doivent être 0 ou 1)");
        }
        if (x == 0 && y == 0) return nw;
        if (x == 1 && y == 0) return ne;
        if (x == 0 && y == 1) return sw;
        if (x == 1 && y == 1) return se;
        
        return 0;
    }
    
    /**
     * Compte le nombre de voisins vivants d'une cellule dans cette feuille.
     * Attention : ne compte QUE les voisins qui sont dans la grille 2×2.
     * Les voisins hors de cette feuille ne sont pas comptés (ils sont gérés au niveau supérieur).
     * @param x coordonnée x de la cellule (0 ou 1)
     * @param y coordonnée y de la cellule (0 ou 1)
     * @return le nombre de voisins vivants dans cette feuille (0 à 8, mais max 3 en pratique)
     * @throws IllegalArgumentException si les coordonnées sont invalides
     */
    public int getCellNeighborCount(int x, int y) {
        if (x < 0 || x > 1 || y < 0 || y > 1) {
            throw new IllegalArgumentException(
                "Coordonnées invalides: x=" + x + ", y=" + y);
        }
        int count = 0;
        
        // Vérifier les 8 voisins potentiels
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                // Sauter la cellule elle-même
                if (dx == 0 && dy == 0) continue;
                
                int nx = x + dx;
                int ny = y + dy;
                
                // Vérifier si le voisin est dans la grille 2×2
                if (nx >= 0 && nx <= 1 && ny >= 0 && ny <= 1) {
                    count += getCell(nx, ny);
                }
            }
        }
        
        return count;
    }
    
    /**
     * Retourne un tableau avec l'état des 8 voisins d'une cellule.
     * L'ordre des voisins dans le tableau :
     * [NW, N, NE, W, E, SW, S, SE]
     * Si un voisin est hors de la grille 2×2, sa valeur est -1.
     * @param x coordonnée x de la cellule (0 ou 1)
     * @param y coordonnée y de la cellule (0 ou 1)
     * @return tableau de 8 entiers représentant les voisins (-1 si hors limites, 0 ou 1 sinon)
     */
    public int[] getNeighbors(int x, int y) {
        int[] neighbors = new int[8];
        int index = 0;
        
        // Parcourir les 8 directions
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                // Sauter la cellule elle-même
                if (dx == 0 && dy == 0) continue;
                
                int nx = x + dx;
                int ny = y + dy;
                
                // Si hors limites, mettre -1
                if (nx < 0 || nx > 1 || ny < 0 || ny > 1) {
                    neighbors[index++] = -1;
                } else {
                    neighbors[index++] = getCell(nx, ny);
                }
            }
        }
        
        return neighbors;
    }
    
    /**
     * Vérifie si un état de cellule est valide.
     * @param state l'état à vérifier
     * @return true si l'état est 0 ou 1, false sinon
     */
    private boolean isValidState(int state) {
        return (state == 0 || state == 1);
    }
    
    /**
     * Retourne le niveau de cette feuille.
     * @return toujours 0 (une feuille est au niveau le plus bas)
     */
    public int getLevel() {
        return 0;
    }
    
    /**
     * Compte le nombre total de cellules vivantes dans cette feuille.
     * @return la somme des 4 cellules (entre 0 et 4)
     */
    public long getPopulation() {
        return nw + ne + sw + se;
    }
    
    /**
     * Calcule l'état futur de cette feuille après un certain nombre de pas.
     * Note : Cette méthode sera implémentée par Personne 2 (algorithme HashLife).
     * Pour l'instant, elle lance une exception.
     * @param steps le nombre de générations à calculer (doit être 1 pour une feuille)
     * @return le nœud résultat (pas encore implémenté)
     * @throws IllegalArgumentException si steps n'est pas 1
     * @throws UnsupportedOperationException si getResult n'est pas encore implémenté
     */
    public Node getResult(int steps) {
        if (steps != 1) {
            throw new IllegalArgumentException(
                "LeafNode peut seulement calculer 1 pas. Reçu: " + steps);
        }
        
        if (cachedResult == null) {
            throw new UnsupportedOperationException(
                "getResult() pas encore implémenté.");
        }
        
        return cachedResult;
    }
    
    /**
     * Les feuilles n'ont pas d'enfants, retourne toujours null.
     * @return null
     */
    public Node getNW() {
        return null;
    }
    
    /**
     * Les feuilles n'ont pas d'enfants, retourne toujours null.
     * @return null
     */
    public Node getNE() {
        return null;
    }
    
    /**
     * Les feuilles n'ont pas d'enfants, retourne toujours null.
     * @return null
     */
    public Node getSW() {
        return null;
    }
    
    /**
     * Les feuilles n'ont pas d'enfants, retourne toujours null.
     * @return null
     */
    public Node getSE() {
        return null;
    }
    
    /**
     * Retourne l'état de la cellule nord-ouest.
     * @return l'état de la cellule (0 ou 1)
     */
    public int getNWCell() {
        return nw;
    }
    
    /**
     * Retourne l'état de la cellule nord-est.
     * @return l'état de la cellule (0 ou 1)
     */
    public int getNECell() { 
        return ne;
    }
    
    /**
     * Retourne l'état de la cellule sud-ouest.
     * @return l'état de la cellule (0 ou 1)
     */
    public int getSWCell() {
        return sw;
    }
    
    /**
     * Retourne l'état de la cellule sud-est.
     * @return l'état de la cellule (0 ou 1)
     */
    public int getSECell() {
        return se;
    }
    
    /**
     * Calcule le code de hachage basé sur les 4 cellules.
     * Deux feuilles avec les mêmes valeurs auront le même hashCode,
     * ce qui permet au HashCache de les identifier comme identiques.
     * @return le code de hachage
     */
    public int hashCode() {
        return Objects.hash(nw, ne, sw, se);
    }
    
    /**
     * Vérifie si cette feuille est égale à un autre objet.
     * Deux feuilles sont égales si leurs 4 cellules ont les mêmes valeurs.
     * @param obj l'objet à comparer
     * @return true si les feuilles ont les mêmes valeurs
     */
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        
        LeafNode other = (LeafNode) obj;
        return nw == other.nw && 
               ne == other.ne && 
               sw == other.sw && 
               se == other.se;
    }
    
    /**
     * Retourne une représentation textuelle de cette feuille.
     * @return la description de la feuille
     */
    public String toString() {
        return String.format("LeafNode[nw=%d, ne=%d, sw=%d, se=%d, pop=%d]",
                            nw, ne, sw, se, getPopulation());
    }
}
