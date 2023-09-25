package ed.inf.adbs.minibase.base;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * This class abstract the concept of schema.
 * It contains a list of string which refers to the type of attributes.
 * For convenience, its toString, hashcode and equals method are
 * overridden.
 */
public class Schema {

    private List<String> types;

    public Schema(String line) {
        assert line != null;
        line = line.trim();
        String[] lineUnits = line.split(" ");
        this.types = new ArrayList<>(Arrays.asList(lineUnits));
    }

    public Schema(List<String> types) {

        assert types != null;
        this.types = types;
    }


    public String getType(int i) {
        return types.get(i);
    }

    public List<String> getTypes() {
        return types;
    }

    @Override
    public String toString() {
        return "Schema{" +
                "types=" + types +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Schema)) return false;
        Schema schema = (Schema) o;
        if(getTypes().size() != schema.getTypes().size()) return false;
        for(int i=0; i<getTypes().size(); i++){
            if(!getType(i).equals(schema.getType(i))) return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTypes());
    }
}
