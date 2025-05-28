public class Input {

    // Key variables
    public final boolean[] keyDown = new boolean[100];
    // Current and last key states for efficient key press detection
    private final boolean[] currentKeys = new boolean[100];
    private final boolean[] lastKeys = new boolean[100];

    // Mouse variables
    public final boolean[] click = new boolean[6];
    // Current and last click states for efficient click detection
    private final boolean[] currentClick = new boolean[6];
    private final boolean[] lastClick = new boolean[6];

    // Mouse position
    public final double[] mousePos = new double[2];

    // Mouse scroll value
    public double mouseScroll = 0;
    public static final double criticalMouseScroll = 0.25;

    public Input(){}

    public void update(){
        // Updates key values efficiently
        System.arraycopy(currentKeys, 0, lastKeys, 0, 100);
        System.arraycopy(keyDown, 0, currentKeys, 0, 100);

        // Updates click values efficiently
        System.arraycopy(currentClick, 0, lastClick, 0, 6);
        System.arraycopy(click, 0, currentClick, 0, 6);
        
        /*
        //Refreshed click and scroll trackers
        if (Math.abs(mouseScroll) > 2) {
            mouseScroll = Math.signum(mouseScroll) * 2;
        }
        mouseScroll += -Math.signum(mouseScroll) * 0.8;
        */
        mouseScroll *= criticalMouseScroll;
    }
    
    public boolean keyDown(int keyCode){
        return currentKeys[keyCode];
    }

    public boolean keyPressed(int keyCode){
        return currentKeys[keyCode] && !lastKeys[keyCode];
    }

    public boolean clickDown(int button){
        return currentClick[button];
    }

    public boolean clickPressed(int button){
        return currentClick[button] && !lastClick[button];
    }
}