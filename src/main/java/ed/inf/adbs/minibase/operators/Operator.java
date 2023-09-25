package ed.inf.adbs.minibase.operators;

import ed.inf.adbs.minibase.base.Tuple;

/**
 * This class defines the interface of Operator.
 */
public abstract class Operator {
    public abstract Tuple getNextTuple();
    public abstract void reset();
    public abstract void dump();
}