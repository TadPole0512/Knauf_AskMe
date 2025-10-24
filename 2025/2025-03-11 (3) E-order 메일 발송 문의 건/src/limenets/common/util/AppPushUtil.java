package com.limenets.common.util;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.lang3.StringUtils;

public class AppPushUtil {
	/**
	 * 앱 푸시 전송.
	 * @param push_title : 푸시 타이틀
	 * @param push_contents : 푸시 내용
	 * @param push_id_list : 푸시 아이디
	 * @param move_url : 푸시 클릭시 이동할 URL
	 */
	public void sendAppPush(String push_title, String push_contents, List<String> push_id_list, String move_url) {
		if(!push_id_list.isEmpty()) {
			try {
				String app_id = "c51373a8-8b61-4bac-b86e-8542f2d0ff06"; // 고정값
				String restapi_key = "ZmIyYjNmOTItMGJlNy00M2Y1LThkZTktZjlhMTJjMDc3MGJl"; // 고정값
				String jsonResponse;
				
				if(StringUtils.equals("", push_title)) push_title = "e-Ordering"; 
				
				// 전송 대상자 세팅.
				String push_ids = "";
				for(int i=0,j=push_id_list.size(); i<j; i++) {
					if(i == 0) {
						push_ids += "[";
						push_ids += "\""+push_id_list.get(i)+"\"";
					}
					else {
						push_ids += ",\""+push_id_list.get(i)+"\"";
					}
					
					if(i == j-1) push_ids += "]";
				}
				
				// 이동 URL 세팅.
				String data_param = "{}";
				if(!StringUtils.equals("", move_url)) {
					data_param = "{\"custom_url\": \""+move_url+"\"}";
				}
				   
				URL url = new URL("https://onesignal.com/api/v1/notifications");
				HttpURLConnection con = (HttpURLConnection)url.openConnection();
				con.setUseCaches(false);
				con.setDoOutput(true);
				con.setDoInput(true);
	
				con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
				con.setRequestProperty("Authorization", "Basic "+restapi_key);
				con.setRequestMethod("POST");
				
				String strJsonBody = "{"
						+   "\"app_id\": \""+app_id+"\","
						+   "\"include_player_ids\": "+push_ids+","
						//+   "\"include_player_ids\": [\"6392d91a-b206-4b7b-a620-cd68e32c3a76\",\"76ece62b-bcfe-468c-8a78-839aeaa8c5fa\",\"8e0f21fa-9a5a-4ae7-a9a6-ca1f24294b86\"],"
						+   "\"headings\": {\"en\": \""+push_title+"\"},"
						+   "\"contents\": {\"en\": \""+push_contents+"\"},"
						+   "\"data\": "+data_param+","
						//+   "\"large_icon\": \"icon_96\","
						//+   "\"small_icon\": \"icon_48\","
						+   "\"ios_badgeType\": \"Increase\","
						+   "\"ios_badgeCount\": 1"
						+ "}";
				   
				System.out.println("strJsonBody:\n" + strJsonBody);
	
				byte [] sendBytes = strJsonBody.getBytes("UTF-8");
				con.setFixedLengthStreamingMode(sendBytes.length);
	
				OutputStream outputStream = con.getOutputStream();
				outputStream.write(sendBytes);
	
				int httpResponse = con.getResponseCode();
				System.out.println("httpResponse: " + httpResponse);
	
				if (httpResponse >= HttpURLConnection.HTTP_OK && httpResponse < HttpURLConnection.HTTP_BAD_REQUEST) {
					Scanner scanner = new Scanner(con.getInputStream(), "UTF-8");
					jsonResponse = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
					scanner.close();
				}
				else {
					Scanner scanner = new Scanner(con.getErrorStream(), "UTF-8");
					jsonResponse = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
					scanner.close();
				}
				System.out.println("jsonResponse:\n" + jsonResponse);
				   
			} catch(Throwable t) {
				t.printStackTrace();
			}
		
		}
	}
}
