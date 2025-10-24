package com.limenets.eorder.svc;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import org.springframework.stereotype.Service;

import com.limenets.eorder.dao.PostalCodeDao;

@Service
public class PostalCodeSvc {
//	private static final Logger logger = LoggerFactory.getLogger(ItemSvc.class);
	
	@Inject private PostalCodeDao postalCodeDao;

	public Map<String, Object> getPostalCodeCount(Map<String, Object> params) throws Exception {
		Map<String, Object> resMap = new HashMap<>();
		
		String [] r = {"Y"};
		params.put("r_useflags", r);
		int cnt = postalCodeDao.cnt(params);
		resMap.put("useFlag", cnt==0 ? 'F' : 'Y');

        return resMap;
	}
}
