package com.limenets.eorder.scheduler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.limenets.common.util.Converter;
import com.limenets.common.util.MailUtil;
import com.limenets.eorder.svc.ConfigSvc;
import com.limenets.eorder.svc.OrderSvc;
import com.limenets.eorder.svc.TempleteSvc;

@Component
public class Scheduler {
	private static final Logger logger = LoggerFactory.getLogger(Scheduler.class);
	
	@Inject OrderSvc orderSvc;
	@Inject private TempleteSvc templeteSvc;
	@Inject private ConfigSvc configSvc;
	@Value("${https.url}") private String httpsUrl;
    @Value("${mail.smtp.sender.addr}") private String smtpSender;
    @Value("${shop.name}") private String shopName;
    @Value("${mail.smtp.url}") private String smtpHost;
    
	/**
	 * O_SALESORDER.STATUS2 = 배차완료(530)일경우, 푸시 발송 스케쥴러.
	 * @작성일 : 2020. 7. 8.
	 * @작성자 : kkyu
	 */
//	@Scheduled( cron ="00 */3 * * * *" )	// 3분마다 실행.
//	public void pushSalesOrder() throws Exception{
//		//logger.debug("####### 3분 마다 실행 스케쥴러 #######");
//		orderSvc.pushSalesOrderScheduler();
//	}
	
	@Scheduled( cron ="00 */15 * * * *" )	// 15분마다 실행.
	public void syncOrderDeleteUpdate() throws Exception{
		logger.debug("<<<<SYNC ORDER DELETE UPDATE>>>>");
		orderSvc.syncOrderDeleteUpdate();
	}
	
	/*
	@Scheduled( cron ="0 0 5 * * *" )	// 5시에 실행.
	public void syncSalesOrderDeliveryComplete() throws Exception{
		logger.debug("<<<<SYNC SALES ORDER DELIVERY COMPLETE>>>>");
		orderSvc.syncSalesOrderDeliveryComplete();
	}*/
	
	
	/**
     * O_SALESORDER.STATUS2 = 매일 오전 6시 QMS 오더 대상목록 동기화.
     * @작성일 : 2021. 7. 18.
     * @작성자 : jsh
     */
    /*@Scheduled( cron ="0 0 6 * * *" )    // 6시에 실행.
    public void syncQmsSalesOrder() throws Exception{
        orderSvc.syncQmsSalesOrder();
    }*/
    
    /**
     * O_SALESORDER.STATUS2 = 매일 오전 QMS 오더 사전입력 자동생성.
     * @작성일 : 2021. 7. 18.
     * @작성자 : jsh
     */
    @Scheduled( cron ="0 10 6 * * *" )    // 6시 10분에 실행.
    public void syncPreQmsSalesOrder() throws Exception{
        orderSvc.syncPreQmsSalesOrder();
    }
	
	/**
     * O_SALESORDER.STATUS2 = 매일 오전 QMS 사전입력 이메일 발송.
     * @작성일 : 2021. 7. 18.
     * @작성자 : jsh
     */
    @Scheduled( cron ="0 20 6 * * *" )    // 6시 20분에 실행.
    public void sendMailPreQmsOrder() throws Exception{
        HttpServletRequest req = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        
        List<Map<String, Object>> qmsPreOrderMailList = orderSvc.getMailPreQmsOrderList();
        
        for(int i = 0; i < qmsPreOrderMailList.size(); i++) {
            Map<String, Object> params = new HashMap<String, Object>();
       
            String r_shiptorepNm = Converter.toStr(qmsPreOrderMailList.get(i).get("SHIPTOREP_NM"));
            String r_shiptoEmail = Converter.toStr(qmsPreOrderMailList.get(i).get("SHIPTO_EMAIL"));
            String r_qmsId = Converter.toStr(qmsPreOrderMailList.get(i).get("QMS_ID"));
            String r_qmsSeq = Converter.toStr(qmsPreOrderMailList.get(i).get("QMS_SEQ"));
            
            params.put("qmsId", r_qmsId);
            params.put("qmsSeq", r_qmsSeq);
            params.put("shiptorepNm", r_shiptorepNm);
            params.put("shiptoEmail", r_shiptoEmail);
            
            String protocol = "";
            if(req.isSecure()) {
                protocol="https";
            }else {
                protocol="http";
            }
            // 메일보내기
            Map<String, Object> resMap = configSvc.getConfigList(params);
            String title = Converter.toStr(resMap.get("MAILTITLE"));
            String url =  protocol+"://"+ req.getServerName() + (req.getServerPort() == 80 ? "" : ":" + Converter.toStr(req.getServerPort())) +  req.getContextPath(); //httpsUrl
            String qmsReportUrl = url + "/front/report/qmsReport.lime?qmsId=" + r_qmsId + "-" + r_qmsSeq;
            String mailBottomImg = url + "/data/config/" + Converter.toStr(resMap.get("MAILBOTTOMIMG"));
            
            String contentStr = templeteSvc.qmsReportEmail(qmsReportUrl, url, mailBottomImg);
            
            MailUtil mail = new MailUtil();
            mail.sendMail(smtpHost,title, r_shiptorepNm, r_shiptoEmail, shopName, smtpSender, contentStr, null, "");
            //발송후 상태값 업데이트
            int result = orderSvc.setQmsMailUpdate(params);
            System.out.println(String.format("RESULT : %d", result));
        }
       
    }
}
