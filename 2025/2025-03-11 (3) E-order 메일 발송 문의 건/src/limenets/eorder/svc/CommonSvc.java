package com.limenets.eorder.svc;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.google.gson.JsonObject;
import com.limenets.common.dto.LoginDto;
import com.limenets.common.exception.LimeBizException;
import com.limenets.common.exception.MsgCode;
import com.limenets.common.util.Converter;
import com.limenets.common.util.FileDown;
import com.limenets.common.util.FileUpload;
import com.limenets.common.util.HttpUtils;
import com.limenets.eorder.dao.CommonDao;
import com.limenets.eorder.dao.ConfigDao;
import com.limenets.eorder.dao.CustOrderHDao;

/**
 * 공통으로 사용하는 서비스.
 */
@Service
public class CommonSvc {
	private static final Logger logger = LoggerFactory.getLogger(CommonSvc.class);
	
	@Inject private CommonDao commonDao;
	@Inject private ConfigDao configDao;
	@Inject private CustOrderHDao custOrderHDao;
	
	@Inject private CustomerSvc customerSvc;
	
	@Resource(name="fileUpload") private FileUpload fileUpload;
	
	/**
	 * 엑셀 샘플파일 다운로드.
	 * @작성일 : 2020. 3. 23.
	 * @작성자 : kkyu
	 */
	public ModelAndView sampleFileDown(Map<String, Object> params, HttpServletRequest req, Model model, LoginDto loginDto) throws LimeBizException{
		String sepa = System.getProperty("file.separator");
		String folderName = "sample";
		String uploadDir = new StringBuilder(req.getSession().getServletContext().getRealPath("/")).append(sepa).append("data").append(sepa).append(folderName).toString();
		
		Map<String, Object> afMap = new HashMap<>();
		afMap.put("FOLDER_NAME", uploadDir);
		afMap.put("FILE_TYPE", "application/vnd.ms-excel");
		afMap.put("FILE_NAME", HttpUtils.restoreXss(Converter.toStr(params.get("r_filename"))));
		model.addAttribute("afMap", afMap);
		
		return new ModelAndView(new FileDown());
	}
	
	/**
	 * CKEditor 이미지 업로드.
	 * PC용.
	 */
	public Map<String, Object> editorFileUpload(HttpServletRequest req) throws Exception {
		Map<String, Object> resMap = new HashMap<String, Object>();
		
		String sepa = System.getProperty("file.separator");
		String folderName = "editor";
		String uploadDir = new StringBuilder(req.getSession().getServletContext().getRealPath("/")).append(sepa).append("data").append(sepa).append(folderName).toString();
		List<Map<String, Object>> fList = new ArrayList<>();
		
		try {
			if (!MultipartHttpServletRequest.class.isInstance(req)) {
				throw new LimeBizException(MsgCode.FILE_REQUEST_ERROR);
			}

			// 파일 업로드
			MultipartHttpServletRequest mtreq = (MultipartHttpServletRequest)req;
			fList = fileUpload.execManyField(mtreq, "imageFiles", uploadDir, "upload");
			
			resMap.put("editorFileName", fList.get(0).get("saveFileName"));
		} catch (Exception e) {
			fileUpload.deleteList(fList, uploadDir);
			throw e;
		}
		
		resMap.putAll(MsgCode.getResultMap(MsgCode.SUCCESS));
		return resMap;
	}
	
	/**
	 * CKEditor 이미지 업로드.
	 * 모바일용.
	 */
	public Map<String, Object> editorFileUploadForMobile(HttpServletRequest req) throws Exception {
		Map<String, Object> resMap = new HashMap<String, Object>();
		
		String sepa = System.getProperty("file.separator");
		String folderName = "editor";
		String uploadDir = new StringBuilder(req.getSession().getServletContext().getRealPath("/")).append(sepa).append("data").append(sepa).append(folderName).toString();
		List<Map<String, Object>> fList = new ArrayList<>();
		
		try {
			if (!MultipartHttpServletRequest.class.isInstance(req)) {
				throw new LimeBizException(MsgCode.FILE_REQUEST_ERROR);
			}
			
			MultipartHttpServletRequest mtreq = (MultipartHttpServletRequest)req;
			MultipartFile mpf = mtreq.getFile("upload");
			
			String ext = "jpg";
			String fileName = mpf.getOriginalFilename();
			int dotIdx = fileName.lastIndexOf(".");
			if (dotIdx > -1) ext = fileName.substring(dotIdx + 1, fileName.length()).toLowerCase();
			logger.debug("ext : {}", ext);
			//logger.debug("MultipartFile.getOriginalFilename() : {}", mpf.getOriginalFilename());
			
			// EXIF의 Orientation을 확인하여 이미지를 회전 시켜준다.
			// 단, jpg 파일만 가능.
			if(StringUtils.equals("jpg", ext)) {
				// 이미지로부터 byte 생성. 스트림을 사용해도 되지만, 스트림은 한번 사용하면 재사용 되지 않음.
				byte[] imgBytes = mpf.getBytes();
				BufferedInputStream bufferedIS = new BufferedInputStream(new ByteArrayInputStream(imgBytes));
				
				// 이미지 메타정보 중 Orientation 추출. By tika-app-1.6.
				int orientation = this.getOrientation(bufferedIS);
				//logger.debug("orientation : {}", orientation);
				
				// 이미지 회전 및 출력(스트림을 재 생성)
				ByteArrayInputStream byteIS = new ByteArrayInputStream(imgBytes);
				BufferedImage buffredI = this.rotateImageForMobile(byteIS, orientation); // 회전된 이미지의 BuffredImage.
				
				// 파일 업로드.
				fList = fileUpload.execManyField(mtreq, "imageFiles", uploadDir, "upload");
				
				// BuffredImage => FILE로 변환. 
				File upfile = (File) fList.get(0).get("upfile");
				ImageIO.write(buffredI, ext, upfile);
			}
			else {
				// 파일 업로드.
				fList = fileUpload.execManyField(mtreq, "imageFiles", uploadDir, "upload");
			}
			
			resMap.put("editorFileName", fList.get(0).get("saveFileName"));
		} catch (Exception e) {
			fileUpload.deleteList(fList, uploadDir);
			throw e;
		}
		
		resMap.putAll(MsgCode.getResultMap(MsgCode.SUCCESS));
		return resMap;
	}
	

	/**
	 * 이미지 메타정보 중 Orientation 가져오기.
	 */
	public int getOrientation(BufferedInputStream is) throws IOException {
		int orientation = 1;
		try {
			Metadata metadata = ImageMetadataReader.readMetadata(is, true);
			Directory directory = metadata.getDirectory(ExifIFD0Directory.class);
			try {
				orientation = directory.getInt(ExifIFD0Directory.TAG_ORIENTATION);
			} catch (MetadataException me) {
				logger.debug("Could not get orientation");
			} catch (NullPointerException npe) {
				return 1;
			}
		} catch (ImageProcessingException e) {
			e.printStackTrace();
		}
		return orientation;
	}
	
	public BufferedImage rotateImageForMobile(InputStream is,int orientation) throws IOException {
		BufferedImage bi = ImageIO.read(is);
        if(orientation == 6){ //정위치
        	return rotateImage(bi, 90);
        }
        else if(orientation == 1){ // 왼쪽으로 눞였을때
        	return bi;
        }
        else if (orientation == 3){ // 오른쪽으로 눞였을때
        	return rotateImage(bi, 180);
        }
        else if (orientation == 8){ // 180도
        	return rotateImage(bi, 270);      
        }
        else{
        	return bi;
        }       
	}

	public BufferedImage rotateImage(BufferedImage orgImage,int radians) {
		BufferedImage newImage;
		
		if(radians==90 || radians==270){
			newImage = new BufferedImage(orgImage.getHeight(),orgImage.getWidth(),orgImage.getType());
		}
		else if (radians==180){
			newImage = new BufferedImage(orgImage.getWidth(),orgImage.getHeight(),orgImage.getType());
		}
		else{
			return orgImage;
		}

		Graphics2D graphics = (Graphics2D) newImage.getGraphics();
		graphics.rotate(Math. toRadians(radians), newImage.getWidth() / 2, newImage.getHeight() / 2);
		graphics.translate((newImage.getWidth() - orgImage.getWidth()) / 2, (newImage.getHeight() - orgImage.getHeight()) / 2);
		graphics.drawImage(orgImage, 0, 0, orgImage.getWidth(), orgImage.getHeight(), null );

        return newImage;
	}
	
	/**
	 * 휴일 리스트 가져오기.
	 * @작성일 : 2020. 4. 16.
	 * @작성자 : kkyu
	 */
	public List<Map<String, Object>> getHolyDayList(Map<String, Object> svcMap){
		return commonDao.getHolyDayList(svcMap);
	}
	
	/**
	 * 주차 리스트 가져오기.
	 * @작성일 : 2020. 4. 16.
	 * @작성자 : kkyu
	 */
	public List<Map<String, Object>> getOrderWeekList(Map<String, Object> svcMap){
		return commonDao.getOrderWeekList(svcMap);
	}
	
	/**
	 * 프론트 로그인 이후 시점, 공통으로 사용하는 메소드.
	 * 프론트 컨트롤러 모든 폼에 해당 함수 호출해야 함.
	 * 
	 * config 테이블에서 list로 받아와서 필요한 것만 람다식으로 model에 담았지만, 리스트에서 많은 데이터를 필요로 한다면 for문 사용이 나을듯하네...
	 * 
	 * @작성일 : 2020. 4. 6.
	 * @작성자 : kkyu
	 */
	public void getFrontCommonData(Map<String, Object> params, HttpServletRequest req, Model model, LoginDto loginDto) throws LimeBizException{
		Map<String, Object> svcMap = new HashMap<>();
		List<Map<String, Object>> configList = configDao.list(params);
		
		// header >>> 로고 이미지 가져오기. => 필요없네...
		Map<String, Object> config1 = configList.stream().filter(x -> x.get("CF_ID").equals("SYSTEMLOGO")).findFirst().get();
		logger.debug("config logo map : {}", config1);
		model.addAttribute("logo", config1.get("CF_VALUE"));
		
		// header >>> 임시저장(99) 개수 가져오기.
		String today = Converter.dateToStr("yyyy-MM-dd");
		String[] ri_statuscd = {"99"};
		svcMap.put("r_insdate", today);
		svcMap.put("r_inedate", today);
		svcMap.put("r_userid", loginDto.getUserId());
		svcMap.put("ri_statuscd", ri_statuscd);
		model.addAttribute("orderStatus99Cnt", custOrderHDao.cnt(svcMap));
		svcMap.clear();
		
		// bottom >>> 영역 영업사원, CS담당자 정보 가져오기
		Map<String, Object> ctMap = customerSvc.getCustomer(loginDto.getCustCd());
		model.addAttribute("ctMap", ctMap);
		
		// bottom >>> 거래처 가상계좌 번호 가져오기.
		//params.put("r_ayan8", loginDto.getCustCd());
		//model.addAttribute("custVAccount", commonDao.getCustVAcount(params));
	}
	
	/**
	 * 프론트 bottom 영역에 거래처별 입금 가상계좌 번호를 가져오는데, 시간이 너무 오래 걸려서 따로 뺐음.
	 * - 뷰테이블 V_ACCOUNT 수정 필요해 보입니다.
	 * - this.getFrontCommonData에 넣었을때 시간이 너무 지체되서 모든 페이지 로딩 속도가 느림.
	 * @작성일 : 2020. 4. 26.
	 * @작성자 : kkyu
	 */
	/*public Map<String, Object> getCustVAccount(Map<String, Object> params, HttpServletRequest req, LoginDto loginDto) throws LimeBizException{
		Map<String, Object> resMap = new HashMap<>();
		// bottom >>> 거래처 가상계좌 번호 가져오기.
		params.put("r_ayan8", loginDto.getCustCd());
		resMap.put("account", commonDao.getCustVAcount(params));
		resMap.putAll(MsgCode.getResultMap(MsgCode.SUCCESS));
		return resMap;
	}*/
	
	/**
	 * 한진 배송조회
	 * return 최종 위치관제 일시, 위/경도, 차량번호
	 * @작성일 : 2020. 4. 24.
	 * @작성자 : an
	 */
	public String hanjinDelivery2(Map<String, Object> params) throws Exception {
//		System.setProperty ( "java.protocol.handler.pkgs","com.sun.net.ssl.internal.www.protocol");
//		com.sun.net.ssl.internal.ssl.Provider provider = new com.sun.net.ssl.internal.ssl.Provider();
//		Security.addProvider(provider);
		
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			@Override
			public X509Certificate[] getAcceptedIssuers() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
				// TODO Auto-generated method stub
			}
		}};

		/*
		SSLContext sc = SSLContext.getInstance("SSL");
		sc.init(null, trustAllCerts, new SecureRandom());
		HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		 */
		
		String truckNo = Converter.toStr(params.get("r_truckno")); //차량번호
		
		//URL url = new URL("https://tms.tjlogis.co.kr/GtmsRequest");
		//URLConnection urlConn = url.openConnection();
		//HttpURLConnection conn = (HttpURLConnection) urlConn;
//		HttpsURLConnection conn = (HttpsURLConnection) new URL("https://tms.tjlogis.co.kr/GtmsRequest").openConnection();
		HttpURLConnection conn = (HttpURLConnection) new URL("http://219.254.32.104:8898/tms/lbsRequest.jsp").openConnection();
		
		conn.setRequestProperty("key", "BCC002CDDB13C2052C99FFDB2B580ED2");
		conn.setRequestProperty("Accept", "application/json");
		conn.setRequestProperty("Content-Type", "application/json");
		conn.setRequestProperty("cache-control", "no-cache");
		conn.setRequestProperty("cache-length", "length");
		conn.setRequestMethod("POST");
		
		conn.setDoOutput(true);
		conn.setDoInput(true);
		conn.setUseCaches(false);
		conn.setDefaultUseCaches(false);
		
		conn.setReadTimeout(10000);
		conn.setConnectTimeout(15000);
		
		/*
		String responseMessage = conn.getHeaderField(0);
		logger.debug("responseMessage : {}", responseMessage);
		 */
		
		JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("service", "getTruckLocation");
        jsonObject.addProperty("truckNo", truckNo);
        String jsonStr = jsonObject.toString();
        logger.debug("jsonStr : {}", jsonStr);
        
        jsonStr = StringUtils.substringBetween(jsonStr, "{", "}");
        logger.debug("jsonStr : "+ jsonStr);
        
        byte[] inByte = jsonStr.getBytes("utf-8");
		OutputStream out = conn.getOutputStream();
        out.write(inByte);
//		OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
//		out.write(jsonStr);
		
		out.flush();
		out.close();
		
		int responseCode = conn.getResponseCode();
		logger.debug("responseCode : {}", responseCode);
		logger.debug("conn.getErrorStream() : {}", conn.getErrorStream());
		
		String buffer = null;
		String result = "";
		
		logger.debug("HttpURLConnection.HTTP_OK : {}", HttpURLConnection.HTTP_OK);
		if(responseCode == HttpURLConnection.HTTP_OK){
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
			while((buffer = in.readLine()) != null){
				result += buffer;
			}
			in.close();
			logger.debug("result : " + result);
			
		}else {
			logger.debug("error");
		}
		
		return result;
		
	}
	
	/**
	 * 한진 배송조회
	 * return 최종 위치관제 일시, 위/경도, 차량번호
	 * @작성일 : 2020. 4. 24.
	 * @작성자 : an
	 */
	public String hanjinDelivery(Map<String, Object> params) throws Exception {
		
		String truckNo = Converter.toStr(params.get("r_truckno")); //차량번호
		
		/*
		URL url = new URL("http://219.254.32.104:8898/tms/lbsRequest.jsp");
		URLConnection urlConn = url.openConnection();
		HttpURLConnection conn = (HttpURLConnection) urlConn;
		*/
		
		HttpURLConnection conn = (HttpURLConnection) new URL("http://219.254.32.104:8898/tms/lbsRequest.jsp").openConnection();
		
		conn.setRequestProperty("Authorization", "key=BCC002CDDB13C2052C99FFDB2B580ED2");
		conn.setRequestProperty("Accept", "application/json");
		conn.setRequestProperty("Content-Type", "application/json");
		conn.setRequestProperty("cache-control", "no-cache");
		conn.setRequestProperty("cache-length", "length");
		conn.setRequestMethod("POST");
		
		conn.setDoOutput(true);
		conn.setDoInput(true);
		conn.setUseCaches(false);
		conn.setDefaultUseCaches(false);
		
		conn.setReadTimeout(10000);
		conn.setConnectTimeout(15000);
		
		//String responseMessage = conn.getHeaderField(0);
		//logger.debug("responseMessage : {}", responseMessage);
		
		JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("service", "getTruckLocation");
        jsonObject.addProperty("truckNo", truckNo);
        String jsonStr = jsonObject.toString();
        //logger.debug("jsonStr : {}", jsonStr);
        
        //jsonStr = StringUtils.substringBetween(jsonStr, "{", "}");
        //logger.debug("jsonStr : "+ jsonStr);
		
        byte[] inByte = jsonStr.getBytes("utf-8");
        OutputStream out = conn.getOutputStream();
		out.write(inByte);
		out.flush();
		out.close();
		
		int code = conn.getResponseCode();
		logger.debug("code : " + code);
		
		String buffer = null;
		String result = "";
		
		if(code == 200){
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
			while((buffer = in.readLine()) != null){
				result += buffer;
			}
			in.close();
			logger.debug("result : " + result);
			
		}else {
			logger.debug("error");
		}
		
		return result;
		
	}
	
	/**
	 * 동원 배송조회
	 * return 위/경도
	 * @작성일 : 2020. 4. 24.
	 * @작성자 : an
	 */
	public String dongwonDelivery(Map<String, Object> params) throws Exception {
		String truckNo = Converter.toStr(params.get("r_truckno")); //차량번호
		
		URL url = new URL("http://mobilentis.dongwon.com/ncall/mobileGeofence.json");
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		
		conn.setRequestMethod("GET");
		conn.setDoOutput(true);
		
		//String responseMessage = conn.getHeaderField(0);
		//logger.debug("responseMessage : {}", responseMessage);
		
		OutputStream out = conn.getOutputStream();
		
		String body = "vehclNo="+truckNo;
		out.write(body.getBytes("utf-8"));
		out.close();
		
		int code = conn.getResponseCode();
		logger.debug("code : " + code);
		
		String result = "";
		String buffer = null;
		BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		
		while((buffer = in.readLine())!=null){
			result += buffer;
		}
		
		in.close();
		logger.debug(result);
		
		return result;
	}
	
	
}
