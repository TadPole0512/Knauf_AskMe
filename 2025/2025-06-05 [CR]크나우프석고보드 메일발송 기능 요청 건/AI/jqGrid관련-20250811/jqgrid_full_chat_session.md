# ChatGPT 세션 정리 - jqGrid 이메일 및 체크박스 변경 감지

**생성일시**: 2025-08-08 08:14:57
**설명**: jqGrid 내에서 이메일, 체크박스, 비고 필드 수정 시 배경 강조 처리 및 변경 추적

---

## ✅ 주요 기능

- 이메일 입력 필드 (`CUST_MAIN_EMAIL`, `SALESREP_EMAIL`) 유효성 검사 및 변경 추적
- 체크박스 필드 (`CUST_SENDMAIL_YN`, `SALESREP_SENDMAIL_YN`) 클릭 이벤트 및 `Y/N` 표준화
- 비고 필드 (`COMMENTS`) 변경 시도 추적 포함
- 행 배경 색상으로 변경 감지 표시 (`changed-row`)
- reload 후에도 변경된 항목 유지

---

## 📦 주요 소스 코드

```javascript

// ✅ 전역 변수 및 보조 함수
var changedRows = {};
var originalData = {};

var EMAIL_COLS = ["CUST_MAIN_EMAIL", "SALESREP_EMAIL"];
var CHECK_COLS = ["CUST_SENDMAIL_YN", "SALESREP_SENDMAIL_YN"];
var TEXT_COLS = ["COMMENTS"]; // 추가

function isValidEmail(s) {
    return /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/.test(s);
}

function normalizeYN(v) {
    return (v === true || v === "Y" || v === "y" || v === "on" || v === "true" || v === 1) ? "Y" : "N";
}

function markChanged(rowid, col, val) {
    if (!changedRows[rowid]) changedRows[rowid] = {};
    changedRows[rowid][col] = val;
}

function unmarkChanged(rowid, col) {
    if (!changedRows[rowid]) return;
    delete changedRows[rowid][col];
    if (Object.keys(changedRows[rowid]).length === 0) {
        delete changedRows[rowid];
    }
}

function isRowChanged(rowid) {
    return !!(changedRows[rowid] && Object.keys(changedRows[rowid]).length);
}

function toggleRowBg(rowid, changed, $grid) {
    $grid = $grid || $("#gridList");
    var $tr = $grid.find("tr[id='" + rowid + "']");
    if (changed) {
        $tr.addClass("changed-row");
    } else {
        $tr.removeClass("changed-row");
    }
}

// ✅ jqGrid 옵션에서 loadComplete, afterSaveCell 등 설정
loadComplete: function(data) {
    originalData = {};
    if (data.list && data.list.length > 0) {
        data.list.forEach(function(row) {
            originalData[row.CUST_CD] = $.extend({}, row);
        });
    }
    $('#listTotalCountSpanId').html(addComma(data.listTotalCount));
    $('.ui-pg-input').val(data.page);
},

afterSaveCell: function(rowid, colname, value, iRow, iCol) {
    var $grid = $(this);
    var origRow = originalData[rowid] || {};
    var newVal = value;

    if (EMAIL_COLS.indexOf(colname) >= 0) {
        if (!isValidEmail(newVal)) {
            alert("유효하지 않은 이메일 형식입니다.");
            setTimeout(function() {
                $grid.jqGrid('editCell', iRow, iCol, true);
            }, 10);
            return;
        }
    }

    if (CHECK_COLS.indexOf(colname) >= 0) {
        newVal = normalizeYN(newVal);
        $grid.jqGrid('setCell', rowid, colname, newVal);
    }

    var origVal = (origRow[colname] != null ? origRow[colname] : "");
    if (newVal != origVal) {
        markChanged(rowid, colname, newVal);
    } else {
        unmarkChanged(rowid, colname);
    }
    toggleRowBg(rowid, isRowChanged(rowid), $grid);
},

beforeSelectRow: function(rowid, e) {
    var $target = $(e.target);
    var $grid = $(this);
    var iCol = $.jgrid.getCellIndex($target.closest("td")[0]);
    var colName = this.p.colModel[iCol] && this.p.colModel[iCol].name;

    if (CHECK_COLS.indexOf(colName) >= 0 && $target.is(":checkbox")) {
        var yn = normalizeYN($target.prop("checked"));
        $grid.jqGrid('setCell', rowid, colName, yn);

        $grid.jqGrid('saveRow', rowid);
        $grid.jqGrid('editRow', rowid, true);
    }
    return true;
},

gridComplete: function() {
    var $grid = $(this);
    $.each(changedRows, function(rowid, changedCols) {
        toggleRowBg(rowid, isRowChanged(rowid), $grid);
    });
}

// ✅ CSS (기존 포함되어 있을 수 있음)
/*
.changed-row {
    background-color: #D1ECF1 !important;
}
.small-checkbox input[type="checkbox"] {
    width: 16px;
    height: 16px;
}
*/

```

