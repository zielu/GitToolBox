package zielu.intellij.junit5;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface ContainsTempFiles {
    boolean value() default true;
}
