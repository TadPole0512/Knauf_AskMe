

/* eorder.o_qmsorder.setQmsOrderFireproofUpdate */
MERGE INTO QMS_ORD_FRCN F
	USING DUAL ON (F.QMS_ID = ? AND F.QMS_SEQ = ? AND F.KEYCODE = ?)
WHEN MATCHED THEN
	UPDATE SET F.DELETEYN = 'N'
WHEN NOT MATCHED THEN
	INSERT (
				F.QMS_ID,
				QMS_SEQ,
				KEYCODE,
				CREATEUSER,
				CREATETIME,
				UPDATEUSER,
				UPDATETIME
			)
	VALUES (
				?,
				?,
				?,
				?,
				GETDATE(),
				?,
				GETDATE()
			)

;

--20243Q0099(String), 1(String), FW0003(String), 20243Q0099(String), 1(String), FW0003(String), develop(String), develop(String)



	/* eorder.o_qmsorder.setQmsOrderMastHistory */
	MERGE INTO QMS_ORD_CORP F
		USING DUAL ON (F.SHIPTO_CD = #{shiptoCd})
	WHEN MATCHED THEN
		UPDATE SET F.SHIPTO_ADDR = #{shiptoAddr}
				  ,F.SHIPTO_EMAIL = #{shiptoEmail}
				  ,F.CNSTR_ADDR = #{cnstrAddr}
				  ,F.CNSTR_BIZ_NO = #{cnstrBizNo}
				  ,F.CNSTR_TEL = #{cnstrTel}
				  ,F.SUPVS_ADDR = #{supvsAddr}
				  <!--  ,F.SUPVS_BIZ_NO = #{supvsBizNo}  -->
				  ,F.SUPVS_QLF_NO = #{supvsQlfNo}
				  ,F.SUPVS_DEC_NO = #{supvsDecNo}
				  ,F.SUPVS_TEL = #{supvsTel}
				  ,F.UPDATEUSER = #{userId}
				  ,F.UPDATETIME = GETDATE()
	WHEN NOT MATCHED THEN
		INSERT (SHIPTO_CD,SHIPTO_NM,SHIPTO_ADDR,SHIPTO_EMAIL,CNSTR_NM,CNSTR_ADDR,CNSTR_BIZ_NO,CNSTR_TEL
				,SUPVS_NM,SUPVS_ADDR 
				<!-- ,SUPVS_BIZ_NO  -->
				,SUPVS_QLF_NO,SUPVS_DEC_NO,SUPVS_TEL,CREATEUSER,CREATETIME,UPDATEUSER,UPDATETIME,DELETEYN)
		VALUES (#{shiptoCd}
			  ,#{shiptoNm}
			  ,#{shiptoAddr}
			  ,#{shiptoEmail}
			  ,#{cnstrNm}
			  ,#{cnstrAddr}
			  ,#{cnstrBizNo}
			  ,#{cnstrTel}
			  ,#{supvsNm}
			  ,#{supvsAddr}
			  <!--  ,#{supvsBizNo}  -->
			  ,#{supvsQlfNo}
			  ,#{supvsDecNo}
			  ,#{supvsTel}
			  ,#{userId}
			  ,GETDATE()
			  ,#{userId}
			  ,GETDATE()
			  ,'N')
;

-- 10183135(String), 광주광역시, 광산구, 첨단중앙로170번길 17(String), admin@knauf.com(String), (String), (String), (String), (String), (String), (String), (String), develop(String), 10183135(String), 아산 배방 생활숙박시설 - (주)한화(String), 광주광역시, 광산구, 첨단중앙로170번길 17(String), admin@knauf.com(String), 시공회사 (String), (String), (String), (String), 감리회사 (String), (String), (String), (String), (String), develop(String), develop(String)



