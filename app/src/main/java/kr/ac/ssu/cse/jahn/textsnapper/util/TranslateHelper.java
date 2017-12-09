package kr.ac.ssu.cse.jahn.textsnapper.util;

import android.os.Handler;
import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by ArchSlave on 2017-12-09.
 */

public class TranslateHelper extends Thread {

    // API Client ID
    private static final String clientId = "XGbfExhpC1n9A3UJpYj7";
    // API Client PW
    private static final String clientSecret = "P6WJ8ogSU1";
    // API URL
    private static final String apiURL = "https://openapi.naver.com/v1/papago/n2mt";

    private static boolean isEng;

    private static Handler handler = new Handler();

    private static String sourceLang;
    private static String targetLang;

    private static String originalText;
    private static String resultText;

    private static TranslateHelper translateHelper = null;

    public static TranslateHelper getInstance(boolean isEng, String originalText) {
        if (translateHelper == null) {
            translateHelper = new TranslateHelper();
        }
        if (isEng) {
            translateHelper.setLanguage("en");
        } else {
            translateHelper.setLanguage("ko");
        }

        translateHelper.setOriginalText(originalText);

        return translateHelper;
    }

    /**
     * 무조건 인자는 ko 아니면 en이 입력되어야 한다.
     */
    public void setLanguage(String curLang) {
        if (curLang.equals("ko")) {
            isEng = false;
            sourceLang = "ko";
            targetLang = "en";
        } else if (curLang.equals("en")) {
            isEng = true;
            sourceLang = "en";
            targetLang = "ko";
        } else {
            throw new IllegalArgumentException("curLang must be 'ko' or 'en'");
        }
    }

    public void setOriginalText(String originalText) {
        this.originalText = originalText;
    }

    public static String getResultText() {
        return resultText;
    }


    @Override
    public void run() {
        super.run();
        try {
            /**
             * API Connection Code
             */
            String text = URLEncoder.encode(originalText, "UTF-8");
            URL url = new URL(apiURL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("X-Naver-Client-Id", clientId);
            con.setRequestProperty("X-Naver-Client-Secret", clientSecret);
            /**
             * Translation Process
             */
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

            String serverResponse = br.readLine();

            // 최종 결과
            resultText = parse(serverResponse);

            /**
             * handler.post를 이용해서 UI에 직접 뿌려주면 완성
             */

            Log.d("TRANSLATE", resultText);

            StringBuffer response = new StringBuffer();
            while ((inputLine = br.readLine()) != null) {
                response.append(inputLine);
            }
            br.close();

        } catch (UnsupportedEncodingException uee) {
            Log.d("DEBUG8", "Error in Translate : " + uee);
        } catch (IOException ioe) {
            Log.d("DEBUG8", "Error in Translate : " + ioe);
        }

    }

    public static String parse(String serverResponse) {
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
