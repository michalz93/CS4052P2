package formula.pathFormula;

import formula.stateFormula.StateFormula;

public class NotPath extends PathFormula {
    public final PathFormula inner;

    public NotPath(PathFormula inner) {
        this.inner = inner;
    }

    @Override
    public void writeToBuffer(StringBuilder buffer) {
        buffer.append("!(");
        inner.writeToBuffer(buffer);
        buffer.append(")");
    }
}