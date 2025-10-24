package com.limenets.eorder.dao;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;

@Repository
public class MenuDao {
	@Inject private SqlSession sqlSession;
	
	public int inRoleMenu(Map<String, Object> svcMap) {
		return sqlSession.insert("eorder.menu.inRoleMenu", svcMap);
	}
	
	public int inRoleMenuBase(Map<String, Object> svcMap) {
		return sqlSession.insert("eorder.menu.inRoleMenuBase", svcMap);
	}
	
	public int delRoleMenu(Map<String, Object> svcMap) {
		return sqlSession.delete("eorder.menu.delRoleMenu", svcMap);
	}
	
	public List<Map<String, Object>> getMenuListForAuthority(Map<String, Object> svcMap) {
		return sqlSession.selectList("eorder.menu.getMenuListForAuthority", svcMap);
	}
	
	public List<Map<String, Object>> getRoleList(Map<String, Object> svcMap) {
		return sqlSession.selectList("eorder.menu.roleList", svcMap);
	}
	
	public List<Map<String, Object>> getRoleMenuListForLogin(Map<String, Object> svcMap) {
		return sqlSession.selectList("eorder.menu.roleMenuListForLogin", svcMap);
	}
	
	public List<Map<String, Object>> roleMenuListForLoginBySystemAdmin(Map<String, Object> svcMap) {
		return sqlSession.selectList("eorder.menu.roleMenuListForLoginBySystemAdmin", svcMap);
	}
	
	public List<Map<String, Object>> getMenuUrl(Map<String, Object> svcMap) {
		return sqlSession.selectList("eorder.menu.menuUrl", svcMap);
	}
}
