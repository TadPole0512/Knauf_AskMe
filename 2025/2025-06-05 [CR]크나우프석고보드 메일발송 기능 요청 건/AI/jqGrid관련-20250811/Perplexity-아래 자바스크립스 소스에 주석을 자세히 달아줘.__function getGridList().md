<img src="https://r2cdn.perplexity.ai/pplx-full-logo-primary-dark%402x.png" style="height:64px;margin-right:32px"/>

# 아래 자바스크립스 소스에 주석을 자세히 달아줘.
```javascript
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
pager: "\#pager",
actions : true,
pginput : true,
//spageable: true,
//groupable: true,
//filterable: true,
//columnMenu: true,
//reorderable: true,
resizable: true,
//sortable: true,
sortable: { // \#\#\#\#\#\#\# 설정 \#\#\#\#\#\#\#
update: function(relativeColumnOrder){
var grid = \$('\#gridList');
var defaultColIndicies = [];
for( var i=0; i<defaultColModel.length; i++ ) {
defaultColIndicies.push(defaultColModel[i].name);
}

globalColumnOrder = []; // 초기화.
var columnOrder = [];
var currentColModel = grid.getGridParam('colModel');
for( var j=0; j<relativeColumnOrder.length; j++ ) {
//console.log('currentColModel[j].name : ', currentColModel[j].name);
if('rn' != currentColModel[j].name \&\& 'cb' != currentColModel[j].name){
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
if('rn' != currentColModel[j].name \&\& 'cb' != currentColModel[j].name){
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

var grid = \$('\#gridList');
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
if (data.list \&\& data.list.length > 0) {
data.list.forEach(function(row) {
originalData[row.CUST_CD] = \$.extend({}, row);
});
}

// 체크박스 클릭 이벤트 등록
setTimeout(function() {
registerCheckboxEvents();
}, 100);

//$('#gridList').getGridParam("reccount"); // 현재 페이지에 뿌려지는 row 개수
//$('\#gridList').getGridParam("records"); // 현재 페이지에 limitrow
\$('\#listTotalCountSpanId').html(addComma(data.listTotalCount));
\$('.ui-pg-input').val(data.page);
},
afterSaveCell: function(rowid, cellname, value, iRow, iCol) {
console.log("afterSaveCell - rowid: " + rowid + ", cellname: " + cellname + ", value: " + value);
checkRowChanges(rowid);
},
onSelectRow: function(rowId){
//console.log('\#\#\#\#\#\#\#\#\#\#\#\#\#\#\#\#\#\#\#\#\#\#\#\#\#\#\# onSelectRow rowId : ' + rowId);
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

// 체크박스 이벤트 등록 함수 추가
function registerCheckboxEvents() {
\$('\#gridList input[type="checkbox"]').off('click.customCheckbox').on('click.customCheckbox', function() {
var \$checkbox = \$(this);
var \$cell = \$checkbox.closest('td');
var rowid = \$cell.closest('tr').attr('id');
var colname = '';

        // 컬럼명 찾기
        var cellIndex = $cell.index();
        var colModel = $('#gridList').jqGrid('getGridParam', 'colModel');
        
        for (var i = 0; i < colModel.length; i++) {
            if (colModel[i].name === 'CUST_SENDMAIL_YN' || colModel[i].name === 'SALESREP_SENDMAIL_YN') {
                var $cellAtIndex = $('#gridList tr[id="' + rowid + '"] td:eq(' + i + ')');
                if ($cellAtIndex.is($cell)) {
                    colname = colModel[i].name;
                    break;
                }
            }
        }
        
        if (colname) {
            console.log("체크박스 클릭 - rowid: " + rowid + ", column: " + colname + ", checked: " + $checkbox.is(':checked'));
            
            // jqGrid에 값 업데이트
            var newValue = $checkbox.is(':checked') ? 'Y' : 'N';
            $('#gridList').jqGrid('setCell', rowid, colname, newValue);
            
            // 변경사항 체크
            setTimeout(function() {
                checkRowChanges(rowid);
            }, 50);
        }
    });
    }

// 행 변경사항 체크 함수
function checkRowChanges(rowid) {
var compareColumns = ["CUST_MAIN_EMAIL", "SALESREP_EMAIL", "COMMENTS", "CUST_SENDMAIL_YN", "SALESREP_SENDMAIL_YN"];
var isChanged = false;
var rowData = \$('\#gridList').jqGrid('getRowData', rowid);

    console.log("checkRowChanges - rowid: " + rowid);
    console.log("현재 rowData:", rowData);
    console.log("원본 originalData:", originalData[rowid]);
    
    for (var i = 0; i < compareColumns.length; i++) {
        var col = compareColumns[i];
        var original = originalData[rowid] && originalData[rowid][col];
        var current = rowData[col];
    
        // 값 보정: null, undefined, 공백 등
        current = current === undefined || current === null ? "" : (current + "").trim();
        original = original === undefined || original === null ? "" : (original + "").trim();
    
        // 체크박스(Y/N)일 때는 강제 변환
        if (col.endsWith('_YN')) {
            current = toYN(current);
            original = toYN(original);
        }
    
        console.log(col + " - 원본: '" + original + "', 현재: '" + current + "'");
    
        if (current != original) {
            isChanged = true;
            console.log(col + " 컬럼이 변경되었습니다!");
            break;
        }
    }
    
    var $tr = $('#gridList tr[id="' + rowid + '"]');
    if (isChanged) {
        $tr.addClass("changed-row");
        console.log("배경색 변경 적용: " + rowid);
    } else {
        $tr.removeClass("changed-row");
        console.log("배경색 변경 제거: " + rowid);
    }
    }

//체크박스 비교 함수
function toYN(val) {
if (val === undefined || val === null) return "";
val = (val + "").toUpperCase().trim();
if (val === "Y" || val === "YES" || val === "1" || val === "TRUE") return "Y";
if (val === "N" || val === "NO"  || val === "0" || val === "FALSE") return "N";
return val;
}
```
좋아, 그럼 너가 올려준 자바스크립트 소스에 **한 줄 한 줄 무슨 역할을 하는지 주석을 추가**해줄게.
이렇게 하면 이후에 유지보수나 기능 수정할 때 이해하기 훨씬 편해질 거야.

***

```javascript
// 그리드 데이터 로드 및 초기화 함수
function getGridList(){
    // 검색조건 데이터 가져오기
    var searchData = getSearchData();

    // jqGrid 초기화
    $('#gridList').jqGrid({
        url: "${url}/admin/customer/getOrderEmailAlarmAjax.lime", // 서버 호출 URL
        editurl: 'clientArray', // 행 편집 시 서버로 전송되지 않도록 설정(로컬에서만 처리)
        // editurl: './deliveryspotUpAjax.lime', // 사용 가능하지만 현재 주석처리
        datatype: "json", // 서버에서 응답받을 데이터 타입
        mtype: 'POST',    // 서버 요청 방식
        postData: searchData, // 서버로 전송할 검색 조건 데이터
        colModel: updateComModel, // 컬럼 및 속성 정의 배열
        cellEdit: true,           // 셀 단위 편집 허용
        cellsubmit: 'clientArray',// 편집결과를 로컬에 저장
        height: '360px',          // 그리드 높이
        autowidth: false,         // 가로 폭 자동 조정 안함
        rowNum : 10,              // 기본 페이지당 출력 행 수
        rowList : ['10','30','50','100'], // 페이지당 행 수 옵션
        rownumbers: true,         // 행 번호 표시
        pagination: true,         // 페이지네이션 활성화
        pager: "#pager",          // 페이지 네비게이션 영역 ID
        actions : true,           // 기본 액션 버튼 표시 여부
        pginput : true,           // 페이지 번호 직접 입력 가능
        resizable: true,          // 열 크기 조절 가능

        // 열 순서 변경 이벤트
        sortable: { 
            update: function(relativeColumnOrder){
                var grid = $('#gridList');
                
                // 기본 컬럼 이름 배열
                var defaultColIndicies = [];
                for( var i=0; i<defaultColModel.length; i++ ) {
                    defaultColIndicies.push(defaultColModel[i].name);
                }

                // 새로운 컬럼 순서 계산
                globalColumnOrder = []; 
                var columnOrder = [];
                var currentColModel = grid.getGridParam('colModel');

                for( var j=0; j<relativeColumnOrder.length; j++ ) {
                    // Row 번호(rn)나 Checkbox(cb) 제외
                    if('rn' != currentColModel[j].name && 'cb' != currentColModel[j].name){
                        columnOrder.push(defaultColIndicies.indexOf(currentColModel[j].name));
                    }
                }
                globalColumnOrder = columnOrder;

                // 변경된 순서를 쿠키로 저장
                setCookie(ckNameJqGrid, globalColumnOrder, 365);

                // 열 너비도 함께 저장
                var tempUpdateColumnWidth = [];
                for( var j=0; j<currentColModel.length; j++ ) {
                    if('rn' != currentColModel[j].name && 'cb' != currentColModel[j].name){
                        tempUpdateColumnWidth.push(currentColModel[j].width); 
                    }
                }
                updateColumnWidth = tempUpdateColumnWidth;
                setCookie(ckNameJqGridWidth, updateColumnWidth, 365);
            }
        },

        // 열 크기 조정 후 실행되는 이벤트
        resizeStop: function(width, index) { 
            console.log('globalColumnOrder : ', globalColumnOrder);
            var minusIdx = 0;
            var grid = $('#gridList');
            var currentColModel = grid.getGridParam('colModel');

            // row number, row checkbox 컬럼이 맨 앞에 있으면 index 조정
            if('rn' == currentColModel[0].name || 'cb' == currentColModel[0].name) minusIdx--;
            if('rn' == currentColModel[1].name || 'cb' == currentColModel[1].name) minusIdx--;

            // 실제 조정된 컬럼 인덱스 계산
            var resizeIdx = index + minusIdx;

            // 변경된 너비 배열 반영
            updateColumnWidth[resizeIdx] = width;

            // 쿠키에 저장
            setCookie(ckNameJqGridWidth, updateColumnWidth, 365);
        },

        sortorder: 'desc', // 정렬 순서 기본값

        jsonReader : { 
            root : 'list', // 서버 응답 JSON에서 데이터 배열 경로
        },

        // 데이터 로드 완료 후 실행
        loadComplete: function(data) {
            // 원본 데이터 저장 (변경 감지용)
            originalData = {};
            if (data.list && data.list.length > 0) {
                data.list.forEach(function(row) {
                    originalData[row.CUST_CD] = $.extend({}, row);
                });
            }

            // 체크박스 클릭 이벤트 바인딩(비동기 렌더 문제 방지 위해 setTimeout 사용)
            setTimeout(function() {
                registerCheckboxEvents();
            }, 100);

            // 총 건수 표시
            $('#listTotalCountSpanId').html(addComma(data.listTotalCount));
            // 현재 페이지 번호 표시
            $('.ui-pg-input').val(data.page);
        },

        // 셀 편집 저장 후 실행되는 이벤트
        afterSaveCell: function(rowid, cellname, value, iRow, iCol) {
            console.log("afterSaveCell - rowid: " + rowid + ", cellname: " + cellname + ", value: " + value);
            checkRowChanges(rowid); // 해당 행의 변경 감지 실행
        },

        // 행 선택 이벤트
        onSelectRow: function(rowId){
            // 현재는 동작 없음, 필요시 구현 가능
        },

        // 전체 선택/해제 이벤트
        onSelectAll: function(rowIdArr, status) {
            // 모든 행 선택했을 때 실행됨
            // status: true=전체선택, false=전체해제
        }
    });
}

// 체크박스 클릭 이벤트를 jqGrid 행에 연결
function registerCheckboxEvents() {
    // 기존 이벤트 제거 후 새로 바인딩(.off().on()) 사용
    $('#gridList input[type="checkbox"]').off('click.customCheckbox').on('click.customCheckbox', function() {
        var $checkbox = $(this);
        var $cell = $checkbox.closest('td');
        var rowid = $cell.closest('tr').attr('id');
        var colname = '';
        
        // 클릭한 체크박스의 컬럼명 찾기
        var colModel = $('#gridList').jqGrid('getGridParam', 'colModel');
        for (var i = 0; i < colModel.length; i++) {
            if (colModel[i].name === 'CUST_SENDMAIL_YN' || colModel[i].name === 'SALESREP_SENDMAIL_YN') {
                var $cellAtIndex = $('#gridList tr[id="' + rowid + '"] td:eq(' + i + ')');
                if ($cellAtIndex.is($cell)) {
                    colname = colModel[i].name;
                    break;
                }
            }
        }
        
        if (colname) {
            console.log("체크박스 클릭 - rowid: " + rowid + ", column: " + colname + ", checked: " + $checkbox.is(':checked'));
            
            // 체커박스 상태를 'Y'/'N'으로 setCell
            var newValue = $checkbox.is(':checked') ? 'Y' : 'N';
            $('#gridList').jqGrid('setCell', rowid, colname, newValue);
            
            // 변경 여부 체크
            setTimeout(function() {
                checkRowChanges(rowid);
            }, 50);
        }
    });
}

// 특정 행의 변경 여부를 체크하여 배경색을 적용/제거
function checkRowChanges(rowid) {
    // 비교 대상 컬럼 목록
    var compareColumns = ["CUST_MAIN_EMAIL", "SALESREP_EMAIL", "COMMENTS", "CUST_SENDMAIL_YN", "SALESREP_SENDMAIL_YN"];
    var isChanged = false;
    var rowData = $('#gridList').jqGrid('getRowData', rowid);

    console.log("checkRowChanges - rowid: " + rowid);
    console.log("현재 rowData:", rowData);
    console.log("원본 originalData:", originalData[rowid]);

    for (var i = 0; i < compareColumns.length; i++) {
        var col = compareColumns[i];
        var original = originalData[rowid] && originalData[rowid][col];
        var current = rowData[col];

        // null, undefined, 공백 보정
        current = current === undefined || current === null ? "" : (current + "").trim();
        original = original === undefined || original === null ? "" : (original + "").trim();

        // Y/N 컬럼이면 통일된 값으로 변환
        if (col.endsWith('_YN')) {
            current = toYN(current);
            original = toYN(original);
        }

        console.log(col + " - 원본: '" + original + "', 현재: '" + current + "'");

        // 값이 다르면 변경 발생
        if (current != original) {
            isChanged = true;
            console.log(col + " 컬럼이 변경되었습니다!");
            break;
        }
    }

    // row에 변경 색상 적용 여부 반영
    var $tr = $('#gridList tr[id="' + rowid + '"]');
    if (isChanged) {
        $tr.addClass("changed-row");
        console.log("배경색 변경 적용: " + rowid);
    } else {
        $tr.removeClass("changed-row");
        console.log("배경색 변경 제거: " + rowid);
    }
}

// 값이 Y/N 성격일 때, 여러 표현을 통일하여 'Y' 또는 'N'으로 변환
function toYN(val) {
    if (val === undefined || val === null) return "";
    val = (val + "").toUpperCase().trim();
    if (val === "Y" || val === "YES" || val === "1" || val === "TRUE") return "Y";
    if (val === "N" || val === "NO"  || val === "0" || val === "FALSE") return "N";
    return val; // 위 조건 외 값은 그대로 반환
}
```


***

### 정리

- **getGridList** → jqGrid를 초기화하고 데이터 로딩 / 컬럼 순서 / 컬럼 크기 저장 / 변경 감지 로직의 핵심
- **registerCheckboxEvents** → 체크박스 클릭을 감지해서 그리드 데이터와 동기화
- **checkRowChanges** → 원본 데이터와 비교하여 행 스타일 변경
- **toYN** → 다양한 Y/N 표현을 표준 값('Y' 또는 'N')으로 변환

***

원하면 내가 이 주석을 **inline 한글 설명 + 흐름도**로 정리해서,
"데이터 로드 → 체크박스 동기화 → 변경 감지 → 배경색 적용" 순서를 시각적으로 보여줄 수도 있어.
그렇게 해줄까?
그럼 로직이 한눈에 들어올 거야.

