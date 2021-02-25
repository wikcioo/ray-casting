package visualiser;

public class Main {
    public static void main(String[] args) {
        System.setProperty("sun.java2d.opengl", "True");
        Visualiser visualiser = new Visualiser("Ray casting", 1280, 720);
        visualiser.start();
    }
}
