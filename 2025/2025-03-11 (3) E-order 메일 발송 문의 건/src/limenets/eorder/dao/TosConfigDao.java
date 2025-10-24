package com.limenets.eorder.dao;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;

import com.limenets.common.util.Converter;
import com.limenets.eorder.dto.TosConfigDto;

@Repository
public class TosConfigDao {
	@Inject private SqlSession sqlSession;

	public int in(Map<String, Object> svcMap) {
		return sqlSession.insert("eorder.tosConfig.in", svcMap);
	}	
	
	public int up(Map<String, Object> svcMap) {
		return sqlSession.update("eorder.tosConfig.up", svcMap);
	}
	
	public List<TosConfigDto> list(Map<String, Object> svcMap) {
		return sqlSession.selectList("eorder.tosConfig.list", svcMap);
	}
	
	public int cnt(Map<String, Object> svcMap) {
		return Converter.toInt(sqlSession.selectOne("eorder.tosConfig.cnt", svcMap));
	}
	
	public TosConfigDto one(Map<String, Object> svcMap) {
		return sqlSession.selectOne("eorder.tosConfig.one", svcMap);
	}

	public TosConfigDto oneForIndex(Map<String, Object> svcMap) {
		return sqlSession.selectOne("eorder.tosConfig.oneForIndex", svcMap);
	}
	
	public int del(Map<String, Object> svcMap) {
		return sqlSession.delete("eorder.tosConfig.del", svcMap);
	}
}
