package ui;

/**
 * Point d'entrée de l'application.
 *
 * <p>Lance la fenêtre principale dans le thread graphique (EDT)
 * comme recommandé par Swing.</p>
 */
public class Main {

    /**
     * Méthode principale: démarre l'interface graphique.
     *
     * @param args arguments de la ligne de commande (non utilisés)
     */
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            MainWindow window = new MainWindow();
            window.setVisible(true);
        });
    }
}