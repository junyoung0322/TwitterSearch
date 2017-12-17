import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class TwitterAPI {
	/**
	 * Twitter TimeLine取得
	 * 
	 * @return
	 * @throws InvalidKeyException
	 * @throws NoSuchAlgorithmException
	 * @throws MalformedURLException
	 * @throws IOException
	 */
    public String getUsesTimeLine()
            throws InvalidKeyException, NoSuchAlgorithmException, MalformedURLException, IOException {
        String sMethod = "GET";
        String sUrl = "https://api.twitter.com/1.1/statuses/user_timeline.json";
        Map<String, String> paramMap = getUserTimeLineParamMap();
        Map<String, String> oAuthParamMap = getOAuthParamMap();
        
        String sUrlWithParams = getUrlWithParams(sUrl, paramMap);
        String sSignatureBaseString = getSignatureBaseString(sMethod, sUrl, paramMap, oAuthParamMap);
        String sAuthorizationHeaderValue = getAuthorizationHeaderValue(sSignatureBaseString, oAuthParamMap);
        
        return request(sUrlWithParams, sAuthorizationHeaderValue);
    }
    
    /**
     * リクエスト
     * 
     * @param sUrlWithParams
     * @param sAuthorizationHeaderValue
     * @return
     * @throws MalformedURLException
     * @throws IOException
     */
    private String request(String sUrlWithParams, String sAuthorizationHeaderValue)
            throws MalformedURLException, IOException {
        URLConnection urlConnection = new URL(sUrlWithParams).openConnection();
        urlConnection.setRequestProperty("Authorization", sAuthorizationHeaderValue);
        
        BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        br.close();
        
        return sb.toString();
    }
    
    /**
     * 検索条件パラメータリスト生成
     * 
     * @return
     */
    private Map<String, String> getUserTimeLineParamMap() {
        Map<String, String> urlParamMap = new HashMap<String, String>();
        urlParamMap.put("screen_name", "@realDonaldTrump");
        urlParamMap.put("count", "10");
        urlParamMap.put("user_trim", "trueW");
        
        return urlParamMap;
    }
    
    /**
     * 署名用パラメータリスト生成
     * 
     * @return
     */
    private Map<String, String> getOAuthParamMap() {
        String sOAuthConsumerKey = "UhuBsj54DbRnnHGQYaPywHmAi";
        String sOAuthAccessToken = "42566916-5CI2BDtmiUQdWFKtTY6ajgxmLlYjURI4ftW2K88h3";
        String sOAuthNonce = String.valueOf(System.currentTimeMillis());
        String sOAuthSignatureMethod = "HMAC-SHA1";
        String sOAuthTimestamp = getTime();
        String sOAuthVersion = "1.0";
        
        Map<String, String> paramMap = new HashMap<String, String>();
        
        paramMap.put("oauth_consumer_key", sOAuthConsumerKey);
        paramMap.put("oauth_nonce", sOAuthNonce);
        paramMap.put("oauth_signature_method", sOAuthSignatureMethod);
        paramMap.put("oauth_timestamp", sOAuthTimestamp);
        paramMap.put("oauth_token", sOAuthAccessToken);
        paramMap.put("oauth_version", sOAuthVersion);

        return paramMap;
    }
    
    /**
     * 署名情報文字列作成
     * 
     * @param sMethod
     * @param sUrl
     * @param urlParamMap
     * @param oAuthParamMap
     * @return
     * @throws UnsupportedEncodingException
     */
    private String getSignatureBaseString(String sMethod, String sUrl,
            Map<String, String> urlParamMap, Map<String, String> oAuthParamMap) throws UnsupportedEncodingException {
        TreeMap<String, String> sortedParamMap = new TreeMap<String, String>();
        sortedParamMap.putAll(urlParamMap);
        sortedParamMap.putAll(oAuthParamMap);
        
        StringBuffer paramStringBuffer = new StringBuffer();
        for (Entry<String, String> paramEntry : sortedParamMap.entrySet()) {
            if (!paramEntry.equals(sortedParamMap.firstEntry())) {
                paramStringBuffer.append("&");
            }
            paramStringBuffer.append(paramEntry.getKey() + "=" + paramEntry.getValue());
        }
        
        String sSignatureBaseStringTemplate = "%s&%s&%s";
        String sSignatureBaseString =  String.format(
        		sSignatureBaseStringTemplate, 
                URLEncoder.encode(sMethod, "UTF-8"), 
                URLEncoder.encode(sUrl, "UTF-8"),
                URLEncoder.encode(paramStringBuffer.toString(), "UTF-8"));
        
        return sSignatureBaseString;
    }
    
    /**
     * 認証用ヘッダー生成
     * 
     * @param sSignatureBaseString
     * @param oAuthParamMap
     * @return
     * @throws UnsupportedEncodingException
     * @throws InvalidKeyException
     * @throws NoSuchAlgorithmException
     */
    private String getAuthorizationHeaderValue(String sSignatureBaseString, Map<String, String> oAuthParamMap)
            throws UnsupportedEncodingException, InvalidKeyException, NoSuchAlgorithmException {
        String sOAuthConsumerSecret = "e1zSaGoQorVKzyW5W7U8LaAmkUsf3nnjdIHCxh45Y03bnnBj86";
        String sOAuthAccessTokenSecret = "YHfdFB1RJxIcHQ6XOriX0GZhmZmFpNmI5wegaBIRDflIU";
        String sCompositeKey = URLEncoder.encode(sOAuthConsumerSecret, "UTF-8") + "&" 
                + URLEncoder.encode(sOAuthAccessTokenSecret, "UTF-8");

        String oAuthSignature =  computeSignature(sSignatureBaseString, sCompositeKey);

        String oAuthSignatureEncoded = URLEncoder.encode(oAuthSignature, "UTF-8");

        String authorizationHeaderValueTempl = 
                "OAuth oauth_consumer_key=\"%s\", oauth_nonce=\"%s\", oauth_signature=\"%s\", " + 
                "oauth_signature_method=\"%s\", oauth_timestamp=\"%s\", oauth_token=\"%s\", oauth_version=\"%s\"";
        String authorizationHeaderValue = String.format(
                authorizationHeaderValueTempl,
                oAuthParamMap.get("oauth_consumer_key"),
                oAuthParamMap.get("oauth_nonce"),
                oAuthSignatureEncoded,
                oAuthParamMap.get("oauth_signature_method"),
                oAuthParamMap.get("oauth_timestamp"),
                oAuthParamMap.get("oauth_token"),
                oAuthParamMap.get("oauth_version"));
        
        return authorizationHeaderValue;
    }
    
    /**
     * URL(パラメータ追加)作成
     * 
     * @param sUrl
     * @param paramMap
     * @return
     */
    private String getUrlWithParams(String sUrl, Map<String, String> paramMap) {
        StringBuffer urlWithParams = new StringBuffer(sUrl);
        TreeMap<String, String> treeMap = new TreeMap<String, String>();
        treeMap.putAll(paramMap);
        for (Entry<String, String> paramEntry : treeMap.entrySet()) {
            if (paramEntry.equals(treeMap.firstEntry())) {
                urlWithParams.append("?");
            } else {
                urlWithParams.append("&");
            }
            urlWithParams.append(paramEntry.getKey() + "=" + paramEntry.getValue());
        }
        
        return urlWithParams.toString();
    }
    
    /**
     * 署名情報計算
     * 
     * @param sBaseString
     * @param sKeyString
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     */
    private static String computeSignature(String sBaseString, String sKeyString)
            throws NoSuchAlgorithmException, InvalidKeyException
    {
        SecretKey secretKey = null;

        byte[] keyBytes = sKeyString.getBytes();
        secretKey = new SecretKeySpec(keyBytes, "HmacSHA1");

        Mac mac = Mac.getInstance("HmacSHA1");

        mac.init(secretKey);

        byte[] text = sBaseString.getBytes();

        return new String(Base64.getEncoder().encodeToString((mac.doFinal(text))).trim());
    }

    /**
     * タイムスタンプ取得
     * 
     * @return
     */
    private String getTime() {
        long lMillis = System.currentTimeMillis();
        long lSecs = lMillis / 1000;
        return String.valueOf( lSecs );
    }
}