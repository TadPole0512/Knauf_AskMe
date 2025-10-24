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

좋아. 아래는 **네가 준 증상 설명을 AI가 어떻게 해석해야 하는지**와, **정확한 답을 받기 위해 AI에게 어떤 질문과 자료를 순서대로 요청해야 하는지**만 정리한 거야. (※ 문제의 원인/해결책 “답변”은 하지 않음)

---

# AI의 이해(요약 프레임)

* 현상 범주: **jqGrid 편집/저장 흐름 + 체크박스/포커스/직렬화 + 백엔드 파라미터 매핑** 이슈의 복합 증상.
* 주요 의심 축:

  * 프런트: `formatter/unformat`, `getRowData/getLocalRow`, `saveRow`, `serialize`, 이벤트 중복/버블, 포커스 이동.
  * 검증 로직: 이메일 정규식 타이밍/트리거(blur/change/save), 모달 경고 반복 트리거.
  * 백엔드: 파라미터 네이밍(배열 vs 인덱스), 빈값 처리, DAO 반영 컬럼.
  * 동기/비동기 흐름: `async:false`, confirm, 재렌더링 부작용.
* 목표: 4가지 증상을 **재현 가능한 최소 사례**로 축소 → 원인 분리 → 수정 포인트 제시.

---

# 먼저 필요한 자료(제출 요청 질문)

AI가 정확히 진단하려면 아래 자료를 **먼저** 달라고 요청해. 각 항목은 “질문 형태”로 되어 있어.

1. **그리드/화면 구성**

* jqGrid 초기화 전체 코드와 `colModel` 전부, 특히 `editable`, `edittype`, `formatter`, `unformat`, `editoptions`, `cellsubmit`, `cellEdit` 설정 보여주세요.
* `getModifiedRows`, `hasOtherModifications`, `syncRowState`, `resetAllStates`, `dataSearch`, `validateEmail`, `originalDataMap` 함수 원본 코드를 보여주세요.
* 저장 폼 HTML(`<form name="iForm">`) 구조와 동일 페이지 내 다른 폼 유무, 같은 name을 가진 input 존재 여부를 보여주세요.

2. **이벤트/검증 흐름**

* 이메일 유효성 검사는 어느 타이밍(blur/change/save 전/후)에서 호출되는지? 관련 이벤트 바인딩 코드와 위임 여부(delegate/on) 코드를 보여주세요.
* 체크박스 클릭/변경과 저장 사이에서 호출되는 모든 핸들러(직접/간접) 코드를 보여주세요.

3. **백엔드 수신/저장**

* 컨트롤러(또는 핸들러) 시그니처, 서비스(`insertUpdateOrderEmailAlarm`) 파라미터 수신부, DAO Mapper(SQL 포함)에서 **어떤 컬럼이 업데이트되는지** 전체 SQL을 보여주세요.
* 실제 서버에서 수신된 **요청 바디 예시(네트워크 탭 캡처)** 1\~2건을 보여주세요. (개인정보는 마스킹)
* DB 테이블 스키마(관련 컬럼: 이메일, 비고, 발송여부)와 PK/UK/Default/Trigger 정보, Null/Length 제약을 보여주세요.

4. **재현 조건/환경**

* STS/Tomcat/jQuery/jqGrid/Java 버전, 사용 중인 MyBatis 버전 알려주세요.
* “문제 재현 단계”를 1,2,3… 순서로 텍스트로 적어주세요. (예: 어떤 셀 클릭→수정→어느 버튼 클릭)

5. **로그/콘솔**

* 브라우저 콘솔 로그, 네트워크 탭 Request/Response 샘플(성공/실패 각 1개).
* 서버 로그(컨트롤러 진입 로그에 파라미터 map dump 있으면 첨부).

---

# 1차 공통 질문(환경/흐름 파악용)

* 현재 그리드는 **Cell Editing**인가 **Inline Row Editing**인가요? `cellEdit`/`inlineNav` 설정을 알려주세요.
* `saveRow` 호출 후 그리드가 **리렌더링**(포맷터 재적용)되나요? 된다면 어느 시점에 `getRowData`/`serialize`가 실행되나요?
* 체크박스 컬럼의 내부 데이터 표준은 \*\*'Y'/'N'\*\*인가요, 아니면 DOM `checked` 상태를 직접 읽나요?
* 이메일/비고 컬럼은 `editable:true`로 편집 중일 때, **save 전에 값 동기화**를 어떤 방식으로 하는지요? (예: `saveRow` 반복 호출 or `beforeSaveCell/afterSaveCell`)

---

# 증상별 정밀 질문

## (1) 이메일 형식 경고창이 계속 반복 노출

* 이메일 검증은 **어떤 이벤트**에 묶여 있나요? (blur/change/keyUp/save 전/후) 해당 이벤트 바인딩 코드를 보여주세요.
* 경고창 노출 후 **포커스 이동/값 롤백/재검증** 로직이 있나요? 있으면 코드와 흐름을 순서대로 설명해주세요.
* `alert` 직후 동일 이벤트가 **다시 트리거**되는 경로(onchange → rerender → onchange 등)가 있는지 콜스택/로그로 확인한 내역이 있나요?
* 같은 입력 필드에 **중복 이벤트 바인딩**(동일 selector, 여러 `on` 호출) 가능성 점검 로그를 제공해 주세요.

## (2) 비고/이메일이 저장되지 않고 발송여부만 'Y'로 저장

* 저장 직전 `modifiedRows`에 담긴 **각 컬럼 값 스냅샷**(console.log)과, `iFormObj.serialize()` 결과 문자열을 첨부해 주세요.
* 백엔드에서 실제로 수신한 `params` 맵 dump와, DAO로 전달되는 `svcMap` 값(특히 이메일/비고)이 무엇인지 로그를 보여주세요.
* DAO Mapper SQL에서 **UPDATE SET 절**에 비고/이메일 컬럼이 포함되어 있나요? 조건절에 의해 갱신이 스킵될 여지는 없나요? SQL 전문을 보여주세요.
* 같은 name을 가진 hidden input이 **중복 작성**되거나 비고/이메일 name이 누락되는 케이스가 있는지 DOM 캡처로 보여주세요.

## (3) 발송 여부 하나만 체크해도 두 곳이 모두 'Y'로 저장

* 체크박스 두 컬럼 각각의 `name`/`data-field`/`colModel.name`이 구분되어 있나요? 렌더된 DOM에서 두 체크박스를 구분하는 selector 캡처를 보여주세요.
* `getModifiedRows`가 체크박스 값을 가져올 때 \*\*DOM의 `value`("on")\*\*를 쓰는지, \*\*`checked`\*\*를 쓰는지, 또는 내부 데이터(`Y/N`)를 쓰는지 코드로 보여주세요.
* 저장 폼 구성 시 `custSendmailYn`과 `salesrepSendmailYn`에 들어가는 값이 어떤 경로로 만들어지는지(계산/대입 부분) 로그를 붙여 주세요.

## (4) 새 행에서 비고/영업 이메일을 클릭하면 포커스가 담당자 이메일로 이동

* jqGrid 편집 모드 진입 시 **첫 포커스 대상**을 지정하는 옵션/커스텀 코드가 있나요? 있으면 코드와 실제 동작 순서를 알려주세요.
* 각 셀의 `editable` 입력이 생성되는 시점에 **autofocus/scrollIntoView** 또는 강제 `focus()` 호출이 있나요? 해당 코드/플러그인 존재 여부를 알려주세요.
* `handleCheckboxClick` 또는 다른 핸들러에서 **`setCell`/`saveRow` 호출**로 인해 재렌더링 되며 포커스가 초기화되는 경로가 있나요? 관련 콜스택/로그를 첨부해 주세요.

---

# 진단/수정 제안 생성 방식(요청)

* 위 자료를 검토한 뒤, AI가 아래 순서로 답변해 달라고 요청하세요:

  1. **원인 가설 리스트업**(증상별 2\~3개씩) → 2) **자료 대조로 가설 검증** →
  2. **최소 수정안**(프런트/백 각각) → 4) **리스크/부작용** → 5) **테스트 시나리오**.
* 답변 시 \*\*라인 단위 지목(파일/함수/라인/selector)\*\*과 전/후 비교 패치(diff) 형식으로 제시해 달라고 요청하세요.
* 체크박스/입력값은 **'Y'/'N' 표준화**와 **unformat 적용** 여부를 구분해서 설명해 달라고 요청하세요.
* 가능하면 **재현 가능한 최소 예제(MRE)** 코드를 생성해 달라고 요청하세요.

---

# 단계별 질문 시나리오(순차 진행용)

## 단계 1: 아키텍처/흐름 파악

* “현재 편집/저장 흐름을 순서도로 적어달라(이벤트 트리거 → 값 동기화 → 직렬화 → Ajax → DAO). 필요 시 의심 지점에 로깅 포인트 제안까지 포함해 달라.”

## 단계 2: 자료 제출 & 점검

* 위 “필요 자료”를 업로드한 뒤:

  * “첨부 코드 기준으로, 각 증상과 매칭되는 **문제 가능성 상위 3개**를 먼저 뽑아 달라. 해당하는 코드 위치를 **정확한 식별자**(파일/함수/라인/셀렉터)로 찍어 달라.”

## 단계 3: 증상별 원인 후보 축소

* (1)\~(4) 각각에 대해:

  * “이벤트 중복/재렌더링/네이밍/직렬화/SQL 미포함 중, 어떤 축이 1순위인지와 근거 로그를 제시해 달라.”

## 단계 4: 수정안 제시

* “프런트 전용 수정안 / 백엔드 전용 수정안 / 양쪽 혼합안 **3가지**를 제시하고, 각 안마다 **패치 코드**와 **테스트 케이스**를 함께 달라.”

## 단계 5: 검증/회귀 테스트

* “수정 후, 4가지 증상이 **재발하지 않는지** 확인할 **자동화 수준의 수동 테스트 체크리스트**를 만들어 달라(입력 방식/순서/엣지 케이스 포함).”

---

# 마지막 요청(형식 지정)

* “답변은 코드/로그/SQL을 **전체** 보여주는 형태로, 일부 생략 없이 주세요.”
* “가능하면 **Diff 패치**(Before/After)와 **네트워크 페이로드 예시**도 포함해 주세요.”
* “설명은 ‘왜’(원인)→‘어디’(코드 위치)→‘어떻게’(패치)→‘무엇으로 검증’(테스트)의 순으로 써 주세요.”
* “GI/MDA 등 사고 공식을 적용해 **가설→검증→수정→검증** 흐름으로 답변해 주세요.”

---

이 질문 템플릿대로 진행하면, AI가 불필요한 추측 없이 **정확한 라인 단위 진단과 실행 가능한 패치**를 내놓을 가능성이 가장 높아져.


---------------------------------------------------------------------
/* ******************************************************************************************************************************* */
/* ******************************************************************************************************************************* */
/* ******************************************************************************************************************************* */
---------------------------------------------------------------------

아래 "[필요한 자료 요청]"을 참고 해서 "[단계별 질문 구성]"의 질문에 대해 대답해줘.


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

---------------------------------------------------------------------

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

좋아. 아래는 네가 지금 바로 복붙해서 AI에게 보낼 **질문 전용 프롬프트**야.
(주의: 원인/해결 “답변”은 요구하지 않고, 오직 **질문과 요청 사항**만 포함돼 있어.)

---

# 1단계: 컨텍스트 제공(질문)

1. “담당자 이메일 관리 기능에서 발생하는 문제를 분석해줘.
   이 화면은 **주문 메일 알람 관리** 기능을 제공하고, 기술 스택은 **Java(8, Zulu), Spring MVC, MyBatis, Tomcat 9.0.99, MSSQL 2019, jQuery, jqGrid**야.”

2. “아래 **코드 첨부 묶음**을 ‘있는 그대로’ 사용해서 분석해줘.

* HTML/JSP (화면/그리드/버튼/폼)
* JavaScript (그리드 셋업, 포맷터, 편집/저장, 유효성검사, 직렬화)
* Controller/Service/DAO (파라미터 수신·배열 처리·MAP 바인딩)
* MyBatis XML (MERGE 쿼리)
* DB 스키마 (O\_CUSTOMER\_MAILINFO)
  분석·재현·패치 제안은 전부 **이 첨부 기준**으로 진행해.”

---

# 2단계: 구체적 문제 상황 설명(질문)

“다음 4가지 문제가 발생하고 있어. **각 항목별로 재현 경로/원인 가설/필요 로그**를 함께 정리해줘.”

1. **이메일 유효성 검사 경고창 무한 반복**

   * 조건: 담당자 이메일 선택 → 잘못된 형식 입력 → 저장 시도
   * 현상: 경고창을 닫아도 즉시 다시 표시됨

2. **데이터 저장 불완전**

   * 조건: 비고 입력 + 저장 클릭
   * 현상: 비고/이메일은 미저장, 발송여부만 ‘Y’로 저장됨

3. **체크박스 상태 동기화 오류**

   * 조건: 발송여부 체크박스 하나만 체크 후 저장
   * 현상: 두 발송여부 모두 ‘Y’ 저장

4. **포커스 이동 오류**

   * 조건: 새 행에서 비고/영업담당 이메일 클릭
   * 현상: 포커스가 담당자 이메일로 튐 → 잘못된 필드에 입력됨

---

# 3단계: 코드 제공 및 분석 요청(질문)

아래 **요청 순서와 산출물 포맷**을 그대로 따라줘.

## A. 사전 확인 & 추가 자료 요청(먼저 답해줘)

* 현재 사용하는 **jQuery/ jqGrid 버전**을 알려줘.
* 브라우저 **네트워크 탭의 실제 요청 페이로드**(성공/문제 1건씩, 민감정보 마스킹)와 **서버에서 수신한 파라미터 Map dump 로그**(가능하면 Controller 진입부) 제공이 필요해? 필요하면 **어떤 키/값**을 찍을지 구체적으로 지정해 줘.
* JavaScript에서 사용하는 `getModifiedRows`, `validateEmail`, `hasOtherModifications`, `syncRowState`, `getCleanRowData`가 **어느 타이밍**(blur/change/saveRow/reloadGrid)에서 호출되는지 **시퀀스 다이어그램**으로 정리해 줄래? 부족한 이벤트 로깅 포인트가 있다면 **추가 로그 위치/메시지**를 제안해줘.
* jqGrid가 **cellEdit vs inline edit** 중 무엇을 사용하고 있는지 첨부 코드로 판단해서 설명하고, 만약 혼용 흔적이 있으면 **의심 지점 목록**을 먼저 적어줘.

## B. 원인 분석(각 문제별로)

“첨부 코드를 기준으로, 각 문제(1\~4)에 대해 **상위 원인 가설 3개**를 우선순위로 제시하고, 각 가설마다:

* (가) 근거가 되는 **코드 라인/함수/셀렉터**
* (나) **재현 확인용 로그 문구**(console/서버)
* (다) 한 줄 요약(왜 이런 현상이 발생하는지)
  를 표로 정리해줘.”

## C. 수정안 제시(3안 병렬)

“각 문제(1\~4)에 대해 다음 **세 가지 레벨의 수정안**을 각각 내줘.

* **프런트 전용 최소 수정안**: 포맷터/언포맷터, `getLocalRow` vs `getRowData`, `checked`/`Y·N` 표준화, 이벤트 바인딩/버블, `saveRow`/`setCell` 타이밍 등
* **백엔드 전용 최소 수정안**: 파라미터 배열 수신 검증, 화이트리스트(Y/N), Null 기본값, MERGE SET 컬럼, 트랜잭션/에러 핸들링
* **프런트+백 혼합안**: 네이밍 규약(배열 vs 인덱스), 직렬화 포맷, 유효성검사 타이밍 통일

각 수정안은 **Before/After diff** 형식으로 파일·함수·라인을 특정해줘. (코드 전체 생략 금지)”

## D. 최종 패치 코드(전체)

“문제(1\~4)를 모두 해결하는 **통합 패치**를 제시해줘.

* HTML/JSP: 필요한 경우 속성/폼/테이블 변경
* JS: 포맷터/언포맷터, 이벤트, 저장 로직, 직렬화, 검증, 포커스 제어
* Controller/Service/DAO/Mapper: 파라미터 검증/배열 매핑/SQL SET/NULL 처리
  **완성본 전체 코드**를 파일 단위로 제공하고, 각 파일 상단에 `// PATCH v1` 주석을 달아줘.”

## E. 회귀 테스트 시나리오

“수정 후 재발 방지를 위해 **체크리스트**를 만들어줘.

* 정상/에러 입력 조합(빈값/잘못된 이메일/한글/특수문자)
* 체크박스 하나만/둘 다/둘 다 해제
* 편집모드 전환/다중 선택/그리드 리로드/정렬/리사이즈
* 저장 성공/실패(서버 4xx/5xx)
  각 케이스별 **예상 결과**와 **필수 관찰 로그**를 함께 적어줘.”

## F. 산출물 형식 고정

“모든 답변은

1. **왜**(원인) → 2) **어디**(코드 위치) → 3) **어떻게**(패치) → 4) **무엇으로 검증**(테스트)
   순으로 작성해줘.
   또한 **네트워크 페이로드 예시**(key=value…)와 **SQL 최종 SET 값** 예시도 포함해줘.”

---

# 4단계: 부족 자료가 있을 경우의 추가 요청(질문)

“분석에 앞서 아래 자료가 더 필요하면 목록으로 알려줘.

* (a) 브라우저 콘솔/네트워크 캡처(요청/응답 전체) 2건 이상
* (b) Controller 진입 파라미터 로그(Map dump) 2건 이상
* (c) 서버 에러 로그 스택트레이스(있다면)
* (d) jqGrid 실제 **렌더된 셀 HTML**(체크박스 두 컬럼) 스냅샷
* (e) `getModifiedRows()` 호출 직전/직후의 **rowData 스냅샷 로그**”

---

# 5단계: 답변 품질 가이드(질문)

“가능하면 \*\*재현 가능한 최소 예제(MRE)\*\*도 함께 만들어줘.
그리고 GI/MDA 등 사고 공식을 적용해 **가설→검증→수정→검증** 흐름으로 서술해줘.”

---

이 프롬프트는 **질문만** 담았고, 원인/해결 자체는 요구하지 않아.
그대로 붙여넣으면, AI가 필요한 추가자료부터 짚고, 증상별 가설/증빙/패치/검증 순서로 **정확한 대답**을 내줄 거야.


---------------------------------------------------------------------
/* ******************************************************************************************************************************* */
/* ******************************************************************************************************************************* */
/* ******************************************************************************************************************************* */
/* ******************************************************************************************************************************* */
/* ******************************************************************************************************************************* */
---------------------------------------------------------------------


[1단계: 컨텍스트 제공(질문)]

1. “담당자 이메일 관리 기능에서 발생하는 문제를 분석해줘.
이 화면은 주문 메일 알람 관리 기능을 제공하고, 기술 스택은 Java(8, Zulu), Spring MVC, MyBatis, Tomcat 9.0.99, MSSQL 2019, jQuery, jqGrid야.”

2. “아래 코드 첨부 묶음을 ‘있는 그대로’ 사용해서 분석해줘.

    - HTML/JSP (화면/그리드/버튼/폼)
    - JavaScript (그리드 셋업, 포맷터, 편집/저장, 유효성검사, 직렬화)
    - Controller/Service/DAO (파라미터 수신·배열 처리·MAP 바인딩)
    - MyBatis XML (MERGE 쿼리)
    - DB 스키마 (O\_CUSTOMER\_MAILINFO)
분석·재현·패치 제안은 전부 이 첨부 기준으로 진행해.”



[2단계: 구체적 문제 상황 설명(질문)]

“다음 4가지 문제가 발생하고 있어. 각 항목별로 재현 경로/원인 가설/필요 로그를 함께 정리해줘.”

1. 이메일 유효성 검사 경고창 무한 반복

    - 조건: 담당자 이메일 선택 → 잘못된 형식 입력 → 저장 시도
    - 현상: 경고창을 닫아도 즉시 다시 표시됨

2. 데이터 저장 불완전

    - 조건: 비고 입력 + 저장 클릭
    - 현상: 비고/이메일은 미저장, 발송여부만 ‘Y’로 저장됨

3. 체크박스 상태 동기화 오류

    - 조건: 발송여부 체크박스 하나만 체크 후 저장
    - 현상: 두 발송여부 모두 ‘Y’ 저장

4. 포커스 이동 오류

    - 조건: 새 행에서 비고/영업담당 이메일 클릭
    - 현상: 포커스가 담당자 이메일로 튐 → 잘못된 필드에 입력됨



[3단계: 코드 제공 및 분석 요청(질문)]

아래 요청 순서와 산출물 포맷을 그대로 따라줘.

A. 사전 확인 & 추가 자료 요청(먼저 답해줘)

    - 현재 사용하는 jQuery/ jqGrid 버전을 알려줘.
    - 브라우저 네트워크 탭의 실제 요청 페이로드(성공/문제 1건씩, 민감정보 마스킹)와 서버에서 수신한 파라미터 Map dump 로그(가능하면 Controller 진입부) 제공이 필요해? 필요하면 어떤 키/값을 찍을지 구체적으로 지정해 줘.
    - JavaScript에서 사용하는 `getModifiedRows`, `validateEmail`, `hasOtherModifications`, `syncRowState`, `getCleanRowData`가 어느 타이밍(blur/change/saveRow/reloadGrid)에서 호출되는지 시퀀스 다이어그램으로 정리해 줄래? 부족한 이벤트 로깅 포인트가 있다면 추가 로그 위치/메시지를 제안해줘.
    - jqGrid가 cellEdit vs inline edit 중 무엇을 사용하고 있는지 첨부 코드로 판단해서 설명하고, 만약 혼용 흔적이 있으면 의심 지점 목록을 먼저 적어줘.

B. 원인 분석(각 문제별로)

“첨부 코드를 기준으로, 각 문제(1\~4)에 대해 상위 원인 가설 3개를 우선순위로 제시하고, 각 가설마다:

    - (가) 근거가 되는 코드 라인/함수/셀렉터
    - (나) 재현 확인용 로그 문구(console/서버)
    - (다) 한 줄 요약(왜 이런 현상이 발생하는지)
를 표로 정리해줘.”

C. 수정안 제시(3안 병렬)

“각 문제(1\~4)에 대해 다음 세 가지 레벨의 수정안을 각각 내줘.

    - 프런트 전용 최소 수정안: 포맷터/언포맷터, `getLocalRow` vs `getRowData`, `checked`/`Y·N` 표준화, 이벤트 바인딩/버블, `saveRow`/`setCell` 타이밍 등
    - 백엔드 전용 최소 수정안: 파라미터 배열 수신 검증, 화이트리스트(Y/N), Null 기본값, MERGE SET 컬럼, 트랜잭션/에러 핸들링
    - 프런트+백 혼합안: 네이밍 규약(배열 vs 인덱스), 직렬화 포맷, 유효성검사 타이밍 통일

각 수정안은 Before/After diff 형식으로 파일·함수·라인을 특정해줘. (코드 전체 생략 금지)”

D. 최종 패치 코드(전체)

“문제(1\~4)를 모두 해결하는 통합 패치를 제시해줘.

    - HTML/JSP: 필요한 경우 속성/폼/테이블 변경
    - JS: 포맷터/언포맷터, 이벤트, 저장 로직, 직렬화, 검증, 포커스 제어
    - Controller/Service/DAO/Mapper: 파라미터 검증/배열 매핑/SQL SET/NULL 처리
완성본 전체 코드를 파일 단위로 제공하고, 각 파일 상단에 `// PATCH v1` 주석을 달아줘.”

E. 회귀 테스트 시나리오

“수정 후 재발 방지를 위해 체크리스트를 만들어줘.

    - 정상/에러 입력 조합(빈값/잘못된 이메일/한글/특수문자)
    - 체크박스 하나만/둘 다/둘 다 해제
    - 편집모드 전환/다중 선택/그리드 리로드/정렬/리사이즈
    - 저장 성공/실패(서버 4xx/5xx)
각 케이스별 예상 결과와 필수 관찰 로그를 함께 적어줘.”

F. 산출물 형식 고정

“모든 답변은

1. 왜(원인) → 2) 어디(코드 위치) → 3) 어떻게(패치) → 4) 무엇으로 검증(테스트)
순으로 작성해줘.
또한 네트워크 페이로드 예시(key=value…)와 SQL 최종 SET 값 예시도 포함해줘.”



[4단계: 부족 자료가 있을 경우의 추가 요청(질문)]

“분석에 앞서 아래 자료가 더 필요하면 목록으로 알려줘.

    - (a) 브라우저 콘솔/네트워크 캡처(요청/응답 전체) 2건 이상
    - (b) Controller 진입 파라미터 로그(Map dump) 2건 이상
    - (c) 서버 에러 로그 스택트레이스(있다면)
    - (d) jqGrid 실제 렌더된 셀 HTML(체크박스 두 컬럼) 스냅샷
    - (e) `getModifiedRows()` 호출 직전/직후의 rowData 스냅샷 로그”



[5단계: 답변 품질 가이드(질문)]

“가능하면 \*\*재현 가능한 최소 예제(MRE)\*\*도 함께 만들어줘.
그리고 GI/MDA 등 사고 공식을 적용해 가설→검증→수정→검증 흐름으로 서술해줘.”



이 프롬프트는 질문만 담았고, 원인/해결 자체는 요구하지 않아.
그대로 붙여넣으면, AI가 필요한 추가자료부터 짚고, 증상별 가설/증빙/패치/검증 순서로 정확한 대답을 내줄 거야.


---------------------------------------------------------------------



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
---------------------------------------------------------------------
---------------------------------------------------------------------
---------------------------------------------------------------------
---------------------------------------------------------------------

아래 답변은 당신이 올려준 “첨부 코드 그대로”를 기준으로 재현·분석·패치 제안을 정리한 것이야.
요구한 형식(A\~F)과 “왜→어디→어떻게→무엇으로 검증” 흐름을 그대로 지켰고, 마지막에 통합 패치 전체 코드(// PATCH v1 주석 포함)도 파일 단위로 넣었어.

---

# A. 사전 확인 & 추가 자료 요청(먼저)

## 1) jQuery / jqGrid 버전 요청

* **jQuery 버전**(예: 1.12.4 / 2.2.x / 3.x)
* **jqGrid 버전**(예: Trirand free 4.6/4.7, Guriddo, free-jqGrid 4.15+ 등)

  * 사유: `getRowData`, `unformat`, `inline edit` 동작과 체크박스 포맷터의 반환값/HTML 구조가 버전에 따라 다름.

## 2) 네트워크 탭 페이로드 & 서버 Map 덤프 요청(각 1건: 정상/문제)

**필요 항목(민감정보 마스킹해서):**

* **요청 URL**(메서드/경로), **status code**, **요청 본문(serialized)**
* **Controller 진입 직후 `params`/`req.getParameterMap()` dump**

  * **키**: `custCd[]`, `custMainEmail[]`, `custSendmailYn[]`, `salesrepEmail[]`, `salesrepSendmailYn[]`, `comments[]`
  * **값 예시**: `custCd=K001&custMainEmail=a@b.com&custSendmailYn=Y...`
  * 배열 여부 확인을 위해 **키 이름과 개수/인덱스 대응**이 중요

**로그 포맷(권장):**

```text
[REQ] insertUpdateOrderEmailAlarmAjax
payload= custCd=K001&custMainEmail=a@b.com&custSendmailYn=Y&salesrepEmail=&salesrepSendmailYn=N&comments=테스트...
map= {custCd=[K001], custMainEmail=[a@b.com], custSendmailYn=[Y], salesrepEmail=[], salesrepSendmailYn=[N], comments=[테스트]}
```

## 3) 클라이언트 함수 호출 타이밍 시퀀스(요청) & 로깅 포인트 제안

**현재 코드 기준 추정 시퀀스:**

```
사용자 편집 → onSelectRow → editRow
  └ oneditfunc 안에서 이메일/비고 input.blur → handleEmailChange/handleTextChange
저장 클릭 → dataSave
  ├ 편집중 행 saveRow
  ├ getModifiedRows
  │   └ hasOtherModifications → (편집모드/비편집모드) 비교
  ├ (루프) validateEmail 재검증
  └ serialize → Ajax POST
```

**추가 로깅 권장(문구 예시):**

* `handleEmailChange`: `console.debug('[emailChange]', rowId, fieldName, newValue)`
* `handleCheckboxClick`: `console.debug('[checkbox]', rowId, fieldName, newValue)`
* `getModifiedRows` 진입/반환 길이
* `hasOtherModifications` 비교 결과: `console.debug('[dirtyCheck]', rowId, diffs)`
* `getCleanRowData` 결과 스냅샷(특히 두 체크박스 컬럼 값)

## 4) cellEdit vs inline edit 판정 & 혼용 의심 지점

* **판정**: 현재 **inline edit(editRow/saveRow)** 사용. (`cellEdit: true` 설정 없음, `editurl:'clientArray'`, `editRow()` 호출로 확인)
* **혼용 의심 포인트**

  1. `getRowData`로 포맷터가 그린 HTML을 그대로 가져온 뒤 수동 파싱 → **unformat 미정의**
  2. 체크박스 값을 `setCell`로 ‘Y/N’ 텍스트로 넣으면서 셀 내부는 `<input type="checkbox"...>` → **표준화 불일치**
  3. multiselect(헤더 cb)와 행 수정 플래그를 **상호간 동기화**하려다 보니 선택/수정 기준이 섞임

---

# B. 원인 분석(문제별) — 가설 Top3 (우선순위)

## 1) “이메일 유효성 경고 무한 반복”

| 우선 | 가설                                 | (가) 근거 코드                                                       | (나) 확인용 로그                                                           | (다) 한 줄 요약                           |
| -- | ---------------------------------- | --------------------------------------------------------------- | -------------------------------------------------------------------- | ------------------------------------ |
| 1  | **blur 핸들러에서 `alert→focus()` 루프**  | `handleEmailChange`에서 invalid 시 `alert` 후 `$(input).focus()`    | `[emailChange][invalid] row=..., field=..., val=...` 직후 blur 재트리거 여부 | blur에서 focus를 다시 걸어 **포커스-블러 루프** 발생 |
| 2  | **저장 시 2중 검증**(blur + dataSave 루프) | `dataSave` 내 다시 `validateEmail` 검사                              | `[save][validate] row=..., field=..., val=...`                       | blur/저장 모두에서 alert → 사용자는 닫아도 다시 뜸   |
| 3  | **정규식/트림/빈값 허용 로직 미스매치**           | `validateEmail`에서 빈문자열 true, 그러나 저장 루프에서 빈값 + Y 체크 케이스로 또 alert | `CUST_SENDMAIL_YN=Y && CUST_MAIN_EMAIL=''` 경로                        | 서로 다른 조건이 교차되며 사용자 체감은 “무한”으로 보임     |

## 2) “데이터 저장 불완전(비고/이메일 미저장, 발송여부만 Y)”

| 우선 | 가설                                    | (가) 근거 코드                                                                                          | (나) 확인용 로그                                       | (다) 요약                             |
| -- | ------------------------------------- | -------------------------------------------------------------------------------------------------- | ------------------------------------------------ | ---------------------------------- |
| 1  | **수정행 수집이 ‘선택행(selarrrow)’ 의존**       | `getModifiedRows()`가 `selectedRows`만 순회                                                            | `[modifiedRows] selectedRows=..., collected=...` | 댓글/이메일만 바꾼 행이 **선택 안돼 있으면 전송 제외**  |
| 2  | **getCleanRowData가 체크박스/포맷터 값 추출 실패** | 포맷터 HTML을 `.val()`로 읽음(checkbox는 기본적으로 value 미보유)                                                  | `[clean] rowData(raw)=..., clean=...`            | 실제로는 ‘Y/N’이 안 담기거나 공백으로 빠짐         |
| 3  | **AJAX URL 오타/불일치**                   | JS: `/admin/system/updateOrderMailAlarmAjax.lime` vs Controller: `insertUpdateOrderEmailAlarmAjax` | 네트워크 탭에서 요청 404/미도달 확인                           | 다른 엔드포인트 호출로 **비고/이메일 쪽 반영 안됨** 가능 |

## 3) “체크박스 동기화 오류(하나만 체크해도 둘 다 Y 저장)”

| 우선 | 가설                                          | (가) 근거 코드                                                        | (나) 확인용 로그                                                   | (다) 요약                     |
| -- | ------------------------------------------- | ---------------------------------------------------------------- | ------------------------------------------------------------ | -------------------------- |
| 1  | **두 체크박스 컬럼 값 구분 실패**                       | `getCleanRowData`가 `input`에서 올바른 값 취득 못함 → 기본/이전값 섞임             | `[clean][YN] CUST_SENDMAIL_YN=..., SALESREP_SENDMAIL_YN=...` | 두 컬럼 모두 동일 값으로 흘러감         |
| 2  | **setCell ‘Y/N’ vs 실제 DOM checkbox 상태 불일치** | `handleCheckboxClick`에서 `setCell(rowId, fieldName, 'Y'/'N')`만 수행 | `[checkbox] checked?, setCellVal?`                           | 내부 상태 비정합으로 서버 전송 시 동일 처리  |
| 3  | **서버 배열 인덱스 매칭 오류**                         | 같은 인덱스의 두 YN 값이 뒤섞여 들어갈 여지                                       | `[server] i, custCd[i], ... YN[i]`                           | 프런트 직렬화 순서가 틀리면 **짝이 어긋남** |

## 4) “포커스 이동 오류(새행에서 비고/영업담당 클릭해도 담당자 이메일로 튐)”

| 우선 | 가설                                      | (가) 근거 코드                                  | (나) 확인용 로그                          | (다) 요약                                           |
| -- | --------------------------------------- | ------------------------------------------ | ----------------------------------- | ------------------------------------------------ |
| 1  | **inline edit 진입 시 기본 포커스가 첫 editable** | `editRow(id, { focusField: true })`        | `[editRow] id=..., focusField=true` | 클릭한 셀 대신 **첫 editable(CUST\_MAIN\_EMAIL)** 로 포커스 |
| 2  | **onSelectRow에서 무조건 editRow 호출**        | `onSelectRow` 말미 `editRow(rowId)`          | `[onSelectRow] target=..., col=...` | 셀 단위 클릭 맥락을 무시                                   |
| 3  | **체크박스/멀티셀렉트 클릭 특별 처리와 충돌**             | `if (cbox) { setTimeout(...) return; }` 분기 | `[select] cbox branch?`             | 분기 로직이 포커스 관리와 꼬이면서 튐                            |

---

# C. 수정안 제시(문제별 3안: 프런트/백/혼합)

> **공통 방향(핵심)**
>
> 1. **값 표준화**: 체크박스는 **항상 내부 데이터(Y/N)** 를 단일 경로로 읽게 한다(**unformat** 또는 별도 data-\* 보관).
> 2. **수정행 수집 기준 변경**: **선택(selarrrow) 의존 제거**, **dirty 플래그** 기반 수집.
> 3. **포커스**: `onCellSelect`로 **클릭한 셀 기준 편집**, `focusField` 사용 금지.

아래 Diff는 대표 포인트만 발췌했어. (D 절에서 **전체 패치본** 제공)

---

## 문제 1) 경고 무한 반복

### (1) 프런트 전용 최소 수정안

**Before (발췌)**

```js
function handleEmailChange(input) {
  ...
  if (newValue && !validateEmail(newValue)) {
      alert('올바른 이메일 형식을 입력해주세요.');
      $(input).focus();
      return false;
  }
  ...
}
```

**After**

```diff
 function handleEmailChange(input) {
   var rowId = $(input).closest('tr').attr('id');
   var fieldName = $(input).attr('name') || $(input).data('field');
   var newValue = $(input).val().trim();

-  if (newValue && !validateEmail(newValue)) {
-      alert('올바른 이메일 형식을 입력해주세요.');
-      $(input).focus();
-      return false;
-  }
+  if (newValue && !validateEmail(newValue)) {
+      // blur 단계에서는 UI만 표시하고 포커스 루프 금지
+      $(input).addClass('invalid-input');
+      return false;
+  } else {
+      $(input).removeClass('invalid-input');
+  }
   ...
 }
```

그리고 **저장 시**에만 alert/포커스 이동:

```diff
 $.each(modifiedRows, function(i, rowData) {
   if (rowData.CUST_MAIN_EMAIL && !validateEmail(rowData.CUST_MAIN_EMAIL)) {
-    alert('...'); validationFailed = true; return false;
+    alert('...'); 
+    $('#gridList').jqGrid('setSelection', rowData.CUST_CD, true);
+    // 저장 단계에서만 포커스
+    $('#'+rowData.CUST_CD).find('input[name="CUST_MAIN_EMAIL"]').focus();
+    validationFailed = true; return false;
   }
   ...
 });
```

### (2) 백엔드 전용 최소 수정안

* **화이트리스트 검증**: `CUST_SENDMAIL_YN`, `SALESREP_SENDMAIL_YN` ∈ {Y,N} 외 값 오면 **N**으로 강제
* **널/빈 이메일 허용하되 Y 체크 시 거부**(400/메시지)

```java
String yn = toYN(req.getParameterValues("custSendmailYn")[i]); // toYN -> Y/N만 허용
if ("Y".equals(yn) && isBlank(email)) throw new BizException("담당자 이메일 필수");
```

### (3) 혼합안

* **blur는 표시만**, **저장은 서버까지 포함한 단일 검증 포인트**
* 서버에서 **검증 실패 시 필드/행 식별자**를 응답 → 프런트가 해당 셀 포커스

---

## 문제 2) 저장 불완전

### (1) 프런트 전용 최소 수정안

* **수정행 수집 기준 변경**: `modifiedRowsSet` 기반으로 **모든 dirty 행**을 수집 (selarrrow 미의존)
* **unformat 제공** 또는 **체크박스 값 별도 hidden/data 보관** 후 `getRowData` 의존 제거

**Before (발췌)**

```js
function getModifiedRows() {
  var selectedRows = grid.jqGrid('getGridParam', 'selarrrow');
  $.each(selectedRows, function(_, rowId) {
    if (hasOtherModifications(rowId)) { ... }
  });
}
```

**After**

```diff
 function getModifiedRows() {
   var grid = $('#gridList');
-  var selectedRows = grid.jqGrid('getGridParam', 'selarrrow');
   var modifiedRows = [];
-  $.each(selectedRows, function(_, rowId) {
+  var ids = grid.jqGrid('getDataIDs');
+  $.each(ids, function(_, rowId) {
     if (hasOtherModifications(rowId)) {
       var isEditMode = $('#' + rowId).hasClass('jqgrow-edit');
       if (isEditMode) grid.jqGrid('saveRow', rowId);
-      var rowData = getCleanRowData(rowId);
+      var rowData = getRowDataSafe(rowId); // ← unformat/checkbox 안전 추출
       if (rowData) modifiedRows.push(rowData);
     }
   });
   return modifiedRows;
 }
```

### (2) 백엔드 전용 최소 수정안

* **Controller URL 불일치 해결**(JS의 호출 URL을 실제 매핑과 일치)
* DAO에서 `selectList` → `update`로 수정 (MERGE는 DML)
* **트랜잭션** 설정, 성공/실패 건수 반환

### (3) 혼합안

* 프런트: dirty 기반 수집 + 안전 추출
* 백엔드: URL/DAO/DML 정정 + 에러 메시지 표준화(코드/메시지/행 식별자)

---

## 문제 3) 체크박스 동기화 오류

### (1) 프런트 전용 최소 수정안

* 체크박스 포맷터에 **unformat**을 추가해 **항상 ‘Y/N’로 왕복**
* `setCell` 대신 **행의 내부 데이터(`setRowData`)** 를 업데이트

**After(핵심 추가)**

```js
function checkboxUnformatter(cellVal, options, cell) {
  // 실제 checkbox checked 상태 → 'Y'/'N'
  var $cb = $(cell).find('input.mail-checkbox');
  return $cb.prop('checked') ? 'Y' : 'N';
}
```

그리고 colModel:

```diff
 {name:"CUST_SENDMAIL_YN", ..., formatter:checkboxFormatter, 
-  editable:false}
+  editable:false, unformat: checkboxUnformatter}
```

### (2) 백엔드 전용 최소 수정안

* 동일 인덱스끼리 매칭 보장(배열 길이 체크/로그)
* 값 누락 시 기본값 **'N'**

### (3) 혼합안

* 프런트는 unformat로 일관값 제공, 백엔드는 화이트리스트/기본값/길이검증

---

## 문제 4) 포커스 이동 오류

### (1) 프런트 전용 최소 수정안

* `onCellSelect(rowid, iCol, cellcontent, e)` 사용해 **클릭한 컬럼만 edit**
* `focusField: false` 또는 사용 안 함, 진입 후 해당 input에 포커스

```diff
 onCellSelect: function(rowId, iCol) {
-  editRow(rowId);
+  var cm = $(this).jqGrid('getGridParam','colModel')[iCol];
+  if (cm && cm.editable) {
+    $(this).jqGrid('editRow', rowId, {keys:true});
+    $('#'+rowId).find('input[name="'+cm.name+'"]').focus();
+  }
 }
```

### (2) 백엔드 전용 최소 수정안

* 해당 없음

### (3) 혼합안

* 프런트 포커스 로직 표준화 + 서버는 영향 없음

---

# D. 최종 패치 코드(전체) — **// PATCH v1**

> 아래는 **동작 핵심에 필요한 전량 코드**야. (JSP/JS, Controller/Service/DAO, Mapper)
> 기존 코드와 비교하면 **URL, unformat, dirty 수집, 포커스, DAO update** 등이 바뀌어.

---

## 1) HTML/JSP (화면/JS 포함) — **orderMailAlarm.jsp**

```jsp
<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
// PATCH v1
<%@ include file="/WEB-INF/views/include/admin/commonimport.jsp" %>
<!DOCTYPE html>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/admin/commonhead.jsp" %>
    <script type="text/javascript" src="${url}/include/js/common/select2/select2.js"></script>
    <link rel="stylesheet" href="${url}/include/js/common/select2/select2.css" />
    <style>
      .changed-row { background-color:#ffebcd !important; }
      .invalid-input { outline:2px solid #e57373 !important; background:#fdecea !important; }
    </style>

    <script type="text/javascript">
    // ==================================================================================
    // PATCH v1: 전역 상태
    // ==================================================================================
    var originalDataMap = {};
    var modifiedRowsSet = new Set();

    // 이메일 유효성
    function validateEmail(email){
      if(!email || email.trim()==='') return true;
      var re=/^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
      return re.test(email.trim());
    }

    // 행 상태 동기화(배경색/dirty)
    function syncRowState(rowId, dirty){
      var $tr = $('#gridList #'+rowId);
      if(dirty){
        $tr.addClass('changed-row');
        modifiedRowsSet.add(rowId);
      }else{
        $tr.removeClass('changed-row');
        modifiedRowsSet.delete(rowId);
      }
    }

    // 현재 행의 값 스냅샷(안전 버전)
    function getRowDataSafe(rowId){
      var grid = $('#gridList');
      var rd = grid.jqGrid('getLocalRow', rowId) || grid.jqGrid('getRowData', rowId);
      // 포맷터 포함 가능성 → DOM에서 보정
      var $tr = $('#'+rowId);
      function pick(name, fallback){
        // input 우선
        var $in = $tr.find('input[name="'+name+'"]');
        if($in.length){
          if($in.is(':checkbox')) return $in.prop('checked')?'Y':'N';
          return ($in.val()||'').trim();
        }
        // 체크박스 포맷터
        var $cb = $tr.find('.mail-checkbox[data-field="'+name+'"]');
        if($cb.length) return $cb.prop('checked')?'Y':'N';
        // 텍스트 셀
        return (rd && rd[name]!=null)? (''+rd[name]).trim() : (fallback||'');
      }
      return {
        CUST_CD: pick('CUST_CD', rowId),
        CUST_MAIN_EMAIL: pick('CUST_MAIN_EMAIL',''),
        CUST_SENDMAIL_YN: pick('CUST_SENDMAIL_YN','N'),
        SALESREP_EMAIL: pick('SALESREP_EMAIL',''),
        SALESREP_SENDMAIL_YN: pick('SALESREP_SENDMAIL_YN','N'),
        COMMENTS: pick('COMMENTS','')
      };
    }

    // 변경 여부 판단
    function hasOtherModifications(rowId){
      var cur = getRowDataSafe(rowId);
      var org = originalDataMap[rowId] || {};
      var fields = ['CUST_MAIN_EMAIL','CUST_SENDMAIL_YN','SALESREP_EMAIL','SALESREP_SENDMAIL_YN','COMMENTS'];
      for(var i=0;i<fields.length;i++){
        var f=fields[i], a=(''+(cur[f]||'')).trim(), b=(''+(org[f]||'')).trim();
        if(a!==b) return true;
      }
      return false;
    }

    // 체크박스 포맷터/언포맷터
    function checkboxFormatter(cellVal, options, rowObj){
      var checked=(cellVal==='Y')?'checked':'';
      var rowId=options.rowId, name=options.colModel.name;
      return '<input type="checkbox" class="mail-checkbox" '+checked+
        ' data-rowid="'+rowId+'" data-field="'+name+'" />';
    }
    function checkboxUnformatter(cellVal, options, cell){
      var $cb=$(cell).find('input.mail-checkbox');
      return $cb.prop('checked')?'Y':'N';
    }

    // 이벤트 위임: 체크박스 클릭
    $(document).on('click','#gridList input.mail-checkbox', function(){
      var rowId=$(this).data('rowid'), field=$(this).data('field');
      var yn = $(this).prop('checked')?'Y':'N';
      // 내부 데이터 반영: setRowData로 일관화
      var cur = getRowDataSafe(rowId);
      cur[field]=yn;
      $('#gridList').jqGrid('setRowData', rowId, cur);
      syncRowState(rowId, hasOtherModifications(rowId));
    });

    // 이메일/비고 blur(경고는 저장 시점에만)
    function handleEmailChange(input){
      var rowId=$(input).closest('tr').attr('id');
      var field=$(input).attr('name')||$(input).data('field');
      var val=($(input).val()||'').trim();
      if(val && !validateEmail(val)){ $(input).addClass('invalid-input'); return false; }
      $(input).removeClass('invalid-input');
      syncRowState(rowId, hasOtherModifications(rowId));
      return true;
    }
    function handleTextChange(input){
      var rowId=$(input).closest('tr').attr('id');
      syncRowState(rowId, hasOtherModifications(rowId));
    }

    // 수정행 수집: dirty 기반(선택 무의존)
    function getModifiedRows(){
      var ids = $('#gridList').jqGrid('getDataIDs');
      var out=[];
      for(var i=0;i<ids.length;i++){
        var id=ids[i];
        if(hasOtherModifications(id)){
          // 편집 중이면 저장
          if($('#'+id).hasClass('jqgrow-edit')){
            $('#gridList').jqGrid('saveRow', id, {aftersavefunc:function(){}});
          }
          out.push(getRowDataSafe(id));
        }
      }
      return out;
    }

    // 그리드
    var defaultColModel = [
      {name:"CUST_CD", key:true, label:'거래처코드', width:120, align:'center', sortable:true},
      {name:"CUST_NM", label:'거래처명', width:220, align:'left', sortable:true},
      {name:"CUST_MAIN_EMAIL", label:'담당자 이메일', width:220, align:'center', sortable:true, editable:true},
      {name:"CUST_SENDMAIL_YN", label:'발송 여부', width:100, align:'center', sortable:true,
        formatter:checkboxFormatter, unformat:checkboxUnformatter},
      {name:"SALESREP_NM", label:'영업 담당', width:100, align:'center', sortable:true},
      {name:"SALESREP_EMAIL", label:'영업 담당 이메일', width:300, align:'center', sortable:true, editable:true},
      {name:"SALESREP_SENDMAIL_YN", label:'발송 여부', width:100, align:'center', sortable:true,
        formatter:checkboxFormatter, unformat:checkboxUnformatter},
      {name:"COMMENTS", label:'비고', width:450, align:'left', sortable:true, editable:true}
    ];

    $(function(){ getGridList(); });

    function getGridList(){
      $('#gridList').jqGrid({
        url: "${url}/admin/customer/getOrderEmailAlarmAjax.lime",
        editurl: 'clientArray',
        datatype: "json",
        mtype: 'POST',
        postData: getSearchData(),
        colModel: defaultColModel,
        height: '360px',
        multiselect: true,
        rowNum: 10,
        rowList: ['10','30','50','100'],
        rownumbers: true,
        pager: "#pager",
        jsonReader: { root: 'list', id: 'CUST_CD' },
        loadComplete: function(data){
          originalDataMap={}; modifiedRowsSet.clear();
          if(data && data.list){
            $.each(data.list, function(_, item){
              originalDataMap[item.CUST_CD]=$.extend(true, {}, item);
            });
          }
        },
        onCellSelect: function(rowId, iCol, content, e){
          var cm = $(this).jqGrid('getGridParam','colModel')[iCol];
          if(cm && cm.editable){
            $(this).jqGrid('editRow', rowId, {
              keys:true,
              oneditfunc: function(rid){
                var $r=$('#'+rid);
                $r.find('input[name="CUST_MAIN_EMAIL"]').off('blur').on('blur', function(){ handleEmailChange(this); });
                $r.find('input[name="SALESREP_EMAIL"]').off('blur').on('blur', function(){ handleEmailChange(this); });
                $r.find('input[name="COMMENTS"]').off('blur').on('blur', function(){ handleTextChange(this); });
              }
            });
            // 클릭한 필드로 포커스
            setTimeout(function(){ $('#'+rowId).find('input[name="'+cm.name+'"]').focus(); },0);
          }
        }
      });
    }

    function getSearchData(){
      return {
        rl_custcd: $('input[name="searchCustCd"]').val(),
        rl_custnm: $('input[name="searchCustNm"]').val(),
        rl_salesrepnm: $('input[name="searchSalesrepNm"]').val()
      };
    }

    // 저장
    function dataSave(btn){
      $(btn).prop('disabled', true);

      // 편집 중인 행 저장
      var ids=$('#gridList').jqGrid('getDataIDs');
      ids.forEach(function(id){
        if($('#'+id).hasClass('jqgrow-edit')){
          $('#gridList').jqGrid('saveRow', id);
        }
      });

      var modifiedRows = getModifiedRows();
      if(modifiedRows.length===0){
        alert('수정된 내용이 없습니다.');
        $(btn).prop('disabled', false); return;
      }

      // 저장 시 검증(경고는 여기서만)
      for(var i=0;i<modifiedRows.length;i++){
        var r=modifiedRows[i];
        if(r.CUST_MAIN_EMAIL && !validateEmail(r.CUST_MAIN_EMAIL)){
          alert('거래처 담당자 이메일 형식이 올바르지 않습니다. (거래처코드: '+r.CUST_CD+')');
          $('#gridList').jqGrid('setSelection', r.CUST_CD, true);
          $('#'+r.CUST_CD).find('input[name="CUST_MAIN_EMAIL"]').focus();
          $(btn).prop('disabled', false); return;
        }
        if(r.SALESREP_EMAIL && !validateEmail(r.SALESREP_EMAIL)){
          alert('영업 담당 이메일 형식이 올바르지 않습니다. (거래처코드: '+r.CUST_CD+')');
          $('#gridList').jqGrid('setSelection', r.CUST_CD, true);
          $('#'+r.CUST_CD).find('input[name="SALESREP_EMAIL"]').focus();
          $(btn).prop('disabled', false); return;
        }
        if(r.CUST_SENDMAIL_YN==='Y' && !r.CUST_MAIN_EMAIL){
          alert('담당자 이메일 발송(Y)이나 이메일이 비어있습니다. (거래처코드: '+r.CUST_CD+')');
          $('#gridList').jqGrid('setSelection', r.CUST_CD, true);
          $('#'+r.CUST_CD).find('input[name="CUST_MAIN_EMAIL"]').focus();
          $(btn).prop('disabled', false); return;
        }
        if(r.SALESREP_SENDMAIL_YN==='Y' && !r.SALESREP_EMAIL){
          alert('영업 담당 이메일 발송(Y)이나 이메일이 비어있습니다. (거래처코드: '+r.CUST_CD+')');
          $('#gridList').jqGrid('setSelection', r.CUST_CD, true);
          $('#'+r.CUST_CD).find('input[name="SALESREP_EMAIL"]').focus();
          $(btn).prop('disabled', false); return;
        }
      }

      // 직렬화(배열 표준화)
      var $f = $('form[name="iForm"]').empty();
      modifiedRows.forEach(function(r){
        $f.append('<input type="hidden" name="custCd" value="'+r.CUST_CD+'">');
        $f.append('<input type="hidden" name="custMainEmail" value="'+(r.CUST_MAIN_EMAIL||'')+'">');
        $f.append('<input type="hidden" name="custSendmailYn" value="'+(r.CUST_SENDMAIL_YN||'N')+'">');
        $f.append('<input type="hidden" name="salesrepEmail" value="'+(r.SALESREP_EMAIL||'')+'">');
        $f.append('<input type="hidden" name="salesrepSendmailYn" value="'+(r.SALESREP_SENDMAIL_YN||'N')+'">');
        $f.append('<input type="hidden" name="comments" value="'+(r.COMMENTS||'')+'">');
      });

      if(!confirm('저장 하시겠습니까?')) { $(btn).prop('disabled', false); return; }

      var payload = $f.serialize();
      $.ajax({
        type: 'POST',
        // IMPORTANT: 기존 오타 수정
        url: '${url}/admin/system/insertUpdateOrderEmailAlarmAjax.lime',
        data: payload,
        success: function(data){
          if(data.RES_CODE==='0000'){
            alert(data.RES_MSG||'저장되었습니다.');
            modifiedRowsSet.clear();
            $('#gridList').trigger('reloadGrid');
          }else{
            alert(data.RES_MSG||'저장 실패');
          }
          $(btn).prop('disabled', false);
        },
        error: function(){
          alert('Error');
          $(btn).prop('disabled', false);
        }
      });
    }

    function dataSearch(){
      $('#gridList').setGridParam({ postData: getSearchData() }).trigger('reloadGrid');
    }

    // 엑셀 다운로드(원본 그대로)
    function excelDown(obj){ /* ... 생략(원본 유지) ... */ }
    </script>
</head>
<body class="page-header-fixed compact-menu">
  <%-- 원본 레이아웃/버튼 영역 그대로, 저장 버튼은 dataSave(this) 그대로 --%>
  <!-- ... (원본 HTML 폼/그리드 래퍼 동일) ... -->
</body>
</html>
```

---

## 2) Controller / Service / DAO

### Controller — **CustomerController.java**

```java
// PATCH v1
@ResponseBody
@PostMapping(value="insertUpdateOrderEmailAlarmAjax")
public Object insertUpdateOrderEmailAlarmAjax(
        @RequestParam Map<String,Object> params,
        HttpServletRequest req,
        HttpServletResponse res,
        Model model,
        LoginDto loginDto) throws Exception {

    return customerSvc.insertUpdateOrderEmailAlarm(params, req, loginDto);
}
```

### Service — **CustomerService.java**

```java
// PATCH v1
@Transactional
public Map<String,Object> insertUpdateOrderEmailAlarm(Map<String,Object> params, HttpServletRequest req, LoginDto loginDto) throws Exception {

    String[] custCdArray              = req.getParameterValues("custCd");
    String[] custMainEmailArray       = req.getParameterValues("custMainEmail");
    String[] custSendmailYnArray      = req.getParameterValues("custSendmailYn");
    String[] salesrepEmailArray       = req.getParameterValues("salesrepEmail");
    String[] salesrepSendmailYnArray  = req.getParameterValues("salesrepSendmailYn");
    String[] commentsArray            = req.getParameterValues("comments");

    if (custCdArray == null || custCdArray.length == 0) {
        return MsgCode.getResultMap(MsgCode.ERROR, "데이터가 없습니다.");
    }

    int total = custCdArray.length;
    int updated = 0;
    String uid = loginDto.getUserId();

    for (int i=0;i<total;i++){
        String custCd  = custCdArray[i];
        String mainE   = valAt(custMainEmailArray, i);
        String sendYN  = yn(valAt(custSendmailYnArray, i));
        String srepE   = valAt(salesrepEmailArray, i);
        String srepYN  = yn(valAt(salesrepSendmailYnArray, i));
        String memo    = valAt(commentsArray, i);

        // 서버 검증(선택)
        if ("Y".equals(sendYN) && isBlank(mainE))
            return MsgCode.getResultMap(MsgCode.ERROR, "담당자 이메일 발송(Y)이나 이메일 누락: "+custCd);
        if ("Y".equals(srepYN) && isBlank(srepE))
            return MsgCode.getResultMap(MsgCode.ERROR, "영업 담당 이메일 발송(Y)이나 이메일 누락: "+custCd);

        Map<String,Object> m = new HashMap<>();
        m.put("m_custCd", custCd);
        m.put("m_custMainEmail", mainE);
        m.put("m_custSendmailYn", sendYN);
        m.put("m_salesrepEmail", srepE);
        m.put("m_salesrepSendmailYn", srepYN);
        m.put("m_comments", memo);
        m.put("m_inid", uid);
        m.put("m_moid", uid);

        // DML 수행
        updated += customerDao.insertUpdateOrderEmailAlarm(m);
    }

    return MsgCode.getResultMap(MsgCode.SUCCESS, "저장 완료("+updated+"건)");
}

private static String valAt(String[] arr, int i){ return (arr!=null && i<arr.length)? (arr[i]==null?"":arr[i]) : ""; }
private static boolean isBlank(String s){ return s==null || s.trim().isEmpty(); }
private static String yn(String s){ return "Y".equalsIgnoreCase(s) ? "Y" : "N"; }
```

### DAO — **CustomerDao.java**

```java
// PATCH v1
public int insertUpdateOrderEmailAlarm(Map<String,Object> svcMap) {
    return sqlSession.update("eorder.o_customer.insertUpdateOrderEmailAlarm", svcMap);
}
```

---

## 3) MyBatis Mapper — **o\_customer.xml**

```xml
<!-- PATCH v1 -->
<update id="insertUpdateOrderEmailAlarm" parameterType="map">
  <![CDATA[
  MERGE dbo.O_CUSTOMER_MAILINFO AS T
  USING (
    SELECT
      #{m_custCd}             AS CUST_CD
    , #{m_custMainEmail}      AS CUST_MAIN_EMAIL
    , #{m_custSendmailYn}     AS CUST_SENDMAIL_YN
    , #{m_salesrepEmail}      AS SALESREP_EMAIL
    , #{m_salesrepSendmailYn} AS SALESREP_SENDMAIL_YN
    , #{m_comments}           AS COMMENTS
    , #{m_inid}               AS INID
    , GETDATE()               AS INDATE
    , #{m_moid}               AS MOID
    , GETDATE()               AS MODATE
  ) AS S
  ON T.CUST_CD = S.CUST_CD
  WHEN MATCHED THEN
    UPDATE SET
      T.CUST_MAIN_EMAIL      = S.CUST_MAIN_EMAIL
    , T.CUST_SENDMAIL_YN     = S.CUST_SENDMAIL_YN
    , T.SALESREP_EMAIL       = S.SALESREP_EMAIL
    , T.SALESREP_SENDMAIL_YN = S.SALESREP_SENDMAIL_YN
    , T.COMMENTS             = S.COMMENTS
    , T.MOID                 = S.MOID
    , T.MODATE               = S.MODATE
  WHEN NOT MATCHED THEN
    INSERT ( CUST_CD, CUST_MAIN_EMAIL, CUST_SENDMAIL_YN, SALESREP_EMAIL, SALESREP_SENDMAIL_YN, COMMENTS, INID, INDATE )
    VALUES ( S.CUST_CD, S.CUST_MAIN_EMAIL, S.CUST_SENDMAIL_YN, S.SALESREP_EMAIL, S.SALESREP_SENDMAIL_YN, S.COMMENTS, S.INID, S.INDATE );
  ]]>
</update>
```

---

# E. 회귀 테스트 시나리오(체크리스트)

| 케이스 | 입력/행동                 | 기대 결과                  | 필수 로그/관찰                               |
| --- | --------------------- | ---------------------- | -------------------------------------- |
| 1   | 올바른 이메일 입력 후 저장       | 성공, 경고 없음              | `[save] modifiedRows=n`, 서버 map 인덱스 정합 |
| 2   | 잘못된 이메일 입력 후 blur     | 셀 테두리 표시(빨강), alert 없음 | `[emailChange][invalid]`               |
| 3   | 잘못된 이메일 상태로 저장        | alert 1회, 해당 셀 포커스     | `[save][validate] email invalid`       |
| 4   | 담당자 Y, 이메일 공백         | 저장 시 alert, 포커스 이동     | `[save] CUST_SENDMAIL_YN=Y & email=''` |
| 5   | 영업담당만 Y               | 해당 필드만 Y 저장            | 서버 map 덤프(Y/N 값/인덱스)                   |
| 6   | 체크박스 하나만 Y            | DB에 정확히 하나만 Y          | SQL 결과 확인                              |
| 7   | 비고만 수정(행 선택 안 함)      | 저장됨(선택 무의존)            | `[modifiedRows] >0`                    |
| 8   | 편집 중 상태에서 바로 저장       | saveRow 후 값 반영         | `[saveRow] called`                     |
| 9   | 그리드 리로드/정렬/리사이즈 후 재수정 | 정상                     | 콘솔 오류 없음                               |
| 10  | 서버 500 강제             | 사용자에게 “저장 실패”          | status/응답 메시지                          |

---

# F. 네트워크 페이로드 & SQL SET 예시

## 1) 요청 페이로드 예시

```
custCd=K001&custMainEmail=a%40b.com&custSendmailYn=Y&salesrepEmail=&salesrepSendmailYn=N&comments=%EC%83%81%ED%83%9C
&custCd=K007&custMainEmail=&custSendmailYn=N&salesrepEmail=sales%40x.com&salesrepSendmailYn=Y&comments=
```

## 2) 서버 Map 덤프 예시

```text
custCd=[K001,K007]
custMainEmail=[a@b.com, ]
custSendmailYn=[Y,N]
salesrepEmail=[ , sales@x.com]
salesrepSendmailYn=[N,Y]
comments=[상태, ]
```

## 3) MERGE 최종 SET 반영 예시(K001 레코드)

```
SET
  CUST_MAIN_EMAIL      = 'a@b.com'
, CUST_SENDMAIL_YN     = 'Y'
, SALESREP_EMAIL       = NULL
, SALESREP_SENDMAIL_YN = 'N'
, COMMENTS             = N'상태'
, MOID                 = 'hsg'
, MODATE               = GETDATE()
```

---

## 추가 자료 요청(부족 시)

* (a) 브라우저 **콘솔/네트워크 캡처** 2건(정상/문제)
* (b) **Controller 파라미터 Map** 덤프 2건
* (c) 서버 **스택트레이스**(있다면)
* (d) 실제 렌더된 **체크박스 셀 HTML** 스냅샷 2컬럼
* (e) `getModifiedRows()` 호출 직전/직후 **rowData 스냅샷**

---

## MRE(재현 가능한 최소 예제) 가이드

* free-jqGrid 기준 1페이지 그리드, 컬럼 5개(CUST\_CD, CUST\_MAIN\_EMAIL, CUST\_SENDMAIL\_YN, SALESREP\_EMAIL, SALESREP\_SENDMAIL\_YN)
* 포맷터/언포맷터 + inline edit + dirty 수집 + 저장 시 검증(blur는 표시만)
* AJAX는 echo 서버로 보내고 payload 콘솔 출력

---

### 결론 요약(Why→Where→How→Test)

1. **왜**: blur+focus 루프/선택의존 수집/unformat 부재/포커스 기본값 충돌
2. **어디**: `handleEmailChange`, `getModifiedRows`, 체크박스 포맷터, `onSelectRow`/`editRow`, JS URL, DAO DML
3. **어떻게**: blur는 표시, 저장에서만 alert; dirty 기반 수집; **unformat 추가**; `onCellSelect`로 포커스; URL/DAO 수정; 서버 화이트리스트/기본값
4. **검증**: 상기 회귀 시나리오 10건 + 네트워크/서버 덤프 비교

필요하면 위 패치에서 **배열 키에 \[]를 명시**(예: `custCd[]`)하도록 확장도 가능해. 지금은 “같은 name 반복” 방식으로도 `getParameterValues`가 잘 받으니 그대로 뒀어.
