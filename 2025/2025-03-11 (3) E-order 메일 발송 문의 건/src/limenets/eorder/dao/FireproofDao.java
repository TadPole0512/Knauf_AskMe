package com.limenets.eorder.dao;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;

@Repository
public class FireproofDao {
	@Inject private SqlSession sqlSession;
	
	public int in(Map<String, Object> svcMap) {
	    if(svcMap.get("m_keycode")==null || svcMap.get("m_keycode").toString().equals("")) {
            svcMap.put("m_keycode",sqlSession.selectOne("eorder.fireproof.getMaxKeycode", svcMap));
        }
		return sqlSession.insert("eorder.fireproof.in", svcMap);
	}
	
	public int up(Map<String, Object> svcMap) {
		return sqlSession.update("eorder.fireproof.up", svcMap);
	}
	
	public int del(Map<String, Object> svcMap) {
        return sqlSession.insert("eorder.fireproof.del", svcMap);
    }

	public Map<String, Object> one(Map<String, Object> svcMap) {
		return sqlSession.selectOne("eorder.fireproof.one", svcMap);
	}
	
	public int upInfoForFile(Map<String, Object> svcMap) {
		return sqlSession.update("eorder.fireproof.upInfoForFile", svcMap);
	}
		
	public int upForFile(Map<String, Object> svcMap) {
		return sqlSession.update("eorder.fireproof.upForFile", svcMap);
	}
	
	
	public int cnt(Map<String, Object> svcMap) {
		return sqlSession.selectOne("eorder.fireproof.cnt", svcMap);
	}
	
	public List<Map<String, Object>> list(Map<String, Object> svcMap) {
		return sqlSession.selectList("eorder.fireproof.list", svcMap);
	}
	
	public int getFireProofCheck(Map<String, Object> svcMap) {
        return sqlSession.selectOne("eorder.fireproof.getFireProofCheck", svcMap);
    }
}
