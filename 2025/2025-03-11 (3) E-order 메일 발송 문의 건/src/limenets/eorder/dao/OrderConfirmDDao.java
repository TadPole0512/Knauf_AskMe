package com.limenets.eorder.dao;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;

@Repository
public class OrderConfirmDDao {
	@Inject private SqlSession sqlSession;
	
	public int in(Map<String, Object> svcMap) {
		return sqlSession.insert("eorder.o_order_confirm_d.in", svcMap);
	}
	
	public int up(Map<String, Object> svcMap) {
		return sqlSession.update("eorder.o_order_confirm_d.up", svcMap);
	}
	
	public int maxLineNo(Map<String, Object> svcMap) {
		return sqlSession.selectOne("eorder.o_order_confirm_d.maxLineNo", svcMap);
	}
	
	public List<Map<String, Object>> list(Map<String, Object> svcMap) {
		return sqlSession.selectList("eorder.o_order_confirm_d.list", svcMap);
	}
}
