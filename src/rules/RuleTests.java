package rules;

import core.HashCache;
import core.LeafNode;
import core.Node;
import rules.HashLifeEngine;

/**
 * Classe de tests manuels couvrant l'ensemble du package {@code rules}.
 *
 * <p>Cette classe regroupe trois catégories de tests exécutés séquentiellement via
 * la méthode {@link #main(String[])} :</p>
 * <ol>
 *   <li>Tests unitaires de {@code nextState()} pour toutes les règles disponibles.</li>
 *   <li>Tests de la logique de transition sur des feuilles 2×2 ({@link LeafNode}).</li>
 *   <li>Tests d'intégration de {@link HashLifeEngine#nextGeneration(Node, HashCache, Rules)}.</li>
 * </ol>
 *
 * <p>En cas d'échec, une {@link AssertionError} est levée avec un message décrivant
 * la valeur attendue et la valeur obtenue.</p>
 *
 * @author Équipe HashLife
 * @version 1.0
 * @see ConwayRule
 * @see Survie2Rule
 * @see Naissance2Survie2Rule
 * @see HashLifeEngine
 */
public class RuleTests {

    /**
     * Point d'entrée principal : exécute l'ensemble des tests et affiche un résumé.
     *
     * @param args arguments de la ligne de commande (non utilisés)
     */
    public static void main(String[] args) {
        HashCache cache = new HashCache();

        System.out.println("TESTS RÈGLES UNITAIRES");
        testAllRulesUnit();

        System.out.println("\n TESTS FEUILLES 2x2");
        testLeafNode2x2(cache);

        System.out.println("\n TESTS nextGeneration");
        testNextGeneration(cache);

        System.out.println("\n TOUS LES TESTS PASSENT !");
    }

    // 1. Tests unitaires nextState()

    /**
     * Teste exhaustivement la méthode {@code nextState()} pour toutes les règles :
     * {@link ConwayRule}, {@link Naissance2Survie2Rule} et {@link Survie2Rule}.
     *
     * <p>Couvre les cas de survie, naissance, isolement et surpopulation.</p>
     */
    private static void testAllRulesUnit() {
        Rules conway   = new ConwayRule();
        Rules highLife = new Naissance2Survie2Rule();
        Rules survie2  = new Survie2Rule();

        // --- ConwayRule (0-8 voisins) ---
        assertEquals(conway.nextState(0, 0), 0, "Conway mort 0v -> mort");
        assertEquals(conway.nextState(0, 3), 1, "Conway mort 3v -> naissance");
        assertEquals(conway.nextState(1, 2), 1, "Conway 2v -> survie");
        assertEquals(conway.nextState(1, 3), 1, "Conway 3v -> survie");
        assertEquals(conway.nextState(1, 1), 0, "Conway 1v -> isolement");
        assertEquals(conway.nextState(1, 4), 0, "Conway 4v -> surpopulation");

        // --- Naissance2Survie2Rule ---
        assertEquals(highLife.nextState(0, 2), 1, "HighLife mort 2v -> naissance");
        assertEquals(highLife.nextState(1, 2), 1, "HighLife 2v -> survie");

        // --- Survie2Rule ---
        assertEquals(survie2.nextState(1, 2), 1, "Survie2 2v -> survie");
        assertEquals(survie2.nextState(1, 3), 0, "Survie2 3v -> mort");

        System.out.println("Unit tests OK");
    }

    // 2. Tests sur les feuilles 2x2

    /**
     * Teste la méthode {@link HashLifeEngine#nextLeaf(LeafNode, HashCache, Rules)}
     * sur différentes configurations de feuilles 2×2 avec la règle de Conway.
     *
     * <p>Scénarios couverts :</p>
     * <ul>
     *   <li>3 cellules vivantes → survie + naissance (population attendue : 4)</li>
     *   <li>Cellule isolée → extinction (population attendue : 0)</li>
     *   <li>3 voisines autour de SW → naissance (population attendue : 4)</li>
     *   <li>4 cellules vivantes → état stable (population attendue : 4)</li>
     *   <li>Ligne droite NW+SW → extinction (population attendue : 0)</li>
     * </ul>
     *
     * @param cache le cache partagé entre les tests pour éviter les recréations inutiles
     */
    private static void testLeafNode2x2(HashCache cache) {
        Rules conway = new ConwayRule();

        // Test 1 : 3 vivantes en ligne → survie + naissance
        LeafNode test1 = cache.getOrCreateLeaf(1, 1, 1, 0);
        LeafNode res1  = HashLifeEngine.nextLeaf(test1, cache, conway);
        assertEquals(4, res1.getPopulation(), "3 vivantes -> 4 (survie + naissance)");

        // Test 2 : cellule isolée → extinction
        LeafNode test2 = cache.getOrCreateLeaf(0, 0, 0, 1);
        LeafNode res2  = HashLifeEngine.nextLeaf(test2, cache, conway);
        assertEquals(0, res2.getPopulation(), "Isolée -> extinction");

        // Test 3 : naissance grâce à 3 voisines autour de SW
        LeafNode test3 = cache.getOrCreateLeaf(1, 1, 0, 1);
        LeafNode res3  = HashLifeEngine.nextLeaf(test3, cache, conway);
        assertEquals(4, res3.getPopulation(), "3 voisines -> naissance SW");

        Rules cnonway = new ConwayRule();
        System.out.println("Conway(1,3): " + cnonway.nextState(1, 3)); // DOIT = 1
        System.out.println("Conway(1,4): " + cnonway.nextState(1, 4)); // DOIT = 0

        // Test 4 : 4 cellules vivantes → état stable
        LeafNode test4 = cache.getOrCreateLeaf(1, 1, 1, 1);
        LeafNode res4  = HashLifeEngine.nextLeaf(test4, cache, conway);
        assertEquals(4, res4.getPopulation(), "4 vivantes dans 2x2 alors etat stable");

        // Test 5 : ligne droite NW+SW → extinction
        LeafNode test5 = cache.getOrCreateLeaf(1, 0, 1, 0);
        LeafNode res5  = HashLifeEngine.nextLeaf(test5, cache, conway);
        assertEquals(0, res5.getPopulation(), "Ligne droite -> extinction");

        System.out.println("tests extinction OK !");
        System.out.println("LeafNode 2x2 OK (Lets Gooooooo)");
    }

    // 3. Tests nextGeneration

    /**
     * Teste la méthode {@link HashLifeEngine#nextGeneration(Node, HashCache, Rules)}
     * sur une feuille simple avec la règle de Conway.
     *
     * <p>Vérifie que 3 cellules vivantes produisent une population de 4 à la génération
     * suivante (survie + naissance).</p>
     *
     * @param cache le cache partagé utilisé pour la mémoïsation
     */
    private static void testNextGeneration(HashCache cache) {
        Rules conway = new ConwayRule();

        LeafNode testLeaf = cache.getOrCreateLeaf(1, 1, 1, 0);
        Node nextNode = HashLifeEngine.nextGeneration(testLeaf, cache, conway);
        assertEquals(4, ((LeafNode) nextNode).getPopulation(), "nextGeneration: 3 vivantes -> 4");

        System.out.println("nextGeneration OK");
    }

    // Utilitaires d'assertion

    /**
     * Vérifie l'égalité entre deux objets et lève une {@link AssertionError} en cas d'échec.
     *
     * @param expected valeur attendue
     * @param actual   valeur obtenue
     * @param message  description du test (affichée en cas d'échec)
     * @throws AssertionError si {@code expected} et {@code actual} ne sont pas égaux
     */
    private static void assertEquals(Object expected, Object actual, String message) {
        if (!expected.equals(actual))
            throw new AssertionError("NOP " + message + "\nAttendu: " + expected + " Obtenu: " + actual);
    }

    /**
     * Vérifie l'égalité entre deux entiers {@code int} et lève une {@link AssertionError}
     * en cas d'échec.
     *
     * @param expected valeur entière attendue
     * @param actual   valeur entière obtenue
     * @param message  description du test (affichée en cas d'échec)
     * @throws AssertionError si les deux valeurs diffèrent
     */
    private static void assertEquals(int expected, int actual, String message) {
        if (expected != actual)
            throw new AssertionError("NOP " + message + "\nAttendu: " + expected + " Obtenu: " + actual);
    }

    /**
     * Vérifie l'égalité entre un {@code int} et un {@code long} et lève une
     * {@link AssertionError} en cas d'échec.
     *
     * <p>Utilisé notamment pour comparer une valeur de population retournée en {@code long}
     * avec une constante entière.</p>
     *
     * @param expected valeur entière attendue (promue en {@code long} pour la comparaison)
     * @param actual   valeur {@code long} obtenue
     * @param message  description du test (affichée en cas d'échec)
     * @throws AssertionError si les deux valeurs diffèrent
     */
    private static void assertEquals(int expected, long actual, String message) {
        if ((long) expected != actual)
            throw new AssertionError("NOP " + message + "\nAttendu: " + expected + " Obtenu: " + actual);
    }
}