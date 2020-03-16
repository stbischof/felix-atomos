/**
 *
 */
package org.apache.felix.atomos.substrate.core;

import java.util.Collections;
import java.util.Dictionary;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.felix.atomos.substrate.api.RegisterServiceCall;

public class RegisterServiceCallImpl implements RegisterServiceCall
{

    String[] classes;
    private final Object service;
    Map<String, ?> cfg = null;
    private boolean valid = true;

    public RegisterServiceCallImpl(Object[] args)
    {
        service = args[1];

        Dictionary<String, ?> dict = (Dictionary<String, ?>) args[2];
        if (dict != null)
        {
            cfg = Collections.list(dict.keys()).stream().collect(
                Collectors.toMap(Function.identity(), dict::get));
        }

        if (args[0] instanceof Class)
        {
            classes = new String[] { ((Class<?>) args[0]).getName() };
        }
        else if (args[0] instanceof Class[])
        {
            classes = Stream.of((Class[]) args[0]).map(Class::getName).toArray(
                String[]::new);
        }
        else if (args[0] instanceof String)
        {
            classes = new String[] { ((String) args[0]) };
        }
        else if (args[0] instanceof String[])
        {
            classes = ((String[]) args[0]);
        }
        else
        {
            valid = false;
        }
    }

    @Override
    public String[] classes()
    {
        return classes;
    }

    @Override
    public Object service()
    {
        return service;
    }

    @Override
    public Map<String, ?> config()
    {
        return cfg;
    }

    public boolean isValid()
    {
        return valid;
    }

}
