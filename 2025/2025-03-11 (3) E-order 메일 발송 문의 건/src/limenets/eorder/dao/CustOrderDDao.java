package com.limenets.eorder.dao;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;

@Repository
public class CustOrderDDao {
	@Inject private SqlSession sqlSession;
	
	public int in(Map<String, Object> svcMap) {
		return sqlSession.insert("eorder.o_cust_order_d.in", svcMap);
	}
	
	public int up(Map<String, Object> svcMap) {
		return sqlSession.update("eorder.o_cust_order_d.up", svcMap);
	}
	
	public int upReqNo(Map<String, Object> svcMap) {
		return sqlSession.update("eorder.o_cust_order_d.upReqNo", svcMap);
	}
	
	public int del(Map<String, Object> svcMap) {
		return sqlSession.update("eorder.o_cust_order_d.del", svcMap);
	}
	
	public Map<String, Object> one(Map<String, Object> svcMap) {
		return sqlSession.selectOne("eorder.o_cust_order_d.one", svcMap);
	}
	
	public int cnt(Map<String, Object> svcMap) {
		return sqlSession.selectOne("eorder.o_cust_order_d.cnt", svcMap);
	}
	
	public List<Map<String, Object>> list(Map<String, Object> svcMap) {
		return sqlSession.selectList("eorder.o_cust_order_d.list", svcMap);
	}
}
