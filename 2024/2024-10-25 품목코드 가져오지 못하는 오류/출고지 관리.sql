


C:\GitHub\Knauf_Eorder_NEW\src\main\webapp\WEB-INF\views\admin\system\plantConfig.jsp

getGridList() => url: /admin/system/plantListAjax.lime


C:\GitHub\Knauf_Eorder_NEW\src\main\java\com\limenets\eorder\ctrl\admin\SystemCtrl.java

출고지 관리 폼 > 출고지 리스트 가져오기 Ajax.
public Object plantListAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {

Map<String, Object> resMap = configSvc.getPlantList(params, req);


출고지 리스트 가져오기.
C:\GitHub\Knauf_Eorder_NEW\src\main\java\com\limenets\eorder\svc\ConfigSvc.java

public Map<String, Object> getPlantList(Map<String, Object> params, HttpServletRequest req)
	List<Map<String, Object>> list = this.getPlantList(params);

public List<Map<String, Object>> getPlantList(Map<String, Object> svcMap){
	return plantDao.list(svcMap);
}


C:\GitHub\Knauf_Eorder_NEW\src\main\java\com\limenets\eorder\dao\PlantDao.java
public List<Map<String, Object>> list(Map<String, Object> svcMap) {
	return sqlSession.selectList("eorder.plant.list", svcMap);
}


	<select id="list" parameterType="hashmap" resultType="hashmap">		
		<!-- 2024-10-15 HSG 주석 처리 후 아래 코드 삽입  SELECT p.WERKS AS PT_CODE
			, p.PT_NAME + '(' + p.WERKS + ')' AS PT_NAME -->
		SELECT 
			p.PT_CODE  PT_CODE ,
			p.PT_NAME AS PT_NAME
		FROM PLANT p 
		WHERE p.PT_USE = 'Y'
		ORDER BY p.PT_SORT
	</select>
	




/* 출고지 관리 */
SELECT
        p.PT_CODE PT_CODE ,
        p.PT_NAME AS PT_NAME
FROM    PLANT p
WHERE   p.PT_USE = 'Y'
ORDER BY  p.PT_SORT 
;


/* *********** DEV *********** */

PT_CODE	PT_NAME
5600	당진공장
300	울산공장
600	여수공장
5593	오류창고
5587	기흥창고
5590	광주창고
5588	중부창고
5578	부산창고
5575	대구창고





/* *********** JDE *********** */

PT_CODE	PT_NAME
5600	당진공장 (5600)
300	울산공장 (300)
600	여수공장 (600)
293	울산 오류창고 (293)
593	여수 오류창고 (593)
5593	당진 오류창고 (5593)
287	울산 기흥창고 (287)
587	여수 기흥창고 (587)
5587	당진 기흥창고 (5587)
275	울산 대구창고 (275)
575	여수 대구창고 (575)
5575	당진 대구창고 (5575)
288	울산 중부창고 (288)
588	여수 중부창고 (588)
5588	당진 중부창고 (5588)
290	울산 광주창고 (290)
590	여수 광주창고 (590)
5590	당진 광주창고 (5590)
278	울산 부산창고 (278)
578	여수 부산창고 (578)
5578	당진 부산창고 (5578)
291	울산 송악창고 (291)
591	여수 송악창고 (591)
5591	당진 송악창고 (5591)
292	울산 온산창고 (292)
592	여수 온산창고 (592)
5592	당진 온산창고 (5592)
289	울산 대전창고 (289)
589	여수 대전창고 (589)
5589	당진 대전창고 (5589)




O_ITEM_MCU

DESC1 : 일반 9.5*900*1800 평보드

/* *********** DEV *********** */

item_mcu2	item_nm
4635	일반 9.5*900*1800 평보드
4636	일반 9.5*900*1800 평보드
4637	일반 9.5*900*1800 평보드
5615	일반 9.5*900*1800 평보드
5616	일반 9.5*900*1800 평보드
5617	일반 9.5*900*1800 평보드
5618	일반 9.5*900*1800 평보드
5619	일반 9.5*900*1800 평보드
5620	일반 9.5*900*1800 평보드


/* *********** JDE *********** */

item_mcu2	item_nm
275	일반 9.5*900*1800 평보드
278	일반 9.5*900*1800 평보드
282	일반 9.5*900*1800 평보드
287	일반 9.5*900*1800 평보드
288	일반 9.5*900*1800 평보드
289	일반 9.5*900*1800 평보드
290	일반 9.5*900*1800 평보드
291	일반 9.5*900*1800 평보드
292	일반 9.5*900*1800 평보드
293	일반 9.5*900*1800 평보드
300	일반 9.5*900*1800 평보드
5575	일반 9.5*900*1800 평보드
5578	일반 9.5*900*1800 평보드
5587	일반 9.5*900*1800 평보드
5588	일반 9.5*900*1800 평보드
5589	일반 9.5*900*1800 평보드
5590	일반 9.5*900*1800 평보드
5591	일반 9.5*900*1800 평보드
5592	일반 9.5*900*1800 평보드
5593	일반 9.5*900*1800 평보드
5600	일반 9.5*900*1800 평보드
575	일반 9.5*900*1800 평보드
578	일반 9.5*900*1800 평보드
587	일반 9.5*900*1800 평보드
588	일반 9.5*900*1800 평보드
589	일반 9.5*900*1800 평보드
590	일반 9.5*900*1800 평보드
591	일반 9.5*900*1800 평보드
592	일반 9.5*900*1800 평보드
593	일반 9.5*900*1800 평보드
600	일반 9.5*900*1800 평보드


