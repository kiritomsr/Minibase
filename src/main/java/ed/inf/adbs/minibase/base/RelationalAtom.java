package ed.inf.adbs.minibase.base;

import ed.inf.adbs.minibase.Utils;

import java.util.List;
import java.util.Objects;

public class RelationalAtom extends Atom {
    private String name;

    private List<Term> terms;

    public RelationalAtom(String name, List<Term> terms) {
        this.name = name;
        this.terms = terms;
    }

    public String getName() {
        return name;
    }

    public List<Term> getTerms() {
        return terms;
    }

    @Override
    public String toString() {
        return name + "(" + Utils.join(terms, ", ") + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RelationalAtom)) return false;
        RelationalAtom atom = (RelationalAtom) o;

        if(!this.getName().equals(atom.getName())) return false;
        for(int i=0; i<this.getTerms().size(); i++) {
            if(!(this.getTerms().get(i)).equals(atom.getTerms().get(i))) return false;
        }
        return true;
//        return Objects.equals(getName(), atom.getName()) && Objects.equals(getTerms(), atom.getTerms());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getTerms());
    }
}
