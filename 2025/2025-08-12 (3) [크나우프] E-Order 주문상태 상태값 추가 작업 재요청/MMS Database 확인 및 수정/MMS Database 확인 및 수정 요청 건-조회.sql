

/* ***************************************************************************************************************************************** */
/* *********** Trigger Search *********** */
;

SELECT 
        trg.name AS 트리거명
      , tbl.name AS 테이블명
      , CASE trg.is_disabled
            WHEN 0 THEN '사용중'
            ELSE '비활성'
        END AS 상태
  FROM  sys.triggers trg
        JOIN sys.tables tbl ON trg.parent_id = tbl.object_id
;





/* ***************************************************************************************************************************************** */
/* *********** MMS_MSG *********** */
;

select @VERSION;

SELECT
  SERVERPROPERTY('ProductVersion') AS 버전,
  SERVERPROPERTY('ProductLevel') AS 서비스팩,
  SERVERPROPERTY('Edition') AS 에디션;



/* ***************************************************************************************************************************************** */
/* ***********  *********** */
;

select * from MMS_MSG;

select * from MMS_LOG_202507;

select * from MMS_LOG_202508;



































