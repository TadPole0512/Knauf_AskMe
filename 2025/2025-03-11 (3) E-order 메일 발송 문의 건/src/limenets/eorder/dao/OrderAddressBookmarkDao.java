package com.limenets.eorder.dao;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;

@Repository
public class OrderAddressBookmarkDao {
	@Inject private SqlSession sqlSession;
	
	public int in(Map<String, Object> svcMap) {
		return sqlSession.insert("eorder.orderAddressBookmark.in", svcMap);
	}
	
	public int del(Map<String, Object> svcMap) {
		return sqlSession.delete("eorder.orderAddressBookmark.del", svcMap);
	}
	
	public int delByArr(Map<String, Object> svcMap) {
		return sqlSession.delete("eorder.orderAddressBookmark.delByArr", svcMap);
	}
	
	public Map<String, Object> one(Map<String, Object> svcMap) {
		return sqlSession.selectOne("eorder.orderAddressBookmark.one", svcMap);
	}
	
	public int cnt(Map<String, Object> svcMap) {
		return sqlSession.selectOne("eorder.orderAddressBookmark.cnt", svcMap);
	}
	
	public List<Map<String, Object>> list(Map<String, Object> svcMap) {
		return sqlSession.selectList("eorder.orderAddressBookmark.list", svcMap);
	}
	
}
