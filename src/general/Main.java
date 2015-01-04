package general;

import akka.actor.ActorSystem;
import animation.SoundPool;
import general.io.PreInitStage;
import gui.ArrowSelectionScreenPreSet;
import gui.LoadingWorldScreen;
import gui.PreWindowScreen;
import gui.Screen;
import player.weapon.ArrowHelper;
import scala.runtime.AbstractFunction1;
import scala.runtime.BoxedUnit;

import java.awt.*;

/**
 * Hauptklasse mit der Main-Methode und den abstraktesten Objekten unseres Spiels.
 * <p>27.02.2014</p>
 * <ul>
 * <li>Updater hinzugef�gt. Der Updater stellt sicher, dass die Daten von Objekten geupdated werden.</li>
 * </ul>
 *
 * @version 1.3.2014
 */
public class Main {

    // NUR INTIALISIERUNG - WIE WERTE UND VARIABLEN ###############

    /** the width of the displayed window
     * @return getMaximumWindowBounds().width
     * @see general.Main#getWindowHeight()
     * @see general.Main#getWindowDimensions() */
    public static int getWindowWidth() { return GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().width; }
    
    /** the height of the displayed window
     * @return getMaximumWindowBounds().height
     * @see Main#getWindowHeight()
     * @see Main#getWindowDimensions() */
    public static int getWindowHeight() { return GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().height; }

    private static GameWindow gameWindow;
    private static GraphicsDevice graphicsDevice;
    private static Main main;

    // Just user data. User data is not intertwined with game data.
	// The game data uses data from user, but the user does not use any data from world.
    private static User user;

	// The game context in which the game currently is.
	// Every time a game is started, the context variable should be non-null.
	private static PfeileContext context = null;

	// The actor system taking care of threaded actors.
	private static ActorSystem actorSystem = ActorSystem.create("system");

    // DONE WITH ALL VARIABELS;
    // MOST IMPORTANT METHODS ####################################
    // ###########################################################

    /**
     * F�hrt nicht die main-Funktion aus, sondern gibt eine Referenz auf das
     * Main-Objekt zur�ck.
     *
     * @return Referenz auf das Main-Objekt. Von hier aus kann auf fast alles
     * zugegriffen werden.
     */
    public static Main getMain() {
        return main;
    }
    // KONSTRUKTOR ###############################################
    /**
     * Der Konstruktor. Hier stehen keine Hauptaufrufe. Die Hauptaufrufe werden
     * in <code>foo()</code> get�tigt.
     */
    private Main() {
    }

    // ###########################################################
    // HIER STEHEN DIE HAUPTAUFRUFE ##############################
    // ################### IMPORTANT #############################
    // ###########################################################

    /**
     * Main-Method �ffnet eine neue Instanz von Main: main
     */
    public static void main(String[] arguments) {
	    PreInitStage.execute();

        // Let's begin playing the title song (so the user knows, that something is done while loading the game)
        SoundPool.play_titleMelodie();

        user = new User("Just a user");

        main = new Main();
        main.printSystemProperties();

        GraphicsEnvironment environmentG = GraphicsEnvironment.getLocalGraphicsEnvironment();
        graphicsDevice = environmentG.getDefaultScreenDevice();
        gameWindow = new GameWindow();

        new player.weapon.ArrowHelper();

        gameWindow.initializeScreens();

        GameWindow.adjustWindow(gameWindow);
        toggleFullscreen(true);

        // window showing process
        gameWindow.createBufferStrategy();
        gameWindow.setVisible(true);

	    ArrowSelectionScreenPreSet.getInstance().onScreenLeft.register(new AbstractFunction1<Screen.ScreenChangedEvent, BoxedUnit>() {
		    @Override
		    public BoxedUnit apply(Screen.ScreenChangedEvent v1) {
			    main.disposeInitialResources();
			    return BoxedUnit.UNIT;
		    }
	    });

	    LoadingWorldScreen.getInstance().onScreenLeft.register(new AbstractFunction1<Screen.ScreenChangedEvent, BoxedUnit>() {
		    @Override
		    public BoxedUnit apply(Screen.ScreenChangedEvent v1) {
			    main.doArrowSelectionAddingArrows();
                getContext().onStartRunningTimeClock().call();
                // the players have been added to entityList, so this call is valid now
                PreWindowScreen.correctArrowNumber();
			    return BoxedUnit.UNIT;
		    }
	    });

        // play the sound
        SoundPool.stop_titleMelodie();
        SoundPool.playLoop_mainThemeMelodie(SoundPool.LOOP_COUNTINOUSLY);

        // starten wir das Spiel
        main.runGame();

		/*
         * TODO Hier kommt cleanup-code, z.B. noch offene Dateien schließen.
		 * Ich weis, noch nichts zum Aufr�umen hinterher, aber wir werden es sp�ter
		 * 100% brauchen.
		 */

        // sanftes Schlie�en des GameWindows anstelle des harten System.exit(0)
        gameWindow.dispose();

        // stop all melodies
        SoundPool.stop_allMelodies();

        // There is no other way, that closes the games.
        // Some Threads were still running in background, that continued the game without seeing a screen.
        System.exit(0);
    }

    /** EMPTY */
    private void disposeInitialResources() {

    }

    // ############ RUN GAME

    /**
     * Hier laeuft das Spiel nach allen Insizialisierungen
     */
    private void runGame() {
	    /*
        // reset TimeClock first, because with the change of the activePlayer a delegate in PfeileContext starts TimeClock and the activePlayer is set before this line
        getContext().getTimeClock().reset();
        // start TimeClock
        getContext().getTimeClock().start();
        */

        GameLoop.run(1 / 60.0);

        // TODO: System, bei der nach jeder Runde der Bonusauswahlbildschirm und ArrowSelectionPreSet kommt
    }

    // #########################################################
    // METHODEN: v. a. alle Threads ############################
    // #########################################################

    /**
     * Fuegt die Pfeile in das Inventar des Spielers ein
     */
    private void doArrowSelectionAddingArrows() {
        final ArrowSelectionScreenPreSet arrowSelection = ArrowSelectionScreenPreSet.getInstance();

        for (String selectedArrow : arrowSelection.selectedArrows) {
            if (!Main.getContext().getActivePlayer().inventory().put(
		            ArrowHelper.instanceArrow(selectedArrow)))
                System.err.println("Cannot add " + selectedArrow + " at Main.doArrowSelectionAddingArrows() - adding the arrowNumberPreSet");
        }
    }

    // ###### GENERATE WORLD
    // ###### SPAWN-SYSTEM

    /**
     * Prints all system properties to console.
     */
    private void printSystemProperties() {
        SystemProperties sys_props_out_thread = new SystemProperties();
        sys_props_out_thread.setPriority(Thread.MIN_PRIORITY);
        sys_props_out_thread.start();
    }


    /**
     * Toggles between fullscreen mode and windowed mode.
     *
     * @param fullscreen The fullscreen flag.
     */
    protected static void toggleFullscreen(boolean fullscreen) {
        if (fullscreen) {
            if (graphicsDevice.getFullScreenWindow() == gameWindow) {
                System.out.println("Already fullscreen.");
                return;
            }
            graphicsDevice.setFullScreenWindow(gameWindow);
        } else {
            if (graphicsDevice.getFullScreenWindow() != gameWindow) {
                System.out.println("Already windowed.");
                return;
            }
            graphicsDevice.setFullScreenWindow(null);
        }
    }

    // ########################################################################
    // UNIMPORTANT METHODS -- NOT USED METHODS -- DEPRECATED METHODS ##########
    // ########################################################################
	// None right now.

    // ########################################
    // GETTERS ################################
    // SETTERS ################################
    // ########################################

    /**
     * Abmessungen des Fensters: <code> new Rectangle (0, 0, Main.getWindowWidth(), Main.getWindowHeight()); </code>
     */
    public static Rectangle getWindowDimensions() {
        return GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
    }

    /**
     * GETTER: GameWindow
     */
    public static GameWindow getGameWindow() {
        return gameWindow;
    }

    public static User getUser() {
        return user;
    }

	public static PfeileContext getContext() {
		return context;
	}

	public static ActorSystem getActorSystem() {
		return actorSystem;
	}

	public static void setContext(PfeileContext context) {
		scala.Predef.require(context != null);
		Main.context = context;
	}
}
