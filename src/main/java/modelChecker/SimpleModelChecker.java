package modelChecker;

import formula.stateFormula.StateFormula;
import model.Model;

public class SimpleModelChecker implements ModelChecker {

    @Override
    public boolean check(Model model, StateFormula query) {
        System.out.println("Query: " + query);

        //if (query instanceof(BoolFormula))



        return false;
    }

    @Override
    public String[] getTrace() {
        // *TODO Auto-generated method stub
        return null;
    }

}
