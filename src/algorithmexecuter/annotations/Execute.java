package algorithmexecuter.annotations;

import algorithmexecuter.enums.FixedAlgorithmNames;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Execute {

    public FixedAlgorithmNames algorithmName();
    
}
