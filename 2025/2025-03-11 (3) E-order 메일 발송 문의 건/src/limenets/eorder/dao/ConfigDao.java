package com.limenets.eorder.dao;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;

@Repository
public class ConfigDao {
	@Inject private SqlSession sqlSession;
	
	public Map<String, Object> one(Map<String, Object> svcMap){
		return sqlSession.selectOne("eorder.config.one", svcMap);
	}
	
	public List<Map<String, Object>> list(Map<String, Object> svcMap){
		return sqlSession.selectList("eorder.config.list", svcMap);
	}
	
	public int up(Map<String, Object> svcMap){
		return sqlSession.update("eorder.config.up", svcMap);
	}
}
