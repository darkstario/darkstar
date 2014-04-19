package io.darkstar.lang;

import org.springframework.beans.BeanUtils;
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
            return newInstance(clazz, (Map)config);
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
            return applyProperties(object, (Map)config);
        }
        throw new UnsupportedOperationException("Cannot yet support shortcut single value configs.");
    }

    public static <T> T applyProperties(T object, Map<String, ?> props) {
        for (Map.Entry<String, ?> entry : props.entrySet()) {
            Field field = ReflectionUtils.findField(object.getClass(), entry.getKey());
            ReflectionUtils.setField(field, object, entry.getValue());
        }
        return object;
    }

}
