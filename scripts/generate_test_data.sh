#!/bin/bash

# Garabu 테스트 데이터 생성 스크립트
# 사용법: ./generate_test_data.sh [옵션]

set -e

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 기본 설정
BASE_URL="http://localhost:8080"
DEFAULT_BOOK_ID=1
DEFAULT_COUNT=50000
OUTPUT_DIR="./test-data-output"

# 도움말 표시
show_help() {
    echo "Garabu 테스트 데이터 생성 스크립트"
    echo ""
    echo "사용법: $0 [옵션]"
    echo ""
    echo "옵션:"
    echo "  -h, --help              이 도움말 표시"
    echo "  -u, --url URL           서버 URL (기본값: $BASE_URL)"
    echo "  -b, --book-id ID        가계부 ID (기본값: $DEFAULT_BOOK_ID)"
    echo "  -c, --count COUNT       생성할 데이터 개수 (기본값: $DEFAULT_COUNT)"
    echo "  -o, --output DIR        출력 디렉토리 (기본값: $OUTPUT_DIR)"
    echo "  -s, --standard          표준 50,000건 생성"
    echo "  -f, --full              완전한 테스트 데이터 생성"
    echo "  -d, --direct            데이터베이스 직접 삽입 (SQL 스크립트 생성 안함)"
    echo ""
    echo "예시:"
    echo "  $0 -c 10000              # 10,000건 생성"
    echo "  $0 -s                    # 표준 50,000건 생성"
    echo "  $0 -f                    # 완전한 테스트 데이터 생성"
    echo "  $0 -d -c 5000            # 5,000건 직접 삽입"
    echo ""
}

# 로그 함수
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 서버 상태 확인
check_server() {
    log_info "서버 상태 확인 중... ($BASE_URL)"
    
    if curl -s -f "$BASE_URL/actuator/health" > /dev/null 2>&1; then
        log_success "서버가 정상 동작 중입니다."
    else
        log_error "서버에 연결할 수 없습니다. 서버가 실행 중인지 확인해주세요."
        exit 1
    fi
}

# 출력 디렉토리 생성
create_output_dir() {
    if [ ! -d "$OUTPUT_DIR" ]; then
        mkdir -p "$OUTPUT_DIR"
        log_info "출력 디렉토리 생성됨: $OUTPUT_DIR"
    fi
}

# SQL 스크립트 생성 및 다운로드
generate_sql_script() {
    local book_id=$1
    local count=$2
    local output_file="$OUTPUT_DIR/ledger_testdata_$(date +%Y%m%d_%H%M%S)_book${book_id}_${count}.sql"
    
    log_info "SQL 스크립트 생성 중... (가계부 ID: $book_id, 개수: $count)"
    
    curl -s -X POST \
        "$BASE_URL/api/v2/test-data/sql/ledger/generate?bookId=$book_id&count=$count" \
        -o "$output_file"
    
    if [ $? -eq 0 ] && [ -f "$output_file" ]; then
        log_success "SQL 스크립트 생성 완료: $output_file"
        log_info "MySQL에서 실행하려면: mysql -u username -p database_name < $output_file"
    else
        log_error "SQL 스크립트 생성 실패"
        exit 1
    fi
}

# 표준 50,000건 SQL 스크립트 생성
generate_standard_sql() {
    local book_id=$1
    local output_file="$OUTPUT_DIR/standard_50k_testdata_$(date +%Y%m%d_%H%M%S)_book${book_id}.sql"
    
    log_info "표준 50,000건 SQL 스크립트 생성 중... (가계부 ID: $book_id)"
    
    curl -s -X POST \
        "$BASE_URL/api/v2/test-data/sql/standard-50k?bookId=$book_id" \
        -o "$output_file"
    
    if [ $? -eq 0 ] && [ -f "$output_file" ]; then
        log_success "표준 50,000건 SQL 스크립트 생성 완료: $output_file"
        log_info "MySQL에서 실행하려면: mysql -u username -p database_name < $output_file"
    else
        log_error "표준 SQL 스크립트 생성 실패"
        exit 1
    fi
}

# 완전한 테스트 데이터 SQL 스크립트 생성
generate_complete_sql() {
    local output_file="$OUTPUT_DIR/complete_testdata_$(date +%Y%m%d_%H%M%S).sql"
    
    log_info "완전한 테스트 데이터 SQL 스크립트 생성 중..."
    
    curl -s -X POST \
        "$BASE_URL/api/v2/test-data/sql/complete/generate" \
        -o "$output_file"
    
    if [ $? -eq 0 ] && [ -f "$output_file" ]; then
        log_success "완전한 테스트 데이터 SQL 스크립트 생성 완료: $output_file"
        log_info "MySQL에서 실행하려면: mysql -u username -p database_name < $output_file"
    else
        log_error "완전한 테스트 데이터 SQL 스크립트 생성 실패"
        exit 1
    fi
}

# 직접 데이터베이스 삽입
direct_insert() {
    local book_id=$1
    local count=$2
    
    log_info "데이터베이스 직접 삽입 중... (가계부 ID: $book_id, 개수: $count)"
    
    response=$(curl -s -X POST \
        "$BASE_URL/api/v2/test-data/ledger/generate?bookId=$book_id&count=$count" \
        -H "Content-Type: application/json")
    
    if echo "$response" | grep -q '"success":true'; then
        created_count=$(echo "$response" | sed -n 's/.*"createdCount":\([0-9]*\).*/\1/p')
        log_success "테스트 데이터 직접 삽입 완료: $created_count 건"
    else
        log_error "테스트 데이터 직접 삽입 실패"
        log_error "응답: $response"
        exit 1
    fi
}

# 테스트 데이터 통계 확인
check_stats() {
    local book_id=$1
    
    log_info "테스트 데이터 통계 확인 중... (가계부 ID: $book_id)"
    
    response=$(curl -s -X GET \
        "$BASE_URL/api/v2/test-data/ledger/stats?bookId=$book_id")
    
    if echo "$response" | grep -q '"success":true'; then
        stats=$(echo "$response" | sed -n 's/.*"stats":"\([^"]*\)".*/\1/p')
        log_success "테스트 데이터 통계:"
        echo -e "$stats" | sed 's/\\n/\n/g'
    else
        log_warning "테스트 데이터 통계 확인 실패"
    fi
}

# 메인 함수
main() {
    local book_id=$DEFAULT_BOOK_ID
    local count=$DEFAULT_COUNT
    local mode="sql"  # sql, standard, complete, direct
    
    # 명령행 인수 처리
    while [[ $# -gt 0 ]]; do
        case $1 in
            -h|--help)
                show_help
                exit 0
                ;;
            -u|--url)
                BASE_URL="$2"
                shift 2
                ;;
            -b|--book-id)
                book_id="$2"
                shift 2
                ;;
            -c|--count)
                count="$2"
                shift 2
                ;;
            -o|--output)
                OUTPUT_DIR="$2"
                shift 2
                ;;
            -s|--standard)
                mode="standard"
                shift
                ;;
            -f|--full)
                mode="complete"
                shift
                ;;
            -d|--direct)
                mode="direct"
                shift
                ;;
            *)
                log_error "알 수 없는 옵션: $1"
                show_help
                exit 1
                ;;
        esac
    done
    
    # 서버 상태 확인
    check_server
    
    # 출력 디렉토리 생성 (direct 모드가 아닌 경우)
    if [ "$mode" != "direct" ]; then
        create_output_dir
    fi
    
    # 모드에 따른 실행
    case $mode in
        sql)
            generate_sql_script "$book_id" "$count"
            ;;
        standard)
            generate_standard_sql "$book_id"
            ;;
        complete)
            generate_complete_sql
            ;;
        direct)
            direct_insert "$book_id" "$count"
            ;;
    esac
    
    # 통계 확인
    check_stats "$book_id"
    
    log_success "테스트 데이터 생성 작업 완료!"
}

# 스크립트 실행
main "$@"