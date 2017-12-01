package kr.ac.ssu.cse.jahn.textsnapper.ui;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import kr.ac.ssu.cse.jahn.textsnapper.R;

public class ConfigActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content,
                        new PrefFragment()).commit();

    }
    /**
     * 아래 2개의 메소드는 Back Button 붙이는 코드
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        getMenuInflater().inflate(R.menu.menu_config, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int curId = item.getItemId();
        switch(curId) {
            case R.id.menuConfig:
                finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
