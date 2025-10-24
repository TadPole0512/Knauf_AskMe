아래의 내용은 현재 운영 중인 웹 프로젝트에 추가할 내용인데 개발 단계별 상세 가이드를 정리해서 알려줘.

[개발환경]
- 개발툴 : STS4.5
- 자바 : Zulu-8
- DB : mssql

[개발 내용]
- 엑셀 업로드를 통한 DB 테이블의 값을 업데이트

[엑셀 파일 내용]
- 추가 1. 엑셀 파일에 SalesDocument, Sales Document Item, Reason for rejection 컬럼, DS(Delivery status) 컬럼, OS(Overall status) 컬럼은 반드시 존재.
- 추가 2. 엑셀 파일에 컬럼은 추가 1.에서 얘기한 컬럼보다 많음.
- 추가 3. 엑셀 파일에 컬럼의 순서가 파일마다 다름.
- 추가 4. 엑셀 파일에 컬럼의 갯수가 파일마다 다름.
- 추가 5. 엑셀 파일의 경우 행이 13000개 이상일 경우가 있음.

[DB 작업]
엑셀 파일에서 얻은 오더번호로 DB 테이블 O_SALESORDER, QMS_SALESORDER에서 오더번호를 찾아 아래의 작업을 해야함.
    - 두 개 테이블(O_SALESORDER, QMS_SALESORDER)을 모두 동일하게 업데이트
SalesDocument, Sales Document Item*100를 AND조건으로 활용하여 ORDERNO, LINE_NO와 매칭
    - 엑셀 파일의 Sales Document Item 컬럼의 값이 DB 테이블의 LINE_NO의 1/100이므로 값을 비교할 때 Sales Document Item에 100을 곱해야 함.
1. Reason for rejection 컬럼의 값이 Null이 아닌 경우
    A. 해당 ORDER의 STATUS1 – 980, STATUS – 999, STATUS_DESC – 오더취소
2. DS 컬럼의 값이 C인 경우
    A. 해당 ORDER의 STATUS1 – 580, STATUS – 620, STATUS_DESC – 배송완료
3. DS 컬럼의 값이 Null 이고 OS 컬럼의 값이 C인 경우
    A. 해당 ORDER의 STATUS1 – 580, STATUS – 620, STATUS_DESC – 배송완료
위의 1,2,3 경우가 중복될 때는 1번이 최우선이며, 2번이 우선 적용하면 됨

[작업 개요]
1. 웹 페이지에서 엑셀 업로드 기능을 이용.
2. 사용자가 엑셀 파일을 업로드하는 즉시 작업 진행.
3. 엑셀 진행률에 대한 정보를 보여주는 것도 필요.
4. 작업이 마무리 되면 웹페이지(화면)에 실패한 건들의 키값과 실패이유에 대한 리포트 제공

[이슈]
1. 작업 속도
    - 임시 테이블을 먼저 만들어 엑셀 파일을 먼저 전체 다 임시 테이블에 저장하고 나서 DB작업은 임시 테이블에서 실행
        A. 작업은 DB에 임시테이블을 만들어서 엑셀 파일에 대한 모든 내용을 전부 업데이트
        B. 임시테이블에서 [DB 작업]의 작업들을 테이블 대 테이블을 비교하면서 진행
    - 엑셀 파일에 컬럼의 갯수가 다르고 몇 개로 구성되어 질 지 모르는 상황에서 임시 테이블 생성.
	- 엑셀 파일마다 컬럼의 순서가 다를 수 있어 임시테이블 생성 시 컬럼의 타입을 엑셀 파일 컬럼에 맞춰 생성
2. 진행률 표시
    - 작업 속도에 미치는 영향
3. 리포트 제공
    - 실패에 대한 정보를 저장하는 테이블 필요?


---------------------------------------------------------------------



# 엑셀 업로드 DB 업데이트 개발 가이드

## 1. 개발 환경 설정

### 1.1 필요한 라이브러리 추가 (pom.xml)
```xml
<!-- Apache POI for Excel processing -->
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi</artifactId>
    <version>5.2.4</version>
</dependency>
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi-ooxml</artifactId>
    <version>5.2.4</version>
</dependency>

<!-- File Upload -->
<dependency>
    <groupId>commons-fileupload</groupId>
    <artifactId>commons-fileupload</artifactId>
    <version>1.4</version>
</dependency>
<dependency>
    <groupId>commons-io</groupId>
    <artifactId>commons-io</artifactId>
    <version>2.11.0</version>
</dependency>

<!-- WebSocket for progress tracking -->
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-websocket</artifactId>
    <version>5.3.23</version>
</dependency>
```

### 1.2 Spring 설정
```xml
<!-- multipart-config 설정 -->
<bean id="multipartResolver" class="org.springframework.web.multipart.commons.CommonsMultipartResolver">
    <property name="maxUploadSize" value="52428800"/> <!-- 50MB -->
    <property name="maxInMemorySize" value="1048576"/> <!-- 1MB -->
</bean>
```

## 2. 데이터베이스 설계

### 2.1 임시 테이블 생성 전략
```sql
-- 동적 임시 테이블 생성 (예시)
CREATE TABLE TEMP_EXCEL_DATA_{session_id} (
    ROW_ID INT IDENTITY(1,1) PRIMARY KEY,
    SALES_DOCUMENT VARCHAR(50),
    SALES_DOCUMENT_ITEM VARCHAR(50),
    REASON_FOR_REJECTION VARCHAR(500),
    DS VARCHAR(10),
    OS VARCHAR(10),
    -- 동적으로 추가되는 컬럼들
    [COLUMN_NAME] VARCHAR(MAX)
);
```

### 2.2 진행률 추적 테이블
```sql
CREATE TABLE EXCEL_PROCESS_LOG (
    PROCESS_ID VARCHAR(50) PRIMARY KEY,
    USER_ID VARCHAR(50),
    FILE_NAME VARCHAR(255),
    TOTAL_ROWS INT,
    PROCESSED_ROWS INT,
    STATUS VARCHAR(20), -- PROCESSING, COMPLETED, FAILED
    START_TIME DATETIME,
    END_TIME DATETIME,
    ERROR_MESSAGE TEXT
);
```

### 2.3 실패 리포트 테이블
```sql
CREATE TABLE EXCEL_PROCESS_ERRORS (
    ERROR_ID INT IDENTITY(1,1) PRIMARY KEY,
    PROCESS_ID VARCHAR(50),
    ROW_NUMBER INT,
    SALES_DOCUMENT VARCHAR(50),
    SALES_DOCUMENT_ITEM VARCHAR(50),
    ERROR_TYPE VARCHAR(50),
    ERROR_MESSAGE VARCHAR(500),
    FOREIGN KEY (PROCESS_ID) REFERENCES EXCEL_PROCESS_LOG(PROCESS_ID)
);
```

## 3. 백엔드 개발

### 3.1 엑셀 파일 분석 및 임시 테이블 생성
```java
@Service
public class ExcelProcessService {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    public String analyzeExcelAndCreateTempTable(MultipartFile file, String sessionId) {
        String tempTableName = "TEMP_EXCEL_DATA_" + sessionId;
        
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            
            // 1. 헤더 분석
            Map<String, Integer> columnIndexMap = analyzeHeaders(headerRow);
            
            // 2. 필수 컬럼 존재 확인
            validateRequiredColumns(columnIndexMap);
            
            // 3. 동적 임시 테이블 생성
            createDynamicTempTable(tempTableName, columnIndexMap);
            
            return tempTableName;
        } catch (Exception e) {
            throw new RuntimeException("엑셀 파일 분석 실패", e);
        }
    }
    
    private Map<String, Integer> analyzeHeaders(Row headerRow) {
        Map<String, Integer> columnMap = new HashMap<>();
        
        for (Cell cell : headerRow) {
            String columnName = cell.getStringCellValue().trim();
            columnMap.put(columnName, cell.getColumnIndex());
        }
        
        return columnMap;
    }
    
    private void validateRequiredColumns(Map<String, Integer> columnMap) {
        String[] requiredColumns = {
            "SalesDocument", 
            "Sales Document Item", 
            "Reason for rejection", 
            "DS", 
            "OS"
        };
        
        for (String column : requiredColumns) {
            if (!columnMap.containsKey(column)) {
                throw new IllegalArgumentException("필수 컬럼 누락: " + column);
            }
        }
    }
    
    private void createDynamicTempTable(String tableName, Map<String, Integer> columnMap) {
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE ").append(tableName).append(" (");
        sql.append("ROW_ID INT IDENTITY(1,1) PRIMARY KEY,");
        
        for (String columnName : columnMap.keySet()) {
            String sanitizedName = sanitizeColumnName(columnName);
            sql.append(sanitizedName).append(" VARCHAR(MAX),");
        }
        
        sql.setLength(sql.length() - 1); // 마지막 콤마 제거
        sql.append(")");
        
        jdbcTemplate.execute(sql.toString());
    }
}
```

### 3.2 엑셀 데이터 배치 처리
```java
@Service
public class ExcelDataProcessor {
    
    private static final int BATCH_SIZE = 1000;
    
    @Async
    public void processExcelData(MultipartFile file, String tempTableName, 
                                String processId, Map<String, Integer> columnMap) {
        
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            int totalRows = sheet.getLastRowNum();
            
            // 진행률 초기화
            updateProcessStatus(processId, 0, totalRows, "PROCESSING");
            
            // 배치 처리
            List<Map<String, Object>> batch = new ArrayList<>();
            
            for (int i = 1; i <= totalRows; i++) { // 헤더 제외
                Row row = sheet.getRow(i);
                if (row != null) {
                    Map<String, Object> rowData = extractRowData(row, columnMap);
                    batch.add(rowData);
                    
                    if (batch.size() >= BATCH_SIZE) {
                        insertBatch(tempTableName, batch, columnMap);
                        batch.clear();
                        
                        // 진행률 업데이트
                        updateProcessStatus(processId, i, totalRows, "PROCESSING");
                    }
                }
            }
            
            // 남은 데이터 처리
            if (!batch.isEmpty()) {
                insertBatch(tempTableName, batch, columnMap);
            }
            
            // DB 업데이트 작업 시작
            performDatabaseUpdate(tempTableName, processId);
            
        } catch (Exception e) {
            updateProcessStatus(processId, 0, 0, "FAILED");
            throw new RuntimeException("엑셀 데이터 처리 실패", e);
        }
    }
    
    private void insertBatch(String tableName, List<Map<String, Object>> batch, 
                           Map<String, Integer> columnMap) {
        
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO ").append(tableName).append(" (");
        
        // 컬럼명 추가
        for (String column : columnMap.keySet()) {
            sql.append(sanitizeColumnName(column)).append(",");
        }
        sql.setLength(sql.length() - 1);
        sql.append(") VALUES ");
        
        // 배치 값 추가
        for (int i = 0; i < batch.size(); i++) {
            sql.append("(");
            for (String column : columnMap.keySet()) {
                sql.append("?,");
            }
            sql.setLength(sql.length() - 1);
            sql.append(")");
            
            if (i < batch.size() - 1) {
                sql.append(",");
            }
        }
        
        // 파라미터 설정
        List<Object> params = new ArrayList<>();
        for (Map<String, Object> row : batch) {
            for (String column : columnMap.keySet()) {
                params.add(row.get(column));
            }
        }
        
        jdbcTemplate.update(sql.toString(), params.toArray());
    }
}
```

### 3.3 DB 업데이트 로직
```java
@Service
public class DatabaseUpdateService {
    
    public void performDatabaseUpdate(String tempTableName, String processId) {
        try {
            // 1. 취소 처리 (최우선)
            processRejections(tempTableName, processId);
            
            // 2. 배송완료 처리 (DS = 'C')
            processDeliveryStatus(tempTableName, processId);
            
            // 3. 배송완료 처리 (DS is null AND OS = 'C')
            processOverallStatus(tempTableName, processId);
            
            // 4. 임시 테이블 정리
            cleanupTempTable(tempTableName);
            
            updateProcessStatus(processId, 0, 0, "COMPLETED");
            
        } catch (Exception e) {
            updateProcessStatus(processId, 0, 0, "FAILED");
            logError(processId, "DB_UPDATE_ERROR", e.getMessage());
            throw e;
        }
    }
    
    private void processRejections(String tempTableName, String processId) {
        String sql = """
            UPDATE o SET 
                o.STATUS1 = 980, 
                o.STATUS = 999, 
                o.STATUS_DESC = '오더취소'
            FROM O_SALESORDER o
            INNER JOIN %s t ON o.ORDERNO = t.SALES_DOCUMENT 
                AND o.LINE_NO = CAST(t.SALES_DOCUMENT_ITEM AS INT) * 100
            WHERE t.REASON_FOR_REJECTION IS NOT NULL 
                AND LTRIM(RTRIM(t.REASON_FOR_REJECTION)) != ''
            """.formatted(tempTableName);
        
        int updatedRows = jdbcTemplate.update(sql);
        
        // QMS_SALESORDER도 동일하게 업데이트
        String qmsSql = sql.replace("O_SALESORDER", "QMS_SALESORDER");
        jdbcTemplate.update(qmsSql);
        
        logProcessResult(processId, "REJECTION", updatedRows);
    }
    
    private void processDeliveryStatus(String tempTableName, String processId) {
        String sql = """
            UPDATE o SET 
                o.STATUS1 = 580, 
                o.STATUS = 620, 
                o.STATUS_DESC = '배송완료'
            FROM O_SALESORDER o
            INNER JOIN %s t ON o.ORDERNO = t.SALES_DOCUMENT 
                AND o.LINE_NO = CAST(t.SALES_DOCUMENT_ITEM AS INT) * 100
            WHERE t.DS = 'C'
                AND (t.REASON_FOR_REJECTION IS NULL OR LTRIM(RTRIM(t.REASON_FOR_REJECTION)) = '')
            """.formatted(tempTableName);
        
        int updatedRows = jdbcTemplate.update(sql);
        
        // QMS_SALESORDER도 동일하게 업데이트
        String qmsSql = sql.replace("O_SALESORDER", "QMS_SALESORDER");
        jdbcTemplate.update(qmsSql);
        
        logProcessResult(processId, "DELIVERY_STATUS", updatedRows);
    }
    
    private void processOverallStatus(String tempTableName, String processId) {
        String sql = """
            UPDATE o SET 
                o.STATUS1 = 580, 
                o.STATUS = 620, 
                o.STATUS_DESC = '배송완료'
            FROM O_SALESORDER o
            INNER JOIN %s t ON o.ORDERNO = t.SALES_DOCUMENT 
                AND o.LINE_NO = CAST(t.SALES_DOCUMENT_ITEM AS INT) * 100
            WHERE t.DS IS NULL 
                AND t.OS = 'C'
                AND (t.REASON_FOR_REJECTION IS NULL OR LTRIM(RTRIM(t.REASON_FOR_REJECTION)) = '')
            """.formatted(tempTableName);
        
        int updatedRows = jdbcTemplate.update(sql);
        
        // QMS_SALESORDER도 동일하게 업데이트
        String qmsSql = sql.replace("O_SALESORDER", "QMS_SALESORDER");
        jdbcTemplate.update(qmsSql);
        
        logProcessResult(processId, "OVERALL_STATUS", updatedRows);
    }
}
```

## 4. 진행률 추적 (WebSocket)

### 4.1 WebSocket 설정
```java
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new ProgressWebSocketHandler(), "/progress")
                .setAllowedOrigins("*");
    }
}

@Component
public class ProgressWebSocketHandler extends TextWebSocketHandler {
    
    private static final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String processId = getProcessId(session);
        sessions.put(processId, session);
    }
    
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String processId = getProcessId(session);
        sessions.remove(processId);
    }
    
    public static void sendProgress(String processId, int current, int total, String status) {
        WebSocketSession session = sessions.get(processId);
        if (session != null && session.isOpen()) {
            try {
                Map<String, Object> progress = new HashMap<>();
                progress.put("current", current);
                progress.put("total", total);
                progress.put("percentage", total > 0 ? (current * 100 / total) : 0);
                progress.put("status", status);
                
                session.sendMessage(new TextMessage(new ObjectMapper().writeValueAsString(progress)));
            } catch (Exception e) {
                // 로그 처리
            }
        }
    }
}
```

## 5. 컨트롤러 개발

### 5.1 메인 컨트롤러
```java
@RestController
@RequestMapping("/api/excel")
public class ExcelUploadController {
    
    @Autowired
    private ExcelProcessService excelProcessService;
    
    @Autowired
    private ExcelDataProcessor excelDataProcessor;
    
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadExcel(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {
        
        try {
            // 파일 유효성 검사
            validateFile(file);
            
            // 프로세스 ID 생성
            String processId = UUID.randomUUID().toString();
            String sessionId = request.getSession().getId();
            
            // 엑셀 분석 및 임시 테이블 생성
            String tempTableName = excelProcessService.analyzeExcelAndCreateTempTable(file, sessionId);
            Map<String, Integer> columnMap = excelProcessService.getColumnMap(file);
            
            // 비동기 처리 시작
            excelDataProcessor.processExcelData(file, tempTableName, processId, columnMap);
            
            Map<String, Object> response = new HashMap<>();
            response.put("processId", processId);
            response.put("message", "엑셀 업로드가 시작되었습니다.");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/status/{processId}")
    public ResponseEntity<Map<String, Object>> getProcessStatus(@PathVariable String processId) {
        // 진행률 조회 로직
        return ResponseEntity.ok(excelProcessService.getProcessStatus(processId));
    }
    
    @GetMapping("/errors/{processId}")
    public ResponseEntity<List<Map<String, Object>>> getProcessErrors(@PathVariable String processId) {
        // 에러 리포트 조회 로직
        return ResponseEntity.ok(excelProcessService.getProcessErrors(processId));
    }
}
```

## 6. 프론트엔드 개발

### 6.1 업로드 페이지 (JSP)
```jsp
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>엑셀 업로드</title>
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
</head>
<body>
    <div id="uploadSection">
        <h2>엑셀 파일 업로드</h2>
        <form id="uploadForm" enctype="multipart/form-data">
            <input type="file" id="excelFile" name="file" accept=".xlsx,.xls" required>
            <button type="submit">업로드</button>
        </form>
    </div>
    
    <div id="progressSection" style="display: none;">
        <h3>처리 진행률</h3>
        <div id="progressBar">
            <div id="progressFill" style="width: 0%; height: 20px; background: #4CAF50;"></div>
        </div>
        <div id="progressText">0%</div>
    </div>
    
    <div id="resultSection" style="display: none;">
        <h3>처리 결과</h3>
        <div id="resultMessage"></div>
        <div id="errorReport"></div>
    </div>

    <script>
        let socket;
        let currentProcessId;
        
        $('#uploadForm').on('submit', function(e) {
            e.preventDefault();
            
            const formData = new FormData(this);
            
            $.ajax({
                url: '/api/excel/upload',
                type: 'POST',
                data: formData,
                processData: false,
                contentType: false,
                success: function(response) {
                    currentProcessId = response.processId;
                    $('#uploadSection').hide();
                    $('#progressSection').show();
                    connectWebSocket();
                },
                error: function(xhr) {
                    alert('업로드 실패: ' + xhr.responseJSON.error);
                }
            });
        });
        
        function connectWebSocket() {
            socket = new WebSocket('ws://localhost:8080/progress');
            
            socket.onmessage = function(event) {
                const data = JSON.parse(event.data);
                updateProgress(data);
            };
            
            socket.onopen = function() {
                socket.send(JSON.stringify({processId: currentProcessId}));
            };
        }
        
        function updateProgress(data) {
            $('#progressFill').css('width', data.percentage + '%');
            $('#progressText').text(data.percentage + '%');
            
            if (data.status === 'COMPLETED') {
                showResults();
            } else if (data.status === 'FAILED') {
                showError();
            }
        }
        
        function showResults() {
            $('#progressSection').hide();
            $('#resultSection').show();
            
            $.get('/api/excel/errors/' + currentProcessId, function(errors) {
                if (errors.length > 0) {
                    displayErrors(errors);
                } else {
                    $('#resultMessage').text('모든 데이터가 성공적으로 처리되었습니다.');
                }
            });
        }
        
        function displayErrors(errors) {
            let errorHtml = '<h4>실패 항목들:</h4><table border="1">';
            errorHtml += '<tr><th>행 번호</th><th>Sales Document</th><th>Sales Document Item</th><th>오류 유형</th><th>오류 메시지</th></tr>';
            
            errors.forEach(function(error) {
                errorHtml += '<tr>';
                errorHtml += '<td>' + error.rowNumber + '</td>';
                errorHtml += '<td>' + error.salesDocument + '</td>';
                errorHtml += '<td>' + error.salesDocumentItem + '</td>';
                errorHtml += '<td>' + error.errorType + '</td>';
                errorHtml += '<td>' + error.errorMessage + '</td>';
                errorHtml += '</tr>';
            });
            
            errorHtml += '</table>';
            $('#errorReport').html(errorHtml);
        }
    </script>
</body>
</html>
```

## 7. 성능 최적화 고려사항

### 7.1 대용량 데이터 처리
- 배치 크기 조정 (BATCH_SIZE = 1000)
- 커넥션 풀 설정 최적화
- 인덱스 활용 (ORDERNO, LINE_NO 복합 인덱스)

### 7.2 메모리 관리
- 스트리밍 방식으로 엑셀 파일 읽기
- 임시 테이블 정리 자동화
- 가비지 컬렉션 최적화

### 7.3 에러 처리
- 트랜잭션 분리 (읽기/쓰기)
- 재시도 로직 구현
- 데드락 방지

## 8. 배포 및 운영 고려사항

### 8.1 모니터링
- 처리 시간 로깅
- 실패율 추적
- 시스템 리소스 모니터링

### 8.2 보안
- 파일 업로드 크기 제한
- 파일 타입 검증
- 세션 관리

### 8.3 백업 및 복구
- 처리 전 데이터 백업
- 롤백 기능 구현
- 로그 아카이빙



이 가이드를 따라 단계별로 구현하시면 대용량 엑셀 파일 처리가 가능한 안정적인 시스템을 구축할 수 있습니다.

엑셀 업로드를 통한 DB 업데이트 기능 개발에 대한 단계별 가이드를 정리해드리겠습니다.위의 가이드는 요구사항에 맞춰 엑셀 업로드 기능을 구현하기 위한 단계별 상세 가이드입니다. 

주요 특징:

1. **동적 임시 테이블 생성**: 엑셀 파일마다 다른 컬럼 구성에 대응
2. **배치 처리**: 13,000개 이상의 행도 효율적으로 처리
3. **실시간 진행률 표시**: WebSocket을 통한 실시간 업데이트
4. **우선순위 기반 처리**: 취소 > 배송완료(DS) > 배송완료(OS) 순으로 처리
5. **에러 리포트**: 실패한 항목들에 대한 상세 정보 제공

구현 시 고려할 점:

- **성능**: 배치 크기와 커넥션 풀 설정을 환경에 맞게 조정
- **안정성**: 트랜잭션 분리와 에러 처리 강화
- **확장성**: 향후 다른 엑셀 형식 지원을 위한 확장 가능한 구조

혹시 특정 부분에 대해 더 자세한 설명이 필요하시거나 구현 과정에서 궁금한 점이 있으시면 언제든 말씀해 주세요.

