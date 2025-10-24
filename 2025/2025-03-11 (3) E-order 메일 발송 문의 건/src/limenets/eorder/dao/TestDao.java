package com.limenets.eorder.dao;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;

import com.limenets.eorder.dto.TestDto;

@Repository
public class TestDao {
	@Inject private SqlSession sqlSession;
	
	public int in(Map<String, Object> params) {
		return sqlSession.insert("eorder.test.in", params);
	}
	
	public TestDto one(Map<String, Object> params){
		return sqlSession.selectOne("eorder.test.one", params);
	}
	
	public List<TestDto> list(Map<String, Object> params){
		return sqlSession.selectList("eorder.test.list");
	}
}
