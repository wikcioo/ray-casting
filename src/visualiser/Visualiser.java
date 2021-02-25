package visualiser;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;
import java.util.Random;

public class Visualiser implements Runnable {

    private final String title;
    private final int width;
    private final int height;

    private Display display;
    private Random random;

    private Thread thread;
    private boolean running = false;

    // Drawing to the screen
    private BufferStrategy bs;
    private Graphics2D g;

    // Input
    private MouseInput mouseInput;

    // Ray casting
    private final int numberOfBounds = 8;
    private final ArrayList<Line2D.Float> bounds;
    private ArrayList<Line2D.Float> rays;

    public Visualiser(String title, int width, int height) {
        this.title = title;
        this.width = width;
        this.height = height;

        mouseInput = new MouseInput();
        bounds = new ArrayList<>();
        rays = new ArrayList<>();
    }

    private void init() {
        display = new Display(title, width, height);
        display.getFrame().addMouseMotionListener(mouseInput);
        display.getCanvas().addMouseMotionListener(mouseInput);

        random = new Random();

        initBounds();
    }

    private void initBounds() {
        for (int i = 0; i < numberOfBounds; i++) {
            int x1 = random.nextInt(width);
            int y1 = random.nextInt(height);
            int x2 = random.nextInt(width);
            int y2 = random.nextInt(height);
            bounds.add(new Line2D.Float(x1, y1, x2, y2));
        }
    }

    @Override
    public void run() {
        init();

        double fps = 60d;
        double timePerRender = 1000000000 / fps;
        double delta = 0;
        long timer = System.currentTimeMillis();
        long lastTime = System.nanoTime();
        long now = 0;
        int frames = 0;

        while (running) {
            now = System.nanoTime();
            delta += (now - lastTime) / timePerRender;
            lastTime = now;

            if (delta >= 1) {
                render();
                frames++;
                delta = 0;
            }

            if (System.currentTimeMillis() - timer >= 1000) {
                display.getFrame().setTitle("Ray casting | " + frames + " FPS");
                frames = 0;
                timer += 1000;
            }
        }

        stop();
    }

    private void render() {
        bs = display.getCanvas().getBufferStrategy();
        if (bs == null) {
            display.getCanvas().createBufferStrategy(3);
            return;
        }

        g = (Graphics2D) bs.getDrawGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, width, height);

        g.setColor(Color.WHITE);
        for (Line2D.Float bound : bounds) {
            g.drawLine((int) bound.x1, (int) bound.y1, (int) bound.x2, (int) bound.y2);
        }

        g.setColor(Color.LIGHT_GRAY);
        rays = calculateRays(bounds, (int) mouseInput.getMouseX(), (int) mouseInput.getMouseY(), 180, 3000);
        for (Line2D.Float ray : rays) {
            g.drawLine((int) ray.x1, (int) ray.y1, (int) ray.x2, (int) ray.y2);
        }

        g.dispose();
        bs.show();
    }

    private ArrayList<Line2D.Float> calculateRays(ArrayList<Line2D.Float> bounds, int mouseX, int mouseY, int resolution, int maxDistance) {
        ArrayList<Line2D.Float> rays = new ArrayList<>();

        for (int i = 0; i < resolution; i++) {
            double dir = (Math.PI * 2) * ((double) i / resolution);
            float minDistance = maxDistance;
            for (Line2D.Float bound : bounds) {
                float distance = Utils.getRayCast(mouseX, mouseY, mouseX + (float) Math.cos(dir) * maxDistance, mouseY + (float) Math.sin(dir) * maxDistance,
                        bound.x1, bound.y1, bound.x2, bound.y2);
                if (distance < minDistance && distance > 0) {
                    minDistance = distance;
                }
            }
            rays.add(new Line2D.Float(mouseX, mouseY, mouseX + (float) Math.cos(dir) * minDistance, mouseY + (float) Math.sin(dir) * minDistance));
        }

        return rays;
    }

    public synchronized void start() {
        running = true;
        thread = new Thread(this);
        thread.start();
    }

    public synchronized void stop() {
        running = false;
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
