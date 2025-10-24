

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
order by m.READDATE desc
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
order by m.PHONE asc, m.REQDATE desc
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

select  top 100 * from MMS_LOG_202507
where MSGKEY in ('375502189', '375503144')
order by REQDATE desc
;




/* *********** MMS_LOG_202506 *********** */
;

select  top 100 * from MMS_LOG_202506
where MSGKEY in ('375502189', '375503144')
order by REQDATE desc
;



/* ***************************************************************************************************************************************** */
/* ***********  *********** */
;

select *
FROM MMS_MSG m
		inner join MMS_LOG_202506 m1 on m.MSGKEY = m1.MSGKEY
;


