	<select id="getReportFor1121" parameterType="hashmap" resultType="hashmap">
		SELECT SUB.*
		FROM (

			SELECT
				GRP.ITEM_DESC
				, CASE GROUPING(GRP.ACTUAL_SHIP_DT)
					 WHEN 1 THEN
					'소계'
					 ELSE ACTUAL_SHIP_DT END AS ACTUAL_SHIP_DT
				, SUM(GRP.ORDER_QTY) AS SUM_ORDER_QTY
				<!-- 2024-11-04 hsg imir-87 출고자료 중복으로 나타남 - group by 에서 빠진 항목은 max 처리함. , GRP.UNIT
				, GRP.ITEM_CD_3
				, GRP.ADD1 -->
				, MAX(GRP.UNIT) AS UNIT
				, MAX(GRP.ITEM_CD_3) AS ITEM_CD_3
				<!-- 2024-11-04 hsg imir-88 “제조사” 나타나지 않음 - 제조사 추가 -->
				, MAX(GRP.MANUFACT) AS MANUFACT
				, MAX(GRP.ADD1) AS ADD1
			FROM (

				SELECT SO.ACTUAL_SHIP_DT
					   , SO.ITEM_DESC
					   , SO.ADD1
				<if test='r_hebechk == null or r_hebechk == "" '>
					   , SO.UNIT
					   , SUM(SO.ORDER_QTY) AS ORDER_QTY
				</if>
				<if test='r_hebechk != null and r_hebechk == "Y" '>
					   , SO.UNIT1 AS UNIT
					   , SUM(SO.PRIMARY_QTY) AS ORDER_QTY
				</if>
					   , SUBSTRING(SO.ITEM_CD, 1, 3) AS ITEM_CD_3
						<!-- 2024-11-04 hsg imir-88 “제조사” 나타나지 않음 - 제조사 추가 -->
					   , CASE WHEN MFG.MFG IS NULL THEN '크나우프 석고보드㈜' ELSE MFG.MFG END  AS MANUFACT
				FROM O_SALESORDER SO
				<!-- 2024-11-04 hsg imir-88 “제조사” 나타나지 않음 - 제조사 추가 하기 위해 O_ITEM_MFG 테이블과 조인. -->
				LEFT JOIN O_ITEM_HEBE OH ON OH.ITEM_CD = SO.ITEM_CD
				LEFT JOIN O_ITEM_MFG MFG ON SO.ITEM_CD = MFG.ITEM_CD
				<where>
					<if test="r_actualshipsdt != null and r_actualshipsdt != '' ">AND ACTUAL_SHIP_DT <![CDATA[>=]]> #{r_actualshipsdt}</if>
					<if test="r_actualshipedt != null and r_actualshipedt != '' ">AND ACTUAL_SHIP_DT <![CDATA[<=]]> #{r_actualshipedt}</if>
					<if test="r_custcd != null and r_custcd != '' ">AND CUST_CD = #{r_custcd}</if>
					<if test="r_shiptonm != null and r_shiptonm != '' ">AND SHIPTO_NM = TRIM(#{r_shiptonm})</if>
					<if test="ri_add1 != null">
						AND ADD1 IN <foreach collection="ri_add1" item="add1" separator="," open="(" close=")">TRIM(#{add1})</foreach>
					</if>
					<if test="ri_itemdesc != null">
						AND ITEM_DESC IN <foreach collection="ri_itemdesc" item="item_desc" separator="," open="(" close=")">TRIM(#{item_desc})</foreach>
					</if>
					<!-- 2024-11-04 hsg imir-87 출고자료 중복으로 나타남 - 조회조건 추가 -->
					AND	STATUS1 <![CDATA[>=]]> '580'
					AND STATUS1 <![CDATA[<>]]> '980'
				</where>
				GROUP BY ITEM_DESC
						 , ACTUAL_SHIP_DT
				<if test='r_hebechk == null or r_hebechk == "" '>, UNIT</if>
				<if test='r_hebechk != null and r_hebechk == "Y" '>, UNIT1</if>
						 , ADD1
						 , SUBSTRING(SO.ITEM_CD, 1, 3)
						 , MFG.MFG
			) GRP

			GROUP BY GRP.ITEM_DESC, GRP.ACTUAL_SHIP_DT<!-- 2024-11-04 hsg imir-87 출고자료 중복으로 나타남 - rollup 문으로인해 중복데이터 발생하는 것 같아 나머지 주석처리. , GRP.UNIT, GRP.ITEM_CD_3, GRP.ADD1 --> WITH ROLLUP
			HAVING GROUPING(GRP.ITEM_DESC) = 0

		) SUB

		<if test=" r_orderby != null and r_orderby != '' ">ORDER BY ${r_orderby}</if>
	</select>
