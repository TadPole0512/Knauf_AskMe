package com.limenets.eorder.dao;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;

import com.limenets.common.util.Converter;

@Repository
public class ReportDao {
	@Inject private SqlSession sqlSession;
	
	//공급자,공급받는자
	public Map<String, Object> vSupplier(Map<String, Object> svcMap) {
		return sqlSession.selectOne("eorder.report.vSupplier", svcMap);
	}
	
	//거래내역
	public List<Map<String, Object>> vClosedSalesOrder(Map<String, Object> svcMap) {
		return sqlSession.selectList("eorder.report.vClosedSalesOrder", svcMap);
	}
	
	//전월채권,당월매출,현금수금,어음수금,당월채권
	public Map<String, Object> vSumPrice(Map<String, Object> svcMap) {
		return sqlSession.selectOne("eorder.report.vSumPrice", svcMap);
	}
	
	//미도래어음
	public long getFailPrice(Map<String, Object> svcMap) {
		return Converter.toInt(sqlSession.selectOne("eorder.report.getFailPrice", svcMap));
	}
	
	//거래처 입금계좌
	public Map<String, Object> getAccount(Map<String, Object> svcMap) {
		return sqlSession.selectOne("eorder.report.getAccount", svcMap);
	}
	
	//메일전송이력 저장
	public int inSendMailHistory(Map<String, Object> svcMap) {
		return sqlSession.insert("eorder.report.inSendMailHistory", svcMap);
	}
	
	//메일전송이력
	public List<Map<String, Object>> sendMailHistoryList(Map<String, Object> svcMap) {
		return sqlSession.selectList("eorder.report.sendMailHistoryList", svcMap);
	}
}
