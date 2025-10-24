package com.limenets.eorder.dao;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;

@Repository
public class CsSalesMapDao {
@Inject private SqlSession sqlSession;
	
	public int in(Map<String, Object> svcMap) {
		return sqlSession.insert("eorder.o_cssalesmap.in", svcMap);
	}
	
	public int merge(Map<String, Object> svcMap) {
		return sqlSession.update("eorder.o_cssalesmap.merge", svcMap);
	}
	
	public int up(Map<String, Object> svcMap) {
		return sqlSession.update("eorder.o_cssalesmap.up", svcMap);
	}
	
	public int del(Map<String, Object> svcMap) {
		return sqlSession.delete("eorder.o_cssalesmap.del", svcMap);
	}
	public int del2(Map<String, Object> svcMap) {
		return sqlSession.delete("eorder.o_cssalesmap.del2", svcMap);
	}
	
	public int cnt(Map<String, Object> svcMap) {
		return sqlSession.selectOne("eorder.o_cssalesmap.cnt", svcMap);
	}
	
	public Map<String, Object> one(Map<String, Object> svcMap) {
		return sqlSession.selectOne("eorder.o_cssalesmap.one", svcMap);
	}
	
	public List<Map<String, Object>> list(Map<String, Object> svcMap) {
		return sqlSession.selectList("eorder.o_cssalesmap.list", svcMap);
	}

}
