package com.limenets.eorder.dao;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;

@Repository
public class CommonDao {
	@Inject private SqlSession sqlSession;
	
	/**
	 * 휴일 리스트 가져오기. O_DATE Table.
	 */
	public List<Map<String, Object>> getHolyDayList(Map<String, Object> svcMap) {
		return sqlSession.selectList("eorder.common.getHolyDayList", svcMap);
	}
	
	/**
	 * 주차 리스트 가져오기. O_F0005 Table. From JDE.
	 */
	public List<Map<String, Object>> getOrderWeekList(Map<String, Object> svcMap) {
		return sqlSession.selectList("eorder.common.getOrderWeekList", svcMap);
	}
	
	/**
	 * 품목 재고 가져오기.
	 */
	/*public Map<String, Object> getItemStock(Map<String, Object> svcMap) {
		return sqlSession.selectOne("eorder.common.getItemStock", svcMap);
	}
	public Map<String, Object> getItemStock2(Map<String, Object> svcMap) {
		return sqlSession.selectOne("eorder.common.getItemStock2", svcMap);
	}*/
	
	/**
	 * 거래처 가상계좌번호 가져오기.
	 */
	/*public Map<String, Object> getCustVAcount(Map<String, Object> svcMap) {
		return sqlSession.selectOne("eorder.common.getCustVAcount", svcMap);
	}*/
}
