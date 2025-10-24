package com.limenets.eorder.dao;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;

import com.limenets.common.util.Converter;

@Repository
public class PopupDao {

	@Inject
	private SqlSession sqlSession;
	
	public int in(Map<String, Object> svcMap) {
		return sqlSession.insert("eorder.popup.in", svcMap);
	}
	
	public int cnt( Map<String, Object> svcMap ){
		return Converter.toInt( sqlSession.selectOne( "eorder.popup.cnt", svcMap ) );
	}
	
	public Map<String, Object> one(Map<String, Object> svcMap) {
		return sqlSession.selectOne("eorder.popup.one", svcMap);
	}
	
	public List<Map<String, Object>> list(Map<String, Object> svcMap) {
		return sqlSession.selectList("eorder.popup.list", svcMap);
	}

	public List<Map<String, Object>> listForFront(Map<String, Object> svcMap) {
		return sqlSession.selectList("eorder.popup.listForFront", svcMap);
	}

	public long max() {
		return sqlSession.selectOne("eorder.popup.max", "");
	}

	public int del(Map<String, Object> svcMap) {
		return sqlSession.delete( "eorder.popup.del", svcMap );
	}
	
	public int up(Map<String, Object> svcMap) {
		return sqlSession.update("eorder.popup.up", svcMap);
	}
	
	public String getCpName(Map<String, Object> svcMap) {
		return sqlSession.selectOne("eorder.popup.getCpName", svcMap);
	}


}
