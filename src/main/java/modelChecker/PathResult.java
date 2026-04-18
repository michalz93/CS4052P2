package modelChecker;

import java.util.*;

public class PathResult {
    public boolean holds;
    public List<String> pathTrace = new ArrayList<>();


    public PathResult(boolean holds, List<String> pathTrace) {
        this.holds = holds;
        this.pathTrace = pathTrace;
    }
}
