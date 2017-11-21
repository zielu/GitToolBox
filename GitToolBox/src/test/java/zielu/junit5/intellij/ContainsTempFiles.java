package zielu.junit5.intellij;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface ContainsTempFiles {
    boolean value() default true;
}
