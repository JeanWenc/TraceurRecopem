package jean.wencelius.traceurrecopem.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Jean Wencélius on 08/04/2020.
 */
public class AppPreferences {
    public static void setDefaultsString(String key,String value, Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static void setDefaultsInt(String key, int value, Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(key, value);
        editor.apply();
    }

    public static String getDefaultsString(String key, Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(key,null);
    }

    public static int getDefaultsInt(String key, Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getInt(key,(int) 0);

    }




}
