// Règle spéciale, pas nécessaire pour notre Hashlife
package rules;

/**
 * Règle d'automate cellulaire « Naissance2/Survie2 » : naissance et survie toutes deux
 * déclenchées par exactement 2 voisins vivants.
 *
 * <p>Cette règle symétrique définit le comportement suivant :</p>
 * <ul>
 *   <li><b>Survie</b> : une cellule vivante reste en vie si et seulement si elle possède
 *       exactement 2 voisins vivants.</li>
 *   <li><b>Naissance</b> : une cellule morte devient vivante si elle possède exactement
 *       2 voisins vivants.</li>
 *   <li>Dans tous les autres cas, la cellule est morte à la génération suivante.</li>
 * </ul>
 *
 * <p><b>Note :</b> Cette règle est fournie à titre expérimental pour tester l'algorithme
 * HashLife avec différentes règles ; elle ne fait pas partie de la simulation principale.</p>
 *
 * <p>Exemple d'utilisation :</p>
 * <pre>{@code
 * Rules n2s2 = new Naissance2Survie2Rule();
 * n2s2.nextState(0, 2); // retourne 1 (naissance)
 * n2s2.nextState(1, 2); // retourne 1 (survie)
 * n2s2.nextState(1, 3); // retourne 0 (mort)
 * n2s2.nextState(0, 3); // retourne 0 (pas de naissance)
 * }</pre>
 *
 * @author Équipe HashLife
 * @version 1.0
 * @see Rules
 * @see HashLifeEngine
 */
public class Naissance2Survie2Rule implements Rules {

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
     * Applique la règle Naissance2/Survie2 : l'état suivant vaut {@code 1} si et seulement si
     * le nombre de voisins vivants est exactement {@code 2}, que la cellule soit vivante ou morte.
     *
     * @param currentState  l'état actuel de la cellule ({@code 0} = morte, {@code 1} = vivante)
     * @param neighborCount le nombre de voisins vivants (entre {@code 0} et {@code 8})
     * @return              {@code 1} si {@code neighborCount == 2}, {@code 0} sinon
     */
    @Override
    public int nextState(int currentState, int neighborCount) {
        if (currentState == 1) {
            return neighborCount == 2 ? 1 : 0; // Survie uniquement avec 2 voisins
        }
        return neighborCount == 2 ? 1 : 0; // Naissance uniquement avec 2 voisins
    }
}