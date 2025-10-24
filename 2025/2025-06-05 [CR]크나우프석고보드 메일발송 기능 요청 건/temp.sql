    <select id="orderEmailAlarmCnt" parameterType="hashmap" resultType="int">
        WITH CUST AS (
                        SELECT
                               CU.CUST_CD                             -- 거래처 코드 (PK)
                             , CU.CUST_NM
                             , U2.USER_EMAIL AS CUST_MAIN_EMAIL       -- 담당자 이메일
                             , SUS.USER_EMAIL AS SALESREP_EMAIL       -- 영업 담당자 이메일
                          FROM O_CUSTOMER CU
                               LEFT JOIN O_USER SUS                   -- CU.SALESREP_CD ↔ SUS.USERID (영업사원 정보)
                                      ON CU.SALESREP_CD = SUS.USERID
                               LEFT JOIN O_USER U2                    -- CU.CUST_CD ↔ U2.USERID (대표 담당자 정보)
                                      ON CU.CUST_CD = U2.USERID
        <where>
            <if test="r_custcd != null and r_custcd != ''">AND CU.CUST_CD = #{r_custcd}</if>
            <if test="r_custnm != null and r_custnm != ''">AND CU.CUST_NM LIKE '%' + #{r_custnm} + '%'</if>
        </where>
                     )
        SELECT COUNT(*)
          FROM CUST CU
               LEFT OUTER JOIN O_CUSTOMER_MAILINFO CM
                            ON CU.CUST_CD = CM.CUST_CD
    </select>



    <select id="orderEmailAlarmList" parameterType="hashmap" resultType="hashmap">
        WITH CUST AS (
                        SELECT
                               CU.CUST_CD                             -- 거래처 코드 (PK)
                             , CU.CUST_NM
                             , U2.USER_EMAIL AS CUST_MAIN_EMAIL       -- 담당자 이메일
                             , SUS.USER_EMAIL AS SALESREP_EMAIL       -- 영업 담당자 이메일
                          FROM O_CUSTOMER CU
                               LEFT JOIN O_USER SUS                   -- CU.SALESREP_CD ↔ SUS.USERID (영업사원 정보)
                                      ON CU.SALESREP_CD = SUS.USERID
                               LEFT JOIN O_USER U2                    -- CU.CUST_CD ↔ U2.USERID (대표 담당자 정보)
                                      ON CU.CUST_CD = U2.USERID
                         WHERE 1 = 1
        <where>
            <if test="r_custcd != null and r_custcd != ''">AND CU.CUST_CD = #{r_custcd}</if>
            <if test="r_custnm != null and r_custnm != ''">AND CU.CUST_NM LIKE '%' + #{r_custnm} + '%'</if>
        </where>
                      )
        SELECT CU.CUST_CD
             , CU.CUST_NM
             , ISNULL(CM.CUST_MAIN_EMAIL, CU.CUST_MAIN_EMAIL) AS CUST_MAIN_EMAIL
             , ISNULL(CM.CUST_SENDMAIL_YN, 'N') AS CUST_SENDMAIL_YN
             , ISNULL(CM.SALESREP_EMAIL, CU.SALESREP_EMAIL) AS SALESREP_EMAIL
             , ISNULL(CM.SALESREP_SENDMAIL_YN, 'N') AS SALESREP_SENDMAIL_YN
             , CM.COMMENTS
          FROM CUST CU
               LEFT OUTER JOIN O_CUSTOMER_MAILINFO CM
                            ON CU.CUST_CD = CM.CUST_CD
    </select>
