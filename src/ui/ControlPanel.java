package ui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Panneau de contrôle de l'application.
 *
 * Permet de gérer la simulation :
 * start, pause, reset, step (mode naïf),
 * génération de patterns et benchmark.
 *
 * Le benchmark compare les performances
 * entre l’algorithme naïf et HashLife.
 */
public class ControlPanel extends JPanel {

    private final GridRenderer grid;

    private JButton btnStart;
    private JButton btnPause;
    private JButton btnReset;
    private JButton btnStep;
    private JButton btnBenchmark;

    private JLabel statusLabel;
    private boolean hashLifeMode = true;

    private static final Color BG       = new Color(20, 20, 35);
    private static final Color PANEL_BG = new Color(26, 26, 44);
    private static final Color ACCENT   = new Color(120, 80, 255);
    private static final Color ACCENT2  = new Color(60, 200, 140);
    private static final Color DANGER   = new Color(220, 60, 80);
    private static final Color TEXT     = new Color(210, 210, 230);

    /**
     * Constructeur du panneau.
     *
     * @param grid la grille associée
     */
    public ControlPanel(GridRenderer grid) {
        this.grid = grid;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(BG);
        setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(50, 50, 80)));
        setPreferredSize(new Dimension(210, 0));

        statusLabel = new JLabel("État : Arrêté");
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        statusLabel.setForeground(DANGER);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        statusLabel.setBorder(new EmptyBorder(6, 10, 6, 10));

        add(Box.createVerticalStrut(14));
        add(buildSection("  SIMULATION", buildSimulationPanel()));
        add(Box.createVerticalStrut(6));
        add(buildSection("  PATTERNS", buildRandomPanel()));
        add(Box.createVerticalStrut(6));
        add(buildSection("  BENCHMARK", buildBenchmarkPanel()));
        add(Box.createVerticalStrut(8));
        add(statusLabel);
        add(buildFooter());
    }

    /**
     * Active ou non le mode HashLife.
     * Cache le bouton Step si actif.
     */
    public void setHashLifeMode(boolean isHashLife) {
        this.hashLifeMode = isHashLife;
        btnStep.setVisible(!isHashLife);
        revalidate();
        repaint();
    }

    private JPanel buildSimulationPanel() {
        JPanel p = darkPanel();
        p.setLayout(new GridLayout(2, 2, 6, 6));
        p.setBorder(new EmptyBorder(10, 10, 10, 10));

        btnStart = makeButton("Start", ACCENT2);
        btnPause = makeButton("Pause", new Color(200, 160, 60));
        btnReset = makeButton("Reset", DANGER);
        btnStep  = makeButton("Step", ACCENT);

        btnStart.addActionListener(e -> onStart());
        btnPause.addActionListener(e -> onPause());
        btnReset.addActionListener(e -> onReset());
        btnStep.addActionListener(e -> { grid.stepOnce(); refreshStats(); });

        btnPause.setEnabled(false);
        btnStep.setVisible(!hashLifeMode);

        p.add(btnStart);
        p.add(btnPause);
        p.add(btnReset);
        p.add(btnStep);

        return p;
    }

    private JPanel buildRandomPanel() {
        JPanel p = darkPanel();
        p.setLayout(new GridLayout(1, 2, 6, 6));
        p.setBorder(new EmptyBorder(10, 10, 10, 10));

        JButton btnGlider = makeButton("Glider", ACCENT);
        btnGlider.addActionListener(e -> grid.loadGlider());

        JButton btnRandom = makeButton("Random", ACCENT2);
        btnRandom.addActionListener(e -> grid.randomize(0.30));

        p.add(btnGlider);
        p.add(btnRandom);

        return p;
    }

    private JPanel buildBenchmarkPanel() {
        JPanel p = darkPanel();
        p.setLayout(new BorderLayout(0, 6));
        p.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel info = new JLabel("<html><center>Compare Naïf vs HashLife<br>sur 10 générations</center></html>");
        info.setFont(new Font("SansSerif", Font.PLAIN, 10));
        info.setForeground(new Color(150, 150, 180));
        info.setHorizontalAlignment(SwingConstants.CENTER);

        btnBenchmark = makeButton("Lancer", new Color(180, 100, 40));
        btnBenchmark.addActionListener(e -> runBenchmark());

        p.add(info, BorderLayout.NORTH);
        p.add(Box.createVerticalStrut(6), BorderLayout.CENTER);
        p.add(btnBenchmark, BorderLayout.SOUTH);

        return p;
    }

    private void runBenchmark() {
        boolean wasRunning = grid.isRunning();
        if (wasRunning) grid.pause();

        btnBenchmark.setEnabled(false);
        btnBenchmark.setText("...");

        new Thread(() -> {
            try {
                String result = BenchmarkTestUI.run();

                SwingUtilities.invokeLater(() -> {
                    showBenchmarkResult(result);
                    btnBenchmark.setEnabled(true);
                    btnBenchmark.setText("Lancer");
                    if (wasRunning) grid.start();
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this,
                        "Erreur : " + ex.getMessage(),
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                    btnBenchmark.setEnabled(true);
                    btnBenchmark.setText("Lancer");
                });
            }
        }).start();
    }

    private void showBenchmarkResult(String result) {
        JTextArea area = new JTextArea(result);
        area.setFont(new Font("Monospaced", Font.PLAIN, 12));
        area.setEditable(false);
        area.setBackground(new Color(20, 20, 35));
        area.setForeground(new Color(200, 200, 220));
        area.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(380, 180));
        scroll.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 160)));

        JOptionPane.showMessageDialog(this, scroll,
            "Résultats du Benchmark", JOptionPane.INFORMATION_MESSAGE);
    }

    private void onStart() {
        grid.start();
        String mode = hashLifeMode ? "HashLife" : "Naïf";
        statusLabel.setText("En cours [" + mode + "]");
        statusLabel.setForeground(new Color(60, 200, 140));
        btnStart.setEnabled(false);
        btnPause.setEnabled(true);
    }

    private void onPause() {
        grid.pause();
        statusLabel.setText("Pausé");
        statusLabel.setForeground(new Color(200, 160, 60));
        btnStart.setEnabled(true);
        btnPause.setEnabled(false);
        refreshStats();
    }

    private void onReset() {
        grid.reset();
        statusLabel.setText("Arrêté");
        statusLabel.setForeground(DANGER);
        btnStart.setEnabled(true);
        btnPause.setEnabled(false);
        refreshStats();
    }

    private void refreshStats() {
        statusLabel.setText("Gén : " + grid.getGeneration()
            + "  |  Vivantes : " + grid.countAliveCells());
        statusLabel.setForeground(TEXT);
    }

    public void updateStatus(String msg) {
        SwingUtilities.invokeLater(this::refreshStats);
    }

    private JPanel darkPanel() {
        JPanel p = new JPanel();
        p.setBackground(PANEL_BG);
        p.setMaximumSize(new Dimension(210, 300));
        p.setBorder(BorderFactory.createLineBorder(new Color(40, 40, 70), 1));
        return p;
    }

    private JButton makeButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 11));
        btn.setForeground(Color.WHITE);
        btn.setBackground(color.darker());
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);

        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(color); }
            @Override public void mouseExited(MouseEvent e)  { btn.setBackground(color.darker()); }
        });

        return btn;
    }

    private JPanel buildFooter() {
        JPanel p = new JPanel();
        p.setBackground(BG);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(210, 40));
        return p;
    }

    private JPanel buildSection(String title, JPanel content) {
        JPanel section = new JPanel(new BorderLayout());
        section.setBackground(PANEL_BG);
        section.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        lbl.setForeground(TEXT);
        lbl.setBorder(new EmptyBorder(6, 6, 6, 6));

        section.add(lbl, BorderLayout.NORTH);
        section.add(content, BorderLayout.CENTER);

        return section;
    }
}
