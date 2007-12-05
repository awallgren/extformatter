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

package net.orfjackal.extformatter.plugin;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import net.orfjackal.extformatter.Resources;
import net.orfjackal.extformatter.settings.IllegalSettingsException;
import net.orfjackal.extformatter.settings.Settings;
import net.orfjackal.extformatter.settings.SettingsManager;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Esko Luontola
 * @since 4.12.2007
 */
@State(
        name = "ExternalCodeFormatter",
        storages = {@Storage(
                id = "other",
                file = "$PROJECT_FILE$"
        )}
)
public class ProjectSettingsComponent implements ProjectComponent, Configurable, PersistentStateComponent<Settings> {

    private static final Logger LOG = Logger.getInstance(ProjectSettingsComponent.class.getName());

    @NotNull private final ProjectCodeStyleInstaller project;
    @NotNull private final Settings settings = new Settings();
    @Nullable private ProjectSettingsForm form;
    @Nullable private ImageIcon icon;

    public ProjectSettingsComponent(@NotNull Project project) {
        this.project = new ProjectCodeStyleInstaller(project);
    }

    public void initComponent() {
    }

    public void disposeComponent() {
    }

    @NotNull
    public String getComponentName() {
        return "ProjectSettingsComponent";
    }

    public void projectOpened() {
        applySettings();
    }

    public void projectClosed() {
        uninstall();
    }

    private void applySettings() {
        try {
            project.changeFormatterTo(SettingsManager.newFormatter(settings));
        } catch (IllegalSettingsException e) {
            LOG.error(e);
        }
    }

    private void uninstall() {
        project.changeFormatterTo(null);
    }

    private void verifySettingsOf(@Nullable ProjectSettingsForm form) throws ConfigurationException {
        try {
            if (form != null) {
                Settings copy = settings.clone();
                form.getData(copy);
                SettingsManager.newFormatter(copy);
            }
        } catch (IllegalSettingsException e) {
            LOG.info(e);
            throw new ConfigurationException("Error in the " + nameOf(e.getField()) + " field: " + e.getExplanation());
        }
    }

    @NotNull
    private static String nameOf(@NotNull String field) {
        // TODO: put names and other texts to a resource bundle
        Map<String, String> fieldNames = new HashMap<String, String>();
        fieldNames.put("eclipseExecutable", "Eclipse executable");
        fieldNames.put("eclipsePrefs", "Eclipse preferences");
        String name = fieldNames.get(field);
        if (name == null) {
            LOG.warn("Unknown field: " + field);
            return field;
        }
        return name;
    }

    @Nls
    public String getDisplayName() {
        return "External\nCode Formatter";
    }

    @NotNull
    public Icon getIcon() {
        if (icon == null) {
            icon = new ImageIcon(Resources.PROGRAM_LOGO_32);
        }
        return icon;
    }

    @Nullable
    @NonNls
    public String getHelpTopic() {
        return null; // TODO: add help
    }

    @NotNull
    public JComponent createComponent() {
        if (form == null) {
            form = new ProjectSettingsForm();
        }
        return form.getRootComponent();
    }

    public boolean isModified() {
        return form != null && form.isModified(settings);
    }

    public void apply() throws ConfigurationException {
        verifySettingsOf(form);
        if (form != null) {
            form.getData(settings);
            applySettings();
        }
    }

    public void reset() {
        if (form != null) {
            form.setData(settings);
        }
    }

    public void disposeUIResources() {
        form = null;
    }

    @NotNull
    public Settings getState() {
        return settings.clone();
    }

    public void loadState(@NotNull Settings state) {
        XmlSerializerUtil.copyBean(state, settings);
        applySettings();
    }
}
