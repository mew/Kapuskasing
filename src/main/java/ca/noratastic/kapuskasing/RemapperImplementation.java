package ca.noratastic.kapuskasing;

import org.objectweb.asm.commons.Remapper;

import static ca.noratastic.kapuskasing.Main.fields;
import static ca.noratastic.kapuskasing.Main.methods;

public class RemapperImplementation extends Remapper {
    @Override
    public String mapMethodName(String owner, String name, String descriptor) {
        return methods.getOrDefault(name, name);
    }

    @Override
    public String mapFieldName(String owner, String name, String descriptor) {
        return fields.getOrDefault(name, name);
    }
}
