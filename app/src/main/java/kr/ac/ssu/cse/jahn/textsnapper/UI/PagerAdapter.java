package kr.ac.ssu.cse.jahn.textsnapper.UI;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class PagerAdapter extends FragmentPagerAdapter {
    List<Fragment> fragmentList = new ArrayList<Fragment>();
    private String titles[] = new String[]{"Recent Files", "Documents"};
    public PagerAdapter(FragmentManager fm) {
        super(fm);
        fragmentList.add(new RecentFilesFragment());
        fragmentList.add(new DocumentFragment());
    }

    @Override
    public Fragment getItem(int position) {
        return this.fragmentList.get(position);
    }

    @Override
    public int getCount() {
        return this.fragmentList.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titles[position];
    }

}
