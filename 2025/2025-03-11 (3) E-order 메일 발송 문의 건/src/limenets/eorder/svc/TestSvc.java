package com.limenets.eorder.svc;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.RuntimeSingleton;
import org.apache.velocity.runtime.resource.loader.StringResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.limenets.common.dto.LoginDto;
import com.limenets.common.exception.LimeBizException;
import com.limenets.common.exception.MsgCode;
import com.limenets.common.util.Converter;
import com.limenets.common.util.ResourceUtils;
import com.limenets.eorder.dao.TestDao;
import com.limenets.eorder.dto.TestDto;

/**
 * Test 서비스.
 */
@Service
public class TestSvc {
	private static final Logger logger = LoggerFactory.getLogger(TestSvc.class);
	
	@Inject TestDao testDao;
	
	/****************************************************************************************************************************************************
	 * Start. Test For Velocity 
	 ****************************************************************************************************************************************************/
	public VelocityEngine initVelocityEngineByFile() throws Exception {
		String file = ResourceUtils.getClassLoader().getResource("velocity.properties").getFile();
		VelocityEngine engine = new VelocityEngine(file);
		engine.init();
		return engine;
	}
	
	public VelocityEngine initVelocityEngineByString() throws Exception {
		VelocityEngine engine = new VelocityEngine();
		//engine.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, "org.apache.velocity.runtime.log.Log4JLogChute");
	    //engine.setProperty("runtime.log.logsystem.log4j.logger", logger.getName());
	    engine.setProperty(Velocity.RESOURCE_LOADER, "string");
	    engine.addProperty("string.resource.loader.class", StringResourceLoader.class.getName());
	    engine.addProperty("string.resource.loader.repository.static", "false");
		engine.init();
		return engine;
	}
	
	/**
	 * velocity By Templete File.
	 */
	public String createTempleteByFile(Map<String, Object> params) throws Exception {
		VelocityEngine engin = initVelocityEngineByFile();
		VelocityContext context = new VelocityContext();
		
		context.put("page_title", "Create Velocity By File");
		context.put("company_name", "라임네츠");
		context.put("tfoot_text", "Create Velocity By File");
		
		List<Map<String, Object>> userList = new ArrayList<>();
		Map<String, Object> map1 = new HashMap<>();
		map1.put("no", 4);
		map1.put("name", "전지현");
		map1.put("employdate", "2017-05-01");
		map1.put("retiredate", "-");
		userList.add(map1);
		
		Map<String, Object> map2 = new HashMap<>();
		map2.put("no", 5);
		map2.put("name", "송혜교");
		map2.put("employdate", "2017-06-01");
		map2.put("retiredate", "2019-01-02");
		userList.add(map2);
		
		Map<String, Object> map3 = new HashMap<>();
		map3.put("no", 6);
		map3.put("name", "고소영");
		map3.put("employdate", "2017-08-01");
		map3.put("retiredate", "2017-12-12");
		userList.add(map3);
		
		context.put("user_list", userList);
		
		Template template = engin.getTemplate("velocity/templete/test.html");
		StringWriter sw = new StringWriter();
		template.merge(context, sw);
		String textBody = sw.toString();
		
		logger.debug("textBody : {}", textBody);
		return textBody;
	}
	
	/**
	 * velocity By String.
	 */
	@SuppressWarnings("unused")
	public String createTempleteByString(Map<String, Object> params) throws Exception {
		VelocityEngine engin = initVelocityEngineByString();
		VelocityContext context = new VelocityContext();
		
		context.put("username", "정우성");
		
		RuntimeServices runtimeServices = RuntimeSingleton.getRuntimeServices();
		StringReader reader = new StringReader("Username is $username"); // 문자열 입력
		
		Template template = new Template();
		template.setRuntimeServices(runtimeServices);
		template.setData(runtimeServices.parse(reader, template));
		template.initDocument();
		
		StringWriter sw = new StringWriter();
		template.merge(context, sw);
		
		String textBody = sw.toString();
		
		logger.debug("textBody : {}", textBody);
		return textBody;
	}
	/****************************************************************************************************************************************************
	 * End. Test For Velocity 
	 ****************************************************************************************************************************************************/
	
	public List<TestDto> getTestList(Map<String, Object> svcMap){
		return testDao.list(svcMap);
	}
	
	public Map<String, Object> insertTestTransaction(Map<String, Object> params, LoginDto loginDto) throws Exception {
		String m_ts1 = Converter.toStr(params.get("m_ts1")); // 필수값
		String m_ts2 = Converter.toStr(params.get("m_ts2"));
		String m_ts3 = Converter.toStr(params.get("m_ts3"));
		//String m_ts1 = "가나다";
		//String m_ts2 = "Aa";
		//String m_ts3 = "똠펲샾잌볖볌뭔믜됌됨";
		logger.debug("m_ts1 : {}", m_ts1);
		logger.debug("m_ts2 : {}", m_ts2);
		logger.debug("m_ts3 : {}", m_ts3);
		
		logger.debug("m_ts1 Get Byte : {}", m_ts1.getBytes().length); //UTF-8
		logger.debug("m_ts2 Get Byte : {}", m_ts2.getBytes().length);
		logger.debug("m_ts3 Get Byte : {}", m_ts3.getBytes().length);
		
		if(StringUtils.equals("", m_ts1)) throw new LimeBizException(MsgCode.DATA_REQUIRE_ERROR2);
		
		params.put("m_ts1", m_ts1);
		params.put("m_ts2", m_ts2);
		params.put("m_ts3", m_ts3);
		testDao.in(params);
		
		List<TestDto> list = testDao.list(params);
		logger.debug("list : {}", list);
		
		return MsgCode.getResultMap(MsgCode.SUCCESS);
	}
}
