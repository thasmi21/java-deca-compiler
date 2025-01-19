package fr.ensimag.deca.context;

import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.deca.tree.Location;
import org.apache.commons.lang.Validate;

/**
 * Type defined by a class.
 *
 * @author gl06
 * @date 01/01/2025
 */
public class ClassType extends Type {
    
    protected ClassDefinition definition;
    
    public ClassDefinition getDefinition() {
        return this.definition;
    }
            
    @Override
    public ClassType asClassType(String errorMessage, Location l) {
        return this;
    }

    @Override
    public boolean isClass() {
        return true;
    }

    @Override
    public boolean isClassOrNull() {
        return true;
    }

    /**
     * Standard creation of a type class.
     */
    public ClassType(Symbol className, Location location, ClassDefinition superClass) {
        super(className);
        this.definition = new ClassDefinition(this, location, superClass);
    }

    /**
     * Creates a type representing a class className.
     * (To be used by subclasses only)
     */
    protected ClassType(Symbol className) {
        super(className);
    }
    

    @Override
    public boolean sameType(Type otherType) {
        ClassType classType = definition.getType();
        if (otherType instanceof ClassType) {
            ClassType otherClassType = (ClassType) otherType;
            return classType.equals(otherClassType);
        }
        return false;
    }

    /**
     * Return true if potentialSuperClass is a superclass of this class.
     */
    public boolean isSubClassOf(ClassType potentialSuperClass) {
        if (potentialSuperClass == null) {
            return false;
        }
        if (potentialSuperClass.isClass()) {
            ClassDefinition classDefinition=definition.getSuperClass();
            while(classDefinition!=null) {
                if(classDefinition.getType().equals(potentialSuperClass)) {
                    return true;
                }
                classDefinition=classDefinition.getSuperClass();
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ClassType) {
            ClassType classType = (ClassType) obj;
            if(definition.getType().getName().equals(classType.getName())) {
                return true;
            }
        }
        return false;
    }
}
