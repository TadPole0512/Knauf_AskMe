package com.limenets.eorder.dao;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;

@Repository
public class CommonCodeDao {
	@Inject private SqlSession sqlSession;
	
	public int in(Map<String, Object> svcMap){
		return sqlSession.insert("eorder.commonCode.in", svcMap);
	}
	
	public int up(Map<String, Object> svcMap){
		return sqlSession.update("eorder.commonCode.up", svcMap);
	}

	public int upArr(Map<String, Object> svcMap){
		return sqlSession.update("eorder.commonCode.upArr", svcMap);
	}

	public Map<String, Object> one(Map<String, Object> svcMap) {
		return sqlSession.selectOne("eorder.commonCode.one", svcMap);
	}
	
	public List<Map<String, Object>> list(Map<String, Object> svcMap) {
		return sqlSession.selectList("eorder.commonCode.list", svcMap);
	}
	
	public List<Map<String, Object>> list2(Map<String, Object> svcMap) {
		return sqlSession.selectList("eorder.commonCode.list2", svcMap);
	}
	
	public List<Map<String, Object>> getCategoryListWithDepth(Map<String, Object> svcMap) {
		return sqlSession.selectList("eorder.commonCode.getCategoryListWithDepth", svcMap);
	}
	
	public int maxCode(Map<String, Object> svcMap){
		return sqlSession.selectOne("eorder.commonCode.maxCode", svcMap);
	}
	
	public int maxCodeForC01(Map<String, Object> svcMap){
		return sqlSession.selectOne("eorder.commonCode.maxCodeForC01", svcMap);
	}

	public int maxSort(Map<String, Object> svcMap) {
		return sqlSession.selectOne("eorder.commonCode.maxSort", svcMap);
	}
	
}
