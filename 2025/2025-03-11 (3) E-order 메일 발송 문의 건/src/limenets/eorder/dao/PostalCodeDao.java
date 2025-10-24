package com.limenets.eorder.dao;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;

@Repository
public class PostalCodeDao {
	@Inject private SqlSession sqlSession;
	
	public int cnt(Map<String, Object> svcMap) {
		return sqlSession.selectOne("eorder.o_postalcode_sap.cnt", svcMap);
	}
	
	public Map<String, Object> one(Map<String, Object> svcMap) {
		return sqlSession.selectOne("eorder.o_postalcode_sap.one", svcMap);
	}
	
	public List<Map<String, Object>> list(Map<String, Object> svcMap) {
		return sqlSession.selectList("eorder.o_postalcode_sap.list", svcMap);
	}
	
	public int in(Map<String, Object> svcMap) {
		return sqlSession.insert("eorder.o_postalcode_sap.in", svcMap);
	}
	
	public int up(Map<String, Object> svcMap) {
		return sqlSession.update("eorder.o_postalcode_sap.up", svcMap);
	}
}
