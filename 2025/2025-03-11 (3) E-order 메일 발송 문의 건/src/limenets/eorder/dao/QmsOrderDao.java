package com.limenets.eorder.dao;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class QmsOrderDao {
	@Inject private SqlSession sqlSession;
	

	public List<Map<String, Object>> list(Map<String, Object> svcMap) {
		return sqlSession.selectList("eorder.o_qmsorder.list", svcMap);
	}
	
	public List<Map<String, Object>> getQmsPopDetlGridList(Map<String, Object> svcMap) {
	    return sqlSession.selectList("eorder.o_qmsorder.getQmsPopDetlGridList", svcMap);
	}
	
	public int cnt(Map<String, Object> svcMap) {
		return sqlSession.selectOne("eorder.o_qmsorder.cnt", svcMap);
	}

    public String getQmsOrderId(Map<String, Object> svcMap) {
        return sqlSession.selectOne("eorder.o_qmsorder.getQmsOrderId", svcMap);
    }
    
    public int getQmsOrderSeq(Map<String, Object> svcMap) {
        return sqlSession.selectOne("eorder.o_qmsorder.getQmsOrderSeq", svcMap);
    }

    public int setQmsOrderMast(Map<String, Object> svcMap) {
        return sqlSession.insert("eorder.o_qmsorder.setQmsOrderMast", svcMap);
    }

    public int setQmsOrderMastSplit(Map<String, Object> svcMap) {
        return sqlSession.insert("eorder.o_qmsorder.setQmsOrderMastSplit", svcMap);
    }
    
    
    
    public int setQmsOrderDetlSplit(Map<String, Object> svcMap) {
        return sqlSession.insert("eorder.o_qmsorder.setQmsOrderDetlSplit", svcMap);
    }

    public int setQmsOrderDetl(Map<String, Object> svcMap) {
        return sqlSession.insert("eorder.o_qmsorder.setQmsOrderDetl", svcMap);
    }
    

    public int setQmsOrderMastTempUpdate(Map<String, Object> svcMap) {
        return sqlSession.update("eorder.o_qmsorder.setQmsOrderMastTempUpdate", svcMap);
    }
    

    public int setQmsOrderMastUpdate(Map<String, Object> svcMap) {
        return sqlSession.update("eorder.o_qmsorder.setQmsOrderMastUpdate", svcMap);
    }
    
    public int setQmsOrderPreMastUpdate(Map<String, Object> svcMap) {
        return sqlSession.update("eorder.o_qmsorder.setQmsOrderPreMastUpdate", svcMap);
    }
    
    public int setQmsOrderMastHistory(Map<String, Object> svcMap) {
        return sqlSession.update("eorder.o_qmsorder.setQmsOrderMastHistory", svcMap);
    }
    
    public int setQmsOrderMastDelete(Map<String, Object> svcMap) {
        return sqlSession.update("eorder.o_qmsorder.setQmsOrderMastDelete", svcMap);
    }

    public int setQmsOrderDetlDelete(Map<String, Object> svcMap) {
        return sqlSession.update("eorder.o_qmsorder.setQmsOrderDetlDelete", svcMap);
    }
    

    public int setQmsOrderFireDelete(Map<String, Object> svcMap) {
        return sqlSession.update("eorder.o_qmsorder.setQmsOrderFireDelete", svcMap);
    }

    public int setQmsOrderDetlTempUpdate(Map<String, Object> svcMap) {
        return sqlSession.update("eorder.o_qmsorder.setQmsOrderDetlTempUpdate", svcMap);
    }
    
    public int setQmsOrderDetlUpdate(Map<String, Object> svcMap) {
        return sqlSession.update("eorder.o_qmsorder.setQmsOrderDetlUpdate", svcMap);
    }
    
    public int setQmsOrderPreDetlUpdate(Map<String, Object> svcMap) {
        return sqlSession.update("eorder.o_qmsorder.setQmsOrderPreDetlUpdate", svcMap);
    }
    
    public int setQmsOrderDetlClear(Map<String, Object> svcMap) {
        return sqlSession.update("eorder.o_qmsorder.setQmsOrderDetlClear", svcMap);
    }
    
    public int setQmsOrderMastClear(Map<String, Object> svcMap) {
        return sqlSession.update("eorder.o_qmsorder.setQmsOrderMastClear", svcMap);
    }

    public int setQmsOrderFireproofInit(Map<String, Object> svcMap) {
        return sqlSession.update("eorder.o_qmsorder.setQmsOrderFireproofInit", svcMap);
    }

    public int setQmsOrderPreFireproofInit(Map<String, Object> svcMap) {
        return sqlSession.update("eorder.o_qmsorder.setQmsOrderPreFireproofInit", svcMap);
    }
    
    public int setQmsOrderFireproofInsert(Map<String, Object> svcMap) {
        return sqlSession.insert("eorder.o_qmsorder.setQmsOrderFireproofInsert", svcMap);
    }
    
    public int setQmsOrderFireproofUpdate(Map<String, Object> svcMap) {
        return sqlSession.update("eorder.o_qmsorder.setQmsOrderFireproofUpdate", svcMap);
    }
    
    public int setQmsOrderPreFireproofUpdate(Map<String, Object> svcMap) {
        return sqlSession.update("eorder.o_qmsorder.setQmsOrderPreFireproofUpdate", svcMap);
    }
    
    public List<Map<String, Object>> getQmsPopMastList(Map<String, Object> svcMap) {
        return sqlSession.selectList("eorder.o_qmsorder.getQmsPopMastList", svcMap);
    }

    public List<Map<String, Object>> getQmsPopDetlList(Map<String, Object> svcMap) {
        return sqlSession.selectList("eorder.o_qmsorder.getQmsPopDetlList", svcMap);
    }
    
    public List<Map<String, Object>> getQmsFireproofList(Map<String, Object> svcMap) {
        return sqlSession.selectList("eorder.o_qmsorder.getQmsFireproofList", svcMap);
    }
    
    public List<Map<String, Object>> getQmsPopEmailGridList(Map<String, Object> svcMap) {
        return sqlSession.selectList("eorder.o_qmsorder.getQmsPopEmailGridList", svcMap);
    }
    
    public int setQmsMailLog(Map<String, Object> svcMap) {
        return sqlSession.insert("eorder.o_qmsorder.setQmsMailLog", svcMap);
    }
    
    public int setQmsMailUpdate(Map<String, Object> svcMap) {
        return sqlSession.update("eorder.o_qmsorder.setQmsMailUpdate", svcMap);
    }
    
    public int getQmsCorpCnt(Map<String, Object> svcMap) {
        return sqlSession.selectOne("eorder.o_qmsorder.getQmsCorpCnt", svcMap);
    }
    
    public Map<String, Object> getQmsCorpShiptoCd(Map<String, Object> svcMap) {
        return sqlSession.selectOne("eorder.o_qmsorder.getQmsCorpShiptoCd", svcMap);
    }
    
    public List<Map<String, Object>> getQmsCorpList(Map<String, Object> svcMap) {
        return sqlSession.selectList("eorder.o_qmsorder.getQmsCorpList", svcMap);
    }
    
    public List<Map<String, Object>> getQmsPopList(Map<String, Object> svcMap) {
        return sqlSession.selectList("eorder.o_qmsorder.getQmsPopList", svcMap);
    }
    
    public int setQmsFileDatainsert(Map<String, Object> svcMap) {
        return sqlSession.insert("eorder.o_qmsorder.setQmsFileDatainsert", svcMap);
    }
    
    public Map<String, Object> getQmsOrderQtyCheck(Map<String, Object> svcMap) {
        return sqlSession.selectOne("eorder.o_qmsorder.getQmsOrderQtyCheck", svcMap);
    }
    
    public Object setQmsOrderTempReset(Map<String, Object> svcMap) {
        //sqlSession.delete("eorder.o_qmsorder.setQmsOrderFileTempReset", svcMap);
        sqlSession.delete("eorder.o_qmsorder.setQmsOrderFrcnTempReset", svcMap);
        sqlSession.delete("eorder.o_qmsorder.setQmsOrderDetlTempReset", svcMap);
        return sqlSession.delete("eorder.o_qmsorder.setQmsOrderMastTempReset", svcMap);
    }
    

    public Map<String, Object> setQmsOrderDetlTempReset(Map<String, Object> svcMap) {
        return sqlSession.selectOne("eorder.o_qmsorder.setQmsOrderDetlTempReset", svcMap);
    }
    
    public String getQmsOrderDupCheck(Map<String, Object> svcMap) {
        return sqlSession.selectOne("eorder.o_qmsorder.getQmsOrderDupCheck", svcMap);
    }
    
    public Object setQmsOrderAllAdd(Map<String, Object> svcMap) {
        //sqlSession.delete("eorder.o_qmsorder.setQmsOrderFileTempReset", svcMap);
        
        return sqlSession.delete("eorder.o_qmsorder.setQmsOrderAllAdd", svcMap);
    }
    
    public List<Map<String, Object>> getShipToList(Map<String, Object> svcMap) {
        return sqlSession.selectList("eorder.o_qmsorder.getShipToList", svcMap);
    }
    
    public Object setQmsOrderRemove(Map<String, Object> svcMap) {
        return sqlSession.delete("eorder.o_qmsorder.setQmsOrderRemove", svcMap);
    }

    public Object getQmsLastShiptoCd(Map<String, Object> svcMap) {
        return sqlSession.selectOne("eorder.o_qmsorder.getQmsLastShiptoCd", svcMap);
    }

    public Map<String, Object> getQmsShiptoInfo(Map<String, Object> svcMap) {
        return sqlSession.selectOne("eorder.o_qmsorder.getQmsShiptoInfo", svcMap);
    }
    
    public Map<String, Object> getQmsLastShiptoInfo(Map<String, Object> svcMap) {
        return sqlSession.selectOne("eorder.o_qmsorder.getQmsLastShiptoInfo", svcMap);
    }
    
    public int getQmsDepartmentListAjaxCnt(Map<String, Object> svcMap) {
        return sqlSession.selectOne("eorder.o_qmsorder.getQmsDepartmentListAjaxCnt", svcMap);
    }
    
    public List<Map<String, Object>> getQmsDepartmentListAjax(Map<String, Object> svcMap) {
        return sqlSession.selectList("eorder.o_qmsorder.getQmsDepartmentListAjax", svcMap);
    }
    
    public int mergeQmsDepartmentAjaxTransaction(Map<String, Object> svcMap) {
        return sqlSession.update("eorder.o_qmsorder.setQmsDepartment", svcMap);
    }
    
    public List<Map<String, Object>> getQmsYearList(Map<String, Object> svcMap) {
        return sqlSession.selectList("eorder.o_qmsorder.getQmsYearList", svcMap);
    }
    
    public List<Map<String, Object>> getQmsOrderYearList(Map<String, Object> svcMap) {
        return sqlSession.selectList("eorder.o_qmsorder.getQmsOrderYearList", svcMap);
    }
    
    public List<Map<String, Object>> getQmsReleaseYearList(Map<String, Object> svcMap) {
        return sqlSession.selectList("eorder.o_qmsorder.getQmsReleaseYearList", svcMap);
    }
    
    public int getQmsDedalinesListAjaxCnt(Map<String, Object> svcMap) {
        return sqlSession.selectOne("eorder.o_qmsorder.getQmsDedalinesListAjaxCnt", svcMap);
    }
    
    public List<Map<String, Object>> getQmsDedalinesListAjax(Map<String, Object> svcMap) {
        return sqlSession.selectList("eorder.o_qmsorder.getQmsDedalinesListAjax", svcMap);
    }
    
    public int mergeQmsDedalinesAjaxTransaction(Map<String, Object> svcMap) {
        return sqlSession.update("eorder.o_qmsorder.setQmsDedalines", svcMap);
    }
    
    public int getQmsStasticsSalesListAjaxCnt(Map<String, Object> svcMap) {
        return sqlSession.selectOne("eorder.o_qmsorder.getQmsStasticsSalesListAjaxCnt", svcMap);
    }
    
    public List<Map<String, Object>> getQmsStasticsSalesListAjax(Map<String, Object> svcMap) {
        return sqlSession.selectList("eorder.o_qmsorder.getQmsStasticsSalesListAjax", svcMap);
    }
    
    public int getQmsStasticsTeamListAjaxCnt(Map<String, Object> svcMap) {
        return sqlSession.selectOne("eorder.o_qmsorder.getQmsStasticsTeamListAjaxCnt", svcMap);
    }
    
    public List<Map<String, Object>> getQmsStasticsTeamListAjax(Map<String, Object> svcMap) {
        return sqlSession.selectList("eorder.o_qmsorder.getQmsStasticsTeamListAjax", svcMap);
    }
    
    public int getQmsRawStasticsListAjaxCnt(Map<String, Object> svcMap) {
        return sqlSession.selectOne("eorder.o_qmsorder.getQmsRawStasticsListAjaxCnt", svcMap);
    }
    
    public List<Map<String, Object>> getQmsRawStasticsListAjax(Map<String, Object> svcMap) {
        return sqlSession.selectList("eorder.o_qmsorder.getQmsRawStasticsListAjax", svcMap);
    }
    
    public int getQmsOrderCnt(Map<String, Object> svcMap) {
        return sqlSession.selectOne("eorder.o_qmsorder.getQmsOrderCnt", svcMap);
    }
    
    public String getOrderDupCheck(Map<String, Object> svcMap) {
        return sqlSession.selectOne("eorder.o_qmsorder.getOrderDupCheck", svcMap);
    }
    
    public String getOrderCustDupCheck(Map<String, Object> svcMap) {
        return sqlSession.selectOne("eorder.o_qmsorder.getOrderCustDupCheck", svcMap);
    }
    
    public String getQmsTempId(Map<String, Object> svcMap) {
        return sqlSession.selectOne("eorder.o_qmsorder.getQmsTempId", svcMap);
    }
    
    public String getQmsFirstOrderCheckAjax(Map<String, Object> svcMap) {
        return sqlSession.selectOne("eorder.o_qmsorder.getQmsFirstOrderCheckAjax", svcMap);
    }
    
    public int setQmsFirstOrderAjax(Map<String, Object> svcMap) {
        return sqlSession.insert("eorder.o_qmsorder.setQmsFirstOrderAjax", svcMap);
    }

    public int setQmsFirstOrderDetailAjax(Map<String, Object> svcMap) {
        return sqlSession.insert("eorder.o_qmsorder.setQmsFirstOrderDetailAjax", svcMap);
    }
    
    public List<Map<String, Object>> getQmsPopPreMastList(Map<String, Object> svcMap) {
        return sqlSession.selectList("eorder.o_qmsorder.getQmsPopPreMastList", svcMap);
    }
    
    public List<Map<String, Object>> getQmsPopPreDetlList(Map<String, Object> svcMap) {
        return sqlSession.selectList("eorder.o_qmsorder.getQmsPopPreDetlList", svcMap);
    }
    
    public List<Map<String, Object>> getQmsPreFireproofList(Map<String, Object> svcMap) {
        return sqlSession.selectList("eorder.o_qmsorder.getQmsPreFireproofList", svcMap);
    }
    
    @Transactional
    public int setQmsPreTestInsert(Map<String, Object> svcMap) {
        //sqlSession.update("eorder.o_qmsorder.setQmsPreTestInsert", svcMap);
        return sqlSession.update("eorder.o_qmsorder.setQmsPreTestSync", svcMap);
    }
    
    /*@Transactional
    public int syncQmsSalesOrder() {
        //sqlSession.update("eorder.o_qmsorder.setQmsPreTestInsert", svcMap);
        return sqlSession.update("eorder.o_qmsorder.syncQmsSalesOrder");
    }*/
    
    public int syncPreQmsSalesOrder() {
        //sqlSession.update("eorder.o_qmsorder.setQmsPreTestInsert", svcMap);
        return sqlSession.update("eorder.o_qmsorder.syncPreQmsSalesOrder");
    }
    
    public int setQmsFirstOrderCancelAjax(Map<String, Object> svcMap) {
        return sqlSession.update("eorder.o_qmsorder.setQmsFirstOrderCancelAjax", svcMap);
    }
    
    public List<Map<String, Object>> getMailPreQmsOrderList() {
        //sqlSession.update("eorder.o_qmsorder.setQmsPreTestInsert", svcMap);
        return sqlSession.selectList("eorder.o_qmsorder.getMailPreQmsOrderList");
    }
    
    public int setQmsPreOrderRemove(Map<String, Object> svcMap) {
        sqlSession.update("eorder.o_qmsorder.setQmsPreOrderDetlRemove", svcMap);
        return sqlSession.update("eorder.o_qmsorder.setQmsPreOrderMastRemove", svcMap);
    }
    

    public String getFindPreQmsOrderId(Map<String, Object> svcMap) {
        return sqlSession.selectOne("eorder.o_qmsorder.getFindPreQmsOrderId", svcMap);
    }
    
    public List<Map<String, Object>> getPulbishCountQmsSalesorder(Map<String, Object> svcMap) {
        return sqlSession.selectList("eorder.o_qmsorder.getPulbishCountQmsSalesorder", svcMap);
    }
}
