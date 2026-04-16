package modelChecker;

import formula.stateFormula.StateFormula;
import model.Model;

/**
 * Defines the interface to model checker.
 *
 **/
public interface ModelChecker {
    /**
     * verifies whether the model satisfies the query under the given
     * constraint.
     * 
     * @param model
     *            - model to verify
     * @param query
     *            - the state formula to verify the model against.
     * @return - true if the model satisfies the query.
     */
    public boolean check(Model model, StateFormula query);

    // Returns a trace of the previous check attempt if it failed.
    public String[] getTrace();
}
