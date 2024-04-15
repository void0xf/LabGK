import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;
import java.util.ArrayList;

/**
 * A panel that displays a two-dimensional animation constructed
 * using a scene graph for hierarchical modeling with a checkbox to toggle the animation.
 */
public class SceneGraph extends JPanel {
    private static final int WIDTH = 800, HEIGHT = 600;
    private static final double X_LEFT = -4, X_RIGHT = 4, Y_BOTTOM = -3, Y_TOP = 3;
    private static final Color BACKGROUND = Color.WHITE;

    private float pixelSize;
    private int frameNumber = 0;
    private CompoundObject world;
    private TransformedObject[] triangles = new TransformedObject[3];
    private TransformedObject[] bars = new TransformedObject[3];
    private TransformedObject[] shapes = new TransformedObject[6];

    public static void main(String[] args) {
        JFrame window = new JFrame("Scene Graph 2D");
        window.setContentPane(new SceneGraph());
        window.pack();
        window.setLocation(100, 60);
        window.setResizable(false);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setVisible(true);
    }

    public SceneGraph() {
        createWorld();
        setupUI();
    }

    private void createWorld() {
        world = new CompoundObject();
        initializeTriangles();
        initializeBars();
        initializeShapes();
    }

    private void initializeTriangles() {
        for (int i = 0; i < triangles.length; i++) {
            triangles[i] = new TransformedObject(filledTriangle);
            double scale = 0.5 + i * 0.2; // Example of varying scale
            triangles[i].setScale(scale, scale + 0.7).setColor(i == 0 ? Color.BLUE : (i == 1 ? new Color(128, 0, 128) : Color.GREEN));
            triangles[i].setTranslation(-2.25 + i * 2, 0.5 + i * 0.5);
            world.add(triangles[i]);
        }
    }

    private void initializeBars() {
        for (int i = 0; i < bars.length; i++) {
            bars[i] = new TransformedObject(filledRect);
            bars[i].setRotation(-22.5).setScale(2 - i * 0.2, 0.1).setTranslation(-2.2 + i * 2, 1.5 - i * 0.3).setColor(Color.RED);
            world.add(bars[i]);
        }
    }

    private void initializeShapes() {
        for (int i = 0; i < shapes.length; i++) {
            shapes[i] = new TransformedObject(hexagon);
            shapes[i].setScale(0.3 - i * 0.05, 0.3 - i * 0.05).setTranslation(-0.889 + i * 1.1, -0.42 + i * 0.667);
            world.add(shapes[i]);
        }
    }

    private void setupUI() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(BACKGROUND);
        setLayout(new BorderLayout(5, 5));
        setBackground(Color.DARK_GRAY);
        setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 4));

        JPanel display = createDisplayPanel();
        addControlPanel();

        add(display, BorderLayout.CENTER);
    }

    private JPanel createDisplayPanel() {
        JPanel display = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                applyLimits(g2, X_LEFT, X_RIGHT, Y_TOP, Y_BOTTOM, false);
                world.draw(g2);
            }
        };
        display.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        display.setBackground(BACKGROUND);
        return display;
    }

    private void addControlPanel() {
        final Timer timer = new Timer(17, e -> {
            updateFrame();
            repaint();
        });

        JCheckBox animationCheck = new JCheckBox("Run Animation");
        animationCheck.addActionListener(e -> {
            if (animationCheck.isSelected()) {
                timer.start();
            } else {
                timer.stop();
            }
        });

        JPanel topPanel = new JPanel();
        topPanel.add(animationCheck);
        add(topPanel, BorderLayout.NORTH);
    }

    public void updateFrame() {
        frameNumber++;
        for (TransformedObject shape : shapes) {
            shape.setRotation(frameNumber * 0.75);
        }
    }

    private void applyLimits(Graphics2D g2, double xleft, double xright, double ytop, double ybottom, boolean preserveAspect) {
        int width = getWidth();
        int height = getHeight();
        if (preserveAspect) {
            adjustAspect(width, height, xleft, xright, ytop, ybottom);
        }
        double pixelWidth = Math.abs((xright - xleft) / width);
        double pixelHeight = Math.abs((ybottom - ytop) / height);
        pixelSize = (float) Math.min(pixelWidth, pixelHeight);
        g2.scale(width / (xright - xleft), height / (ybottom - ytop));
        g2.translate(-xleft, -ytop);
    }

    private void adjustAspect(int width, int height, double xleft, double xright, double ytop, double ybottom) {
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

    private static abstract class SceneGraphNode {
        Color color;

        SceneGraphNode setColor(Color c) {
            this.color = c;
            return this;
        }

        final void draw(Graphics2D g) {
            Color saveColor = color;
            if (color != null) {
                saveColor = g.getColor();
                g.setColor(color);
            }
            doDraw(g);
            if (saveColor != null) {
                g.setColor(saveColor);
            }
        }

        abstract void doDraw(Graphics2D g);
    }

    private static class CompoundObject extends SceneGraphNode {
        ArrayList<SceneGraphNode> subobjects = new ArrayList<>();

        CompoundObject add(SceneGraphNode node) {
            subobjects.add(node);
            return this;
        }

        void doDraw(Graphics2D g) {
            for (SceneGraphNode node : subobjects) {
                node.draw(g);
            }
        }
    }

    private static class TransformedObject extends SceneGraphNode {
        SceneGraphNode object;
        double rotationInDegrees = 0, scaleX = 1, scaleY = 1, translateX = 0, translateY = 0;

        TransformedObject(SceneGraphNode object) {
            this.object = object;
        }

        TransformedObject setRotation(double degrees) {
            rotationInDegrees = degrees;
            return this;
        }

        TransformedObject setTranslation(double dx, double dy) {
            translateX = dx;
            translateY = dy;
            return this;
        }

        TransformedObject setScale(double sx, double sy) {
            scaleX = sx;
            scaleY = sy;
            return this;
        }

        void doDraw(Graphics2D g) {
            AffineTransform savedTransform = g.getTransform();
            applyTransforms(g);
            object.draw(g);
            g.setTransform(savedTransform);
        }

        private void applyTransforms(Graphics2D g) {
            if (translateX != 0 || translateY != 0)
                g.translate(translateX, translateY);
            if (rotationInDegrees != 0)
                g.rotate(rotationInDegrees / 180.0 * Math.PI);
            if (scaleX != 1 || scaleY != 1)
                g.scale(scaleX, scaleY);
        }
    }

    // Definitions of basic shapes follow, each extending SceneGraphNode.

    private static SceneGraphNode filledTriangle = new SceneGraphNode() {
        void doDraw(Graphics2D g) {
            Path2D path = new Path2D.Double();
            path.moveTo(-0.5, 0);
            path.lineTo(0.5, 0);
            path.lineTo(0, 1);
            path.closePath();
            g.fill(path);
        }
    };

    private static SceneGraphNode filledRect = new SceneGraphNode() {
        void doDraw(Graphics2D g) {
            g.fill(new Rectangle2D.Double(-0.5, -0.5, 1, 1));
        }
    };

    private static SceneGraphNode hexagon = new SceneGraphNode() {
        void doDraw(Graphics2D g) {
            int n = 12;
            double angle = (Math.PI * 2) / n;
            int[] xPoints = new int[n];
            int[] yPoints = new int[n];
            for (int i = 0; i < n; i++) {
                xPoints[i] = (int) (350 * Math.sin(i * angle));
                yPoints[i] = (int) (350 * Math.cos(i * angle));
            }
            Polygon polygon = new Polygon(xPoints, yPoints, n);
            g.setStroke(new BasicStroke(4));
            g.scale(0.006, 0.006);
            for (int i = 0; i < n; i++) {
                g.drawLine(xPoints[i], yPoints[i], 0, 0);
            }
            g.draw(polygon);
        }
    };
}
