package lab7;

import java.awt.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.*;
import com.jogamp.opengl.util.awt.AWTGLReadBufferUtil;
import com.jogamp.opengl.util.awt.ImageUtil;
import com.jogamp.opengl.util.gl2.GLUT;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.awt.AWTTextureIO;

/**
 * CPSC 424, Fall 2015, Lab 7:  Image Textures in OpenGL/JOGL.
 * When run as an application, the program shows a window with
 * two panels:  on the left, a PaintPanel where the user can
 * draw 2D images and on the right a GLJPanel that shows a single
 * object to which a texture can be applied.  There is a menu
 * for selecting the object and one for selecting the texture.
 * It is possible to apply the image from the paint panel as
 * a texture on the 3D object, and it possible to copy the
 * image from the OpenGL panel to the paint panel.
 * This program depends on PaintPanel.java, TexturedShapes.java,
 * Camera.java, and three resource files (brick.jpg, earth.jpg,
 * and clouds.jpg) that contain images used as textures.
 */
public class Lab7 extends JPanel implements GLEventListener {

    public static void main(String[] args) {
        JFrame window = new JFrame("Painting and Texturing");
        Lab7 panel = new Lab7();
        window.setContentPane(panel);
        window.setJMenuBar(panel.getMenuBar());
        window.pack();
        window.setResizable(false);
        window.setLocation(50, 50);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setVisible(true);
    }

    private PaintPanel paintPanel;
    private GLJPanel displayGL;
    private GLUT glut = new GLUT();
    private Camera camera;
    private int currentObjectNum = 0;
    private int textureRepeatHorizontal = 1;
    private int textureRepeatVertical = 1;
    private Texture currentTexture = null;

    public Lab7() {
        initializePanels();
        initializeLayout();
        initializeCamera();
    }

    private void initializePanels() {
        paintPanel = new PaintPanel();
        displayGL = new GLJPanel(new GLCapabilities(null));
        paintPanel.setPreferredSize(new Dimension(512, 512));
        displayGL.setPreferredSize(new Dimension(512, 512));
        displayGL.addGLEventListener(this);
    }

    private void initializeLayout() {
        this.setLayout(new GridLayout(1, 2, 4, 4));
        this.add(paintPanel);
        this.add(displayGL);
        this.setBackground(Color.BLACK);
        this.setBorder(BorderFactory.createLineBorder(Color.BLACK, 3));
    }

    private void initializeCamera() {
        camera = new Camera();
        camera.lookAt(1, 1, 4, 0, 0, 0, 0, 1, 0);
        camera.setLimits(-0.8, 0.8, -0.8, 0.8, -2, 2);
        camera.installTrackball(displayGL);
    }

    private void drawCurrentShape(GL2 gl2) {
        switch (currentObjectNum) {
            case 0 -> {
                gl2.glScaled(0.9, 0.9, 0.9);
                TexturedShapes.cube(gl2);
            }
            case 1 -> {
                gl2.glRotated(-90, 1, 0, 0);
                gl2.glTranslated(0, 0, -0.5);
                TexturedShapes.uvCylinder(gl2);
            }
            case 2 -> {
                gl2.glRotated(-90, 1, 0, 0);
                gl2.glTranslated(0, 0, -0.4);
                TexturedShapes.uvCone(gl2);
            }
            case 3 -> {
                gl2.glScaled(1.3, 1.3, 1.3);
                TexturedShapes.uvSphere(gl2);
            }
            case 4 -> {
                gl2.glScaled(1.4, 1.4, 1.4);
                TexturedShapes.uvTorus(gl2);
            }
            case 5 -> glut.glutSolidTeapot(0.47);
            case 6 -> triangularPrism(gl2);
            case 7 -> TexturedShapes.uvPyramid(gl2);
        }
    }

    private void triangularPrism(GL2 gl2) {
        double t = Math.sqrt(3) / 4;
        drawFace(gl2, 0, 1, 0, new double[][]{
                {-t, 0.5, -0.25},
                {t, 0.5, -0.25},
                {0, 0.5, 0.5}
        });
        drawFace(gl2, 0, -1, 0, new double[][]{
                {t, -0.5, -0.25},
                {-t, -0.5, -0.25},
                {0, -0.5, 0.5}
        });
        drawFan(gl2, 0, 0, -1, new double[][]{
                {-t, -0.5, -0.25},
                {-t, 0.5, -0.25},
                {t, 0.5, -0.25},
                {t, -0.5, -0.25}
        });
        drawFan(gl2, -0.75, 0, t, new double[][]{
                {-t, 0.5, -0.25},
                {-t, -0.5, -0.25},
                {0, -0.5, 0.5},
                {0, 0.5, 0.5}
        });
        drawFan(gl2, 0.75, 0, t, new double[][]{
                {0, 0.5, 0.5},
                {0, -0.5, 0.5},
                {t, -0.5, -0.25},
                {t, 0.5, -0.25}
        });
    }

    private void drawFace(GL2 gl2, double nx, double ny, double nz, double[][] vertices) {
        gl2.glBegin(GL2.GL_TRIANGLES);
        gl2.glNormal3d(nx, ny, nz);
        for (double[] vertex : vertices) {
            gl2.glVertex3d(vertex[0], vertex[1], vertex[2]);
        }
        gl2.glEnd();
    }

    private void drawFan(GL2 gl2, double nx, double ny, double nz, double[][] vertices) {
        gl2.glBegin(GL2.GL_TRIANGLE_FAN);
        gl2.glNormal3d(nx, ny, nz);
        for (double[] vertex : vertices) {
            gl2.glVertex3d(vertex[0], vertex[1], vertex[2]);
        }
        gl2.glEnd();
    }

    private Texture textureFromResource(String resourceName) throws IOException {
        URL textureURL = this.getClass().getClassLoader().getResource(resourceName);
        BufferedImage img = ImageIO.read(Objects.requireNonNull(textureURL));
        ImageUtil.flipImageVertically(img);
        return createTexture(img);
    }

    private Texture textureFromPainting() {
        BufferedImage img = paintPanel.copyOSC();
        return createTexture(img);
    }

    private Texture createTexture(BufferedImage img) {
        Texture texture;
        GLContext context = displayGL.getContext();
        boolean needsRelease = false;
        if (!context.isCurrent()) {
            context.makeCurrent();
            needsRelease = true;
        }
        GL2 gl2 = context.getGL().getGL2();
        texture = AWTTextureIO.newTexture(displayGL.getGLProfile(), img, true);
        texture.setTexParameteri(gl2, GL2.GL_TEXTURE_WRAP_S, GL2.GL_REPEAT);
        texture.setTexParameteri(gl2, GL2.GL_TEXTURE_WRAP_T, GL2.GL_REPEAT);
        if (needsRelease) {
            context.release();
        }
        return texture;
    }

    private void paintingFromOpenGL() {
        GLContext context = displayGL.getContext();
        boolean needsRelease = false;
        if (!context.isCurrent()) {
            context.makeCurrent();
            needsRelease = true;
        }
        GL2 gl2 = context.getGL().getGL2();
        AWTGLReadBufferUtil readBuf = new AWTGLReadBufferUtil(displayGL.getGLProfile(), false);
        BufferedImage img = readBuf.readPixelsToBufferedImage(gl2, true);
        if (needsRelease) {
            context.release();
        }
        paintPanel.installImage(img);
    }

    public void display(GLAutoDrawable drawable) {
        GL2 gl2 = drawable.getGL().getGL2();
        gl2.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
        camera.apply(gl2);
        applyTexture(gl2);
        drawCurrentShape(gl2);
    }

    private void applyTexture(GL2 gl2) {
        if (currentTexture != null) {
            currentTexture.enable(gl2);
            currentTexture.bind(gl2);
            currentTexture.disable(gl2);
        }
    }

    public void init(GLAutoDrawable drawable) {
        GL2 gl2 = drawable.getGL().getGL2();
        setupOpenGL(gl2);
        setupLighting(gl2);
        setupMaterial(gl2);
    }

    private void setupOpenGL(GL2 gl2) {
        gl2.glClearColor(1, 1, 1, 1);
        gl2.glEnable(GL2.GL_DEPTH_TEST);
        gl2.glEnable(GL2.GL_NORMALIZE);
        gl2.glEnable(GL2.GL_LIGHTING);
    }

    private void setupLighting(GL2 gl2) {
        gl2.glEnable(GL2.GL_LIGHT0);
        gl2.glEnable(GL2.GL_LIGHT1);
        gl2.glEnable(GL2.GL_LIGHT2);
        gl2.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, new float[]{1, 1, 10, 0}, 0);
        gl2.glLightfv(GL2.GL_LIGHT1, GL2.GL_POSITION, new float[]{0, 5, 0, 0}, 0);
        gl2.glLightfv(GL2.GL_LIGHT2, GL2.GL_POSITION, new float[]{-5, -1, 10, 0}, 0);
        float[] dimmer = {0.3f, 0.3f, 0.3f, 1};
        for (int i = 0; i <= 2; i++) {
            gl2.glLightfv(GL2.GL_LIGHT0 + i, GL2.GL_DIFFUSE, dimmer, 0);
            gl2.glLightfv(GL2.GL_LIGHT0 + i, GL2.GL_SPECULAR, dimmer, 0);
        }
        gl2.glLightModeli(GL2.GL_LIGHT_MODEL_COLOR_CONTROL, GL2.GL_SEPARATE_SPECULAR_COLOR);
    }

    private void setupMaterial(GL2 gl2) {
        float[] diffuse = {1, 1, 1, 1};
        float[] specular = {0.3f, 0.3f, 0.3f, 1};
        gl2.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE, diffuse, 0);
        gl2.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_SPECULAR, specular, 0);
        gl2.glMateriali(GL2.GL_FRONT_AND_BACK, GL2.GL_SHININESS, 32);
    }

    public void dispose(GLAutoDrawable drawable) {
        // Called when the panel is being disposed
    }

    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        // Called when the user resizes the window
    }

    public JMenuBar getMenuBar() {
        JMenuBar menuBar = paintPanel.getMenuBar();
        JMenu textureMenu = createTextureMenu();
        JMenu objectMenu = createObjectMenu();
        menuBar.add(textureMenu);
        menuBar.add(objectMenu);
        return menuBar;
    }

    private JMenu createTextureMenu() {
        JMenu textureMenu = new JMenu("Texture");
        ActionListener textureListener = e -> handleTextureMenuAction(e);
        makeMenuItem(textureMenu, ">>> Texture From Painting >>>", textureListener, 0);
        makeMenuItem(textureMenu, "<<< Painting From OpenGL <<<", textureListener, 1);
        textureMenu.addSeparator();
        makeMenuItem(textureMenu, "No Texture", textureListener, 2);
        makeMenuItem(textureMenu, "Earth Texture", textureListener, 3);
        makeMenuItem(textureMenu, "Brick Texture", textureListener, 4);
        makeMenuItem(textureMenu, "Clouds Texture", textureListener, 5);
        textureMenu.addSeparator();
        makeMenuItem(textureMenu, "Horizontal Repeat = 1", textureListener, 6);
        makeMenuItem(textureMenu, "Horizontal Repeat = 2", textureListener, 7);
        makeMenuItem(textureMenu, "Horizontal Repeat = 3", textureListener, 8);
        makeMenuItem(textureMenu, "Horizontal Repeat = 4", textureListener, 9);
        textureMenu.addSeparator();
        makeMenuItem(textureMenu, "Vertical Repeat = 1", textureListener, 10);
        makeMenuItem(textureMenu, "Vertical Repeat = 2", textureListener, 11);
        makeMenuItem(textureMenu, "Vertical Repeat = 3", textureListener, 12);
        makeMenuItem(textureMenu, "Vertical Repeat = 4", textureListener, 13);
        return textureMenu;
    }

    private void handleTextureMenuAction(ActionEvent e) {
        int itemNum = Integer.parseInt(e.getActionCommand());
        try {
            switch (itemNum) {
                case 0 -> currentTexture = textureFromPainting();
                case 1 -> paintingFromOpenGL();
                case 2 -> currentTexture = null;
                case 3 -> currentTexture = textureFromResource("earth.jpg");
                case 4 -> currentTexture = textureFromResource("brick.jpg");
                case 5 -> currentTexture = textureFromResource("clouds.jpg");
                default -> handleTextureRepeat(itemNum);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        if (itemNum != 1) {
            displayGL.repaint();
        }
    }

    private void handleTextureRepeat(int itemNum) {
        if (itemNum < 10) {
            textureRepeatHorizontal = itemNum - 5;
        } else {
            textureRepeatVertical = itemNum - 9;
        }
    }

    private JMenu createObjectMenu() {
        JMenu objectMenu = new JMenu("3D Object");
        ActionListener objectListener = e -> {
            currentObjectNum = Integer.parseInt(e.getActionCommand());
            displayGL.repaint();
        };
        makeMenuItem(objectMenu, "Cube", objectListener, 0);
        makeMenuItem(objectMenu, "Cylinder", objectListener, 1);
        makeMenuItem(objectMenu, "Cone", objectListener, 2);
        makeMenuItem(objectMenu, "Sphere", objectListener, 3);
        makeMenuItem(objectMenu, "Torus", objectListener, 4);
        makeMenuItem(objectMenu, "Teapot", objectListener, 5);
        makeMenuItem(objectMenu, "Triangular Prism", objectListener, 6);
        makeMenuItem(objectMenu, "Pyramid", objectListener, 7);
        return objectMenu;
    }

    private void makeMenuItem(JMenu menu, String name, ActionListener listener, int i) {
        JMenuItem item = new JMenuItem(name);
        item.addActionListener(listener);
        item.setActionCommand("" + i);
        menu.add(item);
    }
}
