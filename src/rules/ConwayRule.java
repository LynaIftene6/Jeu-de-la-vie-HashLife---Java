package rules;

/**
 * Implémentation des règles classiques du Jeu de la Vie de Conway (B3/S23).
 *
 * <p>Cette règle définit le comportement standard d'un automate cellulaire binaire :</p>
 * <ul>
 *   <li><b>Survie</b> : une cellule vivante avec 2 ou 3 voisins vivants reste en vie.</li>
 *   <li><b>Mort par isolement</b> : une cellule vivante avec moins de 2 voisins meurt.</li>
 *   <li><b>Mort par surpopulation</b> : une cellule vivante avec plus de 3 voisins meurt.</li>
 *   <li><b>Naissance</b> : une cellule morte avec exactement 3 voisins vivants devient vivante.</li>
 * </ul>
 *
 * <p>Exemple d'utilisation :</p>
 * <pre>{@code
 * Rules conway = new ConwayRule();
 * int next = conway.nextState(1, 3); // retourne 1 (survie)
 * int born = conway.nextState(0, 3); // retourne 1 (naissance)
 * int dead = conway.nextState(1, 4); // retourne 0 (surpopulation)
 * }</pre>
 *
 * @author Équipe HashLife
 * @version 1.0
 * @see Rules
 * @see HashLifeEngine
 */
public class ConwayRule implements Rules {

    /**
     * {@inheritDoc}
     *
     * @return {@code 2} (cellules binaires : morte ou vivante)
     */
    @Override
    public int getStateCount() {
        return 2;
    }

    /**
     * Applique les règles de Conway pour déterminer l'état suivant d'une cellule.
     *
     * <table border="1">
     *   <caption>Transitions d'état</caption>
     *   <tr><th>État actuel</th><th>Voisins vivants</th><th>État suivant</th><th>Raison</th></tr>
     *   <tr><td>Vivant (1)</td><td>2 ou 3</td><td>1</td><td>Survie</td></tr>
     *   <tr><td>Vivant (1)</td><td>&lt; 2</td><td>0</td><td>Isolement</td></tr>
     *   <tr><td>Vivant (1)</td><td>&gt; 3</td><td>0</td><td>Surpopulation</td></tr>
     *   <tr><td>Mort (0)</td><td>3</td><td>1</td><td>Naissance</td></tr>
     *   <tr><td>Mort (0)</td><td>≠ 3</td><td>0</td><td>Reste morte</td></tr>
     * </table>
     *
     * @param currentState  l'état actuel de la cellule ({@code 0} = morte, {@code 1} = vivante)
     * @param liveNeighbors le nombre de voisins vivants (entre {@code 0} et {@code 8})
     * @return              {@code 1} si la cellule sera vivante, {@code 0} si elle sera morte
     */
    @Override
    public int nextState(int currentState, int liveNeighbors) {
        if (currentState == 1) {
            if (liveNeighbors == 2 || liveNeighbors == 3) {
                return 1; // survit
            } else {
                return 0; // isolement (< 2) ou surpopulation (> 3)
            }
        } else {
            return (liveNeighbors == 3) ? 1 : 0; // naissance uniquement avec 3 voisins
        }
    }
}