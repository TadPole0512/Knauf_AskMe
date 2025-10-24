**\[환경]**

* jqGrid 4.15, jQuery 3.6, IE11/Chrome 최신 지원
* Spring + JSP
* 외부 플러그인 추가 불가

---

**\[시나리오/구현 목표]**

1. 사용자가 'CUST\_MAIN\_EMAIL' 또는 'SALESREP\_EMAIL' 셀을 수정하면

   * 유효하지 않은 이메일일 경우 경고 및 재입력
   * 유효하면 해당 행 전체 배경색 변경
2. 'CUST\_SENDMAIL\_YN' 또는 'SALESREP\_SENDMAIL\_YN' 체크박스 클릭 시

   * 체크=‘Y’, 해제=‘N’ 값 저장
   * 값이 원래와 다르면 줄 배경색 변경
3. 값/체크박스를 원래대로 돌리면 배경색도 원복
4. 그리드 reload/page 이동해도 변경 표시 유지(가능하다면)
5. 불필요: 서버 반영/자동 저장/외부 플러그인

---

**\[관련 코드/필드]**

```javascript
var defaultColModel = [ //  ####### 설정 #######
{name:"CUST_CD", key:true, label:'코드', width:120, align:'center', sortable:true },
{name:"CUST_NM", label:'거래처명', width:220, align:'left', sortable:true },
{name:"CUST_MAIN_EMAIL", label:'담당자 이메일', width:220, align:'center', sortable:true, editable: true  },
{name:"CUST_SENDMAIL_YN", label:'이메일 발송', width:100, align:'center', sortable:true, editable: true, edittype: "checkbox", formatter: "checkbox", formatoptions: { disabled: false }, editoptions: { value: "Y:N", defaultValue: "Y" }, cellattr: function() { return 'class="small-checkbox"'} },
{name:"SALESREP_NM", label:'영업 담당', width:100, align:'center', sortable:true },
{name:"SALESREP_EMAIL", label:'영업 담당 이메일', width:300, align:'center', sortable:true, editable: true },
{name:"SALESREP_SENDMAIL_YN", label:'이메일 발송', width:100, align:'center', sortable:true, editable: true, edittype: "checkbox", formatter: "checkbox", formatoptions: { disabled: false }, editoptions: { value: "Y:N", defaultValue: "Y" }, cellattr: function() { return 'class="small-checkbox"'} },
{name:"COMMENTS", label:'비고', width:450, align:'left', sortable:true, editable: true },
];


function getGridList(){
// grid init
var searchData = getSearchData();
$('#gridList').jqGrid({
url: "${url}/admin/customer/getOrderEmailAlarmAjax.lime",
editurl: 'clientArray', //사용x
//editurl: './deliveryspotUpAjax.lime',
datatype: "json",
mtype: 'POST',
postData: searchData,
colModel: updateComModel,
cellEdit: true,
cellsubmit: 'clientArray',
height: '360px',
autowidth: false,
rowNum : 10,
rowList : ['10','30','50','100'],
rownumbers: true,
pagination: true,
pager: "#pager",
actions : true,
pginput : true,
resizable: true,
...
```

* afterSaveCell, beforeSelectRow, gridComplete 사용 가능
* 체크박스 값은 반드시 'Y'/'N'

---

**\[요구/조건/우선순위]**

* js/jqGrid만으로 구현, 외부 플러그인 불가
* 안 되는 건 “불가/모름”으로 명확히 알려주길 요청
* 실제 예제 코드와 설명 둘 다 필수!

---

**\[마지막 한마디]**

* 실무에서 바로 쓸 수 있는 코드+로직 예시로 부탁
* 부정확하거나 대충 때우는 답변은 사양

---


좋습니다, 성근님. 요청하신 시나리오를 **jqGrid + jQuery만으로** 충실하게 구현하는 예제를 아래와 같이 정리해드릴게요. 설명은 단계별로 나누고, **실제 코드와 함께** 제공합니다.

***

## ✅ 1단계: 이메일 유효성 검사 + 배경색 변경

### 📌 구현 포인트

*   `afterSaveCell` 이벤트에서 이메일 유효성 검사
*   유효하면 해당 행에 클래스 추가 (`row-modified`)
*   유효하지 않으면 alert 후 원래 값 복원

### ✅ 코드 예시

```js
<div><div class="scriptor-paragraph"><pre><code class="language-Javascript" preview="Code">function isValidEmail(email) {
    var regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return regex.test(email);
}

var originalValues = {}; // 원본 값 저장용

$("#gridList").jqGrid({
    ...
    afterSaveCell: function (rowid, cellname, value, iRow, iCol) {
        if (cellname === "CUST_MAIN_EMAIL" || cellname === "SALESREP_EMAIL") {
            if (!isValidEmail(value)) {
                alert("유효하지 않은 이메일 형식입니다.");
                $("#gridList").jqGrid("setCell", rowid, cellname, originalValues[rowid + "_" + cellname]);
                return;
            }
        }

        checkRowModified(rowid);
    },
    beforeEditCell: function (rowid, cellname, value, iRow, iCol) {
        originalValues[rowid + "_" + cellname] = value;
    },
    ...
});
</code></pre></div><span><!--ScriptorEndFragment--></span></div>
```
***

## ✅ 2단계: 체크박스 클릭 시 값 변경 감지 + 배경색 변경

### 📌 구현 포인트

*   `afterSaveCell`에서 체크박스 값이 바뀌었는지 확인
*   바뀌었으면 `row-modified` 클래스 추가

### ✅ 코드 예시
```js
<div><div class="scriptor-paragraph"><pre><code class="language-Javascript" preview="Code">function checkRowModified(rowid) {
    var rowData = $("#gridList").jqGrid("getRowData", rowid);
    var original = originalValues[rowid];

    if (!original) return;

    var changed = false;
    ["CUST_MAIN_EMAIL", "SALESREP_EMAIL", "CUST_SENDMAIL_YN", "SALESREP_SENDMAIL_YN"].forEach(function (field) {
        if (rowData[field] !== original[field]) {
            changed = true;
        }
    });

    if (changed) {
        $("#" + rowid).addClass("row-modified");
    } else {
        $("#" + rowid).removeClass("row-modified");
    }
}
</code></pre></div><span><!--ScriptorEndFragment--></span></div>
```
***

## ✅ 3단계: 원래 값 저장 및 복원 처리

### 📌 구현 포인트

*   `gridComplete`에서 원본 값 저장
*   reload 후에도 비교 가능하게 유지

### ✅ 코드 예시
```js
<div><div class="scriptor-paragraph"><pre><code class="language-Javascript" preview="Code">$("#gridList").jqGrid({
    ...
    gridComplete: function () {
        var ids = $("#gridList").jqGrid("getDataIDs");
        ids.forEach(function (rowid) {
            var rowData = $("#gridList").jqGrid("getRowData", rowid);
            if (!originalValues[rowid]) {
                originalValues[rowid] = $.extend({}, rowData); // 깊은 복사
            }
            checkRowModified(rowid); // 초기 비교
        });
    }
});
</code></pre></div><span><!--ScriptorEndFragment--></span></div>
```
***

## ✅ 4단계: 스타일 정의 (배경색)

![Visualization](data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAD8AAAAAsCAYAAACgokM4AAAAAXNSR0IArs4c6QAADKJJREFUeF7t2zERAAAMArHi33Rt/JAq4EJXdo4AAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECAQEFsggAgECBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQOAN4T0CAAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECCQED+EQNQhAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQICAAbwfIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAAIGEgAF8ogYhCBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQMAA3g8QIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAQELAAD5RgxAECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgYADvBwgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIEAgIWAAn6hBCAIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAwgPcDBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIJAQMIBP1CAEAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBjA+wECBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQSAgYwCdqEIIAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIEDOD9AAECBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgkBAzgEzUIQYAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIG8H6AAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECBBICBvCJGoQgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQN4P0CAAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECCQED+EQNQhAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQICAAbwfIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAAIGEgAF8ogYhCBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQMAA3g8QIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAQELAAD5RgxAECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgYADvBwgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIEAgIWAAn6hBCAIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAwgPcDBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIJAQMIBP1CAEAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBjA+wECBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQSAgYwCdqEIIAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIEDOD9AAECBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgkBAzgEzUIQYAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIG8H6AAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECBBICBvCJGoQgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQN4P0CAAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECCQED+EQNQhAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQICAAbwfIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAAIGEgAF8ogYhCBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQMAA3g8QIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAQELAAD5RgxAECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgYADvBwgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIEAgIWAAn6hBCAIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAwgPcDBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIECAAAECBAgQIJAQMIBP1CAEAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECBAgQIAAAQIECDyI7AAtH9T3UQAAAABJRU5ErkJggg==)

***

## 🔄 요약

| 기능               | 구현 방식                        |
| ---------------- | ---------------------------- |
| 이메일 유효성 검사       | `afterSaveCell` + 정규식        |
| 체크박스 변경 감지       | `afterSaveCell` + 원본 비교      |
| 배경색 변경           | `row-modified` 클래스 추가/제거     |
| 원래 값 복원 시 배경색 원복 | `checkRowModified()`에서 자동 처리 |
| reload 후에도 유지    | `gridComplete`에서 원본 저장 및 비교  |

***

## ❗주의사항

*   `cellEdit: true`일 때만 `afterSaveCell`이 동작합니다.
*   `formatter: "checkbox"`는 `"Y:N"` 형식으로만 동작하므로 값 비교 시 문자열로 처리해야 합니다.
*   `originalValues`는 메모리 기반이라 페이지 이동 시 유지되지만, **전체 reload 시 초기화**됩니다. 이건 jqGrid 한계로, **로컬스토리지나 hidden 필드로 보완 가능**하지만 외부 플러그인 없이 완전한 유지에는 한계가 있습니다.

***

필요하시면 **로컬스토리지 활용한 변경 상태 유지**도 추가로 설명드릴게요.\
다음 단계로 어떤 걸 더 도와드릴까요? 😊


---------------------------------------------------------------------


# 위의 질문에서 [관련 코드/필드] 부분만 아래의 코드로 변경해서 다시 정리해줘.
```js
<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/include/admin/commonimport.jsp" %>
<!DOCTYPE html>
<html>
<head>
<%@ include file="/WEB-INF/views/include/admin/commonhead.jsp" %>

< script type="text/javascript" src="${url}/include/js/common/select2/select2.js"></script>
<link rel="stylesheet" href="${url}/include/js/common/select2/select2.css" />

<style>
.changed-row {
    background-color: #D1ECF1 !important;
}

.small-checkbox input[type="checkbox"] {
    width: 16px;
    height: 16px;
}

</style>

< script type="text/javascript">
//Start. Setting Jqgrid Columns Order.
var ckNameJqGrid = 'admin/customer/customerList/jqGridCookie'; // 페이지별 쿠키명 설정. // ####### 설정 #######
ckNameJqGrid += '/gridList'; // 그리드명별 쿠키명 설정. // ####### 설정 #######

var globalColumnOrderStr = toStr(decodeURIComponent(getCookie(ckNameJqGrid)));
var globalColumnOrder = globalColumnOrderStr.split(',');

var defaultColModel = [ //  ####### 설정 #######
	{name:"CUST_CD", key:true, label:'코드', width:120, align:'center', sortable:true },
	{name:"CUST_NM", label:'거래처명', width:220, align:'left', sortable:true },
	{name:"CUST_MAIN_EMAIL", label:'담당자 이메일', width:220, align:'center', sortable:true, editable: true  },
	{name:"CUST_SENDMAIL_YN", label:'이메일 발송', width:100, align:'center', sortable:true, editable: true, edittype: "checkbox", formatter: "checkbox", formatoptions: { disabled: false }, editoptions: { value: "Y:N", defaultValue: "Y" }, cellattr: function() { return 'class="small-checkbox"'} },
	{name:"SALESREP_NM", label:'영업 담당', width:100, align:'center', sortable:true },
	{name:"SALESREP_EMAIL", label:'영업 담당 이메일', width:300, align:'center', sortable:true, editable: true },
	{name:"SALESREP_SENDMAIL_YN", label:'이메일 발송', width:100, align:'center', sortable:true, editable: true, edittype: "checkbox", formatter: "checkbox", formatoptions: { disabled: false }, editoptions: { value: "Y:N", defaultValue: "Y" }, cellattr: function() { return 'class="small-checkbox"'} },
	{name:"COMMENTS", label:'비고', width:450, align:'left', sortable:true, editable: true },
];
var defaultColumnOrder = writeIndexToStr(defaultColModel.length);
//console.log('defaultColumnOrder : ', defaultColumnOrder);
var updateComModel = []; // 전역.

if(0 < globalColumnOrder.length){ // 쿠키값이 있을때.
	if(defaultColModel.length == globalColumnOrder.length){
		for(var i=0,j=globalColumnOrder.length; i<j; i++){
			updateComModel.push(defaultColModel[globalColumnOrder[i]]);
		}
		
		setCookie(ckNameJqGrid, globalColumnOrder, 365); // 여기서 계산을 다시 해줘야겠네.
		//delCookie(ckNameJqGrid); // 쿠키삭제
	}else{
		updateComModel = defaultColModel;
		
		setCookie(ckNameJqGrid, defaultColumnOrder, 365);
	}
}
else{ // 쿠키값이 없을때.
	updateComModel = defaultColModel;
	setCookie(ckNameJqGrid, defaultColumnOrder, 365);
}
//console.log('defaultColModel : ', defaultColModel);
//console.log('updateComModel : ', updateComModel);
// End.

//### 1 WIDTH ###################################################################################################
// @@@@@@@ For Resize Column @@@@@@@
//Start. Setting Jqgrid Columns Order.
var ckNameJqGridWidth = ckNameJqGrid+'/width'; // 페이지별 쿠키명 설정.
var globalColumnWidthStr = toStr(decodeURIComponent(getCookie(ckNameJqGridWidth)));
var globalColumnWidth = globalColumnWidthStr.split(',');
// console.log('globalColumnWidthStr : ', globalColumnWidthStr);
// console.log('globalColumnWidth : ', globalColumnWidth);
var defaultColumnWidthStr = '';
var defaultColumnWidth;
var updateColumnWidth;
if('' != globalColumnWidthStr){ // 쿠키값이 있을때.
	if(updateComModel.length == globalColumnWidth.length){
		updateColumnWidth = globalColumnWidth;
	}else{
		for( var j=0; j<updateComModel.length; j++ ) {
			//console.log('currentColModel[j].name : ', currentColModel[j].name);
			if('rn' != updateComModel[j].name && 'cb' != updateComModel[j].name){
				var v = ('' != toStr(updateComModel[j].width)) ? toStr(updateComModel[j].width) : '0';
				if('' == defaultColumnWidthStr) defaultColumnWidthStr = v;
				else defaultColumnWidthStr += ','+v;
			}
		}
		defaultColumnWidth = defaultColumnWidthStr.split(',');
		updateColumnWidth = defaultColumnWidth;
		setCookie(ckNameJqGridWidth, defaultColumnWidth, 365);
	}
}
else{ // 쿠키값이 없을때.
	//console.log('updateComModel : ', updateComModel);
	
	for( var j=0; j<updateComModel.length; j++ ) {
		//console.log('currentColModel[j].name : ', currentColModel[j].name);
		if('rn' != updateComModel[j].name && 'cb' != updateComModel[j].name){
			var v = ('' != toStr(updateComModel[j].width)) ? toStr(updateComModel[j].width) : '0';
			if('' == defaultColumnWidthStr) defaultColumnWidthStr = v;
			else defaultColumnWidthStr += ','+v;
		}
	}
	defaultColumnWidth = defaultColumnWidthStr.split(',');
	updateColumnWidth = defaultColumnWidth;
	setCookie(ckNameJqGridWidth, defaultColumnWidth, 365);
}
//console.log('### defaultColumnWidthStr : ', defaultColumnWidthStr);
//console.log('### updateColumnWidth : ', updateColumnWidth);

if(updateComModel.length == globalColumnWidth.length){
	//console.log('이전 updateComModel : ',updateComModel);
	for( var j=0; j<updateComModel.length; j++ ) {
		updateComModel[j].width = toStr(updateColumnWidth[j]);
	}
	//console.log('이후 updateComModel : ',updateComModel);
}
// End.

$(function(){
	getGridList();
});

var originalData = {}; // 최상단에 선언

function getGridList(){
	// grid init
	var searchData = getSearchData();
	$('#gridList').jqGrid({
		url: "${url}/admin/customer/getOrderEmailAlarmAjax.lime",
		editurl: 'clientArray', //사용x
		//editurl: './deliveryspotUpAjax.lime',
		datatype: "json",
		mtype: 'POST',
		postData: searchData,
		colModel: updateComModel,
		cellEdit: true,
		cellsubmit: 'clientArray',
		height: '360px',
		autowidth: false,
		rowNum : 10,
		rowList : ['10','30','50','100'],
		rownumbers: true,
		pagination: true,
		pager: "#pager",
		actions : true,
		pginput : true,
		resizable: true,
		sortable: { // ####### 설정 #######
			update: function(relativeColumnOrder){
				var grid = $('#gridList');
				var defaultColIndicies = [];
				for( var i=0; i<defaultColModel.length; i++ ) {
					defaultColIndicies.push(defaultColModel[i].name);
				}
	
				globalColumnOrder = []; // 초기화.
				var columnOrder = [];
				var currentColModel = grid.getGridParam('colModel');
				for( var j=0; j<relativeColumnOrder.length; j++ ) {
					//console.log('currentColModel[j].name : ', currentColModel[j].name);
					if('rn' != currentColModel[j].name && 'cb' != currentColModel[j].name){
						columnOrder.push(defaultColIndicies.indexOf(currentColModel[j].name));
					}
				}
				globalColumnOrder = columnOrder;
				
				setCookie(ckNameJqGrid, globalColumnOrder, 365);
				
				// @@@@@@@ For Resize Column @@@@@@@
				//currentColModel = grid.getGridParam('colModel');
				//console.log('이전 updateColumnWidth : ', updateColumnWidth);
				var tempUpdateColumnWidth = [];
				for( var j=0; j<currentColModel.length; j++ ) {
				   if('rn' != currentColModel[j].name && 'cb' != currentColModel[j].name){
				      tempUpdateColumnWidth.push(currentColModel[j].width); 
				   }
				}
				updateColumnWidth = tempUpdateColumnWidth;
				//console.log('이후 updateColumnWidth : ', updateColumnWidth);
				setCookie(ckNameJqGridWidth, updateColumnWidth, 365);
			}
		},
		// @@@@@@@ For Resize Column @@@@@@@
		resizeStop: function(width, index) { 
			console.log('globalColumnOrder : ', globalColumnOrder);
			var minusIdx = 0;
			
			var grid = $('#gridList');
			var currentColModel = grid.getGridParam('colModel');
			//console.log('currentColModel : ', currentColModel);
			if('rn' == currentColModel[0].name || 'cb' == currentColModel[0].name) minusIdx--;
			if('rn' == currentColModel[1].name || 'cb' == currentColModel[1].name) minusIdx--;
			//console.log('minusIdx : ', minusIdx);
			
			var resizeIdx = index + minusIdx;
			//console.log('resizeIdx : ', resizeIdx);
			
			//var realIdx = globalColumnOrder[resizeIdx];
			//console.log('realIdx : ', realIdx);
			
			updateColumnWidth[resizeIdx] = width;
			
			setCookie(ckNameJqGridWidth, updateColumnWidth, 365);
	        //alert('Resize Column : '+index+'\nWidth : '+width);
	    },
		sortorder: 'desc',
		jsonReader : { 
			root : 'list',
		},
		loadComplete: function(data) {
		    originalData = {};
		    if (data.list && data.list.length > 0) {
		        data.list.forEach(function(row) {
		            originalData[row.CUST_CD] = $.extend({}, row);
		        });
		    }
			//$('#gridList').getGridParam("reccount"); // 현재 페이지에 뿌려지는 row 개수
			//$('#gridList').getGridParam("records"); // 현재 페이지에 limitrow
			$('#listTotalCountSpanId').html(addComma(data.listTotalCount));
			$('.ui-pg-input').val(data.page);

		},
		afterSaveCell: function(rowid, cellname, value, iRow, iCol) {
		},
		onSelectRow: function(rowId){
			//console.log('########################### onSelectRow rowId : ' + rowId);
		},
		//onSelectRow: editRow,
		onSelectAll: function(rowIdArr, status) { //전체 체크박스 선택했을때 onSelectRow가 실행이 안되고 onSelectAll 실행되네...
			//console.log('status : ', status); //status : true=전체선택했을때, false=전체해제했을때
			//console.log('rowIdArr : ', rowIdArr); //rowid 배열 타입
			//console.log('rowIdArr.length : ', rowIdArr.length);
		}
		/* 
		beforeProcessing: functi0on(data, status, xhr){ // 서버로 부터 데이터를 받은 후 화면에 찍기 위한 processing을 진행하기 직전에 호출.
			if('0000' != data.RES_CODE){
				alert(data.RES_MSG);
				return false;
			}
		},
		*/
	});
}

function getSearchData(){
	var rl_custcd = $('input[name="rl_custcd"]').val();
	var rl_custnm = $('input[name="rl_custnm"]').val();
	var rl_salesrepnm = $('input[name="rl_salesrepnm"]').val();
	
	var r_salesepcY = '', r_salesepcN = ''; //영업담당자 YN
	if($('input[name="r_salesrepcdyn"]:checked').length == 1){
		if($('input[name="r_salesrepcdyn"]:checked').val() == 'Y') r_salesepcN = '0';
		else r_salesepcY = '0';
	}
	
	var sData = {
		rl_custcd : rl_custcd
		, rl_custnm : rl_custnm
		, rl_salesrepnm : rl_salesrepnm
		, r_salesrepcdyn : r_salesepcY
		, rn_salesrepcdyn : r_salesepcN
	};
	return sData;
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
	
	formPostSubmit('frm', '${url}/admin/customer/customerExcelDown.lime');
	$('form[name="frm"]').attr('action', '');
	
	$('input[name="filetoken"]').remove();
	var fileTimer = setInterval(function() {
		//console.log('token : ', token);
        //console.log("cookie : ", getCookie(token));
		if('true' == getCookie(token)){
			$('#ajax_indicator').fadeOut();
			delCookie(token);
			clearInterval(fileTimer);
		}
    }, 1000 );
}


//gridList Reload.
function reloadGridList(){
	$('#gridList').trigger('reloadGrid', [{current:true}]); // 리로드후 현재 유지.
	
	var gridRowId = toStr($('#gridList').getGridParam('selrow'));
	if('' != toStr(gridRowId)){
		$('#gridList').setSelection(gridRowId, true); // 리로드후 선택
	}
}

</script>
</head>

<body class="page-header-fixed compact-menu">
	
	<!-- Page Content -->
	<main class="page-content content-wrap">
	
		<%@ include file="/WEB-INF/views/include/admin/header.jsp" %>
		<%@ include file="/WEB-INF/views/include/admin/left.jsp" %>
		
		<%-- 임의 form --%>
		<form name="iForm" method="post"></form>
		<%-- <form name="uForm" method="post" action="${url}/admin/system/deliverySpotEditPop.lime" target="deliverySpotEditPop"></form> --%>
		
		<form name="frm" method="post">
		
		<!-- Page Inner -->
		<div class="page-inner">
			<div class="page-title">
				<h3>
					거래처현황
					<div class="page-right">
						<button type="button" class="btn btn-line f-black" title="검색" onclick="dataSearch();"><i class="fa fa-search"></i><em>검색</em></button>
						<button type="button" class="btn btn-line f-black" title="새로고침" onclick="window.location.reload();"><i class="fa fa-refresh"></i><em>새로고침</em></button>
						<button type="button" class="btn btn-line f-black" title="엑셀다운로드" onclick="excelDown(this);"><i class="fa fa-file-excel-o"></i><em>엑셀다운로드</em></button>
					</div>
				</h3>
			</div>
			
			<!-- Main Wrapper -->
			<div id="main-wrapper">
				<!-- Row -->
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
													<input type="text" class="search-input" name="rl_custcd" value="${param.rl_custcd}" onkeypress="if(event.keyCode == 13){dataSearch();}" />
												</div>
											</li>
											<li>
												<label class="search-h">거래처명</label>
												<div class="search-c">
													<input type="text" class="search-input" name="rl_custnm" value="${param.rl_custnm}" onkeypress="if(event.keyCode == 13){dataSearch();}" />
												</div>
											</li>
											<li>
												<label class="search-h">영업담당</label>
												<div class="search-c">
													<input type="text" class="search-input" name="rl_salesrepnm" value="${param.rl_salesrepnm}" onkeypress="if(event.keyCode == 13){dataSearch();}" />
												</div>
											</li>
											<li>
												<label class="search-h">영업담당자</label>
												<div class="search-c checkbox">
													<label><input type="checkbox" name="r_salesrepcdyn" value="Y" onclick="dataSearch();" />Y</label>
													<label><input type="checkbox" name="r_salesrepcdyn" value="N" onclick="dataSearch();" />N</label>
												</div>
											</li>
										</ul>
									</div>
								</div>
							</div>
							
							<div class="panel-body">
								<h5 class="table-title listT">TOTAL <span id="listTotalCountSpanId">0</span>EA</h5>
								<div class="table-responsive in">
									<table id="gridList" class="display table tableList nowrap" width="100%" border="0" cellpadding="0" cellspacing="0"></table>
									<div id="pager"></div>
								</div>
							</div>
							
							
						</div>
					</div>
				</div>
				<!-- //Row -->
			</div>
			<!-- //Main Wrapper -->
			
			<%@ include file="/WEB-INF/views/include/admin/footer.jsp" %>
			
		</div>
		
		</form>
		<!-- //Page Inner -->
	</main>
	<!-- //Page Content -->
	
</body>

</html>
```

---------------------------------------------------------------------



