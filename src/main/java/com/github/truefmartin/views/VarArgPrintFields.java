package com.github.truefmartin.views;

import com.github.truefmartin.models.Player;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;

public class VarArgPrintFields {
    //    Field[] getFields();
    public String varArgPrintFields(int... args) {
        setVarArgFields(args);
        return printPreSetFields();
    }

    public void setVarArgFields(int... indexes) {
        fieldsToPrint.clear();
        for (int i : indexes) {
            fieldsToPrint.add(i);
        }
    }

    public String printPreSetFields(int... indents) {
        String indent = "";
        if (indents.length > 0) {
            indent = "\t".repeat(indents[0]);
        }
        var fields = this.getClass().getFields();
        StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getSimpleName()).append(": {");
        for (int i = 0; i < fields.length; i++) {
            if (fieldsToPrint.contains(i)) {
                sb.append("\n\t").append(indent);
                int nestedIndent = 1;
                if (indents.length > 0) {
                    nestedIndent += indents[0];
                }
                try {
                    var fieldValue = fields[i].get(this);
                    if (fieldValue instanceof Collection<?> collectionCasted) {

                        sb.append(fields[i].getName()).append("=[").append("\n");
                        for (Object o : collectionCasted) {
                            if (o instanceof VarArgPrintFields casted) {
                                if (casted.isPreSetFieldsEmpty()) {
                                    casted.setVarArgFields(0);
                                }

                                sb.append(casted.printPreSetFields(nestedIndent));
                                sb.append(indent).append(",\n");
                            }
                        }
                        sb.append(indent).append("\t],");
                    } else {
                        if (fieldValue instanceof VarArgPrintFields casted) {
                            sb.append(fields[i].getName()).append("=").append(casted.printPreSetFields(nestedIndent)).append(",");
                        } else {
                            sb.append(fields[i].getName()).append("=").append(fieldValue).append(",");
                        }
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        sb.append("\n").append(indent).append("}");
        return sb.toString();
    }

    public ArrayList<Integer> fieldsToPrint = new ArrayList<>();

    public boolean isPreSetFieldsEmpty() {
        return fieldsToPrint.isEmpty();
    }

}
