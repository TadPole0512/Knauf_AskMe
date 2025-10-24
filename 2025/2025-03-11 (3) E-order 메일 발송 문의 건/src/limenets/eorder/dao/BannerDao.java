package com.limenets.eorder.dao;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;

import com.limenets.common.util.Converter;

@Repository
public class BannerDao {
	@Inject private SqlSession sqlSession;
	
	public int in(Map<String, Object> svcMap) {
		return sqlSession.insert("eorder.banner.in", svcMap);
	}
	
	public int up(Map<String, Object> svcMap) {
		return sqlSession.update("eorder.banner.up", svcMap);
	}

	public int del(Map<String, Object> svcMap) {
		return sqlSession.delete("eorder.banner.del", svcMap);
	}

	public Map<String, Object> one(Map<String, Object> svcMap) {
		return sqlSession.selectOne("eorder.banner.one", svcMap);
	}
	
	public int cnt(Map<String, Object> svcMap) {
		return Converter.toInt(sqlSession.selectOne("eorder.banner.cnt", svcMap));
	}

	public List<Map<String, Object>> list(Map<String, Object> svcMap) {
		return sqlSession.selectList("eorder.banner.list", svcMap);
	}

	public List<Map<String, Object>> listForFront(Map<String, Object> svcMap) {
		return sqlSession.selectList("eorder.banner.listForFront", svcMap);
	}
}
