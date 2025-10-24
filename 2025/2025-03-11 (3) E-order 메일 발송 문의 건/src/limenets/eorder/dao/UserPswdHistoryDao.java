package com.limenets.eorder.dao;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;

@Repository
public class UserPswdHistoryDao {
	@Inject private SqlSession sqlSession;
	
	public int in(Map<String, Object> params) {
		return sqlSession.insert("eorder.userPswdHistory.in", params);
	}
	
	public int in2(Map<String, Object> params) {
		return sqlSession.insert("eorder.userPswdHistory.in2", params);
	}
	
	public List<Map<String, Object>> list(Map<String, Object> svcMap) {
		return sqlSession.selectList("eorder.userPswdHistory.list", svcMap);
	}
	
	public int inHistoryByShiptoUser(Map<String, Object> svcMap) {
		return sqlSession.update("eorder.userPswdHistory.inHistoryByShiptoUser", svcMap);
	}

	public int cntForUpdateCheck(Map<String, Object> svcMap) {
		return sqlSession.selectOne("eorder.userPswdHistory.cntForUpdateCheck", svcMap);
	}
	
	public Map<String, Object> getLastHistory(final Map<String, Object> svcMap) {
		return sqlSession.selectOne("eorder.userPswdHistory.getLastHistory", svcMap);
	}
}
