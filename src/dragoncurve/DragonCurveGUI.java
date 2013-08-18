/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dragoncurve;

import dragoncurve.util.Clock;
import dragoncurve.util.Direction;
import dragoncurve.util.Timer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 *
 * @author anderson
 */
public class DragonCurveGUI extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener {

    private final long LOOP_SLEEP = 10;
    private Clock clock;
    private String data = "";
    private float memoryUsage = 0;
    private int iterations = 0;
    private boolean OOBpause = false;
    private boolean pause = false;
    private float color = 0;
    private double zoom = 1.0;
    private int posX = 0, posY = 0;
    private Rectangle bounds = null;
    private Point mouse = new Point();
    private ArrayList<String> info;
    private boolean segmentDraw = false;
    private int segmentCount = 0;
    private Point center = null;

    public DragonCurveGUI() {
        super.setPreferredSize(new Dimension(800, 600));
        super.setIgnoreRepaint(true);

        info = new ArrayList<>();

        clock = new Clock();
        clock.setSleep(30);

        Timer iterator = new Timer(600) {
            boolean gc = true;
            
            @Override
            public void run() {
                if (!OOBpause) {
                    data = iterate(data);
                    iterations++;
                    gc = true;
                } else if (gc){
                    System.gc();
                    gc = false;
                }
                memoryUsage = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
                memoryUsage = memoryUsage / 1024 / 1024;
            }
        };

        clock.addTimer(iterator);
        clock.setPaused(false);

    }

    public static void main(String[] args) {
        DragonCurveGUI GUI = new DragonCurveGUI();

        JFrame window = new JFrame("Dragon Curve Demo");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        window.setContentPane(GUI);

        window.pack();

        // centralizado
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        Rectangle frame = window.getBounds();
        window.setLocation((screen.width - frame.width) / 2, (screen.height - frame.height) / 2);

        window.setVisible(true);
        GUI.loop();
    }

    @Override
    public void paintComponent(Graphics g1) {
        Graphics2D g = (Graphics2D) g1;

        g.setClip(super.getBounds());
        
        g.setColor(Color.BLACK);
        g.fill(g.getClip());
        
        AffineTransform t = new AffineTransform();
        t.translate(posX, posY);
        t.scale(zoom, zoom);
        g.setTransform(t);
        
        
        
        if (segmentDraw){
            foldIncr(data, g);
        } else {
            fold(data, g);
        }

        if (bounds != null && false) {
            
            if (bounds.contains(getMouse(mouse))){
                g.setColor(Color.BLUE);
            } else {
                g.setColor(Color.ORANGE);
            }
            
            g.draw(bounds);
        }

        g.setTransform(new AffineTransform());
        
        if (bounds != null && false) {
            g.setColor(Color.red);
            g.drawOval(bounds.x-5, bounds.y-5, 10, 10);
            g.fillRect(mouse.x-5, mouse.y-5, 10, 10);
            
            g.setColor(Color.yellow);
            g.drawRect(posX-5, posY-5, 10, 10);
            g.drawRect(posX, posY, bounds.width, bounds.height);
            g.fillOval(getMouse(mouse).x-5, getMouse(mouse).y-5, 10, 10);
        }

        g.setColor(Color.WHITE);

        info.clear();
        info.add("Iterations: " + iterations);
        info.add("Data: " + data.length() + " points");
        if (segmentDraw){
            info.add("Drawing: " + segmentCount + " points");
        }
        info.add("Memory usage: ~ " + memoryUsage + " Mb");
        info.add("Free memory: ~ " + Runtime.getRuntime().freeMemory()/1024f/1024f + " Mb");
        info.add("Total memory: ~ " + Runtime.getRuntime().totalMemory()/1024f/1024f + " Mb");
        info.add("Zoom: " + zoom);
        info.add("Out of bounds pause: " + OOBpause);
        info.add("Pause: " + pause);

        int x = 20;

        for (String i : info) {
            g.drawString(i, 20, x);
            x += 15;
        }

        g.dispose();
    }

    @SuppressWarnings("SleepWhileInLoop")
    public void loop() {
        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
        clock.start();
        while (true) {
            clock.increase();
            repaint();
            try {
                Thread.sleep(LOOP_SLEEP);
            } catch (Exception e) {
            }
        }
    }

    @Deprecated
    public void drawDragonCurve(int iterations, Graphics2D g) {

        String data = "";

        for (int i = 0; i < iterations; i++) {
            data = iterate(data);
        }

        fold(data, g);

    }

    public String iterate(String prev) {
        
        StringBuilder ret = new StringBuilder(2 * prev.length() + 1);

        for (int a = prev.length() - 1; a >= 0; a--) {
            if (prev.charAt(a) == '1') {
                ret.append('0');
            } else {
                ret.append('1');
            }
        }

        ret.append('1');
        ret.append(prev);

        return ret.toString();
    }

    public void fold(String data, Graphics2D g) {
        
        Direction dir = Direction.E;
        center = new Point(g.getClipBounds().width / 2, g.getClipBounds().height / 2);
        Point pos = new Point(g.getClipBounds().width / 2, g.getClipBounds().height / 2);
        
        bounds = new Rectangle(pos.x, pos.y, 1, 1);

        color = 0;
        
        for (char c : data.toCharArray()) {
            g.setColor(Color.getHSBColor(color, 1, 1));
            dir = drawSeg(pos, dir, (c == '1'), g, 20);
            bounds.add(pos);
            color += 1.0 / (data.length()/20);
        }
        
        OOBpause = (!g.getClipBounds().contains(bounds) || pause);
    }
    
    public void foldIncr(String data, Graphics2D g) {

        Direction dir = Direction.E;
        center = new Point(g.getClipBounds().width / 2, g.getClipBounds().height / 2);
        Point pos = new Point(g.getClipBounds().width / 2, g.getClipBounds().height / 2);

        bounds = new Rectangle(pos.x, pos.y, 1, 1);
        
        color = 0;
        
        for (int i = 0; i <= segmentCount; i++){
            g.setColor(Color.getHSBColor(color, 1, 1));
            dir = drawSeg(pos, dir, (data.charAt(i) == '1'), g, 20);
            bounds.add(pos);
            color += 1.0 / data.length();
        }
        
        segmentCount = (segmentCount < data.length()-1) ? segmentCount + 1 : 0;
        
        OOBpause = (!g.getClipBounds().contains(bounds) || pause);
        
    }

    public Direction drawSeg(Point pos, Direction dir, boolean val, Graphics2D g, int size) {
        
        int x = pos.x + size * dir.x;
        int y = pos.y + size * dir.y;
        
        g.drawLine(pos.x, pos.y, x, y);
        pos.setLocation(x, y);

        if (val) {
            dir = dir.getNext().getNext();
        } else {
            dir = dir.getPrev().getPrev();
        }

        return dir;
    }

    public synchronized Point getMouse(Point mouse) {
        return new Point((int) ((mouse.getX() - posX) / zoom), (int) ((mouse.getY() - posY) / zoom));
    }

    public synchronized void setZoom(double z, Point pos) {
        if ((zoom + z) > 0.01) {
            
            if (pos != null && bounds.contains(getMouse(mouse))){
                posX -= (int) (((getMouse(mouse).x - bounds.x)*z));
                posY -= (int) (((getMouse(mouse).y - bounds.y)*z));
                
            } else {
                posX = 0;
                posY = 0;
            }
            zoom = zoom + z;
            
            
        }
    }

    public synchronized void setPosition(int x, int y) {
        posX -= x;
        posY -= y;
    }

    @Override
    public void mouseClicked(MouseEvent me) {
        if (me.getButton() == 1){
            pause = !pause;
        } else if (me.getButton() == 3){
            data = "";
            clock.setPaused(false);
            OOBpause = false;
            zoom = 1.0;
            posX = 0;
            posY = 0;
            iterations = 0;
            pause = false;
            segmentDraw = false;
            segmentCount = 0;
        } else {
            segmentDraw = !segmentDraw;
        }
    }

    @Override
    public void mousePressed(MouseEvent me) {
    }

    @Override
    public void mouseReleased(MouseEvent me) {
    }

    @Override
    public void mouseEntered(MouseEvent me) {
    }

    @Override
    public void mouseExited(MouseEvent me) {
    }

    @Override
    public void mouseDragged(MouseEvent me) {
        setPosition((int) (mouse.getX() - me.getPoint().getX()), (int) (mouse.getY() - me.getPoint().getY()));
        mouse = me.getPoint();
    }

    @Override
    public void mouseMoved(MouseEvent me) {
        mouse = me.getPoint();
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent mwe) {
        setZoom(mwe.getWheelRotation() * 0.1, mwe.getPoint());
    }

}
