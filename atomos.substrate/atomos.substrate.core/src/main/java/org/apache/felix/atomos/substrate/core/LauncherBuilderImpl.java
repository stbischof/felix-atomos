package org.apache.felix.atomos.substrate.core;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.felix.atomos.substrate.api.Launcher;
import org.apache.felix.atomos.substrate.api.LauncherBuilder;
import org.apache.felix.atomos.substrate.api.plugin.SubstratePlugin;
import org.apache.felix.atomos.substrate.core.plugins.GogoPlugin;
import org.osgi.util.converter.Converter;
import org.osgi.util.converter.Converters;

public class LauncherBuilderImpl implements LauncherBuilder
{
    public static List<Class<? extends SubstratePlugin<?>>> DEFAULT_PLUGINS = List.of(
        GogoPlugin.class);

    private final Collection<SubstratePlugin<?>> plugins = new HashSet<>();
    private boolean useDefault;
    private final Converter converter = Converters.standardConverter();

    Optional<Class<? extends SubstratePlugin<?>>> loadPluginClass(String className)
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

    private void addInitPlugins(Class<? extends SubstratePlugin<?>> pluginClass,
        Object cfg)
    {
        try
        {

            SubstratePlugin<?> plugin = pluginClass.getConstructor().newInstance();

            //find init Method
            Optional<Method> oMethod = Stream.of(plugin.getClass().getMethods())//
                .filter(m -> m.getName().equals("init"))//
                .filter(m -> m.getParameterCount() == 1)//
                .filter(m -> m.getParameterTypes()[0] != Object.class)//
                .findAny();
            // Convert config and init Plugin
            if (oMethod.isPresent())
            {
                Method method = oMethod.get();
                Object config = converter.convert(cfg).to(method.getParameterTypes()[0]);
                method.invoke(plugin, config);

                plugins.add(plugin);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            // TODO: handle exception
        }
    }

    @Override
    public Launcher build()
    {
        if (useDefault)
        {
            DEFAULT_PLUGINS.forEach(pc -> addPlugin(pc, Map.of()));

        }
        return new LauncherImpl(plugins);
    }

    @Override
    public void init(boolean useDefault)
    {
        this.useDefault = useDefault;
    }

    @Override
    public LauncherBuilder addPlugin(String pluginClassName, Map<String, Object> cfgMap)
    {

        Optional<Class<? extends SubstratePlugin<?>>> optional = loadPluginClass(
            pluginClassName);
        if (optional.isPresent())
        {
            addPlugin(optional.get(), cfgMap);
        }
        return this;
    }

    @Override
    public LauncherBuilder addPlugin(Class<? extends SubstratePlugin<?>> pluginClass,
        Map<String, Object> cfgMap)
    {
        addInitPlugins(pluginClass, cfgMap);
        return this;
    }

    @Override
    public <C> LauncherBuilder addPlugin(Class<? extends SubstratePlugin<C>> pluginClass,
        C cfg)
    {
        addInitPlugins(pluginClass, cfg);
        return this;
    }

    @Override
    public <C> LauncherBuilder addPlugin(SubstratePlugin<C> plugin, C cfg)
    {
        plugin.init(cfg);
        plugins.add(plugin);
        return this;
    }

}
