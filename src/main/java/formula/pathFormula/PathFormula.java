package formula.pathFormula;

public abstract class PathFormula {
    public abstract void writeToBuffer(StringBuilder buffer);

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        writeToBuffer(buffer);
        return buffer.toString();
    }
}
