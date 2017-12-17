import com.google.gson.*;

public class TwitterSearch {

	public static void main(String[] args) {
		// Twitter TimeLineAPIオブジェクト生成
		TwitterAPI twitter = new TwitterAPI();
		try {
			// 検索結果JSON取得
			String sJson = twitter.getUsesTimeLine();
			// 結果出力
			printJson(sJson);
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	/**
	 * 結果出力
	 * 
	 * @param sJson
	 */
	public static void printJson(String sJson) {
		String sHeader= "<<<  Twitter API検索結果 - \"@realDonaldTrump\"  >>>";
		String sBoundary = "======================================";
		
		JsonParser jsonParser = new JsonParser();
		JsonArray aJson = (JsonArray)jsonParser.parse(sJson);
		
		System.out.println(sHeader);
		System.out.println(sBoundary);
		for (int i = 0; i < aJson.size(); i++) {
			JsonObject oJson = (JsonObject)aJson.get(i);
			JsonObject oUser = (JsonObject)oJson.get("user");
			System.out.println("<< " + oJson.get("created_at") + " - " + oUser.get("screen_name") + " >>");
			System.out.println(oJson.get("text"));
			System.out.println("------------------------");
		}	
		System.out.println(sBoundary);
	}
}
