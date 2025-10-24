<img src="https://r2cdn.perplexity.ai/pplx-full-logo-primary-dark%402x.png" style="height:64px;margin-right:32px"/>

# 보여줘

좋아, 그럼 아까 네가 요청했던 **`getGridList` 함수와 관련된 자바스크립트 전체 코드에 주석을 단 상세 버전** 다시 한번 통으로 보여줄게.
이렇게 보면 각 줄이 무슨 역할을 하는지 한눈에 볼 수 있어.

***

```javascript
// 그리드 데이터를 불러오고 jqGrid를 초기화하는 함수
function getGridList(){
    // 현재 검색 조건을 객체로 가져옴
    var searchData = getSearchData();

    $('#gridList').jqGrid({
        // 서버에서 데이터 불러올 URL
        url: "${url}/admin/customer/getOrderEmailAlarmAjax.lime",
        // 셀 수정시 서버 저장하지 않고 로컬에서만 관리
        editurl: 'clientArray',
        datatype: "json",      // 응답 데이터 타입
        mtype: 'POST',         // 전송 방식
        postData: searchData,  // 서버로 보낼 파라미터
        colModel: updateComModel,  // 컬럼 설정
        cellEdit: true,        // 셀 편집 허용
        cellsubmit: 'clientArray', // 로컬 저장 설정
        height: '360px',       // 그리드 높이
        autowidth: false,      // 가로 자동 크기 조절 안함
        rowNum : 10,           // 한 페이지 행 수
        rowList : ['10','30','50','100'], // 행 수 선택 옵션
        rownumbers: true,      // 왼쪽에 행 번호 출력
        pagination: true,      // 페이지네이션 활성화
        pager: "#pager",       // 페이지 표시 영역 ID
        actions : true,        // 기본 액션 기능 허용
        pginput : true,        // 페이지 번호 입력 허용
        resizable: true,       // 열 크기 조절 가능

        // ★ 열 순서 변경 후 실행되는 콜백
        sortable: {
            update: function(relativeColumnOrder){
                var grid = $('#gridList');

                // 기본 컬럼 인덱스 목록
                var defaultColIndicies = [];
                for(var i=0; i<defaultColModel.length; i++){
                    defaultColIndicies.push(defaultColModel[i].name);
                }

                var columnOrder = [];
                var currentColModel = grid.getGridParam('colModel');

                // rn(번호), cb(체크박스) 컬럼 제외하고 순서 저장
                for(var j=0; j<relativeColumnOrder.length; j++){
                    if('rn' != currentColModel[j].name && 'cb' != currentColModel[j].name){
                        columnOrder.push(defaultColIndicies.indexOf(currentColModel[j].name));
                    }
                }
                globalColumnOrder = columnOrder;

                // 컬럼 순서를 쿠키에 저장
                setCookie(ckNameJqGrid, globalColumnOrder, 365);

                // 현재 컬럼Width도 쿠키에 저장
                var tempUpdateColumnWidth = [];
                for(var j=0; j<currentColModel.length; j++){
                    if('rn' != currentColModel[j].name && 'cb' != currentColModel[j].name){
                        tempUpdateColumnWidth.push(currentColModel[j].width);
                    }
                }
                updateColumnWidth = tempUpdateColumnWidth;
                setCookie(ckNameJqGridWidth, updateColumnWidth, 365);
            }
        },

        // ★ 열 크기 조절 후 실행되는 콜백
        resizeStop: function(width, index){
            var minusIdx = 0;
            var grid = $('#gridList');
            var currentColModel = grid.getGridParam('colModel');

            if('rn' == currentColModel[0].name || 'cb' == currentColModel[0].name) minusIdx--;
            if('rn' == currentColModel[1].name || 'cb' == currentColModel[1].name) minusIdx--;

            var resizeIdx = index + minusIdx;

            // 변경한 width 저장 후 쿠키 반영
            updateColumnWidth[resizeIdx] = width;
            setCookie(ckNameJqGridWidth, updateColumnWidth, 365);
        },

        sortorder: 'desc', // 기본 정렬 순서

        jsonReader: { root : 'list' }, // 데이터 리스트 경로

        // ★ 데이터 로드 완료 후 실행 함수
        loadComplete: function(data){
            // 기존 데이터 백업
            originalData = {};
            if(data.list && data.list.length > 0){
                data.list.forEach(function(row){
                    originalData[row.CUST_CD] = $.extend({}, row);
                });
            }

            // 체크박스 이벤트 연결 (비동기 렌더링 대비)
            setTimeout(function(){
                registerCheckboxEvents();
            }, 100);

            // 총 건수 표시
            $('#listTotalCountSpanId').html(addComma(data.listTotalCount));
            $('.ui-pg-input').val(data.page);
        },

        // ★ 셀 저장 후 실행
        afterSaveCell: function(rowid, cellname, value){
            console.log("afterSaveCell - rowid: " + rowid + ", 수정컬럼: " + cellname);
            checkRowChanges(rowid); // 변경 여부 확인
        }
    });
}

// ★ 체크박스 클릭 이벤트 등록
function registerCheckboxEvents(){
    $('#gridList input[type="checkbox"]').off('click.customCheckbox')
    .on('click.customCheckbox', function(){
        var $checkbox = $(this);
        var $cell = $checkbox.closest('td');
        var rowid = $cell.closest('tr').attr('id');
        var colname = '';

        // 체크박스 위치의 컬럼명 찾기
        var colModel = $('#gridList').jqGrid('getGridParam', 'colModel');
        for(var i=0; i<colModel.length; i++){
            if(colModel[i].name === 'CUST_SENDMAIL_YN' || colModel[i].name === 'SALESREP_SENDMAIL_YN'){
                var $cellAtIndex = $('#gridList tr[id="' + rowid + '"] td:eq(' + i + ')');
                if($cellAtIndex.is($cell)){
                    colname = colModel[i].name;
                    break;
                }
            }
        }

        // 해당 컬럼이면 값(Y/N) 업데이트
        if(colname){
            var newValue = $checkbox.is(':checked') ? 'Y' : 'N';
            $('#gridList').jqGrid('setCell', rowid, colname, newValue);

            setTimeout(function(){
                checkRowChanges(rowid);
            }, 50);
        }
    });
}

// ★ 변경된 행 체크 후 배경색 지정
function checkRowChanges(rowid){
    var compareColumns = ["CUST_MAIN_EMAIL","SALESREP_EMAIL","COMMENTS","CUST_SENDMAIL_YN","SALESREP_SENDMAIL_YN"];
    var isChanged = false;
    var rowData = $('#gridList').jqGrid('getRowData', rowid);

    for(var i=0; i<compareColumns.length; i++){
        var col = compareColumns[i];
        var original = originalData[rowid] && originalData[rowid][col];
        var current = rowData[col];

        // 값 보정
        current = current == null ? "" : (current + "").trim();
        original = original == null ? "" : (original + "").trim();

        // Y/N 컬럼은 toYN으로 통일
        if(col.endsWith('_YN')){
            current = toYN(current);
            original = toYN(original);
        }

        if(current != original){
            isChanged = true;
            break;
        }
    }

    // 변경되면 배경색
    var $tr = $('#gridList tr[id="' + rowid + '"]');
    if(isChanged) $tr.addClass("changed-row");
    else $tr.removeClass("changed-row");
}

// Y/N값을 통일하는 함수
function toYN(val){
    if(val == null) return "";
    val = (val + "").toUpperCase().trim();
    if(["Y","YES","1","TRUE"].includes(val)) return "Y";
    if(["N","NO","0","FALSE"].includes(val)) return "N";
    return val;
}
```


***

내가 보기엔 이 흐름은 크게 **데이터 로드 → 편집 이벤트 → 변경 감지 → 배경색 표시** 구조야.
원하면 이걸 내가 **흐름도**로 정리해서 한 장 이미지로 만들어줄 수도 있어.
그렇게 할까? 그러면 이 로직을 처음 보는 사람도 5분 안에 이해할 수 있어.

