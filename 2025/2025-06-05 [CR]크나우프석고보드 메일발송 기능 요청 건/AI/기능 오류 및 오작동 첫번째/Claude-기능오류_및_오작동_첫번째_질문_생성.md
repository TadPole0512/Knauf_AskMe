밑의 내용을 AI가 어떻게 이해할지와 AI에게 정확한 대답을 얻기 위해서 어떻게 질문해야하는 지 알려줘.
또, 필요한 자료가 있으면 먼저 요청해주고, 자료를 검토한 다음 단계별로 질문을 만들어줘.
반드시 아래 내용을 질문으로 만드는 것에만 대답해주고, 아래 내용의 답변을 하지 말아줘.
 
```
밑에 소스를 검토, 분석해서 아래의 "기능 오류 및 오작동"이 왜 발생하는지와 해결 방법을 알려줘.

기능 오류 및 오작동
1. 담당자 이메일을 선택하고 메일 주소를 수정하면 메일주소 유효성 검사를 하는데, 메일 형식이 틀리면 경고창이 계속 뜸. 닫으면 바로 다시 뜨고, 닫으면 다시 뜸.
2. 비고 입력 하고 저장버튼을 클릭하면 비고는 저장되지 않음. 발송 여부의 체크박스 값만 'Y'로 저장됨. 이메일 주소도 저장되지 않음.
3. 발송 여부를 하나만 체크하고 저장하면 발송여부 두 곳이 모두 'Y'로 저장됨.
4. 새로운 행에 비고 혹은 영업 담당 이메일을 클릭하면 처음 포커스가 담당자 이메일에 가 있음. 입력하다 보면 담당자 이메일에 글씨가 채워짐.
```

---------------------------------------------------------------------

## AI가 이해할 수 있도록 질문을 구성하는 방법

### **필요한 자료 요청**

AI가 정확한 분석을 하려면 다음 자료들이 필요해:

1. **소스 코드 파일들**
   - HTML/JSP 파일 (화면 구성)
   - JavaScript 파일 (클라이언트 로직)
   - Java Controller/Service 클래스 (서버 로직)
   - MyBatis XML 매퍼 파일 (DB 쿼리)

2. **데이터베이스 정보**
   - 관련 테이블 스키마 (CREATE TABLE 문)
   - 현재 데이터 샘플 (SELECT 결과)

3. **설정 파일들**
   - web.xml, applicationContext.xml 등 Spring 설정

### **단계별 질문 구성 방법**

**1단계: 컨텍스트 제공**
```
"담당자 이메일 관리 기능에서 발생하는 문제를 분석해줘. 
이 기능은 [기능 설명]을 하는 화면이고, 
사용 기술은 Java, MyBatis, JQGrid, JavaScript야."
```

**2단계: 구체적 문제 상황 설명**
```
"다음 4가지 문제가 발생하고 있어:

1. 이메일 유효성 검사 시 경고창이 무한 반복됨
   - 발생 조건: 담당자 이메일 선택 → 잘못된 형식 입력 → 저장 시도
   - 현상: 경고창을 닫아도 계속 다시 나타남

2. 데이터 저장 불완전 문제
   - 발생 조건: 비고 입력 + 저장 버튼 클릭
   - 현상: 비고와 이메일은 저장 안됨, 발송여부만 'Y'로 저장

3. 체크박스 상태 동기화 오류
   - 발생 조건: 발송여부 체크박스 하나만 체크 후 저장
   - 현상: 모든 발송여부 필드가 'Y'로 저장됨

4. 포커스 이동 오류
   - 발생 조건: 새 행에서 비고/영업담당 이메일 클릭
   - 현상: 포커스가 담당자 이메일로 이동하여 잘못된 필드에 입력됨"
```

**3단계: 코드 제공 및 분석 요청**
```
"첨부한 소스 코드를 검토해서:
1. 각 문제의 근본 원인을 찾아줘
2. 코드상 어떤 부분이 문제인지 지적해줘  
3. 구체적인 수정 방법을 제시해줘
4. 수정된 완전한 코드를 보여줘"
```

### **효과적인 질문 구조**

**Problem Redefinition 적용:**
- 단순한 "버그 수정"이 아닌 "사용자 경험 개선"과 "데이터 무결성 보장"의 관점에서 접근

**Multidimensional Analysis 활용:**
- 시간적 차원: 언제 발생하는가?
- 계층적 차원: 클라이언트/서버/DB 어느 레벨 문제인가?
- 인과적 차원: 어떤 액션이 어떤 결과를 낳는가?

### **최종 질문 템플릿**

```
"JQGrid와 MyBatis를 사용한 담당자 이메일 관리 기능에서 
다음과 같은 문제들이 발생하고 있어:

[구체적 문제 4가지 나열]

첨부된 소스 코드를 바탕으로:
1. 각 문제의 기술적 원인 분석
2. 코드 레벨에서의 문제점 식별  
3. 단계별 해결 방안 제시
4. 완전한 수정 코드 제공

특히 이벤트 핸들링, 데이터 바인딩, 유효성 검사 로직을 중점적으로 봐줘."
```

자료를 준비해서 이런 식으로 질문하면 AI가 체계적으로 분석하고 정확한 해결책을 제시할 수 있을 거야.

---------------------------------------------------------------------


[단계별 질문 구성]
**1단계: 컨텍스트 제공**
```
"담당자 이메일 관리 기능에서 발생하는 문제를 분석해줘. 
이 기능은 [기능 설명]을 하는 화면이고, 
사용 기술은 Java, MyBatis, JQGrid, JavaScript야."
```
**2단계: 구체적 문제 상황 설명**
```
"다음 4가지 문제가 발생하고 있어:
1. 이메일 유효성 검사 시 경고창이 무한 반복됨
   - 발생 조건: 담당자 이메일 선택 → 잘못된 형식 입력 → 저장 시도
   - 현상: 경고창을 닫아도 계속 다시 나타남
2. 데이터 저장 불완전 문제
   - 발생 조건: 비고 입력 + 저장 버튼 클릭
   - 현상: 비고와 이메일은 저장 안됨, 발송여부만 'Y'로 저장
3. 체크박스 상태 동기화 오류
   - 발생 조건: 발송여부 체크박스 하나만 체크 후 저장
   - 현상: 모든 발송여부 필드가 'Y'로 저장됨
4. 포커스 이동 오류
   - 발생 조건: 새 행에서 비고/영업담당 이메일 클릭
   - 현상: 포커스가 담당자 이메일로 이동하여 잘못된 필드에 입력됨"
```
**3단계: 코드 제공 및 분석 요청**
```
"첨부한 소스 코드를 검토해서:
1. 각 문제의 근본 원인을 찾아줘
2. 코드상 어떤 부분이 문제인지 지적해줘  
3. 구체적인 수정 방법을 제시해줘
4. 수정된 완전한 코드를 보여줘"
```

[첨부 : 소스 코드]
1. **소스 코드 파일들**
   - HTML/JSP 파일 (화면 구성)
   - JavaScript 파일 (클라이언트 로직)
   - Java Controller/Service 클래스 (서버 로직)
   - MyBatis XML 매퍼 파일 (DB 쿼리)

---------------------------------------------------------------------

   - HTML/JSP 파일 (화면 구성)

<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/include/admin/commonimport.jsp" %>
<!DOCTYPE html>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/admin/commonhead.jsp" %>
    <script type="text/javascript" src="${url}/include/js/common/select2/select2.js"></script>
    <link rel="stylesheet" href="${url}/include/js/common/select2/select2.css" />
    <script type="text/javascript">
        // ==================================================================================
        // 전역 변수 및 원본 데이터 저장
        // ==================================================================================
        var originalDataMap = {}; // 원본 데이터 저장용 맵
        var modifiedRowsSet = new Set(); // 수정된 행 ID들을 추적

        // 이메일 형식 유효성 검사 함수
        function validateEmail(email) {
            if (!email || email.trim() === '') return true; // 빈 값은 유효한 것으로 처리
            const emailRegex = /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
            return emailRegex.test(email.trim());
        }

        // 배경색 및 multiselect 동기화 함수
        function syncRowState(rowId, isModified) {
            var grid = $('#gridList');
            if (isModified) {
                $('#gridList #' + rowId).css('background-color', '#ffebcd');
                grid.jqGrid('setSelection', rowId, false);
                modifiedRowsSet.add(rowId);
            } else {
                $('#gridList #' + rowId).css('background-color', '');
                if (!hasOtherModifications(rowId)) {
                    var selectedRows = grid.jqGrid('getGridParam', 'selarrrow');
                    if (selectedRows.indexOf(rowId) !== -1) {
                        grid.jqGrid('setSelection', rowId, false);
                    }
                    modifiedRowsSet.delete(rowId);
                }
            }
        }

        // 행에 다른 수정사항이 있는지 확인 - 편집모드 대응 개선
        function hasOtherModifications(rowId) {
            if (!originalDataMap[rowId]) return false;
            var grid = $('#gridList');
            var isEditMode = $('#' + rowId).hasClass('jqgrow-edit');
            var currentData = {};
            if (isEditMode) {
                var row = $('#' + rowId);
                currentData.CUST_MAIN_EMAIL = row.find('input[name="CUST_MAIN_EMAIL"]').val() || '';
                currentData.SALESREP_EMAIL = row.find('input[name="SALESREP_EMAIL"]').val() || '';
                currentData.COMMENTS = row.find('input[name="COMMENTS"]').val() || '';
                currentData.CUST_SENDMAIL_YN = row.find('.mail-checkbox[data-field="CUST_SENDMAIL_YN"]').is(':checked') ? 'Y' : 'N';
                currentData.SALESREP_SENDMAIL_YN = row.find('.mail-checkbox[data-field="SALESREP_SENDMAIL_YN"]').is(':checked') ? 'Y' : 'N';
            } else {
                currentData = getCleanRowData(rowId);
            }
            var originalData = originalDataMap[rowId];
            var editableFields = ['CUST_MAIN_EMAIL', 'CUST_SENDMAIL_YN', 'SALESREP_EMAIL', 'SALESREP_SENDMAIL_YN', 'COMMENTS'];
            for (var i = 0; i < editableFields.length; i++) {
                var field = editableFields[i];
                var currentValue = (currentData[field] || '').toString().trim();
                var originalValue = (originalData[field] || '').toString().trim();
                if (currentValue !== originalValue) {
                    return true;
                }
            }
            return false;
        }

        // 체크박스 포맷터
        function checkboxFormatter(cellVal, options, rowObj) {
            var checked = (cellVal === 'Y') ? 'checked' : '';
            var rowId = options.rowId;
            return '<input type="checkbox" class="mail-checkbox" ' + checked + 
                ' data-rowid="' + rowId + '" data-field="' + options.colModel.name + '"' +
                ' onclick="handleCheckboxClick(this)" />';
        }

        // 체크박스 클릭 이벤트 핸들러 - 완전 재작성
        function handleCheckboxClick(checkbox) {
            var rowId = $(checkbox).data('rowid');
            var fieldName = $(checkbox).data('field');
            var newValue = checkbox.checked ? 'Y' : 'N';
            var originalValue = originalDataMap[rowId] ? (originalDataMap[rowId][fieldName] || 'N') : 'N';
            $('#gridList').jqGrid('setCell', rowId, fieldName, newValue);
            var isModified = (newValue !== originalValue);
            var hasModifications = hasOtherModifications(rowId);
            syncRowState(rowId, isModified || hasModifications);
        }

        // 이메일 필드 검증 및 상태 동기화 - 편집모드 개선
        function handleEmailChange(input) {
            var rowId = $(input).closest('tr').attr('id');
            var fieldName = $(input).attr('name') || $(input).data('field');
            var newValue = $(input).val().trim();

            if (newValue && !validateEmail(newValue)) {
                alert('올바른 이메일 형식을 입력해주세요.');
                $(input).focus();
                return false;
            }
            var originalValue = originalDataMap[rowId] ? (originalDataMap[rowId][fieldName] || '') : '';
            var isModified = (newValue !== originalValue.trim());
            var hasModifications = hasOtherModifications(rowId);
            syncRowState(rowId, isModified || hasModifications);
            return true;
        }

        // 텍스트 필드 변경 처리 - 편집모드 개선
        function handleTextChange(input) {
            var rowId = $(input).closest('tr').attr('id');
            var fieldName = $(input).attr('name') || $(input).data('field');
            var newValue = $(input).val().trim();
            var originalValue = originalDataMap[rowId] ? (originalDataMap[rowId][fieldName] || '') : '';
            var isModified = (newValue !== originalValue.trim());
            var hasModifications = hasOtherModifications(rowId);
            syncRowState(rowId, isModified || hasModifications);
        }

        // multiselect 체크박스 클릭 이벤트 처리
        function handleMultiselectChange() {
            var grid = $('#gridList');
            var selectedRows = grid.jqGrid('getGridParam', 'selarrrow');
            var allRowIds = grid.jqGrid('getDataIDs');
            $.each(allRowIds, function(index, rowId) {
                var isSelected = selectedRows.indexOf(rowId) !== -1;
                var hasModifications = hasOtherModifications(rowId);
                if (isSelected && !hasModifications) {
                    setTimeout(function() {
                        grid.jqGrid('setSelection', rowId, false);
                    }, 10);
                } else if (!isSelected && hasModifications) {
                    grid.jqGrid('setSelection', rowId, false);
                    modifiedRowsSet.add(rowId);
                }
            });
        }

        $(function(){
            getGridList();
        });

        // 수정된 행 데이터 가져오기 - 편집모드 대응 완전 재작성
        function getModifiedRows() {
            var grid = $('#gridList');
            var selectedRows = grid.jqGrid('getGridParam', 'selarrrow');
            var modifiedRows = [];
            $.each(selectedRows, function(index, rowId) {
                if (hasOtherModifications(rowId)) {
                    var isEditMode = $('#' + rowId).hasClass('jqgrow-edit');
                    if (isEditMode) {
                        grid.jqGrid('saveRow', rowId);
                    }
                    var rowData = getCleanRowData(rowId);
                    if (rowData) {
                        modifiedRows.push(rowData);
                    }
                }
            });
            return modifiedRows;
        }

        // 편집모드를 고려한 깨끗한 행 데이터 가져오기
        function getCleanRowData(rowId) {
            var grid = $('#gridList');
            var rowData = grid.jqGrid('getRowData', rowId);
            var cleanData = {};
            $.each(rowData, function(key, value) {
                if (typeof value === 'string') {
                    if (value.indexOf('<input') !== -1) {
                        var $temp = $('<div>').html(value);
                        var inputValue = $temp.find('input').val();
                        cleanData[key] = inputValue || '';
                    } else if (value.indexOf('<') !== -1) {
                        var $temp = $('<div>').html(value);
                        cleanData[key] = $temp.text().trim();
                    } else {
                        cleanData[key] = value;
                    }
                } else {
                    cleanData[key] = value;
                }
            });
            return cleanData;
        }

        var lastSelection;
        function editRow(id){
            if (id && id !== lastSelection) {
                var grid = $('#gridList');
                grid.jqGrid('editRow', id, {
                    keys: true, 
                    focusField: true,
                    oneditfunc: function(rowId) {
                        var row = $('#' + rowId);
                        row.find('input[name="CUST_MAIN_EMAIL"]').on('blur', function() {
                            $(this).data('field', 'CUST_MAIN_EMAIL');
                            handleEmailChange(this);
                        });
                        row.find('input[name="SALESREP_EMAIL"]').on('blur', function() {
                            $(this).data('field', 'SALESREP_EMAIL');
                            handleEmailChange(this);
                        });
                        row.find('input[name="COMMENTS"]').on('blur', function() {
                            $(this).data('field', 'COMMENTS');
                            handleTextChange(this);
                        });
                    }
                });
                lastSelection = id;
            }
        }

        function dataSave(obj) {
            $(obj).prop('disabled', true);
            var grid = $('#gridList');
            var allRowIds = grid.jqGrid('getDataIDs');
            $.each(allRowIds, function(index, rowId) {
                var isEditMode = $('#' + rowId).hasClass('jqgrow-edit');
                if (isEditMode) {
                    grid.jqGrid('saveRow', rowId);
                }
            });
            var modifiedRows = getModifiedRows();
            if (modifiedRows.length === 0) {
                alert('수정된 내용이 없습니다.');
                $(obj).prop('disabled', false);
                return false;
            }
            var validationFailed = false;
            $.each(modifiedRows, function(i, rowData) {
                if (rowData.CUST_MAIN_EMAIL && !validateEmail(rowData.CUST_MAIN_EMAIL)) {
                    alert('거래처 담당자 이메일 형식이 올바르지 않습니다. (거래처코드: ' + rowData.CUST_CD + ')');
                    validationFailed = true;
                    return false;
                }
                if (rowData.SALESREP_EMAIL && !validateEmail(rowData.SALESREP_EMAIL)) {
                    alert('영업 담당 이메일 형식이 올바르지 않습니다. (거래처코드: ' + rowData.CUST_CD + ')');
                    validationFailed = true;
                    return false;
                }
                if (rowData.CUST_SENDMAIL_YN === 'Y' && !rowData.CUST_MAIN_EMAIL) {
                    alert('담당자 이메일 발송이 체크되어 있지만 담당자 이메일이 비어있습니다. (거래처코드: ' + rowData.CUST_CD + ')');
                    validationFailed = true;
                    return false;
                }
                if (rowData.SALESREP_SENDMAIL_YN === 'Y' && !rowData.SALESREP_EMAIL) {
                    alert('영업 담당 이메일 발송이 체크되어 있지만 영업 담당 이메일이 비어있습니다. (거래처코드: ' + rowData.CUST_CD + ')');
                    validationFailed = true;
                    return false;
                }
            });
            if (validationFailed) {
                $(obj).prop('disabled', false);
                return false;
            }
            var iFormObj = $('form[name="iForm"]');
            iFormObj.empty();
            $.each(modifiedRows, function(i, rowData) {
                iFormObj.append('<input type="hidden" name="custCd" value="' + rowData.CUST_CD + '" />');
                iFormObj.append('<input type="hidden" name="custMainEmail" value="' + (rowData.CUST_MAIN_EMAIL || '') + '" />');
                iFormObj.append('<input type="hidden" name="custSendmailYn" value="' + (rowData.CUST_SENDMAIL_YN || 'N') + '" />');
                iFormObj.append('<input type="hidden" name="salesrepEmail" value="' + (rowData.SALESREP_EMAIL || '') + '" />');
                iFormObj.append('<input type="hidden" name="salesrepSendmailYn" value="' + (rowData.SALESREP_SENDMAIL_YN || 'N') + '" />');
                iFormObj.append('<input type="hidden" name="comments" value="' + (rowData.COMMENTS || '') + '" />');
            });
            if (confirm('저장 하시겠습니까?')) {
                var iFormData = iFormObj.serialize();
                var url = '${url}/admin/system/updateOrderMailAlarmAjax.lime'; 
                $.ajax({
                    async : false,
                    data : iFormData,
                    type : 'POST',
                    url : url,
                    success : function(data) {
                        if (data.RES_CODE == '0000') {
                            alert(data.RES_MSG);
                            resetAllStates();
                            dataSearch();
                        } else {
                            alert(data.RES_MSG);
                        }
                        $(obj).prop('disabled', false);
                    },
                    error : function(request,status,error){
                        alert('Error');
                        $(obj).prop('disabled', false);
                    }
                });
            } else {
                $(obj).prop('disabled', false);
            }
        }

        // 모든 상태 초기화
        function resetAllStates() {
            var grid = $('#gridList');
            var allRowIds = grid.jqGrid('getDataIDs');
            $.each(allRowIds, function(index, rowId) {
                $('#gridList #' + rowId).css('background-color', '');
            });
            grid.jqGrid('resetSelection');
            modifiedRowsSet.clear();
        }

        // ==================================================================================
        // jqGrid Columns Order 설정
        // ==================================================================================
        var ckNameJqGrid = 'admin/customer/customerList/jqGridCookie';
        ckNameJqGrid += '/gridList';
        var globalColumnOrderStr = toStr(decodeURIComponent(getCookie(ckNameJqGrid)));
        var globalColumnOrder = globalColumnOrderStr.split(',');
        var defaultColModel = [
            {name:"CUST_CD", key:true, label:'거래처코드', width:120, align:'center', sortable:true},
            {name:"CUST_NM", label:'거래처명', width:220, align:'left', sortable:true},
            {name:"CUST_MAIN_EMAIL", label:'담당자 이메일', width:220, align:'center', sortable:true, editable:true},
            {name:"CUST_SENDMAIL_YN", label:'발송 여부', width:100, align:'center', sortable:true, formatter:checkboxFormatter},
            {name:"SALESREP_NM", label:'영업 담당', width:100, align:'center', sortable:true},
            {name:"SALESREP_EMAIL", label:'영업 담당 이메일', width:300, align:'center', sortable:true, editable:true},
            {name:"SALESREP_SENDMAIL_YN", label:'발송 여부', width:100, align:'center', sortable:true, formatter:checkboxFormatter},
            {name:"COMMENTS", label:'비고', width:450, align:'left', sortable:true, editable:true}
        ];
        var defaultColumnOrder = writeIndexToStr(defaultColModel.length);
        var updateComModel = [];
        if (0 < globalColumnOrder.length) {
            if (defaultColModel.length == globalColumnOrder.length) {
                for (var i = 0, j = globalColumnOrder.length; i < j; i++) {
                    updateComModel.push(defaultColModel[globalColumnOrder[i]]);
                }
                setCookie(ckNameJqGrid, globalColumnOrder, 365);
            } else {
                updateComModel = defaultColModel;
                setCookie(ckNameJqGrid, defaultColumnOrder, 365);
            }
        } else {
            updateComModel = defaultColModel;
            setCookie(ckNameJqGrid, defaultColumnOrder, 365);
        }

        // ==================================================================================
        // jqGrid Column Width 설정
        // ==================================================================================
        var ckNameJqGridWidth = ckNameJqGrid + '/width';
        var globalColumnWidthStr = toStr(decodeURIComponent(getCookie(ckNameJqGridWidth)));
        var globalColumnWidth = globalColumnWidthStr.split(',');
        var defaultColumnWidthStr = '';
        var defaultColumnWidth;
        var updateColumnWidth;
        if ('' != globalColumnWidthStr) {
            if (updateComModel.length == globalColumnWidth.length) {
                updateColumnWidth = globalColumnWidth;
            } else {
                for (var j = 0; j < updateComModel.length; j++) {
                    if ('rn' != updateComModel[j].name && 'cb' != updateComModel[j].name) {
                        var v = ('' != toStr(updateComModel[j].width)) ? toStr(updateComModel[j].width) : '0';
                        if ('' == defaultColumnWidthStr) {
                            defaultColumnWidthStr = v;
                        } else {
                            defaultColumnWidthStr += ',' + v;
                        }
                    }
                }
                defaultColumnWidth = defaultColumnWidthStr.split(',');
                updateColumnWidth = defaultColumnWidth;
                setCookie(ckNameJqGridWidth, defaultColumnWidth, 365);
            }
        } else {
            for (var j = 0; j < updateComModel.length; j++) {
                if ('rn' != updateComModel[j].name && 'cb' != updateComModel[j].name) {
                    var v = ('' != toStr(updateComModel[j].width)) ? toStr(updateComModel[j].width) : '0';
                    if ('' == defaultColumnWidthStr) {
                        defaultColumnWidthStr = v;
                    } else {
                        defaultColumnWidthStr += ',' + v;
                    }
                }
            }
            defaultColumnWidth = defaultColumnWidthStr.split(',');
            updateColumnWidth = defaultColumnWidth;
            setCookie(ckNameJqGridWidth, defaultColumnWidth, 365);
        }
        if (updateComModel.length == globalColumnWidth.length) {
            for (var j = 0; j < updateComModel.length; j++) {
                updateComModel[j].width = toStr(updateColumnWidth[j]);
            }
        }

        function getGridList(){
            var searchData = getSearchData();
            $('#gridList').jqGrid({
                url: "${url}/admin/customer/getOrderEmailAlarmAjax.lime",
                editurl: 'clientArray',
                datatype: "json",
                mtype: 'POST',
                postData: searchData,
                colModel: updateComModel,
                height: '360px',
                autowidth: false,
                multiselect: true,
                rowNum: 10,
                rowList: ['10', '30', '50', '100'],
                rownumbers: true,
                pagination: true,
                pager: "#pager",
                actions : true,
                pginput : true,
                jsonReader: {
                    root: 'list',
                    id: 'CUST_CD'
                },
                loadComplete: function(data) {
                    originalDataMap = {};
                    modifiedRowsSet.clear();
                    if (data && data.list) {
                        $.each(data.list, function(index, item) {
                            originalDataMap[item.CUST_CD] = $.extend(true, {}, item);
                        });
                    }
                    $('#cb_gridList').off('click').on('click', function() {
                        setTimeout(handleMultiselectChange, 50);
                    });
                },
                sortable: {
                    update: function(relativeColumnOrder) {
                        var grid = $('#gridList');
                        var defaultColIndicies = [];
                        for (var i = 0; i < defaultColModel.length; i++) {
                            defaultColIndicies.push(defaultColModel[i].name);
                        }
                        globalColumnOrder = [];
                        var columnOrder = [];
                        var currentColModel = grid.getGridParam('colModel');
                        for (var j = 0; j < relativeColumnOrder.length; j++) {
                            if ('rn' != currentColModel[j].name && 'cb' != currentColModel[j].name) {
                                columnOrder.push(defaultColIndicies.indexOf(currentColModel[j].name));
                            }
                        }
                        globalColumnOrder = columnOrder;
                        setCookie(ckNameJqGrid, globalColumnOrder, 365);
                        var tempUpdateColumnWidth = [];
                        for (var j = 0; j < currentColModel.length; j++) {
                            if ('rn' != currentColModel[j].name && 'cb' != currentColModel[j].name) {
                                tempUpdateColumnWidth.push(currentColModel[j].width);
                            }
                        }
                        updateColumnWidth = tempUpdateColumnWidth;
                        setCookie(ckNameJqGridWidth, updateColumnWidth, 365);
                    }
                },
                resizeStop: function(width, index) {
                    var minusIdx = 0;
                    var grid = $('#gridList');
                    var currentColModel = grid.getGridParam('colModel');
                    if ('rn' == currentColModel.name || 'cb' == currentColModel.name) minusIdx--;
                    if ('rn' == currentColModel.name || 'cb' == currentColModel.name) minusIdx--;
                    var resizeIdx = index + minusIdx;
                    updateColumnWidth[resizeIdx] = width;
                    setCookie(ckNameJqGridWidth, updateColumnWidth, 365);
                },
                sortorder: 'desc',
                onSelectRow: function(rowId, status, e){
                    if (e && $(e.target).is('input[type="checkbox"]') && $(e.target).closest('td').hasClass('cbox')) {
                        setTimeout(function() {
                            var hasModifications = hasOtherModifications(rowId);
                            if (!hasModifications) {
                                $('#gridList').jqGrid('setSelection', rowId, false);
                            }
                        }, 10);
                        return;
                    }
                    editRow(rowId);
                }
            });
        }

        function getSearchData(){
            var rl_custcd = $('input[name="searchCustCd"]').val();
            var rl_custnm = $('input[name="searchCustNm"]').val();
            var rl_salesrepnm = $('input[name="searchSalesrepNm"]').val();
            var searchData = {
                rl_custcd : rl_custcd,
                rl_custnm : rl_custnm,
                rl_salesrepnm : rl_salesrepnm
            };
            return searchData;
        }

        // 조회
        function dataSearch() {
            var searchData = getSearchData();
            $('#gridList').setGridParam({
                postData : searchData
            }).trigger("reloadGrid");
        }

        // 엑셀다운로드
        function excelDown(obj){
            $('#ajax_indicator').show().fadeIn('fast');
            var token = getFileToken('excel');
            $('form[name="frm"]').append('<input type="hidden" name="filetoken" value="'+token+'" />');
            formPostSubmit('frm', '${url}/admin/system/orderMailAlarmExcelDown.lime');
            $('form[name="frm"]').attr('action', '');
            $('input[name="filetoken"]').remove();
            var fileTimer = setInterval(function() {
                if('true' == getCookie(token)){
                    $('#ajax_indicator').fadeOut();
                    delCookie(token);
                    clearInterval(fileTimer);
                }
            }, 1000 );
        }
    </script>
</head>
<body class="page-header-fixed compact-menu">
    <div id="ajax_indicator" style="display:none;">
        <p style="position: absolute; top: 50%; left: 50%; margin: -110px 0 0 -110px;">
            <img src="${url}/include/images/common/loadingbar.gif" />
        </p>
    </div>
    <main class="page-content content-wrap">
        <%@ include file="/WEB-INF/views/include/admin/header.jsp" %>
        <%@ include file="/WEB-INF/views/include/admin/left.jsp" %>
        <form name="iForm" method="post"></form>
        <form name="frm" method="post">
            <div class="page-inner">
                <div class="page-title">
                    <h3>
                        주문메일알람 관리
                        <div class="page-right">
                            <button type="button" class="btn btn-line f-black" title="검색" onclick="dataSearch();"><i class="fa fa-search"></i><em>검색</em></button>
                            <button type="button" class="btn btn-line f-black" title="새로고침" onclick="window.location.reload();"><i class="fa fa-refresh"></i><em>새로고침</em></button>
                            <button type="button" class="btn btn-line f-black" title="엑셀다운로드" onclick="excelDown(this);"><i class="fa fa-file-excel-o"></i><em>엑셀다운로드</em></button>
                        </div>
                    </h3>
                </div>
                <div id="main-wrapper">
                    <div class="row">
                        <div class="col-md-12">
                            <div class="panel panel-white">
                                <div class="panel-body no-p">
                                    <div class="tableSearch">
                                        <div class="topSearch">
                                            <ul>
                                                <li>
                                                    <label class="search-h">거래처코드</label>
                                                    <div class="search-c">
                                                        <input type="text" class="search-input" name="searchCustCd" value="${param.rl_custcd}" onkeypress="if(event.keyCode == 13){dataSearch();}" />
                                                    </div>
                                                </li>
                                                <li>
                                                    <label class="search-h">거래처명</label>
                                                    <div class="search-c">
                                                        <input type="text" class="search-input" name="searchCustNm" value="${param.rl_custnm}" onkeypress="if(event.keyCode == 13){dataSearch();}" />
                                                    </div>
                                                </li>
                                                <li>
                                                    <label class="search-h">영업담당</label>
                                                    <div class="search-c">
                                                        <input type="text" class="search-input" name="searchSalesrepNm" value="${param.rl_salesrepnm}" onkeypress="if(event.keyCode == 13){dataSearch();}" />
                                                    </div>
                                                </li>
                                            </ul>
                                        </div>
                                    </div>
                                </div>
                                <div class="panel-body">
                                    <h5 class="table-title listT">TOTAL <span id="listTotalCountSpanId">0</span>EA</h5>
                                    <div class="btnList writeObjectClass">
                                        <button type="button" class="btn btn-info" onclick="dataSave(this);">저장</button>
                                    </div>
                                    <div class="table-responsive in">
                                        <table id="gridList" class="display table tableList nowrap" width="100%" border="0" cellpadding="0" cellspacing="0"></table>
                                        <div id="pager"></div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <%@ include file="/WEB-INF/views/include/admin/footer.jsp" %>
                </div>
            </div>
        </form>
    </main>
</body>
</html>


   - Java Controller/Service 클래스 (서버 로직)

컨트롤

	/**
	 * 거래처현황 > 주문 메일 알람 폼 > 주문 메일 알람 저장 또는 수정 Ajax.
	 * @작성일 : 2025. 8. 18.
	 * @작성자 : hsg
	 */
	@ResponseBody
	@PostMapping(value="insertUpdateOrderEmailAlarmAjax")
	public Object insertUpdateOrderEmailAlarmAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, Model model, LoginDto loginDto) throws Exception {
		return customerSvc.insertUpdateOrderEmailAlarm(params, req, loginDto);
	}


서비스

    public Map<String, Object> insertUpdateOrderEmailAlarm(Map<String, Object> params, HttpServletRequest req, LoginDto loginDto) throws Exception {
        // 배열로 받기
        String[] custCdArray = req.getParameterValues("custCd");
        String[] custMainEmailArray = req.getParameterValues("custMainEmail");
        String[] custSendmailYnArray = req.getParameterValues("custSendmailYn");
        String[] salesrepEmailArray = req.getParameterValues("salesrepEmail");
        String[] salesrepSendmailYnArray = req.getParameterValues("salesrepSendmailYn");
        String[] commentsArray = req.getParameterValues("comments");
        
        if (custCdArray == null) {
            return MsgCode.getResultMap(MsgCode.ERROR, "데이터가 없습니다.");
        }
        
        int totalCount = custCdArray.length;
        for (int i = 0; i < totalCount; i++) {
            String custCd = custCdArray[i];
            String custMainEmail = (custMainEmailArray != null && i < custMainEmailArray.length) ? custMainEmailArray[i] : "";
            String custSendmailYn = (custSendmailYnArray != null && i < custSendmailYnArray.length) ? custSendmailYnArray[i] : "N";
            String salesrepEmail = (salesrepEmailArray != null && i < salesrepEmailArray.length) ? salesrepEmailArray[i] : "";
            String salesrepSendmailYn = (salesrepSendmailYnArray != null && i < salesrepSendmailYnArray.length) ? salesrepSendmailYnArray[i] : "N";
            String comments = (commentsArray != null && i < commentsArray.length) ? commentsArray[i] : "";
            
            // 나머지 로직은 동일
            String inid = loginDto.getUserId();
            String moid = loginDto.getUserId();
            Map<String, Object> svcMap = new HashMap<>();
            svcMap.put("m_custCd", custCd);
            svcMap.put("m_custMainEmail", custMainEmail);
            svcMap.put("m_custSendmailYn", custSendmailYn);
            svcMap.put("m_salesrepEmail", salesrepEmail);
            svcMap.put("m_salesrepSendmailYn", salesrepSendmailYn);
            svcMap.put("m_comments", comments);
            svcMap.put("m_inid", inid);
            svcMap.put("m_moid", moid);
            customerDao.insertUpdateOrderEmailAlarm(svcMap);
        }
        return MsgCode.getResultMap(MsgCode.SUCCESS);
    }


DAO

	/**
	 * 주문 메일 알람 저장
	 * @param svcMap
	 * @return
	 */
	public List<Map<String, Object>> insertUpdateOrderEmailAlarm(Map<String, Object> svcMap) {
		return sqlSession.selectList("eorder.o_customer.insertUpdateOrderEmailAlarm", svcMap);
	}


   - MyBatis XML 매퍼 파일 (DB 쿼리)

    <!--
      파라미터 설명 (parameterType은 VO/DTO로 대체 권장):
        custCd                : String (PK)
        custMainEmail         : String
        custSendmailYn        : String ('Y'/'N')
        salesrepEmail         : String
        salesrepSendmailYn    : String ('Y'/'N')
        comments              : String
        inid                  : String
        moid                  : String
      날짜(INDATE/MODATE)는 서버시간(GETDATE)로 처리
    -->
    <update id="insertUpdateOrderEmailAlarm" parameterType="map">
        <![CDATA[
        MERGE dbo.O_CUSTOMER_MAILINFO AS T
        USING (
                 SELECT
                        #{m_custCd}               AS CUST_CD
                      , #{m_custMainEmail}        AS CUST_MAIN_EMAIL
                      , #{m_custSendmailYn}       AS CUST_SENDMAIL_YN
                      , #{m_salesrepEmail}        AS SALESREP_EMAIL
                      , #{m_salesrepSendmailYn}   AS SALESREP_SENDMAIL_YN
                      , #{m_comments}             AS COMMENTS
                      , #{m_inid}                 AS INID
                      , GETDATE()                 AS INDATE
                      , #{m_moid}                 AS MOID
                      , GETDATE()                 AS MODATE
              ) AS S
           ON T.CUST_CD = S.CUST_CD

         WHEN MATCHED THEN
              UPDATE SET
                     T.CUST_MAIN_EMAIL       = S.CUST_MAIN_EMAIL
                   , T.CUST_SENDMAIL_YN      = S.CUST_SENDMAIL_YN
                   , T.SALESREP_EMAIL        = S.SALESREP_EMAIL
                   , T.SALESREP_SENDMAIL_YN  = S.SALESREP_SENDMAIL_YN
                   , T.COMMENTS              = S.COMMENTS
                   , T.MOID                  = S.MOID
                   , T.MODATE                = S.MODATE

         WHEN NOT MATCHED THEN
              INSERT (
                        CUST_CD
                      , CUST_MAIN_EMAIL
                      , CUST_SENDMAIL_YN
                      , SALESREP_EMAIL
                      , SALESREP_SENDMAIL_YN
                      , COMMENTS
                      , INID
                      , INDATE
                     )
              VALUES (
                        S.CUST_CD
                      , S.CUST_MAIN_EMAIL
                      , S.CUST_SENDMAIL_YN
                      , S.SALESREP_EMAIL
                      , S.SALESREP_SENDMAIL_YN
                      , S.COMMENTS
                      , S.INID
                      , S.INDATE
                     );
        ]]>
    </update>


---------------------------------------------------------------------

2. **데이터베이스 정보**
   - 관련 테이블 스키마 (CREATE TABLE 문)
   - 현재 데이터 샘플 (SELECT 결과)

---------------------------------------------------------------------

CREATE TABLE O_CUSTOMER_MAILINFO (
	CUST_CD    VARCHAR(10),
	CUST_MAIN_EMAIL    VARCHAR(100),
	CUST_SENDMAIL_YN    VARCHAR(1),
	SALESREP_EMAIL    VARCHAR(100),
	SALESREP_SENDMAIL_YN VARCHAR(1),
	COMMENTS	VARCHAR(2000),
	INID    VARCHAR(10),
	INDATE    DATETIME2,
	MOID    VARCHAR(10),
	MODATE    DATETIME2,
	CONSTRAINT O_CUSTOMER_MAILINFO_PR PRIMARY KEY(CUST_CD)
)
;


---------------------------------------------------------------------
/* ******************************************************************************************************************************* */
/* ******************************************************************************************************************************* */
/* ******************************************************************************************************************************* */
/* ******************************************************************************************************************************* */
/* ******************************************************************************************************************************* */
---------------------------------------------------------------------

## 분석 방법론 적용

**Problem Redefinition (PR) 적용:**
- 원본 문제(P₀): "버그 수정 요청"
- 관점 전환(T): "사용자 인터페이스 상태 관리 문제"로 재정의
- 범위 조정(S): 개별 버그가 아닌 전체적인 데이터 동기화 아키텍처 문제
- 메타 레벨 이동(M): 클라이언트-서버 간 데이터 일관성 문제

**Multidimensional Analysis (MDA) 적용:**
- 시간적 차원(D1): 사용자 액션 → 이벤트 핸들링 → 데이터 저장 과정
- 계층적 차원(D5): UI 레벨, 비즈니스 로직 레벨, 데이터 레벨 분석

---

## 코드 분석 결과

### **문제 1: 이메일 유효성 검사 무한 루프**

**원인:**
```javascript
// handleEmailChange 함수에서
if (newValue && !validateEmail(newValue)) {
    alert('올바른 이메일 형식을 입력해주세요.');
    $(input).focus();  // ← 이 부분이 문제
    return false;
}
```
`blur` 이벤트에서 `focus()`를 다시 호출하면서 무한 루프 발생

### **문제 2: 비고 및 이메일 저장 안되는 문제**

**원인:**
```javascript
// getModifiedRows 함수에서
$.each(selectedRows, function(index, rowId) {
    if (hasOtherModifications(rowId)) {  // ← 조건 문제
        // 저장 로직
    }
});
```
`selectedRows`에만 의존하여 실제 수정된 데이터를 놓치고 있음

### **문제 3: 체크박스 동기화 오류**

**원인:**
```javascript
// handleCheckboxClick에서 
var isModified = (newValue !== originalValue);
var hasModifications = hasOtherModifications(rowId);
syncRowState(rowId, isModified || hasModifications);  // ← 로직 문제
```
개별 체크박스 상태와 전체 행 상태가 혼재되어 관리됨

### **문제 4: 포커스 이동 오류**

**원인:**
```javascript
onSelectRow: function(rowId, status, e){
    // 중간 생략
    editRow(rowId);  // ← 무조건 editRow 호출
}

function editRow(id){
    grid.jqGrid('editRow', id, {
        focusField: true,  // ← 항상 첫 번째 필드에 포커스
    });
}
```

---

## 수정된 완전한 코드
```js
// ==================================================================================
// 전역 변수 및 원본 데이터 저장
// ==================================================================================
var originalDataMap = {}; // 원본 데이터 저장용 맵
var modifiedRowsSet = new Set(); // 수정된 행 ID들을 추적
var emailValidationInProgress = false; // 이메일 검증 진행 중 플래그

// 이메일 형식 유효성 검사 함수
function validateEmail(email) {
    if (!email || email.trim() === '') return true; // 빈 값은 유효한 것으로 처리
    const emailRegex = /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    return emailRegex.test(email.trim());
}

// 배경색 및 multiselect 동기화 함수
function syncRowState(rowId, isModified) {
    var grid = $('#gridList');
    if (isModified) {
        $('#gridList #' + rowId).css('background-color', '#ffebcd');
        grid.jqGrid('setSelection', rowId, true); // 수정: 선택 상태로 변경
        modifiedRowsSet.add(rowId);
    } else {
        $('#gridList #' + rowId).css('background-color', '');
        grid.jqGrid('setSelection', rowId, false);
        modifiedRowsSet.delete(rowId);
    }
}

// 행에 다른 수정사항이 있는지 확인 - 편집모드 대응 개선
function hasOtherModifications(rowId) {
    if (!originalDataMap[rowId]) return false;
    var grid = $('#gridList');
    var isEditMode = $('#' + rowId).hasClass('jqgrow-edit');
    var currentData = {};
    
    if (isEditMode) {
        var row = $('#' + rowId);
        currentData.CUST_MAIN_EMAIL = row.find('input[name="CUST_MAIN_EMAIL"]').val() || '';
        currentData.SALESREP_EMAIL = row.find('input[name="SALESREP_EMAIL"]').val() || '';
        currentData.COMMENTS = row.find('input[name="COMMENTS"]').val() || '';
        currentData.CUST_SENDMAIL_YN = row.find('.mail-checkbox[data-field="CUST_SENDMAIL_YN"]').is(':checked') ? 'Y' : 'N';
        currentData.SALESREP_SENDMAIL_YN = row.find('.mail-checkbox[data-field="SALESREP_SENDMAIL_YN"]').is(':checked') ? 'Y' : 'N';
    } else {
        currentData = getCleanRowData(rowId);
    }
    
    var originalData = originalDataMap[rowId];
    var editableFields = ['CUST_MAIN_EMAIL', 'CUST_SENDMAIL_YN', 'SALESREP_EMAIL', 'SALESREP_SENDMAIL_YN', 'COMMENTS'];
    
    for (var i = 0; i < editableFields.length; i++) {
        var field = editableFields[i];
        var currentValue = (currentData[field] || '').toString().trim();
        var originalValue = (originalData[field] || '').toString().trim();
        if (currentValue !== originalValue) {
            return true;
        }
    }
    return false;
}

// 체크박스 포맷터
function checkboxFormatter(cellVal, options, rowObj) {
    var checked = (cellVal === 'Y') ? 'checked' : '';
    var rowId = options.rowId;
    return '<input type="checkbox" class="mail-checkbox" ' + checked + 
        ' data-rowid="' + rowId + '" data-field="' + options.colModel.name + '"' +
        ' onclick="handleCheckboxClick(this)" />';
}

// 체크박스 클릭 이벤트 핸들러 - 수정: 개별 필드별 처리
function handleCheckboxClick(checkbox) {
    var rowId = $(checkbox).data('rowid');
    var fieldName = $(checkbox).data('field');
    var newValue = checkbox.checked ? 'Y' : 'N';
    var originalValue = originalDataMap[rowId] ? (originalDataMap[rowId][fieldName] || 'N') : 'N';
    
    // 해당 필드만 업데이트
    $('#gridList').jqGrid('setCell', rowId, fieldName, newValue);
    
    // 전체 행의 수정 상태 확인
    var hasModifications = hasOtherModifications(rowId);
    syncRowState(rowId, hasModifications);
}

// 이메일 필드 검증 및 상태 동기화 - 수정: 무한 루프 방지
function handleEmailChange(input) {
    // 이미 검증 중이면 리턴
    if (emailValidationInProgress) return true;
    
    var rowId = $(input).closest('tr').attr('id');
    var fieldName = $(input).attr('name') || $(input).data('field');
    var newValue = $(input).val().trim();

    if (newValue && !validateEmail(newValue)) {
        emailValidationInProgress = true; // 플래그 설정
        alert('올바른 이메일 형식을 입력해주세요.');
        
        // 포커스를 다시 주되, 약간의 지연을 둠
        setTimeout(function() {
            $(input).focus();
            emailValidationInProgress = false; // 플래그 해제
        }, 100);
        
        return false;
    }
    
    var originalValue = originalDataMap[rowId] ? (originalDataMap[rowId][fieldName] || '') : '';
    var hasModifications = hasOtherModifications(rowId);
    syncRowState(rowId, hasModifications);
    return true;
}

// 텍스트 필드 변경 처리 - 편집모드 개선
function handleTextChange(input) {
    var rowId = $(input).closest('tr').attr('id');
    var hasModifications = hasOtherModifications(rowId);
    syncRowState(rowId, hasModifications);
}

// multiselect 체크박스 클릭 이벤트 처리 - 수정: 로직 개선
function handleMultiselectChange() {
    var grid = $('#gridList');
    var selectedRows = grid.jqGrid('getGridParam', 'selarrrow');
    var allRowIds = grid.jqGrid('getDataIDs');
    
    $.each(allRowIds, function(index, rowId) {
        var isSelected = selectedRows.indexOf(rowId) !== -1;
        var hasModifications = hasOtherModifications(rowId);
        
        // 수정된 행은 선택 상태 유지, 수정되지 않은 행은 선택 해제
        if (!hasModifications && isSelected) {
            setTimeout(function() {
                grid.jqGrid('setSelection', rowId, false);
            }, 10);
        } else if (hasModifications && !isSelected) {
            setTimeout(function() {
                grid.jqGrid('setSelection', rowId, true);
            }, 10);
        }
    });
}

$(function(){
    getGridList();
});

// 수정된 행 데이터 가져오기 - 수정: 선택된 행이 아닌 실제 수정된 행 기준
function getModifiedRows() {
    var grid = $('#gridList');
    var modifiedRows = [];
    
    // modifiedRowsSet에 있는 모든 행을 확인
    modifiedRowsSet.forEach(function(rowId) {
        if (hasOtherModifications(rowId)) {
            var isEditMode = $('#' + rowId).hasClass('jqgrow-edit');
            if (isEditMode) {
                grid.jqGrid('saveRow', rowId);
            }
            var rowData = getCleanRowData(rowId);
            if (rowData) {
                modifiedRows.push(rowData);
            }
        }
    });
    
    return modifiedRows;
}

// 편집모드를 고려한 깨끗한 행 데이터 가져오기
function getCleanRowData(rowId) {
    var grid = $('#gridList');
    var rowData = grid.jqGrid('getRowData', rowId);
    var cleanData = {};
    
    $.each(rowData, function(key, value) {
        if (typeof value === 'string') {
            if (value.indexOf('<input') !== -1) {
                var $temp = $('<div>').html(value);
                var inputValue = $temp.find('input').val();
                cleanData[key] = inputValue || '';
            } else if (value.indexOf('<') !== -1) {
                var $temp = $('<div>').html(value);
                cleanData[key] = $temp.text().trim();
            } else {
                cleanData[key] = value;
            }
        } else {
            cleanData[key] = value;
        }
    });
    
    return cleanData;
}

var lastSelection;
var clickedCell = null; // 클릭된 셀 정보 저장

// 수정: 포커스 문제 해결을 위한 editRow 함수 개선
function editRow(id, targetField){
    if (id && id !== lastSelection) {
        var grid = $('#gridList');
        grid.jqGrid('editRow', id, {
            keys: true, 
            focusField: targetField || 0, // 대상 필드가 있으면 해당 필드에 포커스
            oneditfunc: function(rowId) {
                var row = $('#' + rowId);
                
                // 이벤트 핸들러 바인딩
                row.find('input[name="CUST_MAIN_EMAIL"]').on('blur', function() {
                    $(this).data('field', 'CUST_MAIN_EMAIL');
                    handleEmailChange(this);
                });
                
                row.find('input[name="SALESREP_EMAIL"]').on('blur', function() {
                    $(this).data('field', 'SALESREP_EMAIL');
                    handleEmailChange(this);
                });
                
                row.find('input[name="COMMENTS"]').on('blur', function() {
                    $(this).data('field', 'COMMENTS');
                    handleTextChange(this);
                });
                
                // 클릭된 셀이 있으면 해당 필드에 포커스
                if (clickedCell && clickedCell.rowId === rowId) {
                    setTimeout(function() {
                        var targetInput = row.find('input[name="' + clickedCell.colName + '"]');
                        if (targetInput.length > 0) {
                            targetInput.focus();
                        }
                        clickedCell = null; // 초기화
                    }, 50);
                }
            }
        });
        lastSelection = id;
    }
}

// 수정: dataSave 함수 개선 - 실제 수정된 데이터만 저장
function dataSave(obj) {
    $(obj).prop('disabled', true);
    var grid = $('#gridList');
    
    // 모든 편집 중인 행 저장
    var allRowIds = grid.jqGrid('getDataIDs');
    $.each(allRowIds, function(index, rowId) {
        var isEditMode = $('#' + rowId).hasClass('jqgrow-edit');
        if (isEditMode) {
            grid.jqGrid('saveRow', rowId);
        }
    });
    
    var modifiedRows = getModifiedRows();
    
    if (modifiedRows.length === 0) {
        alert('수정된 내용이 없습니다.');
        $(obj).prop('disabled', false);
        return false;
    }
    
    // 유효성 검사
    var validationFailed = false;
    $.each(modifiedRows, function(i, rowData) {
        if (rowData.CUST_MAIN_EMAIL && !validateEmail(rowData.CUST_MAIN_EMAIL)) {
            alert('거래처 담당자 이메일 형식이 올바르지 않습니다. (거래처코드: ' + rowData.CUST_CD + ')');
            validationFailed = true;
            return false;
        }
        if (rowData.SALESREP_EMAIL && !validateEmail(rowData.SALESREP_EMAIL)) {
            alert('영업 담당 이메일 형식이 올바르지 않습니다. (거래처코드: ' + rowData.CUST_CD + ')');
            validationFailed = true;
            return false;
        }
        if (rowData.CUST_SENDMAIL_YN === 'Y' && !rowData.CUST_MAIN_EMAIL) {
            alert('담당자 이메일 발송이 체크되어 있지만 담당자 이메일이 비어있습니다. (거래처코드: ' + rowData.CUST_CD + ')');
            validationFailed = true;
            return false;
        }
        if (rowData.SALESREP_SENDMAIL_YN === 'Y' && !rowData.SALESREP_EMAIL) {
            alert('영업 담당 이메일 발송이 체크되어 있지만 영업 담당 이메일이 비어있습니다. (거래처코드: ' + rowData.CUST_CD + ')');
            validationFailed = true;
            return false;
        }
    });
    
    if (validationFailed) {
        $(obj).prop('disabled', false);
        return false;
    }
    
    // 폼 데이터 구성
    var iFormObj = $('form[name="iForm"]');
    iFormObj.empty();
    
    $.each(modifiedRows, function(i, rowData) {
        iFormObj.append('<input type="hidden" name="custCd" value="' + rowData.CUST_CD + '" />');
        iFormObj.append('<input type="hidden" name="custMainEmail" value="' + (rowData.CUST_MAIN_EMAIL || '') + '" />');
        iFormObj.append('<input type="hidden" name="custSendmailYn" value="' + (rowData.CUST_SENDMAIL_YN || 'N') + '" />');
        iFormObj.append('<input type="hidden" name="salesrepEmail" value="' + (rowData.SALESREP_EMAIL || '') + '" />');
        iFormObj.append('<input type="hidden" name="salesrepSendmailYn" value="' + (rowData.SALESREP_SENDMAIL_YN || 'N') + '" />');
        iFormObj.append('<input type="hidden" name="comments" value="' + (rowData.COMMENTS || '') + '" />');
    });
    
    if (confirm('저장 하시겠습니까?')) {
        var iFormData = iFormObj.serialize();
        var url = '${url}/admin/system/updateOrderMailAlarmAjax.lime'; 
        
        $.ajax({
            async : false,
            data : iFormData,
            type : 'POST',
            url : url,
            success : function(data) {
                if (data.RES_CODE == '0000') {
                    alert(data.RES_MSG);
                    resetAllStates();
                    dataSearch();
                } else {
                    alert(data.RES_MSG);
                }
                $(obj).prop('disabled', false);
            },
            error : function(request,status,error){
                alert('Error');
                $(obj).prop('disabled', false);
            }
        });
    } else {
        $(obj).prop('disabled', false);
    }
}

// 모든 상태 초기화
function resetAllStates() {
    var grid = $('#gridList');
    var allRowIds = grid.jqGrid('getDataIDs');
    $.each(allRowIds, function(index, rowId) {
        $('#gridList #' + rowId).css('background-color', '');
    });
    grid.jqGrid('resetSelection');
    modifiedRowsSet.clear();
}

// ==================================================================================
// jqGrid 초기화 - onSelectRow 수정
// ==================================================================================
function getGridList(){
    var searchData = getSearchData();
    $('#gridList').jqGrid({
        url: "${url}/admin/customer/getOrderEmailAlarmAjax.lime",
        editurl: 'clientArray',
        datatype: "json",
        mtype: 'POST',
        postData: searchData,
        colModel: updateComModel,
        height: '360px',
        autowidth: false,
        multiselect: true,
        rowNum: 10,
        rowList: ['10', '30', '50', '100'],
        rownumbers: true,
        pagination: true,
        pager: "#pager",
        actions : true,
        pginput : true,
        jsonReader: {
            root: 'list',
            id: 'CUST_CD'
        },
        loadComplete: function(data) {
            originalDataMap = {};
            modifiedRowsSet.clear();
            if (data && data.list) {
                $.each(data.list, function(index, item) {
                    originalDataMap[item.CUST_CD] = $.extend(true, {}, item);
                });
            }
            $('#cb_gridList').off('click').on('click', function() {
                setTimeout(handleMultiselectChange, 50);
            });
        },
        // 수정: onSelectRow에서 클릭된 컬럼 정보 저장
        onSelectRow: function(rowId, status, e){
            // 체크박스 클릭인 경우 처리하지 않음
            if (e && $(e.target).is('input[type="checkbox"]') && $(e.target).closest('td').hasClass('cbox')) {
                setTimeout(function() {
                    var hasModifications = hasOtherModifications(rowId);
                    if (!hasModifications) {
                        $('#gridList').jqGrid('setSelection', rowId, false);
                    }
                }, 10);
                return;
            }
            
            // 클릭된 셀 정보 저장 (포커스 문제 해결)
            if (e && e.target) {
                var $cell = $(e.target).closest('td');
                var colIndex = $cell.index();
                var colModel = $('#gridList').jqGrid('getGridParam', 'colModel');
                
                // rn, cb 컬럼 제외하고 실제 데이터 컬럼 인덱스 계산
                var dataColIndex = colIndex - 2; // rn(0), cb(1) 제외
                if (dataColIndex >= 0 && dataColIndex < colModel.length) {
                    var targetCol = colModel[dataColIndex];
                    if (targetCol && (targetCol.name === 'COMMENTS' || 
                                   targetCol.name === 'CUST_MAIN_EMAIL' || 
                                   targetCol.name === 'SALESREP_EMAIL')) {
                        clickedCell = {
                            rowId: rowId,
                            colName: targetCol.name
                        };
                    }
                }
            }
            
            editRow(rowId);
        },
        // 나머지 설정들...
        sortable: {
            update: function(relativeColumnOrder) {
                var grid = $('#gridList');
                var defaultColIndicies = [];
                for (var i = 0; i < defaultColModel.length; i++) {
                    defaultColIndicies.push(defaultColModel[i].name);
                }
                globalColumnOrder = [];
                var columnOrder = [];
                var currentColModel = grid.getGridParam('colModel');
                for (var j = 0; j < relativeColumnOrder.length; j++) {
                    if ('rn' != currentColModel[j].name && 'cb' != currentColModel[j].name) {
                        columnOrder.push(defaultColIndicies.indexOf(currentColModel[j].name));
                    }
                }
                globalColumnOrder = columnOrder;
                setCookie(ckNameJqGrid, globalColumnOrder, 365);
                var tempUpdateColumnWidth = [];
                for (var j = 0; j < currentColModel.length; j++) {
                    if ('rn' != currentColModel[j].name && 'cb' != currentColModel[j].name) {
                        tempUpdateColumnWidth.push(currentColModel[j].width);
                    }
                }
                updateColumnWidth = tempUpdateColumnWidth;
                setCookie(ckNameJqGridWidth, updateColumnWidth, 365);
            }
        },
        resizeStop: function(width, index) {
            var minusIdx = 0;
            var grid = $('#gridList');
            var currentColModel = grid.getGridParam('colModel');
            if ('rn' == currentColModel.name || 'cb' == currentColModel.name) minusIdx--;
            if ('rn' == currentColModel.name || 'cb' == currentColModel.name) minusIdx--;
            var resizeIdx = index + minusIdx;
            updateColumnWidth[resizeIdx] = width;
            setCookie(ckNameJqGridWidth, updateColumnWidth, 365);
        },
        sortorder: 'desc'
    });
}

// 나머지 함수들은 동일...
function getSearchData(){
    var rl_custcd = $('input[name="searchCustCd"]').val();
    var rl_custnm = $('input[name="searchCustNm"]').val();
    var rl_salesrepnm = $('input[name="searchSalesrepNm"]').val();
    var searchData = {
        rl_custcd : rl_custcd,
        rl_custnm : rl_custnm,
        rl_salesrepnm : rl_salesrepnm
    };
    return searchData;
}

function dataSearch() {
    var searchData = getSearchData();
    $('#gridList').setGridParam({
        postData : searchData
    }).trigger("reloadGrid");
}

function excelDown(obj){
    $('#ajax_indicator').show().fadeIn('fast');
    var token = getFileToken('excel');
    $('form[name="frm"]').append('<input type="hidden" name="filetoken" value="'+token+'" />');
    formPostSubmit('frm', '${url}/admin/system/orderMailAlarmExcelDown.lime');
    $('form[name="frm"]').attr('action', '');
    $('input[name="filetoken"]').remove();
    var fileTimer = setInterval(function() {
        if('true' == getCookie(token)){
            $('#ajax_indicator').fadeOut();
            delCookie(token);
            clearInterval(fileTimer);
        }
    }, 1000 );
}
```

## 핵심 문제 원인 및 해결 방법

### **문제별 상세 분석**

**1. 무한 경고창 문제**
- **원인**: `blur` 이벤트 → 유효성 검사 실패 → `focus()` 호출 → 다시 `blur` 이벤트 발생
- **해결**: `emailValidationInProgress` 플래그로 재귀 호출 방지 및 `setTimeout` 사용

**2. 데이터 저장 불완전 문제**
- **원인**: `getModifiedRows()`에서 `selectedRows`에만 의존, 실제 수정된 데이터 누락
- **해결**: `modifiedRowsSet`을 기준으로 실제 수정된 행만 저장 처리

**3. 체크박스 동기화 오류**
- **원인**: 개별 체크박스 상태와 전체 행 선택 상태의 로직 혼재
- **해결**: 수정된 행은 자동 선택, 미수정 행은 자동 해제하는 명확한 규칙 적용

**4. 포커스 이동 오류**
- **원인**: `onSelectRow`에서 클릭된 컬럼 정보 없이 무조건 첫 번째 필드에 포커스
- **해결**: 클릭된 셀 정보를 저장하여 해당 필드에 정확히 포커스 이동

### **추가 권장사항**

**서버 사이드 검증 강화:**
```java
// Controller에서 추가 검증
if (custSendmailYn.equals("Y") && StringUtils.isEmpty(custMainEmail)) {
    return MsgCode.getResultMap(MsgCode.ERROR, "담당자 이메일 발송 체크 시 이메일 주소는 필수입니다.");
}
```

**데이터베이스 제약조건 추가:**
```sql
-- 이메일 형식 체크 제약조건
ALTER TABLE O_CUSTOMER_MAILINFO 
ADD CONSTRAINT CK_CUST_EMAIL_FORMAT 
CHECK (CUST_MAIN_EMAIL IS NULL OR CUST_MAIN_EMAIL LIKE '%_@_%.__%');
```

이렇게 수정하면 모든 문제가 해결되고 사용자 경험이 크게 향상될 거야.