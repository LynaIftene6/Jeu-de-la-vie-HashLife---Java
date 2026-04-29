package core;


public class QuickTest {
    
    public static void main(String[] args) {
        System.out.println("Tests rapides LeafNode");
        testCreationBasique();
        testPopulation();
        testEgalite();
        testExceptions();
        testInternalNode();
        testHashCache();
        testQuadTree();
        System.out.println("\n Tous les tests passent");
    }
    
    private static void testCreationBasique() {
        System.out.println("\nTest création basique");
        LeafNode empty = new LeafNode(0, 0, 0, 0);
        LeafNode full  = new LeafNode(1, 1, 1, 1);
        LeafNode mixed = new LeafNode(1, 0, 1, 0);
        assert empty.getLevel() == 0 : "Le niveau devrait être 0";
        assert full.getLevel()  == 0 : "Le niveau devrait être 0";
        System.out.println("Création de feuilles OK");
        System.out.println("Niveau = " + empty.getLevel());
    }
    
    private static void testPopulation() {
        System.out.println("\nTest population");
        LeafNode empty = new LeafNode(0, 0, 0, 0);
        LeafNode full  = new LeafNode(1, 1, 1, 1);
        LeafNode mixed = new LeafNode(1, 0, 1, 0);
        assert empty.getPopulation() == 0 : "Population vide devrait être 0";
        assert full.getPopulation()  == 4 : "Population pleine devrait être 4";
        assert mixed.getPopulation() == 2 : "Population mixte devrait être 2";
        System.out.println("Population vide = "   + empty.getPopulation());
        System.out.println("Population pleine = " + full.getPopulation());
        System.out.println("Population mixte = "  + mixed.getPopulation());
    }
    
    private static void testEgalite() {
        System.out.println("\nTest égalité");
        LeafNode leaf1 = new LeafNode(1, 0, 1, 0);
        LeafNode leaf2 = new LeafNode(1, 0, 1, 0);
        LeafNode leaf3 = new LeafNode(0, 1, 0, 1);
        assert  leaf1.equals(leaf2) : "Feuilles identiques devraient être égales";
        assert !leaf1.equals(leaf3) : "Feuilles différentes ne devraient pas être égales";
        assert  leaf1.hashCode() == leaf2.hashCode() : "HashCodes devraient être égaux";
        System.out.println("Égalité fonctionne");
        System.out.println("HashCode cohérent");
    }
    
    private static void testExceptions() {
        System.out.println("\nTest exceptions");
        try {
            new LeafNode(2, 0, 0, 0);
            assert false : "Devrait lever IllegalArgumentException";
        } catch (IllegalArgumentException e) {
            System.out.println("Exception état invalide OK");
        }
        LeafNode leaf = new LeafNode(1, 0, 1, 0);
        try {
            leaf.getResult(1);
            assert false : "Devrait lever UnsupportedOperationException";
        } catch (UnsupportedOperationException e) {
            System.out.println("Exception getResult non implémenté OK");
        }
        try {
            leaf.getResult(2);
            assert false : "Devrait lever IllegalArgumentException";
        } catch (IllegalArgumentException e) {
            System.out.println("Exception steps invalide OK\n");
        }
    }

    private static void testInternalNode() {
        System.out.println("Test InternalNode");
        LeafNode nw = new LeafNode(1, 0, 0, 0);
        LeafNode ne = new LeafNode(0, 1, 0, 0);
        LeafNode sw = new LeafNode(0, 0, 1, 0);
        LeafNode se = new LeafNode(0, 0, 0, 1);
        InternalNode internal = new InternalNode(nw, ne, sw, se);
        assert internal.getLevel()      == 1 : "Niveau devrait être 1";
        assert internal.getPopulation() == 4 : "Population devrait être 4";
        System.out.println("Création InternalNode OK");
        System.out.println("Niveau = "     + internal.getLevel());
        System.out.println("Population = " + internal.getPopulation());
        assert internal.getNW() == nw : "getNW devrait retourner nw";
        assert internal.getNE() == ne : "getNE devrait retourner ne";
        System.out.println("Accès aux enfants OK");
    }

    private static void testHashCache() {
        System.out.println("\nTest HashCache");
        HashCache cache = new HashCache();

        System.out.println("\nTest création feuilles");
        LeafNode leaf1 = cache.getOrCreateLeaf(1, 0, 1, 0);
        assert leaf1 != null         : "Feuille devrait être créée";
        assert cache.getMisses() == 1 : "Devrait avoir 1 création";
        assert cache.getHits()   == 0 : "Devrait avoir 0 trouvaille";

        LeafNode leaf2 = cache.getOrCreateLeaf(1, 0, 1, 0);
        assert leaf1 == leaf2        : "Devrait être le MÊME objet (même adresse)";
        assert cache.getMisses() == 1 : "Toujours 1 création";
        assert cache.getHits()   == 1 : "Devrait avoir 1 trouvaille";
        System.out.println("Feuille créée une fois");
        System.out.println("Feuille réutilisée : " + (leaf1 == leaf2));

        LeafNode leaf3 = cache.getOrCreateLeaf(0, 1, 0, 1);
        assert leaf1 != leaf3        : "Feuilles différentes doivent être des objets différents";
        assert cache.getMisses() == 2 : "Devrait avoir 2 créations";
        System.out.println("Feuille différente créée");

        System.out.println("\nTest création nœuds internes");
        LeafNode a = cache.getOrCreateLeaf(1, 0, 0, 0);
        LeafNode b = cache.getOrCreateLeaf(0, 1, 0, 0);
        LeafNode c = cache.getOrCreateLeaf(0, 0, 1, 0);
        LeafNode d = cache.getOrCreateLeaf(0, 0, 0, 1);

        InternalNode internal1 = cache.getOrCreateInternal(a, b, c, d);
        assert internal1 != null : "Nœud interne devrait être créé";

        InternalNode internal2 = cache.getOrCreateInternal(a, b, c, d);
        assert internal1 == internal2 : "Devrait être le MÊME objet";
        System.out.println("Nœud interne créé et réutilisé");

        InternalNode internal3 = cache.getOrCreateInternal(b, a, d, c);
        assert internal1 != internal3 : "Nœuds avec enfants différents";
        System.out.println("Nœud interne différent créé");

        System.out.println("\nTest statistiques");
        System.out.println("Taille cache: "  + cache.getCacheSize() + " nœuds uniques");
        System.out.println("Créations: "     + cache.getMisses());
        System.out.println("Trouvailles: "   + cache.getHits());

        System.out.println("\nTest vider cache");
        cache.clear();
        assert cache.getCacheSize() == 0 : "Cache devrait être vide";
        assert cache.getMisses()    == 0 : "Compteurs remis à zéro";
        assert cache.getHits()      == 0 : "Compteurs remis à zéro";
        System.out.println("Cache vidé");
    }

    private static void testQuadTree() {
        System.out.println("=== Test grille 8x8 ===");
        int[][] pattern = {
            {1,0,1,0,0,0,0,0},
            {0,1,1,1,0,0,1,0},
            {0,0,0,0,1,1,1,1},
            {0,0,0,0,0,0,0,0},
            {0,1,0,0,0,0,0,0},
            {0,0,0,0,1,1,0,0},
            {1,1,0,0,0,0,0,0},
            {0,0,0,0,0,0,0,1}
        };
        QuadTree qt = new QuadTree(pattern);
        assert qt.getCache().getCacheSize() > 0  : "le cache devrait avoir des entrees";
        assert qt.getRacine().getLevel()      == 2  : "niveau devrait etre 2";
        assert qt.getRacine().getPopulation() == 16 : "population devrait etre 16";
        System.out.println("QuadTree cree OK");
        System.out.println("Niveau racine = " + qt.getRacine().getLevel());
        System.out.println("Population = "    + qt.getRacine().getPopulation());
        System.out.println("Cache = "         + qt.getCache().getCacheSize() + " noeuds uniques");
        qt.afficherEtapesQuadrants();

        System.out.println("=== Test grille 16x16 ===");
        int[][] grandeGrille = {
            {0,0,1,0,0,0,0,0,1,0,0,0,0,0,0,0},
            {1,0,1,1,0,0,1,1,1,0,0,1,0,0,1,0},
            {1,1,1,0,1,0,1,0,1,1,0,0,0,0,1,0},
            {0,0,1,1,1,1,0,1,1,0,1,0,0,0,0,1},
            {1,1,0,1,0,0,0,1,1,1,0,0,1,0,1,1},
            {1,0,1,0,0,1,1,1,1,0,0,1,0,0,0,0},
            {0,1,0,1,1,1,1,0,0,1,1,0,1,1,0,1},
            {0,1,0,0,1,0,0,1,0,0,1,1,0,0,1,1},
            {0,0,0,0,0,1,0,0,0,1,0,1,1,0,0,1},
            {1,1,1,1,0,0,0,0,1,0,0,0,0,0,0,0},
            {0,0,1,0,0,1,1,0,0,1,0,1,1,0,0,0},
            {1,1,1,1,1,0,0,0,1,1,0,0,0,0,1,0},
            {1,0,1,1,0,0,1,0,0,0,0,0,0,1,1,1},
            {0,1,0,0,1,0,1,1,1,1,1,1,0,0,1,0},
            {0,0,1,0,0,1,0,0,0,0,0,0,0,1,0,0},
            {0,0,1,1,1,0,1,0,1,1,0,1,1,1,0,0}
        };
        QuadTree qtt = new QuadTree(grandeGrille);
        assert qtt.getRacine().getLevel()      == 3   : "niveau devrait etre 3";
        assert qtt.getRacine().getPopulation() == 111 : "population devrait etre 111";
        qtt.afficherEtapesQuadrants();
    }
}
