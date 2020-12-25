package com.javabinary;


import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 * <b>Hackimen's Game Engine</b>
 *
 * @author Hackimen
 * @version 1.4
 */


/* Example Usage :
import java.awt.*;

public class Main{
    static class Example extends HakimenGameEngine{
        @Override
        public boolean OnUserCreate(){
            return true;
        }
        @Override
        public boolean OnUserUpdate(Graphics2D g){
            return true;
        }
    }
    public static void main(String[] args){
        Example ex = new Example();
        if(ex.Construct("Example",400,400,1,1)){
            System.out.println("[!] Build Successfully");
        }
    }
}
 */

public class HakimenGameEngine {

    enum MouseState {
        NULL(0),
        BUTTON_1(1),
        BUTTON_2(2),
        BUTTON_3(3),
        BUTTON_4(4),
        BUTTON_5(5);

        int btn;

        MouseState(int i) {
            this.btn = i;
        }
    }

    //
    private int translateX = 0, translateY = 0, sx = 1, sy = 1, stroke = 0;
    private Font font;
    Graphics2D g;
    //

    protected boolean b_keys[] = new boolean[256];
    protected boolean b_mouse[] = new boolean[6];
    protected int nScrollDir;
    private int mouseX, mouseY;
    private int nScreenHeight, nScreenWidth;
    private String title;
    private JFrame frame = new JFrame();
    private Renderer r;

    public int MouseX() {
        return mouseX;
    }

    public int MouseY() {
        return mouseY;
    }

    /**
     * Returns the constructed Window Height
     *
     * @since 1.0
     */
    public int ScreenHeight() {
        return this.nScreenHeight;
    }

    /**
     * Returns the constructed Window Width
     *
     * @since 1.0
     */
    public int ScreenWidth() {
        return this.nScreenWidth;
    }

    /**
     * Initializer Called Once on Create ( Use to Create or Alocate static variables )
     *
     * @since 1.0
     */
    public boolean OnUserCreate() {
        return true;
    }

    /**
     * Called every frame after <code>OnUserCreate()</code>
     *
     * @since 1.0
     */

    public boolean OnUserUpdate(Graphics2D g,int elapsedTime) {
        return true;
    }

    /**
     * Construct a window with a <code>JPanel</code> on it
     *
     * @param title         The title of the Window
     * @param nScreenWidth  Width of the Window
     * @param nScreenHeight Height of the Window
     * @since 1.0
     */
    public boolean Construct(String title, int nScreenWidth, int nScreenHeight, int sx, int sy) {
        this.title = title;
        this.nScreenHeight = nScreenHeight * sx;
        this.nScreenWidth = nScreenWidth * sy;
        this.sx = sx;
        this.sy = sy;
        frame.setTitle(String.format("Hakimen's Engine | %s ",title));
        frame.setSize(this.nScreenWidth, this.nScreenHeight);
        frame.setDefaultCloseOperation(3);
        frame.setResizable(false);
        r = new Renderer(this);
        frame.add(r);
        return true;
    }

    private void tryConfig(Graphics2D g) {
        g.setStroke(new BasicStroke(stroke));
        g.translate(translateX, translateX);
        g.scale(sx, sy);
        g.setFont(font);
    }

    void DrawRect(Graphics2D g, int x, int y, int dx, int dy, Color color) {
        g.setColor(color);
        g.drawRect(x, y, dx, dy);
    }

    void FillRect(Graphics2D g, int x, int y, int dx, int dy, Color color) {
        g.setColor(color);
        g.fillRect(x, y, dx, dy);
    }

    void DrawOval(Graphics2D g, int x, int y, int radius, Color color) {
        g.setColor(color);
        g.drawOval(x, y, radius, radius);
    }

    void FillOval(Graphics2D g, int x, int y, int radius, Color color) {
        g.setColor(color);
        g.fillOval(x, y, radius, radius);
    }

    void DrawString(Graphics2D g, int x, int y, Color color, String s) {
        g.setColor(color);
        g.drawString(s, x, y);
    }

    void DrawFormattedString(Graphics2D g, int x, int y, Color color, String template, Object... data) {
        g.setColor(color);
        g.drawString(String.format(template, data), x, y);
    }

    void DrawImage(Graphics2D g, int x, int y, BufferedImage img) {
        g.drawImage(img, x, y, null);
    }

    void DrawPartialImage(Graphics2D g, int x, int y, int topX, int topY, int bottomX, int bottomY, BufferedImage img) {
        g.drawImage(img.getSubimage(topX, topY, bottomX, bottomY), x, y, null);
    }

    void DrawPixel(Graphics2D g, int x, int y, Color color) { ;
        g.setColor(color);
        g.drawLine(x, y, x, y);
    }

    void DrawLine(Graphics2D g, int startX, int startY, int endX, int endY, Color color) {
        g.setColor(color);
        g.drawLine(startX, startY, endX, endY);
    }

    void DrawWireframeModel(Graphics2D g, ArrayList<float[]> modelCoords, int x, int y, float r, int s, Color color) {

        ArrayList<float[]> transformedCoords = new ArrayList<>();
        int vertices = modelCoords.size();
        //Rotate
        for (int i = 0; i < vertices; i++) {
            float key = (float) (modelCoords.get(i)[0] * Math.cos(r) - modelCoords.get(i)[1] * Math.sin(r));
            float val = (float) (modelCoords.get(i)[0] * Math.sin(r) + modelCoords.get(i)[1] * Math.cos(r));
            transformedCoords.add(new float[]{key, val});
        }
        //Scale
        for (int i = 0; i < vertices; i++) {
            float key = transformedCoords.get(i)[0] * s;
            float val = transformedCoords.get(i)[1] * s;
            transformedCoords.set(i, new float[]{key, val});
        }
        //Offset
        for (int i = 0; i < vertices; i++) {
            float key = transformedCoords.get(i)[0] + x;
            float val = transformedCoords.get(i)[1] + y;
            transformedCoords.set(i, new float[]{key, val});
        }
        for (int i = 0; i < vertices + 1; i++) {
            int j = (i + 1);
            DrawLine(g, (int)transformedCoords.get(i % vertices)[0], (int)transformedCoords.get(i % vertices)[1],
                    (int)transformedCoords.get(j % vertices)[0], (int)transformedCoords.get(j % vertices)[1], color);
        }
    }

    void Translate(int x, int y) {
        this.translateX = x;
        this.translateY = y;
    }

    void Font(Font font) {
        this.font = font;
    }

    void Stroke(int stroke) {
        this.stroke = stroke;
    }

    class Renderer extends JPanel implements ActionListener {
        Timer t = new Timer(1, this);
        HakimenGameEngine engine;
        long scrollResetTimer = System.currentTimeMillis();
        public Renderer(HakimenGameEngine engine) {
            setFocusable(true);
            this.engine = engine;
            t.start();
            if(engine.OnUserCreate() == false) System.exit(-2);
            frame.setVisible(true);
            addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent keyEvent) {
                    engine.b_keys[keyEvent.getKeyCode()] = true;
                }
                public void keyReleased(KeyEvent keyEvent) {
                    engine.b_keys[keyEvent.getKeyCode()] = false;
                }
            });
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent mouseEvent) {
                    b_mouse[mouseEvent.getButton()] = true;
                }

                @Override
                public void mouseReleased(MouseEvent mouseEvent) {
                    b_mouse[mouseEvent.getButton()] = false;
                }
            });
            addMouseMotionListener(new MouseAdapter() {
                @Override
                public void mouseDragged(MouseEvent mouseEvent) {
                    mouseX = mouseEvent.getX();
                    mouseY = mouseEvent.getY();
                }

                @Override
                public void mouseMoved(MouseEvent mouseEvent) {
                    mouseX = mouseEvent.getX();
                    mouseY = mouseEvent.getY();
                }
            });
            addMouseWheelListener(new MouseWheelListener() {
                @Override
                public void mouseWheelMoved(MouseWheelEvent mouseWheelEvent) {
                    nScrollDir = mouseWheelEvent.getWheelRotation();
                    scrollResetTimer = System.currentTimeMillis();
                }
            });
        }
        long t1 = System.currentTimeMillis();
        long t2;
        int elapsedTime;
        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            g = (Graphics2D) graphics;
            tryConfig(g);
            if(System.currentTimeMillis()-scrollResetTimer > 100)
                nScrollDir = 0;
            if(engine.OnUserUpdate(g,elapsedTime) == false) System.exit(-1);
            t2 = System.currentTimeMillis();
            elapsedTime = (int)(t2-t1);
            t1 = t2;
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            repaint();
        }
    }
}