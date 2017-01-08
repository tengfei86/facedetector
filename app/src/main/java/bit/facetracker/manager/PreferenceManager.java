package bit.facetracker.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

/**
 * Created by blade on 2/19/16.
 */
public class PreferenceManager {

    private static PreferenceManager sInstance;
    private Context mContext;

    private PreferenceManager(Context context) {
        mContext = context;
    }

    public synchronized static PreferenceManager getInstantce(Context context) {
        if (sInstance == null) {
            sInstance = new PreferenceManager(context);
        }
        return sInstance;
    }

    public String getPrefString(String key, final String defaultValue) {
        if (mContext != null) {
            final SharedPreferences settings = android.preference.PreferenceManager
                    .getDefaultSharedPreferences(mContext);
            return settings.getString(key, defaultValue);
        } else {
            if (!TextUtils.isEmpty(defaultValue)) {
                return defaultValue;
            }
        }

        return "";
    }

    public void setPrefString(final String key,
                              final String value) {
        if (mContext != null) {
            final SharedPreferences settings = android.preference.PreferenceManager
                    .getDefaultSharedPreferences(mContext);
            settings.edit().putString(key, value).commit();
        }

    }

    public boolean getPrefBoolean(final String key,
                                  final boolean defaultValue) {
        if (mContext != null) {
            final SharedPreferences settings = android.preference.PreferenceManager
                    .getDefaultSharedPreferences(mContext);
            return settings.getBoolean(key, defaultValue);
        } else {
            return defaultValue;
        }

    }

    public boolean hasKey(final String key) {
        if (mContext != null) {
            return android.preference.PreferenceManager.getDefaultSharedPreferences(mContext).contains(
                    key);
        } else {
            return false;
        }

    }

    public void setPrefBoolean(final String key,
                               final boolean value) {
        if (mContext != null) {
            final SharedPreferences settings = android.preference.PreferenceManager
                    .getDefaultSharedPreferences(mContext);
            settings.edit().putBoolean(key, value).commit();
        }
    }

    public void setPrefInt(final String key,
                           final int value) {
        if (mContext != null) {
            final SharedPreferences settings = android.preference.PreferenceManager
                    .getDefaultSharedPreferences(mContext);
            settings.edit().putInt(key, value).apply();
        }
    }

    public int getPrefInt(final String key,
                          final int defaultValue) {
        if (mContext != null) {
            final SharedPreferences settings = android.preference.PreferenceManager
                    .getDefaultSharedPreferences(mContext);
            return settings.getInt(key, defaultValue);
        } else {
            return defaultValue;
        }

    }

    public void setPrefFloat(final String key,
                             final float value) {
        if (mContext != null) {
            final SharedPreferences settings = android.preference.PreferenceManager
                    .getDefaultSharedPreferences(mContext);
            settings.edit().putFloat(key, value).apply();
        }
    }

    public float getPrefFloat(final String key,
                              final float defaultValue) {
        if (mContext != null) {
            final SharedPreferences settings = android.preference.PreferenceManager
                    .getDefaultSharedPreferences(mContext);
            return settings.getFloat(key, defaultValue);
        } else {
            return defaultValue;
        }

    }

    public void setPrefLong(final String key,
                            final long value) {
        if (mContext != null) {
            final SharedPreferences settings = android.preference.PreferenceManager
                    .getDefaultSharedPreferences(mContext);
            settings.edit().putLong(key, value).apply();
        }
    }

    public long getPrefLong(final String key,
                            final long defaultValue) {
        if (mContext != null) {
            final SharedPreferences settings = android.preference.PreferenceManager
                    .getDefaultSharedPreferences(mContext);
            return settings.getLong(key, defaultValue);
        } else {
            return defaultValue;
        }

    }

    public void clearPreference(final SharedPreferences p) {
        if (p != null) {
            final SharedPreferences.Editor editor = p.edit();
            editor.clear();
            editor.apply();
        }
    }

}
