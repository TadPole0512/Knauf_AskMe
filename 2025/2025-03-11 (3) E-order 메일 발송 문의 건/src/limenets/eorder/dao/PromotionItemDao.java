package com.limenets.eorder.dao;

import com.limenets.common.util.Converter;
import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

@Repository
public class PromotionItemDao {
	@Inject private SqlSession sqlSession;

	public int in(Map<String, Object> svcMap) {
		return sqlSession.insert("eorder.promotionItem.in", svcMap);
	}	
	
	public List<Map<String, Object>> list(Map<String, Object> svcMap) {
		return sqlSession.selectList("eorder.promotionItem.list", svcMap);
	}
	
	public int cnt(Map<String, Object> svcMap) {
		return Converter.toInt(sqlSession.selectOne("eorder.promotionItem.cnt", svcMap));
	}

	public int del(Map<String, Object> svcMap) {
		return sqlSession.delete("eorder.promotionItem.del", svcMap);
	}
}
