// Règle spéciale, pas nécessaire pour notre Hashlife
package rules;

/**
 * Règle d'automate cellulaire « Survie2 » : survie uniquement avec 2 voisins, sans naissance.
 *
 * <p>Cette règle est une variante minimaliste dans laquelle :</p>
 * <ul>
 *   <li><b>Survie</b> : une cellule vivante reste en vie <em>uniquement</em> si elle possède
 *       exactement 2 voisins vivants.</li>
 *   <li><b>Mort</b> : toute cellule vivante avec un nombre de voisins différent de 2 meurt.</li>
 *   <li><b>Pas de naissance</b> : une cellule morte reste toujours morte, quel que soit
 *       le nombre de voisins vivants.</li>
 * </ul>
 *
 * <p><b>Note :</b> Cette règle est fournie à titre expérimental pour tester l'algorithme
 * HashLife avec différentes règles ; elle ne fait pas partie de la simulation principale.</p>
 *
 * <p>Exemple d'utilisation :</p>
 * <pre>{@code
 * Rules survie2 = new Survie2Rule();
 * survie2.nextState(1, 2); // retourne 1 (survie)
 * survie2.nextState(1, 3); // retourne 0 (mort)
 * survie2.nextState(0, 2); // retourne 0 (pas de naissance)
 * }</pre>
 *
 * @author Équipe HashLife
 * @version 1.0
 * @see Rules
 * @see HashLifeEngine
 */
public class Survie2Rule implements Rules {

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
     * Applique la règle Survie2 pour déterminer l'état suivant d'une cellule.
     *
     * <ul>
     *   <li>Si la cellule est <b>vivante</b> et possède exactement {@code 2} voisins : elle survit ({@code 1}).</li>
     *   <li>Si la cellule est <b>vivante</b> et possède un autre nombre de voisins : elle meurt ({@code 0}).</li>
     *   <li>Si la cellule est <b>morte</b> : elle reste morte ({@code 0}), sans exception.</li>
     * </ul>
     *
     * @param currentState  l'état actuel de la cellule ({@code 0} = morte, {@code 1} = vivante)
     * @param neighborCount le nombre de voisins vivants (entre {@code 0} et {@code 8})
     * @return              {@code 1} si la cellule survit, {@code 0} sinon
     */
    @Override
    public int nextState(int currentState, int neighborCount) {
        if (currentState == 1) {
            return neighborCount == 2 ? 1 : 0;
        }
        return 0; // Pas de naissance dans tous les cas
    }
}