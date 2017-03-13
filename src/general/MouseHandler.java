package general;

import gui.screen.ScreenManager;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MouseHandler extends MouseAdapter {

    private final ScreenManager redirect;

    /**
     * Should events fired by AWT be synchronized to the main thread?
     * Could be helpful to resolve some bugs and data races we have not been able to inspect.
     */
    // Declared final to enable the compiler to optimize away calls to 'awtScheduledEvents', if necessary.
    private final boolean syncAwtEvents = true;

    /**
     * Any callbacks that have been scheduled to be called by the main thread.
     * On beginning the next update cycle, the main thread calls every stored callback and clears this map.
     * This functionality is only enabled if {@code syncAwtEvents == true}.
     */
    private final Map<String, VoidConsumer> awtScheduledEvents = syncAwtEvents ? new ConcurrentHashMap<>() : null;
    // (syncAwtEvents == false) equivalent to expression (awtScheduledEvents == null)

    MouseHandler(ScreenManager redirect) {
        this.redirect = redirect;
    }

    private void processMouseEvent(String methodHandlerKey, VoidConsumer eventProcessor) {
        if(awtScheduledEvents != null) {
            synchronized(awtScheduledEvents) {
                if(!awtScheduledEvents.containsKey(methodHandlerKey)) {
                    awtScheduledEvents.put(methodHandlerKey, eventProcessor);
                }
            }
        } else {
            eventProcessor.call();
        }
    }

    @Override
    public void mousePressed(final MouseEvent e) {
        processMouseEvent("mousePressed", () -> redirect.getActiveScreen().mousePressed(e));
    }

    @Override
    public void mouseReleased(final MouseEvent e) {
        processMouseEvent("mouseReleased", () -> redirect.getActiveScreen().mouseReleased(e));
    }

    @Override
    public void mouseDragged(final MouseEvent e) {
        processMouseEvent("mouseDragged", () -> redirect.getActiveScreen().mouseDragged(e));
    }

    @Override
    public void mouseMoved(final MouseEvent e) {
        processMouseEvent("mouseMoved", () -> redirect.getActiveScreen().mouseMoved(e));
    }

    @Override
    public void mouseWheelMoved(final MouseWheelEvent e) {
        processMouseEvent("mouseWheelMoved", () -> redirect.getActiveScreen().mouseWheelMoved(e));
    }

    /**
     * Calls every callback scheduled by the AWT event thread and clears the callback map for the next
     * update cycle.
     */
    void flushCallbacks() {
        if(syncAwtEvents) {
            Collection<VoidConsumer> fns;

            synchronized(awtScheduledEvents) {
                if(awtScheduledEvents.isEmpty()) {
                    return;
                }
                fns = new LinkedList<>();
                fns.addAll(awtScheduledEvents.values());
                awtScheduledEvents.clear();
            }

            fns.forEach(VoidConsumer::call);
        }
    }

}
