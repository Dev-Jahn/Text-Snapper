package kr.ac.ssu.cse.jahn.textsnapper.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import kr.ac.ssu.cse.jahn.textsnapper.R;

/**
 * Created by ArchSlave on 2017-11-06.
 */

public class PrefFragment extends PreferenceFragment {

    SharedPreferences pref;
    ListPreference listPreference;

    /**
     * ConfigActivity에서 설정 값이 변화한 경우의 Listener
     */
    SharedPreferences.OnSharedPreferenceChangeListener prefListener =
            new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                    if (key.equals("ocrSelect"))
                        listPreference.setSummary("인식할 언어를 선택합니다.\n"
                                + "현재 언어 : " + pref.getString("ocrSelect", "English"));
                }
            };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        addPreferencesFromResource(R.xml.fragment_pref);

        listPreference = (ListPreference) findPreference("ocrSelect");
        pref = PreferenceManager.getDefaultSharedPreferences(getActivity());

        if (!pref.getString("ocrSelect", "").equals(""))
            listPreference.setSummary("인식할 언어를 선택합니다.\n"
                    + "현재 언어 : " + pref.getString("ocrSelect", "English"));

        pref.registerOnSharedPreferenceChangeListener(prefListener);

        return view;
    }
}

