package jp.kaoru;

import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

public class Main {

    public static void main(String[] args) {

        System.out.print("AndroidStudio 日本語化ツール\n\n");
        System.out.print("作成者:かおる\n");
        System.out.print("Google+ : https://plus.google.com/+%E3%81%8B%E3%81%8A%E3%82%8B%E3%82%93\n\n\n");

        File[] files = new File("/home/kaoru/Documents/翻訳/sort/").listFiles();

        for (File f : files) {
            try {
                Sort(f.getPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
            /*LocalDateTime now = LocalDateTime.now();
            System.out.print(now.getHour() + ":" + now.getMinute() + ":" + now.getSecond() + " > " + f.getName() + "の翻訳作業を開始しています\n");
            Properties pro = ReadProperties(f.getPath());
            try {
                BufferedWriter bw = new BufferedWriter(new FileWriter(new File("/home/kaoru/Documents/翻訳/messages/" + f.getName())));
                for (Enumeration<Object> enumeration = pro.keys(); enumeration.hasMoreElements(); ) {
                    String name = (String) enumeration.nextElement();
                    String value = pro.getProperty(name).replace("_", "").replace("&", "");
                    if (value.contains("<") && value.contains(">") || value.contains("choice")) {
                        bw.write(name + "=" + value);
                    } else {
                        bw.write(name + "=" + callPost(value));
                    }
                    bw.newLine();
                }
                bw.close();
            } catch (IOException e) {
                System.out.println(e);
            }
            now = LocalDateTime.now();
            System.out.print(now.getHour() + ":" + now.getMinute() + ":" + now.getSecond() + " > " + f.getName() + "の翻訳作業を終了しています\n");*/
        }
    }

    public static Properties ReadProperties(String filepath) {
        Properties pro = new Properties();
        try {
            pro.load(new FileInputStream(filepath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return pro;
    }

    public static String callPost(String text) {

        HttpURLConnection con = null;
        StringBuffer result = new StringBuffer();
        try {
            URL url = new URL("http://kaorun.dip.jp/translate.php?source_langcode=en&target_langcode=ja&text=" + URLEncoder.encode(text, "UTF-8"));
            con = (HttpURLConnection) url.openConnection();
            con.connect();
            final int status = con.getResponseCode();
            if (status == HttpURLConnection.HTTP_OK) {
                final InputStream in = con.getInputStream();
                String encoding = con.getContentEncoding();
                if (null == encoding) {
                    encoding = "UTF-8";
                }
                final InputStreamReader inReader = new InputStreamReader(in, encoding);
                final BufferedReader bufReader = new BufferedReader(inReader);
                String line = null;
                while ((line = bufReader.readLine()) != null) {
                    result.append(line);
                }
                bufReader.close();
                inReader.close();
                in.close();
            } else {
                System.out.println(status);
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            if (con != null) {
                // コネクションを切断
                con.disconnect();
            }
        }
        JSONObject jo = new JSONObject(result.toString());
        return jo.getJSONArray("sentences").getJSONObject(0).getString("trans");
    }

    public static void Sort(String filepath) throws IOException {
        Properties pro = new Properties();
        pro.load(new InputStreamReader(new FileInputStream(filepath),"UTF-8"));
        SortedProperties sp = new SortedProperties();
        for (Enumeration<Object> enumeration = pro.keys(); enumeration.hasMoreElements(); ) {
            String name = (String) enumeration.nextElement();
            String value = pro.getProperty(name);
            sp.put(name,value);
        }
        FileOutputStream fos = null;
        try {
            sp.store(new OutputStreamWriter(new FileOutputStream(filepath),"UTF-8"),"AndroidStudio3.1 日本語化プロジェクト");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class SortedProperties extends Properties {
        public Enumeration keys() {
            Enumeration keysEnum = super.keys();
            Vector<String> keyList = new Vector<String>();
            while (keysEnum.hasMoreElements()) {
                keyList.add((String) keysEnum.nextElement());
            }
            Collections.sort(keyList);
            return keyList.elements();
        }

    }

    /**
     * Unicode文字列に変換する("あ" -> "\u3042")
     * @param original
     * @return
     */
    private static String convertToUnicode(String original)
    {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < original.length(); i++) {
            sb.append(String.format("\\u%04X", Character.codePointAt(original, i)));
        }
        String unicode = sb.toString();
        return unicode;
    }

    /**
     * Unicode文字列から元の文字列に変換する ("\u3042" -> "あ")
     * @param unicode
     * @return
     */
    private static String convertToOiginal(String unicode)
    {
        String[] codeStrs = unicode.split("\\\\u");
        int[] codePoints = new int[codeStrs.length - 1]; // 最初が空文字なのでそれを抜かす
        for (int i = 0; i < codePoints.length; i++) {
            codePoints[i] = Integer.parseInt(codeStrs[i + 1], 16);
        }
        String encodedText = new String(codePoints, 0, codePoints.length);
        return encodedText;
    }

}
