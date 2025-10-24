package com.limenets.eorder.dao;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;

@Repository
public class ItemDao {
	@Inject private SqlSession sqlSession;
	
	/**
	 * O_ITEM Table.
	 */
	public Map<String, Object> one(Map<String, Object> svcMap) {
		return sqlSession.selectOne("eorder.o_item_new.one", svcMap);
	}
	
	public int cnt(Map<String, Object> svcMap) {
		return sqlSession.selectOne("eorder.o_item_new.cnt", svcMap);
	}

	public int cntByItemcd(Map<String, Object> svcMap) {
		return sqlSession.selectOne("eorder.o_item_new.cntByItemcd", svcMap);
	}

	public List<Map<String, Object>> list(Map<String, Object> svcMap) {
		return sqlSession.selectList("eorder.o_item_new.list", svcMap);
	}
	
	public int itemManageCnt(Map<String, Object> svcMap) {
		return sqlSession.selectOne("eorder.o_item_new.itemManageCnt", svcMap);
	}
	
	public List<Map<String, Object>> itemManageList(Map<String, Object> svcMap) {
		return sqlSession.selectList("eorder.o_item_new.itemManageList", svcMap);
	}
	
	public List<Map<String, Object>> listForCategory(Map<String, Object> svcMap) {
		return sqlSession.selectList("eorder.o_item_new.listForCategory", svcMap);
	}
	
	/**
	 * ITEMINFO Table.
	 */
	public int mergeInfo(Map<String, Object> svcMap) {
		return sqlSession.insert("eorder.o_item_new.mergeInfo", svcMap);
	}
	
	public int upInfoForFile(Map<String, Object> svcMap) {
		return sqlSession.update("eorder.o_item_new.upInfoForFile", svcMap);
	}

	public Map<String, Object> oneInfo(Map<String, Object> svcMap) {
		return sqlSession.selectOne("eorder.o_item_new.oneInfo", svcMap);
	}

	/**
	 * ITEMRECOMMEND Table.
	 */
	public int inRecommend(Map<String, Object> svcMap) {
		return sqlSession.insert("eorder.o_item_new.inRecommend", svcMap);
	}
	
	public int inRecommendByArr(Map<String, Object> svcMap) {
		return sqlSession.update("eorder.o_item_new.inRecommendByArr", svcMap);
	}
	
	public int delRecommend(Map<String, Object> svcMap) {
		return sqlSession.delete("eorder.o_item_new.delRecommend", svcMap);
	}
	
	public List<Map<String, Object>> listRecommend(Map<String, Object> svcMap) {
		return sqlSession.selectList("eorder.o_item_new.listRecommend", svcMap);
	}
	
	/**
	 * ITEMRESEARCH Table.
	 */
	public int inSearch(Map<String, Object> svcMap) {
		return sqlSession.insert("eorder.o_item_new.inSearch", svcMap);
	}
	
	public int inSearchByArr(Map<String, Object> svcMap) {
		return sqlSession.update("eorder.o_item_new.inSearchByArr", svcMap);
	}
	
	public int delSearch(Map<String, Object> svcMap) {
		return sqlSession.delete("eorder.o_item_new.delSearch", svcMap);
	}
	
	/**
	 * ITEMBOOKMARK Table.
	 */
	public int mergeBookmark(Map<String, Object> svcMap) {
		return sqlSession.insert("eorder.o_item_new.mergeBookmark", svcMap);
	}
	
	public int delBookmark(Map<String, Object> svcMap) {
		return sqlSession.delete("eorder.o_item_new.delBookmark", svcMap);
	}
	
	public List<Map<String, Object>> listBookmarkForInsert(Map<String, Object> svcMap) {
		return sqlSession.selectList("eorder.o_item_new.listBookmarkForInsert", svcMap);
	}
	
	public int cntForBookmark(Map<String, Object> svcMap) {
		return sqlSession.selectOne("eorder.o_item_new.cntForBookmark", svcMap);
	}

	public List<Map<String, Object>> listItemMcu(Map<String, Object> svcMap) {
		return sqlSession.selectList("eorder.o_item_new.listItemMcu", svcMap);
	}
	
	public int updateLineTy(Map<String, Object> svcMap) {
		return sqlSession.update("eorder.o_item_new.updateLineTy", svcMap);
	}
	
}
