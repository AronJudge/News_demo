package com.xiangxue.network.environment;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.xiangxue.network.R;


public class EnvironmentActivity extends AppCompatActivity {

    public static final String INTERFACE_ENVIRONMENT_PREF_KEY = "interface_environment_type";
    public static final String NETWORK_ENVIRONMENT_PREF_KEY = "network_environment_type";
    private static String sCurrentInterfaceEnvironment = "";
    private static String sCurrentNetWorkEnvironment = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_environment);
        //setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content, new MyPreferenceFragment())
                .commit();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        sCurrentInterfaceEnvironment = prefs.getString(INTERFACE_ENVIRONMENT_PREF_KEY, "1");
        sCurrentNetWorkEnvironment = prefs.getString(NETWORK_ENVIRONMENT_PREF_KEY, "0");

    }

    public static class MyPreferenceFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.environment_preference);
            findPreference(INTERFACE_ENVIRONMENT_PREF_KEY).setOnPreferenceChangeListener(this);
            findPreference(NETWORK_ENVIRONMENT_PREF_KEY).setOnPreferenceChangeListener(this);
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object o) {
            if (!INTERFACE_ENVIRONMENT_PREF_KEY.equalsIgnoreCase(String.valueOf(o))) {
                Toast.makeText(getContext(), "您已经更改了接口环境，再您退出当前页面的时候APP将会重启切换环境！", Toast.LENGTH_SHORT).show();
            }
            if (!NETWORK_ENVIRONMENT_PREF_KEY.equalsIgnoreCase(String.valueOf(o))) {
                Toast.makeText(getContext(), "您已经更改了网络环境，再您退出当前页面的时候APP将会重启切换环境！", Toast.LENGTH_SHORT).show();
            }
            return true;
        }
    }

    @Override
    public void onBackPressed() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String newInterfaceValue = prefs.getString(EnvironmentActivity.INTERFACE_ENVIRONMENT_PREF_KEY, "1");
        String newNetworkValue = prefs.getString(EnvironmentActivity.NETWORK_ENVIRONMENT_PREF_KEY, "0");
        if (!sCurrentInterfaceEnvironment.equalsIgnoreCase(newInterfaceValue)
                || !sCurrentNetWorkEnvironment.equalsIgnoreCase(newNetworkValue)) {
            android.os.Process.killProcess(android.os.Process.myPid());
        } else {
            finish();
        }
    }

    public static int getNetworkType(Application application) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(application);
        String environment = prefs.getString(EnvironmentActivity.NETWORK_ENVIRONMENT_PREF_KEY, "0");
        return Integer.valueOf(environment);
    }

    public static boolean isOfficialEnvironment(Application application) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(application);
        String environment = prefs.getString(EnvironmentActivity.INTERFACE_ENVIRONMENT_PREF_KEY, "1");
        return "1".equalsIgnoreCase(environment);
    }
}