
# jqGrid Email & Checkbox 변경 감지 기능 구현 (.md 요약)

**생성일시:** 2025-08-08 08:12:57

## 🔧 개발환경
- **jqGrid:** 4.15
- **jQuery:** 3.6
- **브라우저:** IE11 및 최신 Chrome
- **백엔드:** Spring + JSP
- **제약사항:** 외부 플러그인 사용 불가

---

## ✅ 주요 구현 목표

| 항목 | 설명 |
|------|------|
| 이메일 셀 수정 | 유효성 검사 후 변경 시 줄 배경 강조 |
| 체크박스 클릭 | 'Y'/'N' 변환 후 변경 감지 및 강조 |
| 변경 원복 | 기존 값과 같아지면 강조 제거 |
| reload/page 이동 시 | 변경 상태 강조 유지 |
| 서버 연동 | 없음 (클라이언트 상태만 관리) |

---

## 🔑 주요 변수 및 로직 설명

```javascript
// 전역 변수
var changedRows = {};         // 변경된 셀 모음
var originalData = {};        // 최초 loadComplete 시 백업

// 이메일 유효성 검증
function isValidEmail(email) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}

// 체크박스 값 표준화
function normalizeYN(v) {
  return (v === true || v === "Y" || v === "y" || v === "on" || v === "true" || v === 1) ? "Y" : "N";
}

// 변경 감지 시 줄 강조
function toggleRowBg(rowid, changed, $grid) {
  $grid = $grid || $("#gridList");
  var $tr = $grid.find("tr[id='" + rowid + "']");
  if (changed) $tr.addClass("changed-row");
  else $tr.removeClass("changed-row");
}
```

---

## 📍 주요 이벤트 처리

### `loadComplete`

```javascript
loadComplete: function(data) {
  originalData = {};
  data.list.forEach(function(row) {
    originalData[row.CUST_CD] = $.extend({}, row);
  });
}
```

### `afterSaveCell`

- 이메일: 유효성 검사 후 실패 시 다시 편집 모드 진입
- 체크박스: 값 강제 변환 후 변경 감지
- 변경 시 배경색 강조

```javascript
afterSaveCell: function(rowid, colname, value, iRow, iCol) {
  if (EMAIL_COLS.includes(colname) && !isValidEmail(value)) {
    alert("유효하지 않은 이메일 형식입니다.");
    setTimeout(() => $grid.jqGrid('editCell', iRow, iCol, true), 10);
    return;
  }
  // YN 처리, 변경 감지, 강조 처리
}
```

### `beforeSelectRow`

- 체크박스 클릭 시 Y/N값 반영 및 편집모드 재진입

---

## 🎨 CSS

```css
.changed-row {
  background-color: #D1ECF1 !important;
}
.small-checkbox input[type="checkbox"] {
  width: 16px;
  height: 16px;
}
```

---

## 📄 메모

- `COMMENTS` 필드도 이메일과 동일하게 변경 감지 포함됨
- `reloadGrid()`나 `loadComplete` 후에도 강조 유지됨
