package com.limenets.eorder.dao;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;

import com.limenets.common.util.Converter;
import com.limenets.eorder.dto.BoardDto;

@Repository
public class BoardDao {
	@Inject private SqlSession sqlSession;

	public int in(Map<String, Object> svcMap) {
		return sqlSession.insert("eorder.board.in", svcMap);
	}	
	
	public int up(Map<String, Object> svcMap) {
		return sqlSession.update("eorder.board.up", svcMap);
	}

	
    public int sampleFrontUp(Map<String, Object> svcMap) {
        return sqlSession.update("eorder.board.sampleFrontUp", svcMap);
    }
    
    public int status(Map<String, Object> svcMap) {
        return sqlSession.update("eorder.board.status", svcMap);
    }	
    
    public int reply(Map<String, Object> svcMap) {
        return sqlSession.update("eorder.board.reply", svcMap);
    }	
	
	
	public List<BoardDto> list(Map<String, Object> svcMap) {
		return sqlSession.selectList("eorder.board.list", svcMap);
	}

	public List<BoardDto> listForIndex(Map<String, Object> svcMap) {
		return sqlSession.selectList("eorder.board.listForIndex", svcMap);
	}

	public List<BoardDto> listForLogin(Map<String, Object> svcMap) {
		return sqlSession.selectList("eorder.board.listForLogin", svcMap);
	}

	public int cnt(Map<String, Object> svcMap) {
		return Converter.toInt(sqlSession.selectOne("eorder.board.cnt", svcMap));
	}
	
	 public int cntrep(Map<String, Object> svcMap) {
	        return Converter.toInt(sqlSession.selectOne("eorder.board.cntrep", svcMap));
	 }
	
	public BoardDto one(Map<String, Object> svcMap) {
		return sqlSession.selectOne("eorder.board.one", svcMap);
	}
	
	   public BoardDto one2(Map<String, Object> svcMap) {
	        return sqlSession.selectOne("eorder.board.one2", svcMap);
	    }
	
	
	public int del(Map<String, Object> svcMap) {
		return sqlSession.delete("eorder.board.del", svcMap);
	}
	
	public int addViewCnt(Map<String, Object> svcMap) {
		return sqlSession.update("eorder.board.addViewCnt", svcMap);
	}
}
