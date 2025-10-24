package com.limenets.eorder.dao;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;

@Repository
public class AccumSapDao {
	@Inject private SqlSession sqlSession;

	public List<Map<String, Object>> getNotShippedVolume(Map<String, Object> svcMap){
		return sqlSession.selectList("eorder.accumSap.getNotShippedVolume", svcMap);
	}
	
	public List<Map<String, Object>> getShippedVolume(Map<String, Object> svcMap){
		return sqlSession.selectList("eorder.accumSap.getShippedVolume", svcMap);
	}
	
	public List<Map<String, Object>> getPlantList(Map<String, Object> svcMap){
		return sqlSession.selectList("eorder.plant.list", svcMap);
	}
}
