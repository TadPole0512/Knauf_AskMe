package com.limenets.eorder.svc;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.limenets.common.dto.LoginDto;
import com.limenets.common.exception.LimeBizException;
import com.limenets.common.exception.MsgCode;
import com.limenets.common.util.Converter;
import com.limenets.common.util.FileUpload;
import com.limenets.common.util.MailUtil;
import com.limenets.eorder.dao.ReportDao;
import com.limenets.eorder.dao.SalesOrderDao;

/**
 * 공통으로 사용하는 서비스.
 */
@Service
public class ReportSvc {
	private static final Logger logger = LoggerFactory.getLogger(ReportSvc.class);
	
	@Inject private ReportDao reportDao;
	@Inject private SalesOrderDao salesOrderDao;
	
	@Inject private TempleteSvc templeteSvc;
	@Inject private ConfigSvc configSvc;
	@Inject private SapRestApiSvc sapApiSvc;
	
	@Resource(name="fileUpload") private FileUpload fileUpload;
	
	@Value("${https.url}") private String httpsUrl;
	@Value("${mail.smtp.url}") private String smtpHost;
	@Value("${mail.smtp.sender.addr}") private String smtpSender;
	@Value("${shop.name}") private String shopName;
	
	
	/**
	 * 거래사실확인서 - 공급자/공급받는자
	 * @작성일 : 2020. 4. 16.
	 * @작성자 : an
	 */
	public Map<String, Object> getVsupplier(Map<String, Object> params, HttpServletRequest req, LoginDto loginDto) throws Exception {
		Map<String, Object> svcMap = new HashMap<>();
		
		svcMap.put("r_custcd", Converter.toStr(params.get("m_custcd")));
		Map<String, Object> supplier = reportDao.vSupplier(svcMap);
		
		return supplier;
	}
	
	/**
	 * 거래사실확인서 - 품목내역
	 * @작성일 : 2020. 4. 16.
	 * @작성자 : an
	 */
	public List<Map<String, Object>> getVclosedSalesOrder(Map<String, Object> params){
		Map<String, Object> svcMap = new HashMap<>();
		String r_custcd = Converter.toStr(params.get("m_custcd")); //거래처코드
		String r_shiptocd = Converter.toStr(params.get("m_shiptocd")); //납품처
		String r_insdate = Converter.toStr(params.get("insdate")).replaceAll("-", ""); //시작일
		String r_inedate = Converter.toStr(params.get("inedate")).replaceAll("-", ""); //종료일
		
		svcMap.put("r_sdan8", r_custcd);       
		svcMap.put("r_ssdaddj", r_insdate);    
		svcMap.put("r_esdaddj", r_inedate);   
		svcMap.put("r_sdshan", r_shiptocd);   
		List<Map<String, Object>> vClosedSalesOrderList = reportDao.vClosedSalesOrder(svcMap);
		
		return vClosedSalesOrderList;
	}
	
	/**
	 * 거래사실확인서 - 전월채권,당월채권,현금수금,어음수금, 당월채권
	 * @작성일 : 2020. 4. 16.
	 * @작성자 : an
	 */
	public Map<String, Object> getvSumPrice(Map<String, Object> params){
		Map<String, Object> svcMap = new HashMap<>();
		String r_custcd = Converter.toStr(params.get("m_custcd"));     //거래처코드
		String r_inedate = Converter.toStr(params.get("r_inedate")).replaceAll("-", "");   //종료일
		
		String year = r_inedate.substring(0, 4);  //년도
		String month = r_inedate.substring(4, 6); //월
		
		svcMap.put("r_z4cfy", year);
		svcMap.put("r_z4pn", month);
		svcMap.put("r_z4an8", r_custcd);
		
		svcMap.put("r_z3cfy", year);     
		svcMap.put("r_z3pn", month); 
		svcMap.put("r_z3an8", r_custcd); 
		
		return reportDao.vSumPrice(svcMap);
	}
	
	/**
	 * 거래사실확인서 - 미도래어음
	 * @작성일 : 2020. 4. 17.
	 * @작성자 : an
	 */
	public long getFailPrice(Map<String, Object> params, HttpServletRequest req, LoginDto loginDto) throws Exception {
		Map<String, Object> svcMap = new HashMap<>();
		
		String r_custcd = Converter.toStr(params.get("m_custcd")); //거래처코드
		String r_insdate = Converter.toStr(params.get("r_insdate")).replaceAll("-", ""); //시작일
		String r_inedate = Converter.toStr(params.get("r_inedate")).replaceAll("-", ""); //종료일
		
		svcMap.put("r_sdate", r_insdate);
		svcMap.put("r_edate", r_inedate);
		svcMap.put("r_custcd", r_custcd);
		long failPrice = reportDao.getFailPrice(svcMap);
		
		return failPrice;
	}
	
	
	/**
	 * 거래사실확인서 - 거래처입금계좌
	 * @작성일 : 2020. 4. 16.
	 * @작성자 : an
	 */
	public Map<String, Object> getAccount(Map<String, Object> params, HttpServletRequest req, LoginDto loginDto) throws Exception {
		Map<String, Object> svcMap = new HashMap<>();
		svcMap.put("r_ayan8", Converter.toStr(params.get("m_custcd"))); //거래처코드
		Map<String, Object> account = reportDao.getAccount(svcMap);
		
		return account;
	}
	
	
	public boolean checkVendorList(String custCd) {
		if(StringUtils.isEmpty(custCd)) {
			return false;
		}
		
		String[] vendorList = {"10177413", "10177447", "10177958", "10177961", "10178196", "10178216", "10183187", "10183199",  
				"10183301", "10185968", "10186893", "10172665", "10177592", "10177805", "10183123", "10183135", "10185644", 
				"10194312", "10177740", "10177794", "10177808", "10177821", "10177847", "10178199", "10183189", "10184100", 
				"10185366", "10186996", "10178264"};
		
		
		for(int i=0; i<vendorList.length; i++ ) {
			if(custCd.compareTo(vendorList[i]) == 0) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * 거래사실확인서 이메일전송
	 * @작성일 : 2020. 4. 20.
	 * @작성자 : an
	 * @param  
	 * @return
	 */
	boolean chkCust = false;
	@SuppressWarnings("unchecked")
	public Map<String, Object> factReportSendMail(Map<String, Object> params, HttpServletRequest req, LoginDto loginDto) throws Exception {
		
		String[] emailArr = Converter.toStr(params.get("v_email")).split(",", -1); //받는사람
		String m_shiptocd = Converter.toStr(params.get("m_shiptocd"));             //납품처코드
		//String m_shiptoNm = Converter.toStr(params.get("v_shiptonm"));
		
		chkCust = checkVendorList(params.get("m_custcd")==null ? "" : params.get("m_custcd").toString());
		
		// 전송기준(1:거래처,2:납품처)
		String m_papertype = "1"; 								   
		if(!StringUtils.equals("", m_shiptocd)) m_papertype = "2";

		String paramString = "{" 
			+ "\"r_custcd\":\"" + params.get("m_custcd") + "\","
			+ "}";
		Map<String, Object> supplier = sapApiSvc.getTransactionApi(paramString, sapApiSvc.API_URL_SUPPLY);
		
		paramString = "{" 
				+ "\"r_sdan8\":\"" + params.get("m_custcd") + "\","
				+ "\"r_ssdaddj\":\"" + params.get("insdate").toString().replaceAll("-", "") + "\","
				+ "\"r_esdaddj\":\"" + params.get("inedate").toString().replaceAll("-", "") + "\","
				+ "\"r_sdshan\":\"" + /*params.get("m_shiptocd") +*/ "\","
				+ "}";
		
		Map<String, Object> closeOrder = sapApiSvc.getTransactionApi(paramString, sapApiSvc.API_URL_CLOSE_ORDER);

		if(StringUtils.equals(m_papertype, "2")) {
			Map<String, Object> pMap = new HashMap<String, Object>();
			pMap.put("m_shiptocd", m_shiptocd);
			pMap.put("m_custcd", params.get("m_custcd"));
			List<Map<String, Object>> shiplist  = salesOrderDao.getShiptoListWithQuoteQt(pMap);
			
			if(MapUtils.isEmpty(closeOrder)) {
				return MsgCode.getResultMap(MsgCode.DATA_NOT_FIND_ERROR, "거래내역");
			} else {
				List<Map<String, Object>> templist = (List<Map<String, Object>>)closeOrder.get("list");
				List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
				for(Map<String, Object> x : templist) {	
					for(Map<String, Object> s : shiplist) {
						/*if( !Objects.isNull(x.get("SDSHAN")) && !Objects.isNull(s.get("SHIPTO_CD")) ) {
							String s1 = x.get("SDSHAN").toString();
							String s2 = s.get("SHIPTO_CD").toString();
							if(StringUtils.equals(s1, s2)) {
								list.add(x);
								break;
							}
						}*/
						
						if( !Objects.isNull(x.get("QUOTE_QT")) && !Objects.isNull(s.get("QUOTE_QT")) ) {
							String s1 = x.get("QUOTE_QT").toString().substring(2).toString();
							String s2 = s.get("QUOTE_QT").toString().trim();
							if(s1.compareTo(s2) == 0) {
								list.add(x);
								break;
							}
						}
					}
				}
				
				
				String qQt = "";
				long lPrev = 0;
				long lCurr = 0;
				String sABALPH = "";
				for(Map<String, Object> m : list) {
					qQt = (m.get("QUOTE_QT")==null) ? "" :  m.get("QUOTE_QT").toString();
					lCurr = (m.get("SDSHAN")==null) ? 0 : (long)m.get("SDSHAN");
					if(lCurr >=  lPrev) {
						sABALPH = m.get("ABALPH").toString();
						lPrev = lCurr;
					}
				}
				
				for(Map<String, Object> m : list) {
					m.replace("ABALPH", sABALPH);
				}
				
				closeOrder.clear();
				closeOrder.put("list", list);
			}
		}
		
		List<Map<String, Object>> salesOrderList = closeOrder==null ? new ArrayList<>() : (List<Map<String, Object>>) closeOrder.get("list");
		if(salesOrderList.isEmpty()) {
			return MsgCode.getResultMap(MsgCode.DATA_NOT_FIND_ERROR, "거래내역"); //거래내역이 존재하지 않습니다.
		}

		Map<Object, List<Map<String, Object>>> groupedData = salesOrderList.stream()
				.collect(Collectors.groupingBy(map -> map.get("ABALPH")));
		Set<Object> setG = groupedData.keySet();
		
		/*
		  Map<GroupKey, List<Map<String, Object>>> groupedData = salesOrderList.stream()
                .collect(Collectors.groupingBy(map -> new GroupKey(map.get("ABALPH"), map.get("SSDOOT"))));
		*/
		
		salesOrderList.clear();
		for(Object s : setG) {
			List<Map<String, Object>> list = groupedData.get(s);
			System.out.println(">>>>>> ADD DATA:" + list.size());
			
			Collections.sort(list, new Comparator<Map<String, Object>>() {
				@Override
				public int compare(Map<String, Object> o1, Map<String, Object> o2) {
					String name1 = Converter.toStr(o1.get("SDADDJ"));
					String name2 = Converter.toStr(o2.get("SDADDJ"));
					int result =  name1.compareTo(name2);
					
					if(result == 0) {
						name1 = Converter.toStr(o1.get("ABALPH"));
						name2 = Converter.toStr(o2.get("ABALPH"));
						result =  name1.compareTo(name2);
					}
					
					return result;
				}
			});

			String shipCd = "";
			for(Map<String, Object> m : list) {
				if(shipCd.isEmpty()) {
					shipCd = m.get("SDSHAN")==null ? "" : m.get("SDSHAN").toString();
				}
				
				String sddcto = Converter.toStr(m.get("SDDCTO"));
				if(StringUtils.equals(m_papertype, "2")) {
					if( "CR".equals(sddcto) || "DR".equals(sddcto) ) {
						continue;
					}
				}
				
				if( !StringUtils.isEmpty(shipCd) ) {
					m.put("SDSHAN", shipCd);
					salesOrderList.add(m);
				}
			}
			
		}
		
		// 납품처갯수
		String preShiptoCd = "";
		String shiptoCd = "";
		for(int a=0; a<salesOrderList.size(); a++) {
			if("".equals(preShiptoCd)) {
				preShiptoCd = Converter.toStr(salesOrderList.get(a).get("SDSHAN")); 
				shiptoCd += preShiptoCd+","+Converter.toStr(salesOrderList.get(a).get("ABALPH"))+"|";
			}
			
			if(!preShiptoCd.equals(Converter.toStr(salesOrderList.get(a).get("SDSHAN")))) {
				preShiptoCd = Converter.toStr(salesOrderList.get(a).get("SDSHAN"));
				shiptoCd += preShiptoCd+","+ Converter.toStr(salesOrderList.get(a).get("ABALPH"))+"|";
			}
		}
		
		String[] shiptoArr = shiptoCd.split("\\|", -1);

		// 거래내역 리스트생성
		List<Map<String, Object>> paperList = new ArrayList<>();
		
		for(int b=0; b<shiptoArr.length; b++) {
			String[] codeArr = shiptoArr[b].split(",", -1);
			String stCd = Converter.toStr(codeArr[0]);
			
			for (Map<String, Object> salesOrder : salesOrderList){
				
				String oaadd = Converter.toStr(salesOrder.get("OAADD1")) + " " + Converter.toStr(salesOrder.get("OAADD2"));
				salesOrder.put("OAADD", oaadd.trim());
				
				String sdshan = Converter.toStr(salesOrder.get("SDSHAN")); //납품처
				String sddcto = Converter.toStr(salesOrder.get("SDDCTO")); //메인타입
				String sdocto = Converter.toStr(salesOrder.get("SDOCTO")); //서브타입
				String sddoco = Converter.toStr(salesOrder.get("SDDOCO")); //메인번호
				String sdoorn = Converter.toStr(salesOrder.get("SDOORN")); //서브번호
				String sddsc1 = Converter.toStr(salesOrder.get("SDDSC1")); //품목명
				
				if(stCd.equals(sdshan)) {
					if(!"CA".equals(sddcto) && !"CO".equals(sddcto) && !"CR".equals(sddcto)) {
						paperList.add(salesOrder);
					
						//내 서브를 찾는다.
						for (Map<String, Object> salesOrder2 : salesOrderList){
							if(sddoco.equals(Converter.toStr(salesOrder2.get("SDOORN"))) && sddsc1.equals(Converter.toStr(salesOrder2.get("SDDSC1")))) {
								if("CA".equals(Converter.toStr(salesOrder2.get("SDDCTO"))) || "CO".equals(Converter.toStr(salesOrder2.get("SDDCTO"))) || "CR".equals(Converter.toStr(salesOrder2.get("SDDCTO")))) {
									if(!paperList.contains(salesOrder2)) {
										paperList.add(salesOrder2);
									}
								}
							}
						}
						
					}else {
						
						if("".equals(sdoorn)) { // 서브번호가 없으면 그냥 Add
							paperList.add(salesOrder);
						
						}else {
							
							// CA/CO/CR 인데 paperList에 없고 salesOrderList에 메인도 없다면 Add.
							int saleCnt = 0;
							for (Map<String, Object> salesOrder3 : salesOrderList){
								String d_sdshan = Converter.toStr(salesOrder3.get("SDSHAN")); //납품처
								String d_sddcto = Converter.toStr(salesOrder3.get("SDDCTO")); //메인타입
								String d_sddoco = Converter.toStr(salesOrder3.get("SDDOCO")); //메인번호
								String d_sddsc1 = Converter.toStr(salesOrder3.get("SDDSC1")); //품목명
								
								if(sdshan.equals(d_sdshan) && sdocto.equals(d_sddcto) && sdoorn.equals(d_sddoco) && sddsc1.equals(d_sddsc1)) { //리스트에 나의 메인이 있다면
									saleCnt++;
								}
							}
							
							if(!paperList.contains(salesOrder) && saleCnt == 0) {
								paperList.add(salesOrder);
							}
						}
					}
				}
			}
		}

		Map<List<Object>, Map<String, Object>> paperResultMap = paperList.stream()
	            .collect(Collectors.groupingBy(
	                m -> Arrays.asList(m.get("SDADDJ"), m.get("SDDCTO"), m.get("SDDOCO"), m.get("SDDSC1")),
	                Collectors.collectingAndThen(
	                    Collectors.toList(),
	                    group -> {
	                        int sumORG = group.stream().mapToInt(m -> (int) m.get("SDUORG")).sum();
	                        int sumEXP = group.stream().mapToInt(m -> (int) m.get("SDAEXP")).sum();
	                        int price = (sumORG==0) ? 0 :  (int)(sumEXP / sumORG);
	                       
	                        Map<String, Object> resultMap = new HashMap<>();
	                        resultMap.putAll(group.get(0));
	                        resultMap.replace("SDUORG", sumORG);
	                        resultMap.replace("SDAEXP", sumEXP);
	                        resultMap.replace("SDUPRC", price);

	                        String sSddcto = resultMap.get("SDDCTO")==null ? "" : resultMap.get("SDDCTO").toString();
	                        //if(checkVendorList(resultMap.get("CUST_CD").toString())) {
	                        if(chkCust) {
		                        if( (sSddcto.trim().compareTo("CR")==0) || (sSddcto.trim().compareTo("DR")==0) ) {
		                        	resultMap.replace("SDUPRC", 0);
		                        }
	                        }

	                        return resultMap;
	                    }
	                )
	            ));
	
		
		paperList.clear();
		paperResultMap.forEach((key, value) -> {
			try {
				if( (int)value.get("SDUORG") != 0) {
					paperList.add(value);
				}
			} catch(Exception e) {}
		});
		
		Collections.sort(paperList, new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> m1, Map<String, Object> m2) {
                int result;

                result = (Converter.toStr(m1.get("SDADDJ"))).compareTo(Converter.toStr(m2.get("SDADDJ")));
                if (result != 0) return result;

                result = (Converter.toStr(m1.get("SDDCTO"))).compareTo(Converter.toStr(m2.get("SDDCTO")));
                if (result != 0) return result;

                result = (Converter.toStr(m1.get("SDDOCO"))).compareTo(Converter.toStr( m2.get("SDDOCO")));
                if (result != 0) return result;

                return (Converter.toStr(m1.get("SDDSC1"))).compareTo(Converter.toStr(m2.get("SDDSC1")));
            }
        });

		
		//검산
		for(Map<String, Object> m : paperList) {
			String type = Converter.toStr(m.get("SDDCTO"));
			String exp = Converter.getSapNumber(type, Converter.toStr(m.get("SDAEXP")));
			String org = Converter.getSapQty(type, Converter.toStr(m.get("SDUORG")));
			m.put("SDAEXP", Long.parseLong(exp));
			m.put("SDUORG", Long.parseLong(org));
			System.out.println(m);
		}
		
		// 전월채권,당월채권,현금수금,어음수금, 당월채권
		String r_inedate = Converter.toStr(params.get("r_inedate")).replaceAll("-", "");   //종료일
		paramString = "{" 
				+ "\"r_z4cfy\":\"" + r_inedate.substring(0, 4) + "\","
				+ "\"r_z4pn\":\"" + r_inedate.substring(4, 6) + "\","
				+ "\"r_z4an8\":\"" + params.get("m_custcd") + "\","
				+ "\"r_z3cfy\":\"" + r_inedate.substring(0, 4) + "\","
				+ "\"r_z3pn\":\"" + r_inedate.substring(4, 6) + "\","
				+ "\"r_z3an8\":\"" + params.get("m_custcd") + "\","
				+ "}";
		Map<String, Object> sumPrice = sapApiSvc.getTransactionApi(paramString, sapApiSvc.API_URL_SUM_PRICE);
		
		// 미도래어음
		paramString = "{" 
				+ "\"r_sdate\":\"" + params.get("r_insdate").toString().replaceAll("-", "") + "\","
				+ "\"r_edate\":\"" + params.get("r_inedate").toString().replaceAll("-", "") + "\","
				+ "\"r_custcd\":\"" + params.get("m_custcd") + "\","
				+ "}";
		Map<String, Object> failPriceMap = sapApiSvc.getTransactionApi(paramString, sapApiSvc.API_URL_FAIL_PRICE);
	
		long failPrice = 0L;
		if( (StringUtils.equals("1", m_papertype)) && (failPriceMap != null) ) {
			failPrice = Double.valueOf(failPriceMap.get("RYAA").toString().trim()).longValue();
		}		

		// 거래처 입금계좌
		paramString = "{" 
				+ "\"r_ayan8\":\"" + params.get("m_custcd") + "\","
				+ "}";
		Map<String, Object> vAccount = sapApiSvc.getTransactionApi(paramString, sapApiSvc.API_URL_ACCOUNT);
		if( (vAccount != null) && (vAccount.get("AYCBNK") != null) ) {
			String aycbnk = vAccount.get("AYCBNK").toString();
			if(aycbnk.length() == 13) {
				String bAccount = aycbnk.substring(0, 3) + "-"
						+ aycbnk.substring(3, 8) + "-"
						+ aycbnk.substring(8, 11) + "-"
						+ aycbnk.substring(11, aycbnk.length());
				vAccount.put("AYCBNK", bAccount);
			}
		}
		
		// 대표자 직인이미지
		String ceosealImg = Converter.toStr(configSvc.getConfigValue("CEOSEAL"));
		
		// 메일보내기
		String title = "["+shopName+"]거래사실확인서";
		String url = httpsUrl + req.getServerName() + (req.getServerPort() == 80 ? "" : ":" + Converter.toStr(req.getServerPort())) +  req.getContextPath();
		
		String contentStr = templeteSvc.factReportEmail(supplier, shiptoArr, paperList, sumPrice, failPrice, vAccount, Converter.toStr(params.get("insdate")), Converter.toStr(params.get("inedate")), url, ceosealImg, m_papertype);
		
		SimpleDateFormat sdf = new SimpleDateFormat("_yyyy_MM_dd");
        Calendar c1 = Calendar.getInstance();
		// 파일생성
		String filename = "거래사실확인서" + sdf.format(c1.getTime()) + ".html";
		
		File file = new File(filename);
//		FileWriter fw = new FileWriter(file, false);
//		String resultStr = new String(contentStr.getBytes("EUC-KR"), "KSC5601");
//		
//		fw.write(resultStr);
//		fw.flush();
//		fw.close();
		
//		System.out.println(contentStr);
		try {
			file.createNewFile();
			BufferedWriter output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file.getPath()), StandardCharsets.UTF_8));
			output.write(contentStr);
			output.close();
		} catch(UnsupportedEncodingException uee) {
		} catch(IOException ioe) {
		} 
		
		MailUtil mail = new MailUtil();
		for(String email : emailArr) {
			if(!StringUtils.equals("", email)) {
				// 메일전송
				mail.sendMail(smtpHost, title, "", email, shopName, smtpSender, contentStr, file, filename);
				
				// 전송이력 저장
				this.insertSendMailHistory("factReport", Converter.toStr(params.get("insdate")), Converter.toStr(params.get("inedate"))
						, Converter.toStr(params.get("m_custcd")), Converter.toStr(params.get("m_shiptocd")), Converter.toStr(params.get("r_smhtype")), email, loginDto.getUserId());
			}
		}
		
		return MsgCode.getResultMap(MsgCode.SUCCESS);
	}
	
	
	/**
	 * 메일전송이력 저장
	 * @작성일 : 2020. 4. 20.
	 * @작성자 : an
	 */
	public int insertSendMailHistory(String m_smhreport, String m_smhsdate, String m_smhedate, String m_smhcustcd, String m_smhshiptocd, String m_smhtype, String m_smhemail, String m_smhid){
		Map<String, Object> svcMap = new HashMap<String, Object>();
		
		//전송기준
		if(StringUtils.equals("", m_smhshiptocd)) svcMap.put("m_smhtype", "1"); 
		else svcMap.put("m_smhtype", "2");
		
		svcMap.put("m_smhreport", m_smhreport);
		svcMap.put("m_smhsdate", m_smhsdate);
		svcMap.put("m_smhedate", m_smhedate);
		svcMap.put("m_smhcustcd", m_smhcustcd);
		svcMap.put("m_smhshiptocd", m_smhshiptocd);
		svcMap.put("m_smhemail", m_smhemail);
		svcMap.put("m_smhid", m_smhid);
		return reportDao.inSendMailHistory(svcMap);
	}
	
	public List<Map<String, Object>> sendMailHistoryList(String r_smhid){
		Map<String, Object> svcMap = new HashMap<String, Object>();
		svcMap.put("r_smhid", r_smhid);
		return reportDao.sendMailHistoryList(svcMap);
	}
	
	/**
	 * 자재납품 확인서 폼 > 출력유형별 리스트 만들기.
	 * @작성일 : 2020. 6. 5.
	 * @작성자 : kkyu
	 * @param String paper_type : 10=개별아이템-현장, 20=개별아이템-착지, 11=아이템별-현장, 21=아이템별-현장
	 * @param String r_actualshipsdt [공통] 출고일자 검색 시작일.
	 * @param String r_actualshipedt [공통] 출고일자 검색 종료일.
	 * @param String r_custcd [공통] 거래처 코드.
	 * @param String r_shiptocd  [10,11] 현장(납품처) 코드.
	 * @param String[] ri_add1 [20,21] 주소지명 Array.
	 * @param String[] ri_itemdesc [공통] 품목명 Array.
	 */
	public void getDeliveryPaper2(Map<String, Object> params, HttpServletRequest req, LoginDto loginDto, Model model) throws Exception {
		Map<String, Object> svcMap = new HashMap<>();
		
		String paper_type = Converter.toStr(params.get("paper_type"));
		String r_actualshipsdt = Converter.toStr(params.get("r_actualshipsdt"));
		String r_actualshipedt = Converter.toStr(params.get("r_actualshipedt"));
		String r_custcd = Converter.toStr(params.get("r_custcd"));
//		String r_shiptocd = Converter.toStr(params.get("r_shiptocd"));
		String r_shiptonm = Converter.toStr(params.get("r_shiptonm"));
		String r_hebechk = Converter.toStr(params.get("r_hebechk"));
		String[] ri_add1 = req.getParameterValues("ri_add1");
		String[] ri_itemdesc = req.getParameterValues("ri_itemdesc");
		
		logger.debug("paper_type : {}", paper_type);
		
		svcMap.put("r_actualshipsdt", r_actualshipsdt.replaceAll("-", "").trim());
		svcMap.put("r_actualshipedt", r_actualshipedt.replaceAll("-", "").trim());
		svcMap.put("r_custcd", r_custcd);
		svcMap.put("ri_itemdesc", ri_itemdesc);
		svcMap.put("r_hebechk", r_hebechk);
		
		// 대표자 직인 가져오기.
		String ceosealImg = Converter.toStr(configSvc.getConfigValue("CEOSEAL"));
		model.addAttribute("ceosealImg", ceosealImg);
		
		// 오늘 날짜.
		model.addAttribute("todayDate", Converter.dateToStr("yyyy년 M월 d일"));
		
		int pageMaxTrCount = 20; // 한 페이지당 보여질 tr 최고 개수.
		int addrMaxlength = 58;
		int hebeMaxLength = 7;
		
		/******************************************
		 * 10 = 개별아이템-현장
		 * 20 = 개별아이템-착지
		 ******************************************/
		if(StringUtils.equals("10", paper_type) || StringUtils.equals("20", paper_type)) {
			if(StringUtils.equals("10", paper_type)) {
//				svcMap.put("r_shiptocd", r_shiptocd);
				svcMap.put("r_shiptonm", r_shiptonm);
			}
			else if(StringUtils.equals("20", paper_type)) {
				svcMap.put("ri_add1", ri_add1);
			}
			
			int totCnt = salesOrderDao.getCntReportFor1020(svcMap); // 리스트 전제 개수.
			int totPageCnt = totCnt / pageMaxTrCount + 1; // 페이지 전체 개수.
			logger.debug("### {}. 리스트 전제 개수(totCnt) : {}", paper_type, totCnt);
			logger.debug("### {}. 페이지 전체 개수(totPageCnt) : {}", paper_type, totPageCnt);

			svcMap.put("r_orderby", "SUB.ACTUAL_SHIP_DT ASC, SUB.ITEM_DESC ASC ");
			List<Map<String, Object>> totList = salesOrderDao.getReportFor1020(svcMap);
			
			List<List<Map<String, Object>>> deliveryPaperList = new ArrayList<List<Map<String, Object>>>();
				
			totPageCnt = 0;
			int pageCnt = 0;
			List<Map<String, Object>> mapList = null;
			for (Map<String, Object> m : totList ) {
				if(pageCnt == 0) mapList = new ArrayList<Map<String, Object>>();
				
				mapList.add(m);
				String val = (m.get("ADD1")==null) ? "" : m.get("ADD1").toString();
				String qty = (m.get("PRIMARY_QTY")==null) ? "" : m.get("PRIMARY_QTY").toString();
				
				int valLen = val.getBytes().length;
				int qtyLen = qty.getBytes().length;

				pageCnt++;
				if( (valLen>addrMaxlength) || (qtyLen > hebeMaxLength) ) pageCnt++;
				
				if(pageCnt >= (pageMaxTrCount-1)) {
					pageCnt = 0;
					deliveryPaperList.add(mapList);
					totPageCnt++;
				}
			}
			
			if( (pageCnt > 0) && (mapList.size() > 0) ) {
				deliveryPaperList.add(mapList);
				totPageCnt++;
			}
			
			//List<List<Map<String, Object>>> deliveryPaperList = Lists.partition(totList, pageMaxTrCount);

			logger.debug("### {}. deliveryPaperList : {}", paper_type, deliveryPaperList);
			
			model.addAttribute("totPageCnt", totPageCnt);
			model.addAttribute("deliveryPaperList", deliveryPaperList);
			
			// 출고일자 시작일과 종료일 구하기.
			/*
			String startDate = Converter.toStr(deliveryPaperList.get(0).get(0).get("ACTUAL_SHIP_DT"));
			
			List<Map<String, Object>> lastList = deliveryPaperList.get(deliveryPaperList.size()-1);
			Map<String, Object> lastMap = lastList.get(lastList.size()-1);
			String endDate = Converter.toStr(lastMap.get("ACTUAL_SHIP_DT"));;
			
			model.addAttribute("startDate", startDate);
			model.addAttribute("endDate", endDate);
			*/
			Map<String, Object> periodDate = salesOrderDao.getReportPeriodDate(svcMap);
			String startDate = Converter.toStr(periodDate.get("START_DATE"));
			String endDate = Converter.toStr(periodDate.get("END_DATE"));
			logger.debug("### 출고일자 시작일 : {}, 종료일 : {}", startDate, endDate);
			model.addAttribute("startDate", startDate);
			model.addAttribute("endDate", endDate);
			
			// 거래처명 및 현장명 가져오기.
			Map<String, Object> custInfo = salesOrderDao.getCustInfoForReport(svcMap);
			model.addAttribute("custInfo", custInfo);
		}
		
		/******************************************
		 * 11 = 아이템별-현장
		 * 21 = 아이템별-착지
		 ******************************************/
		else if(StringUtils.equals("11", paper_type) || StringUtils.equals("21", paper_type)) {
			if(StringUtils.equals("11", paper_type)) {
//				svcMap.put("r_shiptocd", r_shiptocd);
				svcMap.put("r_shiptonm", r_shiptonm);
			}
			else if(StringUtils.equals("21", paper_type)) {
				svcMap.put("ri_add1", ri_add1);
			}
			
			svcMap.put("r_orderby", "SUB.ITEM_DESC ASC, SUB.ACTUAL_SHIP_DT ASC ");
			List<Map<String, Object>> totList = salesOrderDao.getReportFor1121(svcMap);
			
			/*List<List<Map<String, Object>>> deliveryPaperList = Lists.partition(totList, pageMaxTrCount);
			logger.debug("### {}. deliveryPaperList : {}", paper_type, deliveryPaperList);
			
			int totCnt = totList.size(); // 리스트 전제 개수.
			int totPageCnt = totCnt / pageMaxTrCount + 1; // 페이지 전체 개수.
			logger.debug("### {}. 리스트 전제 개수(totCnt) : {}", paper_type, totCnt);
			logger.debug("### {}. 페이지 전체 개수(totPageCnt) : {}", paper_type, totPageCnt);
			*/
			
			List<List<Map<String, Object>>> deliveryPaperList = new ArrayList<List<Map<String, Object>>>();
			int totPageCnt = 0;
			int pageCnt = 0;
			String unit = "";
			List<Map<String, Object>> mapList = null;
			for (Map<String, Object> m : totList ) {		
				if(pageCnt == 0) mapList = new ArrayList<Map<String, Object>>();
				
				String dt = m.get("ACTUAL_SHIP_DT").toString();
				if(dt.contains("소계")) {
					m.put("UNIT", unit);
				}
				unit = (m.get("UNIT")==null) ? "" :m.get("UNIT").toString();
				
				mapList.add(m);
				String val = (m.get("ADD1")==null) ? "" : m.get("ADD1").toString();
				String qty = (m.get("PRIMARY_QTY")==null) ? "" : m.get("PRIMARY_QTY").toString();
				
				
				int valLen = val.getBytes().length;
				int qtyLen = qty.getBytes().length;
				
				pageCnt++;
				if( (valLen>addrMaxlength) || (qtyLen > hebeMaxLength) ) pageCnt++;
				
				if(pageCnt >= (pageMaxTrCount-1)) {
					pageCnt = 0;
					deliveryPaperList.add(mapList);
					totPageCnt++;
				}
			}
			
			if( (pageCnt > 0) && (mapList.size() > 0) ) {
				deliveryPaperList.add(mapList);
				totPageCnt++;
			}
			

			model.addAttribute("totPageCnt", totPageCnt);
			model.addAttribute("deliveryPaperList", deliveryPaperList);
			
			// 품목명 rowspan을 위한 리스트 가공.
			List<Map<String, Integer>> rowSpanList = new ArrayList<>();
			for(List<Map<String, Object>> list : deliveryPaperList) {			
				Map<String, Integer> rowSpan = new LinkedHashMap<>();
				for(int i=0,j=list.size(); i<j; i++) {
					if(rowSpan.containsKey(Converter.toStr(list.get(i).get("ITEM_DESC")))) { // hashmap 내부에 이미 key값이 존재 하는지 체크.
						rowSpan.put(Converter.toStr(list.get(i).get("ITEM_DESC")), rowSpan.get(list.get(i).get("ITEM_DESC"))+1); // key 값이 있다면 value에 +1.
					}
					else { //key값이 존재 하지 않으면.
						rowSpan.put(Converter.toStr(list.get(i).get("ITEM_DESC")), 1);
					}
				}
				rowSpanList.add(rowSpan);
			}
			logger.debug("### {}. 품목명 rowspan을 위한 리스트(rowSpanList) : {}", paper_type, rowSpanList);
			Gson gson = new Gson();
			model.addAttribute("rowSpanListToJson", gson.toJson(rowSpanList));
			
			// 출고일자 시작일과 종료일 구하기.
			Map<String, Object> periodDate = salesOrderDao.getReportPeriodDate(svcMap);
			String startDate = Converter.toStr(periodDate.get("START_DATE"));
			String endDate = Converter.toStr(periodDate.get("END_DATE"));
			logger.debug("### 출고일자 시작일 : {}, 종료일 : {}", startDate, endDate);
			model.addAttribute("startDate", startDate);
			model.addAttribute("endDate", endDate);
			
			// 거래처명 및 현장명 가져오기.
			Map<String, Object> custInfo = salesOrderDao.getCustInfoForReport(svcMap);
			model.addAttribute("custInfo", custInfo);
		}
	      /******************************************
         * 12 = 아이템별총계-현장
         * 22 = 아이템별총계-착지
         ******************************************/
        else if(StringUtils.equals("12", paper_type) || StringUtils.equals("22", paper_type)) {
            if(StringUtils.equals("12", paper_type)) {
//                svcMap.put("r_shiptocd", r_shiptocd);
            	svcMap.put("r_shiptonm", r_shiptonm);
            }
            else if(StringUtils.equals("22", paper_type)) {
                svcMap.put("ri_add1", ri_add1);
            }
            
            svcMap.put("r_orderby", "SUB.ITEM_DESC ASC ");
            List<Map<String, Object>> totList = salesOrderDao.getReportFor1222(svcMap);
            
            List<List<Map<String, Object>>> deliveryPaperList = Lists.partition(totList, pageMaxTrCount);
            logger.debug("### {}. deliveryPaperList : {}", paper_type, deliveryPaperList);
            
            int totCnt = totList.size(); // 리스트 전제 개수.
            int totPageCnt = totCnt / pageMaxTrCount + 1; // 페이지 전체 개수.
            logger.debug("### {}. 리스트 전제 개수(totCnt) : {}", paper_type, totCnt);
            logger.debug("### {}. 페이지 전체 개수(totPageCnt) : {}", paper_type, totPageCnt);

            model.addAttribute("totPageCnt", totPageCnt);
            model.addAttribute("deliveryPaperList", deliveryPaperList);
            
            Map<String, Object> periodDate = salesOrderDao.getReportPeriodDate(svcMap);
            String startDate = Converter.toStr(periodDate.get("START_DATE"));
            String endDate = Converter.toStr(periodDate.get("END_DATE"));
            logger.debug("### 출고일자 시작일 : {}, 종료일 : {}", startDate, endDate);
            model.addAttribute("startDate", startDate);
            model.addAttribute("endDate", endDate);
            
            // 거래처명 및 현장명 가져오기.
            Map<String, Object> custInfo = salesOrderDao.getCustInfoForReport(svcMap);
            
            if(totList!=null && totList.size() > 0) {
                custInfo.put("SHIPTO_NM", totList.get(0).get("SHIPTO_NM"));
            }
            model.addAttribute("custInfo", custInfo);
        }
		
	}
	
	/**
	 * XXXXXXXX 사용안함. XXXXXXXX
	 * 자재납품 확인서 폼 > 출력유형별 리스트 만들기.
	 * @작성일 : 2020. 5. 7.
	 * @작성자 : kkyu
	 * @param ri_ordernos : O_SALESORDER.ORDERNO ,로 구분.
	 * @param ri_ordertys : O_SALESORDER.ORDERTY ,로 구분.
	 * @param ri_linenos : O_SALESORDER.LINE_NO ,로 구분.
	 * @param paper_type : 10=개별아이템-현장, 20=개별아이템-착지, 11=아이템별-현장, 21=아이템별-현장
	 */
//	public void getDeliveryPaper(Map<String, Object> params, HttpServletRequest req, LoginDto loginDto, Model model) throws Exception {
//		String ri_ordernos = Converter.toStr(params.get("ri_ordernos"));
//		String ri_ordertys = Converter.toStr(params.get("ri_ordertys"));
//		String ri_linenos = Converter.toStr(params.get("ri_linenos"));
//		String paper_type = Converter.toStr(params.get("paper_type"));
//		logger.debug("ri_ordernos : {}", ri_ordernos);
//		logger.debug("ri_ordertys : {}", ri_ordertys);
//		logger.debug("ri_linenos : {}", ri_linenos);
//		logger.debug("paper_type : {}", paper_type);
//		
//		if(StringUtils.equals("", ri_ordernos) || StringUtils.equals("", ri_ordertys) || StringUtils.equals("", ri_linenos) || StringUtils.equals("", paper_type)) throw new LimeBizException(MsgCode.DATA_REQUIRE_ERROR2);
//		
//		// 대표자 직인 가져오기.
//		String ceosealImg = Converter.toStr(configSvc.getConfigValue("CEOSEAL"));
//		model.addAttribute("ceosealImg", ceosealImg);
//		
//		// 오늘 날짜.
//		model.addAttribute("todayDate", Converter.dateToStr("yyyy년 M월 d일"));
//		
//		// 
//		Map<String, Object> svcMap = new HashMap<>();
//		String[] ri_orderno = ri_ordernos.split(",", -1);
//		String[] ri_orderty = ri_ordertys.split(",", -1);
//		String[] ri_lineno = ri_linenos.split(",", -1);
//		String r_wherebody = "";
//		for(int i=0,j=ri_orderno.length; i<j; i++) {
//			if(0==i) {
//				r_wherebody = "( ( ORDERNO = '"+ri_orderno[i]+"' AND ORDERTY = '"+ri_orderty[i]+"' AND LINE_NO = '"+ri_lineno[i]+"' )";
//			}else {
//				r_wherebody += " OR ( ORDERNO = '"+ri_orderno[i]+"' AND ORDERTY = '"+ri_orderty[i]+"' AND LINE_NO = '"+ri_lineno[i]+"' )";
//			}
//			
//			if(i==j-1) {
//				r_wherebody += ") ";
//			}
//		}
//		
//		svcMap.put("r_wherebody", r_wherebody);
//		logger.debug("r_wherebody : {}", r_wherebody);
//
//		int pageMaxCount = 20; // 한 페이지당 보여질 tr 최고 개수.
//		List<Map<String, Object>> groupList = null;
//		
//		/******************************************
//		 * 10 = 개별아이템-현장
//		 ******************************************/
//		if(StringUtils.equals("10", paper_type)) {
//			// 선택한 건들의 현장(납품처) Group By 리스트 가져오기.
//			groupList = salesOrderDao.getOrderShipGroup(svcMap);
//			
//			// 현장(납품처)별 리스트 가져오기.
//			List<Map<String, Object>> deliveryPaperList = new ArrayList<>();
//			int pageCount = 0;
//			svcMap.put("r_orderby", "ACTUAL_SHIP_DT ASC, ITEM_DESC ASC "); // 실제 출하일 ASC, 품목명 ASC
//			for(int i=0,j=groupList.size(); i<j; i++) {
//				Map<String, Object> deliveryPaper = new HashMap<>();
//				svcMap.put("r_shiptocd", groupList.get(i).get("SHIPTO_CD"));
//				
//				
//				List<Map<String, Object>> pageList = salesOrderDao.getDeliveryPaperList(svcMap);
//				
//				int minDt = 30000000, maxDt = 0; // 출고일시 시작일,종료일
//				for(int x=0,y=pageList.size(); x<y; x++) {
//					int compareDt = 0;
//					if(0==x) {
//						minDt = Converter.toInt(pageList.get(x).get("ACTUAL_SHIP_DT").toString().trim()); 
//						maxDt = minDt; 
//					}
//					else if(x==y-1) {
//						compareDt = Converter.toInt(pageList.get(x).get("ACTUAL_SHIP_DT").toString().trim());
//						if(minDt > compareDt) minDt = compareDt;
//						if(maxDt < compareDt) maxDt = compareDt;
//					}
//					else {
//						compareDt = Converter.toInt(pageList.get(x-1).get("ACTUAL_SHIP_DT").toString().trim());
//						if(minDt > compareDt) minDt = compareDt;
//						if(maxDt < compareDt) maxDt = compareDt;
//					}
//				}
//				String minDtStr = Converter.toStr(minDt);
//				String maxDtStr = Converter.toStr(maxDt);
//				deliveryPaper.put("minDt", minDtStr.substring(0, 4)+"-"+minDtStr.substring(4, 6)+"-"+minDtStr.substring(6, 8));
//				deliveryPaper.put("maxDt", maxDtStr.substring(0, 4)+"-"+maxDtStr.substring(4, 6)+"-"+maxDtStr.substring(6, 8));
//				
//				if(pageMaxCount < pageList.size()) { // tr이 23개를 넘으면 pageCount를 증가시켜준다.
//					deliveryPaper.put("deliveryPaper", pageList.subList(0, pageMaxCount));
//					//deliveryPaper.put("deliveryPaper"+pageCount, pageList.subList(0, pageMaxCount));
//					deliveryPaperList.add(deliveryPaper);
//					pageCount++;
//					
//					deliveryPaper.put("deliveryPaper", pageList.subList(pageMaxCount+1, pageList.size()));
//					//deliveryPaper.put("deliveryPaper"+pageCount, pageList.subList(pageMaxCount+1, pageList.size()));
//					deliveryPaperList.add(deliveryPaper);
//				}
//				else {
//					deliveryPaper.put("deliveryPaper", pageList);
//					//deliveryPaper.put("deliveryPaper"+pageCount, pageList);
//					deliveryPaperList.add(deliveryPaper);
//				}
//				
//				pageCount++;
//			}
//			logger.debug("deliveryPaperList : {}", deliveryPaperList);
//			logger.debug("deliveryPaperList Size : {}", deliveryPaperList.size());
//			logger.debug("pageCount : {}", pageCount);
//			
//			model.addAttribute("pageCount", pageCount);
//			model.addAttribute("deliveryPaperList", deliveryPaperList);
//			
//		}
//		/******************************************
//		 * 20 = 개별아이템-착지
//		 ******************************************/
//		else if(StringUtils.equals("20", paper_type)) {
//			// 착지(배송지)별 리스트 가져오기.
//			List<Map<String, Object>> deliveryPaperList = new ArrayList<>();
//			int pageCount = 0;
//			svcMap.put("r_orderby", "ACTUAL_SHIP_DT ASC, ITEM_DESC ASC "); // 실제 출하일 ASC, 품목명 ASC
//			
//			List<Map<String, Object>> pageList = salesOrderDao.getDeliveryPaperList(svcMap);
//			
//			int minDt = 30000000, maxDt = 0; // 출고일시 시작일,종료일
//			for(int x=0,y=pageList.size(); x<y; x++) {
//				int compareDt = 0;
//				if(0==x) {
//					minDt = Converter.toInt(pageList.get(x).get("ACTUAL_SHIP_DT").toString().trim()); 
//					maxDt = minDt; 
//				}
//				else if(x==y-1) {
//					compareDt = Converter.toInt(pageList.get(x).get("ACTUAL_SHIP_DT").toString().trim());
//					if(minDt > compareDt) minDt = compareDt;
//					if(maxDt < compareDt) maxDt = compareDt;
//				}
//				else {
//					compareDt = Converter.toInt(pageList.get(x-1).get("ACTUAL_SHIP_DT").toString().trim());
//					if(minDt > compareDt) minDt = compareDt;
//					if(maxDt < compareDt) maxDt = compareDt;
//				}
//			}
//			String minDtStr = Converter.toStr(minDt);
//			String maxDtStr = Converter.toStr(maxDt);
//			
//			if(pageMaxCount < pageList.size()) { // tr이 23개를 넘으면 pageCount를 증가시켜준다.
//				pageCount = (pageList.size() / pageMaxCount) + 1;
//				int lastTrCount = pageList.size() % pageMaxCount;
//				
//				for(int nowPage=1; nowPage<=pageCount; nowPage++) {
//					Map<String, Object> deliveryPaper = new HashMap<>();
//					
//					int sLimit = (nowPage-1)*pageMaxCount;
//					int eLimit = nowPage*pageMaxCount;
//					if(nowPage == pageCount && 0 != lastTrCount) eLimit = pageList.size();
//					
//					logger.debug("sLimit : {}, eLimit : {}", sLimit, eLimit);
//					
//					deliveryPaper.put("minDt", minDtStr.substring(0, 4)+"-"+minDtStr.substring(4, 6)+"-"+minDtStr.substring(6, 8));
//					deliveryPaper.put("maxDt", maxDtStr.substring(0, 4)+"-"+maxDtStr.substring(4, 6)+"-"+maxDtStr.substring(6, 8));
//					
//					deliveryPaper.put("deliveryPaper", pageList.subList(sLimit, eLimit));
//					deliveryPaperList.add(deliveryPaper);
//				}
//			}
//			else {
//				Map<String, Object> deliveryPaper = new HashMap<>();
//				pageCount = 1;
//				
//				deliveryPaper.put("minDt", minDtStr.substring(0, 4)+"-"+minDtStr.substring(4, 6)+"-"+minDtStr.substring(6, 8));
//				deliveryPaper.put("maxDt", maxDtStr.substring(0, 4)+"-"+maxDtStr.substring(4, 6)+"-"+maxDtStr.substring(6, 8));
//				
//				deliveryPaper.put("deliveryPaper", pageList);
//				deliveryPaperList.add(deliveryPaper);
//			}
//			
//			logger.debug("deliveryPaperList : {}", deliveryPaperList);
//			logger.debug("deliveryPaperList Size : {}", deliveryPaperList.size());
//			logger.debug("pageCount : {}", pageCount);
//			
//			model.addAttribute("pageCount", pageCount);
//			model.addAttribute("deliveryPaperList", deliveryPaperList);
//			
//		}
//		/******************************************
//		 * 11 = 아이템별-현장
//		 ******************************************/
//		else if(StringUtils.equals("11", paper_type)) {
//			// 선택한 건들의 현장(납품처) Group By 리스트 가져오기.
//			groupList = salesOrderDao.getOrderShipGroup(svcMap);
//			
//			// 현장(납품처)의 아이템별 리스트 가져오기.
//			List<Map<String, Object>> deliveryPaperList = new ArrayList<>();
//			int pageCount = 0;
//			svcMap.put("r_orderby", "ITEM_DESC ASC, ACTUAL_SHIP_DT ASC "); // 품목명 ASC, 실제 출하일 ASC 
//			//svcMap.put("r_orderby", "ACTUAL_SHIP_DT ASC, ITEM_DESC ASC "); // 실제 출하일 ASC, 품목명 ASC
//			
//			for(int i=0,j=groupList.size(); i<j; i++) {
//				Map<String, Object> deliveryPaper = new HashMap<>();
//				svcMap.put("r_shiptocd", groupList.get(i).get("SHIPTO_CD"));
//				
//				List<Map<String, Object>> pageList = salesOrderDao.getDeliveryPaperListForItemGroup(svcMap);
//				List<Map<String, Object>> newPageList = new ArrayList<>();
//				
//				int minDt = 30000000, maxDt = 0; // 출고일시 시작일,종료일
//				for(int x=0,y=pageList.size(); x<y; x++) {
//					int compareDt = 0;
//					if(0==x) {
//						minDt = Converter.toInt(pageList.get(x).get("ACTUAL_SHIP_DT").toString().trim()); 
//						maxDt = minDt; 
//					}
//					else if(x==y-1) {
//						compareDt = Converter.toInt(pageList.get(x).get("ACTUAL_SHIP_DT").toString().trim());
//						if(minDt > compareDt) minDt = compareDt;
//						if(maxDt < compareDt) maxDt = compareDt;
//					}
//					else {
//						compareDt = Converter.toInt(pageList.get(x-1).get("ACTUAL_SHIP_DT").toString().trim());
//						if(minDt > compareDt) minDt = compareDt;
//						if(maxDt < compareDt) maxDt = compareDt;
//					}
//					
//					// 소계 행(tr) 세팅.
//					Map<String, Object> subMap = new HashMap<>();
//					subMap.putAll(pageList.get(x));
//					newPageList.add(subMap);
//					
//					int preIdx = (0==x) ? 0 : x-1; 
//					String nowItemDesc = Converter.toStr(pageList.get(x).get("ITEM_DESC"));
//					String preItemDesc = Converter.toStr(pageList.get(preIdx).get("ITEM_DESC"));
//					
//					//int itemCnt = Converter.toInt(pageList.get(x).get("ITEM_CNT"));
//					//logger.debug("### itemCnt : {}", itemCnt);
//					if(!StringUtils.equals(nowItemDesc, preItemDesc)) { // 윗 행과 다른 품목명인 행.
//						// 윗 행의 소계 tr 추가.
//						int sumAddIdx= newPageList.size()-1;
//						int sumAddIdx1 = (0 >= sumAddIdx) ? 0 : sumAddIdx-1;
//						int sumAddIdx2 = (0 > sumAddIdx) ? 0 : sumAddIdx;
//						
//						Map<String, Object> sumMap = new HashMap<>();
//						sumMap.put("ITEM_DESC", "");
//						//sumMap.put("ITEM_DESC", pageList.get(preIdx).get("ITEM_DESC"));
//						sumMap.put("ACTUAL_SHIP_DT", "소계");
//						sumMap.put("ORDER_QTY", newPageList.get(sumAddIdx1).get("ITEM_SUM"));
//						sumMap.put("UNIT", newPageList.get(sumAddIdx1).get("UNIT"));
//						sumMap.put("ADD1", "");
//						sumMap.put("ADD2", "");
//						newPageList.add(sumAddIdx2, sumMap);
//					}
//					else { // 첫 행 + 마지막 행 + 윗 행과 동일한 품목명인 행.
//						// 마지막 행(마지막 this.for문)인 경우에만, 소계 tr 추가.
//						if(x == pageList.size()-1) {
//							Map<String, Object> sumMap2 = new HashMap<>();
//							sumMap2.put("ITEM_DESC", "");
//							//sumMap2.put("ITEM_DESC", pageList.get(preIdx).get("ITEM_DESC"));
//							sumMap2.put("ACTUAL_SHIP_DT", "소계");
//							sumMap2.put("ORDER_QTY", pageList.get(preIdx).get("ITEM_SUM"));
//							sumMap2.put("UNIT", pageList.get(preIdx).get("UNIT"));
//							sumMap2.put("ADD1", "");
//							sumMap2.put("ADD2", "");
//							newPageList.add(sumMap2);
//						}
//					}
//				}
//				String minDtStr = Converter.toStr(minDt);
//				String maxDtStr = Converter.toStr(maxDt);
//				deliveryPaper.put("minDt", minDtStr.substring(0, 4)+"-"+minDtStr.substring(4, 6)+"-"+minDtStr.substring(6, 8));
//				deliveryPaper.put("maxDt", maxDtStr.substring(0, 4)+"-"+maxDtStr.substring(4, 6)+"-"+maxDtStr.substring(6, 8));
//				
//				if(pageMaxCount < newPageList.size()) { // tr이 23개를 넘으면 pageCount를 증가시켜준다.
//					deliveryPaper.put("deliveryPaper", newPageList.subList(0, pageMaxCount));
//					//deliveryPaper.put("deliveryPaper"+pageCount, newPageList.subList(0, pageMaxCount));
//					deliveryPaperList.add(deliveryPaper);
//					pageCount++;
//					
//					deliveryPaper.put("deliveryPaper", newPageList.subList(pageMaxCount+1, newPageList.size()));
//					//deliveryPaper.put("deliveryPaper"+pageCount, newPageList.subList(pageMaxCount+1, newPageList.size()));
//					deliveryPaperList.add(deliveryPaper);
//				}
//				else {
//					deliveryPaper.put("deliveryPaper", newPageList);
//					//deliveryPaper.put("deliveryPaper"+pageCount, newPageList);
//					deliveryPaperList.add(deliveryPaper);
//				}
//				
//				pageCount++;
//			}
//			logger.debug("deliveryPaperList : {}", deliveryPaperList);
//			logger.debug("deliveryPaperList Size : {}", deliveryPaperList.size());
//			logger.debug("pageCount : {}", pageCount);
//			
//			model.addAttribute("pageCount", pageCount);
//			model.addAttribute("deliveryPaperList", deliveryPaperList);
//		}
//		/******************************************
//		 * 21 = 아이템별-착지
//		 ******************************************/
//		else if(StringUtils.equals("21", paper_type)) {
//			// 착지의 아이템별 리스트 가져오기.
//			List<Map<String, Object>> deliveryPaperList = new ArrayList<>();
//			int pageCount = 0;
//			svcMap.put("r_orderby", "ITEM_DESC ASC, ACTUAL_SHIP_DT ASC "); // 품목명 ASC, 실제 출하일 ASC 
//			//svcMap.put("r_orderby", "ACTUAL_SHIP_DT ASC, ITEM_DESC ASC "); // 실제 출하일 ASC, 품목명 ASC
//			
//			List<Map<String, Object>> pageList = salesOrderDao.getDeliveryPaperListForItemGroup(svcMap);
//			List<Map<String, Object>> newPageList = new ArrayList<>();
//			
//			int minDt = 30000000, maxDt = 0; // 출고일시 시작일,종료일
//			for(int x=0,y=pageList.size(); x<y; x++) {
//				int compareDt = 0;
//				if(0==x) {
//					minDt = Converter.toInt(pageList.get(x).get("ACTUAL_SHIP_DT").toString().trim()); 
//					maxDt = minDt; 
//				}
//				else if(x==y-1) {
//					compareDt = Converter.toInt(pageList.get(x).get("ACTUAL_SHIP_DT").toString().trim());
//					if(minDt > compareDt) minDt = compareDt;
//					if(maxDt < compareDt) maxDt = compareDt;
//				}
//				else {
//					compareDt = Converter.toInt(pageList.get(x-1).get("ACTUAL_SHIP_DT").toString().trim());
//					if(minDt > compareDt) minDt = compareDt;
//					if(maxDt < compareDt) maxDt = compareDt;
//				}
//				
//				// 소계 행(tr) 세팅.
//				Map<String, Object> subMap = new HashMap<>();
//				subMap.putAll(pageList.get(x));
//				newPageList.add(subMap);
//				
//				int preIdx = (0==x) ? 0 : x-1; 
//				String nowItemDesc = Converter.toStr(pageList.get(x).get("ITEM_DESC"));
//				String preItemDesc = Converter.toStr(pageList.get(preIdx).get("ITEM_DESC"));
//				
//				//int itemCnt = Converter.toInt(pageList.get(x).get("ITEM_CNT"));
//				//logger.debug("### itemCnt : {}", itemCnt);
//				if(!StringUtils.equals(nowItemDesc, preItemDesc)) { // 윗 행과 다른 품목명인 행.
//					// 윗 행의 소계 tr 추가.
//					int sumAddIdx= newPageList.size()-1;
//					int sumAddIdx1 = (0 >= sumAddIdx) ? 0 : sumAddIdx-1;
//					int sumAddIdx2 = (0 > sumAddIdx) ? 0 : sumAddIdx;
//					
//					Map<String, Object> sumMap2 = new HashMap<>();
//					sumMap2.put("ITEM_DESC", "");
//					sumMap2.put("ACTUAL_SHIP_DT", "소계");
//					sumMap2.put("ORDER_QTY", newPageList.get(sumAddIdx1).get("ITEM_SUM"));
//					sumMap2.put("UNIT", newPageList.get(sumAddIdx1).get("UNIT"));
//					sumMap2.put("ADD1", "");
//					sumMap2.put("ADD2", "");
//					newPageList.add(sumAddIdx2, sumMap2);
//				}
//				else { // 첫 행 + 마지막 행 + 윗 행과 동일한 품목명인 행.
//					// 마지막 행(마지막 this.for문)인 경우에만, 소계 tr 추가.
//					if(x == pageList.size()-1) {
//						Map<String, Object> sumMap2 = new HashMap<>();
//						sumMap2.put("ITEM_DESC", "");
//						//sumMap2.put("ITEM_DESC", pageList.get(preIdx).get("ITEM_DESC"));
//						sumMap2.put("ACTUAL_SHIP_DT", "소계");
//						sumMap2.put("ORDER_QTY", pageList.get(preIdx).get("ITEM_SUM"));
//						sumMap2.put("UNIT", pageList.get(preIdx).get("UNIT"));
//						sumMap2.put("ADD1", "");
//						sumMap2.put("ADD2", "");
//						newPageList.add(sumMap2);
//					}
//				}
//			}
//			String minDtStr = Converter.toStr(minDt);
//			String maxDtStr = Converter.toStr(maxDt);
//			logger.debug("minDtStr : {} maxDtStr : {}", minDtStr, maxDtStr);
//			
//			if(pageMaxCount < newPageList.size()) { // tr이 23개를 넘으면 pageCount를 증가시켜준다.
//				pageCount = (newPageList.size() / pageMaxCount) + 1;
//				int lastTrCount = newPageList.size() % pageMaxCount;
//				
//				for(int nowPage=1; nowPage<=pageCount; nowPage++) {
//					Map<String, Object> deliveryPaper = new HashMap<>();
//					
//					int sLimit = (nowPage-1)*pageMaxCount;
//					int eLimit = nowPage*pageMaxCount;
//					if(nowPage == pageCount && 0 != lastTrCount) eLimit = newPageList.size();
//					
//					logger.debug("sLimit : {}, eLimit : {}", sLimit, eLimit);
//					
//					deliveryPaper.put("minDt", minDtStr.substring(0, 4)+"-"+minDtStr.substring(4, 6)+"-"+minDtStr.substring(6, 8));
//					deliveryPaper.put("maxDt", maxDtStr.substring(0, 4)+"-"+maxDtStr.substring(4, 6)+"-"+maxDtStr.substring(6, 8));
//					
//					deliveryPaper.put("deliveryPaper", newPageList.subList(sLimit, eLimit));
//					deliveryPaperList.add(deliveryPaper);
//				}
//			}
//			else {
//				Map<String, Object> deliveryPaper = new HashMap<>();
//				pageCount = 1;
//				
//				deliveryPaper.put("minDt", minDtStr.substring(0, 4)+"-"+minDtStr.substring(4, 6)+"-"+minDtStr.substring(6, 8));
//				deliveryPaper.put("maxDt", maxDtStr.substring(0, 4)+"-"+maxDtStr.substring(4, 6)+"-"+maxDtStr.substring(6, 8));
//				
//				deliveryPaper.put("deliveryPaper", newPageList);
//				deliveryPaperList.add(deliveryPaper);
//			}
//			
//			logger.debug("deliveryPaperList : {}", deliveryPaperList);
//			logger.debug("deliveryPaperList Size : {}", deliveryPaperList.size());
//			logger.debug("pageCount : {}", pageCount);
//			
//			model.addAttribute("pageCount", pageCount);
//			model.addAttribute("deliveryPaperList", deliveryPaperList);
//		}
//	}
	
	
	
	/*public class GroupKey {
	    private Object abalphi;
	    private Object ssdoot;

	    public GroupKey(Object abalphi, Object ssdoot) {
	        this.abalphi = abalphi;
	        this.ssdoot = ssdoot;
	    }

	    public Object getAbalphi() {
	        return abalphi;
	    }

	    public Object getSsdoot() {
	        return ssdoot;
	    }

	    @Override
	    public boolean equals(Object o) {
	        if (this == o) return true;
	        if (o == null || getClass() != o.getClass()) return false;
	        GroupKey groupKey = (GroupKey) o;
	        return Objects.equals(abalphi, groupKey.abalphi) &&
	               Objects.equals(ssdoot, groupKey.ssdoot);
	    }

	    @Override
	    public int hashCode() {
	        return Objects.hash(abalphi, ssdoot);
	    }

	    @Override
	    public String toString() {
	        return "GroupKey{abalphi=" + abalphi + ", ssdoot=" + ssdoot + '}';
	    }
	}*/
}


