import java.util.logging.Logger;

public class Main {
    public Logger logger = Logger.getLogger(Main.class.getName());

    public Main() {
        // Create Environment
        Environment env = new Environment(); //Creates only a creature and some apples
        AgentMind a = new AgentMind(env); // Creates the Agent Mind and start it
    }

    public static void main(String[] args) {
        Main m = new Main();
    }
}
