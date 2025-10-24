
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



        <form name="iForm" method="post"></form>
        <form name="frm" method="post">


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


   - Java Controller/Service 클래스 (서버 로직)

컨트롤

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

	public List<Map<String, Object>> insertUpdateOrderEmailAlarm(Map<String, Object> svcMap) {
		return sqlSession.selectList("eorder.o_customer.insertUpdateOrderEmailAlarm", svcMap);
	}


   - MyBatis XML 매퍼 파일 (DB 쿼리)

    <update id="insertUpdateOrderEmailAlarm" parameterType="map">
        <![CDATA[
        MERGE dbo.O_CUSTOMER_MAILINFO AS T
        USING (
                 SELECT
                        #{m_custCd}               AS CUST_CD
                      , #{m_custMainEmail}        AS CUST_MAIN_EMAIL
                      , #{m_custSendmailYn}       AS CUST_SENDMAIL_YN
                      , #{m_salesrepEmail}        AS SALESREP_EMAIL
                      , #{m_salesrepSendmailYn}   AS SALESREP_SENDMAIL_YN
                      , #{m_comments}             AS COMMENTS
                      , #{m_inid}                 AS INID
                      , GETDATE()                 AS INDATE
                      , #{m_moid}                 AS MOID
                      , GETDATE()                 AS MODATE
              ) AS S
           ON T.CUST_CD = S.CUST_CD

         WHEN MATCHED THEN
              UPDATE SET
                     T.CUST_MAIN_EMAIL       = S.CUST_MAIN_EMAIL
                   , T.CUST_SENDMAIL_YN      = S.CUST_SENDMAIL_YN
                   , T.SALESREP_EMAIL        = S.SALESREP_EMAIL
                   , T.SALESREP_SENDMAIL_YN  = S.SALESREP_SENDMAIL_YN
                   , T.COMMENTS              = S.COMMENTS
                   , T.MOID                  = S.MOID
                   , T.MODATE                = S.MODATE

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
	CUST_CD    VARCHAR(10),
	CUST_MAIN_EMAIL    VARCHAR(100),
	CUST_SENDMAIL_YN    VARCHAR(1),
	SALESREP_EMAIL    VARCHAR(100),
	SALESREP_SENDMAIL_YN VARCHAR(1),
	COMMENTS	VARCHAR(2000),
	INID    VARCHAR(10),
	INDATE    DATETIME2,
	MOID    VARCHAR(10),
	MODATE    DATETIME2,
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

안녕하세요, IT 컨설턴트입니다. 주신 코드를 꼼꼼히 검토해 보니, 말씀하신 4가지 문제에 대한 원인과 해결 방안을 찾았습니다. 전반적으로 jQuery와 jQGrid를 활용한 클라이언트 측 로직이 복잡하게 얽혀 있고, 서버로 데이터를 전송하는 방식에도 개선이 필요한 부분이 보입니다. 하나씩 차근차근 짚어볼게요. 🔎

-----

### **1. 이메일 유효성 검사 경고창 무한 반복 문제**

**근본 원인 및 문제 코드**
`dataSave()` 함수 내에서 이메일 유효성 검사(`validateEmail()`)를 하고 있습니다.

```javascript
// 문제가 되는 부분
if (rowData.CUST_MAIN_EMAIL && !validateEmail(rowData.CUST_MAIN_EMAIL)) {
    alert('...');
    validationFailed = true;
    return false; // jQuery.each() 루프에서 false를 반환하면 루프만 중단됨
}
```

여기서 \*\*`return false;`\*\*는 `$.each()` 루프를 즉시 중단하는 역할만 할 뿐, `dataSave()` 함수 자체를 종료시키지는 않습니다. 루프가 중단된 후, 다음 `if (validationFailed)` 조건문에서 `validationFailed`가 `true`이므로 `alert()`를 다시 띄우고 `return false;`를 실행합니다. 즉, 유효성 검사에 실패한 레코드가 여러 개일 경우, 첫 번째 실패 레코드를 발견하고 루프를 중단했지만, `dataSave()` 함수 자체는 계속 진행되면서 `if (validationFailed)` 블록으로 진입하여 다시 `alert`를 띄우고 함수를 종료시키는 구조입니다. 이 때문에 경고창이 한 번 더 나타나는 것으로 보입니다.

**수정 방법**
유효성 검사 실패 시 `$.each()` 루프를 즉시 중단하고, `dataSave()` 함수도 함께 종료되도록 로직을 수정해야 합니다. 간단하게는 `validationFailed` 변수를 `true`로 설정하는 즉시 `return false;`를 사용하여 루프를 멈추고, 루프 종료 후 `validationFailed`가 `true`일 경우에만 알림을 띄우고 함수를 종료하도록 명확하게 분리하는 것이 좋습니다.

**수정된 코드**

```javascript
// handleEmailChange 함수는 그대로 두고, dataSave 함수만 수정
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
        return;
    }

    var validationFailed = false;
    var failMessage = '';

    $.each(modifiedRows, function(i, rowData) {
        if (rowData.CUST_MAIN_EMAIL && !validateEmail(rowData.CUST_MAIN_EMAIL)) {
            failMessage = '거래처 담당자 이메일 형식이 올바르지 않습니다. (거래처코드: ' + rowData.CUST_CD + ')';
            validationFailed = true;
            return false; // $.each 루프 중단
        }
        if (rowData.SALESREP_EMAIL && !validateEmail(rowData.SALESREP_EMAIL)) {
            failMessage = '영업 담당 이메일 형식이 올바르지 않습니다. (거래처코드: ' + rowData.CUST_CD + ')';
            validationFailed = true;
            return false;
        }
        if (rowData.CUST_SENDMAIL_YN === 'Y' && !rowData.CUST_MAIN_EMAIL) {
            failMessage = '담당자 이메일 발송이 체크되어 있지만 담당자 이메일이 비어있습니다. (거래처코드: ' + rowData.CUST_CD + ')';
            validationFailed = true;
            return false;
        }
        if (rowData.SALESREP_SENDMAIL_YN === 'Y' && !rowData.SALESREP_EMAIL) {
            failMessage = '영업 담당 이메일 발송이 체크되어 있지만 영업 담당 이메일이 비어있습니다. (거래처코드: ' + rowData.SALESREP_CD + ')';
            validationFailed = true;
            return false;
        }
    });

    if (validationFailed) {
        alert(failMessage);
        $(obj).prop('disabled', false);
        return; // dataSave 함수 종료
    }
    // ... 나머지 저장 로직
}
```

-----

### **2. 데이터 저장 불완전 문제**

**근본 원인 및 문제 코드**
`dataSave()` 함수 내에서 `getModifiedRows()` 함수를 호출하여 수정된 데이터를 가져오고 있습니다.

```javascript
// 문제가 되는 부분 (dataSave.js)
iFormObj.append('<input type="hidden" name="custMainEmail" value="' + (rowData.CUST_MAIN_EMAIL || '') + '" />');
iFormObj.append('<input type="hidden" name="salesrepEmail" value="' + (rowData.SALESREP_EMAIL || '') + '" />');
iFormObj.append('<input type="hidden" name="comments" value="' + (rowData.COMMENTS || '') + '" />');
iFormObj.append('<input type="hidden" name="custSendmailYn" value="' + (rowData.CUST_SENDMAIL_YN || 'N') + '" />');
iFormObj.append('<input type="hidden" name="salesrepSendmailYn" value="' + (rowData.SALESREP_SENDMAIL_YN || 'N') + '" />');
```

이 코드는 **`getModifiedRows()`** 함수가 리턴하는 `rowData` 객체의 데이터에 의존합니다. `getModifiedRows()` 내부에서는 `jqGrid('getRowData')`를 사용하는데, 이 함수는 그리드의 **셀에 표시된 HTML 값**을 가져옵니다. 텍스트 필드(이메일, 비고)의 경우, **인풋 태그 자체가 아닌, `jqGrid('saveRow')`로 저장된 셀의 텍스트 값**을 가져와야 합니다. 현재 코드의 `getCleanRowData()` 함수는 이 문제를 부분적으로 해결하려 했지만, 로직이 완벽하지 않습니다.

**수정 방법**

1.  **`getModifiedRows()`** 로직을 수정하여 `jqGrid('getRowData')` 사용 시 **editable 필드의 실제 입력값**을 정확히 가져오도록 개선해야 합니다.
2.  `getModifiedRows()` 함수에서 `jqGrid('saveRow', rowId)`를 호출할 때 **`successfunc`** 옵션을 활용하여 저장된 데이터를 직접 확인하고, 이를 `modifiedRows` 배열에 푸시하는 방식으로 로직을 단순화할 수 있습니다.
3.  또는 `jqGrid('getChangedCells', 'all')` 같은 함수를 사용하여 변경된 데이터만 가져오는 방법을 고려할 수 있습니다. 다만, 기존 코드의 `hasOtherModifications` 로직을 최대한 유지하며 수정하는 것이 안전합니다.

**수정된 코드**
`getModifiedRows()` 함수를 아래와 같이 개선하여, `jqGrid('saveRow')`로 셀 데이터를 정상적으로 가져오도록 만듭니다.

```javascript
// 수정된 행 데이터 가져오기 - 편집모드 대응 완전 재작성
function getModifiedRows() {
    var grid = $('#gridList');
    var selectedRows = grid.jqGrid('getGridParam', 'selarrrow');
    var allRowIds = grid.jqGrid('getDataIDs');
    var modifiedRows = [];
    
    // 전체 행을 순회하며 수정된 행을 찾음
    $.each(allRowIds, function(index, rowId) {
        var isEditMode = $('#' + rowId).hasClass('jqgrow-edit');
        if (isEditMode) {
            // 편집 모드인 경우, 일단 저장
            grid.jqGrid('saveRow', rowId, {
                // 저장 성공 시 데이터를 가져와 배열에 추가
                successfunc: function(response) {
                    var rowData = grid.jqGrid('getRowData', rowId);
                    modifiedRows.push(rowData);
                    return true;
                }
            });
        } else if (modifiedRowsSet.has(rowId)) {
            // 편집 모드가 아닌 경우(체크박스만 변경 등), Set에 있는 ID의 데이터를 가져옴
            var rowData = grid.jqGrid('getRowData', rowId);
            modifiedRows.push(rowData);
        }
    });
    return modifiedRows;
}
```

**`dataSave()`** 함수 내에서 `getModifiedRows()` 호출 전에 전체 행에 대해 `jqGrid('saveRow')`를 호출하는 방식은 문제가 될 수 있습니다. 이는 `getModifiedRows()` 내부에서 이미 `saveRow`를 호출하고 있기 때문입니다. 중복 호출을 피하고, `getModifiedRows()`가 정확한 데이터를 반환하도록 위와 같이 로직을 수정하는 것이 좋습니다.

-----

### **3. 체크박스 상태 동기화 오류**

**근본 원인 및 문제 코드**
`dataSave()` 함수 내에서 `iFormObj`에 hidden input을 추가하는 부분입니다.

```javascript
// 문제가 되는 부분 (dataSave.js)
$.each(modifiedRows, function(i, rowData) {
    // ...
    iFormObj.append('<input type="hidden" name="custSendmailYn" value="' + (rowData.CUST_SENDMAIL_YN || 'N') + '" />');
    // ...
    iFormObj.append('<input type="hidden" name="salesrepSendmailYn" value="' + (rowData.SALESREP_SENDMAIL_YN || 'N') + '" />');
});
```

그리고 서버 측 **`insertUpdateOrderEmailAlarm`** 서비스 로직은 다음과 같습니다.

```java
// 문제가 되는 부분 (Java Service)
String[] custSendmailYnArray = req.getParameterValues("custSendmailYn");
String[] salesrepSendmailYnArray = req.getParameterValues("salesrepSendmailYn");

//...
String custSendmailYn = (custSendmailYnArray != null && i < custSendmailYnArray.length) ? custSendmailYnArray[i] : "N";
String salesrepSendmailYn = (salesrepSendmailYnArray != null && i < salesrepSendmailYnArray.length) ? salesrepSendmailYnArray[i] : "N";
```

여기서 문제는 클라이언트에서 `getModifiedRows()` 함수가 반환하는 **`modifiedRows`** 배열에 있습니다. `hasOtherModifications()` 함수를 호출하는데, 이 함수는 텍스트 필드(`CUST_MAIN_EMAIL`, `SALESREP_EMAIL`, `COMMENTS`)에 변경이 있을 경우만 \*\*`isModified || hasModifications`\*\*가 `true`가 되도록 로직이 짜여 있습니다. `hasOtherModifications()` 함수 내에서 체크박스 변경 여부를 제대로 추적하지 못하고 있습니다. 또한 `jqGrid('setSelection', rowId, false)` 로직 때문에 수정된 행이 선택 해제되면서 `getModifiedRows()`가 예상치 못한 결과를 반환할 수 있습니다.

**수정 방법**

1.  **`hasOtherModifications()`** 함수를 수정하여 체크박스 값도 원본 데이터와 비교하도록 명확히 해야 합니다.
2.  **`handleCheckboxClick()`** 함수 내에서 `syncRowState()`를 호출할 때 `isModified || hasModifications` 로직을 사용하는데, 이 로직이 잘못 작동하여 수정된 행이 `modifiedRowsSet`에 제대로 추가되지 않는 경우가 발생할 수 있습니다.
3.  가장 확실한 해결책은 `dataSave()` 함수 내에서 `modifiedRows` 배열을 만들 때 \*\*`modifiedRowsSet`\*\*에 추가된 모든 행 ID를 기반으로 데이터를 가져와야 합니다.

**수정된 코드**
`handleCheckboxClick()` 함수와 `hasOtherModifications()` 함수를 수정하여 체크박스 변경도 정확하게 감지하도록 합니다.

```javascript
// 체크박스 클릭 이벤트 핸들러 - 완전 재작성
function handleCheckboxClick(checkbox) {
    var rowId = $(checkbox).data('rowid');
    var fieldName = $(checkbox).data('field');
    var newValue = checkbox.checked ? 'Y' : 'N';
    var originalValue = originalDataMap[rowId] ? (originalDataMap[rowId][fieldName] || 'N') : 'N';
    
    // jqGrid에 값 직접 설정
    $('#gridList').jqGrid('setCell', rowId, fieldName, newValue);
    
    // 변경 상태 감지
    var isModified = (newValue !== originalValue);
    if (isModified) {
        modifiedRowsSet.add(rowId);
        syncRowState(rowId, true);
    } else {
        modifiedRowsSet.delete(rowId);
        syncRowState(rowId, hasOtherModifications(rowId));
    }
}
```

```javascript
// 행에 다른 수정사항이 있는지 확인 - 편집모드 대응 개선
function hasOtherModifications(rowId) {
    if (!originalDataMap[rowId]) return false;
    var grid = $('#gridList');
    var currentData = getCleanRowData(rowId); // 이 함수가 정확한 데이터를 가져와야 함
    var originalData = originalDataMap[rowId];
    
    var editableFields = ['CUST_MAIN_EMAIL', 'SALESREP_EMAIL', 'COMMENTS'];
    // 텍스트 필드 변경 확인
    for (var i = 0; i < editableFields.length; i++) {
        var field = editableFields[i];
        var currentValue = (currentData[field] || '').toString().trim();
        var originalValue = (originalData[field] || '').toString().trim();
        if (currentValue !== originalValue) {
            return true;
        }
    }
    
    // 체크박스 필드 변경 확인
    if (currentData.CUST_SENDMAIL_YN !== (originalData.CUST_SENDMAIL_YN || 'N')) return true;
    if (currentData.SALESREP_SENDMAIL_YN !== (originalData.SALESREP_SENDMAIL_YN || 'N')) return true;

    return false;
}
```

-----

### **4. 포커스 이동 오류**

**근본 원인 및 문제 코드**
`jqGrid`의 `onSelectRow` 이벤트 핸들러와 `editRow()` 함수에 원인이 있습니다.

```javascript
// 문제가 되는 부분 (onSelectRow)
onSelectRow: function(rowId, status, e){
    if (e && $(e.target).is('input[type="checkbox"]') && $(e.target).closest('td').hasClass('cbox')) {
        //...
        return;
    }
    editRow(rowId);
}
```

```javascript
// 문제가 되는 부분 (editRow)
grid.jqGrid('editRow', id, {
    keys: true, 
    focusField: true, // 이 옵션이 문제
    oneditfunc: function(rowId) {
        //...
    }
});
```

`onSelectRow` 이벤트는 행의 어떤 부분을 클릭하든 발생합니다. `jqGrid('editRow')`의 **`focusField: true`** 옵션은 편집 모드로 전환될 때 **첫 번째 editable 필드**에 자동으로 포커스를 이동시킵니다. 현재 그리드의 첫 번째 editable 필드는 `CUST_MAIN_EMAIL`입니다. 따라서 `비고`나 `SALESREP_EMAIL` 필드를 클릭해도, `editRow`가 호출되면서 \*\*`CUST_MAIN_EMAIL`\*\*로 포커스가 강제로 이동하는 현상이 발생합니다.

**수정 방법**
`editRow()` 함수에서 `focusField: true` 옵션을 사용하지 않거나, 클릭한 필드에 맞게 동적으로 포커스를 설정해야 합니다. 사용자가 클릭한 필드의 이름을 `onSelectRow` 이벤트에서 감지하여 `editRow`에 전달하는 방법이 가장 좋습니다.

**수정된 코드**
`onSelectRow`에서 클릭된 요소가 editable 필드인지 확인하고, 해당 필드에 포커스를 맞춥니다.

```javascript
onSelectRow: function(rowId, status, e){
    if (e && $(e.target).is('input[type="checkbox"]')) {
        // 체크박스 클릭 시
        setTimeout(function() {
            var hasModifications = hasOtherModifications(rowId);
            if (!hasModifications) {
                $('#gridList').jqGrid('setSelection', rowId, false);
            }
        }, 10);
        return;
    }
    
    // 행 선택 시
    var grid = $('#gridList');
    var isEditableField = $(e.target).closest('td').hasClass('editable');
    var fieldName = '';
    
    if (isEditableField) {
        // 클릭한 필드명 가져오기
        var cellIndex = $(e.target).closest('td')[0].cellIndex;
        fieldName = grid.jqGrid('getGridParam', 'colModel')[cellIndex].name;
    }

    if (rowId && rowId !== lastSelection) {
        grid.jqGrid('editRow', rowId, {
            keys: true,
            // focusField를 동적으로 설정하거나 false로 변경
            focusField: fieldName || false,
            oneditfunc: function(rowId) {
                // ... (기존 onblur 이벤트 바인딩 로직은 유지)
            }
        });
        lastSelection = rowId;
    }
}
```

**`onSelectRow`** 함수는 사용자가 **어떤 필드를 클릭했는지** 판단하고, 그 필드의 이름을 `editRow`에 전달하여 `focusField` 옵션으로 사용하게 함으로써 원하는 필드에 정확히 포커스가 이동하도록 만들 수 있습니다.

-----

### **최종 수정된 전체 코드**

아래는 위에서 설명한 모든 문제에 대한 수정 사항이 적용된 전체 코드입니다.

```javascript
// ==================================================================================
// 전역 변수 및 원본 데이터 저장
// ==================================================================================
var originalDataMap = {};
var modifiedRowsSet = new Set();
var lastSelection;

// 이메일 형식 유효성 검사 함수
function validateEmail(email) {
    if (!email || email.trim() === '') return true;
    const emailRegex = /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    return emailRegex.test(email.trim());
}

// 배경색 및 multiselect 동기화 함수
function syncRowState(rowId, isModified) {
    var grid = $('#gridList');
    if (isModified) {
        $('#gridList #' + rowId).css('background-color', '#ffebcd');
        grid.jqGrid('setSelection', rowId, true); // 수정: 체크박스도 선택되도록 변경
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

// 체크박스 클릭 이벤트 핸들러
function handleCheckboxClick(checkbox) {
    var rowId = $(checkbox).data('rowid');
    var fieldName = $(checkbox).data('field');
    var newValue = checkbox.checked ? 'Y' : 'N';
    var originalValue = originalDataMap[rowId] ? (originalDataMap[rowId][fieldName] || 'N') : 'N';

    $('#gridList').jqGrid('setCell', rowId, fieldName, newValue);
    
    var isModified = (newValue !== originalValue);
    if (isModified) {
        modifiedRowsSet.add(rowId);
        syncRowState(rowId, true);
    } else {
        modifiedRowsSet.delete(rowId);
        syncRowState(rowId, hasOtherModifications(rowId));
    }
}

// 이메일 필드 검증 및 상태 동기화 - 편집모드 개선
function handleEmailChange(input) {
    var rowId = $(input).closest('tr').attr('id');
    var fieldName = $(input).attr('name') || $(input).data('field');
    var newValue = $(input).val().trim();
    if (!validateEmail(newValue)) {
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
            grid.jqGrid('setSelection', rowId, true); // 수정: 수정사항이 있으면 선택 유지
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
    var modifiedRows = [];
    
    // modifiedRowsSet에 있는 모든 ID에 대해 처리
    var rowIdsToProcess = Array.from(modifiedRowsSet);
    $.each(rowIdsToProcess, function(index, rowId) {
        var isEditMode = $('#' + rowId).hasClass('jqgrow-edit');
        if (isEditMode) {
            // 편집 모드인 경우, 먼저 저장
            grid.jqGrid('saveRow', rowId, {
                // 저장 성공 시 데이터를 가져와 배열에 추가
                successfunc: function(response) {
                    var rowData = grid.jqGrid('getRowData', rowId);
                    modifiedRows.push(rowData);
                    return true;
                }
            });
        } else {
            // 편집 모드가 아닌 경우, 데이터를 가져와 배열에 추가
            var rowData = getCleanRowData(rowId);
            modifiedRows.push(rowData);
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
                var isChecked = $temp.find('input[type="checkbox"]').is(':checked');
                if ($temp.find('input').length > 0) {
                     cleanData[key] = $temp.find('input').val() || '';
                } else if ($temp.find('a').length > 0) {
                     cleanData[key] = $temp.find('a').text().trim();
                } else {
                     cleanData[key] = $temp.text().trim();
                }
                if ($temp.find('input[type="checkbox"]').length > 0) {
                    cleanData[key] = isChecked ? 'Y' : 'N';
                }
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

function editRow(id, focusField) {
    if (id && id !== lastSelection) {
        var grid = $('#gridList');
        grid.jqGrid('editRow', id, {
            keys: true,
            focusField: focusField, // 수정된 부분
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
    
    // getModifiedRows 함수를 호출하여 수정된 데이터 가져오기
    var modifiedRows = getModifiedRows();
    
    if (modifiedRows.length === 0) {
        alert('수정된 내용이 없습니다.');
        $(obj).prop('disabled', false);
        return;
    }
    
    var validationFailed = false;
    var failMessage = '';
    
    $.each(modifiedRows, function(i, rowData) {
        if (rowData.CUST_MAIN_EMAIL && !validateEmail(rowData.CUST_MAIN_EMAIL)) {
            failMessage = '거래처 담당자 이메일 형식이 올바르지 않습니다. (거래처코드: ' + rowData.CUST_CD + ')';
            validationFailed = true;
            return false;
        }
        if (rowData.SALESREP_EMAIL && !validateEmail(rowData.SALESREP_EMAIL)) {
            failMessage = '영업 담당 이메일 형식이 올바르지 않습니다. (거래처코드: ' + rowData.CUST_CD + ')';
            validationFailed = true;
            return false;
        }
        if (rowData.CUST_SENDMAIL_YN === 'Y' && !rowData.CUST_MAIN_EMAIL) {
            failMessage = '담당자 이메일 발송이 체크되어 있지만 담당자 이메일이 비어있습니다. (거래처코드: ' + rowData.CUST_CD + ')';
            validationFailed = true;
            return false;
        }
        if (rowData.SALESREP_SENDMAIL_YN === 'Y' && !rowData.SALESREP_EMAIL) {
            failMessage = '영업 담당 이메일 발송이 체크되어 있지만 영업 담당 이메일이 비어있습니다. (거래처코드: ' + rowData.CUST_CD + ')';
            validationFailed = true;
            return false;
        }
    });

    if (validationFailed) {
        alert(failMessage);
        $(obj).prop('disabled', false);
        return;
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
            if (e && $(e.target).is('input[type="checkbox"]')) { // 수정: checkbox 체크박스 클릭시에도 이벤트 발생하도록 수정
                setTimeout(function() {
                    var hasModifications = hasOtherModifications(rowId);
                    if (!hasModifications) {
                        $('#gridList').jqGrid('setSelection', rowId, false);
                    }
                }, 10);
                return;
            }

            var grid = $('#gridList');
            var cellIndex = $(e.target).closest('td')[0].cellIndex;
            var colModel = grid.jqGrid('getGridParam', 'colModel');
            var fieldName = colModel[cellIndex].name;
            
            editRow(rowId, fieldName); // 수정: 클릭한 필드명 전달
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
```