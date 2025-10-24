package com.limenets.eorder.dao;

import java.util.Map;

import javax.inject.Inject;

import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;

@Repository
public class ErrorLogDao {
	@Inject private SqlSession sqlSession;
	
	public int in(Map<String, Object> params) {
		return sqlSession.insert("eorder.errorLog.in", params);
	}
}
