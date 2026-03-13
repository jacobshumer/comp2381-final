package dataviewer2;

public class DebugManagerFactory {
    public static DebugManager createDefault() {
        return new DebugManager(false, true);
    }
    public static DebugManager createSilent() {
        return new DebugManager(false, false);
    }
    public static DebugManager createVerbose() {
        return new DebugManager(true, true);
    }
}
