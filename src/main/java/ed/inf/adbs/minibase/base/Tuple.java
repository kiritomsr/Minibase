package ed.inf.adbs.minibase.base;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;

/**
 * This class encapsulate the data of tuple, with corresponding
 * schema and terms. The list of values, terms and the schema
 * holds the actual value, variable names, value types in sequence.
 * A HashMap mapping is for the convenience of finding the value
 * with given variable name.
 * For convenience, its toString, hashcode and equals method are
 * overridden.
 */
public class Tuple {
    private List<Object> values;
    private List<Term> terms;
    private Schema schema;
    private HashMap<Term,Object> mapping;

    public Tuple(String row, List<Term> terms, Schema schema) {
        assert row != null && terms != null && schema != null: "null";
        this.terms = terms;
        this.schema = schema;
        row = row.trim();
        String[] value = row.split(", ");
        assert value.length==terms.size() && value.length==schema.getTypes().size(): "size";
//        if(value.length != schema.getTypes().size()) value[value.length] = "";
        this.values = new ArrayList<>();
        this.mapping = new HashMap<>();
        for(int i=0;i<value.length;i++){
            Object o;
            if(this.schema.getTypes().get(i).equals("int")){
                o = Integer.parseInt(value[i]);
            }else {
//                o = ((String) value[i]).replace("'", "");
                o = (String) value[i];
            }
            values.add(o);
            mapping.put(terms.get(i), o);
        }
    }

    public Tuple(List<Object> values, List<Term> terms, Schema schema) {
        assert values != null && terms != null && schema != null: "null";
        assert values.size()==terms.size() && values.size()==schema.getTypes().size():"size";
        this.values = values;
        this.terms = terms;
        this.schema = schema;
        this.mapping = new HashMap<>();
        for(int i = 0; i< values.size(); i++){
            mapping.put(terms.get(i), values.get(i));
        }
    }

    public Object getValue(int index){
        return values.get(index);
    }

    public Object getValue(Term key) {
        if(key instanceof Variable){
            for(Term term: mapping.keySet()){
                if(((Variable) term).getName().equals(((Variable) key).getName())) return mapping.get(term);
            }
        }else if(key instanceof SumAggregate){
            for(Term term: mapping.keySet()){
                if(term instanceof SumAggregate && term.toString().equals(key.toString())) return mapping.get(term);
            }
        }
        return null;
    }

    public List<Object> getValues() {
        return values;
    }

    public Term getTerm(int index) {
        return terms.get(index);
    }

    public List<Term> getTerms() {
        return terms;
    }

    public Schema getSchema() {
        return schema;
    }

    @Override
    public String toString() {
        return "Tuple{" +
                "value=" + values +
                ", terms=" + terms +
                ", schema=" + schema +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tuple)) return false;
        Tuple tuple1 = (Tuple) o;
        if(!getSchema().equals(tuple1.getSchema())) return false;
        for(int i = 0; i< getValues().size(); i++){
            if(!getValue(i).equals(tuple1.getValue(i)) || !getTerm(i).equals(tuple1.getTerm(i))) return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getValues(), getTerms(), getSchema());
    }

}
