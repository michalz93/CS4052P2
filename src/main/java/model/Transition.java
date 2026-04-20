package model;

import java.util.Arrays;
import java.util.Set;

/**
 * Each transition may have a set of actions to be performed. 
 * 
 **/
public class  Transition {
    private String source;
    private String target;
    private String [] actions;
	
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
    public String[] getActions() {
	return actions;
    }

    public boolean hasAllowedAction(Set<String> allowedActions) {
        for (String action : actions) {
            if (allowedActions.contains(action)) {
                return true;
            }
        }
        return false;
    }

    public boolean allActionsAllowed(Set<String> allowedActions) {
        for (String action : actions) {
            if (!allowedActions.contains(action)) {
                return false; // Can move to the state in not allowed way.
            }
        }
        return true;
    }

    public String getIllegalAction(Set<String> allowedActions) {
        for (String action : actions) {
            if (!allowedActions.contains(action)) {
                return action; // Can move to the state in not allowed way.
            }
        }
        return null;
    }

    public String getAllowedAction(Set<String> allowedActions) {
        System.out.println("getAllowedAction, actioms: " + Arrays.toString(actions) + " allowedActions: " + allowedActions);
        for (String action : actions) {
            if (allowedActions.contains(action)) {
                return action;
            }
        }
        return null;
    }
	
    @Override
    public String toString() {
	StringBuilder sb = new StringBuilder();
	sb.append(this.source+"-");
	sb.append(Arrays.toString(this.actions)+"-");
	sb.append(this.target);
	return sb.toString();
    }
	
}
