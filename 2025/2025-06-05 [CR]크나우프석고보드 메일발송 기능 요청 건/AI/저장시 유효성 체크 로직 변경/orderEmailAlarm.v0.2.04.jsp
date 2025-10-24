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

    .selected-row {
        background-color: #E8F5E8 !important;  /* 연한 초록색 - 선택된 행 */
    }

    .small-checkbox input[type="checkbox"] {
        width: 16px;
        height: 16px;
    }

    .invalid-input {
        outline: 2px solid #e57373 !important;  /* 빨간 테두리 */
        background-color: #fdecea !important;   /* 연한 빨간 배경 */
    }
</style>

< script type="text/javascript">
    // ==================================================================================
    // jqGrid Columns Order 설정
    // ==================================================================================
    var ckNameJqGrid = 'admin/customer/customerList/jqGridCookie'; // 페이지별 쿠키명 설정
    ckNameJqGrid += '/gridList'; // 그리드명별 쿠키명 설정

    var globalColumnOrderStr = toStr(decodeURIComponent(getCookie(ckNameJqGrid)));
    var globalColumnOrder = globalColumnOrderStr.split(',');

    // 기본 컬럼 모델 정의
    var defaultColModel = [
        {
            name: "CUST_CD",
            key: true,
            label: '코드',
            width: 120,
            align: 'center',
            sortable: true
        },
        {
            name: "CUST_NM",
            label: '거래처명',
            width: 220,
            align: 'left',
            sortable: true
        },
        {
            name: "CUST_MAIN_EMAIL",
            label: '담당자 이메일',
            width: 220,
            align: 'center',
            sortable: true,
            editable: true,
            editoptions: {
                maxlength: 128,
                placeholder: "example@domain.com"
            }
        },
        {
            name: "CUST_SENDMAIL_YN",
            label: '발송 여부',
            width: 100,
            align: 'center',
            sortable: true,
            editable: true,
            edittype: "checkbox",
            formatter: "checkbox",
            formatoptions: {
                disabled: false
            },
            editoptions: {
                value: "Y:N",
                defaultValue: "Y"
            },
            cellattr: function() {
                return 'class="small-checkbox"';
            }
        },
        {
            name: "SALESREP_NM",
            label: '영업 담당',
            width: 100,
            align: 'center',
            sortable: true
        },
        {
            name: "SALESREP_EMAIL",
            label: '영업 담당 이메일',
            width: 300,
            align: 'center',
            sortable: true,
            editable: true,
            editoptions: {
                maxlength: 128,
                placeholder: "example@domain.com"
            }
        },
        {
            name: "SALESREP_SENDMAIL_YN",
            label: '발송 여부',
            width: 100,
            align: 'center',
            sortable: true,
            editable: true,
            edittype: "checkbox",
            formatter: "checkbox",
            formatoptions: {
                disabled: false
            },
            editoptions: {
                value: "Y:N",
                defaultValue: "Y"
            },
            cellattr: function() {
                return 'class="small-checkbox"';
            }
        },
        {
            name: "COMMENTS",
            label: '비고',
            width: 450,
            align: 'left',
            sortable: true,
            editable: true
        }
    ];

    var defaultColumnOrder = writeIndexToStr(defaultColModel.length);
    var updateComModel = []; // 전역 변수

    // 쿠키에서 컬럼 순서 복원
    if (0 < globalColumnOrder.length) { // 쿠키값이 있을때
        if (defaultColModel.length == globalColumnOrder.length) {
            for (var i = 0, j = globalColumnOrder.length; i < j; i++) {
                updateComModel.push(defaultColModel[globalColumnOrder[i]]);
            }
            setCookie(ckNameJqGrid, globalColumnOrder, 365);
        } else {
            updateComModel = defaultColModel;
            setCookie(ckNameJqGrid, defaultColumnOrder, 365);
        }
    } else { // 쿠키값이 없을때
        updateComModel = defaultColModel;
        setCookie(ckNameJqGrid, defaultColumnOrder, 365);
    }

    // ==================================================================================
    // jqGrid Column Width 설정
    // ==================================================================================
    var ckNameJqGridWidth = ckNameJqGrid + '/width'; // 페이지별 쿠키명 설정
    var globalColumnWidthStr = toStr(decodeURIComponent(getCookie(ckNameJqGridWidth)));
    var globalColumnWidth = globalColumnWidthStr.split(',');
    var defaultColumnWidthStr = '';
    var defaultColumnWidth;
    var updateColumnWidth;

    if ('' != globalColumnWidthStr) { // 쿠키값이 있을때
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
    } else { // 쿠키값이 없을때
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

    // 컬럼 너비 적용
    if (updateComModel.length == globalColumnWidth.length) {
        for (var j = 0; j < updateComModel.length; j++) {
            updateComModel[j].width = toStr(updateColumnWidth[j]);
        }
    }

    // ==================================================================================
    // 전역 변수 및 초기화
    // ==================================================================================
    var originalData = {}; // 원본 데이터 저장용

    $(function() {
        getGridList();
    });

    // ==================================================================================
    // 그리드 데이터 로드 및 초기화 함수
    // ==================================================================================
    function getGridList() {
        // 검색조건 데이터 가져오기
        var searchData = getSearchData();

        // jqGrid 초기화
        $('#gridList').jqGrid({
            url: "${url}/admin/customer/getOrderEmailAlarmAjax.lime", // 서버 호출 URL
            editurl: 'clientArray', // 행 편집 시 서버로 전송되지 않도록 설정(로컬에서만 처리)
            datatype: "json", // 서버에서 응답받을 데이터 타입
            mtype: 'POST',    // 서버 요청 방식
            postData: searchData, // 서버로 전송할 검색 조건 데이터
            colModel: updateComModel, // 컬럼 및 속성 정의 배열
            multiselect: true,        // 체크박스 다중 선택 활성화
            cellEdit: true,           // 셀 단위 편집 허용
            cellsubmit: 'clientArray',// 편집결과를 로컬에 저장
            height: '360px',          // 그리드 높이
            autowidth: false,         // 가로 폭 자동 조정 안함
            rowNum: 10,              // 기본 페이지당 출력 행 수
            rowList: ['10', '30', '50', '100'], // 페이지당 행 수 옵션
            rownumbers: true,         // 행 번호 표시
            pagination: true,         // 페이지네이션 활성화
            pager: "#pager",          // 페이지 네비게이션 영역 ID
            actions: true,           // 기본 액션 버튼 표시 여부
            pginput: true,           // 페이지 번호 직접 입력 가능
            resizable: true,          // 열 크기 조절 가능

            // 열 순서 변경 이벤트
            sortable: {
                update: function(relativeColumnOrder) {
                    var grid = $('#gridList');

                    // 기본 컬럼 이름 배열
                    var defaultColIndicies = [];
                    for (var i = 0; i < defaultColModel.length; i++) {
                        defaultColIndicies.push(defaultColModel[i].name);
                    }

                    // 새로운 컬럼 순서 계산
                    globalColumnOrder = [];
                    var columnOrder = [];
                    var currentColModel = grid.getGridParam('colModel');

                    for (var j = 0; j < relativeColumnOrder.length; j++) {
                        // Row 번호(rn)나 Checkbox(cb) 제외
                        if ('rn' != currentColModel[j].name && 'cb' != currentColModel[j].name) {
                            columnOrder.push(defaultColIndicies.indexOf(currentColModel[j].name));
                        }
                    }
                    globalColumnOrder = columnOrder;

                    // 변경된 순서를 쿠키로 저장
                    setCookie(ckNameJqGrid, globalColumnOrder, 365);

                    // 열 너비도 함께 저장
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

            // 열 크기 조정 후 실행되는 이벤트
            resizeStop: function(width, index) {
                console.log('globalColumnOrder : ', globalColumnOrder);
                var minusIdx = 0;
                var grid = $('#gridList');
                var currentColModel = grid.getGridParam('colModel');

                // row number, row checkbox 컬럼이 맨 앞에 있으면 index 조정
                if ('rn' == currentColModel[0].name || 'cb' == currentColModel[0].name) minusIdx--;
                if ('rn' == currentColModel[1].name || 'cb' == currentColModel[1].name) minusIdx--;

                // 실제 조정된 컬럼 인덱스 계산
                var resizeIdx = index + minusIdx;

                // 변경된 너비 배열 반영
                updateColumnWidth[resizeIdx] = width;

                // 쿠키에 저장
                setCookie(ckNameJqGridWidth, updateColumnWidth, 365);
            },

            sortorder: 'desc', // 정렬 순서 기본값

            jsonReader: {
                root: 'list' // 서버 응답 JSON에서 데이터 배열 경로
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
                registerCheckboxEvents();

                // 총 건수 표시
                $('#listTotalCountSpanId').html(addComma(data.listTotalCount));
                // 현재 페이지 번호 표시
                $('.ui-pg-input').val(data.page);
            },

            // 셀 편집 저장 후 실행되는 이벤트
            afterSaveCell: function(rowid, cellname, value, iRow, iCol) {
                // 체크박스 관련 컬럼이면 이벤트 재바인딩으로 확실하게 보장
                if (cellname === 'CUST_SENDMAIL_YN' || cellname === 'SALESREP_SENDMAIL_YN') {
                    setTimeout(function() {
                        registerCheckboxEvents();
                    }, 50);
                }

                checkRowChanges(rowid); // 해당 행의 변경 감지 실행
            },

            // 행 선택 이벤트
            onSelectRow: function(rowId) {
                // multiselect 체크박스를 직접 클릭했을 때 배경색 변경
                updateRowSelectionStyle(rowId);
            },

            // 전체 선택/해제 이벤트
            onSelectAll: function(rowIdArr, status) {
                // 전체 선택/해제 시 모든 행의 배경색 업데이트
                for (var i = 0; i < rowIdArr.length; i++) {
                    updateRowSelectionStyle(rowIdArr[i]);
                }
            }
        });
    }

    // ==================================================================================
    // 체크박스 이벤트 처리
    // ==================================================================================
    function registerCheckboxEvents() {
        // 이벤트 위임 방식으로 변경 - 완벽한 해결
        $('#gridList').off('click.customCheckbox', 'input[type="checkbox"]')
            .on('click.customCheckbox', 'input[type="checkbox"]', function() {
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
                    // 체크박스 상태를 'Y'/'N'으로 setCell
                    var newValue = $checkbox.is(':checked') ? 'Y' : 'N';
                    $('#gridList').jqGrid('setCell', rowid, colname, newValue);

                    // 변경 여부 체크
                    setTimeout(function() {
                        checkRowChanges(rowid);
                    }, 50);
                }
            });
    }

    // ==================================================================================
    // 행 선택 상태에 따른 스타일 업데이트
    // ==================================================================================
    function updateRowSelectionStyle(rowid) {
        var $tr = $('#gridList tr[id="' + rowid + '"]');
        var isSelected = $('#gridList').jqGrid('getGridParam', 'selarrrow').indexOf(rowid) !== -1;
        var isChanged = $tr.hasClass('changed-row');
        
        if (isSelected) {
            // 선택된 행은 선택 배경색 적용 (변경된 행과 구분)
            if (!isChanged) {
                $tr.addClass('selected-row');
            }
        } else {
            // 선택 해제된 행은 선택 배경색 제거 (변경 배경색은 유지)
            $tr.removeClass('selected-row');
        }
    }

    // ==================================================================================
    // 행 변경 감지 및 스타일 적용
    // ==================================================================================
    function checkRowChanges(rowid) {
        // 비교 대상 컬럼 목록
        var compareColumns = ["CUST_MAIN_EMAIL", "SALESREP_EMAIL", "COMMENTS", "CUST_SENDMAIL_YN", "SALESREP_SENDMAIL_YN"];
        var isChanged = false;
        var rowData = $('#gridList').jqGrid('getRowData', rowid);

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

            // 값이 다르면 변경 발생
            if (current != original) {
                isChanged = true;
                break;
            }
        }

        // row에 변경 색상 적용 여부 반영
        var $tr = $('#gridList tr[id="' + rowid + '"]');
        var isCurrentlySelected = $('#gridList').jqGrid('getGridParam', 'selarrrow').indexOf(rowid) !== -1;
        
        if (isChanged) {
            $tr.addClass("changed-row").removeClass("selected-row");
            // 🔥 데이터가 변경되고 아직 선택되지 않았다면 선택
            if (!isCurrentlySelected) {
                $('#gridList').jqGrid('setSelection', rowid, true);
            }
        } else {
            $tr.removeClass("changed-row");
            // 🔥 데이터가 원래대로 돌아가고 현재 선택되어 있다면 선택 해제
            if (isCurrentlySelected) {
                // jqGrid에서 선택 해제하는 올바른 방법
                var $checkbox = $('#jqg_gridList_' + rowid);
                if ($checkbox.length > 0) {
                    $checkbox.prop('checked', false);
                    $('#gridList').jqGrid('resetSelection');
                    // 다른 선택된 행들은 다시 선택
                    $('.changed-row').each(function() {
                        var otherRowId = $(this).attr('id');
                        if (otherRowId && otherRowId !== rowid) {
                            $('#gridList').jqGrid('setSelection', otherRowId, true);
                        }
                    });
                }
            } else {
                // 선택되지 않은 상태에서 변경이 아니면 선택 스타일 업데이트
                updateRowSelectionStyle(rowid);
            }
        }
    }

    // ==================================================================================
    // 유틸리티 함수들
    // ==================================================================================

    // 값이 Y/N 성격일 때, 여러 표현을 통일하여 'Y' 또는 'N'으로 변환
    function toYN(val) {
        if (val === undefined || val === null) return "";
        val = (val + "").toUpperCase().trim();
        if (val === "Y" || val === "YES" || val === "1" || val === "TRUE") return "Y";
        if (val === "N" || val === "NO" || val === "0" || val === "FALSE") return "N";
        return val; // 위 조건 외 값은 그대로 반환
    }

    // 이메일 정규식(간소화)
    var EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

    // 단일 이메일 유효성 체크(공백 허용)
    function isValidEmail(v) {
        var s = (v || '').trim();
        if (s === '') return true; // 비어있으면 허용
        return EMAIL_REGEX.test(s);
    }

    // ==================================================================================
    // 데이터 처리 함수들
    // ==================================================================================

    // 저장/수정
    function dataInUp(obj, val) {
        $(obj).prop('disabled', true);

        // 선택된 행 확인 (체크박스로 선택된 행들)
        var selectedRows = $('#gridList').jqGrid('getGridParam', 'selarrrow');
        
        if (!selectedRows || selectedRows.length === 0) {
            alert("저장할 데이터를 선택해 주십시오.\n데이터를 수정하면 자동으로 선택됩니다.");
            $(obj).prop('disabled', false);
            return false;
        }

        // 폼 초기화
        var iFormObj = $('form[name="iForm"]');
        iFormObj.empty();

        var ckflag = true;
        var errorMsg = '';

        // 선택된 각 행에 대해 유효성 검사
        for (var i = 0; i < selectedRows.length; i++) {
            var rowid = selectedRows[i];
            var rowData = $('#gridList').jqGrid('getRowData', rowid);

            // 담당자 이메일 유효성 검사
            if (rowData.CUST_MAIN_EMAIL && rowData.CUST_MAIN_EMAIL.trim() !== '') {
                if (!isValidEmail(rowData.CUST_MAIN_EMAIL.trim())) {
                    errorMsg = '거래처 "' + rowData.CUST_NM + '"의 담당자 이메일 형식이 올바르지 않습니다.';
                    ckflag = false;
                    break;
                }
            }

            // 영업 담당 이메일 유효성 검사
            if (rowData.SALESREP_EMAIL && rowData.SALESREP_EMAIL.trim() !== '') {
                if (!isValidEmail(rowData.SALESREP_EMAIL.trim())) {
                    errorMsg = '거래처 "' + rowData.CUST_NM + '"의 영업 담당 이메일 형식이 올바르지 않습니다.';
                    ckflag = false;
                    break;
                }
            }

            // 메일 발송 설정 검증 - 담당자 이메일이 있는데 발송 설정이 Y인 경우
            if (rowData.CUST_SENDMAIL_YN === 'Y' && (!rowData.CUST_MAIN_EMAIL || rowData.CUST_MAIN_EMAIL.trim() === '')) {
                errorMsg = '거래처 "' + rowData.CUST_NM + '"의 담당자 이메일 발송이 Y로 설정되어 있지만 이메일 주소가 없습니다.';
                ckflag = false;
                break;
            }

            // 메일 발송 설정 검증 - 영업 담당 이메일이 있는데 발송 설정이 Y인 경우  
            if (rowData.SALESREP_SENDMAIL_YN === 'Y' && (!rowData.SALESREP_EMAIL || rowData.SALESREP_EMAIL.trim() === '')) {
                errorMsg = '거래처 "' + rowData.CUST_NM + '"의 영업 담당 이메일 발송이 Y로 설정되어 있지만 이메일 주소가 없습니다.';
                ckflag = false;
                break;
            }

            // 폼 데이터 추가
            if (ckflag) {
                iFormObj.append('<input type="hidden" name="custCd_' + i + '" value="' + (rowData.CUST_CD || '') + '" />');
                iFormObj.append('<input type="hidden" name="custMainEmail_' + i + '" value="' + (rowData.CUST_MAIN_EMAIL || '') + '" />');
                iFormObj.append('<input type="hidden" name="custSendmailYn_' + i + '" value="' + (rowData.CUST_SENDMAIL_YN || 'N') + '" />');
                iFormObj.append('<input type="hidden" name="salesrepEmail_' + i + '" value="' + (rowData.SALESREP_EMAIL || '') + '" />');
                iFormObj.append('<input type="hidden" name="salesrepSendmailYn_' + i + '" value="' + (rowData.SALESREP_SENDMAIL_YN || 'N') + '" />');
                iFormObj.append('<input type="hidden" name="comments_' + i + '" value="' + (rowData.COMMENTS || '') + '" />');
            }
        }

        // 유효성 검사 실패 시 처리
        if (!ckflag) {
            alert(errorMsg);
            $(obj).prop('disabled', false);
            return false;
        }

        // 총 처리 건수 추가
        iFormObj.append('<input type="hidden" name="totalCount" value="' + selectedRows.length + '" />');

        // 예약 발송 시간 추가 (선택되어 있다면)
        var scheduleTime = $('select[name="scheduleTime"]').val();
        var scheduleMinute = $('select[name="scheduleMinute"]').val();
        if (scheduleTime && scheduleTime !== '선택' && scheduleMinute && scheduleMinute !== '선택') {
            iFormObj.append('<input type="hidden" name="scheduleTime" value="' + scheduleTime + '" />');
            iFormObj.append('<input type="hidden" name="scheduleMinute" value="' + scheduleMinute + '" />');
        }

        console.log($(iFormObj).html());

        // 저장 확인
        if (confirm('선택된 ' + selectedRows.length + '건을 저장하시겠습니까?')) {
            var iFormData = iFormObj.serialize();
            var url = '${url}/admin/customer/updateCustomerEmailSettingsAjax.lime';
            $.ajax({
                async: false,
                data: iFormData,
                type: 'POST',
                url: url,
                success: function(data) {
                    if (data.RES_CODE == '0000') {
                        alert(data.RES_MSG);
                        // 🔥 저장 성공 후 모든 선택 상태 및 배경색 초기화
                        $('#gridList').jqGrid('resetSelection');
                        $('.changed-row').removeClass('changed-row');
                        $('.selected-row').removeClass('selected-row');
                        dataSearch(); // 데이터 다시 로드
                    } else {
                        alert(data.RES_MSG || '저장 중 오류가 발생했습니다.');
                    }
                    $(obj).prop('disabled', false);
                },
                error: function(request, status, error) {
                    alert('서버 통신 중 오류가 발생했습니다.');
                    $(obj).prop('disabled', false);
                }
            });
        } else {
            $(obj).prop('disabled', false);
        }
    }

    // 검색 데이터 수집
    function getSearchData() {
        var rl_custcd = $('input[name="rl_custcd"]').val();
        var rl_custnm = $('input[name="rl_custnm"]').val();
        var rl_salesrepnm = $('input[name="rl_salesrepnm"]').val();

        var r_salesepcY = '', r_salesepcN = ''; //영업담당자 YN
        if ($('input[name="r_salesrepcdyn"]:checked').length == 1) {
            if ($('input[name="r_salesrepcdyn"]:checked').val() == 'Y') r_salesepcN = '0';
            else r_salesepcY = '0';
        }

        var sData = {
            rl_custcd: rl_custcd,
            rl_custnm: rl_custnm,
            rl_salesrepnm: rl_salesrepnm,
            r_salesrepcdyn: r_salesepcY,
            rn_salesrepcdyn: r_salesepcN
        };
        return sData;
    }

    // 조회
    function dataSearch() {
        var searchData = getSearchData();
        $('#gridList').setGridParam({
            postData: searchData
        }).trigger("reloadGrid");
    }

    // 엑셀다운로드
    function excelDown(obj) {
        $('#ajax_indicator').show().fadeIn('fast');
        var token = getFileToken('excel');
        $('form[name="frm"]').append('<input type="hidden" name="filetoken" value="' + token + '" />');

        formPostSubmit('frm', '${url}/admin/customer/customerExcelDown.lime');
        $('form[name="frm"]').attr('action', '');

        $('input[name="filetoken"]').remove();
        var fileTimer = setInterval(function() {
            if ('true' == getCookie(token)) {
                $('#ajax_indicator').fadeOut();
                delCookie(token);
                clearInterval(fileTimer);
            }
        }, 1000);
    }

    // gridList Reload
    function reloadGridList() {
        $('#gridList').trigger('reloadGrid', [{ current: true }]); // 리로드후 현재 유지

        var gridRowId = toStr($('#gridList').getGridParam('selrow'));
        if ('' != toStr(gridRowId)) {
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

        <form name="frm" method="post">

            <!-- Page Inner -->
            <div class="page-inner">
                <div class="page-title">
                    <h3>
                        거래처현황
                        <div class="page-right">
                            <button type="button" class="btn btn-line f-black" title="새로고침" onclick="window.location.reload();">
                                <i class="fa fa-refresh"></i><em>새로고침</em>
                            </button>
                            <button type="button" class="btn btn-line f-black" title="엑셀다운로드" onclick="excelDown(this);">
                                <i class="fa fa-file-excel-o"></i><em>엑셀다운로드</em>
                            </button>
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
                                                        <input type="text" class="search-input" name="rl_custcd"
                                                               value="${param.rl_custcd}"
                                                               onkeypress="if(event.keyCode == 13){dataSearch();}" />
                                                    </div>
                                                </li>
                                                <li>
                                                    <label class="search-h">거래처명</label>
                                                    <div class="search-c">
                                                        <input type="text" class="search-input" name="rl_custnm"
                                                               value="${param.rl_custnm}"
                                                               onkeypress="if(event.keyCode == 13){dataSearch();}" />
                                                    </div>
                                                </li>
                                                <li>
                                                    <label class="search-h">영업담당</label>
                                                    <div class="search-c">
                                                        <input type="text" class="search-input" name="rl_salesrepnm"
                                                               value="${param.rl_salesrepnm}"
                                                               onkeypress="if(event.keyCode == 13){dataSearch();}" />
                                                    </div>
                                                </li>
                                                <li>
                                                    <label class="search-h">메일발송여부</label>
                                                    <div class="search-c checkbox">
                                                        <label>
                                                            <input type="checkbox" name="r_salesrepcdyn" value="Y" onclick="dataSearch();" />Y
                                                        </label>
                                                        <label>
                                                            <input type="checkbox" name="r_salesrepcdyn" value="N" onclick="dataSearch();" />N
                                                        </label>
                                                    </div>
                                                </li>
                                            </ul>
                                        </div>
                                    </div>
                                </div>

                                <div class="panel-body">
                                    <h5 class="table-title listT">
                                        TOTAL <span id="listTotalCountSpanId">0</span>EA
                                    </h5>
                                    <div class="btnList writeObjectClass">
                                        <%-- 예약발송 시각 설정 --%>
                                        <label>예약발송 시간:</label>
                                        <select name="scheduleTime">
                                            <option value="">선택</option>
                                            <option value="00">00시</option>
                                            <option value="01">01시</option>
                                            <option value="02">02시</option>
                                            <option value="03">03시</option>
                                            <option value="04">04시</option>
                                            <option value="05">05시</option>
                                            <option value="06">06시</option>
                                            <option value="07">07시</option>
                                            <option value="08">08시</option>
                                            <option value="09">09시</option>
                                            <option value="10">10시</option>
                                            <option value="11">11시</option>
                                            <option value="12">12시</option>
                                            <option value="13">13시</option>
                                            <option value="14">14시</option>
                                            <option value="15">15시</option>
                                            <option value="16">16시</option>
                                            <option value="17">17시</option>
                                            <option value="18">18시</option>
                                            <option value="19">19시</option>
                                            <option value="20">20시</option>
                                            <option value="21">21시</option>
                                            <option value="22">22시</option>
                                            <option value="23">23시</option>
                                        </select>
                                        <select name="scheduleMinute">
                                            <option value="">선택</option>
                                            <option value="00">00분</option>
                                            <option value="10">10분</option>
                                            <option value="20">20분</option>
                                            <option value="30">30분</option>
                                            <option value="40">40분</option>
                                            <option value="50">50분</option>
                                        </select>
                                        <button type="button" class="btn btn-info" onclick="dataInUp(this, '');">저장</button>
                                    </div>
                                    <div class="table-responsive in">
                                        <table id="gridList" class="display table tableList nowrap"
                                               width="100%" border="0" cellpadding="0" cellspacing="0"></table>
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