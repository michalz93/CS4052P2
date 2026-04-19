package model;

import java.util.Arrays;
import java.util.Set;

/**
 * Each transition may have a set of actions to be performed. 
 * 
 **/
public class  BTransition {
    private String source;
    private String target;
    private String action;
	
    /**
     * Returns the source state of a transition.
     * @return the id of the source state
     ***/
    public String getSource() {
	return source;
    }
    /**
     * Returns the target state of a transition.
     * @return the id of the target state
     * */
    public String getTarget() {
	return target;
    }
    /**
     * Returns the set of actions in a transition.
     * @return a set of actions.
     * */
    public String getActions() {
	return action;
    }
    
	
    @Override
    public String toString() {
	StringBuilder sb = new StringBuilder();
	sb.append(this.source+"-");
	sb.append(this.action+"-");
	sb.append(this.target);
	return sb.toString();
    }
	
}
