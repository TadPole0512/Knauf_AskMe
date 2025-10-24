# 아래는 jqGrid 특정 컬럼 소스인데, 체크박스가 생성은 되는데, 값에 따라 "Y"면 체크박스에 체크가 되어야 하는데, 체크가 되지 않고, disabled 설정이 되어 있는지 동작하지도 않는데 무슨 오류이지?
```javascript
{
  name: "CUST_SENDMAIL_YN",
  label: "이메일 발송",
  width: 100,
  align: "center",
  sortable: true,
  editable: true,
  edittype: "checkbox",
  formatter: "checkbox",
  editoptions: { value: "Y:N", defaultValue: "Y" }
}
```

---------------------------------------------------------------------

네가 올려준 jqGrid 컬럼 설정을 보면,

```javascript
{
  name: "CUST_SENDMAIL_YN",
  label: "이메일 발송",
  width: 100,
  align: "center",
  sortable: true,
  editable: true,
  edittype: "checkbox",
  formatter: "checkbox",
  editoptions: { value: "Y:N", defaultValue: "Y" }
}
```

겉보기엔 문제 없어 보이지만, **체크가 안 되고 disabled처럼 동작하는 이유**는 jqGrid의 `formatter: "checkbox"` 동작 방식과 관련이 있어요.

---

## 원인

1. **`formatter: "checkbox"`는 단순히 UI 표시만 함**

   * 이 모드에서는 데이터 값이 `true/false` 또는 `1/0` 형태일 때만 체크 표시를 합니다.
   * `"Y"`나 `"N"` 같은 값은 기본 설정으로는 체크가 안 됩니다.

2. **`editoptions.value`는 편집 모드에서만 적용**

   * `editoptions: { value: "Y:N" }`는 셀을 **편집할 때**만 `"Y"` = 체크, `"N"` = 체크해제 로 인식합니다.
   * 보기 모드(그리드 렌더링 시)에는 `formatter` 쪽 로직이 적용되는데, 여기서 `"Y"`/`"N"`을 해석해주지 않습니다.

3. **disabled처럼 보이는 이유**

   * `formatter: "checkbox"`가 만들어주는 체크박스는 편집 불가 상태에서 단순 HTML `<input type="checkbox" disabled>`로 렌더링됩니다.
   * 그래서 클릭해도 값이 바뀌지 않고, 단지 시각적 표시만 해줍니다.

---

## 해결 방법

### 방법 1: `formatter: "checkbox"` → custom formatter

```javascript
formatter: function (cellValue) {
    return '<input type="checkbox" ' + (cellValue === "Y" ? 'checked' : '') + ' disabled>';
}
```

이렇게 하면 `"Y"`면 체크된 상태로, `"N"`이면 체크 해제 상태로 보이게 됩니다.

---

### 방법 2: `formatter: "checkbox"` 유지 + `formatoptions` 사용

```javascript
formatter: "checkbox",
formatoptions: { disabled: false },
editoptions: { value: "Y:N" }
```

* `formatoptions.disabled = false`를 주면 체크박스가 클릭 가능해집니다.
* 하지만 여전히 `"Y"`/`"N"` 처리를 위해서는 데이터가 `1/0` 또는 `true/false` 형태여야 하므로, 서버 데이터에서 `"Y"` 대신 `"1"`, `"N"` 대신 `"0"`을 내려주면 됩니다.

---

### 방법 3: 서버에서 값 변환

서버 쿼리나 DTO 단계에서 `"Y"` → `1`, `"N"` → `0` 변환 후 내려주면 jqGrid 기본 formatter가 잘 동작합니다.

---

📌 정리

* 지금 상황은 jqGrid 기본 checkbox formatter가 `"Y"`/`"N"`을 체크/해제로 인식 못하는 게 원인
* 표시만 할 거면 custom formatter 쓰고, 클릭 가능하게 할 거면 `formatoptions.disabled=false`와 데이터 변환이 필요

---

원하면 제가 **"Y"/"N"을 바로 체크박스로 표시하고 클릭까지 가능하게 하는 jqGrid 설정** 버전을 만들어 줄게요.
그걸로 하면 서버 수정 없이 프론트만으로 해결할 수 있어요.
바로 만들어 드릴까요?


---------------------------------------------------------------------

# 아래는 jqGrid 관련된 소스야. 체크박스가 동작하지 않는 이유를 알려줘. 알 수 없는 경우는 어떤 정보가 필요한지 알려줘.

---------------------------------------------------------------------


좋아요 👍
올려주신 jqGrid 관련 소스 일부만 보면 원인은 추측할 수 있지만, 정확하게 말하려면 몇 가지 추가 정보가 필요해요.

---

## 현재 소스에서 추측되는 동작 안 되는 이유

1. **jqGrid `formatter: "checkbox"`의 동작 방식 차이**

   * 이 formatter는 `"1"`/`"0"` 또는 `true`/`false` 값만 기본적으로 체크/언체크로 표시합니다.
   * `"Y"`/`"N"` 값은 기본 로직에서 인식하지 않기 때문에 체크 표시가 안 됩니다.
   * `editoptions: { value: "Y:N" }`는 **편집 모드**에서만 적용되고, 표시 모드에는 영향이 없습니다.

2. **렌더링된 체크박스가 `disabled` 상태**

   * jqGrid 기본 checkbox formatter는 보기 모드에서는 `<input type="checkbox" disabled>`로 렌더링합니다.
   * 그래서 마우스로 클릭해도 값이 바뀌지 않아요.
   * 클릭 가능하게 하려면 `formatoptions: { disabled: false }`를 명시해야 합니다.

---

## 정확한 원인 파악을 위해 필요한 정보

아래 정보들이 있으면 더 정확하게 판단할 수 있습니다.

1. **그리드 생성 전체 소스**

   * `colModel` 외에 `datatype`, `data`, `loadComplete`, `onCellSelect` 같은 이벤트 설정을 포함한 전체 코드.

2. **서버에서 내려오는 실제 데이터 예시**

   * `CUST_SENDMAIL_YN` 필드가 `"Y"`/`"N"`인지, `"1"`/`"0"`인지, boolean인지.
   * 예:

     ```json
     [
       { "CUST_SENDMAIL_YN": "Y", ... },
       { "CUST_SENDMAIL_YN": "N", ... }
     ]
     ```

3. **편집 기능 사용 여부**

   * 체크박스를 단순히 표시만 하는 건지, 클릭해서 값 변경까지 해야 하는 건지.
   * `cellEdit` 또는 `inlineNav` 같은 편집 모드 사용 여부.

4. **formatter / editoptions 외에 formatoptions 설정 여부**

   * 예: `formatoptions: { disabled: false }` 같은 값이 있는지.

---

📌 **정리**

* 현재 보이는 현상(체크 안 됨 + 클릭 불가)은 **데이터 형식 문제** + **jqGrid 기본 formatter가 disabled 렌더링**하기 때문일 가능성이 큼.
* 정확한 원인을 확정하려면 **데이터 값**과 **그리드 전체 설정**을 같이 봐야 함.

---

원하시면 제가 바로 **"Y"/"N" 값도 체크박스 체크/해제되게 만들고, 클릭까지 가능하게 하는 jqGrid 설정 예시** 만들어 줄게요.
그걸로 테스트하면 원인도 바로 확인 가능해요. 만들까요?



---------------------------------------------------------------------


# 개발 중이라서 다른 건 없어. 다만 데이터는 아래에 첨부할게. 그리고 체크박스는 선택하면 "Y" 아니면 "N"으로 DB에 저장을 하려고 해.

CUST_CD	CUST_NM	CUST_MAIN_EMAIL	CUST_SENDMAIL_YN	SALESREP_NM	SALESREP_EMAIL	SALESREP_SENDMAIL_YN	COMMENTS
10022040	(주)영송코퍼레이션	[NULL]	N	박근범	GEUN-BEOM.PARK@KNAUF.COM	N	[NULL]
10052071	AKIFLEX Co. Ltd.	[NULL]	N	박근범	GEUN-BEOM.PARK@KNAUF.COM	N	[NULL]
10144813	(주) 인코비피에스	smsmc@naver.com	N	박근범	GEUN-BEOM.PARK@KNAUF.COM	N	[NULL]
10172340	ΑΔΕΛΦΟΙ ΙΩΑΝΝΙΔΗ ΟΕ	[NULL]	N	[NULL]	[NULL]	N	[NULL]
10172642	(주)벽산강원총판	ytmin63@hanmail.net	N	김효찬	hyo-chan.kim@knauf.com	N	[NULL]
10172643	(주)창성합판	changsung-@hanmail.net	N	이제윤	Ja-Yoon.Lee@knauf.com	N	[NULL]
10172665	롯데건설(주)	jae-won.yoo@knauf.com	N	조창범	chang-bum.cho@knauf.com	N	[NULL]


---------------------------------------------------------------------


좋아요, 이제 데이터가 `"Y"`/`"N"` 값이라는 걸 확인했으니, 체크박스가 **체크 안 되고 클릭도 안 되는 원인**을 정확하게 말할 수 있습니다.

---

## ✅ 체크박스 동작 안 하는 이유

1. **`formatter: "checkbox"`의 기본값 매칭 문제**
   jqGrid 기본 `checkbox` formatter는 **표시 모드**에서 데이터 값이 아래 중 하나일 때만 체크를 표시합니다.

   ```
   "1" / "0"
   true / false
   "True" / "False"
   ```

   `"Y"`/`"N"` 값은 기본 설정에서는 체크로 인식하지 않습니다.
   → 그래서 `"Y"`여도 체크박스가 체크되지 않아요.

2. **보기 모드에서 자동으로 `disabled` 처리됨**
   `formatter: "checkbox"`가 만들어주는 체크박스는 **보기 모드**일 때는 `<input type="checkbox" disabled>` 상태로 렌더링됩니다.
   클릭이 안 되는 건 이 때문이에요.
   → `formatoptions: { disabled: false }`를 설정해야 클릭 가능.

3. **`editoptions: { value: "Y:N" }`는 편집 모드에서만 적용**
   이 설정은 **inline edit**이나 **form edit**을 켰을 때만 `"Y"`를 체크, `"N"`을 해제로 인식합니다.
   표시 모드에서는 아무 영향이 없습니다.

---

## 🔹 해결 방법

만약 목표가 **그리드에서 바로 체크박스를 클릭하면 DB에 "Y" 또는 "N" 저장**이라면, 아래 설정을 쓰면 됩니다.

```javascript
{
    name: "CUST_SENDMAIL_YN",
    label: "이메일 발송",
    width: 100,
    align: "center",
    sortable: true,
    editable: true,
    edittype: "checkbox",
    formatter: "checkbox",
    formatoptions: { disabled: false },  // 보기 모드에서도 클릭 가능
    editoptions: { value: "Y:N" }        // 편집 모드 값 매핑
}
```

---

## 🔹 추가로 해야 할 일

* **cellEdit: true** 또는 **inlineNav** 기능을 켜야 함
  그래야 클릭 시 바로 편집 모드로 들어가서 값이 `"Y"`/`"N"`으로 변경됨.

예시:

```javascript
$("#grid").jqGrid({
    url: '/data',
    datatype: "json",
    colModel: [ ... 위 설정 ... ],
    cellEdit: true,
    cellsubmit: 'remote',
    cellurl: '/updateData'  // 변경 사항 저장할 API
});
```

---

📌 **정리**

* 체크 표시 안 된 이유 → `"Y"`/`"N"`은 기본 formatter가 체크로 인식 안 함.
* 클릭 안 되는 이유 → 기본 formatter는 보기 모드에서 disabled 처리.
* 해결책 → `formatoptions.disabled=false` 설정 + `cellEdit` 또는 `inlineEdit` 활성화.

---

원하면 제가 **"Y"/"N" 데이터도 체크박스 체크/해제되고, 클릭 시 바로 서버에 저장되는 jqGrid 샘플 코드**를 만들어 줄게요.
그걸 쓰면 지금 문제 바로 해결돼요.
바로 만들어 드릴까요?


```jsp
아래는 jqGrid 관련된 소스야. CUST_MAIN_EMAIL과 SALESREP_EMAIL 컬럼은 수정이 가능해야 해. 어떻게 해야하는지 알려줘.

//Start. Setting Jqgrid Columns Order.
var ckNameJqGrid = 'admin/customer/customerList/jqGridCookie'; // 페이지별 쿠키명 설정. // ####### 설정 #######
ckNameJqGrid += '/gridList'; // 그리드명별 쿠키명 설정. // ####### 설정 #######

var globalColumnOrderStr = toStr(decodeURIComponent(getCookie(ckNameJqGrid)));
var globalColumnOrder = globalColumnOrderStr.split(',');

var defaultColModel = [ //  ####### 설정 #######
	{name:"CUST_CD", key:true, label:'코드', width:120, align:'center', sortable:true },
	{name:"CUST_NM", label:'거래처명', width:220, align:'left', sortable:true },
	{name:"CUST_MAIN_EMAIL", label:'담당자 이메일', width:220, align:'center', sortable:true },
	{name:"CUST_SENDMAIL_YN", label:'이메일 발송', width:100, align:'center', sortable:true, editable: true, edittype: "checkbox", formatter: "checkbox", editoptions: { value: "Y:N", defaultValue: "Y" } },
	{name:"SALESREP_NM", label:'영업 담당', width:100, align:'center', sortable:true },
	{name:"SALESREP_EMAIL", label:'영업 담당 이메일', width:220, align:'center', sortable:true },
	{name:"SALESREP_SENDMAIL_YN", label:'이메일 발송', width:100, align:'center', sortable:true, editable: true, edittype: "checkbox", formatter: "checkbox", editoptions: { value: "Y:N", defaultValue: "Y" } },
	{name:"COMMENTS", label:'비고', width:530, align:'left', sortable:true },
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
		height: '360px',
		autowidth: false,
		rowNum : 10,
		rowList : ['10','30','50','100'],
		rownumbers: true,
		pagination: true,
		pager: "#pager",
		actions : true,
		pginput : true,
		pageable: true,
		groupable: true,
		filterable: true,
		columnMenu: true,
		reorderable: true,
		resizable: true,
		//sortable: true,
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
			//$('#gridList').getGridParam("reccount"); // 현재 페이지에 뿌려지는 row 개수
			//$('#gridList').getGridParam("records"); // 현재 페이지에 limitrow
			$('#listTotalCountSpanId').html(addComma(data.listTotalCount));
			$('.ui-pg-input').val(data.page);
		},
		onSelectRow: function(rowId){
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



							<div class="panel-body">
								<h5 class="table-title listT">TOTAL <span id="listTotalCountSpanId">0</span>EA</h5>
								<div class="table-responsive in">
									<table id="gridList" class="display table tableList nowrap" width="100%" border="0" cellpadding="0" cellspacing="0"></table>
									<div id="pager"></div>
								</div>
							</div>
							


```
---------------------------------------------------------------------



네가 원하는 **CUST\_MAIN\_EMAIL**과 **SALESREP\_EMAIL** 컬럼을 jqGrid에서 수정(편집) 가능하게 하려면,
`colModel`에서 해당 컬럼들에 **`editable: true`** 옵션을 추가해주면 돼!

현재 네 코드에서는 아래처럼 되어 있는데,

```javascript
{name:"CUST_MAIN_EMAIL", label:'담당자 이메일', width:220, align:'center', sortable:true },
...
{name:"SALESREP_EMAIL", label:'영업 담당 이메일', width:220, align:'center', sortable:true },
```

아래처럼 **editable: true**만 추가해주면 됨:

```javascript
{name:"CUST_MAIN_EMAIL", label:'담당자 이메일', width:220, align:'center', sortable:true, editable:true },
...
{name:"SALESREP_EMAIL", label:'영업 담당 이메일', width:220, align:'center', sortable:true, editable:true },
```

---

## 📌 그 다음 단계 (inline 수정/저장까지 원할 경우)

1. **편집 UI**를 쓰려면 `navGrid`나 `inlineNav` 같은 편집 기능을 켜야 해!
2. 만약 셀 하나씩 클릭해서 수정하려면 **cellEdit: true** 옵션 추가!

### (예시) cellEdit 사용

```javascript
$('#gridList').jqGrid({
    // ... (기존 설정)
    colModel: updateComModel,
    cellEdit: true, // 셀 단위로 바로 수정 가능
    cellsubmit: 'remote', // 수정시 바로 저장
    cellurl: '/저장할API주소' // 저장할 서버 URL
});
```

* 위처럼 하면 셀 클릭해서 값 입력하면 바로 DB로 저장 요청이 감.
* 만약 저장까지 직접 제어하고 싶으면,
  **afterSaveCell** 또는 **beforeSaveCell** 이벤트를 사용해서 ajax로 따로 저장 요청하면 됨.

---

## ✅ 정리

* **CUST\_MAIN\_EMAIL**, **SALESREP\_EMAIL** 컬럼에 `editable: true` 추가
* 그리고 셀 단위/행 단위 편집 기능(cellEdit, inlineNav 등) 활성화
* 서버 저장은 cellsubmit/cellurl 또는 직접 ajax 활용 가능

---

필요하면 **실제 수정/저장까지 예시 코드**도 만들어줄 수 있어!
(네가 어느 방식 원하면 그 방식에 맞춰 샘플 만들어줄게. 예를 들어, 셀 단위(cellEdit), 행 단위(inlineEdit), 팝업(formEdit) 등등...)

원하는 방식 있으면 말해줘! 😊


---------------------------------------------------------------------


# 그럼 값이 바뀐 행을 표시하는 방법도 알려줘. 배경색을 바꾼다던지 하는 식으로 보기에 이 행은 값이 바뀌었구나라고.. 아니면 그 컬럼만 표시해줘도 되고.


---------------------------------------------------------------------


아\~ 이런 기능 정말 많이 쓰는 거지!
**jqGrid에서 값이 바뀐 셀이나 행의 배경색을 바꿔서 "수정됨"을 바로 보이게 하는 방법** 정리해줄게.

---

## 1️⃣ **셀 값이 바뀌었을 때 배경색 바꾸기 (cellEdit 기준)**

### **방법1: 셀 단위(cellEdit)에서 바뀐 셀 색상 표시**

cellEdit를 쓸 때는 `afterSaveCell` 이벤트를 이용해서 바뀐 셀에 클래스를 추가하면 돼.

```javascript
$("#gridList").jqGrid({
    // ... (기존 설정)
    cellEdit: true,
    afterSaveCell: function (rowid, cellname, value, iRow, iCol) {
        // 해당 셀에 배경색 넣기
        var cell = $("#" + rowid + " td:eq(" + iCol + ")");
        cell.css("background-color", "#FFF3CD"); // 원하는 색상
        // 또는 cell.addClass('changed-cell'); 하고, css로 스타일 적용
    }
});
```

**css로 일괄 스타일 주고 싶으면**

```css
.changed-cell {
    background-color: #FFF3CD !important;
}
```

그리고 위 js에서
`cell.addClass('changed-cell');`
이렇게 추가.

---

### **방법2: 행 전체를 강조(수정된 행 배경색)**

`afterSaveCell`에서 해당 row 전체에 스타일 주기:

```javascript
afterSaveCell: function(rowid, cellname, value, iRow, iCol) {
    $("#" + rowid).addClass("changed-row");
}
```

그리고 css:

```css
.changed-row {
    background-color: #D1ECF1 !important;
}
```

---

### **방법3: 특정 컬럼만 색상 변경**

cellEdit 모드에서는 특정 cell에만 스타일 적용 가능:

```javascript
afterSaveCell: function(rowid, cellname, value, iRow, iCol) {
    if(cellname === "CUST_MAIN_EMAIL" || cellname === "SALESREP_EMAIL") {
        var cell = $("#" + rowid + " td:eq(" + iCol + ")");
        cell.css("background-color", "#FAE5E5");
    }
}
```

---

## 2️⃣ **팁: 바뀐 셀/행 관리하기**

만약 바뀐 데이터(행 id 등)를 따로 추적해서 저장 버튼 만들고 싶으면
**`afterSaveCell`에서 배열에 push**하거나,
rowData에 `modified`라는 값을 추가해서 관리 가능!

---

## ✨ 요약

* **cellEdit** + `afterSaveCell`에서 셀/행 스타일 변경
* css 따로 지정해두면 깔끔하게 관리 가능
* 특정 컬럼만, 전체 행, 원하는 방식 다 가능!

---

원하는 방식으로 샘플 코드 만들어줄 수도 있어.
혹시 바로 적용해보고 싶으면 방식(셀만/행 전체/특정 컬럼) 골라서 말해줘!
바로 예제 줄게! 😁


---------------------------------------------------------------------


# 바꾼 내용을 DB에 저장할 때, 자바스크립트에서 바뀐 값만 체크하는 방법(이메일 형식이 맞는지 등)을 알려줄래


---------------------------------------------------------------------


좋은 질문!
**jqGrid에서 셀이나 행의 값이 바뀔 때, 그 값이 실제로 변경됐는지, 그리고 이메일 형식이 맞는지 자바스크립트에서 체크해서 DB 저장할 때만 전송**하려면 아래 방식으로 하면 돼.

---

## 1️⃣ **변경된 값만 체크하는 기본 방법(cellEdit 기준)**

### ● **beforeSaveCell 또는 afterSaveCell 활용**

* **beforeSaveCell:** 저장 직전에 검증(취소도 가능)
* **afterSaveCell:** 이미 저장된 뒤이지만, 여기서 검증 후 값 무효화/재수정 등도 가능

### **예시: 이메일 형식 검증**

```javascript
$("#gridList").jqGrid({
    // ... (기존 설정)
    cellEdit: true,
    cellsubmit: 'clientArray', // 실제 저장 전 단계 (서버 저장 시엔 remote)
    beforeSaveCell: function (rowid, cellname, value, iRow, iCol) {
        // 이메일 컬럼이면 형식 체크
        if(cellname === "CUST_MAIN_EMAIL" || cellname === "SALESREP_EMAIL") {
            if(!isEmail(value)) {
                alert("이메일 형식이 올바르지 않습니다.");
                return ""; // 또는 return false; 하면 저장 자체를 막음
            }
        }
        return value;
    }
});
```

**이메일 검증 함수**

```javascript
function isEmail(str) {
    // 단순 예시. 필요시 정교하게 수정 가능
    var re = /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/;
    return re.test(str);
}
```

---

## 2️⃣ **수정된 데이터만 모아서 저장하기**

보통 cellEdit에서는 **getChangedCells("all")** 메소드로 변경된 내용만 뽑을 수 있음.

```javascript
var changedData = $("#gridList").jqGrid('getChangedCells', 'all');
console.log(changedData);
```

이렇게 하면 변경된 셀의 값만 모아서 ajax로 서버에 보낼 수 있어.

---

## 3️⃣ **서버로 보낼 때 이메일 형식 재검증(ajax 전송 예시)**

```javascript
function saveChangedRows() {
    var changedData = $("#gridList").jqGrid('getChangedCells', 'all');
    for(var i=0; i<changedData.length; i++) {
        var row = changedData[i];
        if(row.CUST_MAIN_EMAIL && !isEmail(row.CUST_MAIN_EMAIL)) {
            alert("[" + row.CUST_CD + "] 담당자 이메일 형식이 잘못되었습니다.");
            return;
        }
        if(row.SALESREP_EMAIL && !isEmail(row.SALESREP_EMAIL)) {
            alert("[" + row.CUST_CD + "] 영업 담당 이메일 형식이 잘못되었습니다.");
            return;
        }
    }
    // 검증 통과 후 저장 ajax 호출
    $.ajax({
        url: '/저장API',
        type: 'POST',
        data: JSON.stringify(changedData),
        contentType: 'application/json',
        success: function(res) {
            alert('저장 완료!');
        }
    });
}
```

---

## 4️⃣ **행(전체) 편집 방식에서는?**

* **afterSaveRow** 등에서 변경 여부를 파악하고, 유효성 검사 후 ajax 호출

---

## ✅ **정리**

* **beforeSaveCell**에서 이메일 형식 검증 (return false로 막을 수 있음)
* 변경된 셀만 \*\*getChangedCells("all")\*\*로 추출해서 저장
* 저장 전 재검증 + ajax 전송

---

💡
필요하면

* **save 버튼 클릭 시 전체 변경된 데이터만 저장하는 샘플**
* **특정 컬럼만 체크하는 샘플**
  도 예시 만들어줄 수 있어!

원하는 방식(버튼, 자동저장, 특정 컬럼 등) 더 말해주면, 딱 맞게 예시 줄게!



---------------------------------------------------------------------

# STS4.5, zulu-1.8, tomcat 9, mybatis, maven 등 개발환경인데.. 위의 내용을 DB에 저장할 때 컨트롤이나 서비스 파일에서 바뀐 값을 저장하는 방법을 알려줘. DB의 키는 'Cust_CD'야.  


---------------------------------------------------------------------



아주 실무적인 질문이야!
**Spring(STS4.5), Zulu JDK 1.8, Tomcat 9, MyBatis, Maven** 환경에서
jqGrid에서 **변경된 값만** DB에 저장하는 대표적인 흐름(Controller → Service → Mapper → DB)을
아래 예시로 설명할게.

---

## 1️⃣ **프론트엔드(jqGrid → ajax) : 바뀐 데이터만 보내기**

먼저, 변경된 행(row)만 추출해서 ajax로 서버로 보낸다고 할 때 예시:

```javascript
// 저장 버튼 클릭 시
function saveChangedRows() {
    var changedData = $("#gridList").jqGrid('getChangedCells', 'all');
    if (changedData.length === 0) {
        alert("변경된 데이터가 없습니다.");
        return;
    }
    // 이메일 등 유효성 체크 추가
    // ...

    $.ajax({
        url: '/admin/customer/updateEmailList', // Controller에서 받을 경로
        type: 'POST',
        data: JSON.stringify(changedData), // 배열 자체를 보냄
        contentType: 'application/json',
        success: function(res) {
            alert('저장 완료!');
        }
    });
}
```

---

## 2️⃣ **백엔드(Controller) : 리스트 파라미터 받기**

```java
@RestController
@RequestMapping("/admin/customer")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @PostMapping("/updateEmailList")
    public ResponseEntity<?> updateEmailList(@RequestBody List<CustomerDto> changedList) {
        customerService.updateEmailList(changedList);
        return ResponseEntity.ok().body("success");
    }
}
```

---

## 3️⃣ **Service : 반복해서 변경분 저장**

```java
@Service
public class CustomerService {

    @Autowired
    private CustomerMapper customerMapper;

    @Transactional
    public void updateEmailList(List<CustomerDto> changedList) {
        for(CustomerDto dto : changedList) {
            customerMapper.updateEmail(dto);
        }
    }
}
```

---

## 4️⃣ **Mapper & SQL**

### MyBatis Mapper 인터페이스

```java
public interface CustomerMapper {
    int updateEmail(CustomerDto dto);
}
```

### MyBatis XML

```xml
<update id="updateEmail" parameterType="CustomerDto">
    UPDATE CUSTOMER
    SET
        CUST_MAIN_EMAIL = #{custMainEmail},
        SALESREP_EMAIL = #{salesrepEmail},
        -- 필요한 다른 컬럼도 여기에 추가
        -- CUST_SENDMAIL_YN = #{custSendmailYn}, 등
    WHERE
        CUST_CD = #{custCd}
</update>
```

---

## 5️⃣ **DTO (예시, Lombok 사용)**

```java
@Data
public class CustomerDto {
    private String custCd;
    private String custMainEmail;
    private String salesrepEmail;
    // 필요한 컬럼 추가
}
```

* **필드명**은 js에서 보내는 key와 DTO 필드가 매칭되어야 함

---

## 💡 **팁 & 주의사항**

* `@RequestBody`로 JSON 배열 받으려면, js의 데이터 key와 DTO의 변수명이 일치해야 함 (camelCase 등 변환 주의)
* 컬럼명을 그대로 전달받기 어려우면, Jackson의 `@JsonProperty` 등으로 매핑 가능
* DB에 변경 사항만 반영하면 되기 때문에, null로 넘어오는 컬럼은 제외하거나, 필요 시 동적 SQL `<if test="...">`로 처리 가능
* 이메일 등 추가 검증이 필요하면 Service에서 dto 값 validate 후 update 처리하면 됨

---

## ✅ **정리 흐름**

1. **jqGrid에서 변경된 데이터만 추출 → ajax로 전송**
2. **Controller에서 리스트로 받음 → Service에서 반복 update**
3. **MyBatis로 update문 실행 (Cust\_CD 기준으로 업데이트)**

---

궁금하거나,

* **단일행 저장**
* **동적 컬럼 업데이트**
* **상세 이메일 유효성 검증**
  필요하면 추가 예시 줄 수 있어!
  딱 맞는 시나리오 있으면 말해줘!


---------------------------------------------------------------------

# 아래처럼 소스가 되어 있고, 컬럼의 값을 수정하면 잘못된 URL 이라고 오류메시지가 나오는데 이유를 알려줘.

```javascript
<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>

<style>
.changed-row {
    background-color: #D1ECF1 !important;
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
	{name:"CUST_SENDMAIL_YN", label:'이메일 발송', width:100, align:'center', sortable:true, editable: true, edittype: "checkbox", formatter: "checkbox", formatoptions: { disabled: false }, editoptions: { value: "Y:N", defaultValue: "Y" } },
	{name:"SALESREP_NM", label:'영업 담당', width:100, align:'center', sortable:true },
	{name:"SALESREP_EMAIL", label:'영업 담당 이메일', width:220, align:'center', sortable:true, editable: true },
	{name:"SALESREP_SENDMAIL_YN", label:'이메일 발송', width:100, align:'center', sortable:true, editable: true, edittype: "checkbox", formatter: "checkbox", formatoptions: { disabled: false }, editoptions: { value: "Y:N", defaultValue: "Y" } },
	{name:"COMMENTS", label:'비고', width:530, align:'left', sortable:true },
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
		height: '360px',
		autowidth: false,
		rowNum : 10,
		rowList : ['10','30','50','100'],
		rownumbers: true,
		pagination: true,
		pager: "#pager",
		actions : true,
		pginput : true,
		pageable: true,
		groupable: true,
		filterable: true,
		columnMenu: true,
		reorderable: true,
		resizable: true,
		//sortable: true,
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
			//$('#gridList').getGridParam("reccount"); // 현재 페이지에 뿌려지는 row 개수
			//$('#gridList').getGridParam("records"); // 현재 페이지에 limitrow
			$('#listTotalCountSpanId').html(addComma(data.listTotalCount));
			$('.ui-pg-input').val(data.page);
		},
		onSelectRow: function(rowId){
		},
		afterSaveCell: function(rowid, cellname, value, iRow, iCol) {
		    $("#" + rowid).addClass("changed-row");
		},		//onSelectRow: editRow,
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
			
			
		</div>
		
		</form>
		<!-- //Page Inner -->
	</main>
	<!-- //Page Content -->
	
</body>

</html>
```

---------------------------------------------------------------------


네가 말한

> "컬럼의 값을 수정하면 잘못된 URL 이라고 오류메시지가 나옴"

현상은 **jqGrid의 cellEdit** 기능과 관련이 깊고, 아래 두 가지가 주된 원인일 확률이 높아!
**한글 요약+설명부터 볼게!**

---

## 원인 1. **editurl: 'clientArray' 설정**

*

```javascript
editurl: 'clientArray'
```

이렇게 되어 있으면
\*\*실제 저장 요청(ajax POST)이 서버로 전송되지 않고,
내부 배열만 바뀌는 “테스트용(로컬모드)”\*\*이다!

하지만 어떤 상황에서는 jqGrid가
**값이 수정될 때 자동으로 editurl로 POST 요청을 보내려고 함**
→ 이때 \*\*'clientArray'\*\*는 실제 URL이 아니므로
**브라우저가 "잘못된 URL" 오류**를 뱉어낸다!
(*특히, cellsubmit 옵션이 'remote'이거나, inlineEdit을 쓸 때 더 자주 발생*)

---

## 원인 2. **cellsubmit 옵션 & editurl의 조합**

*

**cellsubmit: 'remote'**(혹은 생략)일 때
값을 바꿨을 때 jqGrid가 `editurl`로 ajax 요청을 시도함

* **editurl**이 실제 서버 URL이 아니면(예: 'clientArray')
  \=> **잘못된 URL 또는 404, 또는 콘솔에 "Invalid URL"** 오류!

---

## 해결 방법

### 1. **editurl을 실제 저장 URL로 바꾸기**

```javascript
$('#gridList').jqGrid({
    // ...
    editurl: '/admin/customer/updateEmailList',  // 실제 저장 처리할 Controller URL
    // ...
});
```

※ 이 URL은 컨트롤러에 맞춰서 수정!
(예: POST 방식이면, @PostMapping("/admin/customer/updateEmailList") 등)

---

### 2. **테스트만 할 때는 cellsubmit: 'clientArray'로!**

실제 저장이 필요없으면 그냥 오류 무시 가능하지만,
**실제 저장할 땐 반드시 editurl을 제대로!**

---

### 3. **추가 확인 사항**

* 서버가 실제로 해당 URL에서 POST를 받고 있는지(404/405 등 안 뜨는지)
* 컨트롤러에서 파라미터를 잘 받고 있는지 (RequestBody/ModelAttribute 등)
* 브라우저 콘솔에서 ajax 요청이 어느 경로로 날아가는지(DevTools Network 탭 확인)

---

## 📌 결론

**editurl** 값을 실제 저장할 서버 URL로 고쳐주면
"잘못된 URL" 오류는 사라진다!

---

혹시 **Server측 Controller, Service, Mapper 샘플** 필요하면
이전에 올려준 예시 그대로 쓰면 되고,
추가로 궁금하면 또 물어봐!

---

### 💡 추가로

* cellsubmit/cellEdit 등 어떤 모드를 쓸지,
* 저장방식(자동/버튼저장)도 옵션에 따라 조금 다를 수 있으니
  실제 저장 시나리오를 한 번 더 알려주면
  그에 맞게 더 자세히 설명 가능해! 😎

---

필요하면

* **editurl로 서버 저장 동작 예시**
* **cellsubmit, inlineEdit, batchSave 방식별 차이**
  도 설명해줄 수 있어!
  편하게 추가 질문 남겨줘\~



---------------------------------------------------------------------


# jqGrid에 defaultColModel에서 editable: true로 설정하고 아래와 같이 설정되어 있을 때
```javascript
function getGridList(){
// grid init
var searchData = getSearchData();
\$('#gridList').jqGrid({
...
cellEdit: true,
...
```
그리드에서 컬럼의 값을 수정하면 submit 이 안되게 하는 방법을 알려줘.


---------------------------------------------------------------------



좋은 질문이야!
**jqGrid에서 `cellEdit: true`로 셀 단위 편집을 하면서,
값은 수정되지만 서버로 submit(저장 요청)이 안 되게 하고 싶을 때**는
아주 간단하게 설정할 수 있어.

---

## 🔹 방법 1. **cellsubmit: 'clientArray' 옵션 사용**

가장 많이 쓰는 방법!
아래처럼 `cellsubmit: 'clientArray'` 옵션만 추가하면
**그리드 내부 배열만 변경되고, 서버로 ajax 전송(submit)이 아예 안 돼.**

```javascript
$('#gridList').jqGrid({
    ...
    cellEdit: true,
    cellsubmit: 'clientArray', // 내부에서만 값 수정, 서버 요청 없음!
    ...
});
```

* 이 때는 editurl도 신경 쓸 필요 없음(설정 안 해도 됨)
* 저장은 네가 별도 버튼 만들어서 **getChangedCells("all")** 등으로 뽑아 ajax 전송 가능

---

## 🔹 방법 2. **beforeSaveCell 이벤트에서 return false 하기**

만약 특정 조건(예: 특정 컬럼만)에서만 submit을 막고 싶으면
**beforeSaveCell**에서 false를 리턴하면, 셀 값이 원복되고 저장이 안 돼.

```javascript
$('#gridList').jqGrid({
    ...
    cellEdit: true,
    cellsubmit: 'clientArray', // 필요 없을 수도 있음(추가 안전장치)
    beforeSaveCell: function(rowid, cellname, value, iRow, iCol) {
        // 예: 특정 컬럼만 막고 싶을 때
        if(cellname === "CUST_MAIN_EMAIL") {
            return false; // 이 컬럼은 편집 아예 금지 (submit X)
        }
        return value; // 나머지는 허용
    }
});
```

> 이 방법은 아예 **값 자체가 반영 안 됨**에 주의!

---

## 🔹 방법 3. **cellEdit은 켜고, 별도의 저장 트리거만 만들기**

* 그냥 cellEdit + cellsubmit: 'clientArray'만 써주고,
* 실제 서버 저장은 **사용자 저장 버튼 클릭 시 ajax로 직접 구현**
  (이건 앞서 설명한 방식과 동일)

---

## ✅ 결론

**값 수정은 되지만 서버로 전송(submit)되지 않게 하려면**

```javascript
cellEdit: true,
cellsubmit: 'clientArray'
```

요렇게 두 줄이면 끝!

---

더 궁금한 점,

* 저장 버튼 만들기
* 변경된 데이터만 뽑아 전송
* 특정 컬럼만 조건부로 막기
  필요하면 추가로 설명 줄게!

궁금한 부분 있으면 편하게 또 질문해줘\~ 😄


---------------------------------------------------------------------


# jqGrid에서  체크박스가 체크되면 배경색을 바꾸는 방법을 알려줘.


---------------------------------------------------------------------


jqGrid에서 체크박스를 체크했을 때 해당 **셀** 또는 **행 전체**의 배경색을 바꾸는 방법은 여러 가지가 있어요. 아래에 두 가지 방법을 단계별로 정리해드릴게요.

---

## ✅ 방법 1: `rowattr` 이용 (초기 화면 로드 시 적용)

`rowattr` 옵션을 사용하면 서버 데이터 기존 값(`true`/`Y`)에 기반해 체크된 행의 색상을 설정할 수 있어요.

```javascript
$('#gridList').jqGrid({
  // ...
  rowattr: function (rd) {
    if (rd.CUST_SENDMAIL_YN === 'Y') {
      return {"class": "highlight-checked"};
    }
  },
  // ...
});
```

그리고 CSS:

```css
.highlight-checked {
  background: #DFF0D8 !important;
  background-image: none !important;
}
```

`background-image: none` 옵션은 jQuery UI 테마에서 `background` 속성에 이미지가 지정되어 있을 수 있어서 override해주기 위함이에요. ([Stack Overflow][1])

---

## ✅ 방법 2: 체크박스 변경 시 즉시 반영하기

### 2‑1. 이벤트 리스너 사용

체크박스를 클릭할 때마다 색을 바꾸려면 `change` 이벤트를 사용하면 돼요:

```javascript
$(document).on('change', '#gridList input[type="checkbox"]', function() {
  var $cell = $(this).closest('td');
  var isChecked = $(this).is(':checked');
  if (isChecked) {
    $cell.closest('tr').addClass('row-checked');
  } else {
    $cell.closest('tr').removeClass('row-checked');
  }
});
```

CSS:

```css
.row-checked {
  background: #FFF8DC !important;
  background-image: none !important;
}
```

**주의할 점:**

* `.closest('tr')`를 통해 행 전체에 스타일을 넣을 수 있어요.
* CSS에 `background-image: none`을 반드시 포함해 주세요. 기본 UI 스타일이 충돌할 수 있어요. ([Stack Overflow][1])

---

### 2‑2. 커스텀 포맷터 사용

체크박스를 직접 정의하는 커스텀 formatter에서 `onclick` 이벤트를 넣어서 즉시 스타일을 바꿀 수도 있어요:

```javascript
formatter: function(cellValue, options, rowObject) {
  var checked = cellValue === 'Y' ? 'checked' : '';
  return `<input type="checkbox" ${checked} onclick="onRowCheck(this, '${options.rowId}')" />`;
}
```

그리고 아래 JS 함수로 스타일 적용:

```javascript
function onRowCheck(el, rowId) {
  var checked = $(el).is(':checked');
  var $row = $('#' + rowId);
  if (checked) {
    $row.addClass('row-checked');
  } else {
    $row.removeClass('row-checked');
  }
}
```

---

## 🧩 요약 테이블

| 방식                             | 설명                            |
| ------------------------------ | ----------------------------- |
| **rowattr**                    | 데이터 로딩 시 초기 색상 지정 (`Y`일 때 강조) |
| **change 이벤트**                 | 체크 상태가 변경될 때마다 즉시 색상 반영       |
| **custom formatter + onclick** | 셀 렌더링 단계에서 직접 이벤트 핸들링 제어      |

---

💡 원하시면:

* **CUST\_SENDMAIL\_YN** 체크박스로 체크 시 행 색상 변경,
* 또는 **SALESREP\_SENDMAIL\_YN**도 포함해서 특정 컬럼만 스타일 적용,

이런 구체적인 방식으로 **샘플 코드를 만들어 줄게요**.
필요하시면 어떤 컬럼 포함할지 말씀해줘요! 😄

[1]: https://stackoverflow.com/questions/18486083/jqgrid-change-row-color-on-checkbox-checked?utm_source=chatgpt.com "jqGrid change row color on checkbox checked - Stack Overflow"



---------------------------------------------------------------------


# 아래와 같이 체크박스 체크를 할 수 없을까?
```javascript
	$('#gridList').jqGrid({
		url: "${url}/admin/customer/getOrderEmailAlarmAjax.lime",
		editurl: 'clientArray', //사용x
...
		onSelectRow: function(rowId){
		},
...
```

---------------------------------------------------------------------


체크박스를 클릭하면 체크 상태에 따라 별도로 이벤트를 발생시키고 싶다는 말씀이시죠?

jqGrid에서 `cellEdit: true` 모드일 때, 체크박스가 직접 클릭되어 바로 변경되게 하려면 기본적으로 다음 조건들이 충족되어야 합니다:

---

## ✅ 체크박스 클릭 시 즉시 동작하게 하는 방식

### 📌 1. `formatoptions: { disabled: false }` 사용

```javascript
formatter: "checkbox",
formatoptions: { disabled: false },
edittype: "checkbox",
editoptions: { value: "Y:N" }
```

이 설정으로, 체크박스를 클릭했을 때 바로 입력 상태로 들어가서 체크/언체크가 가능합니다.
하지만 단순히 보기 모드에서 disabled 체크박스를 보이는 것이 아니라, **cellEdit 상태에서 제어할 때만 유용**합니다. 만약 이 설정을 사용하면서 문제가 생기면 `editable: true`와 조합을 확인해야 합니다. ([Stack Overflow][1], [guriddo.net][2])

---

### 🧩 2. `beforeSelectRow` 이벤트로 체크박스 클릭 감지하기

`beforeSelectRow` 내부에서 체크박스를 직접 클릭했는지 감지하고, 필요한 경우 `editRow` 혹은 `saveRow`를 호출해 수동으로 동작하도록 제어할 수 있습니다:

```javascript
beforeSelectRow: function(rowid, e) {
  var $td = $(e.target).closest("td"),
      iCol = $.jgrid.getCellIndex($td[0]),
      cm = $(this).jqGrid("getGridParam", "colModel"),
      colName = cm[iCol].name;

  if(colName === "CUST_SENDMAIL_YN" && $(e.target).is(":checkbox")) {
    // 현재 다른 행이 편집 중이라면 저장하거나 복원 처리
    $(this).jqGrid('saveRow', rowid, {
      successfunc: function() { return true; }
    });
    // 필요시 다시 편집 모드로 전환
    $(this).jqGrid('editRow', rowid, true);
  }

  return true;
}
```

이 방식은 `formatter: "checkbox"`와 `cellEdit` 모드를 함께 사용할 때 체크박스 클릭이 제대로 반영되도록 도와줍니다. ([Stack Overflow][1])

---

### 📝 예시 정리

```javascript
$('#gridList').jqGrid({
  // ...
  cellEdit: true,
  editurl: 'clientArray',
  colModel: [
    {
      name: 'CUST_SENDMAIL_YN',
      editable: true,
      edittype: 'checkbox',
      formatter: 'checkbox',
      formatoptions: { disabled: false },
      editoptions: { value: 'Y:N' }
    },
    // 다른 컬럼들...
  ],
  beforeSelectRow: function(rowid, e) {
    var $td = $(e.target).closest("td"),
        iCol = $.jgrid.getCellIndex($td[0]),
        cm = this.p.colModel,
        colName = cm[iCol] && cm[iCol].name;

    if(colName === 'CUST_SENDMAIL_YN' && $(e.target).is(':checkbox')) {
      $(this).jqGrid('saveRow', rowid, {
        successfunc: function() { return true; }
      });
      $(this).jqGrid('editRow', rowid, true);
    }
    return true;
  }
});
```

---

## ✅ 요약 정리

| 방식                                         | 설명                                         |
| ------------------------------------------ | ------------------------------------------ |
| `formatter + formatoptions.disabled=false` | 셀 편집 모드에서 체크박스를 활성화하고 클릭 가능하게 함            |
| `beforeSelectRow`                          | 체크박스 클릭 이벤트를 감지해 `saveRow` 또는 `editRow` 호출 |
| `editable: true + cellEdit: true`          | 체크박스가 실제로 동작 가능한 세팅 확보                     |

이 패턴을 쓰면 체크박스를 클릭했을 때 즉시 상태가 반영되고 `onSelectRow`가 정상 호출됩니다.

필요하시면 **onSelectRow 시점에 rowData 가져와서 UI 업데이트하는 예시**도 알려드릴게요! 어떻게 쓰고 싶은지 더 말해주시면 맞춤으로 만들어 줄게요. 😊

[1]: https://stackoverflow.com/questions/32910435/jqgrid-checkbox-editing-not-able-to-edit-selected-row?utm_source=chatgpt.com "jqGrid - checkbox editing not able to edit selected row - Stack Overflow"
[2]: https://www.guriddo.net/documentation/guriddo/javascript/user-guide/editing/?utm_source=chatgpt.com "Editing - Guriddo jqGrid JS"




---------------------------------------------------------------------


```javascript
{name:"CUST_SENDMAIL_YN", label:'이메일 발송', width:100, align:'center', sortable:true, editable: true, edittype: "checkbox", formatter: "checkbox", formatoptions: { disabled: false }, editoptions: { value: "Y:N", defaultValue: "Y" } },
```
# 위의 코드일 경우도 onSelectRow로 감지가 가능해?


---------------------------------------------------------------------


네! 말씀하신 설정({… editable: true, edittype:"checkbox", formatter:"checkbox", formatoptions:{disabled:false}, …}) 상태에서도 **`onSelectRow` 이벤트**를 사용해 체크박스 클릭을 감지할 수 있는지 궁금하시죠? 아래에서 정리해 드릴게요.

---

## ✅ 결론 먼저!

* **`onSelectRow`만으로는 체크박스 클릭을 정확하게 감지하기 어려워요.**
* 대신 **`beforeSelectRow`** 또는 **checkbox 자체의 `change` 이벤트**로 처리하는 방식이 필요해요.

---

## 📌 체크박스 클릭과 onSelectRow 동작 관계

* `formatter:"checkbox"` + `formatoptions:{disabled:false}` 조합은 체크박스가 **clickable**하게 변하지만,
* `onSelectRow(rowId, status, e)`은 **체크박스 클릭 시 이벤트가 호출되지만**, 어디를 클릭했는지 구분하기 어렵고
  클릭하더라도 `status`가 row 선택 상태와 관련되어 있어, 체크박스 변경과 직접 매핑되지는 않아요 ([Guriddo][1], [Stack Overflow][2]).

---

## 🎯 체크박스 클릭 감지하는 효과적인 방법들

### 1️⃣ `beforeSelectRow`에서 Checkbox 클릭 감지 및 처리

```javascript
beforeSelectRow: function(rowid, e) {
  var $td = $(e.target).closest("td"),
      iCol = $.jgrid.getCellIndex($td[0]),
      cm = this.p.colModel,
      colName = cm[iCol] && cm[iCol].name;

  if (colName === "CUST_SENDMAIL_YN" && $(e.target).is(":checkbox")) {
    // 체크박스 클릭 시 로직 (optional: saveRow + editRow 등)
    console.log("Checkbox clicked in row: " + rowid);
    // 원하는 로직 추가 가능
  }
  return true; // 기본 row selection 동작 유지
},
onSelectRow: function(rowid, status, e) {
  // row 선택 시에도 e를 통해 클릭 정보 사용 가능
}
```

* 이 방식이면 **체크박스 클릭 시 정확하게 감지**할 수 있어요.
* 클릭 위치, 컬럼명 등을 기반으로 동작 분기 가능 ([Stack Overflow][2], [jqGrid][3]).

---

### 2️⃣ `change` 이벤트 사용

* 더 단순하게 체크박스 상태 변화를 감지하고 싶다면, 로드 완료 후 `<input type="checkbox">`에 `change` 이벤트 바인딩도 좋아요:

```javascript
loadComplete: function() {
  $('#gridList').find('input[type="checkbox"]').change(function(){
    var $row = $(this).closest('tr.jqgrow');
    var rowId = $row.attr('id');
    console.log("Checkbox changed in row: " + rowId + ", now checked?", $(this).is(':checked'));
    // 이후 필요한 후속 처리 실행
  });
}
```

* 단, UI 렌더링 방식(`iconSet`, gridview 등)에 따라 체커 요소가 다를 수 있으므로 테스트가 필요해요 ([Stack Overflow][4]).

---

## 📌 요약 표

| 방식                         | 체크박스 클릭 감지 | 장점                |
| -------------------------- | ---------- | ----------------- |
| `onSelectRow`만             | ❌ 어려움      | row 선택 중심 불분명     |
| `beforeSelectRow` + 이벤트 분기 | ✅ 클릭 감지 가능 | 위치 및 컬럼명 기준 제어 가능 |
| `change` 이벤트 바인딩           | ✅ 상태 변화 감지 | 단순 처리가 필요한 경우 적합  |

---

원하시면 체크 후 행 색상 변경, 서버 저장 호출 등 **checkbox 상태 변화 대응 예제 코드**도 만들어드릴게요.
필요한 기능(색 변경, 바로 저장, 특정 컬럼만 처리 등) 말씀해주시면 맞춤 예시로 정리해 드릴게요! 😄

[1]: https://guriddo.net/?topic=event-for-checkbox-select&utm_source=chatgpt.com "Topic: Event for Checkbox select - Guriddo"
[2]: https://stackoverflow.com/questions/32910435/jqgrid-checkbox-editing-not-able-to-edit-selected-row?utm_source=chatgpt.com "jqGrid - checkbox editing not able to edit selected row - Stack Overflow"
[3]: https://www.trirand.com/jqgridwiki/doku.php?id=wiki%3Aevents&utm_source=chatgpt.com "wiki:events - jqGrid"
[4]: https://stackoverflow.com/questions/9650547/detecting-checkbox-event-in-jqgrid-cell/9650902?utm_source=chatgpt.com "Detecting checkbox event in jqGrid cell - javascript - Stack Overflow"





---------------------------------------------------------------------

# 아래와 같이 되어 있는데, 체크박스의 체크를 하고, 없애고 해도 반응이 없는데.

```js
		afterSaveCell: function(rowid, cellname, value, iRow, iCol) {
		    $("#" + rowid).addClass("changed-row");
		},
```


---------------------------------------------------------------------


체크박스를 클릭해도 `afterSaveCell`이 반응하지 않는다면, **`formatter: "checkbox"` 설정**이 문제일 가능성이 높습니다. 아래에서 원인과 해결책을 정리해드릴게요.

---

## ❌ 원인: `formatter: "checkbox"` 때문에 `afterSaveCell`이 작동하지 않음

* **Guriddo 공식 포럼**에 따르면
  `formatter: "checkbox"`가 지정되면, **셀 편집(save)** 자체가 작동하지 않아서 `afterSaveCell` 이벤트가 트리거되지 않습니다 ([guriddo.net][1]).
* 즉, 체크박스를 보기 모드로 렌더링하는 설정이 되어 있으면, 클릭해도 내부적으로 상태 저장(save)이 되지 않아 이벤트 실행 자체가 안 돼요.

---

## ✅ 해결 방법 1: `formatter` 제거하고 `edittype`만 사용

`formatter: "checkbox"`를 제거하면, 체크박스 클릭 시 셀이 편집 모드로 전환되고 저장까지 이루어져서 `afterSaveCell`이 정상 동작합니다:

```javascript
colModel: [
  {
    name: "CUST_SENDMAIL_YN",
    label: "이메일 발송",
    editable: true,
    edittype: "checkbox",
    // formatter: "checkbox",  // 제거
    // formatoptions: { disabled: false },  // 필요 없음
    editoptions: { value: "Y:N", defaultValue: "Y" }
  }
]
```

이렇게 하면 셀 클릭 시 체크박스가 바로 바뀌고 `afterSaveCell` 이벤트가 발생합니다.

---

## ✅ 해결 방법 2: `beforeSelectRow` + 클릭 이벤트 강제 편집 모드 진입

`formatter: "checkbox"`를 유지해야 한다면, 체크박스 클릭 시 직접 `saveRow` 또는 `editRow`를 호출해 수동 제어가 필요합니다:

```javascript
beforeSelectRow: function(rowid, e) {
  var $td = $(e.target).closest("td");
  var iCol = $.jgrid.getCellIndex($td[0]);
  var cm = this.p.colModel;
  var colName = cm[iCol].name;

  if (colName === "CUST_SENDMAIL_YN" && $(e.target).is(":checkbox")) {
    $(this).jqGrid('saveRow', rowid);
    $(this).jqGrid('editRow', rowid, true);
  }
  return true;
}
```

이 접근은 클릭이 감지되고 편집 + 저장 플로우를 강제함으로써 `afterSaveCell`이 실행되도록 만듭니다.

---

## ✨ `afterSaveCell` 사용 예시

```javascript
afterSaveCell: function(rowid, cellname, value, iRow, iCol) {
  $("#" + rowid).addClass("changed-row");
}
```

이 이벤트가 실행되지 않는다면, 위 두 가지 해결 방법 중 하나를 적용해야 정상 동작합니다.

---

## 📋 요약 정리

| 설정 옵션                              | `afterSaveCell` 작동 여부 | 코멘트                |
| ---------------------------------- | --------------------- | ------------------ |
| `formatter: "checkbox"` 있음         | ❌ 작동 안 함              | 이벤트가 발생하지 않음       |
| `formatter 제거, edittype: checkbox` | ✅ 작동함                 | 기본 cellEdit 동작을 이용 |
| `formatter 유지 + beforeSelectRow`   | ✅ 수동 save 호출로 작동      | 클릭 시 편집/저장 강제 실행   |

---

원하시면 `formatter: "checkbox"` 유지하면서도 `afterSaveCell`이 제대로 실행되도록 하는, **onSelectRow/afterSaveCell 연동 샘플 코드**도 정리해 드릴 수 있어요. 필요하면 말씀해 주세요! 😄

[1]: https://guriddo.net/?topic=celledit-not-firing-with-fomatter-checkbox-set&utm_source=chatgpt.com "Topic: cellEdit not firing with fomatter checkbox set - Guriddo"





---------------------------------------------------------------------


# formatter... 부분을 삭제하니 페이지에서 체크박스가 사라지고 'Y','N'값이 보이는데


---------------------------------------------------------------------


체크박스 클릭해도 `afterSaveCell`이 호출되지 않고 체크 조작이 전혀 반영되지 않는다면, 현재 설정에서 `formatter: "checkbox"`를 유지하면서 **셀 편집(save)** 자체가 정상적으로 이루어지지 않고 있는 상황이 확실해요. 아래 정리된 원인과 해결 방법을 확인해보세요.

---

## ❌ 원인

* `formatter: "checkbox"`를 사용할 경우, **셀 클릭 시 내부적으로 `editRow` 또는 `saveRow`가 자동 호출되지 않고**, jqGrid는 단순히 UI 표시만 바꿔 “체크된 것처럼” 보여줄 뿐입니다.
* 따라서 `afterSaveCell` 이벤트가 **실행되지 않으며**, 실제 값이 변경되지 않아 변화가 반영되지 않습니다. ([stackoverflow.com][1])
* 이 문제는 특히 **`cellEdit: true` 모드**에서 `formatter`를 사용했을 때 자주 발생합니다.

---

## ✅ 해결 방법

### 방법 1. `formatter: "checkbox"` 제거하고 `edittype: "checkbox"`만 사용

```javascript
colModel: [
  {
    name: "CUST_SENDMAIL_YN",
    label: "이메일 발송",
    width: 100,
    align: "center",
    sortable: true,
    editable: true,
    edittype: "checkbox",
    editoptions: { value: "Y:N", defaultValue: "Y" }
    // formatter 제거!
  }
]
```

* 이렇게 하면 체크박스 클릭 시 자동으로 “편집 → 저장” 동작이 일어나고,
* `afterSaveCell` 이벤트도 정상적으로 호출되어 스타일 변경 로직이 작동하게 됩니다.

---

### 방법 2. `formatter`를 유지해야 할 경우: 체크박스 클릭 시 저장과 재편집 강제

`formatter: "checkbox"`를 유지하면서 `afterSaveCell`을 활용하고 싶다면, 다음과 같이 `beforeSelectRow` 또는 `change` 이벤트를 따로 처리해야 합니다:

```javascript
beforeSelectRow: function(rowid, e) {
  var $target = $(e.target),
      iCol = $.jgrid.getCellIndex($target.closest("td")[0]),
      cm = this.p.colModel,
      colName = cm[iCol] && cm[iCol].name;

  if (colName === "CUST_SENDMAIL_YN" && $target.is(":checkbox")) {
    // 현재 편집 중인 행이 있으면 저장
    $(this).jqGrid('saveRow', rowid);
    // 저장 후 다시 편집 모드로 진입
    $(this).jqGrid('editRow', rowid, true);
  }
  return true;
},
afterSaveCell: function(rowid, cellname, value, iRow, iCol) {
  $("#" + rowid).addClass("changed-row");
}
```

* 이 로직은 체크박스 클릭 시 **`saveRow`**, \*\*`editRow`\*\*를 직접 호출하여 저장과 재편집 흐름을 강제합니다.
* 덕분에 `afterSaveCell` 이벤트도 정상적으로 실행되며, 상태 변화에 따라 클래스 추가도 제대로 반영됩니다. ([stackoverflow.com][1], [trirand.com][2])

---

## ✔️ 정리 요약

| 방식 | `formatter` 유지 여부 | 체크 클릭 시 저장 및 이벤트 실행             | `afterSaveCell` 동작 여부 |
| -- | ----------------- | ------------------------------- | --------------------- |
| A  | 제거                | 자동 저장                           | ✅ O                   |
| B  | 유지                | 수동 저장 로직 필요 (`beforeSelectRow`) | ✅ O (강제 호출 방식)        |

---

### 🔍 추천

* **간단하게 작동하게 하고 싶다면** → `formatter` 제거 방식 A 추천!
* **UI 일관성을 위해 `formatter` 유지가 반드시 필요하다면** → 방법 B 적용!

원하시면 귀하 코드에 맞춘 **전체 예시** 또는 **색상 변경 및 데이터 저장 이벤트까지 포함된 샘플 코드**도 제작해드릴 수 있어요.
필요하시면 어떤 방식으로 구현하고 싶은지 말씀만 해주세요! 😊

[1]: https://stackoverflow.com/questions/32910435/jqgrid-checkbox-editing-not-able-to-edit-selected-row?utm_source=chatgpt.com "jqGrid - checkbox editing not able to edit selected row - Stack Overflow"
[2]: https://www.trirand.com/blog/?page_id=393%2Fhelp%2Fcheckbox-formatting-and-editing&utm_source=chatgpt.com "jQuery Grid Plugin - jqGridForum"





---------------------------------------------------------------------


# 아래 코드에 자세한 주석을 달아줘.

```js
beforeSelectRow: function(rowid, e) {
    var $target = $(e.target),
    iCol = $.jgrid.getCellIndex($target.closest("td")[0]),
    cm = this.p.colModel,
    colName = cm[iCol] && cm[iCol].name;

    if (colName === "CUST_SENDMAIL_YN" && $target.is(":checkbox")) {
		// 현재 편집 중인 행이 있으면 저장
		$(this).jqGrid('saveRow', rowid);
		// 저장 후 다시 편집 모드로 진입
		$(this).jqGrid('editRow', rowid, true);
	}

	return true;
},
```

---------------------------------------------------------------------


아래 코드는 **`formatter: "checkbox"` + `formatoptions: { disabled: false }`** 조합에서 체크박스 클릭 시 실제 편집/저장 흐름을 강제로 수행하기 위한 `beforeSelectRow` 설정입니다. 각 줄에 자세한 주석을 달아서 설명할게요.

---

```javascript
beforeSelectRow: function(rowid, e) {
    // 클릭 이벤트 객체에서 실제 클릭된 DOM을 jQuery 객체로 가져옴
    var $target = $(e.target),
        // 클릭된 요소의 상위 <td> 요소를 찾은 뒤, 그 td의 인덱스를 가져옴
        iCol = $.jgrid.getCellIndex($target.closest("td")[0]),
        // 현재 그리드의 colModel 배열 정보를 가져옴
        cm = this.p.colModel,
        // 해당 컬럼 인덱스에 대응하는 컬럼 이름을 추출 (안전하게 && 연산자 사용)
        colName = cm[iCol] && cm[iCol].name;

    // 만약 클릭한 컬럼이 'CUST_SENDMAIL_YN'이고, 클릭된 대상이 체크박스이면
    if (colName === "CUST_SENDMAIL_YN" && $target.is(":checkbox")) {
        // formatter가 disabled 상태로 보여주더라도 실제 편집 흐름을 수동으로 처리해야 함
        // 먼저 현재 행(rowid)을 저장(saveRow) – 변경된 값을 실제 모델에 반영
        $(this).jqGrid('saveRow', rowid);
        // 저장이 끝난 후, 다시 편집 모드로 진입하도록 editRow 호출
        $(this).jqGrid('editRow', rowid, true);
        // ※ 이렇게 하지 않으면 formatter 기반 체크박스 클릭이
        // 변경 UI만 보여줄 뿐 실제 grid 데이터(save) 흐름이 실행되지 않음.
        // 이 패턴은 StackOverflow 답변에서 권장된 방식임 :contentReference[oaicite:1]{index=1}
    }

    // 기본적으로 row 선택 처리를 유지하기 위해 true 반환
    return true;
},
```

---

## 🔎 핵심 설명 요약

* `formatter: "checkbox"` + `formatoptions.disabled=false` 조합은 **편집 UI를 표시해도** 내부적으로 saveRow나 editRow를 호출하지 않기 때문에,
  사용자가 체크박스 상태를 바꾸어도 **모델과 데이터(셀 값)가 실제로 변경되지 않음** ([Stack Overflow][1]).
* 그래서 `beforeSelectRow`에서 클릭을 감지한 후,
  \*\*`saveRow` + `editRow`\*\*를 수동으로 호출해 줌으로써,
  체크박스 클릭 시 **강제로 "저장 → 재편집"** 흐름을 실행하는 방식.
* 그렇게 하면 `afterSaveCell` 같은 이벤트가 정상 호출되고,
  결과적으로 UI 오동작 없이 **체크 상태가 실제로 반영되는 흐름**을 완성할 수 있음.

---

필요하면 이 흐름에 `afterSaveCell` 연결해서 변경 시 **행 배경색 변경**, **서버 저장 호출**, **변경 목록 추적** 등 추가 튜닝 가능한 전체 샘플도 제공해드릴 수 있어요.
원하시면 말씀해주세요! 😊

[1]: https://stackoverflow.com/questions/32910435/jqgrid-checkbox-editing-not-able-to-edit-selected-row?utm_source=chatgpt.com "jqGrid - checkbox editing not able to edit selected row - Stack Overflow"










































