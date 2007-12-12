/*
 * External Code Formatter
 * Copyright (c) 2007 Esko Luontola, www.orfjackal.net
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.orfjackal.extformatter;

import net.orfjackal.extformatter.util.Directories;
import net.orfjackal.extformatter.util.FilesSupportedBy;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * Uses alternative reformat methods if the underlying formatter does not support all methods.
 *
 * @author Esko Luontola
 * @since 6.12.2007
 */
public class AdaptiveCodeFormatter implements CodeFormatter {

    private final CodeFormatter formatter;

    public AdaptiveCodeFormatter(@NotNull CodeFormatter formatter) {
        this.formatter = formatter;
    }

    public boolean supportsFileType(@NotNull File file) {
        return formatter.supportsFileType(file);
    }

    public boolean supportsReformatFile() {
        return formatter.supportsReformatFile()
                || formatter.supportsReformatFiles();
    }

    public void reformatFile(@NotNull File file) {
        assert supportsFileType(file);
        if (formatter.supportsReformatFile()) {
            formatter.reformatFile(file);
        } else if (formatter.supportsReformatFiles()) {
            formatter.reformatFiles(file);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public boolean supportsReformatFiles() {
        return formatter.supportsReformatFile()
                || formatter.supportsReformatFiles();
    }

    public void reformatFiles(@NotNull File... files) {
        for (File file : files) {
            assert supportsFileType(file);
        }
        if (formatter.supportsReformatFiles()) {
            formatter.reformatFiles(files);
        } else if (formatter.supportsReformatFile()) {
            for (File file : files) {
                formatter.reformatFile(file);
            }
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public boolean supportsReformatFilesInDirectory() {
        return formatter.supportsReformatFile()
                || formatter.supportsReformatFiles()
                || formatter.supportsReformatFilesInDirectory();
    }

    public void reformatFilesInDirectory(@NotNull File directory) {
        if (formatter.supportsReformatFilesInDirectory()) {
            formatter.reformatFilesInDirectory(directory);
        } else {
            File[] files = directory.listFiles(new FilesSupportedBy(this));
            reformatFiles(files);
        }
    }

    public boolean supportsReformatFilesInDirectoryRecursively() {
        return formatter.supportsReformatFile()
                || formatter.supportsReformatFiles()
                || formatter.supportsReformatFilesInDirectory()
                || formatter.supportsReformatFilesInDirectoryRecursively();
    }

    public void reformatFilesInDirectoryRecursively(@NotNull File directory) {
        if (formatter.supportsReformatFilesInDirectoryRecursively()) {
            formatter.reformatFilesInDirectoryRecursively(directory);
        } else {
            File[] subdirs = directory.listFiles(new Directories());
            reformatFilesInDirectory(directory);
            for (File subdir : subdirs) {
                reformatFilesInDirectoryRecursively(subdir);
            }
        }
    }
}
