package com.limenets.eorder.dao;

import com.limenets.common.util.Converter;
import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

@Repository
public class PromotionDao {
	@Inject private SqlSession sqlSession;

	public int in(Map<String, Object> svcMap) {
		return sqlSession.insert("eorder.promotion.in", svcMap);
	}	
	
	public int up(Map<String, Object> svcMap) {
		return sqlSession.update("eorder.promotion.up", svcMap);
	}

	public int del(Map<String, Object> svcMap) {
		return sqlSession.delete("eorder.promotion.del", svcMap);
	}

	public List<Map<String, Object>> list(Map<String, Object> svcMap) {
		return sqlSession.selectList("eorder.promotion.list", svcMap);
	}

	public List<Map<String, Object>> listForFront(Map<String, Object> svcMap) {
		return sqlSession.selectList("eorder.promotion.listForFront", svcMap);
	}

	public int cnt(Map<String, Object> svcMap) {
		return Converter.toInt(sqlSession.selectOne("eorder.promotion.cnt", svcMap));
	}
	
	public Map<String, Object> one(Map<String, Object> svcMap) {
		return sqlSession.selectOne("eorder.promotion.one", svcMap);
	}

	public String maxCode(Map<String, Object> svcMap){
		return sqlSession.selectOne("eorder.promotion.maxCode", svcMap);
	}
}
