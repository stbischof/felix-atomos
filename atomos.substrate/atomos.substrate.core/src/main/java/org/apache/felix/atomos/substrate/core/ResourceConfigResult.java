package org.apache.felix.atomos.substrate.core;

import java.util.Set;
import java.util.TreeSet;

public class ResourceConfigResult
{
    Set<String> allResourceBundles = new TreeSet<>();
    Set<String> allResourcePatterns = new TreeSet<>();
    Set<String> allResourcePackages = new TreeSet<>();
}