package com.limenets.eorder.svc;

import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

import org.apache.commons.codec.binary.Base64;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.limenets.common.util.Converter;
import com.limenets.eorder.dao.ItemDao;

@Service
public class SapRestApiSvc {
	
//	private static final String authUser = "SND_LPKOLTP";
//	private static final String authPw = "cD/7k!6YJ1vRJeaNS9YBxYClMKAXZP";
//	private static final String ATP_CHECK_URL_DEV = "https://appl4.knaufgroup.com:50201/RESTAdapter/LPKOLTP/ATPCheck"; 
//	private static final String ATP_CHECK_URL_REAL = "https://dbs2pi.knaufgroup.com:50101/RESTAdapter/LPKOLTP/ATPCheck";
	
	public final int API_URL_SUPPLY = 100;
	public final int API_URL_CLOSE_ORDER = 200;
	public final int API_URL_FAIL_PRICE = 300;
	public final int API_URL_SUM_PRICE = 400;
	public final int API_URL_ACCOUNT = 500;
	
	@Inject private ItemDao itemDao;

	@Value("${sap.api.authUser}") private String authUser;
	@Value("${sap.api.authPw}") private String authPw;
	@Value("${sap.api.aptCheck}") private String ATP_CHECK_URL;
	@Value("${sap.api.supplier}") private String V_SUPPLIER_URL;
	@Value("${sap.api.closeOrder}") private String V_CLOSE_ORDER_URL;     
	@Value("${sap.api.failPrice}") private String V_FAIL_PRICE_URL;
	@Value("${sap.api.sumPrice}") private String V_SUM_PRICE_URL;
	@Value("${sap.api.account}") private String V_ACCOUNT_URL;
	
	private String createBasicAuthHeaderValue(String user, String password) {
        String auth = user + ":" + password;
        byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.UTF_8));
        String authHeaderValue = "Basic " + new String(encodedAuth);
        return authHeaderValue;
    }
	
	public Map<String, Object> getItemOne(String item_cd){
		Map<String, Object> svcMap = new HashMap<>();
		svcMap.put("r_itemcd", item_cd);
		return this.getItemOne(svcMap);
	}
	public Map<String, Object> getItemOne(Map<String, Object> svcMap){
		return itemDao.one(svcMap);
	}
	
	public Map<String, Object> getATPCheckList(Map<String, Object> params )/*throws Exception*/ {
		String ri_trid = Converter.toStr(params.get("ri_trid"));
		String ri_itemmcu = Converter.toStr(params.get("ri_itemmcu"));
		String ri_itemcd = Converter.toStr(params.get("ri_itemcd"));
		
		Map<String, Object> resMap = new HashMap<>();

		String devider = "!!";
		String[] ri_tridarr = ri_trid.split(devider);
		String[] ri_itemmcuarr = ri_itemmcu.split(devider);
		String[] ri_itemcdarr = ri_itemcd.split(devider);
		
		for(int i=0,j=ri_itemmcuarr.length; i<j; i++) {
			Map<String, Object> item = this.getItemOne(ri_itemcdarr[i]);
			
			params.put("MATERIAL", ri_itemcdarr[i]);  // O_ITEM_NEW  IETM_CD
			params.put("PLANT", ri_itemmcuarr[i]);    // O_ITEM_MCU  ITEM_MCU
			params.put("UNIT", item.get("UNIT4"));
			params.put("CHECK_RULE", "");
			Map<String, Object> is = this.getATPCheckItem(params);
			Map<String, Object> itemStock = new HashMap<>();
			itemStock.put("TOT_AVAIL", (is.get("COM_QTY")==null) ? "0" : is.get("COM_QTY").toString());
//			itemStock.put(ri_tridarr[i]+"_WEIGHT", (itemStock.get("REQ_QTY")==null ? "0" : itemStock.get("REQ_QTY").toString()));
			
			resMap.put(ri_tridarr[i], itemStock);
			resMap.put(ri_tridarr[i]+"_WEIGHT", Converter.toStr(item.get("WEIGHT")));
		}
		
		return resMap;
	}
	
	
	public Map<String, Object> getATPCheckItem(Map<String, Object> param){
		URL url = null;
		HttpURLConnection conn = null;
		
		Map<String, Object> returnMap = new HashMap<>();
		
		String responseData = "";
		BufferedReader br = null;
		StringBuffer sb = null;
		
		String COM_QTY = "";
		String REQ_QTY = "";
		
		String ParamData = "{"
				+ "\"PLANT\":\"" + param.get("PLANT") + "\","
				+ "\"MATERIAL\":\"" + param.get("MATERIAL") + "\","
				+ "\"UNIT\":\"" + "PC" + "\","
				+ "\"CHECK_RULE\":\"" + "A" + "\""
				+ "}";
		System.out.println("[[ParamData]]");
		System.out.println(ParamData);
		String returnData = "";
		
		try {
			url = new URL(ATP_CHECK_URL);
			conn = (HttpURLConnection)url.openConnection();
			
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json; utf-8");
			conn.setRequestProperty("Accept", "application/json");
			conn.setRequestProperty("Authorization", createBasicAuthHeaderValue(authUser, authPw));
			conn.setDoOutput(true);
			try {
				OutputStream os = conn.getOutputStream();
				byte request_data[] = ParamData.getBytes("UTF-8");
				os.write(request_data);
				os.close();
 			} catch(Exception e) {
 				e.printStackTrace();
 			}
			
			conn.connect();
			
			br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
			sb = new StringBuffer();
			while((responseData = br.readLine()) != null) {
				sb.append(responseData);
			}
			
			returnData = sb.toString();
			
			try {
				JSONParser parser = new JSONParser();
				JSONObject jsonObject = (JSONObject) parser.parse(returnData);
				COM_QTY = jsonObject.get("COM_QTY").toString();  
				REQ_QTY = jsonObject.get("REQ_QTY").toString();
			} catch(Exception e) {
				COM_QTY = "0";
				REQ_QTY = "0";
			}
			
			String responseCode = String.valueOf(conn.getResponseCode());
			System.out.println("HTTP Response Code : " + responseCode);
			System.out.println("HTTP Response Data : " + returnData);
			
			if(responseCode.equals("200")) {
				returnMap.put("COM_QTY", COM_QTY.isEmpty() ? "0" : COM_QTY);
				returnMap.put("REQ_QTY", REQ_QTY.isEmpty() ? "0" : REQ_QTY);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally { 
			try {
				if (br != null) {
					br.close();	
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		System.out.println("[[RETURN MAP]]");
		System.out.println(returnMap);
		
		return returnMap;
	}
	
	public Map<String, Object> getTransactionApi(String param, int typeApi) {
		Map<String, Object> resultMap = null;
		
		switch(typeApi) {
		case API_URL_SUPPLY: 
			resultMap = getTransactionMap(param, V_SUPPLIER_URL, API_URL_SUPPLY);
			break;
			
		case API_URL_CLOSE_ORDER:
			resultMap = getTransactionMap(param, V_CLOSE_ORDER_URL, API_URL_CLOSE_ORDER);
			break;
			
		case API_URL_FAIL_PRICE: 
			resultMap = getTransactionMap(param, V_FAIL_PRICE_URL, API_URL_FAIL_PRICE);
			break;
			
		case API_URL_SUM_PRICE:
			resultMap = getTransactionMap(param, V_SUM_PRICE_URL, API_URL_SUM_PRICE);
			break;
			
		case API_URL_ACCOUNT:
			resultMap = getTransactionMap(param, V_ACCOUNT_URL, API_URL_ACCOUNT);
			break;
		}
		
		return resultMap;
	}	
	
	@SuppressWarnings("unchecked")
	public Map<String, Object> getTransactionMap(String ParamData, String apiUrl, int typeAPI){
		URL url = null;
		HttpURLConnection conn = null;
		
		Map<String, Object> returnMap = null;
 		String returnData = "";
		
		String responseData = "";
		BufferedReader br = null;
		StringBuffer sb = null;

		try {
			url = new URL(apiUrl);
			conn = (HttpURLConnection)url.openConnection();
			
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json; utf-8");
			conn.setRequestProperty("Accept", "application/json");
			conn.setRequestProperty("Authorization", createBasicAuthHeaderValue(authUser, authPw));
			conn.setDoOutput(true);
			try {
				OutputStream os = conn.getOutputStream();
				byte request_data[] = ParamData.getBytes("UTF-8");
				os.write(request_data);
				os.close();
 			} catch(Exception e) {
 				e.printStackTrace();
 			}
			
			conn.connect();
			
			br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
			sb = new StringBuffer();
			while((responseData = br.readLine()) != null) {
				sb.append(responseData);
			}
			
			returnData = sb.toString();			
			JSONParser parser = new JSONParser();
			try {
				JSONObject jsonObject = (JSONObject) parser.parse(returnData);
				System.out.println("[" + typeAPI + "]");
				//System.out.println(jsonObject);
				switch(typeAPI) {
					case API_URL_SUPPLY: {
						returnMap = new ObjectMapper().readValue(jsonObject.get("supplier").toString(), Map.class);
					} break;
					
					case API_URL_CLOSE_ORDER: { 
						List<Map<String, Object>> returnList = new ArrayList<Map<String, Object>>();
						Object json = jsonObject.get("list");
						if(json instanceof JSONObject) {
							Map<String, Object> jsonMap = new ObjectMapper().readValue(jsonObject.get("list").toString(), Map.class);
							returnList.add(jsonMap);
						} else {
							returnList = new ObjectMapper().readValue(jsonObject.get("list").toString(), List.class);
						}
						returnMap = new HashMap<>();
						returnMap.put("list", returnList);
					} break;
					
					case API_URL_FAIL_PRICE: {
						returnMap = new ObjectMapper().readValue(jsonObject.get("price").toString(), Map.class);
					} break;
					
					case API_URL_SUM_PRICE: {
						returnMap = new ObjectMapper().readValue(jsonObject.get("price").toString(), Map.class);
					} break;
					
					case API_URL_ACCOUNT: {
						returnMap = new ObjectMapper().readValue(jsonObject.get("account").toString(), Map.class);
					} break;
				}
				
			} catch (ParseException e) {
				e.printStackTrace();
				returnMap = null;
			}
			
		} catch (IOException e) {
			e.printStackTrace();
			returnMap = null;
		} finally { 
			try {
				if (br != null) {
					br.close();
					return returnMap;
				}
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}
		
		return returnMap;
	}
}
