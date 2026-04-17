package modelChecker;

import formula.stateFormula.*;
import formula.pathFormula.*;
import model.*;

public class SimpleModelChecker implements ModelChecker {
    private String trace;

    @Override
    public boolean check(Model model, StateFormula query) {
        System.out.println("Query: " + query);
        trace = ""; 
        State[] initialStates = model.getInitialStates();
        

        for (State s : initialStates) {
            System.out.println(s.getName());
            trace = s.getName();
            if (!checkState(model, s , query)) {
                return false;
            }
        }
        return true;
    }

    public boolean checkState(Model model, State state ,StateFormula query) {
        if (query instanceof BoolProp) {
            BoolProp bp = (BoolProp) query;
            return bp.value;
        } else if (query instanceof Not) {
            Not not = (Not) query;
            StateFormula st = not.stateFormula;
            return !checkState(model, state, st);
        } else if (query instanceof And) {
            And and = (And) query;
            StateFormula left = and.left;
            StateFormula right = and.right;
            return (checkState(model, state, left) && checkState(model, state, right));
        } else if (query instanceof Or) {
            Or or = (Or) query;
            StateFormula left = or.left;
            StateFormula right = or.right;
            return (checkState(model, state, left) || checkState(model, state, right));
        } else if (query instanceof AtomicProp) {
            AtomicProp atom = (AtomicProp) query;
            String label = atom.label;
            String[] stateLabels = state.getLabel();

            for (String stateLabel : stateLabels) {
                if (label.equals(stateLabel)) {
                    return true;
                }
            }

            return false;
        } else if (query instanceof ForAll) {
            ForAll forAll = (ForAll) query;
            Transition[] transitions = model.getTransitions();

            for (Transition transition: transitions) {
                if (transition.getSource().equals(state.getName())) {
                    boolean result = checkPathFormula(model, state, model.getState(transition.getTarget()), forAll.pathFormula);
                    if (!result) return false;
                }
            }

            return true;
        }

        return false;
    }

    public boolean checkPathFormula(Model model, State source, State target, PathFormula pathFormula) {
        if (pathFormula instanceof Next) {
            Next next = (Next) pathFormula;
            return checkState(model, target, next.stateFormula);
        }
        return false;
    }

    @Override
    public String[] getTrace() {
        // *TODO Auto-generated method stub
        return null;
    }

}
