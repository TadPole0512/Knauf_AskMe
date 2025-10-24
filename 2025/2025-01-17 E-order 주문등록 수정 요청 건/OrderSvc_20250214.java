package com.limenets.eorder.svc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Arrays;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import com.limenets.common.dto.LoginDto;
import com.limenets.common.exception.LimeBizException;
import com.limenets.common.exception.MsgCode;
import com.limenets.common.util.AppPushUtil;
import com.limenets.common.util.Converter;
import com.limenets.common.util.FileCompressDown;
import com.limenets.common.util.FileDown;
import com.limenets.common.util.FileUpload;
import com.limenets.common.util.HttpUtils;
import com.limenets.common.util.Pager;
import com.limenets.eorder.dao.AppPushDao;
import com.limenets.eorder.dao.CustOrderDDao;
import com.limenets.eorder.dao.CustOrderHDao;
import com.limenets.eorder.dao.OrderAddressBookmarkDao;
import com.limenets.eorder.dao.OrderConfirmDDao;
import com.limenets.eorder.dao.OrderConfirmHDao;
import com.limenets.eorder.dao.OrderHeaderHistoryDao;
import com.limenets.eorder.dao.QmsOrderDao;
import com.limenets.eorder.dao.SalesOrderDao;
import com.limenets.eorder.dao.ShipToDao;
import com.limenets.eorder.dao.UserDao;
import com.limenets.eorder.util.StatusUtil;
import com.sun.corba.se.impl.orbutil.ObjectUtility;

/**
 * 주문 서비스.
 */
/**
 *
 * @작성일 : 2020. 4. 2.
 * @작성자 : kkyu
 */
@Service
public class OrderSvc {
    private static final Logger logger = LoggerFactory.getLogger(OrderSvc.class);

    @Resource(name="fileUpload") private FileUpload fileUpload;
    
    @Value("${https.url}") private String httpsUrl;
    
    @Inject private ItemSvc itemSvc;
    @Inject private ConfigSvc configSvc;
    @Inject private CustomerSvc customerSvc;
    
    @Inject private AppPushDao appPushDao;
    @Inject private CustOrderHDao custOrderHDao;
    @Inject private CustOrderDDao custOrderDDao;
    @Inject private OrderAddressBookmarkDao orderAddressBookmarkDao;
    @Inject private OrderConfirmHDao orderConfirmHDao;
    @Inject private OrderConfirmDDao orderConfirmDDao;
    @Inject private OrderHeaderHistoryDao orderHeaderHistoryDao;
    @Inject private SalesOrderDao salesOrderDao;
    @Inject private QmsOrderDao qmsOrderDao;
    @Inject private UserDao userDao;
    @Inject private ShipToDao shipToDao;
    @Inject private SapRestApiSvc sapApiSvc;
    
    /**
     * Get O_CUST_ORDER_H One.
     * @작성일 : 2020. 4. 2.
     * @작성자 : kkyu
     */
    public Map<String, Object> getCustOrderHOne(String req_no){
        Map<String, Object> svcMap = new HashMap<>();
        svcMap.put("r_reqno", req_no);
        return this.getCustOrderHOne(svcMap);
    }
    public Map<String, Object> getCustOrderHOne(String req_no, String userid, String status_cd){
        Map<String, Object> svcMap = new HashMap<>();
        svcMap.put("r_reqno", req_no);
        svcMap.put("r_userid", userid);
        svcMap.put("r_statuscd", status_cd);
        return this.getCustOrderHOne(svcMap);
    }
    public Map<String, Object> getCustOrderHOne(LoginDto loginDto, String req_no){
        Map<String, Object> svcMap = new HashMap<>();
        svcMap.put("r_reqno", req_no);
        svcMap.put("r_custcd", loginDto.getCustCd());
        svcMap.put("r_shiptocd", loginDto.getShiptoCd());
        return this.getCustOrderHOne(svcMap);
    }
    public Map<String, Object> getCustOrderHOne(Map<String, Object> svcMap){
        return custOrderHDao.one(svcMap);
    }
    
    /**
     * Get O_CUST_ORDER_H SaveOne. 영업담당자를 저장하기 위해 추가된 메서드
     * @작성일 : 2025. 1. 24.
     * @작성자 : hsg
     */
    public Map<String, Object> getCustOrderHSaveOne(String req_no){
        Map<String, Object> svcMap = new HashMap<>();
        svcMap.put("r_reqno", req_no);
        svcMap.put("r_mode", "save"); // 2025-01-24 hsg. 기존 로직에 저장여부를 구분하는 변수 추가.
        return this.getCustOrderHOne(svcMap);
    }
    public Map<String, Object> getCustOrderHSaveOne(LoginDto loginDto, String req_no){
        Map<String, Object> svcMap = new HashMap<>();
        svcMap.put("r_reqno", req_no);
        svcMap.put("r_custcd", loginDto.getCustCd());
        svcMap.put("r_shiptocd", loginDto.getShiptoCd());
        svcMap.put("r_mode", "save"); // 2025-01-24 hsg. 기존 로직에 저장여부를 구분하는 변수 추가.
        return this.getCustOrderHOne(svcMap);
    }


    
    /**
     * Get O_CUST_ORDER_H List.
     * @작성일 : 2020. 4. 8.
     * @작성자 : kkyu
     */
    public List<Map<String, Object>> getCustOrderHList(Map<String, Object> svcMap){
        return custOrderHDao.list(svcMap);
    }
    
    /**
     * Get O_CUST_ORDER_D List.
     * @작성일 : 2020. 4. 2.
     * @작성자 : kkyu
     */
    public List<Map<String, Object>> getCustOrderDList(String req_no, String cust_cd, String shipto_cd, String order_by, int start_row, int end_row){
        Map<String, Object> svcMap = new HashMap<>();
        svcMap.put("r_reqno", req_no);
        svcMap.put("r_custcd", cust_cd);
        svcMap.put("r_shiptocd", shipto_cd);
        svcMap.put("r_orderby", (StringUtils.equals("", order_by) ? "XX.REQ_NO DESC, XX.LINE_NO ASC " : order_by));
        svcMap.put("r_startrow", start_row);
        svcMap.put("r_endrow", end_row);
        return this.getCustOrderDList(svcMap);
    }
    public List<Map<String, Object>> getCustOrderDList(Map<String, Object> svcMap){
        return custOrderDDao.list(svcMap);
    }
    
    /**
     * Get O_ORDER_CONFIRM_D List.
     * @작성일 : 2020. 4. 28.
     * @작성자 : kkyu
     */
    public List<Map<String, Object>> getOrderConfirmDList(String req_no, String cust_po, String order_by, int start_row, int end_row){
        Map<String, Object> svcMap = new HashMap<>();
        svcMap.put("r_reqno", req_no);
        svcMap.put("r_custcd", cust_po);
        // 2024-10-16 hsg 별칭 오류가 나서 수정. OCD -> XX)
        svcMap.put("r_orderby", (StringUtils.equals("", order_by) ? "XX.CUST_PO ASC, XX.LINE_NO ASC " : order_by));
        svcMap.put("r_startrow", start_row);
        svcMap.put("r_endrow", end_row);
        return this.getOrderConfirmDList(svcMap);
    }
    public List<Map<String, Object>> getOrderConfirmDList(String req_no, String cust_po, String not_status_cd, String order_by, int start_row, int end_row){
        Map<String, Object> svcMap = new HashMap<>();
        svcMap.put("r_reqno", req_no);
        svcMap.put("r_custcd", cust_po);
        svcMap.put("rn_statuscd", not_status_cd);
        // 2024-10-22 hsg 별칭 오류가 나서 수정. OCD -> XX)
        svcMap.put("r_orderby", (StringUtils.equals("", order_by) ? "XX.CUST_PO ASC, XX.LINE_NO ASC " : order_by));
        svcMap.put("r_startrow", start_row);
        svcMap.put("r_endrow", end_row);
        return this.getOrderConfirmDList(svcMap);
    }
    public List<Map<String, Object>> getOrderConfirmDList(Map<String, Object> svcMap){
        return orderConfirmDDao.list(svcMap);
    }
    
    /**
     * Get QMS Order List.
     * @작성일 : 2020. 4. 24.
     * @작성자 : kkyu
     */
    public List<Map<String, Object>> getQmsOrderList(Map<String, Object> svcMap){
        return qmsOrderDao.list(svcMap);
    }
    
    /**
     * Get QMS getQmsPopDetlGridList.
     * @작성일 : 2020. 5. 4.
     * @작성자 : jsh
     */
    public List<Map<String, Object>> getQmsPopDetlGridList(Map<String, Object> svcMap){
        return qmsOrderDao.getQmsPopDetlGridList(svcMap);
    }
    
    /**
     * Get O_SALESORDER List.
     * @작성일 : 2020. 4. 24.
     * @작성자 : kkyu
     */
    public List<Map<String, Object>> getSalesOrderList(Map<String, Object> svcMap){
        return salesOrderDao.list(svcMap);
    }
    
    public Map<String, Object> getSalesOrder(Map<String, Object> svcMap){
        return salesOrderDao.one(svcMap);
    }
    
    public int getCustOrderHCnt(Map<String, Object> svcMap){
        return custOrderHDao.cnt(svcMap);
    }
    
    public int getSalesOrderCnt(Map<String, Object> svcMap){
        return salesOrderDao.cnt(svcMap);
    }
    
    public int getSalesOrderGroupCnt(Map<String, Object> svcMap){
        return salesOrderDao.cntGroup(svcMap);
    }
    
    /**
     * Get OrderHeaderHistory One Recent List.
     * @작성일 : 2020. 5. 12.
     * @작성자 : kkyu
     */
    public Map<String, Object> getOrderHeaderHistoryOneByRecentList(String ohh_reqno){
        Map<String, Object> svcMap = new HashMap<>();
        svcMap.put("r_ohhreqno", ohh_reqno);
        return orderHeaderHistoryDao.oneByRecentList(svcMap);
    }
    
    /**
     * 주문 헤더 리스트 가져오기.
     * @작성일 : 2020. 4. 8.
     * @작성자 : kkyu
     * @param where : admin=관리자, excel=관리자 엑셀, front=거래처,납품처, frontexcel=거래처,납품처 엑셀.
     */
    public Map<String, Object> getOrderHeaderList(Map<String, Object> params, HttpServletRequest req, LoginDto loginDto) throws Exception {
        Map<String, Object> resMap = new HashMap<>();
        
        String where = Converter.toStr(params.get("where"));
        
        //String r_insdate = Converter.toStr(params.get("r_insdate")); // 주문일자 검색 시작일.
        //String r_inedate = Converter.toStr(params.get("r_insdate")); // 주문일자 검색 종료일.
        //String rl_reqno = Converter.toStr(params.get("rl_reqno")); // 주문번호 검색.
        //String r_csuserid = Converter.toStr(params.get("r_csuserid")); // 주문한 거래처의 담당 영업사원의 고정 CS코드 검색.
        //String r_custnm = Converter.toStr(params.get("r_custnm")); // 주문한 거래처명 검색. like 아님.
        //String r_shiptocd = Converter.toStr(params.get("r_shiptocd")); // 주문한 거래처의 납품처 코드 검색.
        //String rl_salesusernm = Converter.toStr(params.get("rl_salesusernm")); // 주문한 거래처의 담당 영업사원명 검색.
        String ri_statuscds = Converter.toStr(params.get("ri_statuscds")); // 상태값으로  ,로 구분.
        
        if(!StringUtils.equals("front", where)) {
            if(StringUtils.equals("", ri_statuscds)) {
                String[] removeStatusArr = {"99"}; //임시저장
                List<String> statusList = (StringUtils.equals("admin", where) || StringUtils.equals("excel", where)) ? StatusUtil.ORDER.getKeyList(removeStatusArr) : StatusUtil.ORDER.getKeyList(); // 관리자는 임시저장 상태 제외한 리스트.
                logger.debug("statusList : {}", statusList);
                params.put("ri_statuscd", statusList);
            }
            else {
                params.put("ri_statuscd", ri_statuscds.split(",", -1));
            }
        }else {
            String[] ri_statuscd = req.getParameterValues("ri_statuscd");
            params.put("ri_statuscd", ri_statuscd);
        }

        int totalCnt = custOrderHDao.cnt(params);

        Pager pager = new Pager();
        pager.gridSetInfo(totalCnt, params, req);
        resMap.put("total", Converter.toInt(params.get("totpage")));
        resMap.put("listTotalCount", totalCnt);
        
        // Start. Define Only For Form-Paging.
        resMap.put("startnumber", params.get("startnumber"));
        resMap.put("r_page", params.get("r_page"));
        resMap.put("startpage", params.get("startpage"));
        resMap.put("endpage", params.get("endpage"));
        resMap.put("r_limitrow", params.get("r_limitrow"));
        // End.
        
        String r_orderby = "";
        String sidx = Converter.toStr(params.get("sidx")); //정렬기준컬럼
        String sord = Converter.toStr(params.get("sord")); //내림차순,오름차순
        if(StringUtils.equals("USERID", sidx)) sidx = "COH."+sidx; //alias 명시.
        if(StringUtils.equals("CUST_CD", sidx)) sidx = "COH."+sidx;
        if(StringUtils.equals("SHIPTO_CD", sidx)) sidx = "COH."+sidx;
        r_orderby = sidx + " " + sord;
        // 2024-10-16 hsg 별칭 오류가 나서 수정. COH -> XX)
        if(StringUtils.equals("", sidx)) { r_orderby = "XX.INDATE DESC "; } //디폴트 지정
        
        params.put("r_orderby", r_orderby);

        // 엑셀 다운로드.
        if(StringUtils.equals("excel", where) || StringUtils.equals("frontexcel", where) || StringUtils.equals("orderadd", where)) {
            params.remove("r_startrow");
            params.remove("r_endrow");
        }
        
        List<Map<String, Object>> list = this.getCustOrderHList(params);
        resMap.put("list", list);
        resMap.put("data", list);
        resMap.put("page", params.get("r_page"));
        
        resMap.put("where", where);
        
        return resMap;
    }
    
    /**
     * CUST_ORDER_Detail 리스트 가져오기.
     * @작성일 : 2020. 3. 27.
     * @작성자 : kkyu
     */
    public Map<String, Object> getCustOrderDetailList(Map<String, Object> params, HttpServletRequest req, LoginDto loginDto) throws Exception {
        Map<String, Object> resMap = new HashMap<>();
        
        String where = Converter.toStr(params.get("where"));
        
        String r_reqno = Converter.toStr(params.get("r_reqno")); // 주문번호.
        String r_custcd = Converter.toStr(params.get("r_custcd")); // 거래처코드.
        String r_shiptocd = Converter.toStr(params.get("r_shiptocd")); // 납품처코드.
        logger.debug("r_reqno : {}", r_reqno);
        logger.debug("r_custcd : {}", r_custcd);
        logger.debug("r_shiptocd : {}", r_shiptocd);

        int totalCnt = custOrderDDao.cnt(params);

        Pager pager = new Pager();
        pager.gridSetInfo(totalCnt, params, req);
        resMap.put("total", Converter.toInt(params.get("totpage")));
        resMap.put("listTotalCount", totalCnt);
        
        // Start. Define Only For Form-Paging.
        resMap.put("startnumber", params.get("startnumber"));
        resMap.put("r_page", params.get("r_page"));
        resMap.put("startpage", params.get("startpage"));
        resMap.put("endpage", params.get("endpage"));
        resMap.put("r_limitrow", params.get("r_limitrow"));
        // End.
        
        String r_orderby = "";
        String sidx = Converter.toStr(params.get("sidx")); //정렬기준컬럼
        String sord = Converter.toStr(params.get("sord")); //내림차순,오름차순
        r_orderby = sidx + " " + sord;
        if(StringUtils.equals("", sidx)) { r_orderby = "LINE_NO ASC "; } //디폴트 지정
        
        params.put("r_orderby", r_orderby);

        // 엑셀 다운로드.
        if(StringUtils.equals("excel", where) || StringUtils.equals("frontexcel", where) || StringUtils.equals("orderadd", where)) {
            params.remove("r_startrow");
            params.remove("r_endrow");
        }
        
        List<Map<String, Object>> list = this.getCustOrderDList(params);
        resMap.put("list", list);
        resMap.put("data", list);
        
        resMap.put("where", where);
        
        return resMap;
    }
    
    /**
     * 최근에 주문한 주문 한 건(CUST_ORDER_Header) 가져오기.
     * @작성일 : 2020. 3. 31.
     * @작성자 : kkyu
     */
    public Map<String, Object> getRecentCustOrderHeader(Map<String, Object> params, HttpServletRequest req, LoginDto loginDto) throws Exception {
        Map<String, Object> resMap = new HashMap<>();
        
        params.put("r_userid", loginDto.getUserId());
     // 2024-10-18 hsg 별칭 오류가 나서 수정. COH -> XX)
        params.put("r_orderby", "XX.INDATE DESC ");
        params.put("r_startrow", 0);
        params.put("r_endrow", 1);
        
        resMap.put("recent", custOrderHDao.list(params));
        return resMap;
    }
    
    /**
     * O_CUST_ORDER_Header 주소록 리스트 가져오기.
     * @작성일 : 2020. 4. 1.
     * @작성자 : kkyu
     */
    public Map<String, Object> getOrderAddressBookmark(Map<String, Object> params, HttpServletRequest req, LoginDto loginDto) throws Exception {
        Map<String, Object> resMap = new HashMap<>();
        
        String layer_pop = Converter.toStr(params.get("layer_pop")); // Y=모바일앱 레이어팝업 여부.
        logger.debug("layer_pop : {}", layer_pop);

        /* ****** 2024-12-24 hsg Camel Clutch first 여기가 시작. E-order front – 주문 등록 – 주소록 List에서 Keyword 서치 기능 추가 ***** */
        String srchBgn = Converter.toStr(params.get("r_srchGbn"));
        String selectedColumn = "";

        if(!srchBgn.equals("")) {
        	switch(srchBgn) {
	        	case "ADD1" : selectedColumn = "OAB_ADD1"; break;
	        	case "RECEIVER" : selectedColumn = "OAB_RECEIVER"; break;
	        	case "TEL1" : selectedColumn = "OAB_TEL1"; break;
	        	case "TEL2" : selectedColumn = "OAB_TEL2"; break;
        	}
        }
        	
        params.put("r_selectedColumn", selectedColumn);
        /* ****** 2024-12-24 hsg Camel Clutch first 쪼기서 시작한거 여기가 끝. E-order front – 주문 등록 – 주소록 List에서 Keyword 서치 기능 추가 ***** */
        int totalCnt = orderAddressBookmarkDao.cnt(params);

        Pager pager = new Pager();
        pager.gridSetInfo(totalCnt, params, req);
        resMap.put("total", Converter.toInt(params.get("totpage")));
        resMap.put("listTotalCount", totalCnt);
        
        // Start. Define Only For Form-Paging.
        resMap.put("startnumber", params.get("startnumber"));
        resMap.put("r_page", params.get("r_page"));
        resMap.put("startpage", params.get("startpage"));
        resMap.put("endpage", params.get("endpage"));
        resMap.put("r_limitrow", params.get("r_limitrow"));
        // End.
        
        String r_orderby = "";
        String sidx = Converter.toStr(params.get("sidx")); //정렬기준컬럼
        String sord = Converter.toStr(params.get("sord")); //내림차순,오름차순
        r_orderby = sidx + " " + sord;
        if(StringUtils.equals("", sidx)) { r_orderby = "OAB_INDATE DESC "; } //디폴트 지정
        params.put("r_orderby", r_orderby);

        // Y=모바일앱 레이어팝업인 경우는 페이징 처리 제거.
        if(StringUtils.equals("Y", layer_pop)) {
            params.remove("r_startrow");
            params.remove("r_endrow");
        }
        
        List<Map<String, Object>> list = orderAddressBookmarkDao.list(params);
        resMap.put("list", list);
        resMap.put("data", list);
        
        return resMap;
    }
    
    /**
     * O_CUST_ORDER_Header 주소록 삭제하기.
     * @작성일 : 2020. 4. 1.
     * @작성자 : kkyu
     */
    public Map<String, Object> deleteOrderAddressBookmark(Map<String, Object> params, HttpServletRequest req, LoginDto loginDto) throws Exception {
        String ri_oabseqs = Converter.toStr(params.get("ri_oabseqs"));
        if(StringUtils.equals("", ri_oabseqs)) throw new LimeBizException(MsgCode.DATA_REQUIRE_ERROR2);
            
        params.put("ri_oabseq", ri_oabseqs.split(",", -1));
        orderAddressBookmarkDao.delByArr(params);
        
        return MsgCode.getResultMap(MsgCode.SUCCESS);
    }
    
    
    /**
     * O_CUST_ORDER 임시저장 시 사전입력
     * @작성일 : 2020. 6. 22.
     * @작성자 : jsh
     * @param where : admin=영업사원, front=거래처,납품처.
     * @param authority by session : SH,SR,SM=내부영업사원, CO=거래처, CT=납품처.
     */
    public Map<String, Object> setQmsFirstOrderAjax(Map<String, Object> params, HttpServletRequest req, LoginDto loginDto) throws Exception {
        String m_reqNo = Converter.toStr(params.get("m_reqNo"));
        params.put("r_userid", loginDto.getUserId());
        Boolean firstInsertFlag = true;
        String qmsTempId = null;
        
        // Insert O_CUST_ORDER_D;
        String[] mi_itemcd = req.getParameterValues("m_itemcd");
        String[] mi_unit   = req.getParameterValues("m_unit");
        String[] mi_quantity = req.getParameterValues("m_quantity");
        String[] mi_fireproof = req.getParameterValues("m_fireproof");
        
        // 기존 QMS 채번여부 체크
        qmsTempId = qmsOrderDao.getQmsFirstOrderCheckAjax(params);
        
        //아이템 코드와 QMS 번호가 존재하는 경우에만 입력 진행.
        if(!ArrayUtils.isEmpty(mi_itemcd) && StringUtils.isEmpty(qmsTempId)) {
            for(int i=0,j=mi_itemcd.length; i<j; i++) {
                params.put("m_lineno", (i+1)*1000);
                params.put("m_itemcd", mi_itemcd[i]);
                params.put("m_unit", mi_unit[i]);
                params.put("mi_fireproof", mi_fireproof[i]);
                params.put("m_quantity", mi_quantity[i].replaceAll(",", ""));
                params.put("m_price", 0);
                //내화구조가 존재하는 경우 디테일 입력
                if(mi_fireproof[i]!=null && mi_fireproof[i].equals("Y")) {
                    //QMS 헤더정보는 한번만 입력
                    if (firstInsertFlag) {
                      //QMS Temp ID 신규 채번
                      qmsTempId = qmsOrderDao.getQmsTempId(params);
                      params.put("qmsTempId", qmsTempId);
                      
                      //QMS 헤더 입력
                      qmsOrderDao.setQmsFirstOrderAjax(params);
                      firstInsertFlag = false;
                    }
                    //QMS 디테일(아이템) 입력
                    qmsOrderDao.setQmsFirstOrderDetailAjax(params);
                }
            }
        }
        
        Map<String, Object> result = MsgCode.getResultMap(MsgCode.SUCCESS);
        result.put("qmsTempId", qmsTempId);
        return result;
    }
    
    /**
     * QMS 오더 사전입력 취소
     * @작성일 : 2021. 7. 12.
     * @작성자 : jsh
     */
    public Object setQmsFirstOrderCancelAjax(Map<String, Object> svcMap){
        return qmsOrderDao.setQmsFirstOrderCancelAjax(svcMap);
    }
    
    /**
     * O_CUST_ORDER 임시저장(99) 및 주문접수(00) 처리.
     * @작성일 : 2020. 4. 1.
     * @작성자 : kkyu
     * @param where : admin=영업사원, front=거래처,납품처.
     * @param authority by session : SH,SR,SM=내부영업사원, CO=거래처, CT=납품처.
     */
    public Map<String, Object> insertCustOrderTransaction(Map<String, Object> params, HttpServletRequest req, LoginDto loginDto) throws Exception {
//      Map<String, Object> svcMap = new HashMap<>();
        
        String where = Converter.toStr(params.get("where"));
        String authority = loginDto.getAuthority();
        
        String r_reqno = Converter.toStr(params.get("r_reqno")); // 주문번호. => 이미 임시저장이였던 
        String m_statuscd = Converter.toStr(params.get("m_statuscd")); // 변경하려는 상태값.
        String m_custcd = Converter.toStr(params.get("m_custcd")); // 거래처 코드.
        String m_shiptocd = Converter.toStr(params.get("m_shiptocd"), "0"); // 납품처 코드.
        String m_transty = Converter.toStr(params.get("m_transty")); // 운송수단 AA=기본운송,AB=자차운송
        String m_reqno = ""; // 주문번호 정의.
        String m_ohhreqno = ""; // 주문이력 주문번호 정의.
        String m_zipcd = Converter.toStr(params.get("m_zipcd"));
        
        String m_accessdevice = Converter.toStr(params.get("m_accessdevice"), "1"); // 1=PC웹, 2=모바일.
        
        if(StringUtils.equals("00", m_statuscd) && StringUtils.equals("AB", m_transty)) { // 주문접수 상태로 변경시 운송수단이 자차운송(AB)인 경우, 우편번호 코드를 90000으로 변경.
            params.put("m_zipcd", "90000");
        }
        
        // 공통 params 세팅.
        if(StringUtils.equals("front", where)) m_custcd =  loginDto.getCustCd();
        if(StringUtils.equals("CT", authority)) m_shiptocd =  loginDto.getShiptoCd();
        
        params.put("m_custcd", m_custcd);
        params.put("m_shiptocd", m_shiptocd);
        params.put("m_userid", loginDto.getUserId());
        params.put("m_insertid", loginDto.getUserId());
        params.put("m_updateid", loginDto.getUserId());
        
        // 현재  저장된 O_CUST_ORDER_H 가져오기.
        Map<String, Object> nOrder = null;
        if(!StringUtils.equals("", r_reqno)) {
            nOrder = this.getCustOrderHOne(r_reqno);
        }
        
        // 임시저장.
        if(StringUtils.equals("99", m_statuscd)) {
            // 변경하려는 상태값이 임시저장(99)인 경우, 임시 주문번호 생성.
            // 임시저장 인서트 => 이미 임시저장 상태였던 건이 아닌, 처음으로 임시저장 상태로 인서트 하려는 경우에만 해당.
            if(StringUtils.equals("", r_reqno)) {
                m_reqno = this.createCustOrderTempReqNo("T");
                params.put("m_reqno", m_reqno);
                params.put("m_accessdevice", m_accessdevice);
                
                // Insert O_CUST_ORDER_H.
                custOrderHDao.in(params); 
                
                // Insert O_CUST_ORDER_D;
                String[] mi_itemcd = req.getParameterValues("m_itemcd");
                String[] mi_unit = req.getParameterValues("m_unit");
                String[] mi_quantity = req.getParameterValues("m_quantity");
                if(!ArrayUtils.isEmpty(mi_itemcd)) {
                    for(int i=0,j=mi_itemcd.length; i<j; i++) {
                        params.put("m_lineno", (i+1)*1000);
                        params.put("m_itemcd", mi_itemcd[i]);
                        params.put("m_unit", mi_unit[i]);
                        params.put("m_quantity", mi_quantity[i].replaceAll(",", ""));
                        params.put("m_price", 0);
                        custOrderDDao.in(params);
                    }
                }
                
                m_ohhreqno = m_reqno; // 주문이력관리 저장할 주문번호 정의.
            }
            // 임시저장으로 업데이트.
            else {
                // 상태값 체크.
                //logger.debug("변경 전 상태값 : {}", nOrder.get("STATUS_CD"));
                //logger.debug("변경 하려는 상태값 : {}", m_statuscd);
                if(!StatusUtil.ORDER.statusCheck(Converter.toStr(nOrder.get("STATUS_CD")), m_statuscd)) throw new LimeBizException(MsgCode.DATA_STATUS_ERROR);
                
                // Update O_CUST_ORDER_H.
                params.put("r_reqno", r_reqno);
                params.put("m_accessdevice", m_accessdevice);
                
                custOrderHDao.up(params);
                
                // Delete O_CUST_ORDER_D;
                params.put("r_reqno", r_reqno);
                custOrderDDao.del(params);
                
                // Insert O_CUST_ORDER_D;
                String[] mi_itemcd = req.getParameterValues("m_itemcd");
                String[] mi_unit = req.getParameterValues("m_unit");
                String[] mi_quantity = req.getParameterValues("m_quantity");
                params.put("m_reqno", r_reqno);
                if(!ArrayUtils.isEmpty(mi_itemcd)) {
                    for(int i=0,j=mi_itemcd.length; i<j; i++) {
                        params.put("m_lineno", (i+1)*1000);
                        params.put("m_itemcd", mi_itemcd[i]);
                        params.put("m_unit", mi_unit[i]);
                        params.put("m_quantity", mi_quantity[i].replaceAll(",", ""));
                        params.put("m_price", 0);
                        custOrderDDao.in(params);
                    }
                }
                
                m_ohhreqno = r_reqno; // 주문이력관리 저장할 주문번호 정의.
            }
        }
        // 주문접수.
        else if(StringUtils.equals("00", m_statuscd)) {
            // 주문번호 생성.
            m_reqno = this.createCustOrderReqNo(m_custcd);
            params.put("m_reqno", m_reqno);
            params.put("m_accessdevice", m_accessdevice);
            
            // 주문접수 인서트.
            if(StringUtils.equals("", r_reqno)) {
                // Insert O_CUST_ORDER_H.
                custOrderHDao.in(params); 
                
                // Insert O_CUST_ORDER_D;
                String[] mi_itemcd = req.getParameterValues("m_itemcd");
                String[] mi_unit = req.getParameterValues("m_unit");
                String[] mi_quantity = req.getParameterValues("m_quantity");
                params.put("m_reqno", m_reqno);
                if(!ArrayUtils.isEmpty(mi_itemcd)) {
                    for(int i=0,j=mi_itemcd.length; i<j; i++) {
                        params.put("m_lineno", (i+1)*1000);
                        params.put("m_itemcd", mi_itemcd[i]);
                        params.put("m_unit", mi_unit[i]);
                        params.put("m_quantity", mi_quantity[i].replaceAll(",", ""));
                        params.put("m_price", 0);
                        custOrderDDao.in(params);
                    }
                }
            }
            // 주문접수로 업데이트.
            else {
                // 상태값 체크.
                String now_status_cd = Converter.toStr(nOrder.get("STATUS_CD"));
                if(!StatusUtil.ORDER.statusCheck(now_status_cd, m_statuscd)) throw new LimeBizException(MsgCode.DATA_STATUS_ERROR);
                
                if(StringUtils.equals("00", now_status_cd)) {
                    m_reqno = r_reqno;
                }
                
                // Update O_CUST_ORDER_H.
                params.put("r_reqno", r_reqno);
                params.put("m_reqno", m_reqno);
                params.put("r_insertdt", "Y");
                custOrderHDao.up(params);
                
                // Delete O_CUST_ORDER_D;
                params.put("r_reqno", r_reqno);
                custOrderDDao.del(params);
                
                // Insert O_CUST_ORDER_D;
                String[] mi_itemcd = req.getParameterValues("m_itemcd");
                String[] mi_unit = req.getParameterValues("m_unit");
                String[] mi_quantity = req.getParameterValues("m_quantity");
                if(!ArrayUtils.isEmpty(mi_itemcd)) {
                    for(int i=0,j=mi_itemcd.length; i<j; i++) {
                        params.put("m_lineno", (i+1)*1000);
                        params.put("m_itemcd", mi_itemcd[i]);
                        params.put("m_unit", mi_unit[i]);
                        params.put("m_quantity", mi_quantity[i].replaceAll(",", ""));
                        params.put("m_price", 0);
                        custOrderDDao.in(params);
                    }
                }
                
                // 주문이력 변경 > Temp REQ_NO => Real REQ_NO 업데이트.
                if(StringUtils.equals("99", now_status_cd)) {
                    this.updateOrderHeaderHistoryForReqNoRef(m_reqno, r_reqno, r_reqno, 0L, "");
                }
            }
            
            m_ohhreqno = m_reqno; // 주문이력관리 저장할 주문번호 정의.
        }
        
        // 주문이력 저장.
        // OrderHeaderHistory 인서트.
        this.insertOrderHeaderHistory(m_ohhreqno, m_statuscd, StatusUtil.ORDER.getValue(m_statuscd), loginDto.getUserId());
        
        // 주소록 저장.
        String r_savebookmark = Converter.toStr(params.get("r_savebookmark"));
        logger.debug("주소록 저장여부 Y/N : {}", r_savebookmark);
        if(StringUtils.equals("Y", r_savebookmark)) {
            this.insertOrderAddressBookmark(loginDto.getUserId(), m_zipcd, Converter.toStr(params.get("m_add1")), Converter.toStr(params.get("m_add2"))
                , Converter.toStr(params.get("m_tel1")), Converter.toStr(params.get("m_tel2")), Converter.toStr(params.get("m_receiver")), loginDto.getUserId());
        }
        Map result = MsgCode.getResultMap(MsgCode.SUCCESS);
        result.put("m_ohhreqno", m_ohhreqno);
        return result;
    }
    
    /**
     * O_ORDER_CONFIRM 주문확정(05), 주문확정(분리)(07) 
     * O_CUST_ORDER 고객취소(01), CS반려(03)
     * @작성일 : 2020. 4. 23.
     * @작성자 : kkyu
     * @param where : admin / front
     * @param r_reqno : O_CUST_ORDER_H.REQ_NO [필수]
     * @param m_statuscd : 상태값. 03=CS전체반려, 05=주문확정, 07=주문확정(분리) [필수]
     */
    public Map<String, Object> insertOrderConfirmTransaction(Map<String, Object> params, HttpServletRequest req, LoginDto loginDto) throws Exception {
        Map<String, Object> svcMap = new HashMap<>();
        
        // 파라미터 정의.
        String where = Converter.toStr(params.get("where"));
        String r_reqno = Converter.toStr(params.get("r_reqno"));
        String m_statuscd = Converter.toStr(params.get("m_statuscd"));
        
        String m_shiptocd = Converter.toStr(params.get("m_shiptocd"), "0"); // 납품처 코드.
        String m_transty = Converter.toStr(params.get("m_transty")); // 운송수단 AA=기본운송,AB=자차운송
        String m_zipcd = Converter.toStr(params.get("m_zipcd"));
        
        // get QUOTE_QT from m_shiptocd
        Map<String, Object> shipToMap = new HashMap<>();
        shipToMap.put("r_shiptocd", m_shiptocd);
        Map<String, Object> mapQT = shipToDao.one(shipToMap);
        String m_quoteQt = "";
        if(mapQT != null)
        	m_quoteQt = Converter.toStr(mapQT.get("QUOTE_QT")==null ? "" : mapQT.get("QUOTE_QT"));   
        
        if(StringUtils.equals("AB", m_transty)) { // 운송수단이 자차운송(AB)인 경우, 우편번호 코드를 90000으로 변경.
            m_zipcd = "90000";
        }
        
        if(StringUtils.equals("", r_reqno) || StringUtils.equals("", m_statuscd)) throw new LimeBizException(MsgCode.DATA_REQUIRE_ERROR2);
        
        // Get O_CUST_ORDER_H 가져오기.
        Map<String, Object> custOrderH = null;
        if(StringUtils.equals("front", where)) {
//            custOrderH = this.getCustOrderHOne(loginDto, r_reqno);
            custOrderH = this.getCustOrderHSaveOne(loginDto, r_reqno); // 2025-01-24 hsg. 영업담당자 설정 변경으로 추가
        }
        else { // admin
//            custOrderH = this.getCustOrderHOne(r_reqno);
            custOrderH = this.getCustOrderHSaveOne(r_reqno); // 2025-01-24 hsg. 영업담당자 설정 변경으로 추가
        }
        if(CollectionUtils.isEmpty(custOrderH)) throw new LimeBizException(MsgCode.DATA_NOT_FOUND_ERROR);
        
        // 상태값 체크.
        if(!StatusUtil.ORDER.statusCheck(Converter.toStr(custOrderH.get("STATUS_CD")), m_statuscd)) throw new LimeBizException(MsgCode.DATA_STATUS_ERROR);
        
        // 샅애값 체크2 > 관리자가 주문처리 하는경우, 거래처가 주문접수상태 건들을 수정했는지 체크.
        if(StringUtils.equals("admin", where) && (StringUtils.equals("03", m_statuscd) || StringUtils.equals("05", m_statuscd) || StringUtils.equals("07", m_statuscd) )) {
            String r_ohhseq_now = Converter.toStr(params.get("r_ohhseq_now"));
            logger.debug("r_ohhseq_now : {}", r_ohhseq_now);
            if(StringUtils.equals("", r_ohhseq_now)) throw new LimeBizException(MsgCode.DATA_PROCESS_ERROR);
            
            Map<String, Object> orderHeaderHistoryRecnetOne = this.getOrderHeaderHistoryOneByRecentList(r_reqno);
            logger.debug("orderHeaderHistoryRecnetOne : {}", orderHeaderHistoryRecnetOne);
            if(CollectionUtils.isEmpty(orderHeaderHistoryRecnetOne)) throw new LimeBizException(MsgCode.DATA_PROCESS_ERROR);
            
            if(!StringUtils.equals(r_ohhseq_now, Converter.toStr(orderHeaderHistoryRecnetOne.get("OHH_SEQ")))) {
                throw new LimeBizException(MsgCode.DATA_ORDER_MODIFY);
            }
        }
        
        // 품목 파라미터 정의.
        String[] mi_itemcd = req.getParameterValues("m_itemcd"); // 품목코드 String[]
        String[] mi_companycd = req.getParameterValues("m_companycd"); // 사업장코드 String[], 주문확정시 모두 동일x >> 모두 동일하다고 해서 주문확정 상태는 아니다.
        String[] mi_unit = req.getParameterValues("m_unit"); // 단위 String[]
        String[] mi_quantity = req.getParameterValues("m_quantity"); // 출고수량 String[]
        String[] mi_pickingdt = req.getParameterValues("m_pickingdt"); // 피킹일자 String[], 주문확정시 모두 동일.
        String[] mi_requestdt2 = req.getParameterValues("m_requestdt2"); // 납품요청일자 String[], 주문확정시에는 상단의 납품요청일시를 참조한다.
        String[] mi_requesttime2 = req.getParameterValues("m_requesttime2");  // 납품요청시간 String[], 주문확정시에는 상단의 납품요청일시를 참조한다.
        String[] mi_week = req.getParameterValues("m_week"); // 주차 String[]
        String[] mi_confirmremark = req.getParameterValues("m_confirmremark"); // 요청사항 String[]
        String[] mi_price = req.getParameterValues("m_price"); // 운송비 String[], 운송비만 존재하며, 그외엔 0으로 저장한다.
        String[] mi_ocdreturncd = req.getParameterValues("m_ocdreturncd"); // 품목 반려 코드 String[], CommonCode.CC_CODE
        String[] mi_ocdreturnmsg = req.getParameterValues("m_ocdreturnmsg"); // 품목 반려 메세지 String[]
        String[] mi_ocdstatuscd = req.getParameterValues("m_ocdstatuscd"); // 품목 반려 상태값 03 String[]
        String[] mi_orderSeparation = req.getParameterValues("m_orderSeparation"); // 2025-02-11 hsg Ankle Rock. 주문분리일 경우 오더번호를 생성하기 위해 주문분리 구분자 추가. 구분자 : 부모행 번호(trId_x)[예)trId_0]
        try {
            logger.debug("### mi_itemcd length : {}", mi_itemcd.length);
            logger.debug("### mi_companycd length : {}", mi_companycd.length);
            logger.debug("### mi_unit length : {}", mi_unit.length);
            logger.debug("### mi_quantity length : {}", mi_quantity.length);
            logger.debug("### mi_pickingdt length : {}", mi_pickingdt.length);
            logger.debug("### mi_requestdt2 length : {}", mi_requestdt2.length);
            logger.debug("### mi_requesttime2 length : {}", mi_requesttime2.length);
            logger.debug("### mi_week length : {}", mi_week.length);
            logger.debug("### mi_confirmremark length : {}", mi_confirmremark.length);
            logger.debug("### mi_price length : {}", mi_price.length);
            logger.debug("### mi_ocdreturncd length : {}", mi_ocdreturncd.length);
            logger.debug("### mi_ocdreturnmsg length : {}", mi_ocdreturnmsg.length);
            logger.debug("### mi_ocdstatuscd length : {}", mi_ocdstatuscd.length);
            logger.debug("### mi_orderSeparation length : {}", mi_orderSeparation.length); // 2025-02-11 hsg Ankle Rock. 주문분리일 경우 오더번호를 생성하기 위해 주문분리 구분자 추가. 구분자 : 부모행 번호(trId_x)[예)trId_0]
        }catch(Exception e) {
            
        }
        
        // 상태별 주문처리.
        if(StringUtils.equals("01", m_statuscd)) { // 고객취소(전체).
            // O_CUST_ORDER_H 상태변경.
            svcMap.put("r_reqno", r_reqno);
            svcMap.put("m_statuscd", m_statuscd);
            //svcMap.put("m_returndesc", params.get("m_returndesc")); // 반려사유.
            //svcMap.put("m_returncd", params.get("m_returncd")); // 반려사유코드.
            svcMap.put("m_canceldt", "Y"); // 취소반려일시.
            svcMap.put("m_updateid", loginDto.getUserId());
            custOrderHDao.up(svcMap);
            svcMap.clear();
        }
        else if(StringUtils.equals("03", m_statuscd)) { // CS전체반려.
            // O_CUST_ORDER_H 상태변경.
            svcMap.put("r_reqno", r_reqno);
            svcMap.put("m_statuscd", m_statuscd);
            svcMap.put("m_returndesc", params.get("m_returndesc")); // 반려사유.
            svcMap.put("m_returncd", params.get("m_returncd")); // 반려사유코드.
            svcMap.put("m_canceldt", "Y"); // 취소반려일시.
            svcMap.put("m_updateid", loginDto.getUserId());
            custOrderHDao.up(svcMap);
            svcMap.clear();
           
            
            // O_ORDER_CONFIRM_H 인서트.
            svcMap.put("m_reqno", r_reqno);
            svcMap.put("m_custpo", r_reqno);
            svcMap.put("m_custcd", custOrderH.get("CUST_CD"));
            svcMap.put("m_userid", custOrderH.get("USERID"));
            svcMap.put("m_salesrepcd", Converter.toInt(custOrderH.get("SALESREP_CD")));// 2015-01-17 hsg Fisherman Suplex. 거래처 담당자가 주기적으로 바뀌기에 당시에 있었던 오더들에 대해서는 담당자가 저장 되어야 합니다.라는 요청에 의해 거래처 담당자를 설정
            svcMap.put("m_salesrepnm", custOrderH.get("SALESREP_NM"));// 2015-01-17 hsg Fisherman Suplex. 거래처 담당자가 주기적으로 바뀌기에 당시에 있었던 오더들에 대해서는 담당자가 저장 되어야 합니다.라는 요청에 의해 거래처 담당자를 설정
            svcMap.put("m_statuscd", m_statuscd);
            svcMap.put("m_zipcd", m_zipcd);
            svcMap.put("m_add1", params.get("m_add1"));
            svcMap.put("m_add2", params.get("m_add2"));
            
            svcMap.put("m_requestdt", params.get("m_requestdt")); // 주문확정인 경우는 상단의 납품요청일시를 참조한다.
            svcMap.put("m_requesttime", params.get("m_requesttime")); // 주문확정인 경우는 상단의 납품요청일시를 가져와서 저장한다.
            svcMap.put("m_companycd", Converter.toStr(mi_companycd[0])); // 주문확정 기준이 사업장이 모두 동일한 경우이므로 첫번째 품목의 사업장을 가져온다.
            
            svcMap.put("m_tel1", custOrderH.get("TEL1"));
            svcMap.put("m_tel2", custOrderH.get("TEL2"));
            svcMap.put("m_shiptocd", m_shiptocd);
            svcMap.put("m_transty", m_transty);
            
            SimpleDateFormat formt = new SimpleDateFormat("yyyyMMdd");
            svcMap.put("m_pickingdt", Converter.toStr(mi_pickingdt[0]).replaceAll("-", ""));
            svcMap.put("m_canceldt", formt.format(new Date())); 
            svcMap.put("m_receiver", custOrderH.get("RECEIVER"));
            svcMap.put("m_confirmid", loginDto.getUserId());
            svcMap.put("m_custommatter", params.get("m_custommatter")); // 거래처 주의사항
            svcMap.put("m_insertid", loginDto.getUserId());


            // 확정 요청사항 정의. 2020-05-28 By Hong 추가.
            String m_remark = Converter.toStr(params.get("m_remark")).trim();
            if(StringUtils.equals("", m_remark)) {
                for(String confirmRemark : mi_confirmremark) {
                    if(!StringUtils.equals("", confirmRemark.trim())) {
                        m_remark = confirmRemark;
                        break;
                    }
                }
            }
            svcMap.put("m_remark", m_remark); // 요청사항
            svcMap.put("m_quoteQt", m_quoteQt);
            
            orderConfirmHDao.in(svcMap);
            svcMap.clear();
            
            // O_ORDER_CONFIRM_D 인서트.
            for(int i=0,j=mi_itemcd.length; i<j; i++) {
                int m_lineno = (i+1)*1000;
                String m_price = "0";
                if(itemSvc.deliveryItemTf(Converter.toStr(mi_itemcd[i]))) { // 운송비 품목이면
                    m_price = Converter.toStr(mi_price[i], "0").replaceAll(",", "");
                }
                
                svcMap.put("m_custpo", r_reqno);
                svcMap.put("m_lineno", m_lineno);
                svcMap.put("m_itemcd", mi_itemcd[i]);
                svcMap.put("m_unit", mi_unit[i]);
                svcMap.put("m_quantity", Converter.toStr(mi_quantity[i], "0").replaceAll(",", ""));
                svcMap.put("m_price", m_price);
                svcMap.put("m_insertid", loginDto.getUserId());
                svcMap.put("m_dummy", "");
                svcMap.put("m_week", mi_week[i]);
                
                svcMap.put("m_ocdreturncd", params.get("m_returncd"));
                svcMap.put("m_ocdreturnmsg", params.get("m_returndesc"));
                svcMap.put("m_ocdstatuscd", "03");
                
                orderConfirmDDao.in(svcMap);
                svcMap.clear();
            }
        }
        else if( StringUtils.equals("05", m_statuscd) ) { // 주문확정.
            // 납품처 정보 가져오기 > 거래처 주의사항 저장을 위한.
            /*
            String m_custommatter = "";
            if(StringUtils.equals("0", m_shiptocd)) {
                Map<String, Object> shipToMap = customerSvc.getShipTo(m_shiptocd);
                m_custommatter = Converter.toStr(shipToMap.get("ADD4")); // TO_BE에서는 ADD2 => ADD4 개선요청.
            }
            */
            
            // O_ORDER_CONFIRM_H 인서트.
            svcMap.put("m_reqno", r_reqno);
            svcMap.put("m_custpo", r_reqno);
            svcMap.put("m_custcd", custOrderH.get("CUST_CD"));
            svcMap.put("m_userid", custOrderH.get("USERID"));
            svcMap.put("m_salesrepcd", Converter.toInt(custOrderH.get("SALESREP_CD")));// 2015-01-17 hsg Fisherman Suplex. 거래처 담당자가 주기적으로 바뀌기에 당시에 있었던 오더들에 대해서는 담당자가 저장 되어야 합니다.라는 요청에 의해 거래처 담당자를 설정
            svcMap.put("m_salesrepnm", custOrderH.get("SALESREP_NM"));// 2015-01-17 hsg Fisherman Suplex. 거래처 담당자가 주기적으로 바뀌기에 당시에 있었던 오더들에 대해서는 담당자가 저장 되어야 합니다.라는 요청에 의해 거래처 담당자를 설정
            svcMap.put("m_statuscd", m_statuscd);
            svcMap.put("m_zipcd", m_zipcd);
            svcMap.put("m_add1", params.get("m_add1"));
            svcMap.put("m_add2", params.get("m_add2"));
            
            svcMap.put("m_requestdt", params.get("m_requestdt")); // 주문확정인 경우는 상단의 납품요청일시를 참조한다.
            svcMap.put("m_requesttime", params.get("m_requesttime")); // 주문확정인 경우는 상단의 납품요청일시를 가져와서 저장한다.
            svcMap.put("m_companycd", Converter.toStr(mi_companycd[0])); // 주문확정 기준이 사업장이 모두 동일한 경우이므로 첫번째 품목의 사업장을 가져온다.
            
            svcMap.put("m_tel1", custOrderH.get("TEL1"));
            svcMap.put("m_tel2", custOrderH.get("TEL2"));
            svcMap.put("m_shiptocd", m_shiptocd);
            svcMap.put("m_transty", m_transty);
            
            svcMap.put("m_pickingdt", Converter.toStr(mi_pickingdt[0]).replaceAll("-", ""));
            svcMap.put("m_receiver", custOrderH.get("RECEIVER"));
            svcMap.put("m_confirmid", loginDto.getUserId());
            svcMap.put("m_custommatter", params.get("m_custommatter")); // 거래처 주의사항
            svcMap.put("m_insertid", loginDto.getUserId());
            
            // 확정 요청사항 정의. 2020-05-28 By Hong 추가.
            String m_remark = Converter.toStr(params.get("m_remark")).trim();
            if(StringUtils.equals("", m_remark)) {
                for(String confirmRemark : mi_confirmremark) {
                    if(!StringUtils.equals("", confirmRemark.trim())) {
                        m_remark = confirmRemark;
                        break;
                    }
                }
            }
            svcMap.put("m_remark", m_remark); // 요청사항
            svcMap.put("m_quoteQt", m_quoteQt);
            
            orderConfirmHDao.in(svcMap);
            svcMap.clear();
            
            // O_ORDER_CONFIRM_D 인서트.
            for(int i=0,j=mi_itemcd.length; i<j; i++) {
                int m_lineno = (i+1)*1000;
                String m_price = "0";
                if(itemSvc.deliveryItemTf(Converter.toStr(mi_itemcd[i]))) { // 운송비 품목이면
                    m_price = Converter.toStr(mi_price[i], "0").replaceAll(",", "");
                }
                
                svcMap.put("m_custpo", r_reqno);
                svcMap.put("m_lineno", m_lineno);
                svcMap.put("m_itemcd", mi_itemcd[i]);
                svcMap.put("m_unit", mi_unit[i]);
                svcMap.put("m_quantity", Converter.toStr(mi_quantity[i], "0").replaceAll(",", ""));
                svcMap.put("m_price", m_price);
                svcMap.put("m_insertid", loginDto.getUserId());
                svcMap.put("m_dummy", "");
                svcMap.put("m_week", mi_week[i]);
                
                svcMap.put("m_ocdreturncd", mi_ocdreturncd[i]);
                svcMap.put("m_ocdreturnmsg", mi_ocdreturnmsg[i]);
                svcMap.put("m_ocdstatuscd", mi_ocdstatuscd[i]);
                
                orderConfirmDDao.in(svcMap);
                svcMap.clear();
            }
            

        	// O_CUST_ORDER_H 상태변경 > 주문확정 05로 상태값 변경.
        	svcMap.put("r_reqno", r_reqno);
            svcMap.put("m_statuscd", m_statuscd);
            svcMap.put("m_updateid", loginDto.getUserId());
            custOrderHDao.up(svcMap);
            svcMap.clear();
        }
        else if(StringUtils.equals("07", m_statuscd)) { // 주문확정(분리). >>> 출고지 별로 인서트
            List<Map<String, Object>> ccList = new ArrayList<>(); // O_ORDER_CONFIRM_H 리스트.
            
            /**
             * 2020.06.03 By Hong 체크로직 추가.
             * # 기존에는 출고지/납품요청일시/피킹일자가 동일한 경우 동일한 주문번호로 묶었다.
             * # 체크로직이 추가된 부분은 출고지/납품요청일시/피킹일자 뿐 아니라 +품목코드까지 동일한 경우가 한 건이라도 있는경우, 모두 각각의 주문번호를 생성(운송비 제외)
             * # 즉, 운송비를 제외한 모든 행 별로 주문번호가 생기는 것이다.
             * # 단, 운송비는 원오더이든, 분리오더이든 간에 부모오더에 종속되어서 품목(Detail)에 추가 되어져야 한다.
             */
            /**
             * 2025.02.13 By hsg. Ankle Rock.  체크로직 변경경.
             * # 주문번호 생성 시 주문분리가 된 경우와 출고지가 다른 경우만 체크.
             * # 주문분리된 경우 주문분리된 모든 주문에 주문번호를 부여.
             * # 이외의 경우엔 출고지가 다른 경우만 주문번호를 부여하고 품목이나 주문일시 등 기존에 적용했던 조건들은 무시.
             * # 운송비에 대한 내용은 기존 로직을 그대로 적용.
             * # 주문분리 체크는 기존 로직을 이용하기 까다롭고 어려워, 주문분리 처리 로직과 기존로직 부분으로 따로 구현한 다음 주문번호 부여시 하나로 묶음.
             */

            String companyCds = "";
            boolean forceDivision = false;
            
            String companyCdPosNos = "";
            boolean isNotAdd = true;

            
            logger.debug("####### mi_orderSeparation : {}", mi_orderSeparation);

            List<Map<String, Object>> mainList = new ArrayList<>(); // O_ORDER_CONFIRM_H 리스트.
            Map<String, Object> detailList = new HashMap<>();

            for( int i = 0, j = mi_companycd.length ; i < j ; i++ ) {
            	if( !itemSvc.deliveryItemTf(mi_itemcd[i]) ) { // 운송비 제외

            		if( i == 0) {
            			companyCdPosNos += i + "||,||";
            			logger.debug("####### companyCdPosNos first : {}", companyCdPosNos);
            		} else {

            			if ( 1 < countInArray(mi_orderSeparation, mi_orderSeparation[i]) ) { // 주문분리일 경우 
                			companyCdPosNos  += i + "||,||";
                			forceDivision = true;
                			logger.debug("####### companyCdPosNos separation : {}", companyCdPosNos);
                			continue;
                    	} else {
                    		for( int a = i - 1 ; 0 <= a ; a-- ) {
                    			if ( 2 > countInArray(mi_orderSeparation, mi_orderSeparation[a]) ) { // 주문분리가 아닐 경우 

                    				if( mi_companycd[i].equals(mi_companycd[a]) ) { // 출고지가 같은 경우
                    					companyCdPosNos += a + "||,||";
                    					isNotAdd = false;
                    					forceDivision = true;
                    					logger.debug("####### companyCdPosNos duplication : {}", companyCdPosNos);
                    					break;
                    				}

                    			}
                    		}
                    		
                    		if( isNotAdd )
                    			companyCdPosNos += i + "||,||";
                    		else
                    			isNotAdd = true;
                    	}

            		}

            	}
            }

            logger.debug("####### companyCdPosNos : {}", companyCdPosNos);
            logger.debug("####### forceDivision : {}", forceDivision);

            ccList = new ArrayList<>();

            if( forceDivision ) {

                companyCds = companyCdPosNos.substring(0, companyCdPosNos.length()-5);
                logger.debug("####### companyCds : {}", companyCds);

                String [] companyCdArr = companyCds.split("\\|\\|,\\|\\|", -1);
                logger.debug("companyCdArr.length : {}", companyCdArr.length);
                logger.debug("companyCdArr : {}", Arrays.toString(companyCdArr));

//                Arrays.sort(companyCdArr);
//                logger.debug("companyCdArr sort : {}", Arrays.toString(companyCdArr));


                for (int i = 0, j = companyCdArr.length ; i < j ; i++ ) {
                	Map<String, Object> one = new HashMap<>();
                	String idxs = "";

                	if( i == 0 ) {
                		idxs = i + ",";
                	} else {
                		if( companyCdArr[i].equals(companyCdArr[i-1]) ) {
                			idxs += i+",";
                		}
                	}


                	logger.debug("idxs : {}", idxs);
                    
                    one.put("company_cd", mi_companycd[i]);
                    one.put("idxs", idxs.substring(0, idxs.length()-1));
                    ccList.add(one);

                }



            }
            // 2. 기존 로직.
            else {
                // 출고지 별로 재배치 처리. >>> 수정 >>> 출고지+납품일자+납품시간+피킹일자 별로 재배치 처리
//                String companyCds = ""; // 중복을 제거시킨 출고지코드를 ,로 구분
//                for(int i=0,j=mi_companycd.length; i<j; i++) {
//                    if(!itemSvc.deliveryItemTf(mi_itemcd[i])) { // 운송비 제외.
//                        if(0==i) {
//                            companyCds = "||"+mi_companycd[i]+"||D||"+mi_requestdt2[i]+"||D||"+mi_requesttime2[i]+"||D||"+mi_pickingdt[i]+"||,||";
//                        }else{
//                            if(0 > companyCds.indexOf("||"+mi_companycd[i]+"||D||"+mi_requestdt2[i]+"||D||"+mi_requesttime2[i]+"||D||"+mi_pickingdt[i]+"||,||")) {
//                                companyCds += mi_companycd[i]+"||D||"+mi_requestdt2[i]+"||D||"+mi_requesttime2[i]+"||D||"+mi_pickingdt[i]+"||,||";
//                            }
//                        }
//                    }
//                }
//                companyCds = companyCds.substring(2, companyCds.length()-5);
//                logger.debug("companyCds : {}", companyCds);
//                String [] companyCdArr = companyCds.split("\\|\\|,\\|\\|", -1);
//                logger.debug("companyCdArr.length : {}", companyCdArr.length);
//                
//                ccList = new ArrayList<>(); // 출고지코드별 리스트.
//                for(int x=0,y=companyCdArr.length; x<y; x++) {
//                    Map<String, Object> one = new HashMap<>();
//                    String idxs = "";
//                    for(int i=0,j=mi_companycd.length; i<j; i++) {
//                        if(!itemSvc.deliveryItemTf(mi_itemcd[i])) { // 운송비가 아니라면,
//                            if(StringUtils.equals(companyCdArr[x], mi_companycd[i]+"||D||"+mi_requestdt2[i]+"||D||"+mi_requesttime2[i]+"||D||"+mi_pickingdt[i])) {
//                                idxs += i+",";
//                                
//                                // 다음 건들이 운송비라면... idxs 추가...
//                                for(int a=i+1,b=j; a<b; a++) {
//                                    if(itemSvc.deliveryItemTf(mi_itemcd[a])) {
//                                        idxs += a+",";
//                                    }else {
//                                        break;
//                                    }
//                                }
//                            }
//                        }
//                    }
//                    
//                    one.put("company_cd", (companyCdArr[x]).split("\\|\\|D\\|\\|")[0]);
//                    //one.put("company_cd", companyCdArr[x]);
//                    one.put("idxs", idxs.substring(0, idxs.length()-1));
//                    ccList.add(one);
//                }
            }
            logger.debug("ccList : {}", ccList);
            
            // DB작업
//            int fi = 0;
//            for(Map<String, Object> one : ccList) {
//                String companyCd = Converter.toStr(one.get("company_cd"));
//                String idxs = Converter.toStr(one.get("idxs"));
//                String[] idxArr = idxs.split(",", -1);
//                int firstIdx = Converter.toInt(idxArr[0]);
//                logger.debug("firstIdx : {}", firstIdx);
//                
//                String m_custpo = r_reqno+"-"+Converter.toStr(fi+1);
//                
//                // O_ORDER_CONFIRM_H 인서트.
//                svcMap.put("m_reqno", r_reqno);
//                svcMap.put("m_custpo", m_custpo);
//                svcMap.put("m_custcd", custOrderH.get("CUST_CD"));
//                svcMap.put("m_userid", custOrderH.get("USERID"));
//                svcMap.put("m_salesrepcd", Converter.toInt(custOrderH.get("SALESREP_CD")));// 2015-01-17 hsg Fisherman Suplex. 거래처 담당자가 주기적으로 바뀌기에 당시에 있었던 오더들에 대해서는 담당자가 저장 되어야 합니다.라는 요청에 의해 거래처 담당자를 설정
//                svcMap.put("m_salesrepnm", custOrderH.get("SALESREP_NM"));// 2015-01-17 hsg Fisherman Suplex. 거래처 담당자가 주기적으로 바뀌기에 당시에 있었던 오더들에 대해서는 담당자가 저장 되어야 합니다.라는 요청에 의해 거래처 담당자를 설정
//                svcMap.put("m_statuscd", m_statuscd);
//                svcMap.put("m_zipcd", m_zipcd);
//                svcMap.put("m_add1", params.get("m_add1"));
//                svcMap.put("m_add2", params.get("m_add2"));
//                
//                svcMap.put("m_requestdt", mi_requestdt2[firstIdx]);
//                svcMap.put("m_requesttime", mi_requesttime2[firstIdx]);
//                svcMap.put("m_companycd", companyCd); 
//                
//                svcMap.put("m_tel1", custOrderH.get("TEL1"));
//                svcMap.put("m_tel2", custOrderH.get("TEL2"));
//                //svcMap.put("m_buildingty", params.get(""));
//                svcMap.put("m_shiptocd", m_shiptocd);
//                svcMap.put("m_transty", m_transty);
//                
//                //svcMap.put("m_confirmdt", params.get("")); // 확정일자로 현재날짜 저장.
//                svcMap.put("m_pickingdt", Converter.toStr(mi_pickingdt[firstIdx]).replaceAll("-", ""));
//                svcMap.put("m_receiver", custOrderH.get("RECEIVER"));
//                svcMap.put("m_confirmid", loginDto.getUserId());
//                //svcMap.put("m_canceldt", params.get(""));
//                svcMap.put("m_custommatter", params.get("m_custommatter")); // 거래처 주의사항
//                //svcMap.put("m_dummy", params.get(""));
//                //svcMap.put("m_interfacedt", params.get("")); // JDE반영일시
//                //svcMap.put("m_insertdt", params.get(""));
//                svcMap.put("m_insertid", loginDto.getUserId());
//                //svcMap.put("m_updatedt", params.get(""));
//                //svcMap.put("m_updateid", params.get(""));
//                
//                // 확정 요청사항 정의. 2020-05-28 By Hong 추가.
//                String m_remark = Converter.toStr(params.get("m_remark")).trim();
//                //if(StringUtils.equals("", m_remark)) {
//                for(String idxOne : idxArr) {
//                    int io = Converter.toInt(idxOne);
//                    if(!StringUtils.equals("", mi_confirmremark[io].trim())){
//                        m_remark = mi_confirmremark[io];
//                        
//                    }
//                }
//                //}
//                svcMap.put("m_remark", m_remark); // 요청사항
//                logger.debug("### 주문(분리)확정 > m_custpo : {}, m_remark : {}", m_custpo, m_remark);
//                svcMap.put("m_quoteQt", m_quoteQt);
//                
//                orderConfirmHDao.in(svcMap);
//                svcMap.clear();
//                
//                // O_ORDER_CONFIRM_D 인서트.
//                for(int i=0,j=idxArr.length; i<j; i++) {
//                    int dIdx = Converter.toInt(idxArr[i]);
//                    int m_lineno = (i+1)*1000;
//                    String m_price = "0";
//                    if(itemSvc.deliveryItemTf(Converter.toStr(mi_itemcd[dIdx]))) { // 운송비 품목이면
//                        m_price = Converter.toStr(mi_price[dIdx], "0").replaceAll(",", "");
//                    }
//                    
//                    svcMap.put("m_custpo", m_custpo);
//                    svcMap.put("m_lineno", m_lineno);
//                    svcMap.put("m_itemcd", mi_itemcd[dIdx]);
//                    svcMap.put("m_unit", mi_unit[dIdx]);
//                    svcMap.put("m_quantity", Converter.toStr(mi_quantity[dIdx], "0").replaceAll(",", ""));
//                    svcMap.put("m_price", m_price);
//                    //svcMap.put("m_insertdt", "");
//                    svcMap.put("m_insertid", loginDto.getUserId());
//                    //svcMap.put("m_updatedt", "");
//                    //svcMap.put("m_upateid", "");
//                    svcMap.put("m_dummy", "");
//                    svcMap.put("m_week", mi_week[dIdx]);
//                    svcMap.put("m_ocdreturncd", mi_ocdreturncd[dIdx]);
//                    svcMap.put("m_ocdreturnmsg", mi_ocdreturnmsg[dIdx]);
//                    svcMap.put("m_ocdstatuscd", mi_ocdstatuscd[dIdx]);
//                    
//                    orderConfirmDDao.in(svcMap);
//                    svcMap.clear();
//                }
//                
//                fi++;
//            }
//            
//            // O_CUST_ORDER_H 상태변경 > 주문확정 07로 상태값 변경.
//            svcMap.put("m_statuscd", m_statuscd);
//            svcMap.put("m_updateid", loginDto.getUserId());
//            svcMap.put("r_reqno", r_reqno);
//            custOrderHDao.up(svcMap);
//            svcMap.clear();
//        }
//        
//        // 주문이력 저장.
//        // OrderHeaderHistory 인서트.
//        this.insertOrderHeaderHistory(r_reqno, m_statuscd, StatusUtil.ORDER.getValue(m_statuscd), loginDto.getUserId());
//        
//        // APP PUSH 발송. => 주문확정 또는 주문확정(분리)
//        if(StringUtils.equals("05", m_statuscd) || StringUtils.equals("07", m_statuscd)) {
//            // 푸시 발송 대상자 리스트 가져오기.
//            String sendUserId = Converter.toStr(custOrderH.get("USERID")).replaceAll(" ", "");;
//            svcMap.put("r_userid", sendUserId);
//            svcMap.put("r_userapppushyn1", "Y"); // 주문확정 푸시 수신여부=Y인 사용자들만.
//            List<Map<String, Object>> userList = userDao.listForAppPush(svcMap);
//            svcMap.clear();
//            
//            if(!userList.isEmpty()) {
//                List<String> ri_userid = new ArrayList<>(); // 푸시 보낼 사용지 아이디 리스트.
//                List<String> pushKeyList = new ArrayList<>(); // 푸시 보낼 사용자의 푸시키 리스트.
//                
//                for(Map<String, Object> user : userList) {
//                    String userid = Converter.toStr(user.get("USERID"));
//                    String user_apppushkey = Converter.toStr(user.get("USER_APPPUSHKEY"));
//                    // PUSH_KEY가 있는 회원들만 저장 후 발송.
//                    if(!StringUtils.equals("", user_apppushkey)) {
//                        ri_userid.add(userid);
//                        pushKeyList.add(user_apppushkey);
//                    }
//                }
//                
//                // 푸시 설정.
//                String title = "USG BORAL e-Ordering";
//                String contents = "";
//                String move_url = httpsUrl + req.getServerName() + (req.getServerPort() == 80 ? "" : ":" + Converter.toStr(req.getServerPort())) +  req.getContextPath();
//                move_url += "/front/order/orderList.lime";
//                
//                // 내용 가지고 오기.
//                contents = configSvc.getConfigValue("PUSH1");
//                String msg1 = "주문번호:"+r_reqno;
//                String msg2 = StringUtils.equals("05", m_statuscd) ? "주문확정" : "주문확정(분리)";
//                contents = contents.replace("#MSG1#", msg1+"가 "+msg2);
//                //contents = contents.replace("#MSG1#", StringUtils.equals("05", m_statuscd) ? "주문확정" : "주문확정(분리)");
//                logger.debug("주문확정 푸시 내용 : {}", contents);
//                
//                if(!ri_userid.isEmpty()) {
//                    // 푸시 테이블 저장.
//                    svcMap.put("ri_userid", ri_userid);
//                    svcMap.put("m_aptitle", title);
//                    svcMap.put("m_apcontent", contents);
//                    svcMap.put("m_apsendyn", "Y"); // 푸시 발송여부 Y/N
//                    svcMap.put("m_apsendtype", "1"); // 1=바로 전송, 2=스케쥴러 전송
//                    svcMap.put("m_apmoveurl", move_url); // 푸시 클릭시 이동할 URL
//                    svcMap.put("m_aptype", "1"); // 1=주문확정,2=배차완료,3=이벤트최초등록
//                    
//                    appPushDao.inByUser(svcMap);
//                    svcMap.clear();
//                    
//                    // 푸시 발송.
//                    AppPushUtil appPush = new AppPushUtil();
//                    appPush.sendAppPush(title, contents, pushKeyList, move_url);
//                }
//            }
        }
        
        return MsgCode.getResultMap(MsgCode.SUCCESS);
    }
    
    /**
     * 임시저장 주문건 삭제.
     * @작성일 : 2020. 4. 27.
     * @작성자 : kkyu
     * @param r_reqno [필수]
     * @param m_statuscd [필수] 변경할 상태값 : 90=임시저장 삭제
     */
    public Map<String, Object> deleteTempSaveTransaction(Map<String, Object> params, HttpServletRequest req, LoginDto loginDto) throws Exception {
        // 파라미터 정의.
        String where = Converter.toStr(params.get("where"));
        String r_reqno = Converter.toStr(params.get("r_reqno"));
        String m_statuscd = Converter.toStr(params.get("m_statuscd"));
        
        if(StringUtils.equals("", r_reqno) || StringUtils.equals("", m_statuscd)) throw new LimeBizException(MsgCode.DATA_REQUIRE_ERROR2); // 컨트롤러에서 체크.
        
        // Get O_CUST_ORDER_H 가져오기.
        Map<String, Object> custOrderH = null;
        if(StringUtils.equals("front", where)) {
            custOrderH = this.getCustOrderHOne(loginDto, r_reqno);
        }
        else { // admin
            custOrderH = this.getCustOrderHOne(r_reqno);
        }
        if(CollectionUtils.isEmpty(custOrderH)) throw new LimeBizException(MsgCode.DATA_NOT_FOUND_ERROR);
        
        // 상태값 체크.
        logger.debug("preStatus : {}", custOrderH.get("STATUS_CD"));
        logger.debug("nextStatus : {}", m_statuscd);
        if(!StatusUtil.ORDER.statusCheck(Converter.toStr(custOrderH.get("STATUS_CD")), m_statuscd)) throw new LimeBizException(MsgCode.DATA_STATUS_ERROR);
        
        // 주문건 삭제.
        custOrderHDao.del(params);
        custOrderDDao.del(params);
        
        // 주문이력 저장.
        // OrderHeaderHistory 인서트.
        this.insertOrderHeaderHistory(r_reqno, m_statuscd, StatusUtil.ORDER.getValue(m_statuscd), loginDto.getUserId());
        
        return MsgCode.getResultMap(MsgCode.SUCCESS);
    }
    
    
    /**
     * 전체주문현황 리스트 가져오기. O_SALESORDER.
     * @작성일 : 2020. 4. 24.
     * @작성자 : kkyu
     * @param where : admin=관리자, excel=관리자 엑셀, front=거래처,납품처, frontexcel=거래처,납품처 엑셀.
     */
    public Map<String, Object> getSalesOrderList(Map<String, Object> params, HttpServletRequest req, LoginDto loginDto) throws Exception {
        Map<String, Object> resMap = new HashMap<>();
        
        String where = Converter.toStr(params.get("where"));
        
        String r_ordersdt = Converter.toStr(params.get("r_ordersdt")); // 주문일자 검색 시작일.
        String r_orderedt = Converter.toStr(params.get("r_orderedt")); // 주문일자 검색 종료일.
        String r_actualshipsdt = Converter.toStr(params.get("r_actualshipsdt")); // 출고일자 검색 시작일.
        String r_actualshipedt = Converter.toStr(params.get("r_actualshipedt")); // 출고일자; 검색 종료일.
        String r_requestsdt = Converter.toStr(params.get("r_requestsdt")); // 납품요청일 검색 시작일.
        String r_requestedt = Converter.toStr(params.get("r_requestedt")); // 납품요청일; 검색 종료일.
        if(!StringUtils.equals("", r_ordersdt)) r_ordersdt = r_ordersdt.replaceAll("-", "");
        if(!StringUtils.equals("", r_orderedt)) r_orderedt = r_orderedt.replaceAll("-", "");
        if(!StringUtils.equals("", r_actualshipsdt)) r_actualshipsdt = r_actualshipsdt.replaceAll("-", "");
        if(!StringUtils.equals("", r_actualshipedt)) r_actualshipedt = r_actualshipedt.replaceAll("-", "");
        if(!StringUtils.equals("", r_requestsdt)) r_requestsdt = r_requestsdt.replaceAll("-", "");
        if(!StringUtils.equals("", r_requestedt)) r_requestedt = r_requestedt.replaceAll("-", "");
        params.put("r_ordersdt", r_ordersdt);
        params.put("r_orderedt", r_orderedt);
        params.put("r_actualshipsdt", r_actualshipsdt);
        params.put("r_actualshipedt", r_actualshipedt);
        params.put("r_requestsdt", r_requestsdt);
        params.put("r_requestedt", r_requestedt);
        
        // 상태값 검색조건 재정의.
        String statuss1 = "";
        String wherebody_status = "";
        String ri_status2 = Converter.toStr(params.get("ri_status2")); // 상태값으로  ,로 구분.
        boolean bShip = false;
        boolean bDeliv = false;
        boolean bCard = false;
        int cntDType = 0;
        if(!StringUtils.equals("", ri_status2)) {
            String[] ri_status2arr = ri_status2.split(",", -1);
            //String today = Converter.dateToStr("yyyyMMdd");
            //String statuss2 = ""; // for 출하완료
            //String statuss3 = ""; // for 배송완료   
            cntDType = ri_status2arr.length;
            if( (cntDType == 1) && (ri_status2arr[0].equalsIgnoreCase("525")) ) {
            	bCard = true;
            } else {
	            for(String status2 : ri_status2arr) {
	                if(560 > Integer.parseInt(status2)) {
	                    if(StringUtils.equals("", statuss1)) {
	                        statuss1 += status2;
	                    }else {
	                        statuss1 += ","+status2;
	                    }
	                }
	                // 출하완료 560.
	                // >>> 출하완료는 텍스트로 검색한다.
	                /*else if(560 == Integer.parseInt(status2)){ 
	                    statuss2 += "(STATUS_DESC = '출하완료')";
	                    //statuss2 += "(STATUS2 >= "+status2+" AND REQUEST_DT <= "+today+")";
	                }
	                // 배송완료 800 > 800은 정의되지 않은 상태값으로 임의로 정의한값이다. > 출하완료 560 이상 & 날짜(요청일자) 지남 > 보랄측에서 새롭게 정의한 사항.
	                // >>> 다시 요청하기로... 배송완료는 텍스르로 검색한다.
	                else { 
	                    statuss3 += "(STATUS_DESC = '배송완료')";
	                    //statuss3 += "(STATUS2 >= "+status2+" AND REQUEST_DT > "+today+")";
	                }*/
	                else {     
	                	if(!statuss1.contains("560")) {
	                    	if(!StringUtils.equals("", statuss1)) {
	                            statuss1 += ",";
	                        }
	                    	
	                		statuss1 += "560,580,620";
	                	}
	                	
	                	if(status2.contains("560")) {
	                		bShip = true;
	                	} else if(status2.contains("800")) {
	                		bDeliv = true;
	                	}
	                }
	            }
            }
        }
        
        if(StringUtils.equals("", statuss1)) {
        	if( bCard ) {
        		wherebody_status = "AND (SO.HOLD_CODE = 'C1' AND SO.STATUS2 <> '999')";
        	} else {
        		wherebody_status += "NOT (SO.STATUS1 = '980')";
        		//wherebody_status += "AND SO.STATUS2 = '0'";
        	}
        } else {
        	wherebody_status += "SO.STATUS2 IN ("+statuss1+")";
        	if( (cntDType==1) && bShip ) {
        	//  2024-10-21 hsg MS-SQL에서 TO_CHAR 함수를 사용할 수 없어 FORMAT으로 변경
//        		wherebody_status += " AND ((SO.STATUS1=580 AND SO.STATUS2=620) AND SO.REQUEST_DT >= TO_CHAR(SYSDATE, 'yyyymmdd'))";
        		wherebody_status += " AND ((SO.STATUS1=580 AND SO.STATUS2=620) AND SO.REQUEST_DT >= FORMAT(GETDATE(), 'yyyymmdd'))";
        	} else if( (cntDType==1) && bDeliv ) {
        	//  2024-10-21 hsg MS-SQL에서 TO_CHAR 함수를 사용할 수 없어 FORMAT으로 변경
//        		wherebody_status += " AND ((SO.STATUS1=580 AND SO.STATUS2=620) AND SO.REQUEST_DT < TO_CHAR(SYSDATE, 'yyyymmdd'))";
        		wherebody_status += " AND ((SO.STATUS1=580 AND SO.STATUS2=620) AND SO.REQUEST_DT < FORMAT(GETDATE(), 'yyyymmdd'))";
        	}
        }
        
        wherebody_status += " AND SO.ORDERTY <> 'KL'";

        System.out.println("wherebody_status : s" +  wherebody_status);
        params.put("wherebody_status", wherebody_status);

        int totalCnt = salesOrderDao.cnt(params);

        Pager pager = new Pager();
        pager.gridSetInfo(totalCnt, params, req);
        resMap.put("total", Converter.toInt(params.get("totpage")));
        resMap.put("listTotalCount", totalCnt);
        
        // Start. Define Only For Form-Paging.
        resMap.put("startnumber", params.get("startnumber"));
        resMap.put("r_page", params.get("r_page"));
        resMap.put("startpage", params.get("startpage"));
        resMap.put("endpage", params.get("endpage"));
        resMap.put("r_limitrow", params.get("r_limitrow"));
        // End.
       
        String r_orderby = "";
        String sidx = Converter.toStr(params.get("sidx")); //정렬기준컬럼
        String sord = Converter.toStr(params.get("sord")); //내림차순,오름차순
        r_orderby = sidx + " " + sord;
        //  2024-10-16 hsg 별칭 오류가 나서 수정. SO -> XX)
        if(StringUtils.equals("", sidx)) { r_orderby = "XX.ORDERNO DESC, XX.LINE_NO ASC"; } //디폴트 지정
        
        params.put("r_orderby", r_orderby);
        

        // 엑셀 다운로드.
        if(StringUtils.equals("excel", where) || StringUtils.equals("frontexcel", where) || StringUtils.equals("orderadd", where)) {
            params.remove("r_startrow");
            params.remove("r_endrow");
        }
        
        List<Map<String, Object>> list = this.getSalesOrderList(params);
        resMap.put("list", list);
        resMap.put("data", list);
        resMap.put("page", params.get("r_page"));
        
        resMap.put("where", where);
        
        return resMap;
    }
    
    
    /**
     * QMS 리스트 가져오기. O_SALESORDER.
     * @작성일 : 2021. 3. 29.
     * @작성자 : jihye lee
     * @param where : admin=관리자, excel=관리자 엑셀, front=거래처,납품처, frontexcel=거래처,납품처 엑셀.
     */
    public Map<String, Object> getQmsOrderList(Map<String, Object> params, HttpServletRequest req, LoginDto loginDto) throws Exception {
        Map<String, Object> resMap = new HashMap<>();
        
        String where = Converter.toStr(params.get("where"));
        
        String r_ordersdt = Converter.toStr(params.get("r_ordersdt")); // 주문일자 검색 시작일.
        String r_orderedt = Converter.toStr(params.get("r_orderedt")); // 주문일자 검색 종료일.
        String r_actualshipsdt = Converter.toStr(params.get("r_actualshipsdt")); // 출고일자 검색 시작일.
        String r_actualshipedt = Converter.toStr(params.get("r_actualshipedt")); // 출고일자; 검색 종료일.
        
        if(r_actualshipsdt.isEmpty() || r_actualshipedt.isEmpty())
        	return null;
        
        //String r_requestsdt = Converter.toStr(params.get("r_requestsdt")); // 납품요청일 검색 시작일.
        //String r_requestedt = Converter.toStr(params.get("r_requestedt")); // 납품요청일; 검색 종료일.
        if(!StringUtils.equals("", r_ordersdt)) r_ordersdt = r_ordersdt.replaceAll("-", "");
        if(!StringUtils.equals("", r_orderedt)) r_orderedt = r_orderedt.replaceAll("-", "");
        if(!StringUtils.equals("", r_actualshipsdt)) r_actualshipsdt = r_actualshipsdt.replaceAll("-", "");
        if(!StringUtils.equals("", r_actualshipedt)) r_actualshipedt = r_actualshipedt.replaceAll("-", "");
        //if(!StringUtils.equals("", r_requestsdt)) r_requestsdt = r_requestsdt.replaceAll("-", "");
        //if(!StringUtils.equals("", r_requestedt)) r_requestedt = r_requestedt.replaceAll("-", "");

        String rl_salesrepnm = Converter.toStr(params.get("rl_salesrepnm")); // 영업사원
        String rl_orderno = Converter.toStr(params.get("rl_orderno")); // 오더번호
        String r_custcd = Converter.toStr(params.get("r_custcd")); // 거래처
        String r_shiptocd = Converter.toStr(params.get("r_shiptocd")); // 납품처
        
        params.put("r_ordersdt", r_ordersdt);
        params.put("r_orderedt", r_orderedt);
        params.put("r_actualshipsdt", r_actualshipsdt);
        params.put("r_actualshipedt", r_actualshipedt);
        params.put("rl_salesrepnm", rl_salesrepnm);
        params.put("rl_orderno", rl_orderno);
        params.put("r_custcd", r_custcd);
        params.put("r_shiptocd", r_shiptocd);
        
        // QMS 상태 조회
        String wherebody_status = "";
        String qms_status  = params.get("qms_status") !=null?params.get("qms_status").toString():"";
        String qms_status2 = params.get("qms_status2")!=null?params.get("qms_status2").toString():"";
        String qms_status3 = params.get("qms_status3")!=null?params.get("qms_status3").toString():"";
        
        //QMS 사전입력 여부
        String qms_preyn   = params.get("qms_preyn")!=null?params.get("qms_preyn").toString():"";
        
        if (!qms_status.equals("ALL")) {
            // QMS 생성 미완료
            if(qms_status.equals("N")) {
                wherebody_status += " CASE WHEN ORDER_QTY = SF_GETQMSQTY(ORDERNO,LINE_NO) THEN 'Y' ELSE 'N' END = 'N' ";
            }
            // QMS 생성완료
            if(qms_status.equals("Y")) {
                if(wherebody_status.length() > 1) {
                    wherebody_status+= " OR ";
                }
                wherebody_status += " CASE WHEN ORDER_QTY = SF_GETQMSQTY(ORDERNO,LINE_NO) THEN 'Y' ELSE 'N' END = 'Y' ";
            }
        }
        
        if (!qms_status2.equals("ALL")) {
            // MAIL 발송 미완료
            if(qms_status2.equals("N")) {
                if(wherebody_status.length() > 1) {
                    wherebody_status+= " AND ";
                }
                wherebody_status += "dbo.SF_GETMAILYN(ORDERNO,LINE_NO) = 'N' ";
            }
            
            // MAIL 발송완료
            if(qms_status2.equals("Y")) {
                if(wherebody_status.length() > 1) {
                    wherebody_status+= " AND ";
                }
                wherebody_status += "dbo.SF_GETMAILYN(ORDERNO,LINE_NO) = 'Y' ";
            }
        }
        
        if (!qms_status3.equals("ALL")) {
            // QMS 회신 미완료
            if(qms_status3.equals("N")) {
                if(wherebody_status.length() > 1) {
                    wherebody_status+= " AND ";
                }
                wherebody_status += "dbo.SF_GETFILEYN(ORDERNO,LINE_NO) = 'N' ";
            }
            
            // QMS 회신완료
            if(qms_status3.equals("Y")) {
                if(wherebody_status.length() > 1) {
                    wherebody_status+= " AND ";
                }
                wherebody_status += "dbo.SF_GETFILEYN(ORDERNO,LINE_NO) = 'Y' ";
            }
        }
        
        if (!qms_preyn.equals("ALL")) {
            // QMS 사전입력
            if(qms_preyn.equals("Y")) {
                if(wherebody_status.length() > 1) {
                    wherebody_status+= " AND ";
                }
                wherebody_status += "dbo.SF_GETpreorderyn(SO.cust_po) = 'Y' ";
            }
            
            // QMS 사후입력건
            if(qms_preyn.equals("N")) {
                if(wherebody_status.length() > 1) {
                    wherebody_status+= " AND ";
                }
                wherebody_status += "dbo.SF_GETpreorderyn(SO.cust_po) = 'N' ";
            }
        }
        

        wherebody_status += "AND NOT(SO.STATUS1 = '980') AND SO.STATUS2 >= '620'";
        
//        if(!qms_status.equals("ALL") || !qms_status2.equals("ALL") || !qms_status3.equals("ALL") || !qms_preyn.equals("ALL")) {
//            params.put("wherebody_status", wherebody_status);
//        }
        params.put("wherebody_status", wherebody_status);
        
        System.out.println(params);
        int totalCnt = qmsOrderDao.cnt(params);
        System.out.println(totalCnt);

        Pager pager = new Pager();
        pager.gridSetInfo(totalCnt, params, req);
        resMap.put("total", Converter.toInt(params.get("totpage")));
        resMap.put("listTotalCount", totalCnt);
        
        // Start. Define Only For Form-Paging.
        resMap.put("startnumber", params.get("startnumber"));
        resMap.put("r_page", params.get("r_page"));
        resMap.put("startpage", params.get("startpage"));
        resMap.put("endpage", params.get("endpage"));
        resMap.put("r_limitrow", params.get("r_limitrow"));
        // End.
        
        String r_orderby = "";
        String sidx = Converter.toStr(params.get("sidx")); //정렬기준컬럼
        String sord = Converter.toStr(params.get("sord")); //내림차순,오름차순
        r_orderby = sidx + " " + sord;
        //  2024-10-16 hsg 별칭 오류가 나서 수정. SO -> XX)
        if(StringUtils.equals("", sidx)) { r_orderby = "XX.ORDERNO DESC, XX.LINE_NO ASC"; } //디폴트 지정
        
        params.put("r_orderby", r_orderby);

        // 엑셀 다운로드.
        if(StringUtils.equals("excel", where) || StringUtils.equals("frontexcel", where) || StringUtils.equals("orderadd", where)) {
            params.remove("r_startrow");
            params.remove("r_endrow");
        }
        
        List<Map<String, Object>> list = this.getQmsOrderList(params);
        System.out.println("======================qms list==============================");
        //System.out.println(list);
        resMap.put("list", list);
        resMap.put("data", list);
        resMap.put("page", params.get("r_page"));
        
        resMap.put("where", where);
        
        return resMap;
    }
    
    /**
     * 거래장(품목) 리스트 가져오기.
     * @작성일 : 2020. 4. 25.
     * @작성자 : kkyu
     */
    public Map<String, Object> getSalesOrderItemList(Map<String, Object> params, HttpServletRequest req, LoginDto loginDto) throws Exception {
        Map<String, Object> resMap = new HashMap<>();
        
        String where = Converter.toStr(params.get("where"));
        
        String r_actualshipsdt = Converter.toStr(params.get("r_actualshipsdt")); // 출고일자 검색 시작일.
        String r_actualshipedt = Converter.toStr(params.get("r_actualshipedt")); // 출고일자; 검색 종료일.
        if(!StringUtils.equals("", r_actualshipsdt)) r_actualshipsdt = r_actualshipsdt.replaceAll("-", "");
        if(!StringUtils.equals("", r_actualshipedt)) r_actualshipedt = r_actualshipedt.replaceAll("-", "");
        
        int totalCnt = salesOrderDao.cntForItem(params);
        
        Pager pager = new Pager();
        pager.gridSetInfo(totalCnt, params, req);
        resMap.put("total", Converter.toInt(params.get("totpage")));
        resMap.put("listTotalCount", totalCnt);
        
        // Start. Define Only For Form-Paging.
        resMap.put("startnumber", params.get("startnumber"));
        resMap.put("r_page", params.get("r_page"));
        resMap.put("startpage", params.get("startpage"));
        resMap.put("endpage", params.get("endpage"));
        resMap.put("r_limitrow", params.get("r_limitrow"));
        // End.
        
        String r_orderby = "";
        /*
        String sidx = Converter.toStr(params.get("sidx")); //정렬기준컬럼
        String sord = Converter.toStr(params.get("sord")); //내림차순,오름차순
        r_orderby = sidx + " " + sord;
        if(StringUtils.equals("", sidx)) { r_orderby = "SO.ORDERNO DESC, SO.CUST_PO DESC "; } //디폴트 지정
        */
        params.put("r_orderby", r_orderby);
        
        // 엑셀 다운로드.
        if(StringUtils.equals("excel", where) || StringUtils.equals("frontexcel", where) || StringUtils.equals("orderadd", where)) {
            params.remove("r_startrow");
            params.remove("r_endrow");
        }
        
        List<Map<String, Object>> list = salesOrderDao.listForItem(params);
        resMap.put("list", list);
        resMap.put("data", list);
        
        resMap.put("where", where);
        
        return resMap;
    }
    
    public void pushSalesOrderScheduler() throws Exception {
        Map<String, Object> svcMap = new HashMap<>();
        String r_ordersdt = Converter.dateToStr("yyyyMMdd", Converter.dateAdd(new Date(), 2, -3)); // 3개월전
        String r_orderedt = Converter.dateToStr("yyyyMMdd"); // 오늘날짜
        logger.debug("r_ordersdt : {}, r_orderedt : {}", r_ordersdt, r_orderedt);
        
        //
        svcMap.put("r_ordersdt", r_ordersdt);
        svcMap.put("r_orderedt", r_orderedt);
        svcMap.put("r_status2", "530"); // 530=배차완료
        List<Map<String, Object>> listForSendPush = salesOrderDao.getListForSendPush(svcMap);
        logger.debug("listForSendPush : {}", listForSendPush);
        
        List<Map<String, Object>> listForSendPushGroup = salesOrderDao.getListForSendPushGroup(svcMap);
        logger.debug("listForSendPushGroup : {}", listForSendPushGroup);
        svcMap.clear();
        
        //
        if(!listForSendPush.isEmpty()) {
            // Insert SALESORDERPUSH.
            for(Map<String, Object> one : listForSendPush) {
                try {
                    String user_apppushkey = Converter.toStr(one.get("USER_APPPUSHKEY"));
                    
                    svcMap.put("m_soporderno", one.get("ORDERNO"));
                    svcMap.put("m_soporderty", one.get("ORDERTY"));
                    svcMap.put("m_soplineno", one.get("LINE_NO"));
                    svcMap.put("m_sopcustcd", one.get("CUST_CD"));
                    svcMap.put("m_sopuserid", Converter.toStr(one.get("USERID")).replaceAll(" ", ""));
                    svcMap.put("m_sopuserapppushkey", user_apppushkey);
                    svcMap.put("m_soppushyn530", "Y");
                    salesOrderDao.inSalesOrderPush(svcMap);
                    svcMap.clear();
                }catch(Exception ex) {
                    ex.printStackTrace();
                }
            }
            
            // 푸시키 중복 제거.
            //logger.debug("pushKeyList 1 : {}", pushKeyList);
            //if(!pushKeyList.isEmpty()) {
                //pushKeyList = pushKeyList.stream().distinct().collect(Collectors.toList());
            //}
            //logger.debug("pushKeyList 2 : {}", pushKeyList);
            
            // 푸시 설정.
            String title = "USG BORAL e-Ordering";
            String contents = "";
            String move_url = "https://eorder.boral.kr/eorder/front/order/salesOrderList.lime";
            //String move_url = httpsUrl + req.getServerName() + (req.getServerPort() == 80 ? "" : ":" + Converter.toStr(req.getServerPort())) +  req.getContextPath();
            //move_url += "/front/order/orderList.lime";
            
            //Set Send Push.
            for(Map<String, Object> one : listForSendPushGroup) {
                List<String> pushKeyList = new ArrayList<>(); // 푸시 보낼 사용자의 푸시키 리스트.
                
                String user_apppushkey = Converter.toStr(one.get("USER_APPPUSHKEY"));
                int cnt = Converter.toInt(one.get("CNT"))-1;
                String orderno = Converter.toStr(one.get("ORDERNO_MAX")).replaceAll(" ", "");
                
                
                if(!StringUtils.equals("", user_apppushkey)) {
                    pushKeyList.add(user_apppushkey);
                }
                
                // 내용 가지고 오기.
                contents = configSvc.getConfigValue("PUSH2");
                String msg1 = "주문번호:"+((0 == cnt) ? orderno : orderno+"외 "+Converter.toStr(cnt)+"건");
                contents = contents.replace("#MSG1#", msg1+"이");
                
                logger.debug("배차완료 푸시 내용 : {}", contents);
                
                // 푸시 발송.
                AppPushUtil appPush = new AppPushUtil();
                appPush.sendAppPush(title, contents, pushKeyList, move_url);
            }
        }
    }
    
    
    
    
    
    
    
    
    /**
     * 주문번호 생성.
     * @작성일 : 2020. 4. 2.
     * @작성자 : kkyu
     */
    public String createCustOrderReqNo(String cust_cd){
        Map<String, Object> svcMap = new HashMap<>();
        svcMap.put("r_custcd", cust_cd);
        return custOrderHDao.createCustOrderReqNo(svcMap);
    }
    
    /**
     * 임시 주문번호 생성.
     * @작성일 : 2020. 4. 2.
     * @작성자 : kkyu
     */
    public String createCustOrderTempReqNo(String div) {
        if("".equals(div)) div = "T";
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmss");
        int ms = cal.get(Calendar.MILLISECOND);
        String reqNo = div + sdf.format(cal.getTime())+((ms*899+100000)/1000);
        return reqNo;
    }
    
    /**
     * 주문이력 저장.
     * @작성일 : 2020. 4. 2.
     * @작성자 : kkyu
     */
    public void insertOrderHeaderHistory(String req_no, String status_cd, String memo, String inid) {
        Map<String, Object> svcMap = new HashMap<>();
        svcMap.put("m_ohhreqno", req_no);
        svcMap.put("m_ohhstatuscd", status_cd);
        svcMap.put("m_ohhmemo", memo);
        svcMap.put("m_ohhinid", inid);
        orderHeaderHistoryDao.in(svcMap);
    }
    
    /**
     * 주문이력 주문번호 업데이트.
     * @작성일 : 2020. 4. 3.
     * @작성자 : kkyu
     * @param m_ohhreqno : Real REQ_NO [필수]
     * @param m_ohhreqnoref : Temp REQ_NO [필수]
     * @param r_ohhreqno : 현재 REQ_NO [필수]
     */
    public void updateOrderHeaderHistoryForReqNoRef(String m_ohhreqno, String m_ohhreqnoref, String r_ohhreqno, long r_ohhseq, String r_ohhstatuscd) {
        Map<String, Object> svcMap = new HashMap<>();
        svcMap.put("m_ohhreqno", m_ohhreqno);
        svcMap.put("m_ohhreqnoref", m_ohhreqnoref);
        svcMap.put("r_ohhreqno", r_ohhreqno);
        svcMap.put("r_ohhseq", r_ohhseq);
        svcMap.put("r_ohhstatuscd", r_ohhstatuscd);
        orderHeaderHistoryDao.upForReqNoRef(svcMap);
    }
    
    /**
     * 주소록 저장.
     * @작성일 : 2020. 4. 2.
     * @작성자 : kkyu
     */
    public void insertOrderAddressBookmark(String oab_userid, String oab_zipcd, String oab_add1, String oab_add2, String oab_tel1, String oab_tel2, String oab_receiver, String oab_inid) {
        Map<String, Object> svcMap = new HashMap<>();
        svcMap.put("m_oabuserid", oab_userid);
        svcMap.put("m_oabzipcd", oab_zipcd);
        svcMap.put("m_oabadd1", oab_add1);
        svcMap.put("m_oabadd2", oab_add2);
        svcMap.put("m_oabtel1", oab_tel1);
        svcMap.put("m_oabtel2", oab_tel2);
        svcMap.put("m_oabreceiver", oab_receiver);
        svcMap.put("m_oabinid", oab_inid);
        orderAddressBookmarkDao.in(svcMap);
    }
    
    /**
     * 내부사용자 웹주문현황  > 별도 권한 설정.
     * @작성일 : 2020. 4. 9.
     * @작성자 : kkyu
     * 
     * # AD > 모든 영업사원의 거래처를 볼 수 있다.
     * # CS > 본인의 고정 영업사원과 임시 영업사원의 거래처만 본다.
     *   단, CS중에 특정사람들은 AD와 같이 모든 주문을 볼수 있어야 한다.
     * # SH,SM,SR > SH,SM은 본인을 포함 자식 영업사원들의 거래처을 모두 볼수 있다.
     *   SR은 본인의 거래처만 볼수 있음.
     */
    public void setParamsForAdminOrderList(Map<String, Object> params, HttpServletRequest req, LoginDto loginDto, Model model) {
        
        String admin_authority = null;
        String admin_userid = null;
        if(loginDto!= null ) {
            admin_authority = loginDto.getAuthority();
            admin_userid = loginDto.getUserId();
        }
        if(StringUtils.equals("AD", admin_authority)) {
            
        }
        else if(StringUtils.equals("CS", admin_authority)) {
            // 특정 CS 유저에게 웹주문현황 폼에서는 AD 권한을 부여.
            //if(StringUtils.equals("", admin_userid)) {
            //  admin_authority = "AD";
            //}
        }
        else if(StringUtils.equals("SH", admin_authority) || StringUtils.equals("SM", admin_authority) || StringUtils.equals("SR", admin_authority)) {
            
        }
        else { // MK
            
        }
        
        Object obj = params.get("rl_salesrepid");
        if( !org.springframework.util.ObjectUtils.isEmpty(obj) ) {
        	int it = Integer.parseInt(obj.toString());
        	params.put("rl_salesrepid", String.valueOf(it));
        }
        
        params.put("r_adminauthority", admin_authority);
        params.put("r_adminuserid", admin_userid);
        
        model.addAttribute("admin_authority", admin_authority);
    }
    
    public List<Map<String, Object>> getCustOrderList(HttpServletRequest req, Map<String, Object> params){
//      List<Map<String, Object>> resList = new ArrayList<>();
        String[] ri_reqno = Converter.toStr(params.get("ri_reqno")).split(",", -1);
        params.put("ri_reqno", ri_reqno);
        List<Map<String, Object>> resList = custOrderHDao.list(params);
        for(Map<String, Object> one : resList) {
            List<Map<String, Object>> sub = this.getCustOrderDList(Converter.toStr(one.get("REQ_NO")), "", "", "XX.LINE_NO ASC ", 0, 0);
            one.put("subList", sub);
            
            List<Map<String, Object>> confirm = this.getOrderConfirmDList(Converter.toStr(one.get("REQ_NO")), "", "03", "", 0, 0);
            one.put("confirmList", confirm);
        }
         
        return resList;
    }
    
    /**
     * 거래명세서
     * @param params
     * @param session
     * @return
     */
    public List<Map<String, Object>> getOrderPaper(Map<String, Object> params, LoginDto loginDto) {
        List<Map<String, Object>> rList = new ArrayList<Map<String, Object>>();
        Map<String, Object> svcMap = new HashMap<String, Object>();
        
        String where = Converter.toStr(params.get("where")); // admin/front
        
        String[] ri_orderno = Converter.toStr(params.get("ri_orderno")).split(",");
        params.put("ri_orderno", ri_orderno);
        
        int idx = 0;
        rList = salesOrderDao.getOrderNoGroup(params);
        for (Map<String, Object> map : rList) {
            svcMap.put("r_orderno", map.get("ORDERNO"));
            svcMap.put("r_truckno", map.get("TRUCK_NO"));
            //svcMap.put("r_osstatus", params.get("r_osstatus"));
            List<Map<String, Object>> osList = salesOrderDao.list(svcMap);
            map.put("osList", osList);
            svcMap.clear();
            
            Map<String, Object> customer = customerSvc.getCustomer(Converter.toStr(map.get("CUST_CD")));
            map.put("customer", customer);
            svcMap.clear();
            
            String paramString = "{" 
        			+ "\"r_custcd\":\"" + Converter.toStr(map.get("CUST_CD")) + "\","
        			+ "}";
        		
            
            // 거래처 대표자명 및 주소 가져오기. > 관리자
            if(StringUtils.equals("admin", where)) {
//                Map<String, Object> customer2 = customerSvc.getOneViewSupplier(Converter.toStr(map.get("CUST_CD")));
                Map<String, Object> customer2 = sapApiSvc.getTransactionApi(paramString, sapApiSvc.API_URL_SUPPLY);
                map.put("customer2", customer2);
            }
            // 거래처 대표자명 및 주소 가져오기. > 거래처/납품처 > 한번만 가져오기. > 아래 view 테이블 검색하는데 속도가 느리네....
            else {
                if(0 == idx) {
//                    Map<String, Object> customer2 = customerSvc.getOneViewSupplier(Converter.toStr(map.get("CUST_CD")));
                    Map<String, Object> customer2 = sapApiSvc.getTransactionApi(paramString, sapApiSvc.API_URL_SUPPLY);
                    map.put("customer2", customer2);
                }
            }
            
            idx++;
        }
        
        return rList;
    }
    
    
    
    /**
     * QMS 오더번호 생성.
     * @작성일 : 2021. 5. 3.
     * @작성자 : jsh
     */
    public String getQmsOrderId(Map<String, Object> params){
        return qmsOrderDao.getQmsOrderId(params);
    }
    
    /**
     * QMS 오더 마스터 생성.
     * @작성일 : 2021. 5. 3.
     * @작성자 : jsh
     */
    public int setQmsOrderMast(Map<String, Object> params){
        //origin shiptoCd
        if(params.get("shiptoCd")!=null) {
            params.put("orgShiptoCd"   ,params.get("shiptoCd"));
        }
        
        Map<String, Object> shiptoMap = qmsOrderDao.getQmsLastShiptoInfo(params);
        
        if(shiptoMap!=null) {
            //납품처 이력이 있는 경우 입력
            params.put("shiptoCd"   ,shiptoMap.get("SHIPTO_CD"   ));
            params.put("shiptoNm"   ,shiptoMap.get("SHIPTO_NM"   ));
            params.put("shiptoAddr" ,shiptoMap.get("SHIPTO_ADDR" ));
            params.put("shiptoTel"  ,shiptoMap.get("SHIPTO_TEL"  ));
            params.put("shiptoEmail",shiptoMap.get("SHIPTO_EMAIL"));
            params.put("cnstrCd"    ,shiptoMap.get("CNSTR_CD"    ));
            params.put("cnstrNm"    ,shiptoMap.get("CNSTR_NM"    ));
            params.put("cnstrAddr"  ,shiptoMap.get("CNSTR_ADDR"  ));
            params.put("cnstrBizNo" ,shiptoMap.get("CNSTR_BIZ_NO"));
            params.put("cnstrTel"   ,shiptoMap.get("CNSTR_TEL"   ));
            params.put("cnstrEmail" ,shiptoMap.get("CNSTR_EMAIL" ));
            params.put("supvsCd"    ,shiptoMap.get("SUPVS_CD"    ));
            params.put("supvsNm"    ,shiptoMap.get("SUPVS_NM"    ));
            params.put("supvsAddr"  ,shiptoMap.get("SUPVS_ADDR"  ));
            params.put("supvsBizNo" ,shiptoMap.get("SUPVS_BIZ_NO"));
            params.put("supvsQlfNo" ,shiptoMap.get("SUPVS_QLF_NO"));
            params.put("supvsDecNo" ,shiptoMap.get("SUPVS_DEC_NO"));
            params.put("supvsTel"   ,shiptoMap.get("SUPVS_TEL"   ));
            params.put("supvsEmail" ,shiptoMap.get("SUPVS_EMAIL" ));
            
        }else {
            //라인에 선택된 납품처 기준으로 현장정보를 입력
            Map<String, Object> orgShiptoMap = qmsOrderDao.getQmsShiptoInfo(params);
            
            if(orgShiptoMap!=null) {
                params.put("shiptoCd"   ,orgShiptoMap.get("ORG_SHIPTO_CD"  ));
                params.put("shiptoNm"   ,orgShiptoMap.get("ORG_SHIPTO_NM"  ));
                params.put("shiptoAddr" ,orgShiptoMap.get("ORG_SHIPTO_ADDR"));
                params.put("shiptoEmail" ,orgShiptoMap.get("ORG_SHIPTO_EMAIL"));
            }
        }
        int result = qmsOrderDao.setQmsOrderMast(params);
        
        //QMS 내화구조 이력이 있는 경우 복사
        qmsOrderDao.setQmsOrderFireproofInsert(params);
        return result;
    }
    
    /**
     * QMS 오더 마스터 현장분할 생성.
     * @작성일 : 2021. 5. 3.
     * @작성자 : jsh
     */
    public int setQmsOrderMastSplit(Map<String, Object> params){
        return qmsOrderDao.setQmsOrderMastSplit(params);
    }
    /**
     * QMS 오더 마스터 현장분할 디테일 생성.
     * @작성일 : 2021. 5. 3.
     * @작성자 : jsh
     */
    public int setQmsOrderDetlSplit(Map<String, Object> params){
        return qmsOrderDao.setQmsOrderDetlSplit(params);
    }
    
    
    /**
     * QMS 오더 디테일 입력.
     * @작성일 : 2021. 5. 3.
     * @작성자 : jsh
     */
    public int setQmsOrderDetl(Map<String, Object> params){
        return qmsOrderDao.setQmsOrderDetl(params);
    }
    

    /**
     * QMS 오더 마스터 UPDATE.
     * @작성일 : 2021. 5. 3.
     * @작성자 : jsh
     */
    public int setQmsOrderMastTempUpdate(Map<String, Object> params){
        return qmsOrderDao.setQmsOrderMastTempUpdate(params);
    }
    
    /**
     * QMS 오더 마스터 UPDATE.
     * @작성일 : 2021. 5. 3.
     * @작성자 : jsh
     */
    public int setQmsOrderMastUpdate(Map<String, Object> params){
        return qmsOrderDao.setQmsOrderMastUpdate(params);
    }
    
    /**
     * QMS 사전입력 오더 마스터 UPDATE.
     * @작성일 : 2021. 6. 23.
     * @작성자 : jsh
     */
    public int setQmsOrderPreMastUpdate(Map<String, Object> params){
        return qmsOrderDao.setQmsOrderPreMastUpdate(params);
    }
    
    /**
     * QMS 오더 마스터 UPDATE.
     * @작성일 : 2021. 5. 3.
     * @작성자 : jsh
     */
    public int setQmsOrderMastHistory(Map<String, Object> params){
        return qmsOrderDao.setQmsOrderMastHistory(params);
    }
    
    /**
     * QMS 오더 마스터 DELETE.
     * @작성일 : 2021. 5. 3.
     * @작성자 : jsh
     */
    public int setQmsOrderMastDelete(Map<String, Object> params){
        return qmsOrderDao.setQmsOrderMastDelete(params);
    }
    
    /**
     * QMS 오더 디테일 DELETE.
     * @작성일 : 2021. 5. 3.
     * @작성자 : jsh
     */
    public int setQmsOrderDetlDelete(Map<String, Object> params){
        return qmsOrderDao.setQmsOrderDetlDelete(params);
    }
    
    /**
     * QMS 오더 디테일 DELETE.
     * @작성일 : 2021. 5. 3.
     * @작성자 : jsh
     */
    public int setQmsOrderFireDelete(Map<String, Object> params){
        return qmsOrderDao.setQmsOrderFireDelete(params);
    }
    
    /**
     * QMS 오더 디테일 임시 UPDATE.
     * @작성일 : 2021. 5. 3.
     * @작성자 : jsh
     */
    public int setQmsOrderDetlTempUpdate(Map<String, Object> params){
        return qmsOrderDao.setQmsOrderDetlTempUpdate(params);
    }
    
    /**
     * QMS 오더 디테일 UPDATE.
     * @작성일 : 2021. 5. 3.
     * @작성자 : jsh
     */
    public int setQmsOrderDetlUpdate(Map<String, Object> params){
        return qmsOrderDao.setQmsOrderDetlUpdate(params);
    }
    
    /**
     * QMS 오더 디테일 사전입력 UPDATE.
     * @작성일 : 2021. 5. 3.
     * @작성자 : jsh
     */
    public int setQmsOrderPreDetlUpdate(Map<String, Object> params){
        return qmsOrderDao.setQmsOrderPreDetlUpdate(params);
    }
    
    /**
     * QMS 오더 Fireproof UPDATE 초기화.
     * @작성일 : 2021. 5. 3.
     * @작성자 : jsh
     */
    public int setQmsOrderFireproofInit(Map<String, Object> params){
        return qmsOrderDao.setQmsOrderFireproofInit(params);
    }
    
    /**
     * QMS 오더 Fireproof 사전입력 UPDATE 초기화.
     * @작성일 : 2021. 6. 21.
     * @작성자 : jsh
     */
    public int setQmsOrderPreFireproofInit(Map<String, Object> params){
        return qmsOrderDao.setQmsOrderPreFireproofInit(params);
    }
    
    /**
     * QMS 오더 Fireproof UPDATE 입력.
     * @작성일 : 2021. 5. 3.
     * @작성자 : jsh
     */
    public int setQmsOrderFireproofUpdate(Map<String, Object> params){
        return qmsOrderDao.setQmsOrderFireproofUpdate(params);
    }
    
    /**
     * QMS 오더 Fireproof UPDATE 사전입력.
     * @작성일 : 2021. 6. 21.
     * @작성자 : jsh
     */
    public int setQmsOrderPreFireproofUpdate(Map<String, Object> params){
        return qmsOrderDao.setQmsOrderPreFireproofUpdate(params);
    }
    
    /**
     * QMS 오더 신규 입력 목록
     * @작성일 : 2021. 5. 3.
     * @작성자 : jsh
     */
    public List<Map<String, Object>> getQmsPopMastList(Map<String, Object> params){
        
        List<Map<String, Object>> qmsPopList = qmsOrderDao.getQmsPopMastList(params);
        return qmsPopList;
    }
    
    /**
     * QMS 오더 신규 입력 목록
     * @작성일 : 2021. 5. 3.
     * @작성자 : jsh
     */
    public List<Map<String, Object>> getQmsPopDetlList(Map<String, Object> params){
        
        List<Map<String, Object>> qmsPopList = qmsOrderDao.getQmsPopDetlList(params);
        return qmsPopList;
    }
    
    /**
     * QMS 오더 현장 분할시 임시저장 삭제처리
     * @작성일 : 2021. 5. 3.
     * @작성자 : jsh
     */
    public int setQmsOrderDetlClear(Map<String, Object> params){
        return qmsOrderDao.setQmsOrderDetlClear(params);
    }
    
    /**
     * QMS 오더 현장 분할시 임시저장 삭제처리
     * @작성일 : 2021. 5. 3.
     * @작성자 : jsh
     */
    public int setQmsOrderMastClear(Map<String, Object> params){
        return qmsOrderDao.setQmsOrderMastClear(params);
    }
    
    /**
     * QMS 오더 내화구조 입력 목록
     * @작성일 : 2021. 5. 3.
     * @작성자 : jsh
     */
    public List<Map<String, Object>> getQmsFireproofList(Map<String, Object> params){
        
        List<Map<String, Object>> qmsPopList = qmsOrderDao.getQmsFireproofList(params);
        return qmsPopList;
    }
    
    
    /**
     * Get QMS getQmsPopEmailGridList.
     * @작성일 : 2020. 5. 4.
     * @작성자 : jsh
     */
    public Map<String, Object> getQmsPopEmailGridList(Map<String, Object> params){
        Map<String, Object> resMap = new HashMap<>();
        String r_orderby = "";
        String sidx = Converter.toStr(params.get("sidx")); //정렬기준컬럼
        String sord = Converter.toStr(params.get("sord")); //내림차순,오름차순
        if(StringUtils.equals("QMS_ORD_NO", sidx)) sidx = "XX."+sidx; //alias 명시.
        if(StringUtils.equals("SHIPTO_NM", sidx)) sidx = "XX."+sidx;
        if(StringUtils.equals("SHIPTOREP_NM", sidx)) sidx = "XX."+sidx;
        if(StringUtils.equals("SHIPTO_EMAIL", sidx)) sidx = "XX."+sidx;
        r_orderby = sidx + " " + sord;
        if(StringUtils.equals("", sidx)) { r_orderby = "XX.QMS_ORD_NO DESC "; } //디폴트 지정
        
        params.put("r_orderby", r_orderby);
        
        List<Map<String, Object>> list = qmsOrderDao.getQmsPopEmailGridList(params);
        resMap.put("list", list);
        resMap.put("data", list);
        return resMap;
    }
    
    
    /**
     * QMS 오더 이메일 발송
     * @작성일 : 2021. 5. 3.
     * @작성자 : jsh
     */
    public int setQmsMailLog(Map<String, Object> params){
        
        return qmsOrderDao.setQmsMailLog(params);
    }
    
    /**
     * QMS 오더 사전입력 이메일 발송상태 업데이트
     * @작성일 : 2021. 5. 3.
     * @작성자 : jsh
     */
    public int setQmsMailUpdate(Map<String, Object> params){
        return qmsOrderDao.setQmsMailUpdate(params);
    }
    
    public List<Map<String, Object>> getQmsCorpList(Map<String, Object> svcMap){
        return qmsOrderDao.getQmsCorpList(svcMap);
    }
    
    /**
     * 현장명 리스트 가져오기 Ajax.
     * @작성일 : 2021. 5. 10.
     * @작성자 : jsh
     */
    public Map<String, Object> getQmsCorpList(Map<String, Object> params, HttpServletRequest req, LoginDto loginDto) throws Exception {
        Map<String, Object> resMap = new HashMap<>();

        int totalCnt = qmsOrderDao.getQmsCorpCnt(params);

        Pager pager = new Pager();
        pager.gridSetInfo(totalCnt, params, req);
        resMap.put("total", Converter.toInt(params.get("totpage")));
        resMap.put("listTotalCount", totalCnt);
        resMap.put("userId", loginDto.getUserId());
        
        // Start. Define Only For Form-Paging.
        resMap.put("startnumber", params.get("startnumber"));
        resMap.put("r_page", params.get("r_page"));
        resMap.put("startpage", params.get("startpage"));
        resMap.put("endpage", params.get("endpage"));
        resMap.put("r_limitrow", params.get("r_limitrow"));
        // End.
        
        String r_orderby = "";
        String sidx = Converter.toStr(params.get("sidx")); //정렬기준컬럼
        String sord = Converter.toStr(params.get("sord")); //내림차순,오름차순
        r_orderby = sidx + " " + sord;
        if(StringUtils.equals("", sidx)) { r_orderby = "SHIPTO_SEQ DESC"; } //디폴트 지정
        params.put("r_orderby", r_orderby);

        // 엑셀 다운로드.
        String where = Converter.toStr(params.get("where"));
        if ("excel".equals(where)) {
            params.remove("r_startrow");
            params.remove("r_endrow");
        }
        
        List<Map<String, Object>> list = this.getQmsCorpList(params);
        resMap.put("list", list);
        resMap.put("data", list);
        
        return resMap;
    }
    
    /**
     * QMS 오더 마스터 UPDATE.
     * @작성일 : 2021. 5. 3.
     * @작성자 : jsh
     */
    public Map<String, Object> getQmsCorpShiptoCd(Map<String, Object> params){
        return qmsOrderDao.getQmsCorpShiptoCd(params);
    }
    
    
    /**
     * Get QMS getQmsPopEmailGridList.
     * @작성일 : 2020. 5. 4.
     * @작성자 : jsh
     */
    public List<Map<String, Object>> getQmsPopList(Map<String, Object> params){
        Map<String, Object> resMap = new HashMap<>();
        
        String where = Converter.toStr(params.get("where"));
        
        String r_ordersdt = Converter.toStr(params.get("r_ordersdt")); // 주문일자 검색 시작일.
        String r_orderedt = Converter.toStr(params.get("r_orderedt")); // 주문일자 검색 종료일.
        String r_actualshipsdt = Converter.toStr(params.get("r_actualshipsdt")); // 출고일자 검색 시작일.
        String r_actualshipedt = Converter.toStr(params.get("r_actualshipedt")); // 출고일자; 검색 종료일.
        if(!StringUtils.equals("", r_ordersdt)) r_ordersdt = r_ordersdt.replaceAll("-", "");
        if(!StringUtils.equals("", r_orderedt)) r_orderedt = r_orderedt.replaceAll("-", "");
        if(!StringUtils.equals("", r_actualshipsdt)) r_actualshipsdt = r_actualshipsdt.replaceAll("-", "");
        if(!StringUtils.equals("", r_actualshipedt)) r_actualshipedt = r_actualshipedt.replaceAll("-", "");

        String rl_salesrepnm = Converter.toStr(params.get("rl_salesrepnm")); // 영업사원
        String rl_orderno = Converter.toStr(params.get("rl_orderno")); // 어다반허        
        String r_custcd = Converter.toStr(params.get("r_custcd")); // 거래처
        String r_shiptocd = Converter.toStr(params.get("r_shiptocd")); // 납품처
        
        params.put("r_ordersdt", r_ordersdt);
        params.put("r_orderedt", r_orderedt);
        params.put("r_actualshipsdt", r_actualshipsdt);
        params.put("r_actualshipedt", r_actualshipedt);
        params.put("rl_salesrepnm", rl_salesrepnm);
        params.put("rl_orderno", rl_orderno);
        params.put("r_custcd", r_custcd);
        params.put("r_shiptocd", r_shiptocd);

        // QMS 상태 조회
        String wherebody_status = "";
        String qms_status  = params.get("qms_status") !=null?params.get("qms_status").toString():"";
        String qms_status2 = params.get("qms_status2")!=null?params.get("qms_status2").toString():"";
        String qms_status3 = params.get("qms_status3")!=null?params.get("qms_status3").toString():"";
        
        //QMS 사전입력 여부
        String qms_preyn   = params.get("qms_preyn")!=null?params.get("qms_preyn").toString():"";
        
        if (!qms_status.equals("ALL")) {
            // QMS 생성 미완료
            if(qms_status.equals("N")) {
                wherebody_status += "dbo.SF_GETQMSID(ORDERNO,LINE_NO) IS NULL";
            }
            // QMS 생성완료
            if(qms_status.equals("Y")) {
                if(wherebody_status.length() > 1) {
                    wherebody_status+= " OR ";
                }
                wherebody_status += "dbo.SF_GETQMSID(ORDERNO,LINE_NO) IS NOT NULL";
            }
        }
        
        if (!qms_status2.equals("ALL")) {
            // MAIL 발송 미완료
            if(qms_status2.equals("N")) {
                if(wherebody_status.length() > 1) {
                    wherebody_status+= " AND ";
                }
                wherebody_status += "dbo.SF_GETMAILYN(ORDERNO,LINE_NO) = 'N' ";
            }
            
            // MAIL 발송완료
            if(qms_status2.equals("Y")) {
                if(wherebody_status.length() > 1) {
                    wherebody_status+= " AND ";
                }
                wherebody_status += "dbo.SF_GETMAILYN(ORDERNO,LINE_NO) = 'Y' ";
            }
        }
        
        if (!qms_status3.equals("ALL")) {
            // QMS 회신 미완료
            if(qms_status3.equals("N")) {
                if(wherebody_status.length() > 1) {
                    wherebody_status+= " AND ";
                }
                wherebody_status += "dbo.SF_GETFILEYN(ORDERNO,LINE_NO) = 'N' ";
            }
            
            // QMS 회신완료
            if(qms_status3.equals("Y")) {
                if(wherebody_status.length() > 1) {
                    wherebody_status+= " AND ";
                }
                wherebody_status += "dbo.SF_GETFILEYN(ORDERNO,LINE_NO) = 'Y' ";
            }
        }
        
        if (!qms_preyn.equals("ALL")) {
            // QMS 사전입력
            if(qms_preyn.equals("Y")) {
                if(wherebody_status.length() > 1) {
                    wherebody_status+= " AND ";
                }
                wherebody_status += "dbo.SF_GETpreorderyn(SO.cust_po) = 'Y' ";
            }
            
            // QMS 사후입력건
            if(qms_preyn.equals("N")) {
                if(wherebody_status.length() > 1) {
                    wherebody_status+= " AND ";
                }
                wherebody_status += "dbo.SF_GETpreorderyn(SO.cust_po) = 'N' ";
            }
        }
        
        if(!qms_status.equals("ALL") || !qms_status2.equals("ALL") || !qms_status3.equals("ALL") || !qms_preyn.equals("ALL")) {
            params.put("wherebody_status", wherebody_status);
        }
        
        System.out.println(params);
        int totalCnt = qmsOrderDao.cnt(params);
        System.out.println(totalCnt);

        // End.
        
        String r_orderby = "";
        String sidx = Converter.toStr(params.get("sidx")); //정렬기준컬럼
        String sord = Converter.toStr(params.get("sord")); //내림차순,오름차순
        r_orderby = sidx + " " + sord;
    //  2024-10-16 hsg 별칭 오류가 나서 수정. SO -> XX)
        if(StringUtils.equals("", sidx)) { r_orderby = "XX.ORDERNO DESC, XX.CUST_PO DESC "; } //디폴트 지정
        
        params.put("r_orderby", r_orderby);
        
        return qmsOrderDao.getQmsPopList(params);
    }
    
    /**
     * QMS 파일업로드 저장
     * @작성일 : 2020. 5. 15.
     * @작성자 : sjk
     */
    public Map<String, Object> qmsOrderPopFileTransaction(Map<String, Object> params, HttpServletRequest req, LoginDto loginDto) throws Exception {
        Map<String, Object> svcMap = new HashMap<>();
    
        // 파일업로드 : 동일한 파일명으로 덮어쓰기.
        String sepa = System.getProperty("file.separator");
        String folderName = "qmsorder";
        String uploadDir = new StringBuilder(req.getSession().getServletContext().getRealPath("/")).append(sepa).append("data").append(sepa).append(folderName).toString();
        List<Map<String, Object>> fList = new ArrayList<>();
        Map<String, Object> resMap = new HashMap<>();
        int qmsFileListSize = Integer.parseInt(params.get("qmsFileListSize").toString());
        String qmsOrdId = "";
        String uuidFileName = "";
        
        try {
            if (!MultipartHttpServletRequest.class.isInstance(req)) {
                throw new LimeBizException(MsgCode.FILE_REQUEST_ERROR);
            }
            MultipartHttpServletRequest multi = (MultipartHttpServletRequest)req;
            
            for (int i = 0; i < qmsFileListSize; i++) {
                MultipartFile file = multi.getFile("qmsFile_" + i);
                         
                if(file!=null && file.getSize() > 0){
                    qmsOrdId = params.get("qmsOrdNo_" + i).toString();
                    
                    UUID randomeUUID = UUID.randomUUID();
                    
                    uuidFileName = randomeUUID + "." + FilenameUtils.getExtension(file.getOriginalFilename());
                    System.out.println("QMS 헤더 번호 : " + qmsOrdId.split("-")[0]);
                    System.out.println("QMS 헤더 순번 : " + qmsOrdId.split("-")[1]);
                    System.out.println("QMS 파일 번호 : " + "auto");
                    System.out.println("파일명 : " + uuidFileName);
                    System.out.println("파일경로 : " + uploadDir);
                    System.out.println("원본 파일명 : " + file.getOriginalFilename());
                    System.out.println("파일 확장자 : " + FilenameUtils.getExtension(file.getOriginalFilename()));
                    System.out.println("파일구분 : " + "?");
                    System.out.println("파라미터명 : " + file.getName());
                    System.out.println("파일크기 : " + file.getSize());
                    System.out.println("파일 존재 : " + file.isEmpty());
                    String mime = fileUpload.getMimeType(file);
                    
                    resMap = new HashMap<>();
                    resMap.put("userFileName", file.getOriginalFilename());
                    resMap.put("saveFileName", uuidFileName);
                    resMap.put("mimeType", mime);
                    resMap.put("qmsId", qmsOrdId.split("-")[0]);
                    resMap.put("qmsSeq", qmsOrdId.split("-")[1]);
                    resMap.put("qmsFileNm", uuidFileName);
                    resMap.put("qmsFileDir", uploadDir);
                    resMap.put("qmsFileOrgNm", file.getOriginalFilename());
                    resMap.put("qmsFileExt", FilenameUtils.getExtension(file.getOriginalFilename()));
                    resMap.put("qmsFileType", mime);
                    
                    InputStream inputStream = null;
                    OutputStream outputStream = null;
                    
                    String organizedfilePath="";
                    
                    try {
                        if (file.getSize() > 0) {
                            inputStream = file.getInputStream();
                            File realUploadDir = new File(uploadDir);
                            
                            if (!realUploadDir.exists()) {
                                realUploadDir.mkdirs();//폴더생성.
                        }
                        
                        organizedfilePath = uploadDir + sepa + uuidFileName;
                        System.out.println(organizedfilePath);//파일이 저장된경로 + 파일 명
                        
                        outputStream = new FileOutputStream(organizedfilePath);
                    
                        int readByte = 0;
                        byte[] buffer = new byte[8192];
                    
                        while ((readByte = inputStream.read(buffer, 0, 8120)) != -1) {
                            outputStream.write(buffer, 0, readByte); //파일 생성 ! 
                            }
                        }
                        
                        fList.add(resMap);
                        
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        outputStream.close();
                        inputStream.close();
                    }
                }    
            }
            
        } catch (Exception e) {
            fileUpload.deleteList(fList, uploadDir);
            throw e;
        }
        
        for(Map<String, Object> file : fList) {
            qmsOrderDao.setQmsFileDatainsert(file);
        }
        
        return MsgCode.getResultMap(MsgCode.SUCCESS);
    }
    
    /**
     * QMS 파일 다운로드.
     * @작성일 : 2020. 5. 15.
     * @작성자 : sjk
     */
    public ModelAndView qmsOrderFileDown(Map<String, Object> params, HttpServletRequest req, Model model, LoginDto loginDto) throws LimeBizException{
        String sepa = System.getProperty("file.separator");
        String folderName = "qmsorder";
        String uploadDir = new StringBuilder(req.getSession().getServletContext().getRealPath("/")).append(sepa).append("data").append(sepa).append(folderName).toString();
        
        Map<String, Object> afMap = new HashMap<>();
        afMap.put("FOLDER_NAME", uploadDir);
        afMap.put("FILE_TYPE", Converter.toStr(params.get("r_filetype")));
        afMap.put("FILE_NAME", HttpUtils.restoreXss(Converter.toStr(params.get("r_filename"))));
        afMap.put("REAL_FILE_NAME", HttpUtils.restoreXss(Converter.toStr(params.get("r_realfilename"))));
        model.addAttribute("afMap", afMap);
        
        return new ModelAndView(new FileDown());
    }
    
    /**
     * QMS 파일 일괄 다운로드.
     * @작성일 : 2020. 5. 15.
     * @작성자 : sjk
     */
    public ModelAndView qmsOrderFileAllDown(Map<String, Object> params, HttpServletRequest req, Model model, LoginDto loginDto, List<String> files) throws LimeBizException{
        String sepa = System.getProperty("file.separator");
        String folderName = "qmsorder";
        String uploadDir = new StringBuilder(req.getSession().getServletContext().getRealPath("/")).append(sepa).append("data").append(sepa).append(folderName).toString();
        
        Map<String, Object> afMap = new HashMap<>();
        afMap.put("FOLDER_NAME", uploadDir);
        afMap.put("FILE_LIST", files);
        model.addAttribute("afMap", afMap);
        
        return new ModelAndView(new FileCompressDown());
    }

    /**
     * Get QMS getQmsOrderQtyCheck.
     * @작성일 : 2020. 5. 4.
     * @작성자 : jsh
     */
    public Map<String, Object> getQmsOrderQtyCheck(Map<String, Object> params){
        
        return qmsOrderDao.getQmsOrderQtyCheck(params);
    }
    
    /**
     * Get QMS setQmsOrderMastTempReset.
     * @작성일 : 2020. 5. 4.
     * @작성자 : jsh
     */
    public Object setQmsOrderTempReset(Map<String, Object> params){
        
        return qmsOrderDao.setQmsOrderTempReset(params);
    }
    
    /**
     * Set QMS 일괄등록 getQmsOrderDupCheck.
     * @작성일 : 2020. 5. 4.
     * @작성자 : jsh
     */
    public Object getQmsOrderDupCheck(Map<String, Object> params, HttpServletRequest req, LoginDto loginDto){
        Map<String, Object> resMap = new HashMap<>();
        
        String where = Converter.toStr(params.get("where"));
        
        String r_ordersdt = Converter.toStr(params.get("r_ordersdt")); // 주문일자 검색 시작일.
        String r_orderedt = Converter.toStr(params.get("r_orderedt")); // 주문일자 검색 종료일.
        String r_actualshipsdt = Converter.toStr(params.get("r_actualshipsdt")); // 출고일자 검색 시작일.
        String r_actualshipedt = Converter.toStr(params.get("r_actualshipedt")); // 출고일자; 검색 종료일.
        //String r_requestsdt = Converter.toStr(params.get("r_requestsdt")); // 납품요청일 검색 시작일.
        //String r_requestedt = Converter.toStr(params.get("r_requestedt")); // 납품요청일; 검색 종료일.
        if(!StringUtils.equals("", r_ordersdt)) r_ordersdt = r_ordersdt.replaceAll("-", "");
        if(!StringUtils.equals("", r_orderedt)) r_orderedt = r_orderedt.replaceAll("-", "");
        if(!StringUtils.equals("", r_actualshipsdt)) r_actualshipsdt = r_actualshipsdt.replaceAll("-", "");
        if(!StringUtils.equals("", r_actualshipedt)) r_actualshipedt = r_actualshipedt.replaceAll("-", "");
        //if(!StringUtils.equals("", r_requestsdt)) r_requestsdt = r_requestsdt.replaceAll("-", "");
        //if(!StringUtils.equals("", r_requestedt)) r_requestedt = r_requestedt.replaceAll("-", "");

        String rl_salesrepnm = Converter.toStr(params.get("rl_salesrepnm")); // 영업사원
        String rl_orderno = Converter.toStr(params.get("rl_orderno")); // 어다반허
        String r_custcd = Converter.toStr(params.get("r_custcd")); // 거래처
        String r_shiptocd = Converter.toStr(params.get("r_shiptocd")); // 납품처
        
        params.put("r_ordersdt", r_ordersdt);
        params.put("r_orderedt", r_orderedt);
        params.put("r_actualshipsdt", r_actualshipsdt);
        params.put("r_actualshipedt", r_actualshipedt);
        params.put("rl_salesrepnm", rl_salesrepnm);
        params.put("rl_orderno", rl_orderno);
        params.put("r_custcd", r_custcd);
        params.put("r_shiptocd", r_shiptocd);
        //params.put("r_requestsdt", r_requestsdt);
        //params.put("r_requestedt", r_requestedt);
        
        //상태값 검색조건 재정의.
        /*
         * String ri_status2 = Converter.toStr(params.get("ri_status2")); // 상태값으로 ,로
         * 구분. if(!StringUtils.equals("", ri_status2)) { String[] ri_status2arr =
         * ri_status2.split(",", -1); String wherebody_status = ""; for(String status2 :
         * ri_status2arr) { // QMS 생성 미완료 wherebody_status="(";
         * if(status2.equals("001")) {
         * 
         * wherebody_status += "dbo.SF_GETQMSID(ORDERNO,LINE_NO) IS NULL"; } // QMS 생성완료
         * if(status2.equals("002")) { if(wherebody_status.length() > 1) {
         * wherebody_status+= " OR "; } wherebody_status +=
         * "dbo.SF_GETQMSID(ORDERNO,LINE_NO) IS NOT NULL"; } wherebody_status+=")";
         * 
         * if(wherebody_status.equals("()")) { wherebody_status = ""; }
         * 
         * // MAIL 발송완료 if(status2.equals("003")) { if(wherebody_status.length() > 1) {
         * wherebody_status+= " AND "; } wherebody_status +=
         * "dbo.SF_GETMAILYN(ORDERNO,LINE_NO) = 'Y' "; } // QMS 회신완료
         * if(status2.equals("004")) { if(wherebody_status.length() > 1) {
         * wherebody_status+= " AND "; } wherebody_status +=
         * "dbo.SF_GETFILEYN(ORDERNO,LINE_NO) = 'Y' "; }
         * 
         * 
         * }
         * 
         * logger.debug("wherebody_status : {}", wherebody_status);
         * params.put("wherebody_status", wherebody_status); }
         */
        
        // QMS 상태 조회
        String wherebody_status = "";
        String qms_status  = params.get("qms_status") !=null?params.get("qms_status").toString():"";
        String qms_status2 = params.get("qms_status2")!=null?params.get("qms_status2").toString():"";
        String qms_status3 = params.get("qms_status3")!=null?params.get("qms_status3").toString():"";
        
        //QMS 사전입력 여부
        String qms_preyn   = params.get("qms_preyn")!=null?params.get("qms_preyn").toString():"";
        
        if (!qms_status.equals("ALL")) {
            // QMS 생성 미완료
            if(qms_status.equals("N")) {
                wherebody_status += "dbo.SF_GETQMSID(ORDERNO,LINE_NO) IS NULL";
            }
            // QMS 생성완료
            if(qms_status.equals("Y")) {
                if(wherebody_status.length() > 1) {
                    wherebody_status+= " OR ";
                }
                wherebody_status += "dbo.SF_GETQMSID(ORDERNO,LINE_NO) IS NOT NULL";
            }
        }
        
        if (!qms_status2.equals("ALL")) {
            // MAIL 발송 미완료
            if(qms_status2.equals("N")) {
                if(wherebody_status.length() > 1) {
                    wherebody_status+= " AND ";
                }
                wherebody_status += "dbo.SF_GETMAILYN(ORDERNO,LINE_NO) = 'N' ";
            }
            
            // MAIL 발송완료
            if(qms_status2.equals("Y")) {
                if(wherebody_status.length() > 1) {
                    wherebody_status+= " AND ";
                }
                wherebody_status += "dbo.SF_GETMAILYN(ORDERNO,LINE_NO) = 'Y' ";
            }
        }
        
        if (!qms_status3.equals("ALL")) {
            // QMS 회신 미완료
            if(qms_status3.equals("N")) {
                if(wherebody_status.length() > 1) {
                    wherebody_status+= " AND ";
                }
                wherebody_status += "dbo.SF_GETFILEYN(ORDERNO,LINE_NO) = 'N' ";
            }
            
            // QMS 회신완료
            if(qms_status3.equals("Y")) {
                if(wherebody_status.length() > 1) {
                    wherebody_status+= " AND ";
                }
                wherebody_status += "dbo.SF_GETFILEYN(ORDERNO,LINE_NO) = 'Y' ";
            }
        }
        
        if (!qms_preyn.equals("ALL")) {
            // QMS 사전입력
            if(qms_preyn.equals("Y")) {
                if(wherebody_status.length() > 1) {
                    wherebody_status+= " AND ";
                }
                wherebody_status += "dbo.SF_GETpreorderyn(SO.cust_po) = 'Y' ";
            }
            
            // QMS 사후입력건
            if(qms_preyn.equals("N")) {
                if(wherebody_status.length() > 1) {
                    wherebody_status+= " AND ";
                }
                wherebody_status += "dbo.SF_GETpreorderyn(SO.cust_po) = 'N' ";
            }
        }
        
        if(!qms_status.equals("ALL") || !qms_status2.equals("ALL") || !qms_status3.equals("ALL") || !qms_preyn.equals("ALL")) {
            params.put("wherebody_status", wherebody_status);
        }
        
        System.out.println(params);
        int totalCnt = qmsOrderDao.cnt(params);
        System.out.println(totalCnt);

        Pager pager = new Pager();
        pager.gridSetInfo(totalCnt, params, req);
        resMap.put("total", Converter.toInt(params.get("totpage")));
        resMap.put("listTotalCount", totalCnt);
        
        // Start. Define Only For Form-Paging.
        resMap.put("startnumber", params.get("startnumber"));
        resMap.put("r_page", params.get("r_page"));
        resMap.put("startpage", params.get("startpage"));
        resMap.put("endpage", params.get("endpage"));
        resMap.put("r_limitrow", params.get("r_limitrow"));
        // End.
        
        String r_orderby = "";
        String sidx = Converter.toStr(params.get("sidx")); //정렬기준컬럼
        String sord = Converter.toStr(params.get("sord")); //내림차순,오름차순
        r_orderby = sidx + " " + sord;
    //  2024-10-16 hsg 별칭 오류가 나서 수정. SO -> XX)
        if(StringUtils.equals("", sidx)) { r_orderby = "XX.ORDERNO DESC, XX.CUST_PO DESC "; } //디폴트 지정
        
        params.put("r_orderby", r_orderby);

        // 엑셀 다운로드.
        if(StringUtils.equals("excel", where) || StringUtils.equals("frontexcel", where) || StringUtils.equals("orderadd", where)) {
            params.remove("r_startrow");
            params.remove("r_endrow");
        }
        
        return qmsOrderDao.getQmsOrderDupCheck(params);
    }
    
    /**
     * Set QMS 일괄등록 setQmsOrderAllAdd.
     * @작성일 : 2020. 5. 4.
     * @작성자 : jsh
     */
    public Object setQmsOrderAllAdd(Map<String, Object> params, HttpServletRequest req, LoginDto loginDto){
        Map<String, Object> resMap = new HashMap<>();
        
        String where = Converter.toStr(params.get("where"));
        
        String r_ordersdt = Converter.toStr(params.get("r_ordersdt")); // 주문일자 검색 시작일.
        String r_orderedt = Converter.toStr(params.get("r_orderedt")); // 주문일자 검색 종료일.
        String r_actualshipsdt = Converter.toStr(params.get("r_actualshipsdt")); // 출고일자 검색 시작일.
        String r_actualshipedt = Converter.toStr(params.get("r_actualshipedt")); // 출고일자; 검색 종료일.
        //String r_requestsdt = Converter.toStr(params.get("r_requestsdt")); // 납품요청일 검색 시작일.
        //String r_requestedt = Converter.toStr(params.get("r_requestedt")); // 납품요청일; 검색 종료일.
        if(!StringUtils.equals("", r_ordersdt)) r_ordersdt = r_ordersdt.replaceAll("-", "");
        if(!StringUtils.equals("", r_orderedt)) r_orderedt = r_orderedt.replaceAll("-", "");
        if(!StringUtils.equals("", r_actualshipsdt)) r_actualshipsdt = r_actualshipsdt.replaceAll("-", "");
        if(!StringUtils.equals("", r_actualshipedt)) r_actualshipedt = r_actualshipedt.replaceAll("-", "");
        //if(!StringUtils.equals("", r_requestsdt)) r_requestsdt = r_requestsdt.replaceAll("-", "");
        //if(!StringUtils.equals("", r_requestedt)) r_requestedt = r_requestedt.replaceAll("-", "");

        String rl_salesrepnm = Converter.toStr(params.get("rl_salesrepnm")); // 영업사원
        String rl_orderno = Converter.toStr(params.get("rl_orderno")); // 오더번호
        String r_custcd = Converter.toStr(params.get("r_custcd")); // 거래처
        String r_shiptocd = Converter.toStr(params.get("r_shiptocd")); // 납품처
        
        params.put("r_ordersdt", r_ordersdt);
        params.put("r_orderedt", r_orderedt);
        params.put("r_actualshipsdt", r_actualshipsdt);
        params.put("r_actualshipedt", r_actualshipedt);
        params.put("rl_salesrepnm", rl_salesrepnm);
        params.put("rl_orderno", rl_orderno);
        params.put("r_custcd", r_custcd);
        params.put("r_shiptocd", r_shiptocd);
        
        // QMS 상태 조회
        String wherebody_status = "";
        String qms_status  = params.get("qms_status") !=null?params.get("qms_status").toString():"";
        String qms_status2 = params.get("qms_status2")!=null?params.get("qms_status2").toString():"";
        String qms_status3 = params.get("qms_status3")!=null?params.get("qms_status3").toString():"";
        
        //QMS 사전입력건은 제외한다.
        String qms_preyn   = "N";
        
        if (!qms_status.equals("ALL")) {
            // QMS 생성 미완료
            if(qms_status.equals("N")) {
                wherebody_status += "dbo.SF_GETQMSID(ORDERNO,LINE_NO) IS NULL";
            }
            // QMS 생성완료
            if(qms_status.equals("Y")) {
                if(wherebody_status.length() > 1) {
                    wherebody_status+= " OR ";
                }
                wherebody_status += "dbo.SF_GETQMSID(ORDERNO,LINE_NO) IS NOT NULL";
            }
        }
        
        if (!qms_status2.equals("ALL")) {
            // MAIL 발송 미완료
            if(qms_status2.equals("N")) {
                if(wherebody_status.length() > 1) {
                    wherebody_status+= " AND ";
                }
                wherebody_status += "dbo.SF_GETMAILYN(ORDERNO,LINE_NO) = 'N' ";
            }
            
            // MAIL 발송완료
            if(qms_status2.equals("Y")) {
                if(wherebody_status.length() > 1) {
                    wherebody_status+= " AND ";
                }
                wherebody_status += "dbo.SF_GETMAILYN(ORDERNO,LINE_NO) = 'Y' ";
            }
        }
        
        if (!qms_status3.equals("ALL")) {
            // QMS 회신 미완료
            if(qms_status3.equals("N")) {
                if(wherebody_status.length() > 1) {
                    wherebody_status+= " AND ";
                }
                wherebody_status += "dbo.SF_GETFILEYN(ORDERNO,LINE_NO) = 'N' ";
            }
            
            // QMS 회신완료
            if(qms_status3.equals("Y")) {
                if(wherebody_status.length() > 1) {
                    wherebody_status+= " AND ";
                }
                wherebody_status += "dbo.SF_GETFILEYN(ORDERNO,LINE_NO) = 'Y' ";
            }
        }
        
        if (!qms_preyn.equals("ALL")) {
            // QMS 사전입력
            if(qms_preyn.equals("Y")) {
                if(wherebody_status.length() > 1) {
                    wherebody_status+= " AND ";
                }
                wherebody_status += "dbo.SF_GETpreorderyn(SO.cust_po) = 'Y' ";
            }
            
            // QMS 사후입력건
            if(qms_preyn.equals("N")) {
                if(wherebody_status.length() > 1) {
                    wherebody_status+= " AND ";
                }
                wherebody_status += "dbo.SF_GETpreorderyn(SO.cust_po) = 'N' ";
            }
        }
        
        if(!qms_status.equals("ALL") || !qms_status2.equals("ALL") || !qms_status3.equals("ALL") || !qms_preyn.equals("ALL")) {
            params.put("wherebody_status", wherebody_status);
        }
        
        System.out.println(params);
        int totalCnt = qmsOrderDao.cnt(params);
        System.out.println(totalCnt);

        //Pager pager = new Pager();
        //pager.gridSetInfo(totalCnt, params, req);
        //resMap.put("total", Converter.toInt(params.get("totpage")));
        //resMap.put("listTotalCount", totalCnt);
        
        // Start. Define Only For Form-Paging.
        //resMap.put("startnumber", params.get("startnumber"));
        //resMap.put("r_page", params.get("r_page"));
        //resMap.put("startpage", params.get("startpage"));
        //resMap.put("endpage", params.get("endpage"));
        //resMap.put("r_limitrow", params.get("r_limitrow"));
        // End.
        
        String r_orderby = "";
        String sidx = Converter.toStr(params.get("sidx")); //정렬기준컬럼
        String sord = Converter.toStr(params.get("sord")); //내림차순,오름차순
        r_orderby = sidx + " " + sord;
    //  2024-10-16 hsg 별칭 오류가 나서 수정. SO -> XX)
        if(StringUtils.equals("", sidx)) { r_orderby = "XX.ORDERNO DESC, XX.CUST_PO DESC "; } //디폴트 지정
        
        params.put("r_orderby", r_orderby);

        List<Map<String, Object>> qmsList = this.getQmsOrderList(params);
        
        if(qmsList.size() > 0) {
            params.put("currQuarter",qmsList.get(0).get("ACTUAL_SHIP_QUARTER"));
            String qmsId = qmsOrderDao.getQmsOrderId(params);
            // qmsId 채번
            params.put("qmsId",qmsId);
            int qmsSeq = qmsOrderDao.getQmsOrderSeq(params);
            params.put("qmsSeq",qmsSeq);
        }
        
        Boolean masterInsertFlag = true;
        
        for (int i = 0 ; i < qmsList.size(); i++) {
            Map<String, Object> qmsMap = (Map<String, Object>) qmsList.get(i);
            
                
            int orderQty = qmsMap.get("ORDER_QTY")!=null?Integer.parseInt(qmsMap.get("ORDER_QTY").toString()):0;
            int orderArrQty = qmsMap.get("QMS_ARR_QTY")!=null?Integer.parseInt(qmsMap.get("QMS_ARR_QTY").toString()):0;
            
            if(orderQty == orderArrQty) {
                //QMS 완료 수량인 경우 작업하지 않음 PASS
                continue;
            }
            
            qmsMap.put("qmsId"    , params.get("qmsId"));
            qmsMap.put("qmsSeq"   , params.get("qmsSeq"));
            qmsMap.put("userId"   , loginDto.getUserId());
            qmsMap.put("orderNo"  , qmsMap.get("ORDERNO")!=null?qmsMap.get("ORDERNO").toString():"");
            qmsMap.put("lineNo"   , qmsMap.get("LINE_NO")!=null?qmsMap.get("LINE_NO").toString():"");
            qmsMap.put("custCd"   , qmsMap.get("CUST_CD")!=null?qmsMap.get("CUST_CD").toString():"");
            qmsMap.put("custPo"   , qmsMap.get("CUST_PO")!=null?qmsMap.get("CUST_PO").toString():"");
            qmsMap.put("shiptoCd" , qmsMap.get("SHIPTO_CD")!=null?qmsMap.get("SHIPTO_CD").toString():"");
            qmsMap.put("orderQty" , orderQty);
            qmsMap.put("orderty"  , qmsMap.get("ORDERTY")!=null?qmsMap.get("ORDERTY").toString():"");
            qmsMap.put("itemCd"   , qmsMap.get("ITEM_CD")!=null?qmsMap.get("ITEM_CD").toString():"");
            qmsMap.put("lotNo"    , qmsMap.get("LOTN")!=null?qmsMap.get("LOTN").toString():"");
            qmsMap.put("qmsOrdQty", 0);
            qmsMap.put("deleteYN" , "T");
            
            //#{qmsId},#{qmsSeq},#{orderNo},#{custPo},#{orderty},#{lineNo},#{itemCd},#{lotNo},#{orderQty},#{qmsOrdQty},#{deleteYN}
            if (masterInsertFlag) {
                setQmsOrderMast(qmsMap);
                // QMS는 한번만 입력한다.
                masterInsertFlag=false; 
            }
            
            qmsOrderDao.setQmsOrderDetl(qmsMap);
            
        }
        
        if(!masterInsertFlag) {
            return params.get("qmsId");
        }else {
            return "NONE";
        }
    }
    
    
    /**
     * Set QMS 건수 체크
     * @작성일 : 2020. 6. 21.
     * @작성자 : jsh
     */
    public int getQmsOrderCnt(Map<String, Object> params){
        Map<String, Object> resMap = new HashMap<>();
        
        String where = Converter.toStr(params.get("where"));
        
        String r_ordersdt = Converter.toStr(params.get("r_ordersdt")); // 주문일자 검색 시작일.
        String r_orderedt = Converter.toStr(params.get("r_orderedt")); // 주문일자 검색 종료일.
        String r_actualshipsdt = Converter.toStr(params.get("r_actualshipsdt")); // 출고일자 검색 시작일.
        String r_actualshipedt = Converter.toStr(params.get("r_actualshipedt")); // 출고일자; 검색 종료일.
        if(!StringUtils.equals("", r_ordersdt)) r_ordersdt = r_ordersdt.replaceAll("-", "");
        if(!StringUtils.equals("", r_orderedt)) r_orderedt = r_orderedt.replaceAll("-", "");
        if(!StringUtils.equals("", r_actualshipsdt)) r_actualshipsdt = r_actualshipsdt.replaceAll("-", "");
        if(!StringUtils.equals("", r_actualshipedt)) r_actualshipedt = r_actualshipedt.replaceAll("-", "");

        String rl_salesrepnm = Converter.toStr(params.get("rl_salesrepnm")); // 영업사원
        String rl_orderno = Converter.toStr(params.get("rl_orderno")); // 오더번호
        String r_custcd = Converter.toStr(params.get("r_custcd")); // 거래처
        String r_shiptocd = Converter.toStr(params.get("r_shiptocd")); // 납품처
        
        params.put("r_ordersdt", r_ordersdt);
        params.put("r_orderedt", r_orderedt);
        params.put("r_actualshipsdt", r_actualshipsdt);
        params.put("r_actualshipedt", r_actualshipedt);
        params.put("rl_salesrepnm", rl_salesrepnm);
        params.put("rl_orderno", rl_orderno);
        params.put("r_custcd", r_custcd);
        params.put("r_shiptocd", r_shiptocd);
        
        // QMS 상태 조회
        String wherebody_status = "";
        String qms_status  = params.get("qms_status") !=null?params.get("qms_status").toString():"";
        String qms_status2 = params.get("qms_status2")!=null?params.get("qms_status2").toString():"";
        String qms_status3 = params.get("qms_status3")!=null?params.get("qms_status3").toString():"";
        
        //QMS 사전입력 여부
        String qms_preyn   = params.get("qms_preyn")!=null?params.get("qms_preyn").toString():"";
        
        if (!qms_status.equals("ALL")) {
            // QMS 생성 미완료
            if(qms_status.equals("N")) {
                wherebody_status += "dbo.SF_GETQMSID(ORDERNO,LINE_NO) IS NULL";
            }
            // QMS 생성완료
            if(qms_status.equals("Y")) {
                if(wherebody_status.length() > 1) {
                    wherebody_status+= " OR ";
                }
                wherebody_status += "dbo.SF_GETQMSID(ORDERNO,LINE_NO) IS NOT NULL";
            }
        }
        
        if (!qms_status2.equals("ALL")) {
            // MAIL 발송 미완료
            if(qms_status2.equals("N")) {
                if(wherebody_status.length() > 1) {
                    wherebody_status+= " AND ";
                }
                wherebody_status += "dbo.SF_GETMAILYN(ORDERNO,LINE_NO) = 'N' ";
            }
            
            // MAIL 발송완료
            if(qms_status2.equals("Y")) {
                if(wherebody_status.length() > 1) {
                    wherebody_status+= " AND ";
                }
                wherebody_status += "dbo.SF_GETMAILYN(ORDERNO,LINE_NO) = 'Y' ";
            }
        }
        
        if (!qms_status3.equals("ALL")) {
            // QMS 회신 미완료
            if(qms_status3.equals("N")) {
                if(wherebody_status.length() > 1) {
                    wherebody_status+= " AND ";
                }
                wherebody_status += "dbo.SF_GETFILEYN(ORDERNO,LINE_NO) = 'N' ";
            }
            
            // QMS 회신완료
            if(qms_status3.equals("Y")) {
                if(wherebody_status.length() > 1) {
                    wherebody_status+= " AND ";
                }
                wherebody_status += "dbo.SF_GETFILEYN(ORDERNO,LINE_NO) = 'Y' ";
            }
        }
        
        if (!qms_preyn.equals("ALL")) {
            // QMS 사전입력
            if(qms_preyn.equals("Y")) {
                if(wherebody_status.length() > 1) {
                    wherebody_status+= " AND ";
                }
                wherebody_status += "dbo.SF_GETpreorderyn(SO.cust_po) = 'Y' ";
            }
            
            // QMS 사후입력건
            if(qms_preyn.equals("N")) {
                if(wherebody_status.length() > 1) {
                    wherebody_status+= " AND ";
                }
                wherebody_status += "dbo.SF_GETpreorderyn(SO.cust_po) = 'N' ";
            }
        }
        
        if(!qms_status.equals("ALL") || !qms_status2.equals("ALL") || !qms_status3.equals("ALL") || !qms_preyn.equals("ALL")) {
            params.put("wherebody_status", wherebody_status);
        }
        
        int totalCnt = qmsOrderDao.getQmsOrderCnt(params);
        
        return totalCnt;
    }
    
    
    /**
     * Get 현장 가져오기
     * @작성일 : 2021. 5. 24.
     * @작성자 : jsh
     */
    public List<Map<String, Object>> getQmsOrderShiptoList(Map<String, Object> svcMap){
        return qmsOrderDao.getShipToList(svcMap);
    }
    
    /**
     * QMS 납품처 가져오기
     * @작성일 : 2021. 5. 24.
     * @작성자 : jsh
     */
    public Map<String, Object> getShipToList(Map<String, Object> params, HttpServletRequest req, LoginDto loginDto) throws Exception {
        Map<String, Object> resMap = new HashMap<>();
        
        String where = Converter.toStr(params.get("where"));
        
        String r_ordersdt = Converter.toStr(params.get("r_ordersdt")); // 주문일자 검색 시작일.
        String r_orderedt = Converter.toStr(params.get("r_orderedt")); // 주문일자 검색 종료일.
        String r_actualshipsdt = Converter.toStr(params.get("r_actualshipsdt")); // 출고일자 검색 시작일.
        String r_actualshipedt = Converter.toStr(params.get("r_actualshipedt")); // 출고일자; 검색 종료일.
        //String r_requestsdt = Converter.toStr(params.get("r_requestsdt")); // 납품요청일 검색 시작일.
        //String r_requestedt = Converter.toStr(params.get("r_requestedt")); // 납품요청일; 검색 종료일.
        if(!StringUtils.equals("", r_ordersdt)) r_ordersdt = r_ordersdt.replaceAll("-", "");
        if(!StringUtils.equals("", r_orderedt)) r_orderedt = r_orderedt.replaceAll("-", "");
        if(!StringUtils.equals("", r_actualshipsdt)) r_actualshipsdt = r_actualshipsdt.replaceAll("-", "");
        if(!StringUtils.equals("", r_actualshipedt)) r_actualshipedt = r_actualshipedt.replaceAll("-", "");
        //if(!StringUtils.equals("", r_requestsdt)) r_requestsdt = r_requestsdt.replaceAll("-", "");
        //if(!StringUtils.equals("", r_requestedt)) r_requestedt = r_requestedt.replaceAll("-", "");

        String rl_salesrepnm = Converter.toStr(params.get("rl_salesrepnm")); // 영업사원
        String rl_orderno = Converter.toStr(params.get("rl_orderno")); // 어다반허
        String r_custcd = Converter.toStr(params.get("r_custcd")); // 거래처
        String r_shiptocd = Converter.toStr(params.get("r_shiptocd")); // 납품처
        
        params.put("r_ordersdt", r_ordersdt);
        params.put("r_orderedt", r_orderedt);
        params.put("r_actualshipsdt", r_actualshipsdt);
        params.put("r_actualshipedt", r_actualshipedt);
        params.put("rl_salesrepnm", rl_salesrepnm);
        params.put("rl_orderno", rl_orderno);
        params.put("r_custcd", r_custcd);
        params.put("r_shiptocd", r_shiptocd);
        
        // QMS 상태 조회
        String wherebody_status = "";
        String qms_status  = params.get("qms_status") !=null?params.get("qms_status").toString():"";
        String qms_status2 = params.get("qms_status2")!=null?params.get("qms_status2").toString():"";
        String qms_status3 = params.get("qms_status3")!=null?params.get("qms_status3").toString():"";
        
        //QMS 사전입력 여부
        String qms_preyn   = params.get("qms_preyn")!=null?params.get("qms_preyn").toString():"";
        
        if (!qms_status.equals("ALL")) {
            // QMS 생성 미완료
            if(qms_status.equals("N")) {
                wherebody_status += "dbo.SF_GETQMSID(ORDERNO,LINE_NO) IS NULL";
            }
            // QMS 생성완료
            if(qms_status.equals("Y")) {
                if(wherebody_status.length() > 1) {
                    wherebody_status+= " OR ";
                }
                wherebody_status += "dbo.SF_GETQMSID(ORDERNO,LINE_NO) IS NOT NULL";
            }
        }
        
        if (!qms_status2.equals("ALL")) {
            // MAIL 발송 미완료
            if(qms_status2.equals("N")) {
                if(wherebody_status.length() > 1) {
                    wherebody_status+= " AND ";
                }
                wherebody_status += "dbo.SF_GETMAILYN(ORDERNO,LINE_NO) = 'N' ";
            }
            
            // MAIL 발송완료
            if(qms_status2.equals("Y")) {
                if(wherebody_status.length() > 1) {
                    wherebody_status+= " AND ";
                }
                wherebody_status += "dbo.SF_GETMAILYN(ORDERNO,LINE_NO) = 'Y' ";
            }
        }
        
        if (!qms_status3.equals("ALL")) {
            // QMS 회신 미완료
            if(qms_status3.equals("N")) {
                if(wherebody_status.length() > 1) {
                    wherebody_status+= " AND ";
                }
                wherebody_status += "dbo.SF_GETFILEYN(ORDERNO,LINE_NO) = 'N' ";
            }
            
            // QMS 회신완료
            if(qms_status3.equals("Y")) {
                if(wherebody_status.length() > 1) {
                    wherebody_status+= " AND ";
                }
                wherebody_status += "dbo.SF_GETFILEYN(ORDERNO,LINE_NO) = 'Y' ";
            }
        }
        
        if (!qms_preyn.equals("ALL")) {
            // QMS 사전입력
            if(qms_preyn.equals("Y")) {
                if(wherebody_status.length() > 1) {
                    wherebody_status+= " AND ";
                }
                wherebody_status += "dbo.SF_GETpreorderyn(SO.cust_po) = 'Y' ";
            }
            
            // QMS 사후입력건
            if(qms_preyn.equals("N")) {
                if(wherebody_status.length() > 1) {
                    wherebody_status+= " AND ";
                }
                wherebody_status += "dbo.SF_GETpreorderyn(SO.cust_po) = 'N' ";
            }
        }
        
        if(!qms_status.equals("ALL") || !qms_status2.equals("ALL") || !qms_status3.equals("ALL") || !qms_preyn.equals("ALL")) {
            params.put("wherebody_status", wherebody_status);
        }
        
        
        // 페이징 없이 End.
        
        String r_orderby = "";
        String sidx = Converter.toStr(params.get("sidx")); //정렬기준컬럼
        String sord = Converter.toStr(params.get("sord")); //내림차순,오름차순
        r_orderby = sidx + " " + sord;
    //  2024-10-16 hsg 별칭 오류가 나서 수정. SO -> XX)
        if(StringUtils.equals("", sidx)) { r_orderby = "XX.ORDERNO DESC, XX.CUST_PO DESC "; } //디폴트 지정
        
        params.put("r_orderby", r_orderby);
        
        List<Map<String, Object>> list = this.getQmsOrderShiptoList(params);
        System.out.println("======================qms list==============================");
        //System.out.println(list);
        resMap.put("list", list);
        resMap.put("data", list);
        
        resMap.put("where", where);
        
        return resMap;
    }
    
    /**
     * QMS 오더 삭제
     * @작성일 : 2021. 5. 29.
     * @작성자 : jsh
     */
    public Object setQmsOrderRemove(Map<String, Object> svcMap){
        
        String[] qmsIdArr = (String[]) svcMap.get("qmsIdArr");
        int result = 0;
        for(int i = 0 ; i < qmsIdArr.length; i++) {
            String[] qmsIdSeq = qmsIdArr[i].split("-");
            
            if(qmsIdSeq.length == 2) {
                svcMap.put("qmsId" , qmsIdSeq[0]);
                svcMap.put("qmsSeq", qmsIdSeq[1]);
                
                qmsOrderDao.setQmsOrderFireDelete(svcMap);
                qmsOrderDao.setQmsOrderDetlDelete(svcMap);
                qmsOrderDao.setQmsOrderMastDelete(svcMap);
                result++;
            }
        }
        
        return result;
    }
    
    /**
     * QMS조직 설정 리스트 가져오기 Ajax.
     * @작성일 : 2021. 5. 29.
     * @작성자 : sjk
     */
    public Map<String, Object> getQmsDepartmentListAjax(Map<String, Object> params, HttpServletRequest req) {
        Map<String, Object> resMap = new HashMap<>();
        
        int totalCnt = qmsOrderDao.getQmsDepartmentListAjaxCnt(params);

        Pager pager = new Pager();
        pager.gridSetInfo(totalCnt, params, req);
        resMap.put("total", Converter.toInt(params.get("totpage")));
        resMap.put("listTotalCount", totalCnt);
        
        // Start. Define Only For Form-Paging.
        resMap.put("startnumber", params.get("startnumber"));
        resMap.put("r_page", params.get("r_page"));
        resMap.put("startpage", params.get("startpage"));
        resMap.put("endpage", params.get("endpage"));
        resMap.put("r_limitrow", params.get("r_limitrow"));
        // End.
        
        String r_orderby = "";
        String sidx = Converter.toStr(params.get("sidx")); //정렬기준컬럼
        String sord = Converter.toStr(params.get("sord")); //내림차순,오름차순
        r_orderby = sidx + " " + sord;
        if(StringUtils.equals("", sidx)) { r_orderby = " QMS_DEPT_NM , QMS_TEAM_NM ASC "; } //디폴트 지정
        params.put("r_orderby", r_orderby);
        
        // 엑셀 다운로드.
        String where = Converter.toStr(params.get("where"));
        if ("excel".equals(where)) {
            params.remove("r_startrow");
            params.remove("r_endrow");
        }
        
        List<Map<String, Object>> list = qmsOrderDao.getQmsDepartmentListAjax(params);
        resMap.put("list", list);
        resMap.put("page", params.get("r_page"));
        
        resMap.putAll(MsgCode.getResultMap(MsgCode.SUCCESS));
        
        return resMap;
    }
    
    public List<Map<String, Object>> getQmsYearList(Map<String, Object> params, HttpServletRequest req) {
        return qmsOrderDao.getQmsYearList(params);
    }
    
    public List<Map<String, Object>> getQmsOrderYearList(Map<String, Object> params, HttpServletRequest req) {
        return qmsOrderDao.getQmsOrderYearList(params);
    }
    
    public List<Map<String, Object>> getQmsReleaseYearList(Map<String, Object> params, HttpServletRequest req) {
        return qmsOrderDao.getQmsReleaseYearList(params);
    }
    
    /**
     * QMS 마감 페이지 리스트 가져오기 Ajax.
     * @작성일 : 2021. 5. 29.
     * @작성자 : sjk
     */
    public Map<String, Object> getQmsDedalinesListAjax(Map<String, Object> params, HttpServletRequest req) {
        Map<String, Object> resMap = new HashMap<>();
        
        int totalCnt = qmsOrderDao.getQmsDedalinesListAjaxCnt(params);

        Pager pager = new Pager();
        pager.gridSetInfo(totalCnt, params, req);
        resMap.put("total", Converter.toInt(params.get("totpage")));
        resMap.put("listTotalCount", totalCnt);
        
        // Start. Define Only For Form-Paging.
        resMap.put("startnumber", params.get("startnumber"));
        resMap.put("r_page", params.get("r_page"));
        resMap.put("startpage", params.get("startpage"));
        resMap.put("endpage", params.get("endpage"));
        resMap.put("r_limitrow", params.get("r_limitrow"));
        // End.
        
        String r_orderby = "";
        String sidx = Converter.toStr(params.get("sidx")); //정렬기준컬럼
        String sord = Converter.toStr(params.get("sord")); //내림차순,오름차순
        r_orderby = sidx + " " + sord;
        if(StringUtils.equals("", sidx)) { r_orderby = " QMS_DELN_YEAR,QMS_DELN_QUAT DESC "; } //디폴트 지정
        params.put("r_orderby", r_orderby);
        
        // 엑셀 다운로드.
        String where = Converter.toStr(params.get("where"));
        if ("excel".equals(where)) {
            params.remove("r_startrow");
            params.remove("r_endrow");
        }
        
        List<Map<String, Object>> list = qmsOrderDao.getQmsDedalinesListAjax(params);
        resMap.put("list", list);
        resMap.put("page", params.get("r_page"));
        
        resMap.putAll(MsgCode.getResultMap(MsgCode.SUCCESS));
        
        return resMap;
    }
    
    /**
     * TreeGrid 리스트 데이터 저장.
     * @작성일 : 2020. 3. 6.
     * @작성자 : kkyu
     */
    public Map<String, Object> mergeQmsDepartmentAjaxTransaction(Map<String, Object> params, HttpServletRequest req, LoginDto loginDto) throws LimeBizException {
        Map<String, Object> svcMap = new HashMap<>();
    
        String userId = loginDto.getUserId();
        
        // 파라미터 정의.
        String [] r_ptcodearr = req.getParameterValues("r_keycode");
        String [] m_userid = req.getParameterValues("m_userid");
        String [] m_qmsDeptId = req.getParameterValues("m_qmsDeptId");
        String [] m_qmsDeptNm = req.getParameterValues("m_qmsDeptNm");
        String [] m_qmsTeamNm = req.getParameterValues("m_qmsTeamNm");
        String [] m_qmsDeptRemark = req.getParameterValues("m_qmsDeptRemark");
        String [] m_active = req.getParameterValues("m_active");

        // 저장/수정 처리.
        for(int i=0,j=r_ptcodearr.length; i<j; i++){
            
            svcMap.put("m_userid", Converter.toStr(m_userid[i]));
            svcMap.put("m_qmsDeptId", Converter.toStr(m_qmsDeptId[i]));
            svcMap.put("m_qmsDeptNm", Converter.toStr(m_qmsDeptNm[i]));
            svcMap.put("m_qmsTeamNm", Converter.toStr(m_qmsTeamNm[i]));
            svcMap.put("m_qmsDeptRemark", Converter.toStr(m_qmsDeptRemark[i]));
            svcMap.put("m_setUserId", Converter.toStr(userId));
            svcMap.put("m_active", Converter.toStr(m_active[i]));
            
            qmsOrderDao.mergeQmsDepartmentAjaxTransaction(svcMap);
            
            svcMap.clear();
        }
        
        return MsgCode.getResultMap(MsgCode.SUCCESS);
    }
    
    /**
     * QMS 마감 저장.
     * @작성일 : 2021. 6. 13.
     * @작성자 : SJK
     */
    public Map<String, Object> mergeQmsDedalinesAjaxTransaction(Map<String, Object> params, HttpServletRequest req, LoginDto loginDto) throws LimeBizException {
        Map<String, Object> svcMap = new HashMap<>();
    
        String userId = loginDto.getUserId();
        
        // 파라미터 정의.
        String [] r_ptcodearr = req.getParameterValues("r_keycode");
        String [] m_qmsDelnDesc = req.getParameterValues("m_qmsDelnDesc");
        String [] m_active = req.getParameterValues("m_active");

        // 저장/수정 처리.
        for(int i=0,j=r_ptcodearr.length; i<j; i++){
            
            svcMap.put("m_qmsDelnYear", Converter.toStr(r_ptcodearr[i].split("-")[0]));
            svcMap.put("m_qmsDelnQuat", Converter.toStr(r_ptcodearr[i].split("-")[1]));
            svcMap.put("m_qmsDelnDesc", Converter.toStr(m_qmsDelnDesc[i]));
            svcMap.put("m_setUserId", Converter.toStr(userId));
            svcMap.put("m_active", Converter.toStr(m_active[i]));
            
            qmsOrderDao.mergeQmsDedalinesAjaxTransaction(svcMap);
            
            svcMap.clear();
        }
        
        return MsgCode.getResultMap(MsgCode.SUCCESS);
    }
    
    /**
     * QMS 통계(QMS 회수 실적현황) 가져오기 Ajax.
     * @작성일 : 2021. 6. 19.
     * @작성자 : sjk
     */
    public Map<String, Object> getQmsStasticsSalesListAjax(Map<String, Object> params, HttpServletRequest req) {
        Map<String, Object> resMap = new HashMap<>();
        
        int totalCnt = qmsOrderDao.getQmsStasticsSalesListAjaxCnt(params);

        Pager pager = new Pager();
        pager.gridSetInfo(totalCnt, params, req);
        resMap.put("total", Converter.toInt(params.get("totpage")));
        resMap.put("listTotalCount", totalCnt);
        
        // Start. Define Only For Form-Paging.
        resMap.put("startnumber", params.get("startnumber"));
        resMap.put("r_page", params.get("r_page"));
        resMap.put("startpage", params.get("startpage"));
        resMap.put("endpage", params.get("endpage"));
        resMap.put("r_limitrow", params.get("r_limitrow"));
        // End.

        String r_orderby = "";
        
        // 처음 화면 or 부서별 정렬할 때 3가지 조건이 들어가게 정렬
        
        String sidx = Converter.toStr(params.get("sidx")); //정렬기준컬럼
        String sord = Converter.toStr(params.get("sord")); //내림차순,오름차순
        r_orderby = sidx + " " + sord;
        if(StringUtils.equals("SALESREP_NM", sidx)) {
            r_orderby = "SALESREP_NM ASC"; //디폴트 지정
        }else if(StringUtils.equals("QMS_DEPT_NM", sidx)){
            r_orderby = "QMS_DEPT_NM, QMS_TEAM_NM, SALESREP_NM ASC";
        }else if(StringUtils.equals("QMS_TEAM_NM", sidx)){
            r_orderby = "QMS_TEAM_NM ASC";
        }else {
            r_orderby = "QMS_DEPT_NM, QMS_TEAM_NM, SALESREP_NM ASC";
        }
        params.put("r_orderby", r_orderby);
        
        // 엑셀 다운로드.
        String where = Converter.toStr(params.get("where"));
        if ("excel".equals(where)) {
            params.remove("r_startrow");
            params.remove("r_endrow");
        }
        
        List<Map<String, Object>> list = qmsOrderDao.getQmsStasticsSalesListAjax(params);
        
        String[] cds = new String[list.size()];
        int count = 0;
        for(Map<String, Object>m : list) {
        	// 2024-10-17 hsg m.get("SALESREP_CD") 값에 null이 포함되어 있어 string으로 변환할 때 오류남
        	cds[count++] = (m.get("SALESREP_CD") == null) ? "" : m.get("SALESREP_CD").toString();
        }
        
        Map<String, Object>  prms = new HashMap<>();
        if(list.size() > 0) {
        	prms.put("repCdArr", cds);
        }
        prms.put("r_actualshipsdt", params.get("r_actualshipsdt"));
        prms.put("r_actualshipedt", params.get("r_actualshipedt"));
        List<Map<String, Object>> repCdList = qmsOrderDao.getPulbishCountQmsSalesorder(params);
        
        Map<String, Object> cdMap = new HashMap<>();
        for(Map<String, Object> m : repCdList) {
        	// 2024-10-17 hsg m.get("SALESREP_CD") 값에 null이 포함되어 있어 string으로 변환할 때 오류남
        	cdMap.put((m.get("SALESREP_CD") == null) ? "" : m.get("SALESREP_CD").toString(), m.get("CNT_QMS_ARR"));
        }
        
        for(Map<String, Object>m : list) {
        	// 2024-10-17 hsg m.get("SALESREP_CD") 값에 null이 포함되어 있어 string으로 변환할 때 오류남
        	String cd = (m.get("SALESREP_CD") == null) ? "" : m.get("SALESREP_CD").toString();	
        	if(cdMap.get(cd) != null) {
        		m.replace("QMS_ORDER_CNT", cdMap.get(cd).toString());
        	}
        }
        
        resMap.put("list", list);
        
        resMap.putAll(MsgCode.getResultMap(MsgCode.SUCCESS));
        
        return resMap;
    }
    
    /**
     * QMS 통계(팀별 QMS 회수율) 가져오기 Ajax.
     * @작성일 : 2021. 6. 19.
     * @작성자 : sjk
     */
    public Map<String, Object> getQmsStasticsTeamListAjax(Map<String, Object> params, HttpServletRequest req) {
        Map<String, Object> resMap = new HashMap<>();
        
        int totalCnt = qmsOrderDao.getQmsStasticsTeamListAjaxCnt(params);

        Pager pager = new Pager();
        pager.gridSetInfo(totalCnt, params, req);
        resMap.put("total", Converter.toInt(params.get("totpage")));
        resMap.put("listTotalCount", totalCnt);
        
        // Start. Define Only For Form-Paging.
        resMap.put("startnumber", params.get("startnumber"));
        resMap.put("r_page", params.get("r_page"));
        resMap.put("startpage", params.get("startpage"));
        resMap.put("endpage", params.get("endpage"));
        resMap.put("r_limitrow", params.get("r_limitrow"));
        // End.
        
        String r_orderby = "";
        String sidx = Converter.toStr(params.get("sidx")); //정렬기준컬럼
        String sord = Converter.toStr(params.get("sord")); //내림차순,오름차순
        r_orderby = sidx + " " + sord;
        if(StringUtils.equals("", sidx)) { r_orderby = " 1 ASC "; } //디폴트 지정
        params.put("r_orderby", r_orderby);
        
        // 엑셀 다운로드.
        String where = Converter.toStr(params.get("where"));
        if ("excel".equals(where)) {
            params.remove("r_startrow");
            params.remove("r_endrow");
        }
        
        List<Map<String, Object>> list = qmsOrderDao.getQmsStasticsTeamListAjax(params);
        resMap.put("list", list);
        
        resMap.putAll(MsgCode.getResultMap(MsgCode.SUCCESS));
        
        return resMap;
    }
    
    /**
     * QMS 통계(팀별 QMS 회수율) 가져오기 Ajax.
     * @작성일 : 2021. 6. 19.
     * @작성자 : sjk
     */
    public Map<String, Object> getQmsRawStasticsListAjax(Map<String, Object> params, HttpServletRequest req) {
        Map<String, Object> resMap = new HashMap<>();
        
        int totalCnt = qmsOrderDao.getQmsRawStasticsListAjaxCnt(params);

        Pager pager = new Pager();
        pager.gridSetInfo(totalCnt, params, req);
        resMap.put("total", Converter.toInt(params.get("totpage")));
        resMap.put("listTotalCount", totalCnt);
        
        // Start. Define Only For Form-Paging.
        resMap.put("startnumber", params.get("startnumber"));
        resMap.put("r_page", params.get("r_page"));
        resMap.put("startpage", params.get("startpage"));
        resMap.put("endpage", params.get("endpage"));
        resMap.put("r_limitrow", params.get("r_limitrow"));
        // End.
        
        String r_orderby = "";
        String sidx = Converter.toStr(params.get("sidx")); //정렬기준컬럼
        String sord = Converter.toStr(params.get("sord")); //내림차순,오름차순
        r_orderby = sidx + " " + sord;
        // 2024-10-17 hsg MS-SQL에서는 순번으로 정렬할 수 없어서 컬럼명으로 수정 if(StringUtils.equals("", sidx)) { r_orderby = " 1 ASC,2 ASC,4 ASC "; } //디폴트 지정
        if(StringUtils.equals("", sidx)) { r_orderby = " SEQNUM ASC, RNUM ASC, QMS_SEQ ASC "; } //디폴트 지정
        params.put("r_orderby", r_orderby);
        
        // 엑셀 다운로드.
        String where = Converter.toStr(params.get("where"));
        if ("excel".equals(where)) {
            params.remove("r_startrow");
            params.remove("r_endrow");
        }
        
        List<Map<String, Object>> list = qmsOrderDao.getQmsRawStasticsListAjax(params);
        resMap.put("list", list);
        resMap.put("page", params.get("r_page"));
        
        resMap.putAll(MsgCode.getResultMap(MsgCode.SUCCESS));
        
        return resMap;
    }
    
    
    /**
     * Get QMS 메일일괄발송 getOrderDupCheck.
     * @작성일 : 2020. 6. 21.
     * @작성자 : jsh
     */
    public Object getOrderDupCheck(Map<String, Object> params, HttpServletRequest req, LoginDto loginDto){
        Map<String, Object> resMap = new HashMap<>();
        
        String where = Converter.toStr(params.get("where"));
        
        String r_ordersdt = Converter.toStr(params.get("r_ordersdt")); // 주문일자 검색 시작일.
        String r_orderedt = Converter.toStr(params.get("r_orderedt")); // 주문일자 검색 종료일.
        String r_actualshipsdt = Converter.toStr(params.get("r_actualshipsdt")); // 출고일자 검색 시작일.
        String r_actualshipedt = Converter.toStr(params.get("r_actualshipedt")); // 출고일자; 검색 종료일.
        if(!StringUtils.equals("", r_ordersdt)) r_ordersdt = r_ordersdt.replaceAll("-", "");
        if(!StringUtils.equals("", r_orderedt)) r_orderedt = r_orderedt.replaceAll("-", "");
        if(!StringUtils.equals("", r_actualshipsdt)) r_actualshipsdt = r_actualshipsdt.replaceAll("-", "");
        if(!StringUtils.equals("", r_actualshipedt)) r_actualshipedt = r_actualshipedt.replaceAll("-", "");

        String rl_salesrepnm = Converter.toStr(params.get("rl_salesrepnm")); // 영업사원
        String rl_orderno = Converter.toStr(params.get("rl_orderno")); // 어다반허
        String r_custcd = Converter.toStr(params.get("r_custcd")); // 거래처
        String r_shiptocd = Converter.toStr(params.get("r_shiptocd")); // 납품처
        
        params.put("r_ordersdt", r_ordersdt);
        params.put("r_orderedt", r_orderedt);
        params.put("r_actualshipsdt", r_actualshipsdt);
        params.put("r_actualshipedt", r_actualshipedt);
        params.put("rl_salesrepnm", rl_salesrepnm);
        params.put("rl_orderno", rl_orderno);
        params.put("r_custcd", r_custcd);
        params.put("r_shiptocd", r_shiptocd);
        
        // QMS 상태 조회
        String wherebody_status = "";
        String qms_status  = params.get("qms_status") !=null?params.get("qms_status").toString():"";
        String qms_status2 = params.get("qms_status2")!=null?params.get("qms_status2").toString():"";
        String qms_status3 = params.get("qms_status3")!=null?params.get("qms_status3").toString():"";
        
        //QMS 사전입력 여부
        String qms_preyn   = params.get("qms_preyn")!=null?params.get("qms_preyn").toString():"";
        
        if (!qms_status.equals("ALL")) {
            // QMS 생성 미완료
            if(qms_status.equals("N")) {
                wherebody_status += "dbo.SF_GETQMSID(ORDERNO,LINE_NO) IS NULL";
            }
            // QMS 생성완료
            if(qms_status.equals("Y")) {
                if(wherebody_status.length() > 1) {
                    wherebody_status+= " OR ";
                }
                wherebody_status += "dbo.SF_GETQMSID(ORDERNO,LINE_NO) IS NOT NULL";
            }
        }
        
        if (!qms_status2.equals("ALL")) {
            // MAIL 발송 미완료
            if(qms_status2.equals("N")) {
                if(wherebody_status.length() > 1) {
                    wherebody_status+= " AND ";
                }
                wherebody_status += "dbo.SF_GETMAILYN(ORDERNO,LINE_NO) = 'N' ";
            }
            
            // MAIL 발송완료
            if(qms_status2.equals("Y")) {
                if(wherebody_status.length() > 1) {
                    wherebody_status+= " AND ";
                }
                wherebody_status += "dbo.SF_GETMAILYN(ORDERNO,LINE_NO) = 'Y' ";
            }
        }
        
        if (!qms_status3.equals("ALL")) {
            // QMS 회신 미완료
            if(qms_status3.equals("N")) {
                if(wherebody_status.length() > 1) {
                    wherebody_status+= " AND ";
                }
                wherebody_status += "dbo.SF_GETFILEYN(ORDERNO,LINE_NO) = 'N' ";
            }
            
            // QMS 회신완료
            if(qms_status3.equals("Y")) {
                if(wherebody_status.length() > 1) {
                    wherebody_status+= " AND ";
                }
                wherebody_status += "dbo.SF_GETFILEYN(ORDERNO,LINE_NO) = 'Y' ";
            }
        }
        
        if (!qms_preyn.equals("ALL")) {
            // QMS 사전입력
            if(qms_preyn.equals("Y")) {
                if(wherebody_status.length() > 1) {
                    wherebody_status+= " AND ";
                }
                wherebody_status += "dbo.SF_GETpreorderyn(SO.cust_po) = 'Y' ";
            }
            
            // QMS 사후입력건
            if(qms_preyn.equals("N")) {
                if(wherebody_status.length() > 1) {
                    wherebody_status+= " AND ";
                }
                wherebody_status += "dbo.SF_GETpreorderyn(SO.cust_po) = 'N' ";
            }
        }
        
        if(!qms_status.equals("ALL") || !qms_status2.equals("ALL") || !qms_status3.equals("ALL") || !qms_preyn.equals("ALL")) {
            params.put("wherebody_status", wherebody_status);
        }
        
        return qmsOrderDao.getOrderDupCheck(params);
    }
    
    /**
     * Get QMS 메일일괄발송 getOrderDupCheck.
     * @작성일 : 2020. 6. 21.
     * @작성자 : jsh
     */
    public Object getOrderCustDupCheck(Map<String, Object> params, HttpServletRequest req, LoginDto loginDto){
        Map<String, Object> resMap = new HashMap<>();
        
        String where = Converter.toStr(params.get("where"));
        
        String r_ordersdt = Converter.toStr(params.get("r_ordersdt")); // 주문일자 검색 시작일.
        String r_orderedt = Converter.toStr(params.get("r_orderedt")); // 주문일자 검색 종료일.
        String r_actualshipsdt = Converter.toStr(params.get("r_actualshipsdt")); // 출고일자 검색 시작일.
        String r_actualshipedt = Converter.toStr(params.get("r_actualshipedt")); // 출고일자; 검색 종료일.
        if(!StringUtils.equals("", r_ordersdt)) r_ordersdt = r_ordersdt.replaceAll("-", "");
        if(!StringUtils.equals("", r_orderedt)) r_orderedt = r_orderedt.replaceAll("-", "");
        if(!StringUtils.equals("", r_actualshipsdt)) r_actualshipsdt = r_actualshipsdt.replaceAll("-", "");
        if(!StringUtils.equals("", r_actualshipedt)) r_actualshipedt = r_actualshipedt.replaceAll("-", "");

        String rl_salesrepnm = Converter.toStr(params.get("rl_salesrepnm")); // 영업사원
        String rl_orderno = Converter.toStr(params.get("rl_orderno")); // 어다반허
        String r_custcd = Converter.toStr(params.get("r_custcd")); // 거래처
        String r_shiptocd = Converter.toStr(params.get("r_shiptocd")); // 납품처
        
        params.put("r_ordersdt", r_ordersdt);
        params.put("r_orderedt", r_orderedt);
        params.put("r_actualshipsdt", r_actualshipsdt);
        params.put("r_actualshipedt", r_actualshipedt);
        params.put("rl_salesrepnm", rl_salesrepnm);
        params.put("rl_orderno", rl_orderno);
        params.put("r_custcd", r_custcd);
        params.put("r_shiptocd", r_shiptocd);
        
        // QMS 상태 조회
        String wherebody_status = "";
        String qms_status  = params.get("qms_status") !=null?params.get("qms_status").toString():"";
        String qms_status2 = params.get("qms_status2")!=null?params.get("qms_status2").toString():"";
        String qms_status3 = params.get("qms_status3")!=null?params.get("qms_status3").toString():"";
        
        //QMS 사전입력 여부
        String qms_preyn   = params.get("qms_preyn")!=null?params.get("qms_preyn").toString():"";
        
        if (!qms_status.equals("ALL")) {
            // QMS 생성 미완료
            if(qms_status.equals("N")) {
                wherebody_status += "dbo.SF_GETQMSID(ORDERNO,LINE_NO) IS NULL";
            }
            // QMS 생성완료
            if(qms_status.equals("Y")) {
                if(wherebody_status.length() > 1) {
                    wherebody_status+= " OR ";
                }
                wherebody_status += "dbo.SF_GETQMSID(ORDERNO,LINE_NO) IS NOT NULL";
            }
        }
        
        if (!qms_status2.equals("ALL")) {
            // MAIL 발송 미완료
            if(qms_status2.equals("N")) {
                if(wherebody_status.length() > 1) {
                    wherebody_status+= " AND ";
                }
                wherebody_status += "dbo.SF_GETMAILYN(ORDERNO,LINE_NO) = 'N' ";
            }
            
            // MAIL 발송완료
            if(qms_status2.equals("Y")) {
                if(wherebody_status.length() > 1) {
                    wherebody_status+= " AND ";
                }
                wherebody_status += "dbo.SF_GETMAILYN(ORDERNO,LINE_NO) = 'Y' ";
            }
        }
        
        if (!qms_status3.equals("ALL")) {
            // QMS 회신 미완료
            if(qms_status3.equals("N")) {
                if(wherebody_status.length() > 1) {
                    wherebody_status+= " AND ";
                }
                wherebody_status += "dbo.SF_GETFILEYN(ORDERNO,LINE_NO) = 'N' ";
            }
            
            // QMS 회신완료
            if(qms_status3.equals("Y")) {
                if(wherebody_status.length() > 1) {
                    wherebody_status+= " AND ";
                }
                wherebody_status += "dbo.SF_GETFILEYN(ORDERNO,LINE_NO) = 'Y' ";
            }
        }
        
        if (!qms_preyn.equals("ALL")) {
            // QMS 사전입력
            if(qms_preyn.equals("Y")) {
                if(wherebody_status.length() > 1) {
                    wherebody_status+= " AND ";
                }
                wherebody_status += "dbo.SF_GETpreorderyn(SO.cust_po) = 'Y' ";
            }
            
            // QMS 사후입력건
            if(qms_preyn.equals("N")) {
                if(wherebody_status.length() > 1) {
                    wherebody_status+= " AND ";
                }
                wherebody_status += "dbo.SF_GETpreorderyn(SO.cust_po) = 'N' ";
            }
        }
        
        if(!qms_status.equals("ALL") || !qms_status2.equals("ALL") || !qms_status3.equals("ALL") || !qms_preyn.equals("ALL")) {
            params.put("wherebody_status", wherebody_status);
        }
        
        return qmsOrderDao.getOrderCustDupCheck(params);
    }
    
    
    /**
     * QMS 오더 신규 사전 입력 목록 조회
     * @작성일 : 2021. 6. 21.
     * @작성자 : jsh
     */
    public List<Map<String, Object>> getQmsPopPreMastList(Map<String, Object> params){
        List<Map<String, Object>> qmsPopList = qmsOrderDao.getQmsPopPreMastList(params);
        return qmsPopList;
    }
    
    
    /**
     * QMS 오더 신규 사전 입력 목록 조회
     * @작성일 : 2021. 6. 21.
     * @작성자 : jsh
     */
    public List<Map<String, Object>> getQmsPopPreDetlList(Map<String, Object> params){
        List<Map<String, Object>> qmsPopPreDetlList = qmsOrderDao.getQmsPopPreDetlList(params);
        return qmsPopPreDetlList;
    }
    
    
    /**
     * QMS 오더 신규 사전 입력 목록 조회
     * @작성일 : 2021. 6. 21.
     * @작성자 : jsh
     */
    public List<Map<String, Object>> getQmsPreFireproofList(Map<String, Object> params){
        List<Map<String, Object>> qmsPreFireproofList = qmsOrderDao.getQmsPreFireproofList(params);
        return qmsPreFireproofList;
    }
    
    
    /**
     * QMS 오더 사전입력 강제생성
     * @작성일 : 2021. 7. 11.
     * @작성자 : jsh
     */
    public Object setQmsPreTestInsert(Map<String, Object> svcMap){
        return qmsOrderDao.setQmsPreTestInsert(svcMap);
    }
    
    /**
     * QMS 오더 사전입력 생성
     * @작성일 : 2021. 7. 11.
     * @작성자 : jsh
     */
    /*public Object syncQmsSalesOrder(){
        return qmsOrderDao.syncQmsSalesOrder();
    }*/
    
    public Object syncOrderDeleteUpdate(){
        return salesOrderDao.syncOrderDeleteUpdate();
    }
    
    public Object syncSalesOrderDeliveryComplete(){
        return salesOrderDao.syncSalesOrderDeliveryComplete();
    }
    
    
    
    /**
     * QMS 오더 사전입력 생성
     * @작성일 : 2021. 7. 11.
     * @작성자 : jsh
     */
    public Object syncPreQmsSalesOrder(){
        return qmsOrderDao.syncPreQmsSalesOrder();
    }
    
    /**
     * QMS 오더 사전입력 메일발송
     * @param params 
     * @작성일 : 2021. 7. 14.
     * @작성자 : jsh
     */
    public List<Map<String, Object>> getMailPreQmsOrderList(){
        return qmsOrderDao.getMailPreQmsOrderList();
    }
    
    /**
     * QMS 오더 사전입력(취소) 삭제처리
     * @작성일 : 2022. 1. 4.
     * @작성자 : jsh
     */
    public int setQmsPreOrderRemove(Map<String, Object> params){
        String reqNoTxt = null;
        String[] custPoArr = null;
        int workcnt = 0;
        if(params.get("custPoTxt") != null) {
            reqNoTxt = params.get("custPoTxt").toString();
        }
        
        if(reqNoTxt != null) {
            custPoArr = reqNoTxt.split(",");
        }
        
        for(String custPo : custPoArr) {
            params.put("custPo", custPo);
            String qmsTempId = qmsOrderDao.getFindPreQmsOrderId(params);
            if(qmsTempId!=null) {
                params.put("qmsTempId", qmsTempId);
                qmsOrderDao.setQmsPreOrderRemove(params);
                workcnt++;
            }
        }
        
        return workcnt;
    }






    /**
     * 2025-02-11 hsg Ankle Rock
     * 문자열 배열 안에 넘겨받은 문자가 있는지 확인.
     * array를 스트림으로 변환한 다음 target 문자열과 매칭되는게 있는 지 확인.
     * @param params
     * @작성일 : 2025. 2. 12.
     * @작성자 : hsg
     */
    public boolean checkInArray(String[] arr, String str){
        return Arrays.stream(arr).anyMatch(str::equals);
    }

    /**
     * 2025-02-11 hsg Ankle Rock
     * 문자열 배열 안에 넘겨받은 문자가 몇 개 있는지 확인
     * @param params
     * @작성일 : 2025. 2. 12.
     * @작성자 : hsg
     */
    public int countInArray(String[] arr, String target){
    	Map<String, Integer> frequencyMap = new HashMap<>();

        for (String str : arr) {
            frequencyMap.put(str, frequencyMap.getOrDefault(target, 0) + 1);
        }

    	return frequencyMap.getOrDefault(target, 0);
    }

    /**
     * 2025-02-11 hsg Ankle Rock
     * 문자열 배열 안에 넘겨받은 문자가 시작위치 이전에 있었는지 확인
     * @param params
     * @작성일 : 2025. 2. 12.
     * @작성자 : hsg
     */
    public boolean previousInArray(String[] arr, String target, int startNo){
    	boolean b = false ;

    	for(int i = startNo ; 0 <= i ; i--) {
	    	if( target.equals(arr[i]) ) {
	    		b = true;
	    		break;
	    	}
	    }

    	return b;
    }

    /**
     * 2025-02-11 hsg Ankle Rock
     * 문자열 배열 안에 넘겨받은 문자가 시작위치 이후에 있었는지 확인
     * @param params
     * @작성일 : 2025. 2. 12.
     * @작성자 : hsg
     */
    public boolean nextInArray(String[] arr, String target, int startNo){
    	boolean b = false ;

    	int arrLen = arr.length;
    	int nextNo = startNo + 1;
    	if(nextNo < arrLen) {
        	for ( int i = nextNo, j = arrLen ; i < j ; i++ ) {
    	    	if( target.equals(arr[i]) ) {
    	    		b = true;
    	    		break;
    	    	}
    	    }
    	}


    	return b;
    }





}
