package com.limenets.eorder.dao;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;

@Repository
public class OrderHeaderHistoryDao {
	@Inject private SqlSession sqlSession;
	
	public int in(Map<String, Object> svcMap) {
		return sqlSession.insert("eorder.orderHeaderHistory.in", svcMap);
	}
	
	public int upForReqNoRef(Map<String, Object> svcMap) {
		return sqlSession.update("eorder.orderHeaderHistory.upForReqNoRef", svcMap);
	}
	
	public Map<String, Object> oneByRecentList(Map<String, Object> svcMap) {
		return sqlSession.selectOne("eorder.orderHeaderHistory.oneByRecentList", svcMap);
	}
	
	public int cnt(Map<String, Object> svcMap) {
		return sqlSession.selectOne("eorder.orderHeaderHistory.cnt", svcMap);
	}
	
	public List<Map<String, Object>> list(Map<String, Object> svcMap) {
		return sqlSession.selectList("eorder.orderHeaderHistory.list", svcMap);
	}
}
