package ui;

import algorithms.CellularAutomaton;
import core.QuadTree;
import algorithms.HashLife;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.function.Consumer;

/**
 * Composant graphique qui affiche et gère la grille du Jeu de la Vie.
 *
 * Gère :
 * - le rendu des cellules (fond sombre, lignes de grille, cellules colorées)
 * - les interactions souris (clic, zoom, déplacement)
 * - la simulation (naïf ou HashLife)
 */
public class GridRenderer extends JPanel {

    private static final int SIZE = 64;

    private int[][] cells      = new int[SIZE][SIZE];
    private int     generation = 0;

    private CellularAutomaton automaton    = null;
    private Timer   simulationTimer;
    private boolean running        = false;
    private boolean isHashLifeMode = true;

    private int   cellSize = 8;
    private static final int MIN_CELL = 3;
    private static final int MAX_CELL = 30;
    private int   offsetX = 0, offsetY = 0;
    private Point dragStart = null;
    private int   dragOffsetX, dragOffsetY;

    // Couleurs 
    private static final Color BG_COLOR   = new Color(12, 12, 22);
    private static final Color GRID_COLOR = new Color(28, 28, 48);
    private static final Color CELL_A     = new Color(120, 80, 255);   
    private static final Color CELL_B     = new Color(200, 160, 255);  
    private static final Color BORDER_CLR = new Color(40, 40, 70);

    private Consumer<String> statusCallback;

    public GridRenderer() {
        setBackground(BG_COLOR);
        setPreferredSize(new Dimension(700, 580));
        setFocusable(true);
        initTimer();
        initMouseListeners();
        centerViewOnResize();
    }

    //Timer 

    private void initTimer() {
        simulationTimer = new Timer(100, e -> {
            if (isHashLifeMode) stepHashLife();
            else                stepNaif();
            repaint();
            fireStatus();
        });
    }

    //Souris 
    private void initMouseListeners() {
        MouseAdapter ma = new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isMiddleMouseButton(e)) {
                    dragStart   = e.getPoint();
                    dragOffsetX = offsetX;
                    dragOffsetY = offsetY;
                } else {
                    handleCellClick(e);
                }
            }
            @Override public void mouseDragged(MouseEvent e) {
                if (dragStart != null) {
                    offsetX = dragOffsetX + (e.getX() - dragStart.x);
                    offsetY = dragOffsetY + (e.getY() - dragStart.y);
                    repaint();
                } else {
                    handleCellClick(e);
                }
            }
            @Override public void mouseReleased(MouseEvent e)  { dragStart = null; }
            @Override public void mouseWheelMoved(MouseWheelEvent e) {
                int newSize = Math.max(MIN_CELL,
                             Math.min(MAX_CELL, cellSize - e.getWheelRotation()));
                double mx = (e.getX() - offsetX) / (double) cellSize;
                double my = (e.getY() - offsetY) / (double) cellSize;
                cellSize = newSize;
                offsetX  = (int)(e.getX() - mx * cellSize);
                offsetY  = (int)(e.getY() - my * cellSize);
                repaint();
            }
        };
        addMouseListener(ma);
        addMouseMotionListener(ma);
        addMouseWheelListener(ma);
    }

    private void handleCellClick(MouseEvent e) {
        int col = (e.getX() - offsetX) / cellSize;
        int row = (e.getY() - offsetY) / cellSize;
        if (row >= 0 && row < SIZE && col >= 0 && col < SIZE) {
            cells[row][col] = SwingUtilities.isLeftMouseButton(e) ? 1 : 0;
            automaton = null;
            repaint();
            fireStatus();
        }
    }

    private void centerViewOnResize() {
        addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                offsetX = (getWidth()  - SIZE * cellSize) / 2;
                offsetY = (getHeight() - SIZE * cellSize) / 2;
                repaint();
            }
        });
    }

    // Simulation 

    /** HashLife : macro-step naturel (plusieurs générations d'un coup). */
    private void stepHashLife() {
        ensureAutomaton();
        int macro = HashLife.getMacroStepFor(automaton.getCurrent().getRacine());
        automaton.run_hashlife(macro);
        generation += macro;
        pullFromQuadTree(automaton.getCurrent());
    }

    /** Naïf : exactement 1 génération. */
    private void stepNaif() {
        ensureAutomaton();
        automaton.step();
        generation++;
        pullFromQuadTree(automaton.getCurrent());
    }

    private void ensureAutomaton() {
        if (automaton == null)
            automaton = new CellularAutomaton(new QuadTree(cells));
    }

    private void pullFromQuadTree(QuadTree qt) {
        int qtSize  = qt.getTaille();
        int originX = (qtSize - SIZE) / 2;
        int originY = (qtSize - SIZE) / 2;
        for (int r = 0; r < SIZE; r++)
            for (int c = 0; c < SIZE; c++)
                cells[r][c] = qt.getCell(originX + c, originY + r);
    }

    //API publique 

    public void setMode(boolean isHashLife) {
        this.isHashLifeMode = isHashLife;
        reset();
    }

    public void start() {
        if (!running) { running = true; simulationTimer.start(); }
    }

    public void pause() {
        running = false;
        simulationTimer.stop();
    }

    public void reset() {
        pause();
        generation = 0;
        cells      = new int[SIZE][SIZE];
        automaton  = null;
        HashLife.resetCache();
        repaint();
        fireStatus();
    }

    /** Bouton Step : 1 génération naïve. Ignoré si simulation en cours. */
    public void stepOnce() {
        if (!running) { stepNaif(); repaint(); fireStatus(); }
    }

    public void randomize(double density) {
        reset();
        for (int r = 0; r < SIZE; r++)
            for (int c = 0; c < SIZE; c++)
                cells[r][c] = Math.random() < density ? 1 : 0;
        repaint();
        fireStatus();
    }

    public void loadGlider() {
        reset();
        int r = SIZE / 2, c = SIZE / 2;
        for (int[] p : new int[][]{{0,1},{1,2},{2,0},{2,1},{2,2}})
            cells[r + p[0]][c + p[1]] = 1;
        repaint();
        fireStatus();
    }

    public void setSpeed(int ms)  { simulationTimer.setDelay(ms); }
    public boolean isRunning()    { return running; }
    public int  getGeneration()   { return generation; }

    public int countAliveCells() {
        int n = 0;
        for (int[] row : cells) for (int v : row) n += v;
        return n;
    }

    public void setStatusCallback(Consumer<String> cb) { statusCallback = cb; }

    private void fireStatus() {
        if (statusCallback != null)
            statusCallback.accept(String.format(
                "Génération : %d  |  Vivantes : %d  |  Grille : %dx%d",
                generation, countAliveCells(), SIZE, SIZE));
    }

    //Rendu complet 

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

        // 1. Fond global
        g2.setColor(BG_COLOR);
        g2.fillRect(0, 0, getWidth(), getHeight());

        // 2. Fond de la zone de grille (légèrement différent)
        g2.setColor(new Color(16, 16, 30));
        g2.fillRect(offsetX, offsetY, SIZE * cellSize, SIZE * cellSize);

        // 3. Lignes de grille (visibles seulement si cellules assez grandes)
        if (cellSize >= 5) {
            g2.setColor(GRID_COLOR);
            for (int c = 0; c <= SIZE; c++)
                g2.drawLine(offsetX + c * cellSize, offsetY,
                            offsetX + c * cellSize, offsetY + SIZE * cellSize);
            for (int r = 0; r <= SIZE; r++)
                g2.drawLine(offsetX, offsetY + r * cellSize,
                            offsetX + SIZE * cellSize, offsetY + r * cellSize);
        }

        // 4. Cellules vivantes avec dégradé selon densité de voisinage
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (cells[r][c] == 1) {
                    float t = countNeighbors(r, c) / 8.0f;
                    g2.setColor(new Color(
                        lerp(CELL_A.getRed(),   CELL_B.getRed(),   t),
                        lerp(CELL_A.getGreen(), CELL_B.getGreen(), t),
                        lerp(CELL_A.getBlue(),  CELL_B.getBlue(),  t)));
                    int pad = cellSize > 5 ? 1 : 0;
                    g2.fillRect(offsetX + c * cellSize + pad,
                                offsetY + r * cellSize + pad,
                                cellSize - pad, cellSize - pad);
                }
            }
        }

        // 5. Bordure de la grille
        g2.setColor(BORDER_CLR);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRect(offsetX, offsetY, SIZE * cellSize, SIZE * cellSize);
    }

    private int countNeighbors(int r, int c) {
        int n = 0;
        for (int dr = -1; dr <= 1; dr++)
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;
                n += cells[(r + dr + SIZE) % SIZE][(c + dc + SIZE) % SIZE];
            }
        return n;
    }

    private static int lerp(int a, int b, float t) {
        return (int)(a + t * (b - a));
    }
}
