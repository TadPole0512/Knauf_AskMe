package com.limenets.eorder.dao;

import java.util.Map;

import javax.inject.Inject;

import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;

@Repository
public class AppPushDao {
@Inject private SqlSession sqlSession;
	
	public int in(Map<String, Object> svcMap) {
		return sqlSession.insert("eorder.appPush.in", svcMap);
	}
	
	public int inByUser(Map<String, Object> svcMap){
		return sqlSession.insert("eorder.appPush.inByUser", svcMap);
	}
	
}
