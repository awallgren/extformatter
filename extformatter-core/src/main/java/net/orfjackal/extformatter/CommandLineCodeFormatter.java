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

import static net.orfjackal.extformatter.util.FileUtil.listOf;
import static net.orfjackal.extformatter.util.FileUtil.quoted;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Matcher;

/**
 * {@link CodeFormatter} for a generic command line tool.
 *
 * @author Esko Luontola
 * @since 30.11.2007
 */
public class CommandLineCodeFormatter implements CodeFormatter {

    private static final String FILE_TAG = "%FILE%";
    private static final String FILES_TAG = "%FILES%";
    private static final String DIRECTORY_TAG = "%DIRECTORY%";

    @NotNull private final SupportedFileTypes supportedFileTypes;
    @Nullable private final String oneFileCommand;
    @Nullable private final String manyFilesCommand;
    @Nullable private final String directoryCommand;
    @Nullable private final String recursiveCommand;
    @NotNull private final Executer executer;

    public CommandLineCodeFormatter(@NotNull String[] supportedFileTypes,
                                    @Nullable String oneFileCommand,
                                    @Nullable String manyFilesCommand,
                                    @Nullable String directoryCommand,
                                    @Nullable String recursiveCommand,
                                    @NotNull Executer executer) {
        this.supportedFileTypes = new SupportedFileTypes(supportedFileTypes);
        this.oneFileCommand = oneFileCommand;
        this.manyFilesCommand = manyFilesCommand;
        this.directoryCommand = directoryCommand;
        this.recursiveCommand = recursiveCommand;
        this.executer = executer;
    }

    public CommandLineCodeFormatter(@NotNull String[] supportedFileTypes,
                                    @Nullable String oneFileCommand,
                                    @Nullable String manyFilesCommand,
                                    @Nullable String directoryCommand,
                                    @Nullable String recursiveCommand) {
        this(supportedFileTypes, oneFileCommand, manyFilesCommand, directoryCommand, recursiveCommand, new ExecuterImpl());
    }

    public boolean supportsFileType(@NotNull File file) {
        return supportedFileTypes.matches(file);
    }

    public boolean supportsReformatOne() {
        return oneFileCommand != null;
    }

    public void reformatOne(@NotNull File file) {
        if (oneFileCommand != null) {
            executer.execute(parsed(oneFileCommand, file));
        }
    }

    public boolean supportsReformatMany() {
        return manyFilesCommand != null;
    }

    public void reformatMany(@NotNull File... files) {
        if (manyFilesCommand != null) {
            executer.execute(parsed(manyFilesCommand, files));
        }
    }

    public boolean supportsReformatDirectory() {
        return directoryCommand != null;
    }

    public void reformatDirectory(@NotNull File directory) {
        if (directoryCommand != null) {
            executer.execute(parsed(directoryCommand, directory));
        }
    }

    public boolean supportsReformatRecursively() {
        return recursiveCommand != null;
    }

    public void reformatRecursively(@NotNull File directory) {
        if (recursiveCommand != null) {
            executer.execute(parsed(recursiveCommand, directory));
        }
    }

    private String parsed(@NotNull String command, @NotNull File file) {
        try {
            // TODO: remove code duplication
            if (command.contains(FILE_TAG) && file.isFile()) {
                assert supportsFileType(file);
                command = command.replaceAll(FILE_TAG, Matcher.quoteReplacement(quoted(file.getCanonicalPath())));
            } else if (command.contains(DIRECTORY_TAG) && file.isDirectory()) {
                command = command.replaceAll(DIRECTORY_TAG, Matcher.quoteReplacement(quoted(file.getCanonicalPath())));
            } else {
                throw new IllegalArgumentException("command '" + command + "',  file '" + file + "'");
            }
            return command;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String parsed(String command, File[] files) {
        try {
            if (command.contains(FILES_TAG) && areFiles(files)) {
                command = command.replaceAll(FILES_TAG, Matcher.quoteReplacement(listOf(files)));
            } else {
                throw new IllegalArgumentException("command '" + command + "',  file '" + Arrays.toString(files) + "'");
            }
            return command;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean areFiles(File[] files) {
        for (File file : files) {
            if (!file.isFile()) {
                return false;
            }
        }
        return true;
    }
}
