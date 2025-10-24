package com.limenets.eorder.svc;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.limenets.eorder.dao.AccumSapDao;

@Service
public class AccumSapSvc {
	
	@Inject private AccumSapDao sapDao;

	public Map<String, Object> getNotShippedVolume(Map<String, Object> param){
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
		
		// FOR TEST
		//calendar.add(Calendar.DATE, -103);
		
		calendar.add(Calendar.DATE, -1);
		List<Map<String, Object>> dailyAccumlist = new ArrayList<Map<String, Object>>();
		Map<String, Object> pm = new HashMap<String, Object>();
		for(int i=0; i<14; i++) {
			Map<String, Object> dMap = new HashMap<String, Object>();
			calendar.add(Calendar.DATE, 1);
			dMap.put("day", sdf.format(calendar.getTime()));
			
			pm.clear();
			pm.put("dt", sdf.format(calendar.getTime()).replaceAll("/", ""));
			List<Map<String, Object>> list = sapDao.getShippedVolume(pm);
			
			if(list.isEmpty()) {
				dMap.put("volList", new ArrayList<Map<String, Object>>());
			} else {
				Map<Object, List<Map<String, Object>>> groupedData = list.stream()
						.collect(Collectors.groupingBy(map -> map.get("PLANT_CD"), LinkedHashMap::new, Collectors.toList()));
				Set<Object> setG = groupedData.keySet();
				
				List<Map<String, Object>> addList = new ArrayList<Map<String, Object>>();
				for(Object obj : setG) {
					List<Map<String, Object>> lst =  groupedData.get(obj);
					boolean cM2 = false;
					boolean cKG = false;
					boolean cPAC = false;
					for(Map<String, Object> m : lst) {
						String unit = m.get("UNIT").toString();
						if(unit.compareToIgnoreCase("M2") == 0)
							cM2 = true;
						else if(unit.compareToIgnoreCase("KG") == 0)
							cKG = true;
						else if(unit.compareToIgnoreCase("PAC") == 0) 
							cPAC = true;
					}
					
					if(cM2) {
						for(Map<String, Object> m1 : lst) {
							String unit = m1.get("UNIT").toString();
							if(unit.compareToIgnoreCase("M2") == 0)
								addList.add(m1);
						}
					} else {
						Map<String, Object> m2 = new HashMap<String, Object>();
						m2.put("PLANT_CD", obj);
						m2.put("PLANT_NM", lst.get(0).get("PLANT_NM"));
						m2.put("CATEG", "BOARD");
						m2.put("UNIT", "M2");
						addList.add(m2);
					}
					
					if(cKG) {
						for(Map<String, Object> m1 : lst) {
							String unit = m1.get("UNIT").toString();
							if(unit.compareToIgnoreCase("KG") == 0)
								addList.add(m1);
						}
					} else {
						Map<String, Object> m2 = new HashMap<String, Object>();
						m2.put("PLANT_CD", obj);
						m2.put("UNIT", "KG");
						m2.put("CATEG", "BOND");
						addList.add(m2);
					}
					
					if(cPAC) {
						for(Map<String, Object> m1 : lst) {
							String unit = m1.get("UNIT").toString();
							if(unit.compareToIgnoreCase("PAC") == 0)
								addList.add(m1);
						}
					} else {
						Map<String, Object> m2 = new HashMap<String, Object>();
						m2.put("PLANT_CD", obj);
						m2.put("UNIT", "PAC");
						m2.put("CATEG", "GYPTEX");
						addList.add(m2);
					}
				}
				
				dMap.put("volList", addList);
			}
			dailyAccumlist.add(dMap);
		}
		
		calendar = Calendar.getInstance();
		// FOR TEST
		//calendar.add(Calendar.DATE, -103);
		
		int nToday = calendar.get(Calendar.DATE);
		String firstDay = "";
		String lastday = "";
		List<Map<String, Object>> resList = new ArrayList<Map<String, Object>>();
		if(nToday > 1) {		
			calendar.add(Calendar.DATE, -1);
			lastday = sdf.format(calendar.getTime());
			calendar.set(Calendar.DATE, 1);
			firstDay = sdf.format(calendar.getTime());
			
			pm.clear();
			pm.put("fDt", firstDay.replaceAll("/", ""));
			pm.put("tDt", lastday.replaceAll("/", ""));
			resList = sapDao.getNotShippedVolume(pm);
		} else {
			firstDay = sdf.format(calendar.getTime());
			lastday = sdf.format(calendar.getTime());
		}

		Map<String, Object> resMap = new HashMap<String, Object>();
		resMap.put("fromDt", firstDay);
		resMap.put("toDt", lastday);
		resMap.put("nShippedVolumes", resList);
		resMap.put("dailyAccumlist", dailyAccumlist);

		return resMap;
	}

}
