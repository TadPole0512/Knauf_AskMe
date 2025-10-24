package com.limenets.eorder.dao;

import java.util.Map;

import javax.inject.Inject;

import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;

@Repository
public class LoginAuthDao {
@Inject private SqlSession sqlSession;
	
	public long merge(Map<String, Object> svcMap) {
		return sqlSession.insert("eorder.loginAuth.merge", svcMap);
	}
	
	public Map<String, Object> one(Map<String, Object> svcMap) {
		return sqlSession.selectOne("eorder.loginAuth.one", svcMap);
	}
}
