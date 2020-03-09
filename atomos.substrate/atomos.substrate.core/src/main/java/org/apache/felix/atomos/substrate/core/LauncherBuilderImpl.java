package org.apache.felix.atomos.substrate.core;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.felix.atomos.substrate.api.Launcher;
import org.apache.felix.atomos.substrate.api.LauncherBuilder;
import org.apache.felix.atomos.substrate.api.plugin.SubstratePlugin;
import org.apache.felix.atomos.substrate.core.plugin.gogo.GogoPlugin;

public class LauncherBuilderImpl implements LauncherBuilder
{
    public static List<Class<? extends SubstratePlugin<?>>> DEFAULT_PLUGINS = List.of(
        GogoPlugin.class);

    private final Map<String, Object> map = new HashMap<>();
    private final Collection<Class<? extends SubstratePlugin<?>>> pluginClasses = new HashSet<>();
    private boolean useDefault;

    Optional<Class<? extends SubstratePlugin<?>>> loadClass(String className)
    {
        if (className != null && !className.isEmpty())
        {

            try
            {
                Class<?> clazz = getClass().getClassLoader().loadClass(className);

                if (Stream.of(clazz.getGenericInterfaces()).filter(
                    SubstratePlugin.class::isInstance).findAny().isPresent())
                {
                    return Optional.of((Class<? extends SubstratePlugin<?>>) clazz);
                }
            }
            catch (ClassNotFoundException e)
            {
                e.printStackTrace();
            }
        }
        return Optional.empty();
    }

    @Override
    public LauncherBuilder addPlugins(Collection<String> pluginClassNames)
    {
        if (pluginClassNames != null)
        {
            pluginClassNames.forEach(n -> addPlugin(n));
        }
        return this;
    }

    @Override
    public LauncherBuilder addPlugin(String pluginClassName)
    {

        Optional<Class<? extends SubstratePlugin<?>>> optional = loadClass(
            pluginClassName);
        if (optional.isPresent())
        {
            pluginClasses.add(optional.get());
        }
        return this;
    }

    @Override
    public LauncherBuilder addPlugin(Class<SubstratePlugin<?>> pluginClass)
    {

        pluginClasses.add(pluginClass);
        return this;
    }

    @Override
    public LauncherBuilder addPlugin(Collection<Class<SubstratePlugin<?>>> pluginClasses)
    {
        pluginClasses.addAll(pluginClasses);
        return this;
    }

    @Override
    public LauncherBuilder removePlugin(Class<SubstratePlugin<?>> pluginClass)
    {
        removePlugins(List.of(pluginClass));
        return this;
    }

    @Override
    public LauncherBuilder removePlugins(
        Collection<Class<SubstratePlugin<?>>> pluginClasses)
    {
        this.pluginClasses.removeAll(pluginClasses);
        return this;
    }

    @Override
    public LauncherBuilder addConfig(String key, Object value)
    {
        map.put(key, value);
        return this;
    }

    @Override
    public LauncherBuilder addConfig(Map<String, Object> config)
    {
        map.putAll(config);
        return this;
    }

    @Override
    public LauncherBuilder removeConfig(String key)
    {
        map.remove(key);
        return this;
    }

    @Override
    public Launcher build()
    {
        String env_plugins = System.getProperty(Launcher.SYS_PROP_PLUGINS);
        if (env_plugins != null)
        {
            Stream.of(env_plugins.split(Launcher.SYS_PROP_SEPARATOR)).map(
                this::loadClass).filter(Objects::nonNull).filter(
                    o -> o.isPresent()).forEach(p -> pluginClasses.add(p.get()));
        }
        if (useDefault)
        {
            pluginClasses.addAll(DEFAULT_PLUGINS);
        }
        return new LauncherImpl(pluginClasses, map);
    }

    @Override
    public void init(boolean useDefault)
    {
        this.useDefault = useDefault;
    }
}
