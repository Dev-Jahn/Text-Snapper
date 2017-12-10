package kr.ac.ssu.cse.jahn.textsnapper.ui;

import android.content.Context;
import android.content.Intent;
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
    Context context;

    /**
     * ConfigActivity에서 설정 값이 변화한 경우의 Listener
     */
    SharedPreferences.OnSharedPreferenceChangeListener prefListener =
            new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                    if (key.equals("ocrSelect")) {
                        listPreference.setSummary("인식할 언어를 선택합니다.\n"
                                + "현재 언어 : " + pref.getString("ocrSelect", "English"));
                        /**
                         * 환경설정에서 OCR Language를 변경할 때
                         * FloatingButton의 FloatingBar가 열려있다면
                         * LanguageImage를 직접 변경해준다.
                         */
                        if(FloatingService.isBarActive()) {
                            String curLang = pref.getString("ocrSelect", "English");
                            FloatingService.setLanguageImage(curLang);
                        }
                    }
                    if(key.equals("floatingButtonUse")) {
                        boolean isAvailable = pref.getBoolean("floatingButtonUse", true);
                        if (isAvailable == false) {
                            // 옵션 비활성화했는데 현재 서비스가 켜져있다면 종료
                            if (FloatingService.isServiceActive()) {
                                Intent stopIntent = FloatingService.getCurrentFloatingService();
                                context.stopService(stopIntent);
                            }
                        }
                    }
                    if(key.equals("floatingButtonLocation")) {
                        boolean isFixed = pref.getBoolean("floatingButtonLocation", false);
                        FloatingService.setIsFixed(isFixed);
                    }
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

    @Override
    public void onResume() {
        super.onResume();
        context = getActivity().getApplicationContext();
    }
}
