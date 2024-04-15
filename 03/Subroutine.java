import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;

/**
 * A panel that displays a two-dimensional animation using hierarchical modeling.
 * Includes a checkbox to toggle animation.
 */
public class SubroutineHierarchy extends JPanel {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final double X_LEFT = -4;
    private static final double X_RIGHT = 4;
    private static final double Y_BOTTOM = -3;
    private static final double Y_TOP = 3;
    private static final Color BACKGROUND_COLOR = Color.WHITE;
    private static final int TIMER_DELAY = 17; // approx. 60 FPS
    private float pixelSize;

    private int frameNumber = 0;
    private Timer animationTimer;
    private JPanel display;

    public static void main(String[] args) {
        JFrame window = new JFrame("Subroutine Hierarchy");
        window.setContentPane(new SubroutineHierarchy());
        window.pack();
        window.setLocation(100, 60);
        window.setResizable(false);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setVisible(true);
    }

    public SubroutineHierarchy() {
        setupUI();
        configureAnimationTimer();
    }

    private void setupUI() {
        display = createDisplayPanel();
        display.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        display.setBackground(BACKGROUND_COLOR);

        JCheckBox animationCheck = createAnimationCheckbox();
        JPanel topPanel = new JPanel();
        topPanel.add(animationCheck);

        setLayout(new BorderLayout(5, 5));
        setBackground(Color.DARK_GRAY);
        setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 4));
        add(topPanel, BorderLayout.NORTH);
        add(display, BorderLayout.CENTER);
    }

    private JPanel createDisplayPanel() {
        return new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                applyRenderingHints(g2);
                applyCoordinateLimits(g2, X_LEFT, X_RIGHT, Y_TOP, Y_BOTTOM, false);
                drawWorld(g2);
            }
        };
    }

    private JCheckBox createAnimationCheckbox() {
        JCheckBox animationCheck = new JCheckBox("Run Animation");
        animationCheck.addActionListener(e -> toggleAnimation(animationCheck.isSelected()));
        return animationCheck;
    }

    private void configureAnimationTimer() {
        animationTimer = new Timer(TIMER_DELAY, e -> updateAndRepaint());
    }

    private void toggleAnimation(boolean run) {
        if (run && !animationTimer.isRunning()) {
            animationTimer.start();
        } else if (!run && animationTimer.isRunning()) {
            animationTimer.stop();
        }
    }

    private void updateAndRepaint() {
        frameNumber++;
        repaint();
    }

    private void applyRenderingHints(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    }

    private void applyCoordinateLimits(Graphics2D g2, double xleft, double xright,
                                       double ytop, double ybottom, boolean preserveAspect) {
        int width = display.getWidth();
        int height = display.getHeight();
        adjustAspect(preserveAspect, width, height, xleft, xright, ytop, ybottom);
        double pixelWidth = Math.abs((xright - xleft) / width);
        double pixelHeight = Math.abs((ybottom - ytop) / height);
        pixelSize = (float) Math.min(pixelWidth, pixelHeight);
        g2.scale(width / (xright - xleft), height / (ybottom - ytop));
        g2.translate(-xleft, -ytop);
    }

    private void adjustAspect(boolean preserveAspect, int width, int height,
                              double xleft, double xright, double ytop, double ybottom) {
        if (preserveAspect) {
            double displayAspect = Math.abs((double) height / width);
            double requestedAspect = Math.abs((ybottom - ytop) / (xright - xleft));
            if (displayAspect > requestedAspect) {
                double excess = (ybottom - ytop) * (displayAspect / requestedAspect - 1);
                ybottom += excess / 2;
                ytop -= excess / 2;
            } else if (displayAspect < requestedAspect) {
                double excess = (xright - xleft) * (requestedAspect / displayAspect - 1);
                xright += excess / 2;
                xleft -= excess / 2;
            }
        }
    }

    private void drawWorld(Graphics2D g2) {
        // Draw rotating shapes at specified coordinates
        drawRotatingShape(g2, 100, -1.02, -0.05);
        drawRotatingShape(g2, 100, 1.04, -0.98);
        drawRotatingShape(g2, 80, -1.379, 1.40);
        drawRotatingShape(g2, 80, -3.13, 2.23);
        drawRotatingShape(g2, 60, 0.9, 2.05);
        drawRotatingShape(g2, 60, 2.12, 1.45);

        // Draw bars at specified coordinates
        drawBar(g2, 1, 1.05, 0, -0.5);
        drawBar(g2, 0.85, 0.95, -2.65, 1.90);
        drawBar(g2, 0.6, 0.70, 2.5, 2.5);

        // Draw triangles at specified coordinates
        drawTriangle(g2, 0.5, 0.5, 0, -2, Color.BLUE);
        drawTriangle(g2, 0.35, 0.35, -2.25, 0.75, Color.PINK);
        drawTriangle(g2, 0.25, 0.25, 1.5, 1, Color.GREEN);
    }

    private void drawBar(Graphics2D g2, double xScale, double yScale, double offsetX, double offsetY) {
        AffineTransform saveTransform = g2.getTransform();
        g2.scale(xScale, yScale);
        g2.setColor(Color.RED);
        g2.translate(offsetX, offsetY);
        g2.rotate(-Math.PI / 8);
        g2.scale(2.3, 0.15);
        drawFilledRect(g2);
        g2.setTransform(saveTransform);
    }

    private void drawTriangle(Graphics2D g2, double scaleX, double scaleY, double offsetX, double offsetY, Color color) {
        AffineTransform saveTransform = g2.getTransform();
        g2.setColor(color);
        g2.translate(offsetX, offsetY);
        g2.scale(scaleX, scaleY);
        g2.fillPolygon(new int[]{0, 1, -1}, new int[]{3, 0, 0}, 3);
        g2.setTransform(saveTransform);
    }

    private void drawRotatingShape(Graphics2D g2, double radius, double offsetX, double offsetY) {
        AffineTransform saveTransform = g2.getTransform();
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(2));

        int numOfVertices = 12;
        double angle = (Math.PI * 2) / numOfVertices;
        Polygon polygon = new Polygon();

        for (int i = 0; i < numOfVertices; i++) {
            int x = (int) (radius * Math.sin(i * angle));
            int y = (int) (radius * Math.cos(i * angle));
            polygon.addPoint(x, y);
        }

        g2.translate(offsetX, offsetY);
        g2.rotate(Math.toRadians(frameNumber));
        g2.scale(0.005, 0.005);
        g2.draw(polygon);

        g2.setTransform(saveTransform);
    }

    private static void drawFilledRect(Graphics2D g2) {
        g2.fill(new Rectangle2D.Double(-0.5, -0.5, 1, 1));
    }
}
