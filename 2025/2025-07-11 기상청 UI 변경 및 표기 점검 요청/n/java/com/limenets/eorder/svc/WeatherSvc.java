package com.limenets.eorder.svc;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.limenets.common.exception.MsgCode;
import com.limenets.common.util.Converter;
import com.limenets.eorder.dao.WeatherDao;
import com.limenets.eorder.dto.WeatherDto;
import com.limenets.eorder.dto.WeatherForecastDto;
import com.limenets.eorder.util.GeoTransUtil;
import com.limenets.eorder.util.WeatherMidLandResponseJson;
import com.limenets.eorder.util.WeatherMidLandResponseXml;
import com.limenets.eorder.util.WeatherMidTaResponseJson;
import com.limenets.eorder.util.WeatherMidTaResponseXml;
import com.limenets.eorder.util.WeatherShortResponseJson;
import com.limenets.eorder.util.WeatherShortSummaryByHalfDay;

/**
 * 날씨 예보 서비스.
 */
@Service
public class WeatherSvc {
	private static final Logger logger = LoggerFactory.getLogger(WeatherSvc.class);
	
	@Value("${kakao.local.api.key}")     private String kakaoApiKey;
	@Value("${kakao.local.api.url}")     private String kakaoApiUrl;
	@Value("${weather.api.en.key}")      private String weatherApiEnKey; //인코딩 키. 기상청에서 인코딩, 디코딩키 둘 다 제공. 디코딩을 인코딩해서 사용하면 오류 발생해서 그냥 인코딩 버전을 사용.
	@Value("${weather.api.de.key}")      private String weatherApiDeKey; //디코딩 키. 기상청측에서도 api환경, 호출 조건에 따라 적용되는 방식이 다르다며 둘 중 구동되는 키를 사용하라고 함.
	@Value("${weather.api.short.url}")   private String weatherApiShortUrl;
	@Value("${weather.api.midta.url}")   private String weatherApiMidTaUrl;
	@Value("${weather.api.midland.url}") private String weatherApiMidLandUrl;
	
	
	@Inject private WeatherDao weatherDao;
	
	@Autowired
	private KakaoLocalApiSvc kakaoLocalApiService;
	
	
	//로컬에서 카카오 api 사용시 인증서 관련 오류 발생. 테스트 환경에서만 사용해야 함. 보안에 취약.
	private static void disableSslVerification() throws Exception {
		TrustManager[] trustAllCerts = new TrustManager[]{
			new X509TrustManager() {
				public X509Certificate[] getAcceptedIssuers() { return null; }
				public void checkClientTrusted(X509Certificate[] certs, String authType) { }
				public void checkServerTrusted(X509Certificate[] certs, String authType) { }
			}
		};

		SSLContext sc = SSLContext.getInstance("SSL");
		sc.init(null, trustAllCerts, new java.security.SecureRandom());
		HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

		HostnameVerifier allHostsValid = (hostname, session) -> true;
		HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
	}
	
	/**
	 * 납품처의 주소 정보로 해당 지역의 날씨 정보를 기상청 API로 조회
	 * @작성일 : 2025. 6. 12
	 * @작성자 : ijy
	 */
	public Map<String, Object> getWeatherForecastApiAjax(Map<String, Object> params) throws Exception {
		
//		try {
//			disableSslVerification(); //로컬 테스트용.
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		
		Map<String, Object> svcMap = new HashMap<>();
		Map<String, Object> msgMap = new HashMap<>();
		
		String forecastType = Converter.toStr(params.get("forecastType")); //1:납품일자 선택 시, 2:상단 D+7

		if("1".equals(forecastType)) {
			
			String addr = Converter.toStr(params.get("addr")).trim(); //납품처 주소
			String selDate = Converter.toStr(params.get("selDate")).trim(); //선택날짜
			
			if("".equals(addr) || "".equals(selDate) || "".equals(forecastType)) {
				return MsgCode.getResultMap(MsgCode.DATA_REQUIRE_ERROR2);
			}
			
			svcMap.put("addr",  addr);
			
			//선택 날짜가 오늘부터 3일 이내인지 4~10일인지 체크. 11일 이상은 기상청 API에서 서비스 제공 안함.
			String toDate = Converter.dateToStr("yyyyMMdd");
			selDate  = Converter.convertDateFormat(selDate, "yyyyMMdd");
			int diff = Converter.dateDiff(toDate, selDate, "D");
//			//logger.debug("diff : " + diff);
			
			//api 종류 (1~3일 단기예보, 4~10일 중기예보) > 기상청 API는 주소나 우편번호를 쓰지 않고 좌표값이나 기상청에서 정의한 구역 코드를 이용해서 호출해야 함. 자세한 설명은 아래에..
			if(diff > 3) {
				//3일 넘어가면 중기예보 사용.
				//중기예보: 4~10일까지 일별로 오전, 오후 요약 날씨 조회 가능. (8,9,10일 날씨는 오전,오후 구분 없이 하루 요약 정보만 조회 가능)
				//중기에보는 중기기온예보와 중기육상예보 2가지로 구분.
				//중기기온예보는 최저/최고 기온 조회, 중기육상예보는 맑음,흐림 등 날씨 정보 / 비,눈,비없음 등 강수여부 정보 / 강수확률 정보.
				//중기예보 API는 기상청에서 미리 정의한 중기기온예보구역코드와 중기육상예보구역코드를 활용해서 호출해야 함. (같은 중기예보구역코든데 코드값이 또 달라..)
				
				//주소 정보로 각 예보구역코드 조회
				String midTaAreaCd   = getWeatherAreaCd(addr, "ta");   //중기기온예보
				String midLandAreaCd = getWeatherAreaCd(addr, "land"); //중기육상예보
				
//				//logger.debug("midTaAreaCd = " + midTaAreaCd);
//				//logger.debug("midLandAreaCd = " + midLandAreaCd);
				
				//예보구역코드로 중기 예보 API호출.
				///ijy
				String tmFc = toDate+"0600"; //중기예보 6시 발표. 0600은 그냥 고정.
				Map<String, Object> midTaItem   = getWeatherMidTaApi(tmFc, midTaAreaCd);
				Map<String, Object> midLandItem = getWeatherMidLandApi(tmFc, midLandAreaCd);
				
				
				//2종의 응답값을 하나로 묶어서 리턴.
				List<Map<String, Object>> weatherList = new ArrayList<>();
//				List<Map<String, Object>> midList = summarizeMidForecastOne(midTaItem, midLandItem, selDate);  //이게 통합이 될거라 생각했는데 안됨...
//				
//				//조회된 날씨 정보 목록 중 원하는 날짜의 데이터만 추출.
//				if(midList != null && midList.size() > 0) {
//					for(Map<String, Object> oneDay : midList ) {
//						if(selDate.equals(oneDay.get("selDate"))) {
//							weatherList.add(oneDay);
//						}
//					}
//				}
				
				msgMap.put("resultCode", "00");
//				msgMap.put("weatherList", weatherList);
				msgMap.put("weatherList", summarizeMidForecastOne(midTaItem, midLandItem, selDate));
//				

			} else {
				//3일 이내는 단기예보 사용.
				//단기예보: 1~5일까지 1시간 단위로 날씨 조회 가능(5일차 데이터는 1~4일 데이터와 다르게 요약 날씨인듯, 데이터 불량. 어차피 1~3일만 사용 예정.)
				//단기예보 API는 위/경도 값을 기상청API에서 사용하는 격자값으로 변환 후 호출해야 함.
				
				//주소 정보를 카카오 로컬 API를 통해 위/경도 조회 후 격자값으로 변환.
//				Map<String, Object> kakaoMap = getKakaoLocalApi(addr);
				Map<String, Object> kakaoMap = kakaoLocalApiService.getKakaoLocalApi(addr);
				String nx = Converter.toStr(kakaoMap.get("nx"));
				String ny = Converter.toStr(kakaoMap.get("ny"));
				
				//단기예보 API호출. 날짜 형식은 yyyyMMdd로 해야 됨.
				msgMap = getWeatherShortApi(toDate, nx, ny); //1일~4일. (5일 데이터는 의미 없음. 데이터 이상함)
				
				List<Map<String, Object>> weatherList = new ArrayList<>();
				List<Map<String, Object>> shortList   = (List<Map<String, Object>>) msgMap.get("shortList");
				
				//조회된 날씨 정보 목록 중 원하는 날짜의 데이터만 추출.
				if(shortList != null && shortList.size() > 0) {
					for(Map<String, Object> oneDay : shortList ) {
						if(selDate.equals(oneDay.get("date"))) {
							weatherList.add(oneDay);
						}
					}
				}
				msgMap.put("weatherList", weatherList);
				msgMap.remove("shortList");
				
			}
			
			
			
		} else {
//			//상단 D+7은 단기와 중기 모두 사용해서 하나로 합쳐서 뿌려줘야 함. 1~3일은 단기, 4~10일은 중기. 단기예보의 형식으로 통합하면 될듯.
//			List<Map<String, Object>> weatherList = new ArrayList<>();
//
//
//			/* **************** 주요시도 목록 ******************** */
//			/* 1. 주요 시도 배열 */
//			//String[] cities = { "서울", "부산", "대구", "인천", "광주", "대전", "울산", "세종", "수원", "춘천", "청주", "천안", "전주", "여수", "포항", "창원", "제주" };
//			String[] cities = { "서울", "경기", "인천", "대전", "강원", "대구", "부산", "광주", "울산" };
////			String[] cities = { "11B00000", "11H20000", "11H10000", "11B00000", "11F20000", "11C20000", "11H20000", "11C20000" };
//
//
//			/* 2. 오늘 날짜 구하기 */
//			String toDate = Converter.dateToStr("yyyyMMdd");
//
//			//String addr = cities[0];
//			/* **************** 3. 주요 시도 반복 ******************** */
//			for(String addr : cities) {
//				/* **************** 3-1. 주요 시도 좌표(KakaoLocalAPi) 구하기 ******************** */
//				//1~3일. 주소 정보를 카카오 로컬 API를 통해 위/경도 조회 후 격자값으로 변환.
//				//logger.debug("getKakaoLocalApi Start Time : " + Converter.dateToStr("yyyy-MM-dd HH:mm:ss"));
//
//				Map<String, Object> kakaoMap = getKakaoLocalApi(addr);
//				String nx = Converter.toStr(kakaoMap.get("nx"));
//				String ny = Converter.toStr(kakaoMap.get("ny"));
//				//logger.debug("################ addr : " + addr);
//				//logger.debug("################ KAKAO NX : " + kakaoMap.get("nx"));
//				//logger.debug("################ KAKAO NY : " + kakaoMap.get("ny"));
////				logger.debug("################ NX : " + nx);
////				logger.debug("################ NY : " + ny);
//			
//				//logger.debug("getKakaoLocalApi End Time : " + Converter.dateToStr("yyyy-MM-dd HH:mm:ss"));
//
//				/* **************** 3-2. 단기예보(1-3일) 구하기 ******************** */
//				//단기예보 API호출. 날짜 형식은 yyyyMMdd로 해야 됨.
//				//logger.debug("getWeatherShortApi Start Time : " + Converter.dateToStr("yyyy-MM-dd HH:mm:ss"));
//				msgMap = getWeatherShortApi(toDate, nx, ny); //1일~4일. (5일 데이터는 의미 없음. 데이터 이상함)
//				//logger.debug("getWeatherShortApi End Time : " + Converter.dateToStr("yyyy-MM-dd HH:mm:ss"));
//
//				List<Map<String, Object>> shortList   = (List<Map<String, Object>>) msgMap.get("shortList");
//
//				/* **************** 3-3. 주소 정보로 중기기온예보 구역코드 구하기 ******************** */
//				//4~10일. 주소 정보로 각 예보구역코드 조회
//				//logger.debug("getWeatherAreaCd Start Time : " + Converter.dateToStr("yyyy-MM-dd HH:mm:ss"));
//				String midTaAreaCd   = getWeatherAreaCd(addr, "ta");   //중기기온예보
//				//logger.debug("getWeatherAreaCd End Time : " + Converter.dateToStr("yyyy-MM-dd HH:mm:ss"));
//				/* **************** 3-4. 주소 정보로 중기육상예보 구역코드 구하기 ******************** */
//				//logger.debug("getWeatherAreaCd Start Time : " + Converter.dateToStr("yyyy-MM-dd HH:mm:ss"));
//				String midLandAreaCd = getWeatherAreaCd(addr, "land"); //중기육상예보
//				//logger.debug("getWeatherAreaCd End Time : " + Converter.dateToStr("yyyy-MM-dd HH:mm:ss"));
//
//
//				//예보구역코드로 중기 예보 API호출.
//				String tmFc = toDate+"0600"; //중기예보 6시 발표. 0600은 그냥 고정.
//
//				/* **************** 3-5. 중기기온예보(4-10일) 구하기 ******************** */
//				//logger.debug("getWeatherMidTaApi Start Time : " + Converter.dateToStr("yyyy-MM-dd HH:mm:ss"));
//				Map<String, Object> midTaItem   = getWeatherMidTaApi(tmFc, midTaAreaCd);
//				//logger.debug("getWeatherShortApi End Time : " + Converter.dateToStr("yyyy-MM-dd HH:mm:ss"));
//				/* **************** 3-6. 중기육상예보(4-10일) 구하기 ******************** */
//				//logger.debug("getWeatherMidLandApi Start Time : " + Converter.dateToStr("yyyy-MM-dd HH:mm:ss"));
//				Map<String, Object> midLandItem = getWeatherMidLandApi(tmFc, midLandAreaCd);
//				//logger.debug("getWeatherMidLandApi End Time : " + Converter.dateToStr("yyyy-MM-dd HH:mm:ss"));
//
//
//
//				/* **************** 3-7. 중기기온예보와 중기육상예보 합치기 ******************** */
//				//2종의 응답값을 하나로 묶어서 리턴.
//				//logger.debug("summarizeMidForecast Start Time : " + Converter.dateToStr("yyyy-MM-dd HH:mm:ss"));
//				List<Map<String, Object>> midList = summarizeMidForecast(midTaItem, midLandItem);  //이게 통합이 될거라 생각했는데 안됨...
//				//logger.debug("summarizeMidForecast End Time : " + Converter.dateToStr("yyyy-MM-dd HH:mm:ss"));
//				
//		
//				/* **************** 3-8. 단기 예보와 중기 예보 합치기 ******************** */
//				List<Map<String, Object>> mergeList = new ArrayList<>(shortList.subList(0, shortList.size()- 2));
//				mergeList.addAll(midList);
//
//				//logger.debug("cityWeatherList Start Time : " + Converter.dateToStr("yyyy-MM-dd HH:mm:ss"));
//				Map<String, Object> tmpMap = new HashMap<>();
//				tmpMap.put("city", addr);
//				tmpMap.put("cityWeatherList", mergeList);
//				weatherList.add(tmpMap);
//				//logger.debug("cityWeatherList End Time : " + Converter.dateToStr("yyyy-MM-dd HH:mm:ss"));
//			}
//
////			List<WeatherCityDto> weatherCitiesList = new ArrayList<>();
////			weatherCitiesList = setMainCitiesCoords();
//			//System.out.println("weatherCitiesList : " + weatherCitiesList);
//			//getMainCitiesOfWeatherForecast(weatherCitiesList);
//
//			//getMainCitiesOfWeatherForecast(weatherCitiesList);
//			msgMap.put("weatherList", weatherList);
			//msgMap.put("weatherList", getWeatherListByCity());
//			getMainCitiesOfWeatherForecast(setMainCitiesCoords());
			return getWeatherListByCity();

		}
		
		
		return msgMap;
	}
	
	
	/**
	 * 카카오 로컬 API. 납품처 주소로 위/경도 조회. 사용안한ㅁ
	 * @작성일 : 2025. 6.12
	 * @작성자 : ijy
	 */
	public Map<String, Object> getKakaoLocalApi(String addr){
		Map<String, Object> returnMap = new HashMap<>();
		

//		logger.debug("Start Time : " + System.currentTimeMillis());
		String query = UriComponentsBuilder
				.fromHttpUrl(kakaoApiUrl)
				.queryParam("query", addr)
				.build()
				.toUriString();
//		logger.debug("Fininsh Time : " + System.currentTimeMillis());

		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", "KakaoAK " + kakaoApiKey); //카카오 api는 키를 헤더에 담아서 보내야함. (기상청은 url에 추가)

		HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
		RestTemplate restTemplate = new RestTemplate();

//		logger.debug("Start Time : " + System.currentTimeMillis());
		ResponseEntity<JsonNode> response = restTemplate.exchange(
				query,
				HttpMethod.GET,
				requestEntity,
				JsonNode.class
		);
//		logger.debug("Finish Time : " + System.currentTimeMillis());

		JsonNode documents = response.getBody().path("documents");
		
		//logger.debug("response.getStatusCode = " + response.getStatusCode());
		//logger.debug("response.getHeaders = " + response.getHeaders());
		//logger.debug("response.getBody = " + response.getBody());
		
		int nx = 0;
		int ny = 0;
		
		if (documents.isArray() && documents.size() > 0) {
			JsonNode location = documents.get(0);
			double x = location.path("x").asDouble(); // longitude
			double y = location.path("y").asDouble(); // latitude
			
			//logger.debug("x: " + x + " / y: " + y);
			
			GeoTransUtil.GridPoint point = GeoTransUtil.convertGPS2GRID(x, y);
			
			nx = point.x;
			ny = point.y;
			
			//logger.debug("기상청 격자 X: " + nx);
			//logger.debug("기상청 격자 Y: " + ny);
			
		} else {
			//logger.debug("카카오 API 좌표 조회 실패. " + addr);
		}
		
		returnMap.put("nx", nx);
		returnMap.put("ny", ny);
		return returnMap;
	}
	
	
	/**
	 * 기상청 API 단기예보######################################
	 * @작성일 : 2025. 6. 12
	 * @작성자 : ijy
	 */
	public Map<String, Object> getWeatherShortApi(String base_date, String nx, String ny) {
	    Map<String, Object> returnMap = new HashMap<>();

	    try {
	        String base_time = "0500"; // 단기예보 발표시각 고정

	        StringBuilder urlBuilder = new StringBuilder(weatherApiShortUrl);
	        urlBuilder.append("?serviceKey=").append(weatherApiEnKey);
	        urlBuilder.append("&pageNo=1");
	        urlBuilder.append("&numOfRows=1000");
	        urlBuilder.append("&dataType=JSON");  // XML → JSON으로 변경!
	        urlBuilder.append("&base_date=").append(base_date);
	        urlBuilder.append("&base_time=").append(base_time);
	        urlBuilder.append("&nx=").append(nx);
	        urlBuilder.append("&ny=").append(ny);

	        URL url = new URL(urlBuilder.toString());
	        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

	        conn.setConnectTimeout(2000); // 타임아웃 명확하게!
	        conn.setReadTimeout(2000);
	        conn.setRequestMethod("GET");
	        // Content-Type은 생략하거나, "application/json"으로 명확하게 (GET에서는 없어도 됨)
	        // conn.setRequestProperty("Content-type", "application/json");

	        BufferedReader rd;
	        int respCode = conn.getResponseCode();
	        if (respCode >= 200 && respCode <= 300) {
	            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	        } else {
	            rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
	        }

	        StringBuilder sb = new StringBuilder();
	        String line;
	        while ((line = rd.readLine()) != null) sb.append(line);
	        rd.close();
	        conn.disconnect();

	        String json = sb.toString();

	        // JSON 파싱 (Jackson 사용)
	        ObjectMapper mapper = new ObjectMapper();
	        WeatherShortResponseJson response = mapper.readValue(json, WeatherShortResponseJson.class);

	        String resultCode = response.getResponse().getHeader().getResultCode();
	        returnMap.put("resultCode", resultCode);

	        if ("00".equals(resultCode)) {
	            List<Map<String, Object>> list = WeatherShortSummaryByHalfDay.summarizeJson(
	                    response.getResponse().getBody().getItems().getItem()
	            );
	            if (list != null && !list.isEmpty()) {
	                returnMap.put("shortList", list);
	            }
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	    }

	    return returnMap;
	}

	
	
	/**
	 * 기상청 API 중기기온예보
	 * @작성일 : 2025. 6. 12
	 * @작성자 : ijy
	 */
	public Map<String, Object> getWeatherMidTaApi(String tmFc, String regId) {
	    Map<String, Object> returnMap = new HashMap<>();
	    try {
	        StringBuilder urlBuilder = new StringBuilder(weatherApiMidTaUrl);
	        urlBuilder.append("?serviceKey=").append(weatherApiEnKey);
	        urlBuilder.append("&pageNo=1");
	        urlBuilder.append("&numOfRows=100");
	        urlBuilder.append("&dataType=JSON"); // XML → JSON
	        urlBuilder.append("&regId=").append(regId);
	        urlBuilder.append("&tmFc=").append(tmFc);

	        URL url = new URL(urlBuilder.toString());
	        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

	        conn.setConnectTimeout(2000); // 네트워크 타임아웃 명확히
	        conn.setReadTimeout(2000);
	        conn.setRequestMethod("GET");
	        // Content-Type GET에서는 필요없음. 주석 처리하거나 생략
	        // conn.setRequestProperty("Content-type", "application/json");

	        BufferedReader rd;
	        int respCode = conn.getResponseCode();
	        if (respCode >= 200 && respCode <= 300) {
	            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	        } else {
	            rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
	        }

	        StringBuilder sb = new StringBuilder();
	        String line;
	        while ((line = rd.readLine()) != null) sb.append(line);
	        rd.close();
	        conn.disconnect();

	        String json = sb.toString();

	        // JSON → 객체 변환 (Jackson 사용)
	        ObjectMapper mapper = new ObjectMapper();
	        WeatherMidTaResponseJson response = mapper.readValue(json, WeatherMidTaResponseJson.class);

	        String resultCode = response.getResponse().getHeader().getResultCode();
	        //returnMap.put("resultCode", resultCode);


	        if ("00".equals(resultCode)) {
	            List<WeatherMidTaResponseJson.Item> itemList = response.getResponse().getBody().getItems().getItem();
	            if (itemList != null && !itemList.isEmpty()) {
	                // 여러 개가 올 수도 있음. 첫 번째만 반환
	                returnMap.put("midtaItem", itemList.get(0));
	            }
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return returnMap;
	}
	
	
	/**
	 * 기상청 API 중기육상예보
	 * @작성일 : 2025. 6. 12
	 * @작성자 : ijy
	 */
	public Map<String, Object> getWeatherMidLandApi(String tmFc, String regId) {
	    Map<String, Object> returnMap = new HashMap<>();
	    try {
	        StringBuilder urlBuilder = new StringBuilder(weatherApiMidLandUrl);
	        urlBuilder.append("?serviceKey=").append(weatherApiEnKey);
	        urlBuilder.append("&pageNo=1");
	        urlBuilder.append("&numOfRows=100");
	        urlBuilder.append("&dataType=JSON"); // XML → JSON
	        urlBuilder.append("&regId=").append(regId);
	        urlBuilder.append("&tmFc=").append(tmFc);

	        URL url = new URL(urlBuilder.toString());
	        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

	        conn.setConnectTimeout(2000); // 타임아웃 명확히
	        conn.setReadTimeout(2000);
	        conn.setRequestMethod("GET");
	        // Content-Type은 GET에선 생략 가능. 필요시만 설정
	        // conn.setRequestProperty("Content-type", "application/json");

	        BufferedReader rd;
	        int respCode = conn.getResponseCode();
	        if (respCode >= 200 && respCode <= 300) {
	            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	        } else {
	            rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
	        }

	        StringBuilder sb = new StringBuilder();
	        String line;
	        while ((line = rd.readLine()) != null) sb.append(line);
	        rd.close();
	        conn.disconnect();

	        String json = sb.toString();

	        // JSON 파싱 (Jackson 사용)
	        ObjectMapper mapper = new ObjectMapper();
	        WeatherMidLandResponseJson response = mapper.readValue(json, WeatherMidLandResponseJson.class);

	        String resultCode = response.getResponse().getHeader().getResultCode();

	        if ("00".equals(resultCode)) {
	            List<WeatherMidLandResponseJson.Item> itemList = response.getResponse().getBody().getItems().getItem();
	            if (itemList != null && !itemList.isEmpty()) {
	                returnMap.put("midlandItem", itemList.get(0)); // 첫 번째만 반환, 필요시 전부
	            }
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return returnMap;
	}

	
	/**
	 * 주소로 중기예보구역코드 조회.
	 * 기상청에서 제공한 구역 코드는 광주, 고성 등 동일 지명이 존재하고 전북자치도, 충청남도 등으로 제공되는데
	 * 크나우프에서 사용하는 실제 주소는 광주 북구 무슨대로, 광주시 남구 무슨동, 광주광역시, 전북 순천 무슨리, 전라북도 순천 무슨리
	 * 경남 고성, 경상남도 고성, 강원도 고성, 강원 고성, 대구 북구 고성동 등 과 같은 다양한 형태로 저장되어 있음.
	 * 단순 주소로만 조회하면 오작동할 확률이 높아 구역코드를 경남 고성, 경상남도 고성, 강원 고성, 강원도 고성 강원특별자치도 고성 등 동일 코드, 다양한 명칭으로 적재하고 매칭 로직 추가.
	 * @작성일 : 2025. 6. 13
	 * @작성자 : ijy
	 */
	public String getWeatherAreaCd (String addr, String type) {
		Map<String, Object> svcMap = new HashMap<>();
		svcMap.put("addr", addr);
		
		//주소 정보로 예보구역코드를 조회, 더 일치하는 구역코드로 매핑.
		List<WeatherDto> areaList = new ArrayList<WeatherDto>();
		if("ta".equals(type)) {
			areaList = weatherDao.getWeatherMidTaAreaList(svcMap); //기온
		} else {
			//중기육상예보구역코드는 서울, 인천, 경기, 경남, 경북, 강원 등 광역자치단체 구분
			//다른 지역은 문제 없으나 강원 지역만 강원 영동,영서로 구분됨.
			//강원은 광역이 아닌 춘천, 원주, 홍천, 강릉, 동해, 속초 등 기초자치단체까지 적재하여 영동, 영서 구분
			areaList = weatherDao.getWeatherMidLandAreaList(svcMap); //육상
		}
		
		//고성, 광주 등 동일 지명이 포함된 주소일 경우 오작동 예방 차원, 더 일치하는 구역코드로 매칭.
		String areaCd = "";
		String areaNm = "";
		String normalizedAddress = addr.replace("도 ", "도").replaceAll("\\s+", "");
		Optional<WeatherDto> bestMatch = areaList.stream()
		    .filter(dto -> normalizedAddress.contains(dto.getAREA_NM().replaceAll("\\s+", "")))
		    .max(Comparator.comparingInt(dto -> dto.getAREA_NM().replaceAll("\\s+", "").length()));

		if (bestMatch.isPresent()) {
		    areaCd = bestMatch.get().getAREA_CD();
		    areaNm = bestMatch.get().getAREA_NM();
		    //logger.debug(type + "] 최장 일치 코드: " + areaCd + " / NM: " + areaNm + " / addr:" + addr);
		} else {
		    //logger.debug(type + "] 일치하는 코드 없음 / " + addr);
			areaCd = ((WeatherDto)areaList.get(0)).getAREA_CD();
			areaNm = ((WeatherDto)areaList.get(0)).getAREA_NM();
		}

		return areaCd;
	}
	
	
	public static List<Map<String, Object>> summarizeMidForecastOne( Map<String, Object> tItem, Map<String, Object> lItem ) {
		
//      WeatherMidTaResponseXml.Body.Items.Item taItem     = (WeatherMidTaResponseXml.Body.Items.Item) tItem.get("midtaItem");
//      WeatherMidLandResponseXml.Body.Items.Item landItem = (WeatherMidLandResponseXml.Body.Items.Item) lItem.get("midlandItem");
		WeatherMidTaResponseJson.Item taItem     = (WeatherMidTaResponseJson.Item) tItem.get("midtaItem");
        WeatherMidLandResponseJson.Item landItem = (WeatherMidLandResponseJson.Item) lItem.get("midlandItem");

        
	    List<Map<String, Object>> list = new ArrayList<>();
	    LocalDate today = LocalDate.now();
	    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	    for (int i = 4; i <= 10; i++) {
	        Map<String, Object> map = new HashMap<>();

	        LocalDate targetDate = today.plusDays(i);
	        DayOfWeek dayOfWeek = targetDate.getDayOfWeek();

	        map.put("date", targetDate.format(dateFormatter));
	        map.put("dayOfWeek", getKoreanDayOfWeek(dayOfWeek));

	        map.put("minTemp", getField2(taItem, "taMin" + i));
	        map.put("maxTemp", getField2(taItem, "taMax" + i));

	        if (i <= 7) {
	            map.put("amWeather", getField2(landItem, "wf" + i + "Am"));
	            map.put("pmWeather", getField2(landItem, "wf" + i + "Pm"));
	            map.put("amPop", getField2(landItem, "rnSt" + i + "Am"));
	            map.put("pmPop", getField2(landItem, "rnSt" + i + "Pm"));
	        } else {
	            String weather = getField(landItem, "wf" + i);
	            String pop = getField2(landItem, "rnSt" + i).toString();
	            map.put("amWeather", weather);
	            map.put("pmWeather", weather);
	            map.put("amPop", pop);
	            map.put("pmPop", pop);
	        }

	        list.add(map);
	    }

	    return list;
	}
	

	public static List<Map<String, Object>> summarizeMidForecastOne( Map<String, Object> tItem, Map<String, Object> lItem, String selDate) {
		
//      WeatherMidTaResponseXml.Body.Items.Item taItem     = (WeatherMidTaResponseXml.Body.Items.Item) tItem.get("midtaItem");
//      WeatherMidLandResponseXml.Body.Items.Item landItem = (WeatherMidLandResponseXml.Body.Items.Item) lItem.get("midlandItem");
		WeatherMidTaResponseJson.Item taItem     = (WeatherMidTaResponseJson.Item) tItem.get("midtaItem");
        WeatherMidLandResponseJson.Item landItem = (WeatherMidLandResponseJson.Item) lItem.get("midlandItem");

        
	    List<Map<String, Object>> list = new ArrayList<>();
	    LocalDate today = LocalDate.now();
	    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	    DateTimeFormatter dateFormatter2 = DateTimeFormatter.ofPattern("yyyyMMdd");

	    for (int i = 4; i <= 10; i++) {
	        Map<String, Object> map = new HashMap<>();

	        LocalDate targetDate = today.plusDays(i);
	        DayOfWeek dayOfWeek = targetDate.getDayOfWeek();

	        if(selDate.equals(targetDate.format(dateFormatter2))) {
		        map.put("date", targetDate.format(dateFormatter));
		        map.put("dayOfWeek", getKoreanDayOfWeek(dayOfWeek));

		        map.put("minTemp", getField2(taItem, "taMin" + i));
		        map.put("maxTemp", getField2(taItem, "taMax" + i));

		        if (i <= 7) {
		            map.put("amWeather", getField2(landItem, "wf" + i + "Am"));
		            map.put("pmWeather", getField2(landItem, "wf" + i + "Pm"));
		            map.put("amPop", getField2(landItem, "rnSt" + i + "Am"));
		            map.put("pmPop", getField2(landItem, "rnSt" + i + "Pm"));
		        } else {
		            String weather = getField(landItem, "wf" + i);
		            String pop = getField2(landItem, "rnSt" + i).toString();
		            map.put("amWeather", weather);
		            map.put("pmWeather", weather);
		            map.put("amPop", pop);
		            map.put("pmPop", pop);
		        }

		        list.add(map);
	        }
	    }

	    return list;
	}
	

	/**
	 * 중기 예보 데이터를 요약하여 날짜별 날씨 정보를 리스트로 반환합니다.
	 *
	 * @param tItem 기온 정보가 포함된 맵 (key: "taItem", value: WeathenseXml.Body.Items.Item)
	 * @param lItem 육상 예보 정보가 포함된 맵 (key: "landItem", value: WeatherMidLandResponseXml.Body.Items.Item)
	 * @return 날짜, 요일, 기온, 날씨, 강수확률 정보를 포함하는 리스트
	 */
	public static List<Map<String, Object>> summarizeMidForecast( Map<String, Object> tItem, Map<String, Object> lItem ) {
		
	    // Step 1: 입력 맵에서 기온 및 육상 예보 객체 추출
//        WeatherMidTaResponseXml.Body.Items.Item taItem     = (WeatherMidTaResponseXml.Body.Items.Item) tItem.get("midtaItem");
//        WeatherMidLandResponseXml.Body.Items.Item landItem = (WeatherMidLandResponseXml.Body.Items.Item) lItem.get("midlandItem");
        WeatherMidTaResponseJson.Item taItem     = (WeatherMidTaResponseJson.Item) tItem.get("midtaItem");
        WeatherMidLandResponseJson.Item landItem = (WeatherMidLandResponseJson.Item) lItem.get("midlandItem");
                
        
        // 결과를 저장할 리스트
	    List<Map<String, Object>> list = new ArrayList<>();
	    LocalDate today = LocalDate.now();
	    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	    DateTimeFormatter dateFormatter2 = DateTimeFormatter.ofPattern("yyyyMMdd");

	    // Step 2: 4일 후부터 10일 후까지 루프
	    for (int i = 4; i <= 7; i++) {
	        Map<String, Object> map = new HashMap<>();

	        // Step 3: 날짜 및 요일 계산
	        LocalDate targetDate = today.plusDays(i);
	        DayOfWeek dayOfWeek = targetDate.getDayOfWeek();

	        map.put("date", targetDate.format(dateFormatter));
	        map.put("selDate", targetDate.format(dateFormatter2));
	        map.put("dayOfWeek", getKoreanDayOfWeek(dayOfWeek));

	        // Step 4: 기온 정보 설정
	        map.put("minTemp", getField2(taItem, "taMin" + i));
	        map.put("maxTemp", getField2(taItem, "taMax" + i));

	        // Step 5: 오전/오후 날씨 및 강수확률 설정 (7일까지는 AM/PM, 이후는 단일 예보 복제)
            map.put("amWeather", getField2(landItem, "wf" + i + "Am"));
            map.put("pmWeather", getField2(landItem, "wf" + i + "Pm"));
            map.put("amPop", getField2(landItem, "rnSt" + i + "Am"));
            map.put("pmPop", getField2(landItem, "rnSt" + i + "Pm"));

	        // Step 6: 결과 리스트에 추가
	        list.add(map);
	    }

	    return list;
	}
	


	/**
	 * 상단 주요도시 일주일 날씨 정보 조회
	 * @작성일 : 2025. 6. 30
	 * @작성자 : hsg
	 */
//	public Map<String, Object> getMainCitiesOfWeatherForecast(List<WeatherCityDto> weatherCitiesList) {
	public void getMainCitiesOfWeatherForecast(List<WeatherForecastDto> weatherCitiesList) {
		//상단 D+7은 단기와 중기 모두 사용해서 하나로 합쳐서 뿌려줘야 함. 1~3일은 단기, 4~10일은 중기. 단기예보의 형식으로 통합하면 될듯.
		List<Map<String, Object>> weatherList = new ArrayList<>();

		//Map<String, Object> svcMap = new HashMap<>();
		Map<String, Object> msgMap = new HashMap<>();


		/* 2. 오늘 날짜 구하기 */
		String toDate = Converter.dateToStr("yyyyMMdd");

		//String addr = cities[0];
		/* **************** 3. 주요 시도 반복 ******************** */
		for (WeatherForecastDto weatherCity : weatherCitiesList) {
			// 기본정보 설정
			/* **************** 3-1. 주요 시도 좌표(KakaoLocalAPi) 구하기 ******************** */
//			//1~3일. 주소 정보를 카카오 로컬 API를 통해 위/경도 조회 후 격자값으로 변환.
//			//logger.debug("getKakaoLocalApi Start Time : " + Converter.dateToStr("yyyy-MM-dd HH:mm:ss"));
//
//			Map<String, Object> kakaoMap = getKakaoLocalApi(addr);
//			String nx = Converter.toStr(kakaoMap.get("nx"));
//			String ny = Converter.toStr(kakaoMap.get("ny"));
//		
//			//logger.debug("getKakaoLocalApi End Time : " + Converter.dateToStr("yyyy-MM-dd HH:mm:ss"));

			/* **************** 3-2. 단기예보(1-3일) 구하기 ******************** */
			//단기예보 API호출. 날짜 형식은 yyyyMMdd로 해야 됨.
			//logger.debug("getWeatherShortApi Start Time : " + Converter.dateToStr("yyyy-MM-dd HH:mm:ss"));
//			logger.debug("################ " + weatherCity.getCity() + " : "+ weatherCity.getNx() + " / " + weatherCity.getNy());
			msgMap = getWeatherShortApi(toDate, weatherCity.getNx(), weatherCity.getNy()); //1일~4일. (5일 데이터는 의미 없음. 데이터 이상함)
			//logger.debug("getWeatherShortApi End Time : " + Converter.dateToStr("yyyy-MM-dd HH:mm:ss"));

			/*, {date=2025-07-07, pmWeather=구름많음, dayOfWeek=월, maxTemp=32, pmPop=20, amWeather=구름많음, amPop=20, minTemp=25}*/
			List<Map<String, Object>> shortList   = (List<Map<String, Object>>) msgMap.get("shortList");
			int idx = 0;
			for(Map<String, Object> shortWeather : shortList) {
				if(idx > 2)
					break;

				WeatherForecastDto dto = new WeatherForecastDto();
				dto.setCid(weatherCity.getCid());
				dto.setInsertid("admin");
				dto.setUpdateid("admin");

				String weathDate = shortWeather.get("date").toString();
				if(weathDate.length() == 8)
					dto.setWeather_date(weathDate.substring(0,4)+"-"+weathDate.substring(4,6)+"-"+weathDate.substring(6,8));
				else
					dto.setWeather_date(weathDate);

				dto.setDay_of_week(shortWeather.get("dayOfWeek").toString());
				dto.setAm_weather(shortWeather.get("amWeather").toString());
				dto.setPm_weather(shortWeather.get("pmWeather").toString());
				dto.setAm_temp(shortWeather.get("minTemp").toString());
				dto.setPm_temp(shortWeather.get("maxTemp").toString());
				dto.setAm_pop(shortWeather.get("amPop").toString());
				dto.setPm_pop(shortWeather.get("pmPop").toString());

				weatherDao.mergeWeatherForecast(dto);
				idx++;
			}

			/* **************** 3-3. 주소 정보로 중기기온예보 구역코드 구하기 ******************** */
			//4~10일. 주소 정보로 각 예보구역코드 조회
			//logger.debug("getWeatherAreaCd Start Time : " + Converter.dateToStr("yyyy-MM-dd HH:mm:ss"));
			//String midTaAreaCd   = getWeatherAreaCd(weatherCity.getCity(), "ta");   //중기기온예보
			//logger.debug("getWeatherAreaCd End Time : " + Converter.dateToStr("yyyy-MM-dd HH:mm:ss"));
			/* **************** 3-4. 주소 정보로 중기육상예보 구역코드 구하기 ******************** */
			//logger.debug("getWeatherAreaCd Start Time : " + Converter.dateToStr("yyyy-MM-dd HH:mm:ss"));
			//String midLandAreaCd = getWeatherAreaCd(weatherCity.getCity(), "land"); //중기육상예보
			//logger.debug("getWeatherAreaCd End Time : " + Converter.dateToStr("yyyy-MM-dd HH:mm:ss"));


			//예보구역코드로 중기 예보 API호출.
			String tmFc = toDate+"0600"; //중기예보 6시 발표. 0600은 그냥 고정.

			/* **************** 3-5. 중기기온예보(4-10일) 구하기 ******************** */
			//logger.debug("getWeatherMidTaApi Start Time : " + Converter.dateToStr("yyyy-MM-dd HH:mm:ss"));
			Map<String, Object> midTaItem   = getWeatherMidTaApi(tmFc, weatherCity.getMit_cd());
			//logger.debug("getWeatherShortApi End Time : " + Converter.dateToStr("yyyy-MM-dd HH:mm:ss"));
			/* **************** 3-6. 중기육상예보(4-10일) 구하기 ******************** */
			//logger.debug("getWeatherMidLandApi Start Time : " + Converter.dateToStr("yyyy-MM-dd HH:mm:ss"));
			Map<String, Object> midLandItem = getWeatherMidLandApi(tmFc, weatherCity.getMil_cd());
			//logger.debug("getWeatherMidLandApi End Time : " + Converter.dateToStr("yyyy-MM-dd HH:mm:ss"));


		    // Step 1: 입력 맵에서 기온 및 육상 예보 객체 추출
	        WeatherMidTaResponseJson.Item taItem     = (WeatherMidTaResponseJson.Item) midTaItem.get("midtaItem");
	        WeatherMidLandResponseJson.Item landItem = (WeatherMidLandResponseJson.Item) midLandItem.get("midlandItem");
	        
	        
	        // 결과를 저장할 리스트
		    List<Map<String, Object>> list = new ArrayList<>();
		    LocalDate today = LocalDate.now();
		    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

		    // Step 2: 4일 후부터 10일 후까지 루프
		    for (int i = 3; i < 7; i++) {
		        Map<String, Object> map = new HashMap<>();
				WeatherForecastDto dto = new WeatherForecastDto();
				dto.setCid(weatherCity.getCid());
				dto.setInsertid("admin");
				dto.setUpdateid("admin");


		        // Step 3: 날짜 및 요일 계산
		        LocalDate targetDate = today.plusDays(i);
		        DayOfWeek dayOfWeek = targetDate.getDayOfWeek();

		        dto.setWeather_date(targetDate.format(dateFormatter));
				dto.setDay_of_week(getKoreanDayOfWeek(dayOfWeek));
				//logger.debug("####################### targetDate: " + targetDate + ", dayOfWeek: " + dayOfWeek);
				dto.setAm_weather(getField(landItem, "wf" + (i+1) + "Am"));
				dto.setPm_weather(getField(landItem, "wf" + (i+1) + "Pm"));
				dto.setAm_temp(String.valueOf(getField2(taItem, "taMin" + (i+1))));
				dto.setPm_temp(String.valueOf(getField2(taItem, "taMax" + (i+1))));
				dto.setAm_pop(String.valueOf(getField2(landItem, "rnSt" + (i+1) + "Am")));
				dto.setPm_pop(String.valueOf(getField2(landItem, "rnSt" + (i+1) + "Pm")));

		        // Step 6: 결과 리스트에 추가
				weatherDao.mergeWeatherForecast(dto);
		    }


			/* **************** 3-7. 중기기온예보와 중기육상예보 합치기 ******************** */
			//2종의 응답값을 하나로 묶어서 리턴.
			//logger.debug("summarizeMidForecast Start Time : " + Converter.dateToStr("yyyy-MM-dd HH:mm:ss"));
//			List<Map<String, Object>> midList = summarizeMidForecast(midTaItem, midLandItem);  //이게 통합이 될거라 생각했는데 안됨...
			//logger.debug("summarizeMidForecast End Time : " + Converter.dateToStr("yyyy-MM-dd HH:mm:ss"));
			
	
			/* **************** 3-8. 단기 예보와 중기 예보 합치기 ******************** */
//			List<Map<String, Object>> mergeList = new ArrayList<>(shortList.subList(0, shortList.size()- 2));
//			mergeList.addAll(midList);

//			logger.debug("cityWeatherList Start Time : " + Converter.dateToStr("yyyy-MM-dd HH:mm:ss"));
//			Map<String, Object> tmpMap = new HashMap<>();
//			tmpMap.put("city", weatherCity.getCid());
//			tmpMap.put("cityWeatherList", mergeList);
//			weatherList.add(tmpMap);
			//logger.debug("cityWeatherList End Time : " + Converter.dateToStr("yyyy-MM-dd HH:mm:ss"));
		}

//		msgMap.put("weatherList", weatherList);

//		return msgMap;
	}




	/**
	 * 주요도시 좌표 업데이트 및 목록 가져오기
	 * @작성일 : 2025. 7. 1
	 * @작성자 : hsg
	 */
	public List<WeatherForecastDto> setMainCitiesCoords() {

		List<WeatherForecastDto> newWeatherCitiesList = new ArrayList<>();

		try {
			/* **1. DB에서 주요도시 목록을 조회** */
			  /* 도시명, 코드 등으로 추출 */
			  /* (필요하다면 이전 좌표도 함께 조회) */
			List<WeatherForecastDto> weatherCitiesList = new ArrayList<>();
			weatherCitiesList = weatherDao.getWeatherCityList();


			/* **2. 주요도시 목록 반복** */
			  /* 각 도시마다 아래 작업 반복 */		
			for (WeatherForecastDto weatherCity : weatherCitiesList) {
				boolean isChange = false;

			    /* **2-1. 카카오 로컬 API로 좌표 구하기** */
//				Map<String, Object> kakaoMap = getKakaoLocalApi(weatherCity.getCity());
				Map<String, Object> kakaoMap = kakaoLocalApiService.getKakaoLocalApi(weatherCity.getCity());
			    /* 도시명을 주소로 변환 → 위도/경도(x, y)값 추출 */
				String nx = Converter.toStr(kakaoMap.get("nx"));
				String ny = Converter.toStr(kakaoMap.get("ny"));

				/* 응답값의 좌표 정확도, 실패 시 재시도/오류 처리 */
				if(StringUtils.isNotEmpty(nx) && StringUtils.isNotEmpty(ny)) {

					if(!StringUtils.equals(nx, weatherCity.getNx())) {
						weatherCity.setNx(nx);
						isChange = true;
						
					}
					if(!StringUtils.equals(ny, weatherCity.getNy())) {
						weatherCity.setNy(ny);
						isChange = true;
					}


					/* **2-2. DB에 좌표 업데이트** */

				    //* 해당 도시의 기존 레코드를 UPDATE
				    //* 신규 도시는 INSERT (필요시 upsert 적용)
				    //* 트랜잭션 사용(여러 건 처리 시 일괄 커밋/롤백)
					if(isChange) {
						weatherDao.updateCityCoords(weatherCity);
					}

					newWeatherCitiesList.add(weatherCity);
				}

			}

		} catch(Exception e) {
			// 로깅 및 예외 무시
            e.printStackTrace();
		}

		return newWeatherCitiesList;
	}


	public Map<String, Object> getWeatherListByCity() {
	    Map<String, Object> returnMap = new HashMap<>();

	    try {
	        List<WeatherForecastDto> allList = weatherDao.selectWeeklyForecasts(); // DB 조회

	        // city별로 그룹핑
//	        Map<String, List<WeatherForecastDto>> grouped = allList.stream()
//	            .filter(dto -> dto.getCity() != null)
//	            .collect(Collectors.groupingBy(WeatherForecastDto::getCity));

	        Map<String, List<WeatherForecastDto>> grouped = allList.stream()
	        	    .filter(dto -> dto.getCity() != null)
	        	    .collect(Collectors.groupingBy(
	        	        WeatherForecastDto::getCity,
	        	        LinkedHashMap::new,      // 순서 보장!
	        	        Collectors.toList()
	        	    ));

	        List<Map<String, Object>> weatherList = new ArrayList<>();

	        for (Map.Entry<String, List<WeatherForecastDto>> entry : grouped.entrySet()) {
	            Map<String, Object> cityMap = new HashMap<>();
	            cityMap.put("city", entry.getKey());
	            // cityWeatherList는 JS가 바로 쓰는 필드명으로 맞춰줌
	            cityMap.put("cityWeatherList", new ArrayList<>(entry.getValue()));
	            weatherList.add(cityMap);
	        }

	        // JS에서 data.resultCode, data.weatherList 그대로 사용
	        returnMap.put("weatherList", weatherList);
	        returnMap.put("resultCode", "00");
	    } catch (Exception e) {
	        returnMap.put("resultCode", "99");
	        returnMap.put("resultMsg", "Exception : " + e.getMessage());
	    }

	    return returnMap;
	}



	private static String getField(Object obj, String fieldName) {
	    try {
	        Field field = obj.getClass().getDeclaredField(fieldName);
	        field.setAccessible(true);
	        return (String) field.get(obj);
	    } catch (Exception e) {
	        return null;
	    }
	}
	


	private static Object getField2(Object obj, String fieldName) {
	    try {
	        Field field = obj.getClass().getDeclaredField(fieldName);
	        field.setAccessible(true);
	        return field.get(obj);
	    } catch (Exception e) {
	        return null;
	    }
	}
	
	private static String getKoreanDayOfWeek(DayOfWeek dayOfWeek) {
	    switch (dayOfWeek) {
	        case MONDAY:    return "월";
	        case TUESDAY:   return "화";
	        case WEDNESDAY: return "수";
	        case THURSDAY:  return "목";
	        case FRIDAY:    return "금";
	        case SATURDAY:  return "토";
	        case SUNDAY:    return "일";
	        default:        return "";
	    }
	}



}