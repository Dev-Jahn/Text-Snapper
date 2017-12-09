package kr.ac.ssu.cse.jahn.textsnapper.util;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import kr.ac.ssu.cse.jahn.textsnapper.R;

/**
 * Created by ArchSlave on 2017-12-09.
 */

public class TranslateHelper extends AppCompatActivity {
    EditText ed;
    TextView tv;
    Button bt;
    RadioGroup rg;
    Trans tt = new Trans();

    Handler handle = new Handler();

    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
/**
        ed = (EditText) findViewById(R.id.ed);
        tv = (TextView) findViewById(R.id.tv);
        bt = (Button) findViewById(R.id.bt);
        rg = (RadioGroup) findViewById(R.id.rg);


        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (i == R.id.rb1) {
                    tt.setting("ko", "en");
                } else if (i == R.id.rb2) {
                    tt.setting("en", "ko");
                }
            }
        });

*/
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ed.getText().toString().length() == 0) {
                    ed.requestFocus();
                    return;
                }

                tt.setT(ed.getText().toString());
                tt.start();
            }
        });

    }

    class Trans extends Thread {

        String sendText;
        String receiveText;
        String serverResponse;
        String sourceLang;
        String targetLang;

        public void setting(String s, String t) {
            sourceLang = s;
            targetLang = t;
        }

        public void setT(String s) {
            sendText = s;
        }

        @Override
        public void run() {
            super.run();
            //애플리케이션 클라이언트 아이디값";
            String clientId = "XGbfExhpC1n9A3UJpYj7";
            //애플리케이션 클라이언트 시크릿값";
            String clientSecret = "P6WJ8ogSU1";
            try {
                String text = URLEncoder.encode(sendText, "UTF-8");
                String apiURL = "https://openapi.naver.com/v1/papago/n2mt";
                URL url = new URL(apiURL);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("X-Naver-Client-Id", clientId);
                con.setRequestProperty("X-Naver-Client-Secret", clientSecret);

                /// en하고 ko수정
                String postParams = "source=" + sourceLang + "&target=" + targetLang + "&text=" + text;
                con.setDoOutput(true);
                DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                wr.writeBytes(postParams);
                wr.flush();
                wr.close();
                int responseCode = con.getResponseCode();
                BufferedReader br;
                if (responseCode == 200) { // 정상 호출
                    br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                } else {  // 에러 발생
                    br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
                }
                String inputLine;
                serverResponse = br.readLine();
                receiveText = parse();
                handle.post(new Runnable() {
                    @Override
                    public void run() {
                        tv.setText(receiveText);
                    }
                });
                StringBuffer response = new StringBuffer();
                while ((inputLine = br.readLine()) != null) {
                    response.append(inputLine);
                }
                br.close();
            } catch (Exception e) {
                System.out.println(e);
            }
        }

        public String parse() {
            JsonElement jelement = new JsonParser().parse(serverResponse);
            JsonObject jobject = jelement.getAsJsonObject();
            jobject = jobject.getAsJsonObject("message");
            jobject = jobject.getAsJsonObject("result");
            String tmp = jobject.get("translatedText").toString();
            int startI = tmp.indexOf('"') + 1;
            int endI = startI + tmp.length() - 2;
            String result = tmp.substring(startI, endI);
            return result;
        }
    }
}

