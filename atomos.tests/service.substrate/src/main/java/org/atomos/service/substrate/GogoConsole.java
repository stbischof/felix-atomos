/*******************************************************************************
 * Copyright (c) 2018 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.atomos.service.substrate;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.atomos.framework.AtomosRuntime;
import org.osgi.framework.BundleException;

import sun.misc.Signal;

public class GogoConsole 
{
    public static void main( String[] args ) throws BundleException
    {
    	long start = System.nanoTime();
    	Signal.handle(new Signal("INT"), sig -> System.exit(0));
    	AtomosRuntime.launch(AtomosRuntime.getConfiguration(args));
    	long total = System.nanoTime() - start;
    	System.out.println("Total time: " + TimeUnit.NANOSECONDS.toMillis(total));

     	if (Arrays.asList(args).contains("-exit")) {
     		System.exit(0);
     	}
    }
}
