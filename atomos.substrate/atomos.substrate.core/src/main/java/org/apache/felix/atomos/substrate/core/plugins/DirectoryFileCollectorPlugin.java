/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.felix.atomos.substrate.core.plugins;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.apache.felix.atomos.substrate.api.SubstrateContext;
import org.apache.felix.atomos.substrate.api.plugin.FileCollectorPlugin;

public class DirectoryFileCollectorPlugin implements FileCollectorPlugin<DirectoryFileCollectorPluginConfig>
{
    private DirectoryFileCollectorPluginConfig config;

    @Override
    public void init(DirectoryFileCollectorPluginConfig config)
    {
        this.config = config;
    }

    @Override
    public void collectFiles(SubstrateContext context)
    {
        FileVisitor<Path> v = new SimpleFileVisitor<>()
        {

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attr)
            {
                if (config.filters() == null || config.filters().stream().filter(
                    file.toString()::matches).findAny().isPresent())
                {
                    context.addFile(file, config.fileType());
                }

                return FileVisitResult.CONTINUE;
            }

            // Print each directory visited.
            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc)
            {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc)
            {
                System.err.println(exc);
                return FileVisitResult.CONTINUE;
            }

        };
        config.paths().parallelStream().forEach(p -> {
            try
            {
                Files.walkFileTree(p, v);
            }
            catch (IOException e)
            {
                throw new UncheckedIOException(e);
            }
        });

    }

}
