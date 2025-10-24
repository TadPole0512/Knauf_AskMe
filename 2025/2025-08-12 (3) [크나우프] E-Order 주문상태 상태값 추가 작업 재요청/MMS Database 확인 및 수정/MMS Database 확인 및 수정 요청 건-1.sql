

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

select * from MMS_MSG m
--order by m.PHONE, m.MSG
;



/* ***********  *********** */
;


select m.PHONE, m.MSG, Count(*) as cnt from MMS_MSG m
group BY m.PHONE, m.MSG
having Count(*) > 1
;



/* ***********  *********** */
;

select * from MMS_MSG m
inner join (
				select m1.PHONE, m1.MSG, Count(*) as cnt from MMS_MSG m1
				group BY m1.PHONE, m1.MSG
				having Count(*) > 1
			) m2
			on m.PHONE = m2.PHONE and m.MSG = m2.MSG
order by m.REQDATE desc
;




/* ***************************************************************************************************************************************** */
/* *********** Table Search *********** */
;

select * from INFORMATION_SCHEMA.TABLES t
where t.TABLE_NAME like 'MMS_LOG%'
order by t.TABLE_NAME desc
;


/* *********** MMS_LOG_202507 *********** */
;

select  top 100 * from MMS_LOG_202508
--where PHONE = '01043823223'
order by REQDATE desc
;

select *
from MMS_MSG
where 1 = 1
--and SUBJECT not in ('크나우프석고보드_오더접수', '크나우프석고보드_보류오더 안내')
order by REQDATE desc
;

--374775003	010-6368-7366	2025-07-16 14:59:50.000


/* *********** MMS_LOG_202506 *********** */
;

select  top 100 * from MMS_LOG_202506
order by REQDATE desc
;


--
--USE SPA;
--GO
--
--IF OBJECT_ID(N'dbo.TRG_MMS_MSG_DUP_CHECK', N'TR') IS NOT NULL
--    DROP TRIGGER dbo.TRG_MMS_MSG_DUP_CHECK;
--GO





