package general;

import gui.GameScreen;

import java.awt.Color;
import java.awt.Graphics2D;

import comp.Component;
import scala.concurrent.duration.Duration$;
import scala.concurrent.duration.FiniteDuration;


/**
 * Klasse f�r Zeitbeschr�nkung und Anzeige.
 * <br><br>
 * <b>4.1.2014:</b> TimeClock erbt jetzt von Component. TimeClock ist jetzt in besserer
 * Handhabung.
 * 
 * @version 4.1.2014
 * 
 */
public class TimeClock extends Component implements Runnable {
	
	// VARIABLEN - INITIALISIERUNG
	
	/** Variable, ob die Zeit abl�uft: 
	 * true: Zeit l�uft ab
	 * false: TimeClock ist gestoppt */
	private boolean isRunning;

	/** aktuelle Zeit */
	private long timeCurrent = 0;

    private long sumTime = 0;
	
	/** String, der am Bildschirm die Zeit angeben soll */
	private String timePrintString = null;

	public final Delegate.Function0Delegate onTimeOver = new Delegate.Function0Delegate();

    private FiniteDuration _turnTime = null;

	// KONSTURCKTOR
	/** KONSTRUCKTOR der Klasse 
	 */
	public TimeClock () {
		super(Main.getWindowWidth() / 2 - 72 / 2, 25, 72, 26, 
				GameScreen.getInstance());
		stop();
	}
	
	/** �bernimmt timeSinceLastFrame + Aufruf durch 'Main'
	 *  .... updated die aktuelle Zeit; �bernimmt Thread */
	@Override
	public void run() {
		long lastTime = System.currentTimeMillis();
		while (true) {
			if (isRunning()) {
				timeCurrent = System.currentTimeMillis(); 
				sumTime = sumTime + (timeCurrent - lastTime);
				if (turnTime().toMillis() - sumTime <= 0) {
					isRunning = false;
                    timePrintString = timeFormatter(0);
					onTimeOver.call();
				} else {
					timePrintString = timeFormatter (turnTime().toMillis() - sumTime);
					lastTime = timeCurrent;
				}
			} else {
				try {
					Thread.sleep(30);
				} catch (InterruptedException e) {e.printStackTrace();}
			}
		}
	}

	/** stoppt die Ausf�hrung von TimeClock */
	public void stop () {
		isRunning = false;
	}
	
	/** started TimeClock */
	public void start () {
		isRunning = true;
	}
	
	/** setzt TimeClock auf maximale Zeit zur�ck
	 * HINWEIS: an Start/Stop wird nicht ge�ndert, also ggf. stop / start aufrufen */
	public synchronized void reset() {
		sumTime = 0;
	}
	
	
	/** Umwandlung einer long-Variable in einem String min:sec:ms */
	public static String timeFormatter(long millisecTime) {
        String time = null;
        if(millisecTime < 0){  //�berpr�ft ob die Zeit negativ ist
            //throw new RuntimeException("Negativ time value provided");
        	time = "00:00:000";
//        } else if (millisecTime > 357539999){   //�berpr�ft ob das Limit des Formates nicht �berschreitet
//            throw new RuntimeException("Time value exceeds allowed format");
        } else {
          long min = millisecTime / (60 * 1000);
          long sec = (millisecTime - min * 60 * 1000) / 1000;
          long ms = millisecTime - min * 60 * 1000 - sec * 1000;
          
          if (min <= 0)
        	  time = "00";
          else if (min < 10) 
        	  time = "0" + min;
          else 
        	  time = "" + min;
          
          time = time + ":";
          
          if (sec <= 0)
        	  time = time + "00";
          else if (sec < 10)
        	  time = time + "0" + sec;
          else 
        	  time = time + sec;
          
          time = time + ":";
          
          if (ms <= 0) 
        	  time = time + "000";
          else if (ms < 10) 
        	  time = time + "00" + ms;
          else if (ms < 100)
        	  time = time + "0" + ms;
          else 
        	  time = time + ms;
        }
        return time;
	}
	
	/** Umwandlung einer Zeitangabe in Millisekunden in einen String [min:sec] */
	public static String timeFormatterShort(long millisecTime) {
        String time = null;
        if(millisecTime<0){                        //�berpr�ft ob zeit negativ ist
            //throw new RuntimeException("Negativ time value provided");
        	
        	time = "00:00:000";
        }else if (millisecTime>357539999){                 //�berpr�ft ob das limit von format nicht �berschreitet
            throw new RuntimeException("Time value exceeds allowed format"); 
        }else {
          millisecTime = millisecTime/1000000;
          long min= (millisecTime/60);
          long sec= millisecTime-min*60;
          
          if(min < 10 && sec < 10){
              time = "0"+min+":"+"0"+sec;
          }
          if(min > 10 && sec < 10){
                  time = ""+min+":"+"0"+sec;
              }
          if(min < 10 && sec > 10){
                  time = "0"+min+":"+sec;
              }
          if(min > 10 && sec > 10){
                  time = ""+min+":"+sec;
              }
          
          
        } 
        return time;
    }
	
	/**
	 * UNUSED
	 * 
	 * gibt zur�ck ob der Zug enden muss oder nicht
	 * 
	 * @return true - wenn die maximale Zeit pro Zug ('timeMax') ohne die Vergangene Zeit ('timeCurrent') kleiner als 0
	 */
	public synchronized boolean isEnd () {
        return turnTime().toMillis() - timeCurrent < 0;
	}
	
	/** GETTER 
	 * @return timePrintString*/ 
	public synchronized String getTimePrintString () {
		return timePrintString;
	}
	
	/** 
	 * @return timeLeft - die �brige Zeit f�r diesen Zug
	 */
	public synchronized long getMilliDeath() {
		return turnTime().toMillis() - timeCurrent;
	}
	
	public boolean isRunning() {
		return isRunning;
	}

    /** Returns the time in which a player is allowed to make moves. <p>
     *
     * If the underlying turn time variable is null, this method returns Duration.Inf,
     * otherwise it returns the underlying turn time variable directly. <p>
     *
     * For direct time calculation, use time conversion methods provided with the Duration object:
     * <code>toMillis, toNanos, toMinutes, toSeconds</code>
     */
    public scala.concurrent.duration.Duration turnTime ()
    {
        if(_turnTime == null)
            return Duration$.MODULE$.Inf();
        else
            return _turnTime;
    }

    /** Sets the new turn time.
     *
     * The new value may be <code>null</code>. In case of <code>null</code> the turn time
     * defaults to infinite time.
     */
    public void setTurnTime (FiniteDuration turnTime) {
        _turnTime = turnTime;
    }

    /** Returns true if the turn time is infinite. */
    public boolean isTurnTimeInfinite () {
        return _turnTime == null;
    }

	@Override
	public void draw(Graphics2D g) {
		g.setColor(Color.DARK_GRAY.brighter());
		g.fillRoundRect(getX() - 2,
				getY() - 2,
				getWidth() + 4,
				getHeight() + 4, 8, 5);
		g.setColor(Color.LIGHT_GRAY);
		g.fillRoundRect(getX(),
				getY(),
				getWidth(),
				getHeight(), 30, 12);
		
		g.setColor(Color.BLACK);
		g.setFont(STD_FONT);
        g.drawString(this.getTimePrintString(), getX() + 4, getY() + 16);
	}
}
