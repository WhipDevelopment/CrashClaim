package net.crashcraft.crashclaim.crashutils;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public class ServiceUtil {
    public static <T> T getService(Class<T> clazz) {
        return ServiceLoader.load(clazz).findFirst().orElseThrow(() -> new IllegalStateException("No service found for " + clazz.getName()));
    }

    public static <T> T getService(Class<T> clazz, T def) {
        return ServiceLoader.load(clazz).findFirst().orElse(def);
    }

    public static <T> T getService(Class<T> clazz, ClassLoader loader) {
        return ServiceLoader.load(clazz, loader).findFirst().orElseThrow(() -> new IllegalStateException("No service found for " + clazz.getName()));
    }

    public static <T> T getService(Class<T> clazz, ClassLoader loader, T def) {
        return ServiceLoader.load(clazz, loader).findFirst().orElse(def);
    }

    public static <T> List<T> getServices(Class<T> clazz) {
        final List<T> services = new ArrayList<>();
        ServiceLoader.load(clazz).iterator().forEachRemaining(services::add);
        return services;
    }

    public static <T> List<T> getServices(Class<T> clazz, ClassLoader loader) {
        final List<T> services = new ArrayList<>();
        ServiceLoader.load(clazz, loader).iterator().forEachRemaining(services::add);
        return services;
    }

    public static <T> List<T> getServices(Class<T> clazz, List<T> def) {
        final List<T> services = new ArrayList<>();
        ServiceLoader.load(clazz).iterator().forEachRemaining(services::add);
        if (services.isEmpty()) {
            return def;
        }
        return services;
    }

    public static <T> List<T> getServices(Class<T> clazz, ClassLoader loader, List<T> def) {
        final List<T> services = new ArrayList<>();
        ServiceLoader.load(clazz, loader).iterator().forEachRemaining(services::add);
        if (services.isEmpty()) {
            return def;
        }
        return services;
    }
}
