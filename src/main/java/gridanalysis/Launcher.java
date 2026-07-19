package gridanalysis;

import javafx.application.Application;

/**
 * Application entry point that avoids the JDK's special JavaFX launcher path.
 */
public final class Launcher {

    private Launcher() {
    }

    public static void main(String[] args) {
        Application.launch(GridAnalysis.class, args);
    }
}
