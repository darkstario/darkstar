package io.darkstar.plugin.stereotype;

import org.springframework.stereotype.Component;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that an annotated class is a Darkstar Plugin which will be loaded at startup and for which Darkstar will
 * automatically perform lifecycle management.
 * <p>This annotation serves as a specialization of {@link org.springframework.stereotype.Component @Component},
 * allowing for implementation classes to be auto-detected through classpath scanning.</p>
 *
 * @see org.springframework.stereotype.Component
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface Plugin {

    /**
     * The name of the plugin.  If one is not specified, a name will be automatically generated based.
     * <p>For example, with a class name of {@code com.foo.whatever.darkstar.MyAwesomePlugin}, the auto-generated name
     * might be {@code myAwesomePlugin}.</p>
     *
     * @return the name of the plugin or the empty string (default) if a name will be automatically generated.
     */
    String value() default "";

    /**
     * A Set of the names of all attributes that can be inspected and processed by this plugin.  Whenever a
     * configuration attribute is discovered in <em>any</em> context matching one of these names, the plugin's
     * config handler method will be invoked.
     *
     * @return the names of supported attributes
     */
    String[] supportedAttributeNames() default "";

}
