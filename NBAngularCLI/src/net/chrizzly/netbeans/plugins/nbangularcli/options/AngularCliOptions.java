package net.chrizzly.netbeans.plugins.nbangularcli.options;

import java.util.List;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import net.chrizzly.netbeans.plugins.nbangularcli.executables.AngularCliExecutable;
import org.netbeans.api.annotations.common.CheckForNull;
import org.openide.util.NbPreferences;

public class AngularCliOptions {
    public static final String ANGULAR_CLI_PATH = "angular-cli.path"; // NOI18N

    // Do not change arbitrary - consult with layer's folder OptionsExport
    // Path to Preferences node for storing these preferences
    private static final String PREFERENCES_PATH = "nodejs"; // NOI18N

    private static final AngularCliOptions INSTANCE = new AngularCliOptions();

    private final Preferences preferences;
    private volatile boolean angularCliSearched = false;

    public static AngularCliOptions getInstance() {
        return INSTANCE;
    }

    public void addPreferenceChangeListener(PreferenceChangeListener listener) {
        preferences.addPreferenceChangeListener(listener);
    }

    public void removePreferenceChangeListener(PreferenceChangeListener listener) {
        preferences.removePreferenceChangeListener(listener);
    }

    private AngularCliOptions() {
        preferences = NbPreferences.forModule(AngularCliOptions.class).node(PREFERENCES_PATH);
    }

    @CheckForNull
    public String getAngularCli() {
        String path = preferences.get(ANGULAR_CLI_PATH, null);

        if (path == null && !angularCliSearched) {
            angularCliSearched = true;
            List<String> files = FileUtils.findFileOnUsersPath(AngularCliExecutable.ANGULAR_CLI_NAME);

            if (!files.isEmpty()) {
                path = files.get(0);
                setAngularCli(path);
            }
        }

        return path;
    }

    public void setAngularCli(String angularCli) {
        preferences.put(ANGULAR_CLI_PATH, angularCli);
    }
}
