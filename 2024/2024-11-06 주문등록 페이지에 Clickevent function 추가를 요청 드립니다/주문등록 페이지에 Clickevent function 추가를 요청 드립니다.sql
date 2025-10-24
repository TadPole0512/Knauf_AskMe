
/* *********** eorder.board.cnt *********** */

SELECT
		COUNT(*)
FROM	BOARD BD
		LEFT OUTER JOIN O_USER US ON BD.BD_INID = US.USERID
WHERE	BD_ID = 'notice'
;



/* *********** eorder.board.list *********** */

SELECT
		*
FROM	(
			SELECT
					ROW_NUMBER() OVER( ORDER BY BD_NOTICEYN DESC , BD_INDATE DESC ) AS ROWNUM ,
					XX.*
			FROM	(
						SELECT
								BD.*,
								US.* ,
								(
									SELECT
											CC_NAME
									FROM	COMMONCODE
									WHERE	CC_CODE = BD_TYPE
								) BD_TYPENM
						FROM	BOARD BD
								LEFT OUTER JOIN O_USER US ON BD.BD_INID = US.USERID
						WHERE	BD_ID = 'notice'
					) XX
		) S
WHERE	ROWNUM <= 10

;




/* *********** eorder.commonCode.getCategoryListWithDepth *********** */

SELECT
		c.CC_CODE,
		c.CC_NAME
FROM	COMMONCODE c
WHERE	c.CC_CODE LIKE '' + '' + '%'
AND		c.CC_DEPTH = 3--4
ORDER BY c.CC_CODE 

;




/* *********** eorder.board.cnt *********** */

SELECT
		COUNT(*)
FROM	BOARD BD
		LEFT OUTER JOIN O_USER US
				ON BD.BD_INID = US.USERID
WHERE	BD_SEQ = 1902

;



/* *********** eorder.board.on *********** */

SELECT
		BD.* ,
		(
			SELECT
					CC_NAME
			FROM	COMMONCODE
			WHERE	1 = 1
			AND		CC_CODE =	(
									CASE
										WHEN LEN(BD.BD_TYPE3)= 10 THEN BD.BD_TYPE3
										ELSE BD.BD_TYPE
									END
								)
		) BD_TYPENM
FROM	BOARD BD
		LEFT OUTER JOIN O_USER US ON BD.BD_INID = US.USERID
WHERE	BD_SEQ = 1902

;



/* ***************************************************************************************************************************************** */

SELECT
		*
FROM	BOARD A
WHERE	1 = 1
AND		BD_ID = 'notice'
--GROUP BY 
ORDER BY BD_SEQ DESC
;


SELECT
		*
FROM	BOARD A
WHERE	1 = 1
AND		BD_ID = 'notice'
AND     BD_SEQ = 2220
--GROUP BY 
ORDER BY BD_SEQ DESC
;



-- BD_SEQ	BD_ID	BD_TYPE	BD_DISPLAYTYPE	BD_TITLE
-- 2262	notice	[NULL]	user	크나우프석고보드 배송안내서


-- INSERT INTO BOARD (BD_SEQ, BD_ID, BD_TYPE, BD_DISPLAYTYPE, BD_TITLE, BD_CONTENT, BD_FILE, BD_FILETYPE, BD_IMAGE, BD_IMAGETYPE, BD_FILEYN, BD_NOTICEYN, BD_USEYN, BD_MOID, BD_INID, BD_MODATE, BD_INDATE, BD_REPLY, BD_REPLYYN, VIEW_CNT, BD_TYPE3, BD_TYPE2)
-- VALUES(2262, 'notice', NULL, 'user', '크나우프석고보드 배송안내서', '<p><img alt="" src="/eorder/data/editor/공지사항5.png" style="height:623px; width:1100px" /></p>', '크나우프석고보드_배송안내서.pdf', 'application/pdf', NULL, NULL, 'N', 'Y', 'Y', '0080002338', '0080002338', '2024-11-06 16:38:21.000000', '2024-11-06 16:34:03.000000', NULL, 'N', 0, NULL, NULL);














SELECT
		COUNT(*)
FROM	LOGINLOG
WHERE	LL_USERID = '10177560'
AND		LL_PSYN = ?
;


