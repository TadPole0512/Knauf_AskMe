<img src="https://r2cdn.perplexity.ai/pplx-full-logo-primary-dark%402x.png" style="height:64px;margin-right:32px"/>

# **\[환경]**

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

안녕하세요! 말씀하신 시나리오를 아래처럼 단계별로 실무에서 바로 적용 가능한 코드와 설명으로 정리했습니다.

***
## 1. 이메일 셀 편집 - 유효성 및 배경색 처리

### (1) 이메일 유효성 검사 함수

```javascript
function isValidEmail(email) {
  var re = /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/;
  return re.test(email);
}
```


### (2) afterSaveCell 이벤트에서 처리

```javascript
$("#gridList").jqGrid({
  ...
  cellEdit: true,
  cellsubmit: 'clientArray',
  afterSaveCell: function (rowid, cellname, value, iRow, iCol) {
    // 이메일 필드 유효성 검사
    if (cellname === "CUST_MAIN_EMAIL" || cellname === "SALESREP_EMAIL") {
      if (!isValidEmail(value)) {
        alert("유효한 이메일 주소를 입력하세요.");
        var oriData = $("#gridList").jqGrid('getCell', rowid, cellname);
        $("#gridList").jqGrid('setCell', rowid, cellname, '', {background:''}); // 배경 복구
        // 셀 다시 편집
        $("#gridList").jqGrid('editCell', iRow, iCol, true);
        return;
      }
      markRowChanged(rowid);
    }
  },
  ...
});
```


---
## 2. 체크박스 값 변경 처리 (‘Y’/‘N’ 저장, 행 배경 변화)

### (1) beforeSelectRow에서 체크박스 클릭 감지

(jqGrid의 체크박스 클릭 감지는 event delegation 필요)

```javascript
$("#gridList").on("change", "input[type=checkbox]", function(e){
  var $cell = $(this).closest('td');
  var colName = $cell.attr('aria-describedby').replace(/^gridList_/, '');
  var $row = $cell.closest('tr.jqgrow');
  var rowid = $row.attr('id');
  // 체킹 처리
  var checked = $(this).prop('checked');
  var newVal = checked ? 'Y' : 'N';
  var oriVal = $("#gridList").jqGrid('getCell', rowid, colName);
  $("#gridList").jqGrid('setCell', rowid, colName, newVal);
  if (oriVal !== newVal) {
    markRowChanged(rowid);
  } else {
    resetRowStyleIfOriginal(rowid);
  }
});
```


---
## 3. 값이 원래대로 복귀하면 배경색 원복

원본 데이터와 비교하여 처리

```javascript
// 초기값 저장
var originalData = {};
function saveOriginalGridData() {
  var ids = $("#gridList").jqGrid('getDataIDs');
  originalData = {};
  for(var i=0; i<ids.length; i++) {
    var rowData = $("#gridList").jqGrid('getRowData', ids[i]);
    originalData[ids[i]] = $.extend({}, rowData);
  }
}

// 행 변동시 표시/원복
function markRowChanged(rowid) {
  $("#gridList").jqGrid('setRowData', rowid, {}, { background: "#FFF3CD" });
}
function resetRowStyleIfOriginal(rowid) {
  var rowData = $("#gridList").jqGrid('getRowData', rowid);
  var oriRow = originalData[rowid];
  var isChanged = false;

  ["CUST_MAIN_EMAIL","SALESREP_EMAIL",
   "CUST_SENDMAIL_YN","SALESREP_SENDMAIL_YN"]
   .forEach(function(field){
     if(rowData[field] !== oriRow[field]) isChanged = true;
   });

  if (!isChanged) {
    $("#gridList").jqGrid('setRowData', rowid, {}, { background: "" });
  }
}
```

그리드 로드/페이지 변경 시에도 유지하려면, `gridComplete`에서 한 번 더 체크:

```javascript
$("#gridList").jqGrid({
  ...
  gridComplete: function() {
    saveOriginalGridData(); // 최초 한 번 저장
    var ids = $("#gridList").jqGrid('getDataIDs');
    for (var i=0; i<ids.length; i++) {
      resetRowStyleIfOriginal(ids[i]);
    }
  },
  ...
});
```


---
## 4. 그리드 reload/page 이동 시 변경표시 유지

변경값을 별도 배열에 저장해야 reload 후에도 유지 가능합니다. 예를 들어:

```javascript
var changedRows = {};

function markRowChanged(rowid) { /* ... */
  changedRows[rowid] = true;
}

function resetRowStyleIfOriginal(rowid) { /* ... */
  if (!isChanged && changedRows[rowid]) {
    delete changedRows[rowid];
    $("#gridList").jqGrid('setRowData', rowid, {}, {background:''});
  }
}

// gridComplete에서도 체크하여 표시
$("#gridList").jqGrid({
  ...
  gridComplete: function() {
    var ids = $("#gridList").jqGrid('getDataIDs');
    for (var i=0; i<ids.length; i++) {
      if (changedRows[ids[i]]) {
        $("#gridList").jqGrid('setRowData', ids[i], {}, {background:'#FFF3CD'});
      }
    }
  },
  ...
});
```


***
## 코드 전체 요약

- 이메일 셀 편집 = 유효성 체크 + 행 배경
- 체크박스 변경 = 값/원본 비교 + 행 배경
- 값 원복 = 입력값/원본 비교 → 행 스타일 원상복구
- reload/page 이동 = 변경된 행 배열 저장/사용

***
## 주의 \& 한계

- 외부 플러그인 없이 jqGrid + 순수 JS/jQuery 코드만 사용!
- 브라우저 reload(새로고침)까지 상태 저장은 localStorage등 추가코드 필요(질문 조건 상 배제)
- 실무 환경에 맞게 colModel/rowData 등 기타 옵션 커스터마이즈 필요

***

**모르거나 불가한 부분:**

- 완벽한 변경표시 지속(새로고침/다른 페이지 이동간 유지)은 현재 요구에선 불가
- 서버 반영/저장 불가
- 외부 플러그인 불가 (조건상 충족)

***

궁금한 점, 추가 시나리오 있으면 말씀해 주세요!

