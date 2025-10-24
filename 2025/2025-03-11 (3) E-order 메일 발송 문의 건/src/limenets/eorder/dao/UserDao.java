package com.limenets.eorder.dao;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;

@Repository
public class UserDao {
	@Inject private SqlSession sqlSession;
	
	public int in(Map<String, Object> svcMap) {
		return sqlSession.insert("eorder.o_user.in", svcMap);
	}
	
	public int up(Map<String, Object> svcMap) {
		return sqlSession.update("eorder.o_user.up", svcMap);
	}
	public int upByArr(Map<String, Object> svcMap) {
		return sqlSession.update("eorder.o_user.upByArr", svcMap);
	}
	
	public Map<String, Object> one(Map<String, Object> svcMap) {
		return sqlSession.selectOne("eorder.o_user.one", svcMap);
	}
	public Map<String, Object> oneAll(Map<String, Object> svcMap) {
		return sqlSession.selectOne("eorder.o_user.oneAll", svcMap);
	}

	public int cnt(Map<String, Object> svcMap) {
		return sqlSession.selectOne("eorder.o_user.cnt", svcMap);
	}
	public int cntAll(Map<String, Object> svcMap) {
		return sqlSession.selectOne("eorder.o_user.cntAll", svcMap);
	}
	
	public List<Map<String, Object>> list(Map<String, Object> svcMap) {
		return sqlSession.selectList("eorder.o_user.list", svcMap);
	}
	
	public List<Map<String, Object>> listForAppPush(Map<String, Object> svcMap) {
		return sqlSession.selectList("eorder.o_user.listForAppPush", svcMap);
	}
	
	public List<Map<String, Object>> getUserAuthorityList(Map<String, Object> svcMap) {
		return sqlSession.selectList("eorder.o_user.getUserAuthorityList", svcMap);
	}
	
	public int getUserSortMax(Map<String, Object> svcMap) {
		return sqlSession.selectOne("eorder.o_user.getUserSortMax", svcMap);
	}
	
	public int inShiptoUserByArr(Map<String, Object> svcMap) {
		return sqlSession.update("eorder.o_user.inShiptoUserByArr", svcMap);
	}
	
	public int inLoginLog(final Map<String, Object> svcMap) {
		return sqlSession.insert("eorder.o_user.inLoginLog", svcMap);
	}
	
	public Map<String, Object> checkForLock(Map<String, Object> svcMap) {
		return sqlSession.selectOne("eorder.o_user.checkForLock", svcMap);
	}
	
	public int loginCnt(Map<String, Object> svcMap) {
		return sqlSession.selectOne("eorder.o_user.loginCnt", svcMap);
	}
	
	public Map<String, Object> indexCntMap(Map<String, Object> svcMap) {
		return sqlSession.selectOne("eorder.o_user.indexCntMap", svcMap);
	}
	
	public List<Map<String, Object>> listForSalesUserCategory(Map<String, Object> svcMap) {
		return sqlSession.selectList("eorder.o_user.listForSalesUserCategory", svcMap);
	}
	
	public int cntCustomerUser(Map<String, Object> param) {
		return sqlSession.selectOne("cntCustomerUser", param);
	}
	
	public int inCustomerUser(Map<String, Object> param) {
		return sqlSession.insert("inCustomerUser", param);
	}
	
	// ########## START. O_USER2 ##########
	public int checkSalesUser(Map<String, Object> svcMap) {
		return sqlSession.selectOne("eorder.o_user.checkSalesUser", svcMap);
	}
	
	public int getUser2ParentSort(Map<String, Object> svcMap) {
		return sqlSession.selectOne("eorder.o_user.getUser2ParentSort", svcMap);
	}
	
	public int getUser2SortMax(Map<String, Object> svcMap) {
		return sqlSession.selectOne("eorder.o_user.getUser2SortMax", svcMap);
	}
	
	public int getUser2SeqMax(Map<String, Object> svcMap) {
		return sqlSession.selectOne("eorder.o_user.getUser2SeqMax", svcMap);
	}
	
	public int inUser2(Map<String, Object> svcMap) {
		return sqlSession.insert("eorder.o_user.inUser2", svcMap);
	}
	public int inUser2ForDefault(Map<String, Object> svcMap) {
		return sqlSession.insert("eorder.o_user.inUser2ForDefault", svcMap);
	}
	
	public List<Map<String, Object>> getUser2AuthorityList(Map<String, Object> svcMap) {
		return sqlSession.selectList("eorder.o_user.getUser2AuthorityList", svcMap);
	}
	
	public int delForUser2Insert(Map<String, Object> svcMap) {
		return sqlSession.delete("eorder.o_user.delForUser2Insert", svcMap);
	}
	
	public int delUser2(Map<String, Object> svcMap) {
		return sqlSession.delete("eorder.o_user.delUser2", svcMap);
	}
	
	public int inForUser2(Map<String, Object> svcMap) {
		return sqlSession.insert("eorder.o_user.inForUser2", svcMap);
	}
	
	public int upUser2(Map<String, Object> svcMap) {
		return sqlSession.update("eorder.o_user.upUser2", svcMap);
	}
	// ########## END. O_USER2 ##########
}
