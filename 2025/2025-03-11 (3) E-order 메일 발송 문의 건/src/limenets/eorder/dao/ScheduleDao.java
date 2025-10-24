package com.limenets.eorder.dao;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;

import com.limenets.common.util.Converter;

@Repository
public class ScheduleDao {
	@Inject private SqlSession sqlSession;

	public int in(Map<String, Object> svcMap) {
		return sqlSession.insert("eorder.schedule.in", svcMap);
	}	
	
	public int up(Map<String, Object> svcMap) {
		return sqlSession.update("eorder.schedule.up", svcMap);
	}
	
	public List<Map<String, Object>> list(Map<String, Object> svcMap) {
		return sqlSession.selectList("eorder.schedule.list", svcMap);
	}
	
	public List<Map<String, Object>> listGroup(final Map<String, Object> svcMap) {
		return sqlSession.selectList("eorder.schedule.listGroup", svcMap);
	}
	
	public int cnt(Map<String, Object> svcMap) {
		return Converter.toInt(sqlSession.selectOne("eorder.schedule.cnt", svcMap));
	}
	
	public Map<String, Object> one(Map<String, Object> svcMap) {
		return sqlSession.selectOne("eorder.schedule.one", svcMap);
	}
	
	public int del(Map<String, Object> svcMap) {
		return sqlSession.delete("eorder.schedule.del", svcMap);
	}
}
