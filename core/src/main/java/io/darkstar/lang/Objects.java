package io.darkstar.lang;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.Map;

@SuppressWarnings("unchecked")
public class Objects {

    public static <T> T newInstance(Class<T> clazz, Object config) {
        if (config == null) {
            return BeanUtils.instantiate(clazz);
        }
        if (clazz.isInstance(config)) {
            T instance = BeanUtils.instantiate(clazz);
            BeanUtils.copyProperties(config, instance);
            return instance;
        }
        if (config instanceof Map) {
            Map<String,?> m = (Map<String,?>)config;
            return newInstance(clazz, m);
        }
        throw new UnsupportedOperationException("Cannot yet support shortcut single value configs.");
    }

    public static <T> T newInstance(Class<T> clazz, Map<String, ?> props) {
        T instance = BeanUtils.instantiate(clazz);
        return applyProperties(instance, props);
    }

    public static <T> T applyProperties(T object, Object config) {
        if (object.getClass().isInstance(config)) {
            BeanUtils.copyProperties(config, object);
            return object;
        }
        if (config instanceof Map) {
            Map<String,?> m = (Map<String,?>)config;
            return applyProperties(object, m);
        }
        throw new UnsupportedOperationException("Cannot yet support shortcut single value configs.");
    }

    public static <T> T applyProperties(T object, Map<String, ?> props) {
        for (Map.Entry<String, ?> entry : props.entrySet()) {
            Field field = ReflectionUtils.findField(object.getClass(), entry.getKey());
            field.setAccessible(true);
            ReflectionUtils.setField(field, object, entry.getValue());
        }
        return object;
    }


    public static <T> T get(Class<T> type, Object o, String propPath) {
        if (o == null) {
            return null;
        }

        ExpressionParser parser = new SpelExpressionParser();
        Expression expression = parser.parseExpression(propPath);
        return expression.getValue(o, type);
    }

}
