package ui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Fenêtre principale de l'application.
 *
 * <p>Assemble les trois composants principaux :
 * <ul>
 *   <li>{@link GridRenderer} : la grille de simulation au centre</li>
 *   <li>{@link ControlPanel} : les boutons de contrôle à droite</li>
 *   <li>Une barre de menu pour choisir le mode d'algorithme</li>
 * </ul>
 */
public class MainWindow extends JFrame {

    private GridRenderer gridRenderer;
    private ControlPanel controlPanel;

    /**
     * Construit et configure la fenêtre principale.
     */
    public MainWindow() {
        super("Jeu de la Vie - HashLife");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(0, 0));
        setJMenuBar(buildMenuBar());
        getContentPane().setBackground(new Color(18, 18, 28));
        add(buildTitleBar(), BorderLayout.NORTH);
        gridRenderer = new GridRenderer();
        add(gridRenderer, BorderLayout.CENTER);
        controlPanel = new ControlPanel(gridRenderer);
        add(controlPanel, BorderLayout.EAST);
        add(buildStatusBar(), BorderLayout.SOUTH);
        gridRenderer.setStatusCallback(controlPanel::updateStatus);
        pack();
        setMinimumSize(new Dimension(900, 620));
        setLocationRelativeTo(null);
    }

    /**
     * Construit la barre de menu avec le choix du mode (Itératif / HashLife).
     *
     * @return la barre de menu configurée
     */
    private JMenuBar buildMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(new Color(13, 13, 20));
        menuBar.setBorder(BorderFactory.createEmptyBorder());

        JMenu menuMode = new JMenu("Mode");
        menuMode.setForeground(new Color(160, 140, 255));
        menuMode.setFont(new Font("SansSerif", Font.BOLD, 12));

        JRadioButtonMenuItem iterativeItem = new JRadioButtonMenuItem("Version Itérative");
        JRadioButtonMenuItem hashlifeItem  = new JRadioButtonMenuItem("Version HashLife");

        ButtonGroup group = new ButtonGroup();
        group.add(iterativeItem);
        group.add(hashlifeItem);
        hashlifeItem.setSelected(true);

        iterativeItem.addActionListener(e -> switchToIterative());
        hashlifeItem .addActionListener(e -> switchToHashLife());

        menuMode.add(iterativeItem);
        menuMode.add(hashlifeItem);
        menuBar.add(menuMode);

        return menuBar;
    }

    /**
     * Construit la barre de titre en haut de la fenêtre.
     *
     * @return le panneau titre
     */
    private JPanel buildTitleBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(new Color(13, 13, 20));
        bar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(80, 80, 160)));
        bar.setPreferredSize(new Dimension(0, 42));

        JLabel title = new JLabel("  ◈  JEU DE LA VIE  —  HashLife Simulator");
        title.setFont(new Font("Monospaced", Font.BOLD, 13));
        title.setForeground(new Color(160, 140, 255));
        bar.add(title, BorderLayout.WEST);

        return bar;
    }

    /**
     * Construit la barre de statut en bas avec les raccourcis souris.
     *
     * @return le panneau de statut
     */
    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        bar.setBackground(new Color(13, 13, 20));
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(50, 50, 80)));

        JLabel hint = new JLabel("Clic gauche : activer  |  Clic droit : effacer  |  Molette : zoom  |  Milieu : déplacer");
        hint.setFont(new Font("Monospaced", Font.PLAIN, 10));
        hint.setForeground(new Color(70, 70, 100));
        bar.add(hint);

        return bar;
    }

    /**
     * Passe en mode Itératif : réinitialise la grille et affiche le bouton Step.
     */
    private void switchToIterative() {
        gridRenderer.setMode(false);
        controlPanel.setHashLifeMode(false);
        JOptionPane.showMessageDialog(this,
            "Mode Itératif activé\nLe bouton Step avance d'une génération exacte.",
            "Mode", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Passe en mode HashLife : réinitialise la grille et cache le bouton Step.
     */
    private void switchToHashLife() {
        gridRenderer.setMode(true);
        controlPanel.setHashLifeMode(true);
        JOptionPane.showMessageDialog(this,
            "Mode HashLife activé\nStart calcule plusieurs générations par tick.",
            "Mode", JOptionPane.INFORMATION_MESSAGE);
    }
}