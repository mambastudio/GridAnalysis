package gridanalysis;

import javafx.application.Application;

/**
 * Plain Java entry point for the expansion laboratory. Keeping this separate
 * avoids the JDK's special JavaFX Application launcher path.
 */
public final class ExpansionDebugLauncher {

    private ExpansionDebugLauncher() {
    }

    public static void main(String[] args) {
        Application.launch(ExpansionDebug.class, args);
    }
}
