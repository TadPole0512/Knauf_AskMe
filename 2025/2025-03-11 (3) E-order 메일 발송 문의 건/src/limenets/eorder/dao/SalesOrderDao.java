package com.limenets.eorder.dao;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class SalesOrderDao {
	@Inject private SqlSession sqlSession;
	
	public Map<String, Object> one(Map<String, Object> svcMap) {
		return sqlSession.selectOne("eorder.o_salesorder.one", svcMap);
	}
	
	public int cnt(Map<String, Object> svcMap) {
		return sqlSession.selectOne("eorder.o_salesorder.cnt", svcMap);
	}
	
	public List<Map<String, Object>> list(Map<String, Object> svcMap) {
		return sqlSession.selectList("eorder.o_salesorder.list", svcMap);
	}
	
	public int cntGroup(Map<String, Object> svcMap) {
		return sqlSession.selectOne("eorder.o_salesorder.cntGroup", svcMap);
	}
	
	public int cntForItem(Map<String, Object> svcMap) {
		return sqlSession.selectOne("eorder.o_salesorder.cntForItem", svcMap);
	}
	
	public List<Map<String, Object>> listForItem(Map<String, Object> svcMap) {
		return sqlSession.selectList("eorder.o_salesorder.listForItem", svcMap);
	}
	
	public List<Map<String, Object>> getOrderNoGroup(final Map<String, Object> svcMap) {
		return sqlSession.selectList("eorder.o_salesorder.getOrderNoGroup", svcMap);
	}
	
	// Start. For 납품확인서 리스트.
	public List<Map<String, Object>> getOrderShipGroup(Map<String, Object> svcMap) {
		return sqlSession.selectList("eorder.o_salesorder.getOrderShipGroup", svcMap);
	}
	
	public List<Map<String, Object>> getOrderShipNmGroup(Map<String, Object> svcMap) {
		return sqlSession.selectList("eorder.o_salesorder.getOrderShipNmGroup", svcMap);
	}
	
	/*
	public List<Map<String, Object>> getOrderItemGroupItem(Map<String, Object> svcMap) {
		return sqlSession.selectList("eorder.o_salesorder.getOrderItemGroupItem", svcMap);
	}
	*/
	
	
	// USE.
	public int getCntReportFor1020(Map<String, Object> svcMap) {
		return sqlSession.selectOne("eorder.o_salesorder.getCntReportFor1020", svcMap);
	}
	// USE.
	public List<Map<String, Object>> getReportFor1020(Map<String, Object> svcMap) {
		return sqlSession.selectList("eorder.o_salesorder.getReportFor1020", svcMap);
	}
	
	// USE.
	public int getCntReportFor1121(Map<String, Object> svcMap) {
		return sqlSession.selectOne("eorder.o_salesorder.getCntReportFor1121", svcMap);
	}
	// USE.
	public List<Map<String, Object>> getReportFor1121(Map<String, Object> svcMap) {
		return sqlSession.selectList("eorder.o_salesorder.getReportFor1121", svcMap);
	}
    // USE.
    public List<Map<String, Object>> getReportFor1222(Map<String, Object> svcMap) {
        return sqlSession.selectList("eorder.o_salesorder.getReportFor1222", svcMap);
    }
	// USE.
	public Map<String, Object> getReportPeriodDate(Map<String, Object> svcMap) {
		return sqlSession.selectOne("eorder.o_salesorder.getReportPeriodDate", svcMap);
	}
	
	public List<Map<String, Object>> getOrderAdd1Group(Map<String, Object> svcMap) {
		return sqlSession.selectList("eorder.o_salesorder.getOrderAdd1Group", svcMap);
	}
	
	// USE.
	public List<Map<String, Object>> getOrderAdd1Group2(Map<String, Object> svcMap) {
		return sqlSession.selectList("eorder.o_salesorder.getOrderAdd1Group2", svcMap);
	}
	
	// USE.
	public List<Map<String, Object>> getOrderItemDescGroup(Map<String, Object> svcMap) {
		return sqlSession.selectList("eorder.o_salesorder.getOrderItemDescGroup", svcMap);
	}
	
	public List<Map<String, Object>> getDeliveryPaperList(Map<String, Object> svcMap) {
		return sqlSession.selectList("eorder.o_salesorder.getDeliveryPaperList", svcMap);
	}
	
	public List<Map<String, Object>> getDeliveryPaperListForItemGroup(Map<String, Object> svcMap) {
		return sqlSession.selectList("eorder.o_salesorder.getDeliveryPaperListForItemGroup", svcMap);
	}
	
	// USE.
	public Map<String, Object> getCustInfoForReport(Map<String, Object> svcMap){
		return sqlSession.selectOne("eorder.o_salesorder.getCustInfoForReport", svcMap);
	}
	// End. For 납품확인서 리스트.
	
	
	// Start. 배차완료 푸시를 위한.
	public List<Map<String, Object>> getListForSendPush(Map<String, Object> svcMap){
		return sqlSession.selectList("eorder.o_salesorder.getListForSendPush", svcMap);
	}
	public List<Map<String, Object>> getListForSendPushGroup(Map<String, Object> svcMap){
		return sqlSession.selectList("eorder.o_salesorder.getListForSendPushGroup", svcMap);
	}
	public int inSalesOrderPush(Map<String, Object> svcMap){
		return sqlSession.insert("eorder.o_salesorder.inSalesOrderPush", svcMap);
	}
	// END. 배차완료 푸시를 위한.
	
	@Transactional
    public int syncOrderDeleteUpdate() {
        return sqlSession.update("eorder.o_salesorder.syncOrderDeleteUpdate");
    }
	
	
	public List<Map<String, Object>> getShiptoListWithQuoteQt(Map<String, Object> svcMap){
		return sqlSession.selectList("eorder.o_salesorder.getShiptoListWithQuoteQt", svcMap);
	}
	
	@Transactional
    public int syncSalesOrderDeliveryComplete() {
        return sqlSession.update("eorder.o_salesorder.syncSalesOrderDeliveryComplete");
    }
}
