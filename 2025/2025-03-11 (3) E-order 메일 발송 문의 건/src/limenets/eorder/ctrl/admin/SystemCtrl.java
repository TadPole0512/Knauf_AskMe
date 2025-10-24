package com.limenets.eorder.ctrl.admin;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.limenets.common.dto.LoginDto;
import com.limenets.common.exception.LimeBizException;
import com.limenets.common.exception.MsgCode;
import com.limenets.common.util.Converter;
import com.limenets.eorder.excel.AdminUserConfigExcel;
import com.limenets.eorder.excel.ItemManageExcel;
import com.limenets.eorder.excel.PlantConfigExcel;
import com.limenets.eorder.excel.PostalCodeConfigExcel;
import com.limenets.eorder.excel.QmsDedalinesExcel;
import com.limenets.eorder.excel.QmsDepartmentExcel;
import com.limenets.eorder.excel.QmsRawStasticsExcel;
import com.limenets.eorder.excel.QmsStasticsSalesExcel;
import com.limenets.eorder.excel.QmsStasticsTeamExcel;
import com.limenets.eorder.excel.SalesUserCategoryEditExcel;
import com.limenets.eorder.svc.CommonCodeSvc;
import com.limenets.eorder.svc.ConfigSvc;
import com.limenets.eorder.svc.ItemSvc;
import com.limenets.eorder.svc.MenuSvc;
import com.limenets.eorder.svc.OrderSvc;
import com.limenets.eorder.svc.UserSvc;

/**
 * Admin 시스템 컨트롤러.
 */
@Controller
@RequestMapping("/admin/system/*")
public class SystemCtrl {
	private static final Logger logger = LoggerFactory.getLogger(SystemCtrl.class);
			
	@Inject private CommonCodeSvc commonCodeSvc;
	@Inject private ConfigSvc configSvc;
	@Inject private MenuSvc menuSvc;
	@Inject private UserSvc userSvc;
	@Inject private OrderSvc orderSvc;
	@Inject private ItemSvc itemSvc;
	/**
	 * 환경설정 폼
	 * @작성일 : 2020. 2. 26.
	 * @작성자 : kkyu
	 */
	@GetMapping(value="systemConfig")
	public String systemConfig(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		Map<String, Object> resMap = configSvc.getConfigList(params);
		model.addAttribute("configList", resMap);
		
		String sysAdminId = configSvc.getConfigValue("SYSTEMADMIN"); // 시스템관리자 아이디
		model.addAttribute("systemAdmin", userSvc.getUserOne(sysAdminId));
		
		return "admin/system/systemConfig";
	}
	
	/**
	 * 환경설정 수정.
	 * @작성일 : 2020. 2. 26.
	 * @작성자 : kkyu
	 */
	@PostMapping(value="updateSystemConfig")
	public String updateSystemConfig(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, Model model, LoginDto loginDto) throws Exception {
		Map<String, Object> resMap = configSvc.updateSystemConfigTransaction(params, req, loginDto);
		String resMsg = Converter.toStr(resMap.get("RES_MSG"));
		req.setAttribute("resultAjax", "<script>location.href='./systemConfig.lime'; alert('"+resMsg+"');</script>");
		return "textAjax";
	}
	
	/**
     * QMS 환경설정 수정.
     * @작성일 : 2020. 2. 26.
     * @작성자 : kkyu
     */
    @PostMapping(value="updateQmsConfig")
    public String updateQmsConfig(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, Model model, LoginDto loginDto) throws Exception {
        Map<String, Object> resMap = configSvc.updateSystemConfigTransaction(params, req, loginDto);
        String resMsg = Converter.toStr(resMap.get("RES_MSG"));
        req.setAttribute("resultAjax", "<script>location.href='./qmsConfig.lime'; alert('"+resMsg+"');</script>");
        return "textAjax";
    }
	
	/**
	 * 사용자 현황 폼.
	 * @작성일 : 2020. 2. 26.
	 * @작성자 : kkyu
	 */
	@GetMapping(value="adminUserConfig")
	public String adminUserConfig(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		return "admin/system/adminUserConfig";
	}

	/**
	 * 사용자 현황 폼 > 내부사용자 권한별 리스트 가져오기 Ajax.
	 * @작성일 : 2020. 2. 27.
	 * @작성자 : kkyu
	 */
	@ResponseBody
	@PostMapping(value="getAdminUserAuthorityListAjax")
	public Object getAdminUserAuthorityListAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		return userSvc.getAdminUserAuthorityList(params, req, loginDto);
	}
	
	/**
	 * 사용자 현황 폼 > 내부사용자 리스트 가져오기 Ajax.
	 * @작성일 : 2020. 3. 2.
	 * @작성자 : kkyu
	 */
	@ResponseBody
	@PostMapping(value="getAdminUserListAjax")
	public Object getAdminUserListAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		String[] rni_authority = {"AS", "CO", "CT"};
		params.put("rni_authority", rni_authority); // 거래처/납품처 및 시스템총괄관리자 제외.
		
		return userSvc.getUserList(params, req, loginDto);
	}
	
	/**
	 * 출고지 관리 폼 > 출고지 리스트 가져오기 Ajax.
	 * @작성일 : 2020. 3. 9.
	 * @작성자 : kkyu
	 */
	@PostMapping(value="adminUserConfigExcelDown")
	public ModelAndView adminUserConfigExcelDown(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		Map<String, Object> resMap = new HashMap<>();
		
		// Start. 뷰단에서 넘어온 파일 쿠키 put.
		String fileToken = Converter.toStr(params.get("filetoken"));
		resMap.put("fileToken", fileToken);
		// End.
		
		params.put("where", "excel");
		String[] rni_authority = {"AS", "CO", "CT"};
		params.put("rni_authority", rni_authority); // 거래처/납품처 및 시스템총괄관리자 제외.
		resMap.putAll(userSvc.getUserList(params, req, loginDto));
		
		return new ModelAndView(new AdminUserConfigExcel(), resMap);
	}
	
	/**
	 * 사용자 현황 폼 > 팝업 폼. 
	 * {process} 정의.
	 * - addEditPop : 내부사용자 등록/수정 팝업 폼.
	 * - viewPop : 내부사용자 확인 팝업 폼.
	 * @작성일 : 2020. 3. 6.
	 * @작성자 : kkyu
	 * @param r_parentuserid 상위 단계 사용자 아이디. [수정/등록 모두 필수]
	 * @param r_userid 수정할 사용자 아읻. [수정시에만 필수]
	 */
	@PostMapping(value="/adminUser/pop/{process}")
	public String adminUserAddEditViewPop(@PathVariable("process") String process, @RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		String r_parentuserid = Converter.toStr(params.get("r_parentuserid"));
		String r_userid = Converter.toStr(params.get("r_userid"));
		String page_type = (StringUtils.equals("addEditPop", process)) ? "ADD" : "VIEW";
		
		// 공통.
		if(StringUtils.equals("", r_parentuserid)) throw new LimeBizException(MsgCode.DATA_REQUIRE_ERROR, "상위 단계 사용자 아이디");
		Map<String, Object> parentAdminUser = userSvc.getUserOneAll(r_parentuserid);
		//logger.debug("parentAdminUser : {}", parentAdminUser);
		if(CollectionUtils.isEmpty(parentAdminUser)) {
			throw new LimeBizException(MsgCode.DATA_NOT_FOUND_ERROR);
		}
		
		model.addAttribute("parentAdminUser", parentAdminUser);
		
		// 수정
		if(!StringUtils.equals("", r_userid)) {
			Map<String, Object> adminUser = userSvc.getUserOne(r_userid); 
			if(CollectionUtils.isEmpty(adminUser)) {
				throw new LimeBizException(MsgCode.DATA_NOT_FOUND_ERROR);
			}
			
			model.addAttribute("adminUser", adminUser);
			
			if(StringUtils.equals("CS", Converter.toStr(adminUser.get("AUTHORITY")))) {
				//model.addAttribute("csSalesMap", userSvc.getCsSalesMapOne(Converter.toStr(adminUser.get("USERID")), "Y"));
			}
			
			page_type = (StringUtils.equals("addEditPop", process)) ? "EDIT" : "VIEW";
		}
		
		model.addAttribute("page_type", page_type); // return ADD/EDIT/VIEW.
		
		return "admin/system/adminUserAddEditPop";
	}
	
	
	
	
	
	
	/**
	 * 사용자 현황 폼 > 영업조직 > 조직변경 팝업.
	 * @작성일 : 2020. 6. 9.
	 * @작성자 : kkyu
	 * 
	 */
	@GetMapping(value="/adminUser/pop2/salesUserCategoryEditPop")
	public String salesUserCategoryEditPop(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		return "admin/system/salesUserCategoryEditPop";
	}
	
	/**
	 * 사용자 현황 폼 > 영업조직 > 조직변경 팝업 > 영업조직현황 엑셀다운로드.
	 * @작성일 : 2020. 6. 9.
	 * @작성자 : kkyu
	 */
	@PostMapping(value="salesUserCategoryEditExcelDown")
	public ModelAndView salesUserCategoryEditExcelDown(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		Map<String, Object> resMap = new HashMap<>();
		
		// Start. 뷰단에서 넘어온 파일 쿠키 put.
		String fileToken = Converter.toStr(params.get("filetoken"));
		resMap.put("fileToken", fileToken);
		// End.
		
		params.put("where", "excel");
		resMap.putAll(userSvc.salesUserCategoryEditExcel(params, req, loginDto));
		return new ModelAndView(new SalesUserCategoryEditExcel(), resMap);
	}
	
	/**
	 * 사용자 현황 폼 > 영업조직 > 조직변경 팝업 > 영업조직 변경 임시 테이블 저장 Ajax.
	 * @작성일 : 2020. 6. 9.
	 * @작성자 : kkyu
	 */
	@ResponseBody
	@PostMapping(value="updateSalesUserCategoryEditExcelAjax")
	public Object updateItemExcelAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		return userSvc.updateSalesUserCategoryEditExceTransaction(params, req, loginDto);
	}
	
	/**
	 * 사용자 현황 폼 > 영업조직 > 조직변경 팝업 > 영업조직 구조 수정 Ajax > 임시 테이블(O_USER2) 리스트 가져오기 Ajax.
	 * @작성일 : 2020. 6. 9.
	 * @작성자 : kkyu
	 */
	@ResponseBody
	@PostMapping(value="getAdminUserAuthorityTempListAjax")
	public Object getAdminUserAuthorityTempListAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		return userSvc.getAdminUserAuthorityTempList(params, req, loginDto);
	}
	
	/**
	 * 사용자 현황 폼 > 영업조직 > 조직변경 팝업 > 엑셀 업로드로 임시 테이블에 저장 된 데이터를 O_USER 테이블에 반영. delete > insert Ajax.
	 * @작성일 : 2020. 6. 12.
	 * @작성자 : kkyu
	 */
	@ResponseBody
	@PostMapping(value="updateSalesUserCategoryAjax")
	public Object updateSalesUserCategoryAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		return userSvc.updateSalesUserCategoryTransaction(params, req, loginDto);
	}
	
	/**
	 * 사용자 현황 폼 > 영업조직 > 조직변경 팝업 > 엑셀 업로드로 임시 테이블에 저장 된 데이터 delete Ajax.
	 * @작성일 : 2020. 6. 12.
	 * @작성자 : kkyu
	 */
	@ResponseBody
	@PostMapping(value="deleteTempSalesUserCategoryAjax")
	public Object deleteTempSalesUserCategoryAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		return userSvc.deleteTempSalesUserCategory(params, req, loginDto);
	}
	
	
	
	
	
	
	/**
	 * 사용자 현황 폼 > 팝업 폼 > 내부사용자 저장 또는 수정 Ajax.
	 * @작성일 : 2020. 3. 6.
	 * @작성자 : kkyu
	 */
	@ResponseBody
	@PostMapping(value="insertUpdateAdminUserAjax")
	public Object insertUpdateAdminUserAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, Model model, LoginDto loginDto) throws Exception {
		return userSvc.insertUpdateAdminUser(params, req, loginDto);
	}
	
	/**
	 * 출고지 관리 폼 > 출고지 리스트 가져오기 Ajax.
	 * @작성일 : 2020. 3. 7.
	 * @작성자 : kkyu
	 */
	@ResponseBody
	@PostMapping(value="plantListAjax")
	public Object plantListAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		Map<String, Object> resMap = configSvc.getPlantList(params, req);
		return resMap;
	}
	
	/**
	 * 출고지 관리 폼 > 출고지 리스트 가져오기 Ajax.
	 * @작성일 : 2020. 3. 9.
	 * @작성자 : kkyu
	 */
	@PostMapping(value="plantConfigExcelDown")
	public ModelAndView plantConfigExcelDown(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		Map<String, Object> resMap = new HashMap<>();
		
		// Start. 뷰단에서 넘어온 파일 쿠키 put.
		String fileToken = Converter.toStr(params.get("filetoken"));
		resMap.put("fileToken", fileToken);
		// End.
		
		params.put("where", "excel");
		resMap.putAll(configSvc.getPlantList(params, req));
		
		return new ModelAndView(new PlantConfigExcel(), resMap);
	}
	
	/**
	 * 출고지 관리 폼 > 출고지 저장/수정 Ajax.
	 * @작성일 : 2020. 3. 8.
	 * @작성자 : kkyu
	 */
	@ResponseBody
	@PostMapping(value="insertUpdatePlantAjax")
	public Object insertUpdatePlantAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		Map<String, Object> resMap = configSvc.insertUpdatePlantTransaction(params, req, loginDto);
		return resMap;
	}
	

	/**
	 * 출고지 관리 폼.
	 * @작성일 : 2020. 2. 26.
	 * @작성자 : kkyu
	 */
	@GetMapping(value="plantConfig")
	public String plantConfig(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		return "admin/system/plantConfig";
	}
	
	/**
	 * 우편번호 관리 폼.
	 * @작성일 : 2023. 8. 28.
	 * @작성자 : Squall
	 */
	@GetMapping(value="postalCodeManager")
	public String postalCodeManager(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		return "admin/system/postalCodeManager";
	}
	
	/**
	 * 우편번호 관리 폼 > 우편번호 리스트 가져오기 Ajax.
	 * @작성일 : 2023. 9. 12.
	 * @작성자 : Squall. Koh
	 */
	@ResponseBody
	@PostMapping(value="postalCodeListAjax")
	public Object postalCodeListAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		Map<String, Object> resMap = configSvc.getPostalList(params, req);
		return resMap;
	}
	
	/**
	 * 우편번호 관리 폼 > 우편번호 저장/수정 Ajax.
	 * @작성일 : 2023. 9. 13.
	 * @작성자 : Squall. Koh
	 */
	@ResponseBody
	@PostMapping(value="insertUpdatePostalCodeAjax")
	public Object insertUpdatePostalCodeAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		Map<String, Object> resMap = configSvc.insertUpdatePostalCodeTransaction(params, req, loginDto);
		return resMap;
	}
	
	/**
	 * 우편번호 관리 폼 > 엑셀 파일 다운로드 Ajax.
	 * @작성일 : 2023.09.21.
	 * @작성자 : Squall. Koh
	 */
	@PostMapping(value="postalCodeConfigExcelDown")
	public ModelAndView postalCodeConfigExcelDown(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		Map<String, Object> resMap = new HashMap<>();
		
		// Start. 뷰단에서 넘어온 파일 쿠키 put.
		String fileToken = Converter.toStr(params.get("filetoken"));
		resMap.put("fileToken", fileToken);
		// End.
		
		params.put("where", "excel");
		resMap.putAll(configSvc.getPostalList(params, req));
		
		return new ModelAndView(new PostalCodeConfigExcel(), resMap);
	}
	
	@PostMapping(value="itemCodeManageExcelDown")
	public ModelAndView itemCodeManageExcelDown(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		Map<String, Object> resMap = new HashMap<>();
		
		// Start. 뷰단에서 넘어온 파일 쿠키 put.
		String fileToken = Converter.toStr(params.get("filetoken"));
		resMap.put("fileToken", fileToken);
		// End.
		
		params.put("where", "excel");
		resMap.putAll(itemSvc.getItemManageList(params, req, loginDto));
		
		return new ModelAndView(new ItemManageExcel(), resMap);
	}
	
	/**
	 * 내화구조관리 폼.
	 * @작성일 : 2021. 4. 5.
	 * @작성자 : jihye lee
	 */
	@GetMapping(value="fireproofStructure")
	public String fireproofStructure(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		return "admin/system/fireproofStructure";
	}
	
	/**
     * QMS조직 설정
     * @작성일 : 2021. 6. 5.
     * @작성자 : jsh
     */
    @GetMapping(value="qmsDepartment")
    public String qmsDepartment(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
        return "admin/system/qmsDepartment";
    }
    
    /**
     * QMS조직 설정 리스트 가져오기 Ajax.
     * @작성일 : 2021. 6. 5.
     * @작성자 : SJK
     */
    @ResponseBody
    @PostMapping(value="getQmsDepartmentListAjax")
    public Object getQmsDepartmentListAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
        
        Map<String, Object> resList = orderSvc.getQmsDepartmentListAjax(params, req);
        return resList;
    }
    
    /**
     * QMS조직 설정 리스트 가져오기 Ajax.
     * @작성일 : 2021. 6. 5.
     * @작성자 : SJK
     */
    @ResponseBody
    @PostMapping(value="mergeQmsDepartmentAjax")
    public Object mergeQmsDepartmentAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
        Map<String, Object> resMap = orderSvc.mergeQmsDepartmentAjaxTransaction(params, req, loginDto);
        return resMap;
    }
    
    /**
     * QMS조직 설정 리스트 가져오기 Ajax.
     * @작성일 : 2021. 6. 5.
     * @작성자 : SJK
     */
    @PostMapping(value="qmsDepartmentExcelDown")
    public ModelAndView qmsDepartmentExcelDown(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
        Map<String, Object> resMap = new HashMap<>();
        
        // Start. 뷰단에서 넘어온 파일 쿠키 put.
        String fileToken = Converter.toStr(params.get("filetoken"));
        resMap.put("fileToken", fileToken);
        // End.
        
        params.put("where", "excel");
        resMap.putAll(orderSvc.getQmsDepartmentListAjax(params, req));
        
        return new ModelAndView(new QmsDepartmentExcel(), resMap);
    }
    
    
    /**
     * QMS 통계 페이지
     * @작성일 : 2021. 6. 12.
     * @작성자 : jsh
     */
    @GetMapping(value="qmsStastics")
    public String qmsStastics(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
        List<Map<String, Object>> releaseYearList = orderSvc.getQmsReleaseYearList(params, req);
        
        model.addAttribute("releaseYearList", releaseYearList);
        
        // jsh 시작일자 -30일 기본값 적용
        Calendar cal = Calendar.getInstance();
        Date toDayDate = Converter.dateAdd(new Date(),5,-30);
        String toDay = Converter.dateToStr("yyyy-MM-dd",toDayDate);
        String fromDay = Converter.dateToStr("yyyy-MM-dd");
        model.addAttribute("ordersdt", toDay);
        model.addAttribute("orderedt", fromDay);
        
     // 현재날짜 기준 직전분기
        String preYear = fromDay.substring(0,4) + "년";
        String preQuat = "";
        String tmpYear = fromDay.substring(0,4);
        String tmpDay = fromDay.substring(5);
        String[] rangeDay = tmpDay.split("-");
        if(Integer.parseInt(rangeDay[0]) <= 3) {
            preYear = String.valueOf(Integer.parseInt(fromDay.substring(0,4)) - 1) + "년";
            preQuat = "4분기";
        }else if(Integer.parseInt(rangeDay[0]) >= 4 && Integer.parseInt(rangeDay[0]) <= 6){
            preQuat = "1분기";
        }else if(Integer.parseInt(rangeDay[0]) >= 7 && Integer.parseInt(rangeDay[0]) <= 9){
            preQuat = "2분기";
        }else{
            preQuat = "3분기";
        }
        
        model.addAttribute("preYear", preYear);
        model.addAttribute("preQuat", preQuat);
        
        return "admin/system/qmsStastics";
    }
    
    /**
     * QMS 통계(QMS 회수 실적현황) 가져오기 Ajax.
     * @작성일 : 2021. 6. 19.
     * @작성자 : SJK
     */
    @ResponseBody
    @PostMapping(value="getQmsStasticsSalesListAjax")
    public Object getQmsStasticsSalesListAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
        Map<String, Object> resList = orderSvc.getQmsStasticsSalesListAjax(params, req);
        return resList;
    }
    
    /**
     * QMS 통계(팀별 QMS 회수율) 가져오기 Ajax.
     * @작성일 : 2021. 6. 19.
     * @작성자 : SJK
     */
    @ResponseBody
    @PostMapping(value="getQmsStasticsTeamListAjax")
    public Object getQmsStasticsTeamListAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
        Map<String, Object> resList = orderSvc.getQmsStasticsTeamListAjax(params, req);
        return resList;
    }
    
    /**
     * QMS 통계(QMS 회수 실적현황) Excel Down
     * @작성일 : 2021. 6. 19.
     * @작성자 : SJK
     */
    @PostMapping(value="qmsStasticsSalesExcelDown")
    public ModelAndView qmsStasticsSalesExcelDown(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
        Map<String, Object> resMap = new HashMap<>();
        
        // Start. 뷰단에서 넘어온 파일 쿠키 put.
        String fileToken = Converter.toStr(params.get("filetoken"));
        resMap.put("fileToken", fileToken);
        // End.
        
        params.put("where", "excel");
        resMap.putAll(orderSvc.getQmsStasticsSalesListAjax(params, req));
        
        return new ModelAndView(new QmsStasticsSalesExcel(), resMap);
    }
    
    /**
     * QMS 통계(팀별 QMS 회수율) Excel Down
     * @작성일 : 2021. 6. 19.
     * @작성자 : SJK
     */
    @PostMapping(value="qmsStasticsTeamExcelDown")
    public ModelAndView qmsStasticsTeamExcelDown(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
        Map<String, Object> resMap = new HashMap<>();
        
        // Start. 뷰단에서 넘어온 파일 쿠키 put.
        String fileToken = Converter.toStr(params.get("filetoken"));
        resMap.put("fileToken", fileToken);
        // End.
        
        params.put("where", "excel");
        resMap.putAll(orderSvc.getQmsStasticsTeamListAjax(params, req));
        
        return new ModelAndView(new QmsStasticsTeamExcel(), resMap);
    }
    
    
    /**
     * QMS 마감 페이지
     * @작성일 : 2021. 6. 13.
     * @작성자 : jsh
     */
    @GetMapping(value="qmsDedalines")
    public String qmsDedalines(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
        
        List<Map<String, Object>> yearList = orderSvc.getQmsYearList(params, req);
        
        model.addAttribute("yearList", yearList); // return ADD/EDIT/VIEW.
        
        return "admin/system/qmsDedalines";
    }
    
    /**
     * QMS 마감 페이지 리스트 가져오기 Ajax.
     * @작성일 : 2021. 6. 13.
     * @작성자 : SJK
     */
    @ResponseBody
    @PostMapping(value="getQmsDedalinesListAjax")
    public Object getQmsDedalinesListAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
        Map<String, Object> resList = orderSvc.getQmsDedalinesListAjax(params, req);
        return resList;
    }
    
    /**
     * QMS 마감 저장.
     * @작성일 : 2021. 6. 13.
     * @작성자 : SJK
     */
    @ResponseBody
    @PostMapping(value="mergeQmsDedalinesAjax")
    public Object mergeQmsDedalinesAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
        Map<String, Object> resMap = orderSvc.mergeQmsDedalinesAjaxTransaction(params, req, loginDto);
        return resMap;
    }
    
    /**
     * QMS 마감 Excel Down Ajax.
     * @작성일 : 2021. 6. 5.
     * @작성자 : SJK
     */
    @PostMapping(value="qmsDedalinesExcelDown")
    public ModelAndView qmsDedalinesExcelDown(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
        Map<String, Object> resMap = new HashMap<>();
        
        // Start. 뷰단에서 넘어온 파일 쿠키 put.
        String fileToken = Converter.toStr(params.get("filetoken"));
        resMap.put("fileToken", fileToken);
        // End.
        
        params.put("where", "excel");
        resMap.putAll(orderSvc.getQmsDedalinesListAjax(params, req));
        
        return new ModelAndView(new QmsDedalinesExcel(), resMap);
    }
    
	
	/**
	 * 내화구조관리 폼 > 내화구조 리스트 가져오기 Ajax.
	 * @작성일 : 2021. 4. 5.
	 * @작성자 : jihye lee
	 */
	@ResponseBody
	@PostMapping(value="fireproofListAjax")
	public Object fireproofListAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		Map<String, Object> resMap = configSvc.getFireproofList(params, req);
		System.out.println(resMap);
		return resMap;
	}

	/**
	 * 내화구조관리 폼 > 출고지 저장/수정 Ajax.
	 * @작성일 : 2021. 4. 5.
	 * @작성자 : jihye lee
	 */
	@ResponseBody
	@PostMapping(value="insertUpdateFireproofAjax")
	public Object insertUpdateFireproofAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		Map<String, Object> resMap = configSvc.insertUpdateFireproofTransaction(params, req, loginDto);
		return resMap;
	}
	
	/**
	 * OK !!!
	 * 내화방화 세부정보
	 * @작성일 : 2021. 4. 5.
	 * @작성자 : jihye lee
	 */
	@PostMapping(value="itemViewPop")
	public String itemViewPop(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		String r_itemcd = Converter.toStr(params.get("r_itemcd"));
		model.addAttribute("item", configSvc.getFireProofOne(r_itemcd));
		return "admin/system/itemViewPop";
	}
	
	/**
	 * OK !!!
	 * 내화방화 세부정보
	 * @작성일 : 2021. 4. 5.
	 * @작성자 : jihye lee
	 */	
	@ResponseBody
	@PostMapping(value="updateFireproofImageAjax")
	public Object updateFireproofImageAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		return configSvc.updateFireproofTransaction(params, req, loginDto);
	}
	
	
	/**
	 * 공통코드 폼.
	 * @작성일 : 2020. 2. 26.
	 * @작성자 : kkyu
	 */
	@GetMapping(value="commonCodeConfig")
	public String commonCodeConfig(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		return "admin/system/commonCodeConfig";
	}
	
	/**
	 * 공통코드 폼 > Tree JqGrid 리스트 데이터 가져오기 Ajax.
	 * @작성일 : 2020. 3. 6.
	 * @작성자 : kkyu
	 */
	@ResponseBody
	@PostMapping(value="getCommonCodeTreeListAjax")
	public Object getCommonCodeTreeListAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		List<Map<String, Object>> resList = commonCodeSvc.getTreeList();
		return resList;
	}
	
	/**
	 * 공통코드 폼 > 상세  JqGrid 리스트 데이터 가져오기 Ajax.
	 * @작성일 : 2020. 3. 6.
	 * @작성자 : kkyu
	 */
	@ResponseBody
	@PostMapping(value="getCommonCodeDetailListAjax")
	public Object getCommonCodeDetailListAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		String r_ccparent = Converter.toStr(params.get("r_ccparent"));
		
		if (!StringUtils.equals("", r_ccparent)) {
			return commonCodeSvc.getDetailList(Converter.toStr(params.get("r_ccparent")));
		} else {
			return null;
		}
	}
	
	/**
	 * 공통코드 폼 > Tree JqGrid 리스트 > 저장/수정 Ajax.
	 * @작성일 : 2020. 3. 6.
	 * @작성자 : kkyu
	 */
	@ResponseBody
	@PostMapping(value="insertUpdateCommonRootCodeAjax")
	public Object insertUpdateCommonRootCodeAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		return commonCodeSvc.insertUpdateCommonRootCodeTransaction(params, req);
	}
	
	/**
	 * 공통코드 폼 > 상세  JqGrid 리스트 > 저장 Ajax.
	 * @작성일 : 2020. 3. 6.
	 * @작성자 : kkyu
	 */
	@ResponseBody
	@PostMapping(value="insertUpdateCommonCodeAjax")
	public Object insertUpdateCommonCodeAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		return commonCodeSvc.inserUpdateCommonCodeTransaction(params, req);
	}
	
	/**
	 * 공통코드 폼 > 상세  JqGrid 리스트 > 로우 순서 변경 Ajax.
	 * @작성일 : 2020. 3. 7.
	 * @작성자 : kkyu
	 */
	@ResponseBody
	@PostMapping(value="updateCommonCodeSortAjax")
	public Object updateCommonCodeSortAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		return commonCodeSvc.updateCommonCodeSortTransaction(params, req);
	}
	
	/**
	 * 권한관리 폼.
	 * @작성일 : 2020. 2. 26.
	 * @작성자 : kkyu
	 */
	@GetMapping(value="authorityConfig")
	public String authorityConfig(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		model.addAttribute("roleList", menuSvc.getRoleList("Y"));
		return "admin/system/authorityConfig";
	}
	
	/**
	 * 권한관리 폼 > 메뉴리스트 가져오기 Ajax
	 * @작성일 : 2020. 2. 26.
	 * @작성자 : kkyu
	 */
	@ResponseBody
	@PostMapping(value="getMenuListForAuthorityAjax")
	public Object getMenuListForAuthorityAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		params.put("r_mnuse", "Y");
		params.put("r_mnbase", "N");
		return menuSvc.getMenuListForAuthority(params);
	}
	
	/**
	 * 권한관리 폼 > 권한 저장 Ajax
	 * @작성일 : 2020. 2. 26.
	 * @작성자 : kkyu
	 */
	@ResponseBody
	@PostMapping(value="insertRoleMenuAjax")
	public Object insertRoleMenuAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		return menuSvc.insertRoleMenuTransaction(params, req, loginDto);
	}
	
	/**
	 * 파일 다운로드.
	 * @작성일 : 2020. 2. 26.
	 * @작성자 : kkyu
	 */
	@GetMapping(value="fileDown")
	public ModelAndView fileDown(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, Model model, LoginDto loginDto) throws Exception {
		return configSvc.fileDown(params, req, model, loginDto);
	}
	
    /**
     * QMS 설정
     * @작성일 : 2021. 6. 19.
     * @작성자 : sjk
     */
    @GetMapping(value="qmsConfig")
    public String qmsConfig(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
        Map<String, Object> resMap = configSvc.getConfigList(params);
        model.addAttribute("configList", resMap);
        return "admin/system/qmsConfig";
    }
    
    /**
     * QMS 발급대장
     * @작성일 : 2021. 6. 19.
     * @작성자 : sjk
     */
    @GetMapping(value="qmsRawStastics")
    public String qmsRawStastics(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
        List<Map<String, Object>> releaseYearList = orderSvc.getQmsReleaseYearList(params, req);
        model.addAttribute("releaseYearList", releaseYearList);
        
        // jsh 시작일자 -30일 기본값 적용
        Date toDayDate = Converter.dateAdd(new Date(),5,-30);
        String toDay = Converter.dateToStr("yyyy-MM-dd",toDayDate);
        String fromDay = Converter.dateToStr("yyyy-MM-dd");
        model.addAttribute("ordersdt", toDay);
        model.addAttribute("orderedt", fromDay);
        
        // 현재날짜 기준 직전분기
        String preYear = fromDay.substring(0,4) + "년";
        String preQuat = "";
        String tmpYear = fromDay.substring(0,4);
        String tmpDay = fromDay.substring(5);
        String[] rangeDay = tmpDay.split("-");
        if(Integer.parseInt(rangeDay[0]) <= 3) {
            preYear = String.valueOf(Integer.parseInt(fromDay.substring(0,4)) - 1) + "년";
            preQuat = "4분기";
        }else if(Integer.parseInt(rangeDay[0]) >= 4 && Integer.parseInt(rangeDay[0]) <= 6){
            preQuat = "1분기";
        }else if(Integer.parseInt(rangeDay[0]) >= 7 && Integer.parseInt(rangeDay[0]) <= 9){
            preQuat = "2분기";
        }else{
            preQuat = "3분기";
        }
        
        model.addAttribute("preYear", preYear);
        model.addAttribute("preQuat", preQuat);
        
        return "admin/system/qmsRawStastics";
    }
    
    /**
     * QMS 발급대장 리스트 가져오기 Ajax.
     * @작성일 : 2021. 6. 5.
     * @작성자 : SJK
     */
    @ResponseBody
    @PostMapping(value="getQmsRawStasticsListAjax")
    public Object getQmsRawStasticsListAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
        Map<String, Object> resList = orderSvc.getQmsRawStasticsListAjax(params, req);
        return resList;
    }
    
    /**
     * QMS조직 설정 리스트 가져오기 Ajax.
     * @작성일 : 2021. 6. 5.
     * @작성자 : SJK
     */
    @PostMapping(value="qmsRawStasticsExcelDown")
    public ModelAndView qmsRawStasticsExcelDown(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
        Map<String, Object> resMap = new HashMap<>();
        
        // Start. 뷰단에서 넘어온 파일 쿠키 put.
        String fileToken = Converter.toStr(params.get("filetoken"));
        resMap.put("fileToken", fileToken);
        // End.
        
        params.put("where", "excel");
        resMap.putAll(orderSvc.getQmsRawStasticsListAjax(params, req));
        
        return new ModelAndView(new QmsRawStasticsExcel(), resMap);
    }
    
    /**
     * OK !!!
     * 내화방화 등록여부 체크
     * @작성일 : 2021. 7. 15.
     * @작성자 : jsh
     */ 
    @ResponseBody
    @PostMapping(value="getFireProofCheck")
    public int getFireProofCheck(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
        return configSvc.getFireProofCheck(params, req, loginDto);
    }
    
    
	/**
	 * QMS 품목관리.
	 * @작성일 : 2023. 10. 23.
	 * @작성자 : Squall
	 */
	@GetMapping(value="qmsItemManagement")
	public String qmsItemManagement(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		return "admin/system/qmsItemManagement";
	}
	
}
