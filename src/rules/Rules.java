package rules;

/**
 * Interface définissant le contrat de toute règle d'automate cellulaire.
 *
 * <p>Chaque implémentation de cette interface encapsule la logique de transition
 * d'état propre à une règle particulière (ex. Conway, Survie2, etc.).
 * Elle est utilisée par {@link HashLifeEngine} pour calculer les générations suivantes.</p>
 *
 * @author Équipe HashLife
 * @version 1.0
 * @see ConwayRule
 * @see Survie2Rule
 * @see Naissance2Survie2Rule
 */
public interface Rules {

    /**
     * Retourne le nombre d'états distincts que peut prendre une cellule.
     *
     * <p>Pour un automate binaire classique (vivant/mort), cette méthode retourne {@code 2}.</p>
     *
     * @return le nombre d'états possibles (généralement {@code 2})
     */
    int getStateCount();

    /**
     * Calcule l'état suivant d'une cellule en fonction de son état courant
     * et du nombre de ses voisins vivants.
     *
     * @param currentState  l'état actuel de la cellule ({@code 0} = morte, {@code 1} = vivante)
     * @param neighborCount le nombre de voisins vivants (entre {@code 0} et {@code 8} pour une grille 2D)
     * @return              l'état de la cellule à la prochaine génération ({@code 0} ou {@code 1})
     */
    int nextState(int currentState, int neighborCount);

    /**
     * Génère une clé unique identifiant cette règle, utilisée par {@code HashCache}
     * pour mémoriser les résultats par règle.
     *
     * <p>L'implémentation par défaut retourne le nom complet de la classe
     * (ex. {@code "rules.ConwayRule"}), ce qui suffit dans la plupart des cas.</p>
     *
     * @return une chaîne unique identifiant la règle
     */
    default String cacheKey() {
        return getClass().getName();
    }
}