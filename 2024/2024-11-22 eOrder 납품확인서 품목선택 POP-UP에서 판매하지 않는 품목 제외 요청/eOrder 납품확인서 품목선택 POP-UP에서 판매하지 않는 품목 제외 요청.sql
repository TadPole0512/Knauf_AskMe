/* ***************************************************************************************************************************************** */
/* *********** /admin/base/getCustomerListAjax.lime *********** */

/* *********** /admin/base/getCustomerListAjax.lime : eorder.o_customer.cnt *********** */
SELECT	COUNT(*)
FROM O_CUSTOMER CU
		LEFT JOIN O_USER US ON CU.SALESREP_CD = US.USERID
;


/* *********** /admin/base/getCustomerListAjax.lime : eorder.o_customer.list *********** */
SELECT
		*
FROM	(
			SELECT
					ROW_NUMBER() OVER( ORDER BY XX.CUST_CD ASC  ) AS ROWNUM
					, XX.*
			FROM	(
						SELECT
								CU.*, SUS.USER_NM AS SALESREP_NM2, SUS.AUTHORITY
								, (SELECT COUNT(*) FROM O_USER WHERE CUST_CD = CU.CUST_CD AND AUTHORITY = 'CO') AS CUSTOMER_USER_CNT
								, (SELECT COUNT(*) FROM O_USER WHERE CUST_CD = CU.CUST_CD AND AUTHORITY = 'CT') AS SHIPTO_USER_CNT
								, (SELECT COUNT(*) FROM O_SHIPTO WHERE CUST_CD = CU.CUST_CD) AS SHIPTO_CNT
								, (SELECT USER_EMAIL FROM O_USER WHERE USERID = CU.CUST_CD) AS CUST_MAIN_EMAIL
						FROM O_CUSTOMER CU
								LEFT JOIN O_USER SUS ON CU.SALESREP_CD = SUS.USERID
					) XX
		) S
WHERE	ROWNUM BETWEEN 1 AND 10
;




/* *********** /admin/base/getCustomerListAjax.lime : eorder.o_customer.cnt *********** */
SELECT
		COUNT(*)
FROM	O_CUSTOMER CU
		LEFT JOIN O_USER US ON CU.SALESREP_CD = US.USERID
WHERE	CUST_NM LIKE '%' + '자두건설' + '%'
;



/* *********** /admin/base/getCustomerListAjax.lime : eorder.o_customer.list *********** */
SELECT
		*
FROM	(
			SELECT
					ROW_NUMBER() OVER( ORDER BY XX.CUST_CD ASC  ) AS ROWNUM
					, XX.*
			FROM	(
						SELECT
								CU.*, SUS.USER_NM AS SALESREP_NM2, SUS.AUTHORITY
								, (SELECT COUNT(*) FROM O_USER WHERE CUST_CD = CU.CUST_CD AND AUTHORITY = 'CO') AS CUSTOMER_USER_CNT
								, (SELECT COUNT(*) FROM O_USER WHERE CUST_CD = CU.CUST_CD AND AUTHORITY = 'CT') AS SHIPTO_USER_CNT
								, (SELECT COUNT(*) FROM O_SHIPTO WHERE CUST_CD = CU.CUST_CD) AS SHIPTO_CNT
								, (SELECT USER_EMAIL FROM O_USER WHERE USERID = CU.CUST_CD) AS CUST_MAIN_EMAIL
						FROM	O_CUSTOMER CU
								LEFT JOIN O_USER SUS ON CU.SALESREP_CD = SUS.USERID
						WHERE	CUST_NM LIKE '%' + '자두건설' + '%'
					) XX
		) S
WHERE	ROWNUM BETWEEN 1 AND 10

;


/* ***************************************************************************************************************************************** */
/* *********** /admin/base/getShiptoListBySalesOrderAjax.lime : eorder.o_salesorder.getOrderShipNmGroup *********** */


/* *********** /admin/base/getShiptoListBySalesOrderAjax.lime : eorder.o_salesorder.getOrderShipNmGroup *********** */
SELECT
		MAX(SHIPTO_CD) AS SHIPTO_CD, SHIPTO_NM
FROM	O_SALESORDER
WHERE	ACTUAL_SHIP_DT >= '20240901'
AND		ACTUAL_SHIP_DT <= '20240930'
AND		CUST_CD = '10177560'
AND		STATUS1 >= '580'
AND		STATUS1 <> '980'
GROUP BY SHIPTO_NM
ORDER BY SHIPTO_NM ASC

;



/* *********** /admin/base/getItemDescListBySalesOrderAjax.lime : eorder.o_salesorder.getOrderItemDescGroup *********** */
SELECT
		IT.*
FROM	(
			SELECT	ITEM_DESC
			FROM	O_SALESORDER A
			WHERE	ACTUAL_SHIP_DT >= '20240901'
			AND		ACTUAL_SHIP_DT <= '20240930'
			AND		CUST_CD = '10177560'
			AND		SHIPTO_NM = TRIM('포일남교회 신축공사 - 자두건설 주식회사')
			AND		EXISTS ( SELECT * FROM O_ITEM_NEW O WHERE A.ITEM_CD = O.ITEM_CD AND O.SALES_CD3 != 'DAP42600' )
			GROUP BY ITEM_DESC
		) IT
		ORDER BY IT.ITEM_DESC ASC

;
































