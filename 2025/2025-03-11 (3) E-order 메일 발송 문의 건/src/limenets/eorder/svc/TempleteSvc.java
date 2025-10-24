package com.limenets.eorder.svc;

import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.resource.loader.StringResourceLoader;
import org.apache.velocity.tools.generic.NumberTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;

import com.limenets.common.util.Converter;
import com.limenets.common.util.ResourceUtils;
import com.limenets.eorder.dto.BoardDto;



/**
 * Templete 생성 서비스.
 */
@Service
public class TempleteSvc {
    @Inject private BoardSvc boardSvc;

	private static final Logger logger = LoggerFactory.getLogger(TempleteSvc.class);
	
	/**
	 * Initialize VelocityEngine By File.
	 */
	public VelocityEngine initVelocityEngineByFile() throws Exception {
		String file = ResourceUtils.getClassLoader().getResource("velocity.properties").getFile();
		file = file.replaceAll("%20", " ");
		
		VelocityEngine engine = new VelocityEngine(file);
		engine.init();
		return engine;
	}
	public VelocityEngine initVelocityEngineByFileEuckr() throws Exception {
		String file = ResourceUtils.getClassLoader().getResource("velocity.properties").getFile();
		file = file.replaceAll("%20", " ");
		
		VelocityEngine engine = new VelocityEngine(file);
		engine.addProperty("input.encoding", "euc-kr"); 
		engine.addProperty("output.encoding", "euc-kr");
		engine.init();
		return engine;
	}
	
	/**
	 * Initialize VelocityEngine By String.
	 */
	public VelocityEngine initVelocityEngineByString() throws Exception {
		VelocityEngine engine = new VelocityEngine();
		//engine.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, "org.apache.velocity.runtime.log.Log4JLogChute");
	    //engine.setProperty("runtime.log.logsystem.log4j.logger", logger.getName());
	    engine.setProperty(Velocity.RESOURCE_LOADER, "string");
	    engine.addProperty("string.resource.loader.class", StringResourceLoader.class.getName());
	    engine.addProperty("string.resource.loader.repository.static", "false");
		engine.init();
		return engine;
	}
	
	/**
	 * 회원 비밀번호 초기화 이메일 폼
	 */
	public String userPswdResetEmail(String m_userid, String m_usernm, String m_userpswd, String url) throws Exception {
		VelocityEngine engin = initVelocityEngineByFile();
		VelocityContext context = new VelocityContext();
		
		context.put("user_id", m_userid);      //회원아이디
		context.put("user_name", m_usernm);    //회원명
		context.put("user_pswd", m_userpswd);  //초기화 비밀번호
		context.put("url", url);  			   //url
		
		Template template = engin.getTemplate("velocity/templete/userPswdGuide.html");
		StringWriter sw = new StringWriter();
		template.merge(context, sw);
		String textBody = sw.toString();
		
		logger.debug("textBody : {}", textBody);
		return textBody;
	}
	
	
	/**
	 * 거래사실확인서 이메일 폼
	 */
	public String factReportEmail(Map<String, Object> supplier, String[] shiptoArr, List<Map<String, Object>> salesOrderList, Map<String, Object> sumPrice
			, long failPrice, Map<String, Object> vAccount, String r_insdate, String r_inedate, String url, String ceosealImg, String m_papertype) throws Exception {
		VelocityEngine engin = initVelocityEngineByFile();
		VelocityContext context = new VelocityContext();
		
		context.put("supplier", supplier);  //공급자,공급받는자
		
		String ABTAX = Converter.toStr(supplier.get("ABTAX"));       //공급자 사업자등록번호
		String CUST_REG = Converter.toStr(supplier.get("CUST_REG")); //공급받는자 사업자등록번호
		
		if(ABTAX.length() > 9)
			context.put("ABTAX", ABTAX.substring(0, 3) + "-" + ABTAX.substring(3, 5) + "-" + ABTAX.substring(5, 10)); 
		else 
			context.put("ABTAX", "");
		
		if(CUST_REG.length() > 9)
			context.put("CUST_REG", CUST_REG.substring(0, 3) + "-" + CUST_REG.substring(3, 5) + "-" + CUST_REG.substring(5, 10));  
		else 
			context.put("CUST_REG", "");
		
		context.put("shiptoArr", shiptoArr);  			//납품처
		context.put("salesOrderList", salesOrderList);  //거래내역
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        Calendar c1 = Calendar.getInstance();
        String strToday = sdf.format(c1.getTime());
		
		//전월채권,당월매출,현금수금,어음수금,당월채권
		long OPEN_AMOUNT = 0L, GROSS_AMOUNT = 0L, RECEIVED_CASH = 0L, Z4AG_CHECKAMT_6 = 0L, CURRENT_AMOUNT = 0L, RECEIVED_CASH_NOTE = 0L;
		if(!CollectionUtils.isEmpty(sumPrice)) {
			OPEN_AMOUNT = Converter.toLong(Converter.getSapNumber("", sumPrice.get("OPEN_AMOUNT")));
			GROSS_AMOUNT = Converter.toLong(Converter.getSapNumber("", sumPrice.get("GROSS_AMOUNT")));
			RECEIVED_CASH = Converter.toLong(Converter.getSapNumber("", sumPrice.get("RECEIVED_CASH")));
			//Z4AG_CHECKAMT_6 = Converter.toLong(Converter.getSapNumber("", sumPrice.get("Z4AG_CHECKAMT_6")));
			Z4AG_CHECKAMT_6 = Converter.toLong(Converter.getSapNumber("", sumPrice.get("RECEIVED_NOTE")));
			CURRENT_AMOUNT = Converter.toLong(Converter.getSapNumber("", sumPrice.get("CURRENT_AMOUNT")));
			RECEIVED_CASH_NOTE = Converter.toLong(Converter.getSapNumber("", sumPrice.get("RECEIVED_CASH_NOTE")));
		}
		context.put("OPEN_AMOUNT", OPEN_AMOUNT);
		context.put("GROSS_AMOUNT", GROSS_AMOUNT); 
		context.put("RECEIVED_CASH", RECEIVED_CASH);
		context.put("Z4AG_CHECKAMT_6", Z4AG_CHECKAMT_6); 
		context.put("CURRENT_AMOUNT", CURRENT_AMOUNT);
		context.put("RECEIVED_CASH_NOTE", RECEIVED_CASH_NOTE);
		
		context.put("failPrice", failPrice);     		  //미도래어음
		context.put("vAccount", vAccount);      		  //거래처입금계좌
		context.put("r_insdate", r_insdate);  			  //조회시작일
		context.put("r_inedate", r_inedate);  			  //조회종료일
		context.put("r_year", r_inedate.substring(0, 4)); //년도
		context.put("r_month", r_inedate.substring(5, 7));//월
		context.put("r_today_year", strToday.substring(0, 4));
		context.put("r_today_mon", strToday.substring(4, 6));
		context.put("r_today_day", strToday.substring(6, 8));
		
		context.put("ceoseal", ceosealImg);     		  //직인이미지
		context.put("url", url);  						  //url
		context.put("papertype", m_papertype);  		  //전송기준
		context.put("number", new NumberTool());
		
		Template template = engin.getTemplate("velocity/templete/tradingConfirmationPaperPop.html", "UTF-8");
		StringWriter sw = new StringWriter();
		template.merge(context, sw);
		String textBody = sw.toString();
		
		//logger.debug("textBody : {}", textBody);
		return textBody;
	}
	
	/**
     * QMS Report 이메일 폼
     */
    public String qmsReportEmail(String qmsReportUrl, String url, String mailBottomImg) throws Exception {
        VelocityEngine engin = initVelocityEngineByFile();
        VelocityContext context = new VelocityContext();
        
        context.put("qmsReportUrl", qmsReportUrl);   //report url
        context.put("url", url);                     //url
        context.put("mailBottomImg", mailBottomImg); //mailBottomImg
        
        Template template = engin.getTemplate("velocity/templete/qmsReportPage.html");
        StringWriter sw = new StringWriter();
        template.merge(context, sw);
        String textBody = sw.toString();
        
        logger.debug("textBody : {}", textBody);
        return textBody;
    }   
    /**
     * sample Report 이메일 폼
     */
    public String sampleReportEmail(String sampleReportUrl, String url, String mailBottomImg,  String  m_bdindate, String m_bdtitle,  String m_bduser) throws Exception {
        System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&     sampleReportEmail BEGIN         &&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
        VelocityEngine engin = initVelocityEngineByFile();
        VelocityContext context = new VelocityContext();
        //Map<String, Object> params = new HashMap<String, Object>(); ////
        //params.put("r_bdseq", bdseq);                                                  ////
       //BoardDto resMap = boardSvc.getBoardOne(params);                 ////
        
        //model.addAttribute("boardOne", resMap);
        
        //context.put("r_bdseq", bdseq);
        context.put("sampleReportUrl", sampleReportUrl);   //report url
        context.put("url", url);                     //url
        context.put("mailBottomImg", mailBottomImg); //mailBottomImg
        context.put("m_bdindate", m_bdindate); //m_bdindate
        context.put("m_bdtitle", m_bdtitle); //m_bdtitle
        context.put("m_bduser", m_bduser); //m_bduser
        
        Template template = engin.getTemplate("velocity/templete/sampleReportPage.html");
        StringWriter sw = new StringWriter();
        template.merge(context, sw);
        String textBody = sw.toString();
        logger.debug("textBody : {}", textBody);
        System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&       sampleReportEmail END       &&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
        return textBody;
    }
    
}
