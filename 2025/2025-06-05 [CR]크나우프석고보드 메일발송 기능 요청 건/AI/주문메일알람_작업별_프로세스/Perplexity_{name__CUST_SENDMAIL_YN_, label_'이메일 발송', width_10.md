<img src="https://r2cdn.perplexity.ai/pplx-full-logo-primary-dark%402x.png" class="logo" width="120"/>

# {name:"CUST_SENDMAIL_YN", label:'이메일 발송', width:100, align:'center', sortable:true, editable: true, edittype: "checkbox", editoptions: { value: "Y:N", defaultValue: "Y" } },

위의 소스는 jqGrid 소스로 컬럼에 체크박스가 생성되지 않는데 이유를 알려줘.

주어진 소스처럼 jqGrid의 colModel에서 아래와 같이 컬럼을 설정했는데도 체크박스가 보이지 않는 원인은 여러 가지가 있을 수 있습니다.

```javascript
{
  name: "CUST_SENDMAIL_YN",
  label: "이메일 발송",
  width: 100,
  align: "center",
  sortable: true,
  editable: true,
  edittype: "checkbox",
  editoptions: { value: "Y:N", defaultValue: "Y" }
}
```

**주요 원인 및 해결 방법:**

- **1. formatter가 지정되지 않은 경우**
colModel에서 체크박스를 보이게 하려면 단순히 edittype만으로는 부족할 수 있습니다.
일반적으로 체크박스 컬럼에는 `formatter: "checkbox"` 옵션도 함께 넣어야 실제로 체크박스가 렌더링됩니다.

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

이 옵션을 추가하지 않으면 실제 조회 상태(cell view mode)에서는 단순 텍스트(`Y` 또는 `N`)로만 표시될 수 있습니다[^1][^2].
- **2. 데이터 값이 Y/N, Yes/No와 불일치**
editoptions의 value에서 정의한 값(`Y:N`)과 실제 데이터의 값이 다르면 체크 여부가 올바로 반영되지 않거나 표시되지 않을 수 있습니다.
예를 들어, 실제 데이터가 `"true"`/`"false"`거나, `"1"`/`"0"`이면 `"Y:N"` 설정으로 제대로 표시가 안됩니다.
→ 데이터와 editoptions.value의 쌍을 맞춰 주세요.
- **3. 편집(수정) 모드에서만 체크박스 생성**
edittype은 "edit"모드에서만 의미가 있어서, 일반 표시 상태(뷰 상태)에서는 단순 값으로만 나옵니다.
항상 체크박스가 보이게 하려면 **formatter**도 함께 지정해야 합니다.
- **4. loadOnce, datatype, data 구조 오류**
데이터가 정상적으로 표시되지 않을 때도 체크박스가 생성되지 않을 수 있습니다. 데이터가 잘 전달되는지도 확인해 주세요.

**요약**

- 체크박스를 컬럼에 항상 보이게 하려면 `formatter: "checkbox"`를 colModel에 반드시 추가해야 합니다.
- editoptions.value와 실제 데이터 값의 일치도 확인하세요.
- edittype만 추가하면 수정(편집) 모드에서만 체크박스가 나타납니다.
- 그 외 데이터 전달이 정상인지도 점검해 보세요[^1][^2].

이 부분들을 확인하시면 대부분의 경우 체크박스가 정상적으로 보이게 됩니다.

<div style="text-align: center">⁂</div>

[^1]: http://www.trirand.com/blog/?page_id=393%2Fhelp%2Fjqgrid-with-an-editable-checkbox-column

[^2]: https://forum.jquery.com/portal/en/community/topic/jquery-jqgrid-with-an-editable-checkbox-column

[^3]: https://stackoverflow.com/questions/32910435/jqgrid-checkbox-editing-not-able-to-edit-selected-row

[^4]: http://guriddo.net/?topic=jqgrid-with-an-editable-checkbox-column

[^5]: https://stackoverflow.com/questions/8357651/jqgrid-has-missing-id-in-checkbox/8358996

[^6]: https://www.infragistics.com/blogs/jquery-grid-checkbox/

[^7]: https://supportcenter.devexpress.com/ticket/details/t635794/gridview-checkbox-column-is-not-editable

[^8]: https://mine-it-record.tistory.com/452

